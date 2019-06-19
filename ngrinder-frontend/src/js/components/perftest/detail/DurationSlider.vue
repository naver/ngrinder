<template>
    <div class="duration-slider-container">
        <input class="slider" ref="durationInput" data-slider-step="1"/>
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
        sliderMax = 1000;
        durationMap = [];
        $durationInput = null;

        mounted() {
            this.initDuration();
        }

        initDuration() {
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

            this.$durationInput = $(this.$refs.durationInput);
            this.$durationInput.slider({
                max: this.sliderMax,
                min: 1,
                tooltip: 'hide',
                handle: 'square',
            });

            this.$durationInput.on('slide', event => {
                const maxIndex = this.durationMap.length - 1;
                let durationSec = 0;
                if (maxIndex === parseInt(event.value)) {
                    durationSec = (this.durationMap[maxIndex] + 59) * 60 + 59;
                } else {
                    durationSec = this.durationMap[event.value] * 60;
                }
                this.$emit('change', durationSec);
            });

            this.initSliderFromDurationMs(this.durationMs);
        }

        initSliderFromDurationMs(durtaionMs) {
            for (let i = 0; i <= this.sliderMax; i++) {
                if (this.durationMap[i] * 60000 >= durtaionMs) {
                    this.$durationInput.slider('setValue', i);
                    break;
                }
                if (i === this.sliderMax) {
                    this.$durationInput.slider('setValue', this.sliderMax);
                }
            }
        }
    }
</script>

<style lang="less" scoped>
    .duration-slider-container {
        .slider {
            margin-left: 0;
            width: 255px;
        }
    }
</style>
