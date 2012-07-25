<!DOCTYPE html>
<html>
	<head>
		<title>nGrinder Test Report</title>
		<#include "../common/common.ftl">
		
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
								<strong>${(test.vuserPerAgent)!}</strong>
							</div>
						</div>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Agents</label>
							<div class="controls">
								<span>${(test.agentCount)!}</span><a class="btn btn-mini btn-info" id="agentInfoBtn" href="#agentListModal" data-toggle="modal">Info</a>
							</div>
						</div>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Processes</label>
							<div class="controls">
								${(test.processes)!0}
							</div>
						</div>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Threads</label>
							<div class="controls">
								${(test.threads)!0}
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
								${(test.duration)!0}
							</div>
						</div>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Ignore Count</label>
							<div class="controls">
								<span>${(test.ignoreSampleCount)!0}</span><code>sec</code>
							</div>
						</div>
						<hr>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Sample Interval</label>
							<div class="controls">
								<span>${(test.sampleInterval)!1000}</span><code>ms</code>
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
								<strong>Total ${(test.tps)!}</strong>
							</div>
						</div>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Mean Time</label>
							<div class="controls">
								<span>${(test.meanTestTime)!}</span><code>ms</code>
							</div>
						</div>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Peak TPS</label>
							<div class="controls">
								<strong>${(test.peakTps)!}</strong>
							</div>
						</div>
						<div class="control-group">
							<label for="testNameInput" class="control-label">Finished Tests</label>
							<div class="controls">
								${(test.tests)!}
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
	<script>
		$(document).ready(function() {
			
		});
	</script>
	</body>
</html>