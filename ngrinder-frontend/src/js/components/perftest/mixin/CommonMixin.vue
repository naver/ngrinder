<script>
    import { Mixin } from 'vue-mixin-decorator';

    @Mixin
    export default class CommonMixin {
        getStatusDataContent(progressMessage, lastProgressMessage) {
            const dataContent = progressMessage ? `${progressMessage}<br>` : '';
            try {
                lastProgressMessage = decodeURIComponent(lastProgressMessage);
            } catch (e) {
                // do nothing
            }

            return `${dataContent}<b>${this.$htmlEntities.encode(lastProgressMessage)}</b>`.replace(/\n/g, '<br>');
        }

        isRunningStatus(status) {
            return status && (status.category === 'TESTING' || status.category === 'PROGRESSING');
        }
    }
</script>
