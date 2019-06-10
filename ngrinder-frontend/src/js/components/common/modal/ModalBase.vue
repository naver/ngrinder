<script>
    import Component from 'vue-class-component';
    import Base from 'Base.vue';

    @Component({
        name: 'modalBase',
    })
    export default class ModalBase extends Base {
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
