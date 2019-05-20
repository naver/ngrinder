<template>
    <div v-if="dataLoadFinished" id="config-container" class="row config">
        <div class="span6" :data-hello="false">
            <fieldset>
                <legend><span v-text="i18n('perfTest.config.basicConfiguration')"></span></legend>
            </fieldset>
            <div class="form-horizontal form-horizontal-2">
                <div class="row intro agent-config-container" data-step="4" :data-intro="i18n('intro.config.basic.agent')">
                    <div class="span4">
                        <control-group labelMessageKey="perfTest.config.agent" ref="agentCountControlGroup">
                            <input-append name="agentCount" ref="agentCount"
                                          v-model="test.agentCount"
                                          @validationResult="$refs.agentCountControlGroup.handleError($event)"
                                          :validationRules="agentCountValidationRules"
                                          errStyle="position: absolute; margin: 0;"
                                          appendPrefix="perfTest.config.max"
                                          :append="maxAgentCount"
                                          message="perfTest.config.agent">
                            </input-append>
                        </control-group>
                    </div>

                    <div v-if="config.clustered" class="span2">
                        <control-group labelMessageKey="perfTest.config.region">
                            <select id="region" name="region" class="pull-right required">
                                <option value=""></option>
                                <option v-for="region in regions" :value="region" :selected="region === test.region" v-text="region"></option>
                            </select>
                        </control-group>
                    </div>
                </div>

                <control-group labelMessageKey="perfTest.config.vuserPerAgent" ref="vuserPerAgentControlGroup" dataStep="5" :dataIntro="i18n('intro.config.basic.vuser')">
                    <input-append name="vuserPerAgent" ref="vuserPerAgent"
                                  v-model="test.vuserPerAgent"
                                  @validationResult="$refs.vuserPerAgentControlGroup.handleError($event)"
                                  :validationRules="{ required: true, max_value: testConfig.maxVuserPerAgent, min_value: 1 }"
                                  @change="changeVuserPerAgent"
                                  errStyle="margin: 0; width: 140px;"
                                  appendPrefix="perfTest.config.max"
                                  :append="testConfig.maxVuserPerAgent"
                                  message="perfTest.config.vuserPerAgent">
                    </input-append>
                    <i class="pointer-cursor expand" @click="display.vuserPanel = !display.vuserPanel"></i>
                    <div class="pull-right">
                        <span class="badge badge-info pull-right">
                            <span v-text="i18n('perfTest.config.availVuser')"></span>
                            <span v-text="totalVuser"></span>
                        </span>
                    </div>
                    <transition name="fade">
                        <div v-show="display.vuserPanel" id="vuser-panel">
                            <input-prepend name="processes" v-model="test.processes"
                                           @change="changeProcessThreadCount"
                                           message="perfTest.config.process" extraCss="control-group">
                            </input-prepend>
                            <input-prepend name="threads" v-model="test.threads"
                                           @change="changeProcessThreadCount"
                                           message="perfTest.config.thread" extraCss="control-group">
                            </input-prepend>
                        </div>
                    </transition>
                </control-group>

                <control-group labelMessageKey="perfTest.config.script" ref="scriptNameControlGroup">
                    <select2 v-model="test.scriptName" name="scriptName" ref="scriptName" customStyle="width: 275px;" :option="{placeholder: i18n('perfTest.config.scriptInput')}"
                             :validationRules="{ required: true, scriptValidation: true }" @validationResult="$refs.scriptNameControlGroup.handleError($event)" errStyle="position: absolute;">
                        <option value=""></option>
                        <option v-for="script in scripts" :data-validate="script.validated" v-text="script.pathInShort" :value="script.path"></option>
                    </select2>
                    <input type="hidden" name="scriptRevision" :value="test.scriptRevision">
                    <button class="btn btn-mini btn-info pull-right btn-script-revision" type="button">
                        R <span v-if="test.scriptRevision !== -1" v-text="test.scriptRevision"></span>
                        <span v-else v-text="test.quickScriptRevision ? test.quickScriptRevision : 'HEAD'"></span>
                    </button>
                </control-group>

                <control-group labelMessageKey="perfTest.config.scriptResources">
                    <div class="div-resources">
                        <div class="resource" v-for="resource in resources" v-text="resource"></div>
                    </div>
                </control-group>

                <control-group labelMessageKey="perfTest.config.targetHost">
                    <a class="btn pull-right btn-mini add-host-btn" data-toggle="modal"
                       href="#add-host-modal" v-text="i18n('perfTest.config.add')">
                    </a>
                    <div class="div-host"
                         data-toggle="popover"
                         data-placement="bottom"
                         data-html="true"
                         :title="i18n('perfTest.config.targetHost')"
                         :data-content="i18n('perfTest.config.targetHost.help')">
                        <span v-for="host in targetHosts">
                            <p class="host">
                                <a class="pointer-cursor" @click="showTargetHostInfoModal(host)" v-text="host"></a>
                                <a class="pointer-cursor"><i class="icon-remove-circle" @click="removeHost(host)"></i></a>
                            </p>
                            <br style="line-height: 0">
                        </span>
                    </div>
                    <input type="hidden" name="targetHosts" :value="targetHosts.join(',')">
                </control-group>
                <hr>

                <control-group :radio="{radioValue: 'D', checked: test.threshold === 'D'}" v-model="test.threshold" labelMessageKey="perfTest.config.duration" name="threshold" id="duration">
                    <select class="select-item" id="select_hour" v-model="duration.hour" @change="changeDuration">
                        <option v-for="(v, h) in 8" :value="h" v-text="h"></option>
                    </select> :
                    <select class="select-item" id="select_min" v-model="duration.min" @change="changeDuration">
                        <option v-for="(v, m) in 60" :value="m" v-text="m < 10 ? `0${m}` : m"></option>
                    </select> :
                    <select class="select-item" id="select_sec" v-model="duration.sec" @change="changeDuration">
                        <option v-for="(v, s) in 60" :value="s" v-text="s < 10 ? `0${s}` : s"></option>
                    </select> &nbsp;&nbsp;
                    <code>HH:MM:SS</code>
                    <input type="hidden" id="duration" name="duration" :value="durationSeconds * 1000"/>
                    <input type="hidden" id="duration_hour" name="durationHour" value="0"/>
                    <vue-slider ref="durationSlider" @callback="changeDurationSlider" v-model="durationSeconds" width="278" :max="28799" tooltip="none"></vue-slider>
                </control-group>

                <control-group :radio="{radioValue: 'R', checked: test.threshold === 'R'}" v-model="test.threshold" labelMessageKey="perfTest.config.runCount" ref="runCountControlGroup" name="threshold" id="runCount">
                    <input-append name="runCount" ref="runCount"
                                  appendPrefix="perfTest.config.max"
                                  :append="testConfig.maxRunCount"
                                  @validationResult="$refs.runCountControlGroup.handleError($event)"
                                  :validationRules="{ required: true, max_value: testConfig.maxRunCount, min_value: 0 }"
                                  v-model="test.runCount"
                                  @focus="test.threshold = TEST_THRESHOLD_RUNCOUNT"
                                  message="perfTest.config.runCount">
                    </input-append>
                </control-group>

                <div class="row accordion-heading detail-config-btn-container">
                    <span class="pull-right">
                        <a @click.prevent="display.detailConfig = !display.detailConfig"
                           class="pointer-cursor" v-text="i18n('perfTest.config.showAdvancedConfig')"></a>
                    </span>
                    <hr>
                </div>

                <transition name="fade">
                    <div id="advanced-config" v-show="display.detailConfig">
                        <div class="row">
                            <div class="span3">
                                <control-group name="samplingInterval" labelMessageKey="perfTest.config.samplingInterval">
                                    <select class="select-item" id="sampling_interval"
                                            name="samplingInterval" v-model="test.samplingInterval">
                                        <option v-for="interval in samplingIntervals" :value="interval" v-text="interval"></option>
                                    </select>
                                </control-group>
                            </div>
                            <div class="span3">
                                <control-group name="ignoreSampleCount" ref="ignoreSampleCountControlGroup" labelStyle="width: 150px;" labelMessageKey="perfTest.config.ignoreSampleCount">
                                    <input-popover v-model="test.ignoreSampleCount"
                                                   ref="ignoreSampleCount"
                                                   @validationResult="$refs.ignoreSampleCountControlGroup.handleError($event)"
                                                   :validationRules="{ numeric: true }"
                                                   dataPlacement="top"
                                                   errStyle="margin-left: -120px;"
                                                   name="ignoreSampleCount"
                                                   message="perfTest.config.ignoreSampleCount" extraCss="input-mini">
                                    </input-popover>
                                </control-group>
                            </div>
                        </div>
                        <div class="row">
                            <div class="span3">
                                <control-group name="safeDistribution" labelMessageKey="perfTest.config.safeDistribution" labelHelpMessageKey="perfTest.config.safeDistribution">
                                    <input type="checkbox" name="safeDistribution" :checked="test.safeDistribution">
                                </control-group>
                            </div>
                            <div class="span3">
                                <control-group name="param" labelMessageKey="perfTest.config.param" controlsStyle="margin-left: 85px;"
                                               labelStyle="width: 70px;" ref="paramControlGroup">
                                    <input-popover name="param"
                                                   ref="param"
                                                   @validationResult="$refs.paramControlGroup.handleError($event)"
                                                   :validationRules="{ regex: /^[a-zA-Z0-9_\.,\|=]{0,50}$/ }"
                                                   dataPlacement="top"
                                                   v-model="test.param"
                                                   message="perfTest.config.param"
                                                   customStyle="width: 125px;">
                                    </input-popover>
                                </control-group>
                            </div>
                        </div>
                    </div>
                </transition>
            </div>
        </div>
        <ramp-up ref="rampUp" :test="test" :rampUpTypes="rampUpTypes"></ramp-up>
        <host-modal :id="'add-host-modal'" @add-host="addHost"></host-modal>
        <target-host-info-modal ref="targetHostInfoModal" :id="'target-info-modal'" :ip="targetHostIp"></target-host-info-modal>
    </div>
