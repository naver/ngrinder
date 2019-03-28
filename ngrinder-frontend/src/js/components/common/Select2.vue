<template>
    <select :style="customStyle">
        <slot></slot>
    </select>
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
            customStyle: {
                type: String,
            },
            placeHolder: {
                type: String,
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
            $(this.$el)
                .select2({
                    placeholder: this.placeHolder || '',
                    allowClear: true,
                })
                .change(function () {
                    component.$emit('input', this.value);
                    component.$emit('change');
                });
        }
    }

</script>

<style lang="less" scoped>
    @import '../../../plugins/select2/select2.css';
</style>
