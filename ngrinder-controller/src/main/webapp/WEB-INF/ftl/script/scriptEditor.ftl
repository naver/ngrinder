<!DOCTYPE html>
<html>
	<head>	
		<meta http-equiv="X-UA-Compatible" content="IE=8" />
		<#include "../common/common.ftl">
		<title><@spring.message "script.editor.title"/></title>
	</head>

	<body>
	<#include "../common/navigator.ftl">
	<div class="container">
		<div class="row">
			<div class="span12">
				<form id="contentForm" method="post" target="_self">
				<div class="well" style="margin-bottom:20px">
					<div class="form-horizontal form-horizontal-1">
						<fieldset>
							<div class="control-group">
								<label class="control-label" for="testName"><@spring.message "script.option.name"/></label>
								<div class="controls">   
									<input type="text" id="scriptNameInput" class="span7" name="path" value="${file.path!}" readonly/>
									<a class="btn btn-success" href="javascript:void(0);" id="saveBtn" ><@spring.message "common.button.save"/></a>
									<a class="btn btn-primary" href="javascript:void(0);" id="validateBtn" ><@spring.message "script.editor.button.validate"/></a>
								</div>
							</div>
							<div style="margin-bottom: 0" class="control-group">
								<label class="control-label" for="description"><@spring.message "script.option.commit"/></label>
								<div class="controls">  
									<textarea class="input-xlarge span9" id="descInput" rows="3" name="description" style="resize: none" >
										${(file.description)!}
									</textarea>
								</div>
							</div>
						</fieldset>
					</div>
				</div>
				<input type="hidden" id="contentHidden" name="content" value="">
				</form>
				
				<table style="border:none;width:100%">
					<tr>
						<td>
							<div id="script_1" style="width:100%">
								<textarea id="display_content" name="content" style="height:550px;width:100%;">${(file.content)!}</textarea>
							</div>
						</td>
					</tr>
				</table>
				<pre style="height:100px; margin-top:5px;" class="prettyprint pre-scrollable hidden" id="validateRsPre">
				</pre>
			</div>	
		</div>
		<#include "../common/copyright.ftl">
	</div>
	
	
	<script src="${req.getContextPath()}/plugins/editarea/edit_area.js"></script>
	<script>
		$(document).ready(function() {
			$("#n_script").addClass("active");
			
			editAreaLoader.baseURL = "${req.getContextPath()}/plugins/editarea/"; 
			
			editAreaLoader.init({
				id: "display_content"
				,is_editable: true
				,start_highlight: true
				,allow_resize: false
				,allow_toggle: false
				,language: "en"
				,syntax: "python" 
				,replace_tab_by_spaces: 4
				,font_size: "10"
				,font_family: "verdana, monospace"
			});
			
			$("#saveBtn").on('click', function() {
				var scriptContent = editAreaLoader.getValue("display_content");
				$('#contentHidden').val(scriptContent);
				
				document.forms.contentForm.action = "${req.getContextPath()}/script/save";
				document.forms.contentForm.submit();
			});

			$("#validateBtn").on('click', function() {
				validateScript();
			});
		});

		function validateScript() {
			showInformation("<@spring.message "script.editor.message.validate"/>");
			var scriptContent = editAreaLoader.getValue("display_content");
			var scriptPath = $("#scriptNameInput").val();
			$('#validateRsPre').attr("class", "prettyprint pre-scrollable hidden");
			$.ajax({
		  		url: "${req.getContextPath()}/script/validate",
		    	async: true,
		    	type: "POST",
				data: {'path':scriptPath, 'content': scriptContent},
		    	success: function(res) {
					var validationInfo = "";
					$('#validateRsPre').text(res);
					$('#validateRsPre').attr("class", "prettyprint pre-scrollable");
		    	},
		    	error: function() {
		    		showErrorMsg("<@spring.message "script.editor.error.validate"/>");
		    	}
		  	});
		}
		</script>
	</body>
</html>