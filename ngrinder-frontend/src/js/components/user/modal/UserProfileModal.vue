<template>
    <div v-if="dataLoadFinished" id="user-profile-modal" class="modal fade" role="dialog">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h4 class="modal-title" v-text="i18n('navigator.dropDown.profile.title')"></h4>
        </div>
        <div class="modal-body">
            <user-info :userProps="user" :config="config" ref="userInfo" @saved="hide"></user-info>
        </div>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import ModalBase from '../../common/modal/ModalBase.vue';
    import UserInfo from '../UserInfo.vue';
    import MessagesMixin from '../../common/mixin/MessagesMixin.vue';

    @Component({
        name: 'userProfileModal',
        components: { UserInfo },
    })
    export default class UserProfileModal extends Mixins(ModalBase, MessagesMixin) {
        user = {};
        config = {};
        dataLoadFinished = false;

        created() {
            this.$http.get('/user/api/profile').then(res => {
                this.user = res.data.user;
                this.config = {
                    allowPasswordChange: res.data.allowPasswordChange,
                    allowRoleChange: res.data.allowRoleChange,
                    allowShareChange: res.data.allowShareChange,
                    followers: res.data.followers,
                    owners: res.data.owners,
                    showPasswordByDefault: res.data.showPasswordByDefault,
                    userSecurityEnabled: res.data.userSecurityEnabled,
                };
                this.dataLoadFinished = true;
            }).catch(() => this.showErrorMsg(this.i18n('common.message.loading.error')));
        }
    }

</script>

<style lang="less" scoped>
    #user-profile-modal {
        display: none;

        .modal.fade.in {
            top: 2%;
        }

        .modal-body {
            max-height: 640px;
            padding-left: 45px;
        }
    }
</style>
