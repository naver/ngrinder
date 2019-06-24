<template>
    <div id="control-group" class="control-group" :class="{success: success}"
         :data-step="dataStep" :data-intro="dataIntro" data-html="true">
        <label class="control-label" :for="id ? id : name" :style="labelStyle">
            <input v-if="radio"
                   type="radio"
                   :id="id"
                   :value="radio.radioValue"
                   :name="name"
                   @input="$emit('input', $event.target.value)"
                   @change="$emit('change')"
                   :checked="radio.checked">
            <span v-show="required" class="required-mark" v-text="'*'"></span>
            <span v-html="i18n(labelMessageKey)"></span>
            <span v-if="labelHelpMessageKey"
                  data-toggle="popover"
                  data-html="true"
                  :data-content="i18n(`${labelHelpMessageKey}.help`)"
                  :title="i18n(labelHelpMessageKey)"
                  data-placement='top'>
				<i class="icon-question-sign" style="vertical-align: middle;"></i>
			</span>
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
    }
</script>

<style lang="less" scoped>
    .control-group {
        i {
            vertical-align: middle;
        }

        .required-mark {
            vertical-align: middle;
            margin-right: 2px;
            color: red;
        }
    }
</style>
