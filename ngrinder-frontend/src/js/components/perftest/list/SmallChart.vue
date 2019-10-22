<template>
    <table class="small-chart-table">
        <tr>
            <td><div style="width: 280px; height: 150px;" class="small-chart" :id="`tps_${rowData.id}`"></div></td>
            <td><div style="width: 280px; height: 150px;" class="small-chart" :id="`mtt_${rowData.id}`"></div></td>
            <td><div style="width: 280px; height: 150px;" class="small-chart" :id="`err_${rowData.id}`"></div></td>
        </tr>
    </table>
</template>

<script>
    import Component from 'vue-class-component';
    import Base from '../../Base.vue';
    import Chart from '../../../chart.js';

    @Component({
        name: 'smallChart',
        props: {
            rowData: {
                type: Object,
                required: true,
            },
            rowIndex: {
                type: Number,
            },
        },
    })
    export default class SmallChart extends Base {
        CHART_GRID_PADDING = { top: 15, right: 10, bottom: 30, left: 40 };
        CHART_NUM_X_TICKS = 7;
        CHART_LEGEND_MARGIN = 1;
        CHART_LEGEND_LOCATION = 'nw';

        created() {
            this.showChart();
        }

        showChart() {
            this.$http.get(`/perftest/api/${this.rowData.id}/graph`, {
                params: {
                    dataType: 'TPS,Errors,Mean_Test_Time_(ms),Mean_time_to_first_byte,User_defined',
                    imgWidth: 100,
                    onlyTotal: true,
                },
            }).then(res => this.initCharts(res.data));
        }

        initCharts(data) {
            if (data.TPS.labels.length >= 1) {
                data.TPS.labels[0] = 'TPS';
            }
            this.makeNewChart(`tps_${this.rowData.id}`, data.TPS.data, data.chartInterval, data.TPS.labels);

            if (data.Mean_Test_Time_ms.labels.length >= 1) {
                data.Mean_Test_Time_ms.labels[0] = 'MTT';
            }
            this.makeNewChart(`mtt_${this.rowData.id}`, data.Mean_Test_Time_ms.data, data.chartInterval, data.Mean_Test_Time_ms.labels);

            if (data.Errors.labels.length >= 1) {
                data.Errors.labels[0] = 'ERR';
            }
            this.makeNewChart(`err_${this.rowData.id}`, data.Errors.data, data.chartInterval, data.Errors.labels);
        }

        makeNewChart(id, data, interval, labels) {
            new Chart(id, data, interval, {
                    labels,
                    gridPadding: this.CHART_GRID_PADDING,
                    numXTicks: this.CHART_NUM_X_TICKS,
                    legend_margin: this.CHART_LEGEND_MARGIN,
                    legend_location: this.CHART_LEGEND_LOCATION,
                }).plot();
        }
    }
</script>

<style lang="less" scoped>
    .fade-enter-active {
        transition: opacity .6s;
    }
    .fade-enter, .fade-leave-to {
        opacity: 0;
    }
</style>

<style lang="less">
    @import '../../../../plugins/jqplot/css/jquery.jqplot.min.css';

    .small-chart-table {
        width: 100%;

        &.jqplot-table-legend {
            left: 32px !important;
            top: 17px !important;
            width: 20px;
            height: 16px;
            padding-bottom: 0;
        }

        div.small-chart {
            border: 1px solid #878988;
            height: 150px;
            width: 289px;
        }

        th.small-border {
            padding-left: 3px;
            padding-right: 3px;
        }
    }

</style>
