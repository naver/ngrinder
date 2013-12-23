<!DOCTYPE html>
<html>
<head>
<#include "../common/common.ftl">
<#include "../common/datatables.ftl">
<#include "../common/jqplot.ftl">
	<title><@spring.message "perfTest.list.title"/></title>
	<style>
		td.today {
			background: url('${req.getContextPath()}/img/icon_today.png') no-repeat left top;
		}

		td.yesterday {
			background: url('${req.getContextPath()}/img/icon_yesterday.png') no-repeat left top;
		}

		.popover {
			width: auto;
			min-width: 300px;
			max-width: 600px;
		}

		.popover-content {
			white-space: nowrap;
			overflow: hidden;
			text-overflow: ellipsis;
		}

		div.small-chart {
			border: 1px solid #878988;
			height: 150px;
			min-width: 290px;
		}

		td.no-padding {
			padding: 0;
		}

		td.jqplot-table-legend {
			padding-bottom: 0;
		}

        .img-unit {
            background-image: url('${req.getContextPath()}/img/bg_perftest_banner_en.png?${nGrinderVersion}');
            height: 110px;
            padding: 0;
            margin-top: 40px;
        }

	</style>
</head>

<body>
<div id="wrap">
	<#include "../common/navigator.ftl">

	<div class="container">

		<div class="img-unit">
		</div>

	<form id="test_list_form" name="test_list_form"
	      class="well form-inline search-bar" style="margin-top:0;height:30px;"
		  action="${req.getContextPath()}/perftest/list" method="POST">
		<input type="hidden" id="sort_column" name="page.sort" value="${sortColumn!'lastModifiedDate'}">
		<input type="hidden" id="sort_direction" name="page.sort.dir" value="${sortDirection!'desc'}">

		<div class="left-float">
			<select id="tag" name="tag" style="width:150px">
				<option value=""></option>
			<@list list_items = availTags  ; eachTag >
				<option value="${eachTag}" <#if tag?? && eachTag == tag>selected </#if> >${eachTag}</option>
			</@list>
			</select>
			<input type="text" class="search-query search-query-without-radios span2" placeholder="Keywords"
				   name="query" id="query" value="${query!}">

			<button type="submit" class="btn" id="search_btn">
				<i class="icon-search"></i> <@spring.message "common.button.search"/>
			</button>
			<label class="checkbox" style="position:relative; margin-left:5px">
				<input type="checkbox" id="running_only_checkbox" name="queryFilter"
					   <#if queryFilter?? && queryFilter == 'R'>checked</#if>
					   value="R">
			<@spring.message "perfTest.action.running"/>
			</label>
			<label class="checkbox" style="position:relative; margin-left:5px">
				<input type="checkbox" id="scheduled_only_checkbox" name="queryFilter"
					   <#if queryFilter?? && queryFilter == 'S'>checked</#if>
					   value="S">
			<@spring.message "perfTest.action.scheduled"/>
			</label>
		</div>

		<div class="right-float">
			<a class="btn btn-primary" href="${req.getContextPath()}/perftest/new" id="create_btn">
				<i class="icon-file icon-white"></i>
			<@spring.message "perfTest.action.createTest"/>
			</a>
			<a class="pointer-cursor btn btn-danger" id="delete_btn">
				<i class="icon-remove icon-white"></i>
			<@spring.message "perfTest.action.deleteSelectedTest"/>
			</a>
		</div>

		<input type="hidden" id="page_number" name="page.page" value="${page.pageNumber + 1}">
		<input type="hidden" id="page_size" name="page.size" value="${page.pageSize}">
	</form>
	<div class="pull-right" style="margin-top:-20px">
		<code id="current_running_status" style="width:300px"></code>
	</div>
<@security.authorize ifAnyGranted="A, S">
	<#assign isAdmin = true />
