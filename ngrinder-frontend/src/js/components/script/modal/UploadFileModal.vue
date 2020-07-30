<template>
    <div class="modal fade" id="upload-file-modal" ref="uploadFileModal">
        <div class="modal-dialog">
            <div class="modal-content">
                <header class="modal-header">
                    <h4 v-text="i18n('script.action.upload')"></h4>
                    <a class="close pointer-cursor" data-dismiss="modal" id="upCloseBtn">x</a>
                </header>
                <div class="modal-body">
                    <div class="form-horizontal">
                        <fieldset>
                            <control-group name="description" labelMessageKey="script.action.commit">
                                <input type="text" id="description" class="form-control mb-2" v-model="description">
                            </control-group>
                            <control-group :class="{ error: errors.has('file') }" labelMessageKey="script.info.file" ref="fileControlGroup">
                                <div :title="i18n('script.message.upload.title')"
                                     :data-content="i18n('script.message.upload.content')"
                                     data-toggle="popover"
                                     data-trigger="focus hover">
                                    <input type="file"
                                           ref="file"
                                           name="file"
                                           class="d-block"
                                           @change="file = $event.target"
                                           v-validate="{ required: true }"/>
                                </div>
                            </control-group>
                            <span v-show="errors.has('file')" class="validation-message mt-1" v-text="errors.first('file')"></span>
                        </fieldset>
                    </div>
                </div>
                <footer class="modal-footer">
                    <button class="btn btn-primary" @click="uploadFile">
                        <i class="fa fa-upload mr-1"></i>
                        <span v-text="i18n('script.action.upload')"></span>
                    </button>
                    <button class="btn btn-danger" data-dismiss="modal" v-text="i18n('common.button.cancel')"></button>
                </footer>
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

        beforeHidden() {
            this.description = '';
            this.$refs.file.value = '';
        }
    }
</script>

<style lang="less" scoped>
    #upload-file-modal {
        input[type="text"] {
            width: 220px;
            height: 30px;
        }

        .control-group {
            display: flex;
            align-items: center;
        }

        .validation-message {
            margin-left: 161px;
        }
    }
</style>

<style lang="less">
    #upload-file-modal {
        .control-label {
            width: 140px;
        }

        .controls {
            margin-left: 20px;
        }
    }
</style>
