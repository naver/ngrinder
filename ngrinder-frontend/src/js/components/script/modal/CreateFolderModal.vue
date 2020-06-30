<template>
    <div id="create-folder-modal" class="modal fade" ref="createFolderModal">
        <div class="modal-dialog">
            <div class="modal-content">
                <header class="modal-header">
                    <h4 v-text="i18n('script.action.createFolder')"></h4>
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                </header>
                <div class="modal-body">
                    <div class="form-horizontal">
                        <fieldset>
                            <control-group :class="{ error: errors.has('folderName') }"
                                           name="folderName"
                                           label-message-key="script.info.folderName"
                                           ref="folderNameControlGroup">
                                <input type="text"
                                       class="form-control d-block"
                                       id="folderName"
                                       name="folderName"
                                       data-toggle="popover"
                                       data-trigger="focus"
                                       data-placement="bottom"
                                       :data-content="i18n('script.info.folderName.help')"
                                       ref="folderName"
                                       v-model="folderName"
                                       v-validate="{ required: true, regex: '^[a-zA-Z]{1}([a-zA-Z0-9]|[_]|[-]|[.]){2,19}$' }"/>
                            </control-group>
                            <span v-visible="errors.has('folderName')" class="validation-message mt-1" v-text="errors.first('folderName')"></span>
                        </fieldset>
                    </div>
                </div>
                <footer class="modal-footer">
                    <button class="btn btn-primary" @click="create">
                        <i class="fa fa-plus mr-1"></i>
                        <span v-text="i18n('common.button.create')"></span>
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
        name: 'createFolderModal',
        components: { ControlGroup },
        props: {
            currentPath: {
                type: String,
                required: true,
            },
        },
        $_veeValidate: {
            validator: 'new',
        },
    })
    export default class CreateFolderModal extends ModalBase {
        folderName = '';

        create() {
            this.$validator.validate('folderName')
                .then(result => {
                    if (result) {
                        this.$http({
                            method: 'post',
                            url: `/script/api/new/${this.currentPath}?type=folder`,
                            data: this.folderName,
                            headers: { 'Content-Type': 'text/plain' },
                        }).then(() => {
                            $(this.$refs.createFolderModal).modal('hide');
                            this.folderName = '';
                            this.errors.clear();
                            this.$refs.folderNameControlGroup.success = false;

                            this.$EventBus.$emit(this.$Event.REFRESH_SCRIPT_LIST);
                        });
                    } else {
                        this.$refs.folderName.focus();
                    }
                });
        }

        beforeHidden() {
            this.folderName = '';
        }
    }
</script>

<style lang="less" scoped>
    #create-folder-modal {
        .control-group {
            display: flex;
            align-items: center;

            .controls {
                margin: 0;
            }

            input[type="text"] {
                width: 164px;
                height: 30px;
                margin-left: 20px;
            }
        }

        .validation-message {
            margin-left: 161px;
        }
    }

    .modal-body {
        overflow: visible;
    }
</style>

<style lang="less">
    #create-folder-modal {
        .control-label {
            width: 140px;
            text-align: right;
        }
    }
</style>
