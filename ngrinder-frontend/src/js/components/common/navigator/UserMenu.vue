<template>
    <li class="dropdown">
        <a data-toggle="dropdown" class="dropdown-toggle clickable">
            <span v-text="currentUser.name"></span>
            <b class="caret"></b>
        </a>
        <ul class="dropdown-menu">
            <li>
                <a href="/user/switch?to=" v-text="i18n('common.button.return')"></a>
            </li>
            <li>
                <a id="user_profile_menu" class="clickable" v-text="i18n('navigator.dropDown.profile')"></a>
            </li>
            <li>
                <a id="switch_user_menu" class="clickable" v-text="i18n('navigator.dropDown.switchUser')"></a>
            </li>
            <li class="divider"></li>
            <template v-if="isAdmin">
                <li v-if="config.clustered" class="dropdown-submenu">
                    <a class="clickable" v-text="i18n('navigator.dropDown.downloadAgent')"></a>
                </li>
                <li v-else>
                    <a href="/agent/download" v-text="i18n('navigator.dropDown.downloadAgent')"></a>
                </li>
            </template>
            <template v-if="!isAdmin">
                <li v-if="config.clustered" class="dropdown-submenu"><a class="clickable" v-text="i18n('navigator.dropDown.downloadPrivateAgent')"></a>
                </li>
                <li v-else>
                    <a :href="`/agent/download?owner=${currentUser.id}`" v-text="i18n('navigator.dropDown.downloadPrivateAgent')"></a>
                </li>
            </template>
            <li>
                <a href="/monitor/download" v-text="i18n('navigator.dropDown.downloadMonitor')"></a>
            </li>
            <li>
                <a href="https://github.com/naver/ngrinder/wiki/nGrinder-Recorder-Guide" target="_blank" v-text="i18n('navigator.dropDown.downloadRecorder')"></a>
            </li>
            <template v-if="isAdmin">
                <li class="divider"></li>
                <li><a href="/user/" v-text="i18n('navigator.dropDown.userManagement')"></a></li>
                <li><a href="/agent/" v-text="i18n('navigator.dropDown.agentManagement')"></a></li>
                <li v-if="!config.clustered">
                    <a href="/operation/log" v-text="i18n('navigator.dropDown.logMonitoring')"></a>
                </li>
                <li>
                    <router-link to="/operation/script_console" v-text="i18n('navigator.dropDown.scriptConsole')"></router-link>
                </li>
                <li><router-link to="/operation/system_config" v-text="i18n('navigator.dropDown.systemConfig')"></router-link></li>
            </template>
            <template v-if="isAdminOrSuperUser">
                <li class="divider"></li>
                <li>
                    <router-link to="/operation/announcement" v-text="i18n('navigator.dropDown.announcement')"></router-link>
                </li>
            </template>
            <li class="divider"></li>
            <li>
                <a href="/logout" v-text="i18n('navigator.dropDown.logout')"></a>
            </li>
        </ul>
    </li>
</template>


<script>
    import Component from 'vue-class-component';
    import Base from '../../Base.vue';

    @Component({
        name: 'userMenu',
    })
    export default class UserMenu extends Base {}
</script>

<style lang="less" scoped>

</style>
