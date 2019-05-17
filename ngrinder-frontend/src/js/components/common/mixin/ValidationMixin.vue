<script>
    import Base from "../../Base.vue"
    import { Mixin } from 'vue-mixin-decorator'

    @Mixin
    export default class ValidationMixin extends Base {
        checkValidation() {
            this.$validator.validateAll()
                .then(() => this.$emit('validationResult', this.errors.any()))
                .catch(() => this.$emit('validationResult', false));
        }

        getCheckValidationPromise() {
            return new Promise((resolve) => {
                this.$validator.validateAll().then(() => {
                    this.$emit('validationResult', this.errors.any());
                    resolve();
                }).catch(() => {
                    this.$emit('validationResult', false);
                    resolve();
                });
            });
        }
    }
</script>
