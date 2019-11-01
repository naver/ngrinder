<template>
    <div class="detail-report-perftest-menu">
        <div class="pb-2 mt-4 mb-3 border-bottom">
            <h4 v-text="'Performance'"></h4>
            <button @click.prevent="downloadCSV" class="download-csv btn btn-primary float-right">
                <i class="fa fa-download mr-1"></i>
                <span v-text="i18n('perfTest.report.downloadCSV')"></span>
            </button>
        </div>

        <h6>
            <span v-text="'TPS'"></span>
            <span data-toggle="popover"
                  data-html="true"
                  data-trigger="hover"
                  :data-content="i18n('perfTest.report.tps.help')"
                  :title="i18n('perfTest.report.tps')">
                <i class="fa fa-question-circle"></i>
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
        optionalChart = {
            meantimeToFirstByte: true,
            userDefinedChart: true,
        };

        mounted() {
            this.$http.get(`/perftest/api/${this.id}/perf`, {
                params: {
                    dataType: 'TPS,Errors,Mean_Test_Time_(ms),Mean_time_to_first_byte,User_defined,Vuser',
                    imgWidth: parseInt(this.$refs.tpsChart.offsetWidth),
                },
            }).then(res => {
                const interval = res.data.chartInterval;

                this.drawChart('tps-chart', 'TPS', res.data.TPS, interval);
                this.drawChart('mean-time-chart', 'Mean_Test_Time_ms', res.data.Mean_Test_Time_ms, interval);
                this.drawChart('vuser-chart', 'Vuser', res.data.Vuser, interval);
                this.drawChart('error-chart', 'Errors', res.data.Errors, interval);

                if (res.data.Mean_time_to_first_byte && res.data.Mean_time_to_first_byte.length > 0) {
                    this.drawChart('min-time-first-byte-chart', 'Mean_time_to_first_byte', res.data.Mean_time_to_first_byte, interval);
                } else {
                    this.optionalChart.meantimeToFirstByte = false;
                }

                if (res.data.User_defined && res.data.User_defined.length > 0) {
                    this.drawChart('user-defined-chart', 'User_defined', res.data.User_defined, interval);
                } else {
                    this.optionalChart.userDefinedChart = false;
                }
            }).catch(() => this.showErrorMsg(this.i18n('common.message.loading.error')));

            $('[data-toggle="popover"]').popover();
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
                border: 1px solid #878988;
                height: 300px;
                min-width: 615px;
                margin-bottom: 20px;

                &.chart {
                    height: 200px;
                }
            }
        }
    }
</style>
