<template>
    <div class="modal fade" id="create-script-modal" ref="createScriptModal">
        <div class="modal-dialog">
            <div class="modal-content">
                <header class="modal-header">
                    <h4 v-text="i18n('script.action.createScript')"></h4>
                    <div id="script-sample"
                         title="Sample Script Link"
                         data-toggle="popover"
                         data-trigger="hover"
                         data-html="true"
                         data-placement="left"
                         class="ml-auto"
                         :data-content="i18n('script.editor.sample.message')">
                        <code>
                            <a target="_blank" href="https://github.com/naver/ngrinder/tree/master/script-sample">
                                Script Samples
                            </a>
                        </code>
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                    </div>
                </header>
                <div class="modal-body">
                    <div class="form-horizontal">
                        <control-group :class="{ error: errors.has('fileName') }" name="fileName" labelMessageKey="script.info.name">
                            <select name="scriptType" class="form-control ml-2" v-model="scriptHandler">
                                <option v-for="handler in handlers"
                                        :value="handler"
                                        v-text="handler.title">
                                </option>
                            </select>
                            <input type="text" id="fileName" name="fileName"
                                   class="ml-1 form-control" :title="i18n('script.info.name')"
                                   data-toggle="popover"
                                   data-trigger="focus"
                                   data-placement="bottom"
                                   ref="fileName"
                                   :data-content="i18n('script.info.name.help')"
                                   v-model="fileName"
                                   v-validate="{ required: true, regex: '^[a-zA-Z]{1}([a-zA-Z0-9]|[_]|[-]|[.]){2,19}$' }">
                        </control-group>
                        <control-group :class="{ error: errors.has('testUrl') }" name="testUrl" labelMessageKey="script.info.url">
                            <select id="method" name="method" class="form-control ml-2" v-model="method">
                                <option value="GET" selected="selected">GET</option>
                                <option value="POST">POST</option>
                            </select>
                            <input type="text" id="testUrl" name="testUrl"
                                   class="ml-1 form-control" :title="i18n('home.tip.url.title')"
                                   data-toggle="popover"
                                   data-trigger="focus"
                                   data-placement="bottom"
                                   ref="testUrl"
                                   v-model="testUrl"
                                   v-validate="scriptHandler.projectHandler ? null : { url: { require_protocol: true } }"
                                   :placeholder="i18n('home.placeholder.url')"
                                   :data-content="i18n('home.tip.url.content')">
                        </control-group>
                        <div class="d-flex justify-content-center">
                            <div>
                                <div class="d-flex align-items-center mb-2">
                                    <input id="createLibAndResource"
                                           type="checkbox"
                                           name="createLibAndResource"
                                           data-toggle="popover"
                                           data-trigger="focus"
                                           data-placement="right"
                                           v-model="createLibAndResource"
                                           :title="i18n('script.action.createResourceAndLib')"
                                           :data-content="i18n('script.message.libAndResource')">
                                    <label for="createLibAndResource" class="pointer-cursor ml-1 mb-0" v-text="i18n('script.action.createResourceAndLib')"></label>
                                </div>
                                <div class="card bg-light mb-2 create-script-help-message">
                                    <div>
                                        <span v-html="i18n('script.action.createResourceAndLib.help')"></span>
                                        <a href="https://github.com/naver/ngrinder/wiki/How-to-use-lib-and-resources"
                                           target="_blank" class="ml-2" v-text="'guide'">
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="d-flex align-items-center flex-column">
                        <div>
                            <a class="pointer-cursor" @click="showScriptOption = !showScriptOption"
                               href="#" v-text="i18n('perfTest.config.showAdvancedConfig')"></a>
                        </div>
                        <div v-show="showScriptOption" class="card bg-light mt-2 w-100">
                            <script-option ref="scriptOption" :method="method"></script-option>
                        </div>
                    </div>
                </div>
                <footer class="modal-footer">
                    <button class="btn btn-primary" @click="createScript">
                        <i class="fa fa-plus mr-1"></i>
                        <span v-text="i18n('common.button.create')"></span>
                    </button>
                    <button class="btn btn-danger" v-text="i18n('common.button.cancel')" data-dismiss="modal"></button>
                </footer>
            </div>
        </div>
    </div>
</template>

<script>
    import { Component } from 'vue-property-decorator';
    import ModalBase from '../../common/modal/ModalBase.vue';
    import ControlGroup from '../../common/ControlGroup.vue';
    import ScriptOption from './ScriptOption.vue';

    const resolve = (source, relative) => {
        const removeAppendedSlash = path => (path.startsWith('/') ? path.slice(1) : path);
        const removePrependedSlash = path => (path.endsWith('/') ? path.slice(0, path.length - 1) : path);
        return `${removePrependedSlash(source)}/${removeAppendedSlash(relative)}`;
    };

    @Component({
        name: 'createScriptModal',
        props: {
            currentPath: {
                type: String,
                required: true,
            },
        },
        components: { ControlGroup, ScriptOption },
        $_veeValidate: {
            validator: 'new',
        },
    })
    export default class CreateScriptModal extends ModalBase {
        fileName = '';
        handlers = [];
        scriptHandler = {};
        method = 'GET';
        testUrl = '';
        createLibAndResource = false;

        showScriptOption = false;

        mounted() {
            this.$http.get('/script/api/handlers')
                .then(res => {
                    this.handlers = res.data;
                    this.scriptHandler = this.handlers[0];
                });
        }

        createScript() {
            this.$validator.validateAll()
                .then(result => {
                    if (result) {
                        this.sendCreateScriptRequest();
                    } else {
                        this.focusToInvalidField();
                    }
                });
        }

        beforeHidden() {
            this.fileName = '';
            this.scriptHandler = this.handlers[0];
            this.method = 'GET';
            this.testUrl = '';
            this.createLibAndResource = false;
            this.showScriptOption = false;
            this.$refs.scriptOption.reset();
        }

        sendCreateScriptRequest() {
            if (this.scriptHandler.projectHandler !== true) {
                // append extension
                const extension = `.${this.scriptHandler.extension.toLowerCase()}`;
                if (this.fileName.toLowerCase().lastIndexOf(extension) === -1) {
                    this.fileName = this.fileName + extension;
                }
            }

            const params = {
                fileName: this.fileName,
                testUrl: this.testUrl,
                options: JSON.stringify(this.$refs.scriptOption.toJson),
                scriptType: this.scriptHandler.key,
                createLibAndResource: this.createLibAndResource,
            };

            this.$http.post(`/script/api/new/${this.currentPath}?type=script`, params)
                .then(res => {
                    this.hide();
                    if (res.data.message) {
                        this.$router.push(res.data.path);
                    } else {
                        this.$router.push(resolve('/script/detail', res.data.file.path));
                    }
                });
        }

        focusToInvalidField() {
            if (this.fields.testUrl.invalid) {
                $(this.$refs.testUrl).focus();
            }

            if (this.fields.fileName.invalid) {
                $(this.$refs.fileName).focus();
            }
        }
    }
</script>

<style lang="less" scoped>
    #create-script-modal {
        .control-group {
            margin-bottom: 20px;
            display: flex;
            align-items: center;
        }

        .modal-dialog {
            max-width: 800px;
        }

        select {
            width: 140px;
        }
    }

    .modal-body {
        max-height: 500px;
        overflow-y: auto;
    }

    .create-script-help-message {
        padding: 10px 15px;
    }

    input[type="text"] {
        width: 380px;
        height: 30px;
    }
</style>

<style lang="less">
    #create-script-modal {
        .control-label {
            width: 160px;
        }
    }
</style>
