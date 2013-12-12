<!DOCTYPE html>
<html>
<head>
<#include "../common/common.ftl">
<#include "../common/jqplot.ftl">
	<title><@spring.message "agent.info.title"/></title>
	<style>
		.left {
			border-right: 1px solid #878988
		}

		div.chart {
			border: 1px solid #878988;
			height: 250px;
			min-width: 615px;
			margin-bottom: 12px;
			padding: 5px
		}

		.jqplot-yaxis {
			margin-right: 10px;
		}

		.jqplot-xaxis {
			margin-top: 5px;
		}
	</style>
</head>

<body>
<#include "../common/navigator.ftl">
<div class="container">
	<legend class="header">
	<@spring.message "agent.info.head"/>
		<button class="btn pull-right" onClick="window.history.back();">
		<@spring.message "common.button.return"/>
		</button>
	</legend>
	<div class="row">
		<div class="span3">
			<table class="table table-bordered table-striped" style="border-top:#cccccc solid 1px;margin-top:14px">
				<tbody>
				<tr>
					<th><@spring.message "agent.table.IP"/></th>
				</tr>
				<tr>
					<td>${(agent.ip)!}</td>
				</tr>

				<tr>
					<th><@spring.message "agent.table.port"/></th>
				</tr>
				<tr>
					<td>${(agent.port)!}</td>
				</tr>

				<tr>
					<th><@spring.message "agent.table.name"/></th>
				</tr>
				<tr>
					<td>${(agent.hostName)!}</td>
				</tr>

				<tr>
					<th><@spring.message "agent.table.region"/></th>
				</tr>
				<tr>
					<td>${(agent.region)!}</td>
				</tr>

				<tr>
					<th><@spring.message "agent.table.version"/></th>
				</tr>
				<tr>
					<td>${(agent.version)!"Prev 3.3"}</td>
				</tr>

				<tr>
					<th><@spring.message "agent.table.state"/></th>
				</tr>
				<tr>
					<td>${(agent.state)!}</td>
				</tr>
				</tbody>
			</table>
			<label><@spring.message "agent.info.refreshInterval"/></label>
			<input id="refresh_interval" type="text" class="span3" placeholder="number" value="1">
		</div>
		<div class="span9">
			<div class="page-header page-header">
				<h4><@spring.message "agent.info.systemData"/></h4>
			</div>
			<h6>CPU</h6>

			<div class="chart" id="cpu_usage_chart"></div>
			<h6 style="margin-top:20px">Memory</h6>

			<div class="chart" id="memory_usage_chart"></div>
		</div>
	</div>
<#include "../common/copyright.ftl">
	<!--content-->
</div>
<script src="${req.getContextPath()}/js/queue.js?${nGrinderVersion}"></script>
<script>
	var interval = 1;
	var timer;
	var cpuUsage = new Queue(60);
	var memoryUsage = new Queue(60);
	var cpuChart = new Chart("cpu_usage_chart", [cpuUsage.aElement], interval,
			{yAxisFormatter: formatPercentage}).plot();
	var memoryChart = new Chart("memory_usage_chart", [memoryUsage.aElement], interval,
			{yAxisFormatter: formatMemory}).plot();


	$(document).ready(function () {
		getState();
		$("#refresh_interval").keyup(function () {
			var number = $(this).val();
			$(this).val(number.replace(/[\D]/g, ""))
		}).blur(function () {
					if (timer) {
						window.clearInterval(timer);
					}
					interval = $(this).val();
					if (interval == 0) {
						interval = 1;
					}
					timer = window.setInterval("getState()", interval * 1000);
				}).blur();
	});

	function getState() {
		var ajaxObj = new AjaxObj("/agent/api/${agent.id}/state");
		ajaxObj.params = { 'ip': '${(agent.ip)!}', 'name': '${(agent.hostName)!}', 'imgWidth': 700 };
		ajaxObj.success = function (res) {
			cpuUsage.enQueue(res.cpuUsedPercentage);
			memoryUsage.enQueue(res.totalMemory - res.freeMemory);
			cpuChart.plot();
			memoryChart.plot();
			return true;
		};
		ajaxObj.call();
	}

</script>
</body>
</html>
