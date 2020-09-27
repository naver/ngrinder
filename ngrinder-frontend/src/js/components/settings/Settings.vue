<template>
    <div class="container settings">
        <vue-headful :title="i18n('common.settings')"/>
        <div class="container d-flex p-0">
            <div class="sidebar-nav">
                <div class="card bg-light d-inline-block">
                    <ul class="nav flex-column">
                        <li class="nav-item border-bottom-0" :class="{'selected' : selectedMenu === 'webhook'}">
                            <a class="nav-link pointer-cursor"  v-text="i18n('common.webhook')" @click="clickMenu('webhook')"></a>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="component-container">
                <component :is="currentMenuComponent"></component>
            </div>
        </div>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import VueHeadful from 'vue-headful';

    import Base from '../Base.vue';
    import MessagesMixin from '../common/mixin/MessagesMixin.vue';

    @Component({
        name: 'settings',
        components: { VueHeadful },
    })
    export default class Settings extends Mixins(Base, MessagesMixin) {
        selectedMenu = 'webhook';
        currentMenuComponent;

        menuComponents = {
        };

        created() {
            const currentMenu = this.$route.params.currentMenu;
            this.selectedMenu = currentMenu;
            this.currentMenuComponent = this.menuComponents[currentMenu];
        }

        clickMenu(selectedMenu) {
            if (this.selectedMenu === selectedMenu) {
                return;
            }
            this.selectedMenu = selectedMenu;
            this.currentMenuComponent = this.menuComponents[selectedMenu];
            history.replaceState('', '', `${this.contextPath}/settings/${selectedMenu}`);
        }
    }
</script>

<style lang="less" scoped>
    .settings {
        .component-container {
            flex: 1;
        }

        .sidebar-nav {
            margin-top: 30px;
            padding-right: 24px;

            .card {
                width: 200px;
                border-radius: 0 3px 3px 0;

                ul {
                    background: white;

                    .nav-item {
                        font-size: 12px;
                        color: #1b1f23;
                        border-bottom: 1px solid #EAECEF;

                        &.selected {
                            .nav-link {
                                &:before {
                                    position: absolute;
                                    top: 0;
                                    bottom: 0;
                                    left: -1px;
                                    width: 2px;
                                    content: '';
                                    background-color: #007BFF;
                                }
                            }
                        }

                        .nav-link {
                            position: relative;
                            padding: 8px 16px;
                        }
                    }
                }
            }
        }
    }
</style>
