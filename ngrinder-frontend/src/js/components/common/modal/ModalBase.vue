<script>
    import { Component, Prop } from 'vue-property-decorator';
    import Base from 'Base.vue';

    @Component({
        name: 'modalBase',
    })
    export default class ModalBase extends Base {
        @Prop({ type: String, required: false })
        focus;

        mounted() {
            $(this.$el).on('shown.bs.modal', () => {
                if (this.$refs[this.focus]) {
                    this.$refs[this.focus].focus();
                }
            });
        }

        hide() {
            $(this.$el).modal('hide');
        }

        show() {
            this.reset();
            $(this.$el).modal('show');
        }

        reset() {
            // Implement in child class
        }
    }
</script>
