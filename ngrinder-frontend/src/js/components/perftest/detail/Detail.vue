<template>
    <div class="perftest-detail-container">
        <div class="container">
            <div class="card bg-light">
                <div class="form-horizontal info">
                    <fieldset>
                        <div class="control-group">
                            <div class="row">
                                <div class="test-name-container" data-step="1" :data-intro="i18n('intro.detail.testName')">
                                    <control-group :class="{ error: errors.has('testName') }" ref="testNameControlGroup" labelMessageKey="perfTest.config.testName">
                                        <input class="required form-control float-left" name="testName"
                                               maxlength="80" size="30" type="text"
                                               v-validate="{ required: true }"
                                               v-model="test.testName"/>
                                        <span v-show="errors.has('testName')" v-text="errors.first('testName')" class="validation-message"></span>
                                    </control-group>
                                </div>
                                <div class="tag-container" data-step="2" :data-intro="i18n('intro.detail.tags')">
                                    <control-group name="tagString" labelMessageKey="perfTest.config.tags">
                                        <select2 v-model="test.tagString" :value="test.tagString" customStyle="width: 175px" type="input" name="tagString"
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
                                             :title="i18n(test.status.springMessageKey)"
                                             :src="`${contextPath}${perftestStatus.iconPath}`"/>
                                    </div>
                                    <div class="ml-auto" data-step="3" :data-intro="i18n('intro.detail.startbutton')">
                                        <div class="control-group">
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
                    <a v-if="isAdmin" class="ml-auto" :href="`${contextPath}/user/switch?to=${test.createdUser.userId}`" v-text="switchUserTitle"></a>
                </ul>
                <div class="tab-content">
                    <div class="tab-pane" id="test-config-section">
                        <config ref="config" :testProp="test" :scriptsProp="scripts" :config="config"></config>
                    </div>
                    <div class="tab-pane" id="running-section">
                        <running ref="running" :testProp="test"></running>
                    </div>
                    <div class="tab-pane" id="report-section">
                        <report :id="id" ref="report"></report>
                    </div>
                </div>
            </div>
            <intro-button></intro-button>
        </div>
        <schedule-modal ref="scheduleModal" @run="runPerftest" :timezoneOffset="timezoneOffset"></schedule-modal>
    </div>
