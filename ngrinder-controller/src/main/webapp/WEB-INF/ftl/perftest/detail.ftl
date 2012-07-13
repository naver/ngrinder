<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<title>nGrinder Performance Test Detail</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="nGrinder Performance Test Detail">
	<meta name="author" content="AlexQin">
	
	<link rel="shortcut icon" href="favicon.ico" />
	<link href="${req.getContextPath()}/css/bootstrap.min.css" rel="stylesheet">
	<style>
		body {
			padding-top: 60px;
			/* 60px to make the container go all the way to the bottom of the topbar */
		}
		
		.form-horizontal-1 .control-label {
			width: 80px;
			color: #666666;
			font-family: Tahoma, applegothic, sans-serif;
			font-size: 12px;
			font-weight: bold;
		}
		
		.form-horizontal-1 .controls {
			margin-left: 100px;
		}
		
		.form-horizontal-1 .controls a {
			margin-left: 20px;
		}
		
		.form-horizontal-1 .controls input {
			width: 720px;
		}
		
		.form-horizontal-2 .control-label {
			width: 110px;
			text-align: left;
			color: #666666;
			font-family: Tahoma, applegothic, sans-serif;
			font-size: 12px;
			font-weight: bold;
		}
		
		.form-horizontal-2 .controls {
			margin-left: 130px;
		}
		
		.form-horizontal-3 {
			margin-bottom: 0
		}
		
		.form-horizontal-3 .control-label {
			width: 100px;
			text-align: left;
			color: #666666;
			font-family: Tahoma, applegothic, sans-serif;
			font-size: 12px;
			font-weight: bold;
		}
		
		.form-horizontal-3 .controls {
			margin-left: 115px;
		}
		
		div.div-host {
			border: 1px solid #D6D6D6;
			height: 80px;
			margin-bottom: 8px;
			overflow-y: scroll;
			border-radius: 3px 3px 3px 3px;
		}
		
		div.div-host .host {
			color: #666666;
			display: inline-block;
			margin: 7px;
		}
		
		.select-item {
			width: 50px;
		}
		
		.control-label input {
			vertical-align: top;
			margin-left: 2px
		}
		
		.controls code {
			vertical-align: middle;
		}
	</style>
	
	<input type="hidden" id="contextPath" value="${req.getContextPath()}">
	<#setting number_format="computer">
</head>

