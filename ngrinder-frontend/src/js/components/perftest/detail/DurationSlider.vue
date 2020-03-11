<template>
    <div class="duration-slider-container">
        <b-form-slider class="slider" :value=value :max="sliderMax" :min="1"  @change="changeSlider"></b-form-slider>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import Base from '../../Base.vue';

    @Component({
        name: 'durationSlider',
        props: {
            maxRunHour: {
                type: Number,
                required: true,
            },
            durationMs: {
                type: Number,
                required: true,
            },
        },
    })
    export default class DurationSlider extends Base {
        value = 0;
        sliderMax = 1000;
        durationMap = [];

        mounted() {
            this.init();
        }

        init() {
            this.durationMap[0] = 0;

            for (let i = 1; i <= this.sliderMax; i++) {
                if (i <= 10) {
                    this.durationMap[i] = this.durationMap[i - 1] + 1;
                } else if (i <= 20) {
                    this.durationMap[i] = this.durationMap[i - 1] + 5;
                } else if (i <= 32) { // until 180 min
                    this.durationMap[i] = this.durationMap[i - 1] + 10;
                } else if (i <= 38) { // 360 min
                    this.durationMap[i] = this.durationMap[i - 1] + 30;
                } else if (i <= 56) { // 24 hours
                    this.durationMap[i] = this.durationMap[i - 1] + 60;
                } else if (i <= 72) {
                    this.durationMap[i] = this.durationMap[i - 1] + 60 * 6;
                } else if (i <= 78) {
                    this.durationMap[i] = this.durationMap[i - 1] + 60 * 12;
                } else {
                    this.durationMap[i] = this.durationMap[i - 1] + 60 * 24;
                }
                if ((this.durationMap[i] / 60) >= this.maxRunHour) {
                    this.sliderMax = i;
                    this.durationMap[i] = (this.maxRunHour - 1) * 60;
                    break;
                }
            }

            this.setDurationMs(this.durationMs);
        }

        changeSlider(values) {
            const maxIndex = this.durationMap.length - 1;
            let durationSec;
            if (maxIndex === values.newValue) {
                durationSec = (this.durationMap[maxIndex] + 59) * 60 + 59;
            } else {
                durationSec = this.durationMap[values.newValue] * 60;
            }
            this.$emit('change', durationSec);
        }

        setDurationMs(durationMs) {
            for (let i = 0; i <= this.sliderMax; i++) {
                if (this.durationMap[i] * 60000 >= durationMs) {
                    this.value = i;
                    break;
                }

                if (i === this.sliderMax) {
                    this.value = this.sliderMax;
                }
            }
        }
    }
</script>

<style lang="less">
    .duration-slider-container {
        .slider {
            width: 255px;
        }

        .slider-handle {
            width: 16px;
            height: 16px;
            top: 2px;
            border: 1px solid #ccc;
            border-radius: 6px;
            background: #f0f0f0;
        }

        .slider-selection {
            background: #337ab7;
        }

        .slider-track-high {
            border: 1px solid #ccc;
        }
    }
</style>
