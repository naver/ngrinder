<template>
    <main>
        <sign-up-modal v-if="ngrinder.config.signUpEnabled" ref="signUpModal" focus="userId"></sign-up-modal>
        <header>
            <vue-headful title="nGrinder Login"/>
            <img :src="`${contextPath}/img/logo_ngrinder_a.png`" width="400" alt="nGrinder logo">
        </header>
        <section>
            <form ref="loginForm" :action="`${contextPath}/form_login`" method="post">
                <fieldset>
                    <div class="login-input-container">
                        <div>
                            <input v-focus type="text" class="form-control d-block" name="j_username" placeholder="User ID">
                            <input type="password" class="form-control" name="j_password" placeholder="Password">
                        </div>
                        <input type="image" :src="`${contextPath}/img/login.gif`" alt="Login" class="login-btn">
                    </div>

                    <div class="prompt">
                        <input type="checkbox" class="checkbox remember-me" name="remember-me">
                        <span>Remember Me</span>
                        <select class="form-control native-language" name="native_language" v-model="userLanguage">
                            <option value="en">English</option>
                            <option value="kr">한국어</option>
                            <option value="cn">中文</option>
                        </select>
                    </div>

                    <div class="prompt">
                        <select class="form-control user-timezone" name="user_timezone" v-model="userTimezone">
                            <option v-for="timezone in timezones" :value="timezone" v-text="timezone"></option>
                        </select>
                    </div>
                    <div v-if="ngrinder.config.signUpEnabled" class="prompt">
                        <a @click="$refs.signUpModal.show()" class="pointer-cursor sign-up-btn">Sign Up</a>
                    </div>
                </fieldset>
            </form>
        </section>
    </main>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import Base from 'Base.vue';
    import vueHeadful from 'vue-headful';
    import jstz from 'jstz';
    import SignUpModal from './user/modal/SignUpModal.vue';
    import MessagesMixin from './common/mixin/MessagesMixin.vue';

    @Component({
        name: 'login',
        components: { vueHeadful, SignUpModal },
    })
    export default class Login extends Mixins(Base, MessagesMixin) {
        userLanguage = 'en';
        userTimezone = jstz.determine().name();
        timezones = [];

        created() {
            // just for enable cache
            this.$http.get('home/api/panel');

            this.getConfig();
            this.getTimezones();
        }

        getTimezones() {
            this.$http.get('home/api/timezones')
                .then(res => this.timezones = res.data)
                .catch(() => this.showErrorMsg(this.i18n('common.message.loading.error')));
        }

        getConfig() {
            this.$http.get('home/api/config')
                .then(res => this.userLanguage = res.data.userLanguage || 'en')
                .catch(() => this.showErrorMsg(this.i18n('common.message.loading.error')));
        }
    }
</script>

<style lang="less" scoped>
    body {
        overflow-y: hidden;
    }

    main {
        header {
            margin-top: 150px;
            text-align: center;
        }

        section {
            height: 635px;
            margin-top: 30px;
            padding-top: 30px;
            background-color: #f5f4f2;

            fieldset {
                border: 0;

                .login-input-container {
                    text-align: center;
                    div {
                        display: inline-block;
                        input {
                            width: 135px;
                            height: 30px;
                            margin-bottom: 5px;
                        }
                    }

                    .login-btn {
                        display: inline-block;
                        border-radius: 2px;
                        margin-left: 20px;
                        vertical-align: top;
                        margin-top: 0;
                    }
                }

                .prompt {
                    font-size: 12px;
                    text-align: center;
                    margin-top: 5px;
                    height: 30px;

                    .form-control {
                        display: inline-block;
                    }

                    .remember-me {
                        position: relative;
                        top: 4px;
                    }

                    .native-language {
                        margin-left: 60px;
                        width: 85px;
                    }

                    .user-timezone {
                        width: 240px;
                    }

                    .sign-up-btn {
                        color: #08c;
                        margin-left: 200px;
                    }

                    select {
                        width: 75px;
                    }
                }
            }
        }
    }
</style>
