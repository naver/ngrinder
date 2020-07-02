<template>
    <header>
        <nav class="navbar navbar-expand bg-dark navbar-dark fixed-top">
            <div class="container">
                <router-link class="navbar-brand" to="/home">
                    <img :src="`${contextPath}/img/logo_ngrinder_a_header_inv.png`" alt="nGrinder"/>
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
                        <img :src="`${contextPath}/img/cluster_icon.png`" title="Cluster Mode" alt="Cluster Mode">
                    </li>
                    <li class="divider-vertical"></li>
                    <li class="nav-item">
                        <a data-toggle="dropdown" class="dropdown-toggle pointer-cursor">
                            <span v-if="ngrinder.config.userSwitchMode" v-text="`${ngrinder.currentUser.name} (${ngrinder.currentUser.factualUser.name})`"></span>
                            <span v-else v-text="ngrinder.currentUser.name"></span>
                        </a>
                        <user-menu class="user-menu"
                                   @showUserEditModal="$refs.userEditModal.show()"
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
        <user-switch-modal ref="userSwitchModal"></user-switch-modal>
        <user-edit-modal focus="userName"
                         ref="userEditModal">
        </user-edit-modal>
    </header>
</template>

<script>
    import Component from 'vue-class-component';
    import Base from '../../Base.vue';
    import UserMenu from './UserMenu.vue';
    import UserSwitchModal from './modal/UserSwitchModal.vue';
    import UserEditModal from '../../user/modal/UserEditModal.vue';

    @Component({
        name: 'navigator',
        components: { UserMenu, UserSwitchModal, UserEditModal },
    })
    export default class Navigator extends Base {
        showUserEditModal = false;
    }
</script>

<style lang="less" scoped>
    header {
        .navbar {
            height: 40px;

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

                    &.active {
                        background-color: #555555
                    }

                    a {
                        &:hover {
                            color: white !important;
                        }
                        padding: 10px 15px 10px;
                    }

                    &.show {
                        a {
                            color: white;
                        }
                        background-color: #111111;
                    }
                }

                .user-menu {
                    position: relative;
                    top: 20px;
                    left: -60px;
                }
            }
        }

        .bg-dark {
            background-color: #1b1b1b !important;

            a {
                color: #999;
            }
        }
    }
</style>