</@security.authorize>
<table class="table table-striped table-bordered ellipsis" id="test_table" style="width:940px">
	<colgroup>
		<col width="30">
		<col width="50">
		<col>
		<col>
		<col width="70">
	<#if clustered>
		<col width="70">
	</#if>
		<col width="120">
		<col width="80">
		<col width="65">
		<col width="65">
		<col width="70">
		<col width="60">
		<col width="60">
	</colgroup>
	<thead>
	<tr id="head_tr_id">
		<th class="nothing"><input id="chkboxAll" type="checkbox" class="checkbox" value=""></th>
		<th class="center nothing" style="padding-left:3px"><@spring.message "common.label.status"/></th>
		<th id="test_name" name="testName"><@spring.message "perfTest.list.testName"/></th>
		<th id="script_name" name="scriptName"><@spring.message "perfTest.list.scriptName"/></th>
		<th class="nothing"><#if isAdmin??><@spring.message "perfTest.list.owner"/><#else><@spring.message "perfTest.list.modifier.oneLine"/></#if></th>
	<#if clustered>
		<th id="region" name="region"><@spring.message "common.region"/></th>
	</#if>
		<th id="start_time" name="startTime"><@spring.message "perfTest.list.startTime"/></th>
		<th class="nothing"><span class="ellipsis"><@spring.message "perfTest.list.threshold"/></th>
		<th id="tps" name="tps"><@spring.message "perfTest.list.tps"/></th>
		<th id="mean_test_time" name="meanTestTime" title='<@spring.message "perfTest.list.meantime"/>'>MTT</th>
		<th id="errors" class="ellipsis" name="errors"><@spring.message "perfTest.list.errorRate"/></th>
		<th class="nothing"><@spring.message "perfTest.list.vusers"/></th>
		<th class="nothing"><@spring.message "common.label.actions"/></th>
	</tr>
	</thead>
	<tbody>
	<#assign testList = testListPage.content/>
	<@list list_items=testList colspan="12"; test, test_index>
	<#assign totalVuser = (test.vuserPerAgent) * (test.agentCount) />
	<#assign deletable = !(test.status.deletable) />
	<#assign stoppable = !(test.status.stoppable) />
	<tr id="tr${test.id}" class='${["odd", ""][test_index%2]}'>
		<td class="center">
			<input id="check_${test.id}" type="checkbox" class="perf_test checkbox" value="${test.id}"
				   status="${test.status}" <#if deletable>disabled</#if>>
		</td>
		<td class="center" id="row_${test.id}">
			<div class="ball" id="ball_${test.id}"
				 data-html="true"
				 data-content="${"${test.progressMessage}<br/><b>${test.lastProgressMessage}</b>"?replace('\n', '<br>')?html}"
				 title="<@spring.message "${test.status.springMessageKey}"/>"
				 rel="popover">
				<img class="status" src="${req.getContextPath()}/img/ball/${test.status.iconName}"/>
			</div>
		</td>
		<td class="ellipsis ${test.dateString!""}">
			<div class="ellipsis"
				 rel="popover"
				 data-html="true"
				 data-content="${((test.description!"")?html)?replace("\n", "<br/>")} <p>${test.testComment?js_string?replace("\n", "<br/>")}</p><#if test.scheduledTime??><@spring.message "perfTest.list.scheduledTime"/> : ${test.scheduledTime?string('yyyy-MM-dd HH:mm')}<br/></#if><@spring.message "perfTest.list.modifiedTime"/> : <#if test.lastModifiedDate??>${test.lastModifiedDate?string("yyyy-MM-dd HH:mm")}</#if><br/><#if test.tagString?has_content><@spring.message "perfTest.config.tags"/> : ${test.tagString}<br/></#if><@spring.message "perfTest.list.owner"/> : ${test.createdUser.userName} (${test.createdUser.userId})<br/> <@spring.message "perfTest.list.modifier.oneLine"/> : ${test.lastModifiedUser.userName} (${test.lastModifiedUser.userId})"
				 data-title="${test.testName!""}">
				<a href="${req.getContextPath()}/perftest/${test.id}" target="_self">${test.testName!""}</a>
			</div>
		</td>
		<td class="ellipsis">
			<div class="ellipsis"
				 rel="popover"
				 data-html="true"
				 data-content="${test.scriptName}<br/> - <@spring.message "script.list.revision"/> : ${(test.scriptRevision)!'HEAD'}"
				 title="<@spring.message "perfTest.list.scriptName"/>">
				<#if isAdmin??>
					<a href="${req.getContextPath()}/script/detail/${test.scriptName}?r=${(test.scriptRevision)!-1}&ownerId=${(test.createdUser.userId)!}">${test.scriptName}</a>
				<#else>
					<a href="${req.getContextPath()}/script/detail/${test.scriptName}?r=${(test.scriptRevision)!-1}">${test.scriptName}</a>
				</#if>
			</div>
		</td>
		<td>
			<div class="ellipsis"
				 rel="popover"
				 title="<@spring.message "perfTest.list.participants"/>"
				 data-html="true"
				 data-content="<@spring.message "perfTest.list.owner"/> : ${test.createdUser.userName} (${test.createdUser.userId})<br/> <@spring.message "perfTest.list.modifier.oneLine"/> : ${test.lastModifiedUser.userName} (${test.lastModifiedUser.userId})">
				<#if isAdmin??>
					${test.createdUser.userName}
				<#else>
					${test.lastModifiedUser.userName}
				</#if>
			</div>
		</td>
		<#if clustered>
			<td class="ellipsis" title="<@spring.message "common.region"/>" data-html="true"
				data-content="<#if test.region?has_content><@spring.message "${test.region}"/></#if>"> <#if test.region?has_content><@spring.message "${test.region}"/></#if>
			</td>
		</#if>
		<td>
			<#if test.startTime??>${test.startTime?string('yyyy-MM-dd HH:mm')}</#if>
		</td>
		<td
			<#if test.isThresholdDuration()>
					title="<@spring.message "perfTest.config.duration"/>">
			${test.durationStr}
			<#else>
				title="<@spring.message "perfTest.config.runCount"/>" >
			${test.runCount}
			</#if>
		</td>
		<td><#if test.tps??>${(test.tps)?string(",##0.#")}</#if></td>
		<td><#if test.meanTestTime??>${(test.meanTestTime)?string("0.##")}</#if></td>
		<td>
			<div class="ellipsis" rel="popover" data-html="true" data-placement="top"
				 data-content="<@spring.message "perfTest.list.totalTests"/> : ${((test.tests + test.errors)?string(",##0"))!""}<br/><@spring.message "perfTest.list.successfulTests"/> : ${(test.tests?string(",##0"))!""}<br/><@spring.message "perfTest.list.errors"/> : ${(test.errors?string(",##0"))!""}<br/>">
				<#if test.tests?? && test.tests != 0>${(test.errors/(test.tests + test.errors) * 100)?string("0.##")}%</#if>
			</div>
		</td>
		<td>
			<div class="ellipsis" rel="popover" data-html="true" data-placement="left"
				 data-content="<@spring.message "perfTest.report.agent"/> : ${test.agentCount!"0"}<br/><@spring.message "perfTest.report.process"/>  : ${test.processes!"0"}<br/><@spring.message "perfTest.report.thread"/> : ${test.threads!"0"}">
				${totalVuser}
			<div>
		</td>
		<td class="center">
			<i title="<@spring.message 'perfTest.action.showChart'/>" id="show_${test.id}"
			   style="<#if !test.status.isReportable()>display: none;</#if>"
			   class="icon-download	test-display pointer-cursor"  sid="${test.id}"></i>
			<i title="<@spring.message "common.button.delete"/>" id="delete_${test.id}"
			   style="<#if deletable>display: none;</#if>"
			   class="icon-remove test-remove pointer-cursor" sid="${test.id}"></i>
			<i title="<@spring.message "common.button.stop"/>" id="stop_${test.id}"
			   style="<#if stoppable>display: none;</#if>"
			   class="icon-stop test-stop pointer-cursor" sid="${test.id}"></i>
		</td>
	</tr>
	</@list>
	</tbody>
