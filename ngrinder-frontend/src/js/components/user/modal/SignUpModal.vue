<template>
    <div v-if="dataLoadFinished" class="modal fade" id="sign-up-modal">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header align-items-center">
                    <h4 class="modal-title" v-text="modalHeader"></h4>
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                </div>
                <div class="modal-body">
                    <user-info :base-path="basePath" :userProps="user" :config="config" ref="userInfo" @saved="$emit('saved') & hide()"></user-info>
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
            this.$http.get(`${this.basePath}/api/new`).then(res => {
                this.user = res.data.user;
                this.config = res.data.config;
                this.dataLoadFinished = true;

                this.$nextTick(() => {
                    $(this.$el).on('hide.bs.modal', this.$refs.userInfo.reset);
                    $(this.$el).on('shown.bs.modal', () => {
                        if (this.$refs.userInfo.$refs[this.focus]) {
                            this.$refs.userInfo.$refs[this.focus].focus();
                        }
                    });
                });
            }).catch(() => this.showErrorMsg(this.i18n('common.message.loading.error')));
        }

        get basePath() {
            return this.$route.name === 'login' ? '/sign_up' : '/user';
        }

        get modalHeader() {
            return this.$route.name === 'login' ? this.i18n('user.signup.header') : this.i18n('user.info.header');
        }
    }

</script>

<style lang="less" scoped>
    #sign-up-modal {
        display: none;

        .modal.fade.in {
            top: 2%;
        }

        .modal-dialog {
            margin-top: 100px;
        }

        .modal-body {
            max-height: 740px;
            padding-left: 30px;
        }
    }
</style>
