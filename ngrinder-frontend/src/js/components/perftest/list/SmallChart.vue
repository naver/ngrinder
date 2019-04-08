<template>
    <transition name="fade">
        <tr class="small-chart-container" v-show="displayChart">
            <td class="no-padding" colspan="12">
                <table>
                    <tr>
                        <td><div class="small-chart" :id="`tps_${perfTestId}`"></div></td>
                        <td><div class="small-chart" :id="`mtt_${perfTestId}`"></div></td>
                        <td><div class="small-chart" :id="`err_${perfTestId}`"></div></td>
                    </tr>
                </table>
            </td>
        </tr>
    </transition>
</template>

<script>
    import Base from '../../Base.vue';
    import Component from 'vue-class-component';
    import Chart from '../../../chart.js';

    @Component({
        name: 'smallChart',
        props: {
            perfTestId: {
                type: Number,
                required: true,
            },
        },
    })
    export default class SmallChart extends Base {
        CHART_GRID_PADDING = { top: 15, right: 10, bottom: 30, left: 40 };
        CHART_NUM_X_TICKS = 7;
        CHART_LEGEND_MARGIN = 1;
        CHART_LEGEND_LOCATION = 'nw';

        displayChart = false;

        showChart() {
            this.$http.get(`/perftest/api/${this.perfTestId}/graph`, {
                params: {
                    dataType: 'TPS,Errors,Mean_Test_Time_(ms),Mean_time_to_first_byte,User_defined',
                    imgWidth: 100,
                    onlyTotal : true,
                },
            }).then(res => {
                this.initCharts(res.data);
            }).catch((error) => console.log(error));
        }

        initCharts(data) {
            if (data.TPS.labels.length >= 1) {
                data.TPS.labels[0] = "TPS";
            }
            this.makeNewChart(`tps_${this.perfTestId}`, data.TPS.data, data.chartInterval, data.TPS.labels);

            if (data.Mean_Test_Time_ms.labels.length >= 1) {
                data.Mean_Test_Time_ms.labels[0] = "MTT";
            }
            this.makeNewChart(`mtt_${this.perfTestId}`, data.Mean_Test_Time_ms.data, data.chartInterval, data.Mean_Test_Time_ms.labels);

            if (data.Errors.labels.length >= 1) {
                data.Errors.labels[0] = "ERR";
            }
            this.makeNewChart(`err_${this.perfTestId}`, data.Errors.data, data.chartInterval, data.Errors.labels);
        }

        makeNewChart(id, data, interval, labels) {
            new Chart(id, data, interval, {
                    labels: labels,
                    gridPadding: this.CHART_GRID_PADDING,
                    numXTicks: this.CHART_NUM_X_TICKS,
                    legend_margin: this.CHART_LEGEND_MARGIN,
                    legend_location : this.CHART_LEGEND_LOCATION,
                }).plot();
        }

        toggleDisplay() {
            this.displayChart = !this.displayChart;
            if (this.displayChart) {
                this.showChart();
            }
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

    .small-chart-container {
        .jqplot-table-legend {
            width: 20px;
            height: 16px;
            padding-bottom: 0;
        }

        table {
            width: 100%;
            &.jqplot-table-legend {
                left: 32px !important;
                top: 17px !important;
            }

            div.small-chart {
                border: 1px solid #878988;
                height: 150px;
                min-width: 290px;
            }

            th.small-border {
                padding-left:3px;
                padding-right:3px;
            }
        }
    }
</style>
