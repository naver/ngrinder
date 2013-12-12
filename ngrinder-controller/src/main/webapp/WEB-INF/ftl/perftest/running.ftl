<#import "../common/spring.ftl" as spring>
<#include "../common/ngrinder_macros.ftl">
<div class="row">
	<div class="span5">
		<fieldSet>
			<legend><@spring.message "perfTest.testRunning.summary"/></legend>
		</fieldSet>
		<div class="form-horizontal form-horizontal-3" style="margin-top:10px;">
			<fieldset>
			<@control_group label_message_key="perfTest.testRunning.vusers">
				<strong>${test.vuserPerAgent}</strong>
			</@control_group>

			<@control_group label_message_key="perfTest.testRunning.agents">
				<span>${test.agentCount}</span>
			</@control_group>
			<@control_group label_message_key="perfTest.testRunning.processes">
			${test.processes}
				<span class="badge badge-info pull-right">
					<@spring.message "perfTest.testRunning.running"/> <span id="process_data"></span>
				</span>
			</@control_group>

			<@control_group label_message_key="perfTest.testRunning.threads">
			${test.threads}
				<span class="badge badge-info pull-right">
					<@spring.message "perfTest.testRunning.running"/> <span id="thread_data"></span>
				</span>
			</@control_group>
				<hr>
			<@control_group label_message_key="perfTest.configuration.targetHost">
				<@list list_items = test.targetHosts?split(",") ; host >
				${host?trim}<br>
				</@list>
			</@control_group>
				<hr>
				<div class="control-group">
				<#if test.threshold == "D">
					<@control_group label_message_key="perfTest.configuration.duration">
						<span>${test.durationStr}</span>
						<code>HH:MM:SS</code>
					</@control_group>
				<#else>
					<@control_group label_message_key="perfTest.configuration.runCount">
					${test.runCount}
						<span class="badge badge-success pull-right"> <span
								id="running_count"></span>  <@spring.message "perfTest.table.runcount"/></span>
					</@control_group>
				</#if>
				</div>
				<div class="control-group">
					<label class="control-label"><@spring.message "perfTest.testRunning.targetState"/></label>
				</div>
				<div class="control-group">
					<div id="monitor_state" style="font-size:12px;margin-left:-20px">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label"><@spring.message "perfTest.testRunning.agentState"/></label>
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
			<@spring.message "perfTest.testRunning.tpsStatistics"/>
				<span class="badge badge-success" style="vertical-align:middle;">
					<span id="running_time"></span>
				</span>
				<a id="stop_test_btn" class="btn btn-danger pull-right" sid="${(test.id)!}">
				<@spring.message "common.button.stop"/>
				</a>
			</legend>
		</fieldSet>
		<div id="running_tps_chart" class="chart" style="width: 530px; height: 300px"></div>
		<div class="tabbable">
			<ul class="nav nav-tabs" style="" id="sample_tab">
				<li class="active">
					<a href="#last_sample_tab" tid="ls"><@spring.message "perfTest.testRunning.latestsample"/></a>
				</li>
				<li><a href="#accumulated_sample_tab"
					   tid="as"><@spring.message "perfTest.testRunning.accumulatedstatistic"/></a></li>
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
							<col width="55px">
						</colgroup>
						<thead>
						<tr>
							<th class="no-click"><@spring.message "perfTest.testRunning.testID"/></th>
							<th class="no-click"><@spring.message "perfTest.table.testName"/></th>
							<th class="no-click"><@spring.message "perfTest.testRunning.successfulTest"/></th>
							<th class="no-click"><@spring.message "perfTest.table.errors"/></th>
							<th class="no-click" title="<@spring.message "perfTest.table.meantime"/>">MTT</th>
							<th class="no-click"><@spring.message "perfTest.table.tps"/></th>
							<th class="no-click"><@spring.message "perfTest.testRunning.responseBytePerSecond"/></th>
							<th class="no-click" title='<@spring.message "perfTest.testRunning.meanTimeToFirstByte"/>'>
								MTFB
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
							<col width="55px">
						</colgroup>
						<thead>
						<tr>
							<th class="no-click"><@spring.message "perfTest.testRunning.testID"/></th>
							<th class="no-click"><@spring.message "perfTest.table.testName"/></th>
							<th class="no-click"><@spring.message "perfTest.testRunning.successfulTest"/></th>
							<th class="no-click"><@spring.message "perfTest.table.errors"/></th>
							<th class="no-click" title="<@spring.message "perfTest.table.meantime"/>">MTT</th>
							<th class="no-click"><@spring.message "perfTest.table.tps"/></th>
							<th class="no-click"><@spring.message "perfTest.detail.peakTPS"/></th>
							<th class="no-click"><@spring.message "perfTest.testRunning.responseBytePerSecond"/></th>
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
	var curPerf;
	var curAgentStat
	var curMonitorStat;
	var tpsQueue = new Queue(60 / ${test.samplingInterval});
	var tpsChart = new Chart('running_tps_chart', [tpsQueue.getArray()], ${test.samplingInterval});

	var samplingAjax = new AjaxObj("/perftest/${test.id}/api/sample");
	var $runningtime = $("#running_time");
	var $processdata = $("#process_data");
	var $threaddata = $("#thread_data");
	var $runningcount = $("#running_count");
	var $agentstate = $("#agent_state");
	var $monitorstate = $("#monitor_state");
	var $accumulatedSampleResult = $("#accumulated_sample_result");
	var $lastSampleResult = $("#last_sample_result");

	function showLastPerTestResult(container, statistics) {
		var existing = container.find("tr");
		for (var i = 0; i < statistics.length; i++) {
			var record = existing.get(i);
			if (record == undefined) {
				container.append("<tr></tr>");
				record = container.find("tr").get(i);
			}
			var output = "<td>" + toNum(statistics[i].testNumber) + "</td>";
			output = output + "<td class='ellipsis'>" + statistics[i].testDescription + "</td>"
			output = output + "<td>" + toNum(statistics[i].Tests) + "</td>";
			output = output + "<td>" + toNum(statistics[i].Errors) + "</td>";
			output = output + "<td>" + toNum(statistics[i]["Mean_Test_Time_(ms)"]) + "</td>";
			output = output + "<td>" + toNum(statistics[i].TPS) + "</td>";
			output = output + "<td>" + toSize(statistics[i].Response_bytes_per_second) + "</td>";
			output = output + "<td>" + toNum(statistics[i].Mean_time_to_first_byte, 0) + "</td>";
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
			output = output + "<td class='ellipsis'>" + statistics[i].testDescription + "</td>"
			output = output + "<td>" + toNum(statistics[i].Tests) + "</td>";
			output = output + "<td>" + toNum(statistics[i].Errors) + "</td>";
			output = output + "<td>" + toNum(statistics[i]["Mean_Test_Time_(ms)"]) + "</td>";
			output = output + "<td>" + toNum(statistics[i].TPS) + "</td>";
			output = output + "<td>" + toNum(statistics[i].Peak_TPS) + "</td>";
			output = output + "<td>" + toSize(statistics[i].Response_bytes_per_second) + "</td>";
			$(record).html(output);
		}
	}

	samplingAjax.success = function (res) {
		if (res.status == "TESTING") {
			curPerf = res.perf;
			curAgentStat = res.agent;
			curMonitorStat = res.monitor;
			$runningtime.text(showRunTime(curPerf.testTime));
			$processdata.text($.number(curPerf.process));
			$threaddata.text($.number(curPerf.thread));
			$runningcount.text($.number(curPerf.totalStatistics.Tests + curPerf.totalStatistics.Errors));
			$agentstate.html(createMonitoringStatusString(curAgentStat));
			$monitorstate.html(createMonitoringStatusString(curMonitorStat));
			showLastPerTestResult($lastSampleResult, curPerf.lastSampleStatistics);
			showAccumulatedPerTestResult($accumulatedSampleResult, curPerf.cumulativeStatistics);
			tpsQueue.enQueue(curPerf.tpsChartData.toFixed(0));
			tpsChart.plot();
		} else {
			if ($('#running_section_tab:hidden')[0]) {
				window.clearInterval(objTimer);
			}
		}
	}

	samplingAjax.error = function () {
		if ($('#running_section_tab:hidden')[0]) {
			window.clearInterval(objTimer);
		}
	}

	function toNum(num, precision) {
		if (num == undefined) {
			return "-";
		}
		precision = precision || 0;
		return $.number(num, precision);
	}

	function toPercent(num) {
		return $.number(num / 100, 1) + "%"
	}

	function toSize(bytes) {
		if (bytes == undefined) {
			return "-";
		}
		bytes = bytes.toFixed(0);
		var sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
		if (bytes == 0) return 'n/a';
		var i = parseInt(Math.floor(Math.log(Math.round(bytes)) / Math.log(1024)));
		if (i == 0) return bytes +  sizes[i];
		return (bytes / Math.pow(1024, i)).toFixed(1) + sizes[i];
	}


	function createMonitoringStatusString(status) {
		var monitorStatusString = "<ul>";
		$.each(status, function (name, value) {
			monitorStatusString = monitorStatusString +
					"<li class='monitor_state'><div style='width:100%;' class='ellipsis'>";
			monitorStatusString = monitorStatusString +
					"<span title='" + name + "'><b>" + name + "</b></span> " +
					"CPU-" + toPercent(value.cpuUsedPercentage) + " MEM-" + toPercent(value.totalMemory / value
					.freeMemory);
			if (value.receivedPerSec != 0 || value.sentPerSec != 0) {
				monitorStatusString = monitorStatusString + "/ RX-" + toSize(value.receivedPerSec) +
						" TX-" + toSize(value.sentPerSec) + "</dv></li>";
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
		var ajaxObj = new AjaxPostObj("${req.getContextPath()}/perftest/api/stop",
				{ "ids": ids },
				"<@spring.message "perfTest.table.message.success.stop"/>",
				"<@spring.message "perfTest.table.message.error.stop"/>");
		ajaxObj.call();
	}

	$("#stop_test_btn").click(function () {
		var id = $(this).attr("sid");
		bootbox.confirm("<@spring.message "perfTest.table.message.confirm.stop"/>", "<@spring.message "common.button.cancel"/>", "<@spring.message "common.button.ok"/>", function (result) {
			if (result) {
				stopTests(id);
			}
		});
	});

	$('#sample_tab a').click(function (e) {
		e.preventDefault();
		$(this).tab('show');
	});
	$('#sample_tab a:first').tab('show');
	samplingAjax.call();
	objTimer = window.setInterval("samplingAjax.call()", 1000 * ${test.samplingInterval});
</script>