</table>
<#if testList?has_content>
	<#include "../common/paging.ftl">
	<@paging  testListPage.totalElements testListPage.number+1 testListPage.size 10 ""/>
<script type="text/javascript">
	function doSubmit(page) {
		getList(page);
	}
</script>
</#if>
</div>

</div>
<#include "../common/copyright.ftl">

<script>
$(document).ready(function () {
	var columnCount = $('#head_tr_id').find('th').length;

	$("#tag").select2({
		placeholder: '<@spring.message "perfTest.action.selectATag"/>',
		allowClear: true
	}).change(function () {
			document.forms.test_list_form.submit();
		});

	$("#nav_test").addClass("active");

	enableCheckboxSelectAll("test_table");

	$("#delete_btn").click(function () {
		var list = $("td input:checked");
		if (list.length == 0) {
			bootbox.alert("<@spring.message "perfTest.message.delete.alert"/>", "<@spring.message "common.button.ok"/>");
			return;
		}

		bootbox.confirm("<@spring.message "perfTest.message.delete.confirm"/>", "<@spring.message "common.button.cancel"/>", "<@spring.message "common.button.ok"/>", function (result) {
			if (result) {
				var ids = list.map(function () {
					return $(this).val();
				}).get().join(",");

				deleteTests(ids);
			}
		});
	});

	$("i.test-remove").click(function () {
		var id = $(this).attr("sid");
		bootbox.confirm("<@spring.message "perfTest.message.delete.confirm"/>", "<@spring.message "common.button.cancel"/>", "<@spring.message "common.button.ok"/>", function (result) {
			if (result) {
				deleteTests(id);
				setTimeout(location.reload, 1000);
			}
		});
	});

	$("i.test-display").click(function () {
		var id = $(this).attr("sid");
		var perftestChartTrId = "test_tr_" + id;
		var tpsId = "tps_chart" + id;
		var meanTimeChartId = "mean_time_chart" + id;
		var errorChartId = "error_chart" + id;
		if (!$(this).closest('tr').next('#' + perftestChartTrId).length) {
			var testInfoTr = $("<tr id='" + perftestChartTrId + "' style='display:none'>" +
					"<td colspan='" + columnCount + "' class='no-padding'>" +
					"<table style='width:100%'>" +
					"<tr>" +
					"<td><div class='small-chart' id=" + tpsId + "></div></td>" +
					"<td><div class='small-chart' id=" + meanTimeChartId + "></div></td> " +
					"<td><div class='small-chart' id=" + errorChartId + "></div></td>" +
					"</tr>" +
					"</table>" +
					"</td></tr>");
			$(this).closest('tr').after(testInfoTr);
			var gridPadding = { top: 15, right: 10, bottom: 30, left: 40 };
			var ajaxObj = new AjaxObj("/perftest/api/" + id + "/graph");
			ajaxObj.params = {
				'dataType': 'TPS,Errors,Mean_Test_Time_(ms),Mean_time_to_first_byte,User_defined',
				'imgWidth': 100
			};
			ajaxObj.success = function (res) {
				/** @namespace res.chartInterval */
				var chartInterval = res.chartInterval;
				/** @namespace res.TPS */
				new Chart(tpsId, res.TPS.data, chartInterval,
						{
							labels: ["TPS"], gridPadding: gridPadding,
							numXTicks: 7, legend_margin: 3
						}).plot();
				/** @namespace res.Mean_Test_Time_ms */
				new Chart(meanTimeChartId, res.Mean_Test_Time_ms.data, chartInterval,
						{
							labels: ["Mean Test Time"], gridPadding: gridPadding, numXTicks: 7,
							legend_margin: 3
						}).plot();
				/** @namespace res.Errors */
				new Chart(errorChartId, res.Errors.data, chartInterval,
						{
							labels: ["Errors"],
							gridPadding: gridPadding, numXTicks: 7, legend_margin: 3
						}).plot();
				return true;
			};
			ajaxObj.call();
			testInfoTr.show("slow");
		} else {
			$("#" + perftestChartTrId).remove();
		}

	});

	$("i.test-stop").click(function () {
		var id = $(this).attr("sid");
		bootbox.confirm("<@spring.message code="perfTest.message.stop.confirm"/>", "<@spring.message "common.button.cancel"/>", "<@spring.message "common.button.ok"/>", function (result) {
			if (result) {
				stopTests(id);
			}
		});
	});

	$("th").each(function () {
		var $this = $(this);
		if (!$this.hasClass("nothing")) {
			$this.addClass("sorting");
		}
	});

	var sortColumn = $("#sort_column").val();
	var sortDir = $("#sort_direction").val().toLowerCase();

	$("th[name='" + sortColumn + "']").addClass("sorting_" + sortDir);

	$("th.sorting").click(function () {
		var $currObj = $(this);
		var sortDirection = "ASC";
		if ($currObj.hasClass("sorting_asc")) {
			sortDirection = "DESC";
		}

		$("#sort_column").val($currObj.attr('name'));
		$("#sort_direction").val(sortDirection);

		getList(1);
	});

	$("#current_running_status").click(function () {
		$("#current_running_status_div").toggle();
	});

	$("#scheduled_only_checkbox, #running_only_checkbox").click(function () {
		var $this = $(this);
		var checkId = $this.attr("id");
		if (checkId == "scheduled_only_checkbox") {
			checkboxReject($this, $("#running_only_checkbox"));
		} else if (checkId == "running_only_checkbox") {
			checkboxReject($this, $("#scheduled_only_checkbox"));
		}
		document.forms.test_list_form.submit();
	});
});

