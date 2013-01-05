<div class="row">
	<div class="span5">
		<div class="page-header">
			<h4><@spring.message "perfTest.testRunning.summary"/></h4>
		</div>
		<div class="form-horizontal form-horizontal-3" style="margin-top: 10px;">
			<fieldset>
				<div class="control-group">
					<label class="control-label"><@spring.message "perfTest.testRunning.scriptName"/></label>
					<div class="controls" title="${(test.scriptName)!}">${(test.scriptNameInShort)!}</div> 
				</div>
				<hr>
				<div class="control-group">
					<label class="control-label"><@spring.message "perfTest.testRunning.vusers"/></label>
					<div class="controls">
						<strong>${(test.vuserPerAgent)!}</strong>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label"><@spring.message "perfTest.testRunning.agents"/></label>
					<div class="controls">
						<span>${(test.agentCount)!}</span>
						<!--<a class="btn btn-mini btn-info" id="agentInfoBtn" href="#agentListModal" data-toggle="modal">Info</a>-->
					</div>
				</div>
				<div class="control-group">
					<label class="control-label"><@spring.message "perfTest.testRunning.processes"/></label>
					<div class="controls">
						${(test.processes)!} 
						<span class="badge badge-info pull-right"><@spring.message "perfTest.testRunning.running"/> <span id="process_data"></span></span>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label"><@spring.message "perfTest.testRunning.threads"/></label>
					<div class="controls">
						${(test.threads)!} <span class="badge badge-info pull-right"><@spring.message "perfTest.testRunning.running"/> <span id="thread_data"></span></span>
					</div>
				</div>
				<hr>
				<div class="control-group">
					<label class="control-label"><@spring.message "perfTest.configuration.targetHost"/></label>
					<div class="controls">
						<#if test?exists && test.targetHosts?has_content>
							<#list test.targetHosts?split(",") as host>
								${host?trim}<br>
							</#list>
						</#if>
					</div>
				</div>
				<hr>
				<div class="control-group">
					<#if test??>
						<#if test.threshold == "D">
							<label class="control-label"> <@spring.message "perfTest.configuration.duration"/> </label>
							<div class="controls">
								<span>${(test.durationStr)!}</span>
								<code>HH:MM:SS</code>
							</div>
						<#else>
							<label class="control-label"> <@spring.message "perfTest.configuration.runCount"/> </label>
							<div class="controls">
								${(test.runCount)!}
								<span class="badge badge-success pull-right"> <span id="running_count"></span>  <@spring.message "perfTest.table.runcount"/></span>
							</div>
						</#if>
					</#if>
				</div>
				<div class="control-group">
					<label class="control-label"><@spring.message "perfTest.configuration.ignoreSampleCount"/> </label>
					<div class="controls">
						<span>${(test.ignoreSampleCount)!0}</span> 
					</div>
				</div>
				<div class="control-group">
					<label class="control-label">Agent Status</label>
				</div>
				<div class="control-group">
					<div id="agent_status">
					</div>
				</div>

			</fieldset>
		</div>
	</div>
	<!-- end ruuning content left -->
	
	<div class="span7">
		<div class="page-header">
			<h4><@spring.message "perfTest.testRunning.tpsStatistics"/> <span class="badge badge-success"><@spring.message "perfTest.testRunning.runTime"/> <span id="running_time"></span></span></h4>
			<a class="btn  btn-danger pull-right" id="stopTestButton" style="margin-top:-25px;margin-right:10px" sid="${(test.id)!}"><@spring.message "common.button.stop"/></a>			
		</div> 
		<div id="runningTps" class="chart" style="width: 530px; height: 300px"></div>
		<div class="tabbable">
			<ul class="nav nav-pills" style="" id="tableTab">
				<li><a href="#lsTab" tid="ls"><@spring.message "perfTest.testRunning.latestsample"/></a></li>
				<li><a href="#asTab" tid="as"><@spring.message "perfTest.testRunning.accumulatedstatistic"/></a></li>
			</ul>
			<div class="tab-content">
				<div class="tab-pane" id="lsTab">
					<table class="table table-striped table-bordered ellipsis" id="lsTable">
						<colgroup>
							<col width="30px">
							<col width="85px">
							<col width="85px">
							<col width="55px">
							<col width="60px">
							<col width="65px">
							<col width="65px">
							<col width="55px">
						</colgroup>
						<thead>
							<tr>
								<th class="noClick"><@spring.message "perfTest.testRunning.testID"/></th>
								<th class="noClick"><@spring.message "perfTest.table.testName"/></th>
								<th class="noClick"><@spring.message "perfTest.testRunning.successfulTest"/></th>
								<th class="noClick"><@spring.message "perfTest.table.errors"/></th>
								<th class="noClick" title="<@spring.message "perfTest.table.meantime"/>">MTT</th>
								<th class="noClick"><@spring.message "perfTest.table.tps"/></th>
								<th class="noClick"><@spring.message "perfTest.detail.peakTPS"/></th>
								<th class="noClick"><@spring.message "perfTest.testRunning.mtsd"/></th>
							</tr>
						</thead>
						<tbody>
						</tbody>
					</table>
				</div>
				<div class="tab-pane" id="asTab">
					<table class="table table-striped table-bordered ellipsis" id="asTable"> 
						<colgroup>
							<col width="30px">
							<col width="85px">
							<col width="85px">
							<col width="55px">
							<col width="60px">
							<col width="65px">
							<col width="65px">
							<col width="55px">
						</colgroup>
						<thead>
							<tr>
								<th class="noClick"><@spring.message "perfTest.testRunning.testID"/></th>
								<th class="noClick"><@spring.message "perfTest.table.testName"/></th>
								<th class="noClick"><@spring.message "perfTest.testRunning.successfulTest"/></th>
								<th class="noClick"><@spring.message "perfTest.table.errors"/></th>
								<th class="noClick" title="<@spring.message "perfTest.table.meantime"/>">MTT</th>
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
	<!-- end running content right -->
