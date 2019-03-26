<template>
    <div class="container">
        <div v-if="announcement" id="announcement-container">
            <div class="alert alert-block">
                <div class="page-header">
                    <span>
                        <span v-if="hasNew" class="label label-important" v-text="'new'"></span>
                        <span class="announcement-title" v-text="i18n('announcement.title')"></span>
                        <span class="clickable pull-right" id="hide-announcement" @click.prevent="toggleDisplay">
                            <i id="announcement-icon" :class="{'icon-plus': hide, 'icon-minus': !hide}"></i>
                        </span>
                    </span>
                </div>
                <transition name="fade">
                    <div v-if="!hide" id="announcement-content" v-html="announcement"></div>
                </transition>
            </div>
        </div>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import Base from '../Base.vue';

    @Component({
        name: 'announcement',
    })
    export default class Announcement extends Base {
        ANNOUNCEMENT_HIDE_SESSION_KEY = "announcement_hide";

        announcement = '';
        hasNew = false;
        hide = false;

        created() {
            this.getAnnouncement();
            this.hide = this.$session.has(this.ANNOUNCEMENT_HIDE_SESSION_KEY) ? this.$session.get(this.ANNOUNCEMENT_HIDE_SESSION_KEY) : false;
        }

        getAnnouncement() {
            this.$http.get('/announcement/api', {
            }).then(res => {
                this.announcement = res.data;
            }).catch(error => console.error(error));
        }

        toggleDisplay() {
            this.hide = !this.hide;
            this.$session.set(this.ANNOUNCEMENT_HIDE_SESSION_KEY, this.hide);
        }
    }
</script>

<style lang="less" scoped>
    .container {
        padding-top: 40px;

        .alert-block {
            padding:5px 20px;
            margin-bottom:0;

            .fade-enter-active, .fade-leave-active {
                transition-duration: .3s;
                transition-timing-function: ease-in;
            }

            .fade-leave-active {
                transition-duration: .3s;
                transition-timing-function: cubic-bezier(0, 1, 0.5, 1);
            }

            .fade-enter, .fade-leave-to {
                max-height: 0;
                overflow: hidden;
            }

            .fade-enter-to, .fade-leave {
                max-height: 100%;
                overflow: hidden;
            }

            .page-header {
                margin:0;
                padding-bottom:2px;

                .announcement-title {
                    margin-top:0;
                    margin-bottom:0;
                    font-size: 15px;
                }
            }
        }
    }
</style>