</template>

<script>
    import Base from '../../Base.vue';
    import Select2 from '../../common/Select2.vue';
    import ControlGroup from '../../common/ControlGroup.vue';
    import InputAppend from '../../common/InputAppend.vue';
    import InputPrepend from '../../common/InputPrepend.vue';
    import InputPopover from '../../common/InputPopover.vue';
    import HostModal from '../modal/HostModal.vue';
    import RampUp from './RampUp.vue';
    import VueSlider from 'vue-slider-component';
    import TargetHostInfoModal from '../modal/TargetHostInfoModal.vue';
    import { Component, Watch } from 'vue-property-decorator';
    import { Validator } from 'vee-validate';

    @Component({
        name: 'config',
        props: {
            data: {
                type: Object,
                required: true,
            },
        },
        components: { TargetHostInfoModal, ControlGroup, InputAppend, InputPrepend, InputPopover, VueSlider, HostModal, Select2, RampUp },
    })
    export default class Config extends Base {
        MAX_PROCESS_COUNT_PER_AGENT = 10;
        TEST_THRESHOLD_DURATION = 'D';
        TEST_THRESHOLD_RUNCOUNT = 'R';

        test = {
            testName: '',
            agentCount: 0,
            rampUpInitCount: 0,
            rampUpStep: 0,
            rampUpInitSleepTime: 0,
            rampUpIncrementInterval: 0,
            param: '',
            ignoreSampleCount: 0,
            runCount: 0,
            processes: 0,
            threads: 0,
            vuserPerAgent: 0,
            samplingInterval: 2,
        };

        testConfig = {
            maxRunCount: 10000,
            maxRunHour: 8,
            maxVuserPerAgent: 3000,
        };

        scripts = [];
        resources = [];

        regions = [];
        samplingIntervals = [1, 2, 3, 4, 5, 10, 30, 60];
        rampUpTypes = [];
        regionAgentCountMap = {};

        // to use Set object reactively in vue
        targetHostsChangeTracker = 1;
        targetHostIp = '';
        targetHost = new Set();

        maxAgentCount = 0;
        durationSeconds = 0;
        dataLoadFinished = false;

        display = {
            vuserPanel: false,
            detailConfig: false,
        };

        duration = {
            hour: 0,
            min: 0,
            sec: 0,
        };

        agentCountValidationRules = { required: true, agentCountValidation: true,};
        validationGroup = [];

        created() {
            this.test = this.data.test;
            this.regionAgentCountMap = this.data.regionAgentCountMap;
            this.rampUpTypes = this.data.availRampUpType;
            this.testConfig.maxRunCount = this.data.maxRunCount;
            this.testConfig.maxRunHour = this.data.maxRunHour;
            this.testConfig.maxVuserPerAgent = this.data.maxVuserPerAgent;

            this.$http.get('/perftest/api/script').then(res => {
                if (this.config.clustered) {
                    // TODO
                } else {
                    this.changeMaxAgentCount("NONE");
                }
                this.setScripts(res.data, this.test.scriptName);
                this.setDuration();
                this.setTargetHost(this.test.targetHosts);
                this.getScriptResource();
                this.finishDataLoad();
            }).catch(error => console.error(error));
        }

        mounted() {
            this.setCustomValidationRules();
            this.setCustomValidationMessages();
        }

        changeMaxAgentCount(region) {
            this.maxAgentCount = this.regionAgentCountMap[region].value;
        }

        setScripts(scripts, selectedScript) {
            if (!scripts.some(script => script.pathInShort === selectedScript)) {
                if (selectedScript) {
                    scripts.push({pathInShort: `(deleted) ${selectedScript}`, path: selectedScript, validated: -1});
                }
            }
            this.scripts = scripts;
        }

        getScriptResource() {
            if (!this.test.scriptName) {
                return;
            }

            this.$http.get('/perftest/api/resource', {
                params: {
                    scriptPath: this.test.scriptName,
                },
            }).then(res => {
                this.resources = res.data.resources;
            }).catch((error) => console.error(error));
        }

        finishDataLoad() {
            this.dataLoadFinished = true;
            this.$nextTick(() => {
                $('[data-toggle="popover"]').popover('destroy');
                $('[data-toggle="popover"]').popover({trigger: 'hover', container: '#config-container'});
                this.$refs.rampUp.updateRampUpChart();
                this.validationGroup = [this.$refs.agentCount, this.$refs.vuserPerAgent, this.$refs.ignoreSampleCount, this.$refs.param, this.$refs.runCount, this.$refs.scriptName];
            });
        }

        setCustomValidationMessages() {
            const dictionary = {
                required: () => this.i18n('common.message.validate.empty'),
                regex: (name) => {
                    if (name === 'domain') {
                        return this.i18n('perfTest.config.addHost.inputTargetDomain');
                    }
                    if (name === 'ip') {
                        return this.i18n('perfTest.config.addHost.inputTargetIp');
                    }
                    return this.i18n('perfTest.message.param');
                },
            };

            const messages = {
                en: {
                    messages: dictionary,
                },
                kr: {
                    messages: dictionary,
                },
                cn: {
                    messages: dictionary,
                },
            };

            Validator.localize(messages);
        }

        setCustomValidationRules() {
            this.$validator.extend('agentCountValidation', {
                getMessage: this.i18n('common.message.validate.max', {maxValue: this.maxAgentCount}),
                validate: agentCount => agentCount <= this.maxAgentCount,
            });

            this.$validator.extend('scriptValidation', {
                getMessage: this.i18n('perfTest.message.script'),
                validate: () => {
                    if (this.$refs.scriptName) {
                        return this.$refs.scriptName.getSelectedOptionValidate() !== '-1';
                    }
                    return true;
                },
            });
        }

        // duration format: '00:00:00'
        setDuration() {
            let durationTokens = this.test.duration.split(':');
            this.duration.hour = parseInt(durationTokens[0]);
            this.duration.min = parseInt(durationTokens[1]);
            this.duration.sec = parseInt(durationTokens[2]);
            this.changeDuration();
        }

        @Watch('test.threshold')
        watchTestThreshold() {
            if (this.$refs.runCount && this.test.threshold === 'D') {
                if (this.$refs.runCount.errors.has('runCount')) {
                    this.test.runCount = 0;
                    this.$refs.runCount.errors.clear();
                    this.$refs.runCountControlGroup.hasError = false;
                }
            }
        }

        changeVuserPerAgent() {
            this.test.processes = this.getAdjustedProcessCount(this.test.vuserPerAgent);
            this.test.threads = parseInt(this.test.vuserPerAgent / this.test.processes);
            this.updateVuserPerAgent();

            if (this.$refs.rampUp.enableRampUp) {
                this.$refs.rampUp.updateRampUpChart();
            }
        }

        changeProcessThreadCount() {
            this.updateVuserPerAgent();
            if (this.$refs.rampUp.enableRampUp) {
                this.$refs.rampUp.updateRampUpChart();
            }
        }

        updateVuserPerAgent() {
            this.test.vuserPerAgent = this.test.processes * this.test.threads;
        }

        changeDuration() {
            this.test.threshold = this.TEST_THRESHOLD_DURATION;
            this.durationSeconds = this.duration.hour * 3600 + this.duration.min * 60 + this.duration.sec;
        }

        changeDurationSlider(duration) {
            this.test.duration = this.$moment.duration(duration, 'seconds').format('hh:mm:ss');
            if (duration < 3600) {
                this.test.duration = `00:${this.test.duration}`;
            }
            if (duration < 60) {
                this.test.duration = `00:${this.test.duration}`;
            }
            this.setDuration();
        }

        hasValidationError() {
            let error = false;
            this.validationGroup.forEach(validation => error = error || validation.errors.any());
            return error;
        }

        addHost(host) {
            this.targetHost.add(host);
            this.targetHostsChangeTracker += 1;
        }

        setTargetHost(targetHost) {
            if (!targetHost) {
                return;
            }
            targetHost.split(',').forEach(host => this.targetHost.add(host));
            this.targetHostsChangeTracker += 1;
        }

        removeHost(host) {
            this.targetHost.delete(host);
            this.targetHostsChangeTracker += 1;
        }

        showTargetHostInfoModal(host) {
            const hostToken = host.split(':');
            this.targetHostIp = hostToken[1] ? hostToken[1] : hostToken[0];
            this.$refs.targetHostInfoModal.show();
        }

        getAdjustedProcessCount(vuser) {
            if (vuser < 2) {
                return 1;
            }

            let processCount = 2;
            if (vuser > 80) {
                processCount = parseInt(vuser / 40) + 1;
            }

            if (processCount > this.MAX_PROCESS_COUNT_PER_AGENT) {
                processCount = this.MAX_PROCESS_COUNT_PER_AGENT;
            }
            return processCount;
        }

        get totalVuser() {
            return this.test.agentCount * this.test.vuserPerAgent;
        }

        get targetHosts() {
            return this.targetHostsChangeTracker && Array.from(this.targetHost);
        }
    }
