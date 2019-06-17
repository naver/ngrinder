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
                            <control-group :class="{error: errors.has('file')}" name="uploadFile" labelMessageKey="script.info.file" ref="fileControlGroup">
                                <div data-html='true'
                                     :title="i18n(' script.message.upload.title')"
                                     :data-content="i18n('script.message.upload.content')">
                                    <input type="file" class="input-file form-control" id="file" name="file"
                                           @change="file = $event.target"
                                           ref="file"
                                           v-validate="{required: true}"/>
                                    <span v-show="errors.has('file')" class="help-inline" v-text="errors.first('file')"></span>
                                </div>
                            </control-group>
                        </fieldset>
                    </form>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-primary" id="upload_file_button" @click="uploadFile"
                            v-text="i18n('script.action.upload')">
                    </button>
                    <button class="btn" data-dismiss="modal" v-text="i18n('common.button.cancel')"></button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import { Component } from 'vue-property-decorator';
    import ModalBase from '../../common/modal/ModalBase.vue';

    import ControlGroup from '../../common/ControlGroup.vue';


    @Component({
        name: 'uploadFileModal',
        props: {
            currentPath: {
                type: String,
                required: true,
            },
        },
        components: { ControlGroup },
        $_veeValidate: {
            validator: 'new',
        },
    })
    export default class UploadFileModal extends ModalBase {
        file = null;
        description = '';

        uploadFile() {
            this.$validator.validate('file')
                .then(result => {
                    if (result) {
                        const formData = new FormData();
                        formData.append('uploadFile', this.file.files[0]);
                        this.$http.post(`/script/api/upload/${this.currentPath}`, formData, {
                            headers: {
                                'content-type': 'multipart/form-data',
                            },
                            params: {
                                description: this.description,
                            },
                        }).then(() => {
                            $(this.$refs.uploadFileModal).modal('hide');
                            this.file = null;
                            this.errors.clear();

                            this.$EventBus.$emit(this.$Event.REFRESH_SCRIPT_LIST);
                        });
                    } else {
                        this.$refs.file.focus();
                    }
                });
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
