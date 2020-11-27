<template>
    <div ref="editorContainer" class="container d-flex flex-column overflow-y-auto h-100">
        <vue-headful :title="i18n('script.editor.title')"/>
        <i v-show="isFullScreen()" class="fa fa-compress pointer-cursor" @click="fullScreen"></i>
        <div class="file-desc-container flex-grow-0">
            <div class="form-horizontal">
                <button class="btn-return-to-list btn-primary border-0 position-absolute" @click="returnToList" v-text="i18n('common.list')"></button>
                <div class="caret-box pointer-cursor" @click="toggleHideDescription">
                    <i class="fa" :class="{ 'fa-caret-up' : !hideDescription, 'fa-caret-down' : hideDescription }"></i>
                </div>
                <div class="control-group" :class="{ 'mb-2' : !hideDescription }">
                    <div>
                        <label class="control-label" v-text="i18n('script.info.name')"></label>
                        <span class="d-inline-block border rounded form-control uneditable-input">
                            <span v-if="basePath !== ''">
                                <template v-for="(each, index) in basePath.split('/')">
                                    <router-link :key="each" :to="breadcrumbPathUrl.slice(0, index + 2).join('/')" v-text="each"></router-link>/<!--
                             --></template>
                            </span><!--
                         --><span v-text="file.fileName"></span>
                        </span>
                    </div>
                    <div>
                        <template v-if="isTestScript || isGitConfig">
                            <button v-shortkey="['ctrl', 'shift', 's']" class="btn btn-success"
                                    @shortkey="save(false)"
                                    @click="save(false)">
                                <i class="fa fa-save mr-1"></i>
                                <span v-text="i18n('common.button.save')"></span>
                            </button>
                            <button class="btn btn-success" @click="save(true)">
                                <i class="fa fa-undo mr-1"></i>
                                <span v-text="i18n('common.button.save.and.close')"></span>
                            </button>
                            <button v-shortkey="['ctrl', 'shift', 'v']" class="btn btn-primary"
                                    @shortkey="validate"
                                    @click="validate">
                                <i class="fa fa-check mr-1"></i>
                                <span v-text="i18n('script.editor.button.validate')"></span>
                            </button>
                        </template>
                        <template v-else>
                            <button v-shortkey="['ctrl', 'shift', 's']" class="btn btn-success"
                                    @shortkey="save(false)"
                                    @click="save(false)">
                                <i class="fa fa-save mr-1"></i>
                                <span v-text="i18n('common.button.save')"></span>
                            </button>
                            <button class="btn btn-success" @click="save(true)">
                                <i class="fa fa-undo mr-1"></i>
                                <span v-text="i18n('common.button.save.and.close')"></span>
                            </button>
                        </template>
                    </div>
                </div>
                <div class="control-group description-container" v-show="!hideDescription">
                    <div>
                        <label class="control-label" for="description" v-text="i18n('script.action.commit')"></label>
                        <textarea class="form-control" id="description"
                                  name="description" v-model="file.description">
                        </textarea>
                    </div>
                    <div class="div-host"
                         :title="i18n('perfTest.config.targetHost')"
                         :data-content="i18n('perfTest.config.targetHost.help')"
                         data-toggle="popover"
                         data-trigger="hover"
                         data-placement="bottom">
                        <button v-shortkey="['ctrl', 'shift', 'a']"
                                @shortkey="showAddHostModal"
                                @click.prevent="showAddHostModal"
                                class="btn btn-info float-right add-host-btn">
                            <i class="fa fa-plus"></i>
                            <span v-text="i18n('common.button.add')"></span>
                        </button>
                        <div v-for="(host, index) in targetHosts" class="host">
                            <a href="#" @click="showTargetHostInfoModal(host)" v-text="host"></a>
                            <i class="fa fa-times-circle pointer-cursor" @click="targetHosts.splice(index, 1)"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <splitpanes class="flex-grow-1 overflow-y-auto default-theme h-100"
                    @resize="editorSize = $event[0].size"
                    horizontal>
            <pane :min-size="15" :size="editorSize">
                <div class="h-100 position-relative">
                    <i v-show="!isFullScreen()" class="fa fa-expand pointer-cursor" @click="fullScreen"></i>
                    <code-mirror ref="editor"
                                 class="h-100"
                                 :value="file.content"
                                 :options="cmOptions">
                    </code-mirror>
                </div>
            </pane>
            <pane v-if="validationResult" :min-size="15" :size="100 - editorSize">
                <vue-scroll class="border validation-result-container">
                    <pre class="h-100 validation-result" v-html="validationResult"></pre>
                </vue-scroll>
            </pane>
        </splitpanes>
        <div v-if="validationResult" class="script-samples-link">
            <a target="_blank" href="https://github.com/naver/ngrinder/tree/master/script-sample">Script Samples</a>
        </div>
        <host-modal ref="addHostModal" @add-host="addHost" focus="domain"></host-modal>
        <target-host-info-modal ref="targetHostInfoModal" :ip="targetHostIp"></target-host-info-modal>
    </div>
