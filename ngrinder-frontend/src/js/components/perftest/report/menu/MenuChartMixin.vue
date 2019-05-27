<script>
    import { Mixin } from 'vue-mixin-decorator'
    import Chart from '../../../../chart.js';

    @Mixin
    export default class MenuChartMixin {
        drawChart(id, data, interval, opts) {
            return new Chart(id, data, interval, opts).plot();
        }

        drawOptionalChart(id, data, interval, opts, displayOpts) {
            if (data !== undefined && data.length !== 0) {
                if (!this.drawChart(id, data, interval, opts).isEmpty()) {
                    return;
                }
            }

            try {
                displayOpts.displayFlags[displayOpts.key] = false;
            } catch(e) {
                // nOop
            }
        }

        createChartExportButton(btnLabel, title) {
            Chart.createChartExportButton(btnLabel, title);
        }

        formatPercentage(format, value) {
            return Chart.getFormatPercentage(format, value);
        }

        formatNetwork(format, value) {
            return Chart.getFormatNetwork(format, value);
        }

        formatMemory(format, value) {
            return Chart.getFormatMemory(format, value);
        }
    }
</script>