</div>
<script>
	var curPeakTps = 0;
	var curTps = 0;
	var curRunningTime = 0;
	var curRunningProcesses = 0;
	var curRunningThreads = 0;
	var curRunningCount = 0;
	var curStatus = false;
	var curAgentPerfStates = [];
	var agentPerfStates = [];
	function refreshData() {
		var refreshDiv = $("<div></div>");
		var peakTps = 50;
		refreshDiv.load(
			"${req.getContextPath()}/perftest/running/refresh",
			{"testId": $("#testId").val()},
			function() {
				$("#running_time").text(showRunTime(curRunningTime));
				if (curStatus == true) {
					$("#lsTable tbody").html(refreshDiv.find("#lsTableItem"));
					$("#asTable tbody").html(refreshDiv.find("#asTableItem"));
		
					$("#process_data").text(curRunningProcesses);
					$("#thread_data").text(curRunningThreads);
					$("#running_count").text(curRunningCount);
					var agentStatusString = "<ul>";
					for ( var i = 0; i < curAgentPerfStates.length; i++) {
						var eachAgent = curAgentPerfStates[i];
						if (agentPerfStates[eachAgent.agent] === undefined) {
							agentPerfStates[eachAgent.agent] = {"cpu" : {}, "mem" : {}};
						}
						agentPerfStates[eachAgent.agent].cpu = eachAgent.cpu;
						agentPerfStates[eachAgent.agent].mem = eachAgent.mem;
						// Use sparkle line...     
						agentStatusString = agentStatusString
								+ "<li>" + curAgentPerfStates[i].agent + " CPU - "
								+ curAgentPerfStates[i].cpu + "%   MEM - "
								+ curAgentPerfStates[i].mem + "%</li>";
					}
					agentStatusString += "</ul>"; 
					$("#agent_status").html(agentStatusString);
					peakTps = curPeakTps;
					test_tps_data.enQueue(curTps);
				} else {
					if ($('#runningContent_tab:hidden')[0]) {
						window.clearInterval(objTimer);
						return;
					} else {
						test_tps_data.enQueue(0);
					}
				}
		
				if (test_tps_data.getSize() > 60) {
					test_tps_data.deQueue();
				}
		
				showChart('runningTps', test_tps_data.aElement, peakTps);
			}
		);
	}
	
	function showRunTime(s) {
		if (s < 60) {
			return "" + s + "s";
		}
		if (s < 3600) {
			return "" + parseInt(s / 60) + "m " + (s % 60) + "s";
		}
		if (s < 86400) {
			return "" + parseInt(s / 3600) + "h " + parseInt(s % 3600 / 60) + "m " + (s % 3600 % 60) + "s";
		}
	
		return "" + parseInt(s / 86400) + "d " + parseInt(s % 86400 / 3600) + "h " + parseInt(s % 86400 % 3600 / 60) + "m " + (s % 86400 % 3600 % 60) + "s";
	}
	
	function showChart(containerId, data, peakTps) {
		if (jqplotObj) {
			replotChart(jqplotObj, data, peakTps);
		} else {
			jqplotObj = drawChart('TPS', containerId, data);
		}
	}
	
	function stopTests(ids) {
		$.ajax({
	  		url: "${req.getContextPath()}/perftest/stopTests",
			type: "POST",
	  		data: {"ids":ids},
			dataType:'json',
	    	success: function(res) {
	    		if (res.success) {
		    		showSuccessMsg("<@spring.message "perfTest.table.message.success.stop"/>");
	    		} else {
		    		showErrorMsg("<@spring.message "perfTest.table.message.error.stop"/>:" + res.message);
	    		}
	    	},
	    	error: function() {
	    		showErrorMsg("<@spring.message "perfTest.table.message.error.stop"/>!");
	    	}
	  	});
	}
	
	$(document).ready(function() {
		$("#stopTestButton").click(function() {
			var id = $(this).attr("sid");
			bootbox.confirm("<@spring.message "perfTest.table.message.confirm.stop"/>", "<@spring.message "common.button.cancel"/>", "<@spring.message "common.button.ok"/>", function(result) {
				if (result) {
					stopTests(id);
				}
			});
		});
	});
</script>