<template>
    <div class="modal modal-lg fade" id="create-script-modal" ref="createScriptModal">
        <div class="modal-dialog modal-script" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                    <div class="modal-header-title">
                        <h4 v-text="i18n('script.action.createScript')"></h4>
                    </div>
                    <div id="script-sample"
                         title="Sample Script Link"
                         data-toggle="popover"
                         :data-content="i18n('script.editor.sample.message')"
                         data-html="true"
                         data-placement="left">
                        <code>
                            <a target="_blank" href="https://github.com/naver/ngrinder/tree/master/script-sample">
                                Script Samples
                            </a>
                        </code>
                    </div>
                </div>
                <div class="modal-body" id="create-script-modal-body">
                    <form class="form-horizontal" method="post" target="_self" id="createForm">
                        <control-group name="fileName" labelMessageKey="script.info.name" ref="fileNameControlGroup">
                            <select name="scriptType" class="form-control span2" v-model="scriptHandler">
                                <option v-for="handler in handlers"
                                        :value="handler"
                                        v-text="handler.title">
                                </option>
                            </select>
                            <input type="text" id="fileName" name="fileName"
                                   class="span5" :title="i18n('script.info.name')"
                                   data-toggle="popover"
                                   data-placement="right"
                                   :data-content="i18n('script.info.name.help')"
                                   v-model="fileName"
                                   v-validate="{required: true, regex: '^[a-zA-Z]{1}([a-zA-Z0-9]|[_]|[-]|[.]){2,19}$'}"
                                   ref="fileName"/>
                        </control-group>

                        <control-group name="testUrl" labelMessageKey="script.info.url" ref="testUrlControlGroup">
                            <select id="method" name="method" class="form-control span2" v-model="method">
                                <option value="GET" selected="selected">GET</option>
                                <option value="POST">POST</option>
                            </select>
                            <input type="text" id="testUrl" name="testUrl"
                                   class="span5" :title="i18n('home.tip.url.title')"
                                   data-toggle="popover"
                                   data-placement="bottom"
                                   :data-content="i18n('home.tip.url.content')"
                                   v-model="testUrl"
                                   :placeholder="i18n('home.placeholder.url')"
                                   v-validate="scriptHandler.projectHandler ? null : {url: {require_protocol: true}, required: true}"
                                   ref="testUrl"/>
                        </control-group>

                        <div class="control-group">
                            <div class="controls">
                                <label class="create-lib-and-resource-checkbox">
                                    <input type="checkbox"
                                           name="createLibAndResource"
                                           data-toggle="popover"
                                           data-placement="right"
                                           :title="i18n('script.action.createResourceAndLib')"
                                           :data-content="i18n('script.message.libAndResource.help')"
                                           v-model="createLibAndResource"/>
                                    <span v-text="i18n('script.action.createResourceAndLib')"></span>
                                </label>
                                <span class="help-inline well create-script-help-message">
                                    <span v-text="i18n('script.action.createResourceAndLib.help')"></span>
                                    <a href="https://github.com/naver/ngrinder/wiki/How-to-use-lib-and-resources"
                                       target="blank"><i
                                        class="icon-question-sign how-to-use-lib-and-resources-icon"></i></a>
                                </span>
                            </div>
                        </div>
                    </form>
                </div>
                <div class="modal-body" id="advanced-option-body">
                    <div class="text-center">
                        <a id="detail_config_section_btn" class="pointer-cursor"
                           v-on:click="showScriptOption = !showScriptOption"
                           v-text="i18n('perfTest.config.showAdvancedConfig')"></a>
                    </div>
                    <div :class="{hide: !showScriptOption}" class="well" style="overflow: scroll">
                        <script-option ref="scriptOption" :method="method"></script-option>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-primary" v-text="i18n('common.button.create')" @click="createScript"></button>
                    <button class="btn" v-text="i18n('common.button.cancel')" data-dismiss="modal"></button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import { Component, Watch } from 'vue-property-decorator';
    import Base from '../../Base.vue';
    import ControlGroup from '../../common/ControlGroup.vue';
    import ScriptOption from './ScriptOption.vue';

    @Component({
        name: 'createScriptModal',
        props: {
            currentPath: {
                type: String,
                required: true,
            },
        },
        components: {ControlGroup, ScriptOption}
    })
    export default class CreateScriptModal extends Base {
        fileName = '';
        handlers = [];
        scriptHandler = {};
        method = 'GET';
        testUrl = '';
        createLibAndResource = false;

        showScriptOption = false;

        mounted() {
            this.$http.get("/script/api/handlers")
                .then(res => {
                    this.handlers = res.data;
                    this.scriptHandler = this.handlers[0];
                });
        }

        async createScript() {
            if (await this.validFields() === false) {
                return;
            }

            $(this.$refs.createScriptModal).modal('hide');
            if (this.scriptHandler.projectHandler !== true) {
                // append extension
                const extension = `.${this.scriptHandler.extension.toLowerCase()}`;
                if (this.fileName.toLowerCase().lastIndexOf(extension) === -1) {
                    this.fileName = this.fileName + extension;
                }
            }

            this.$http.post(`/script/api/new/${this.currentPath}`, formDataOf(
                "fileName", this.fileName,
                "testUrl", this.testUrl,
                "options", JSON.stringify(this.$refs.scriptOption.toJson),
                "scriptType", this.scriptHandler.key,
                "createLibAndResource", this.createLibAndResource
            ), {
                params: { "type": 'script' }
            })
            .then(res => {
                if (res.data.message) {
                    console.log(res.data.message);
                    this.$router.push(res.data.path);
                } else {
                    this.$router.push(resolve('/script/detail', res.data.file.path));
                }
            });

            this.resetFields();
        }

        resetFields() {
            this.fileName = '';
            this.scriptHandler = this.handlers[0];
            this.method = 'GET';
            this.testUrl = '';
            this.createLibAndResource = false;
            this.$refs.testUrlControlGroup.success = false;
            this.$refs.fileNameControlGroup.success = false;
        }

        validFields() {
            if (this.fields.testUrl.invalid) {
                $(this.$refs.testUrl).focus();
            } else {
                this.$refs.testUrlControlGroup.success = true;
            }

            if (this.fields.fileName.invalid) {
                $(this.$refs.fileName).focus();
            } else {
                this.$refs.fileNameControlGroup.success = true;
            }

            return this.$validator.validateAll();
        }

        @Watch('errors', {deep: true})
        errorsChanged(errors) {
            this.$refs.fileNameControlGroup.hasError = !!errors.first('fileName');
            this.$refs.testUrlControlGroup.hasError = !!errors.first('testUrl');
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

    const resolve = (source, relative) => {
        const removeAppendedSlash = path => path.startsWith('/') ? path.slice(1) : path;
        const removePrependedSlash = path => path.endsWith('/') ? path.slice(0, path.length - 1) : path;

        return removePrependedSlash(source) + '/' + removeAppendedSlash(relative);
    }
</script>

<style lang="less" scoped>
    #create-script-modal {
        display: none;

        .control-group {
            margin-bottom: 20px;
        }
    }

    #script-sample {
        margin-top: -35px;
        margin-left: 753px;
    }

    #create-script-modal-body {
        max-height: 500px;
    }

    #advanced-option-body {
        overflow: scroll;
    }

    .how-to-use-lib-and-resources-icon {
        margin-top: 2px;
    }

    .create-lib-and-resource-checkbox {
        input[type=checkbox] {
            margin: -1px 0 0  -20px;
        }

        span {
            padding-left: 5px;
        }
    }

    .create-script-help-message {
        min-height: 20px;
        padding: 19px;
    }

    .modal-body {
        overflow: visible;
    }

    input[type="text"] {
        height: 30px;
    }
</style>
