<script>
    import Vue from 'vue';
    import { Mixin } from 'vue-mixin-decorator';
    import bb from 'billboard.js';

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
                .attr('width', '100%')
                .attr('height', '100%')
                .attr('class', 'chart-background');
        },
        onrendered(ctx) {
            const zoomRect = ctx.$.svg.select('.bb-zoom-rect');

            ctx.$.svg.select('.chart-background')
                .attr('width', +zoomRect.attr('width'))
                .attr('height', +zoomRect.attr('height'));
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
            const duration = Vue.prototype.$moment.duration(seconds, 'seconds');
            const timeSeries = duration.format('d[d] HH:mm:ss', { forceLength: true });

            if (timeSeries.length < 3) {
                return `00:${timeSeries}`;
            } else {
                return timeSeries;
            }
        }

        processData(data, dataType) {
            let result = {};
            Object.entries(data).forEach(([key, value]) => {
                if (dataType === key) {
                    result['Total'] = value;
                    return true;
                }
                result[this.getTestDesc(dataType, key)] = value;
            });
            return result;
        }

        processUserDefinedData(userDefinedData, numOfTestRecord) {
            let dataType;
            let userDefinedDataTemp;
            let result = [];

            Object.entries(userDefinedData).forEach(([key, value], index) => {
                if (index % numOfTestRecord === 0) {
                    dataType = key;
                    userDefinedDataTemp = { title: this.getUserDefinedChartTitle(key), data: {}, };
                    userDefinedDataTemp.data['Total'] = value;
                    result[index/numOfTestRecord] = userDefinedDataTemp;
                } else {
                    userDefinedDataTemp.data[this.getTestDesc(dataType, key)] = value;
                }
            });
            return result;
        }

        getUserDefinedChartTitle(dataType) {
            const title = dataType.replace(/User_defined|_/g, ' ').trim();
            return title ? title : this.i18n('perfTest.report.header.userDefinedChart');
        }

        getTestDesc(dataType, dataFileName) {
            dataFileName = dataFileName.replace(dataType, '');
            return dataFileName.substring(dataFileName.indexOf('_') + 1)
                .replace(/_/g, ' ')
                .trim();
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
        stroke: #999999;
        stroke-width: 1px;
    }

    .bb-grid line {
        stroke: #dddddd;
    }
</style>
