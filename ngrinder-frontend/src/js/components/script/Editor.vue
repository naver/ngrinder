<template>
    <div class="container" ref="container">
        <div id="top" class="well" ref="top">
            <div class="form-horizontal">
                <div class="flex-box control-group">
                    <div>
                        <label class="control-label" v-text="i18n('script.info.name')"></label>
                    </div>
                    <div>
                        <span class="input-large uneditable-input span6" v-html="breadcrumbPath"></span>
                    </div>
                    <div>
                        <template v-if="scriptHandler && scriptHandler.validatable">
                            <a class="pointer-cursor btn btn-success" v-on:click="save"
                               v-text="i18n('common.button.save')"></a>
                            <a class="pointer-cursor btn btn-primary" v-on:click="validate"
                               v-text="i18n('script.editor.button.validate')"></a>
                        </template>
                        <a v-else class="pointer-cursor btn btn-success" v-text="i18n('common.button.save')"></a>
                    </div>
                </div>
                <div class="flex-box">
                    <div>
                        <label class="control-label" for="description" v-text="i18n('script.action.commit')"></label>
                    </div>
                    <div>
                        <textarea class="form-control" id="description" name="description"
                                  v-model="file.description"></textarea>
                    </div>
                    <div>
                        <a class="btn pull-right btn-mini add-host-btn" data-toggle="modal"
                           href="#add-host-modal" v-text="i18n('perfTest.config.add')">
                        </a>
                        <div class="div-host" rel="popover"
                             id="host-div"
                             :title="i18n('perfTest.config.targetHost')"
                             :data-content="i18n('perfTest.config.targetHost.help')"
                             data-html="true"
                             data-placement="bottom">
                            <p class="host" v-for="host in targetHosts">
                                <a href="#target_info_modal" data-toggle="modal"
                                   @click="showHostInfo(host)"
                                   v-text="host">
                                </a>
                                <a class="pointer-cursor">
                                    <i class="icon-remove-circle" @click="removeHost(host)"></i>
                                </a>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <textarea id="codemirror-content"></textarea>
        <div class="pull-right tip" id="tip" rel="popover" title="Tip" data-html="true" data-placement="left" :data-content="
            'Ctrl-F / Cmd-F :' + i18n('script.editor.tip.startSearching') + '<br/>' +
            'Ctrl-G / Cmd-G : ' + i18n('script.editor.tip.findNext') + '<br/>' +
            'Shift-Ctrl-G / Shift-Cmd-G : ' + i18n('script.editor.tip.findPrev') + '<br/>' +
            'Shift-Ctrl-F / Cmd-Option-F : ' + i18n('script.editor.tip.replace') + '<br/>' +
            'Shift-Ctrl-R / Shift-Cmd-Option-F : ' + i18n('script.editor.tip.replaceAll') + '<br/>' +
            'F12 : ' + i18n('script.editor.tip.fullScreen') + '<br/>' +
            'ESC : ' + i18n('script.editor.tip.back') ">
            <code class="tip">Tip</code>
        </div>
        <div class="script-samples-link" :class="{ hide : !validating && validated }" ref="sampleLink">
            <a target="_blank" href="https://github.com/naver/ngrinder/tree/master/script-sample">Script Samples</a>
        </div>
        <div class="validation-result-panel" :class="{ hide : !validationResult || (validating && !validated) }">
            <pre class="prettyprint pre-scrollable validation-result"
                 :class="{ expanded: validationResultExpanded }"
                 v-text="validationResult"></pre>
            <div class="pull-right expand-btn-container">
                <a class="pointer-cursor" id="expand-btn" v-on:click="expand">
                    <code v-text="validationResultExpanded ? '-' : '+'"></code>
                </a>
            </div>
        </div>
        <host-modal @add-host="addHost"></host-modal>
        <!-- TODO: Implement monitor -->
        <messages ref="messages"></messages>
    </div>
</template>

