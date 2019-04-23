<template>
    <div class="input-group input-append">
        <input :type="inputType"
               v-validate="validationRules"
               data-toggle="popover" :name="name" :id="name"
               data-html="true"
               :data-placement="dataPlacement"
               :data-content="dataContent ? dataContent : i18n(`${this.message}.help`)"
               :title='i18n(message)'
               :value="value"
               @input="$emit('input', $event.target.value)"
               @change="$emit('change')"
               @focus="$emit('focus')"
               @keyup="checkValidation"
               aria-describedby="basic-addon2"/>
        <span v-if="append !== null" class="add-on">
            <span v-text="i18n(appendPrefix)"></span>
            <span v-html="append"></span>
        </span>
        <div v-show="errors.has(name)" class="validation-message" v-text="errors.first(name)" :style="errStyle"></div>
    </div>
</template>

<script>
    import Base from '../Base.vue';
    import Component from 'vue-class-component';

    @Component({
        name: 'inputAppend',
        props: {
            value: {
                type: [String, Number],
                required: true,
            },
            name: {
                type: String,
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
            type: {
                type: String,
                default: 'text',
            },
            append: {
                type: [String, Number],
                default: null,
            },
            dataContent: String,
            validationRules: Object,
            appendPrefix: String,
            errStyle: String,
        },
    })
    export default class InputAppend extends Base {
        mounted() {
            this.$EventBus.$on(this.$Event.SIGN_UP_FORM_VALIDATION_CHECK, () => this.checkValidation());
        }

        checkValidation() {
            this.$validator.validateAll().then(result => {this.$emit('validationResult', !result)});
        }

        get inputType() {
            return this.type ? this.type : 'text';
        }

        destroyed() {
            this.initEventBus();
        }
    }

</script>

<style lang="less" scoped>
    .input-group {
        input {
            height: 30px;
            width: 74px;
        }

        .validation-message {
            margin-top: 2px;
        }
    }
</style>
