<template>
    <select v-if="type === 'select'" :style="customStyle">
        <slot></slot>
    </select>
    <input v-else value=" " :style="customStyle">
</template>

<script>
    import Vue from 'vue';
    import Component from 'vue-class-component';
    import '../../../plugins/select2/select2.min';

    @Component({
        props: {
            value: {
                type: String,
                required: true,
            },
            type: {
                type: String,
                required: true,
            },
            customStyle: {
                type: String,
            },
            option: {
                type: Object,
                default: {},
            },
        },
        name: 'select2',
    })
    export default class Select2 extends Vue {
        mounted() {
            this.init();
        }

        init() {
            const component = this;
            $(this.$el).select2(this.option, []).change(function () {
                    component.$emit('input', this.value);
                    component.$emit('change');
                });
        }
    }

</script>

<style lang="less" scoped>
    @import '../../../plugins/select2/select2.css';
</style>
