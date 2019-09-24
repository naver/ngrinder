<template>
    <div class="ramp-up-container intro">
        <fieldset>
            <legend class="border-bottom">
                <label class="pointer-cursor" for="useRampUp">
                    <input type="checkbox" id="useRampUp" class="use-ramp-up" name="useRampUp" v-model="useRampUp"/>
                    <span v-text="i18n('perfTest.config.rampUp.enable')"></span>
                </label>
                <select class="pull-right form-control" name="rampUpType" :disabled="!useRampUp" v-model="rampUpType" @change="updateRampUpChart">
                    <option v-for="rampUpType in rampUpTypes" :value="rampUpType" v-text="i18n(`perfTest.config.rampUp.${rampUpType.toLowerCase()}`)"></option>
                </select>
            </legend>
        </fieldset>
        <div class="form-horizontal form-horizontal-2 ramp-up-config-container">
            <!--eslint-disable-next-line vue/no-unused-vars-->
            <template v-for="i in 1">
                <div>
                    <div class="row m-0">
                        <div class="ramp-up-config-item">
                            <input-label name="rampUpInitCount"
                                         v-model="test.rampUpInitCount"
                                         ref="rampUpConfig"
                                         message="perfTest.config.rampUp.initialCount">
                            </input-label>
                        </div>
                        <div class="ramp-up-config-item">
                            <input-label name="rampUpStep"
                                         v-model="test.rampUpStep"
                                         ref="rampUpConfig"
                                         message="perfTest.config.rampUp.step">
                            </input-label>
                        </div>
                    </div>
                    <div class="row m-0">
                        <div class="ramp-up-config-item">
                            <input-label name="rampUpInitSleepTime"
                                         v-model="test.rampUpInitSleepTime"
                                         ref="rampUpConfig"
                                         message="perfTest.config.rampUp.initialSleepTime" others="<code>MS</code>">
                            </input-label>
                        </div>
                        <div class="ramp-up-config-item">
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
        <legend class="center mt-0 pt-0">
            <span v-text="i18n('perfTest.config.rampUp.des')"></span>
        </legend>
        <div id="ramp-up-chart"></div>
    </div>
</template>

<script>
    import { Component, Watch } from 'vue-property-decorator';
    import 'jqplot/jquery.jqplot.min.js';
    import 'jqplot/jqplot.cursor.js';
    import 'jqplot/jqplot.donutRenderer.js';
    import 'jqplot/jqplot.highlighter.js';

    import '../../../../plugins/jqplot/jqplot.canvasAxisTickRenderer.js';
    import '../../../../plugins/jqplot/jqplot.canvasTextRenderer.js';
    import '../../../../plugins/jqplot/jqplot.categoryAxisRenderer.js';
    import '../../../../plugins/jqplot/jqplot.enhancedLegendRenderer.js';

    import Base from '../../Base.vue';
    import InputLabel from '../../common/InputLabel.vue';

    @Component({
        name: 'rampUp',
        components: { InputLabel },
        props: {
            testProps: {
                type: Object,
                required: true,
            },
            rampUpTypes: {
                type: Array,
                required: true,
            },
        },
    })
    export default class RampUp extends Base {
        plotObj = '';
        useRampUp = false;
        rampUpType = 'PROCESS';
        test = {};

        created() {
            Object.assign(this.test, this.testProps);
            this.useRampUp = this.test.useRampUp;
            this.rampUpType = this.test.rampUpType;
        }

        mounted() {
            this.$watchAll(['test.rampUpStep', 'test.rampUpInitCount', 'test.rampUpInitSleepTime', 'test.rampUpIncrementInterval'], this.updateRampUpChart);
        }

        @Watch('useRampUp')
        watchUseRampUp(val) {
            this.$refs.rampUpConfig.forEach(component => component.readonly = !val);
            this.updateRampUpChart();
        }

        getParams() {
            return {
                useRampUp: this.useRampUp,
                rampUpType: this.rampUpType,
                rampUpInitCount: this.test.rampUpInitCount,
                rampUpStep: this.test.rampUpStep,
                rampUpInitSleepTime: this.test.rampUpInitSleepTime,
                rampUpIncrementInterval: this.test.rampUpIncrementInterval,
            };
        }

        updateRampUpChart() {
            if (!this.isPlotTargetExist()) {
                return;
            }

            let base;
            let factor;

            if (this.rampUpType === 'PROCESS') {
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

            if (this.useRampUp) {
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

                $('#ramp-up-chart').empty();

                const maxX = seriesArray[seriesArray.length - 1][0];
                const maxY = seriesArray[seriesArray.length - 1][1];
                this.drawRampUp(seriesArray, internalTime, maxX, maxY);
            } else {
                let curX = 0;
                for (let step = 0; step <= steps; step++) {
                    seriesArray.push([curX + 0.01, destination]);
                    curX += internalTime;
                    seriesArray.push([curX, destination]);
                }

                if (this.plotObj) {
                    this.plotObj.series[0].data = seriesArray;
                    this.plotObj.replot();
                } else {
                    const maxX = seriesArray[seriesArray.length - 1][0];
                    const maxY = seriesArray[seriesArray.length - 1][1];
                    this.drawRampUp(seriesArray, internalTime, maxX, maxY);
                }
            }
        }

        drawRampUp(data, intervalTime, maxX, maxY) {
            const numTicks = (Math.min(parseInt(data.length / 2) + 1, 8));
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
                            formatter: (format, value) => {
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
                            formatter: (format, value) => {
                                value = value || 0;
                                return (value).toFixed(0);
                            },
                        },
                    },
                },
            });
        }

        isPlotTargetExist() {
            return $('#ramp-up-chart').length !== 0;
        }
    }
</script>

<style lang="less" scoped>
    .ramp-up-container {
        width: 460px;
        margin-left: 18px;

        .ramp-up-config-container {
            margin-top: 10px;
        }

        #ramp-up-chart {
            margin-left: 20px;
        }

        .ramp-up-config-item {
            width: 220px;
        }

        select {
            width: 90px;
            font-size: 12px;
        }

        .input-label-container {
            input {
                width: 54px !important;
            }
        }
    }
</style>
