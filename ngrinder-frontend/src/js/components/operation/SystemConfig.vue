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
        <codemirror ref="editor" :options="cmOptions"></codemirror>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import 'codemirror/mode/properties/properties.js';
    import 'codemirror/addon/display/fullscreen.js';
    import 'codemirror/addon/dialog/dialog.js';
    import 'codemirror/addon/search/search.js';
    import 'codemirror/addon/selection/active-line.js';

    import { codemirror } from 'vue-codemirror';
    import Component from 'vue-class-component';
    import VueHeadful from 'vue-headful';

    import Base from '../Base.vue';
    import MessagesMixin from '../common/mixin/MessagesMixin.vue';

    @Component({
        name: 'systemConfig',
        components: { codemirror, VueHeadful },
    })
    export default class SystemConfig extends Mixins(Base, MessagesMixin) {
        cmOptions = {
            mode: 'properties',
            theme: 'eclipse',
            line: true,
            lineNumbers: true,
            lineWrapping: true,
            indentUnit: 4,
            tabSize: 4,
            indentWithTabs: true,
            smartIndent: false,
            visibleTab: true,
            readOnly: false,
            styleActiveLine: true,
            extraKeys: {
                'F11': function(cm) {
                    cm.setOption('fullScreen', !cm.getOption('fullScreen'));
                },
                'Esc': function(cm) {
                    if (cm.getOption('fullScreen')) cm.setOption('fullScreen', false);
                },
                Tab: 'indentMore',
            },
        };

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
    @import '~codemirror/lib/codemirror.css';
    @import '~codemirror/theme/eclipse.css';
    @import '~codemirror/addon/display/fullscreen.css';
    @import '~codemirror/addon/dialog/dialog.css';

    .CodeMirror {
        border: 1px solid #dddddd;
    }

    .cm-tab {
        background: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAMCAYAAAAkuj5RAAAAAXNSR0IArs4c6QAAAGFJREFUSMft1LsRQFAQheHPowAKoACx3IgEKtaEHujDjORSgWTH/ZOdnZOcM/sgk/kFFWY0qV8foQwS4MKBCS3qR6ixBJvElOobYAtivseIE120FaowJPN75GMu8j/LfMwNjh4HUpwg4LUAAAAASUVORK5CYII=) no-repeat right;
    }

    button {
        height: 30px;
    }
</style>
