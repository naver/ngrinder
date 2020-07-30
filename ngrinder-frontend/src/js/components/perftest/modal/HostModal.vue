<template>
    <div class="modal fade" id="add-host-modal">
        <div class="modal-dialog">
            <div class="modal-content">
                <header class="modal-header">
                    <h4>
                        <span v-text="i18n('perfTest.config.addHost')"></span>
                        <small v-text="i18n('perfTest.config.pleaseInputOneOptionAtLeast')"></small>
                    </h4>
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                </header>
                <div class="modal-body">
                    <div class="form-horizontal form-horizontal-4">
                        <fieldset>
                            <control-group :class="{error: errors.has('domain')}" labelMessageKey="perfTest.config.domain">
                                <input type="text"
                                       ref="domain"
                                       class="form-control"
                                       name="domain"
                                       v-model="host"
                                       v-validate="{ regex: /^([a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,6}$/ }"
                                       data-toggle="popover"
                                       data-html="true"
                                       data-trigger="hover"
                                       data-placement="right"
                                       :title="i18n('perfTest.config.domain')"
                                       :data-content="i18n('perfTest.config.addHost.inputTargetDomain')">
                                <div v-show="errors.has('domain')" class="validation-message mt-1" v-text="errors.first('domain')"></div>
                            </control-group>
                            <control-group :class="{error: errors.has('ip')}" labelMessageKey="common.IP">
                                <input type="text"
                                       name="ip"
                                       class="form-control"
                                       v-model="ip"
                                       v-validate="{ regex: /((^\s*((([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))\s*$)|(^\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?\s*$))/ }"
                                       data-toggle="popover"
                                       data-html="true"
                                       data-trigger="hover"
                                       data-placement="right"
                                       title="IP"
                                       :data-content="i18n('perfTest.config.addHost.inputTargetIp')">
                                <div v-show="errors.has('ip')" class="validation-message mt-1" v-text="errors.first('ip')"></div>
                            </control-group>
                        </fieldset>
                    </div>
                </div>
                <footer class="modal-footer">
                    <button class="btn btn-success" @click.prevent="addHost">
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
    import Component from 'vue-class-component';
    import ModalBase from '../../common/modal/ModalBase.vue';
    import ControlGroup from '../../common/ControlGroup.vue';

    @Component({
        name: 'hostModal',
        components: { ControlGroup },
        $_veeValidate: {
            validator: 'new',
        },
    })
    export default class HostModal extends ModalBase {
        host = '';
        ip = '';

        mounted() {
            this.$nextTick(() => {
                $('[data-toggle="popover"]').popover();
            });
        }

        addHost() {
            if (!this.ip && !this.host) {
                this.$refs.domain.focus();
                return;
            }

            this.$validator.validateAll().then(result => {
                if (result) {
                    let host = this.host;

                    if (this.ip) {
                        host = host ? `${host}:${this.ip}` : this.ip;
                    }

                    this.$emit('add-host', host);
                    this.hide();
                }
            });
        }

        beforeHidden() {
            this.host = '';
            this.ip = '';
        }
    }
</script>

<style lang="less" scoped>
    #add-host-modal {
        .modal-dialog {
            margin-top: 80px;
        }

        .validation-message {
            margin-left: 5px;
        }

        .control-group {
            display: flex;
            margin-bottom: 2px;
            min-height: 45px;
        }
    }

    input[type="text"] {
        width: 200px;
        height: 30px;
        display: block;
    }
</style>

<style lang="less">
    #add-host-modal {
        .controls {
            margin: 0 0 0 30px;
        }

        .control-label {
            .label-message {
                display: inline-block;
                margin-top: 5px;
            }
        }
    }
</style>
