<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title>nGrinder Script List</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta name="description" content="nGrinder Test Result Detail">
		<meta name="author" content="AlexQin">

		<link rel="shortcut icon" href="favicon.ico"/>
		<link href="${req.getContextPath()}/css/bootstrap.min.css" rel="stylesheet">
		<link href="${req.getContextPath()}/css/bootstrap-responsive.min.css" rel="stylesheet">
		
		<style>
			body {
				padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
			}
			.form-horizontal-1 .control-label {width:80px;color: #666666; font-family: Tahoma,applegothic,sans-serif; font-size: 12px; font-weight:bold;}
			.form-horizontal-1 .controls {margin-left:100px;}
			.form-horizontal-1 .controls a {margin-left:20px;}
			.form-horizontal-1 .controls input {width:720px;}
			.form-horizontal-2 .control-label {width:110px;text-align:left;color: #666666; font-family: Tahoma,applegothic,sans-serif; font-size: 12px; font-weight:bold;}
			.form-horizontal-2 .controls {margin-left:130px;}
			.form-horizontal-3 { margin-bottom:0}
			.form-horizontal-3 .control-label {width:100px;text-align:left;color: #666666; font-family: Tahoma,applegothic,sans-serif; font-size: 12px; font-weight:bold;}
			.form-horizontal-3 .controls {margin-left:115px;}
			div.div-host { 
				border: 1px solid #D6D6D6;
				height: 80px;
				margin-bottom: 8px;
				overflow-y: scroll;
				border-radius: 3px 3px 3px 3px;
			}
			div.div-host .host{color: #666666; display: inline-block; margin: 7px;}
			.select-item { width:50px; }
			.control-label input { vertical-align:top; margin-left:2px }
			.controls code { vertical-align:middle; }
		</style>
		
		<input type="hidden" id="contextPath" value="${req.getContextPath()}">
		<#setting number_format="computer">
	</head>

	<body>
    	<#include "../common/navigator.ftl">
		<div class="container">
			<div class="row">
				<div class="span10 offset1">
					<div class="well">
						<div class="row">
							<div class="span10">
								<div class="form-horizontal form-horizontal-1" style="margin-bottom:0">
									<fieldset>
										<div class="control-group">
											<label for="testNameInput" class="control-label">Test Name</label>
											<div class="controls">
											  <input type="text" id="testNameInput">
											  <a class="btn">Save</a>
											  <!--<span class="help-inline"></span>-->
											</div>
										</div>
										<div class="control-group" style="margin-bottom:0">
											<label for="descriptionInput" class="control-label">Description</label>
											<div class="controls">
											  <input type="text" id="descriptionInput">
											  <a class="btn">Start</a>
											</div>
										</div>
									</fieldset>
								</div>
							</div>
						</div>
					</div>
					<div class="tabbable">
						<ul class="nav nav-tabs" id="homeTab">
						  <li><a href="#testContent" data-toggle="tab">Test</a></li>
						  <li><a href="#reportContent" data-toggle="tab">Report</a></li>
						</ul>
						<div class="tab-content">
							<div class="tab-pane" id="testContent">
								<div class="row">
									<div class="span5">
										<div class="page-header">
											<h4>Load Test Detail</h4>
										</div>
										<div class="form-horizontal form-horizontal-2" style="margin-bottom:0">
											<fieldset>
												<div class="control-group">
													<label for="agentInput" class="control-label">Agent</label>
													<div class="controls">
														<div class="input-append">
															<input type="text" class="input input-small" id="agentInput" readonly>
															<button type="button" class="btn" id="agentSetBtn">Set</button>
														</div>
														<span class="label label-info pull-right">Vuser:1</span>
													</div>
												</div>
												<div class="control-group">
													<label for="scriptSelect" class="control-label">Script</label>
													<div class="controls">
														<select id="scriptSelect">
															<option value="">1.py</option>
														</select>
													</div>
												</div>
												<div class="control-group">
													<label class="control-label">Target Host</label>
													<div class="controls">
														<div class="div-host">
															<p class="host">
																1.1.1.1
																<a href=""><i class="icon-remove-circle"></i></a>
															</p>
															<p class="host">
																111.111.111.111
																<a href=""><i class="icon-remove-circle"></i></a>
															</p>
															<p class="host">
																1.1.1.1
																<a href=""><i class="icon-remove-circle"></i></a>
															</p>
															<p class="host">
																12.13.14.15
																<a href=""><i class="icon-remove-circle"></i></a>
															</p>
														</div>
														<button class="btn pull-right btn-mini">Add</button>
													</div>
												</div>
												<hr>
												<div class="control-group">
													<label class="control-label">
														<input type="radio">
														Duration
													</label>
													<div class="controls" >
														<select class="select-item" id="dSelect"></select> :
														<select class="select-item" id="hSelect"></select> :
														<select class="select-item" id="mSelect"></select> :
														<select class="select-item" id="sSelect"></select>&nbsp;&nbsp;
														<code>DD:HH:MM:SS</code>
													</div>
												</div>
												<div class="control-group">
													<label for="runCntInput" class="control-label">
														<input type="radio">
														Run Count
													</label>
													<div class="controls">
														<input type="text" class="input input-small" id="runCntInput">
													</div>
												</div>
												<div class="control-group">
													<label for="ignoreInput" class="control-label">
														Ignore Count
													</label>
													<div class="controls">
														<input type="text" class="input input-small" id="ignoreInput">
													</div>
												</div>
												<div class="control-group">
													<label for="sapInTvlInput" class="control-label">
														Sample Interval
													</label>
													<div class="controls">
														<input type="text" class="input input-small" id="sapInTvlInput">
														<code>MS</code>
													</div>
												</div>
											</fieldset>
										</div>
									</div>
									<div class="span5">
										<div class="page-header">
											<label class="checkbox" style="margin-bottom:0">
												<input type="checkbox" id="rampupCheckbox"><h4>Enable Ramp-Up</h4>
											</label>
										</div>
										<table>
											<tr>
												<td style="width:50%">
												<div class="form-horizontal form-horizontal-3">
														<fieldset>
															<div class="control-group">
																<label for="initProcessInput" class="control-label">
																	Inital Processes
																</label>
																<div class="controls">
																	<input type="text" class="input input-mini" id="initProcessInput">
																	<input type="hidden" id="initProcessesHidden" value="${(properties.grinder_initialProcesses)!0}">
																</div>
															</div>
															<div class="control-group">
																<label for="rampUpInput" class="control-label">
																	Ramp-Up
																</label>
																<div class="controls">
																	<input type="text" class="input input-mini" id="rampUpInput">
																</div>
															</div>
														</fieldset>
													</div>
												</td>
												<td>
													<div class="form-horizontal form-horizontal-2">
														<fieldset>
															<div class="control-group">
																<label for="initSleepTimeInput" class="control-label">
																	Inital Sleep Time
																</label>
																<div class="controls">
																	<input type="text" class="input input-mini" id="initSleepTimeInput">
																	<code>MS</code>
																</div>
															</div>
															<div class="control-group">
																<label for="everyInput" class="control-label">
																	Processes Every
																</label>
																<div class="controls">
																	<input type="text" class="input input-mini" id="everyInput">
																	<code>MS</code>
																</div>
															</div>
														</fieldset>
													</div>
												</td>
											</tr>
										</table>
										<div id="rampChart1" style="width: 460px; height: 315px;">  
										</div>
										<div id="rampChart2" style="width: 460px; height: 315px; display:none"> 
										</div>
									</div>
								</div>
							</div>
							<div class="tab-pane" id="reportContent">
								<div class="row">
									<div class="span3">
										<div class="form-horizontal form-horizontal-3" style="margin-left:20px">
											<fieldset>
												<div class="control-group">
													<label for="agentInput" class="control-label control-label-1">TPS</label>
													<div class="controls">
														<span class="label label-info">Total 26.67</span>
													</div>
												</div>
												<div class="control-group">
													<label for="agentInput" class="control-label">Mean Time</label>
													<div class="controls">
														316.5 <code>MS</code>
													</div>
												</div>
												<div class="control-group">
													<label for="agentInput" class="control-label">Peak TPS</label>
													<div class="controls">
														41
													</div>
												</div>
												<div class="control-group">
													<label for="agentInput" class="control-label">Finished Tests</label>
													<div class="controls">
														3560
													</div>
												</div>
												<div class="control-group">
													<label for="agentInput" class="control-label">Errors</label>
													<div class="controls">
														12
													</div>
												</div>
											</fieldset>
										</div>
									</div>
									<div class="span7">
										<img src="" height="220" width="750" border="0">
									</div>
								</div>
								<div class="row" style="margin-top:10px;">
									<div class="span10">
										<a class="btn pull-right" target="_blank" href="#">Report in Detail</a>
									</div>
								</div>
							</div>
						</div>
					</div>
					<!--content-->
					<#include "../common/copyright.ftl">
				</div>
			</div>
		</div>
		<script src="${req.getContextPath()}/js/jquery-1.7.2.min.js"></script>
		<script src="${req.getContextPath()}/js/bootstrap.min.js"></script>
		<script src="${req.getContextPath()}/js/jquery.gchart.pack.js"></script>
		<script src="${req.getContextPath()}/js/utils.js"></script>
		<script src="${req.getContextPath()}/js/rampup.js"></script>
		<script>
			$(document).ready(function() {
				$("#homeTab a:first").tab('show');
				
				$('a[data-toggle="tab"]').on('show', function(e) {
					//alert("current tab: " + e.target + "\nlast tab: " + e.relatedTarget);
				});	
							
				$("#dSelect").append(getOption(100));
				$("#hSelect").append(getOption(24));
				$("#mSelect").append(getOption(60));
				$("#sSelect").append(getOption(60));
			});
			
			function getOption(cnt) {
				var contents = [];
				
				for(i = 0; i < cnt; i++) {
					contents.push("<option value='" + i + "'>" + i + "</option>");
				}
				
				return contents.join("\n");
			}
		</script>
	</body>
</html>