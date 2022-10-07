<template>
    <select v-if="multiple" ref="select2" v-model="value" :style="customStyle" :name="name"></select>
    <span v-else>
        <select ref="select2"
                :style="customStyle"
                :name="name"
                v-model="value"
                v-validate="validationRules">
            <slot></slot>
        </select>
        <div v-show="errors.has(name)" class="validation-message" v-text="errors.first(name)" :style="errStyle"></div>
    </span>
</template>

<script>
    import Vue from 'vue';
    import Component from 'vue-class-component';
    import { Inject } from 'vue-property-decorator';
    import 'select2/dist/js/select2.full.js';
    import 'select2/dist/css/select2.css';

    @Component({
        props: {
            value: {
                type: [String, Array],
                required: true,
            },
            multiple: {
                type: Boolean,
                default: false,
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
                .on('change', () => {
                    self.$emit('input', $(self.$refs.select2).val());
                    self.$emit('change');
                    if (!self.multiple) {
                        self.$nextTick(() => self.$validator.validate(self.name));
                    }
                })
                .on('select2:opening', () => self.$emit('opening'));

            if (self.multiple && this.value) {
                const initFunction = this.option.initSelect2 || (() => []);
                const options = initFunction();
                options.forEach(option => $(this.$refs.select2).append(option));
                $(this.$refs.select2).trigger('change');
            }
        }

        selectValue(value) {
            $(this.$refs.select2).val(value);
            $(this.$refs.select2).trigger('change');
            this.$emit('input', value);
        }

        getSelectedOption(key) {
            return $(this.$refs.select2).find(':selected')[0].dataset[key];
        }

        refreshDropDown() {
            $(this.$refs.select2).select2('close');
            $(this.$refs.select2).select2('open');
        }
    }

</script>

<style lang="less">
    @error-color: #d9534f;

    #ngrinder {
        .select2-container .select2-selection--single {
            height: 30px;
        }

        .error {
            .dropdown {
                button {
                    border: 1px solid @error-color;

                    &.show-placeholder {
                        color: @error-color;
                    }
                }
            }

            .select2-selection {
                border-color: @error-color;

                span {
                    color: @error-color;
                }

                b {
                    border-color: @error-color transparent;
                }
            }
        }

        .select2-no-results, .select2-input {
            font-size: 12px;
        }
    }
</style>
