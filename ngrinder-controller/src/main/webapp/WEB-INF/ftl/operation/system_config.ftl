<!DOCTYPE html>
<html>
<head>
<#include "../common/common.ftl">
	<title><@spring.message "operation.config.title"/></title>
</head>

<body>
<div id="wrap">
<#include "../common/navigator.ftl">
<div class="container">
			<form id="sys_config_form" name="sys_config_form" method="post">
				<fieldset>
					<legend class="header">
					<@spring.message "navigator.dropDown.systemConfig"/>
						<button id="save_btn" class="btn btn-success pull-right">
						<@spring.message "common.button.save"/>
						</button>
					</legend>
					<textarea id="sys_config_editor" name="content">${content!}</textarea>
				</fieldset>
			</form>
</div>
</div>
<#include "../common/copyright.ftl">
<#include "../common/codemirror.ftl">
<script src="${req.getContextPath()}/plugins/codemirror/lang/properties.js"></script>
<script>
	$(document).ready(function () {
		var editor = CodeMirror.fromTextArea(document.getElementById("sys_config_editor"), {
			mode: "properties",
			theme: "eclipse",
			lineNumbers: true,
			lineWrapping: true,
			indentUnit: 4,
			tabSize: 4,
			indentWithTabs: true,
			smartIndent: false,
			extraKeys: {
				"F11": function (cm) {
					setFullScreen(cm, !isFullScreen(cm));
				},
				"Esc": function (cm) {
					if (isFullScreen(cm)) setFullScreen(cm, false);
				},
				Tab: "indentMore"
			},
			onCursorActivity: function () {
				editor.setLineClass(hlLine, null, null);
				hlLine = editor.setLineClass(editor.getCursor().line, null, "activeline");
			}
		});
		var hlLine = editor.setLineClass(0, "activeline");

		$("#save_btn").click(function () {
			$('#sys_config_editor').text(editor.getValue());
			document.forms.sys_config_form.action = "${req.getContextPath()}/operation/system_config/save";
			document.forms.sys_config_form.submit();
		});

	<#if success??>
		<#if success>
			showSuccessMsg("<@spring.message "common.message.alert.save.success"/>");
		<#else>
			showErrorMsg("<@spring.message "common.message.alert.save.error"/>");
		</#if>
	</#if>
	});
</script>
</body>
</html>
