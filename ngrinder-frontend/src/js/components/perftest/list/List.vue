<template>
    <div class="container perftest-list-container p-0">
        <vue-headful title="Performance Test"></vue-headful>
        <div class="img-unit"></div>
        <div class="current-running-status-container">
            <code v-text="runningSummary"></code>
        </div>

        <search-bar ref="searchBar" @filter-running="runQueryFilter" @filter-schduled="runQueryFilter" @create="$router.push('/perftest/new')"
                    @search="getPerfTest" @change-tag="getPerfTest" @delete-selected-tests="deleteTests(selectedTests.toString())"></search-bar>

        <table class="table table-bordered ellipsis">
            <colgroup>
                <col width="30">
                <col width="50">
                <col>
                <col>
                <col width="70">
                <col v-if="ngrinder.config.clustered" width="70">
                <col width="130">
                <col width="80">
                <col width="65">
                <col width="65">
                <col width="70">
                <col width="65">
                <col width="60">
            </colgroup>
            <thead>
                <tr>
                    <th>
                        <input type="checkbox" v-model="selectAll" class="checkbox" @change="changeSelectAll">
                    </th>
                    <th class="center status" data-step="4" :data-intro="i18n('intro.list.perftest.status')">
                        <span v-text="i18n('common.label.status')"></span>
                    </th>
                    <th>
                        <span v-text="i18n('perfTest.list.testName')"></span>
                    </th>
                    <th>
                        <span v-text="i18n('perfTest.list.scriptName')"></span>
                    </th>
                    <th>
                        <span v-if="isAdmin" v-text="i18n('perfTest.list.owner')"></span>
                        <span v-else v-text="i18n('perfTest.list.modifier.oneLine')"></span>
                    </th>
                    <th v-if="ngrinder.config.clustered">
                        <span v-text="i18n('common.region')"></span>
                    </th>
                    <th>
                        <span v-text="i18n('perfTest.list.startTime')"></span>
                    </th>
                    <th>
                        <span class="ellipsis"></span>
                        <span v-text="i18n('perfTest.list.threshold')"></span>
                    </th>
                    <th>
                        <span v-text="i18n('perfTest.list.tps')"></span>
                    </th>
                    <th :title="i18n('perfTest.list.meantime')" v-text="'MIT'"></th>
                    <th class="ellipsis">
                        <span v-text="i18n('perfTest.list.errorRate')"></span>
                    </th>
                    <th class="nothing small-border">
                        <span v-text="i18n('perfTest.list.vusers')"></span>
                    </th>
                    <th data-step="5" :data-intro="i18n('intro.list.perftest.actions')">
                        <span v-text="i18n('common.label.actions')"></span>
                    </th>
                </tr>
            </thead>
            <tbody>
                <template v-for="(test, index) in tests">
                    <tr>
                        <td class="center">
                            <input type="checkbox" v-model="selectedTests" :value="test.id" :disabled="!test.deletable">
                        </td>
                        <td class="center">
                            <a class="ball"
                               data-toggle="popover"
                               data-html="true"
                               data-trigger="hover"
                               :id="`ball_${test.id}`"
                               :title="test.status"
                               :data-content="`${test.progressMessage}<br><b>${test.lastProgressMessage}</b>`.replace(/\n/g, '<br>')">
                               <img :src="`/img/ball/${test.iconName}`">
                            </a>
                        </td>
                        <td class="ellipsis">
                            <div class="ellipsis"
                                 data-toggle="popover"
                                 data-html="true"
                                 data-trigger="hover"
                                 :title="test.testName"
                                 :data-content="getTestNamePopoverContent(test).replace(/\n/g, '<br>')">
                                <router-link :to="`/perftest/${test.id}`" target="_self" v-text="test.testName"></router-link>
                            </div>
                        </td>
                        <td class="ellipsis">
                            <div class="ellipsis"
                                 data-toggle="popover"
                                 data-html="true"
                                 data-trigger="hover"
                                 :title="i18n('perfTest.list.scriptName')"
                                 :data-content="`${test.scriptName}<br> - ${i18n('script.list.revision')} : ${(test.scriptRevision)}`">
                                <a v-if="isAdmin" :href="`/script/detail/${test.scriptName}?r=${(test.scriptRevision)}&ownerId=${(test.createdUserId)}`" v-text="test.scriptName"></a>
                                <a v-else :href="`/script/detail/${test.scriptName}?r=${(test.scriptRevision)}`" v-text="test.scriptName"></a>
                            </div>
                        </td>
                        <td>
                           <div class="ellipsis"
                                data-toggle="popover"
                                data-html="true"
                                data-trigger="hover"
                                :title="i18n('perfTest.list.participants')"
                                :data-content="getOwnerPopoverContent(test).replace(/\n/g, '<br>')">
                                <span v-if="isAdmin" v-text="test.createdUserName"></span>
                                <span v-else v-text="test.lastModifiedUserName"></span>
                            </div>
                        </td>
                        <td v-if="ngrinder.config.clustered" class="ellipsis" :title="i18n('common.region')" data-html="true">
                            <span v-text="test.region"></span>
                        </td>
                        <td>
                            <span v-if="test.startTime">{{ test.startTime | dateFormat('YYYY-MM-DD HH:mm') }}</span>
                        </td>
                        <td>
                            <div v-if="test.threshold === 'D'" :title="i18n('perfTest.list.duration')" v-text="test.duration"></div>
                            <div v-else v-text="test.runCount" :title="i18n('perfTest.list.runCount')"></div>
                        </td>
                        <td>
                            <span v-if="$utils.exists(test.tps)" v-text="test.tps.toFixed(1)"></span>
                        </td>
                        <td>
                            <span v-if="$utils.exists(test.meanTestTime)" v-text="test.meanTestTime.toFixed(1)"></span>
                        </td>
                        <td>
                            <div class="ellipsis"
                                 data-toggle="popover"
                                 data-html="true"
                                 data-trigger="hover"
                                 data-placement="top"
                                 :data-content="getErrorRatePopoverContent(test)"
                                 v-text="getErrorRate(test.tests, test.errors)">
                            </div>
                        </td>
                        <td>
                            <div class="ellipsis"
                                 data-toggle="popover"
                                 data-html="true"
                                 data-trigger="hover"
                                 data-placement="left"
                                 :data-content="getVuserPopoverContent(test)"
                                 v-text="(test.vuserPerAgent) * (test.agentCount)">
                            </div>
                        </td>
                        <td class="center">
                            <i v-if="test.reportable" @click="showChart(index)" :title="i18n('perfTest.action.showChart')" class="fa fa-line-chart pointer-cursor"></i>
                            <i v-if="test.deletable" @click="deleteTests(test.id)" :title="i18n('common.button.delete')" class="fa fa-remove pointer-cursor"></i>
                            <i v-if="test.stoppable" @click="stopTest(test.id)" :title="i18n('common.button.stop')" class="fa fa-stop pointer-cursor"></i>
                        </td>
                    </tr>
                    <small-chart ref="smallChart" :key="test.id" :perfTestId="test.id"></small-chart>
                </template>
            </tbody>
        </table>
        <intro-button></intro-button>
        <div v-show="totalElements > 0">
            <paginate
                pageClass="page-item"
                prevClass="page-item"
                nextClass="page-item"
                pageLinkClass="page-link"
                prevLinkClass="page-link"
                nextLinkClass="page-link"
                containerClass="pagination"
                ref="perftestPaginate"
                v-model="currentPage"
                :page-count="totalActivationPageCount"
                :page-range="COUNT_OF_TEST_PER_PAGE"
                :click-handler="changeActivationPage"
                :prev-text="`← ${i18n('common.paging.previous')}`"
                :next-text="`${i18n('common.paging.next')} →`">
            </paginate>
        </div>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import { Component, Watch } from 'vue-property-decorator';
    import Paginate from 'vuejs-paginate';
    import vueHeadful from 'vue-headful';

    import Base from '../../Base.vue';
    import SearchBar from './Searchbar.vue';
    import IntroButton from '../../common/IntroButton.vue';
    import MessagesMixin from '../../common/mixin/MessagesMixin.vue';
    import SmallChart from './SmallChart.vue';

    @Component({
        name: 'perfTestList',
        components: { IntroButton, vueHeadful, SearchBar, SmallChart, Paginate },
    })
    export default class PerfTestList extends Mixins(Base, MessagesMixin) {
        COUNT_OF_TEST_PER_PAGE = 10;

        totalActivationPageCount = 1;
        currentPage = 0;
        runningSummary = `0 ${this.i18n('perfTest.list.runningSummary')}`;
        totalElements = 0;
        tests = [];
        autoUpdateTargets = [];
        selectAll = false;
        selectedTests = [];
        queryFilter = new Set();
        selectedTag = '';

        updateStatusTimeoutId = 0;

        mounted() {
            this.getPerfTest({ showErrorMsg: true });
            this.updateStatusTimeoutId = setTimeout(this.updatePerftestStatus, 2000);
        }

        beforeDestroy() {
            clearTimeout(this.updateStatusTimeoutId);
        }

        @Watch('tests')
        watchTests() {
            this.autoUpdateTargets = [];
            this.tests.forEach((test, index) => {
                if (!this.isFinishedStatusType(test)) {
                    this.autoUpdateTargets.push({ 'id': test.id, 'index': index });
                }
            });
        }

        isFinishedStatusType(test) {
            return test.status === 'FINISHED' || test.status === 'STOP_BY_ERROR' || test.status === 'STOP_ON_ERROR' || test.status === 'CANCELED';
        }

        changeSelectAll(event) {
            if (event.target.checked) {
                this.tests.forEach(test => this.selectedTests.push(test.id));
            } else {
                this.selectedTests = [];
            }
        }

        getOwnerPopoverContent(test) {
            let content = `${this.i18n('perfTest.list.owner')} : ${test.createdUserName} (${test.createdUserId})`;
            if (test.lastModifiedUserId) {
                content += `<br> ${this.i18n('perfTest.list.modifier.oneLine')} : ${test.lastModifiedUserName} (${test.lastModifiedUserId})`;
            }
            return content;
        }

        getErrorRatePopoverContent(test) {
            return `${this.i18n('perfTest.list.totalTests')} : ${test.tests + test.errors}<br>` +
                `${this.i18n('perfTest.list.successfulTests')} : ${test.tests}<br>` +
                `${this.i18n('perfTest.list.errors')} : ${test.errors}`;
        }

        getVuserPopoverContent(test) {
            return `${this.i18n('perfTest.list.agent')} : ${test.agentCount ? test.agentCount : 0}<br>` +
                `${this.i18n('perfTest.list.process')} : ${test.processes ? test.processes : 0}<br>` +
                `${this.i18n('perfTest.list.thread')} : ${test.threads ? test.threads : 0}`;
        }

        getTestNamePopoverContent(test) {
            let content = `${test.description} <p>${test.testComment}</p>`;
            if (test.scheduledTime) {
                content += `${this.i18n('perfTest.list.scheduledTime')} : ${this.$options.filters.dateFormat(test.scheduledTime, 'YYYY-MM-DD HH:mm')}<br>`;
            }
            content += `${this.i18n('perfTest.list.modifiedTime')} : ${this.$options.filters.dateFormat(test.lastModifiedDate, 'YYYY-MM-DD HH:mm')}<br>`;
            if (test.tagString) {
                content += `${this.i18n('perfTest.config.tags')} : ${test.tagString}<br>`;
            }
            content += this.getOwnerPopoverContent(test);
            return content;
        }

        getPerfTest(options) {
            this.$http.get('/perftest/api/list', {
                params: {
                    'page.page': this.currentPage - 1,
                    'page.size': this.COUNT_OF_TEST_PER_PAGE,
                    'query': this.$refs.searchBar.searchText,
                    'queryFilter': this.makeQueryFilter(),
                    'tag': this.$refs.searchBar.selectedTag,
                },
            }).then(res => {
                this.tests = res.data.tests;
                this.totalElements = res.data.totalElements;
                this.totalActivationPageCount = Math.ceil(this.totalElements / this.COUNT_OF_TEST_PER_PAGE);
                this.$nextTick(this.initPopover);
            }).catch(() => {
                if (options && options.showErrorMsg) {
                    this.showErrorMsg(this.i18n('common.message.loading.error', { content: this.i18n('common.button.test') }));
                }
            });
        }

        initPopover() {
            $('[data-toggle="popover"]').popover();
        }

        getErrorRate(tests, errors) {
            if (!this.$utils.exists(tests) || !this.$utils.exists(errors)) {
                return '';
            }
            return `${(errors / (tests + errors) * 100).toFixed(1)}%`;
        }

        changeActivationPage(page) {
            this.selectAll = false;
            this.selectedTests = [];
            this.currentPage = page;
            this.getPerfTest();
        }

        runQueryFilter(queryFilter) {
            queryFilter.enable ? this.queryFilter.add(queryFilter.token) : this.queryFilter.delete(queryFilter.token);
            this.getPerfTest();
        }

        makeQueryFilter() {
            let queryFilterString = '';
            this.queryFilter.forEach(val => queryFilterString += val);
            return queryFilterString;
        }

        deleteTests(ids) {
            this.$bootbox.confirm({
                message: this.i18n('perfTest.message.delete.confirm'),
                buttons: {
                    confirm: { label: this.i18n('common.button.ok') },
                    cancel: { label: this.i18n('common.button.cancel') },
                },
                callback: result => {
                    if (result && ids) {
                        this.$http.delete('/perftest/api', {
                            params: {
                                ids,
                            },
                        }).then(res => {
                            if (res.data.success) {
                                this.getPerfTest();
                                this.selectAll = false;
                                this.showSuccessMsg(this.i18n('perfTest.message.delete.success'));
                            }
                        }).catch(() => this.showErrorMsg(this.i18n('perfTest.message.delete.error')));
                    }
                },
            });
        }

        stopTest(id) {
            this.$bootbox.confirm({
                message: this.i18n('perfTest.message.stop.confirm'),
                buttons: {
                    confirm: { label: this.i18n('common.button.ok') },
                    cancel: { label: this.i18n('common.button.cancel') },
                },
                callback: result => {
                    if (result) {
                        this.$http.put(`/perftest/api/${id}?action=stop`).then(res => {
                            if (res.data.success) {
                                this.showSuccessMsg(this.i18n('perfTest.message.stop.success'));
                            }
                        }).catch(() => this.showErrorMsg(this.i18n('perfTest.message.stop.error')));
                    }
                },
            });
        }

        showChart(index) {
            this.$refs.smallChart[index].toggleDisplay();
        }

        updatePerftestStatus() {
            const ids = this.autoUpdateTargets.map(test => test.id).join(',');
            if (ids) {
                this.$http.get('/perftest/api/status', {
                    params: {
                        ids,
                    },
                }).then(res => {
                    const status = res.data.status.reverse();
                    this.autoUpdateTargets.forEach((target, index) => {
                        if (status[index].reportable) {
                            this.getPerfTest();
                        }
                        this.tests[target.index].iconName = status[index].icon;
                        this.tests[target.index].reportable = status[index].reportable;
                        this.tests[target.index].deletable = status[index].deletable;
                        this.tests[target.index].stoppable = status[index].stoppable;
                        this.tests[target.index].status = status[index].status_id;
                        this.runningSummary = `${res.data.perfTestInfo.length} ${this.i18n('perfTest.list.runningSummary')}`;

                        const $ball = $(`#ball_${this.tests[target.index].id}`);
                        $ball.attr('title', status[index].name);
                        $ball.attr('data-content', status[index].message);
                    });
                }).finally(() => this.updateStatusTimeoutId = setTimeout(this.updatePerftestStatus, 2000));
            } else {
                this.updateStatusTimeoutId = setTimeout(this.updatePerftestStatus, 2000);
            }
        }
    }
</script>

<style lang="less" scoped>
    .perftest-list-container {
        .img-unit {
            background-image: url('/img/bg_perftest_banner_en.png');
            height: 110px;
            padding: 0;
            margin-top: 0;
        }

        table {
            th, td {
                padding: 8px;
            }

            th {
                border-bottom: none;
            }

            font-size: 12px;
            width: 940px;
            margin-bottom: 6px;

            .ball {
                img {
                    width: 23px;
                    height: 23px;
                }
            }
        }

        .current-running-status-container {
            text-align: right;
            margin-top: -19px;

            code {
                padding: 0 4px;
                border-radius: 2px;
                background-color: white;
                width: 300px;
            }
        }

        .intro-button-container {
            margin-top: -26px;
        }

        .popover {
            width: auto;
            min-width: 200px;
            max-width: 600px;
            max-height: 500px;
        }
    }

    .intro-button-container {
        .intro-button-title {
            margin-right: -30px;
        }
    }
</style>
