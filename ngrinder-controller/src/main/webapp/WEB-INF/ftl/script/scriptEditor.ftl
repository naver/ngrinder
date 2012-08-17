<!DOCTYPE html>
<html>
	<head>
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
									<input type="text" id="scriptNameInput" name="path" value="${file.path!}" readonly/>
									<a class="btn btn-success" href="javascript:void(0);" id="saveBtn" style="margin-left:310px;width:30px"><@spring.message "common.button.save"/></a>
									<a class="btn btn-primary" href="javascript:void(0);" id="validateBtn" style="width:85px"><@spring.message "script.editor.button.validate"/></a>
								</div>
							</div>
							<div style="margin-bottom: 0" class="control-group">
								<label class="control-label" for="description"><@spring.message "common.label.description"/></label>
								<div class="controls">  
									<input type="text" id="descInput" name="description" class="span9" value="${(file.description)!}">
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
					<#if oldfile?has_content> 
					<td>
						<textarea id="display_content_2" style="height:550px;width:100%;">${oldfile}</textarea>
						<div id="script_2" style="width:100%">
						</div>
					</td>
					</#if>
					</tr>
				</table>
				<pre style="height:100px; margin-top:5px;" class="prettyprint pre-scrollable hidden" id="validateRsPre"></pre>
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
				,allow_resize: true
				,allow_toggle: false
				,language: "en"
				,syntax: "python" 
				,replace_tab_by_spaces: 4
				,font_size: "10"
				,font_family: "verdana, monospace"
				,EA_load_callback: "listenEditArea"
			});
			
			editAreaLoader.init({
				id: "display_content_2"
				,is_editable: true
				,start_highlight: true
				,allow_resize: true
				,allow_toggle: false
				,language: "en"
				,syntax: "python" 
				,replace_tab_by_spaces: 4
				,font_size: "10"
				,font_family: "verdana, monospace"
			});
			
			if ("0" == "0") {
	      		$('#script_2').hide();
	      		loadCache();
	  		} else {
	      		$('#script_2').show();
	  		}
			
			$("#compareBtn").on('click', function() {
				if ($("#historySelect").val() == 0) {
					alert("Please select a history file.");
					return;
				}
				document.forms.contentForm.action = "${req.getContextPath()}/script/detail";
				document.forms.contentForm.submit();
			});

			$("#saveBtn").on('click', function() {
				var scriptContent = editAreaLoader.getValue("display_content");
				$('#contentHidden').val(scriptContent);
				
				document.forms.contentForm.action = "${req.getContextPath()}/script/save";
				document.forms.contentForm.submit();
			});

			$(".listBtn").on('click', function() {
				if (!confirm("<@spring.message "script.editor.confirm.cancelPage"/>")) {
					return;
				}

				document.location.replace("${req.getContextPath()}/script/list");
			});

			$("#validateBtn").on('click', function() {
				validateScript();
			});
		});

		function validateScript() {
			showInformation("<@spring.message "script.editor.message.validate"/>");
			
			var scriptContent = editAreaLoader.getValue("display_content");
			var scriptPath = $("#scriptNameInput").val();
			$.ajax({
		  		url: "${req.getContextPath()}/script/validate",
		    	async: true,
		    	type: "POST",
				data: {'path':scriptPath, 'content': scriptContent},
		    	success: function(res) {
					var validationInfo = "";
					$.each(res, function(i,item){
						validationInfo = validationInfo + "\n" + item + "\n";
					});
					$('#validateRsPre').text(validationInfo);
		    	},
		    	error: function() {
		    		showErrorMsg("<@spring.message "script.editor.error.validate"/>");
		    	}
		  	});
		}
		
		//TODO  is it necessary now?
		function loadCache() {
			
		}
		
		function listenEditArea() {
			clearInterval(window.interval);
			window.lastContent = editAreaLoader.getValue("display_content");
			window.interval = setInterval(function(){autoSave()}, 10000);
		}
		
		function autoSave() {
			var scriptContent = editAreaLoader.getValue("display_content");
			
			if (scriptContent == window.lastContent) {
				return;
			}
			
			$.ajax({
				url: "${req.getContextPath()}/script/autoSave",
				async: false,
				cache: false,
				type: "POST",
				dataType:'json',
				data: {'id': ${(script.getFileName())!0}, 'content': scriptContent},
		        success: function(res) {
		        	if (res.success) {
		        		showInformation("<@spring.message "script.editor.message.autoSave"/> " + new Date());
		        	} else {
		        		showErrorMsg(res.message);
		        	}
		        },
		        timeout: function() {
		        	showErrorMsg("<@spring.message "script.editor.error.autoSave"/>");  
		        },
		        error: function() {
		        	showErrorMsg("<@spring.message "script.editor.error.autoSave"/>");  
		        }
			});
			
			window.lastContent = scriptContent;
		}
	</script>
	</body>
</html>