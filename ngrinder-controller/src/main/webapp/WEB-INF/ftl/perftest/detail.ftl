<!DOCTYPE html>
<html>
<head>
<title>nGrinder Performance Test Detail</title>
<#include "../common/common.ftl">
<#include "../common/jqplot.ftl">
<link href="${req.getContextPath()}/css/slider.css" rel="stylesheet">
<link href="${req.getContextPath()}/plugins/datepicker/css/datepicker.css" rel="stylesheet">
<style>
div.div-resources {
	border: 1px solid #D6D6D6;
	height: 80px;
	margin-bottom: 8px;
	overflow-y: scroll;
	border-radius: 3px 3px 3px 3px;
}

div.div-resources .resource {
	color: #666666;
	display: inline-block;
	margin-left: 7px;
	margin-top:2px;
	margin-bottom:2px;
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
	margin-left: 7px;
	margin-top:2px;
	margin-bottom:2px;
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
	margin-bottom: 12px;
}

.table thead th {
    vertical-align: middle;
}

.rampChart {
	width: 450px; 
	height: 355px
}
</style>

</head>

<body>
	<#include "../common/navigator.ftl">
	<div class="container">
		<form id="testContentForm"  name="testContentForm" action="${req.getContextPath()}/perftest/create" method="POST" style="margin-bottom:0;">
			<div class="well" style="padding:10px">
				<input type="hidden" id="testId" name="id" value="${(test.id)!}">
				<input type="hidden" id="threshold" name="threshold" value="${(test.threshold)!"D"}">
				<input type="hidden" id="threads" name="threads" value="${(test.threads)!0}">
				<input type="hidden" id="processes" name="processes" value="${(test.processes)!0}">						

				<div class="form-horizontal form-horizontal-1">
					<fieldset>
						<div class="control-group">
							<label for="testName" class="control-label">Test Name</label>
							<div class="controls">  
								<input class="span3 required" size="40" type="text" id="testName" name="testName" value="${(test.testName)!}">
								<#if test??>
								<span  
										<#if test.status == 'STOP_ON_ERROR'>
											 rel="popover"
											 data-content="Error on ${test.testErrorCause} phase. ${(test.testErrorStackTrace)! ?replace('\n', '<br/>')?html}" 
											 data-original-title="${test.status}"
											 type="toggle"
										<#else>
											 rel="popover"
											 data-content="${test.createdDate}" 
											 data-original-title="${test.status}"
											 type="toggle"
										</#if>
								>
								
									<img src="${req.getContextPath()}/img/ball/${test.status.iconName}"/>
								</span>
								</#if>
								<button type="submit" class="btn btn-primary pull-right" style="margin-left:5px;margin-right:70px" data-toggle="modal" href="#scheduleModal"  id="saveScheduleBtn">
									<#if test?? && (test.status != "SAVED")>Clone<#else>Save</#if> and Start
								</button>  
								<button type="submit" class="btn btn-success  pull-right" style="margin-left:5px"  id="saveTestBtn"> 
									<#if test?? && (test.status != "SAVED")>Clone<#else>Save</#if>
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
				<ul class="nav nav-tabs" id="homeTab" style="margin-bottom:5px">
					<li><a href="#testContent" data-toggle="tab">Test Configuration</a></li>
					<#if test?? && (test.status == "TESTING")>
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
													<input type="text" class="input required positiveNumber span2" 
														number_limit="${(maxAgentSizePerConsole)}" id="agentCount" name="agentCount" 
														value="${(test.agentCount)!}"><span class="add-on">MAX : ${(maxAgentSizePerConsole)}</span>
												</div>
											</div>
										</div>
										<div class="control-group">
											<label for="vuserPerAgent" class="control-label">Vuser per agent</label>
											<div class="controls">
												<div class="input-append">
													<input type="text" class="input required positiveNumber span2" rel="popover"
														number_limit="${(maxVuserPerAgent)}"
														id="vuserPerAgent" name="vuserPerAgent" value="${(test.vuserPerAgent)!}"
														data-content="Input vuser count for every agent." 
														data-original-title="Vuser count" ><span class="add-on">MAX : ${(maxVuserPerAgent)}</span>
												</div>
												<#assign vuserTotal = (test.vuserPerAgent)!0 * (test.agentCount)!0 />
												<span class="badge badge-info pull-right" id="vuserTotal">Vuser: ${vuserTotal}</span>
											</div>
										</div>
										<div class="control-group">
											<label for="scriptName" class="control-label">Script</label>
											<div class="controls">
												<select id="scriptName" class="required" name="scriptName">
													<#if scriptList?? && scriptList?size &gt; 0>
														<#list scriptList as scriptItem>
															<#if test?? && scriptItem.fileName == test.scriptName>
																<#assign isSelected = "selected"/>
															<#else>
																<#assign isSelected = ""/>
															</#if>
															<option value="${scriptItem.path}"${isSelected}>${scriptItem.path}</option>
														</#list>
													</#if>
												</select>
											</div>
										</div>
										<div class="control-group">
											<label for="Script Resources" class="control-label">Script Resources</label>
											<div class="controls">
												<div class="div-resources read-only" id="scriptResources" readonly="readonly"> 
												</div>
											</div> 
										</div>
										
										<div class="control-group">
											<label class="control-label">Target Host</label>
											<div class="controls">
												<div class="div-host"></div>
												<input type="hidden" name="targetHosts" id="hostsHidden" value="${(test.targetHosts)!}">
												<a class="btn pull-right btn-mini" data-toggle="modal" href="#addHostModal">Add</a>
											</div>
										</div>
										<hr>
										<div class="control-group">
											<label class="control-label"> 
												<input type="radio" id="durationChkbox"> Duration
											</label>
											<div class="controls docs-input-sizes"> 
												<select	class="select-item" id="hSelect"></select> : 
												<select	class="select-item" id="mSelect"></select> : 
												<select	class="select-item" id="sSelect"></select>
												&nbsp;&nbsp;
												<code>HH:MM:SS</code>
												<input type="hidden" id="duration" class="required positiveNumber" name="duration" value="${(test.duration)!0}">
												<div id="durationSlider" class="slider" style="margin-left:0; width:250px"></div>
												<input id="hiddenDurationInput" class="span1 hide" 
														data-slider="#durationSlider"
														data-max="40" data-min="0" data-step="1">
												
											</div>
										</div>
										<div class="control-group">
											<label for="runCount" class="control-label"> <input
												type="radio" id="runcountChkbox"> Run Count
											</label>
											<div class="controls">
												<div class="input-append">
													<input type="text" id="runCount" class="input span2"
														number_limit="${(maxRunCount)}"
														name="runCount" 
														value="${(test.runCount)!0}"><span class="add-on">MAX : ${(maxRunCount)}</span>
												</div>
											</div>
										</div>
										<div class="control-group">
											<label for="ignoreSampleCount" class="control-label">
												Ignore Count </label>
											<div class="controls">
												<input type="text" class="input required CountNumber"
													id="ignoreSampleCount" name="ignoreSampleCount"
													value="${(test.ignoreSampleCount)!0}">
											</div>
										</div>
										<div class="control-group">
											<label for="sampleInterval" class="control-label">
												Sample Interval </label>
											<div class="controls">
												<input type="text" class="input required positiveNumber"
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
															<input type="text" class="input input-mini required CountNumber"
																id="initProcesses" name="initProcesses"
																value="${(test.initProcesses)!0}" />
														</div>
													</div>
													<div class="control-group">
														<label for="processIncrement" class="control-label">
															Ramp-Up </label>
														<div class="controls">
															<input type="text" class="input input-mini required positiveNumber"
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
															Initial Sleep Time </label>
														<div class="controls">
															<input type="text" class="input input-mini required CountNumber"
																id="initSleepTime" name="initSleepTime"
																value="${(test.initSleepTime)!0}">
															<code>MS</code>
														</div>
													</div>
													<div class="control-group">
														<label for="processIncrementInterval" class="control-label">
															Processes Every </label>
														<div class="controls">
															<input type="text" class="input input-mini required positiveNumber"
																id="processIncrementInterval"
																name="processIncrementInterval"
																value="${(test.processIncrementInterval)!1000}">
															<code>MS</code>
														</div>
													</div>
												</fieldset>
											</div>
										</td>
									</tr>
								</table>
								
								<div id="rampChart" class="rampChart"></div>
							</div>
						</div>
					</div>
					<div class="tab-pane" id="reportContent">
						<div class="row">
							<div class="span4">
								<div class="page-header">
									<h4>Summary</h4>
								</div>
								<div class="form-horizontal form-horizontal-3" style="margin-left: 10px">
									<fieldset>
										<div class="control-group">
											<label for="agentInput" class="control-label control-label-1">TPS</label>
											<div class="controls">
												<strong>Total ${(test.tps)!}</strong>
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
							<div class="span8" style="margin-top:10px;">
								<div id="tpsDiv" class="chart" style="width:610px; height:240px"></div>
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
								<div class="page-header">
									<h4>Summary</h4>
								</div>
								<div class="form-horizontal form-horizontal-3" style="margin-top:10px;"> 
									<fieldset>
										<div class="control-group">
											<label for="agentCount" class="control-label">Script File Name</label>
											<div class="controls">
												${(test.scriptName)!}
											</div>
										</div>
										<hr>
										<div class="control-group">
											<label for="vuserPerAgent" class="control-label">Vusers</label>
											<div class="controls">
												<strong>${(test.vuserPerAgent)!}</strong>
											</div>
										</div>
										<div class="control-group">
											<label for="scriptName" class="control-label">Agents</label>
											<div class="controls">
												<span>${(test.agentCount)!}</span><a class="btn btn-mini btn-info" id="agentInfoBtn" href="#agentListModal" data-toggle="modal">Info</a>
											</div>
										</div>
										<div class="control-group">
											<label class="control-label">Processes</label>
											<div class="controls">
												${(test.processes)!}
												<span class="badge badge-info pull-right">Running <data id="process_data"></data></span>
											</div>
										</div>
										<div class="control-group">
											<label class="control-label">Threads</label>
											<div class="controls">
												${(test.threads)!}
												<span class="badge badge-info pull-right">Running <data id="thread_data"></data></span>
											</div>
										</div>
										<hr>
										<div class="control-group">
											<label class="control-label">Target Host</label>
											<div class="controls">
												${(test.targetHosts)!}
											</div>
										</div>
										<hr>
										<div class="control-group">
											<label class="control-label"> 
												Duration
											</label>
											<div class="controls">
												<span>${(test.durationStr)!}</span><code>HH:MM:SS</code>
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
											<div class="controls" style="margin-top:0">
												<input type="text" class="input span2"
													id="sampleInterval" name="sampleInterval"
													value="${(test.sampleInterval)!1000}">
												<code>MS</code>
											</div>
										</div>
										<!--
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
										-->
									</fieldset>
								</div>
							</div>
							<div class="span7">
								<div class="page-header">
									<h4>Statistics</h4>
								</div>
								<div id="runningTps"class="chart" style="width:530px; height:195px"></div>
								<div class="tabbable">
									<ul class="nav nav-pills" style="margin20px 0" id="tableTab">
									    <li><a href="#lsTab" tid="ls">Latest Sample</a></li>
									    <li><a href="#asTab" tid="as">Accumulated Statistics</a></li>
									    <!--<li class="pull-right"><a href="#" target="_blank">Expand View</a></li>-->
								    </ul>
								    <div class="tab-content">
								    	<div class="tab-pane active" id="lsTab">
											<table class="table table-striped table-bordered ellipsis" id="lsTable">
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
														<th class="noClick ellipsis">Test Name</th>
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
										<div class="tab-pane active" id="asTab">
											<table class="table table-striped table-bordered ellipsis" id="asTable">
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
														<th class="noClick ellipsis">Test Name</th>
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
						</div>
					</div>
				</div>
				</div>
				<input type="hidden" id="scheduleInput" name="scheduledTime"/>
				<#if test??>
					<input type="hidden" id="testStatus"  name="status" value="${(test.status)}">
				<#else> 
					<input type="hidden" id="testStatus"  name="status" value="SAVED">		
				</#if>				
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
				<a class="btn btn-primary" id="runNowBtn">Run NOW</a>
				<a class="btn btn-primary" id="addScheduleBtn">Schedule</a>
			</div>
		</div>
	
	<script src="${req.getContextPath()}/plugins/datepicker/js/bootstrap-datepicker.js"></script>
	<script src="${req.getContextPath()}/js/rampup.js"></script>
	<script src="${req.getContextPath()}/js/bootstrap-slider.min.js"></script>
	
	<script>
	   var objTimer;
	   var sliderMax = 40;
	   var durationMap = {};
	   durationMap[0] = 0;
	   for (var i = 1; i <= sliderMax; i++) {
		   if (i <= 10) {
			   durationMap[i] = durationMap[i-1] + 1;
		   } else if (i <= 20) {
			   durationMap[i] = durationMap[i-1] + 5;
		   } else if (i <= 32) { //untill 180 min
			   durationMap[i] = durationMap[i-1] + 10;
		   } else if (i <= 38) { //360 min
			   durationMap[i] = durationMap[i-1] + 30;
		   } else if (i <= 56) { //24 hours
			   durationMap[i] = durationMap[i-1] + 60;
		   } else if (i <= 72) {
			   durationMap[i] = durationMap[i-1] + 60*6;
		   } else if (i <= 78) {
			   durationMap[i] = durationMap[i-1] + 60*12;
		   } else {
			   durationMap[i] = durationMap[i-1] + 60*24;
		   }
	   }
	   $(document).ready(function() {
				var date = new Date();
				$("#sDateInput").val(('0'+date.getFullYear()).substr(-4,4)+'-'+('0'+(date.getMonth() +1)).substr(-2,2)+'-'+ ('0'+date.getDate()).substr(-2,2));
				<#if test?exists>
			    objTimer = window.setInterval("refreshData()", ${test.sampleInterval!1000});
			    </#if>
			    
				$("#n_test").addClass("active");
				<#if !scriptList?? || scriptList?size == 0>
					showErrorMsg("User has not script yet! Please create a script first.");
				</#if>
				
				$("#homeTab a:first").tab('show');	
				$("#tableTab a:first").tab('show');	
				
				$('#testContentForm input').hover(function() {
			        $(this).popover('show')
			    });
				
				for (var i=0; i<=sliderMax; i++) {
					if (durationMap[i] * 60000 == $("#duration").val()) {
						$("#hiddenDurationInput").val(i);
					}
				}
				
				$("#scriptName").change(function() {
					updateScriptResources();
				});
				
			    $("#hiddenDurationInput").bind("slide", function(e){
			    		$("#duration").val(durationMap[this.value] * 60000);
			    		initDuration();
			    		$("#duration").valid();
			    });
			    	
			    $("#testContentForm").validate({
			    	ignore: "", //make the validation on hidden input work
			        errorClass: "help-inline",
			        errorElement: "span",
			        errorPlacement: function(error, element) {
			        	if (element.next().attr("class") == "add-on") {
			        		error.insertAfter(element.next());
			        	} else {
			        		error.insertAfter(selement);
			        	}
			        },
			        highlight:function(element, errorClass, validClass) {
			            $(element).parents('.control-group').addClass('error');
			            $(element).parents('.control-group').removeClass('success');
			        },
			        unhighlight: function(element, errorClass, validClass) {
			            $(element).parents('.control-group').removeClass('error');
			            $(element).parents('.control-group').addClass('success');
			        }
			    });
			    $("#addHostBtn").click(function() {
					var curHostDiv = $(".div-host").html();
					var curHostVal = $("#hostsHidden").val();
					var content = "";
					
					
					if (!checkEmptyByID("domainInput")) {
						content = getValueByID("domainInput");
					} 
					content = content + ":";
					if (!checkEmptyByID("ipInput")) {
						content = content + getValueByID("ipInput");
					}
					
					
					if (content == ":") {
						$("#addHostModal small").addClass("errorColor");
						return;
					}
					
					curHostDiv += hostItem(content);					
					$(".div-host").html(curHostDiv);
					buildHost();
					$("#addHostModal").modal("hide");
					$("#addHostModal small").removeClass("errorColor");
				});
				
				function hostItem(content) {
			   	   return "<p class='host'>" + content + "  <a href='javascript:void(0);'><i class='icon-remove-circle'></i></a><input type='hidden' class='hostsItem' value='" + content + "'></p><br>"
			    }
			
				function buildHost() {
					var contents = [];
					$(".hostsItem").each(function() {
						contents.push($(this).val());
					});
					$("#hostsHidden").val(contents.join(","));
				}
				
				function initHosts() {
					if (checkEmptyByID("hostsHidden")) {
						return;
					}
					
					var contents = $("#hostsHidden").val().split(",");
					var str = "";
					for (i = 0; i < contents.length; i++) {
						str += hostItem($.trim(contents[i]));
					}
					
					$(".div-host").empty();
					$(".div-host").html(str);
				}
				
				
				$("i.icon-remove-circle").live('click', function() {
					var $elem = $(this).parents("p");
					$elem.next("br").remove();
					$elem.remove();
					buildHost();
				});
				
				$("#saveTestBtn").click (function() {
					if (!$("#testContentForm").valid()) {
						return false;
					}
					<#if test?? && (test.status != "SAVED")>
						$("#testId").val("");
						$("#testStatus").val("SAVED");
					</#if>
					$("#scheduleInput").attr('name','');
					return true;
				});
				
				$("#runNowBtn").click(function() {
					$("#scheduleModal").modal("hide");
					$("#scheduleModal small").html("");
					$("#scheduleInput").attr('name','');
					$("#testStatus").val("READY");
					document.testContentForm.submit();
				});
								
				$("#addScheduleBtn").click(function() {
					if (checkEmptyByID("sDateInput")) {
						$("#scheduleModal small").html("Please select date before schedule.");
						return;
					}
					
					var timeStr = $("#sDateInput").val() + " " + $("#shSelect").val() + ":" + $("#smSelect").val() +":0";
					var scheduledTime = new Date(timeStr.replace(/-/g,"/"));
					if (new Date() > scheduledTime) {
						$("#scheduleModal small").html("Schedule time must be later than now.");
						return;
					}
					$("#scheduleInput").val(scheduledTime);
					$("#scheduleModal").modal("hide");
					$("#scheduleModal small").html("");
					<#if test?? && (test.status != "SAVED")>
						$("#testId").val("");
					</#if>
					$("#testStatus").val("READY");
					document.testContentForm.submit();
				});
				
				
				
				$('#sDateInput').datepicker({
					format: 'yyyy-mm-dd'
				});
						
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
						$("#runCount").addClass("required");
						$("#runCount").addClass("positiveNumber");
						$("#durationChkbox").removeAttr("checked");
						$("#duration").removeClass("required");
						$("#duration").removeClass("positiveNumber");
						$("#duration").valid();
						$("#runCount").valid();
					}
				});
				$("#durationChkbox").change(function (){
					if ($("#durationChkbox").attr("checked") == "checked") {
						$("#threshold").val("D");
						$("#duration").addClass("required positiveNumber");
						$("#runcountChkbox").removeAttr("checked");
						$("#runCount").removeClass("required");
						$("#runCount").removeClass("positiveNumber");
						$("#duration").valid();
						$("#runCount").valid();
					}
				});
				
				$("#agentCount").change(function() {
					updateVuserTotal();
				});
				
				$("#vuserPerAgent").change (function() {
					if ($("#vuserPerAgent").valid()) {
						updateVuserPolicy();
					}
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
				initHosts();
				initDuration();
				updateChart();
				resetFooter();
				updateScriptResources();
			});
			
			function updateVuserTotal () {
				var agtCount = $("#agentCount").val();
				var vcount = $("#vuserPerAgent").val();
				$("#vuserTotal").text("Vuser:" + agtCount*vcount);
			}
			
			function updateScriptResources() {
				$('#messageDiv').ajaxSend(function() {
					showInformation("Updating script resources...");
				});
				$.ajax({
			  		url: "${req.getContextPath()}/perftest/getResourcesOnScriptFolder",
					dataType:'json',
					data: {'scriptPath': $("#scriptName").val()},
			    	success: function(res) {
			    		var html = "";
				    	var len=res.length;
						for(var i=0; i<len; i++) {
							var value = res[i];
							html = html + "<div class='resource'>" + value + "</div><br/>";
						}
						$("#scriptResources").html(html);
			    	},
			    	error: function() {
			    		showErrorMsg("Error!");
						return false;
			    	}
			  	});
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

				    		//if ramp-up chart is not enabled, update init process count as total 
				    		if (!$("#rampupCheckbox")[0].checked) {
				    			$('#initProcesses').val($('#processes').val());
				    		}
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
		        var durationH = parseInt((durationInSec%(60*60*24))/3600);
		        var durationM = parseInt((durationInSec%3600)/60);
		        var durationS = durationInSec%60;
				$("#hSelect").val(durationH);
				$("#mSelect").val(durationM);
				$("#sSelect").val(durationS);
			}
			
			function getDurationMS() {
				var durationH = parseInt($("#hSelect").val());   
				var durationM = parseInt($("#mSelect").val());   
				var durationS = parseInt($("#sSelect").val());
				var durationMs = (durationS + durationM * 60 + durationH * 3600) * 1000;
				var durationObj = $("#duration");
				durationObj.val(durationMs);
				durationObj.valid(); //trigger validation
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
				getReportDataTPS();
			}
			
			function getReportDataTPS(){
			    $.ajax({
                    url: "${req.getContextPath()}/perftest/getReportData",
                    dataType:'json',
                    data: {'testId': $("#testId").val(),
                           'dataType':'tps_total',
                           'imgWidth':$("#tpsDiv").width()},
                    success: function(res) {
                        if (res.success) {
                            showChart('tpsDiv', res.tps_total);
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
				var refreshDiv = $("<div></div>");
				var url = "${req.getContextPath()}/perftest/running/refresh?testId=" + $("#testId").val();
				refreshDiv.load(url, function(){
					var succesVal = refreshDiv.find("#input_status").val();
		
					if(succesVal == 'SUCCESS'){
						$("#lsTable tbody").empty();
						$("#asTable tbody").empty();
						$("#lsTable tbody").prepend(refreshDiv.find("#lsTableItem"));
						$("#asTable tbody").prepend(refreshDiv.find("#asTableItem"));
						
						$("#process_data").text(refreshDiv.find("#input_process").val());
						$("#thread_data").text(refreshDiv.find("#input_thread").val());
						
						$("#runningTps").empty();
						tpsPlot = showChart('runningTps', refreshDiv.find("#tpsChartData").val());
					}else{
						if (objTimer){
							window.clearInterval(objTimer);
							//window.clearInterval(countTime);
						}
					}
				});
			}
			
			function showChart(containerId, data) {
				drawChart('TPS', containerId, data);
            }
		</script>
</body>
</html>
