<template>
    <div v-if="dataLoadFinished" class="perftest-detail-container">
        <div class="container">
            <form ref="configForm">
                <div class="well">
                    <input type="hidden" name="id" :value="test.id">
                    <div class="form-horizontal info">
                        <fieldset>
                            <div class="control-group">
                                <div class="row">
                                    <div class="span4-5" data-step="1" :data-intro="i18n('intro.detail.testName')">
                                        <control-group :class="{error: errors.has('testName')}" ref="testNameControlGroup" labelMessageKey="perfTest.config.testName">
                                            <input class="required span3 left-float" name="testName"
                                                   maxlength="80" size="30" type="text"
                                                   v-validate="{ required: true }"
                                                   v-model="test.testName"/>
                                            <div v-show="errors.has('testName')" v-text="errors.first('testName')" class="validation-message"></div>
                                        </control-group>
                                    </div>
                                    <div class="span3-4 tag-container" data-step="2" :data-intro="i18n('intro.detail.tags')">
                                        <control-group name="tagString" labelMessageKey="perfTest.config.tags">
                                            <select2 v-model="test.tagString" :value="test.tagString" customStyle="width: 175px" type="input" name="tagString"
                                                     :option="{tokenSeparators: [',', ' '], tags:[''], placeholder: i18n('perfTest.config.tagInput'),
                                                      maximumSelectionSize: 5, initSelection: initSelection, query: select2Query}"></select2>
                                        </control-group>
                                    </div>
                                    <div class="span1 status-image-container">
                                        <img id="test-status-img" class="ball"
                                             data-html="true"
                                             data-toggle="popover"
                                             data-placement="bottom"
                                             :title="i18n(test.springMessageKey)"
                                             :src="perftestStatus.iconPath"/>
                                    </div>
                                    <div class="span2-3 start-button-container" data-step="3" :data-intro="i18n('intro.detail.startbutton')">
                                        <div class="control-group">
                                            <input type="hidden" name="isClone" :value="isClone"/>
                                            <button class="btn btn-success" :disabled="disabled" @click.prevent="clonePerftest"
                                                    v-text="isClone ? i18n('perfTest.action.clone') : i18n('common.button.save')"></button>
                                            <button class="btn btn-primary" :disabled="disabled" @click.prevent="saveAndStart" v-text="saveScheduleBtnTitle()"></button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="control-group description-container">
                                <label for="description" class="control-label" v-text="i18n('common.label.description')"></label>
                                <div class="controls">
                                    <textarea id="description" rows="2" name="description" v-text="test.description"></textarea>
                                </div>
                            </div>
                        </fieldset>
                    </div>
                </div>

                <div v-if="isAdmin" class="pull-right">
                    <a :href="`/user/switch?to=${test.createdUserId}`" v-text="switchUserTitle"></a>
                </div>

                <div class="tabbable tab-container">
                    <ul class="nav nav-tabs" id="homeTab">
                        <li>
                            <a v-show="tab.display.config" href="#test-config-section" data-toggle="tab" ref="configTab" v-text="i18n('perfTest.config.testConfiguration')"></a>
                        </li>
                        <li>
                            <a v-show="tab.display.running" href="#running-section" data-toggle="tab" ref="runningTab" v-text="i18n('perfTest.running.title')"></a>
                        </li>
                        <li>
                            <a v-show="tab.display.report" href="#report-section" data-toggle="tab" ref="reportTab" v-text="i18n('perfTest.report.tab')"></a>
                        </li>
                    </ul>
                    <div class="tab-content">
                        <div class="tab-pane" id="test-config-section">
                            <config ref="config" :testProps="test" :config="config"></config>
                        </div>
                        <div class="tab-pane" id="running-section">
                            <running ref="running" :testProps="test"></running>
                        </div>
                        <div class="tab-pane" id="report-section">
                            <report :id="id" ref="report"></report>
                        </div>
                    </div>
                    <input v-if="scheduledTime" type="hidden" name="scheduledTime" :value="scheduledTime">
                    <input type="hidden" name="status" :value="params.testStatus">
                </div>
            </form>
            <intro-button></intro-button>
        </div>
        <schedule-modal ref="scheduleModal" @run="runPerftest" :timezoneOffset="timezoneOffset"></schedule-modal>
    </div>
