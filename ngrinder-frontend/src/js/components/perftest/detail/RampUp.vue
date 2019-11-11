<template>
    <div class="ramp-up-container intro">
        <fieldset>
            <legend class="border-bottom">
                <input type="checkbox" name="useRampUp" v-model="useRampUp" />
                <span class="pointer-cursor" v-text="i18n('perfTest.config.rampUp.enable')" @click="useRampUp = !useRampUp"></span>
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
    import { Component, Prop, Watch } from 'vue-property-decorator';
    import bb from 'billboard.js';

    import Base from '../../Base.vue';
    import InputLabel from '../../common/InputLabel.vue';

    class CoordinationArray {
        constructor() {
            this.xArray = [];
            this.yArray = [];
        }

        push(x, y) {
            this.xArray.push(x);
            this.yArray.push(y);
        }

        get() {
            return {
                x: this.xArray,
                y: this.yArray,
            };
        }
    }

    @Component({
        name: 'rampUp',
        components: { InputLabel },
    })
    export default class RampUp extends Base {
        @Prop({ type: Object, required: true })
        testProp;

        @Prop({ type: Array, required: true })
        rampUpTypes;

        test = {
            rampUpInitCount: 0,
            rampUpStep: 1,
            rampUpInitSleepTime: 0,
            rampUpIncrementInterval: 1000,
        };

        chart = null;
        useRampUp = false;
        rampUpType = 'PROCESS';

        created() {
            Object.assign(this.test, this.testProp);
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

        getCurrentBaseAndFactor() {
            let base;
            let factor;

            if (this.rampUpType === 'PROCESS') {
                base = this.test.processes;
                factor = this.test.threads;
            } else {
                base = this.test.threads;
                factor = this.test.processes;
            }

            return { base, factor };
        }

        updateRampUpChart() {
            if (this.$parent.errors.has('vuserPerAgent')) {
                return;
            }

            const { base, factor } = this.getCurrentBaseAndFactor();

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

            const tail = arr => arr[arr.length - 1];

            const coordinates = new CoordinationArray();

            if (this.useRampUp) {
                let curX = initialSleepTime;
                let curY = initialCount;
                if (initialSleepTime > 0) {
                    coordinates.push(0, 0);
                    coordinates.push(initialSleepTime, 0);
                }
                coordinates.push(curX + 0.01, curY);

                curX += internalTime;
                coordinates.push(curX, curY);

                for (let step = 1; step <= Math.ceil(steps); step++) {
                    curY += increment;
                    if (curY > destination) {
                        curY = destination;
                    }
                    coordinates.push(curX + 0.01, curY);

                    curX += internalTime;
                    coordinates.push(curX + 0.01, curY);
                }

                const maxX = tail(coordinates.get().x);
                const maxY = tail(coordinates.get().y);
                this.drawRampUp(coordinates, internalTime, maxX, maxY);
            } else {
                let curX = 0;
                for (let step = 0; step <= steps; step++) {
                    coordinates.push(curX + 0.01, destination);
                    curX += internalTime;
                    coordinates.push(curX + 0.01, destination);
                }

                const maxX = tail(coordinates.get().x);
                const maxY = tail(coordinates.get().y);
                this.drawRampUp(coordinates, internalTime, maxX, maxY);
            }
        }

        drawRampUp(coordinates, intervalTime, maxX, maxY) {
            const numTicks = (Math.min(parseInt(coordinates.get().x.length / 2) + 1, 8));
            let pointCutter = 1;
            if (parseInt(intervalTime / 1000) === (intervalTime / 1000)) {
                pointCutter = 0;
            }

            this.chart = bb.generate({
                bindto: '#ramp-up-chart',
                data: {
                    json: coordinates.get(),
                    xs: { 'y': 'x' },
                },
                axis: {
                    x: {
                        tick: {
                            format: value => {
                                value = value || 0;
                                return (value / 1000).toFixed(pointCutter);
                            },
                            count: numTicks,
                        },
                        padding: {
                            left: 0,
                            right: 0,
                        },
                        min: 1,
                        max: maxX,
                    },
                    y: {
                        tick: {
                            format: value => {
                                value = value || 0;
                                return (value).toFixed(0);
                            },
                            count: numTicks - 1,
                        },
                        padding: {
                            top: 0,
                            bottom: 0,
                        },
                        min: 0,
                        max: maxY,
                    },
                },
                line: { point: false },
                tooltip: { show: false },
                legend: { show: false },
                grid: {
                    x: { show: true },
                    y: { show: true },
                },
                oninit() {
                    $('rect.bb-zoom-rect').css({ opacity: 1 });
                    this.svg.select('g.bb-grid')
                        .insert('rect', ':first-child')
                        .attr('class', 'chart-background');
                },
            });
        }
    }
</script>

<style lang="less" scoped>
    @import '~billboard.js/dist/billboard.min.css';
    @import '~billboard.js/dist/theme/insight.min.css';

    .ramp-up-container {
        width: 460px;
        margin-left: 18px;

        .ramp-up-config-container {
            margin-top: 10px;
        }

        #ramp-up-chart {
            margin-left: 20px;

            .chart-background {
                fill: #fffdf6;
                fill-opacity: 1;
                width: 100%;
                height: 100%;
                border: 2px solid #c4c4c4;
            }
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
<style lang="less">
    #ramp-up-chart {
        .bb-line {
            stroke-width: 1.5px
        }
    }
</style>
