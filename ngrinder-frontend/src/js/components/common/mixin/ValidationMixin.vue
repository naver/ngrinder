<script>
    import Base from "../../Base.vue"
    import { Mixin } from 'vue-mixin-decorator'


    @Mixin
    export default class ValidationMixin extends Base {
        checkValidation() {
            this.$validator.validateAll().then(result => this.$emit('validationResult', !result))
        }

        getCheckValidationPromise() {
            return new Promise((resolve) => {
                this.$validator.validateAll().then(result => {
                    this.$emit('validationResult', !result);
                    resolve();
                })
            });
        }
    }
</script>