<script>
    import { Component, Watch } from 'vue-property-decorator';
    import Base from '../Base.vue';
    import ControlGroup from '../common/ControlGroup.vue';
    import HostModal from '../perftest/modal/HostModal.vue';
    import CodeMirror from '../../common/codemirror';
    import Messages from '../common/Messages.vue';

    import { isFullScreen, setFullScreen } from '../../../plugins/codemirror/util/fullscreen'

    Component.registerHooks(['beforeRouteLeave',]);

    @Component({
        name: 'scriptEditor',
        components: { HostModal, ControlGroup, Messages, },
    })
    export default class Editor extends Base {

        file = {
            description: '',
            validated: false,
        };
        breadcrumbPath = '';
        scriptHandler = {};

        createLibAndResource = false;

        // to use Set object reactively in vue
        targetHostsChangeTracker = 1;
        targetHostSet = new Set();

        editor = null;
        editorSize = 0;

        validated = false;
        validating = false;
        validationResultExpanded = false;
        validationResult = '';

        mounted() {
            this.initScriptDetail();

            $('#host-div').popover({trigger: 'hover'});
            $('#tip').popover({trigger: 'hover'});
        }

        initScriptDetail() {
            const path = this.$route.path.replace('/script/detail/', '');
            this.$http.get(`/script/api/detail/${path}?r=${this.$route.query.r ? this.$route.query.r : -1}`)
            .then(res => {
                Object.assign(this.file, res.data.file);
                Object.assign(this.breadcrumbPath, res.data.breadcrumbPath);
                Object.assign(this.scriptHandler, res.data.scriptHandler);

                this.validated = this.file.validated;

                this.initCodeMirror();
            })
        }

        initCodeMirror() {
            const editor = CodeMirror.fromTextArea(document.getElementById("codemirror-content"), {
                mode: this.scriptHandler.codemirrorKey,
                theme: "eclipse",
                lineNumbers: true,
                lineWrapping: true,
                indentUnit: 4,
                tabSize: 4,
                indentWithTabs: true,
                smartIndent: false,
                extraKeys: {
                    "F11": function (cm) {
                        setFullScreen(cm, !isFullScreen(cm));
                    },
                    "Esc": function (cm) {
                        if (isFullScreen(cm)) setFullScreen(cm, false);
                    },
                    Tab: "indentMore"
                },
                onCursorActivity: function () {
                    editor.setLineClass(hlLine, null, null);
                    hlLine = editor.setLineClass(editor.getCursor().line, null, "activeline");
                },
            });
            let hlLine = editor.setLineClass(0, "activeline");

            this.editor = editor;
            this.editor.setValue(this.file.content);
            this.initEditorSize();
        }

        initEditorSize() {
            this.editorSize = this.$refs.container.clientHeight - this.$refs.top.clientHeight - this.$refs.sampleLink.clientHeight;
        }

        save() {
            const newContent = this.editor.getValue();
            if (this.file.content !== newContent) {
                this.validated = false;
            }

            const formData = new FormData();
            formData.append('content', newContent);

            window.onbeforeunload = undefined;

            if (this.file.revision > 0 && this.file.lastRevision > 0 && this.file.revision < this.file.lastRevision) {
                bootbox.confirm(
                    this.i18n('script.editor.message.overWriteNewer'),
                    this.i18n('common.button.cancel'),
                    this.i18n('common.button.ok'),
                    result => result ? this.saveScript() : noop())
            } else {
                this.saveScript();
            }
        }

        saveScript() {
            const formData = new FormData();
            formData.append('path', this.file.path);
            formData.append('description', this.file.description ? this.file.description : '');
            formData.append('createLibAndResource', this.createLibAndResource);
            formData.append('validated', this.validated);
            formData.append('content', this.editor.getValue());
            formData.append('targetHosts', this.targetHosts.join(','));

            this.$http.post('/script/api/save', formData)
            .then(res => {
                window.onbeforeunload = null;
                this.$router.push(`/script/list/${res.data}`);
            })
            .catch(err => console.error(err))
        }

        validate() {
            if (this.validating) {
                return;
            }
            this.validating = true;
            this.validationResult = '';

            this.$refs.messages.showProgressBar(this.i18n('script.editor.message.validate'));

            const formData = formDataOf(
                'path', this.file.path,
                'content', this.editor.getValue(),
                'hostString', this.targetHosts.join(',')
            );
            if (this.isAdminOrSuperUser && this.ownerId) {
                formData.append('ownerId', this.ownerId);
            }

            this.$http.post('/script/api/validate', formData)
            .then(res => {
                this.validationResult = res.data;
                this.validated = true;
            })
            .catch(() => console.log(this.i18n('script.editor.error.validate')))
            .finally(() => {
                this.$refs.messages.hideProgressBar();
                this.validating = false;
            });
        }

        get targetHosts() {
            return this.targetHostsChangeTracker && Array.from(this.targetHostSet);
        }

        addHost(host) {
            console.log(host);
            this.targetHostSet.add(host);
            this.targetHostsChangeTracker += 1;
        }

        removeHost(host) {
            this.targetHostSet.delete(host);
            this.targetHostsChangeTracker += 1;
        }

        showHostInfo(host) {    // TODO: Implement monitor
            this.$http.get(`/monitor/api/info?ip=${$.trim(host)}`)
            .then(res => console.log(res))
            .catch(err => console.log(this.i18n("common.error.error")));
        }

        @Watch('editorSize')
        editorSizeChanged(newValue) {
            if (!newValue) {
                return;
            }
            this.editor.setSize(null, newValue);
        }

        expand() {
            this.validationResultExpanded = !this.validationResultExpanded;
            if (this.validationResultExpanded) {
                this.editorSize = this.editorSize - 200;
            } else {
                this.editorSize = this.editorSize + 200;
            }
        }

        changed() {
            if (!this.editor || !this.file) {
                return false;
            }

            return this.editor.getValue() !== this.file.content;
        }

        beforeRouteLeave(to, from, next) {
            if (this.changed()) {
                bootbox.confirm(
                    this.i18n('script.editor.message.exitWithoutSave'),
                    this.i18n('common.button.cancel'),
                    this.i18n('common.button.ok'),
                    result => result ? (window.onbeforeunload = null & next()) : next(false)
                );
            } else {
                window.onbeforeunload = null;
                next();
            }
        }

        beforeMount() {
            window.onbeforeunload = this.unload;
        }

        beforeDestroy() {
            window.onbeforeunload = null;
        }

        unload = () => {
            if (!this.changed()) {
                return;
            }

            return this.i18n('script.editor.message.exitWithoutSave');
        }
    }

    function formDataOf() {
        if (arguments.length % 2 !== 0) {
            console.error('Form data must be consist of key value pairs');
            return null;
        }

        const formData = new FormData();
        for (let i = 0; i < arguments.length; i += 2) {
            formData.append(arguments[i], arguments[i + 1]);
        }

        return formData;
    }

    function noop() {
    }
