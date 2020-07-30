<template>
    <div class="w-100">
        <div class="input-group input-popover">
            <input type="text"
                   data-html="true"
                   data-trigger="hover"
                   data-toggle="popover"
                   class="form-control"
                   v-validate="validationRules"
                   :id="name"
                   :name="name"
                   :class="extraCss"
                   :placeholder="placeholder ? placeholder : ''"
                   :title="i18n(message)"
                   :data-content="messageContent ? messageContent : i18n(`${message}.help`)"
                   :data-placement="dataPlacement"
                   :style="customStyle ? customStyle : false"
                   :value="value"
                   @input="$emit('input', $event.target.value)"/>
        </div>
        <div v-show="errors.has(name)" class="validation-message" v-text="errors.first(name)" :style="errStyle"></div>
    </div>
</template>

<script>
    import { Inject } from 'vue-property-decorator';
    import Component from 'vue-class-component';
    import Base from '../Base.vue';

    @Component({
        name: 'inputPopover',
        props: {
            value: {
                type: [String, Number],
                required: true,
            },
            message: {
                type: String,
                required: true,
            },
            dataPlacement: {
                type: String,
                default: 'bottom',
            },
            extraCss: String,
            name: String,
            errStyle: String,
            placeholder: String,
            messageContent: String,
            customStyle: String,
            validationRules: Object,
        },
    })
    export default class InputPopover extends Base {
        @Inject() $validator;
    }
</script>

<style lang="less" scoped>

</style>
