<template>
    <div class="modal fade" :id="id">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                    <h4>
                        <span v-text="i18n('perfTest.config.addHost')"></span>&nbsp;
                        <small v-text="i18n('perfTest.config.pleaseInputOneOptionAtLeast')"></small>
                    </h4>
                </div>
                <div class="modal-body">
                    <div class="form-horizontal form-horizontal-4">
                        <fieldset>
                            <control-group labelMessageKey="perfTest.config.domain" ref="domainControlGroup" labelStyle="text-align: right; margin-right: 10px;">
                                <input type="text"
                                       ref="domainInput"
                                       class="input-medium"
                                       name="domain"
                                       v-model="host"
                                       v-validate="{ regex: /^([a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,6}$/ }"
                                       data-toggle="popover"
                                       data-html="true"
                                       data-placement="right"
                                       @keyup="$refs.domainControlGroup.handleError(errors.has('domain'))"
                                       :title="i18n('perfTest.config.domain')"
                                       :data-content="i18n('perfTest.config.addHost.inputTargetDomain')">
                                <span v-show="errors.has('domain')" class="validation-message" v-text="errors.first('domain')"></span>
                            </control-group>
                            <control-group labelMessageKey="common.IP" ref="ipControlGroup" labelStyle="text-align: right; margin-right: 10px;">
                                <input type="text"
                                       name="ip"
                                       class="input-medium"
                                       v-model="ip"
                                       v-validate="{ regex: /((^\s*((([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))\s*$)|(^\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?\s*$))/ }"
                                       data-toggle="popover"
                                       data-html="true"
                                       data-placement="right"
                                       @keyup="$refs.ipControlGroup.handleError(errors.has('ip'))"
                                       title="IP"
                                       :data-content="i18n('perfTest.config.addHost.inputTargetIp')">
                                <span v-show="errors.has('ip')" class="validation-message" v-text="errors.first('ip')"></span>
                            </control-group>
                        </fieldset>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-primary" v-text="i18n('perfTest.config.add')" @click.prevent="addHost"></button>
                    <button class="btn" data-dismiss="modal" v-text="i18n('common.button.cancel')"></button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import ModalBase from '../../common/modal/ModalBase.vue';
    import Component from 'vue-class-component';
    import ControlGroup from '../../common/ControlGroup.vue';

    @Component({
        name: 'hostModal',
        components: {ControlGroup},
    })
    export default class HostModal extends ModalBase {
        host = '';
        ip = '';

        mounted() {
            this.$nextTick(() => {
                $('[data-toggle="popover"]').popover('destroy');
                $('[data-toggle="popover"]').popover({trigger: 'hover', container: '#add-host-modal'});
            });
        }

        addHost() {
            if (!this.ip && !this.host) {
                this.$refs.domainInput.focus();
                return;
            }

            this.$validator.validateAll().then(result => {
                if (result) {
                    this.hide();

                    let host = this.host;

                    if (this.ip) {
                        host = host ? `${host}:${this.ip}` : this.ip;
                    }

                    this.$emit('add-host', host);
                    this.clear();
                }
            });
        }

        clear() {
            this.host = '';
            this.ip = '';
        }
    }
</script>

<style lang="less" scoped>
    #add-host-modal {
        .validation-message {
            margin-left: 5px;
        }

        &.fade div {
            display: none;
        }

        &.in div {
            display: block;
        }
    }
</style>
