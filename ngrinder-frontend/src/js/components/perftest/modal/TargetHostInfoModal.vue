<template>
    <div ref="targetHostInfoModal" class="modal fade" id="target-host-info-modal">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header border-bottom align-content-center">
                    <h4 v-text="i18n('monitor.info.header')"></h4>
                    <span class="ml-1" v-text="`( ${ip} )`"></span>
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                </div>
                <div class="modal-body">
                    <div>
                        <h5 v-text="i18n('monitor.info.cpu')"></h5>
                        <div class="chart" id="cpu-usage-chart"></div>
                        <h5 v-text="i18n('monitor.info.memory')"></h5>
                        <div class="chart" id="memory-usage-chart"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import ModalBase from '../../common/modal/ModalBase.vue';
    import ChartMixin from '../../common/mixin/ChartMixin.vue';
    import FormatMixin from '../../common/mixin/FormatMixin.vue';
    import Queue from '../../../queue.js';

    @Component({
        name: 'targetHostInfoModal',
        props: {
            ip: {
                type: String,
            },
        },
    })
    export default class TargetHostInfoModal extends Mixins(ModalBase, ChartMixin, FormatMixin) {
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

                this.cpu.chart = this.drawChart('cpu-usage-chart', { 'cpu-usage': this.cpu.queue.getArray() }
                    , this.INTERVAL, this.formatPercentage, { transition: { duration: null } });
                this.memory.chart = this.drawChart('memory-usage-chart', { 'memory-usage': this.memory.queue.getArray() }
                    , this.INTERVAL, this.formatMemory, { transition: { duration: null } });

                this.currentIntervalId = setInterval(this.getState, this.INTERVAL * 1000);
            });

            $(this.$refs.targetHostInfoModal).on('hide.bs.modal', () => {
                clearInterval(this.currentIntervalId);
                this.closeMonitorConnection();
            });
        }

        getState() {
            this.$http.get('/monitor/api/state', {
                params: {
                    ip: this.ip,
                },
            }).then(res => {
                this.cpu.queue.enQueue(res.data.cpuUsedPercentage);
                this.memory.queue.enQueue(res.data.totalMemory - res.data.freeMemory);
                this.cpu.chart.load({ json: { 'cpu-usage': this.cpu.queue.getArray() } });
                this.memory.chart.load({ json: { 'memory-usage': this.memory.queue.getArray() } });
            });
        }

        closeMonitorConnection() {
            this.$http.get('/monitor/api/close', {
                params: {
                    ip: this.ip
                },
            });
        }
    }
</script>

<style lang="less" scoped>
    #target-host-info-modal {
        .modal-body {
            padding: 20px;
            max-height: 1200px;
        }

        .modal-content {
            width: 600px;
        }

        .modal-header {
            span {
                margin-top: 2px;
            }
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
