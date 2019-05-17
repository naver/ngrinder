<template>
    <div class="span6 intro" id="ramp-up-container">
        <fieldset>
            <legend>
                <input type="checkbox" id="use-ramp-up" name="useRampUp" v-model="enableRampUp" :checked="test.useRampUp"/>
                <span v-text="i18n('perfTest.config.rampUp.enable')"></span>
                <select id="ramp_up_type" class="span2 pull-right" name="rampUpType" :disabled="!enableRampUp" v-model="test.rampUpType" @change="updateRampUpChart">
                    <option v-for="rampUpType in rampUpTypes" :value="rampUpType" v-text="i18n(`perfTest.config.rampUp.${rampUpType.toLowerCase()}`)"></option>
                </select>
            </legend>
        </fieldset>
        <div class="form-horizontal form-horizontal-2 ramp-up-config-container">
            <template v-for="i in 1">
                <div class="control-group">
                    <div class="row">
                        <div class="span3">
                            <input-label name="rampUpInitCount"
                                         v-model="test.rampUpInitCount"
                                         ref="rampUpConfig"
                                         message="perfTest.config.rampUp.initialCount">
                            </input-label>
                        </div>
                        <div class="span3">
                            <input-label name="rampUpStep"
                                         v-model="test.rampUpStep"
                                         ref="rampUpConfig"
                                         message="perfTest.config.rampUp.step">
                            </input-label>
                        </div>
                    </div>
                    <div class="row">
                        <div class="span3">
                            <input-label name="rampUpInitSleepTime"
                                         v-model="test.rampUpInitSleepTime"
                                         ref="rampUpConfig"
                                         message="perfTest.config.rampUp.initialSleepTime" others="<code>MS</code>">
                            </input-label>
                        </div>
                        <div class="span3">
                            <input-label name="rampUpIncrementInterval"
                                         v-model="test.rampUpIncrementInterval"
                                         ref="rampUpConfig"
                                         message="perfTest.config.rampUp.incrementInterval" others="<code>MS</code>">
                            </input-label>
                        </div>
                    </div>
                </div>
            </template>
        </div>
        <legend class="center ramp-ip-desc">
            <span v-text="i18n('perfTest.config.rampUp.des')"></span>
        </legend>
        <div id="ramp-up-chart"></div>
    </div>
</template>

