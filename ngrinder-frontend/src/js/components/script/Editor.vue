<template>
    <div class="container" ref="container">
        <div id="top" class="well" ref="top">
            <div class="form-horizontal">
                <div class="flex-box control-group">
                    <div>
                        <label class="control-label" v-text="i18n('script.info.name')"></label>
                    </div>
                    <div>
                        <span class="input-large uneditable-input span6">
                            <template v-if="basePath !== ''"
                                      v-for="(each, index) in basePath.split('/')"><!--eslint-disable-next-line vue/valid-v-for--><!--
                                --><router-link :to="breadcrumbPathUrl.slice(0, index + 2).join('/')"
                                                v-text="each"></router-link><!--
                                -->/<!--
                            --></template><span v-text="file.fileName"></span>
                        </span>
                    </div>
                    <div>
                        <template v-if="scriptHandler && scriptHandler.validatable">
                            <a class="pointer-cursor btn btn-success" @click="save"
                               v-text="i18n('common.button.save')"></a>
                            <a class="pointer-cursor btn btn-primary" @click="validate"
                               v-text="i18n('script.editor.button.validate')"></a>
                        </template>
                        <a v-else class="pointer-cursor btn btn-success" @click="save"
                           v-text="i18n('common.button.save')"></a>
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
                        <a class="btn pull-right btn-mini add-host-btn"
                           @click.prevent="$refs.addHostModal.show" v-text="i18n('perfTest.config.add')">
                        </a>
                        <div class="div-host" rel="popover"
                             id="host-div"
                             :title="i18n('perfTest.config.targetHost')"
                             :data-content="i18n('perfTest.config.targetHost.help')"
                             data-html="true"
                             data-placement="bottom">
                            <span v-for="(host, index) in targetHosts">
                                <p class="host">
                                    <a class="pointer-cursor" @click="showTargetHostInfoModal(host)" v-text="host"></a>
                                    <a class="pointer-cursor"><i class="icon-remove-circle"
                                                                 @click="targetHosts.splice(index, 1)"></i></a>
                                </p>
                                <br>
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <code-mirror ref="editor"
                     :value="this.file.content"
                     :options="cmOptions"></code-mirror>
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
        <host-modal ref="addHostModal" @add-host="addHost"></host-modal>
        <target-host-info-modal ref="targetHostInfoModal" :ip="targetHostIp"></target-host-info-modal>
    </div>
</template>

