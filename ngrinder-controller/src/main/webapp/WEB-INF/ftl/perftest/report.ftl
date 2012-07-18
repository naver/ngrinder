<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title>nGrinder Test Report</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta name="description" content="nGrinder Test Report">
		<meta name="author" content="AlexQin">

		<link rel="shortcut icon" href="${req.getContextPath()}/favicon.ico"/>
		<link href="${req.getContextPath()}/css/bootstrap.min.css" rel="stylesheet">
		<link href="${req.getContextPath()}/css/ngrinder.css" rel="stylesheet">
		
		<style>
			body {
				padding-top: 0;
			}	
			.left { border-right: 1px solid #878988 }
			div.chart { border: 1px solid #878988; height:195px; min-width:615px; margin-bottom:12px}
		</style>
		
		<input type="hidden" id="contextPath" value="${req.getContextPath()}">
		<#setting number_format="computer">
	</head>

	<body>
	<ul class="breadcrumb">
		<li>
			<h3>nGrinder Report</h3>
		</li>
	</ul>
	<div class="container">
		<div class="row">
			<div class="span12" style="margin-bottom:10px;">
				<button class="btn btn-large pull-right"><i class="icon-download-alt"></i><strong>Download CSV</strong></button>
			</div>
		</div>
		<div class="row">
			<div class="span4 left">
				<select id="scriptSelect">
					<option value="0">Performance</option>
				</select>
				<div class="form-horizontal form-horizontal-3" style="margin-top:20px">
					<fieldset>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Vuser</label>
							<div class="controls">
								<strong>10</strong>
							</div>
						</div>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Agents</label>
							<div class="controls">
								<span>1</span><a class="btn btn-mini btn-info" id="agentInfoBtn" href="#agentListModal" data-toggle="modal">Info</a>
							</div>
						</div>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Processes</label>
							<div class="controls">
								1
							</div>
						</div>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Threads</label>
							<div class="controls">
								10
							</div>
						</div>
						<hr>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Target Hosts</label>
							<div class="controls">
								10.34.64.36
							</div>
						</div>
						<hr>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Duration</label>
							<div class="controls">
								00:00:02:19
							</div>
						</div>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Ignore Count</label>
							<div class="controls">
								<span>0</span><code>sec</code>
							</div>
						</div>
						<hr>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Sample Interval</label>
							<div class="controls">
								<span>1000</span><code>ms</code>
							</div>
						</div>
						<hr>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Test Comment</label>
							<div class="controls">
								Copied
							</div>
						</div>
						<hr>
						<div class="control-group">
							<label for="testNameInput" class="control-label">TPS</label>
							<div class="controls">
								<strong>Total 26.67</strong>
							</div>
						</div>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Mean Time</label>
							<div class="controls">
								<span>316.5</span><code>ms</code>
							</div>
						</div>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Peak TPS</label>
							<div class="controls">
								<strong>41</strong>
							</div>
						</div>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Finished Tests</label>
							<div class="controls">
								3560
							</div>
						</div>
					</fieldset>
				</div>
			</div>
			<div class="span7">
				<div class="chart"></div>
				<div class="chart"></div>
				<div class="chart"></div>
				<div class="chart"></div>
				<!--
				<img src="image01.jpg" height="210" width="800" border="0">
				<img src="image01.jpg" height="210" width="800" border="0">
				<img src="image01.jpg" height="210" width="800" border="0">
				<img src="image01.jpg" height="210" width="800" border="0">
				-->
			</div>
		</div>
		<#include "../common/copyright.ftl">
	</div>
	<div class="modal fade" id="agentListModal">
		<div class="modal-header">
			<a class="close" data-dismiss="modal" id="upCloseBtn">&times;</a>
			<h3>Agent List</h3>
		</div>
		<div class="modal-body">
			Agent List
		</div>
	</div>
	
	<script src="${req.getContextPath()}/js/jquery-1.7.2.min.js"></script>
	<script src="${req.getContextPath()}/js/bootstrap.min.js"></script>
	<script src="${req.getContextPath()}/js/utils.js"></script>
	<script>
		$(document).ready(function() {
			
		});
	</script>
	</body>
</html>