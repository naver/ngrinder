<template>
    <main class="detail-report">
        <header>
            <vue-headful :title="i18n('perfTest.report.title')"></vue-headful>
            <h3 v-text="`${i18n('perfTest.report.reportPage')} ${test.testName}`"></h3>
        </header>
        <div class="container p-0">
            <div class="row">
                <div class="report-summary-container">
                    <table class="table table-bordered compact-padding">
                        <colgroup>
                            <col width="120px">
                            <col>
                        </colgroup>
                        <tr>
                            <th v-text="i18n('perfTest.report.totalVusers')"></th>
                            <td><strong>{{ test.vuserPerAgent * test.agentCount | numFormat }}</strong></td>
                        </tr>
                        <tr>
                            <th v-text="i18n('perfTest.report.agent')"></th>
                            <td><span v-text="test.agentCount"></span></td>
                        </tr>
                        <tr>
                            <th class="pre-wrap" v-text="`${i18n('perfTest.report.process')}\n${i18n('perfTest.report.thread')}`"></th>
                            <td class="process-thread-col" v-text="`${test.processes} / ${test.threads}`"></td>
                        </tr>
                        <tr>
                            <td colspan="2" class="divider"></td>
                        </tr>
                        <tr>
                            <th v-text="i18n('perfTest.report.ignoreSampleCount')"></th>
                            <td><span>{{ test.ignoreSampleCount | numFormat }}</span></td>
                        </tr>
                        <tr>
                            <td colspan=2></td>
                        </tr>
                        <tr>
                            <th v-text="'TPS'"></th>
                            <td><strong>{{ test.tps | numFormat('0,0.0') }}</strong></td>
                        </tr>
                        <tr>
                            <th v-text="i18n('perfTest.report.peakTPS')"></th>
                            <td><strong>{{ test.peakTps | numFormat }}</strong></td>
                        </tr>
                        <tr>
                            <th v-text="i18n('perfTest.report.meantime')"></th>
                            <td>
                                <span>{{ test.meanTestTime | numFormat('0,0.00') }}</span>
                                <code class="ml-1">ms</code>
                            </td>
                        </tr>
                        <tr>
                            <th v-text="i18n('perfTest.report.totalTests')"></th>
                            <td>{{ test.tests + test.errors | numFormat }}</td>
                        </tr>
                        <tr>
                            <th v-text="i18n('perfTest.report.successfulTests')"></th>
                            <td>{{ test.tests | numFormat }}</td>
                        </tr>
                        <tr>
                            <th v-text="i18n('perfTest.report.errors')"></th>
                            <td>{{ test.errors | numFormat }}</td>
                        </tr>
                    </table>
                    <div class="card bg-light">
                        <ul class="nav flex-column">
                            <li class="nav-item active pl-3" ref="perftestNavMenu" :class="{ 'mb-1': test.targetHosts }">
                                <a href="#" class="nav-link px-0" @click="showPerftestMenu($event)" v-text="i18n('perfTest.report.performanceReport')"></a>
                            </li>

                            <template v-if="test.targetHosts">
                                <li class="nav-item mb-1 pl-3" v-text="i18n('perfTest.report.targetHost')"></li>
                                <li v-for="targetHost in test.targetHosts.split(',')" class="monitor pt-1">
                                    <a href="#" @click="showMonitorMenu($event, targetHost)" class="nav-link py-0 ml-3 pb-1" v-text="targetHost"></a>
                                </li>
                            </template>
                        </ul>
                    </div>
                </div>
                <div class="report-chart-container">
                    <table class="table table-bordered">
                        <colgroup>
                            <col width="120">
                            <col>
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
                                <td>
                                    <span>{{ test.duration | durationFormat('HH:mm:ss') }}</span>
                                    <code class="ml-1">HH:MM:SS</code>
                                </td>
                            </template>
                            <template v-else>
                                <th v-text="i18n('perfTest.report.runCount')"></th>
                                <td><span v-text="test.runCount"></span></td>
                            </template>
                            <th v-text="i18n('perfTest.report.runtime')"></th>
                            <td>
                                <span v-text="test.runtime"></span>
                                <code class="ml-1">HH:MM:SS</code>
                            </td>
                        </tr>
                        <tr v-if="test.description">
                            <th v-text="i18n('common.label.description')"></th>
                            <td class="pre-wrap" colspan="3" v-text="test.description"></td>
                        </tr>
                        <tr v-if="test.testComment">
                            <th v-text="i18n('perfTest.report.testComment')"></th>
                            <td class="pre-wrap" colspan="3" v-text="test.testComment"></td>
                        </tr>
                    </table>
                    <div>
                        <component :key="props.targetIP" :is="currentMenuComponent" v-bind="props"></component>
                    </div>
                </div>
            </div>
        </div>
    </main>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import vueHeadful from 'vue-headful';
    import Component from 'vue-class-component';
    import Base from '../../Base.vue';
    import ControlGroup from '../../common/ControlGroup.vue';
    import PerfTest from './menu/PerfTest.vue';
    import Monitor from './menu/Monitor.vue';
    import MessagesMixin from '../../common/mixin/MessagesMixin.vue';

    @Component({
        name: 'detailReport',
        components: { ControlGroup, vueHeadful },
        props: {
            id: {
                type: String,
                required: true,
            },
        },
    })
    export default class DetailReport extends Mixins(Base, MessagesMixin) {
        props = {};
        test = {
            targetHosts: '',
        };
        plugins = [];
        currentActiveNavMenu = null;
        currentMenuComponent = PerfTest;

        created() {
            this.props.id = this.id;
        }

        mounted() {
            this.showProgressBar();
            this.currentActiveNavMenu = this.$refs.perftestNavMenu;
            this.$http.get(`/perftest/api/${this.id}/detail_report`).then(res => {
                this.test = res.data.test;
                this.plugins = res.data.plugins;
            })
            .catch(() => this.showErrorMsg(this.i18n('common.message.loading.error'), { content: this.i18n('perfTest.report.detailedReport') }))
            .finally(this.hideProgressBar);
        }

        showMonitorMenu($event, targetHost) {
            if (!this.switchActiveNavMenu($event.target.parentElement)) {
                return;
            }

            this.props = {
                id: this.id,
                targetIP: (targetHost.indexOf(':') === -1) ? targetHost : targetHost.split(':')[1],
            };
            this.currentMenuComponent = Monitor;
        }

        showPerftestMenu($event) {
            if (!this.switchActiveNavMenu($event.target.parentElement)) {
                return;
            }

            this.props = {
                id: this.id,
            };
            this.currentMenuComponent = PerfTest;
        }

        switchActiveNavMenu(target) {
            if (this.currentActiveNavMenu === target) {
                return false;
            }
            target.classList.add('active');
            this.currentActiveNavMenu.classList.remove('active');
            this.currentActiveNavMenu = target;
            return true;
        }
    }
