<template>
    <div v-show="dataLoadFinished" id="report-container">
        <div class="row m-0">
            <div class="intro summary-chart-container"
                 :data-step="shownBsTab ? 4 : undefined"
                 :data-intro="shownBsTab ? i18n('intro.report.summary') : undefined">
                <fieldset>
                    <legend class="border-bottom" v-text="i18n('perfTest.report.summary')"></legend>
                </fieldset>
                <div class="summary form-horizontal form-horizontal-3">
                    <fieldset>
                        <control-group lable_extra_class="control-label-wide non-cursor" labelMessageKey="perfTest.report.totalVusers">
                            <strong>{{ report.test.agentCount * report.test.vuserPerAgent | numFormat }}</strong>
                        </control-group>

                        <control-group lable_extra_class="control-label-wide non-cursor" labelMessageKey="perfTest.report.tps">
                            <strong>{{ report.test.tps | numFormat('0,0.0') }}</strong>
                        </control-group>

                        <control-group lable_extra_class="control-label-wide non-cursor" labelMessageKey="perfTest.report.peakTPS">
                            <span>{{ report.test.peakTps | numFormat('0,0.0') }}</span>
                        </control-group>

                        <control-group lable_extra_class="control-label-wide non-cursor" labelMessageKey="perfTest.report.meantime">
                            <span>{{ report.test.meanTestTime | numFormat('0,0.00') }}</span>
                            <code>MS</code>
                        </control-group>

                        <control-group lable_extra_class="control-label-wide non-cursor" labelMessageKey="perfTest.report.totalTests">
                            <span>{{ report.test.tests + report.test.errors | numFormat }}</span>
                        </control-group>

                        <control-group lable_extra_class="control-label-wide non-cursor" labelMessageKey="perfTest.report.successfulTests">
                            <span>{{ report.test.tests | numFormat }}</span>
                        </control-group>

                        <control-group lable_extra_class="control-label-wide non-cursor" labelMessageKey="perfTest.report.errors">
                            <span>{{ report.test.errors | numFormat }}</span>
                        </control-group>

                        <control-group lable_extra_class="control-label-wide non-cursor" labelMessageKey="perfTest.report.runtime">
                            <span v-text="report.test.runtime"></span>
                        </control-group>
                    </fieldset>
                </div>
            </div>
            <div class="pl-4 intro"
                 :data-step="shownBsTab ? 5 : undefined"
                 :data-intro="shownBsTab ? i18n('intro.report.tpsGraph') : undefined">
                <fieldSet>
                    <legend class="border-bottom">
                        <span v-text="i18n('perfTest.report.tpsGraph')"></span>
                        <router-link :to="`/perftest/${report.test.id}/detail_report`"
                                     class="btn btn-primary float-right"
                                     target="_blank">
                            <i class="fa fa-file mr-1"></i>
                            <span v-text="i18n('perfTest.report.detailedReport')"></span>
                        </router-link>
                    </legend>
                </fieldSet>
                <div id="tps-chart"></div>
            </div>
        </div>
        <div class="row m-0">
            <div class="log-container">
                <fieldSet>
                    <legend class="border-bottom">
                        <span v-text="i18n('perfTest.report.logs')"></span>
                        <span class="log-comment"
                              data-toggle="popover"
                              data-trigger="hover"
                              :data-content="i18n('perfTest.report.logs.help')"
                              :title="i18n('perfTest.report.logs')">
                            <i class="fa fa-question-circle pointer-cursor"></i>
                        </span>
                    </legend>
                </fieldSet>
                <div>
                    <div v-for="log in logs" class="ellipsis w-100">
                        <a :href="`${contextPath}/perftest/${report.test.id}/show_log/${log}`" target="log" title="open the log in the new window">
                            <img :src="`${contextPath}/img/open_external.png`"></a>
                        <a :href="`${contextPath}/perftest/${report.test.id}/download_log/${log}`" v-text="log"></a>
                    </div>
                </div>
                <div v-if="logs.length <= 0" v-text="i18n('perfTest.report.message.noLog')"></div>
            </div>
            <div class="pl-4 intro comment-container"
                 :data-step="shownBsTab ? 6 : undefined"
                 :data-intro="shownBsTab ? i18n('intro.report.testComment') : undefined">
                <fieldSet>
                    <legend class="border-bottom">
                        <span v-text="i18n('perfTest.report.testComment')"></span>
                        <button type="button" @click="leaveComment" class="btn btn-primary float-right">
                            <i class="fa fa-comment mr-1"></i>
                            <span v-text="i18n('perfTest.report.leaveComment')"></span>
                        </button>
                    </legend>
                </fieldSet>
                <textarea class="form-control" v-model="report.test.testComment" rows="3"></textarea>
            </div>
        </div>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import Base from '../../Base.vue';
    import ControlGroup from '../../common/ControlGroup.vue';
    import MessageMixin from '../../common/mixin/MessagesMixin.vue';
    import ChartMixin from '../../common/mixin/ChartMixin.vue';

    @Component({
        name: 'report',
        props: {
            id: {
                type: String,
                default: '',
            },
        },
        components: { ControlGroup },
    })
    export default class Report extends Mixins(Base, MessageMixin, ChartMixin) {
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
        shownBsTab = false;
        logs = [];

        fetchReportData() {
            if (!this.id) {
                return;
            }

            this.$http.get(`/perftest/api/${this.id}/basic_report?imgWidth=600`)
                .then(res => {
                    this.logs = res.data.logs;
                    Object.assign(this.report.test, res.data.test);
                    this.report.interval = res.data.chartInterval;
                    this.report.tps = res.data.tps;
                    this.dataLoadFinished = true;
                    this.$nextTick(() => {
                        $('[data-toggle="popover"]').popover();
                        this.drawChart('tps-chart', { Total: this.report.tps }, this.report.interval, null, { legend: { show: false } });
                    });
                }).catch(() => this.showErrorMsg(this.i18n('perfTest.report.message.fetch.basicReport.error')));
        }

        leaveComment() {
            this.$http.post(`/perftest/api/${this.id}/leave_comment`, {
                testComment: this.report.test.testComment,
                tagString: this.$parent.selectedTag,
            }).then(res => {
                if (res.data.success) {
                    this.showSuccessMsg(this.i18n('perfTest.report.message.leaveComment'));
                }
            }).catch(() => this.showErrorMsg(this.i18n('perfTest.report.message.leaveComment.error')));
        }
    }
</script>

<style lang="less">
    #report-container {
        .control-label {
            width: 170px;
        }

        .controls {
            margin-top: 0;
        }
    }
</style>

<style lang="less" scoped>
    #report-container {
        .summary-chart-container {
            width: 400px;
        }

        .border-bottom {
            margin-bottom: 15px;
        }

        .log-container {
            width: 400px;

            img {
                margin-top: -3px;
            }

            .log-comment {
                margin-top: 10px;
                margin-left: 10px;
            }
        }

        .comment-container {
            height: 150px;
            width: 800px;

            textarea {
                height: 84px;
                resize: none;
            }
        }

        .control-group {
            padding-top: 6px;

            label {
                &.control-label {
                    width: 170px
                }
            }
        }

        #tps-chart {
            width: 780px;
            height: 300px;
            border: 1px solid #c4c4c4;
        }
    }
</style>
