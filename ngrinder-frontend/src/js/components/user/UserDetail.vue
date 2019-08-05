<template>
    <div v-if="dataLoaded" class="container">
        <fieldset>
            <legend class="header border-bottom">
                <span v-text="i18n('user.info.header')"></span>
                <button class="btn btn-success float-right" @click="$router.go(-1)">
                    <i class="fa fa-undo mr-1"></i>
                    <span v-text="i18n('common.button.return')"></span>
                </button>
            </legend>
        </fieldset>
        <user-info :user-props="user" :config="config" @saved="$router.push({ name: 'userList' })"></user-info>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import Base from '../Base.vue';
    import UserInfo from './UserInfo.vue';

    @Component({
        name: 'userDetail',
        props: {
            userId: {
                type: String,
            },
        },
        components: { UserInfo },
    })
    export default class extends Base {
        user = {};
        config = {};
        dataLoaded = false;

        created() {
            // TODO: Assemble allow configs in a config.
            if (this.$route.name === 'createNewUser') {
                this.$http.get('/user/api/new')
                    .then(res => {
                        this.user = res.data.user;
                        this.config = {
                            allowUserIdChange: res.data.allowUserIdChange,
                            allowPasswordChange: res.data.allowPasswordChange,
                            allowRoleChange: res.data.allowRoleChange,
                            roleSet: res.data.roleSet,
                            allowShareChange: res.data.allowShareChange,
                            showPasswordByDefault: res.data.showPasswordByDefault,
                            userSecurityEnabled: res.data.userSecurityEnabled,
                        };
                        this.dataLoaded = true;
                    });
            } else {
                this.$http.get(`/user/api/${this.userId}/detail`)
                    .then(res => {
                        this.user = res.data.user;
                        this.config = {
                            roleSet: res.data.roleSet,
                            allowPasswordChange: res.data.allowPasswordChange,
                            allowRoleChange: res.data.allowRoleChange,
                            allowShareChange: res.data.allowShareChange,
                            showPasswordByDefault: res.data.showPasswordByDefault,
                            userSecurityEnabled: res.data.userSecurityEnabled,
                        };
                        this.dataLoaded = true;
                    });
            }
        }
    }
</script>
