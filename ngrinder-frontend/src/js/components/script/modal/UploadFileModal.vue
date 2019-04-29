<template>
    <div class="modal fade" id="upload-file-modal" ref="uploadFileModal">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <a class="close" data-dismiss="modal" id="upCloseBtn">&times;</a>
                    <h4 v-text="i18n('script.action.upload')"></h4>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal" method="post" target="_self"
                          :action="`/script/upload/${currentPath}`" id="uploadForm"
                          enctype="multipart/form-data">
                        <fieldset>
                            <input type="hidden" id="path" name="path"/>
                            <control-group name="description" labelMessageKey="script.action.commit">
                                <input type="text" id="description" name="description" class="form-control"
                                       v-model="description">
                            </control-group>
                            <control-group name="uploadFile" labelMessageKey="script.info.file" ref="fileControlGroup">
                                <div data-html='true'
                                     :title="i18n(' script.message.upload.title')"
                                     :data-content="i18n('script.message.upload.content')">
                                    <input type="file" class="input-file form-control" id="file" name="file"
                                           @change="file = $event.target"
                                           ref="file"
                                           v-validate="{required: true}"/>
                                    <span class="help-inline" v-text="errors.first('file')"></span>
                                </div>
                            </control-group>
                        </fieldset>
                    </form>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-primary" id="upload_file_button" v-on:click="uploadFile"
                            v-text="i18n('script.action.upload')">
                    </button>
                    <button class="btn" data-dismiss="modal" v-text="i18n('common.button.cancel')"></button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import { Component, Watch } from 'vue-property-decorator';
    import Base from '../../Base.vue';

    import ControlGroup from '../../common/ControlGroup.vue';
    import { Validator } from 'vee-validate';


    @Component({
        name: "uploadFileModal",
        props: {
            currentPath: {
                type: String,
                required: true,
            }
        },
        components: { ControlGroup },
    })
    export default class UploadFileModal extends Base {

        file = null;
        description = '';

        mounted() {
            this.setCustomValidationMessages();
        }

        async uploadFile() {
            if (await this.validFields() === false) {
                return;
            }

            const formData = new FormData();
            formData.append('uploadFile', this.file.files[0]);
            this.$http.post(`/script/api/upload/${this.currentPath}`, formData, {
                headers: {
                    'content-type': 'multipart/form-data'
                },
                params: {
                    user: this.currentUser,
                    description: this.description,
                }
            })
            .then(() => {
                $(this.$refs.uploadFileModal).modal('hide');
                this.file = null;
                this.errors.clear();

                this.$EventBus.$emit(this.$Event.REFRESH_SCRIPT_LIST);
            });
        }

        validFields() {
            if (this.fields.file.invalid) {
                this.$refs.fileControlGroup.hasError = true;
                this.$refs.file.focus();
            }

            return this.$validator.validateAll();
        }

        setCustomValidationMessages() {
            const dictionary = {
                required: () => this.i18n('common.message.validate.empty'),
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

        @Watch('errors', {deep: true})
        errorsChanged(errors) {
            this.$refs.fileControlGroup.hasError = !!errors.first('file');
        }
    }
</script>

<style lang="less" scoped>
    #upload-file-modal {
        display: none;

        .control-group {
            margin-bottom: 20px;
        }
    }

    input[type="text"] {
        width: 220px;
        height: 30px;
    }
</style>
