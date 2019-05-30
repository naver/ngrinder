<template>
    <div ref="targetHostInfoModal" class="modal hide fade" id="target-host-info-modal"
         tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
        </div>
        <div class="modal-body">
            <div id="system_chart">
                <div class="page-header">
                    <h4 v-text="i18n('monitor.info.header')"></h4>
                    <span v-text="`( ${ip} )`"></span>
                </div>
                <h5 v-text="i18n('monitor.info.cpu')"></h5>
                <div class="chart" id="cpu-usage-chart"></div>
                <h5 v-text="i18n('monitor.info.memory')"></h5>
                <div class="chart" id="memory-usage-chart"></div>
            </div>
        </div>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import ModalBase from '../../common/modal/ModalBase.vue';

    import Chart from '../../../chart.js';
    import Queue from '../../../queue.js';

    @Component({
        name: 'targetHostInfoModal',
        props: {
            ip: {
                type: String,
            },
        },
    })
    export default class TargetHostInfoModal extends ModalBase {
        INTERVAL = 3;

        currentIntervalId = -1;
        currentHostIp = '';

        cpu = {
            chart: null,
            queue: null,
        };

        memory = {
            chart: null,
            queue: null,
        };

        mounted() {
            this.cpu.queue = new Queue(60 / this.INTERVAL);
            this.memory.queue = new Queue(60 / this.INTERVAL);
            this.setEvent();
        }

        setEvent() {
            $(this.$refs.targetHostInfoModal).on('shown.bs.modal', () => {
                if (this.currentHostIp !== this.ip) {
                    this.currentHostIp = this.ip;
                    this.cpu.queue.clear();
                    this.memory.queue.clear();
                }

                this.cpu.chart = new Chart('cpu-usage-chart', [this.cpu.queue.getArray()]
                    , this.INTERVAL, { yAxisFormatter: this.formatPercentage }).plot();
                this.memory.chart = new Chart('memory-usage-chart', [this.memory.queue.getArray()]
                    , this.INTERVAL, { yAxisFormatter: this.formatMemory }).plot();

                this.currentIntervalId = setInterval(this.getState, this.INTERVAL * 1000);
            });

            $(this.$refs.targetHostInfoModal).on('hide.bs.modal', () => clearInterval(this.currentIntervalId));
        }

        getState() {
            this.$http.get(`/monitor/api/state`, {
                params: {
                    ip: this.ip,
                },
            }).then(res => {
                this.cpu.queue.enQueue(res.data.cpuUsedPercentage);
                this.memory.queue.enQueue(res.data.totalMemory - res.data.freeMemory);
                this.cpu.chart.plot();
                this.memory.chart.plot();
            }).catch((error) => console.log(error));
        }

        formatMemory(format, value) {
            value = value || 0;
            if (value < 1024) {
                return `${value.toFixed(1)}K `;
            } else if (value < 1048576) { //1024 * 1024
                return `${(value / 1024).toFixed(1)}M `;
            } else {
                return `${(value / 1048576).toFixed(2)}G `;
            }
        }

        formatPercentage(format, value) {
            value = value || 0;
            if (value < 10) {
                return `${value.toFixed(1)}% `;
            } else {
                return `${value.toFixed(0)}% `;
            }
        }
    }
</script>

<style lang="less" scoped>
    #target-host-info-modal {
        width: 580px;

        .modal-body {
            max-height: 1200px;
            padding-left: 30px;
        }

        .modal-header {
            border: none;
        }

        .page-header {
            h4 {
                display: inline;
                margin-right: 1px;
            }
        }

        .chart {
            border: 1px solid #666;
            position: relative;
            padding: 5px;
            margin-bottom: 12px;
            min-width: 510px;
            height: 250px;
        }
    }
</style>