<body>
	<#include "../common/navigator.ftl">
	<div class="container">
		<div class="well">
			<div class="row">
				<div class="span10">
					<div class="form-horizontal form-horizontal-1"
						style="margin-bottom: 0">
						<fieldset>
							<div class="control-group">
								<label for="testNameInput" class="control-label">Test Name</label>
								<div class="controls">
									<input type="text" id="testName" value="${(test.testName)!}">
									<a class="btn">Save</a>
									<!--<span class="help-inline"></span>-->
								</div>
							</div>
							<div class="control-group" style="margin-bottom: 0">
								<label for="descriptionInput" class="control-label">Description</label>
								<div class="controls">
									<input type="text" id="description"
										value="${(test.description)!}"> <a class="btn">Save&Start</a>
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
				<#if test?? && test.status == "FINISHED">
					<li><a href="#reportContent" data-toggle="tab">Report</a></li>
				</#if>
			</ul>
			<div class="tab-content">
				<div class="tab-pane" id="testContent">
					<div class="row">
						<div class="span5">
							<div class="page-header">
								<h4>Load Test Detail</h4>
							</div>
							<div class="form-horizontal form-horizontal-2"
								style="margin-bottom: 0">
								<fieldset>
									<div class="control-group">
										<label for="agentInput" class="control-label">Agent</label>
										<div class="controls">
											<div class="input-append">
												<input type="text" class="input input-small"
													id="agentCount" value="${(test.agentCount)!}" readonly>
												<button type="button" class="btn" id="agentSetBtn">Set</button>
											</div>
											<span class="label label-info pull-right">Vuser:${(test.vuser)!}</span>
										</div>
									</div>
									<div class="control-group">
										<label for="scriptSelect" class="control-label">Script</label>
										<div class="controls">
											<select id="scriptName" name="scriptName">
												<option>---</option>
												<#if scriptList?size &gt; 0>
													<#list scriptList as scriptItem>
														<#if scriptItem.fileName == test.scriptName>
															<#assign isSelected = "selected"/>
														<#else>
															<#assign isSelected = ""/>
														</#if>
														<option value="${scriptItem.fileName}"${isSelected}>${scriptItem.fileName}</option>
													</#list>
												</#if>
											</select>
										</div>
									</div>
									<div class="control-group">
										<label class="control-label">Target Host</label>
										<div class="controls">
											<div class="div-host">
												<p class="host">
													1.1.1.1 <a href=""><i class="icon-remove-circle"></i></a>
												</p>
												<p class="host">
													111.111.111.111-aaa.com <a href=""><i
														class="icon-remove-circle"></i></a>
												</p>
											</div>
											<button class="btn pull-right btn-mini">Add</button>
										</div>
									</div>
									<hr>
									<div class="control-group">
										<label class="control-label"> <input type="radio"
											id="durationChkbox"> Duration
										</label> <input type="hidden" id="duration" name="duration"
											value="${(test.duration)!}">
										<div class="controls">
											<select class="select-item" id="dSelect"></select> : <select
												class="select-item" id="hSelect"></select> : <select
												class="select-item" id="mSelect"></select> : <select
												class="select-item" id="sSelect"></select>&nbsp;&nbsp;
											<code>DD:HH:MM:SS</code>
										</div>
									</div>
									<div class="control-group">
										<label for="runCntInput" class="control-label"> <input
											type="radio" id="runcountChkbox"> Run Count
										</label>
										<div class="controls">
											<input type="text" class="input input-small" id="runCount"
												name="runCount">
										</div>
									</div>
									<div class="control-group">
										<label for="ignoreInput" class="control-label">
											Ignore Count </label>
										<div class="controls">
											<input type="text" class="input input-small"
												id="ignoreSampleCount" name="ignoreSampleCount"
												value="${test.ignoreSampleCount}">
										</div>
									</div>
									<div class="control-group">
										<label for="sapInTvlInput" class="control-label">
											Sample Interval </label>
										<div class="controls">
											<input type="text" class="input input-small"
												id="sampleInterval" name="sampleInterval"
												value="${test.sampleInterval}">
											<code>MS</code>
										</div>
									</div>
								</fieldset>
							</div>
						</div>
						<div class="span5">
							<div class="page-header">
								<label class="checkbox" style="margin-bottom: 0"> <input
									type="checkbox" id="rampupCheckbox">
								<h4>Enable Ramp-Up</h4>
								</label>
							</div>
							<table>
								<tr>
									<td style="width: 50%">
										<div class="form-horizontal form-horizontal-3">
											<fieldset>
												<div class="control-group">
													<label for="initProcessInput" class="control-label">
														Inital Processes </label>
													<div class="controls">
														<input type="text" class="input input-mini"
															id="initProcesses" name="initProcesses"
															value="${test.initProcesses}">
													</div>
												</div>
												<div class="control-group">
													<label for="rampUpInput" class="control-label">
														Ramp-Up </label>
													<div class="controls">
														<input type="text" class="input input-mini"
															id="processIncrement" name="processIncrement"
															value="${test.processIncrement}">
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
														Inital Sleep Time </label>
													<div class="controls">
														<input type="text" class="input input-mini"
															id="initSleepTime" name="initSleepTime"
															value="${test.initSleepTime}">
														<code>MS</code>
													</div>
												</div>
												<div class="control-group">
													<label for="everyInput" class="control-label">
														Processes Every </label>
													<div class="controls">
														<input type="text" class="input input-mini"
															id="processIncrementInterval"
															name="processIncrementInterval"
															value="${test.processIncrementInterval}">
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
							<div id="rampChart2"
								style="width: 460px; height: 315px; display: none"></div>
						</div>
					</div>
				</div>
				<div class="tab-pane" id="reportContent">not finished yet</div>
			</div>
		</div>
		<!--content-->
		<#include "../common/copyright.ftl">
	</div>
	<script src="${req.getContextPath()}/js/jquery-1.7.2.min.js"></script>
	<script src="${req.getContextPath()}/js/bootstrap.min.js"></script>
	<script src="${req.getContextPath()}/js/jquery.gchart.pack.js"></script>
	<script src="${req.getContextPath()}/js/utils.js"></script>
	<script src="${req.getContextPath()}/js/rampup.js"></script>
	<script>
			$(document).ready(function() {
				
				if (${scriptList?size} == 0) {
					alert ("User has not script yet! Please create a script first.");
					document.location.href = "${req.getContextPath()}/script/list";
					return;
				}
				$("#homeTab a:first").tab('show');
				
				$('a[data-toggle="tab"]').on('show', function(e) {
					//alert("current tab: " + e.target + "\nlast tab: " + e.relatedTarget);
				});	
							
				$("#dSelect").append(getOption(100));
				$("#hSelect").append(getOption(24));
				$("#mSelect").append(getOption(60));
				$("#sSelect").append(getOption(60));
				
				initThresholdChkBox();
				initDuration();
			});
			
			function initThresholdChkBox() {
				if ("${test.threshold}" == "R") { //runcount
					$("#runcountChkbox").attr("checked", "checked");
				} else { //duration
					$("#durationChkbox").attr("checked", "checked");					
				}
			}
			
			function initDuration() {
				
			}
			
			function initRampup() {
				
			}
			
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