</template>
<script>
    import { Mixins } from 'vue-mixin-decorator';
    import { Component, Prop } from 'vue-property-decorator';

    import Base from '../../Base.vue';
    import Config from './Config.vue';
    import Report from './Report.vue';
    import Running from './Running.vue';
    import ControlGroup from '../../common/ControlGroup.vue';
    import IntroButton from '../../common/IntroButton.vue';
    import Select2 from '../../common/Select2.vue';
    import ScheduleModal from '../modal/ScheduleModal.vue';
    import MessagesMixin from '../../common/mixin/MessagesMixin.vue';

    Component.registerHooks(['beforeRouteEnter', 'beforeRouteUpdate']);
    @Component({
        name: 'perfTestDetail',
        components: { ControlGroup, Config, Report, Running, IntroButton, Select2, ScheduleModal },
        $_veeValidate: {
            validator: 'new',
        },
    })
    export default class PerfTestDetail extends Mixins(Base, MessagesMixin) {
        @Prop({ type: String, required: false })
        id;

        @Prop({ type: Object, required: true })
        config;

        @Prop({ type: Object, required: true })
        test;

        @Prop({ type: Number, required: true })
        timezoneOffset;

        @Prop({ type: Array, required: true })
        scripts;

        perftestStatus = {
            message: '',
            iconPath: '',
        };

        isClone = false;
        scheduledTime = 0;

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

        getParams() {
            const params = {
                id: this.test.id,
                testName: this.test.testName,
                tagString: this.test.tagString,
                description: this.test.description,
                status: this.params.testStatus,
            };

            if (this.scheduledTime) {
                params.scheduledTime = this.scheduledTime;
            }

            if (this.ngrinder.config.isClustered) {
                params.region = this.$refs.config.test.region;
            }

            Object.assign(params, this.$refs.config.getParams());
            Object.assign(params, this.$refs.config.$refs.rampUp.getParams());

            return params;
        }

        beforeRouteEnter(to, from, next) {
            PerfTestDetail.prepare(to)
                .then(next)
                .catch(() => next('/perftest'));
        }

        beforeRouteUpdate(to, from, next) {
            PerfTestDetail.prepare(to)
                .then(next)
                .catch(() => next('/perftest'));
        }

        created() {
            $('[data-toggle="popover"]').popover('hide');
        }

        mounted() {
            this.init();
        }

        static prepare(route) {
            return Promise.all([
                PerfTestDetail.preparePerfTest(route),
                PerfTestDetail.prepareScripts(route),
            ]);
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

        static prepareScripts(route) {
            return Base.prototype.$http.get('/perftest/api/script')
                .then(res => route.params.scripts = res.data);
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
                this.$testStatusImage.attr('data-content', `${this.test.progressMessage}<br><b>${this.test.lastProgressMessage}</b>`.replace(/\n/g, '<br>'));
                this.currentRefreshStatusTimeoutId = this.refreshPerftestStatus();
                $('[data-toggle="popover"]').popover();
                this.setTabEvent();
                this.updateTabDisplay();
            });
        }

        beforeDestroy() {
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

        refreshPerftestStatus() {
            if (!this.test.id || !this.isUpdatableStatus()) {
                return;
            }

            this.$http.get(`/perftest/api/${this.test.id}/status`).then(res => {
                const status = res.data.status;
                const message = res.data.message;

                if (this.test.status.name !== status.name) {
                    this.test.status.name = status.name;
                    this.isClone = this.test.status.name !== 'SAVED';
                    this.updateStatus(status.springMessageKey, message, status.iconName);
                }
                if (this.test.status.category !== status.category) {
                    this.test.status.category = status.category;
                    this.updateTabDisplay();
                }
                this.currentRefreshStatusTimeoutId = setTimeout(this.refreshPerftestStatus, 3000);
            });
        }

        updateStatus(messageKey, message, icon) {
            this.$testStatusImage.attr('data-original-title', this.i18n(messageKey));
            this.$testStatusImage.attr('data-content', message);

            if (this.perftestStatus.iconPath !== `/img/ball/${icon}`) {
                this.perftestStatus.iconPath = `/img/ball/${icon}`;
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
            if (!this.isUpdatableStatus()) {
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

        isUpdatableStatus() {
            return !(this.test.status.category === 'FINISHED' ||
                this.test.status.category === 'STOP' ||
                this.test.status.category === 'ERROR' ||
                this.test.status.category === 'CANCELED');
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
                        this.$http.post(`/perftest/api/save?isClone=${this.isClone}`, this.getParams())
                            .then(() => this.$router.push('/perftest'))
                            .catch(() => this.showErrorMsg(this.i18n('perfTest.message.save.error')));
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
                this.$http.post(`/perftest/api/save?isClone=${this.isClone}`, this.getParams())
                    .then(res => {
                        this.showSuccessMsg(this.i18n('perfTest.message.testStart'));
                        this.$router.push(`/perftest/${res.data.id}`);
                    }).catch(() => this.showErrorMsg(this.i18n('perfTest.message.saveAndRun.error')));
            });
        }

        get saveScheduleBtnTitle() {
            return `${this.isClone ? this.i18n('perfTest.action.clone') : this.i18n('common.button.save')} ${this.i18n('perfTest.action.andStart')}`;
        }

        get switchUserTitle() {
            return `${this.i18n('perfTest.list.owner')} : ${this.test.createdUser.userName} (${this.test.createdUser.userId})`;
        }

        get disabled() {
            return this.test.createdUser.userId !== this.ngrinder.currentUser.factualUser.id;
        }
    }
</script>

<style lang="less">
    .perftest-detail-container {
        .info {
            padding-top: 10px;

            .control-label {
                width: 95px;
            }
        }

        fieldset {
            .d-flex {
                width: 275px;
            }
        }

        .controls {
            margin-left: 120px;
        }

        .tag-container {
            width: 260px;

            label.control-label {
                width: 60px;
            }

            .controls {
                margin-left: 85px;
            }
        }

        .test-name-container {
            width: 360px;
            height: 48px;

            input {
                width: 240px;
            }
        }

        input[type="text"] {
            height: 30px;
        }

        .intro-button-container {
            margin-top: -28px;
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
            width: 776px;
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
        }

        .datepicker {
            z-index: 1151;
        }

        div {
            &.chart {
                border: 1px solid #878988;
                margin-bottom: 12px;
            }
        }

        div.modal-body div.chart {
            border: 1px solid #878988;
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
