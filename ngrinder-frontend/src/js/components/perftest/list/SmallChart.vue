<template>
    <table class="small-chart-table">
        <tr>
            <td><div class="small-chart" :id="`tps_${rowData.id}`"></div></td>
            <td><div class="small-chart" :id="`mtt_${rowData.id}`"></div></td>
            <td><div class="small-chart" :id="`mttfb_${rowData.id}`"></div></td>
            <td><div class="small-chart" :id="`err_${rowData.id}`"></div></td>
        </tr>
    </table>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import bb from 'billboard.js';

    import Base from '../../Base.vue';
    import ChartMixin from '../../common/mixin/ChartMixin.vue';

    const defaultChartOptions = {
        grid: {
            x: { show: true },
            y: { show: true, ticks: 5 },
        },
        point: { show: false },
        zoom: {
            enabled: {
                type: 'drag',
            },
        },
        legend: {
            position: 'inset',
            inset: {
                anchor: 'bottom-right',
                x: 20,
                y: 50,
            },
            item: {
                onclick() { }, // disable default click event
            },
        },
        line: {
            connectNull: true,
        },
        padding: {
            top: 10,
            right: 16,
            left: 36,
        },
        tooltip: {
            contents: {
                template: '<span class={=CLASS_TOOLTIP}>{{<span>{=VALUE}</span></li>}}</span>',
            },
        },
        oninit() {
            this.svg.select('g.bb-grid')
                .insert('rect', ':first-child')
                .attr('width', '100%')
                .attr('height', '100%')
                .attr('class', 'chart-background');
            this.svg.select('g.bb-legend-item > text')
                .attr('y', 17);

            if (this.data.targets.length === 0) {
                this.svg.select('g.bb-grid')
                    .insert('text', ':last-child')
                    .attr('class', 'bb-text bb-empty empty-chart')
                    .attr('text-anchor', 'middle')
                    .attr('dominant-baseline', 'middle')
                    .text('No Data');
                this.config.axis_y_tick_count = 1;
                this.config.axis_y_tick_culling = false;
            }
        },
        onrendered(ctx) {
            const zoomRect = ctx.$.svg.select('.bb-zoom-rect');

            ctx.$.svg.select('.chart-background')
                .attr('width', +zoomRect.attr('width'))
                .attr('height', +zoomRect.attr('height'));

            ctx.$.svg.select('.empty-chart')
                .attr('x', zoomRect.attr('width') / 2)
                .attr('y', zoomRect.attr('height') / 2);
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
    export default class SmallChart extends Mixins(Base, ChartMixin) {
        created() {
            this.showChart();
        }

        showChart() {
            this.$http.get(`/perftest/api/${this.rowData.id}/graph`, {
                params: {
                    dataType: 'TPS,Errors,Mean_Test_Time_(ms),Mean_time_to_first_byte',
                    imgWidth: 100,
                    onlyTotal: true,
                },
            }).then(res => {
                const interval = res.data['chartInterval'];
                Object.entries(res.data).forEach(([key, value]) => {
                    res.data[key] = this.processData(value, key);
                });
                this.initCharts(res.data, interval);
            });
        }

        initCharts(data, interval) {
            this.drawSmallChart(`tps_${this.rowData.id}`, 'TPS', data['TPS'].Total, interval);
            this.drawSmallChart(`mtt_${this.rowData.id}`, 'MTT', data['Mean_Test_Time_(ms)'].Total, interval);
            this.drawSmallChart(`mttfb_${this.rowData.id}`, 'MTTFB', data['Mean_time_to_first_byte'].Total, interval);
            this.drawSmallChart(`err_${this.rowData.id}`, 'ERR', data['Errors'].Total, interval);

            this.$nextTick(() => {
                $('g.bb-axis.bb-axis-x text[style*="display: none;"]').siblings().css({ display: 'none' });
            });
        }

        drawSmallChart(id, label, data, interval) {
            return bb.generate({
                bindto: `#${id}`,
                data: {
                    json: { [label]: data || [] },
                    colors: { [label]: ChartMixin.DEFAULT_COLOR },
                },
                axis: {
                    x: {
                        type: 'seconds',
                        tick: {
                            count: 5,
                            format: x => ChartMixin.timeSeriesFormat(x * interval),
                            text: {
                                position: { y: 8 },
                            },
                        },
                        padding: {
                            left: 0,
                            right: 0,
                        },
                    },
                    y: {
                        min: 0,
                        tick: {
                            culling: true,
                            text: {
                                position: { x: -2 },
                            },
                        },
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
            background-color: #ffffff;
        }

        div.small-chart {
            width: 280px;
            height: 150px;
            border: 1px solid #c4c4c4;
            background-color: #ffffff;
        }

        th.small-border {
            padding-left: 3px;
            padding-right: 3px;
        }

        rect.chart-background {
            fill: #fffdf6;
            stroke: #999999;
            stroke-width: 1px;
        }

        g.bb-legend-item {
            text {
                font-size: 0.8em;
            }
        }

        g.tick > text > tspan {
            font-size: 8px;
        }

        tbody > tr:not([render="true"]) > td {
            padding: 0;
        }
    }

    .bb-tooltip {
        padding: 3px;
    }
</style>
