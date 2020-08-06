<script>
    import { Mixin } from 'vue-mixin-decorator';
    import Vue from 'vue';

    @Mixin
    export default class Guide extends Vue {
        get guides() {
            return {
                perftest:
                    'You can use various log levels. [trace, debug, info, warn, error]\n' +
                    'ex) grinder.logger.${level}("message")\n\n' + // eslint-disable-line no-template-curly-in-string
                    'You can access to response body with HTTPResponse.getText() method.\n' +
                    'ex) HTTPResponse result = request.GET("...")\n' +
                    '    grinder.logger.debug(result.text)\n\n' +
                    'You can test multiple transactions by recording new GTest instance.\n' +
                    'ex) @BeforeProcess\n' +
                    '    public static void beforeProcess() {\n' +
                    '        test1 = new GTest(1, "...")\n' +
                    '        test2 = new GTest(2, "...")\n' +
                    '    }\n\n' +
                    '    @BeforeThread\n' +
                    '    public void beforeThread() {\n' +
                    '        test1.record(this, "test1")\n' +
                    '        test2.record(this, "test2")\n' +
                    '    }\n\n' +
                    '    @Test\n' +
                    '    public void test1() { ... }\n\n' +
                    '    @Test\n' +
                    '    public void test2() { ... }\n\n' +
                    'You can specify the test run rate with @RunRate annotation.\n' +
                    'ex) import net.grinder.scriptengine.groovy.junit.annotation.RunRate\n\n' +
                    '    @Test\n' +
                    '    @RunRate(50)\n' +
                    '    public void test() { ... } // This test will run only half of the total run which you specified.\n\n',
                gitconfig:
                    'Git Config Field Details\n' +
                    '* name: Configuration name. (unique, required)\n' +
                    '* owner: Repository organization/owner name. (required)\n' +
                    '* repo: Repository name (required)\n' +
                    '* access-token: Github personal access token (required)\n' +
                    '* branch: The branch to find your test scripts. (optional, default: default branch)\n' +
                    `* base-url: The API base URL of github. If you are using your own Github Enterprise Server, you need to set it (optional, default: ${this.ngrinder.config.githubBaseUrl})\n` +
                    '* script-root: Root path for scripts searching. (optional, default: project root)',
            };
        }

        get shortcutConfigs() {
            return [
                '', // for accumulation.
                { key: 'Ctrl-Shift-V', desc: 'script.editor.button.validate' },
                { key: 'Ctrl-Shift-S', desc: 'common.button.save' },
                { key: 'Ctrl-Shift-G / Shift-Cmd-G', desc: 'script.editor.tip.findPrev' },
                { key: 'Ctrl-Shift-F / Cmd-Option-F', desc: 'script.editor.tip.replace' },
                { key: 'Ctrl-Shift-R / Shift-Cmd-Option-F', desc: 'script.editor.tip.replaceAll' },
                { key: 'Ctrl-F / Cmd-F', desc: 'script.editor.tip.startSearching' },
                { key: 'Ctrl-G / Cmd-G', desc: 'script.editor.tip.findNext' },
                { key: 'F11', desc: 'script.editor.tip.fullScreen' },
                { key: 'ESC', desc: 'script.editor.tip.back' },
            ];
        }
    }

</script>
