<!DOCTYPE html>
<html>
<head>
<#include "../common/codemirror.ftl">
<#include "../common/common.ftl">
<#include "../common/datatables.ftl">
<title><@spring.message "operation.script.title"/></title>
<style>
	.CodeMirror-scroll {
		height: 400px;
	}
	.validation {
		height:150px; margin-top:5px;
	}
</style>
</head>
<body>
<div id="wrap">
	<#include "../common/navigator.ftl">
	<div class="container">
		<form action="${req.getContextPath()}/operation/script_console" name="script_form" method="POST">
			<fieldset>
				<legend class="header">
					<@spring.message "navigator.dropDown.scriptConsole"/>
					<button class="btn btn-success pull-right" id="run_btn">
						<@spring.message "operation.script.runScript"/>
					</button>
				</legend>
			</fieldset>
			<textarea class="span12" id="script_editor" style="resize: none">${(script)!}</textarea>
			<input type="hidden" id="script" name="script" value=""/>
		</form>
		<pre class="prettyprint pre-scrollable validation"><#if result??>${result}<#else>
You can write groovy code to monitor the ngrinder internal state.

Following variables are available.

- applicationContext (org.springframework.context.ApplicationContext)
- agentManager (org.ngrinder.perftest.service.AgentManager)
- agentManagerService (org.ngrinder.agent.service.AgentManagerService)
- regionService (org.ngrinder.region.service.RegionService)
- consoleManager (org.ngrinder.perftest.service.ConsoleManager)
- userService (org.ngrinder.user.service.UserService)
- perfTestService  (org.ngrinder.perftest.service.PerfTestService)
- tagService (org.ngrinder.perftest.service.TagService)
- fileEntryService	(org.ngrinder.script.service.FileEntryService)
- config (org.ngrinder.infra.config.Config)
- pluginManager (org.ngrinder.infra.plugin.PluginManager)
- cacheManager (org.springframework.cache.CacheManager)

Please type following and click the Submit button as a example

print agentManager.getAllAttachedAgents()

please refer nGrinder javadoc to find out more APIs on the given variables.
		</#if></pre>
	</div>
</div>
<#include "../common/copyright.ftl">
	<script src="${req.getContextPath()}/plugins/codemirror/lang/groovy.js"></script>
	<script>
		$(document).ready(function() {

			var editor = CodeMirror.fromTextArea(document.getElementById("script_editor"), {
				mode: "groovy",
				theme: "eclipse",
				lineNumbers: true,
				lineWrapping: true,
				indentUnit:4,
				tabSize:4,
				indentWithTabs:true,
				smartIndent:false,
				extraKeys: {
					"F11": function(cm) {
						setFullScreen(cm, !isFullScreen(cm));
					},
					"Esc": function(cm) {
						if (isFullScreen(cm)) setFullScreen(cm, false);
					},
				Tab: "indentMore"
				},
				onCursorActivity: function() {
					editor.setLineClass(hlLine, null, null);
					hlLine = editor.setLineClass(editor.getCursor().line, null, "activeline");
				}
			});
			var hlLine = editor.setLineClass(0, "activeline");
			
			$("#run_btn").click(function() {
				$('#script').val(editor.getValue());
				document.forms.script_form.submit();
			});
		});
	</script>
</body>
</html>
