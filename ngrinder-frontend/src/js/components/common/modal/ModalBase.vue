<script>
    import Component from 'vue-class-component';
    import Base from 'Base.vue';

    @Component({
        name: 'modalBase',
        props: {
            id: {
                type: String,
                required: true,
            },
        },
    })
    export default class ModalBase extends Base {
        hide() {
            $(`#${this.id}`).modal('hide');
        }

        show(id) {
            $(`#${this.id}`).modal('show');
        }
    }
</script>
