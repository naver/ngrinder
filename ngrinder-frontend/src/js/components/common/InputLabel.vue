<template>
    <div class="input-label-container">
        <label :for="name" class="control-label pointer-cursor"><span v-text="i18n(message)"></span></label>
        <div class="controls">
            <input :type="type" class="input form-control" :id="name" :name="name" :style="extraCss"
                   :value="value" :min="min" @input="$emit('input', $event.target.value)" :readonly="readonly"/>
            <span v-if="others" v-html="others"></span>
            <div :id="`err-${name}`" :style="errStyle"></div>
        </div>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import Base from '../Base.vue';

    @Component({
        name: 'inputLabel',
        props: {
            type: {
                type: String,
                default: 'text',
            },
            value: {
                type: [String, Number],
                required: true,
            },
            min: {
                type: [String, Number],
                default: 0,
            },
            message: {
                type: String,
                required: true,
            },
            extraCss: String,
            name: String,
            errStyle: String,
            others: String,
        },
    })
    export default class InputLabel extends Base {
        readonly = true;
    }
</script>

<style lang="less" scoped>
    .input-label-container {
        margin-bottom: 10px;
        input {
            padding: 4px 6px;
            width: 50px;
            font-size: 12px;

            &.form-control {
                display: inline-block;
            }
        }
    }
</style>
