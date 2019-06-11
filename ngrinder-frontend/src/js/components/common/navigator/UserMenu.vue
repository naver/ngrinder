<template>
    <li class="dropdown">
        <a data-toggle="dropdown" class="dropdown-toggle clickable">
            <span v-if="ngrinder.config.userSwitchMode" v-text="`${ngrinder.currentUser.name}(${ngrinder.currentUser.factualUser.name})`"></span>
            <span v-else v-text="ngrinder.currentUser.name"></span>
            <b class="caret"></b>
        </a>
        <ul class="dropdown-menu">
            <li v-if="ngrinder.config.userSwitchMode">
                <a href="/user/switch?to=" v-text="i18n('common.button.return')"></a>
            </li>
            <li v-show="!ngrinder.config.userSwitchMode">
                <a @click.prevent="$emit('showUserProfileModal')" class="clickable" v-text="i18n('navigator.dropDown.profile')"></a>
            </li>
            <li v-show="!ngrinder.config.userSwitchMode">
                <a @click.prevent="$emit('showUserSwitchModal')" class="clickable" v-text="i18n('navigator.dropDown.switchUser')"></a>
            </li>
            <li class="divider"></li>
            <template v-if="isAdmin">
                <li v-if="ngrinder.config.clustered" class="dropdown-submenu">
                    <a class="clickable" v-text="i18n('navigator.dropDown.downloadAgent')"></a>
                    <ul class="dropdown-menu">
                        <li v-for="region in ngrinder.config.visibleRegions">
                            <a :href="`/agent/download?region=${region}`" v-text="region"></a>
                        </li>
                    </ul>
                </li>
                <li v-else>
                    <a href="/agent/download" v-text="i18n('navigator.dropDown.downloadAgent')"></a>
                </li>
            </template>
            <template v-if="!isAdmin">
                <li v-if="ngrinder.config.clustered" class="dropdown-submenu">
                    <a class="clickable" v-text="i18n('navigator.dropDown.downloadPrivateAgent')"></a>
                    <ul class="dropdown-menu">
                        <li v-for="region in ngrinder.config.visibleRegions">
                            <a :href="`/agent/download/${region}/${ngrinder.currentUser.id}`" v-text="region"></a>
                        </li>
                    </ul>
                </li>
                <li v-else>
                    <a :href="`/agent/download?owner=${ngrinder.currentUser.id}`" v-text="i18n('navigator.dropDown.downloadPrivateAgent')"></a>
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
                <li><router-link to="/user" v-text="i18n('navigator.dropDown.userManagement')"></router-link></li>
                <li><a href="/agent/" v-text="i18n('navigator.dropDown.agentManagement')"></a></li>
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
