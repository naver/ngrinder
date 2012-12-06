<!DOCTYPE html>
<html>
<head><#include "../common/common.ftl">
<title><@spring.message "announcement.view.title"/></title>
</head>

<body>
	<#include "../common/navigator.ftl">
	<div class="container">
		<div class="row">
			<div class="span12">
				<div class="page-header pageHeader">
					<h3><@spring.message "navigator.dropdown.announcement"/>&nbsp;&nbsp;<small><@spring.message "announcement.view.message"/></small></h3>
				</div>
				<form id="annountcementForm" method="post">
				<a id="testBtn" class="btn btn-primary pull-right" href="javascript:void(0);" style="margin-top:-55px; margin-right:55px">Test</a>
				<button id="saveBtn" class="btn btn-success pull-right" style="margin-top:-55px;"><@spring.message "common.button.save"/></button>
				<textarea id="announcementTT" name="content">${content!}</textarea>
				</form>
			</div>
		</div>
		<#include "../common/copyright.ftl">
	</div>
	<#include "../common/codemirror.ftl"> 
	<script src="${req.getContextPath()}/plugins/codemirror/lang/xml.js"></script>
	<script src="${req.getContextPath()}/plugins/codemirror/lang/javascript.js"></script>
	<script src="${req.getContextPath()}/plugins/codemirror/lang/css.js"></script>
	<script src="${req.getContextPath()}/plugins/codemirror/lang/htmlmixed.js"></script>
	<script>
		$(document).ready(function() {
			var editor = CodeMirror.fromTextArea(document.getElementById("announcementTT"), {
			   mode: "text/html",
			   theme: "eclipse",
			   alignCDATA: true,
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
				$('#annountcementTT').text(editor.getValue());
				document.forms.annountcementForm.action = "${req.getContextPath()}/operation/announcement/save";
				document.forms.annountcementForm.submit();
			});
			
			$("#testBtn").click(function() {
				var content = editor.getValue();
				if (content == "") {
					$("#announcementDiv").slideUp();
					return false;
				}
				if (content.indexOf("</") < 0 && content.indexOf("<br>") < 0) {
					content = content.replaceAll("\n", "<br>");
					content = content.replaceAll('\t', '&nbsp;&nbsp;&nbsp;&nbsp;')
				}
				$("#ancemtContentDiv").html(content);
				$("#announcementDiv").slideDown();
			});
			String.prototype.replaceAll = function(s1,s2) { 
			    return this.replace(new RegExp(s1,"gm"),s2); 
			}
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