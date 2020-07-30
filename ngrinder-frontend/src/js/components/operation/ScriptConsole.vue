<template>
    <div class="script-console-container container d-flex flex-column flex-grow-0 vh-100 overflow-y-auto">
        <div class="flex-grow-0">
            <vue-headful :title="i18n('operation.script.title')"/>
            <fieldset>
                <legend class="header border-bottom d-flex">
                    <span v-text="i18n('navigator.dropDown.scriptConsole')"></span>
                    <button class="btn btn-success mt-auto mb-auto ml-auto" @click="runScript">
                        <i class="fa fa-play mr-1"></i>
                        <span v-text="i18n('operation.script.runScript')"></span>
                    </button>
                </legend>
            </fieldset>
        </div>
        <div class="flex-grow-1 overflow-y-auto">
            <code-mirror class="h-100" ref="editor"></code-mirror>
        </div>
        <div class="result-console-container flex-grow-0 rounded border">
            <vue-scroll>
                <pre v-text="result"></pre>
            </vue-scroll>
        </div>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import VueHeadful from 'vue-headful';

    import Base from '../Base.vue';
    import CodeMirror from '../common/CodeMirror.vue';

    @Component({
        name: 'scriptConsole',
        components: { CodeMirror, VueHeadful },
    })
    export default class ScriptConsole extends Base {
        result = 'You can write groovy code to monitor the ngrinder internal state.\n' +
            '\n' +
            'Following variables are available.\n' +
            '\n' +
            '- applicationContext  (org.springframework.context.ApplicationContext)\n' +
            '- agentManager        (org.ngrinder.perftest.service.AgentManager)\n' +
            '- agentService        (org.ngrinder.agent.service.AgentService)\n' +
            '- regionService       (org.ngrinder.region.service.RegionService)\n' +
            '- consoleManager      (org.ngrinder.perftest.service.ConsoleManager)\n' +
            '- userService         (org.ngrinder.user.service.UserService)\n' +
            '- perfTestService     (org.ngrinder.perftest.service.PerfTestService)\n' +
            '- tagService          (org.ngrinder.perftest.service.TagService)\n' +
            '- fileEntryService    (org.ngrinder.script.service.FileEntryService)\n' +
            '- config              (org.ngrinder.infra.config.Config)\n' +
            '- pluginManager       (org.ngrinder.infra.plugin.PluginManager)\n' +
            '- cacheManager        (org.springframework.cache.CacheManager)\n' +
            '- hazelcastService    (org.ngrinder.infra.hazelcast.hazelcastService)\n' +
            '\n' +
            'Please type following and click the Submit button as a example\n' +
            '\n' +
            'print agentManager.getAllAttachedAgents()\n' +
            '\n' +
            'please refer nGrinder javadoc to find out more APIs on the given variables.\n';

        get codemirror() {
            return this.$refs.editor.codemirror;
        }

        runScript() {
            const script = this.codemirror.getValue().trim();
            if (!script) {
                return;
            }

            this.$http.post('/operation/script_console/api', {
                script,
            })
            .then(res => this.result = res.data.result);
        }
    }
</script>

<style lang="less">

    .script-console-container {
        .__panel {
            background-color: #f5f5f5;
        }
    }

</style>

<style lang="less" scoped>

    .result-console-container {
        height: 250px;
        max-height: 340px;
        margin-top: 5px;

        pre {
            padding: 14px 10px 2px 10px;
            font-size: 12px;
            background-color: #f5f5f5;
        }
    }

</style>
