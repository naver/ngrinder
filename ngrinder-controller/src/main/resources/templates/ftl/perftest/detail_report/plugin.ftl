<#setting number_format="computer">
<#import "../../common/spring.ftl" as spring/>
<div class="page-header">
	<h4>${plugin?replace("_", " ")} - ${kind}</h4>
</div>

<div id="chart_container">
</div>

<script>
	//@ sourceURL=/perftest/detail_report/plugin
	function getPluginDataAndDraw(testId, plugin, kind) {
		var $container = $("#chart_container");
		var ajaxObj = new AjaxObj("/perftest/api/" + testId + "/plugin/" + plugin);
		ajaxObj.params = {
			kind : kind,
			imgWidth: parseInt($container.width())
		};
		ajaxObj.success = function (data) {
			var headers = eval(data.header);
			for (var i = 0; i < headers.length; i++) {
				var currentHead = headers[i];
				$container.append(
						"<h6>" +
						currentHead.replace(/_/g, " ") +
						"</h6>" +
						"<div id='" + currentHead + "' class='chart'></div>"
				);
				var currentData = data[currentHead];
				var dataFormat;
				var currentHeadLow = currentHead.toLowerCase();
				if (currentHeadLow.lastIndexOf("usage") >= 0) {
					dataFormat = formatPercentage;
				} else if (currentHeadLow.lastIndexOf("size") >= 0) {
					dataFormat = formatMemoryInByte;
				} else {
					dataFormat = null;
				}
				new Chart(currentHead, [currentData], data.interval, { yAxisFormatter:dataFormat }).plot();
			}
			createChartExportButton("<@spring.message "perfTest.report.exportImg.button"/>", "<@spring.message "perfTest.report.exportImg.title"/>");
		};
		ajaxObj.call();
	}

	getPluginDataAndDraw(${id}, "${plugin}", "${kind}");

</script>