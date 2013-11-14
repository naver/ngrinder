<#import "../common/spring.ftl" as spring/>
<div id="system_chart">
	<div class="page-header page-header">
		<h4><@spring.message "agent.info.systemData"/></h4>
		<input type="hidden" id="target_ip" value="${(targetIP)!}">
	</div>
	<h6>CPU</h6>
	<div class="chart" id="cpu_usage_chart"></div>
	<h6 style="margin-top: 20px">Memory</h6>
	<div class="chart" id="memory_usage_chart"></div>
</div>


<script src="${req.getContextPath()}/js/queue.js?${nGrinderVersion}"></script>
<script>
	var interval = 1;
	var timer;
	var cpuUsage = new Queue();
	var memoryUsage = new Queue();
	var jqplots = [];
	var maxCPU = 0;
	var maxMemory = 0;
	var errorCount = 0;
	$(document).ready(function() {
		initChartData();
		if (getState()) {
			timer = window.setInterval("getState()", interval * 1000);
		}
	});

	function getMax(prev, current) {
		var currentMax = 0;
		for ( var i = 0; current.length > i; i++) {
			if (current[i] > currentMax) {
				currentMax = current[i];
			}
		}
		if (prev > currentMax) {
			return prev;
		}
		return currentMax;
	}

	function getState() {
		var result = true;
		$.ajax({
			url : "${req.getContextPath()}/monitor/state",
			async : false,
			cache : false,
			dataType : 'json',
			data : {
				'ip' : '${(targetIP)!}'
			},
			success : function(res) {
				getChartData(res);
				maxCPU = getMax(maxCPU, cpuUsage.aElement);
				showChart('cpu_usage_chart', cpuUsage.aElement, 0, formatPercentage, maxCPU);
				maxMemory = getMax(maxMemory, memoryUsage.aElement);
				showChart('memory_usage_chart', memoryUsage.aElement, 1, formatMemory, maxMemory);
				result = true;
				errorCount = 0;
			},
			error : function() {
				errorCount = errorCount + 1;
				if (errorCount > 3) {
					showErrorMsg("Failed to get the monitoring data.");
					result = false;
					if (timer) {
						window.clearInterval(timer);
					}
				}
			}
		});
		return result;
	}

	function showChart(containerId, data, index, formatYaxis, maxY) {
		var pt = jqplots[index];
		if (pt) {
			replotChart(pt, data, maxY, interval);
		} else {
			jqplots[index] = drawChart(containerId, data, formatYaxis);
		}
	}

	function initChartData() {
		for ( var i = 0; i < 60; i++) {
			cpuUsage.enQueue(0);
			memoryUsage.enQueue(0);
		}
	}

	function getChartData(dataObj) {
		cpuUsage.enQueue(dataObj.cpuUsedPercentage);
		memoryUsage.enQueue(dataObj.totalMemory - dataObj.freeMemory);

		if (cpuUsage.getSize() > 60) {
			cpuUsage.deQueue();
			memoryUsage.deQueue();
		}
	}

	function cleanChartData() {
		cpuUsage.makeEmpty();
		memoryUsage.makeEmpty();
		initChartData();
	}
</script>