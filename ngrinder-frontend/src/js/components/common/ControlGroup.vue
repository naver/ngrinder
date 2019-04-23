<template>
    <div class="control-group" :class="{error: hasError}">
        <label class="control-label" :for="name">
            <span v-show="required" class="required-mark" v-text="'*'"></span><span v-html="i18n(labelMessageKey)"></span>
        </label>
        <div class="controls">
            <slot></slot>
        </div>
    </div>
</template>

<script>
    import Base from '../Base.vue';
    import Component from 'vue-class-component';

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
        hasError = false;

        handleError(result) {
            this.hasError = result;
        }
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
