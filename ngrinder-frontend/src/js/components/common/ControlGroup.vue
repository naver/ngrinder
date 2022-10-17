<template>
    <div class="control-group" :class="{success: success}" :data-step="dataStep" :data-intro="dataIntro" data-html="true">
        <label class="control-label pointer-cursor" :for="id ? id : name" :style="labelStyle">
            <input v-if="radio"
                   type="radio"
                   :id="id"
                   :value="radio.radioValue"
                   :name="name"
                   @input="$emit('input', $event.target.value)"
                   @change="$emit('change')"
                   :checked="radio.checked">
            <span v-show="required" class="required-mark" v-text="'*'"></span>
            <span v-html="i18n(labelMessageKey)" class="label-message"></span>
            <span v-if="labelHelpMessageKey"
                  data-toggle="popover"
                  data-html="true"
                  data-trigger="hover"
                  :data-content="i18n(`${labelHelpMessageKey}.help`)"
                  :title="i18n(labelHelpMessageKey)"
                  data-placement='top'>
				<i class="fa fa-question-circle align-middle"></i>
			</span>
            <i v-if="labelIconOption" v-show="display.labelIcon" :class="labelIconOption.class" @click="$emit('clickLabelIcon')"></i>
        </label>
        <div class="controls" :style="controlsStyle">
            <slot></slot>
        </div>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import Base from '../Base.vue';

    @Component({
        name: 'controlGroup',
        props: {
            labelIconOption: {
                type: Object,
                required: false
            },
            labelMessageKey: {
                type: String,
                required: true,
            },
            radio: {
                type: [Object, Boolean],
                default: false,
            },
            value: String,
            labelStyle: String,
            controlsStyle: String,
            labelHelpMessageKey: String,
            required: Boolean,
            dataStep: [Number, String],
            dataIntro: String,
            error: Boolean,
            name: String,
            id: String,
        },
    })
    export default class ControlGroup extends Base {
        success = false;
        display = {
            labelIcon: !!this.labelIconOption,
        };
    }
</script>

<style lang="less" scoped>
    .control-group {
        i {
            vertical-align: middle;

            &.fa-question-circle {
                width: 14px;
                height: 14px;
                color: black;
            }
        }
    }
</style>
