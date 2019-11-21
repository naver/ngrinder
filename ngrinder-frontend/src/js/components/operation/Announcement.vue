<template>
    <div class="container d-flex flex-column flex-grow-0 vh-100 overflow-y-auto">
        <div class="flex-grow-0">
            <vue-headful :title="i18n('operation.announcement.title')"/>
            <fieldset class="mb-0">
                <legend class="header border-bottom d-flex">
                    <span v-text="i18n('navigator.dropDown.announcement')"></span>
                    <small class="ml-3" v-text="i18n('operation.announcement.help')"></small>
                    <span class="ml-auto mt-auto mb-auto">
                        <button class="btn btn-primary" @click="test">
                            <i class="fa fa-play mr-1"></i>
                            <span v-text="i18n('common.button.test')"></span>
                        </button>
                        <button class="btn btn-success" @click="save">
                            <i class="fa fa-save mr-1"></i>
                            <span v-text="i18n('common.button.save')"></span>
                        </button>
                    </span>
                </legend>
            </fieldset>
        </div>
        <div class="flex-grow-1 overflow-y-auto">
            <code-mirror class="h-100" ref="editor" :options="{ mode: 'text/html' }"></code-mirror>
        </div>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import VueHeadful from 'vue-headful';

    import Base from '../Base.vue';
    import CodeMirror from '../common/CodeMirror.vue';
    import MessagesMixin from '../common/mixin/MessagesMixin.vue';

    @Component({
        name: 'announcement',
        components: { CodeMirror, VueHeadful },
    })
    export default class Announcement extends Mixins(Base, MessagesMixin) {
        originContent = '';

        mounted() {
            this.pullAnnouncement();
        }

        beforeDestroy() {
            this.$EventBus.$emit(this.$Event.CHANGE_ANNOUNCEMENT, this.originContent);
        }

        pullAnnouncement() {
            this.$http.get('/operation/announcement/api')
            .then(res => {
                this.originContent = res.data;
                this.$refs.editor.setValue(this.originContent);
            });
        }

        test() {
            const content = this.$refs.editor.getValue().trim();
            if (!content) {
                return;
            }

            this.$EventBus.$emit(this.$Event.CHANGE_ANNOUNCEMENT, content);
        }

        save() {
            const content = this.$refs.editor.getValue().trim();
            this.$http({
                method: 'post',
                url: '/operation/announcement/api',
                data: content,
                headers: { 'Content-Type': 'text/plain' },
            }).then(res => {
                if (res.data.success) {
                    this.originContent = content;
                    this.showSuccessMsg(this.i18n('common.message.alert.save.success'));
                    this.$EventBus.$emit(this.$Event.CHANGE_ANNOUNCEMENT, content);
                } else {
                    this.showErrorMsg(this.i18n('common.message.alert.save.error'));
                }
            });
        }
    }
</script>

<style lang="less" scoped>
    legend {
        small {
            font-size: 15px;
            color: #999999;
        }
    }
</style>
