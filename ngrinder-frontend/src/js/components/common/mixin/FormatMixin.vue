<script>
    import { Mixin } from 'vue-mixin-decorator';

    @Mixin
    export default class FormatMixin {
        formatNetwork(value) {
            value = value || 0;
            if (value < 1024) {
                return `${value.toFixed(1)}B`;
            } else if (value < 1048576) { // 1024 * 1024
                return `${(value / 1024).toFixed(1)}K`;
            } else {
                return `${(value / 1048576).toFixed(2)}M`;
            }
        }

        formatTestTime(s) {
            if (s < 60) {
                return ` ${s}s`;
            }
            if (s < 3600) {
                return ` ${parseInt(s / 60)}m ${(s % 60)}s`;
            }
            if (s < 86400) {
                return ` ${parseInt(s / 3600)}h ${parseInt(s % 3600 / 60)}m ${(s % 3600 % 60)}s`;
            }
            return ` ${parseInt(s / 86400)}d ${parseInt(s % 86400 / 3600)}h ${parseInt(s % 86400 % 3600 / 60)}m ${(s % 86400 % 3600 % 60)}s`;
        }

        formatPercentage(value) {
            if (value === undefined || value === null) {
                return 'N/A';
            }

            if (value < 10) {
                return `${value.toFixed(1)}%`;
            } else {
                return `${value.toFixed(0)}%`;
            }
        }

        formatMemory(value) {
            value = value || 0;
            if (value < 1024) {
                return `${value.toFixed(1)}K`;
            } else if (value < 1048576) { // 1024 * 1024
                return `${(value / 1024).toFixed(1)}M`;
            } else {
                return `${(value / 1048576).toFixed(2)}G`;
            }
        }
    }
</script>
