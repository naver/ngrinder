<template>
    <form action="/sign_up/save" class="form-horizontal form-horizontal-left" id="user_form" name="user_form" method="POST">
        <fieldset>
            <control-group name="userId" labelMessageKey="user.info.userId" ref="userIdControlGroup" required>
                <input-append name="userId" ref="userId"
                              v-model="user.userId"
                              @validationResult="$refs.userIdControlGroup.handleError($event)"
                              :validationRules="{ required: true, userIdExist: true, regex: /^[a-zA-Z]{1}[a-zA-Z0-9_\.]{3,20}$/ }"
                              message="user.info.userId"/>
            </control-group>

            <control-group name="userName" labelMessageKey="user.info.name" ref="userNameControlGroup" required>
                <input-append name="userName" ref="userName"
                              v-model="user.userName"
                              @validationResult="$refs.userNameControlGroup.handleError($event)"
                              :validationRules="{ required: true, max: 20 }"
                              message="user.info.name"/>
            </control-group>

            <control-group name="email" labelMessageKey="user.info.email" ref="emailControlGroup">
                <input-append name="email" ref="email"
                              v-model="user.email"
                              @validationResult="$refs.emailControlGroup.handleError($event)"
                              :validationRules="{ email: true }"
                              message="user.info.email"/>
            </control-group>

            <control-group name="description" labelMessageKey="common.label.description">
                <textarea cols="30" id="description" name="description"
                          rows="3" title="Description" class="tx_area span4"
                          v-model="user.description"></textarea>
            </control-group>

            <control-group name="mobilePhone" labelMessageKey="user.info.phone" ref="mobilePhoneControlGroup">
                <input-append name="mobilePhone" ref="mobilePhone"
                              v-model="user.mobilePhone"
                              @validationResult="$refs.mobilePhoneControlGroup.handleError($event)"
                              :validationRules="{ regex: /^\+?\d{2,3}-?\d{2,5}(-?\d+)?$/ }"
                              message="user.info.phone"/>
            </control-group>

            <div class="accordion-inner password-container">
                <control-group name="password" labelMessageKey="user.info.pwd" ref="passwordControlGroup" required>
                    <input-append name="password" ref="password"
                                  v-model="user.password"
                                  @validationResult="$refs.passwordControlGroup.handleError($event)"
                                  :validationRules="{ required: true, lengthRange: [6, 15] }"
                                  type="password" message="user.info.pwd"/>
                </control-group>

                <control-group name="confirmPassword" labelMessageKey="user.info.cpwd" ref="confirmPasswordControlGroup" required>
                    <input-append name="confirmPassword" ref="confirmPassword"
                                  v-model="user.confirmPassword"
                                  @validationResult="$refs.confirmPasswordControlGroup.handleError($event)"
                                  :validationRules="{ required: true, lengthRange: [6, 15], confirmed: user.password}"
                                  type="password" message="user.info.cpwd"/>
                </control-group>
            </div>

            <div class="control-group">
                <div class="controls pull-right">
                    <a class="btn btn-success" id="save_user_btn" @click="save" v-text="i18n('user.info.button.saveUser')"></a>
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

    @Component({
        name: 'userInfo',
        components: { ControlGroup, InputAppend },
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
        };

        mounted() {
            $('[data-toggle="popover"]').popover({trigger: 'hover', container: '#user_form'});
            this.$EventBus.$on(this.$Event.RESET_SIGN_UP_MODAL, this.reset);
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

        destroyed() {
            this.initEventBus();
        }
    }
</script>

<style lang="less" scoped>
    #user_form {
        textarea {
            resize: none;
        }

        .control-group {
            margin-bottom: 12px;
        }

        .controls {
            #save_user_btn {
                margin-top: 20px;
            }
        }

        .password-container {
            padding: 9px 0;
        }
    }
</style>
