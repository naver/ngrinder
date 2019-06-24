<template>
    <div v-if="dataLoadFinished" class="modal fade" id="sign-up-modal">
        <div class="modal-dialog" role="document">
            <div class="modal-content sign-up">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title" v-text="i18n('user.signup.header')"></h4>
                </div>
                <div class="modal-body">
                    <user-info :type="'signUp'" :userProps="user" :config="config" ref="userInfo" @saved="hide"></user-info>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import ModalBase from '../../common/modal/ModalBase.vue';
    import MessagesMixin from '../../common/mixin/MessagesMixin.vue';
    import UserInfo from '../UserInfo.vue';

    @Component({
        name: 'signUpModal',
        components: { UserInfo },
    })
    export default class SignUpModal extends Mixins(ModalBase, MessagesMixin) {
        user = {};
        config = {};
        dataLoadFinished = false;

        created() {
            this.$http.get('/sign_up/api/new').then(res => {
                this.user = res.data.user;
                this.config = {
                    newUser: res.data.newUser,
                    roleSet: res.data.roleSet,
                    allowPasswordChange: res.data.allowPasswordChange,
                    allowRoleChange: res.data.allowRoleChange,
                    allowShareChange: res.data.allowShareChange,
                    allowUserIdChange: res.data.allowUserIdChange,
                    showPasswordByDefault: res.data.showPasswordByDefault,
                    userSecurityEnabled: res.data.userSecurityEnabled,
                };
                this.dataLoadFinished = true;
            }).catch(() => this.showErrorMsg(this.i18n('common.message.loading.error')));
        }

        reset() {
            this.$refs.userInfo.reset();
        }
    }

</script>

<style lang="less" scoped>
    #sign-up-modal {
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
