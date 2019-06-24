<template>
    <div id="config-container" class="row config">
        <div class="span6">
            <fieldset>
                <legend><span v-text="i18n('perfTest.config.basicConfiguration')"></span></legend>
            </fieldset>
            <div class="form-horizontal form-horizontal-2">
                <div class="row intro agent-config-container" data-step="4" :data-intro="i18n('intro.config.basic.agent')">
                    <div class="span4">
                        <control-group :class="{error: errors.has('agentCount')}" labelMessageKey="perfTest.config.agent" ref="agentCountControlGroup">
                            <input-append name="agentCount" ref="agentCount"
                                          v-model="test.agentCount"
                                          :validationRules="agentCountValidationRules"
                                          errStyle="width: 145px; word-break: break-all; white-space: normal;"
                                          appendPrefix="perfTest.config.max"
                                          :append="maxAgentCount"
                                          message="perfTest.config.agent">
                            </input-append>
                        </control-group>
                    </div>

                    <div v-if="ngrinder.config.clustered" class="span2">
                        <control-group :class="{error: errors.has('region')}" ref="regionControlGroup" labelMessageKey="perfTest.config.region" labelHelpMessageKey="perfTest.config.region" labelStyle="margin-left: -50px; width: 80px;">
                            <select2 name="region" ref="region" v-model="test.region" @change="changeMaxAgentCount"
                                     class="pull-right required" customStyle="width: 110px;" :validationRules="{ required: true }">
                                <option v-for="region in config.regions" :value="region" :selected="region === test.region" v-text="region"></option>
                            </select2>
                        </control-group>
                    </div>
                </div>

                <control-group :class="{error: errors.has('vuserPerAgent')}" labelMessageKey="perfTest.config.vuserPerAgent" ref="vuserPerAgentControlGroup" dataStep="5" :dataIntro="i18n('intro.config.basic.vuser')">
                    <input-append name="vuserPerAgent" ref="vuserPerAgent"
                                  v-model="test.vuserPerAgent"
                                  :validationRules="{ required: true, max_value: config.maxVuserPerAgent, min_value: 1 }"
                                  @change="changeVuserPerAgent"
                                  errStyle="margin: 0; width: 140px;"
                                  appendPrefix="perfTest.config.max"
                                  :append="config.maxVuserPerAgent"
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

                <control-group :class="{error: errors.has('scriptName')}" labelMessageKey="perfTest.config.script">
                    <select2 v-model="test.scriptName" name="scriptName" ref="scriptSelect" customStyle="width: 275px;" :option="{placeholder: i18n('perfTest.config.scriptInput')}"
                             :validationRules="{ required: true, scriptValidation: true }" errStyle="position: absolute;">
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
                    <a class="btn pull-right btn-mini add-host-btn" @click.prevent="$refs.addHostModal.show" v-text="i18n('perfTest.config.add')">
                    </a>
                    <div class="div-host"
                         data-toggle="popover"
                         data-placement="bottom"
                         data-html="true"
                         :title="i18n('perfTest.config.targetHost')"
                         :data-content="i18n('perfTest.config.targetHost.help')">
                        <span v-for="(host, index) in targetHost">
                            <p class="host">
                                <a class="pointer-cursor" @click="showTargetHostInfoModal(host)" v-text="host"></a>
                                <a class="pointer-cursor"><i class="icon-remove-circle" @click="targetHost.splice(index, 1)"></i></a>
                            </p>
                            <br style="line-height: 0">
                        </span>
                    </div>
                    <input type="hidden" name="targetHosts" :value="targetHost.join(',')">
                </control-group>
                <hr>

                <div class="threshold-container">
                    <control-group :class="{error: errors.has('duration')}" :radio="{radioValue: 'D', checked: test.threshold === 'D'}" v-model="test.threshold"
                                   ref="thresholdControlGroup" labelMessageKey="perfTest.config.duration" name="threshold" id="duration">
                        <select class="select-item" id="select_hour" v-model="duration.hour" @change="changeDuration({ focus: true, updateSlider: true })">
                            <option v-for="(v, h) in durationMaxHour" :value="h" v-text="h"></option>
                        </select> :
                        <select class="select-item" id="select_min" v-model="duration.min" @change="changeDuration({ focus: true, updateSlider: true })">
                            <option v-for="(v, m) in 60" :value="m" v-text="m < 10 ? `0${m}` : m"></option>
                        </select> :
                        <select class="select-item" id="select_sec" v-model="duration.sec" @change="changeDuration({ focus: true, updateSlider: true })">
                            <option v-for="(v, s) in 60" :value="s" v-text="s < 10 ? `0${s}` : s"></option>
                        </select> &nbsp;&nbsp;
                        <code>HH:MM:SS</code>
                        <input v-validate="{min_value: test.threshold === 'D' ? 1 : 0}" type="hidden" name="duration" v-model="durationMs"/>
                        <duration-slider @change="changeDurationSlider" ref="durationSlider" :durationMs="durationMs" :maxRunHour="config.maxRunHour"></duration-slider>
                        <div v-show="errors.has('duration')" class="validation-message" v-text="errors.first('duration')"></div>
                    </control-group>

                    <control-group :class="{error: errors.has('runCount')}" :radio="{radioValue: 'R', checked: test.threshold === 'R'}" v-model="test.threshold" labelMessageKey="perfTest.config.runCount" ref="runCountControlGroup" name="threshold" id="runCount">
                        <input-append name="runCount" ref="runCount"
                                      appendPrefix="perfTest.config.max"
                                      :append="config.maxRunCount"
                                      :validationRules="{required: true, max_value: config.maxRunCount, min_value: test.threshold === 'R' ? 1 : 0}"
                                      v-model="test.runCount"
                                      @focus="test.threshold = 'R'"
                                      message="perfTest.config.runCount">
                        </input-append>
                    </control-group>
                </div>

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
                                <control-group :class="{error: errors.has('ignoreSampleCount')}" name="ignoreSampleCount" labelStyle="width: 150px;" labelMessageKey="perfTest.config.ignoreSampleCount">
                                    <input-popover v-model="test.ignoreSampleCount"
                                                   ref="ignoreSampleCount"
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
                                <control-group :class="{error: errors.has('param')}" labelMessageKey="perfTest.config.param"
                                               controlsStyle="margin-left: 85px;" labelStyle="width: 70px;" ref="paramControlGroup">
                                    <input-popover name="param"
                                                   ref="param"
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
        <ramp-up ref="rampUp" :test="test" :rampUpTypes="config.rampUpTypes"></ramp-up>
        <host-modal ref="addHostModal" @add-host="addHost"></host-modal>
        <target-host-info-modal ref="targetHostInfoModal" :ip="targetHostIp"></target-host-info-modal>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import { Component, Watch, Inject } from 'vue-property-decorator';
    import Base from '../../Base.vue';
    import Select2 from '../../common/Select2.vue';
    import ControlGroup from '../../common/ControlGroup.vue';
    import InputAppend from '../../common/InputAppend.vue';
    import InputPrepend from '../../common/InputPrepend.vue';
    import InputPopover from '../../common/InputPopover.vue';
    import MessagesMixin from '../../common/mixin/MessagesMixin.vue';
    import HostModal from '../modal/HostModal.vue';
    import RampUp from './RampUp.vue';
    import TargetHostInfoModal from '../modal/TargetHostInfoModal.vue';
    import DurationSlider from './DurationSlider.vue';

    @Component({
        name: 'config',
        props: {
            testProps: {
                type: Object,
                required: true,
            },
            config: {
                type: Object,
                required: true,
            },
        },
        components: { DurationSlider, TargetHostInfoModal, ControlGroup, InputAppend, InputPrepend, InputPopover, HostModal, Select2, RampUp },
    })
    export default class Config extends Mixins(Base, MessagesMixin) {
        @Inject() $validator;

        test = {
            param: '',
            region: '',
            testName: '',
            agentCount: 0,
            rampUpInitCount: 0,
            rampUpStep: 0,
            rampUpInitSleepTime: 0,
            rampUpIncrementInterval: 0,
            ignoreSampleCount: 0,
            runCount: 0,
            processes: 0,
            threads: 0,
            vuserPerAgent: 0,
            samplingInterval: 2,
        };

        scripts = [];
        resources = [];

        samplingIntervals = [1, 2, 3, 4, 5, 10, 30, 60];
        regionAgentCountMap = {};

        targetHostIp = '';
        targetHost = [];

        maxAgentCount = 0;
        durationMs = 0;

        display = {
            vuserPanel: false,
            detailConfig: false,
        };

        durationMaxHour = 0;
        duration = {
            hour: 0,
            min: 0,
            sec: 0,
        };

        agentCountValidationRules = { required: true, agentCountValidation: true, min_value: 0 };

        created() {
            this.setCustomValidationRules();
            this.test = this.testProps;
            this.changeMaxAgentCount();
            this.initDurationFromDurationStr();
            this.changeDuration();
            this.setTargetHost(this.test.targetHosts);
            this.getScriptResource();
        }

        mounted() {
            this.getScripts();

            const durationHour = parseInt(this.durationMs / 3600000) + 1;
            this.durationMaxHour = (durationHour > this.config.maxRunHour) ? durationHour : this.config.maxRunHour;

            this.$nextTick(() => {
                $('[data-toggle="popover"]').popover('destroy');
                $('[data-toggle="popover"]').popover({ trigger: 'hover', container: '#config-container' });
                this.$refs.rampUp.updateRampUpChart();
            });
        }

        changeMaxAgentCount() {
            if (this.test.region && this.config.regionAgentCountMap[this.test.region]) {
                this.maxAgentCount = this.config.regionAgentCountMap[this.test.region].value;
                return;
            }
            this.maxAgentCount = 0;
        }

        setScripts(scripts, selectedScript) {
            if (!scripts.some(script => script.pathInShort === selectedScript)) {
                if (selectedScript) {
                    scripts.push({ pathInShort: `(deleted) ${selectedScript}`, path: selectedScript, validated: -1 });
                }
            }
            this.scripts = scripts;
        }

        getScripts() {
            this.$http.get('/perftest/api/script').then(res => {
                if (!this.ngrinder.config.clustered) {
                    this.test.region = 'NONE';
                }
                this.setScripts(res.data, this.test.scriptName);
                this.$nextTick(() => {
                    this.$refs.scriptSelect.selectValue(this.test.scriptName);
                });
            }).catch(() => this.showErrorMsg(this.i18n('navigator.script')));
        }

        getScriptResource() {
            if (!this.test.scriptName) {
                return;
            }

            this.$http.get('/perftest/api/resource', {
                params: {
                    scriptPath: this.test.scriptName,
                },
            })
            .then(res => this.resources = res.data.resources)
            .catch(() => this.showErrorMsg(this.i18n('perfTest.config.scriptResources')));
        }

        setCustomValidationRules() {
            this.$validator.extend('agentCountValidation', {
                getMessage: () => this.i18n('common.message.validate.max', { maxValue: this.maxAgentCount }),
                validate: agentCount => agentCount <= this.maxAgentCount,
            });

            this.$validator.extend('scriptValidation', {
                getMessage: this.i18n('perfTest.message.script'),
                validate: () => {
                    if (this.$refs.scriptSelect) {
                        return this.$refs.scriptSelect.getSelectedOptionValidate() !== '-1';
                    }
                    return true;
                },
            });
        }

        // duration string format: '00:00:00'
        initDurationFromDurationStr() {
            const durationTokens = this.test.duration.split(':');
            this.duration.hour = parseInt(durationTokens[0]);
            this.duration.min = parseInt(durationTokens[1]);
            this.duration.sec = parseInt(durationTokens[2]);
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
            this.test.processes = getProcessCount(this.test.vuserPerAgent);
            this.test.threads = getThreadCount(this.test.vuserPerAgent);
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

        changeDuration(options) {
            if (options && options.focus) {
                this.test.threshold = 'D';
            }
            this.durationMs = (this.duration.hour * 3600 + this.duration.min * 60 + this.duration.sec) * 1000;
            if (options && options.updateSlider) {
                this.$refs.durationSlider.initSliderFromDurationMs(this.durationMs);
            }
        }

        changeDurationSlider(durationSec) {
            this.test.duration = this.$moment.duration(durationSec, 'seconds').format('hh:mm:ss');
            if (durationSec < 3600) {
                this.test.duration = `00:${this.test.duration}`;
            }
            if (durationSec < 60) {
                this.test.duration = `00:${this.test.duration}`;
            }
            this.initDurationFromDurationStr();
            this.changeDuration({ focus: true });
        }

        addHost(newHost) {
            if (this.targetHosts.some(host => host === newHost)) {
                return;
            }
            this.targetHost.push(newHost);
        }

        setTargetHost(targetHost) {
            if (!targetHost) {
                return;
            }
            targetHost.split(',').forEach(host => this.targetHost.push(host));
        }

        showTargetHostInfoModal(host) {
            const hostToken = host.split(':');
            this.targetHostIp = hostToken[1] ? hostToken[1] : hostToken[0];
            this.$refs.targetHostInfoModal.show();
        }

        get totalVuser() {
            return this.test.agentCount * this.test.vuserPerAgent;
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

        .threshold-container {
            margin-left: 2px;
        }

        .btn-script-revision {
            position: relative;
            margin-top: 3px;
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
