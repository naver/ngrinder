<!DOCTYPE html>
<html>
	<head>
		<title>nGrinder Script List</title>
		<#include "../common/common.ftl">
		<#include "../common/datatables.ftl">
	</head>

	<body>
    <#include "../common/navigator.ftl">
	<div class="container">
		<div class="well form-inline searchBar" style="margin-top:0;">
			<!--<legend>introduction</legend>-->
			<input type="text" class="search-query" placeholder="Keywords" id="searchText" value="${keywords!}">
			<button type="submit" class="btn" id="searchBtn"><i class="icon-search"></i> Search</button>
			<#if svnUrl?has_content>
			<div class="input-prepend pull-right"> 
               <span class="add-on" style="cursor:default">SVN</span><span class="input-xlarge uneditable-input span6" style="cursor:text">${svnUrl}</span>
        	</div> 
        	</#if>	
        	<div style="margin-top:10px">
				<a class="btn" href="#createScriptModal" id="createBtn" data-toggle="modal">
					<i class="icon-file"></i>
					Create a script
				</a>
				<a class="btn" href="#createFolderModal" id="folderBtn" data-toggle="modal">
					<i class=" icon-folder-open"></i>
					Create a folder
				</a>
				<a class="btn" href="#uploadScriptModal" id="uploadBtn" data-toggle="modal">
					<i class="icon-upload"></i>
					Upload script or resources
				</a>
				<a class="btn btn-danger pull-right" href="javascript:void(0);" id="deleteBtn">
					<i class="icon-remove"></i>
					Delete selected scripts
				</a>
			</div>
		</div>
			
		<table class="table table-striped table-bordered ellipsis" id="scriptTable">
			<colgroup>
				<col width="30">
				<col width="35">
				<col width="160">
				<col>
				<col width="170">
				<col width="100">
				<col width="85">
			</colgroup>
			<thead>
				<tr>
					<th><input type="checkbox" class="checkbox" value=""></th>
					<th><a href="${req.getContextPath()}/script/list/${currentPath}/../" target="_self"><img src="${req.getContextPath()}/img/up_folder.png"/></a> 
					</th>
					<th>Script File Name</th>
					<th class="noClick">Commit Message</th>
					<th>Last Modified Date</th>
					<th>Size(KB)</th>
					<th class="noClick">Download</th>
				</tr>
			</thead>
			<tbody>
				<#if files?has_content>	
					<#list files as script>
						<tr>
							<td><#if script.fileName != ".."><input type="checkbox" value="${script.fileName}"></#if></td>
							<td>
								<#if script.fileType.fileCategory.isEditable()>
									<i class="icon-file"></i>
								<#elseif script.fileType == "dir">
									<i class="icon-folder-open"></i>
								<#else>	
									<i class="icon-briefcase"></i>
								</#if>
							</td>
							<td>
								<#if script.fileType.fileCategory.isEditable()>
									<a href="${req.getContextPath()}/script/detail/${script.path}" target="_self">${script.fileName}</a>
								<#elseif script.fileType == "dir">
									<a href="${req.getContextPath()}/script/list/${script.path}" target="_self">${script.fileName}</a>
								<#else>	
									<a href="${req.getContextPath()}/svn/${currentUser.userId}/${script.path}" target="_blank">${script.fileName}</a>
								</#if>
							</td>
							<td class="ellipsis" title="${(script.description)!}">${(script.description)!}</td>
							<td><#if script.lastModifiedDate?exists>${script.lastModifiedDate?string('yyyy-MM-dd HH:mm:ss')}</#if></td>
							<td><#assign floatSize = script.fileSize?number/1024>${floatSize?string("0.##")}</td>
							<td><a href="javascript:void(0);"><i class="icon-download-alt script-download" spath="${script.path}" sname="${script.fileName}"></i></a></td>
						</tr>
					</#list>
				<#else>
					<tr>
						<td colspan="7" class="noData">
							No data to display.
						</td>
					</tr>
				</#if>		
				</tbody>
				</table>
				<#include "../common/copyright.ftl">
			</div>
		</div>

		<div class="modal fade" id="createScriptModal">
			<div class="modal-header">
				<a class="close" data-dismiss="modal" id="createCloseBtn">&times;</a>
				<h3>Create a script</h3>
			</div>
			<div class="modal-body">
				<form class="form-horizontal" method="post" target="_self" id="createForm" action="${req.getContextPath()}/script/create/${currentPath}">
					<fieldset>
						<div class="control-group">
							<label for="scriptNameInput" class="control-label">Script Name</label>
							<div class="controls">
							  <input type="text" id="scriptNameInput" name="fileName">
							  <span class="help-inline"></span>
							</div>
						</div>
						<div class="control-group">
							<label for="languageSelect" class="control-label">Type</label>
							<div class="controls">
								<input type="hidden" name="type" value="script"/>
								<select id="languageSelect" name="scriptType">
									<option value="py">PythonScript</option>
								</select>
							  <span class="help-inline"></span>
							</div>
						</div>
						<div class="control-group">
							<label for="urlInput" class="control-label">URL to be tested</label>
							<div class="controls">
							  <input type="text" id="urlInput" name="testUrl"/>
							  <span class="help-inline"></span>
							</div>
						</div>					
					</fieldset>
				</form>
			</div>
			
			<div class="modal-footer">
				<a href="#" class="btn btn-primary" id="createBtn2">Create</a>
				<a href="#createScriptModal" class="btn" id="cancelBtn" data-toggle="modal">Cancel</a>
			</div>
		</div>
		
		<div class="modal fade" id="createFolderModal">
			<div class="modal-header">
				<a class="close" data-dismiss="modal" id="createCloseBtn">&times;</a>
				<h3>Create a folder</h3>
			</div>
			<div class="modal-body">
				<form class="form-horizontal" method="post" target="_self" id="createFolderForm" action="${req.getContextPath()}/script/create/${currentPath}">
					<fieldset>
						<div class="control-group">
							<label for="folderNameInput" class="control-label">Folder Name</label>
							<div class="controls">
							  <input type="hidden" name="type" value="folder"/>
							  <input type="text" id="folderNameInput" name="folderName"/>
							  <span class="help-inline"></span>
							</div>
						</div>					
					</fieldset>
				</form>
			</div>
			
			<div class="modal-footer">
				<a href="#" class="btn btn-primary" id="createFolderBtn">Create</a>
				<a href="#createFolderModal" class="btn" id="cancelBtn" data-toggle="modal">Cancel</a>
			</div>
		</div>
	
		<div class="modal fade" id="uploadScriptModal">
			<div class="modal-header">
				<a class="close" data-dismiss="modal" id="upCloseBtn">&times;</a>
				<h3>Upload a JavaScript or Selenium File</h3>
			</div>
			<div class="modal-body">
				<form class="form-horizontal" method="post" target="_self" action="${req.getContextPath()}/script/upload"
						id="uploadForm" enctype="multipart/form-data">
					<fieldset>
						<div class="control-group">
							<label for="upScriptNameInput" class="control-label">Name</label>
							<div class="controls">
							  <input type="text" id="upScriptNameInput" name="fileName">
							  <input type="hidden" id="path" name="path"/>
							  <span class="help-inline"></span>
							</div>
						</div>
						<div class="control-group">
							<label for="discriptionInput" class="control-label">Description</label>
							<div class="controls">
							  <input type="text" id="discriptionInput" name="description">
							  <span class="help-inline"></span>
							</div>
						</div>
						<div class="control-group">
							<label for="fileInput" class="control-label">File</label>
							<div class="controls">
							  <input type="file" class="input-file" id="fileInput" name="uploadFile">
							  <span class="help-inline"></span>
							</div>
						</div>				
					</fieldset>
				</form>
			</div>
			<div class="modal-footer">
				<a href="#" class="btn btn-primary" id="uploadBtn2">Upload</a>
			</div>
		</div>
	</div>

	<script>
		$(document).ready(function() {
			$("#n_script").addClass("active");
			
			$("#createBtn2").on('click', function() {
				var $elem = $("#scriptNameInput");
				if (checkSimpleNameByObj($elem)) {
					cleanErrMsg($elem);
				} else {
					showErrMsg($elem, "Script name is not correct.");
					return;
				}

				var $ext = $("#languageSelect");
				if (checkScriptFileExtension($elem.val(), $ext.val())) {
					cleanErrMsg($elem);
				} else {
					if (!confirm("You already chose " + $ext.text().trim() + " as a script type.\nMay I append ." + $ext.val() + " file extension after your script name?")) {
						showErrMsg($elem, "You should append '." + $ext.val() + "' extension of a script file.");
						return;
					}
					$elem.val($elem.val() + ".py");
				}

				$elem = $("#urlInput");
				if (checkEmpty($elem)) {
					showErrMsg($elem, "URL can't be empty.");
					return;
				} else {
					cleanErrMsg($elem);
				}

				if ($elem.val().indexOf("http://") != -1 && $elem.val().indexOf("https://") != -1) {
					cleanErrMsg($elem);
				} else {
					if (confirm("You omit a url type.\nMay I append a url type as 'http://' on the url?")) {
						$elem.val("http://" + $elem.val());
					}
				}

				if (!confirm("Are you sure to continue?")) {
					return;
				}

				document.forms.createForm.submit();
			});
			
			$("#uploadBtn2").on('click', function() {
				var $elem = $("#upScriptNameInput");
				if (checkSimpleNameByObj($elem)) {
					cleanErrMsg($elem);
				} else {
					showErrMsg($elem, "Script name is not correct.");
					return;
				}
				
				$elem = $("#discriptionInput");
				if (checkEmpty($elem)) {
					showErrMsg($elem, "Description can't be empty.")
					return;
				} else {
					cleanErrMsg($elem);
				}
				
				$elem = $("#fileInput");
				if (checkEmpty($elem)) {
					showErrMsg($elem, "Please set a file.");
					return;
				} else {
					cleanErrMsg($elem);
				}
				$("#path").val($("#upScriptNameInput").val());
				document.forms.uploadForm.submit();
			});
			
			$("#createFolderBtn").on('click', function() {
				var $elem = $("#folderNameInput");
				if (checkSimpleNameByObj($elem)) {
					cleanErrMsg($elem);
				} else {
					showErrMsg($elem, "Script name not correct.");
					return;
				}
				document.forms.createFolderForm.submit();
			});
						
			$("#deleteBtn").on('click', function() {
				var ids = "";
				var list = $("td input:checked");
				if(list.length == 0) {
					alert("Please select any scripts first.");
					return;
				}
				if (confirm('Are you sure to delete the script(s)?')) {
					var agentArray = [];
					list.each(function() {
						agentArray.push($(this).val());
					});
					ids = agentArray.join(",");
					
					document.location.href = "${req.getContextPath()}/script/delete/${currentPath}?filesString=" + ids;
				}
			});
			
			$("#searchBtn").on('click', function() {
				searchScriptList();
			});

			$("#onlyMineCkb").on('click', function() {
				searchScriptList();
			});
			
			$("td input").on("click", function() {
				if($("td input").size() == $("td input:checked").size()) {
						$("th input").attr("checked", "checked");
				} else {
					$("th input").removeAttr("checked");
				}
			});
			
			$("th input").on('click', function(event) {
				if($(this)[0].checked) {
					$("td input").each(function(){
						$(this).attr("checked", "checked");
					});
				} else {
					$("td input").each(function() {
						$(this).removeAttr("checked");
					});
				}
				
				event.stopImmediatePropagation();
			});
			
			$("i.script-remove").on('click', function() {
				if (confirm("Do you want to delete this script file?")) {
					document.location.href = "${req.getContextPath()}/script/delete/${currentPath}?filesString=" + $(this).attr("sid");
				}
			});
			
			$("i.script-download").on('click', function() {
				var $elem = $(this);
				document.forms.downloadForm.action = "${req.getContextPath()}/svn/" + $elem.attr("spath");
				document.forms.downloadForm.submit();
			});

			<#if files?has_content>
				$("#scriptTable").dataTable({
					"bAutoWidth": false,
					"bFilter": false,
					"bLengthChange": false,
					"bInfo": false,
					"iDisplayLength": 10,
					"aaSorting": [[2, "asc"]],
					"bProcessing": true,
					"aoColumns": [{ "asSorting": []}, { "asSorting": []}, null, { "asSorting": []}, null, null, { "asSorting": []}],
					"sPaginationType": "bootstrap"
				});
			</#if>
			
			$(".noClick").off('click');
			
		});
		
		function searchScriptList() {
			document.location.href = "${req.getContextPath()}/script/search?query=" + $("#searchText").val();
		}
	</script>
	</body>
</html>