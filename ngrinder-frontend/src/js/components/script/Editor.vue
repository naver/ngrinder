<template>
    <div class="container d-flex flex-column overflow-y-auto">
        <vue-headful :title="i18n('script.editor.title')"/>
        <div class="file-desc-container flex-grow-0">
            <div class="form-horizontal">
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
                        <template v-if="scriptHandler && scriptHandler.validatable">
                            <button class="btn btn-success" @click="save(false)">
                                <i class="fa fa-save mr-1"></i>
                                <span v-text="i18n('common.button.save')"></span>
                            </button>
                            <button class="btn btn-success" @click="save(true)">
                                <i class="fa fa-undo mr-1"></i>
                                <span v-text="i18n('common.button.save.and.close')"></span>
                            </button>
                            <button class="btn btn-primary" @click="validate">
                                <i class="fa fa-check mr-1"></i>
                                <span v-text="i18n('script.editor.button.validate')"></span>
                            </button>
                        </template>
                        <template v-else>
                            <button class="btn btn-success" @click="save(false)">
                                <i class="fa fa-save mr-1"></i>
                                <span v-text="i18n('common.button.save')"></span>
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
                         data-html="true"
                         data-trigger="hover"
                         data-placement="bottom">
                        <button class="btn btn-info float-right add-host-btn" @click.prevent="$refs.addHostModal.show">
                            <i class="fa fa-plus"></i>
                            <span v-text="i18n('perfTest.config.add')"></span>
                        </button>
                        <div v-for="(host, index) in targetHosts" class="host">
                            <a href="#" @click="showTargetHostInfoModal(host)" v-text="host"></a>
                            <i class="fa fa-times-circle pointer-cursor" @click="targetHosts.splice(index, 1)"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <splitpanes class="flex-grow-1 overflow-y-auto default-theme" ref="splitPane" horizontal>
            <pane :min-size="10" size="85">
                <code-mirror ref="editor"
                             class="h-100"
                             :value="this.file.content"
                             :options="cmOptions">
                </code-mirror>
            </pane>
            <pane v-if="validationResult" :min-size="10" size="15">
                <pre class="border h-100 validation-result" v-text="validationResult"></pre>
            </pane>
        </splitpanes>
        <div class="script-samples-link" ref="sampleLink">
            <a target="_blank" href="https://github.com/naver/ngrinder/tree/master/script-sample">Script
                Samples</a>
            <div class="float-right pointer-cursor tip" data-toggle="popover" title="Tip" data-html="true"
                 data-placement="left" data-trigger="hover" :data-content="
                            'Ctrl-F / Cmd-F :' + i18n('script.editor.tip.startSearching') + '<br/>' +
                            'Ctrl-G / Cmd-G : ' + i18n('script.editor.tip.findNext') + '<br/>' +
                            'Shift-Ctrl-G / Shift-Cmd-G : ' + i18n('script.editor.tip.findPrev') + '<br/>' +
                            'Shift-Ctrl-F / Cmd-Option-F : ' + i18n('script.editor.tip.replace') + '<br/>' +
                            'Shift-Ctrl-R / Shift-Cmd-Option-F : ' + i18n('script.editor.tip.replaceAll') + '<br/>' +
                            'F11 : ' + i18n('script.editor.tip.fullScreen') + '<br/>' +
                            'ESC : ' + i18n('script.editor.tip.back') ">
                <code>Tip</code>
            </div>
        </div>
        <host-modal ref="addHostModal" @add-host="addHost"></host-modal>
        <target-host-info-modal ref="targetHostInfoModal" :ip="targetHostIp"></target-host-info-modal>
    </div>
</template>

