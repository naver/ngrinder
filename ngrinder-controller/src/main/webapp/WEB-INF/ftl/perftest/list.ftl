<!DOCTYPE html>
<html>
	<head>
		<title>nGrinder Performance Test List</title>
		<#include "../common/common.ftl">
		<#include "../common/datatables.ftl">
		<style>
			.dataTables_filter {visibility:hidden;}
			
		</style>
	</head>

	<body>
    	<#include "../common/navigator.ftl">

		<div class="container">
			<img src="${req.getContextPath()}/img/bg_perftest_banner.png" style="margin-top:-20px;margin-bottom:10px"/>
			
			<form id="listForm" action=""${req.getContextPath()}/perftest/list" method="POST">
				<input type="hidden" id="sortColumn" name="page.sort" value="${(sortColumn)!'status'}">
				<input type="hidden" id="sortDirection" name="page.sort.dir" value="${(sortDirection)!'asc'}">
		
				<div class="well form-inline searchBar">
					<input type="text" class="search-query" placeholder="Keywords" name ="query" id="query" value="${query!}">
					<button type="submit" class="btn" id="searchBtn">Search</button>
					<label class="checkbox" style="position:relative;">
						<input type="checkbox" id="onlyFinished" name="onlyFinished" <#if isFinished??&&isFinished>checked</#if>> Only Finished
					</label>
					<span class="pull-right">
						<a class="btn" href="${req.getContextPath()}/perftest/detail" id="createBtn" data-toggle="modal">
							<i class="icon-file"></i>
							Create test
						</a>
						<a class="btn btn-danger" href="javascript:void(0);" id="deleteBtn">
							<i class="icon-remove"></i>
							Delete selected tests
						</a>
					</span>
				</div>
				<table class="table table-striped table-bordered ellipsis" id="testTable">
					<colgroup>
						<col width="30">
						<col width="75">
						<col width="100">
						<col width="110">
						<col>
						<col width="100">
						<col width="65">
						<col width="105">
						<col width="65">
						<col width="75">
						<col width="85">
						<col width="40">
					</colgroup>
					<thead>
						<tr>
							<th><input id="chkboxAll" type="checkbox" class="checkbox" value=""></th>
							<th column="1">Status</th>
							<th column="2">Test Name</th>
							<th column="3">Script Name</th>
							<th column="4" class="noClick">Description</th>
							<th column="5">Start Time</th>
							<th column="6">Duration</th>
							<th column="7">TPS</th>
							<th column="8">Mean Time</th>
							<th column="9">Errors</th>
							<th>Vusers</th>
							<th class="noClick">Del</th>
						</tr>
					</thead>
					<tbody>
						<#assign testList = testListPage.content/>
						<#if testList?has_content>
							<#list testList as test>
								<#assign vuserTotal = (test.vuserPerAgent)!0 * (test.agentCount)!0 />
								<tr>
									<td><input type="checkbox" class="checkbox" value="${test.id}"></td>
									<td>${test.status}</td>
									<td class="ellipsis" title="${test.testName}"><a href="${req.getContextPath()}/perftest/detail?id=${test.id}" target="_self">${test.testName}</a></td>
									<td class="ellipsis">${test.scriptName}</td>
									<td class="ellipsis" title="${(test.description)!}">${(test.description)!}</td>
									<td><#if test.startTime?exists>${test.startTime?string('yyyy-MM-dd HH:mm:ss')}</#if></td>
									<td>${(test.duration)!0}</td>
									<td>${(test.tps)!}</td>
									<td>${(test.meanTime)!0}</td>
									<td>${(test.errors)!0}</td>
									<td>${vuserTotal}</td>
									<td><a href="javascript:void(0);"><i class="icon-remove test-remove" sid="${test.id}"></i></a></td>
								</tr>
							</#list>
						<#else>
							<tr>
								<td colspan="12" class="noData">
									No data to display.
								</td>
							</tr>
						</#if>
					</tbody>
				</table>
					<#include "../common/paging.ftl">
					<@paging  testListPage.totalElements!0 testListPage.number+1 testListPage.size 10 ""/>
					<INPUT type="hidden" id="pageNumber" name="page.page" value="${page.pageNumber + 1}">
					<INPUT type="hidden" id="pageSize" name="page.size" value="${page.pageSize!15}">
					<script type="text/javascript">
							function doSubmit(page) {
								$("#pageNumber").val(page);
								document.forms.listForm.submit();
							}
					</script>
			</form>
					<!--content-->
			<#include "../common/copyright.ftl">
		</div>
	</div>
