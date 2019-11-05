<template>
    <div class="detail-report-monitor-menu">
        <header class="pb-2 mt-4 mb-3 border-bottom">
            <h4 v-text="'Monitor'"></h4>
        </header>

        <div v-show="optionalChart.cpuUsageChart">
            <h6 v-text="'CPU'"></h6>
            <div class="chart" ref="cpuUsageChart" id="cpu-usage-chart"></div>
        </div>

        <div v-show="optionalChart.memUsageChart">
            <h6 v-text="'Used Memory'"></h6>
            <div class="chart" id="mem-usage-chart"></div>
        </div>

        <div v-show="optionalChart.receivedBytePerSecChart">
            <h6 v-text="'Received Byte Per Second'"></h6>
            <div class="chart" id="received-byte-per-sec-chart"></div>
        </div>

        <div v-show="optionalChart.sentBytePerSecChart">
            <h6 v-text="'Sent Byte Per Second'"></h6>
            <div class="chart" id="sent-byte-per-sec-chart"></div>
        </div>

        <div v-show="optionalChart.customMonitorChart1">
            <h6 v-text="'Custom Monitor Chart 1'"></h6>
            <div class="chart" id="custom-monitor-chart-1"></div>
        </div>

        <div v-show="optionalChart.customMonitorChart2">
            <h6 v-text="'Custom Monitor Chart 2'"></h6>
            <div class="chart" id="custom-monitor-chart-2"></div>
        </div>

        <div v-show="optionalChart.customMonitorChart3">
            <h6 v-text="'Custom Monitor Chart 3'"></h6>
            <div class="chart" id="custom-monitor-chart-3"></div>
        </div>

        <div v-show="optionalChart.customMonitorChart4">
            <h6 v-text="'Custom Monitor Chart 4'"></h6>
            <div class="chart" id="custom-monitor-chart-4"></div>
        </div>

        <div v-show="optionalChart.customMonitorChart5">
            <h6 v-text="'Custom Monitor Chart 5'"></h6>
            <div class="chart" id="custom-monitor-chart-5"></div>
        </div>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import Base from '../../../Base.vue';
    import ChartMixin from '../../../common/mixin/ChartMixin.vue';
    import FormatMixin from '../../../common/mixin/FormatMixin.vue';
    import MessagesMixin from '../../../common/mixin/MessagesMixin.vue';

    @Component({
        name: 'monitor',
        props: {
            id: {
                type: [Number, String],
                required: true,
            },
            targetIP: {
                type: String,
                required: true,
            },
        },
    })
    export default class Monitor extends Mixins(Base, ChartMixin, FormatMixin, MessagesMixin) {
        optionalChart = {
            cpuUsageChart: false,
            memUsageChart: false,
            receivedBytePerSecChart: false,
            sentBytePerSecChart: false,
            customMonitorChart1: false,
            customMonitorChart2: false,
            customMonitorChart3: false,
            customMonitorChart4: false,
            customMonitorChart5: false,
        };

        mounted() {
            this.$http.get(`/perftest/api/${this.id}/monitor`, {
                params: {
                    targetIP: this.targetIP,
                    imgWidth: parseInt(this.$refs.cpuUsageChart.offsetWidth),
                },
            }).then(res => {
                const interval = res.data.interval;

                this.optionalChart.cpuUsageChart = !!this.drawChart('cpu-usage-chart', 'cpu', res.data.cpu, interval, this.formatPercentage);
                this.optionalChart.memUsageChart = !!this.drawChart('mem-usage-chart', 'memory', res.data.memory, interval, this.formatMemory);
                this.optionalChart.receivedBytePerSecChart = !!this.drawChart('received-byte-per-sec-chart', 'received', res.data.received, interval, this.formatNetwork);
                this.optionalChart.sentBytePerSecChart = !!this.drawChart('sent-byte-per-sec-chart', 'sent', res.data.sent, interval, this.formatNetwork);
                this.optionalChart.customMonitorChart1 = !!this.drawChart('custom-monitor-chart-1', 'customData1', res.data.customData1, interval, this.formatNetwork);
                this.optionalChart.customMonitorChart2 = !!this.drawChart('custom-monitor-chart-2', 'customData2', res.data.customData2, interval, this.formatNetwork);
                this.optionalChart.customMonitorChart3 = !!this.drawChart('custom-monitor-chart-3', 'customData3', res.data.customData3, interval, this.formatNetwork);
                this.optionalChart.customMonitorChart4 = !!this.drawChart('custom-monitor-chart-4', 'customData4', res.data.customData4, interval, this.formatNetwork);
                this.optionalChart.customMonitorChart5 = !!this.drawChart('custom-monitor-chart-5', 'customData5', res.data.customData5, interval, this.formatNetwork);
            }).catch(() => this.showErrorMsg(this.i18n('common.message.loading.error')));
        }
    }
</script>

<style lang="less" scoped>
    .detail-report-monitor-menu {
        div {
            &.chart {
                border: 1px solid #878988;
                height: 200px;
                min-width: 615px;
                margin-bottom: 20px;
            }
        }
    }
</style>
