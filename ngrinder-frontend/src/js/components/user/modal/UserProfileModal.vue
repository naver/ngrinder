<template>
    <div v-if="dataLoadFinished" id="user-profile-modal" class="modal fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header align-items-center">
                    <h4 class="modal-title" v-text="i18n('navigator.dropDown.profile.title')"></h4>
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                </div>
                <div class="modal-body">
                    <user-info :userProps="user" :config="config" ref="userInfo" @saved="hide"></user-info>
                </div>
            </div>
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
                this.config = res.data.config;
                this.dataLoadFinished = true;
                this.$nextTick(() => {
                    this.show();
                    $(this.$el).on('hidden.bs.modal', () => this.$emit('hidden'));
                });
            }).catch(() => this.showErrorMsg(this.i18n('common.message.loading.error')));
        }
    }

</script>

<style lang="less" scoped>
    #user-profile-modal {
        .modal-dialog {
            margin-top: 80px;
        }

        .modal.fade.in {
            top: 2%;
        }

        .modal-body {
            max-height: 640px;
            padding: 20px;
        }
    }
</style>
