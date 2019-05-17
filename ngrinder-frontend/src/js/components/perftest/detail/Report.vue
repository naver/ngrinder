<template>
    <div v-show="dataLoadFinished" id="report-container">
        <div class="row">
            <div ref="" class="span4 intro" data-step="4" :data-intro="i18n('intro.report.summary')">
                <fieldset>
                    <legend v-text="i18n('perfTest.report.summary')"></legend>
                </fieldset>
                <div class="summary form-horizontal form-horizontal-3">
                    <fieldset>
                        <control-group lable_extra_class="control-label-wide non-cursor" labelMessageKey="perfTest.report.totalVusers">
                            <strong v-text="report.test.agentCount * report.test.vuserPerAgent"></strong>
                        </control-group>

                        <control-group lable_extra_class="control-label-wide non-cursor" labelMessageKey="perfTest.report.tps">
                            <strong v-text="report.test.tps.toFixed(1)"></strong>
                        </control-group>

                        <control-group lable_extra_class="control-label-wide non-cursor" labelMessageKey="perfTest.report.peakTPS">
                            <span v-text="report.test.peakTps.toFixed(1)"></span>
                        </control-group>

                        <control-group lable_extra_class="control-label-wide non-cursor" labelMessageKey="perfTest.report.meantime">
                            <span v-text="report.test.meanTestTime.toFixed(2)"></span>
                            <code>MS</code>
                        </control-group>

                        <control-group lable_extra_class="control-label-wide non-cursor" labelMessageKey="perfTest.report.totalTests">
                            <span v-text="report.test.tests + report.test.errors"></span>
                        </control-group>

                        <control-group lable_extra_class="control-label-wide non-cursor" labelMessageKey="perfTest.report.successfulTests">
                            <span v-text="report.test.tests"></span>
                        </control-group>

                        <control-group lable_extra_class="control-label-wide non-cursor" labelMessageKey="perfTest.report.errors">
                            <span v-text="report.test.errors"></span>
                        </control-group>

                        <control-group lable_extra_class="control-label-wide non-cursor" labelMessageKey="perfTest.report.runtime">
                            <span v-text="report.test.runtime"></span>
                        </control-group>
                    </fieldset>
                </div>
            </div>
            <div class="span8 intro" data-step="5" :data-intro="i18n('intro.report.tpsGraph')">
                <fieldSet>
                    <legend>
                        <span v-text="i18n('perfTest.report.tpsGraph')"></span>
                        <a @click="detailReport" class="btn btn-primary pull-right" v-text="i18n('perfTest.report.detailedReport')"></a>
                    </legend>
                </fieldSet>
                <div id="tps-chart" class="chart"></div>
            </div>
        </div>
        <div class="row">
            <div class="span4">
                <fieldSet>
                    <legend>
                        <span v-text="i18n('perfTest.report.logs')"></span>
                        <span class="log-comment"
                              data-toggle="popover"
                              data-html="true"
                              :data-content="i18n('perfTest.report.logs.help')"
                              :title="i18n('perfTest.report.logs')">
                            <i class="icon-question-sign pointer-cursor"></i>
                        </span>
                    </legend>
                </fieldSet>
                <div>
                    <div v-for="log in logs" class="ellipsis logs-container">
                        <a :href="`/perftest/${report.test.id}/show_log/${log}`" target="log" title="open the log in the new window">
                            <img src="/img/open_external.png"></a>
                        <a :href="`/perftest/${report.test.id}/download_log/${log}`" v-text="log"></a>
                    </div>
                </div>
                <div v-if="logs.length <= 0" v-text="i18n('perfTest.report.message.noLog')"></div>
            </div>
            <div class="span8 intro commend-container" data-step="6" :data-intro="i18n('intro.report.testComment')">
                <fieldSet>
                    <legend>
                        <span v-text="i18n('perfTest.report.testComment')"></span>
                        <a @click="leaveComment" class="btn btn-primary pull-right" v-text="i18n('perfTest.report.leaveComment')"></a>
                    </legend>
                </fieldSet>
                <textarea v-model="report.test.testComment" rows="3"></textarea>
            </div>
        </div>
    </div>
</template>

<script>
    import Base from '../../Base.vue';
    import Component from 'vue-class-component';
    import ControlGroup from '../../common/ControlGroup.vue';
    import Chart from '../../../chart.js';

    @Component({
        name: 'report',
        components: { ControlGroup },
    })
    export default class Report extends Base {
        report = {
            test: {
                tps: 0,
                meanTestTime: 0,
                peakTps: 0,
            },
            tps: [],
            interval: 0,
        };

        dataLoadFinished = false;
        logs = [];

        created() {
            this.fetchReportData();
        }

        detailReport() {
            // TODO
        }

        fetchReportData() {
            if (!this.$route.params.id) {
                return;
            }

            this.$http.get(`/perftest/api/${this.$route.params.id}/basic_report?imgWidth=600`)
                .then(res => {
                    this.logs = res.data.logs;
                    Object.assign(this.report.test, res.data.test);
                    this.report.interval = res.data.chartInterval;
                    this.report.tps = res.data.tps;
                    this.dataLoadFinished = true;
                    this.$nextTick(() => {
                        $('[data-toggle="popover"]').popover('destroy');
                        $('[data-toggle="popover"]').popover({trigger: 'hover', container: '#report-container'});
                        new Chart('tps-chart', [this.report.tps], this.report.interval).plot();
                    });
                }).catch((error) => console.log(error));
        }

        leaveComment() {
            this.$http.post(`/perftest/api/${this.$route.params.id}/leave_comment`, {
                testComment: this.report.test.testComment,
                tagString: this.$parent.selectedTag,
            }).then(res => {
                if (res.data.success) {
                    alert(this.i18n('perfTest.report.message.leaveComment'));
                }
            }).catch((error) => console.log(error));
        }

    }
</script>

<style lang="less">
    #report-container {
        .control-label {
            width: 170px;
        }
    }
</style>

<style lang="less" scoped>
    #report-container {
        .log-comment {
            margin-top: 10px;
            margin-left: 10px;
        }

        .commend-container {
            textarea {
                width: 620px;
                resize: none;
            }
        }

        .control-group {
            label {
                &.control-label {
                    width: 170px
                }
            }
        }

        .logs-container {
            width: 100%;

            img {
                margin-top: -3px;
            }
        }

        .summary {
            margin-left: 10px;
        }

        .chart {
            width: 610px;
            height: 300px;
            border: 1px solid #666;
        }
    }
</style>
