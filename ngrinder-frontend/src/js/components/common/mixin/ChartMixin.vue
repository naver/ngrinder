<script>
    import { Mixin } from 'vue-mixin-decorator';
    import bb from 'billboard.js';

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

    const approximateFillUp = array => {
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
            show: false,
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
        drawChart(id, label, data, interval, yAxisFormatter) {
            if (data === undefined || data.length === 0) {
                return null;
            }

            return bb.generate({
                bindto: `#${id}`,
                data: {
                    type: 'line',
                    json: { [label]: approximateFillUp(data) },
                    colors: { [label]: '#4bb2c5' },
                },
                axis: {
                    x: {
                        type: 'seconds',
                        tick: {
                            culling: true,
                            format: x => timeSeriesFormat(x * interval),
                        },
                    },
                    y: {
                        min: 0,
                        tick: {
                            culling: true,
                            format: yAxisFormatter,
                        },
                    },
                },
                ...defaultChartOptions,
            });
        }
    }
</script>

<style scoped>
    @import '../../../../../node_modules/billboard.js/dist/billboard.min.css';
    @import '../../../../../node_modules/billboard.js/dist/theme/insight.min.css';
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
