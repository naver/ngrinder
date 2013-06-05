<#import "../common/spring.ftl" as spring/>
<div id="system_chart">
	<div class="page-header pageHeader">
		<h4><@spring.message "agent.info.systemData"/></h4>
		<input type="hidden" id="monitor_ip" value="${(monitorIp)!}">
	</div>
	<h6>CPU</h6>
	<div class="chart" id="cpu_div"></div>
	<h6 style="margin-top: 20px">Used Memory</h6>
	<div class="chart" id="memory_div"></div>
</div>


<script src="${req.getContextPath()}/js/queue.js?${nGrinderVersion}"></script>
<script>
	var interval = 1;
	var timer;
	var totalCpuQueue = new Queue();
	var usedMemoryQueue = new Queue();
	var jqplots = [];
	var maxCPU = 0;
	var maxMemory = 0;
	var errorCount = 0;
	$(document).ready(function() {

		$('#target_info_modal').css({
			'margin-top' : function() {
				return -380;
			}
		});
		initChartData();
		if (getStatus()) {
			timer = window.setInterval("getStatus()", interval * 1000);
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

	function getStatus() {
		var result = true;
		$.ajax({
			url : "${req.getContextPath()}/monitor/status",
			async : false,
			cache : false,
			dataType : 'json',
			data : {
				'ip' : '${(monitorIp)!}'
			},
			success : function(res) {
				if (res.success) {
					getChartData(res);
					maxCPU = getMax(maxCPU, totalCpuQueue.aElement);
					showChart('cpu_div', totalCpuQueue.aElement, 0,
							formatPercentage, maxCPU);
					maxMemory = getMax(maxMemory, usedMemoryQueue.aElement);
					showChart('memory_div', usedMemoryQueue.aElement, 1,
							formatMemory, maxMemory);
					result = true;
					errorCount = 0;
				} else {
					errorCount = errorCount + 1;
					if (errorCount > 3) {
						showErrorMsg("Get monitor data failed.");
						result = false;
						if (timer) {
							window.clearInterval(timer);
						}
					}
				}
			},
			error : function() {
				showErrorMsg("Get monitor data failed.");
				result = false;
				if (timer) {
					window.clearInterval(timer);
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
			totalCpuQueue.enQueue(0);
			usedMemoryQueue.enQueue(0);
		}
	}

	function getChartData(dataObj) {
		totalCpuQueue.enQueue(dataObj.systemData.cpuUsedPercentage);
		usedMemoryQueue.enQueue(dataObj.systemData.totalMemory - dataObj.systemData.freeMemory);

		if (totalCpuQueue.getSize() > 60) {
			totalCpuQueue.deQueue();
			usedMemoryQueue.deQueue();
		}
	}

	function cleanChartData() {
		totalCpuQueue.makeEmpty();
		sys_usedMemory.makeEmpty();
		initChartData();
	}
</script>