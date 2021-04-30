<template>
    <div class="perftest-detail-container">
        <vue-headful :title="i18n('perfTest.title')"></vue-headful>
        <div class="container">
            <div class="card bg-light">
                <div class="form-horizontal info">
                    <fieldset>
                        <div class="control-group">
                            <div class="row">
                                <button class="btn-return-to-list btn-primary border-0 position-relative" @click="returnToList" v-text="i18n('common.list')"></button>
                                <div class="test-name-container" data-step="1" :data-intro="i18n('intro.detail.testName')">
                                    <control-group :class="{ error: errors.has('testName') }" ref="testNameControlGroup" labelMessageKey="perfTest.config.testName">
                                        <input class="required form-control float-left" name="testName"
                                               maxlength="80" size="30" type="text"
                                               v-validate="{ required: true }"
                                               v-model="test.testName"
                                               v-focus/>
                                        <span v-show="errors.has('testName')" v-text="errors.first('testName')" class="validation-message"></span>
                                    </control-group>
                                </div>
                                <div class="tag-container" data-step="2" :data-intro="i18n('intro.detail.tags')">
                                    <control-group name="tagString" labelMessageKey="perfTest.config.tags">
                                        <select2 v-model="test.tagString" :value="test.tagString" customStyle="width: 300px; min-height: 30px;" type="input" name="tagString"
                                                 :option="{ tokenSeparators: [',', ' '], tags: [''], placeholder: i18n('perfTest.config.tagInput'),
                                                  maximumSelectionSize: 5, initSelection: initSelection, query: select2Query }">
                                        </select2>
                                    </control-group>
                                </div>
                                <div class="d-flex">
                                    <div class="flex-grow-1 text-center">
                                        <img ref="testStatusImage" class="ball"
                                             data-html="true"
                                             data-toggle="popover"
                                             data-trigger="hover"
                                             data-placement="bottom"
                                             :class="{'pointer-cursor': !isRunningStatus(test.status), 'wait-cursor': isRunningStatus(test.status)}"
                                             :title="i18n(test.status.springMessageKey)"
                                             :src="`${contextPath}${perftestStatus.iconPath}`"/>
                                    </div>
                                    <div class="ml-auto" data-step="3" :data-intro="i18n('intro.detail.startbutton')">
                                        <div class="control-group start-btn-container">
                                            <button class="btn btn-success" :disabled="disabled" @click.prevent="clonePerftest">
                                                <i class="fa fa-clone mr-1"></i>
                                                <span v-text="isClone ? i18n('perfTest.action.clone') : i18n('common.button.save')"></span>
                                            </button>
                                            <button class="btn btn-primary" :disabled="disabled" @click.prevent="saveAndStart">
                                                <i class="fa fa-play mr-1"></i>
                                                <span v-text="saveScheduleBtnTitle"></span>
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="control-group description-container">
                            <label for="description" class="control-label" v-text="i18n('common.label.description')"></label>
                            <div class="controls">
                                <textarea class="form-control" id="description" rows="2" name="description" v-model="test.description"></textarea>
                            </div>
                        </div>
                    </fieldset>
                </div>
            </div>

            <div class="tabbable tab-container">
                <ul class="nav nav-tabs home-tab">
                    <li class="nav-item">
                        <a v-show="tab.display.config" href="#test-config-section" class="nav-link"
                           data-toggle="tab" ref="configTab" v-text="i18n('perfTest.config.testConfiguration')"></a>
                    </li>
                    <li class="nav-item">
                        <a v-show="tab.display.running" href="#running-section" class="nav-link"
                           data-toggle="tab" ref="runningTab" v-text="i18n('perfTest.running.title')"></a>
                    </li>
                    <li class="nav-item">
                        <a v-show="tab.display.report" href="#report-section" class="nav-link"
                           data-toggle="tab" ref="reportTab" v-text="i18n('perfTest.report.tab')"></a>
                    </li>
                    <a v-if="isAdmin && (ngrinder.currentUser.id !== test.createdBy.userId)" class="ml-auto" :href="`${contextPath}/user/switch?to=${test.createdBy.userId}`" v-text="switchUserTitle"></a>
                </ul>
                <div class="tab-content">
                    <div class="tab-pane" id="test-config-section">
                        <config ref="config" :test="test" :scriptsMap="scriptsMap" :config="config"></config>
                    </div>
                    <div class="tab-pane" id="running-section">
                        <running ref="running" :id="id" :config="test.config"></running>
                    </div>
                    <div class="tab-pane" id="report-section">
                        <report :id="id" ref="report"></report>
                    </div>
                </div>
            </div>
        </div>
        <schedule-modal ref="scheduleModal" @run="runPerftest" :timezoneOffset="timezoneOffset"></schedule-modal>
    </div>
