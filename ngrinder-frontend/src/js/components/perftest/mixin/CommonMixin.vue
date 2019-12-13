<script>
    import { Mixin } from 'vue-mixin-decorator';

    @Mixin
    export default class CommonMixin {
        getStatusDataContent(progressMessage, lastProgressMessage) {
            const dataContent = progressMessage ? `${progressMessage}<br>` : '';
            return `${dataContent}<b>${this.$htmlEntities.encode(decodeURIComponent(lastProgressMessage))}</b>`.replace(/\n/g, '<br>');
        }
    }
</script>
