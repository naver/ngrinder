	<#import "../common/spring.ftl" as spring/>
	<div id="system_chart">
		<div class="page-header pageHeader">
			<h3><@spring.message "agent.info.systemData"/></h3>
			<input type="hidden" id="monitorIp" value="${(monitorIp)!}">
		</div>
		<h6>CPU</h6>
	    <div class="chart" id="cpuDiv"></div>
		<h6 style="margin-top:20px">Used Memory</h6>
	    <div class="chart" id="memoryDiv"></div>
	</div>
    
    
     <script src="${req.getContextPath()}/js/queue.js?${nGrinderVersion}"></script>
        <script>
            var interval =1;
            var timer;
			var sys_totalCpuValue = new Queue();
			var sys_usedMemory = new Queue();
			var jqplots = [];
			var maxCPU = 0;
            var maxMemory = 0;
            var errorCount = 0;
            $(document).ready(function() {
            
            	$('#targetInfoModal').css({
			        'margin-top': function () {
			            return -380;
			        }
    			});
            	initChartData();
            	if (getStatus()) {
                	timer = window.setInterval("getStatus()",interval * 1000);
            	}
            });
            
            function getMax(prev, current) {
            	var currentMax = 0;
            	for (var i = 0; current.length > i; i++) {
	            	if (current[i] > currentMax) {
	            		currentMax = current[i];
	            	}
            	}
            	if (prev > currentMax) {
            		return prev;
            	}
            	return currentMax;
            }
            
            function getStatus(){
            	var result = true;
                $.ajax({
                    url: "${req.getContextPath()}/monitor/status",
                    async: false,
					cache: false,
                    dataType:'json',
                    data: {'ip': '${(monitorIp)!}'},
                    success: function(res) {
                        if (res.success) {
                        	getChartData(res);
                    		maxCPU = getMax(maxCPU, sys_totalCpuValue.aElement);
                    		showChart('cpuDiv', sys_totalCpuValue.aElement, 0, formatPercentage, maxCPU);
                    		maxMemory = getMax(maxMemory, sys_usedMemory.aElement);
                        	showChart('memoryDiv', sys_usedMemory.aElement, 1, formatMemory, maxMemory);
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
                    error: function() {
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
                for (var i = 0; i < 60; i++) {
		        	sys_totalCpuValue.enQueue(0);
		        	sys_usedMemory.enQueue(0);
            	}	
            }
            
            function getChartData(dataObj) {				
				sys_totalCpuValue.enQueue(dataObj.systemData.cpuUsedPercentage);
				sys_usedMemory.enQueue(dataObj.systemData.totalMemory - dataObj.systemData.freeMemory);
				
				if (sys_totalCpuValue.getSize() > 60) {
					sys_totalCpuValue.deQueue();
					sys_usedMemory.deQueue();
				}
			}
			
			function cleanChartData() {
				sys_totalCpuValue.makeEmpty();
				sys_usedMemory.makeEmpty();
				initChartData();
			}
        </script>