</template>
<script>
    import { Mixins } from 'vue-mixin-decorator';
    import { Component, Prop } from 'vue-property-decorator';
    import VueHeadful from 'vue-headful';

    import Base from '../../Base.vue';
    import Config from './Config.vue';
    import Report from './Report.vue';
    import Running from './Running.vue';
    import ControlGroup from '../../common/ControlGroup.vue';
    import Select2 from '../../common/Select2.vue';
    import ScheduleModal from '../modal/ScheduleModal.vue';
    import MessagesMixin from '../../common/mixin/MessagesMixin.vue';
    import PopoverMixin from '../../common/mixin/PopoverMixin.vue';
    import CommonMixin from '../mixin/CommonMixin.vue';
    import Utils from '../../../utils.js';
    import { TipType } from '../../../constants';

    class PerfTestSerializer {
        static serialize(test) {
            return {
                // basic
                id: test.id,
                testName: test.testName,
                tagString: test.tagString,
                status: test.status.name,
                description: test.description,
                scheduledTime: test.scheduledTime,

                // config
                agentCount: test.config.agentCount,
                region: test.config.region,
                vuserPerAgent: test.config.vuserPerAgent,
                processes: test.config.processes,
                threads: test.config.threads,
                scriptName: test.config.scriptName,
                targetHosts: test.config.targetHosts,
                threshold: test.config.threshold,
                duration: test.config.duration,
                runCount: test.config.runCount,
                samplingInterval: test.config.samplingInterval,
                ignoreSampleCount: test.config.ignoreSampleCount,
                ignoreTooManyError: test.config.ignoreTooManyError,
                connectionReset: test.config.connectionReset,
                safeDistribution: test.config.safeDistribution,
                param: test.config.param,
                scm: test.config.scm,
                scriptRevision: test.config.scriptRevision,

                // rampUp
                useRampUp: test.rampUp.enable,
                rampUpType: test.rampUp.type,
                rampUpInitCount: test.rampUp.initCount,
                rampUpStep: test.rampUp.step,
                rampUpInitSleepTime: test.rampUp.initSleepTime,
                rampUpIncrementInterval: test.rampUp.interval,
            };
        }

        static deserialize(test) {
            return {
                id: test.id,
                createdBy: test.createdBy,
                progressMessage: test.progressMessage,
                lastProgressMessage: test.lastProgressMessage,
                testName: test.testName,
                tagString: test.tagString,
                status: test.status,
                description: test.description,
                config: {
                    agentCount: test.agentCount,
                    region: test.region,
                    vuserPerAgent: test.vuserPerAgent,
                    processes: test.processes,
                    threads: test.threads,
                    scriptName: test.scriptName,
                    scriptRevision: Utils.isNumeric(test.scriptRevision) ?
                        parseInt(test.scriptRevision) : test.scriptRevision,
                    targetHosts: test.targetHosts,
                    threshold: test.threshold,
                    duration: test.duration,
                    runCount: test.runCount,
                    samplingInterval: test.samplingInterval,
                    ignoreSampleCount: test.ignoreSampleCount,
                    ignoreTooManyError: test.ignoreTooManyError,
                    connectionReset: test.connectionReset,
                    safeDistribution: test.safeDistribution,
                    param: test.param,
                    scm: test.scm || 'svn',
                },
                rampUp: {
                    enable: test.useRampUp,
                    type: test.rampUpType,
                    initCount: test.rampUpInitCount,
                    step: test.rampUpStep,
                    initSleepTime: test.rampUpInitSleepTime,
                    interval: test.rampUpIncrementInterval,
                },
            };
        }
    }

    Component.registerHooks(['beforeRouteEnter', 'beforeRouteUpdate']);
    @Component({
        name: 'perfTestDetail',
        components: { ControlGroup, Config, Report, Running, Select2, ScheduleModal, VueHeadful },
        $_veeValidate: {
            validator: 'new',
        },
    })
    export default class PerfTestDetail extends Mixins(Base, MessagesMixin, PopoverMixin, CommonMixin) {
        @Prop({ type: String, required: false })
        id;

        @Prop({ type: Object, required: true })
        config;

        @Prop({ type: Number, required: true })
        timezoneOffset;

        @Prop({ type: Object, required: true })
        scriptsMap;

        perftestStatus = {
            message: '',
            iconPath: '',
        };

        isClone = false;
        currentRefreshStatusTimeoutId = 0;

        $testStatusImage = null;

        tab = {
            display: {
                running: false,
                report: false,
                config: false,
            },
        };

        data() {
            return { test: PerfTestSerializer.deserialize(this.$route.params.test) };
        }

        beforeRouteEnter(to, from, next) {
            PerfTestDetail.prepare(to)
                .then(next)
                .catch(() => { /* noOp */ });
        }

        beforeRouteUpdate(to, from, next) {
            PerfTestDetail.prepare(to)
                .then(next)
                .catch(() => { /* noOp */ });
        }

        mounted() {
            this.$store.commit('activeTip', TipType.INTROJS);
            this.init();
        }

        returnToList() {
            this.$router.referer ? this.$router.back() : this.$router.push('/perftest/');
        }

        static async prepare(route) {
            route.params.scriptsMap = {};
            try {
                await PerfTestDetail.preparePerfTest(route);
                return Promise.all([
                    PerfTestDetail.prepareSvnScripts(route),
                    PerfTestDetail.prepareGitHubConfig(route)
                ]);
            } catch (e) {
                return Promise.reject();
            }
        }

        static preparePerfTest(route) {
            let promise;
            if (route.name === 'quickStart') {
                promise = Base.prototype.$http.post('/perftest/api/quickstart', {
                    url: route.query.url,
                    scriptType: route.query.scriptType,
                });
            } else {
                const apiPath = route.params.id ? `/perftest/api/${route.params.id}/detail` : '/perftest/api/create';
                promise = Base.prototype.$http.get(apiPath);
            }
            return promise.then(res => Object.assign(route.params, res.data));
        }

        static prepareSvnScripts(route) {
            let apiUrl = '/perftest/api/script';
            if (route.params.isAdmin) {
                apiUrl += `?ownerId=${route.params.test.createdBy.userId}`;
            }
            return Base.prototype.$http.get(apiUrl)
                .then(res => route.params.scriptsMap.svn = res.data);
        }

        static prepareGitHubConfig(route) {
            return Base.prototype.$http.get('/script/api/github-config')
                .then(res => route.params.config.github = res.data)
                .catch(error => route.params.config.github = { error: true, message: error.response.data.message });
        }

        init() {
            this.isClone = this.test.status.name !== 'SAVED';
            this.perftestStatus.iconPath = `/img/ball/${this.test.status.iconName}`;
            if (this.ngrinder.config.clustered && this.test.region === 'NONE') {
                this.test.region = '';
            }
            this.$nextTick(() => {
                if (this.test.status.category === 'TESTING') {
                    this.$refs.running.startSamplingInterval();
                }
                this.$testStatusImage = $(this.$refs.testStatusImage);
                this.$testStatusImage.attr('data-content', this.getStatusDataContent(this.test.progressMessage, this.test.lastProgressMessage));
                this.currentRefreshStatusTimeoutId = this.startRefreshPerfTestStatusInterval();
                this.initPopover($('[data-toggle="popover"]'));
                this.setTabEvent();
                this.updateTabDisplay();
            });
        }

        beforeDestroy() {
            this.$store.commit('activeTip', '');
            window.clearTimeout(this.currentRefreshStatusTimeoutId);
            window.clearInterval(this.$refs.running.samplingIntervalId);
        }

        setTabEvent() {
            $(this.$refs.configTab).on('shown.bs.tab', () => {
                this.$refs.config.shownBsTab = true;
                this.$refs.config.$refs.rampUp.updateRampUpChart();
            });

            $(this.$refs.reportTab).on('shown.bs.tab', () => {
                this.$refs.report.shownBsTab = true;
                this.$refs.report.fetchReportData();
            });

            $(this.$refs.runningTab).on('shown.bs.tab', () => {
                this.$refs.running.shownBsTab = true;
                if (this.$refs.running.samplingIntervalId === -1) {
                    this.$refs.running.startSamplingInterval();
                }
            });

            $(this.$refs.configTab).on('hidden.bs.tab', () => this.$refs.config.shownBsTab = false);
            $(this.$refs.reportTab).on('hidden.bs.tab', () => this.$refs.report.shownBsTab = false);
            $(this.$refs.runningTab).on('hidden.bs.tab', () => this.$refs.running.shownBsTab = false);
        }

        startRefreshPerfTestStatusInterval() {
            if (!this.test.id || this.test.status.reportable) {
                return;
            }

            this.$http.get(`/perftest/api/${this.test.id}/status`).then(res => {
                const prevStatus = this.test.status;
                this.test.status = res.data.status;

                if (this.test.status.name !== prevStatus.name) {
                    this.isClone = this.test.status.name !== 'SAVED';
                    this.updateStatusPopover(res.data.message);
                }
                if (this.test.status.category !== prevStatus.category) {
                    this.updateTabDisplay();
                }
                this.currentRefreshStatusTimeoutId = setTimeout(this.startRefreshPerfTestStatusInterval, 3000);
            });
        }

        updateStatusPopover(popoverContent) {
            this.$testStatusImage.attr('data-original-title', this.i18n(this.test.status.springMessageKey));
            this.$testStatusImage.attr('data-content', popoverContent);

            const newIconPath = `/img/ball/${this.test.status.iconName}`;
            if (this.perftestStatus.iconPath !== newIconPath) {
                this.perftestStatus.iconPath = newIconPath;
            }
        }

        updateTabDisplay() {
            this.tab.display.config = true;
            if (this.test.status.category === 'TESTING') {
                this.tab.display.running = true;
                this.tab.display.report = false;
                this.$nextTick(() => this.$refs.runningTab.click());
                return;
            }
            if (this.test.status.reportable) {
                if (this.$refs.running) {
                    window.clearInterval(this.$refs.running.samplingIntervalId);
                }
                this.tab.display.report = true;
                this.tab.display.running = false;
                this.$nextTick(() => this.$refs.reportTab.click());
                return;
            }
            this.$nextTick(() => this.$refs.configTab.click());
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
            const agentCountField = this.$refs.config.$validator.fields.find({ name: 'agentCount' });
            agentCountField.update({ rules: this.$refs.config.agentCountValidationRules });

            if (this.test.config.scm === 'svn') {
                this.checkClonedPerftestValidation();
                return;
            }

            this.$refs.config.loadGitHubScript()
                .then(this.checkClonedPerftestValidation, this.clickConfigTabIfHasErrors);
        }

        checkClonedPerftestValidation() {
            this.$validator.validateAll()
                .then(() => {
                    if (this.errors.any()) {
                        this.$refs.configTab.click();
                    } else {
                        this.test.status.name = 'SAVED';
                        this.$nextTick(() => {
                            this.$http.post(`/perftest/api/save?isClone=${this.isClone}`, PerfTestSerializer.serialize(this.test))
                                .then(() => this.$router.push('/perftest'))
                                .catch(() => this.showErrorMsg(this.i18n('perfTest.message.save.error')));
                        });
                    }
            });
        }

        saveAndStart() {
            this.$set(this.$refs.config.agentCountValidationRules, 'min_value', 1);
            const agentCountField = this.$refs.config.$validator.fields.find({ name: 'agentCount' });
            agentCountField.update({ rules: this.$refs.config.agentCountValidationRules });

            if (this.test.config.scm === 'svn') {
                this.checkSaveAndStartValidation();
                return;
            }

            this.$refs.config.loadGitHubScript()
                .then(this.checkSaveAndStartValidation, this.clickConfigTabIfHasErrors);
        }

        checkSaveAndStartValidation() {
            this.$validator.validateAll()
                .then(() => {
                    if (this.errors.any()) {
                        this.$refs.configTab.click();
                    } else {
                        if (typeof(scheduleTestBlockingHook) === 'function') {
                            scheduleTestBlockingHook.call(this, this.$refs.scheduleModal.show);
                            return;
                        }
                        this.$refs.scheduleModal.show();
                    }
            });
        }

        clickConfigTabIfHasErrors() {
            if (this.errors.any()) {
                this.$refs.configTab.click();
            }
        }

        runPerftest(scheduledTime) {
            this.$refs.scheduleModal.hide();
            this.test.status.name = 'READY';
            this.test.scheduledTime = scheduledTime ? scheduledTime.getTime() : scheduledTime;

            this.$nextTick(() => {
                this.$http.post(`/perftest/api/save?isClone=${this.isClone}`, PerfTestSerializer.serialize(this.test))
                    .then(res => {
                        if (scheduledTime) {
                            this.showSuccessMsg(this.i18n('perfTest.message.scheduled.success'));
                            this.$router.push('/perftest/');
                        } else {
                            this.showSuccessMsg(this.i18n('perfTest.message.testStart'));
                            this.$router.push(`/perftest/${res.data.id}`);
                        }
                    }).catch(() => this.showErrorMsg(this.i18n('perfTest.message.saveAndRun.error')));
            });
        }

        get saveScheduleBtnTitle() {
            return `${this.isClone ? this.i18n('perfTest.action.clone') : this.i18n('common.button.save')} ${this.i18n('perfTest.action.andStart')}`;
        }

        get switchUserTitle() {
            return `${this.i18n('perfTest.list.owner')} : ${this.test.createdBy.userName} (${this.test.createdBy.userId})`;
        }

        get disabled() {
            return this.test.createdBy.userId !== this.ngrinder.currentUser.factualUser.id;
        }
    }
