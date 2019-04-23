<template>
    <div class="input-prepend-container input-prepend" :class="`input-group ${extraCss}`">
		<span class="input-group-addon add-on" :title='i18n(message)' v-text="i18n(message)"></span>
        <input class="form-control input-mini" type="text"
               :id="name" :name="name" :value="value"
               @input="$emit('input', $event.target.value)"
               @change="$emit('change')"
               aria-describedby="basic-addon1"/>
    </div>
</template>

<script>
    import Base from '../Base.vue';
    import Component from 'vue-class-component';

    @Component({
        name: 'inputPrepend',
        props: {
            value: {
                type: [String, Number],
                required: true,
            },
            message: {
                type: String,
                required: true,
            },
            extraCss: String,
            name: String,
        },
    })
    export default class InputPrepend extends Base {

    }
</script>

<style lang="less" scoped>
    .input-prepend-container {
        margin-bottom: 0;
    }
</style>
