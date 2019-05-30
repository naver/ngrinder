<template>
    <div v-if="dataLoadFinished" id="user-profile-modal" class="modal fade" role="dialog">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h4 class="modal-title" v-text="i18n('navigator.dropDown.profile.title')"></h4>
        </div>
        <div class="modal-body">
            <user-info :info="info" ref="userInfo"></user-info>
        </div>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import ModalBase from '../../common/modal/ModalBase.vue';
    import UserInfo from '../UserInfo.vue';

    @Component({
        name: 'userProfileModal',
        components: { UserInfo },
    })
    export default class UserProfileModal extends ModalBase {
        info = {};
        dataLoadFinished = false;

        created() {
            this.$http.get('/user/api/profile').then(res => {
                this.info = res.data;
                this.dataLoadFinished = true;
            }).catch((error) => console.error(error));
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
