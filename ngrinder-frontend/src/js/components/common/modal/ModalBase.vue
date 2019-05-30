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
            $(this.$el).modal('show');
        }
    }
</script>
