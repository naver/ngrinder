<!DOCTYPE html>
<html>
	<head>
		<#include "../common/common.ftl">
		<#include "../common/jqplot.ftl">
		<title><@spring.message "script.editor.title"/></title>
		<style>
			div.div-host {
				background-color: #FFFFFF;
				border: 1px solid #D6D6D6;
				height: 63px;
				overflow-y: scroll;
				border-radius: 3px 3px 3px 3px;
				width:250px;
			}

			div.div-host .host {
				color: #666666;
				display: inline-block;
				margin-left: 7px;
				margin-top: 2px;
				margin-bottom: 2px;
			}

			.add-host-btn {
				margin-top:38px;
				margin-left:210px;
                position:absolute
			}

			div.modal-body div.chart {
				border:1px solid #878988;
				height:250px;
				min-width:500px;
				margin-bottom:12px;
				padding:5px
			}
			code a {
				color:inherit;
			}
		</style>
	</head>

	<body>
	<div id="wrap">
	<#include "../common/navigator.ftl">
	<div class="container">
		<form id="content_form" name="content_form" class="well" method="post" target="_self"
			  style="margin-bottom:10px; margin-top:0">
			<div class="form-horizontal">
				<fieldset>
					<div class="control-group">
						<table style="width:100%">
							<colgroup>
								<col width="150px"/>
								<col width="*"/>
								<col width="300px"/>
							</colgroup>
							<tr>
								<td>
									<label class="control-label" for="script_name">
										<@spring.message "script.info.name"/>
									</label>
								</td>
								<td>
									<span class="input-large uneditable-input span6" style="cursor:text">${breadcrumbPath}</span>
									<input type="hidden" id="script_name" class="span6" name="path" value="${(file.path)!}" readonly/>
								</td>
								<td>
								<#if scriptHandler.isValidatable()>
									<a class="pointer-cursor btn btn-success" id="save_btn" style="margin-left:73px; width:40px;"><@spring.message "common.button.save"/></a>
									<a class="pointer-cursor btn btn-primary" id="validate_btn" style="width:90px;"><@spring.message "script.editor.button.validate"/></a>
								<#else>
									<a class="pointer-cursor btn btn-success" id="save_btn" style="margin-left:190px; width:40px;"><@spring.message "common.button.save"/></a>
								</#if>
								</td>
							</tr>
						</table>
					</div>
					<div style="margin-bottom: 0" class="control-group">
						<table style="width:100%">
							<colgroup>
								<col width="150px"/>
								<col width="*"/>
								<col width="300px"/>
							</colgroup>
							<tr>
								<td>
									<label class="control-label" for="description"><@spring.message "script.action.commit"/></label>
								</td>
								<td>
									<textarea class="span6" id="description" name="description" style="resize:none; height:55px" >${(file.description)!}</textarea>
								</td>
								<td>
									<#if file?? && file.properties.targetHosts??>
										<#assign targetHosts = file.properties.targetHosts/>
									</#if>
									<#include "../perftest/host.ftl"/>
								</td>
							</tr>
						</table>
					</div>
				</fieldset>
			</div>
			<input type="hidden" id="create_lib_and_resource" name="createLibAndResource" value="<#if createLibAndResource?? && createLibAndResource==true>true<#else>false</#if>"/>
			<input type="hidden" id="validated" name="validated" value="${(file.properties.validated)!"0"}">
			<input type="hidden" id="contentHd" name="content">
			<@security.authorize access="hasAnyRole('A', 'S')">
				<#if ownerId??>
					<input type="hidden" id="ownerId" name="ownerId" value="${ownerId}"/>
				</#if>
			</@security.authorize>
		</form>


		<textarea id="codemirror_content">${((file.content)!"")?replace("&para", "&amp;para")}</textarea>
		<textarea id="old_content" class="hidden">${((file.content)!"")?replace("&para", "&amp;para")}</textarea>
		<div class="pull-right" rel="popover" style="float;margin-top:-20px;margin-right:-30px;cursor: pointer"
			title="Tip" data-html="ture"
			data-placement="left"
			data-content="
				Ctrl-F / Cmd-F : <@spring.message 'script.editor.tip.startSearching'/><br/>
				Ctrl-G / Cmd-G : <@spring.message 'script.editor.tip.findNext'/><br/>
				Shift-Ctrl-G / Shift-Cmd-G : <@spring.message 'script.editor.tip.findPrev'/><br/>
				Shift-Ctrl-F / Cmd-Option-F : <@spring.message 'script.editor.tip.replace'/><br/>
				Shift-Ctrl-R / Shift-Cmd-Option-F : <@spring.message 'script.editor.tip.replaceAll'/><br/>
				F12 : <@spring.message 'script.editor.tip.fullScreen'/><br/>
				ESC : <@spring.message 'script.editor.tip.back'/>
				"><code>Tip</code></div>
		<div id="script_samples_link" style="text-align: center;margin-top: 10px;">
			<a target="_blank" href="https://github.com/naver/ngrinder/tree/master/script-sample">Script Samples</a>
		</div>
		<div id="validation_result_panel" style="display:none;">
			<pre style="height:100px; margin:5px 0 10px; " class="prettyprint pre-scrollable" id="validation_result_pre_div">
			</pre>
			<div class="pull-right" rel="popover" style="float;margin-top:-30px;margin-right:-16px;"><a class="pointer-cursor" id="expand_btn"><code>+</code></a></div>
		</div>
	</div>
	</div>
	<#include "../common/copyright.ftl">
	<#include "../common/codemirror.ftl">
	<script src="${req.getContextPath()}/plugins/codemirror/lang/${scriptHandler.codemirrorKey!scriptHandler.getCodemirrorKey(file.fileType)}.js"></script>
	<#include "../perftest/host_modal.ftl">
	<script>
		var curRevision = ${curRevision!0};
		var lastRevision = ${lastRevision!0};

		function beforeUnload() {
			return "<@spring.message "script.editor.message.exitWithoutSave"/>";
		}
		function saveScript() {
			document.forms.content_form.action = "${req.getContextPath()}/script/save";
			document.forms.content_form.submit();
		}
		$(document).ready(function() {
			var editor = CodeMirror.fromTextArea(document.getElementById("codemirror_content"), {
				mode: "${scriptHandler.codemirrorKey!scriptHandler.getCodemirrorKey(file.fileType)}",
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
				},
				onChange : function() {
					window.onbeforeunload = beforeUnload;
				}
			});
			var hlLine = editor.setLineClass(0, "activeline");

			$("#save_btn").click(function() {
				var newContent = editor.getValue();
				if ($("#old_content").val() != newContent) {
					$("#validated").val("0");
				}
				$('#contentHd').val(newContent);
				window.onbeforeunload = undefined;
				if (curRevision > 0 && lastRevision > 0 && curRevision <  lastRevision) {
					bootbox.confirm("<@spring.message "script.editor.message.overWriteNewer"/>", "<@spring.message "common.button.cancel"/>", "<@spring.message "common.button.ok"/>", function(result) {
						if (result) {
							saveScript();
						}
					});
				} else {
					saveScript();
				}
			});

			var validating = false;
			$("#validate_btn").click(function() {
				if (validating) {
					return;
				}
				validating = true;
				var scriptPath = $("#script_name").val();
				var hostString = $("#target_hosts").val();
				$('#validation_result_panel').hide();
				$('#script_samples_link').show();
				var newContent = editor.getValue();
				showProgressBar("<@spring.message 'script.editor.message.validate'/>");

				var ajaxObj = new AjaxPostObj("/script/api/validate",
								{
									'path':scriptPath, 'content': newContent,
									<@security.authorize access="hasAnyRole('A', 'S')"><#if ownerId??>'ownerId': "${ownerId}",</#if></@security.authorize>
									'hostString': hostString
								},
								null,
								"<@spring.message 'script.editor.error.validate'/>");
				ajaxObj.success = function(res) {
					validating = false;
					$('#validation_result_pre_div').text(res);
					$('#validation_result_panel').show();
					$('#script_samples_link').hide();
					$('#validated').val("1");//should control the validation success or not later.
				};
				ajaxObj.complete = function () {
					hideProgressBar();
				};
				ajaxObj.error = function () {
					validating = false;
				};
				ajaxObj.call();

			});

			$("#expand_btn").click(function() {
				var $validationResultDiv = $("#validation_result_pre_div");
				var heightStr = $validationResultDiv.css("height");
				if (heightStr == "100px") {
					$validationResultDiv.css("height", "300px");
					editor.setSize(null, 300);
				} else {
					$validationResultDiv.css("height", "100px");
					editor.setSize(null, 500);
				}
			});
		});


		</script>
	</body>
</html>
