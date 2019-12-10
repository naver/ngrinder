<template>
    <div class="ramp-up-container intro">
        <fieldset>
            <legend class="border-bottom">
                <input type="checkbox" class="align-middle" v-model="rampUp.enable"/>
                <span class="pointer-cursor" v-text="i18n('perfTest.config.rampUp.enable')" @click="rampUp.enable = !rampUp.enable"></span>
                <select class="pull-right form-control" name="rampUpType" :disabled="!rampUp.enable" v-model="rampUp.type" @change="updateRampUpChart">
                    <option v-for="rampUpType in rampUpTypes" :value="rampUpType" v-text="i18n(`perfTest.config.rampUp.${rampUpType.toLowerCase()}`)"></option>
                </select>
            </legend>
        </fieldset>
        <div class="form-horizontal form-horizontal-2 ramp-up-config-container">
            <!--eslint-disable-next-line vue/no-unused-vars-->
            <template v-for="i in 1">
                <div>
                    <div class="row">
                        <div class="ramp-up-config-item">
                            <input-label name="rampUpInitCount"
                                         type="number"
                                         v-model="rampUp.initCount"
                                         ref="rampUpConfig"
                                         message="perfTest.config.rampUp.initialCount">
                            </input-label>
                        </div>
                        <div class="ramp-up-config-item">
                            <input-label name="rampUpStep"
                                         type="number"
                                         v-model="rampUp.step"
                                         ref="rampUpConfig"
                                         message="perfTest.config.rampUp.step">
                            </input-label>
                        </div>
                    </div>
                    <div class="row">
                        <div class="ramp-up-config-item">
                            <input-label name="rampUpInitSleepTime"
                                         type="number"
                                         v-model="rampUp.initSleepTime"
                                         ref="rampUpConfig"
                                         message="perfTest.config.rampUp.initialSleepTime" others="<code>MS</code>">
                            </input-label>
                        </div>
                        <div class="ramp-up-config-item">
                            <input-label name="rampUpIncrementInterval"
                                         type="number"
                                         v-model="rampUp.interval"
                                         ref="rampUpConfig"
                                         message="perfTest.config.rampUp.incrementInterval" others="<code>MS</code>">
                            </input-label>
                        </div>
                    </div>
                </div>
            </template>
        </div>
        <legend class="center mt-0 pt-0 border-bottom">
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
        rampUp;

        @Prop({ type: Array, required: true })
        rampUpTypes;

        @Prop({ type: Number, required: true })
        processes;

        @Prop({ type: Number, required: true })
        threads;

        chart = null;

        mounted() {
            this.$watchAll([
                'rampUp.step',
                'rampUp.initCount',
                'rampUp.initSleepTime',
                'rampUp.interval',
                'processes',
                'threads'], this.updateRampUpChart);
            this.$refs.rampUpConfig.forEach(component => component.readonly = !this.rampUp.enable);
        }

        @Watch('rampUp.enable')
        watchUseRampUp(val) {
            this.$refs.rampUpConfig.forEach(component => component.readonly = !val);
            this.updateRampUpChart();
        }

        getCurrentBaseAndFactor() {
            let base;
            let factor;

            if (this.rampUp.type === 'PROCESS') {
                base = this.processes;
                factor = this.threads;
            } else {
                base = this.threads;
                factor = this.processes;
            }

            return { base, factor };
        }

        updateRampUpChart() {
            if (this.$parent.errors.has('vuserPerAgent') || this.processes === 0 || this.threads === 0) {
                return;
            }

            const { base, factor } = this.getCurrentBaseAndFactor();

            const factorVar = parseInt(factor, 10);
            const destination = parseInt(base, 10) * factorVar;
            const increment = parseInt(this.rampUp.step, 10) * factorVar;
            const initialCount = parseInt(this.rampUp.initCount, 10) * factorVar;
            const internalTime = parseInt(this.rampUp.interval, 10);

            if (isNaN(initialCount) || isNaN(destination) || isNaN(increment) || isNaN(internalTime)) {
                return;
            }
            if (initialCount > destination) {
                this.rampUp.initCount = 1;
                return;
            }
            if (initialCount < destination && increment === 0) {
                this.rampUp.step = 1;
                return;
            }

            let steps = (destination - initialCount) / increment;
            if (steps === 0) {
                steps = 1;
            }

            const initialSleepTime = parseInt(this.rampUp.initSleepTime);

            if (isNaN(initialSleepTime)) {
                return;
            }

            const tail = arr => arr[arr.length - 1];

            const coordinates = new CoordinationArray();

            if (this.rampUp.enable) {
                let curX = initialSleepTime;
                let curY = initialCount;
                if (initialSleepTime > 0) {
                    coordinates.push(0, 0);
                    coordinates.push(initialSleepTime, 0);
                }
                coordinates.push(curX, curY);

                curX += internalTime;
                coordinates.push(curX, curY);

                for (let step = 1; step <= Math.ceil(steps); step++) {
                    curY += increment;
                    if (curY > destination) {
                        curY = destination;
                    }
                    coordinates.push(curX, curY);

                    curX += internalTime;
                    coordinates.push(curX, curY);
                }

                const maxX = tail(coordinates.get().x);
                const maxY = tail(coordinates.get().y);
                this.drawRampUp(coordinates, internalTime, maxX, maxY);
            } else {
                let curX = 0;
                for (let step = 0; step <= steps; step++) {
                    coordinates.push(curX, destination);
                    curX += internalTime;
                    coordinates.push(curX, destination);
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
                    colors: { y: '#4bb2c5' },
                },
                axis: {
                    x: {
                        tick: {
                            format: value => {
                                value = value || 0;
                                return (value / 1000).toFixed(pointCutter);
                            },
                            count: numTicks,
                            text: {
                                position: { y: 10 },
                            },
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
                            format: this.getYTickFormatter(maxY),
                            count: numTicks - 1,
                            text: {
                                position: { x: -5 },
                            },
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
                padding: {
                    top: 10,
                    right: 16,
                    left: 40,
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
            });
        }

        getYTickFormatter(maxY) {
            let formatter = null;
            if (maxY <= 10) {
                formatter = value => Math.round(value * 10) / 10;
            } else {
                formatter = value => value.toFixed(0);
            }

            return value => formatter(value || 0);
        }
    }
</script>

<style lang="less" scoped>
    @import '~billboard.js/dist/billboard.min.css';
    @import '~billboard.js/dist/theme/insight.min.css';

    .ramp-up-container {
        width: 530px;
        margin-left: 18px;

        .ramp-up-config-container {
            margin-top: 10px;
        }

        #ramp-up-chart {
            width: 530px;
            height: 350px;

            .chart-background {
                fill: #fffdf6;
                stroke: #999999;
                stroke-width: 1px;
            }
        }

        .ramp-up-config-item {
            width: 250px;
        }

        .ramp-up-config-item + .ramp-up-config-item {
            margin-left: 30px;
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

        g.tick > text > tspan {
            font-size: 0.95em;
        }
    }

    .ramp-up-config-item {
        .input-label-container {
            .controls {
                margin-left: 140px;
            }

            .form-control {
                width: 70px;
            }
        }
    }
</style>