</template>

<script>
    import { Component, Prop } from 'vue-property-decorator';
    import { Mixins } from 'vue-mixin-decorator';
    import { Splitpanes, Pane } from 'splitpanes';
    import VueHeadful from 'vue-headful';
    import YAML from 'js-yaml';

    import Base from '../Base.vue';
    import ControlGroup from '../common/ControlGroup.vue';
    import HostModal from '../perftest/modal/HostModal.vue';
    import TargetHostInfoModal from '../perftest/modal/TargetHostInfoModal.vue';
    import CodeMirror from '../common/CodeMirror.vue';
    import MessagesMixin from '../common/mixin/MessagesMixin.vue';
    import GuideMixin from './mixin/Guide.vue';
    import { TipType } from '../../constants';

    const GIT_CONFIG_FILE_NAME = '.gitconfig.yml';

    Component.registerHooks(['beforeRouteEnter', 'beforeRouteLeave']);
    @Component({
        name: 'scriptEditor',
        components: { HostModal, TargetHostInfoModal, ControlGroup, CodeMirror, Splitpanes, Pane, VueHeadful },
    })
    export default class Editor extends Mixins(Base, MessagesMixin, GuideMixin) {
        @Prop({ type: Object, required: true })
        file;

        @Prop({ type: Object, required: true })
        scriptHandler;

        @Prop({ type: String, required: true })
        codemirrorKey;

        @Prop({ type: String, required: true })
        remainedPath;

        createLibAndResource = false;

        targetHosts = [];
        targetHostIp = '';

        validated = false;
        validationResult = '';

        cmOptions = {};

        SCRIPT_DESCRIPTION_HIDE_KEY = 'script_description_hide';
        hideDescription = false;

        editorSize = 70;

        beforeRouteEnter(to, from, next) {
            const path = to.params.remainedPath;
            const revision = to.query.r || -1;
            const ownerId = to.query.ownerId;

            let apiUrl = `/script/api/detail/${path}?r=${revision}`;
            if (ownerId) {
                apiUrl += `&ownerId=${ownerId}`;
            }

            Base.prototype.$http.get(apiUrl)
                .then(res => res.data.file ? res : Promise.reject()) // eslint-disable-line no-confusing-arrow
                .then(res => Object.assign(to.params, res.data))
                .then(next)
                .catch(() => next('/script'));
        }

        created() {
            this.hideDescription = this.$localStorage.get(this.SCRIPT_DESCRIPTION_HIDE_KEY, false, Boolean);
        }

        fullScreen() {
            this.$refs.editor.codemirror.setOption('fullScreen', !this.isFullScreen());
        }

        mounted() {
            this.$store.commit('activeTip', TipType.EDITOR_SHORTCUT);
            this.setConfirmBeforeLeave();
            this.init();

            $('[data-toggle="popover"]').popover();

            this.$nextTick(() => {
                this.$refs.editor.codemirror.focus();

                switch (true) {
                    case this.isGitConfig:
                        this.validationResult = this.guides.gitconfig;
                        break;
                    case /\\*.groovy/.test(this.file.fileName):
                    case /\\*.py/.test(this.file.fileName):
                        this.validationResult = this.guides.perftest;
                        break;
                    default:
                        this.validationResult = '';
                }

                $(this.$refs.editorContainer).on('click', 'a.validation-error-link', event => {
                    event.preventDefault();
                    event.stopPropagation();
                    this.$refs.editor.codemirror.focus();
                    this.$refs.editor.codemirror.setCursor({
                        line: event.target.dataset.errorLine - 1,
                        ch: 0,
                    });
                });

            });
        }

        beforeDestroy() {
            this.$store.commit('activeTip', '');
        }

        beforeRouteLeave(to, from, next) {
            if (this.contentChanged()) {
                this.$bootbox.confirm({
                    message: this.i18n('script.editor.message.exitWithoutSave'),
                    buttons: {
                        confirm: { label: this.i18n('common.button.ok') },
                        cancel: { label: this.i18n('common.button.cancel') },
                    },
                    onConfirm: () => window.onbeforeunload = null & next(),
                    onCancel: () => next(false),
                });
            } else {
                window.onbeforeunload = null;
                next();
            }
        }

        init() {
            if (this.file.properties.targetHosts) {
                this.targetHosts = this.file.properties.targetHosts.split(',').filter(s => s);
            }
            this.validated = this.file.validated;
            this.cmOptions = { mode: this.codemirrorKey };
            this.$nextTick(() => this.$refs.editor.codemirror.clearHistory());
        }

        setConfirmBeforeLeave() {
            window.onbeforeunload = () => {
                if (this.contentChanged()) {
                    return this.i18n('script.editor.message.exitWithoutSave');
                }
                return null;
            };
        }

        contentChanged() {
            return !this.$utils.equalsIgnoreNewLineChar(this.$refs.editor.getValue(), this.file.content);
        }

        save(isClose) {
            if (this.contentChanged()) {
                this.validated = false;
            }

            if (this.file.revision > 0 && this.file.lastRevision > 0 && this.file.revision < this.file.lastRevision) {
                this.$bootbox.confirm({
                    message: this.i18n('script.editor.message.overWriteNewer'),
                    buttons: {
                        confirm: { label: this.i18n('common.button.ok') },
                        cancel: { label: this.i18n('common.button.cancel') },
                    },
                    onConfirm: () => {
                        this.saveScript(isClose);
                    },
                });
            } else {
                this.saveScript(isClose);
            }
        }

        saveScript(isClose) {
            const params = {
                fileEntry: {
                    path: this.file.path,
                    description: this.file.description ? this.file.description : '',
                    content: this.$refs.editor.getValue(),
                },
                validated: this.validated,
                createLibAndResource: this.createLibAndResource,
                targetHosts: this.targetHosts.join(','),
            };

            this.$http.post('/script/api/save', params)
            .then(() => {
                this.showSuccessMsg(this.i18n('common.message.alert.save.success'));
                this.file.content = this.$refs.editor.getValue();
                if (isClose) {
                    this.$router.push('/script/list/');
                }
            })
            .catch(() => this.showErrorMsg(this.i18n('script.message.save.error')));
        }

        validate() {
            // Initialize validation result without re-rendering.
            this.validationResult = ' ';
            if (this.isGitConfig) {
                this.validateGitConfig();
                return;
            }
            this.validateScript();
        }

        validateGitConfig() {
            const content = this.$refs.editor.getValue();
            try {
                const configs = YAML.loadAll(content);

                if (configs.filter(config => !!config).length === 0) {
                    this.$bootbox.alert({
                        message: this.i18n('script.message.empty.github.config'),
                        buttons: {
                            ok: { label: this.i18n('common.button.ok') },
                        },
                    });
                    return;
                }

                this.showProgressBar(this.i18n('script.editor.message.validate'));
                this.$http.post('/script/api/github/validate', { content })
                    .then(() => this.showScriptValidationResult(this.i18n('script.editor.validate.success')))
                    .catch(error => this.showScriptValidationResult(this.decorateGitHubConfigurationErrorMessage(error.response.data.message)))
                    .finally(this.hideProgressBar);
            } catch (error) {
                this.hideProgressBar();
                this.showScriptValidationResult(`YAML syntax error<br>${error.message}`);
            }
        }

        showScriptValidationResult(result) {
            this.editorSize = 60;
            this.validationResult = result;
        }

        decorateGitHubConfigurationErrorMessage(errorMsg) {
            return errorMsg
                    .replace('Not Found', "Not found GitHub repository.<br>Please check your 'owner' or 'repo' field is correct.")
                    .replace('Bad credentials', "Bad credentials<br>Please check your 'access-token' field is correct.");
        }

        validateScript() {
            this.showProgressBar(this.i18n('script.editor.message.validate'));
            this.$http.post('/script/api/validate', {
                fileEntry: {
                    path: this.file.path,
                    content: this.$refs.editor.getValue(),
                },
                hostString: this.targetHosts.join(','),
            }).then(res => {
                this.showScriptValidationResult(this.appendEditorLink(this.$htmlEntities.encode(res.data)));
                this.validated = true;
            }).catch(() => this.showErrorMsg(this.i18n('script.editor.validate.error')))
              .finally(this.hideProgressBar);
        }

        appendEditorLink(result) {
            const regex = new RegExp(`(^\\$\{NGRINDER_HOME\}.*)?${this.file.fileName}: ?[0-9]{1,4}`, 'gm');
            const linkableErrors = result.match(regex);
            if (linkableErrors) {
                const linkableError = linkableErrors[0];
                result = result.replace(regex, `<a href="#" class="validation-error-link" data-error-line="${this.extractErrorLine(linkableError)}">${linkableError}</a>`);
            }
            return result;
        }

        extractErrorLine(error) {
            const token = error.split(':');
            return token[token.length - 1].trim();
        }

        addHost(newHost) {
            if (this.targetHosts.some(host => host === newHost)) {
                return;
            }
            this.targetHosts.push(newHost);
        }

        showTargetHostInfoModal(host) {
            const hostToken = host.split(':');
            this.targetHostIp = hostToken[1] ? hostToken[1] : hostToken[0];
            this.$refs.targetHostInfoModal.show();
        }

        showAddHostModal() {
            this.$refs.addHostModal.show();
        }

        toggleHideDescription() {
            this.hideDescription = !this.hideDescription;
            this.$localStorage.set(this.SCRIPT_DESCRIPTION_HIDE_KEY, this.hideDescription);
        }

        returnToList() {
            this.$router.referer ? this.$router.back() : this.$router.push('/script/');
        }

        isFullScreen() {
            return this.$refs.editor && this.$refs.editor.codemirror.getOption('fullScreen');
        }

        get basePath() {
            return this.remainedPath.substring(0, this.remainedPath.lastIndexOf('/'));
        }

        get breadcrumbPathUrl() {
            return ['/script/list', ...this.basePath.split('/')];
        }

        get isGitConfig() {
            return this.file.path === GIT_CONFIG_FILE_NAME;
        }

        get isTestScript() {
            return this.scriptHandler && this.scriptHandler.validatable;
        }
    }
