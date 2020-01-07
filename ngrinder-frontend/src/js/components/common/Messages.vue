<template>
    <div id="message-container">
        <div class="message-div alert"
             :class="msgClass"
             v-show="showMsgDiv"
             v-text="alertMessage">
        </div>
        <div class="message-div alert alert-danger error-message-div"
             v-show="showErrMsgDiv">
            <button class="close" @click="close">x</button>
            <h4 class="alert-heading">ERROR</h4>
            <div class="error-message" v-html="errMessage"></div>
        </div>
        <transition name="fade">
            <div class="message-div rounded progress-bar progress-bar-striped progress-bar-animated" v-show="showPrgDiv">
                <div class="w-100" v-text="progressMessage"></div>
            </div>
        </transition>
    </div>
</template>

<script>
    import Vue from 'vue';
    import Component from 'vue-class-component';

    @Component({
        name: 'messages',
    })
    export default class Messages extends Vue {
        msgTimeout = {};

        showMsgDiv = false;
        showErrMsgDiv = false;
        showPrgDiv = false;

        msgClass = '';
        progressMessage = '';
        alertMessage = '';
        errMessage = '';

        mounted() {
            $('.error-message').on('click', '.link-git-config', () => {
                this.$router.push('/script/detail/.gitconfig.yml');
                this.close();
            });
        }

        close() {
            this.showErrMsgDiv = false;
            this.errMessage = '';
        }

        showMsg(color, message) {
            message = message || '';
            this.showMsgDiv = false;
            this.msgClass = color;
            this.alertMessage = message;
            this.showMsgDiv = true;

            clearTimeout(this.msgTimeout);
            this.msgTimeout = setTimeout(() => {
                this.showMsgDiv = false;
                this.msgClass = '';
            }, 3000);
        }

        showSuccessMsg(msg) {
            msg = msg || '';
            this.showMsg('alert-success', msg);
        }

        showInfoMsg(msg) {
            msg = msg || '';
            this.showMsg('alert-info', msg);
        }

        showErrorMsg(msg) {
            msg = msg || '';
            msg = msg.replace(/\n/g, '<br>')
                     .replace('.gitconfig.yml', '<span class="link-git-config pointer-cursor">.gitconfig.yml</span>');
            this.showErrMsgDiv = false;
            this.errMessage = msg;
            this.showErrMsgDiv = true;
        }

        showProgressBar(msg) {
            msg = msg || 'Loading...';
            this.showPrgDiv = true;
            this.progressMessage = msg;
        }

        hideProgressBar() {
            this.showPrgDiv = false;
        }
    }
</script>
<style lang="less" scoped>
    .error-message-div {
        z-index: 1152;

        .error-message {
            margin-left: 20px;
        }
    }

    .message-div {
        z-index: 1151;
    }

    .progress-bar {
        height: 20px;
    }
</style>

<style lang="less">
    .error-message {
        .link-git-config {
            color: #007bff;
            text-decoration: underline;
            text-underline-position: under;
        }
    }

    .fade-enter-active, .fade-leave-active {
        transition: opacity .1s;
    }

    .fade-enter, .fade-leave-to {
        opacity: 0;
    }
</style>