</script>

<style lang="less">
    #advanced-config {
        .validation-message {
            margin-left: -85px;
            margin-top: 5px;
        }

        div.row {
            width: 500px;
        }
    }
</style>

<style lang="less" scoped>
    #config-container {
        .badge-info {
            padding: 7px 20px 7px 20px;
            border-radius: 20px;
            -webkit-border-radius: 20px;
            -moz-border-radius: 20px;
        }

        #region {
            width: 110px;
        }

        #vuser-panel {
            margin-top: 5px;
        }

        .detail-config-btn-container {
            margin-top: -20px;

            span {
                margin-right: 10px;
            }
        }

        .btn-script-revision {
            position: relative;
            margin-top: 3px;
        }

        div.error {
            .btn-script-revision {
                margin-top: -45px;
            }
        }

        i {
            &.expand {
                background: url('/img/icon_expand.png') no-repeat;
                display: inline-block;
                height: 16px;
                width: 16px;
                line-height: 16px;
                vertical-align: text-top;
                position: absolute;
                margin: 8px 0 0 2px;
            }
        }

        div {
            &.div-resources {
                border: 1px solid #D6D6D6;
                height: 40px;
                margin-bottom: 8px;
                overflow-y: auto;
                border-radius: 3px 3px 3px 3px;

                .resource {
                    width: 300px;
                    color: #666666;
                    display: block;
                    margin-left: 7px;
                    margin-top: 2px;
                    margin-bottom: 2px;
                }
            }

            &.div-host {
                border: 1px solid #D6D6D6;
                height: 50px;
                margin-bottom: 8px;
                overflow-y: auto;
                border-radius: 3px;

                .host {
                    color: #666666;
                    display: inline-block;
                    margin-left: 7px;
                    margin-top: 2px;
                    margin-bottom: 2px;
                }
            }

            &.vue-slider-component {
                padding: 8px 0 !important;
            }
        }

        .select-item {
            width: 60px;
        }

        .add-host-btn {
            margin-top: 27px;
            margin-left: 287px;
            position: absolute;
        }

        .control-group {
            label {
                &.control-label {
                    width: 110px;
                }
            }
        }
    }
</style>
