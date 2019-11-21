<template>
    <div class="container">
        <div class="alert alert-block">
            <div class="border-bottom">
                <span>
                    <span v-if="ngrinder.config.hasNewAnnouncement" class="badge badge-danger" v-text="'new'"></span>
                    <span class="announcement-title" v-text="i18n('announcement.title')"></span>
                    <span class="pointer-cursor announcement-icon float-right" @click.prevent="toggleDisplay">
                        <i class="fa" :class="{'fa-plus': hide, 'fa-minus': !hide}"></i>
                    </span>
                </span>
            </div>
            <div v-show="!hide" class="announcement-content" v-html="announcement"></div>
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
        ANNOUNCEMENT_HIDE_KEY = 'announcement_hide';

        announcement = '';
        hide = false;

        created() {
            this.getAnnouncement();
            this.hide = this.$ls.get(this.ANNOUNCEMENT_HIDE_KEY, false, Boolean);

            this.$EventBus.$on(this.$Event.CHANGE_ANNOUNCEMENT, newContent => {
                this.setAnnouncement(newContent);
                if (this.hide) {
                    this.toggleDisplay();
                }
            });
        }

        getAnnouncement() {
            this.$http.get('/operation/announcement/api')
                .then(res => this.setAnnouncement(res.data))
                .catch(() => this.showErrorMsg(this.i18n('common.message.loading.error', { content: this.i18n('announcement.title') })));
        }

        toggleDisplay() {
            this.hide = !this.hide;
            this.$ls.set(this.ANNOUNCEMENT_HIDE_KEY, this.hide);
        }

        setAnnouncement(announcement) {
            this.announcement = announcement.replace(/\t/g, '&nbsp;&nbsp;&nbsp;&nbsp;');
        }
    }
</script>

<style lang="less">
    .announcement-content {
        li {
            line-height: 20px;
        }
    }
</style>

<style lang="less" scoped>
    .container {
        .alert-block {
            color: #c09853;
            padding: 45px 20px 0;
            margin-bottom: 0;
            text-shadow: 0 1px 0 rgba(255, 255, 255, 0.5);
            background-color: #fcf8e3;
            border: 1px solid #fbeed5;
            border-radius: 4px;

            .border-bottom {
                margin: 0;
                padding-bottom: 2px;

                .announcement-title {
                    margin-top: 0;
                    margin-bottom: 0;
                    font-size: 15px;
                }
            }
        }

        .announcement-icon {
            margin-top: 5px;
            color: black;
        }

        .announcement-content {
            margin-top: 10px;
        }
    }
</style>
