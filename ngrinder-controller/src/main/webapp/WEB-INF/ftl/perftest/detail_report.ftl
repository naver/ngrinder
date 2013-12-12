<!DOCTYPE html>
<html>
<head>
<#include "../common/common.ftl">
<#include "../common/jqplot.ftl">
	<title><@spring.message "perfTest.report.title"/></title>
	<style>
		body {
			padding-top: 0;
		}

		.left {
			border-right: 1px solid #878988
		}

		div.chart {
			border: 1px solid #878988;
			height: 200px;
			min-width: 615px;
		}

		div.bigchart {
			border: 1px solid #878988;
			height: 300px;
			min-width: 615px;
		}

		h6 {
			margin-top: 20px;
		}

		td strong {
			color: #6DAFCF
		}

		.jqplot-yaxis {
			margin-right: 10px;
		}

		.jqplot-xaxis {
			margin-right: 5px;
			margin-top: 5px;
		}

		.compactpadding th {
			padding: 8px 5px;
			vertical-align: middle;
		}

		.jqplot-image-button {
			margin-top: 5px;
			margin-bottom: 5px;
		}

		div.jqplot-image-container {
			position: relative;
			z-index: 11;
			margin: auto;
			display: none;
			background-color: #ffffff;
			border: 1px solid #999;
			display: inline-block;
			min-width: 698px;
		}

		div.jqplot-image-container-header {
			font-size: 1.0em;
			font-weight: bold;
			padding: 5px 15px;
			background-color: #eee;
		}

		div.jqplot-image-container-content {
			background-color: #ffffff;
		}

		a.jqplot-image-container-close {
			float: right;
		}
	</style>
</head>

<#setting number_format="number">
<body>
<div class="navbar-inner" style="width:912px; margin-left:auto; margin-right:auto; margin-bottom:0">
	<h3><@spring.message "perfTest.report.reportPage"/> ${test.testName}</h3>
