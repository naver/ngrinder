<template>
    <div class="container">
        <vue-headful :title="i18n('operation.config.title')"/>
        <fieldset>
            <legend class="header border-bottom d-flex">
                <span v-text="i18n('navigator.dropDown.systemConfig')"></span>
                <button class="btn btn-success mt-auto mb-auto ml-auto" @click="save">
                    <i class="fa fa-save mr-1"></i>
                    <span v-text="i18n('common.button.save')"></span>
                </button>
            </legend>
        </fieldset>
        <code-mirror ref="editor" :options="{ mode: 'properties' }"></code-mirror>
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
        name: 'systemConfig',
        components: { CodeMirror, VueHeadful },
    })
    export default class SystemConfig extends Mixins(Base, MessagesMixin) {

        mounted() {
            this.pullSystemConfig();
            this.codemirror.setSize(null, 500);
        }

        get codemirror() {
            return this.$refs.editor.codemirror;
        }

        pullSystemConfig() {
            this.$http.get('/operation/system_config/api')
            .then(res => {
                this.codemirror.setValue(res.data);
                this.codemirror.clearHistory(); // Prevent to be empty content by undo
            });
        }

        save() {
            const formData = new FormData();
            formData.append('content', this.codemirror.getValue());

            this.$http.post('/operation/system_config/api', formData)
            .then(() => {
                this.showSuccessMsg(this.i18n('perfTest.running.success'));
            });
        }
    }
</script>

<style lang="less" scoped>
    button {
        height: 30px;
    }
</style>
