<#import "../common/spring.ftl" as spring>
<#include "../common/ngrinder_macros.ftl">
<div class="row running">
	<div class="span5 intro" data-step="4" data-intro="<@spring.message 'intro.running.summary'/>">
		<fieldSet>
			<legend><@spring.message "perfTest.running.summaryTitle"/></legend>
		</fieldSet>
		<div class="form-horizontal form-horizontal-3" style="margin-top:10px;">
			<fieldset>
			<@control_group label_message_key="perfTest.running.totalVusers">
				<strong>${test.vuserPerAgent * test.agentCount}</strong>
				<span class="badge badge-info pull-right">
					<@spring.message "perfTest.running.running"/> <span id="running_thread"></span>
				</span>
			</@control_group>
			<@control_group label_message_key="perfTest.running.totalProcesses">
				${test.processes * test.agentCount}
				<span class="badge badge-info pull-right">
					<@spring.message "perfTest.running.running"/> <span id="running_process"></span>
				</span>
			</@control_group>
				<hr>
			<@control_group label_message_key="perfTest.config.targetHost">
				<@list list_items = test.targetHosts?split(",") ; host >
				${host?trim}<br>
				</@list>
			</@control_group>
				<hr>
				<div class="control-group">
				<#if test.threshold == "D">
					<@control_group label_message_key="perfTest.running.duration">
						<span>${test.durationStr}</span>
						<code>HH:MM:SS</code>
					  	<span class="badge badge-success pull-right">
							<span id="running_count"></span> <@spring.message "perfTest.running.runCount"/>
						</span>
					</@control_group>
				<#else>
					<@control_group label_message_key="perfTest.running.totalRunCount">
						${test.runCount * test.agentCount * test.vuserPerAgent}
						<span class="badge badge-success pull-right">
							<@spring.message "perfTest.running.runCount"/> <span id="running_count"></span>
						</span>
					</@control_group>
				</#if>
				</div>
				<div class="control-group">
					<label class="control-label"><@spring.message "perfTest.running.targetState"/></label>
				</div>
				<div class="control-group">
					<div id="monitor_state" style="font-size:12px;margin-left:-20px">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label"><@spring.message "perfTest.running.agentState"/></label>
				</div>
				<div class="control-group">
					<div id="agent_state" style="font-size:12px;margin-left:-20px">
					</div>
				</div>

			</fieldset>
		</div>
	</div>
	<!-- end running content left -->

	<div class="span7">
		<fieldSet>
			<legend>
			<@spring.message "perfTest.running.tpsGraph"/>
				<span id="running_time" class="badge badge-success">&nbsp;</span>
				<a id="stop_test_btn" class="btn btn-danger pull-right intro" data-step="5" data-intro="<@spring.message 'intro.running.stopButton'/>">
				<@spring.message "common.button.stop"/>
				</a>
			</legend>
		</fieldSet>
		<div id="running_tps_chart" class="chart" style="width: 530px; height: 300px"></div>
		<div class="tabbable intro" data-step="6" data-intro="<@spring.message 'intro.running.accumulated'/>">
			<ul class="nav nav-tabs" style="" id="sample_tab">
				<li class="active">
					<a href="#last_sample_tab"><@spring.message "perfTest.running.latestSample"/></a>
				</li>
				<li>
					<a href="#accumulated_sample_tab"><@spring.message "perfTest.running.accumulatedStatistic"/></a>
				</li>
			</ul>
			<div class="tab-content">
				<div class="tab-pane active" id="last_sample_tab">
					<table class="table table-striped table-bordered ellipsis" id="last_sample_table">
						<colgroup>
							<col width="30px">
							<col width="85px">
							<col width="85px">
							<col width="55px">
							<col width="60px">
							<col width="65px">
							<col width="65px">
							<col width="60px">
						</colgroup>
						<thead>
						<tr>
							<th class="no-click"><@spring.message "perfTest.running.testID"/></th>
							<th class="no-click"><@spring.message "perfTest.running.testName"/></th>
							<th class="no-click"><@spring.message "perfTest.running.success"/></th>
							<th class="no-click"><@spring.message "perfTest.running.errors"/></th>
							<th class="no-click" title="<@spring.message 'perfTest.running.meantime'/>">MTT</th>
							<th class="no-click"><@spring.message "perfTest.running.tps"/></th>
							<th class="no-click" title="<@spring.message 'perfTest.running.meanTimeToFirstByte'/>">MTTFB</th>
							<th class="no-click" title="<@spring.message 'perfTest.running.responseBytePerSecond.full'/>">
								<@spring.message "perfTest.running.responseBytePerSecond"/>
							</th>
						</tr>
						</thead>
						<tbody id="last_sample_result">
						</tbody>
					</table>
				</div>
				<div class="tab-pane" id="accumulated_sample_tab">
					<table class="table table-striped table-bordered ellipsis" id="accumulated_sample_table">
						<colgroup>
							<col width="30px">
							<col width="85px">
							<col width="85px">
							<col width="55px">
							<col width="60px">
							<col width="65px">
							<col width="65px">
							<col width="60px">
						</colgroup>
						<thead>
						<tr>
							<th class="no-click"><@spring.message "perfTest.running.testID"/></th>
							<th class="no-click"><@spring.message "perfTest.running.testName"/></th>
							<th class="no-click"><@spring.message "perfTest.running.success"/></th>
							<th class="no-click"><@spring.message "perfTest.running.errors"/></th>
							<th class="no-click" title="<@spring.message 'perfTest.running.meantime'/>">MTT</th>
							<th class="no-click"><@spring.message "perfTest.running.tps"/></th>
							<th class="no-click"
								title="<@spring.message 'perfTest.running.peakTPS.full'/>">
									<@spring.message "perfTest.running.peakTPS"/>
							</th>
							<th class="no-click"
								title="<@spring.message 'perfTest.running.responseBytePerSecond.full'/>">
									<@spring.message "perfTest.running.responseBytePerSecond"/>
							</th>
						</tr>
						</thead>
						<tbody id="accumulated_sample_result">
						</tbody>
					</table>
				</div>
			</div>
		</div>
	</div>
	<!-- end running content right -->
