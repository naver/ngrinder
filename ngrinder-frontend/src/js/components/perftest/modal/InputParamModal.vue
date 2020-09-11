<template>
    <div class="input-param-modal modal fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <header class="modal-header">
                    <h4>
                        <span v-text="i18n('perfTest.config.param')"></span>
                        <span data-toggle="popover"
                              data-html="true"
                              data-trigger="hover"
                              :data-content="i18n('perfTest.config.param.help')"
                              :title="i18n('perfTest.config.param')"
                              data-placement='bottom'>
                            <i class="fa fa-question-circle align-middle"></i>
                        </span>
                    </h4>
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                </header>
                <div class="modal-body">
                    <code-mirror ref="editor"
                                 class="h-100"
                                 :options="cmOptions">
                    </code-mirror>
                </div>
                <footer class="modal-footer">
                    <button class="btn btn-success" v-text="i18n('common.button.save')" @click.prevent="save"></button>
                    <button class="btn btn-danger" data-dismiss="modal" v-text="i18n('common.button.cancel')"></button>
                </footer>
            </div>
        </div>
    </div>
</template>

<script>
    import 'codemirror/addon/edit/matchbrackets.js';
    import 'codemirror/addon/edit/closebrackets.js';
    import 'codemirror/addon/lint/lint.js';
    import 'codemirror/addon/lint/json-lint.js';
    import 'codemirror/addon/lint/javascript-lint.js';

    import { jsonc } from 'jsonc';
    import Component from 'vue-class-component';
    import ModalBase from '../../common/modal/ModalBase.vue';
    import CodeMirror from '../../common/CodeMirror.vue';

    @Component({
        name: 'inputParamModel',
        components: { CodeMirror },
        props: {
            value: {
                type: String,
                required: true,
            },
        },
    })
    export default class InputParamModal extends ModalBase {
        cmOptions = {
            mode: 'application/json',
            lint: true,
            line: false,
            lineNumbers: false,
            matchBrackets: true,
            autoCloseBrackets: true,
        };

        beforeShown() {
            const value = jsonc.isJSON(this.value) ? jsonc.beautify(this.value) : this.value;
            this.$refs.editor.codemirror.setValue(value);
        }

        shown() {
            this.$refs.editor.codemirror.refresh();
            this.$refs.editor.codemirror.focus();
        }

        save() {
            const value = this.$refs.editor.codemirror.getValue();
            const result = jsonc.isJSON(value) ? jsonc.uglify(value) : value;
            this.$emit('save', result);
            this.hide();
        }
    }
</script>

<style lang="less" scoped>
    .input-param-modal {
        .modal-body {
            height: 350px;
        }

        .fa-question-circle {
            font-size: 13px;
        }
    }
</style>

<style lang="less">
    @import '~codemirror/addon/lint/lint.css';

    .CodeMirror-lint-tooltip {
        z-index: 1200;
    }
</style>
