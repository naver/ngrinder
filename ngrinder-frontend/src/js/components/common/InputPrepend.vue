<template>
    <div class="input-prepend-container input-prepend mb-0" :class="`input-group ${extraCss}`">
        <div class="input-group-prepend">
            <span class="input-group-text" :title='i18n(message)' v-text="i18n(message)"></span>
        </div>
        <input class="form-control d-inline-flex" :type="type"
               :id="name" :name="name" :value="value" :min="min"
               @input="$emit('input', $event.target.value)"
               @change="$emit('change')"/>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import Base from '../Base.vue';

    @Component({
        name: 'inputPrepend',
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
        },
    })
    export default class InputPrepend extends Base {}
</script>

<style lang="less" scoped>
    .input-group {
        input {
            height: 30px;
        }
    }
</style>
