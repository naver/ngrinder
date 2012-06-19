<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title>nGrinder Script List</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta name="description" content="nGrinder Test Result Detail">
		<meta name="author" content="AlexQin">

		<link rel="shortcut icon" href="favicon.ico"/>
		<link href="/css/bootstrap.css" rel="stylesheet">
		<style>
			body {
				padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
			}
			table .td_left {
				text-align: left;
			}
		</style>
		<link href="/css/bootstrap-responsive.css" rel="stylesheet">
		<link href="/plugins/sort/style.css" rel="stylesheet">
		<link href="/plugins/google_code_prettify/prettify.css" rel="stylesheet">
		
		<!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
		<!--[if lt IE 9]>
		  <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
		<![endif]-->
	</head>

	<body>
    <div class="navbar navbar-fixed-top">
		<div class="navbar-inner">
			<div class="container">
				<a class="brand" href="#" style="padding:0 20px"><img src="/img/logo_ngrinder_a_header.png" alt="nGrinder"></img></a>
				<div class="nav-collapse">
					<ul class="nav">
					  <li class="active"><a href="#">Monitoring</a></li>
					  <li><a href="#about">Load Test</a></li>
					  <li><a href="#contact">Script</a></li>
					  <li><a href="#contact">Alert</a></li>
					</ul>
					<ul class="nav pull-right">
						<li><a href="#">Login</a></li>
						<li class="divider-vertical"></li>
						<li class="dropdown"><a href="#">Help</a></li>
					</ul>
				</div><!--/.nav-collapse -->
			</div>
		</div>
    </div>
	<div class="container">
		<div class="row">
			<div class="span10 offset1">
				<div class="tabbable">
					<ul class="nav nav-tabs" id="homeTab">
					  <li><a href="#monitoringContent" data-toggle="tab">Monitoring</a></li>
					  <li><a href="#loadTestContent" data-toggle="tab">Load Test</a></li>
					  <li><a href="#scriptListContent" data-toggle="tab">Script</a></li>
					  <li><a href="#alertContent" data-toggle="tab">Alert</a></li>
					</ul>
					<div class="tab-content">
						<div class="tab-pane" id="monitoringContent">
							Monitoring
						</div>
						<div class="tab-pane" id="loadTestContent">
							Load Test
						</div>
						<div class="tab-pane" id="alertContent">
							Alert
						</div>
						<div class="tab-pane" id="scriptListContent">
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
							<div class="well form-inline" style="padding:5px;margin:5px 0">
								<!--<legend>introduction</legend>-->
								<input type="text" class="input-medium search-query" placeholder="Keywords">
								<button type="submit" class="btn">Search</button>
								<label class="checkbox pull-right" style="position:relative;top:5px">
									<input type="checkbox" id="onlyMineCkb"> See only my script
								</label>
							</div>
							<table class="table table-striped table-condensed tablesorter" id="scriptTable" style="border-collapse:separate;">
								<thead>
									<tr>
										<th><input type="checkbox" class="checkbox" value=""></th>
										<th>ID</th>
										<th type="1">Script File Name</th>
										<th type="2">Last Test Date</th>
										<th type="3">Last Modified Date</th>
										<th type="4">Size</th>
										<th>Download</th>
										<th>Del</th>
									</tr>
								</thead>
								<tbody>
									<tr>
										<td><input type="checkbox" value=""></td>
										<td>1111</td>
										<td class="td_left"><a href="#">fff.py</a></td>
										<td>2012-05-01 10:10:10</td>
										<td>2012-06-01 10:10:10</td>
										<td>9KB</td>
										<td><a href="#"><i class="icon-download-alt"></i></a></td>
										<td><a href="#"><i class="icon-remove"></i></a></td>
									</tr>
									<tr>
										<td><input type="checkbox" value=""></td>
										<td>1111</td>
										<td class="td_left"><a href="#">ggg.py</a></td>
										<td>2012-05-01 10:10:10</td>
										<td>2012-06-01 10:10:10</td>
										<td>10KB</td>
										<td><a href="#"><i class="icon-download-alt"></i></a></td>
										<td><a href="#"><i class="icon-remove"></i></a></td>
									</tr>
									<tr>
										<td><input type="checkbox" value=""></td>
										<td>1111</td>
										<td class="td_left"><a href="#">aaa.py</a></td>
										<td>2012-05-01 10:10:10</td>
										<td>2012-06-01 10:10:10</td>
										<td>11KB</td>
										<td><a href="#"><i class="icon-download-alt"></i></a></td>
										<td><a href="#"><i class="icon-remove"></i></a></td>
									</tr>
								</tbody>
							</table>
							<div class="pagination pagination-centered">
							  <ul>
								<li class="disabled"><a href="#">Prev</a></li>
								<li class="active"><a href="#">1</a></li>
								<li><a href="#">2</a></li>
								<li><a href="#">3</a></li>
								<li><a href="#">4</a></li>
								<li><a href="#">Next</a></li>
							  </ul>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<div class="modal fade" id="createScriptModal">
			<div class="modal-header">
				<a class="close" data-dismiss="modal" id="createCloseBtn">&times;</a>
				<h3>Create a script</h3>
			</div>
			<div class="modal-body">
				<form class="form-horizontal" style="margin-bottom:0">
					<fieldset>
						<div class="control-group">
							<label for="scriptNameInput" class="control-label">Script Name</label>
							<div class="controls">
							  <input type="text" id="scriptNameInput">
							  <span class="help-inline">Introduction</span>
							</div>
						</div>
						<div class="control-group">
							<label for="languageSelect" class="control-label">Language</label>
							<div class="controls">
								<select id="languageSelect">
									<option value="0">PythonScript</option>
									<option value="1">JavaScript</option>
								</select>
							  <span class="help-inline"></span>
							</div>
						</div>
						<div class="control-group">
							<label for="urlInput" class="control-label">URL to be tested</label>
							<div class="controls">
							  <input type="text" id="urlInput">
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
				<form class="form-horizontal" style="margin-bottom:0">
					<fieldset>
						<div class="control-group warning">
							<label for="upScriptNameInput" class="control-label">Name</label>
							<div class="controls">
							  <input type="text" class="input-small" id="upScriptNameInput">
							  <span class="help-inline"></span>
							</div>
						</div>
						<div class="control-group">
							<label for="discriptionInput" class="control-label">Description</label>
							<div class="controls">
							  <input type="text" disabled id="discriptionInput" placeholder="Contents">
							  <span class="help-inline"></span>
							</div>
						</div>
						<div class="control-group">
							<label for="tagsInput" class="control-label">Tags</label>
							<div class="controls">
								<span class="uneditable-input">Contents</span>
								<input type="hidden" id="tagsInput" value="Contents">
							</div>
						</div>
						<div class="control-group">
							<label for="fileInput" class="control-label">File</label>
							<div class="controls">
							  <input type="file" class="input-file" id="fileInput">
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

	<script src="/js/jquery-1.7.2.min.js"></script>
	<script src="/js/bootstrap.min.js"></script>
	<script src="/plugins/sort/tablesorter.min.js"></script>
	<script src="/plugins/google_code_prettify/prettify.js"></script>
	<script>
		$(document).ready(function() {
			$("#homeTab a:first").tab('show');
			
			$('a[data-toggle="tab"]').on('shown', function(e) {
				alert("current tab: " + e.target + "\nlast tab: " + e.relatedTarget);
			});

			$("table#scriptTable").tablesorter({ 
				sortList: [[2,0]], 
				headers: {0: {sorter: false}, 1: {sorter: false}, 6: {sorter: false}, 7: {sorter: false}} 
			});
			$("#createBtn2").on('click', function() {
				$('#createScriptModal').modal('hide');
			});
			$("#uploadBtn2").on('click', function() {
				$('#uploadScriptModal').modal('hide');
			});				
			$("#downloadBtn").on('click', function() {
			
			});
			$("table.tablesorter th.header").on('click', function() {
				var $this = $(this);
				alert($this.attr("type") + " " + $this.hasClass('headerSortUp'));
			});
			
			$("#onlyMineCkb").on('click', function() {
				alert($(this)[0].checked);
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
		});
	</script>
	</body>
</html>