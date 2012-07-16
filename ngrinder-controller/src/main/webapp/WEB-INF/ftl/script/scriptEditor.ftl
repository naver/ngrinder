<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8" />
		<title>nGrinder Script Editor</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0" />
		<meta name="description" content="nGrinder Test Result Detail" />
		<meta name="author" content="AlexQin" />

		<link rel="shortcut icon" href="${req.getContextPath()}/favicon.ico"/>
		<link href="${req.getContextPath()}/css/bootstrap.min.css" rel="stylesheet">
		<link href="${req.getContextPath()}/css/ngrinder.css" rel="stylesheet">
		
		<input type="hidden" id="contextPath" value="${req.getContextPath()}" />
		<#setting number_format="computer">
	</head>

	<body>
	<#include "../common/navigator.ftl">
	<div class="container">
		<form id="contentForm" method="post" target="_self">
			<a class="btn saveBtn" href="javascript:void(0);">Save</a>
			<a class="btn" href="javascript:void(0);" id="validateTopBtn">Validate Script</a>
			<a class="btn listBtn" href="javascript:void(0);">Go back</a>
			<div class="alert alert-info fade in" style="margin-top:5px;" id="autoSaveMsgTop"></div>

			<div class="well form-inline" style="padding:5px;margin:5px 0">
				<label class="label" for="scriptNameInput">
					Script Name
				</label>
				<input type="text" id="scriptNameInput" name="fileName" value="${(result.fileName)!}" readonly="readonly" />
				<input type="hidden" id="scriptId" name="id" value="${(result.id)!}" />
				<#if result.historyFileNames?has_content>
				<div class="pull-right">
				<label class="label" for="historySelect">
					History
				</label>
				<select id="historySelect" name="historyFileName">
					<option value="0">History List</option>
					<#list result.historyFileNames as fileName>
					<option value="${fileName}" <#if historyFileName?has_content && fileName == historyFileName>selected</#if>>${fileName}</option>
					</#list>
				</select>
				<a class="btn" href="javascript:void(0);" id="compareBtn">Compare</a>
				</div>
				</#if>
			</div>
			<table style="border:none;width:100%">
				<tr>
					<td>
						<div id="script_1" style="width:100%">
							<textarea id="display_content" name="content" style="height:550px;width:100%;">${(result.content)!}</textarea>
						</div>
					</td>
					<td>
						<div id="script_2" style="width:100%">
							<textarea id="display_content_2" style="height:550px;width:100%;">${(result.historyContent)!}</textarea>
						</div>
					</td>
			</table>
			
			<div class="well form-inline" style="padding:5px;margin:5px 0">
				<table>
					<tr>
						<td widht="40px">
							<label class="label" for="tagsInput">
								Tags
							</label>
						</td>
						<td>
							<input type="text" id="tagsInput" name="tagsString" class="span4" value="${(result.tagsString)!}">&nbsp;&nbsp;
						</td>
						<td>
							<label class="label" for="descInput">
								Description
							</label>
						</td>
						<td>
							<input type="text" id="descInput" name="description" style="min-width:500px" value="${(result.description)!}">
						</td>
					</tr>
				</table>
			</div>

			<a class="btn saveBtn" href="javascript:void(0);">Save</a>
			<a class="btn" href="javascript:void(0);" id="validateBottomBtn">Validate Script</a>
			<a class="btn listBtn" href="javascript:void(0);">Go back</a>

			<span class="help-inline" id="messageDiv"></span>
			<div class="alert alert-info fade in" style="margin-top:5px;" id="autoSaveMsgBottom"></div>
			<pre style="height:100px; margin-top:5px;" class="prettyprint pre-scrollable hidden" id="validateRsPre"></pre>
		</form>
	</div>

	<script src="${req.getContextPath()}/js/jquery-1.7.2.min.js"></script>
	<script src="${req.getContextPath()}/js/bootstrap.min.js"></script>
	<script src="${req.getContextPath()}/plugins/editarea/edit_area.js"></script>
	<script>
		$(document).ready(function() {
			$("#n_script").addClass("active");
			
			editAreaLoader.baseURL = "../plugins/editarea/";
			
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
			
			if (${historyFileName!"0"} == "0") {
	      		$('#script_2').hide();
	      		loadCache();
	  		} else {
	      		$('#script_2').show();
	  		}
	  		
	  		$('#autoSaveMsgTop').fadeOut();
	  		$('#autoSaveMsgBottom').fadeOut();
			
			$("#compareBtn").on('click', function() {
				if ($("#historySelect").val() == 0) {
					alert("Please select a history file.");
					return;
				}
				
				document.forms.contentForm.action = "${req.getContextPath()}/script/detail";
				document.forms.contentForm.submit();
			});

			$(".saveBtn").on('click', function() {
				var scriptContent = editAreaLoader.getValue("display_content");
				$('#display_content').val(scriptContent);
				
				document.forms.contentForm.action = "${req.getContextPath()}/script/save";
				document.forms.contentForm.submit();
			});

			$(".listBtn").on('click', function() {
				if (!confirm("You are writting this script.\nAre you sure to cancel modified script and move the scripts list?")) {
					return;
				}

				document.location.replace("${req.getContextPath()}/script/list");
			});

			$("#validateTopBtn").on('click', function() {
				validateScript(true);
			});

			$("#validateBottomBtn").on('click', function() {
				validateScript(false);
			});
		});

		function validateScript(isTopPosition) {
			var scriptContent = editAreaLoader.getValue("display_content");
			$('#messageDiv').ajaxSend(function() {
			  $(this).html("Validating script......");
			});

			$.ajax({
		  		url: "${req.getContextPath()}/script/validate",
		    	async: true,
				dataType:'json',
				data: {'scriptContent': scriptContent},
		    	success: function(res) {
		    		if (res.success) {
						var validationInfo = "";
						$.each(res, function(i,item){
							validationInfo = validationInfo + "\n" + item + "\n";
						});
						$('#messageDiv').html("");

						if (isTopPosition)
							$('#validateRsPreTop').text(validationInfo);
						else
							$('#validateRsPreBottom').text(validationInfo);
		    		} else {
		    			showMsg("Validation error:" + res.message, isTopPosition);
		    		}
		    	},
		    	error: function() {
		    		$('#messageDiv').html("");
		    		showMsg("Validate Script error.", isTopPosition);
		    	}
		  	});
		}

		function showMsg(message, isTopPosition) {
        	var $autoMsg = isTopPosition ? $('#autoSaveMsgTop') : $('#autoSaveMsgBottom');
        	$autoMsg.html(message);
        	$autoMsg.fadeIn("fast");

    		setTimeout(function(){$autoMsg.fadeOut('fast')}, 3000);
		}
		
		function loadCache() {
			var cacheContent = '${(result.cacheContent)!}';
			if (cacheContent != "" && cacheContent != window.lastContent) {
				if (confirm("Do you want to load cache content?")) {
					editAreaLoader.setValue("display_content", cacheContent);
				}
			}
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
				data: {'id': ${(result.id)!0}, 'content': scriptContent},
		        success: function(res) {
		        	if (res.success) {
		        		showMsg("Auto save script at " + new Date(), false)
		        	} else {
		        		showMsg(res.message, false);
		        	}
		        },
		        timeout: function() {
		        	showMsg("Auto save script is time out.", false);  
		        },
		        error: function() {
		        	showMsg("Auto save script is error.", false);  
		        }
			});
			
			window.lastContent = scriptContent;
		}
	</script>
	</body>
</html>