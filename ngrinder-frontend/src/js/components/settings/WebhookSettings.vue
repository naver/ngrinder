<template>
    <div class="container webhook d-flex">
        <div class="webhook-config">
            <div class="subhead">
                <h2 v-text="i18n('webhook.settings')"></h2>
            </div>
            <div class="form-container">
                <dl class="form-group" :class="{'hasError': errors.has('payloadUrl')}">
                    <dt>
                        <label for="payload-url" v-text="i18n('common.payloadUrl')"></label>
                        <span class="required-mark">*</span>
                    </dt>
                    <dd class="input-payloadUrl-container">
                        <input id="payload-url"
                               class="form-control"
                               type="text"
                               name="payloadUrl"
                               ref="payloadUrl"
                               placeholder="http://please-input-webhook-url/"
                               v-validate="{ required: true, url: {require_protocol: true} }"
                               v-model="config.payloadUrl"/>
                        <button class="btn btn-primary validationBtn"
                                data-toggle="popover"
                                data-html="true"
                                data-trigger="hover"
                                data-placement="bottom"
                                ref="validationBtn"
                                v-text="i18n('script.editor.button.validate')"
                                @click="checkValidation"
                                :data-content="i18n('webhook.button.validate.help')">
                        </button>
                        <div v-show="errors.has('payloadUrl')" class="validation-message" v-text="errors.first('payloadUrl')"></div>
                    </dd>
                </dl>
                <dl class="form-group">
                    <dt>
                        <label for="content-type" v-text="i18n('common.contentType')"></label>
                    </dt>
                    <dd>
                        <select id="content-type" class="custom-select" v-model="config.contentType">
                            <option value="JSON">application/json</option>
                            <option value="FORM_URL_ENCODED">application/x-www-form-urlencoded</option>
                        </select>
                    </dd>
                </dl>
            </div>
            <div class="form-container mt-4">
                <dl class="form-group mb-0">
                    <dt>
                        <label v-text="i18n('common.event')"></label>
                    </dt>
                    <dd>
                        <div class="form-checkout custom-control custom-checkbox">
                            <input id="event-start" type="checkbox" class="custom-control-input" v-model="eventStart">
                            <label class="custom-control-label" for="event-start" v-text="i18n('common.start')"></label>
                            <p v-text="i18n('webhook.config.event.help', { event: i18n('common.start') })"></p>
                        </div>
                        <div class="form-checkout custom-control custom-checkbox">
                            <input id="event-finish" type="checkbox" class="custom-control-input" v-model="eventFinish">
                            <label class="custom-control-label" for="event-finish" v-text="i18n('common.finish')"></label>
                            <p v-text="i18n('webhook.config.event.help', { event: i18n('common.finish') })"></p>
                        </div>
                    </dd>
                </dl>
                <dl class="form-group m-0">
                    <dt>
                        <label v-text="i18n('common.activation')"></label>
                    </dt>
                    <dd>
                        <div class="form-checkout custom-control custom-checkbox">
                            <input id="active" type="checkbox" class="custom-control-input" v-model="config.active">
                            <label class="custom-control-label" for="active" v-text="i18n('common.active')"></label>
                            <p v-text="i18n('webhook.config.active.help')"></p>
                        </div>
                        <div class="form-checkout">
                            <button class="btn btn-primary float-right"
                                    @click="save"
                                    v-text="i18n('common.button.save')">
                            </button>
                        </div>
                    </dd>
                </dl>
            </div>
        </div>

        <div class="webhook-activation">
            <div class="subhead">
                <h2 v-text="i18n('webhook.recent.activation')"></h2>
            </div>
            <ul class="p-0 list-group">
                <li v-if="!hasActivations" class="w-100 no-data list-group-item list-group-item-light border-0" v-text="i18n('common.message.noData')"></li>
                <li v-for="(activation, index) in activations" class="list-group-item list-group-item-light">
                    <i v-if="isSuccess(activation.statusCode)" class="fa fa-check"></i>
                    <i v-else class="fa fa-exclamation"></i>
                    <span class="pointer-cursor" v-text="activation.uuid" @click="toggleDetail(activation)"></span>
                    <span class="createdTime float-right">{{ activation.createdAt | dateFormat('YYYY-MM-DD HH:mm') }}</span>
                    <div v-show="activation.showDetail" class="activation-detail mt-4">
                        <nav>
                            <div class="nav nav-tabs" role="tablist">
                                <a class="nav-item nav-link active" :id="`nav-request-tab-${index}`" data-toggle="tab" :href="`#nav-request-${index}`"
                                   role="tab" aria-controls="nav-request" aria-selected="true" v-text="i18n('common.request')">Request</a>
                                <a class="nav-item nav-link" :id="`nav-response-tab-${index}`" data-toggle="tab" :href="`#nav-response-${index}`"
                                   role="tab" aria-controls="nav-response" aria-selected="false">
                                    {{ i18n('common.response') }} <span class="activation-response-status"
                                                   :class="{'success' : isSuccess(activation.statusCode)}"
                                                   v-text="activation.statusCode"></span>
                                </a>
                            </div>
                        </nav>
                        <div class="tab-content mt-3">
                            <div class="tab-pane show active" :id="`nav-request-${index}`"
                                 role="tabpanel" :aria-labelledby="`nav-request-tab-${index}`">
                                <label>Payload</label>
                                <pre v-text="activation.requestPayload"></pre>
                            </div>
                            <div class="tab-pane" :id="`nav-response-${index}`"
                                 role="tabpanel" :aria-labelledby="`nav-response-tab-${index}`">
                                <label>Header</label>
                                <pre v-text="activation.responseHeader"></pre>
                                <label>Body</label>
                                <pre v-text="activation.responseBody"></pre>
                            </div>
                        </div>
                    </div>
                </li>
            </ul>
            <div v-if="hasActivations">
                <button class="btn btn-default w-100 btn-load-more" @click="loadMore" v-text="i18n('common.button.loadMore')"></button>
            </div>
        </div>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';

    import MessagesMixin from '../common/mixin/MessagesMixin.vue';
    import Base from '../Base.vue';

    @Component({
        name: 'webhookSettings',
        $_veeValidate: {
            validator: 'new',
        },
    })
    export default class WebhookSettings extends Mixins(Base, MessagesMixin) {
        VALIDATION_RETRY_MILLISECOND = 2000;

        eventStart = false;
        eventFinish = false;

        activations = [];

        config = {
            payloadUrl: '',
            contentType: 'JSON',
            active: true,
            events: '',
            creatorId: '',
        };

        activationPage = 0;
        $popoverElements;

        created() {
            this.loadWebhookConfig();
            this.loadMore();
        }

        mounted() {
            this.$popoverElements = $('[data-toggle="popover"]');
            this.$popoverElements.popover();
        }

        save() {
            this.$validator.validate('payloadUrl').then(result => {
                if (result) {
                    this.config.events = this.getEventToken();
                    this.config.creatorId = this.ngrinder.currentUser.factualUser.id;

                    this.$http.post('/webhook/api/', this.config)
                        .then(() => this.showSuccessMsg('save successfully'))
                        .catch(() => this.showErrorMsg('save failed'));

                    return;
                }
                this.$refs.payloadUrl.focus();
            });
        }

        getEventToken() {
            let events = '';
            if (this.eventStart) {
                events += 'START,';
            }

            if (this.eventFinish) {
                events += 'FINISH';
            }

            if (events.endsWith(',')) {
                events = events.slice(0, -1);
            }
            return events;
        }

        loadWebhookConfig() {
            this.$http.get('/webhook/api').then(res => {
                Object.assign(this.config, res.data);
                if (this.config.events.includes('START')) {
                    this.eventStart = true;
                }
                if (this.config.events.includes('FINISH')) {
                    this.eventFinish = true;
                }
            });
        }

        loadMore() {
            this.$http.get('/webhook/api/activation', this.getActivationRequestParams).then(res => {
                res.data = res.data.map(activation => this.parseActivation(activation));
                this.activations = this.activations.concat(res.data);
                this.activationPage++;
            });
        }

        parseActivation(activation) {
            activation.responseBody = 'No Contents';

            if (this.isJSONString(activation.response)) {
                const response = JSON.parse(activation.response);

                if (response.body) {
                    activation.responseBody = response.body;

                    if (this.isJSONString(response.body)) {
                        activation.responseBody = this.jsonBeautify(JSON.parse(response.body));
                    }
                }
                activation.statusCode = response.statusCode;
                activation.responseHeader = this.jsonBeautify(response.header);
            } else {
                activation.statusCode = 400;
                activation.responseHeader = '';
            }

            if (this.isJSONString(activation.request)) {
                const request = JSON.parse(activation.request);
                activation.requestPayload = this.jsonBeautify(request);
            }
            return activation;
        }

        isSuccess(statusCode) {
            return statusCode >= 200 && statusCode < 300;
        }

        toggleDetail(activation) {
            if (activation.showDetail === undefined) {
                this.$set(activation, 'showDetail', true);
                return;
            }
            activation.showDetail = !activation.showDetail;
        }

        checkValidation() {
            this.$validator.validate('payloadUrl').then(result => {
                if (result) {
                    this.sendDummyWebhookRequest();
                    return;
                }
                this.showErrorMsg(this.i18n('webhook.message.invalidUrlFormat'));
            });
        }

        sendDummyWebhookRequest() {
            this.$refs.validationBtn.disabled = true;
            this.$popoverElements.popover('hide');

            const params = {
                payloadUrl: this.config.payloadUrl,
                contentType: this.config.contentType,
            };

            this.showProgressBar(this.i18n('script.editor.message.validate'));
            this.$http.post('/webhook/api/validate', params).then(res => {
                if (res.data.success) {
                    this.refreshRecentActivation();
                }
            }).finally(() => {
                this.hideProgressBar();
                setTimeout(() => this.$refs.validationBtn.disabled = false, this.VALIDATION_RETRY_MILLISECOND);
            });
        }

        refreshRecentActivation() {
            this.activationPage = 0;
            this.activations = [];
            this.loadMore();
        }

        isJSONString(str) {
            try {
                JSON.parse(str);
            } catch (e) {
                return false;
            }
            return true;
        }

        jsonBeautify(jsonObject) {
            return JSON.stringify(jsonObject, null, 4);
        }

        get getActivationRequestParams() {
            return {
                params: {
                    creatorId: this.ngrinder.currentUser.factualUser.id,
                    sort: 'id,DESC',
                    'page.page': this.activationPage,
                    'page.size': 10,
                },
            };
        }

        get hasActivations() {
            return this.activations.length > 0;
        }
    }