</div>
<div class="container">
	<form name="download_csv_form">
		<input type="hidden" id="test_id" name="testId" value="${test.id}">
	</form>
	<div class="row">
		<div class="span3">
			<table class="table table-bordered compactpadding">
				<colgroup>
					<col width="120px">
					<col>
				</colgroup>
				<tr>
					<th><@spring.message "perfTest.report.totalVuser"/></th>
					<td><strong>${test.vuserPerAgent * test.agentCount}</strong></td>
				</tr>
				<tr>
					<th><@spring.message "perfTest.report.agent"/></th>
					<td><span>${test.agentCount}</span>
				</tr>
				<tr>
					<th><@spring.message "perfTest.report.process"/><br/><@spring.message "perfTest.report.thread"/></th>
					<td>${test.processes} / ${test.threads}</td>
				</tr>
				<tr>
					<td colspan="2" class="divider"></td>
				</tr>
				<tr>
					<th><@spring.message "perfTest.report.ignoreSampleCount"/></th>
					<td><span>${test.ignoreSampleCount}</span></td>
				</tr>
				<tr>
					<td colspan=2></td>
				</tr>
				<tr>
					<th>TPS</th>
					<td><strong><#if test.tps??>${(test.tps)?string(",##0.#")}</#if></strong></td>
				</tr>
				<tr>
					<th><@spring.message "perfTest.report.meantime"/></th>
					<td><span>${(test.meanTestTime!0)?string(",##0.##")}</span>&nbsp;&nbsp; <code>ms</code></td>
				</tr>
				<tr>
					<th><@spring.message "perfTest.report.peakTPS"/></th>
					<td><strong>${test.peakTps!""}</strong></td>
				</tr>
				<tr>
					<th><@spring.message "perfTest.report.totalTests"/></th>
					<td>${(test.tests + test.errors)!""}</td>
				</tr>
				<tr>
					<th><@spring.message "perfTest.report.successfulTests"/></th>
					<td>${(test.tests)!""}</td>
				</tr>
				<tr>
					<th><@spring.message "perfTest.report.errors"/></th>
					<td>${test.errors!""}</td>
				</tr>
			</table>

			<div class="well" style="max-width: 340px; padding: 8px 0;">
				<ul class="nav nav-list">
					<li class="active pointer-cursor perf  nav-header">
						<a class="pointer-cursor"><@spring.message "perfTest.report.performanceReport"/></a>
					</li>
					<li class="nav-header"><@spring.message "perfTest.report.targetHost"/></li>

					<@list list_items=test.targetHostIP others="no_message" ; targetIP >
						<li class="monitor pointer-cursor" ip="${targetIP}">
							<a class="pointer-cursor">${targetIP}</a>
						</li>
					</@list>

				<li  class="nav-header"><@spring.message "perfTest.report.plugins"/></li>
					<@list list_items=plugins others="no_message" ; each >
						<li class="plugin pointer-cursor" plugin="${each.first}" ip="${each.second}">
							<a class="pointer-cursor">${each.first} - ${each.second}</a>
						</li>
					</@list>
				</ul>
			</div>
		</div>
		<div class="span9">
			<table class="table table-bordered" style="margin-bottom:35px">
				<colgroup>
					<col width="120">
					<col width="220">
					<col width="120">
					<col>
				</colgroup>
				<tr>
					<th><@spring.message "perfTest.table.startTime"/></th>
					<td>${(test.startTime?string('yyyy-MM-dd HH:mm:ss'))!"&nbsp"}</td>
					<th><@spring.message "perfTest.table.finishTime"/></th>
					<td>${(test.finishTime?string('yyyy-MM-dd HH:mm:ss'))!"&nbsp"}</td>
				</tr>
				<#if test.threshold == "D">
				<tr>
					<th><@spring.message "perfTest.configuration.duration"/></th>
					<td><span>${test.durationStr}</span> <code>HH:MM:SS</code></td>
				<#else>
					<th><@spring.message "perfTest.configuration.runCount"/></th>
					<td><span>${test.runCount}</td>
				</#if>
					<th><@spring.message "perfTest.configuration.runtime"/></th>
					<td><span>${test.runtimeStr}</span> <code>HH:MM:SS</code></td>
				</tr>
				<tr>
					<th><@spring.message "perfTest.report.testcomment"/></th>
					<td colspan="3">${(test.testComment)!?html?replace('\n', '<br>')}</td>
				</tr>
			</table>
			<div id="detail_panel">
			</div>
		</div>
	</div>
<#include "../common/copyright.ftl">
</div>
<#include "../common/messages.ftl">


<script src="${req.getContextPath()}/js/generate-img.js"></script>
<script>
	var imgBtnLabel = "<@spring.message "perfTest.report.exportImg.button"/>";
	var imgTitle = "<@spring.message "perfTest.report.exportImg.title"/>"

	$(document).ready(function () {
		$("li.perf").click(function () {
			getPerfChart();
			changActiveLink($(this));
		});

		$("li.monitor").click(function () {
			getMonitorChart($(this).attr("ip"));
			changActiveLink($(this));
		});

		$("li.plugin").click(function () {
			getPluginChart($(this).attr("plugin"), $(this).attr("ip"));
			changActiveLink($(this));
		});

		$.ajaxSetup({"cache": false});
		getPerfChart();
	});

	function changActiveLink(obj) {
		$("li.active").removeClass("active");
		obj.addClass("active");
	}

	function getPerfChart() {
		$("#detail_panel").load("${req.getContextPath()}/perftest/${(test.id)?c}/detail_report/perf");
	}

	function getMonitorChart(ip) {
		$("#detail_panel").load("${req.getContextPath()}/perftest/${(test.id)?c}/detail_report/monitor?targetIP=" + ip);
	}

	function getPluginChart(plugin, kind) {
		$("#detail_panel").load("${req.getContextPath()}/perftest/${(test.id)?c}/detail_report/plugin/" + plugin +
				"?kind=" + kind);
	}
</script>
</body>
</html>
