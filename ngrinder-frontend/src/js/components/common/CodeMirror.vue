<template>
    <codemirror ref="editor" :value="value" :options="cmOptions"></codemirror>
</template>

<script>
    import 'codemirror/mode/properties/properties.js';
    import 'codemirror/mode/groovy/groovy.js';
    import 'codemirror/mode/python/python.js';
    import 'codemirror/mode/javascript/javascript.js';
    import 'codemirror/mode/htmlmixed/htmlmixed';
    import 'codemirror/mode/xml/xml';
    import 'codemirror/mode/yaml/yaml';

    import 'codemirror/addon/display/fullscreen.js';
    import 'codemirror/addon/dialog/dialog.js';
    import 'codemirror/addon/search/search.js';
    import 'codemirror/addon/selection/active-line.js';
    import 'codemirror/addon/scroll/simplescrollbars.js';

    import Component from 'vue-class-component';
    import { codemirror } from 'vue-codemirror';

    import Base from '../Base.vue';

    @Component({
        name: 'codeMirror',
        components: { codemirror },
        props: {
            value: {
                type: String,
                required: false,
                default: '',
            },
            options: {
                type: Object,
                required: false,
            },
        },
    })
    export default class CodeMirror extends Base {
        mounted() {
            Object.assign(this.cmOptions, this.options);
        }

        get cmOptions() {
            return {
                mode: 'groovy',
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
                scrollbarStyle: 'simple',
                extraKeys: {
                    'F11': function(cm) {
                        cm.setOption('fullScreen', !cm.getOption('fullScreen'));
                    },
                    'Esc': function(cm) {
                        if (cm.getOption('fullScreen')) cm.setOption('fullScreen', false);
                    },
                    Tab: 'indentMore',
                },
                ...this.options,
            };
        }

        get codemirror() {
            return this.$refs.editor.codemirror;
        }

        getValue() {
            return this.codemirror.getValue();
        }

        setValue(content) {
            this.codemirror.setValue(content);
        }

        setSize(width, height) {
            this.codemirror.setSize(width, height);
        }
    }
</script>

<style lang="less">
    @import '~codemirror/lib/codemirror.css';
    @import '~codemirror/theme/eclipse.css';
    @import '~codemirror/addon/display/fullscreen.css';
    @import '~codemirror/addon/dialog/dialog.css';
    @import '~codemirror/addon/scroll/simplescrollbars.css';

    .CodeMirror {
        border: 1px solid #dddddd;
        font-family: Consolas, monospace;
        height: 100%;
    }

    .cm-tab {
        background: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAMCAYAAAAkuj5RAAAAAXNSR0IArs4c6QAAAGFJREFUSMft1LsRQFAQheHPowAKoACx3IgEKtaEHujDjORSgWTH/ZOdnZOcM/sgk/kFFWY0qV8foQwS4MKBCS3qR6ixBJvElOobYAtivseIE120FaowJPN75GMu8j/LfMwNjh4HUpwg4LUAAAAASUVORK5CYII=) no-repeat right;
    }
</style>