</template>
<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import Base from '../../Base.vue';
    import Config from './Config.vue';
    import Report from './Report.vue';
    import Running from './Running.vue';
    import ControlGroup from '../../common/ControlGroup.vue';
    import IntroButton from '../../common/IntroButton.vue';
    import Select2 from '../../common/Select2.vue';
    import ScheduleModal from '../modal/ScheduleModal.vue';
    import MessagesMixin from '../../common/mixin/MessagesMixin.vue';

    @Component({
        name: 'perfTestDetail',
        props: {
            id: {
                type: String,
                default: '',
            },
            url: String,
            scriptType: String,
        },
        components: { ControlGroup, Config, Report, Running, IntroButton, Select2, ScheduleModal },
        $_veeValidate: {
            validator: 'new',
        },
    })
    export default class PerfTestDetail extends Mixins(Base, MessagesMixin) {
        config = {};
        test = {
            id: '',
            status: '',
            iconName: '',
            testName: '',
            progressMessage: '',
            description: '',
            createdUserId: '',
            lastProgressMessage: '',
            springMessageKey: '',
        };

        perftestStatus = {
            message: '',
            iconPath: '',
        };

        dataLoadFinished = false;
        isClone = false;
        scheduledTime = 0;
        timezoneOffset = 0;

        currentRefreshStatusTimeoutId = 0;

        $testStatusImage = null;

        params = {
            testStatus: 'SAVED',
        };

        tab = {
            display: {
                running: false,
                report: false,
                config: false,
            },
        };

        mounted() {
            this.showProgressBar();
            if (this.$route.name === 'quickStart') {
                this.$http.post('/perftest/api/quickstart', {
                    url: this.url,
                    scriptType: this.scriptType,
                })
                .then(res => this.initPerfTestDetail(res))
                .catch(() => this.showErrorMsg(this.i18n('common.message.loading.error', { content: this.i18n('common.button.test') })))
                .finally(this.hideProgressBar);
            } else {
                const apiPath = this.id ? `/perftest/api/${this.id}` : '/perftest/api/create';
                this.$http.get(apiPath)
                .then(res => this.initPerfTestDetail(res))
                .catch(() => this.showErrorMsg(this.i18n('common.message.loading.error', { content: this.i18n('common.button.test') })))
                .finally(this.hideProgressBar);
            }
        }

        initPerfTestDetail(res) {
            this.config = {
                regions: res.data.regions,
                regionAgentCountMap: res.data.regionAgentCountMap,
                rampUpTypes: res.data.availRampUpType,
                maxRunCount: res.data.maxRunCount,
                maxRunHour: res.data.maxRunHour,
                maxVuserPerAgent: res.data.maxVuserPerAgent,
            };
            this.test = res.data.test;
            this.timezoneOffset = res.data.timezone_offset;
            this.isClone = this.test.status !== 'SAVED';
            this.perftestStatus.iconPath = `/img/ball/${this.test.iconName}`;
            if (this.ngrinder.config.clustered && this.test.region === 'NONE') {
                this.test.region = '';
            }
            this.dataLoadFinished = true;
            this.updateTabDisplay();
            this.$nextTick(() => {
                if (this.test.category === 'TESTING') {
                    this.$refs.running.startSamplingInterval();
                }
                this.$testStatusImage = $('#test-status-img');
                this.$testStatusImage.attr('data-content', `${this.test.progressMessage}<br><b>${this.test.lastProgressMessage}</b>`.replace(/\n/g, '<br>'));
                this.currentRefreshStatusTimeoutId = this.refreshPerftestStatus();
                this.setTabEvent();
            });
        }

        beforeDestroy() {
            window.clearTimeout(this.currentRefreshStatusTimeoutId);
            window.clearInterval(this.$refs.running.samplingIntervalId);
        }

        setTabEvent() {
            $(this.$refs.configTab).on('shown.bs.tab', this.$refs.config.$refs.rampUp.updateRampUpChart);

            $(this.$refs.runningTab).on('shown.bs.tab', () => {
                this.$refs.running.tpsChart.plot();
                if (this.$refs.running.samplingIntervalId === -1) {
                    this.$refs.running.startSamplingInterval();
                }
            });

            $(this.$refs.reportTab).on('shown.bs.tab', this.$refs.report.fetchReportData);
        }

        refreshPerftestStatus() {
            if (!this.test.id || !this.isUpdatableStatus()) {
                return;
            }

            this.$http.get(`/perftest/api/${this.test.id}/status`).then(res => {
                const status = res.data.status[0];

                if (this.test.status !== status.status_id) {
                    this.test.status = status.status_id;
                    this.updateStatus(status.status_id, status.status_type, status.name, status.icon, status.deletable, status.stoppable, status.message);
                }
                if (this.test.category !== status.status_type) {
                    this.test.category = status.status_type;
                    this.updateTabDisplay();
                }
                this.currentRefreshStatusTimeoutId = setTimeout(this.refreshPerftestStatus, 3000);
            });
        }

        updateStatus(id, statusType, name, icon, deletable, stoppable, message) {
            if (!this.isUpdatableStatus()) {
                window.clearInterval(this.$refs.running.samplingIntervalId);
            }

            this.$testStatusImage.attr('data-original-title', name);
            this.$testStatusImage.attr('data-content', message);

            if (this.perftestStatus.iconPath !== `/img/ball/${icon}`) {
                this.perftestStatus.iconPath = `/img/ball/${icon}`;
            }
        }

        updateTabDisplay() {
            this.tab.display.config = true;
            if (this.test.category === 'TESTING') {
                this.tab.display.running = true;
                this.tab.display.report = false;
                this.$nextTick(() => this.$refs.runningTab.click());
                return;
            }

            if (!this.isUpdatableStatus()) {
                this.tab.display.report = true;
                this.tab.display.running = false;
                this.$nextTick(() => this.$refs.reportTab.click());
                return;
            }
            this.$nextTick(() => this.$refs.configTab.click());
        }

        isUpdatableStatus() {
            return !(this.test.category === 'FINISHED' || this.test.category === 'STOP' || this.test.category === 'ERROR' || this.test.category === 'CANCELED');
        }

        initSelection(element, callback) {
            const data = [];
            this.test.tagString.split(',').forEach(tag => {
                if (tag) {
                    data.push({ id: tag, text: tag });
                }
            });
            element.val('');
            callback(data);
        }

        select2Query(query) {
            const data = {
                results: [],
            };

            this.$http.get('/perftest/api/search_tag', {
                params: {
                    query: query.term,
                },
            }).then(res => {
                res.data.forEach(tag => data.results.push({ id: tag, text: tag }));
                query.callback(data);
            });
        }

        clonePerftest() {
            this.$delete(this.$refs.config.agentCountValidationRules, 'min_value');
            const agentCountField = this.$refs.config.$refs.agentCount.$validator.fields.find({ name: 'agentCount' });
            agentCountField.update({ rules: this.$refs.config.agentCountValidationRules });

            this.$validator.validateAll().then(() => {
                if (this.errors.any()) {
                    this.$refs.configTab.click();
                } else {
                    this.params.testStatus = 'SAVED';
                    this.$nextTick(() => {
                        this.$http.post('/perftest/api/new', $(this.$refs.configForm).serialize()).then(res => {
                            if (res.data === 'list') {
                                this.$router.push('/perftest');
                            } else {
                                this.$router.push(`/perftest/${res.data}`);
                            }
                        }).catch(() => this.showErrorMsg(this.i18n('perfTest.message.save.error')));
                    });
                }
            });
        }

        saveAndStart() {
            this.$set(this.$refs.config.agentCountValidationRules, 'min_value', 1);
            const agentCountField = this.$refs.config.$refs.agentCount.$validator.fields.find({ name: 'agentCount' });
            agentCountField.update({ rules: this.$refs.config.agentCountValidationRules });

            this.$validator.validateAll().then(() => {
                if (this.errors.any()) {
                    this.$refs.configTab.click();
                } else {
                    this.$refs.scheduleModal.show();
                }
            });
        }

        runPerftest(scheduledTime) {
            this.$refs.scheduleModal.hide();
            this.params.testStatus = 'READY';
            this.scheduledTime = scheduledTime;

            this.$nextTick(() => {
                this.$http.post('/perftest/api/new', $(this.$refs.configForm).serialize())
                    .then(res => {
                        this.showSuccessMsg(this.i18n('perfTest.message.testStart'));
                        this.$router.push(`/perftest/${res.data}`);
                    }).catch(() => this.showErrorMsg(this.i18n('perfTest.message.saveAndRun.error')));
            });
        }

        saveScheduleBtnTitle() {
            return `${this.isClone ? this.i18n('perfTest.action.clone') : this.i18n('common.button.save')} ${this.i18n('perfTest.action.andStart')}`;
        }

        get switchUserTitle() {
            return `${this.i18n('perfTest.list.owner')} : ${this.test.createdUserName} (${this.test.createdUserId})`;
        }

        get disabled() {
            return this.test.createdUserId !== this.ngrinder.currentUser.factualUser.id;
        }
    }
