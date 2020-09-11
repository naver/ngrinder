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

            $(this.$el).on('show.bs.modal', this.beforeShown);
            $(this.$el).on('hide.bs.modal', this.beforeHidden);
            $(this.$el).on('shown.bs.modal', this.shown);
        }

        hide() {
            $(this.$el).modal('hide');
        }

        show() {
            $(this.$el).modal('show');
        }

        shown() {
            // Implement in child class
        }

        beforeShown() {
            // Implement in child class
        }

        beforeHidden() {
            // Implement in child class
        }
    }
</script>
