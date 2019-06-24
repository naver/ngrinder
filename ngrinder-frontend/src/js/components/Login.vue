<template>
    <div id="login-container">
        <vue-headful title="Login"/>
        <sign-up-modal v-if="ngrinder.config.signUpEnabled" ref="signUpModal"></sign-up-modal>
        <div class="logo">
            <img src="/img/logo_ngrinder_a.png" width="400" alt="nGrinder logo">
        </div>
        <div class="content">
            <form ref="loginForm" action="/form_login" method="post">
                <fieldset>
                    <div class="login">
                        <div class="lgn_ipt">
                            <input v-focus type="text" class="span2 input" name="j_username" placeholder="User ID"><br>
                            <input type="password" class="span2 input" name="j_password" placeholder="Password">
                        </div>
                        <input id="loginBtn" type="image" src="/img/login.gif" alt="Login" class="btn_lgn">
                    </div>

                    <div class="prompt">
                        <input id="remember_me" type="checkbox" class="checkbox" name='_spring_security_remember_me'>
                        <span>Remember Me</span>
                        <select id="native_language" name="native_language" v-model="userLanguage">
                            <option value="en">English</option>
                            <option value="kr">한국어</option>
                            <option value="cn">中文</option>
                        </select>
                    </div>

                    <div class="prompt">
                        <select id="user_timezone" name="user_timezone" v-model="userTimezone">
                            <option v-for="timezone in timezones" :value="timezone" v-text="timezone"></option>
                        </select>
                    </div>
                    <div v-if="ngrinder.config.signUpEnabled" class="prompt">
                        <a @click="$refs.signUpModal.show()" class="pointer-cursor sign-up-btn">Sign Up</a>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
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
                .then(res => this.userLanguage = res.data.userLanguage)
                .catch(() => this.showErrorMsg(this.i18n('common.message.loading.error')));
        }
    }
</script>

<style lang="less" scoped>

    body {
        overflow-y: hidden;
    }

    #login-container {
        .logo {
            margin-top: 150px;
            text-align: center;
        }

        .content {
            height: 635px;
            margin-top: 30px;
            padding-top: 30px;
            background-color: #f5f4f2;

            fieldset {
                border: 0;

                .lgn_ipt {
                    display: inline-block;
                    input {
                        height: 30px;
                        margin-bottom: 5px;
                    }
                }

                .login {
                    text-align: center;
                    input.span2 {
                        border: 1px solid #e0e0e0;
                        width: 135px;
                    }
                }

                .btn_lgn {
                    display: inline-block;
                    margin-left: 20px;
                    vertical-align: top;
                    margin-top: 0;
                }

                .prompt {
                    text-align: center;
                    margin-top: 5px;
                    height: 30px;

                    #remember_me {
                        margin-top: -4px;
                    }

                    #native_language {
                        margin-left: 60px;
                        width: 80px;
                    }

                    #user_timezone {
                        width: 240px;
                    }

                    .sign-up-btn {
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
