<script>
    import Vue from 'vue';
    import { Mixin } from 'vue-mixin-decorator';

    @Mixin
    export default class MessagesMixin extends Vue {
        showSuccessMsg(msg) {
            this.$root.$refs.messages.showSuccessMsg(msg);
        }

        showInfoMsg(msg) {
            this.$root.$refs.messages.showInfoMsg(msg);
        }

        showErrorMsg(msg) {
            this.$root.$refs.messages.showErrorMsg(msg);
        }

        showProgressBar(msg) {
            this.$root.$refs.messages.showProgressBar(msg);
        }

        hideProgressBar() {
            this.$root.$refs.messages.hideProgressBar();
        }
    }
</script>
