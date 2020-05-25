<template>
    <div class="modal fade" id="add-external-agent-modal">
        <div class="modal-dialog">
            <div class="modal-content">
                <header class="modal-header">
                    <h4>
                        <span>Add External Agent</span>
                    </h4>
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                </header>
                <div class="modal-body">
                    <div class="form-horizontal form-horizontal-4">
                        <fieldset>
                            <control-group labelMessageKey="agent.list.IPAndDns">
                                <input type="text"
                                       name="ip"
                                       class="form-control"
                                       v-model="ip"
                                       ref="ip">
                            </control-group>
                            <control-group labelMessageKey="agent.info.port">
                                <input type="text"
                                       name="port"
                                       class="form-control"
                                       v-model="port"
                                       @keyup.enter.prevent="addExternalAgent">
                            </control-group>
                        </fieldset>
                    </div>
                </div>
                <footer class="modal-footer">
                    <button class="btn btn-success" @click.prevent="addExternalAgent">
                        <i class="fa fa-plus mr-1"></i>
                        <span v-text="i18n('common.button.add')"></span>
                    </button>
                    <button class="btn btn-danger" data-dismiss="modal" v-text="i18n('common.button.cancel')"></button>
                </footer>
            </div>
        </div>
    </div>
</template>

<script>
    import { Component } from 'vue-property-decorator';
    import { Mixins } from 'vue-mixin-decorator';

    import ControlGroup from '../../common/ControlGroup.vue';
    import ModalBase from '../../common/modal/ModalBase.vue';
    import MessageMixin from '../../common/mixin/MessagesMixin.vue';

    @Component({
        name: 'addExternalAgentModal',
        components: { ControlGroup },
    })
    export default class AddExternalAgentModal extends Mixins(ModalBase, MessageMixin) {
        ip = '';
        port = null;

        addExternalAgent() {
            this.$http.post(`/agent/api/external/${this.ip}/${this.port}`)
                .catch(() => this.showErrorMsg(this.i18n('common.error.error')))
                .finally(() => this.hide());
        }

        reset() {
            this.ip = '';
            this.port = null;
        }
    }
</script>

<style lang="less" scoped>
    #add-external-agent-modal {
        .modal-dialog {
            margin-top: 80px;
        }

        .control-group {
            display: flex;
            align-items: center;
            margin-bottom: 10px;
        }
    }

    input[type="text"] {
        width: 200px;
        height: 30px;
        display: block;
    }
</style>

<style lang="less">
    #add-external-agent-modal {
        .controls {
            margin: 0 0 0 30px;
        }
    }
</style>
