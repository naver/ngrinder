<template>
    <div class="row running" id="running-container">
        <div class="span5 intro" data-step="4" :data-intro="i18n('intro.running.summary')">
            <fieldSet>
                <legend v-text="i18n('perfTest.running.summaryTitle')"></legend>
            </fieldSet>
            <div class="content-left form-horizontal form-horizontal-3">
                <fieldset>
                    <control-group labelMessageKey="perfTest.running.totalVusers">
                        <strong v-text="test.vuserPerAgent * test.agentCount"></strong>
                        <span class="badge badge-info pull-right">
                            <span v-text="i18n('perfTest.running.running')"></span>
                            <span v-text="runningThread"></span>
                        </span>
                    </control-group>

                    <control-group labelMessageKey="perfTest.running.totalProcesses">
                        <span v-text="test.processes * test.agentCount"></span>
                        <span class="badge badge-info pull-right">
                            <span v-text="i18n('perfTest.running.running')"></span>
                            <span v-text="runningProcess"></span>
                        </span>
                    </control-group><hr>

                    <control-group labelMessageKey="perfTest.config.targetHost" labelStyle="width: 120px;">
                        <template v-if="test.targetHosts" v-for="host in test.targetHosts.split(',')">
                            <span v-text="host.trim()"></span><br/>
                        </template>
                    </control-group><hr>

                    <div class="control-group">
                        <control-group v-if="test.threshold === 'D'" labelMessageKey="perfTest.running.duration" labelStyle="width: 130px;">
                            <span v-text="test.duration"></span>
                            <code>HH:MM:SS</code>
                            <span class="badge badge-success pull-right">
                                <span v-text="i18n('perfTest.running.runCount')"></span><span v-text="(totalStatistics.Tests + totalStatistics.Errors).toFixed(0)"></span>
                            </span>
                        </control-group>
                        <control-group v-else labelMessageKey="perfTest.running.totalRunCount">
                            <span v-text="test.runCount * test.agentCount * test.vuserPerAgent"></span>
                            <span class="badge badge-success pull-right">
                                <span v-text="i18n('perfTest.running.runCount')"></span><span v-text="(totalStatistics.Tests + totalStatistics.Errors).toFixed(0)"></span>
                            </span>
                        </control-group>
                    </div>
                    <div class="control-group">
                        <label class="control-label" v-text="i18n('perfTest.running.targetState')"></label>
                    </div>
                    <div class="control-group">
                        <div id="monitor-state">
                            <ul v-for="(monitor, name) in monitorState">
                                <li>
                                    <div class="ellipsis">
                                        <span><b v-text="getShortenString(name)"></b></span>
                                        <span v-text="getPackageState(monitor)"></span>
                                    </div>
                                </li>
                            </ul>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" v-text="i18n('perfTest.running.agentState')"></label>
                    </div>
                    <div class="control-group">
                        <div id="agent-state">
                            <ul v-for="(agent, name) in agentState">
                                <li>
                                    <div class="ellipsis">
                                        <span><b v-text="getShortenString(name)"></b></span>
                                        <span v-text="getPackageState(agent)"></span>
                                    </div>
                                </li>
                            </ul>
                        </div>
                    </div>
                </fieldset>
            </div>
        </div>
        <!-- end running content left -->

        <div class="span7">
            <fieldSet>
                <legend>
                    <span v-text="i18n('perfTest.running.tpsGraph')"></span>
                    <span id="running_time" class="badge badge-success" v-text="formatTestTime(testTime)"></span>
                    <a @click.prevent="stopRunningTest" class="btn btn-danger pull-right intro" data-step="5"
                       :data-intro="i18n('intro.running.stopButton')" v-text="i18n('common.button.stop')"></a>
                </legend>
            </fieldSet>
            <div id="running-tps-chart" class="chart"></div>
            <div class="tabbable intro" data-step="6" :data-intro="i18n('intro.running.accumulated')">
                <ul class="nav nav-tabs" id="sample_tab">
                    <li class="active">
                        <a href="#last-sample-tab" data-toggle="tab" v-text="i18n('perfTest.running.latestSample')"></a>
                    </li>
                    <li>
                        <a href="#accumulated-sample-tab" data-toggle="tab" v-text="i18n('perfTest.running.accumulatedStatistic')"></a>
                    </li>
                </ul>
                <div class="tab-content">
                    <div class="tab-pane active" id="last-sample-tab">
                        <sampling-table :statistics="lastSampleStatistics"></sampling-table>
                    </div>
                    <div class="tab-pane" id="accumulated-sample-tab">
                        <sampling-table :statistics="cumulativeStatistics"></sampling-table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import Base from '../../Base.vue';
    import ControlGroup from '../../common/ControlGroup.vue';
    import MessagesMixin from '../../common/mixin/MessagesMixin.vue';
    import SamplingTable from './SamplingTable.vue';
    import Chart from '../../../chart.js';
    import Queue from '../../../queue.js';
    import FormatMixin from '../mixin/FormatMixin.vue';

    @Component({
        name: 'running',
        props: {
            testProps: {
                type: Object,
                required: true,
            },
        },
        components: { ControlGroup, SamplingTable },
    })
    export default class Running extends Mixins(Base, FormatMixin, MessagesMixin) {
        lastSampleStatistics = [];
        cumulativeStatistics = [];
        totalStatistics = { Tests: 0, Errors: 0 };
        agentState = {};
        monitorState = {};
        samplingIntervalId = -1;
        runningProcess = 0;
        runningThread = 0;
        testTime = 0;

        tpsQueue = {};
        tpsChart = {};

        test = {};

        created() {
            this.test = this.testProps;
            this.tpsQueue = new Queue(60 / this.test.samplingInterval);
            this.tpsChart = new Chart('running-tps-chart', [this.tpsQueue.getArray()], this.test.samplingInterval);
        }

        startSamplingInterval() {
            this.updateSamplingData();
            this.samplingIntervalId = setInterval(this.updateSamplingData, 1000 * this.test.samplingInterval);
        }

        updateSamplingData() {
            if (!this.test.id) {
                return;
            }
            this.$http.get(`/perftest/api/${this.test.id}/sample`).then(res => {
                const perfTestSample = res.data.perf;
                if (perfTestSample) {
                    this.lastSampleStatistics = perfTestSample.lastSampleStatistics;
                    this.cumulativeStatistics = perfTestSample.cumulativeStatistics;
                    this.totalStatistics = perfTestSample.totalStatistics;
                    this.runningProcess = perfTestSample.process;
                    this.runningThread = perfTestSample.thread;
                    this.testTime = perfTestSample.testTime;
                    this.tpsQueue.enQueue(perfTestSample.tpsChartData);
                    this.tpsChart.plot();
                }
                this.agentState = res.data.agent || {};
                this.monitorState = res.data.monitor || {};
            }).catch(error => console.log(error));
        }

        stopRunningTest() {
            bootbox.confirm(this.i18n('perfTest.message.stop.confirm'), this.i18n('common.button.cancel'), this.i18n('common.button.ok'), result => {
                if (result) {
                    this.$http.put(`/perftest/api/${this.test.id}?action=stop`).then(res => {
                        if (res.data.success) {
                            this.showSuccessMsg(this.i18n('perfTest.message.stop.success'));
                        }
                    }).catch(() => this.showErrorMsg(this.i18n('perfTest.message.stop.error')));
                }
            });
        }

        // monitor or agent state
        getPackageState(targetPackage) {
            let packageState = `CPU-${this.formatPercentage(null, targetPackage.cpuUsedPercentage)}
            MEM-${this.formatPercentage(null, ((targetPackage.totalMemory - targetPackage.freeMemory) / targetPackage.totalMemory) * 100)}`;

            if (targetPackage.receivedPerSec !== 0 || targetPackage.sentPerSec !== 0) {
                packageState += ` RX-${this.formatNetwork(null, targetPackage.receivedPerSec)} TX-${this.formatNetwork(null, targetPackage.sentPerSec)}`;
            }
            return packageState;
        }

        getShortenString(str, start, end) {
            start = start || 0;
            end = end || 20;
            if (str.length >= end) {
                str = str.substr(start, end - 4);
                str += '...';
            }
            return str;
        }
    }
</script>

<style lang="less">
    #running-container {
        .control-label {
            width: 170px;
        }
    }
</style>

<style lang="less" scoped>
    #running-container {
        #running-tps-chart {
            width: 530px;
            height: 300px;
        }

        .content-left {
            margin-top: 10px;
        }

        #agent-state, #monitor-state {
            font-size: 12px;
            margin-left: -20px;

            li {
                height: 20px;
                div {
                    width: 100%;
                }
            }
        }

        .commend-container {
            textarea {
                width: 620px;
                resize: none;
            }
        }

        table {
            font-size: 12px;
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
            margin-bottom: 12px;
        }
    }
</style>
