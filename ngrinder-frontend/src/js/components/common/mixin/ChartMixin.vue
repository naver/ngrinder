<script>
    import { Mixin } from 'vue-mixin-decorator';
    import bb from 'billboard.js';
    import moment from 'moment';
    import momentDurationFormatSetup from 'moment-duration-format';

    momentDurationFormatSetup(moment);

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
        legend: {
            position: 'inset',
            inset: {
                anchor: 'top-right',
                x: 20,
                y: 10,
            },
        },
        transition: {
            duration: null,
        },
        line: {
            connectNull: true,
        },
        padding: {
            top: 10,
            right: 16,
            left: 58,
        },
        oninit() {
            this.svg.select('g.bb-grid')
                .insert('rect', ':first-child')
                .attr('class', 'chart-background');
        },
    };

    @Mixin
    export default class ChartMixin {
        static DEFAULT_COLOR = '#4bb2c5';

        drawChart(id, data, interval, yAxisFormatter, externalOptions) {
            if (Object.keys(data).length === 0 || Object.values(data).flatMap(x => x).length === 0) {
                return null;
            }

            let colorsKey;
            if (Object.keys(data).length === 1) {
                colorsKey = Object.keys(data)[0];
            } else {
                colorsKey = 'Total';
            }

            this.$nextTick(() => {
                $('g.bb-axis.bb-axis-x text[style*="display: none;"]').siblings().css({ display: 'none' });
                $('rect.bb-zoom-rect').css({ opacity: 1 });
            });

            return bb.generate({
                bindto: `#${id}`,
                data: {
                    json: data,
                    colors: { [colorsKey]: ChartMixin.DEFAULT_COLOR },
                },
                axis: {
                    x: {
                        type: 'seconds',
                        tick: {
                            culling: true,
                            format: x => ChartMixin.timeSeriesFormat(x * interval),
                            text: {
                                position: { y: 10 },
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
                            format: yAxisFormatter,
                            text: {
                                position: { x: -5 },
                            },
                        },
                        padding: {
                            bottom: 0,
                        },
                    },
                },
                ...defaultChartOptions,
                ...externalOptions,
            });
        }

        static timeSeriesFormat(seconds) {
            const duration = moment.duration(seconds, 'seconds');
            const timeSeries = duration.format('d[d] hh:mm:ss', { forceLength: true });

            if (timeSeries.length < 3) {
                return `00:${timeSeries}`;
            } else {
                return timeSeries;
            }
        }
    }
</script>

<style scoped>
    @import '~billboard.js/dist/billboard.min.css';
    @import '~billboard.js/dist/theme/insight.min.css';
</style>

<style>
    .chart-background {
        fill: #fffdf6;
        fill-opacity: 1;
        width: 100%;
        height: 100%;
        border: 2px solid #c4c4c4;
    }

    .bb-grid line {
        stroke: #dddddd;
    }
</style>