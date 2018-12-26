<#setting number_format="computer">
<#import "../../common/spring.ftl" as spring/>
<div class="page-header page-header">
	<h4>Monitor</h4>
</div>
<h6 id="cpu_usage_chart_header">CPU</h6>
<div class="chart" id="cpu_usage_chart"></div>
<h6 id="mem_usage_chart_header">Used Memory</h6>
<div class="chart" id="mem_usage_chart"></div>
<h6 id="received_byte_per_sec_chart_header">Received Byte Per Second</h6>
<div class="chart" id="received_byte_per_sec_chart"></div>
<h6 id="sent_byte_per_sec_chart_header">Sent Byte Per Second</h6>
<div class="chart" id="sent_byte_per_sec_chart"></div>
<h6 id="custom_monitor_chart_1_header">Custom Monitor Chart 1</h6>
<div class="chart" id="custom_monitor_chart_1"></div>
<h6 id="custom_monitor_chart_2_header">Custom Monitor Chart 2</h6>
<div class="chart" id="custom_monitor_chart_2"></div>
<h6 id="custom_monitor_chart_3_header">Custom Monitor Chart 3</h6>
<div class="chart" id="custom_monitor_chart_3"></div>
<h6 id="custom_monitor_chart_4_header">Custom Monitor Chart 4</h6>
<div class="chart" id="custom_monitor_chart_4"></div>
<h6 id="custom_monitor_chart_5_header">Custom Monitor Chart 5</h6>
<div class="chart" id="custom_monitor_chart_5"></div>

<script>
	//@ sourceURL=/perftest/detail_report/monitor
	function getMonitorDataAndDraw(testId, targetIP) {
		var ajaxObj = new AjaxObj("/perftest/api/" + testId + "/monitor");
		ajaxObj.params = {
			targetIP: targetIP,
			imgWidth: parseInt($("#cpu_usage_chart").width())
		};
		ajaxObj.success = function (data) {
			var interval = data.interval;
			drawChart('cpu_usage_chart', [data.cpu], formatPercentage, interval);
			drawChart('mem_usage_chart', [data.memory], formatMemory, interval);
			drawChart("received_byte_per_sec_chart", [data.received], formatNetwork, interval);
			drawChart("sent_byte_per_sec_chart", [data.sent], formatNetwork, interval);
			drawOptionalChart("custom_monitor_chart_1", [data.customData1], formatNetwork, interval);
			drawOptionalChart("custom_monitor_chart_2", [data.customData2], formatNetwork, interval);
			drawOptionalChart("custom_monitor_chart_3", [data.customData3], formatNetwork, interval);
			drawOptionalChart("custom_monitor_chart_4", [data.customData4], formatNetwork, interval);
			drawOptionalChart("custom_monitor_chart_5", [data.customData5], formatNetwork, interval);
			createChartExportButton("<@spring.message "perfTest.report.exportImg.button"/>", "<@spring.message "perfTest.report.exportImg.title"/>");
		};
		ajaxObj.call();
	}
	function drawChart(id, data, yFormat, interval) {
		return new Chart(id, data, interval, {yAxisFormatter: yFormat}).plot();
	}

	function drawOptionalChart(id, data, interval, labels) {
		var result = drawChart(id, data, interval, labels);
		if (result.isEmpty()) {
			$("#" + id).hide();
			$("#" + id + "_header").hide();
		}
	}
	getMonitorDataAndDraw(${id}, "${targetIP}");


</script>