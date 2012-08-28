<!DOCTYPE html>
<html>
<head>
<title>nGrinder Performance Test Detail</title> <#include "../common/common.ftl"> <#include "../common/jqplot.ftl">
<link href="${req.getContextPath()}/css/slider.css" rel="stylesheet">
<link href="${req.getContextPath()}/plugins/datepicker/css/datepicker.css" rel="stylesheet">
<style>
div.div-resources {
	border: 1px solid #D6D6D6;
	height: 50px;
	margin-bottom: 8px;
	overflow-y: scroll;
	border-radius: 3px 3px 3px 3px;
}

div.div-resources .resource {
	color: #666666;
	display: inline-block;
	margin-left: 7px;
	margin-top: 2px;
	margin-bottom: 2px;
}

div.div-host {
	border: 1px solid #D6D6D6;
	height: 50px;
	margin-bottom: 8px;
	overflow-y: scroll;
	border-radius: 3px 3px 3px 3px;
}

div.div-host .host {
	color: #666666;
	display: inline-block;
	margin-left: 7px;
	margin-top: 2px;
	margin-bottom: 2px;
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

.jqplot-yaxis {
    margin-right: 10px;
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
		<form id="testContentForm" name="testContentForm" action="${req.getContextPath()}/perftest/create" method="POST"
			style="margin-bottom: 0;">
			<div class="well" style="padding: 10px">
				<input type="hidden" id="testId" name="id" value="${(test.id)!}"> 
				<input type="hidden" id="threshold"	name="threshold" value="${(test.threshold)!"D"}"> 
				<input type="hidden" id="threads" name="threads" value="${(test.threads)!0}"> 
				<input type="hidden" id="processes" name="processes" value="${(test.processes)!0}">

				<div class="form-horizontal form-horizontal-1">
					<fieldset>
						<div class="control-group">
							<label for="testName" class="control-label"><@spring.message "perfTest.table.testName"/></label>
							<div class="controls">
								<input class="span3 required" size="40" type="text" id="testName" name="testName" value="${(test.testName)!}">
								<#if test??> 
									<span id="teststatus_pop_over"
										rel="popover" data-content='${"${test.progressMessage}/n${test.lastProgressMessage}"?replace('/n', '<br>')?html}'  
											data-original-title="${test.status}" type="toggle">
										<img id="testStatus_img_id" src="${req.getContextPath()}/img/ball/${test.status.iconName}" />
									</span> 
								</#if>
								<button type="submit" class="btn btn-primary pull-right" style="margin-left: 5px; margin-right: 70px"
									data-toggle="modal" href="#scheduleModal" id="saveScheduleBtn">
									<#if test?? && (test.status != "SAVED")>
										<@spring.message "perfTest.detail.clone"/>
									<#else>
										<@spring.message "common.button.save"/>
									</#if>
									&nbsp;<@spring.message "perfTest.detail.andStart"/>
								</button>
								<button type="submit" class="btn btn-success  pull-right" style="margin-left: 5px" id="saveTestBtn">
									<#if test?? && (test.status != "SAVED")>
										<@spring.message "perfTest.detail.clone"/>
									<#else>
										<@spring.message "common.button.save"/>
									</#if>
								</button>
							</div>
						</div>
						<div class="control-group" style="margin-bottom: 0">
							<label for="description" class="control-label"><@spring.message "common.label.description"/></label>
							<div class="controls">
								<textarea class="input-xlarge span9" id="description" rows="3" name="description" style="resize: none">${(test.description)!}</textarea>
							</div>
						</div>
					</fieldset>
				</div>
			</div>
			<div class="tabbable">
				<ul class="nav nav-tabs" id="homeTab" style="margin-bottom: 5px">
					<li id="testContent_tab">
						<a href="#testContent" data-toggle="tab">
							<@spring.message "perfTest.configuration.testConfiguration"/>
						</a>
					</li> 
					<li id="runningContent_tab">
						<a href="#runningContent" data-toggle="tab">
							<@spring.message "perfTest.testRunning.title"/>
						</a>
					</li>
				
					<li id="reportContent_tab">
						<a href="#reportContent" data-toggle="tab" id="reportLnk">
							<@spring.message "perfTest.report.title"/>
						</a>
					</li>
				</ul>
				<div class="tab-content">
					<div class="tab-pane" id="testContent">
						<div class="row">
							<div class="span6">
								<div class="page-header">
									<h4><@spring.message "perfTest.configuration.basicConfiguration"/></h4>
								</div>
								<div class="form-horizontal form-horizontal-2">
									<fieldset>
										<div class="control-group">
											<label for="agentCount" class="control-label"><@spring.message "perfTest.configuration.agent"/></label>
											<div class="controls">
												<div class="input-append">
													<input type="text" class="input required positiveNumber span2" number_limit="${(maxAgentSizePerConsole)}"
														id="agentCount" name="agentCount" value="${(test.agentCount)!}"><span class="add-on"><@spring.message "perfTest.configuration.max"/>${(maxAgentSizePerConsole)}</span>
										 		</div>
											</div>
										</div>
										<div class="control-group">
											<label for="vuserPerAgent" class="control-label"><@spring.message "perfTest.configuration.vuserPerAgent"/></label>
											<div class="controls">
												<div class="input-append">
													<input type="text" class="input required positiveNumber span2" rel="popover"
														number_limit="${(maxVuserPerAgent)}" id="vuserPerAgent" name="vuserPerAgent"
														value="${(test.vuserPerAgent)!}" data-content="Input vuser count for every agent."
														data-original-title="Vuser count"><span class="add-on">
															<@spring.message "perfTest.configuration.max"/> ${(maxVuserPerAgent)}
														</span>
												</div>
												<#assign vuserTotal = (test.vuserPerAgent)!0 * (test.agentCount)!0 /> 
												<span class="badge badge-info pull-right" id="vuserTotal"><@spring.message "perfTest.configuration.availVuser"/> ${vuserTotal}</span>
											</div>
										</div>
										<div class="control-group">
											<label for="scriptName" class="control-label"><@spring.message "perfTest.configuration.script"/></label>
											<div class="controls">
												<select id="scriptName" class="required" name="scriptName"> 
												<#if scriptList?? && scriptList?size &gt; 0> 
													<#list scriptList as scriptItem> 
														<#if  test?? && scriptItem.path == test.scriptName> 
															<#assign isSelected = "selected"/> 
														<#else> 
															<#assign isSelected = 	""/> 
														</#if>
														<option value="${scriptItem.path}" ${isSelected}>${scriptItem.path}</option> 
													</#list> 
												</#if>
												</select>
												<input type="hidden" id="scriptRevision" name="scriptRevision" value="${(test.scriptRevision)!-1}">
												<button class="pull-right btn btn-mini btn-info" type="button" id="showScript" style="margin-top:3px"><@spring.message "perfTest.configuration.showScript"/></button>
											</div> 
										</div>
										<div class="control-group">
											<label for="Script Resources" class="control-label"><@spring.message "perfTest.configuration.scriptResources"/></label>
											<div class="controls">
												<div class="div-resources read-only" id="scriptResources" readonly="readonly"></div>
											</div>
										</div>

										<div class="control-group">
											<label class="control-label"><@spring.message "perfTest.configuration.targetHost"/></label>
											<div class="controls">
												<div class="div-host"></div>
												<input type="hidden" name="targetHosts" id="hostsHidden" value="${(test.targetHosts)!}"> 
												<a class="btn pull-right btn-mini" data-toggle="modal" href="#addHostModal" style="margin-right:20px;margin-top:-30px">   
													<@spring.message "perfTest.configuration.add"/>
												</a>
											</div>
										</div>
										<hr>
										<div class="control-group"> 
											<label class="control-label"> <input type="radio" id="durationChkbox" checked="true"> <@spring.message "perfTest.configuration.duration"/>
											</label>
											<div class="controls docs-input-sizes">
												<select class="select-item" id="hSelect"></select> : 
												<select class="select-item" id="mSelect"></select> : 
												<select class="select-item" id="sSelect"></select> &nbsp;&nbsp;
												<code>HH:MM:SS</code>
												<input type="hidden" id="duration" class="required positiveNumber" name="duration"
													value="${(test.duration)!60000}">
												<div id="durationSlider" class="slider" style="margin-left: 0; width: 250px"></div>
												<input id="hiddenDurationInput" class="span1 hide" data-slider="#durationSlider" data-max="39" data-min="1"
													data-step="1">

											</div>
										</div>
										<div class="control-group">
											<label for="runCount" class="control-label"> <input type="radio" id="runcountChkbox"> 
												<@spring.message "perfTest.configuration.runCount"/>
											</label>
											<div class="controls">
												<div class="input-append">
													<input type="text" id="runCount" class="input span2" number_limit="${(maxRunCount)}" name="runCount"
														value="${(test.runCount)!0}"><span class="add-on"><@spring.message "perfTest.configuration.max"/> ${(maxRunCount)}</span>
												</div>
											</div>
										</div>
										<div class="control-group">
											<label for="ignoreSampleCount" class="control-label"> <@spring.message "perfTest.configuration.ignoreSampleCount"/> </label>
											<div class="controls">
												<input type="text" class="input required CountNumber" id="ignoreSampleCount" name="ignoreSampleCount"
													value="${(test.ignoreSampleCount)!0}">
											</div>
										</div>
										<div class="control-group">
											<label for="sampleInterval" class="control-label"> <@spring.message "perfTest.configuration.sampleInterval"/> </label>
											<div class="controls">
												<input type="text" class="input required positiveNumber" id="sampleInterval" name="sampleInterval"
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
										<input type="checkbox" id="rampupCheckbox"
											<#if test?? && test.processes &gt; test.initProcesses>checked</#if> 
										/>
										<h4>
											<@spring.message "perfTest.configuration.rampEnable"/>
											<small> <@spring.message "perfTest.configuration.rampUpDes"/></small>
										</h4>
									</label>
								</div>
								<table>
									<tr>
										<td style="width: 50%">
											<div class="form-horizontal form-horizontal-2">
												<fieldset>
													<div class="control-group">
														<label for="initProcesses" class="control-label"> <@spring.message "perfTest.configuration.initalProcesses"/> </label>
														<div class="controls">
															<input type="text" class="input input-mini required CountNumber" id="initProcesses" name="initProcesses"
																value="${(test.initProcesses)!0}" />
														</div>
													</div>
													<div class="control-group">
														<label for="processIncrement" class="control-label"> <@spring.message "perfTest.configuration.rampup"/> </label>
														<div class="controls">
															<input type="text" class="input input-mini required positiveNumber" id="processIncrement"
																name="processIncrement" value="${(test.processIncrement)!1}">
														</div>
													</div>
												</fieldset>
											</div>
										</td>
										<td>
											<div class="form-horizontal form-horizontal-2">
												<fieldset>
													<div class="control-group">
														<label for="initSleepTime" class="control-label"> <@spring.message "perfTest.configuration.initalSleepTime"/> </label>
														<div class="controls">
															<input type="text" class="input input-mini required CountNumber" id="initSleepTime" name="initSleepTime"
																value="${(test.initSleepTime)!0}">
															<code>MS</code>
														</div>
													</div>
													<div class="control-group">
														<label for="processIncrementInterval" class="control-label"> 
															<@spring.message "perfTest.configuration.processesEvery"/> 
														</label>
														<div class="controls">
															<input type="text" class="input input-mini required positiveNumber" id="processIncrementInterval"
																name="processIncrementInterval" value="${(test.processIncrementInterval)!1000}">
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
									<h4><@spring.message "perfTest.report.summary"/></h4>
								</div>
								<div class="form-horizontal form-horizontal-3" style="margin-left: 10px">
									<fieldset>
										<div class="control-group">
											<label for="agentInput" class="control-label control-label-1"><@spring.message "perfTest.table.tps"/></label>
											<div class="controls">
												<strong>Total ${(test.tps)!}</strong>
											</div>
										</div>
										<div class="control-group">
											<label for="agentInput" class="control-label"><@spring.message "perfTest.table.meantime"/></label>
											<div class="controls">
												${(test.meanTestTime)!}
												<code>MS</code>
											</div>
										</div>
										<div class="control-group">
											<label for="agentInput" class="control-label"><@spring.message "perfTest.detail.peakTPS"/></label>
											<div class="controls">${(test.peakTps)!}</div>
										</div>
										<div class="control-group">
											<label for="agentInput" class="control-label"><@spring.message "perfTest.report.finishedTest"/></label>
											<div class="controls">${(test.tests)!}</div>
										</div>
										<div class="control-group">
											<label for="agentInput" class="control-label"><@spring.message "perfTest.table.errors"/></label>
											<div class="controls">${(test.errors)!}</div>
										</div>
									</fieldset>
								</div>
							</div>
							<div class="span8">
								<div class="page-header">
									<h4><@spring.message "perfTest.report.tpsgraph"/></h4>
									<a id="reportDetail" class="btn pull-right" style="margin-top:-10px" href="#"><@spring.message "perfTest.report.reportDetail"/></a>
								</div>
								<div id="tpsDiv" class="chart" style="width: 610px; height: 240px"></div>
							</div>								
						</div>
						<div class="row" style="margin-top: 10px;">
							<div class="span4">
								<#if logs??>
								
									<div class="page-header">
										<h4><@spring.message "perfTest.report.logs"/></h4>
									</div>
									<div class="form-horizontal form-horizontal-3" style="margin-left: 10px">
										<#list logs as eachLog>
											<div><a href="${req.getContextPath()}/perftest/downloadLog/${eachLog}?testId=${test.id}">${eachLog}</a></div> 
										</#list>									
									</div>
								</#if>
							</div>	
							<div class="span8">
								<div class="page-header">
										<h4><@spring.message "perfTest.report.testcomment"/></h4>
								</div>
								<div class="control-group"> 
									<textarea class="span8" id="testComment" rows="3" name="testComment" style="resize: none"> ${(test.testComment)!} </textarea>							
                        			<button class="btn btn-mini btn-primary" type="button" id="leaveCommentButton"><@spring.message "perfTest.report.leaveComment"/></button>
                                </div> 
							</div>
						</div>
					</div>
					<div class="tab-pane" id="runningContent">
						<div class="row">
							<div class="span5">
								<div class="page-header">
									<h4><@spring.message "perfTest.testRunning.summary"/></h4>
								</div>
								<div class="form-horizontal form-horizontal-3" style="margin-top: 10px;">
									<fieldset>
										<div class="control-group">
											<label for="agentCount" class="control-label"><@spring.message "perfTest.testRunning.scriptName"/></label>
											<div class="controls">${(test.scriptName)!}</div>
										</div>
										<hr>
										<div class="control-group">
											<label for="vuserPerAgent" class="control-label"><@spring.message "perfTest.table.vusers"/></label>
											<div class="controls">
												<strong>${(test.vuserPerAgent)!}</strong>
											</div>
										</div>
										<div class="control-group">
											<label for="scriptName" class="control-label"><@spring.message "perfTest.testRunning.agents"/></label>
											<div class="controls">
												<span>${(test.agentCount)!}</span>
												<!--<a class="btn btn-mini btn-info" id="agentInfoBtn" href="#agentListModal" data-toggle="modal">Info</a>-->
											</div>
										</div>
										<div class="control-group">
											<label class="control-label"><@spring.message "perfTest.testRunning.processes"/></label>
											<div class="controls">
												${(test.processes)!} 
												<span class="badge badge-info pull-right"><@spring.message "perfTest.testRunning.running"/> <data id="process_data"></data></span>
											</div>
										</div>
										<div class="control-group">
											<label class="control-label"><@spring.message "perfTest.testRunning.threads"/></label>
											<div class="controls">
												${(test.threads)!} <span class="badge badge-info pull-right"><@spring.message "perfTest.testRunning.running"/> <data id="thread_data"></data></span>
											</div>
										</div>
										<hr>
										<div class="control-group">
											<label class="control-label"><@spring.message "perfTest.configuration.targetHost"/></label>
											<div class="controls">${(test.targetHosts)!}</div>
										</div>
										<hr>
										<div class="control-group">
											<label class="control-label"> <@spring.message "perfTest.table.duration"/> </label>
											<div class="controls">
												<span>${(test.durationStr)!}</span>
												<code>HH:MM:SS</code>
											</div>
										</div>
										<div class="control-group">
											<label for="ignoreSampleCount" class="control-label"><@spring.message "perfTest.configuration.ignoreSampleCount"/> </label>
											<div class="controls">
												<span>0</span> 
											</div>
										</div>
										<hr>
										<div class="control-group">
											<label for="sampleInterval" class="control-label"><@spring.message "perfTest.configuration.sampleInterval"/> </label>
											<div class="controls" style="margin-top: 0">
												<input type="text" class="input span2" id="sampleInterval" name="sampleInterval"
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
												<input type="text" class="input span2" id="collectSample" name="collectSample" value="${(test.collectSample)!1000}">
												<code>MS</code>
											</div>
										</div>
										-->
									</fieldset>
								</div>
							</div>
							<div class="span7">
								<div class="page-header">
									<h4><@spring.message "perfTest.testRunning.tpsStatistics"/> <span class="badge badge-success pull-center"><@spring.message "perfTest.testRunning.runTime"/> <data id="running_time"></data></span></h4>
								   
								</div>
								<div id="runningTps" class="chart" style="width: 530px; height: 195px"></div>
								<div class="tabbable">
									<ul class="nav nav-pills" style="" id="tableTab">
										<li><a href="#lsTab" tid="ls"><@spring.message "perfTest.testRunning.latestsample"/></a></li>
										<li><a href="#asTab" tid="as"><@spring.message "perfTest.testRunning.accumulatedstatistic"/></a></li>
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
														<th class="noClick"><@spring.message "perfTest.testRunning.testID"/></th>
														<th class="noClick ellipsis"><@spring.message "perfTest.table.testName"/></th>
														<th class="noClick"><@spring.message "perfTest.testRunning.successfulTest"/></th>
														<th class="noClick"><@spring.message "perfTest.table.errors"/></th>
														<th class="noClick"><@spring.message "perfTest.table.meantime"/></th>
														<th class="noClick"><@spring.message "perfTest.table.tps"/></th>
														<th class="noClick"><@spring.message "perfTest.detail.peakTPS"/></th>
														<th class="noClick"><@spring.message "perfTest.testRunning.mtsd"/></th>
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
														<th class="noClick"><@spring.message "perfTest.testRunning.testID"/></th>
														<th class="noClick ellipsis"><@spring.message "perfTest.table.testName"/></th>
														<th class="noClick"><@spring.message "perfTest.testRunning.successfulTest"/></th>
														<th class="noClick"><@spring.message "perfTest.table.errors"/></th>
														<th class="noClick"><@spring.message "perfTest.table.meantime"/></th>
														<th class="noClick"><@spring.message "perfTest.table.tps"/></th>
														<th class="noClick"><@spring.message "perfTest.detail.peakTPS"/></th>
														<th class="noClick"><@spring.message "perfTest.testRunning.mtsd"/></th>
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
			<input type="hidden" id="scheduleInput" name="scheduledTime" /> 
			<#if test??> 
				<input type="hidden" id="testStatus" name="status" value="${(test.status)}"> 
			<#else> 
				<input type="hidden" id="testStatus" name="status" value="SAVED">
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
				Add Host <small>Please input one option at least.</small>
			</h3>
		</div>
		<div class="modal-body">
			<div class="form-horizontal">
				<fieldset>
					<div class="control-group">
						<label for="domainInput" class="control-label">Domain</label>
						<div class="controls">
							<input type="text" id="domainInput"> <span class="help-inline"></span>
						</div>
					</div>
					<div class="control-group">
						<label for="ipInput" class="control-label">IP</label>
						<div class="controls">
							<input type="text" id="ipInput"> <span class="help-inline"></span>
						</div>
					</div>
				</fieldset>
			</div>
		</div>
		<div class="modal-footer">
			<a class="btn btn-primary" id="addHostBtn"><@spring.message "perfTest.configuration.add"/></a>
		</div>
	</div>

	<div class="modal fade" id="scheduleModal">
		<div class="modal-header">
			<a class="close" data-dismiss="modal">&times;</a>
			<h3>
				<@spring.message "perfTest.testRunning.scheduleTitle"/> <small class="errorColor"></small>
			</h3>
		</div>
		<div class="modal-body">
			<div class="form-horizontal">
				<fieldset>
					<div class="control-group">
						<label class="control-label"><@spring.message "perfTest.testRunning.schedule"/></label>
						<div class="controls form-inline">
							<input type="text" class="input span2" id="sDateInput" value="" readyonly>&nbsp; 
							<select id="shSelect" class="select-item"></select> : <select id="smSelect" class="select-item"></select>
							<code>HH:MM</code>
						</div>
					</div>
				</fieldset>
			</div>
		</div>
		<div class="modal-footer">
			<a class="btn btn-primary" id="runNowBtn"><@spring.message "perfTest.testRunning.runNow"/></a> <a class="btn btn-primary" id="addScheduleBtn"><@spring.message "perfTest.testRunning.schedule"/></a>
		</div>
	</div>

	<script src="${req.getContextPath()}/plugins/datepicker/js/bootstrap-datepicker.js"></script>
	<script src="${req.getContextPath()}/js/rampup.js"></script>
	<script src="${req.getContextPath()}/js/bootstrap-slider.min.js"></script>
 	<script src="${req.getContextPath()}/js/queue.js"></script>
	<script>
	  var jqplotObj;
	  var objTimer;
	  var sliderMax = 40;
	  var durationMap = [];
	  durationMap[0] = 0;
	  for (var i = 1; i <= sliderMax; i++) {
	      if (i <= 10) {
	          durationMap[i] = durationMap[i - 1] + 1;
	      } else if (i <= 20) {
	          durationMap[i] = durationMap[i - 1] + 5;
	      } else if (i <= 32) { //untill 180 min
	          durationMap[i] = durationMap[i - 1] + 10;
	      } else if (i <= 38) { //360 min
	          durationMap[i] = durationMap[i - 1] + 30;
	      } else if (i <= 56) { //24 hours
	          durationMap[i] = durationMap[i - 1] + 60;
	      } else if (i <= 72) {
	          durationMap[i] = durationMap[i - 1] + 60 * 6;
	      } else if (i <= 78) {
	          durationMap[i] = durationMap[i - 1] + 60 * 12;
	      } else {
	          durationMap[i] = durationMap[i - 1] + 60 * 24;
	      }
	  }
	  var test_tps_data = new Queue();
	  $(document).ready(function () {
	      var date = new Date();
	      var year = date.getFullYear();
	      var month = date.getMonth() + 1;
	      var day = date.getDate();
	      $("#sDateInput").val(year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day));
	      
		<#if test??>
			<#if test.status =="TESTING">
				displayCfgAndTestRunning();
			<#elseif test.status =="FINISHED">
				displayCfgAndTestReport();
			<#else>
				displayCfgOnly();
			</#if>
					  $("#leaveCommentButton").click(function(){
		    var comment = $("#testComment").val();
		  	$.post(
		  		"${req.getContextPath()}/perftest/leaveComment",
		  		{ 
		  			"testId": ${test.id},   
		  			"testComment": comment 
		  		},
		  		function() {
		  			showSuccessMsg("Comment is successfully reflected");
		  		}
		     );
		  });
			
		<#else>
			displayCfgOnly();
		</#if>
	      $("#tableTab a:first").tab('show');

	      $('#testContentForm input').hover(function () {
	          $(this).popover('show')
	      });

	      for (var i = 0; i <= sliderMax; i++) {
	          if (durationMap[i] * 60000 == $("#duration").val()) {
	              $("#hiddenDurationInput").val(i);
	          }
	      }

	      $("#scriptName").change(function () {
	          updateScriptResources();
	      });

	      $("#hiddenDurationInput").bind("slide", function (e) {
	          $("#duration").val(durationMap[this.value] * 60000);
	          initDuration();
	          $("#duration").valid();
	      });

	      $("#testContentForm").validate({
		      	 rules:{
		            testName:"required",
		            agentCount:"required",
		            vuserPerAgent:"required"
		        },
		        messages:{
		            testName:"<@spring.message "perfTest.warning.testName"/>",
		            agentCount:"<@spring.message "perfTest.warning.agentNumber"/>",
		            vuserPerAgent:"<@spring.message "perfTest.warning.vuserPerAgent"/>"
		        },
	          ignore: "", //make the validation on hidden input work
	          errorClass: "help-inline",
	          errorElement: "span",
	          errorPlacement: function (error, element) {
	              if (element.next().attr("class") == "add-on") {
	                  error.insertAfter(element.next());
	              } else {
	                  error.insertAfter(element);
	              }
	          },
	          highlight: function (element, errorClass, validClass) {
	              $(element).parents('.control-group').addClass('error');
	              $(element).parents('.control-group').removeClass('success');
	          },
	          unhighlight: function (element, errorClass, validClass) {
	              $(element).parents('.control-group').removeClass('error');
	              $(element).parents('.control-group').addClass('success');
	          }
	      });

	      $("#addHostBtn").click(function () {
	          var content = [];
	          if (!checkEmptyByID("domainInput")) {
	              content.push(getValueByID("domainInput"));
	          }
	          if (!checkEmptyByID("ipInput")) {
	              content.push(getValueByID("ipInput"));
	          }

	          if (content.length == 0) {
	              $("#addHostModal small").addClass("errorColor");
	              return;
	          }

	          var contentStr = content.join(":");
	          $(".div-host").html(hostItem(contentStr));
	          $("#hostsHidden").val(contentStr);
	          $("#addHostModal").modal("hide");
	          $("#addHostModal small").removeClass("errorColor");
	      });

	      function hostItem(content) {
	          return "<p class='host'>" + content + "  <a href='javascript:void(0);'><i class='icon-remove-circle'></i></a></p><br>"
	      }

	      function initHosts() {
	          if (checkEmptyByID("hostsHidden")) {
	              return;
	          }

	          $(".div-host").html(hostItem($("#hostsHidden").val()));
	      }

	      $("i.icon-remove-circle").live('click', function () {
	          var $elem = $(this).parents("p");
	          $elem.next("br").remove();
	          $elem.remove();
	          $("#hostsHidden").val("");
	      });
	      
	      $("#saveScheduleBtn").click(function () {
	          if (!$("#testContentForm").valid()) {
	              return false;
	          }
	      });
	      
	      $("#saveTestBtn").click(function () {
	          if (!$("#testContentForm").valid()) {
	              return false;
	          }
	          if ($("#testStatus").val() != "SAVED") {
	        	  $("#testId").val("");  
	          }
	          $("#testStatus").val("SAVED");
	          $("#scheduleInput").attr('name', '');
	          return true;
	      });

	      $("#runNowBtn").click(function () {
	          $("#scheduleModal").modal("hide");
	          $("#scheduleModal small").html("");
	          $("#scheduleInput").attr('name', '');
	          if ($("#testStatus").val() != "SAVED") {
	        	  $("#testId").val("");  
	          }
	          $("#testStatus").val("READY");
	          document.testContentForm.submit();
	      });

	      $("#addScheduleBtn").click(function () {
	          if (checkEmptyByID("sDateInput")) {
	              $("#scheduleModal small").html("<@spring.message "perfTest.detail.message.setScheduleDate"/>");
	              return;
	          }

	          var timeStr = $("#sDateInput").val() + " " + $("#shSelect").val() + ":" + $("#smSelect").val() + ":0";
	          var scheduledTime = new Date(timeStr.replace(/-/g, "/"));
	          if (new Date() > scheduledTime) {
	              $("#scheduleModal small").html("<@spring.message "perfTest.detail.message.errScheduleDate"/>");
	              return;
	          }
	          $("#scheduleInput").val(scheduledTime);
	          $("#scheduleModal").modal("hide");
	          $("#scheduleModal small").html("");
	          $("#testStatus").val("READY");
	          document.testContentForm.submit();
	      });


          
	      $('#sDateInput').datepicker({
	          format: 'yyyy-mm-dd'
	      });

	      $("#hSelect").append(getOption(7 + 1));
	      $("#hSelect").change(getDurationMS);

	      $("#mSelect").append(getOption(60));
	      $("#mSelect").change(getDurationMS);

	      $("#sSelect").append(getOption(60));
	      $("#sSelect").change(getDurationMS);

	      $("#shSelect").append(getOption(24));
	      $("#smSelect").append(getOption(60));

	      //add toggle event to threshold
	      $("#runcountChkbox").change(function () {
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
	      $("#durationChkbox").change(function () {
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

	      $("#agentCount").change(function () {
	          updateVuserTotal();
	      });

	      $("#vuserPerAgent").change(function () {
	          if ($(this).valid()) {
	          	updateVuserPolicy();
	          }
	      });

	      $("#reportLnk").click(function () {
	          generateReportChart();
	      });

	      $("#reportDetail").click(function () {
	          window.open("${req.getContextPath()}/perftest/report?testId=" + $("#testId").val());
	      });

	      $('#tableTab a').click(function (e) {
	          var $this = $(this);
	          if ($this.hasClass("pull-right")) {} else {
	              e.preventDefault();
	              $this.attr("tid");
	              $this.tab('show');
	          }
	      });

	      $("#homeTab a").click(function () {
	          resetFooter();
	      });
			
			
		  $("#showScript").click(function() {
		  	  var currentScript = $("#scriptName").val();
		      if (currentScript != "") {
		      	var scriptRevision = $("#scriptRevision").val();
		      	window.open ("${req.getContextPath()}/script/detail/" + currentScript + "?r=" + scriptRevision, "scriptSource");
		      }
		  });
		  	
	      initThresholdChkBox();
	      initHosts();
	      initDuration();
	      updateChart();
	      resetFooter();

	      updateScriptResources();
	      validateHostForm();
	      $("#durationSlider").mousedown(function() {
	    	  $("#durationChkbox").click();
	      });
	      $("#runCount").focus(function() {
	    	  $("#runcountChkbox").click();
	      });
	      
	  });

	  function updateVuserTotal() {
	      var agtCount = $("#agentCount").val();
	      var vcount = $("#vuserPerAgent").val();
	      $("#vuserTotal").text("Vuser:" + agtCount * vcount);
	  }

	  function updateScriptResources() {
	      $('#messageDiv').ajaxSend(function (e, xhr, settings) {
	          var url = settings.url;
	          if (url.indexOf("refresh") == 0) 
	          	showInformation("<@spring.message "perfTest.detail.message.updateResource"/>");
	      });
	      $.ajax({
	          url: "${req.getContextPath()}/perftest/getResourcesOnScriptFolder",
	          dataType: 'json',
	          data: {
	              'scriptPath': $("#scriptName").val(),
	              'r':$("#scriptRevision").val()
	          },
	          success: function (res) {
	              var html = "";
	              var len = res.length;
	              for (var i = 0; i < len; i++) {
	                  var value = res[i];
	                  html = html + "<div class='resource'>" + value + "</div><br/>";
	              }
	              $("#scriptResources").html(html);
	          },
	          error: function () {
	              showErrorMsg("<@spring.message "common.error.error"/>");
	              return false;
	          }
	      });
	  }

	  function updateVuserPolicy() {
	      updateVuserTotal();
	      showInformation("<@spring.message "perfTest.detail.message.calculatePolicy"/>");

	      $.ajax({
	          url: "${req.getContextPath()}/perftest/updateVuser",
	          dataType: 'json',
	          data: {
	              'newVuser': $("#vuserPerAgent").val()
	          },
	          success: function (res) {
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
	                  showErrorMsg("<@spring.message "perfTest.detail.error.updateVuser"/>" + res.message);
	                  return false;
	              }
	          },
	          error: function () {
	              showErrorMsg("Error!");
	              return false;
	          }
	      });
	  }

	  function initThresholdChkBox() {
	      if ($("#threshold").val() == "R") { //runcount
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
	      var durationH = parseInt((durationInSec % (60 * 60 * 24)) / 3600);
	      var durationM = parseInt((durationInSec % 3600) / 60);
	      var durationS = durationInSec % 60;

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

	      for (i = 0; i < cnt; i++) {
	          contents.push("<option value='" + i + "'>" + i + "</option>");
	      }

	      return contents.join("\n");
	  }

	  function generateReportChart() {
	      showInformation("<@spring.message "common.message.genTpsChart"/>");
	      getReportDataTPS();
	  }

	  function getReportDataTPS() {
	      $.ajax({
	          url: "${req.getContextPath()}/perftest/getReportData",
	          dataType: 'json',
	          data: {
	              'testId': $("#testId").val(),
	              'dataType': 'TPS',
	              'imgWidth': $("#tpsDiv").width()
	          },
	          success: function (res) {
	              if (res.success) {
	                  drawChart('TPS', 'tpsDiv', res.TPS);
	                  return true;
	              } else {
	                  showErrorMsg("Get report data failed.");
	                  return false;
	              }
	          },
	          error: function () {
	              showErrorMsg("Error!");
	              return false;
	          }
	      });
	  }

	  function refreshData() {
	      var refreshDiv = $("<div></div>");
	      var url = "${req.getContextPath()}/perftest/running/refresh?testId=" + $("#testId").val();
	      refreshDiv.load(url, function () {
	          var succesVal = refreshDiv.find("#input_status").val();

	          if (succesVal == 'SUCCESS') {
	              $("#lsTable tbody").empty();
	              $("#asTable tbody").empty();
	              $("#lsTable tbody").prepend(refreshDiv.find("#lsTableItem"));
	              $("#asTable tbody").prepend(refreshDiv.find("#asTableItem"));

	              $("#process_data").text(refreshDiv.find("#input_process").val());
	              $("#thread_data").text(refreshDiv.find("#input_thread").val());
	              
	              $("#running_time").text(showRunTime(refreshDiv.find("#test_time").val()));

	              test_tps_data.enQueue(refreshDiv.find("#tpsChartData").val());
	          } else {
	             if($('#runningContent_tab:hidden')[0]){
	             	window.clearInterval(objTimer);
	             	return;
	             }else{
	             	test_tps_data.enQueue(0);
	             }
	          }
	          
	      	  if (test_tps_data.getSize() > 60) {
	              test_tps_data.deQueue();
	          }
	          
	          showChart('runningTps', test_tps_data.aElement);
	      });
	  }
	  
	  function showRunTime(s) {
			if (s < 60) {
				return "" + s + "s";
			}
			if (s < 3600) {
				return "" + parseInt(s/60) + "m " + (s%60) + "s";
			}
			if (s < 86400) {
				return "" + parseInt(s/3600) + "h " + parseInt(s%3600/60) + "m " + (s%3600%60) + "s";
			}
			
			return "" + parseInt(s/86400) + "d "  + parseInt(s%86400/3600) + "h " + parseInt(s%86400%3600/60) + "m " + (s%86400%3600%60) + "s";
	   }

		function showChart(containerId, data) {
			if (jqplotObj) {
				replotChart(jqplotObj, data);
			} else {
				jqplotObj = drawChart('TPS', containerId, data);
			}
		}

	  function validateHostForm() {
	      $("#ipInput").blur(function () {
	          var $this = $(this);
	          if (!checkEmptyByObj($this)) {
	              markInput($this, isIPByObj($this), "IP is invalid.");
	          }
	      });

	      $("#domainInput").blur(function () {
	          if (!checkEmptyByID("domainInput")) {
	              var $this = $(this);
	              var rule = "^([a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,6}$";
	              var str = $this.val();
	              markInput($this, checkStringFormat(str, rule), "Domain is invalid.");
	          }
	      });
	  }

	  function updateStatus(id, status, icon, deletable, stoppable, message) {
		  if(status == "FINISHED") {
			  isFinished = true;
		  }
		  if ($("#testStatus").val() == status) {
		  	return;
		  }
	      var ballImg = $("#testStatus_img_id");
	      
		  $("#teststatus_pop_over").attr("data-original-title", status);
		  $("#teststatus_pop_over").attr("data-content", message);
			
	      $("#testStatus").val(status);
	      if (ballImg.attr("src") != "${req.getContextPath()}/img/ball/" + icon) {
	          ballImg.attr("src", "${req.getContextPath()}/img/ball/" + icon);
          }
	     
		  if(status == "TESTING") {
		   		displayCfgAndTestRunning();
		  } else if(status =="FINISHED") { 
		   		displayCfgAndTestReport();
		  } else {
		      	displayCfgOnly();
		  }
	    
	  }
	
	  var isFinished = false;
	  // Wrap this function in a closure so we don't pollute the namespace
	  (function refreshContent() {
	      var ids = [];
	      var testId = $('#testId').val();
	      if (testId == "" || isFinished) {
	          return;
	      }

	      $.ajax({
	          url: '${req.getContextPath()}/perftest/updateStatus',
	          type: 'GET',
	          data: {
	              "ids": testId
	          },
	          success: function (data) {
	              data = eval(data);
	              for (var i = 0; i < data.length; i++) {
	                  updateStatus(data[i].id, data[i].name, data[i].icon, data[i].deletable, data[i].stoppable, data[i].message);
	              }
	          },
	          complete: function () {
	              setTimeout(refreshContent, 5000);
	          }
	      });
	  })();
	  
	  function displayCfgOnly() {
		$("#testContent_tab a").tab('show');
		$("#runningContent_tab").hide();
		$("#reportContent_tab").hide();
	  }
	  
	  function displayCfgAndTestRunning() {
		$("#runningContent_tab a").tab('show');
		$("#reportContent_tab").hide();
		$("#runningContent_tab").show();
		
		objTimer = window.setInterval("refreshData()", 1000);
	  }
	  
	  function displayCfgAndTestReport() {
		$("#testContent_tab a").tab('show');
		$("#runningContent_tab").hide();
		$("#reportContent_tab").show();
		
		if (objTimer) {
	  		window.clearInterval(objTimer);
	  	}
	  }
	</script>
	</body>
</html>
