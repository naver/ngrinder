<template>
    <span v-if="type === 'select'">
        <select ref="select2"
                :style="customStyle"
                :name="name"
                v-model="value"
                v-validate="validationRules">
            <slot></slot>
        </select>
        <div v-show="errors.has(name)" class="validation-message" v-text="errors.first(name)" :style="errStyle"></div>
    </span>
    <input v-else ref="select2" value=" " :style="customStyle" :name="name">
</template>

<script>
    import Vue from 'vue';
    import Component from 'vue-class-component';
    import { Inject } from 'vue-property-decorator';
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
                default: () => ({}),
            },
            name: String,
            customStyle: String,
            validationRules: Object,
            errStyle: String,
        },
        name: 'select2',
    })
    export default class Select2 extends Vue {
        @Inject() $validator;

        mounted() {
            this.init();
        }

        init() {
            const self = this;
            $(this.$refs.select2)
                .select2(this.option, [])
                .change(function() {
                    self.$emit('input', this.value);
                    self.$emit('change');
                    self.$nextTick(() => self.$validator.validateAll());
                });
        }

        selectValue(value) {
            $(this.$refs.select2).select2('val', value);
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