function checkboxReject(obj1, obj2) {
	if (obj1.attr("checked") == "checked" && obj2.attr("checked") == "checked") {
		obj2.attr("checked", false);
	}
}

function deleteTests(ids) {
	var ajaxObj = new AjaxPostObj("/perftest/api/delete",
			{ "ids": ids },
			"<@spring.message "perfTest.message.delete.success"/>",
			"<@spring.message "perfTest.message.delete.error"/>");
	ajaxObj.success = function () {
		setTimeout(function () {
			getList(1);
		}, 500);
	};
	ajaxObj.call();
}

function stopTests(ids) {
	var ajaxObj = new AjaxObj("${req.getContextPath()}/perftest/api/stop",
			"<@spring.message "perfTest.message.stop.success"/>",
			"<@spring.message "perfTest.message.stop.error"/>");
	ajaxObj.type = "POST";
	ajaxObj.params = { "ids": ids };
	ajaxObj.call();
}

function getList(page) {
	$("#page_number").val(page);
	document.forms.test_list_form.submit();
}

function updateStatus(id, status, statusId, icon, stoppable, deletable, reportable, message) {
	var $ballImg = $("#ball_" + id + " img");
	if ($ballImg.attr("src") != "${req.getContextPath()}/img/ball/" + icon) {
		$ballImg.attr("src", "${req.getContextPath()}/img/ball/" + icon);
		$(".icon-remove[sid=" + id + "]").remove();
	}

	var $ball = $("#ball_" + id);
	$ball.attr("data-original-title", status);
	$ball.data('popover').options.content = message;

	if (stoppable == true) {
		$("#stop_" + id).show();
	} else {
		$("#stop_" + id).hide();
	}

	if (deletable == true) {
		$("#delete_" + id).show();
	} else {
		$("#check_" + id).attr("disabled", true);
		$("#delete_" + id).hide();
	}

	if (reportable == true) {
		$("#show_" + id).show();
	}
}
// Wrap this function in a closure so we don't pollute the namespace
(function updateStatuses() {
	var ids = [];
	$('input.perf_test').each(function() {
		var $each = $(this);
		if (!isFinishedStatusType($each.attr("status"))) {
			ids.push($each.val());
		}
	});
	var ajaxObj = new AjaxObj("${req.getContextPath()}/perftest/api/status");
	ajaxObj.type = "POST";
	ajaxObj.params = {"ids": ids.join(",")};
	ajaxObj.success = function (data) {
		data = eval(data);
		var status = data.status;
		/** @namespace data.perfTestInfo */
		var perfTest = data.perfTestInfo;
		var springMessage = perfTest.length + " <@spring.message "perfTest.running.summary"/>";
		$("#current_running_status").text(springMessage);
		for (var i = 0; i < status.length; i++) {
			var each = status[i];
			/** @namespace each.status_id */

			var statusId = each.status_type;
			$("#check_" + each.id).attr("status", statusId);
			if (each.reportable) {
				location.reload();
			}
			/** @namespace each.deletable */
			/** @namespace each.reportable */
			/** @namespace each.stoppable */
			updateStatus(each.id, each.name, each.status_type, each.icon, each.stoppable, each.deletable,
					each.reportable, each.message);
		}
		if (ids.length == 0) {
			return true;
		}
		setTimeout(updateStatuses, 2000);
		return true;
	};
	ajaxObj.error = function () {
		var springMessage = "0 <@spring.message "perfTest.running.summary"/>";
		$("#current_running_status").text(springMessage);
		return true;
	};
	ajaxObj.call();
})();
</script>
</body>
</html>
