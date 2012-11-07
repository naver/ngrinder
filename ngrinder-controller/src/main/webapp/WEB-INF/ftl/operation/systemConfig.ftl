<!DOCTYPE html>
<html>
<head><#include "../common/common.ftl">
<title><@spring.message "navigator.dropdown.systemConfig"/></title>
</head>

<body>
	<#include "../common/navigator.ftl">
	<div class="container">
		<div class="row">
			<div class="span12">
				<div class="page-header pageHeader">
					<h3><@spring.message "navigator.dropdown.systemConfig"/></h3>
				</div>
				<form id="sysConfigForm" method="post">
				<button id="saveBtn" href="javascript:void(0);" class="btn btn-success pull-right" style="margin-top:-55px;"><@spring.message "common.button.save"/></button>
				<textarea id="sysFileContent" name="content">${content!}</textarea>
				</form>
			</div>
		</div>
		<#include "../common/copyright.ftl">
	</div>
	<#include "../common/messages.ftl"> 
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
					showSuccessMsg("<@spring.message "systemConfig.message.success"/>");
				<#else>
					showErrorMsg("<@spring.message "systemConfig.message.error"/>");
				</#if>
			</#if>
		});
	</script>
</body>
</html>