<script>
    import { Component, Watch } from 'vue-property-decorator';
    import { Mixins } from 'vue-mixin-decorator';
    import querystring from 'querystring';
    import Base from '../Base.vue';
    import ControlGroup from '../common/ControlGroup.vue';
    import HostModal from '../perftest/modal/HostModal.vue';
    import TargetHostInfoModal from '../perftest/modal/TargetHostInfoModal.vue';
    import CodeMirror from '../common/CodeMirror.vue';
    import MessagesMixin from '../common/mixin/MessagesMixin.vue';

    Component.registerHooks(['beforeRouteLeave']);
    @Component({
        name: 'scriptEditor',
        components: { HostModal, TargetHostInfoModal, ControlGroup, CodeMirror },
    })
    export default class Editor extends Mixins(Base, MessagesMixin) {
        file = {
            fileName: '',
            description: '',
            content: '',
            validated: false,
        };
        scriptHandler = {};

        createLibAndResource = false;

        targetHosts = [];
        targetHostIp = '';

        editorSize = 0;

        validated = false;
        validating = false;
        validationResultExpanded = false;
        validationResult = '';

        saved = false;

        cmOptions = {};

        mounted() {
            this.initScriptDetail();

            $('#host-div').popover({ trigger: 'hover' });
            $('#tip').popover({ trigger: 'hover' });
        }

        get basePath() {
            const getBaseDirectory = s => s.substring(0, s.lastIndexOf('/'));

            return getBaseDirectory(this.$route.path.replace('/script/detail/', ''));
        }

        get breadcrumbPathUrl() {
            return ['/script/list', ...this.basePath.split('/')];
        }

        initScriptDetail() {
            const path = this.$route.path.replace('/script/detail/', '');
            this.$http.get(`/script/api/detail/${path}?r=${this.$route.query.r ? this.$route.query.r : -1}`)
                .then(res => {
                    if (!res.data.file) {
                        this.$router.push({ path: '/script/' });
                    }

                    Object.assign(this.file, res.data.file);
                    Object.assign(this.scriptHandler, res.data.scriptHandler);
                    this.scriptHandler.codemirrorKey = res.data.codemirrorKey;

                    if (this.file.properties.targetHosts) {
                        this.targetHosts = this.file.properties.targetHosts.split(',').filter(s => s);
                    }

                    this.validated = this.file.validated;

                    this.initCodeMirror();
                });
        }

        initCodeMirror() {
            this.cmOptions = { mode: this.scriptHandler.codemirrorKey };
            this.editorSize = 500;
            this.$nextTick(() => this.$refs.editor.codemirror.clearHistory());
        }

        save() {
            const newContent = this.$refs.editor.getValue();
            if (this.file.content !== newContent) {
                this.validated = false;
            }

            if (this.file.revision > 0 && this.file.lastRevision > 0 && this.file.revision < this.file.lastRevision) {
                bootbox.confirm(
                    this.i18n('script.editor.message.overWriteNewer'),
                    this.i18n('common.button.cancel'),
                    this.i18n('common.button.ok'),
                    result => {
                        if (result) {
                            this.saveScript();
                        }
                    });
            } else {
                this.saveScript();
            }
        }

        saveScript() {
            const params = querystring.stringify({
                path: this.file.path,
                description: this.file.description ? this.file.description : '',
                content: this.$refs.editor.getValue(),
                validated: this.validated,
                createLibAndResource: this.createLibAndResource,
                targetHosts: this.targetHosts.join(','),
            });

            this.$http.post('/script/api/save', params)
            .then(res => {
                this.saved = true;
                this.$router.push(`/script/list/${res.data}`);
            })
            .catch(() => this.showErrorMsg(this.i18n('script.message.save.error')));
        }

        validate() {
            if (this.validating) {
                return;
            }
            this.validating = true;
            this.validationResult = '';

            this.showProgressBar(this.i18n('script.editor.message.validate'));

            const params = querystring.stringify({
                path: this.file.path,
                content: this.$refs.editor.getValue(),
                hostString: this.targetHosts.join(','),
            });

            this.$http.post('/script/api/validate', params)
            .then(res => {
                this.validationResult = res.data;
                this.validated = true;
            })
            .catch(() => this.showErrorMsg(this.i18n('script.editor.error.validate')))
            .finally(() => {
                this.hideProgressBar();
                this.validating = false;
            });
        }

        addHost(newHost) {
            if (this.targetHosts.some(host => host === newHost)) {
                return;
            }
            this.targetHost.push(newHost);
        }

        showTargetHostInfoModal(host) {
            const hostToken = host.split(':');
            this.targetHostIp = hostToken[1] ? hostToken[1] : hostToken[0];
            this.$refs.targetHostInfoModal.show();
        }

        @Watch('editorSize')
        editorSizeChanged(newValue) {
            if (!newValue) {
                return;
            }
            this.$refs.editor.setSize(null, newValue);
        }

        expand() {
            this.validationResultExpanded = !this.validationResultExpanded;
            if (this.validationResultExpanded) {
                this.editorSize -= 200;
            } else {
                this.editorSize += 200;
            }
        }

        changed() {
            return this.$refs.editor.getValue() !== this.file.content;
        }

        beforeRouteLeave(to, from, next) {
            if (!this.saved && this.changed()) {
                bootbox.confirm(
                    this.i18n('script.editor.message.exitWithoutSave'),
                    this.i18n('common.button.cancel'),
                    this.i18n('common.button.ok'),
                    result => (result ? (window.onbeforeunload = null & next()) : next(false)),
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
                return null;
            }
            return this.i18n('script.editor.message.exitWithoutSave');
        }
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
