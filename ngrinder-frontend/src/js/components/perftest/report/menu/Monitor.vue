<template>
    <div class="detail-report-monitor-menu">
        <header class="pb-2 mt-4 mb-3 border-bottom">
            <h4 v-text="'Monitor'"></h4>
        </header>

        <div v-show="cpuUsageChart">
            <h6 v-text="'CPU(%)，建议值：小于75%'"></h6>
            <div class="chart" ref="cpuUsageChart" id="cpu-usage-chart"></div>
        </div>

        <div v-show="memUsageChart">
            <h6 v-text="'Memory(%)，建议值：小于80%'"></h6>
            <div class="chart" id="mem-usage-chart"></div>
        </div>

        <div v-show="receivedBytePerSecChart">
            <h6 v-text="'Received (byte/s)'"></h6>
            <div class="chart" id="received-byte-per-sec-chart"></div>
        </div>

        <div v-show="sentBytePerSecChart">
            <h6 v-text="'Sent (byte/s)'"></h6>
            <div class="chart" id="sent-byte-per-sec-chart"></div>
        </div>


        <div v-show="diskBusyChart">
            <h6 v-text="'Disk IO_util(%)'"></h6>
            <div class="chart" id="diskbusy_chart"></div>
        </div>
        <div v-show="readBytePerSecChart">
            <h6 v-text="'Disk Read (byte/s)'"></h6>
            <div class="chart" id="read_byte_per_sec_chart"></div>
        </div>
        <div v-show="writeBytePerSecChart">
            <h6 v-text="'Disk Write (byte/s)'"></h6>
            <div class="chart" id="write_byte_per_sec_chart"></div>
        </div>
        <div v-show="loadChart">
            <h6 v-text="'Load-average(one-minute)'"></h6>
            <div class="chart" id="load_chart"></div>
        </div>
        <div v-show="cpuWaitChart">
            <h6 v-text="'CPU_Wait(%)'"></h6>
            <div class="chart" id="cpuwait_chart"></div>
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
</template>

<script>
    import {Mixins} from 'vue-mixin-decorator';
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

        diskBusyChart = {};
        readBytePerSecChart = {};
        writeBytePerSecChart = {};
        loadChart = {};
        cpuWaitChart = {};

        customMonitorChart1 = {};
        customMonitorChart2 = {};
        customMonitorChart3 = {};
        customMonitorChart4 = {};
        customMonitorChart5 = {};

        mounted() {
            this.$http.get(`/perftest/api/${this.id}/monitor`, {
                params: {
                    targetIP: this.targetIP,
                    imgWidth: parseInt(this.$refs.cpuUsageChart.offsetWidth),
                },
            }).then(res => {
                const interval = res.data.interval;

                this.cpuUsageChart = this.drawChart('cpu-usage-chart', {'cpu': res.data.cpu}, interval, this.formatPercentage);
                this.memUsageChart = this.drawChart('mem-usage-chart', {'memused': res.data.memused}, interval, this.formatPercentage);
                this.receivedBytePerSecChart = this.drawChart('received-byte-per-sec-chart', {'received': res.data.received}, interval, this.formatNetwork);
                this.sentBytePerSecChart = this.drawChart('sent-byte-per-sec-chart', {'sent': res.data.sent}, interval, this.formatNetwork);

                //add by lingj
                this.diskBusyChart = this.drawChart('diskbusy_chart', {'diskbusy': res.data.diskbusy}, interval, this.formatPercentage);
                this.readBytePerSecChart = this.drawChart('read_byte_per_sec_chart', {'read': res.data.read}, interval, this.formatNetwork);
                this.writeBytePerSecChart = this.drawChart('write_byte_per_sec_chart', {'write': res.data.write}, interval, this.formatNetwork);
                this.loadChart = this.drawChart('load_chart', {'load': res.data.load}, interval, this.formatNetwork);
                this.cpuWaitChart = this.drawChart('cpuwait_chart', {'cpuwait': res.data.cpuwait}, interval, this.formatPercentage);

                this.customMonitorChart1 = this.drawChart('custom-monitor-chart-1', {'customData1': res.data.customData1}, interval, this.formatNetwork);
                this.customMonitorChart2 = this.drawChart('custom-monitor-chart-2', {'customData2': res.data.customData2}, interval, this.formatNetwork);
                this.customMonitorChart3 = this.drawChart('custom-monitor-chart-3', {'customData3': res.data.customData3}, interval, this.formatNetwork);
                this.customMonitorChart4 = this.drawChart('custom-monitor-chart-4', {'customData4': res.data.customData4}, interval, this.formatNetwork);
                this.customMonitorChart5 = this.drawChart('custom-monitor-chart-5', {'customData5': res.data.customData5}, interval, this.formatNetwork);
            }).catch(() => this.showErrorMsg(this.i18n('common.message.loading.error')));
        }
    }
</script>

<style lang="less" scoped>
    .detail-report-monitor-menu {
        div {
            &.chart {
                border: 1px solid #c4c4c4;
                height: 200px;
                min-width: 615px;
                margin-bottom: 20px;
            }
        }
    }
</style>