</script>

<style lang="less">
    .perftest-detail-container {
        .info {
            padding-top: 10px;

            .control-label {
                padding-top: 5px;
                width: 110px;
            }
        }

        fieldset {
            .d-flex {
                width: 304px;
            }
        }

        .controls {
            margin-left: 130px;
        }

        .tag-container {
            width: 385px;

            label.control-label {
                width: 60px;
            }

            .controls {
                margin-left: 85px;
            }
        }

        .test-name-container {
            width: 430px;
            height: 40px;

            input {
                width: 300px;
            }
        }

        input[type="text"] {
            height: 30px;
        }

        .select2-dropdown-open {
            .select2-choices {
                border-radius: 3px 3px 0 0;
            }
        }

        .select2-choices {
            border: 1px solid #ced4da;

            .select2-input {
                height: 26px !important;
            }
        }
    }
</style>

<style lang="less" scoped>
    .perftest-detail-container {
        .container {
            padding: 0;

            .row {
                margin: 0;
            }
        }

        .card {
            padding: 5px;
            margin-bottom: 5px;
            margin-top: 0;
        }

        #description {
            resize: none;
            width: 990px;
            height: 66px;
        }

        #save_schedule_btn {
            width: 116px;
        }

        .tab-container {
            ul {
                &.home-tab {
                    margin-bottom: 5px;
                    width: 100%;
                }
            }
        }

        .description-container {
            margin-bottom: 0;
            margin-left: 50px;
        }

        .btn-return-to-list {
            right: 6px;
            height: 30px;
            width: 50px;
            border-radius: 0 3px 3px 0;
        }

        .control-label {
            input {
                vertical-align: top;
                margin-left: 2px
            }
        }

        .start-btn-container {
            text-align: right;
            width: 194px;

            .btn-success {
                width: 70px;
            }

            .btn-primary {
                width: 120px;
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
        }

        .datepicker {
            z-index: 1151;
        }

        div {
            &.chart {
                border: 1px solid #c4c4c4;
                margin-bottom: 12px;
            }
        }

        div.modal-body div.chart {
            border: 1px solid #c4c4c4;
            height: 250px;
            min-width: 500px;
            margin-bottom: 12px;
            padding: 5px;
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
            height: 300px;
        }

        i {
            &.collapse {
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
                margin-bottom: 5px;
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
                margin-left: -40px;
            }
        }
    }

</style>
