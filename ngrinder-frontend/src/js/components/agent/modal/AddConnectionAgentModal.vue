<template>
    <div class="modal fade" id="add-connection-agent-modal">
        <div class="modal-dialog">
            <div class="modal-content">
                <header class="modal-header">
                    <h4>
                        <span>Add Connection Agent</span>
                    </h4>
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                </header>
                <div class="modal-body">
                    <div class="form-horizontal form-horizontal-4">
                        <fieldset>
                            <control-group :class="{ error: errors.has('region') }" v-show="ngrinder.config.clustered"
                                           name="region" labelMessageKey="agent.info.region">
                                <select name="region" class="form-control"
                                        v-model="region" v-validate="{ required: true }">
                                    <option v-for="region in regions"
                                            :value="region"
                                            v-text="i18n(region)">
                                    </option>
                                </select>
                                <div v-visible="errors.has('region')" class="validation-message" v-text="errors.first('region')"></div>
                            </control-group>
                            <control-group :class="{ error: errors.has('ip') }" name="ip"
                                           labelMessageKey="agent.list.IPAndDns">
                                <input type="text" name="ip" class="form-control"
                                       v-model="ip" ref="ip"
                                       v-validate="{ required: true }"/>
                                <div v-visible="errors.has('ip')" class="validation-message" v-text="errors.first('ip')"></div>
                            </control-group>
                            <control-group :class="{ error: errors.has('port') }" name="port"
                                           labelMessageKey="agent.info.port">
                                <input type="text" name="port" class="form-control"
                                       v-model="port" ref="port"
                                       v-validate="{ required: true, numeric: true }"
                                       @keyup.enter.prevent="addConnectionAgent"/>
                                <div v-visible="errors.has('port')" class="validation-message" v-text="errors.first('port')"></div>
                            </control-group>
                        </fieldset>
                    </div>
                </div>
                <footer class="modal-footer">
                    <button class="btn btn-success" @click.prevent="addConnectionAgent">
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
    import { Component, Prop } from 'vue-property-decorator';
    import { Mixins } from 'vue-mixin-decorator';

    import ControlGroup from '../../common/ControlGroup.vue';
    import ModalBase from '../../common/modal/ModalBase.vue';
    import MessageMixin from '../../common/mixin/MessagesMixin.vue';

    @Component({
        name: 'addConnectionAgentModal',
        components: { ControlGroup },
        $_veeValidate: {
            validator: 'new',
        },
    })
    export default class AddConnectionAgentModal extends Mixins(ModalBase, MessageMixin) {
        @Prop({ type: Array, required: false, default: [] })
        regions;

        region = '';

        ip = '';
        port = null;

        created() {
            if (!this.ngrinder.config.clustered) {
                this.region = 'NONE';
            }
        }

        addConnectionAgent() {
            this.$validator.validateAll()
                .then(result => {
                    if (result) {
                        this.$http.post(`/agent/api/connect/${this.ip}/${this.port}?region=${this.region}`)
                            .catch(err => this.showErrorMsg(`Unable to connect to connection agent <b>${this.ip}:${this.port}</b><br>${err.response.data.message}`))
                            .finally(() => this.hide());
                    }
                });
        }

        beforeShown() {
            this.region = '';
            this.ip = '';
            this.port = null;
        }
    }
</script>

<style lang="less" scoped>
    #add-connection-agent-modal {
        .modal-dialog {
            margin-top: 80px;
        }

        .control-group {
            display: flex;
            align-items: center;
            margin-bottom: 16px;
        }

        .validation-message {
            position: absolute;
        }
    }

    input[type="text"] {
        width: 200px;
        height: 30px;
        display: block;
    }
</style>

<style lang="less">
    #add-connection-agent-modal {
        .controls {
            margin: 0 0 0 30px;
        }
    }
</style>
