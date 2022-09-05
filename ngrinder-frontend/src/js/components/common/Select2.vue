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
    <input v-else ref="select2" v-model="value" :style="customStyle" :name="name">
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
                .on('change', function() {
                    self.$emit('input', this.value);
                    self.$emit('change');
                    if (self.type === 'select') {
                        self.$nextTick(() => self.$validator.validate(self.name));
                    }
                })
                .on('select2:opening', () => self.$emit('opening'));
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
