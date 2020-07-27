<template>
    <div v-if="dataLoadFinished" id="user-edit-modal" class="modal fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header align-items-center">
                    <h4 class="modal-title" v-text="modalHeader"></h4>
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                </div>
                <div class="modal-body">
                    <user-info :userProps="user" :config="config" ref="userInfo" @saved="$emit('saved') & hide()"></user-info>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import { Component, Prop } from 'vue-property-decorator';
    import ModalBase from '../../common/modal/ModalBase.vue';
    import UserInfo from '../UserInfo.vue';
    import MessagesMixin from '../../common/mixin/MessagesMixin.vue';

    @Component({
        name: 'userEditModal',
        components: { UserInfo },
    })
    export default class UserEditModal extends Mixins(ModalBase, MessagesMixin) {
        @Prop({ type: String })
        userId;

        user = {};
        config = {};
        dataLoadFinished = false;

        // Override ModalBase's show()
        show() {
            const url = this.userId ? `/user/api/${this.userId}/detail` : '/user/api/profile';
            this.$http.get(url)
                .then(res => this.init(res.data.user, res.data.config))
                .catch(() => this.showErrorMsg(this.i18n('common.message.loading.error')));
        }

        init(user, config) {
            this.user = user;
            this.config = config;
            this.dataLoadFinished = true;

            this.$nextTick(() => {
                $(this.$el).modal('show');
                $(this.$el).on('hidden.bs.modal', () => this.dataLoadFinished = false);
                $(this.$el).on('shown.bs.modal', () => {
                    if (this.$refs.userInfo.$refs[this.focus]) {
                        this.$refs.userInfo.$refs[this.focus].focus();
                    }
                });
            });
        }

        get modalHeader() {
            return this.userId ? this.i18n('user.info.header') : this.i18n('navigator.dropDown.profile.title');
        }
    }

</script>

<style lang="less" scoped>
    #user-edit-modal {
        .modal-dialog {
            margin-top: 150px;
        }

        .modal.fade.in {
            top: 2%;
        }

        .modal-body {
            max-height: 700px;
            padding: 20px;
        }
    }
</style>