</div>

<script src="${req.getContextPath()}/js/queue.js?${nGrinderVersion}"></script>
<script>

	//@ sourceURL=/perftest/running
	var curPerf;
	var curAgentStat;
	var curMonitorStat;
	var tpsQueue = new Queue(60 / ${test.samplingInterval?c});
	var tpsChart = new Chart('running_tps_chart', [tpsQueue.getArray()], ${test.samplingInterval?c});

	var samplingAjax = new AjaxObj("/perftest/{testId}/api/sample");
	samplingAjax.params = { testId: ${(test.id!0)?c} };

	function showLastPerTestResult(container, statistics) {
		var existing = container.find("tr");
		for (var i = 0; i < statistics.length; i++) {
			var record = existing.get(i);
			if (record == undefined) {
				container.append("<tr></tr>");
				record = container.find("tr").get(i);
			}
			var output = "<td>" + toNum(statistics[i].testNumber) + "</td>";
			var testDescription = statistics[i].testDescription;
			output = output + "<td class='ellipsis' title='" + testDescription + "'>" + testDescription + "</td>";
			output = output + "<td>" + toNum(statistics[i].Tests) + "</td>";
			output = output + "<td>" + toNum(statistics[i].Errors) + "</td>";
			output = output + "<td>" + toNum(statistics[i]["Mean_Test_Time_(ms)"]) + "</td>";
			output = output + "<td>" + toNum(statistics[i].TPS) + "</td>";
			output = output + "<td>" + toNum(statistics[i].Mean_time_to_first_byte, 0) + "</td>";
			output = output + "<td>" + formatNetwork(null, statistics[i].Response_bytes_per_second) + "</td>";
			$(record).html(output);
		}
	}


	function showAccumulatedPerTestResult(container, statistics) {
		var existing = container.find("tr");
		for (var i = 0; i < statistics.length; i++) {
			var record = existing.get(i);
			if (record == undefined) {
				container.append("<tr></tr>");
				record = container.find("tr").get(i);
			}
			var output = "<td>" + toNum(statistics[i].testNumber) + "</td>";
			var testDescription = statistics[i].testDescription;
			output = output + "<td class='ellipsis' title='" + testDescription + "'>" + testDescription + "</td>";
			output = output + "<td>" + toNum(statistics[i].Tests) + "</td>";
			output = output + "<td>" + toNum(statistics[i].Errors) + "</td>";
			output = output + "<td>" + toNum(statistics[i]["Mean_Test_Time_(ms)"]) + "</td>";
			output = output + "<td>" + toNum(statistics[i].TPS) + "</td>";
			output = output + "<td>" + toNum(statistics[i].Peak_TPS) + "</td>";
			output = output + "<td>" + formatNetwork(null, statistics[i].Response_bytes_per_second) + "</td>";
			$(record).html(output);
		}
	}

	samplingAjax.success = function (res) {
		if (res.status == "TESTING") {
			/** @namespace res.perf */
			curPerf = res.perf;
			curAgentStat = res.agent;
			curMonitorStat = res.monitor;
			if (curAgentStat !== undefined) {
				$agentState.html(createMonitoringStatusString(curAgentStat));
			}
			if (curMonitorStat !== undefined) {
				$monitorState.html(createMonitoringStatusString(curMonitorStat));
			}
			if (curPerf !== undefined) {
				$runningTime.text(showRunTime(curPerf.testTime));
				$runningProcess.text($.number(curPerf.process));
				$runningThread.text($.number(curPerf.thread));
				$runningCount.text($.number(curPerf.totalStatistics.Tests + curPerf.totalStatistics.Errors));
				showLastPerTestResult($lastSampleResult, curPerf.lastSampleStatistics);
				showAccumulatedPerTestResult($accumulatedSampleResult, curPerf.cumulativeStatistics);
				tpsQueue.enQueue(curPerf.tpsChartData);
				tpsChart.plot();
			}
		} else {
			if ($('#running_section_tab:hidden')[0]) {
				window.clearInterval(objTimer);
			}
		}
	};

	samplingAjax.error = function () {
		if ($('#running_section_tab:hidden')[0]) {
			window.clearInterval(objTimer);
		}
	};

	var $runningTime = $("#running_time");
	var $runningProcess = $("#running_process");
	var $runningThread = $("#running_thread");
	var $runningCount = $("#running_count");
	var $agentState = $("#agent_state");
	var $monitorState = $("#monitor_state");
	var $accumulatedSampleResult = $("#accumulated_sample_result");
	var $lastSampleResult = $("#last_sample_result");

	function toNum(num, precision) {
		if (num == undefined) {
			return "-";
		}
		precision = precision || 0;
		return $.number(num, precision);
	}

	function createMonitoringStatusString(status) {
		var monitorStatusString = "<ul>";
		$.each(status, function (name, value) {
			monitorStatusString = monitorStatusString +
					"<li class='monitor-state' style='height:20px'><div style='width:100%;' class='ellipsis'>";
			monitorStatusString = monitorStatusString +
					"<span title='" + name + "'><b>" + getShortenString(name) + "</b></span>" +
					" CPU-" + formatPercentage(null, value.cpuUsedPercentage) +
					" MEM-" + formatPercentage(null, ((value.totalMemory - value.freeMemory) / value.totalMemory) * 100);
			if (value.receivedPerSec != 0 || value.sentPerSec != 0) {
				monitorStatusString = monitorStatusString + "/" +
						" RX-" + formatNetwork(null, value.receivedPerSec) +
						" TX-" + formatNetwork(null, value.sentPerSec) +
						"</dv></li>";
			}
		});
		monitorStatusString += "</ul>";
		return monitorStatusString;
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

	function stopTests(ids) {
		var ajaxObj = new AjaxPutObj("/perftest/api?action=stop",
				{ "ids" : ids },
				"<@spring.message "perfTest.message.stop.success"/>",
				"<@spring.message "perfTest.message.stop.error"/>");
		ajaxObj.call();

	}

	$("#stop_test_btn").click(function () {
		bootbox.confirm("<@spring.message "perfTest.message.stop.confirm"/>", "<@spring.message "common.button.cancel"/>", "<@spring.message "common.button.ok"/>", function (result) {
			if (result) {
				stopTests("${(test.id!0)?c}");
			}
		});
	});

	var $samplingTab = $('#sample_tab');
	$samplingTab.find('a').click(function (e) {
		e.preventDefault();
		$(this).tab('show');
	});
	$samplingTab.find('a:first').tab('show');
	samplingAjax.call();
	objTimer = window.setInterval("samplingAjax.call()", 1000 * ${test.samplingInterval?c});
</script>