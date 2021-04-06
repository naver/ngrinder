<template>
    <li class="dropdown">
        <ul class="dropdown-menu">
            <template v-if="ngrinder.config.userSwitchMode">
                <li>
                    <a class="dropdown-item" :href="`${contextPath}/user/switch?to=`" v-text="i18n('common.button.return')"></a>
                </li>
            </template>
            <template v-else>
                <li>
                    <a @click.prevent="$emit('showUserEditModal')" class="dropdown-item" v-text="i18n('navigator.dropDown.profile')"></a>
                </li>
                <li>
                    <a @click.prevent="$emit('showUserSwitchModal')" class="dropdown-item" v-text="i18n('navigator.dropDown.switchUser')"></a>
                </li>
            </template>
            <li class="dropdown-divider"></li>
            <template v-if="isAdmin">
                <li v-if="ngrinder.config.clustered" class="dropdown-submenu">
                    <a class="dropdown-item dropdown-toggle">
                        <span v-text="i18n('navigator.dropDown.downloadAgent')"></span>
                    </a>
                    <ul class="dropdown-menu">
                        <template v-for="(regionInfo, idx) in regions">
                            <li class="dropdown-submenu">
                                <a class="dropdown-item"
                                   :href="`${contextPath}/agent/download?region=${regionInfo.region}`"
                                   v-text="i18n(regionInfo.region)">
                                </a>
                                <a v-for="subregion in regionInfo.subregion"
                                   class="dropdown-item" :href="`${contextPath}/agent/download?region=${regionInfo.region}&subregion=${subregion}`"
                                   v-text="i18n(`${regionInfo.region}.${subregion}`)">
                                </a>
                            </li>
                            <li v-if="idx !== (regions.length -1)" class="dropdown-divider m-0"></li>
                        </template>
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
                        <template v-for="(regionInfo, idx) in regions">
                            <li class="dropdown-submenu">
                                <a class="dropdown-item"
                                   :href="`${contextPath}/agent/download/${regionInfo.region}/${ngrinder.currentUser.id}`"
                                   v-text="i18n(regionInfo.region)">
                                </a>
                                <a v-for="subregion in regionInfo.subregion"
                                   class="dropdown-item" :href="`${contextPath}/agent/download/${regionInfo.region}/${subregion}/${ngrinder.currentUser.id}`"
                                   v-text="i18n(`${regionInfo.region}.${subregion}`)">
                                </a>
                            </li>
                            <li v-if="idx !== (regions.length -1)" class="dropdown-divider m-0"></li>
                        </template>
                    </ul>
                </li>
                <li v-else>
                    <a class="dropdown-item" :href="`${contextPath}/agent/download?owner=${ngrinder.currentUser.id}`" v-text="i18n('navigator.dropDown.downloadPrivateAgent')"></a>
                </li>
            </template>
            <li>
                <a class="dropdown-item" :href="`${contextPath}/monitor/download`" v-text="i18n('navigator.dropDown.downloadMonitor')"></a>
            </li>
            <li class="dropdown-divider"></li>
            <template v-if="isAdmin">
                <li><router-link class="dropdown-item" to="/user" v-text="i18n('navigator.dropDown.userManagement')"></router-link></li>
            </template>
            <li><router-link class="dropdown-item" to="/agent/" v-text="i18n('navigator.dropDown.agentManagement')"></router-link></li>
            <template v-if="isAdmin">
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
                <router-link class="dropdown-item" to="/webhook/settings" v-text="i18n('webhook.settings')"></router-link>
            </li>
            <li>
                <a class="dropdown-item" :href="`${contextPath}/logout`" v-text="i18n('navigator.dropDown.logout')"></a>
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

        .dropdown-toggle::after {
            display: inline-block;
            position: absolute;
            margin-left: 0.255em;
            vertical-align: 0.255em;
            content: "";
            border-top: 0.3em solid;
            border-right: 0.3em solid transparent;
            border-bottom: 0;
            border-left: 0.3em solid transparent;
            right: 7px;
            top: 10px;
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
