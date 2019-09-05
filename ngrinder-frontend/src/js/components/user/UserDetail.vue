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
        <user-info :user-props="user" :config="config" @saved="showSuccessMsg(i18n('user.message.save.success'))"></user-info>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import Base from '../Base.vue';
    import UserInfo from './UserInfo.vue';
    import MessageMixin from '../common/mixin/MessagesMixin.vue';

    @Component({
        name: 'userDetail',
        props: {
            userId: {
                type: String,
            },
        },
        components: { UserInfo },
    })
    export default class extends Mixins(Base, MessageMixin) {
        user = {};
        config = {};
        dataLoaded = false;

        created() {
            const url = this.$route.name === 'createNewUser' ? '/user/api/new' : `/user/api/${this.userId}/detail`;

            this.$http.get(url).then(res => {
                this.user = res.data.user;
                this.config = res.data.config;
                this.dataLoaded = true;
            });
        }
    }
</script>
