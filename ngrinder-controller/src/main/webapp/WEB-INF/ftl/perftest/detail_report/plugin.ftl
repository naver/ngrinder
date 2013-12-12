<div class="page-header">
	<h4>${plugin} - ${kind}</h4>
</div>

<div id="chart_container">
</div>

<script>


	function getPluginDataAndDraw(testId, plugin, kind) {
		var ajaxObj = new AjaxObj("/perftest/api/" + testId + "/plugin/" + plugin);
		ajaxObj.params = {'kind': kind, 'imgWidth': 700};
		ajaxObj.success = function (data) {
			drawPluginChart(data);
		};
		ajaxObj.call();
	}

	function drawPluginChart(data) {
		var headers = eval(data.header);
		var $container = $("#chart_container");
		for (var i = 0; i < headers.length; i++) {
			var currentHead = headers[i];
			$container.append("<h6>" + currentHead.replace(/_/g, " ") + "</h6>" +
					"<div id='" + currentHead + "' class='chart'></div>");
			var currentData = data[currentHead];
			var dataFormat;
			var currentHeadLow = currentHead.toLowerCase();
			if (currentHeadLow.lastIndexOf("cpu") >= 0) {
				dataFormat = formatPercentage;
			} else if (currentHeadLow.lastIndexOf("memory") >= 0 || currentHeadLow.lastIndexOf("heap") >= 0) {
				dataFormat = formatMemoryInByte;
			} else {
				dataFormat = null;
			}
			drawChart(currentHead, [currentData], dataFormat, data.interval);
		}
		generateImg(imgBtnLabel, imgTitle);
	}

	function drawChart(id, data, yFormat, interval) {
		var result = new Chart(id, data, null, yFormat, interval);
		return result.plot();
	}

	getPluginDataAndDraw(${id}, "${plugin}", "${kind}");
</script>