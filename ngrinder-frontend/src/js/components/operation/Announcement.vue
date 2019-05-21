<template>
    <div class="container">
        <fieldset>
            <legend class="header">
                <span v-text="i18n('navigator.dropDown.announcement')"></span>&nbsp;&nbsp;
                <small v-text="i18n('operation.announcement.help')"></small>
                <a class="pointer-cursor btn btn-primary pull-right"
                   v-text="i18n('common.button.test')" @click="test"></a>
                <button class="btn btn-success pull-right"
                        v-text="i18n('common.button.save')" @click="save"></button>
            </legend>
        </fieldset>
        <code-mirror ref="editor" :options="{ mode: 'text/html' }"></code-mirror>
        <messages ref="messages"></messages>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import Base from '../Base.vue';

    import Messages from '../common/Messages.vue';
    import CodeMirror from '../common/CodeMirror.vue';

    @Component({
        name: "announcement",
        components: { CodeMirror, Messages }
    })

    export default class Announcement extends Base {

        mounted() {
            this.pullAnnouncement();
            this.$refs.editor.codemirror.setSize(null, 500);
        }

        pullAnnouncement() {
            this.$http.get('/announcement/api')
            .then(res => this.$refs.editor.setValue(res.data));
        }

        test() {
            let content = this.$refs.editor.getValue();
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
                    this.$refs.messages.showSuccessMsg(this.i18n('common.message.alert.save.success'));
                    this.$EventBus.$emit(this.$Event.CHANGE_ANNOUNCEMENT, this.$refs.editor.getValue());
                } else {
                    this.$refs.messages.showErrorMsg(this.i18n('common.message.alert.save.error'))
                }
            });
        }
    }
</script>

<style lang="less" scoped>
    .btn-success {
        margin-right: 5px
    }
</style>