</script>

<style lang="less" scoped>
    .webhook {
        margin-top: 27px;

        .subhead {
            border-bottom: 1px solid #e1e4e8;
        }

        .webhook-config {
            padding-right: 15px;
            flex-basis: 500px;

            .form-container {
                .form-group {
                    &.hasError {
                        label, input, input::placeholder {
                            color: #d9534f;
                        }

                        input {
                            border: 1px solid #d9534f;
                        }
                    }
                }

                .validationBtn {
                    height: 32px;
                    margin-bottom: 1px;

                    &:disabled {
                        cursor: wait;
                    }
                }

                input, select {
                    height: 32px;
                }

                .required-mark {
                    font-size: 15px;
                    margin-left: 1px;
                }

                .input-payloadUrl-container {
                    height: 45px;

                    #payload-url {
                        width: 396px;
                    }

                    .validation-message {
                        margin-top: 5px !important;
                    }
                }


                .custom-select {
                    width: 245px;
                }

                dl {
                    margin-top: 13px;

                    dt {
                        label {
                            font-size: 14px;
                        }
                    }
                }

                .form-checkout {
                    display: inline-block;
                    width: 230px;
                    padding: 5px 0 5px 30px;

                    label {
                        font-weight: 600;
                    }

                    p {
                        font-weight: 400;
                        color: #586069;
                    }
                }
            }
        }

        .webhook-activation {
            flex: 1;

            ul {
                list-style: none;

                li {
                    margin: 0;
                    padding: 9px;
                    background-color: white;
                    font-size: 13px;
                    border: none;
                    border-bottom: 1px solid #e1e4e8;

                    .createdTime {
                        font-size: 11px;
                    }

                    .fa-check {
                        margin-left: -3px;
                        margin-right: 5px;
                        color: green;
                    }

                    .fa-exclamation {
                        margin-right: 11px;
                        color: red;
                    }
                }
            }

            pre {
                width: 680px;
                padding: 7px 12px;
                word-break: break-all;
                overflow: auto;
                font-size: 13px;
                line-height: 1.5;
                background-color: #f8f8f8;
                border: 1px solid #ddd;
                border-radius: 6px;
            }

            label {
                color: #24292e;
                font-size: 14px;
                font-weight: 600;
            }

            .nav-tabs {
                .nav-link {
                    border-radius: 6px 6px 0 0;
                    padding: 8px 16px;
                    color: #24292e;
                    font-weight: 500;

                    &:hover {
                        &:not(.active) {
                            border-color: transparent transparent transparent;
                        }
                    }

                    &.active {
                        color: #6a737d;
                    }
                }
            }

            .activation {
                height: 25px;

            }

            .activation-response-status {
                display: inline-block;
                padding: 4px 6px 3px;
                margin-left: 4px;
                font-weight: 600;
                font-size: 10px;
                line-height: 1.1;
                color: #fff;
                border: 1px solid transparent;
                border-radius: 6px;
                background-color: #d73a49;

                &.success {
                    background-color: #28a745;
                }
            }

            .no-data {
                text-align: center;
                height: 30px;
                margin-top: 20px;
                font-size: 15px;
                font-weight: 400;
                color: #586069;
            }

            .btn-load-more {
                color: #24292e;
                font-weight: 500;
                height: 35px;
                font-size: 14px;;
                border: 1px solid #ced4da;
                border-radius: 4px;
                margin-top: 10px;
            }
        }

    }
</style>
