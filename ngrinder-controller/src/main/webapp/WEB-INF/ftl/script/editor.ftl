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
				margin-top:-25px;
				margin-right:67px 
			}
			
			div.modal-body div.chart {
				border:1px solid #878988; 
				height:250px; 
				min-width:500px; 
				margin-bottom:12px; 
				padding:5px 
		    }
		</style>
	</head>

	<body>
	<#include "../common/navigator.ftl">
	<div class="container">
		<div class="row">
			<div class="span12">
				<form id="contentForm" class="well" method="post" target="_self" style="margin-bottom:10px;"> 	
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
												<label class="control-label" for="testName"><@spring.message "script.option.name"/></label>
											</td>
											<td>
												<input type="text" id="scriptNameInput" class="span6" name="path" value="${(file.path)!}" readonly/>
											</td>
											<td>
											<#if scriptHandler.isValidatable()>
												<a class="btn btn-success" href="javascript:void(0);" id="saveBtn" style="margin-left:73px; width:40px;"><@spring.message "common.button.save"/></a>
												<a class="btn btn-primary" href="javascript:void(0);" id="validateBtn" style="width:90px;"><@spring.message "script.editor.button.validate"/></a>
											<#else>
												<a class="btn btn-success" href="javascript:void(0);" id="saveBtn" style="margin-left:190px; width:40px;"><@spring.message "common.button.save"/></a>
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
											<label class="control-label" for="description"><@spring.message "script.option.commit"/></label>
										</td>
										<td>
											<textarea class="span6" id="descInput" name="description" style="resize:none; height:55px" >${(file.description)!}</textarea>
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
					<input type="hidden" id="createLibAndResource" name="createLibAndResource" value="<#if createLibAndResource?? && createLibAndResource==true>true<#else>false</#if>"/>
					<input type="hidden" id="validatedHd" name="validated" value="${(file.properties.validated)!"0"}">
					<input type="hidden" id="contentHd" name="content">
					<@security.authorize ifAnyGranted="A, S">
						<#if ownerId??>					
							<input type="hidden" id="ownerId" name="ownerId" value="${ownerId}"/>
						</#if>
					</@security.authorize>
				</form>
				
				
				<textarea id="codemirrorContent">${(file.content)!}</textarea>
				<textarea id="oldContent" class="hidden">${(file.content)!}</textarea>
				<div class="pull-right" rel="popover" style="position:float;margin-top:-20px;margin-right:-30px" data-original-title="Tip" data-content="
			      Ctrl-F / Cmd-F : <@spring.message "script.editor.tip.startSearching"/>&lt;br&gt; 
			      Ctrl-G / Cmd-G : <@spring.message "script.editor.tip.findNext"/>&lt;br&gt;
			      Shift-Ctrl-G / Shift-Cmd-G : <@spring.message "script.editor.tip.findPrev"/>&lt;br&gt;
			      Shift-Ctrl-F / Cmd-Option-F : <@spring.message "script.editor.tip.replace"/>&lt;br&gt;
			      Shift-Ctrl-R / Shift-Cmd-Option-F : <@spring.message "script.editor.tip.replaceAll"/>&lt;br&gt;
			      F12 : <@spring.message "script.editor.tip.fullScreen"/>&lt;br&gt;
			      ESC : <@spring.message "script.editor.tip.back"/>&lt;br&gt;
			      " placement="left"
			    ><code>Tip</code></div> 
			</div>
		</div>
		<div id="validationPanel" style="display:none;">
			<pre style="height:100px; margin:5px 0 10px; " class="prettyprint pre-scrollable" id="validateRsPre">
			</pre>
			<div class="pull-right" rel="popover" style="position:float;margin-top:-30px;margin-right:-16px;"><a href="javascript:void(0)" id="expandBtn"><code>+</code></a></div>
		</div>		 
		<#include "../common/copyright.ftl"> 
	</div>
	
	<#include "../common/codemirror.ftl">
	<script src="${req.getContextPath()}/plugins/codemirror/lang/${scriptHandler.codemirrorKey!scriptHandler.getCodemirrorKey(file.fileType)}.js"></script>
    <#include "../common/datatables.ftl">
    <script>
    	changed = false;
    	$(window).on('beforeunload', function() {
    		if (changed == true) {
    			return "<@spring.message "script.editor.message.exitwithoutsave"/>";
    		}
    	});
    	$(document).ready(function() {
			var editor = CodeMirror.fromTextArea(document.getElementById("codemirrorContent"), {
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
				   changed = true;
			   }
			});
			var hlLine = editor.setLineClass(0, "activeline");

			$("#saveBtn").click(function() {
				var newContent = editor.getValue();
				if ($("#oldContent").val() != newContent) {
					$("#validatedHd").val("0");
				}
				$('#contentHd').val(newContent);
				changed = false;
				document.forms.contentForm.action = "${req.getContextPath()}/script/save";
				document.forms.contentForm.submit();
			});

			var validating = false;
			$("#validateBtn").click(function() {
				if (validating) {
					return;
				}
				validating = true;
				var scriptPath = $("#scriptNameInput").val();
				var hostString = $("#targetHosts").val();
				$('#validationPanel').hide();
				var newContent = editor.getValue();
				showProgressBar("<@spring.message "script.editor.message.validate"/>");
				$.ajax({
			  		url: "${req.getContextPath()}/script/validate",
			    	async: true,
			    	type: "POST",
					data: {'path':scriptPath, 'content': newContent, 
						<@security.authorize ifAnyGranted="A, S">
							<#if ownerId??>	
				  				'ownerId': "${ownerId}",
							</#if>
						</@security.authorize>
					'hostString': hostString},
			    	success: function(res) {
			    		validating = false;
						$('#validateRsPre').text(res);
						$('#validationPanel').show();
						$('#validatedHd').val("1");//should control the validation success or not later.
						$("#oldContent").val(newContent);
						hideProgressBar();
			    	},
			    	error: function() {
			    		validating = false;
			    		showErrorMsg("<@spring.message "script.editor.error.validate"/>");
			    	}
			  	});
			});
			
			$("#expandBtn").click(function() {
				var heightStr = $("#validateRsPre").css("height");
				if (heightStr == "100px") {
					$("#validateRsPre").css("height", "300px");
				} else {
					$("#validateRsPre").css("height", "100px");
				}
			});
		});
    	
    
		</script>
	</body>
</html>
