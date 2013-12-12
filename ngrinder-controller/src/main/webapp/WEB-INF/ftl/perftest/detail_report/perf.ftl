<#import "../../common/spring.ftl" as spring/>
<div class="page-header">
	<h4>Performance</h4>
	<button id="download_csv" class="btn btn-primary pull-right" style="margin-top: -36px;">
		<i class="icon-download-alt icon-white"></i> <@spring.message "perfTest.report.downloadCSV"/>
	</button>
</div>

<h6>
	TPS
	<span rel="popover"
		data-content='<@spring.message "perfTest.report.tps.help"/>'
		title='<@spring.message "perfTest.report.tps"/>'
		data-html='true' id="tps_title">
		<i class="icon-question-sign pointer-cursor" style="vertical-align:middle;"></i>
	</span>
</h6>
<div class="bigchart" id="tps_chart"></div>
<h6><@spring.message "perfTest.report.header.meantime"/>&nbsp;(ms)</h6>
<div class="chart" id="mean_time_chart"></div>
<h6 id="min_time_first_byte_chart_header"><@spring.message "perfTest.report.header.meantimetofirstbyte"/>&nbsp;(ms)</h6>
<div class="chart" id="min_time_first_byte_chart"></div>
<h6 id="user_defined_chart_header"><@spring.message "perfTest.report.header.userDefinedChart"/></h6>
<div class="chart" id="user_defined_chart"></div>
<h6><@spring.message "perfTest.report.header.errors"/></h6>
<div class="chart" id="error_chart"></div>

<script>
	$("#tps_title").popover({trigger: 'hover', container:'body'});

	function getGraphDataAndDraw(testId) {
		var ajaxObj = new AjaxObj("/perftest/api/" + testId + "/perf");
		ajaxObj.params = {
			'dataType': 'TPS,Errors,Mean_Test_Time_(ms),Mean_time_to_first_byte,User_defined',
			'imgWidth': 700
		};
		ajaxObj.success = function (data) {
			var interval = data.chartInterval;
			drawChart("tps_chart", data.TPS.data, interval, data.TPS.lables);
			drawChart("mean_time_chart", data.Mean_Test_Time_ms.data, interval, data.Mean_Test_Time_ms.lables);
			drawChart('error_chart', data.Errors.data, interval, data.Errors.lables);
			drawOptionalChart("min_time_first_byte_chart", data.Mean_time_to_first_byte.data, interval,
					data.Mean_time_to_first_byte.lables);
			drawOptionalChart("user_defined_chart", data.User_defined.data, interval, data.User_defined.lables);
			generateImg(imgBtnLabel, imgTitle);

		};
		ajaxObj.call();
	}
	function drawChart(id, data, interval, lables) {
		var result = new Chart(id, data, null, null, interval, lables);
		result.plot();

	}
	function drawOptionalChart(id, data, interval, lables) {
		if (data !== undefined && data.length != 0) {
			drawChart(id, data, interval, lables);
		} else {
			$("#" + id).hide();
			$("#" + id + "_header").hide();
		}
	}
	$("#download_csv").click(function () {
		document.forms.download_csv_form.action = "${req.getContextPath()}/perftest/${id}/download_csv";
		document.forms.download_csv_form.submit();
	});
	getGraphDataAndDraw(${id});
</script>