<template>
    <a data-toggle="dropdown" class="dropdown dropdown-toggle pointer-cursor">
        <span v-if="ngrinder.config.userSwitchMode" v-text="`${ngrinder.currentUser.name}(${ngrinder.currentUser.factualUser.name})`"></span>
        <span v-else v-text="ngrinder.currentUser.name"></span>
        <ul class="dropdown-menu">
            <template v-if="ngrinder.config.userSwitchMode">
                <li>
                    <a class="dropdown-item" :href="`${contextPath}/user/switch?to=`" v-text="i18n('common.button.return')"></a>
                </li>
            </template>
            <template v-else>
                <li>
                    <a @click.prevent="$emit('showUserProfileModal')" class="dropdown-item" v-text="i18n('navigator.dropDown.profile')"></a>
                </li>
                <li>
                    <a @click.prevent="$emit('showUserSwitchModal')" class="dropdown-item" v-text="i18n('navigator.dropDown.switchUser')"></a>
                </li>
            </template>
            <li class="dropdown-divider"></li>
            <template v-if="isAdmin">
                <li v-if="ngrinder.config.clustered" class="dropdown-submenu">
                    <a class="dropdown-item">
                        <span v-text="i18n('navigator.dropDown.downloadAgent')"></span>
                    </a>
                    <ul class="dropdown-menu">
                        <li v-for="region in regions">
                            <a class="dropdown-item" :href="`${contextPath}/agent/download?region=${region}`" v-text="region"></a>
                        </li>
                    </ul>
                </li>
                <li v-else>
                    <a class="dropdown-item" :href="`${contextPath}/agent/download`" v-text="i18n('navigator.dropDown.downloadAgent')"></a>
                </li>
            </template>
            <template v-else>
                <li v-if="ngrinder.config.clustered" class="dropdown-submenu">
                    <a class="dropdown-item">
                        <span v-text="i18n('navigator.dropDown.downloadPrivateAgent')"></span>
                    </a>
                    <ul class="dropdown-menu">
                        <li v-for="region in regions">
                            <a class="dropdown-item" :href="`${contextPath}/agent/download/${region}/${ngrinder.currentUser.id}`" v-text="region"></a>
                        </li>
                    </ul>
                </li>
                <li v-else>
                    <a class="dropdown-item" :href="`${contextPath}/agent/download?owner=${ngrinder.currentUser.id}`" v-text="i18n('navigator.dropDown.downloadPrivateAgent')"></a>
                </li>
            </template>
            <li>
                <a class="dropdown-item" :href="`${contextPath}/monitor/download`" v-text="i18n('navigator.dropDown.downloadMonitor')"></a>
            </li>
            <li>
                <a class="dropdown-item" href="https://github.com/naver/ngrinder/wiki/nGrinder-Recorder-Guide" target="_blank" v-text="i18n('navigator.dropDown.downloadRecorder')"></a>
            </li>
            <template v-if="isAdmin">
                <li class="dropdown-divider"></li>
                <li><router-link class="dropdown-item" to="/user" v-text="i18n('navigator.dropDown.userManagement')"></router-link></li>
                <li><router-link class="dropdown-item" to="/agent/" v-text="i18n('navigator.dropDown.agentManagement')"></router-link></li>
                <li><router-link class="dropdown-item" to="/operation/script_console" v-text="i18n('navigator.dropDown.scriptConsole')"></router-link></li>
                <li><router-link class="dropdown-item" to="/operation/system_config" v-text="i18n('navigator.dropDown.systemConfig')"></router-link></li>
            </template>
            <template v-if="isAdminOrSuperUser">
                <li class="dropdown-divider"></li>
                <li>
                    <router-link class="dropdown-item" to="/operation/announcement" v-text="i18n('navigator.dropDown.announcement')"></router-link>
                </li>
            </template>
            <li class="dropdown-divider"></li>
            <li>
                <a class="dropdown-item" :href="`${contextPath}/logout`" v-text="i18n('navigator.dropDown.logout')"></a>
            </li>
        </ul>
    </a>
</template>

<script>
    import Component from 'vue-class-component';
    import Base from '../../Base.vue';

    @Component({
        name: 'userMenu',
    })
    export default class UserMenu extends Base {
        regions = [];

        created() {
            if (this.ngrinder.config.clustered) {
                this.$http.get('/agent/api/regions').then(res => this.regions = res.data);
            }
        }
    }
</script>

<style lang="less" scoped>
    .dropdown {
        .dropdown-toggle {
            &:hover {
                color: white;
            }
        }

        .dropdown-item {
            cursor: pointer;
            font-size: 12px;

            &:hover {
                color: #fff;
                background-color: #007bff;
            }
        }

        > .dropdown-menu::after {
            content: '';
            display: inline-block;
            border-left: 6px solid transparent;
            border-right: 6px solid transparent;
            border-bottom: 6px solid #fff;
            position: absolute;
            top: -6px;
            left: 10px;
        }

        .dropdown-submenu {
            position: relative;

            &:hover {
                > ul.dropdown-menu {
                    display: block;
                }
            }
        }

        .dropdown-submenu {
            > .dropdown-menu {
                top: -8px;
                left: -100%;
                width: 100%;
            }
        }
    }
</style>
