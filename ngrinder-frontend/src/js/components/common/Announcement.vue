<template>
    <div class="container">
        <div v-if="announcement" id="announcement-container">
            <div class="alert alert-block">
                <div class="page-header">
                    <span>
                        <span v-if="ngrinder.config.hasNewAnnouncement" class="label label-important" v-text="'new'"></span>
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
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import Base from '../Base.vue';
    import MessagesMixin from '../common/mixin/MessagesMixin.vue';

    @Component({
        name: 'announcement',
    })
    export default class Announcement extends Mixins(Base, MessagesMixin) {
        ANNOUNCEMENT_HIDE_SESSION_KEY = 'announcement_hide';

        announcement = '';
        hide = false;

        created() {
            this.getAnnouncement();
            this.hide = this.$session.has(this.ANNOUNCEMENT_HIDE_SESSION_KEY) ? this.$session.get(this.ANNOUNCEMENT_HIDE_SESSION_KEY) : false;

            this.$EventBus.$on(this.$Event.CHANGE_ANNOUNCEMENT, newContent => {
                this.setAnnouncement(newContent);
                if (this.hide) {
                    this.toggleDisplay();
                }
            });
        }

        getAnnouncement() {
            this.$http.get('/announcement/api')
                .then(res => this.setAnnouncement(res.data))
                .catch(() => this.showErrorMsg(this.i18n('common.message.loading.error', { content: this.i18n('announcement.title') })));
        }

        toggleDisplay() {
            this.hide = !this.hide;
            this.$session.set(this.ANNOUNCEMENT_HIDE_SESSION_KEY, this.hide);
        }

        setAnnouncement(announcement) {
            this.announcement = announcement.replace(/\n/g, '<br>').replace(/\t/g, '&nbsp;&nbsp;&nbsp;&nbsp;');
        }
    }
</script>

<style lang="less" scoped>
    .container {
        padding-top: 40px;

        .alert-block {
            padding:5px 20px;
            margin-bottom:0;

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

        #announcement-content {
            margin-top: 10px;
        }
    }
</style>
