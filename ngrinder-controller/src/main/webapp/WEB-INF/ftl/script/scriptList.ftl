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
		<#if svnUrl?has_content>
			<div class="well form-inline searchBar">
				svn URL : ${svnUrl}
			</div>
		</#if>

		<div class="row">
			<div class="span12">
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

		<div class="well form-inline searchBar">
			<!--<legend>introduction</legend>-->
			<input type="text" class="search-query" placeholder="Keywords" id="searchText" value="${keywords!}">
			<button type="submit" class="btn" id="searchBtn">Search</button>
		</div>
		<table class="display ellipsis jsTable" id="scriptTable">
			<colgroup>
				<col width="35">
				<col width="160">
				<col>
				<col width="170">
				<col width="160">
				<col width="100">
				<col width="100">
				<col width="80">
			</colgroup>
			<thead>
				<tr>
					<th><input type="checkbox" class="checkbox" value=""></th>
					<th>Script File Name</th>
					<th class="noClick">Description</th>
					<th>Last Modified Date</th>
					<th>Last Modified By</th>
					<th>Size(KB)</th>
					<th class="noClick">Tags</th>
					<th class="noClick">Download</th>
				</tr>
			</thead>
			<tbody>		
						<#list files as script>
						<tr>
							<td><input type="checkbox" value="${script.fileName}"></td>
							<td class="left">
								<#if script.fileType.fileCategory.isEditable()>
									<a href="${req.getContextPath()}/script/detail/${script.path}" target="_self">${script.fileName}</a>
								<#elseif script.fileType == "dir">
									<a href="${req.getContextPath()}/script/list/${script.path}" target="_self">${script.fileName}</a>
								<#else>	
									<a href="${req.getContextPath()}/svn/${currentUser.userId}${script.path}" target="_self">${script.fileName}</a>
								</#if>
								</td>
							<td class="left ellipsis" title="${(script.description)!}">${(script.description)!}</td>
							<td><#if script.lastModifiedDate?exists>${script.lastModifiedDate?string('yyyy-MM-dd HH:mm:ss')}</#if></td>
							<td>${(script.lastModifiedUser.userName)!}</td>
							<td>${(script.fileSize)!0}</td>
							<td class="left ellipsis" title="${(script.tagsString)!}">${(script.tagsString)!}</td>
							<td><a href="javascript:void(0);"><i class="icon-download-alt script-download" spath="${script.path}" sname="${script.fileName}"></i></a></td>
						</tr>
						</#list>
						
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
				<form class="form-horizontal" style="margin-bottom:0" method="post" target="_self" id="createForm" action="${req.getContextPath()}/script/create/${currentPath}">
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
								<select id="languageSelect" name="language">
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
				<form class="form-horizontal" style="margin-bottom:0" method="post" target="_self" id="createFolderForm" action="${req.getContextPath()}/script/create/${currentPath}">
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
				<form class="form-horizontal" style="margin-bottom:0" method="post" target="_self" action="${req.getContextPath()}/script/upload"
						id="uploadForm" enctype="multipart/form-data">
					<fieldset>
						<div class="control-group">
							<label for="upScriptNameInput" class="control-label">Name</label>
							<div class="controls">
							  <input type="text" id="upScriptNameInput" name="fileName">
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
				
				$elem = $("#urlInput");
				if (checkEmpty($elem)) {
					showErrMsg($elem, "URL can't be empty.");
					return;
				} else {
					cleanErrMsg($elem);
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
				"aaSorting": [[1, "asc"]],
				"bProcessing": true,
				"aoColumns": [{ "asSorting": []}, null, { "asSorting": []}, null, null, null, {"asSorting": []}, { "asSorting": []}],
				//"bJQueryUI": true,
				//"oLanguage": {"sLengthMenu": "每页显示 _MENU_ 条记录","sZeroRecords": "抱歉， 没有找到","sInfo": "从 _START_ 到 _END_ /共 _TOTAL_ 条数据","sInfoEmpty": "没有数据","sInfoFiltered": "(从 _MAX_ 条数据中检索)","oPaginate": {"sFirst": "首页","sPrevious": "前一页","sNext": "后一页","sLast": "尾页"},"sZeroRecords": "没有检索到数据"},
				"sPaginationType": "full_numbers"
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