</div>
	<script>
		console.log(${testListPage.totalElements} +","+ ${testListPage.number}+","+${testListPage.size});
			var oTable;
			var perfTestSortColumnMap = {
					1:"status",
					2:"testName",
					3:"scriptName",
					4:"description",
					5:"startTime",
					6:"tps",
					7:"meanTestTime",
					8:"errors"};
			var perfTestSortColumnMapRevert = {
					"status":1,
					"testName":2,
					"scriptName":3,
					"description":4,
					"startTime":5,
					"tps":6,
					"meanTestTime":7,
					"errors":8};
			
			$(document).ready(function() {
				
				$("#n_test").addClass("active");
				
				$("#searchBtn").on('click', function() {
					searchTestList();
				});
				
				enableChkboxSelectAll();
				
				$("#deleteBtn").on('click', function() {
					var ids = "";
					var list = $("td input:checked");
					if(list.length == 0) {
						alert("Please select any tests first.");
						return;
					}
					if (confirm('Are you sure to delete the test(s)?')) {
						var idArray = [];
						list.each(function() {
							idArray.push($(this).val());
						});
						ids = idArray.join(",");
						
						var delUrl = "${req.getContextPath()}/perftest/deleteTests?ids=" + ids;
						deleteTests(delUrl);
					}
				});
				
				$("i.test-remove").on('click', function() {
					if (confirm("Do you want to delete this test(s)?")) {
						var delUrl = "${req.getContextPath()}/perftest/deleteTest?id=" + $(this).attr("sid");
						deleteTests(delUrl);
						oTable.fnDeleteRow($(this).parents('tr')[0]);
					}
				});
				
				<#if testList?has_content>
				var sortColumn = $("#sortColumn").val();
				var sortColNum = parseInt(perfTestSortColumnMapRevert[sortColumn]);

				var sortDir = $("#sortDirection").val().toLowerCase();
				oTable = $("#testTable").dataTable({
					"sDom":'lfrti',
					"bAutoWidth": false,
					"bFilter": false,
					"bLengthChange": false,
					"bInfo": false,
					"iDisplayLength": 15,
					"aaSorting": [[sortColNum, sortDir]],
					"aoColumns": [{ "asSorting": []}, null, null, null, { "asSorting": []}, null, null, null, null, null, { "asSorting": []}, { "asSorting": []}],
					"bProcessing": true
					//"bJQueryUI": true,
					//"sPaginationType": "full_numbers"
				});

				$("th.sorting,th.sorting_asc").on('click', function() {
					var $currObj = $(this);
					var sortColNum = $currObj.attr('column');
					$("#sortColumn").val(getSortColumn(sortColNum));
					
					var sortDirection = "ASC";
					if ($currObj.hasClass('sorting_asc')) {
						sortDirection = "DESC";
					}
					$("#sortDirection").val(sortDirection);
					console.log("Set sort column:" + getSortColumn(sortColNum) + ": " + sortDirection);
				});
				
				removeClick();
				
				</#if>
				
			});
			
			function deleteTests(delUrl) {
				$.ajax({
			  		url: delUrl,
					dataType:'json',
			    	success: function(res) {
			    		if (res.success) {
				    		showErrorMsg("The test(s) deleted successfully.");
							return true;
			    		} else {
				    		showErrorMsg("Test(s) deletion failed:" + res.message);
							return false;
			    		}
			    	},
			    	error: function() {
			    		showErrorMsg("Test(s) deletion failed!");
						return false;
			    	}
			  	});
			}
			
			function getSortColumn(colNum) {
				return perfTestSortColumnMap[colNum];
			}
			
		</script>
	</body>
</html>