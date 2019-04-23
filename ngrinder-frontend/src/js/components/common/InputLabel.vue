<template>
    <div class="control-group input-label-container">
        <label :for="name" class="control-label"><span v-text="i18n(message)"></span></label>
        <div class="controls">
            <input type="text" class="input input-mini" :id="name" :name="name" :style="extraCss"
                   :value="value" @input="$emit('input', $event.target.value)" :readonly="readonly"/>
            <span v-if="others" v-html="others"></span>
            <div :id="`err-${name}`" :style="errStyle"></div>
        </div>
    </div>
</template>

<script>
    import Base from '../Base.vue';
    import Component from 'vue-class-component';

    @Component({
        name: 'inputLabel',
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
        input {
            width: 40px;
        }
    }
</style>