</script>

<style lang="less" scoped>
    #top {
        margin-bottom: 10px;
        margin-top: 0;
    }

    #description {
        resize: none;
        height: 65px;
        width: 460px;
    }

    .uneditable-input {
        cursor: text;
    }

    .tip {
        float: right;
        cursor: pointer;
        margin-top: -13px;
        margin-right: -16px;
    }

    .flex-box {
        display: flex;
        div {
            margin-left: 10px;
        }
    }

    .add-host-btn {
        margin-top: 38px;
        margin-left: 210px;
        position: absolute;
    }

    .script-samples-link {
        margin-top: 10px;
        text-align: center;
    }

    .btn-success {
        margin-left: 73px;
        width: 40px;
    }

    .btn-primary {
        width: 90px;
    }

    div.div-host {
        background-color: #FFFFFF;
        border: 1px solid #D6D6D6;
        height: 63px;
        overflow-y: scroll;
        border-radius: 3px 3px 3px 3px;
        width: 250px;
        margin-left: 0;

        .host {
            color: #666666;
            display: inline-block;
            margin-left: 7px;
            margin-top: 2px;
            margin-bottom: 2px;
        }
    }

    .validation-result {
        height: 100px;
        margin: 5px 0px 10px;
    }

    .expand-btn-container {
        margin-top: -30px;
        margin-right: -17px;
    }

    .expanded {
        height: 300px;
    }

    input[type="text"] {
        width: 164px;
        height: 30px;
    }
</style>

<style>
    @import "../../../plugins/codemirror/codemirror.css";
    @import "../../../plugins/codemirror/eclipse.css";
    @import "../../../plugins/codemirror/util/dialog.css";
</style>
