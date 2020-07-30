<template>
    <div class="detail-report-monitor-menu">
        <header class="pb-2 mt-4 mb-3 border-bottom">
            <h4 v-text="'Monitor'"></h4>
        </header>

        <div v-if="interval">
            <div v-show="cpuUsageChart">
                <h6 v-text="'CPU'"></h6>
                <div class="chart" id="cpu-usage-chart"></div>
            </div>

            <div v-show="memUsageChart">
                <h6 v-text="'Used Memory'"></h6>
                <div class="chart" id="mem-usage-chart"></div>
            </div>

            <div v-show="receivedBytePerSecChart">
                <h6 v-text="'Received Byte Per Second'"></h6>
                <div class="chart" id="received-byte-per-sec-chart"></div>
            </div>

            <div v-show="sentBytePerSecChart">
                <h6 v-text="'Sent Byte Per Second'"></h6>
                <div class="chart" id="sent-byte-per-sec-chart"></div>
            </div>

            <div v-show="customMonitorChart1">
                <h6 v-text="'Custom Monitor Chart 1'"></h6>
                <div class="chart" id="custom-monitor-chart-1"></div>
            </div>

            <div v-show="customMonitorChart2">
                <h6 v-text="'Custom Monitor Chart 2'"></h6>
                <div class="chart" id="custom-monitor-chart-2"></div>
            </div>

            <div v-show="customMonitorChart3">
                <h6 v-text="'Custom Monitor Chart 3'"></h6>
                <div class="chart" id="custom-monitor-chart-3"></div>
            </div>

            <div v-show="customMonitorChart4">
                <h6 v-text="'Custom Monitor Chart 4'"></h6>
                <div class="chart" id="custom-monitor-chart-4"></div>
            </div>

            <div v-show="customMonitorChart5">
                <h6 v-text="'Custom Monitor Chart 5'"></h6>
                <div class="chart" id="custom-monitor-chart-5"></div>
            </div>
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
        cpuUsageChart = {};
        memUsageChart = {};
        receivedBytePerSecChart = {};
        sentBytePerSecChart = {};
        customMonitorChart1 = {};
        customMonitorChart2 = {};
        customMonitorChart3 = {};
        customMonitorChart4 = {};
        customMonitorChart5 = {};

        interval = 0;

        mounted() {
            this.showProgressBar();
            this.$http.get(`/perftest/api/${this.id}/monitor`, {
                params: {
                    targetIP: this.targetIP,
                    imgWidth: 960,
                },
            }).then(res => {
                this.interval = parseInt(res.data.interval);
                if (!this.interval) {
                    return;
                }
                this.$nextTick(() => {
                    this.cpuUsageChart = this.drawChart('cpu-usage-chart', { 'cpu': res.data.cpu }, this.interval, this.formatPercentage);
                    this.memUsageChart = this.drawChart('mem-usage-chart', { 'memory': res.data.memory }, this.interval, this.formatMemory);
                    this.receivedBytePerSecChart = this.drawChart('received-byte-per-sec-chart', { 'received': res.data.received }, this.interval, this.formatNetwork);
                    this.sentBytePerSecChart = this.drawChart('sent-byte-per-sec-chart', { 'sent': res.data.sent }, this.interval, this.formatNetwork);
                    this.customMonitorChart1 = this.drawChart('custom-monitor-chart-1', { 'customData1': res.data.customData1 }, this.interval, this.formatNetwork);
                    this.customMonitorChart2 = this.drawChart('custom-monitor-chart-2', { 'customData2': res.data.customData2 }, this.interval, this.formatNetwork);
                    this.customMonitorChart3 = this.drawChart('custom-monitor-chart-3', { 'customData3': res.data.customData3 }, this.interval, this.formatNetwork);
                    this.customMonitorChart4 = this.drawChart('custom-monitor-chart-4', { 'customData4': res.data.customData4 }, this.interval, this.formatNetwork);
                    this.customMonitorChart5 = this.drawChart('custom-monitor-chart-5', { 'customData5': res.data.customData5 }, this.interval, this.formatNetwork);
                })
            }).catch(() => this.showErrorMsg(this.i18n('common.message.loading.error')))
              .finally(this.hideProgressBar);
        }
    }
</script>

<style lang="less" scoped>
    .detail-report-monitor-menu {
        div {
            &.chart {
                border: 1px solid #c4c4c4;
                height: 200px;
                width: 960px;
                margin-bottom: 20px;
            }
        }
    }
</style>
