<template>
    <div class="container">
        <fieldset>
            <legend class="header">
                <span v-text="i18n('navigator.dropDown.scriptConsole')"></span>
                <button id="run_btn" class="btn btn-success pull-right"
                        v-text="i18n('operation.script.runScript')" v-on:click="runScript">
                </button>
            </legend>
        </fieldset>
        <codemirror ref="editor"
                    :options="cmOptions"></codemirror>
        <pre class="prettyprint pre-scrollable validation" v-text="result"></pre>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import Base from '../Base.vue';

    import { codemirror } from 'vue-codemirror';

    import 'codemirror/mode/groovy/groovy.js';

    import 'codemirror/addon/display/fullscreen.js';
    import 'codemirror/addon/dialog/dialog.js';
    import 'codemirror/addon/search/search.js';
    import 'codemirror/addon/selection/active-line.js';

    @Component({
        name: "scriptConsole",
        components: { codemirror }
    })
    export default class ScriptConsole extends Base {

        result = 'You can write groovy code to monitor the ngrinder internal state.\n' +
            '\n' +
            'Following variables are available.\n' +
            '\n' +
            '- applicationContext (org.springframework.context.ApplicationContext)\n' +
            '- agentManager (org.ngrinder.perftest.service.AgentManager)\n' +
            '- agentManagerService (org.ngrinder.agent.service.AgentManagerService)\n' +
            '- regionService (org.ngrinder.region.service.RegionService)\n' +
            '- consoleManager (org.ngrinder.perftest.service.ConsoleManager)\n' +
            '- userService (org.ngrinder.user.service.UserService)\n' +
            '- perfTestService  (org.ngrinder.perftest.service.PerfTestService)\n' +
            '- tagService (org.ngrinder.perftest.service.TagService)\n' +
            '- fileEntryService\t(org.ngrinder.script.service.FileEntryService)\n' +
            '- config (org.ngrinder.infra.config.Config)\n' +
            '- pluginManager (org.ngrinder.infra.plugin.PluginManager)\n' +
            '- cacheManager (org.springframework.cache.CacheManager)\n' +
            '\n' +
            'Please type following and click the Submit button as a example\n' +
            '\n' +
            'print agentManager.getAllAttachedAgents()\n' +
            '\n' +
            'please refer nGrinder javadoc to find out more APIs on the given variables.\n';

        cmOptions = {
            mode: 'groovy',
            theme: "eclipse",
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
                "F11": function (cm) {
                    cm.setOption("fullScreen", !cm.getOption("fullScreen"));
                },
                "Esc": function (cm) {
                    if (cm.getOption("fullScreen")) cm.setOption("fullScreen", false);
                },
                Tab: "indentMore"
            },
        };

        mounted() {
            this.codemirror.setSize(null, 400);
        }

        get codemirror() {
            return this.$refs.editor.codemirror;
        }

        runScript() {
            this.$http.get('/operation/script_console/api/run', {
                params: { script: this.codemirror.getValue() }
            })
            .then(res => this.result = res.data);
        }
    }
</script>
<style lang="less" scoped>
    @import '~codemirror/lib/codemirror.css';
    @import '~codemirror/theme/eclipse.css';
    @import '~codemirror/addon/display/fullscreen.css';
    @import '~codemirror/addon/dialog/dialog.css';

    .validation {
        margin-top: 20px;
        height: 150px;
    }

    .CodeMirror {
        border: 1px solid #dddddd;
    }

    .cm-tab {
        background: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAMCAYAAAAkuj5RAAAAAXNSR0IArs4c6QAAAGFJREFUSMft1LsRQFAQheHPowAKoACx3IgEKtaEHujDjORSgWTH/ZOdnZOcM/sgk/kFFWY0qV8foQwS4MKBCS3qR6ixBJvElOobYAtivseIE120FaowJPN75GMu8j/LfMwNjh4HUpwg4LUAAAAASUVORK5CYII=) no-repeat right;
    }
</style>
