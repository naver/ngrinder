<template>
    <div class="container">
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
        <code-mirror ref="editor" :options="{ mode: 'text/html' }"></code-mirror>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import Base from '../Base.vue';

    import CodeMirror from '../common/CodeMirror.vue';
    import MessagesMixin from '../common/mixin/MessagesMixin.vue';

    @Component({
        name: 'announcement',
        components: { CodeMirror },
    })
    export default class Announcement extends Mixins(Base, MessagesMixin) {
        mounted() {
            this.pullAnnouncement();
            this.$refs.editor.codemirror.setSize(null, 500);
        }

        pullAnnouncement() {
            this.$http.get('/announcement/api')
            .then(res => this.$refs.editor.setValue(res.data));
        }

        test() {
            const content = this.$refs.editor.getValue();
            if (!content) {
                return;
            }

            this.$EventBus.$emit(this.$Event.CHANGE_ANNOUNCEMENT, content);
        }

        save() {
            const formData = new FormData();
            formData.append('content', this.$refs.editor.getValue());

            this.$http.post('/operation/announcement/api', formData)
            .then(res => {
                if (res.data.success) {
                    this.showSuccessMsg(this.i18n('common.message.alert.save.success'));
                    this.$EventBus.$emit(this.$Event.CHANGE_ANNOUNCEMENT, this.$refs.editor.getValue());
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

        button {
            height: 30px;
        }
    }
</style>
