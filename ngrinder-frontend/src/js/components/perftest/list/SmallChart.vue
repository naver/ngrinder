<template>
    <table class="small-chart-table">
        <tr>
            <td><div class="small-chart" :id="`tps_${rowData.id}`"></div></td>
            <td><div class="small-chart" :id="`mtt_${rowData.id}`"></div></td>
            <td><div class="small-chart" :id="`err_${rowData.id}`"></div></td>
        </tr>
    </table>
</template>

<script>
    import Component from 'vue-class-component';
    import bb from 'billboard.js';

    import Base from '../../Base.vue';


    const timeSeriesFormat = x => {
        let minutes = Math.floor(x / 60);
        let seconds = x % 60;

        if (minutes < 10) {
            minutes = `0${minutes}`;
        }
        if (seconds < 10) {
            seconds = `0${seconds}`;
        }
        return `${minutes}:${seconds}`;
    };

    const defaultChartOptions = {
        grid: {
            x: { show: true },
            y: { show: true, ticks: 5 },
        },
        point: {
            show: true,
            r: 1.0,
        },
        zoom: {
            enabled: {
                type: 'drag',
            },
        },
        padding: {
            top: 10,
            right: 16,
            left: 36,
        },
        oninit() {
            this.svg.select('g.bb-grid')
                .insert('rect', ':first-child')
                .attr('class', 'chart-background');
        },
        legend: {
            position: 'inset',
            inset: {
                anchor: 'top-right',
                x: 20,
                y: 10,
            },
        },
    };

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
            const interval = data.chartInterval;

            this.drawSmallChart(`tps_${this.rowData.id}`, 'TPS', data.TPS, interval);
            this.drawSmallChart(`mtt_${this.rowData.id}`, 'MTT', data.Mean_Test_Time_ms, interval);
            this.drawSmallChart(`err_${this.rowData.id}`, 'ERR', data.Errors, interval);

            this.$nextTick(() => {
                $('g.bb-axis.bb-axis-x text[style*="display: none;"]').siblings().css({ display: 'none' });
                $('rect.bb-zoom-rect').css({ opacity: 0.5});
            });
        }

        drawSmallChart(id, label, data, interval) {
            if (data === undefined || data.length === 0) {
                return null;
            }

            return bb.generate({
                bindto: `#${id}`,
                data: {
                    type: 'line',
                    json: { [label]: data },
                    colors: { [label]: '#4bb2c5' },
                },
                axis: {
                    x: {
                        type: 'seconds',
                        tick: {
                            culling: { max: 5 },
                            format: x => timeSeriesFormat(x * interval),
                        },
                        padding: {
                            left: 0,
                        },
                    },
                    y: {
                        min: 0,
                        tick: { culling: true },
                        padding: {
                            bottom: 0,
                        },
                    },
                },
                ...defaultChartOptions,
            });
        }
    }
</script>

<style lang="less" scoped>
    @import '~billboard.js/dist/billboard.min.css';
    @import '~billboard.js/dist/theme/insight.min.css';

    .fade-enter-active {
        transition: opacity .6s;
    }
    .fade-enter, .fade-leave-to {
        opacity: 0;
    }
</style>

<style lang="less">
    .small-chart-table {
        width: 100%;

        td {
            background-color: #f9f9f9;
        }

        div.small-chart {
            width: 290px;
            height: 150px;
            border: 1px solid #c4c4c4;
        }

        th.small-border {
            padding-left: 3px;
            padding-right: 3px;
        }
    }

    tbody > tr:not([render="true"]) > td {
        padding: 0;
    }

    .chart-background {
        fill: #fffdf6;
        fill-opacity: 1;
        width: 100%;
        height: 100%;
    }
</style>
