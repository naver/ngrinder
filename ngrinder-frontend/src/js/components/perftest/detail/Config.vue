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
                        <control-group id="agentCount" :class="{ error: errors.has('agentCount') || errors.has('region') }"
                                       labelMessageKey="perfTest.config.agent">
                            <div class="input-group">
                                <div v-if="ngrinder.config.clustered" class="input-group-prepend">
                                    <button class="btn p-0 select-region-btn dropdown-toggle"
                                            type="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                        <span class="d-flex region-popover-wrapper"
                                              data-trigger="hover"
                                              data-toggle="popover"
                                              data-html="true"
                                              :title="i18n('perfTest.config.region')"
                                              :data-content="i18n('perfTest.config.region.help')">
                                            <span class="flex-grow-1 text-left" v-text="currentRegion"></span>
                                            <i class="fa fa-caret-down"></i>
                                            <input type="hidden" name="region" v-validate="{ regionValidation: true, required: true }" v-model="test.config.region"/>
                                        </span>
                                    </button>
                                    <div class="dropdown-menu">
                                        <a v-for="region in config.regions" class="dropdown-item pointer-cursor"
                                           @click="test.config.region = region" v-text="i18n(region)"></a>
                                    </div>
                                </div>
                                <input id="agentCount" name="agentCount" class="form-control agent-count-input"
                                       type="number" ref="agentCount" min="0"
                                       v-validate="agentCountValidationRules" v-model="test.config.agentCount"
                                       data-trigger="hover"
                                       data-toggle="popover"
                                       data-html="true"
                                       :title="i18n('perfTest.config.agent')"
                                       :data-content="i18n('perfTest.config.agent.help')"
                                       data-placement="right"/>
                                <div class="input-group-append">
                                    <span class="input-group-text">
                                        <span class="mr-1" v-text="i18n('perfTest.config.max')"></span>
                                        <span v-text="maxAgentCount"></span>
                                    </span>
                                </div>
                            </div>
                            <div class="validation-message"
                                 v-visible="errors.has('agentCount') || errors.has('region')"
                                 v-text="errors.first('agentCount') || errors.first('region')"></div>
                        </control-group>
                    </div>
                    <div class="ml-auto">
                        <span class="badge badge-info float-right">
                            <span v-text="i18n('perfTest.config.availVuser')"></span>
                            <span v-text="totalVuser"></span>
                        </span>
                    </div>
                </div>

                <control-group id="vuserPerAgent" :class="{ error: errors.has('vuserPerAgent') }"
                               labelMessageKey="perfTest.config.vuserPerAgent"
                               ref="vuserPerAgentControlGroup"
                               :data-step="shownBsTab ? 5 : undefined"
                               :data-intro="shownBsTab ? i18n('intro.config.basic.vuser') : undefined">
                    <div class="d-flex vuser-per-agent-container">
                        <div>
                            <input-append name="vuserPerAgent" ref="vuserPerAgent"
                                          type="number" v-model="test.config.vuserPerAgent"
                                          :validationRules="{ required: true, max_value: config.maxVuserPerAgent, min_value: 1 }"
                                          @change="changeVuserPerAgent"
                                          errStyle="white-space: nowrap; width: 150px;"
                                          appendPrefix="perfTest.config.max"
                                          :title-content="i18n('perfTest.config.vuserPerAgent').replace(/<br>/g, ' ')"
                                          :append="config.maxVuserPerAgent"
                                          message="perfTest.config.vuserPerAgent">
                            </input-append>
                        </div>
                        <div class="vuser-panel row border-left">
                            <input-prepend name="processes" type="number" v-model.number="test.config.processes"
                                           @change="changeProcessThreadCount"
                                           message="perfTest.config.process" extraCss="control-group">
                            </input-prepend>
                            <input-prepend name="threads" type="number" v-model.number="test.config.threads"
                                           @change="changeProcessThreadCount"
                                           message="perfTest.config.thread" extraCss="control-group">
                            </input-prepend>
                        </div>
                    </div>
                </control-group>

                <control-group :class="{ error: errors.has('scriptName'), 'script-control-group': true }" labelMessageKey="perfTest.config.script"
                               :data-step="shownBsTab ? 6 : undefined"
                               :data-intro="shownBsTab ? i18n('intro.config.basic.script') : undefined">
                    <select2 v-model="test.config.scm" name="scm"
                             ref="scmSelect" customStyle="width: 90px;"
                             @change="changeScriptStorage">
                        <option value="svn">svn</option>
                        <option v-show="config.github && config.github.length > 0" v-for="gitHubConfig in config.github"
                                v-text="gitHubConfig.name"
                                :value="`${gitHubConfig.name}:${gitHubConfig.revision}`">
                        </option>
                        <option v-if="!config.github" class="add-github" value="addGitHub" v-text="i18n('script.github.add.config')"></option>
                    </select2>
                    <select2 v-model="test.config.scriptName" name="scriptName" ref="scriptSelect" customStyle="width: 430px;"
                             :option="{ placeholder: i18n('perfTest.config.scriptInput') }"
                             @change="changeScript"
                             @opening="openingScriptSelect"
                             :validationRules="{ required: true, scriptValidation: true }" errStyle="position: absolute;">
                        <option value=""></option>
                        <option v-for="script in scripts"
                                :data-validate="script.validated"
                                v-text="script.pathInShort"
                                :value="script.path">
                        </option>
                    </select2>
                    <button v-show="showRevisonBtn" class="btn btn-info float-right btn-script-revision" type="button" @click="showScript">
                        <i class="fa fa-file mr-1"></i>
                        R
                        <span v-if="test.config.scriptRevision === -1">HEAD</span>
                        <span v-else v-text="test.config.scriptRevision"></span>
                    </button>
                    <button v-show="display.showGitHubRefreshBtn" class="btn btn-info float-right btn-github-refresh" type="button" @click="LoadGitHubScript(true)">
                        <i class="fa fa-refresh mr-2"></i><span>Refresh</span>
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
                            <i class="fa fa-times-circle pointer-cursor" @click="removeHost(host, index)"></i>
                        </div>
                    </div>
                    <input type="hidden" name="targetHosts" :value="targetHosts.join(',')">
                </control-group>
                <hr>

                <div class="threshold-container">
                    <control-group id="duration" name="threshold" :class="{ error: errors.has('duration') }"
                                   :radio="{ radioValue: 'D', checked: test.config.threshold === 'D' }"
                                   labelMessageKey="perfTest.config.duration"
                                   v-model="test.config.threshold"
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
                        <input v-validate="{ min_value: test.config.threshold === 'D' ? 1 : 0 }" type="hidden" name="duration" v-model="test.config.duration"/>
                        <duration-slider @change="changeDurationSlider" ref="durationSlider" :durationMs="test.config.duration" :maxRunHour="config.maxRunHour"></duration-slider>
                        <div v-show="errors.has('duration')" class="validation-message" v-text="errors.first('duration')"></div>
                    </control-group>
                    <control-group id="runCount" :class="{ error: errors.has('runCount') }" :radio="{ radioValue: 'R', checked: test.config.threshold === 'R' }" v-model="test.config.threshold"
                                   labelMessageKey="perfTest.config.runCount" name="threshold"
                                   :data-step="shownBsTab ? 10 : undefined"
                                   :data-intro="shownBsTab ? i18n('intro.config.basic.runcount') : undefined">
                        <input-append name="runCount" ref="runCount"
                                      type="number" v-model="test.config.runCount"
                                      appendPrefix="perfTest.config.max"
                                      @focus="test.config.threshold = 'R'"
                                      message="perfTest.config.runCount"
                                      :append="config.maxRunCount"
                                      :validationRules="{ required: true, max_value: config.maxRunCount, min_value: test.config.threshold === 'R' ? 1 : 0 }">
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
                            <select class="select-item form-control" name="samplingInterval" v-model="test.config.samplingInterval">
                                <option v-for="interval in samplingIntervals" :value="interval" v-text="interval"></option>
                            </select>
                        </control-group>
                        <control-group :class="{ error: errors.has('ignoreSampleCount') }" name="ignoreSampleCount"
                                       labelMessageKey="perfTest.config.ignoreSampleCount">
                            <input-popover v-model="test.config.ignoreSampleCount"
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
                                       labelHelpMessageKey="perfTest.config.safeDistribution" controlsStyle="padding-top: 6px;">
                            <input type="checkbox" id="safeDistribution" name="safeDistribution" v-model="test.config.safeDistribution">
                        </control-group>
                        <control-group :class="{error: errors.has('param')}"
                                       name="param" labelMessageKey="perfTest.config.param"
                                       controlsStyle="margin-left: 85px;" labelStyle="width: 70px;">
                            <input-popover name="param"
                                           ref="param"
                                           dataPlacement="top"
                                           v-model="test.config.param"
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
            <ramp-up ref="rampUp" :rampUp="test.rampUp" :rampUpTypes="config.rampUpTypes"
                     :processes="test.config.processes" :threads="test.config.threads"></ramp-up>
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
        test;

        @Prop({ type: Object, required: true })
        scriptsMap;

        @Prop({ type: Object, required: true })
        config;

        scripts = [];
        resources = [];

        samplingIntervals = [1, 2, 3, 4, 5, 10, 30, 60];
        regionAgentCountMap = {};

        targetHostIp = '';
        targetHosts = [];

        maxAgentCount = 0;
        shownBsTab = false;

        display = {
            vuserPanel: false,
            detailConfig: false,
            showRevisionBtn: false,
            showGitHubRefreshBtn: false,
        };

        durationMaxHour = 0;
        duration = {
            hour: 0,
            min: 0,
            sec: 0,
        };

        agentCountValidationRules = { required: true, agentCountValidation: true, min_value: 0 };

        gitHubLazyLoading = true;

        created() {
            this.setScripts(this.test.config.scriptName);
            this.setCustomValidationRules();
            this.setDurationMS();
            this.changeDuration();
            this.setTargetHosts(this.test.config.targetHosts);
            this.getScriptResource();
        }

        mounted() {
            if (!this.ngrinder.config.clustered) {
                this.test.config.region = 'NONE';
            }

            this.changeMaxAgentCount();

            const durationHour = parseInt(this.test.config.duration / 3600000) + 1;
            this.durationMaxHour = (durationHour > this.config.maxRunHour) ? durationHour : this.config.maxRunHour;

            this.$nextTick(() => {
                $('[data-toggle="popover"]').popover();
            });
        }

        changeScriptStorage() {
            if (this.test.config.scm === 'addGitHub') {
                this.createGitConfig();
            } else {
                this.display.showGitHubRefreshBtn = this.isGitHubStorage();
                this.scripts = this.scriptsMap[this.test.config.scm];
                this.test.config.scriptName = '';
                this.$nextTick(() => this.$refs.scriptSelect.selectValue(''));
            }
        }

        createGitConfig() {
            this.$http.post('/script/api/github-config')
                .then(() => {
                    this.$bootbox.confirm({
                        message: this.i18n('script.message.script.github.config'),
                        buttons: {
                            confirm: { label: this.i18n('common.button.ok') },
                            cancel: { label: this.i18n('common.button.cancel') },
                        },
                        onConfirm: () => this.$router.push('/script/detail/.gitconfig.yml'),
                        onCancel: () => {
                            this.config.github = [];
                            this.$refs.scmSelect.selectValue('svn');
                        },
                    });
                });
        }

        @Watch('test.config.region')
        changeMaxAgentCount() {
            if (this.test.config.region && this.config.regionAgentCountMap[this.test.config.region]) {
                this.maxAgentCount = this.config.regionAgentCountMap[this.test.config.region];
            } else {
                this.maxAgentCount = 0;
            }
            this.$validator.validate('agentCount');
        }

        setScripts(selectedScript) {
            if (this.isGitHubStorage()) {
                this.display.showGitHubRefreshBtn = true;
                this.scripts.push({ pathInShort: this.extractScriptName(selectedScript), path: selectedScript, validated: 0 });
            } else {
                this.scripts = this.scriptsMap.svn;
                if (!selectedScript) {
                    return;
                }

                if (!this.scripts.some(script => script.path === selectedScript)) {
                    this.scripts.push({ pathInShort: `(deleted) ${selectedScript}`, path: selectedScript, validated: -1 });
                } else {
                    this.display.showRevisionBtn = true;
                }
            }

            this.$nextTick(() => {
                this.$refs.scriptSelect.selectValue(this.test.config.scriptName);
                this.$validator.validate('scriptName');
            });
        }

        LoadGitHubScript(refresh) {
            if (!this.config.github) {
                return;
            }

            this.showProgressBar();
            this.$http.get(`/script/api/github?refresh=${!!refresh}`)
                .then(res => {
                    for (const key in res.data) {
                        this.scriptsMap[key] = res.data[key].map(script => ({
                            revision: script.sha,
                            validated: 0,
                            pathInShort: this.extractScriptName(script.path),
                            path: script.path,
                        }));
                    }

                    this.scripts = this.scriptsMap[this.test.config.scm];
                    if (this.gitHubLazyLoading) {
                        this.gitHubLazyLoading = false;
                        this.$nextTick(() => this.$refs.scriptSelect.refreshDropDown());
                    }

                    if (refresh) {
                        this.showSuccessMsg(this.i18n('script.message.refresh.success'));
                        this.$nextTick(() => this.$refs.scriptSelect.refreshDropDown());
                    }
                })
                .catch(() => this.showErrorMsg(this.i18n('script.message.refresh.error')))
                .finally(() => this.hideProgressBar());
        }

        getScriptResource() {
            if (!this.test.config.scriptName) {
                return;
            }

            this.$http.get('/perftest/api/resource', {
                params: {
                    scriptPath: this.test.config.scriptName,
                },
            })
            .then(res => this.resources = res.data.resources)
            .catch(() => this.showErrorMsg(this.i18n('perfTest.config.scriptResources')));
        }

        showScript() {
            let showScriptUrl = `${this.contextPath}/script/detail/${this.test.config.scriptName}?r=${this.test.config.scriptRevision}`;
            if (this.isAdmin || this.isSuperUser) {
                showScriptUrl += `&ownerId=${this.test.createdUser.userId}`;
            }
            const openedWindow = window.open(showScriptUrl, 'scriptSource');
            openedWindow.focus();
        }

        openingScriptSelect() {
            if (this.gitHubLazyLoading && this.isGitHubStorage()) {
                this.LoadGitHubScript();
            }
        }

        changeScript(revision) {
            if (this.isGitHubStorage()) {
                this.test.config.scriptRevision = revision;
                return;
            }

            this.test.config.scriptRevision = -1;
            if (this.$refs.scriptSelect.getSelectedOption('validate') !== '-1') {
                this.refreshTargetHosts();
                this.getScriptResource();
                this.display.showRevisionBtn = true;
            } else {
                this.targetHosts = [];
                this.resources = [];
                this.display.showRevisionBtn = false;
            }
        }

        refreshTargetHosts() {
            this.$http.get(`/script/api/detail/${this.test.config.scriptName}?r=${this.test.config.scriptRevision}`)
                .then(res => {
                    if (res.data.file && res.data.file.properties.targetHosts) {
                        this.test.config.targetHosts = res.data.file.properties.targetHosts;
                    } else {
                        this.test.config.targetHosts = '';
                    }
                    this.setTargetHosts(this.test.config.targetHosts);
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
                        return this.$refs.scriptSelect.getSelectedOption('validate') !== '-1';
                    }
                    return true;
                },
            });

            this.$validator.extend('regionValidation', {
                getMessage: () => this.i18n('perfTest.message.region'),
                validate: () => !this.ngrinder.config.clustered || (this.ngrinder.config.clustered && this.test.config.region !== 'NONE'),
            });
        }

        setDurationMS() {
            const duration = this.$moment.duration(this.test.config.duration);

            this.duration.hour = duration.hours() + (duration.days() * 24);
            this.duration.min = duration.minutes();
            this.duration.sec = duration.seconds();
        }

        @Watch('test.threshold')
        watchTestThreshold() {
            if (this.$refs.runCount && this.test.config.threshold === 'D') {
                if (this.$refs.runCount.errors.has('runCount')) {
                    this.test.config.runCount = 0;
                    this.$refs.runCount.errors.clear();
                    this.$refs.runCountControlGroup.hasError = false;
                }
            } else {
                this.$refs.runCount.focus();
            }
        }

        @Watch('test.config.processes')
        @Watch('test.config.threads')
        onProcessesAndThreadsChanged() {
            this.test.config.processes = this.test.config.processes || 0;
            this.test.config.threads = this.test.config.threads || 0;
        }

        changeVuserPerAgent() {
            this.test.config.processes = getProcessCount(this.test.config.vuserPerAgent);
            this.test.config.threads = getThreadCount(this.test.config.vuserPerAgent);
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
            this.test.config.vuserPerAgent = this.test.config.processes * this.test.config.threads;
        }

        changeDuration(options) {
            if (options && options.focus) {
                this.test.config.threshold = 'D';
            }
            this.test.config.duration = (this.duration.hour * 3600 + this.duration.min * 60 + this.duration.sec) * 1000;
            if (options && options.updateSlider) {
                this.$refs.durationSlider.setDurationMs(this.test.config.duration);
            }
        }

        changeDurationSlider(durationSec) {
            this.test.config.duration = durationSec * 1000;
            this.setDurationMS();
            this.changeDuration({ focus: true });
        }

        // TODO: Change targetHosts to array. Not the comma separated string.
        addHost(newHost) {
            if (this.targetHosts.some(host => host === newHost)) {
                return;
            }
            this.targetHosts.push(newHost);
            this.test.config.targetHosts = this.targetHosts.join(',');
        }

        removeHost(host, index) {
            this.targetHosts.splice(index, 1);
            this.test.config.targetHosts = this.targetHosts.join(',');
        }

        setTargetHosts(hosts) {
            this.targetHosts.splice(0, this.targetHosts.length);

            if (hosts) {
                hosts.split(',').forEach(host => this.targetHosts.push(host));
            }
        }

        showTargetHostInfoModal(host) {
            const hostToken = host.split(':');
            this.targetHostIp = hostToken[1] ? hostToken[1] : hostToken[0];
            this.$refs.targetHostInfoModal.show();
        }

        extractScriptName(path) {
            const pathToken = path.split('/');
            return pathToken[pathToken.length - 1];
        }

        isGitHubStorage() {
            return this.test.config.scm !== 'svn';
        }

        get totalVuser() {
            return this.test.config.agentCount * this.test.config.vuserPerAgent;
        }

        get currentRegion() {
            if (this.ngrinder.config.clustered && this.test.config.region === 'NONE') {
                return this.i18n('perfTest.config.region.setting');
            }
            return this.i18n(this.test.config.region);
        }

        get showRevisonBtn() {
            return this.display.showRevisionBtn && !this.display.showGitHubRefreshBtn && this.test.config.scriptName;
        }
    }
