<div class="page-header page-header">
	<h4>Monitor</h4>
</div>
<h6>CPU</h6>
<div class="chart" id="cpu_usage_chart"></div>
<h6>Used Memory</h6>
<div class="chart" id="mem_usage_chart"></div>
<h6 id="received_byte_per_sec_header">Received Byte Per Second</h6>
<div class="chart" id="received_byte_per_sec_chart"></div>
<h6 id="sent_byte_per_sec_header">Sent Per Second</h6>
<div class="chart" id="sent_byte_per_sec_chart"></div>
<h6 id="custom_monitor_header_1">Custom Monitor Chart 1</h6>
<div class="chart" id="custom_monitor_chart_1"></div>
<h6 id="custom_monitor_header_2">Custom Monitor Chart 2</h6>
<div class="chart" id="custom_monitor_chart_2"></div>
<h6 id="custom_monitor_header_3">Custom Monitor Chart 3</h6>
<div class="chart" id="custom_monitor_chart_3"></div>
<h6 id="custom_monitor_header_4">Custom Monitor Chart 4</h6>
<div class="chart" id="custom_monitor_chart_4"></div>
<h6 id="custom_monitor_header_5">Custom Monitor Chart 5</h6>
<div class="chart" id="custom_monitor_chart_5"></div>

<script>
	function getMonitorDataAndDraw(testId, targetIP) {
		$.ajax({
			url: "${req.getContextPath()}/perftest/api/" + testId + "/monitor",
			dataType: 'json',
			cache: true,
			data: {'targetIP': targetIP, 'imgWidth': 700}
		}).done(
			function (result) {
				drawMonitorChart(result);
			}
		).fail(
			function () {
				showErrorMsg("monitor data failed!");
			}
		);
	}

	function drawMonitorChart(data) {
		drawChart('cpu_usage_chart', data.cpu, formatPercentage, data.interval);
		drawChart('mem_usage_chart', data.memory, formatMemory, data.interval);
		drawChart("received_byte_per_sec_chart", data.received, formatNetwork, data.interval);
		drawChart("sent_byte_per_sec_chart", data.sent, formatNetwork, data.interval);
		drawExtMonitorData(data);
		generateImg(imgBtnLabel, imgTitle);
	}

	function drawExtMonitorData(data) {
		checkDataAndDraw("custom_monitor_chart_1", data.customData1, formatNetwork, data.interval);
		checkDataAndDraw("custom_monitor_chart_2", data.customData2, formatNetwork, data.interval);
		checkDataAndDraw("custom_monitor_chart_3", data.customData3, formatNetwork, data.interval);
		checkDataAndDraw("custom_monitor_chart_4", data.customData4, formatNetwork, data.interval);
		checkDataAndDraw("custom_monitor_chart_5", data.customData5, formatNetwork, data.interval);
	}

	getMonitorDataAndDraw(${id}, "${targetIP}");
</script>