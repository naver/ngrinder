<!DOCTYPE html>
<html>
<head>
<title>nGrinder Performance Test Detail</title>
<#include "../common/common.ftl">
<#include "../common/jqplot.ftl">
<link href="${req.getContextPath()}/plugins/datepicker/css/datepicker.css" rel="stylesheet">
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

div.chart { 
	border: 1px solid #878988; 
	height: 195px; 
	min-width: 400px; 
	margin-bottom: 12px
}

.table thead th {
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
			<div class="well" style="padding:10px">
				<input type="hidden" id="testId" name="id" value="${(test.id)!}">
				<input type="hidden" id="threshold" name="threshold" value="${(test.threshold)!"D"}">
				<input type="hidden" id="threads" name="threads" value="${(test.threads)!0}">
				<input type="hidden" id="processes" name="processes" value="${(test.processes)!0}">						

				<div class="form-horizontal">
					<fieldset>
						<div class="control-group">
							<label for="testName" class="control-label">Test Name</label>
							<div class="controls">  
								<input class="span6" size="40" type="text" id="testName" name="testName" value="${(test.testName)!}">
								<button type="submit" class="btn btn-success btn-primary" style="margin-left:70px"> 
									<#if test??>Clone<#else>Save</#if> and Start 
								</button>  
								<button type="submit" class="btn btn-primary" data-toggle="modal" href="#scheduleModal">
									<#if test??>Clone<#else>Save</#if> and Schedule
								</button> 
							</div>
						</div>
						<div class="control-group" style="margin-bottom: 0">
							<label for="description" class="control-label">Description</label>
							<div class="controls">  
								<textarea class="input-xlarge span9" id="description" rows="3" name="description" style="resize:none">${(test.description)!}</textarea>
							</div>
						</div>
					</fieldset>
				</div>
			</div>
			<div class="tabbable">
				<ul class="nav nav-tabs" id="homeTab">
					<li><a href="#testContent" data-toggle="tab">Test Configuration</a></li>
					<#if test?? && (test.status == "TESTING" || test.status != "")>
						<li><a href="#runningContent" data-toggle="tab">Test Running</a></li>
					</#if>
					<#if test?? && (test.status == "FINISHED" || test.status == "CANCELED")>
						<li><a href="#reportContent" data-toggle="tab" id="reportLnk">Report</a></li>
					</#if>
				</ul>
				<div class="tab-content">
					<div class="tab-pane" id="testContent">
						<div class="row">
							<div class="span6">
								<div class="page-header">
									<h4>Basic Configuration</h4>
								</div>
								<div class="form-horizontal form-horizontal-2"> 
									<fieldset>
										<div class="control-group">
											<label for="agentCount" class="control-label">Agent</label>
											<div class="controls">
												<div class="input-append">
													<input type="text" class="input"
														id="agentCount" name="agentCount" value="${(test.agentCount)!}">
												</div>
											</div>
										</div>
										<div class="control-group">
											<label for="vuserPerAgent" class="control-label">Vuser per agent</label>
											<div class="controls">
												<div class="input-append">
													<input type="text" class="input"
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
												<div class="div-host"></div>
												<a class="btn pull-right btn-mini" data-toggle="modal" href="#addHostModal">Add</a>
											</div>
										</div>
										<hr>
										<div class="control-group">
											<label class="control-label"> 
												<input type="radio" id="durationChkbox"> Duration
											</label>
											<input type="hidden" id="duration" name="duration"
												value="${(test.duration)!0}">
											<div class="controls">
												<select class="select-item" id="dSelect"></select> : 
												<select	class="select-item" id="hSelect"></select> : 
												<select	class="select-item" id="mSelect"></select> : 
												<select	class="select-item" id="sSelect"></select>
												&nbsp;&nbsp;
												<code>DD:HH:MM:SS</code>
											</div>
										</div>
										<div class="control-group">
											<label for="runCount" class="control-label"> <input
												type="radio" id="runcountChkbox"> Run Count
											</label>
											<div class="controls">
												<input type="text" class="input" id="runCount"
													name="runCount" value="${(test.runCount)!0}">
											</div>
										</div>
										<div class="control-group">
											<label for="ignoreSampleCount" class="control-label">
												Ignore Count </label>
											<div class="controls">
												<input type="text" class="input"
													id="ignoreSampleCount" name="ignoreSampleCount"
													value="${(test.ignoreSampleCount)!0}">
											</div>
										</div>
										<div class="control-group">
											<label for="sampleInterval" class="control-label">
												Sample Interval </label>
											<div class="controls">
												<input type="text" class="input"
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
										<h4>Enable Ramp-Up <small>(ramp-up chart for every agent)</small></h4>
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
								<div id="rampChart1" class="rampChart"></div>
								<div id="rampChart2" class="rampChart hidden"></div>
							</div>
						</div>
					</div>
					<div class="tab-pane" id="reportContent">
						<div class="row">
							<div class="span3">
								<div class="form-horizontal form-horizontal-3" style="margin-left: 20px">
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
								<img id="tpsimg" src="" height="220" width="750" border="1"/>
								<div id="tpsdiv" style="min-width: 700px; height: 300px; margin: 0 auto"></div>
							</div>
						</div>
						<div class="row" style="margin-top: 10px;">
							<div class="span12">
								<a id="reportDetail" class="btn pull-right" href="#">Report in Detail</a>
							</div>
						</div>
					</div>
					<div class="tab-pane" id="runningContent">
						<div class="row">
							<div class="span5">
								<div class="form-horizontal form-horizontal-3"> 
									<fieldset>
										<div class="control-group">
											<label for="agentCount" class="control-label">Script File Name</label>
											<div class="controls">
												hellp.py
											</div>
										</div>
										<hr>
										<div class="control-group">
											<label for="vuserPerAgent" class="control-label">Vusers</label>
											<div class="controls">
												<strong>2</strong>
											</div>
										</div>
										<div class="control-group">
											<label for="scriptName" class="control-label">Agents</label>
											<div class="controls">
												<span>1</span><a class="btn btn-mini btn-info" id="agentInfoBtn" href="#agentListModal" data-toggle="modal">Info</a>
											</div>
										</div>
										<div class="control-group">
											<label class="control-label">Processes</label>
											<div class="controls">
												1
												<span class="badge badge-info pull-right">Running <data id="process_data">1</data></span>
											</div>
										</div>
										<div class="control-group">
											<label class="control-label">Threads</label>
											<div class="controls">
												2
												<span class="badge badge-info pull-right">Running <data id="thread_data">2</data></span>
											</div>
										</div>
										<hr>
										<div class="control-group">
											<label class="control-label">Target Host</label>
											<div class="controls">
												10.34.64.36
											</div>
										</div>
										<hr>
										<div class="control-group">
											<label class="control-label"> 
												Duration
											</label>
											<div class="controls">
												<span>00:00:01:00</span><code>DD:HH:MM:SS</code>
											</div>
										</div>
										<div class="control-group">
											<label for="ignoreSampleCount" class="control-label">
												Ignore Count 
											</label>
											<div class="controls">
												<span>0</span><code>sec</code>
											</div>
										</div>
										<hr>
										<div class="control-group">
											<label for="sampleInterval" class="control-label">
												Sample Interval
											</label>
											<div class="controls">
												<input type="text" class="input span2"
													id="sampleInterval" name="sampleInterval"
													value="${(test.sampleInterval)!1000}">
												<code>MS</code>
											</div>
										</div>
										<div class="control-group">
											<label for="collectSample" class="control-label">
												Collect Sample Forever
											</label>
											<div class="controls">
												<input type="text" class="input span2"
													id="collectSample" name="collectSample"
													value="${(test.collectSample)!1000}">
												<code>MS</code>
											</div>
										</div>
									</fieldset>
								</div>
							</div>
							<div class="span7">
								<div class="page-header">
									<h4>TPS</h4>
								</div>
								<div class="chart"></div>
								<ul class="nav nav-pills" style="margin20px 0" id="tableTab">
								    <li><a href="#" tid="ls">Latest Sample</a></li>
								    <li><a href="#" tid="as">Accumulated Statistics</a></li>
								    <li class="pull-right"><a href="#" target="_blank">Expand View</a></li>
							    </ul>
								<table class="table table-striped table-bordered ellipsis" id="testTable">
									<colgroup>
										<col width="30px">
										<col>
										<col width="85px">
										<col width="55px">
										<col width="50px">
										<col width="50px">
										<col width="50px">
										<col width="55px">
									</colgroup>
									<thead>
										<tr>
											<th class="noClick">ID</th>
											<th class="noClick">Test Name</th>
											<th class="noClick">Successful Tests</th>
											<th class="noClick">Errors</th>
											<th class="noClick">Mean Time</th>
											<th class="noClick">TPS</th>
											<th class="noClick">Peak TPS</th>
											<th class="noClick">MTSD</th>
										</tr>
									</thead>
									<tbody>
									</tbody>
								</table>
							</div>
						</div>
					</div>
				</div>
				<input type="hidden" id="scheduleInput" name="scheduleTime"/>
			</form>
			<!--content-->
			<#include "../common/copyright.ftl">
		</div>
		
		<!-- modal -->
		<div class="modal fade" id="addHostModal">
			<div class="modal-header">
				<a class="close" data-dismiss="modal">&times;</a>
				<h3>
					Add Host
					<small>Please input one option at least.</small>
				</h3>
			</div>
			<div class="modal-body">
				<div class="form-horizontal">
					<fieldset>
						<div class="control-group">
							<label for="domainInput" class="control-label">Domain</label>
							<div class="controls">
							  <input type="text" id="domainInput">
							  <span class="help-inline"></span>
							</div>
						</div>					
						<div class="control-group">
							<label for="ipInput" class="control-label">IP</label>
							<div class="controls">
							  <input type="text" id="ipInput">
							  <span class="help-inline"></span>
							</div>
						</div>					
					</fieldset>
				</div>
			</div>
			<div class="modal-footer">
				<a class="btn btn-primary" id="addHostBtn">Add</a>
			</div>
		</div>

		<div class="modal fade" id="scheduleModal">
			<div class="modal-header">
				<a class="close" data-dismiss="modal">&times;</a>
				<h3>
					Schedule Setting
					<small class="errorColor"></small>
				</h3>
			</div>
			<div class="modal-body">
				<div class="form-horizontal">
					<fieldset>
						<div class="control-group">
							<label class="control-label">Schedule</label>
							<div class="controls form-inline">
							  <input type="text" class="input span2" id="sDateInput" value="" readyonly>&nbsp;
							  <select id="shSelect" class="select-item"></select>
							  :
							  <select id="smSelect" class="select-item"></select>
							  <code>HH:MM</code>
							</div>
						</div>					
					</fieldset>
				</div>
			</div>
			<div class="modal-footer">
				<a class="btn btn-primary" id="addScheduleBtn">Schedule</a>
			</div>
		</div>
	
	<#include "../common/jqplot.ftl">
	<script src="${req.getContextPath()}/plugins/datepicker/js/bootstrap-datepicker.js"></script>
	<script src="${req.getContextPath()}/js/rampup.js"></script>
	<script>
	   var chart;
	   var objTimer;
			$(document).ready(function() {
				$("#n_test").addClass("active");
				if (${scriptList?size} == 0) {
					alert ("User has not script yet! Please create a script first.");
				}
				
				$("#homeTab a:first").tab('show');	
				$("#tableTab a:first").tab('show');	
				
				$("#addHostBtn").on('click', function() {
					var elemStr = "";
					if (!checkEmptyByID("ipInput")) {
						elemStr += hostItem("ipInput");
					}
					if (!checkEmptyByID("domainInput")) {
						elemStr += hostItem("domainInput");
					}
					if (elemStr == "") {
						$("#addHostModal small").addClass("errorColor");
						return;
					}
					
					$(".div-host").empty();
					$(".div-host").append(elemStr);
					$("#addHostModal").modal("hide");
					$("#addHostModal small").removeClass("errorColor");
				});
				
				$("i.icon-remove-circle").live('click', function() {
					var $elem = $(this).parents("p");
					$elem.next("br").remove();
					$elem.remove();
				});
				
				$("#addScheduleBtn").click(function() {
					if (checkEmptyByID("sDateInput")) {
						$("#scheduleModal small").html("Please select date before schedule.");
						return;
					}
					
					var timeStr = $("#sDateInput").val() + " " + $("#shSelect").val() + ":" + $("#smSelect").val() +":0";
					alert(timeStr);
					if (new Date() > new Date(timeStr.replace(/-/g,"/"))) {
						$("#scheduleModal small").html("Schedule time must be later than now.");
						return;
					}
					$("#scheduleInput").val(timeStr);
					$("#scheduleModal").modal("hide");
					$("#scheduleModal small").html("");
				});
				
				$('#sDateInput').datepicker({
					format: 'yyyy-mm-dd'
				});
						
				$("#dSelect").append(getOption(100));
				$("#dSelect").change(getDurationMS);
				
				$("#hSelect").append(getOption(24));
				$("#hSelect").change(getDurationMS);
				
				$("#mSelect").append(getOption(60));
				$("#mSelect").change(getDurationMS);
				
				$("#sSelect").append(getOption(60));
				$("#sSelect").change(getDurationMS);
				
				$("#shSelect").append(getOption(24));
				$("#smSelect").append(getOption(60));

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
				
				$("#agentCount").change(function() {
					updateVuserTotal ();
				});
				
				$("#vuserPerAgent").change (function() {
					updateVuserPolicy ();
				});
				
				$("#reportLnk").click(function () {
					generateReportChart();
				});
				
				$("#reportDetail").click(function () {
                    window.open("${req.getContextPath()}/perftest/report?testId="+$("#testId").val());
                });
                
                $('#tableTab a').click(function (e) {
				    var $this = $(this);
				    if ($this.hasClass("pull-right")) {
				    } else {
					    e.preventDefault();
				    	$this.attr("tid");
				    	$this.tab('show');
				    }
			    });
			    
			    $("#homeTab a").click(function () {
			    	resetFooter();
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
			
			function hostItem(id) {
				return "<p class=\"host\">" + $.trim($("#" + id).val()) + "<a href=\"javascript:void(0);\"><i class=\"icon-remove-circle\"></i></a></p><br>"
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
				getReportDataTPS();
			}
			
			function getReportDataTPS(){
			    $.ajax({
                    url: "${req.getContextPath()}/perftest/getReportData",
                    dataType:'json',
                    data: {'testId': $("#testId").val(),
                           'dataType':'tps_total',
                           'imgWidth':$("#tpsimg").width()},
                    success: function(res) {
                        if (res.success) {
                            drawChart('TPS', 'tpsDiv', res.tps_total);
                            return true;
                        } else {
                            showErrorMsg("Get report data failed.");
                            return false;
                        }
                    },
                    error: function() {
                        showErrorMsg("Error!");
                        return false;
                    }
                });
			}
			
			function refreshData() {
				var refreshDiv = $("<div id=\"refreshDiv\"></div>");
				var type = $('#tableTab i.active').attr("tid");
				var url = "${req.getContextPath()}/perftest/running/refresh?type=" + type;
				refreshDiv.load(url, function(){
					var succesVal = refreshDiv.find("#input_status").val();
		
					if(succesVal == 'SUCCESS'){
						$("#testTable tbody").html(refreshDiv.children("tableItem").html());
						$("#process_data").text(refreshDiv.children("#input_process").val());
						$("#thread_data").text(refreshDiv.children("#input_thread").val());
						drawTPS(refreshDiv.children("tpsChartData").val());
					}else{
						if (objTimer){
							window.clearInterval(objTimer);
							window.clearInterval(countTime);
						}
					}
				});
			}
			
			function drawTPS(data) {
			}
			function drawChart(title, id, data) {
                var plot1 = $.jqplot(id, [data], { 
                    title: title, 
                    series: [{ 
                        label: '', 
                        neighborThreshold: -1 
                    }], 
                    axes: { 
                        xaxis: { 
                            tickRenderer: $.jqplot.CanvasAxisTickRenderer,
                            tickOptions: {
                              angle: -30
                            } 
                        }, 
                        yaxis: {  
                            renderer: $.jqplot.LogAxisRenderer
                        } 
                    }, 
                    cursor:{
                        show: true, 
                        zoom: true
                    }
                });
            }
		</script>
</body>
</html>