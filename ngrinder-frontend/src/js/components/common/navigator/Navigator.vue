<template>
    <header>
        <nav class="navbar navbar-expand bg-dark navbar-dark fixed-top">
            <div class="container">
                <router-link class="navbar-brand" to="/home">
                    <img src="/img/logo_ngrinder_a_header_inv.png" alt="nGrinder"/>
                </router-link>
                <ul class="navbar-nav link-nav">
                    <li class="nav-item" :class="{ active : $route.name === 'perfTestList' }">
                        <router-link class="nav-link" to="/perftest" v-text="i18n('navigator.perfTest')"></router-link>
                    </li>
                    <li class="nav-item" :class="{ active : $route.name === 'scriptList' }">
                        <router-link class="nav-link" to="/script" v-text="i18n('navigator.script')"></router-link>
                    </li>
                </ul>
                <ul class="navbar-nav user-nav">
                    <li v-if="ngrinder.config.clustered" class="nav-item cluster-icon-container" >
                        <img src="/img/cluster_icon.png" title="Cluster Mode" alt="Cluster Mode">
                    </li>
                    <li class="divider-vertical"></li>
                    <li class="nav-item">
                        <user-menu class="nav-link" @showUserProfileModal="$refs.userProfileModal.show()"
                                   @showUserSwitchModal="$refs.userSwitchModal.show()">
                        </user-menu>
                    </li>
                    <li class="divider-vertical"></li>
                    <li class="nav-item">
                        <a class="nav-link" :href="ngrinder.config.helpUrl" target="_blank" v-text="i18n('navigator.help')"></a>
                    </li>
                </ul>
            </div>
        </nav>
        <announcement></announcement>
        <user-switch-modal ref="userSwitchModal"></user-switch-modal>
        <user-profile-modal ref="userProfileModal"></user-profile-modal>
    </header>
</template>

<script>
    import Component from 'vue-class-component';
    import Base from '../../Base.vue';
    import Announcement from '../Announcement.vue';
    import UserMenu from './UserMenu.vue';
    import UserSwitchModal from './modal/UserSwitchModal.vue';
    import UserProfileModal from '../../user/modal/UserProfileModal.vue';

    @Component({
        name: 'navigator',
        components: { Announcement, UserMenu, UserSwitchModal, UserProfileModal },
    })
    export default class Navigator extends Base {}
</script>

<style lang="less" scoped>
    header {
        .navbar {
            padding: 1px 0;

            .container {
                padding: 0;
                justify-content: normal;
            }

            .divider-vertical {
                height: 40px;
                margin: 0 9px;
                border-left: 1px solid #111;
                border-right: 1px solid #222;
            }

            .navbar-nav {
                &.link-nav {
                    min-width: 200px;
                }

                &.user-nav {
                    width: 100%;
                    justify-content: flex-end;
                }

                &.cluster-icon-container {
                    padding-top: 5px;
                }

                .nav-item {
                    display: flex;
                    align-items: center;
                }
            }
        }

        .bg-dark {
            background-color: #1b1b1b !important;

            a {
                color: #999;
                &.active, &:hover {
                    color: white;
                }
            }
        }
    }
</style>
