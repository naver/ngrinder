<!DOCTYPE html>
<html>
<head><#include "../common/common.ftl">
<title><@spring.message "config.view.title"/></title>
</head>

<body>
	<#include "../common/navigator.ftl">
	<div class="container">
		<div class="row">
			<div class="span12">
				<form id="sysConfigForm" method="post">
					<legend class="header">
						<@spring.message "navigator.dropdown.systemConfig"/>
						<button id="saveBtn" class="btn btn-success pull-right">
							<@spring.message "common.button.save"/>
						</button>
					</legend>
					<textarea id="sysFileContent" name="content">${content!}</textarea>
				</form>
			</div>
		</div>
		<#include "../common/copyright.ftl">
	</div>
	<#include "../common/codemirror.ftl"> 
	<script src="${req.getContextPath()}/plugins/codemirror/lang/properties.js"></script>
	<script>
		$(document).ready(function() {
			var editor = CodeMirror.fromTextArea(document.getElementById("sysFileContent"), {
			   mode: "properties",
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
			
			$("#saveBtn").click(function() {
				$('#sysFileContent').text(editor.getValue());
				document.forms.sysConfigForm.action = "${req.getContextPath()}/operation/systemConfig/save";
				document.forms.sysConfigForm.submit();
			});
			
			<#if success?exists>
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
