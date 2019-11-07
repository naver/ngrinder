<script>
    import { Mixin } from 'vue-mixin-decorator';
    import bb from 'billboard.js';

    const DEFAULT_COLOR = '#4bb2c5';

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

    const approximateFillUpArray = array => {
        for (let i = 0; i < array.length; i++) {
            if (i === 0 || i === array.length - 1) {
                continue;
            }

            if (array[i] === null) {
                array[i] = (array[i - 1] + array[i + 1]) / 2;
            }
        }
        return array;
    };

    const approximateFillUp = data => {
        for (const [key, value] of Object.entries(data)) {
            data[key] = approximateFillUpArray(value);
        }

        return data;
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
        drawChart(id, data, interval, yAxisFormatter, externalOptions) {
            if (Object.keys(data).length === 0) {
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
                    type: 'line',
                    json: approximateFillUp(data),
                    colors: { [colorsKey]: DEFAULT_COLOR },
                },
                axis: {
                    x: {
                        type: 'seconds',
                        tick: {
                            culling: true,
                            format: x => timeSeriesFormat(x * interval),
                        },
                        padding: {
                            left: 0,
                        },
                    },
                    y: {
                        min: 0,
                        tick: {
                            culling: true,
                            format: yAxisFormatter,
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
