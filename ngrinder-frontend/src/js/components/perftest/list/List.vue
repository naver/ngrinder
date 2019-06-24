<template>
    <div class="container perftest-list-container">
        <vue-headful title="Performance Test"></vue-headful>
        <div class="img-unit"></div>
        <div class="pull-right current_running_status-container">
            <code id="current_running_status" v-text="runningSummary"></code>
        </div>

        <search-bar ref="searchBar" @filter-running="runQueryFilter" @filter-schduled="runQueryFilter" @create="createPerftest"
                    @search="getPerfTest" @change-tag="getPerfTest" @delete-selected-tests="deleteTests(selectedTests.toString())"></search-bar>

        <table class="table table-striped table-bordered ellipsis" id="test_table">
            <colgroup>
                <col width="30">
                <col width="50">
                <col>
                <col>
                <col width="70">
                <col v-if="ngrinder.config.clustered" width="70">
                <col width="120">
                <col width="80">
                <col width="65">
                <col width="65">
                <col width="70">
                <col width="65">
                <col width="60">
            </colgroup>
            <thead>
                <tr id="head_tr_id">
                    <th class="nothing">
                        <input type="checkbox" v-model="selectAll" class="checkbox" @change="changeSelectAll">
                    </th>
                    <th class="center nothing status" data-step="4" :data-intro="i18n('intro.list.perftest.status')">
                        <span v-text="i18n('common.label.status')"></span>
                    </th>
                    <th id="test_name">
                        <span v-text="i18n('perfTest.list.testName')"></span>
                    </th>
                    <th id="script_name">
                        <span v-text="i18n('perfTest.list.scriptName')"></span>
                    </th>
                    <th class="nothing">
                        <span v-if="isAdmin" v-text="i18n('perfTest.list.owner')"></span>
                        <span v-else v-text="i18n('perfTest.list.modifier.oneLine')"></span>
                    </th>
                    <th v-if="ngrinder.config.clustered" id="region">
                        <span v-text="i18n('common.region')"></span>
                    </th>
                    <th id="start_time">
                        <span v-text="i18n('perfTest.list.startTime')"></span>
                    </th>
                    <th class="nothing">
                        <span class="ellipsis"></span>
                        <span v-text="i18n('perfTest.list.threshold')"></span>
                    </th>
                    <th id="tps">
                        <span v-text="i18n('perfTest.list.tps')"></span>
                    </th>
                    <th id="mean_test_time" :title="i18n('perfTest.list.meantime')" v-text="'MIT'"></th>
                    <th id="errors" class="ellipsis">
                        <span v-text="i18n('perfTest.list.errorRate')"></span>
                    </th>
                    <th class="nothing small-border">
                        <span v-text="i18n('perfTest.list.vusers')"></span>
                    </th>
                    <th class="nothing" data-step="5" :data-intro="i18n('intro.list.perftest.actions')">
                        <span v-text="i18n('common.label.actions')"></span>
                    </th>
                </tr>
            </thead>
            <tbody>
                <template v-for="(test, index) in tests">
                    <tr>
                        <td class="center">
                            <input type="checkbox" v-model="selectedTests" class="perf_test checkbox" :value="test.id" :disabled="!test.deletable">
                        </td>
                        <td class="center">
                            <div class="ball" data-html="true"
                                 data-toggle="popover"
                                 :id="`ball_${test.id}`"
                                 :title="test.status"
                                 :data-content="`${test.progressMessage}<br><b>${test.lastProgressMessage}</b>`.replace(/\n/g, '<br>')">
                            <img class="status" :src="`/img/ball/${test.iconName}`">
                            </div>
                        </td>
                        <td class="ellipsis">
                            <div class="ellipsis" :title="test.testName" data-toggle="popover" data-html="true" :data-content="getTestNamePopoverContent(test).replace(/\n/g, '<br>')">
                                <a @click.prevent="$router.push(`/perftest/${test.id}`)" target="_self" class="clickable" v-text="test.testName"></a>
                            </div>
                        </td>
                        <td class="ellipsis">
                            <div class="ellipsis" data-toggle="popover" data-html="true"
                                :data-content="`${test.scriptName}<br> - ${i18n('script.list.revision')} : ${(test.scriptRevision)}`"
                                :title="i18n('perfTest.list.scriptName')">
                                <a v-if="isAdmin" :href="`/script/detail/${test.scriptName}?r=${(test.scriptRevision)}&ownerId=${(test.createdUserId)}`" v-text="test.scriptName"></a>
                                <a v-else :href="`/script/detail/${test.scriptName}?r=${(test.scriptRevision)}`" v-text="test.scriptName"></a>
                            </div>
                        </td>
                        <td>
                           <div class="ellipsis" data-toggle="popover" :title="i18n('perfTest.list.participants')" data-html="true"
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
                            <div v-if="test.threshold === THRESHOLD_TYPE_DURATION" :title="i18n('perfTest.list.duration')" v-text="test.duration"></div>
                            <div v-else v-text="test.runCount" :title="i18n('perfTest.list.runCount')"></div>
                        </td>
                        <td>
                            <span v-if="test.tps" v-text="test.tps.toFixed(1)"></span>
                        </td>
                        <td>
                            <span v-if="test.meanTestTime" v-text="test.meanTestTime.toFixed(1)"></span>
                        </td>
                        <td>
                            <div class="ellipsis" data-toggle="popover" data-html="true" data-placement="top" v-text="getErrorRate(test.tests, test.errors)"></div>
                        </td>
                        <td>
                            <div class="ellipsis" data-toggle="popover" data-html="true" data-placement="left" v-text="(test.vuserPerAgent) * (test.agentCount)"></div>
                        </td>
                        <td class="center">
                            <i v-if="test.reportable" @click="showChart(index)" :title="i18n('perfTest.action.showChart')" class="icon-download test-display pointer-cursor"></i>
                            <i v-if="test.deletable" @click="deleteTests(test.id)" :title="i18n('common.button.delete')" class="icon-remove test-remove pointer-cursor"></i>
                            <i v-if="test.stoppable" @click="stopTest(test.id)" :title="i18n('common.button.stop')" class="icon-stop test-stop pointer-cursor"></i>
                        </td>
                    </tr>
                    <small-chart ref="smallChart" :key="test.id" :perfTestId="test.id"></small-chart>
                </template>
            </tbody>
        </table>
        <intro-button></intro-button>
        <div v-show="totalElements > 0" class="pagination dataTables_paginate">
            <paginate
                v-model="currentPage"
                :pageCount="totalActivationPageCount"
                :page-range="COUNT_OF_TEST_PER_PAGE"
                :click-handler="changeActivationPage"
                :prev-text="`← ${i18n('common.paging.previous')}`"
                :next-text="`${i18n('common.paging.next')} →`"
                ref="perftestPaginate">
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
        THRESHOLD_TYPE_DURATION = 'D';
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
                this.tests.forEach(test => {
                    this.selectedTests.push(test.id);
                });
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
            $('[data-toggle="popover"]').popover('destroy');
            $('[data-toggle="popover"]').popover({ trigger: 'hover', container: '#test_table' });
        }

        getErrorRate(tests, errors) {
            if (!tests || !errors) {
                return '';
            }

            if (!tests && !errors) {
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
            bootbox.confirm(this.i18n('perfTest.message.delete.confirm'), this.i18n('common.button.cancel'), this.i18n('common.button.ok'), result => {
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
            });
        }

        stopTest(id) {
            bootbox.confirm(this.i18n('perfTest.message.stop.confirm'), this.i18n('common.button.cancel'), this.i18n('common.button.ok'), result => {
                if (result) {
                    this.$http.put(`/perftest/api/${id}?action=stop`).then(res => {
                        if (res.data.success) {
                            this.showSuccessMsg(this.i18n('perfTest.message.stop.success'));
                        }
                    }).catch(() => this.showErrorMsg(this.i18n('perfTest.message.stop.error')));
                }
            });
        }

        showChart(index) {
            this.$refs.smallChart[index].toggleDisplay();
        }

        createPerftest() {
            this.$router.push('/perftest/new');
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
                        $ball.attr('data-original-title', status[index].name);
                        $ball.data('popover').options.content = status[index].message;
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
            th.status {
                padding-left: 3px;
            }
            font-size: 12px;
            width: 940px;
            margin-bottom: 6px;
        }

        .current_running_status-container {
            margin-top: -20px;

            #current_running_status {
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
</style>
