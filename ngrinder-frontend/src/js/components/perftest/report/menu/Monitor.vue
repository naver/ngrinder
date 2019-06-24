<template>
    <div id="detail-report-monitor-menu">
        <div class="page-header page-header">
            <h4 v-text="'Monitor'"></h4>
        </div>

        <template v-if="optionalChart.cpuUsageChart">
            <h6 v-text="'CPU'"></h6>
            <div class="chart" ref="cpuUsageChart" id="cpu-usage-chart"></div>
        </template>

        <template v-if="optionalChart.memUsageChart">
            <h6 v-text="'Used Memory'"></h6>
            <div class="chart" id="mem-usage-chart"></div>
        </template>

        <template v-if="optionalChart.receivedBytePerSecChart">
            <h6 v-text="'Received Byte Per Second'"></h6>
            <div class="chart" id="received-byte-per-sec-chart"></div>
        </template>

        <template v-if="optionalChart.sentBytePerSecChart">
            <h6 v-text="'Sent Byte Per Second'"></h6>
            <div class="chart" id="sent-byte-per-sec-chart"></div>
        </template>

        <template v-if="optionalChart.customMonitorChart1">
            <h6 v-text="'Custom Monitor Chart 1'"></h6>
            <div class="chart" id="custom-monitor-chart-1"></div>
        </template>

        <template v-if="optionalChart.customMonitorChart2">
            <h6 v-text="'Custom Monitor Chart 2'"></h6>
            <div class="chart" id="custom-monitor-chart-2"></div>
        </template>

        <template v-if="optionalChart.customMonitorChart3">
            <h6 v-text="'Custom Monitor Chart 3'"></h6>
            <div class="chart" id="custom-monitor-chart-3"></div>
        </template>

        <template v-if="optionalChart.customMonitorChart4">
            <h6 v-text="'Custom Monitor Chart 4'"></h6>
            <div class="chart" id="custom-monitor-chart-4"></div>
        </template>

        <template v-if="optionalChart.customMonitorChart5">
            <h6 v-text="'Custom Monitor Chart 5'"></h6>
            <div class="chart" id="custom-monitor-chart-5"></div>
        </template>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import Base from '../../../Base.vue';
    import MenuChartMixin from './MenuChartMixin.vue';
    import FormatMixin from '../../mixin/FormatMixin.vue';
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
    export default class Monitor extends Mixins(Base, MenuChartMixin, FormatMixin, MessagesMixin) {
        optionalChart = {
            cpuUsageChart: true,
            memUsageChart: true,
            receivedBytePerSecChart: true,
            sentBytePerSecChart: true,
            customMonitorChart1: true,
            customMonitorChart2: true,
            customMonitorChart3: true,
            customMonitorChart4: true,
            customMonitorChart5: true,
        };

        mounted() {
            this.$http.get(`/perftest/api/${this.id}/monitor`, {
                params: {
                    targetIP: this.targetIP,
                    imgWidth: parseInt(this.$refs.cpuUsageChart.offsetWidth),
                },
            }).then(res => {
                const interval = res.data.interval;
                this.drawOptionalChart('cpu-usage-chart', [res.data.cpu], interval,
                    { yAxisFormatter: this.formatPercentage }, { displayFlags: this.optionalChart, key: 'cpuUsageChart' });
                this.drawOptionalChart('mem-usage-chart', [res.data.memory], interval,
                    { yAxisFormatter: this.formatMemory }, { displayFlags: this.optionalChart, key: 'memUsageChart' });
                this.drawOptionalChart('received-byte-per-sec-chart', [res.data.received], interval,
                    { yAxisFormatter: this.formatNetwork }, { displayFlags: this.optionalChart, key: 'receivedBytePerSecChart' });
                this.drawOptionalChart('sent-byte-per-sec-chart', [res.data.sent], interval,
                    { yAxisFormatter: this.formatNetwork }, { displayFlags: this.optionalChart, key: 'sentBytePerSecChart' });
                this.drawOptionalChart('custom-monitor-chart-1', [res.data.customData1], interval,
                    { yAxisFormatter: this.formatNetwork }, { displayFlags: this.optionalChart, key: 'customMonitorChart1' });
                this.drawOptionalChart('custom-monitor-chart-2', [res.data.customData2], interval,
                    { yAxisFormatter: this.formatNetwork }, { displayFlags: this.optionalChart, key: 'customMonitorChart2' });
                this.drawOptionalChart('custom-monitor-chart-3', [res.data.customData3], interval,
                    { yAxisFormatter: this.formatNetwork }, { displayFlags: this.optionalChart, key: 'customMonitorChart3' });
                this.drawOptionalChart('custom-monitor-chart-4', [res.data.customData4], interval,
                    { yAxisFormatter: this.formatNetwork }, { displayFlags: this.optionalChart, key: 'customMonitorChart4' });
                this.drawOptionalChart('custom-monitor-chart-5', [res.data.customData5], interval,
                    { yAxisFormatter: this.formatNetwork }, { displayFlags: this.optionalChart, key: 'customMonitorChart5' });
                this.createChartExportButton(this.i18n('perfTest.report.exportImg.button'), this.i18n('perfTest.report.exportImg.title'));
            }).catch(() => this.showErrorMsg(this.i18n('common.message.loading.error')));
        }
    }
</script>

<style lang="less">

</style>

<style lang="less" scoped>
    #detail-report-monitor-menu {
        div {
            &.chart {
                border: 1px solid #878988;
                height: 200px;
                min-width: 615px;
            }
        }
    }
</style>