</script>

<style lang="less" scoped>
    @import "~splitpanes/dist/splitpanes.css";

    div.caret-box {
        position: absolute;
        left: 53px;
        padding: 5px;
        color: #495057
    }

    div.file-desc-container {
        padding: 10px 10px 10px 60px;
        margin-bottom: 0;
        background-color: #f9f9f9;
        position: relative;
        border: 1px solid rgba(0, 0, 0, 0.125);
        border-radius: 0.25rem;

        .control-group {
            display: flex;
            justify-content: space-between;
        }

        .control-label {
            width: 110px;
            margin: 4px 10px 0px -10px;
        }
    }

    .btn-return-to-list {
        left: -1px;
        height: 30px;
        width: 50px;
        border-radius: 0 3px 3px 0;
    }

    .description-container {
        height: 90px;
    }

    #description {
        resize: none;
        height: 90px;
        width: 762px;
    }

    .uneditable-input {
        cursor: text;
        width: 762px;
        height: 30px;
    }

    .tip {
        margin-top: -10px;
    }

    button:not(.add-host-btn) {
        height: 30px;
    }

    .add-host-btn {
        margin-top: 70px;
        margin-left: 194px;
        position: absolute;
        padding: 1px 4px;
        font-size: 10px;
    }

    .script-samples-link {
        display: inline-block;
        margin: -18px 3px 0 auto;
        z-index: 1;
    }

    .div-host {
            background-color: #FFF;
            border: 1px solid #D6D6D6;
            height: 90px;
            overflow-y: scroll;
            border-radius: 3px;
            width: 250px;
            margin-left: 0;
        }

    .host {
        color: #666;
        margin: 2px 0 2px 7px;
    }

    .validation-result-container {
        background-color: #f5f5f5;

        .validation-result {
            padding: 5px 5px 0 5px;
            margin-bottom: 0;
            font-size: 12px;
            background-color: inherit;
        }
    }

    .fa-expand {
        font-size: 14px;
        z-index: 100;
        position: absolute;
        right: 14px;
        top: 7px;
    }

    .fa-compress {
        font-size: 15px;
        position: absolute;
        z-index: 100;
        right: 15px;
        top: 42px;
    }

    input[type="text"] {
        width: 164px;
        height: 30px;
    }

    .splitpanes .splitpanes__pane {
        transition: none;   // Remove splitpanes initial transition
    }
</style>