</script>

<style lang="less">
    @gray: #6c757d;

    .config-container {
        .advanced-config {
            margin-top: 10px;

            .row {
                height: 48px;
                width: 650px;

                .control-group {
                    width: 240px;
                }

                .control-group + .control-group {
                    margin-left: 60px;
                }
            }
        }

        .agent-region-container {
            .control-label {
                float: none;
                width: fit-content;
            }

            .controls {
                display: inline-block;
                vertical-align: top;
                margin-left: 5px;
            }
        }

        .input-append {
            input {
                @media screen and (-ms-high-contrast: active), (-ms-high-contrast: none) {
                    max-width: 74px;
                }
            }
        }

        .dropdown-toggle:after {
            display: none;
        }

        .dropdown-menu {
            border-color: #ced4da;;
            min-width: 100px;

            .dropdown-item {
                padding: 0.25rem 0.75rem;

                &:active {
                    background-color: #e9ecef;
                }
            }

            &.show {
                top: -2px !important;
                border-top-left-radius: unset;
                border-top-right-radius: unset;
            }
        }
    }

    ul.select2-results {
        li.add-github {
            color: red;

            &:hover {
                color: white;
            }
        }
    }
</style>

<style lang="less" scoped>
    @gray: #6c757d;
    @error-color: #d9534f;

    .config-container {
        .basic-config-container {
            width: 650px;

            .form-horizontal {
                margin-top: 10px;

                hr {
                    margin: 15px 0;
                }
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
                    max-width: 380px;
                    height: 55px;

                    .input-group-append, .input-group-prepend {
                        height: 30px;
                    }

                    .agent-count-input {
                        width: 74px;
                        height: 30px;
                        padding-left: 8px;
                    }

                    .show {
                        .select-region-btn {
                            color: #495057;
                        }
                    }

                    .select-region-btn {
                        display: flex;
                        flex-direction: row;
                        justify-content: space-between;
                        width: 100px;
                        height: 30px;

                        color: #495057;
                        border-color: #ced4da;

                        .region-popover-wrapper {
                            width: 100%;
                            padding: 0.375rem 0.75rem;

                            > span {
                                padding: 0 2px;
                                display: block;
                                overflow: hidden;
                                white-space: nowrap;
                                text-overflow: ellipsis;
                            }

                            > i {
                                line-height: 18px;
                            }
                        }
                    }

                    .error {
                        .select-region-btn {
                            border-color: @error-color;
                            color: @error-color;
                        }

                        .show > .select-region-btn.dropdown-toggle {
                            border-color: @error-color;
                            color: @error-color;

                            &:focus {
                                outline: 0;
                                box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
                            }
                        }
                    }

                    .validation-message {
                        position: absolute;
                        white-space: nowrap;
                    }
                }

                .agent-region-container {
                    width: 220px;
                    max-width: 220px;
                }
            }

            .vuser-per-agent-container {
                height: 44px;

                .vuser-panel {
                    margin-left: 15px;
                    padding-left: 15px;
                    height: 30px;

                    div + div {
                        margin-left: 5px;
                    }

                    .input-prepend-container {
                        width: 120px;
                    }
                }
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

        .btn-script-revision, .btn-github-refresh {
            position: relative;
            width: 82px;

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
                height: 60px;
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
                height: 70px;
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
            margin-top: 50px;
            margin-left: 463px;
            position: absolute;

            i {
                vertical-align: initial;
            }
        }

        .control-group {
            vertical-align: baseline;
            margin-bottom: 15px;

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
