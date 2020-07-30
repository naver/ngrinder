<template>
    <div class="detail-report-perftest-menu">
        <div class="pb-2 mt-4 mb-3 border-bottom">
            <h4 v-text="'Performance'"></h4>
            <button @click.prevent="downloadCSV" class="download-csv btn btn-primary float-right">
                <i class="fa fa-download mr-1"></i>
                <span v-text="i18n('perfTest.report.downloadCSV')"></span>
            </button>
        </div>

        <div v-if="interval">
            <div v-show="tpsChart">
                <h6>
                    <span v-text="'TPS'"></span>
                    <span data-toggle="popover"
                          data-trigger="hover"
                          :data-content="i18n('perfTest.report.tps.help')"
                          :title="i18n('perfTest.report.tps')">
                        <i class="fa fa-question-circle"></i>
                    </span>
                </h6>
                <div class="bigchart" id="tps-chart"></div>
            </div>

            <div v-show="meanTimeChart">
                <h6 v-text="`${i18n('perfTest.report.header.meantime')} (ms)`"></h6>
                <div class="chart" id="mean-time-chart"></div>
            </div>

            <div v-show="meanTimeToFirstByteChart">
                <h6 v-text="`${i18n('perfTest.report.header.meantimeToFirstByte')} (ms)`"></h6>
                <div class="chart" id="min-time-first-byte-chart"></div>
            </div>

            <div v-show="vuserChart">
                <h6 v-text="i18n('perfTest.report.header.vuser')"></h6>
                <div class="chart" id="vuser-chart"></div>
            </div>

            <div v-show="userDefinedData.length">
                <template v-for="(data, index) in userDefinedData">
                    <h6 v-text="data.title"></h6>
                    <div class="chart" :id="`user-defined-chart-${index}`"></div>
                </template>
            </div>

            <div v-show="errorChart">
                <h6 v-text="i18n('perfTest.report.header.errors')"></h6>
                <div class="chart" id="error-chart"></div>
            </div>
        </div>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import Base from '../../../Base.vue';
    import MessagesMixin from '../../../common/mixin/MessagesMixin.vue';
    import ChartMixin from '../../../common/mixin/ChartMixin.vue';

    @Component({
        name: 'perfTest',
        props: {
            id: {
                type: [Number, String],
                required: true,
            },
        },
    })
    export default class PerfTest extends Mixins(Base, ChartMixin, MessagesMixin) {
        tpsChart = {};
        meanTimeChart = {};
        meanTimeToFirstByteChart = {};
        vuserChart = {};
        errorChart = {};
        userDefinedData = [];

        interval = 0;

        mounted() {
            this.showProgressBar();
            this.$http.get(`/perftest/api/${this.id}/perf`, {
                params: {
                    dataType: 'TPS,Errors,Mean_Test_Time_(ms),Mean_time_to_first_byte,User_defined,Vuser',
                    imgWidth: 960,
                },
            }).then(res => {
                this.interval = parseInt(res.data['chartInterval']);

                if (!this.interval) {
                    return;
                }

                const numOfTestRecord = Object.keys(res.data['TPS']).length;

                Object.entries(res.data).forEach(([key, value]) => {
                    if (key === 'User_defined') {
                        this.userDefinedData = this.processUserDefinedData(value, numOfTestRecord);
                        return;
                    }
                    res.data[key] = this.processData(value, key);
                });

                this.$nextTick(() => {
                    this.drawReportChart(res.data, this.interval);
                    $('[data-toggle="popover"]').popover();
                });
            }).catch(() => this.showErrorMsg(this.i18n('common.message.loading.error')))
              .finally(this.hideProgressBar);
        }

        drawReportChart(data, interval) {
            this.tpsChart = this.drawChart('tps-chart', data['TPS'], interval);
            this.meanTimeChart = this.drawChart('mean-time-chart', data['Mean_Test_Time_(ms)'], interval);
            this.meanTimeToFirstByteChart = this.drawChart('min-time-first-byte-chart', data['Mean_time_to_first_byte'], interval);
            this.vuserChart = this.drawChart('vuser-chart', data['Vuser'], interval);
            this.errorChart = this.drawChart('error-chart', data['Errors'], interval);
            this.$nextTick(() => {
                this.userDefinedData.forEach((each, index) => this.drawChart(`user-defined-chart-${index}`, each.data, interval));
            });
        }

        downloadCSV() {
            location.href = `${this.contextPath}/perftest/${this.id}/download_csv`;
        }
    }
</script>

<style lang="less" scoped>
    .detail-report-perftest-menu {

        .download-csv {
            margin-top: -36px;
        }

        .icon-question-sign {
            vertical-align: middle;
        }

        div {
            &.bigchart, &.chart {
                border: 1px solid #c4c4c4;
                height: 300px;
                width: 960px;
                margin-bottom: 20px;

                &.chart {
                    height: 200px;
                }
            }
        }
    }
</style>
