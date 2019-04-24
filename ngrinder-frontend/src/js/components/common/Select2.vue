<template>
    <span v-if="type === 'select'">
        <select ref="select2"
                :style="customStyle"
                :name="name"
                v-model="value"
                v-validate="validationRules">
            <slot></slot>
        </select>
        <div v-show="errors.has(name)" class="validation-message" v-text="errors.first(name)" errStyle="errStyle"></div>
    </span>
    <input v-else ref="select2" value=" " :style="customStyle" :name="name">
</template>

<script>
    import Vue from 'vue';
    import Component from 'vue-class-component';
    import ValidationMixin from './mixin/ValidationMixin.vue';
    import { Mixins } from 'vue-mixin-decorator';
    import '../../../plugins/select2/select2.min';

    @Component({
        props: {
            value: {
                type: String,
                required: true,
            },
            type: {
                type: String,
                default: 'select',
            },
            option: {
                type: Object,
                default: {},
            },
            name: String,
            customStyle: String,
            validationRules: Object,
            errStyle: String,
        },
        name: 'select2',
    })
    export default class Select2 extends Mixins(Vue, ValidationMixin) {
        mounted() {
            this.init();
        }

        init() {
            const component = this;
            $(this.$refs.select2)
                .select2(this.option, [])
                .change(function () {
                    component.checkValidation();
                    component.$emit('input', this.value);
                    component.$emit('change');
                });
        }

        // for only type 'select'
        getSelectedOptionValidate() {
            return this.$refs.select2.options[this.$refs.select2.options.selectedIndex].dataset.validate;
        }
    }

</script>

<style lang="less" scoped>
    @import '../../../plugins/select2/select2.css';
</style>
