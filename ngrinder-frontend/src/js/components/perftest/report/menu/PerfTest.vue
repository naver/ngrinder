<template>
    <div id="detail-report-perftest-menu">
        <div class="page-header">
            <h4 v-text="'Performance'"></h4>
            <button @click.prevent="downloadCSV" class="download-csv btn btn-primary pull-right">
                <i class="icon-download-alt icon-white"></i> <span v-text="i18n('perfTest.report.downloadCSV')"></span>
            </button>
        </div>

        <h6>
            <span v-text="'TPS'"></span>
            <span data-toggle="popover"
                  :data-content="i18n('perfTest.report.tps.help')"
                  :title="i18n('perfTest.report.tps')"
                  data-html="true" id="tps-title">
                <i class="icon-question-sign pointer-cursor"></i>
            </span>
        </h6>
        <div class="bigchart" ref="tpsChart" id="tps-chart"></div>

        <h6 v-text="`${i18n('perfTest.report.header.meantime')} (ms)`"></h6>
        <div class="chart" id="mean-time-chart"></div>

        <template v-if="optionalChart.meantimeToFirstByte">
            <h6 v-text="`${i18n('perfTest.report.header.meantimeToFirstByte')} (ms)`"></h6>
            <div class="chart" id="min-time-first-byte-chart"></div>
        </template>

        <h6 v-text="i18n('perfTest.report.header.vuser')"></h6>
        <div class="chart" id="vuser-chart"></div>

        <template v-if="optionalChart.userDefinedChart">
            <h6 v-text="i18n('perfTest.report.header.userDefinedChart')"></h6>
            <div class="chart" id="user-defined-chart"></div>
        </template>

        <h6 v-text="i18n('perfTest.report.header.errors')"></h6>
        <div class="chart" id="error-chart"></div>
    </div>
</template>

<script>
    import Base from '../../../Base.vue';
    import Component from 'vue-class-component';
    import Chart from '../../../../chart.js';

    @Component({
        name: 'perfTest',
        props: {
            id: {
                type: [Number, String],
                required: true,
            },
        },
    })
    export default class PerfTest extends Base {

        optionalChart = {
            meantimeToFirstByte: true,
            userDefinedChart: true,
        };

        mounted() {
            this.$http.get(`/perftest/api/${this.id}/perf`, {
                params: {
                    dataType : 'TPS,Errors,Mean_Test_Time_(ms),Mean_time_to_first_byte,User_defined,Vuser',
                    imgWidth : parseInt($(this.$refs.tpsChart).width()),
                },
            }).then(res => {
                const interval = res.data.chartInterval;
                this.drawChart('tps-chart', res.data.TPS.data, interval, res.data.TPS.labels);
                this.drawChart('mean-time-chart', res.data.Mean_Test_Time_ms.data, interval, res.data.Mean_Test_Time_ms.labels);
                this.drawChart('vuser-chart', res.data.Vuser.data, interval, res.data.Vuser.labels);
                this.drawChart('error-chart', res.data.Errors.data, interval, res.data.Errors.labels);

                if (!this.drawOptionalChart('min-time-first-byte-chart', res.data.Mean_time_to_first_byte.data,
                    interval, res.data.Mean_time_to_first_byte.labels)) {
                    this.optionalChart.meantimeToFirstByte = false;
                }

                if (!this.drawOptionalChart('user-defined-chart', res.data.User_defined.data, interval, res.data.User_defined.labels)) {
                    this.optionalChart.userDefinedChart = false;
                }

                Chart.createChartExportButton(this.i18n('perfTest.report.exportImg.button'), this.i18n('perfTest.report.exportImg.title'));
            }).catch((error) => console.error(error));

            $('[data-toggle="popover"]').popover('destroy');
            $('[data-toggle="popover"]').popover({trigger: 'hover', container: '#tps-title'});
        }

        drawChart(id, data, interval, labels) {
            new Chart(id, data, interval, { labels: labels }).plot();
        }

        drawOptionalChart(id, data, interval, labels) {
            if (data !== undefined && data.length !== 0) {
                this.drawChart(id, data, interval, labels);
                return true;
            }
            return false;
        }

        downloadCSV() {
            this.$http.get(`/perftest/${this.id}/download_csv`).catch((error) => console.error(error));
        }
    }
</script>

<style lang="less" scoped>
    #detail-report-perftest-menu {
        .download-csv {
            margin-top: -36px;
        }

        .icon-question-sign {
            vertical-align: middle;
        }

        div {
            &.chart {
                border: 1px solid #878988;
                height: 200px;
                min-width: 615px;
            }

            &.bigchart {
                border: 1px solid #878988;
                height: 300px;
                min-width: 615px;
            }
        }
    }
</style>
