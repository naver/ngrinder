<#import "../../common/spring.ftl" as spring/>
<div class="page-header">
	<span><h4>Performance</h4></span>
	<button id="download_csv" class="btn btn-primary pull-right" style="margin-top: -36px;">
		<i class="icon-download-alt icon-white"></i> <@spring.message "perfTest.report.downloadCSV"/>
	</button>
</div>

<h6>TPS
        <span rel="popover"
			  data-content='<@spring.message "perfTest.report.tps.help"/>'
			  title='<@spring.message "perfTest.report.tps"/>'
			  data-html='true'>
			<i class="icon-question-sign" style="vertical-align:middle;"></i>
        </span>
</h6>
<div class="bigchart" id="tps_chart"></div>
<h6><@spring.message "perfTest.report.header.meantime"/>&nbsp;(ms)</h6>
<div class="chart" id="mean_time_chart"></div>
<h6 id="min_time_first_byte_header"><@spring.message "perfTest.report.header.meantimetofirstbyte"/>&nbsp;(ms)</h6>
<div class="chart" id="min_time_first_byte_chart"></div>
<h6 id="user_defined_header"><@spring.message "perfTest.report.header.userDefinedChart"/></h6>
<div class="chart" id="user_defined_chart"></div>
<h6><@spring.message "perfTest.report.header.errors"/></h6>
<div class="chart" id="error_chart"></div>

<script>
	function getPerformanceDataAndDraw(testId) {
		$.ajax({
			url: "${req.getContextPath()}/perftest/api/" + testId + "/perf",
			dataType: 'json',
			cache: true,
			data: {'dataType': 'TPS,Errors,Mean_Test_Time_(ms),Mean_time_to_first_byte,User_defined', 'imgWidth': 700}
		}).done(
			function (perfData) {
				drawPerformanceChart(perfData);
			}).fail(function (xhr, status, error) {
				showErrorMsg("Get report data failed!");
			});
	}

	function drawPerformanceChart(perfData) {
		var result = drawMultiPlotChart('tps_chart', perfData.TPS.data, perfData.TPS.lables, perfData.chartInterval);
		if (result !== undefined) {
			result.replot();
		}
		result = drawMultiPlotChart('mean_time_chart', perfData.Mean_Test_Time_ms.data, perfData.Mean_Test_Time_ms.lables, perfData.chartInterval);
		if (result !== undefined) {
			result.replot();
		}

		if (perfData.Mean_time_to_first_byte !== undefined && perfData.Mean_time_to_first_byte.data !== '[ ]') {
			drawMultiPlotChart('min_time_first_byte_chart', perfData.Mean_time_to_first_byte.data, perfData.Mean_time_to_first_byte.lables, perfData.chartInterval);
		} else {
			$("#min_time_first_byte_chart").hide();
			$("#min_time_first_byte_header").hide();
		}
		if (perfData.User_defined !== undefined && perfData.User_defined.lables !== undefined
				&& perfData.User_defined.lables.length != 0) {
			drawMultiPlotChart('user_defined_chart', perfData.User_defined.data, perfData.User_defined.lables, perfData.chartInterval);
		} else {
			$("#user_defined_chart").hide();
			$("#user_defined_header").hide();
		}
		drawMultiPlotChart('error_chart', perfData.Errors.data, perfData.Errors.lables, perfData.chartInterval);
		generateImg(imgBtnLabel, imgTitle);
		return true;
	}

	$(document).ready(function () {
		$("#download_csv").click(function () {
			document.forms.download_csv_form.action = "${req.getContextPath()}/perftest/${id}/download_csv";
			document.forms.download_csv_form.submit();
		});
		getPerformanceDataAndDraw(${id});
	});
</script>