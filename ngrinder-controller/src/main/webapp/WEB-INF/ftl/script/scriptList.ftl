<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title>nGrinder Script List</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta name="description" content="nGrinder Test Result Detail">
		<meta name="author" content="AlexQin">

		<link rel="shortcut icon" href="favicon.ico"/>
		<link href="${Request.getContextPath()}/css/bootstrap.min.css" rel="stylesheet">
		<link href="${Request.getContextPath()}/css/bootstrap-responsive.min.css" rel="stylesheet">
		<link href="${Request.getContextPath()}/plugins/datatables/css/demo_table.css" rel="stylesheet">
		<link href="${Request.getContextPath()}/plugins/datatables/css/demo_page.css" rel="stylesheet">
		<link href="${Request.getContextPath()}/plugins/datatables/css/demo_table_jui.css" rel="stylesheet">
		<link href="${Request.getContextPath()}/plugins/google_code_prettify/prettify.css" rel="stylesheet">
		<style>
			body {
				padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
			}
			table th, table td {text-align: center;}
			table.display thead th {padding: 3px 10px}
			table.display tbody .left {text-align: left}
		</style>
		
		<input type="hidden" id="contextPath" value="${Request.getContextPath()}">
		<#setting number_format="computer">
	</head>

	<body>
    <#include "../common/navigator.ftl">
	<div class="container">
		<div class="row">
			<div class="span10 offset1">
				<div class="row">
					<div class="span10">
						<a class="btn" href="#createScriptModal" id="createBtn" data-toggle="modal">
							<i class="icon-file"></i>
							Create a script
						</a>
						<a class="btn" href="#uploadScriptModal" id="uploadBtn" data-toggle="modal">
							<i class="icon-upload"></i>
							Upload script or resources
						</a>
						<a class="btn pull-right" href="javascript:void(0);" id="downloadBtn">
							<i class="icon-download"></i>
							Download selected script or resources
						</a>
					</div>
				</div>
				<div class="well form-inline" style="padding:5px;margin:10px 0">
					<!--<legend>introduction</legend>-->
					<input type="text" class="input-medium search-query" placeholder="Keywords" id="searchText" value="${keywords!}">
					<button type="submit" class="btn" id="searchBtn">Search</button>
					<label class="checkbox pull-right" style="position:relative;top:5px">
						<input type="checkbox" id="onlyMineCkb" <#if isOwner>checked</#if>> See only my script
					</label>
				</div>
				<table class="display" id="scriptTable" style="margin-bottom:10px;">
					<thead>
						<tr>
							<th class="center"><input type="checkbox" class="checkbox noClick" value=""></th>
							<th>Script File Name</th>
							<th>Last Test Date</th>
							<th>Last Modified Date</th>
							<th>Size(KB)</th>
							<th class="noClick">Download</th>
							<th class="noClick">Del</th>
						</tr>
					</thead>
					<tbody>
						<#assign scriptList = scripts.content/>
						<#if scriptList?has_content>
						<#list scriptList as script>
						<tr>
							<td><input type="checkbox" value="${script.id}"></td>
							<td class="left"><a href="${Request.getContextPath()}/script/detail?id=${script.id}" target="_self">${script.fileName}</a></td>
							<td><#if script.lastTestDate?exists>${script.lastTestDate?string('yyyy-MM-dd HH:mm:ss')}</#if></td>
							<td><#if script.lastModifiedDate?exists>${script.lastModifiedDate?string('yyyy-MM-dd HH:mm:ss')}</#if></td>
							<td>${(script.fileSize)!0}</td>
							<td><a href="javascript:void(0);"><i class="icon-download-alt script-download" sid="${script.id}" sname="${script.fileName}"></i></a></td>
							<td><a href="javascript:void(0);"><i class="icon-remove script-remove" sid="${script.id}"></i></a></td>
						</tr>
						</#list>
						<#else>
							<tr>
								<td colspan="8">
									No data to display.
								</td>
							</tr>
						</#if>
					</tbody>
				</table>
				<div class="page-header" style="margin:65px 0 10px; padding-bottom:5px;">
					<h3>Resource List</h3>
				</div>
				<table class="display" id="resourceTable">
					<thead>
						<tr>
							<th>Resource Name</th>
							<th>Size(KB)</th>
							<th class="noClick">Download</th>
							<th class="noClick">Del</th>
						</tr>
					</thead>
					<tbody>
						<#if libraries?has_content>
							<#list libraries as library>
							<tr>
								<td class="left">${library.fileName}</td>
								<td>${(library.fileSize)!0}</td>
								<td><a href="javascript:void(0);"><i class="icon-download-alt resource-download" sname="${library.fileName}"></i></a></td>
								<td><a href="javascript:void(0);"><i class="icon-remove resource-remove" sname="${library.fileName}"></i></a></td>
							</tr>
							</#list>
						<#else>
							<tr>
								<td colspan="4">
									No data to display.
								</td>
							</tr>
						</#if>
					</tbody>
				</table>
			</div>
		</div>

		<div class="modal fade" id="createScriptModal">
			<div class="modal-header">
				<a class="close" data-dismiss="modal" id="createCloseBtn">&times;</a>
				<h3>Create a script</h3>
			</div>
			<div class="modal-body">
				<form class="form-horizontal" style="margin-bottom:0" method="post" target="_self" id="createForm" action="${Request.getContextPath()}/script/detail">
					<fieldset>
						<div class="control-group">
							<label for="scriptNameInput" class="control-label">Script Name</label>
							<div class="controls">
							  <input type="text" id="scriptNameInput" name="fileName">
							  <span class="help-inline"></span>
							</div>
						</div>
						<div class="control-group">
							<label for="languageSelect" class="control-label">Language</label>
							<div class="controls">
								<select id="languageSelect" name="language">
									<option value="py">PythonScript</option>
								</select>
							  <span class="help-inline"></span>
							</div>
						</div>
						<div class="control-group">
							<label for="urlInput" class="control-label">URL to be tested</label>
							<div class="controls">
							  <input type="text" id="urlInput" name="testURL">
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
		<div class="modal fade" id="uploadScriptModal">
			<div class="modal-header">
				<a class="close" data-dismiss="modal" id="upCloseBtn">&times;</a>
				<h3>Upload a JavaScript or Selenium File</h3>
			</div>
			<div class="modal-body">
				<form class="form-horizontal" style="margin-bottom:0" method="post" target="_self" action="${Request.getContextPath()}/script/upload" id="uploadForm">
					<fieldset>
						<div class="control-group">
							<label for="upScriptNameInput" class="control-label control-label-small">Name</label>
							<div class="controls controls-small">
							  <input type="text" id="upScriptNameInput" name="fileName">
							  <span class="help-inline"></span>
							</div>
						</div>
						<div class="control-group">
							<label for="discriptionInput" class="control-label control-label-small">Description</label>
							<div class="controls controls-small">
							  <input type="text" id="discriptionInput" name="description">
							  <span class="help-inline"></span>
							</div>
						</div>
						<div class="control-group">
							<label for="tagsInput" class="control-label control-label-small">Tags</label>
							<div class="controls controls-small">
								<input type="text" id="tagsInput" name="scriptTags">
								<span class="help-inline"></span>
							</div>
						</div>
						<div class="control-group">
							<label for="fileInput" class="control-label control-label-small">File</label>
							<div class="controls controls-small">
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
	<form id="downloadForm" method="post" target="downloadFrame">
		<input type="hidden" id="download_id" name="id">
		<input type="hidden" id="download_name" name="fileName">
	</form>
	<iframe name="downloadFrame" style="display: none;"></iframe>
	<script src="${Request.getContextPath()}/js/jquery-1.7.2.min.js"></script>
	<script src="${Request.getContextPath()}/js/bootstrap.min.js"></script>
	<script src="${Request.getContextPath()}/js/utils.js"></script>
	<script src="${Request.getContextPath()}/plugins/datatables/js/jquery.dataTables.min.js"></script>
	<script src="${Request.getContextPath()}/plugins/google_code_prettify/prettify.js"></script>
	<script>
		$(document).ready(function() {
			$("#n_script").addClass("active");
			
			$(".noClick").off('click');
			
			$("#createBtn2").on('click', function() {
				var $elem = $("#scriptNameInput");
				if (checkSimpleNameByObj($elem)) {
					cleanErrMsg($elem);
				} else {
					showErrMsg($elem, "Script name not correct.");
					return;
				}
				
				$elem = $("#urlInput");
				if (checkEmpty($elem)) {
					showErrMsg($elem, "Descripition can't be empyt.")
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
					showErrMsg($elem, "Script name not correct.");
					return;
				}
				
				$elem = $("#discriptionInput");
				if (checkEmpty($elem)) {
					showErrMsg($elem, "Descripition can't be empyt.")
					return;
				} else {
					cleanErrMsg($elem);
				}
				
				$elem = $("#tagsInput");
				if (checkEmpty($elem)) {
					showErrMsg($elem, "Tags can't be empyt.");
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
						
			$("#downloadBtn").on('click', function() {
			
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
			
			$("th input").on('click', function() {
				if($(this)[0].checked) {
					$("td input").each(function(){
						$(this).attr("checked", "checked");
					});
				} else {
					$("td input").each(function() {
						$(this).removeAttr("checked");
					});
				}
			});
			
			$("i.script-remove").on('click', function() {
				if (confirm("Do you want to delete this script file?")) {
					document.location.href = "${Request.getContextPath()}/script/deleteScript?id=" + $(this).attr("sid");
				}
			});
			
			$("i.script-download").on('click', function() {
				var $elem = $(this);
				$("#download_id").val($elem.attr("sid"));
				$("#download_name").val($elem.attr("sname"));
				document.forms.downloadForm.action = "${Request.getContextPath()}/script/downloadScript";
				document.forms.downloadForm.submit();
			});

			$("i.resource-remove").on('click', function() {
				if (confirm("Do you want to delete this resource file?")) {
					document.location.href = "${Request.getContextPath()}/script/deleteResource?fileName=" + encodeURI($(this).attr("sname"));
				}
			});
			
			$("i.resource-download").on('click', function() {
				var $elem = $(this);
				$("#download_name").val($elem.attr("sname"));
				document.forms.downloadForm.action = "${Request.getContextPath()}/script/downloadResource";
				document.forms.downloadForm.submit();
			});
						
			<#if scriptList?has_content>
			$("#scriptTable").dataTable({
				"bFilter": false,
				"bLengthChange": false,
				"bInfo": false,
				"iDisplayLength": 10,
				"aaSorting": [[1, "asc"]],
				"bProcessing": true,
				"aoColumns": [{ "asSorting": []}, null, null, null, null, {"asSorting": []}, { "asSorting": []}],
				//"bJQueryUI": true,
				//"oLanguage": {"sLengthMenu": "每页显示 _MENU_ 条记录","sZeroRecords": "抱歉， 没有找到","sInfo": "从 _START_ 到 _END_ /共 _TOTAL_ 条数据","sInfoEmpty": "没有数据","sInfoFiltered": "(从 _MAX_ 条数据中检索)","oPaginate": {"sFirst": "首页","sPrevious": "前一页","sNext": "后一页","sLast": "尾页"},"sZeroRecords": "没有检索到数据"},
				"sPaginationType": "full_numbers"
			});
			</#if>
			
			<#if libraries?has_content>
			$("#resourceTable").dataTable({
				"bFilter": false,
				"bLengthChange": false,
				"bInfo": false,
				"bProcessing": true,
				"aaSorting": [[0, "asc"]],
				"aoColumns": [null, null, {"asSorting": []}, { "asSorting": []}],
				"sScrollY": "200px",
        		"bPaginate": false,
       			"bScrollCollapse": true
			});
			</#if>
		});
		
		function searchScriptList() {
			var isOwner = 0;
			if ($("#onlyMineCkb")[0].checked) {
				isOwner = 1;
			}
			
			document.location.href = "${Request.getContextPath()}/script/list?keywords=" + $("#searchText").val() + "&isOwner=" + isOwner;
		}
	</script>
	</body>
</html>