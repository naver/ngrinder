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

    const timeSeriesTickArray = [];
    for (let i = 0; i < 60; i++) {
        timeSeriesTickArray.push(i);
    }

    const defaultChartOptions = {
        grid: {
            x: { show: true },
            y: { show: true },
        },
        point: {
            show: false,
        },
        zoom: {
            enabled: {
                type: 'drag',
            },
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
                    type: 'area',
                    json: { [label]: approximateFillUp(data) },
                },
                axis: {
                    x: {
                        type: 'seconds',
                        tick: {
                            values: timeSeriesTickArray,
                            format: x => timeSeriesFormat(x * interval),
                        },
                    },
                    y: {
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
