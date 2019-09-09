<template>
    <div class="input-group input-append">
        <input :id="name"
               :type="inputType"
               :readonly="readonly"
               :name="name"
               :title='i18n(message)'
               :value="value"
               v-validate="validationRules"
               class="form-control"
               @input="$emit('input', $event.target.value)"
               @change="$emit('change')"
               @focus="$emit('focus')"/>
        <div v-if="append !== null" class="input-group-append">
            <span class="input-group-text">
                <span class="mr-1" v-text="i18n(appendPrefix)"></span>
                <span v-html="append"></span>
            </span>
        </div>
        <div v-show="errors.has(name)" class="validation-message" v-text="errors.first(name)" :style="errStyle"></div>
    </div>
</template>

<script>
    import { Inject } from 'vue-property-decorator';
    import Component from 'vue-class-component';
    import Base from '../Base.vue';

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
            type: {
                type: String,
                default: 'text',
            },
            append: {
                type: [String, Number],
                default: null,
            },
            readonly: {
                type: Boolean,
                default: false,
            },
            validationRules: Object,
            appendPrefix: String,
            errStyle: String,
        },
    })
    export default class InputAppend extends Base {
        @Inject() $validator;

        get inputType() {
            return this.type ? this.type : 'text';
        }
    }

</script>

<style lang="less" scoped>
    .input-group {
        input {
            flex-grow: unset;
            height: 30px;
            width: 74px;
        }

        .validation-message {
            margin-top: 2px;
            white-space: normal;
        }
    }
</style>
