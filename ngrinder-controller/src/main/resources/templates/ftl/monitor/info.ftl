<#setting number_format="computer">
<#import "../common/spring.ftl" as spring/>
<div id="system_chart">
	<div class="page-header page-header">
		<h4><@spring.message "monitor.info.header"/></h4>
		<input type="hidden" id="target_ip" value="${(targetIP)!}">
	</div>
	<h5><@spring.message "monitor.info.cpu"/></h5>
	<div class="chart" id="cpu_usage_chart"></div>
	<h5><@spring.message "monitor.info.memory"/></h5>
	<div class="chart" id="memory_usage_chart"></div>
</div>
<script src="${req.getContextPath()}/js/queue.js?${nGrinderVersion}"></script>
<script>
	var interval = 3;
	var cpuUsage = new Queue(60 / interval);
	var memoryUsage = new Queue(60 / interval);
	var cpuChart = new Chart("cpu_usage_chart", [cpuUsage.getArray()], interval,
			{yAxisFormatter: formatPercentage}).plot();
	var memoryChart = new Chart("memory_usage_chart", [memoryUsage.getArray()], interval,
			{yAxisFormatter: formatMemory}).plot();
	var errorCount = 0;

	function getState() {
		var ajaxObj = new AjaxObj("/monitor/state");
		ajaxObj.params = {'ip': '${(targetIP)!}'};
		ajaxObj.success = function (res) {
			cpuUsage.enQueue(res.cpuUsedPercentage);
			memoryUsage.enQueue(res.totalMemory - res.freeMemory);
			cpuChart.plot();
			memoryChart.plot();
			return true;
		};
		ajaxObj.call();
	}

	getState();
	var timer = window.setInterval("getState()", interval * 1000);
	$('#target_info_modal').on('hidden', function () {
		if (timer) {
			window.clearInterval(timer);
		}
	});
	//@ sourceURL=monitor/info
</script>
