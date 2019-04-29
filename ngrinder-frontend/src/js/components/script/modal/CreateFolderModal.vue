<template>
    <div id="create-folder-modal" class="modal fade" ref="createFolderModal">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                    <h4 v-text="i18n('script.action.createFolder')"></h4>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal form-horizontal-4">
                        <fieldset>
                            <control-group name="folderName" label-message-key="script.info.folderName" ref="folderNameControlGroup">
                                <input type="text"
                                       class="input-medium"
                                       id="folderName"
                                       name="folderName"
                                       data-placement="right"
                                       :data-content="i18n('script.info.folderName.help')"
                                       ref="folderName"
                                       v-model="folderName"
                                       v-validate="{required: true, regex: '^[a-zA-Z]{1}([a-zA-Z0-9]|[_]|[-]|[.]){2,19}$'}"/>
                                <span v-text="errors.first('folderName')"></span>
                            </control-group>
                        </fieldset>
                    </form>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-primary" v-text="i18n('common.button.create')" v-on:click="create"></button>
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
        name: "createFolderModal",
        components: { ControlGroup },
        props: {
            currentPath: {
                type: String,
                required: true,
            }
        }
    })
    export default class CreateFolderModal extends Base {

        folderName = '';

        mounted() {
            this.setCustomValidationMessages();
        }

        async create() {
            if (await this.validFields() === false) {
                return;
            }

            this.$http.post(`/script/api/new/${this.currentPath}`, null, {
                params: {
                    type: 'folder',
                    user: this.currentUser,
                    folderName: this.folderName,
                }
            })
            .then(() => {
                $(this.$refs.createFolderModal).modal('hide');
                this.folderName = '';
                this.errors.clear();
                this.$refs.folderNameControlGroup.success = false;

                this.$EventBus.$emit(this.$Event.REFRESH_SCRIPT_LIST);
            })
        }

        validFields() {
            if (this.fields.folderName.invalid) {
                this.$refs.folderNameControlGroup.hasError = true;
                this.$refs.folderName.focus();
            } else {
                this.$refs.folderNameControlGroup.success = true;
            }

            return this.$validator.validateAll();
        }

        setCustomValidationMessages() {
            const dictionary = {
                required: () => this.i18n('common.message.validate.empty'),
                regex: () => this.i18n('common.message.validate.format'),
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
            this.$refs.folderNameControlGroup.hasError = !!errors.first('folderName');
        }
    }
</script>

<style lang="less" scoped>
    #create-folder-modal {
        display: none;

        .control-group {
            margin-bottom: 20px;
        }
    }

    input[type="text"] {
        width: 164px;
        height: 30px;
    }

    .modal-body {
        overflow: visible;
    }
</style>
