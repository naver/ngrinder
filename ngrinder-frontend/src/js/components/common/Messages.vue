<template>
    <div id="message-container">
        <transition name="fade">
            <div class="alert message-div"
                 :class="msgClass"
                 v-show="showMsgDiv"
                 v-text="alertMessage"></div>
        </transition>
        <transition name="fade">
            <div class="alert alert-error message-div error-message-div"
                 v-show="showErrMsgDiv">
                <button class="close" @click="close">&times;</button>
                <h4 class="alert-heading">ERROR</h4>
                <span class="error-message" v-text="errMessage"></span>
            </div>
        </transition>
        <transition name="fade">
            <div class="message-div progress progress-striped progress-bar active" v-show="showPrgDiv">
                <div class="bar" v-text="progressMessage"></div>
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

        showErrorMsg(msg) {
            msg = msg || '';
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
         .bar {
             width: 100%;
         }
    }
</style>

<style>
    .fade-enter-active, .fade-leave-active {
        transition: opacity .1s
    }

    .fade-enter, .fade-leave-to {
        opacity: 0
    }
</style>
