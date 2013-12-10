<div class="page-header">
	<h4>${plugin} - ${kind}</h4>
</div>

<div id="chart_container">
</div>

<script>
	function getPluginChartAndDraw(testId, plugin, kind) {
		$.ajax({
			url: "${req.getContextPath()}/perftest/api/" + testId + "/plugin/" + plugin,
			dataType: 'json',
			cache: true,
			data: {'kind': kind, 'imgWidth': 700}
		}).done(
			function (result) {
				drawPluginChart(result);
			}).fail(function () {
				showErrorMsg("Get plugin data failed!");
			}
		);
	}

	function drawPluginChart(data) {
		var headerStr = data['header'];
		var headerList = eval(headerStr);
		var $container = $("#chart_container");

		for (var i = 0; i < headerList.length; i++) {
			var currentHead = headerList[i];
			$container.append("<h6>" + currentHead + "</h6><div id='" + currentHead + "' class='chart'></div>");
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

			checkDataAndDraw(currentHead, currentData, dataFormat, data.interval);
		}
		generateImg(imgBtnLabel, imgTitle);
	}
	getPluginChartAndDraw(${id}, "${plugin}", "${kind}");

</script>