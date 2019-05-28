<template>
    <form :action="formUrl" class="form-horizontal form-horizontal-left" id="user_form" name="user_form" method="POST">
        <fieldset>
            <control-group name="userId" labelMessageKey="user.info.userId" ref="userIdControlGroup" required>
                <input-append name="userId" ref="userId"
                              v-model="user.userId"
                              @validationResult="$refs.userIdControlGroup.handleError($event)"
                              :readonly="type === 'save'"
                              :validationRules="{ required: true, userIdExist: true, regex: /^[a-zA-Z]{1}[a-zA-Z0-9_\.]{3,20}$/ }"
                              message="user.info.userId"/>
                <input type="hidden" name="id" :value="user.id"/>
            </control-group>

            <control-group name="userName" labelMessageKey="user.info.name" ref="userNameControlGroup" required>
                <input-append name="userName" ref="userName"
                              v-model="user.userName"
                              @validationResult="$refs.userNameControlGroup.handleError($event)"
                              :validationRules="{ required: true, max: 20 }"
                              message="user.info.name"/>
            </control-group>

            <control-group v-if="info.allowRoleChange" name="role" labelMessageKey="user.info.role">
                <select v-model="user.role" name="role">
                    <option v-for="role in info.roleSet" :value="role"
                            v-text="role.fullName" :selected="user.role === role">
                    </option>
                </select>
            </control-group>

            <control-group name="email" labelMessageKey="user.info.email" ref="emailControlGroup">
                <input-append name="email" ref="email"
                              v-model="user.email"
                              @validationResult="$refs.emailControlGroup.handleError($event)"
                              :validationRules="{ email: true }"
                              message="user.info.email"/>
            </control-group>

            <control-group name="description" labelMessageKey="common.label.description">
                <textarea cols="30" name="description"
                          rows="3" title="Description" class="tx_area span4"
                          v-model="user.description"></textarea>
            </control-group>

            <control-group name="mobilePhone" labelMessageKey="user.info.phone" ref="mobilePhoneControlGroup">
                <input-append name="mobilePhone" ref="mobilePhone"
                              errStyle="width: 285px; white-space: normal;"
                              v-model="user.mobilePhone"
                              @validationResult="$refs.mobilePhoneControlGroup.handleError($event)"
                              :validationRules="{ regex: /^\+?\d{2,3}-?\d{2,5}(-?\d+)?$/ }"
                              message="user.info.phone"/>
            </control-group>

            <control-group v-if="info.allowShareChange" labelMessageKey="user.share.title">
                <select2 v-model="user.followersStr" type="input" name="followersStr" :option="followerSelect2Option" customStyle="width: 285px;"></select2>
            </control-group>

            <template v-if="info.allowPasswordChange">
                <div v-if="!info.showPasswordByDefault" class="accordion-heading">
                    <a @click="displayPasswordField = !displayPasswordField" class="pointer-cursor" v-text="i18n('user.info.button.changePwd')"></a>
                </div>

                <div v-show="displayPasswordField" class="accordion-inner password-container">
                    <control-group name="password" labelMessageKey="user.info.pwd" ref="passwordControlGroup" :required="info.showPasswordByDefault">
                        <input-append name="password" ref="password"
                                      v-model="user.password"
                                      @validationResult="$refs.passwordControlGroup.handleError($event)"
                                      :validationRules="{ required: info.showPasswordByDefault, lengthRange: [6, 15] }"
                                      type="password" message="user.info.pwd"/>
                    </control-group>

                    <control-group name="confirmPassword" labelMessageKey="user.info.cpwd" ref="confirmPasswordControlGroup" :required="info.showPasswordByDefault">
                        <input-append name="confirmPassword" ref="confirmPassword"
                                      v-model="user.confirmPassword"
                                      @validationResult="$refs.confirmPasswordControlGroup.handleError($event)"
                                      :validationRules="{ required: info.showPasswordByDefault, lengthRange: [6, 15], confirmed: user.password}"
                                      type="password" message="user.info.cpwd"/>
                    </control-group>
                </div>
            </template>

            <div class="control-group">
                <div class="controls pull-right">
                    <a class="btn btn-success save-user-btn" @click="save" v-text="i18n('user.info.button.saveUser')"></a>
                </div>
            </div>
        </fieldset>
    </form>
</template>