</script>

<style lang="less">
    .perftest-detail-container {
        .info {
            .control-label {
                width: 95px;
            }
        }

        .controls {
            margin-left: 120px;
        }

        .tag-container {
            label.control-label {
                width: 60px;
            }

            .controls {
                margin-left: 85px;
            }
        }

        input[type="text"] {
            height: 30px;
        }

        .intro-button-container {
            margin-top: -50px;
        }
    }
</style>

<style lang="less" scoped>
    .perftest-detail-container {
        .well {
            margin-bottom: 5px;
            margin-top: 0;
        }

        .status-image-container {
            text-align: center;
        }

        .start-button-container {
            margin-left: 19px;
        }

        #description {
            resize: none;
            width: 776px;
        }

        #save_schedule_btn {
            width: 116px;
        }

        .tab-container {
            ul {
                &#homeTab {
                    margin-bottom: 5px;
                }
            }
        }

        .description-container {
            margin-bottom: 0;

            div.controls {
                margin-left: 120px;
            }
        }

        .control-label {
            input {
                vertical-align: top;
                margin-left: 2px
            }
        }

        li {
            &.monitor-state {
                height: 10px;
            }
        }

        .controls {
            code {
                vertical-align: middle;
            }

            .span3 {
                margin-left: 0;
            }
        }

        .datepicker {
            z-index:1151;
        }

        div {
            &.chart {
                border: 1px solid #878988;
                margin-bottom: 12px;
            }
        }

        div.modal-body div.chart {
            border:1px solid #878988;
            height:250px;
            min-width:500px;
            margin-bottom:12px;
            padding:5px
        }

        .table {
            thead {
                th {
                    vertical-align: middle;
                }
            }
        }

        .jqplot-yaxis {
            margin-right: 20px;
        }

        .jqplot-xaxis {
            margin-top: 5px;
        }

        .rampup-chart {
            width: 400px;
            height: 300px
        }

        i {
            &.collapse{
                background: url('/img/icon_collapse.png') no-repeat;
                display: inline-block;
                height: 16px;
                width: 16px;
                line-height: 16px;
                vertical-align: text-top;
            }
        }

        #test_name + span {
            float: left;
        }

        .form-horizontal {
            .control-group {
                margin-bottom:10px;
            }
        }

        .control-group.success td > label[for="test_name"] {
            color: #468847;
        }

        .control-group.error td > label[for="test_name"] {
            color: #B94A48;
        }

        #script_control.error {
            .select2-choice {
                border-color: #B94A48;
                color: #B94A48;
            }
        }

        #script_control.success {
            .select2-choice {
                border-color: #468847;
                color: #468847;
            }
        }

        legend {
            padding-top: 10px;
            &.region {
                margin-left:-40px;
            }
        }
    }

</style>
