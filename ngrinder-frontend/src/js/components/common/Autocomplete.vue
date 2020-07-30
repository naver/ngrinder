<template>
    <input type="text" ref="autocomplete" v-model="value" class="form-control">
</template>

<script>
    import { Component, Watch } from 'vue-property-decorator';
    import Base from '../Base.vue';
    import '../../../plugins/autocomplete/jquery-ui.min';

    @Component({
        name: 'autocomplete',
        props: {
            source: {
                type: Array,
                required: true,
                default: () => [],
            },
            select: {
                type: Function,
                required: false,
            },
        },
    })
    export default class Autocomplete extends Base {
        value = '';

        mounted() {
            this.init();
        }

        init() {
            $(this.$refs.autocomplete).autocomplete({
                minLength: 0,
                source: this.source,
                select: (event, ui) => {
                    this.value = ui.item.value;

                    if (this.select) { // callback function
                        this.select(event, ui);
                    }

                    $(event.target).blur();
                },
            }).focus(function() {
                $(this).autocomplete('search', '');
            });
        }

        @Watch('value')
        valueChanged(newValue) {
            this.$emit('input', newValue);
        }

        @Watch('source')
        sourceChanged() {
            this.value = '';
            this.init();
        }
    }
</script>

<style lang="less">
    @import "../../../plugins/autocomplete/jquery-ui.min.css";

    .ui-autocomplete.ui-front.ui-menu.ui-widget-content {
        z-index: 1100;
    }
</style>
