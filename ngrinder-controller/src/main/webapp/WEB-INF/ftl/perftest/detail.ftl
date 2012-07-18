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
<link href="${req.getContextPath()}/css/ngrinder.css" rel="stylesheet">
<style>
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
		<form id="testContentForm" action="${req.getContextPath()}/perftest/create" method="POST">
			<div class="well">
				<div class="row">
					<div class="span10">
						<!-- not to pass test id, because test can not be modified.
						<input type="hidden" id="testId" name="id" value="${(test.id)!}">
						 -->
						<input type="hidden" id="threshold" name="threshold" value="${(test.threshold)!"D"}">
						<input type="hidden" id="threads" name="threads" value="${(test.threads)!0}">
						<input type="hidden" id="processes" name="processes" value="${(test.processes)!0}">						

						<div class="form-horizontal form-horizontal-1" style="margin-bottom: 0">
							<fieldset>
								<div class="control-group">
									<label for="testName" class="control-label">Test Name</label>
									<div class="controls">
										<input type="text" id="testName" name="testName" value="${(test.testName)!}">
										<#if test??>
											<button type="submit" class="btn">Clone&Start</a>
										<#else>
											<button type="submit" class="btn">Save&Start</a>
										</#if>
									</div>
								</div>
								<div class="control-group" style="margin-bottom: 0">
									<label for="description" class="control-label">Description</label>
									<div class="controls">
										<input type="text" id="description" name="description" value="${(test.description)!}">
										<#if test??>
											<button type="submit" class="btn">Clone&Schedule</a>
										<#else>
											<button type="submit" class="btn">Save&Schedule</a>
										</#if>
										
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
							<li><a href="#reportContent" data-toggle="tab" id="reportLnk">Report</a></li>
						</#if>
					</ul>
					<div class="tab-content">
						<div class="tab-pane" id="testContent">
							<div class="row">
								<div class="span6">
									<div class="page-header">
										<h4>Load Test Detail</h4>
									</div>
									<div class="form-horizontal form-horizontal-2"
										style="margin-bottom: 0">
										<fieldset>
											<div class="control-group">
												<label for="agentCount" class="control-label">Agent</label>
												<div class="controls">
													<div class="input-append">
														<input type="text" class="input input-small"
															id="agentCount" name="agentCount" value="${(test.agentCount)!}">
													</div>
												</div>
											</div>
											<div class="control-group">
												<label for="vuserPerAgent" class="control-label">Vuser on every agent</label>
												<div class="controls">
													<div class="input-append">
														<input type="text" class="input input-small"
															id="vuserPerAgent" name="vuserPerAgent" value="${(test.vuserPerAgent)!}">
													</div>
													<#assign vuserTotal = (test.vuserPerAgent)!0 * (test.agentCount)!0 />
													<span class="badge badge-info pull-right" id="vuserTotal">Vuser: ${vuserTotal}</span>
												</div>
											</div>
											<div class="control-group">
												<label for="scriptName" class="control-label">Script</label>
												<div class="controls">
													<select id="scriptName" name="scriptName">
														<option>---</option>
														<#if scriptList?size &gt; 0>
															<#list scriptList as scriptItem>
																<#if test?? && scriptItem.fileName == test.scriptName>
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
												</label>
												<input type="hidden" id="duration" name="duration"
													value="${(test.duration)!0}">
												<div class="controls">
													<select class="select-item" id="dSelect"></select> : <select
														class="select-item" id="hSelect"></select> : <select
														class="select-item" id="mSelect"></select> : <select
														class="select-item" id="sSelect"></select>&nbsp;&nbsp;
													<code>DD:HH:MM:SS</code>
												</div>
											</div>
											<div class="control-group">
												<label for="runCount" class="control-label"> <input
													type="radio" id="runcountChkbox"> Run Count
												</label>
												<div class="controls">
													<input type="text" class="input input-small" id="runCount"
														name="runCount" value="${(test.runCount)!0}">
												</div>
											</div>
											<div class="control-group">
												<label for="ignoreSampleCount" class="control-label">
													Ignore Count </label>
												<div class="controls">
													<input type="text" class="input input-small"
														id="ignoreSampleCount" name="ignoreSampleCount"
														value="${(test.ignoreSampleCount)!0}">
												</div>
											</div>
											<div class="control-group">
												<label for="sampleInterval" class="control-label">
													Sample Interval </label>
												<div class="controls">
													<input type="text" class="input input-small"
														id="sampleInterval" name="sampleInterval"
														value="${(test.sampleInterval)!1000}">
													<code>MS</code>
												</div>
											</div>
										</fieldset>
									</div>
								</div>
								<div class="span6">
									<div class="page-header">
										<label class="checkbox" style="margin-bottom: 0">
											<input type="checkbox" id="rampupCheckbox" <#if test?? && test.processes &gt; test.initProcesses>checked</#if> />
											<h4>Enable Ramp-Up</h4>(ramp-up chart for every agent)
										</label>
									</div>
									<table>
										<tr>
											<td style="width: 50%">
												<div class="form-horizontal form-horizontal-2">
													<fieldset>
														<div class="control-group">
															<label for="initProcesses" class="control-label">
																Inital Processes </label>
															<div class="controls">
																<input type="text" class="input input-mini"
																	id="initProcesses" name="initProcesses"
																	value="${(test.initProcesses)!0}" />
															</div>
														</div>
														<div class="control-group">
															<label for="processIncrement" class="control-label">
																Ramp-Up </label>
															<div class="controls">
																<input type="text" class="input input-mini"
																	id="processIncrement" name="processIncrement"
																	value="${(test.processIncrement)!1}">
															</div>
														</div>
													</fieldset>
												</div>
											</td>
											<td>
												<div class="form-horizontal form-horizontal-2">
													<fieldset>
														<div class="control-group">
															<label for="initSleepTime" class="control-label">
																Inital Sleep Time </label>
															<div class="controls">
																<input type="text" class="input input-mini"
																	id="initSleepTime" name="initSleepTime"
																	value="${(test.initSleepTime)!0}">
																<code>MS</code>
															</div>
														</div>
														<div class="control-group">
															<label for="processIncrementInterval" class="control-label">
																Processes Every </label>
															<div class="controls">
																<input type="text" class="input input-mini"
																	id="processIncrementInterval"
																	name="processIncrementInterval"
																	value="${(test.processIncrementInterval)!1}">
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
						<div class="tab-pane" id="reportContent">
							<div class="row">
								<div class="span3">
									<div class="form-horizontal form-horizontal-3"
										style="margin-left: 20px">
										<fieldset>
											<div class="control-group">
												<label for="agentInput" class="control-label control-label-1">TPS</label>
												<div class="controls">
													<span class="label label-info">Total ${(test.tps)!}</span>
												</div>
											</div>
											<div class="control-group">
												<label for="agentInput" class="control-label">Mean Time</label>
												<div class="controls">
													${(test.meanTestTime)!}
													<code>MS</code>
												</div>
											</div>
											<div class="control-group">
												<label for="agentInput" class="control-label">Peak TPS</label>
												<div class="controls">${(test.peakTps)!}</div>
											</div>
											<div class="control-group">
												<label for="agentInput" class="control-label">Finished Tests</label>
												<div class="controls">${(test.tests)!}</div>
											</div>
											<div class="control-group">
												<label for="agentInput" class="control-label">Errors</label>
												<div class="controls">${(test.errors)!}</div>
											</div>
										</fieldset>
									</div>
								</div>
								<div class="span7">
									<img src="" height="220" width="750" border="0">
								</div>
							</div>
							<div class="row" style="margin-top: 10px;">
								<div class="span10">
									<a class="btn pull-right" target="_blank" href="#">Report in
										Detail</a>
								</div>
							</div>
						</div>
					</div>
				</div>
			</form>
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
				$("#n_test").addClass("active"); 
				if (${scriptList?size} == 0) {
					alert ("User has not script yet! Please create a script first.");
					return;
				}
				$("#homeTab a:first").tab('show');
				
				$('a[data-toggle="tab"]').on('show', function(e) {
					//alert("current tab: " + e.target + "\nlast tab: " + e.relatedTarget);
				});	
							
				$("#dSelect").append(getOption(100));
				$("#dSelect").change(getDurationMS);
				
				$("#hSelect").append(getOption(24));
				$("#hSelect").change(getDurationMS);
				
				$("#mSelect").append(getOption(60));
				$("#mSelect").change(getDurationMS);
				
				$("#sSelect").append(getOption(60));
				$("#sSelect").change(getDurationMS);
				
				//add toggle event to threshold
				$("#runcountChkbox").change(function (){
					if ($("#runcountChkbox").attr("checked") == "checked") {
						$("#threshold").val("R");
						$("#durationChkbox").removeAttr("checked");
					}
				});
				$("#durationChkbox").change(function (){
					if ($("#durationChkbox").attr("checked") == "checked") {
						$("#threshold").val("D");
						$("#runcountChkbox").removeAttr("checked");
					}
				});
				
				$("#agentCount").change (function() {
					updateVuserTotal ();
				});
				
				$("#vuserPerAgent").change (function() {
					updateVuserPolicy ();
				});
				
				$("#reportLnk").click(function () {
					generateReportChart();
				});
				
				initThresholdChkBox();
				initDuration();
			});
			
			function updateVuserTotal () {
				var agtCount = $("#agentCount").val();
				var vcount = $("#vuserPerAgent").val();
				$("#vuserTotal").text("Vuser:" + agtCount*vcount);
			}
			
			function updateVuserPolicy() {
				updateVuserTotal();
				$('#messageDiv').ajaxSend(function() {
					showInformation("Updating vuser policy from server...");
				});

				$.ajax({
			  		url: "${req.getContextPath()}/perftest/updateVuser",
					dataType:'json',
					data: {'newVuser': $("#vuserPerAgent").val()},
			    	success: function(res) {
			    		if (res.success) {
				    		var processCount = res.processCount;
				    		var threadCount = res.threadCount;
				    		$('#processes').val(processCount);
				    		$('#threads').val(threadCount);
				    		updateChart();
							return true;
			    		} else {
				    		showErrorMsg("Update vuser failed:" + res.message);
							return false;
			    		}
			    	},
			    	error: function() {
			    		showErrorMsg("Error!");
						return false;
			    	}
			  	});
			}
			function initThresholdChkBox() {
				if ($("#testId").value == 0 || $("#threshold").value == "R") { //runcount
					$("#runcountChkbox").attr("checked", "checked");
					$("#durationChkbox").removeAttr("checked");
				} else { //duration
					$("#durationChkbox").attr("checked", "checked");
					$("#runcountChkbox").removeAttr("checked");					
				}
			}
			
			function initDuration() {
				var duration = $("#duration").val();
				var durationInSec = parseInt(duration / 1000);
		        var durationD = parseInt(durationInSec /(60*60*24))
		        var durationH = parseInt((durationInSec%(60*60*24))/3600);
		        var durationM = parseInt((durationInSec%3600)/60);
		        var durationS = durationInSec%60;
				$("#dSelect").val(durationD);
				$("#hSelect").val(durationH);
				$("#mSelect").val(durationM);
				$("#sSelect").val(durationS);
			}
			
			function getDurationMS() {
				var durationD = parseInt($("#dSelect").val());     
				var durationH = parseInt($("#hSelect").val());   
				var durationM = parseInt($("#mSelect").val());   
				var durationS = parseInt($("#sSelect").val());
				var durationMs = (durationS + durationM * 60 + durationH * 3600 + durationD * 3600*24) * 1000;
				$("#duration").val(durationMs);
				return durationMs;
			}
			
			function toggleThreshold() {
				$("#runcountChkbox").toggle();
				$("#durationChkbox").toggle();
			}
			
			function getOption(cnt) {
				var contents = [];
				
				for(i = 0; i < cnt; i++) {
					contents.push("<option value='" + i + "'>" + i + "</option>");
				}
				
				return contents.join("\n");
			}
			
			function generateReportChart() {
				showInformation("Generating TPS Chart...");
			}
			
		</script>
</body>
</html>