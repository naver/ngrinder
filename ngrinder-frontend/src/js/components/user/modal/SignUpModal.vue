<template>
    <div class="modal fade" id="sign_up_modal">
        <div class="modal-dialog" role="document">
            <div class="modal-content sign-up">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title" v-text="i18n('user.signup.header')"></h4>
                </div>
                <div class="modal-body">
                    <user-info ref="userInfo"></user-info>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import Base from '../../Base.vue';
    import Component from 'vue-class-component';
    import UserInfo from '../UserInfo.vue'
    import { Validator } from 'vee-validate'

    @Component({
        name: 'signUpModal',
        components: { UserInfo },
    })
    export default class SignUpModal extends Base {

        mounted() {
            this.setCustomValidationRules();
            this.setCustomValidationMessages();
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

<style lang="less" scoped>
    #sign_up_modal {

        display: none;

        .modal.fade.in {
            top: 2%;
        }

        .modal-body {
            max-height: 740px;
            padding-left: 30px;
        }
    }
</style>
