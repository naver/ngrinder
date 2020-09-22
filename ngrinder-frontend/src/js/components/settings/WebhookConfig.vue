<template>
    <div class="webhook">
        <div class="subhead">
            <h2>Webhook Settings</h2>
        </div>
        <div class="webhook-config d-flex">
            <div class="form-container">
                <dl class="form-group" :class="{'hasError': errors.has('payloadUrl')}">
                    <dt>
                        <label for="payload-url">Payload URL</label>
                        <span class="required-mark">*</span>
                    </dt>
                    <dd class="input-payloadUrl-container">
                        <input id="payload-url"
                               class="form-control"
                               type="text"
                               name="payloadUrl"
                               ref="payloadUrl"
                               placeholder="http://www.target.com"
                               v-validate="{ required: true, url: {require_protocol: true} }"
                               v-model="config.payloadUrl"/>
                        <div v-show="errors.has('payloadUrl')" class="validation-message" v-text="errors.first('payloadUrl')"></div>
                    </dd>
                </dl>
                <dl class="form-group">
                    <dt>
                        <label for="content-type">Content-Type</label>
                    </dt>
                    <dd>
                        <select id="content-type" class="custom-select" v-model="config.contentType">
                            <option value="JSON">application/json</option>
                            <option value="FORM_URL_ENCODED">application/x-www-form-urlencoded</option>
                        </select>
                    </dd>
                </dl>
                <button class="btn btn-primary mt-3" @click="save">Save</button>
            </div>
            <div class="form-container">
                <dl class="form-group mb-0">
                    <dt>
                        <label>Event</label>
                    </dt>
                    <dd>
                        <div class="form-checkout custom-control custom-checkbox">
                            <input id="event-start" type="checkbox" class="custom-control-input" v-model="eventStart">
                            <label class="custom-control-label" for="event-start">Start</label>
                            <p>You can check this option if you want to trigger a hook at the start of the test</p>
                        </div>
                        <div class="form-checkout custom-control custom-checkbox">
                            <input id="event-finish" type="checkbox" class="custom-control-input" v-model="eventFinish">
                            <label class="custom-control-label" for="event-finish">Finish</label>
                            <p>You can check this option if you want to trigger a hook at the finish of the test</p>
                        </div>
                    </dd>
                </dl>
                <dl class="form-group m-0">
                    <dt>
                        <label>Activation</label>
                    </dt>
                    <dd>
                        <div class="form-checkout custom-control custom-checkbox">
                            <input id="active" type="checkbox" class="custom-control-input" v-model="config.active">
                            <label class="custom-control-label" for="active">Active</label>
                            <p>We will send webhook request if this option is checked</p>
                        </div>
                    </dd>
                </dl>
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
        name: 'webhookConfig',
        $_veeValidate: {
            validator: 'new',
        },
    })
    export default class WebhookConfig extends Mixins(Base, MessagesMixin) {
        eventStart = false;
        eventFinish = false;

        config = {
            payloadUrl: '',
            contentType: 'JSON',
            active: true,
            events: '',
            createdUserId: '',
        };

        created() {
            this.$http.get('/webhook/api').then(res => {
                Object.assign(this.config, res.data);
                if (this.config.events.includes('START')) {
                    this.eventStart = true;
                }
                if (this.config.events.includes('FINISH')) {
                    this.eventFinish = true;
                }
            });
            this.loadMore();
        }

        save() {
            this.$validator.validate('payloadUrl').then(result => {
                if (result) {
                    this.config.events = this.getEventToken();
                    this.config.createdUserId = this.ngrinder.currentUser.factualUser.id;

                    this.$http.post(`/webhook/api/`, this.config)
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
    }
</script>

<style lang="less" scoped>
    .webhook {
        margin-top: 27px;

        .subhead {
            border-bottom: 1px solid #e1e4e8;
        }

        .webhook-config {
            padding: 5px 22px;
            border-radius: 4px;
            margin-top: 11px;

            .form-container {
                display: inline-block;
                flex: 1;

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
                        width: 400px;
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
    }
</style>