</script>

<style lang="less" scoped>
    .detail-report {
        header {
            display: flex;
            align-items: center;
            height: 60px;
            width: 1200px;
            margin: 0 auto 20px auto;
            min-height: 40px;
            padding-left: 20px;
            padding-right: 20px;
            background-color: #fafafa;
            background-image: -moz-linear-gradient(top, #ffffff, #f2f2f2);
            background-image: -webkit-gradient(linear, 0 0, 0 100%, from(#ffffff), to(#f2f2f2));
            background-image: -webkit-linear-gradient(top, #ffffff, #f2f2f2);
            background-image: -o-linear-gradient(top, #ffffff, #f2f2f2);
            background-image: linear-gradient(to bottom, #ffffff, #f2f2f2);
            background-repeat: repeat-x;
            filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#ffffffff', endColorstr='#fff2f2f2', GradientType=0);
            border: 1px solid #d4d4d4;
            -webkit-border-radius: 4px;
            -moz-border-radius: 4px;
            border-radius: 4px;
            -webkit-box-shadow: 0 1px 4px rgba(0, 0, 0, 0.065);
            -moz-box-shadow: 0 1px 4px rgba(0, 0, 0, 0.065);
            box-shadow: 0 1px 4px rgba(0, 0, 0, 0.065);
        }

        .container {
            th, td {
                font-size: 12px;

                strong {
                    color: #6DAFCF;
                }
            }

            .process-thread-col {
                vertical-align: middle;
            }
        }

        .compact-padding {
            th {
                padding: 8px 5px;
                vertical-align: middle;
            }
        }

        .report-summary-container {
            width: 220px;

            .nav {
                a {
                    text-decoration: none;
                }

                li {
                    &.active {
                        background-color: #0069d9;

                        a {
                            color: #fff;
                        }
                    }
                }
            }
        }

        .report-chart-container {
            margin-left: 20px;
            width: 960px;

            .table {
                margin-bottom: 35px;
            }
        }
    }
</style>