<script>
    import { Component, Prop } from 'vue-property-decorator';
    import { Mixins } from 'vue-mixin-decorator';
    import { Splitpanes, Pane } from 'splitpanes';
    import VueHeadful from 'vue-headful';

    import Base from '../Base.vue';
    import ControlGroup from '../common/ControlGroup.vue';
    import HostModal from '../perftest/modal/HostModal.vue';
    import TargetHostInfoModal from '../perftest/modal/TargetHostInfoModal.vue';
    import CodeMirror from '../common/CodeMirror.vue';
    import MessagesMixin from '../common/mixin/MessagesMixin.vue';

    Component.registerHooks(['beforeRouteEnter', 'beforeRouteLeave']);
    @Component({
        name: 'scriptEditor',
        components: { HostModal, TargetHostInfoModal, ControlGroup, CodeMirror, Splitpanes, Pane, VueHeadful },
    })
    export default class Editor extends Mixins(Base, MessagesMixin) {
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
            this.hideDescription = this.$ls.get(this.SCRIPT_DESCRIPTION_HIDE_KEY, false, Boolean);
        }

        mounted() {
            this.setConfirmBeforeLeave();
            this.init();

            $('[data-toggle="popover"]').popover();

            this.$nextTick(() => {
                this.$refs.editor.codemirror.focus();
                this.validationResult = 'You can use various log levels. [trace, debug, info, warn, error]\n' +
                    'ex) grinder.logger.${level}("message")\n\n' + // eslint-disable-line no-template-curly-in-string
                    'You can access to response body with HTTPResponse.getText() method.\n' +
                    'ex) HTTPResponse result = request.GET("...")\n' +
                    '    grinder.logger.debug(result.text)\n\n' +
                    'You can test multiple transactions by recording new GTest instance.\n' +
                    'ex) @BeforeProcess\n' +
                    '    public static void beforeProcess() {\n' +
                    '        test1 = new GTest(1, "...")\n' +
                    '        test2 = new GTest(2, "...")\n' +
                    '    }\n\n' +
                    '    @BeforeThread\n' +
                    '    public void beforeThread() {\n' +
                    '        test1.record(this, "test1")\n' +
                    '        test2.record(this, "test2")\n' +
                    '    }\n\n' +
                    '    public void test1() { ... }\n' +
                    '    public void test2() { ... }\n\n' +
                    'You can specify the test run rate with @RunRate annotation.\n' +
                    'ex) import net.grinder.scriptengine.groovy.junit.annotation.RunRate\n\n' +
                    '    @Test\n' +
                    '    @RunRate(50)\n' +
                    '    public void test() { ... } // This test will run only half of the total run which you specified.\n\n';
            });
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
            this.targetHosts = this.file.properties.targetHosts.split(',').filter(s => s);
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
            return this.$refs.editor.getValue() !== this.file.content;
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
            this.showProgressBar(this.i18n('script.editor.message.validate'));

            const params = {
                fileEntry: {
                    path: this.file.path,
                    content: this.$refs.editor.getValue(),
                },
                hostString: this.targetHosts.join(','),
            };

            this.$http.post('/script/api/validate', params)
            .then(res => {
                this.validationResult = res.data;
                this.validated = true;
            })
            .catch(() => this.showErrorMsg(this.i18n('script.editor.error.validate')))
            .finally(() => {
                this.hideProgressBar();
            });
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

        toggleHideDescription() {
            this.hideDescription = !this.hideDescription;
            this.$ls.set(this.SCRIPT_DESCRIPTION_HIDE_KEY, this.hideDescription);
        }

        get basePath() {
            return this.remainedPath.substring(0, this.remainedPath.lastIndexOf('/'));
        }

        get breadcrumbPathUrl() {
            return ['/script/list', ...this.basePath.split('/')];
        }
    }
</script>

<style lang="less" scoped>
    @import "~splitpanes/dist/splitpanes.css";

    div.caret-box {
        position: absolute;
        left: 25px;
        padding: 5px;
        color: #495057
    }

    div.file-desc-container {
        padding: 10px 70px;
        margin-bottom: 0;
        background-color: #f9f9f9;
        position: relative;
        display: -ms-flexbox;
        display: flex;
        -ms-flex-direction: column;
        flex-direction: column;
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

    .description-container {
        height: 90px;
    }

    #description {
        resize: none;
        height: 100%;
        width: 690px;
    }

    .uneditable-input {
        cursor: text;
        width: 690px;
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
        margin-top: 10px;
        text-align: center;
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

    .validation-result {
        padding: 5px;
        font-size: 12px;
        background-color: #f5f5f5;
    }

    .expand-btn-container {
        position: absolute;
        left: 50%;

        width: 40px;
        height: 20px;
        margin-left: -20px;
        margin-top: 6px;
    }

    input[type="text"] {
        width: 164px;
        height: 30px;
    }
</style>
