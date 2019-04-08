<template>
    <div class="navigator-container">
        <div class="navbar navbar-inverse navbar-fixed-top">
            <div class="navbar-inner">
                <div class="container">
                    <div class="navbar-header">
                        <a class="brand clickable" @click.prevent="$router.push('/home')">
                            <img src="/img/logo_ngrinder_a_header_inv.png" alt="nGrinder"/>
                        </a>
                    </div>
                    <ul class="nav navbar-nav">
                        <li id="nav_test" class="clickable">
                            <a @click.prevent="$router.push('/perftest')" v-text="i18n('navigator.perfTest')"></a>
                        </li>
                    </ul>
                    <ul class="nav navbar-nav pull-right">
                        <template v-if="config.clustered">
                            <li class="cluster-icon-container">
                                <img src="/img/cluster_icon.png" title="Cluster Mode" alt="Cluster Mode">
                            </li>
                            <li class="divider-vertical"></li>
                        </template>

                        <user-menu></user-menu>

                        <li class="divider-vertical"></li>
                        <li><a :href="config.helpUrl" target="_blank" v-text="i18n('navigator.help')"></a></li>
                    </ul>
                </div>
            </div>
        </div>
        <announcement></announcement>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import Base from '../../Base.vue';
    import Announcement from '../Announcement.vue'
    import UserMenu from './UserMenu.vue'

    @Component({
        name: 'navigator',
        components: { Announcement, UserMenu },
    })
    export default class Navigator extends Base {}
</script>

<style lang="less" scoped>
    .navigator-container {

        .profile-dialog {
            height: auto;
            padding-bottom: 30px;
        }

        .navbar-inner {
            filter: none;
        }

        li {
            &.cluster-icon-container {
                padding-top: 5px;
            }
        }
    }
</style>