<script>
    import Base from '../Base.vue';
    import Component from 'vue-class-component';
    import ControlGroup from '../common/ControlGroup.vue'
    import InputAppend from '../common/InputAppend.vue'
    import Select2 from '../common/Select2.vue';
    import { Validator } from 'vee-validate'

    @Component({
        name: 'userInfo',
        props: {
            info: {
                type: Object,
                default: () => { return {}; },
            },
            type: {
                type: String,
                default: 'save',
            }
        },
        components: { ControlGroup, InputAppend, Select2 },
    })
    export default class UserInfo extends Base {
        user = {
            userId: '',
            userName: '',
            email: '',
            description: '',
            mobilePhone: '',
            password: '',
            confirmPassword: '',
            followersStr: '',
        };

        displayPasswordField = true;
        followerSelect2Option = {};

        formUrl = '/user/save';

        created() {
            this.setCustomValidationRules();
            this.setCustomValidationMessages();

            delete this.info.user.password;
            Object.assign(this.user, this.info.user);

            this.displayPasswordField = this.info.showPasswordByDefault;

            if (this.type === 'signUp') {
                this.formUrl = '/sign_up/save';
            }

            if (this.info.allowShareChange) {
                this.followerSelect2Option = {
                    multiple: true,
                    minimumInputLength: 3,
                    ajax: {
                        url: '/user/api/search',
                        dataType: 'json',
                        data: (term, page) => {
                            return {
                                keywords: term,
                                pageNumber: page,
                                pageSize: 10,
                            }
                        },
                        results: (data) => {
                            return { results: data };
                        },
                    },
                    initSelection: this.initSelection,
                    formatSelection: (data) => data.text,
                }
            }
        }

        mounted() {
            $('[data-toggle="popover"]').popover({trigger: 'hover', container: '#user_form'});
            this.$EventBus.$on(this.$Event.RESET_SIGN_UP_MODAL, this.reset);
        }

        destroyed() {
            this.initEventBus();
        }

        initSelection(element, callback) {
            let data = [];
            if (this.info.followers) {
                this.info.followers.forEach(follower => data.push({id: follower.id, text: follower.text}));
            }
            element.val('');
            callback(data);
        }

        reset() {
            for (let prop in this.user) {
                this.user[prop] = '';
            }
            this.$nextTick(() => this.$EventBus.$emit(this.$Event.SIGN_UP_FORM_VALIDATION_CHECK));
        }

        save() {
            if (this.hasValidationError()) {
                return;
            }
            document.forms.user_form.submit();
        }

        hasValidationError() {
            return this.$refs.userId.errors.any()
                || this.$refs.userName.errors.any()
                || this.$refs.email.errors.any()
                || this.$refs.mobilePhone.errors.any()
                || this.$refs.password.errors.any()
                || this.$refs.confirmPassword.errors.any();
        }

        setCustomValidationRules() {
            this.$validator.extend('confirmed', {
                getMessage: this.i18n('user.info.cpwd.help'),
                validate: (confirmPassword, [password]) => confirmPassword === password,
            });

            this.$validator.extend('lengthRange', {
                getMessage: (name, val) => this.i18n('common.message.validate.rangeLength', {minLength: val[0], maxLength: val[1]}),
                validate: (value, [min, max]) => Number(min) <= value.length && Number(max) >= value.length,
            });

            this.$validator.extend('userIdExist', {
                getMessage: this.i18n('user.info.userId.exist'),
                validate: userId => {
                    if (userId && userId.length > 0) {
                        return this.$http.get(`/sign_up/api/${userId}/check_duplication`)
                            .then(res => res.data.success)
                            .catch(error => console.error(error));
                    } else {
                        return false;
                    }
                },
            });
        }

        setCustomValidationMessages() {
            const dictionary = {
                required: () => this.i18n('common.message.validate.empty'),
                regex: (name) => name === 'userId' ? this.i18n('user.info.userId.help') : this.i18n('user.info.phone.help'),
                email: () => this.i18n('user.info.email.help'),
                max: (name, val) => this.i18n('common.message.validate.maxLength', {maxLength: val[0]}),
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
    }
</script>

<style lang="less">
    #user_form {
        .input-group {
            input {
                width: 286px;
                border-radius: 4px;
            }
        }
    }
</style>

<style lang="less" scoped>
    #user_form {
        textarea {
            resize: none;
        }

        .control-group {
            margin-bottom: 12px;
        }

        .controls {
            .save-user-btn {
                margin-top: 20px;
            }
        }

        .password-container {
            padding: 9px 0;
        }
    }
</style>
