<template>
    <div class="detail-report">
        <vue-headful :title="i18n('perfTest.report.title')"></vue-headful>
        <div class="navbar-inner">
            <h3 v-text="`${i18n('perfTest.report.reportPage')} ${test.testName}`"></h3>
        </div>
        <div class="content container">
            <form name="download_csv_form">
                <input type="hidden" id="test_id" name="testId" :value="test.id">
            </form>
            <div class="row">
                <div class="span3">
                    <table class="table table-bordered compactpadding">
                        <colgroup>
                            <col width="120px">
                            <col>
                        </colgroup>
                        <tr>
                            <th v-text="i18n('perfTest.report.totalVusers')"></th>
                            <td><strong v-text="test.vuserPerAgent * test.agentCount"></strong></td>
                        </tr>
                        <tr>
                            <th v-text="i18n('perfTest.report.agent')"></th>
                            <td><span v-text="test.agentCount"></span></td>
                        </tr>
                        <tr>
                            <th v-html="`${i18n('perfTest.report.process')}<br>${i18n('perfTest.report.thread')}`"></th>
                            <td class="process-thread-col" v-text="`${test.processes} / ${test.threads}`"></td>
                        </tr>
                        <tr>
                            <td colspan="2" class="divider"></td>
                        </tr>
                        <tr>
                            <th v-text="i18n('perfTest.report.ignoreSampleCount')"></th>
                            <td><span v-text="test.ignoreSampleCount"></span></td>
                        </tr>
                        <tr>
                            <td colspan=2></td>
                        </tr>
                        <tr>
                            <th v-text="'TPS'"></th>
                            <td><strong>{{ test.tps | numFormat('0.0') }}</strong></td>
                        </tr>
                        <tr>
                            <th v-text="i18n('perfTest.report.peakTPS')"></th>
                            <td><strong v-text="test.peakTps"></strong></td>
                        </tr>
                        <tr>
                            <th v-text="i18n('perfTest.report.meantime')"></th>
                            <td><span>{{ test.meanTestTime | numFormat('0.00')}}</span>&nbsp;&nbsp; <code>ms</code></td>
                        </tr>
                        <tr>
                            <th v-text="i18n('perfTest.report.totalTests')"></th>
                            <td v-text="test.tests + test.errors"></td>
                        </tr>
                        <tr>
                            <th v-text="i18n('perfTest.report.successfulTests')"></th>
                            <td v-text="test.tests"></td>
                        </tr>
                        <tr>
                            <th v-text="i18n('perfTest.report.errors')"></th>
                            <td v-text="test.error || 0"></td>
                        </tr>
                    </table>
                    <div class="well">
                        <ul class="nav nav-list">
                            <li class="active pointer-cursor perf nav-header">
                                <a class="pointer-cursor" v-text="i18n('perfTest.report.performanceReport')"></a>
                            </li>
                            <li class="nav-header" v-text="i18n('perfTest.report.targetHost')"></li>

                            <li v-for="ip in test.targetHostIP" class="monitor pointer-cursor" :ip="ip">
                                <a class="pointer-cursor" v-text="ip"></a>
                            </li>

                            <li  class="nav-header" v-text="i18n('perfTest.report.plugins')"></li>
                            <li v-for="plugin in plugins" class="plugin pointer-cursor" :plugin="plugin.first" :ip="plugin.second">
                                <a class="pointer-cursor" v-text="`${plugin.first.replace('_', ' ')} - ${plugin.second}`"></a>
                            </li>
                        </ul>
                    </div>
                </div>
                <div class="span9">
                    <table class="table table-bordered">
                        <colgroup>
                            <col width="120">
                            <col width="220">
                            <col width="120">
                            <col>
                        </colgroup>
                        <tr>
                            <th v-text="i18n('perfTest.report.startTime')"></th>
                            <td>{{ test.startTime | dateFormat('YYYY-MM-DD HH:mm:ss') }}</td>
                            <th v-text="i18n('perfTest.report.finishTime')"></th>
                            <td>{{ test.finishTime | dateFormat('YYYY-MM-DD HH:mm:ss') }}</td>
                        </tr>
                        <tr>
                            <template v-if="test.threshold === 'D'">
                                <th v-text="i18n('perfTest.report.duration')"></th>
                                <td><span v-text="test.duration"></span> &nbsp;<code>HH:MM:SS</code></td>
                            </template>
                            <template v-else>
                                <th v-text="i18n('perfTest.report.runCount')"></th>
                                <td><span v-text="test.runCount"></span></td>
                            </template>
                            <th v-text="i18n('perfTest.report.runtime')"></th>
                            <td><span v-text="test.runtime"></span> &nbsp;<code>HH:MM:SS</code></td>
                        </tr>
                        <tr v-if="test.description">
                            <th v-text="i18n('common.label.description')"></th>
                            <td colspan="3" v-text="test.description.replace(/\n/g, '<br>')"></td>
                        </tr>
                        <tr v-if="test.testComment">
                            <th v-text="i18n('perfTest.report.testComment')"></th>
                            <td colspan="3" v-text="test.testComment.replace(/\n/g, '<br>')"></td>
                        </tr>
                    </table>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import Base from '../../Base.vue';
    import vueHeadful from 'vue-headful';
    import Component from 'vue-class-component';
    import ControlGroup from '../../common/ControlGroup.vue';

    @Component({
        name: 'detailReport',
        components: { ControlGroup, vueHeadful },
    })
    export default class DetailReport extends Base {
        test = {};
        plugins = [];

        created() {
            this.$http.get(`/perftest/api/${this.$route.params.id}/detail_report`).then(res => {
                this.test = res.data.test;
                this.plugins = res.data.plugins;
            }).catch((error) => console.error(error));
        }
    }
</script>

<style lang="less" scoped>
    .detail-report {
        margin-bottom: 80px;
        .content {
            th, td {
                font-size: 12px;
            }

            .process-thread-col {
                vertical-align: middle;
            }
        }

        .compactpadding {
            th {
                padding: 8px 5px;
                vertical-align: middle;
            }
        }

        .well {
            max-width: 340px;
            padding: 8px 0;
        }

        .navbar-inner {
            width: 912px;
            margin-left: auto;
            margin-right: auto;
            margin-bottom: 0;
        }

        .span9 {
            .table {
                margin-bottom: 35px;
            }
        }
    }
</style>
