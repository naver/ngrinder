<template>
    <form class="form-horizontal form-horizontal-left user-form">
        <fieldset>
            <control-group :class="{error: errors.has('userId')}" name="userId" labelMessageKey="user.info.userId" required>
                <input-append name="userId" ref="userId"
                              v-model="user.userId"
                              :readonly="!config.allowUserIdChange"
                              :validationRules="{ required: true, userIdExist: !!config.allowUserIdChange, regex: /^[a-zA-Z]{1}[a-zA-Z0-9_\.]{3,20}$/ }"
                              message="user.info.userId"/>
            </control-group>

            <control-group :class="{error: errors.has('userName')}" name="userName" labelMessageKey="user.info.name" required>
                <input-append name="userName" ref="userName"
                              v-model="user.userName"
                              :validationRules="{ required: true, max: 20 }"
                              message="user.info.name"/>
            </control-group>

            <control-group v-if="config.allowRoleChange" name="role" labelMessageKey="user.info.role">
                <select v-model="user.role" name="role">
                    <option v-for="role in config.roleSet" :value="role"
                            v-text="role.fullName" :selected="user.role === role">
                    </option>
                </select>
            </control-group>

            <control-group :class="{error: errors.has('email')}" name="email" labelMessageKey="user.info.email">
                <input-append name="email" ref="email"
                              v-model="user.email"
                              :validationRules="{ email: true }"
                              message="user.info.email"/>
            </control-group>

            <control-group name="description" labelMessageKey="common.label.description">
                <textarea cols="30" name="description"
                          rows="3" title="Description" class="tx_area span4"
                          v-model="user.description"></textarea>
            </control-group>

            <control-group :class="{error: errors.has('mobilePhone')}" name="mobilePhone" labelMessageKey="user.info.phone">
                <input-append name="mobilePhone" ref="mobilePhone"
                              errStyle="width: 285px;"
                              v-model="user.mobilePhone"
                              :validationRules="{ regex: /^\+?\d{2,3}-?\d{2,5}(-?\d+)?$/ }"
                              message="user.info.phone"/>
            </control-group>

            <control-group v-if="config.allowShareChange" labelMessageKey="user.share.title">
                <select2 v-model="user.followersStr" type="input" name="followersStr" :option="followerSelect2Option" customStyle="width: 285px;"></select2>
            </control-group>

            <div v-if="!config.showPasswordByDefault" class="accordion-heading">
                <a @click="displayPasswordField = !displayPasswordField" class="pointer-cursor" v-text="i18n('user.info.button.changePwd')"></a>
            </div>

            <template v-if="config.allowPasswordChange">
                <div v-show="displayPasswordField" class="accordion-inner password-container">
                    <control-group :class="{error: errors.has('password')}" name="password" labelMessageKey="user.info.pwd" :required="config.showPasswordByDefault">
                        <input-append name="password" ref="password"
                                      v-model="user.password"
                                      :validationRules="{ required: config.showPasswordByDefault, lengthRange: [6, 15] }"
                                      type="password" message="user.info.pwd"/>
                    </control-group>

                    <control-group :class="{error: errors.has('confirmPassword')}" name="confirmPassword" labelMessageKey="user.info.cpwd" :required="config.showPasswordByDefault">
                        <input-append name="confirmPassword" ref="confirmPassword"
                                      v-model="user.confirmPassword"
                                      :validationRules="{ required: config.showPasswordByDefault, lengthRange: [6, 15], confirmed: user.password}"
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
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import Base from '../Base.vue';
    import ControlGroup from '../common/ControlGroup.vue';
    import InputAppend from '../common/InputAppend.vue';
    import Select2 from '../common/Select2.vue';
    import MessagesMixin from '../common/mixin/MessagesMixin.vue';
    import userDescription from '../common/filter/UserDescriptionFilter';

    @Component({
        name: 'userInfo',
        props: {
            userProps: {
                type: Object,
                required: true,
            },
            config: {
                type: Object,
                required: true,
            },
            type: {
                type: String,
                default: 'save',
            },
        },
        components: { ControlGroup, InputAppend, Select2 },
        $_veeValidate: {
            validator: 'new',
        },
    })
    export default class UserInfo extends Mixins(Base, MessagesMixin) {
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
        formUrl = '/user/api/save';

        created() {
            delete this.userProps.password;
            Object.assign(this.user, this.userProps);
            if (this.user.followers) {
                this.user.followersStr = this.user.followers.map(user => user.userId).join(',');
            }

            this.displayPasswordField = this.config.showPasswordByDefault;

            this.setCustomValidationRules();
            if (this.type === 'signUp') {
                this.formUrl = '/sign_up/api/save';
            }

            if (this.config.allowShareChange) {
                this.followerSelect2Option = {
                    multiple: true,
                    minimumInputLength: 3,
                    ajax: {
                        url: '/user/api/search',
                        dataType: 'json',
                        data: (term, page) => ({
                                keywords: term,
                                pageNumber: page,
                                pageSize: 10,
                        }),
                        results: users => {
                            const select2Data = users.map(user => ({
                                id: user.userId,
                                text: userDescription(user),
                            }));
                            return { results: select2Data };
                        },
                    },
                    initSelection: this.initSelection,
                    formatSelection: data => data.text,
                };
            }
        }

        mounted() {
            $('[data-toggle="popover"]').popover({ trigger: 'hover', container: '#user_form' });
        }

        initSelection(element, callback) {
            const data = [];
            if (this.user.followers) {
                this.user.followers.forEach(follower => data.push({ id: follower.userId, text: userDescription(follower) }));
            }
            element.val('');
            callback(data);
        }

        reset() {
            for (const prop in this.user) {
                this.user[prop] = '';
            }
            this.$nextTick(() => this.$validator.validateAll());
        }

        save() {
            this.$validator.validateAll().then(result => {
                if (result) {
                    this.$http.post(this.formUrl, this.user)
                        .then(() => this.$emit('saved'))
                        .catch(() => this.showErrorMsg(this.i18n('user.message.save.error')));
                }
            });
        }

        setCustomValidationRules() {
            this.$validator.extend('confirmed', {
                getMessage: this.i18n('user.info.cpwd.help'),
                validate: (confirmPassword, [password]) => confirmPassword === password,
            });

            this.$validator.extend('lengthRange', {
                getMessage: (name, val) => this.i18n('common.message.validate.rangeLength', { minLength: val[0], maxLength: val[1] }),
                validate: (value, [min, max]) => Number(min) <= value.length && Number(max) >= value.length,
            });

            this.$validator.extend('userIdExist', {
                getMessage: this.i18n('user.info.userId.exist'),
                validate: userId => {
                    if (userId && userId.length > 0) {
                        return this.$http.get(`/sign_up/api/${userId}/check_duplication`)
                            .then(res => res.data.success);
                    } else {
                        return false;
                    }
                },
            });
        }
    }
</script>

<style lang="less">
    #ngrinder {
        .user-form {
            .input-group {
                input {
                    width: 286px;
                    border-radius: 4px;
                }
            }
        }
    }
</style>

<style lang="less" scoped>
    .user-form {
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
