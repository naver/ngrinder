<template>
    <div class="row config-container">
        <div class="basic-config-container">
            <fieldset>
                <legend class="border-bottom">
                    <span v-text="i18n('perfTest.config.basicConfiguration')"></span>
                </legend>
            </fieldset>
            <div class="form-horizontal form-horizontal-2">
                <div class="row intro agent-config-container"
                     :data-step="shownBsTab ? 4 : undefined"
                     :data-intro="shownBsTab ? i18n('intro.config.basic.agent') : undefined">
                    <div class="agent-count-container">
                        <control-group id="agentCount" :class="{ error: errors.has('agentCount') }"
                                       labelMessageKey="perfTest.config.agent" ref="agentCountControlGroup">
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
                    <div v-if="ngrinder.config.clustered" class="agent-region-container ml-auto">
                        <control-group id="region" :class="{ error: errors.has('region') }"
                                       ref="regionControlGroup"
                                       labelStyle="position: absolute;"
                                       labelMessageKey="perfTest.config.region"
                                       labelHelpMessageKey="perfTest.config.region">
                            <select2 name="region" ref="region" v-model="test.region" @change="changeMaxAgentCount"
                                     errStyle="position: absolute; max-width: 170px; margin-left: -51px;"
                                     class="float-right required" customStyle="width: 110px;" :validationRules="{ required: true }">
                                <option v-for="region in config.regions" :value="region" :selected="region === test.region" v-text="region"></option>
                            </select2>
                        </control-group>
                    </div>
                </div>
                <control-group id="vuserPerAgent" :class="{ error: errors.has('vuserPerAgent') }" labelMessageKey="perfTest.config.vuserPerAgent"
                               ref="vuserPerAgentControlGroup"
                               :data-step="shownBsTab ? 5 : undefined"
                               :data-intro="shownBsTab ? i18n('intro.config.basic.vuser') : undefined">
                    <div class="vuser-per-agent-container">
                        <input-append name="vuserPerAgent" ref="vuserPerAgent"
                                      v-model="test.vuserPerAgent"
                                      :validationRules="{ required: true, max_value: config.maxVuserPerAgent, min_value: 1 }"
                                      @change="changeVuserPerAgent"
                                      errStyle="white-space: nowrap; width: 130px;"
                                      appendPrefix="perfTest.config.max"
                                      :append="config.maxVuserPerAgent"
                                      message="perfTest.config.vuserPerAgent">
                        </input-append>
                    </div>
                    <i class="pointer-cursor expand" :style="`background: url('${contextPath}/img/icon_expand.png') no-repeat`" @click="display.vuserPanel = !display.vuserPanel"></i>
                    <div class="float-right">
                        <span class="badge badge-info float-right">
                            <span v-text="i18n('perfTest.config.availVuser')"></span>
                            <span v-text="totalVuser"></span>
                        </span>
                    </div>
                    <div v-show="display.vuserPanel" class="vuser-panel">
                        <input-prepend name="processes" v-model="test.processes"
                                       @change="changeProcessThreadCount"
                                       message="perfTest.config.process" extraCss="control-group">
                        </input-prepend>
                        <input-prepend name="threads" v-model="test.threads"
                                       @change="changeProcessThreadCount"
                                       message="perfTest.config.thread" extraCss="control-group">
                        </input-prepend>
                    </div>
                </control-group>

                <control-group :class="{ error: errors.has('scriptName'), 'script-control-group': true }" labelMessageKey="perfTest.config.script"
                               :data-step="shownBsTab ? 6 : undefined"
                               :data-intro="shownBsTab ? i18n('intro.config.basic.script') : undefined">
                    <select2 v-model="test.scriptName" name="scriptName" ref="scriptSelect" customStyle="width: 250px;"
                             :option="{ placeholder: i18n('perfTest.config.scriptInput') }"
                             @change="changeScript"
                             :validationRules="{ required: true, scriptValidation: true }" errStyle="position: absolute;">
                        <option value=""></option>
                        <option v-for="script in scripts"
                                :data-revision="script.revision"
                                :data-validate="script.validated"
                                v-text="script.pathInShort"
                                :value="script.path">
                        </option>
                    </select2>
                    <button v-show="display.showScriptBtn" class="btn btn-info float-right btn-script-revision" type="button" @click="showScript">
                        <i class="fa fa-file mr-1"></i>
                        R
                        <span v-if="test.scriptRevision !== -1" v-text="test.scriptRevision"></span>
                        <span v-else v-text="'HEAD'"></span>
                    </button>
                </control-group>

                <control-group labelMessageKey="perfTest.config.scriptResources"
                               :data-step="shownBsTab ? 7 : undefined"
                               :data-intro="shownBsTab ? i18n('intro.config.basic.scriptResources') : undefined">
                    <div class="div-resources">
                        <div class="resource" v-for="resource in resources" v-text="resource"></div>
                    </div>
                </control-group>

                <control-group labelMessageKey="perfTest.config.targetHost"
                               :data-step="shownBsTab ? 8 : undefined"
                               :data-intro="shownBsTab ? i18n('intro.config.basic.target') : undefined">
                    <button class="btn btn-info float-right add-host-btn" @click.prevent="$refs.addHostModal.show">
                        <i class="fa fa-plus"></i>
                        <span v-text="i18n('perfTest.config.add')"></span>
                    </button>
                    <div class="div-host"
                         data-toggle="popover"
                         data-html="true"
                         data-placement="bottom"
                         data-trigger="hover"
                         :title="i18n('perfTest.config.targetHost')"
                         :data-content="i18n('perfTest.config.targetHost.help')">
                        <div v-for="(host, index) in targetHosts" class="host">
                            <a href="#" @click="showTargetHostInfoModal(host)" v-text="host"></a>
                            <i class="fa fa-times-circle pointer-cursor" @click="targetHosts.splice(index, 1)"></i>
                        </div>
                    </div>
                    <input type="hidden" name="targetHosts" :value="targetHosts.join(',')">
                </control-group>
                <hr>

                <div class="threshold-container">
                    <control-group :class="{ error: errors.has('duration') }" :radio="{ radioValue: 'D', checked: test.threshold === 'D' }" v-model="test.threshold"
                                   ref="thresholdControlGroup" labelMessageKey="perfTest.config.duration" name="threshold" id="duration"
                                   :data-step="shownBsTab ? 9 : undefined"
                                   :data-intro="shownBsTab ? i18n('intro.config.basic.duration') : undefined">
                        <select class="select-item form-control" v-model="duration.hour" @change="changeDuration({ focus: true, updateSlider: true })">
                            <option v-for="(v, h) in durationMaxHour" :value="h" v-text="h"></option>
                        </select> :
                        <select class="select-item form-control" v-model="duration.min" @change="changeDuration({ focus: true, updateSlider: true })">
                            <option v-for="(v, m) in 60" :value="m" v-text="m < 10 ? `0${m}` : m"></option>
                        </select> :
                        <select class="select-item form-control" v-model="duration.sec" @change="changeDuration({ focus: true, updateSlider: true })">
                            <option v-for="(v, s) in 60" :value="s" v-text="s < 10 ? `0${s}` : s"></option>
                        </select>
                        <code>HH:MM:SS</code>
                        <input v-validate="{ min_value: test.threshold === 'D' ? 1 : 0 }" type="hidden" name="duration" v-model="durationMs"/>
                        <duration-slider @change="changeDurationSlider" ref="durationSlider" :durationMs="durationMs" :maxRunHour="config.maxRunHour"></duration-slider>
                        <div v-show="errors.has('duration')" class="validation-message" v-text="errors.first('duration')"></div>
                    </control-group>
                    <control-group id="runCount" :class="{ error: errors.has('runCount') }" :radio="{ radioValue: 'R', checked: test.threshold === 'R' }" v-model="test.threshold"
                                   labelMessageKey="perfTest.config.runCount" ref="runCountControlGroup" name="threshold"
                                   controlsStyle="height: 45px;"
                                   :data-step="shownBsTab ? 10 : undefined"
                                   :data-intro="shownBsTab ? i18n('intro.config.basic.runcount') : undefined">
                        <input-append name="runCount" ref="runCount"
                                      appendPrefix="perfTest.config.max"
                                      v-model="test.runCount"
                                      @focus="test.threshold = 'R'"
                                      message="perfTest.config.runCount"
                                      :append="config.maxRunCount"
                                      :validationRules="{ required: true, max_value: config.maxRunCount, min_value: test.threshold === 'R' ? 1 : 0 }">
                        </input-append>
                    </control-group>
                </div>

                <div class="row detail-config-btn-container">
                    <a href="" @click.prevent="display.detailConfig = !display.detailConfig"
                       class="pointer-cursor" v-text="i18n('perfTest.config.showAdvancedConfig')"></a>
                </div>

                <div class="advanced-config" v-show="display.detailConfig">
                    <div class="row">
                        <control-group name="samplingInterval" labelMessageKey="perfTest.config.samplingInterval">
                            <select class="select-item form-control" name="samplingInterval" v-model="test.samplingInterval">
                                <option v-for="interval in samplingIntervals" :value="interval" v-text="interval"></option>
                            </select>
                        </control-group>
                        <control-group :class="{ error: errors.has('ignoreSampleCount') }" name="ignoreSampleCount"
                                       labelMessageKey="perfTest.config.ignoreSampleCount">
                            <input-popover v-model="test.ignoreSampleCount"
                                           ref="ignoreSampleCount"
                                           dataPlacement="top"
                                           name="ignoreSampleCount"
                                           message="perfTest.config.ignoreSampleCount"
                                           extraCss="input-mini"
                                           errStyle="white-space: nowrap;"
                                           :validationRules="{ numeric: true }">
                            </input-popover>
                        </control-group>
                    </div>
                    <div class="row">
                        <control-group name="safeDistribution" labelMessageKey="perfTest.config.safeDistribution"
                                       labelHelpMessageKey="perfTest.config.safeDistribution">
                            <input type="checkbox" name="safeDistribution" v-model="test.safeDistribution">
                        </control-group>
                        <control-group :class="{error: errors.has('param')}" labelMessageKey="perfTest.config.param"
                                       controlsStyle="margin-left: 85px;" labelStyle="width: 70px;" ref="paramControlGroup">
                            <input-popover name="param"
                                           ref="param"
                                           dataPlacement="top"
                                           v-model="test.param"
                                           message="perfTest.config.param"
                                           customStyle="width: 125px;"
                                           errStyle="white-space: nowrap;"
                                           :validationRules="{ regex: /^[a-zA-Z0-9_\.,\|=]{0,50}$/ }">
                            </input-popover>
                        </control-group>
                    </div>
                </div>
            </div>
        </div>
        <div :data-step="shownBsTab ? 11 : undefined"
             :data-intro="shownBsTab ? i18n('intro.config.rampup') : undefined">
            <ramp-up ref="rampUp" :testProp="testProp" :rampUpTypes="config.rampUpTypes"></ramp-up>
        </div>
        <host-modal ref="addHostModal" @add-host="addHost"></host-modal>
        <target-host-info-modal ref="targetHostInfoModal" :ip="targetHostIp"></target-host-info-modal>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import { Component, Prop, Watch, Inject } from 'vue-property-decorator';
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
        components: { DurationSlider, TargetHostInfoModal, ControlGroup, InputAppend, InputPrepend, InputPopover, HostModal, Select2, RampUp },
    })
    export default class Config extends Mixins(Base, MessagesMixin) {
        @Inject() $validator;

        @Prop({ type: Object, required: true })
        testProp;

        @Prop({ type: Array, required: true })
        scriptsProp;

        @Prop({ type: Object, required: true })
        config;

        test = {};

        scripts = [];
        resources = [];

        samplingIntervals = [1, 2, 3, 4, 5, 10, 30, 60];
        regionAgentCountMap = {};

        targetHostIp = '';
        targetHosts = [];

        maxAgentCount = 0;
        durationMs = 0;
        shownBsTab = false;

        display = {
            vuserPanel: false,
            detailConfig: false,
            showScriptBtn: true,
        };

        durationMaxHour = 0;
        duration = {
            hour: 0,
            min: 0,
            sec: 0,
        };

        agentCountValidationRules = { required: true, agentCountValidation: true, min_value: 0 };

        created() {
            Object.assign(this.test, this.testProp);
            this.scripts = this.scriptsProp;
            this.setCustomValidationRules();
            this.setDurationFromDurationStr();
            this.changeDuration();
            this.setTargetHosts(this.test.targetHosts);
            this.getScriptResource();
        }

        mounted() {
            if (!this.ngrinder.config.clustered) {
                this.test.region = 'NONE';
            }
            this.setScripts(this.test.scriptName);
            this.$nextTick(() => {
                this.$refs.scriptSelect.selectValue(this.test.scriptName);
                this.$validator.validate('scriptName');
            });

            this.changeMaxAgentCount();

            const durationHour = parseInt(this.durationMs / 3600000) + 1;
            this.durationMaxHour = (durationHour > this.config.maxRunHour) ? durationHour : this.config.maxRunHour;

            this.$nextTick(() => {
                $('[data-toggle="popover"]').popover();
                this.$refs.rampUp.updateRampUpChart();
            });
        }

        changeMaxAgentCount() {
            if (this.test.region && this.config.regionAgentCountMap[this.test.region]) {
                this.maxAgentCount = this.config.regionAgentCountMap[this.test.region];
            } else {
                this.maxAgentCount = 0;
            }
            this.$validator.validate('agentCount');
        }

        getParams() {
            return {
                param: this.test.param,
                region: this.test.region,
                ignoreSampleCount: this.test.ignoreSampleCount,
                scriptName: this.test.scriptName,
                scriptRevision: this.test.scriptRevision,
                targetHosts: this.targetHosts.join(','),
                threshold: this.test.threshold,
                runCount: this.test.runCount,
                processes: this.test.processes,
                agentCount: this.test.agentCount,
                threads: this.test.threads,
                safeDistribution: this.test.safeDistribution,
                vuserPerAgent: this.test.vuserPerAgent,
                samplingInterval: this.test.samplingInterval,
                duration: this.durationMs,
            };
        }

        setScripts(selectedScript) {
            if (!this.scripts.some(script => script.path === selectedScript)) {
                if (selectedScript) {
                    this.scripts.push({ pathInShort: `(deleted) ${selectedScript}`, path: selectedScript, validated: -1 });
                    this.display.showScriptBtn = false;
                }
            }
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

        showScript() {
            let showScriptUrl = `${this.contextPath}/script/detail/${this.test.scriptName}?r=${this.test.scriptRevision}`;
            if (this.isAdmin || this.isSuperUser) {
                showScriptUrl += `&ownerId=${this.test.createdUser.userId}`;
            }
            const openedWindow = window.open(showScriptUrl, 'scriptSource');
            openedWindow.focus();
        }

        changeScript(revision) {
            if (this.$refs.scriptSelect.getSelectedOptionValidate() !== '-1') {
                this.test.scriptRevision = revision;
                this.getTargetHosts();
                this.getScriptResource();
                this.display.showScriptBtn = true;
            } else {
                this.test.scriptRevision = -1;
                this.targetHosts = [];
                this.resources = [];
                this.display.showScriptBtn = false;
            }
        }

        getTargetHosts() {
            this.$http.get(`/script/api/detail/${this.test.scriptName}?r=${this.test.scriptRevision}`)
                .then(res => {
                    if (res.data.file && res.data.file.properties.targetHosts) {
                        this.targetHosts = res.data.file.properties.targetHosts.split(',');
                    } else {
                        this.targetHosts = [];
                    }
                });
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
        setDurationFromDurationStr() {
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
            } else {
                this.$refs.runCount.focus();
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

            this.$nextTick(() => {
                this.$validator.validate('vuserPerAgent');
            });
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
                this.$refs.durationSlider.setDurationMs(this.durationMs);
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
            this.setDurationFromDurationStr();
            this.changeDuration({ focus: true });
        }

        addHost(newHost) {
            if (this.targetHosts.some(host => host === newHost)) {
                return;
            }
            this.targetHosts.push(newHost);
        }

        setTargetHosts(targetHosts) {
            if (!targetHosts) {
                return;
            }
            targetHosts.split(',').forEach(host => this.targetHosts.push(host));
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
    .config-container {
        .advanced-config {
            margin-top: 10px;

            .row {
                height: 48px;
                width: 480px;

                .control-group {
                    width: 240px;
                }
            }
        }

        .input-append {
            input {
                @media screen and (-ms-high-contrast: active), (-ms-high-contrast: none) {
                    max-width: 74px;
                }
            }
        }
    }
</style>

<style lang="less" scoped>
    .config-container {
        .basic-config-container {
            width: 460px;

            .form-horizontal {
                margin-top: 10px;
            }

            .badge-info {
                font-size: 12px;
                padding: 7px 20px;
                border-radius: 20px;
                max-width: 145px;
                -webkit-border-radius: 20px;
                -moz-border-radius: 20px;
            }

            .agent-config-container {
                .agent-count-container {
                    max-width: 285px;
                    height: 61px;

                    .input-append {
                        width: 155px;
                    }
                }

                .agent-region-container {
                    width: 175px;
                    max-width: 175px;
                }
            }

            .vuser-per-agent-container {
                height: 48px;
                max-width: 173px;
                display: inline-block;
            }
        }

        .vuser-panel {
            .input-prepend-container {
                width: 130px;
            }

            .input-group {
                display: inline-flex;
                margin: 0;
            }
        }

        .detail-config-btn-container {
            justify-content: flex-end;

            span {
                margin-right: 10px;
            }
        }

        .threshold-container {
            margin-left: 2px;

            .select-item {
                display: inline-block;
                padding: 4px 6px;
            }
        }

        .btn-script-revision {
            position: relative;

            i {
                vertical-align: baseline;
            }
        }

        i {
            &.expand {
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
                border-radius: 3px;

                .resource {
                    width: 300px;
                    color: #666;
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
                    color: #666;
                    margin: 2px 0 2px 7px;
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
            font-size: 10px;
            padding: 1px 3px;
            margin-top: 30px;
            margin-left: 283px;
            position: absolute;

            i {
                vertical-align: initial;
            }
        }

        .control-group {
            margin-bottom: 5px;

            &.script-control-group {
                margin-bottom: 20px;
            }

            label {
                &.control-label {
                    width: 110px;
                }
            }
        }
    }
</style>