<script>
    import Base from '../../Base.vue';
    import InputLabel from "../../common/InputLabel.vue";
    import { Component, Watch } from 'vue-property-decorator';

    import 'jqplot/jquery.jqplot.min.js';
    import 'jqplot/jqplot.cursor.js';
    import 'jqplot/jqplot.donutRenderer.js';
    import 'jqplot/jqplot.highlighter.js';

    import '../../../../plugins/jqplot/jqplot.canvasAxisTickRenderer.js';
    import '../../../../plugins/jqplot/jqplot.canvasTextRenderer.js';
    import '../../../../plugins/jqplot/jqplot.categoryAxisRenderer.js';
    import '../../../../plugins/jqplot/jqplot.enhancedLegendRenderer.js';

    @Component({
        name: 'rampUp',
        components: { InputLabel },
        props: {
            test: {
                type: Object,
                required: true,
            },
            rampUpTypes: {
                type: Array,
                required: true,
            }
        },
    })
    export default class RampUp extends Base {
        plotObj = '';
        enableRampUp = false;

        mounted() {
            this.$watchAll(['test.rampUpStep', 'test.rampUpInitCount', 'test.rampUpInitSleepTime', 'test.rampUpIncrementInterval'], this.updateRampUpChart);
        }

        @Watch('enableRampUp')
        watchEnableRampUp(val) {
            this.$refs.rampUpConfig.forEach((component) => component.readonly = !val);
            this.updateRampUpChart();
        }

        updateRampUpChart() {
            if (!this.isPlotTargetExist()) {
                return;
            }

            let base;
            let factor;

            if (this.test.rampUpType === "PROCESS") {
                base = this.test.processes;
                factor = this.test.threads;
            } else {
                base = this.test.threads;
                factor = this.test.processes;
            }

            const factorVar = parseInt(factor, 10);
            const destination = parseInt(base, 10) * factorVar;
            const increment = parseInt(this.test.rampUpStep, 10) * factorVar;
            const initialCount = parseInt(this.test.rampUpInitCount, 10) * factorVar;
            const internalTime = parseInt(this.test.rampUpIncrementInterval, 10);

            if (isNaN(initialCount) || isNaN(destination) || isNaN(increment) || isNaN(internalTime)) {
                return;
            }
            if (initialCount > destination) {
                this.test.rampUpInitCount = 1;
                return;
            }
            if (initialCount < destination && increment === 0) {
                this.test.rampUpStep = 1;
                return;
            }

            let steps = (destination - initialCount) / increment;
            if (steps === 0) {
                steps = 1;
            }

            const initialSleepTime = parseInt(this.test.rampUpInitSleepTime);

            if (isNaN(initialSleepTime)) {
                return;
            }

            const seriesArray = [];

            if (this.enableRampUp) {
                let curX = initialSleepTime;
                let curY = initialCount;
                if (initialSleepTime > 0) {
                    seriesArray.push([0, 0]);
                    seriesArray.push([initialSleepTime, 0]);
                }
                seriesArray.push([curX + 0.01, curY]);
                curX += internalTime;
                seriesArray.push([curX, curY]);

                for (let step = 1; step <= Math.ceil(steps); step++) {
                    curY += increment;
                    if (curY > destination) {
                        curY = destination;
                    }
                    seriesArray.push([curX + 0.01, curY]);
                    curX += internalTime;
                    seriesArray.push([curX, curY]);
                }

                $("#ramp-up-chart").empty();

                let maxX = seriesArray[seriesArray.length - 1][0];
                let maxY = seriesArray[seriesArray.length - 1][1];
                this.drawRampUp(seriesArray, internalTime, maxX, maxY);
            } else {
                let curX = 0;
                for (let step = 0; step <= steps; step++) {
                    seriesArray.push([curX + 0.01, destination]);
                    curX = curX + internalTime;
                    seriesArray.push([curX, destination]);
                }

                if (this.plotObj) {
                    this.plotObj.series[0].data = seriesArray;
                    this.plotObj.replot();
                } else {
                    let maxX = seriesArray[seriesArray.length - 1][0];
                    let maxY = seriesArray[seriesArray.length - 1][1];
                    this.drawRampUp(seriesArray, internalTime, maxX, maxY);
                }
            }
        }

        drawRampUp(data, intervalTime, maxX, maxY) {
            let numTicks = (Math.min(parseInt(data.length / 2) + 1, 8));
            let pointCutter = 1;
            if (parseInt(intervalTime / 1000) === (intervalTime / 1000)) {
                pointCutter = 0;
            }
            this.plotObj = $.jqplot('ramp-up-chart', [data], {
                axesDefaults: {
                    tickRenderer: $.jqplot.AxisTickRenderer,
                    tickOptions: {
                        showMark: false,
                    },
                },
                seriesDefaults: {
                    showMarker: false,
                    lineWidth: 1.5,
                },
                axes: {
                    xaxis: {
                        min: 1,
                        max: maxX,
                        pad: 0,
                        numberTicks: numTicks,
                        tickOptions: {
                            show: true,
                            formatter: function (format, value) {
                                value = value || 0;
                                return (value / 1000).toFixed(pointCutter);
                            },
                        },
                    },
                    yaxis: {
                        min: 0,
                        pad: 10,
                        max: maxY,
                        numberTicks: numTicks - 1,
                        tickOptions: {
                            show: true,
                            formatter: function (format, value) {
                                value = value || 0;
                                return (value).toFixed(0);
                            },
                        },
                    },
                },
            });
        }

        isPlotTargetExist() {
            return $("#ramp-up-chart").length !== 0;
        }
    }
</script>

<style lang="less">
    #ramp-up-container {

    }
</style>

<style lang="less" scoped>
    #ramp-up-container {
        margin-left: 18px;

        #ramp-up-chart {
            margin-left: 20px;
        }

        .ramp-ip-desc {
            margin-top: 0;
            padding-top :0;
        }

        input {
            &#use-ramp-up {
                vertical-align: middle;
                margin-bottom: 5px;
            }
        }

        .input-label-container {
            input {
                width: 54px !important;
            }
        }
    }
</style>
