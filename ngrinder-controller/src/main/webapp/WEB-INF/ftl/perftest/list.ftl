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
			<div class="well form-inline searchBar">
				<input type="text" class="search-query" placeholder="Keywords" id="query" value="${query!}">
				<button class="btn" id="searchBtn">Search</button>
				<label class="checkbox" style="position:relative;">
					<input type="checkbox" id="onlyFinished" <#if isFinished??&&isFinished>checked</#if>> Only Finished
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
					<th>Status</th>
					<th>Test Name</th>
					<th>Script Name</th>
					<th class="noClick">Description</th>
					<th>Start Time</th>
					<th>TPS</th>
					<th>Mean Time</th>
					<th>Errors</th>
					<th>Vusers</th>
					<th>Duration</th>
					<th class="noClick">Del</th>
				</tr>
			</thead>
			<tbody>
				<#assign testList = testListPage.content/>
				<#if testList?has_content>
					<#list testList as test>
						<tr>
							<td><input type="checkbox" class="checkbox" value="${test.id}"></td>
							<td>${test.status}</td>
							<td class="left"><a href="${req.getContextPath()}/perftest/detail?id=${test.id}" target="_self">${test.testName}</a></td>
							<td>${test.scriptName}</td>
							<td class="left ellipsis" title="${(test.description)!}">${(test.description)!}</td>
							<td><#if test.startTime?exists>${test.startTime?string('yyyy-MM-dd HH:mm:ss')}</#if></td>
							<td>${(test.tps)!}</td>
							<td>${(test.meanTime)!0}</td>
							<td>${(test.errors)!0}</td>
							<td>${(test.vusers)!0}</td>
							<td>${(test.duration)!0}</td>
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
					<!--content-->
			<#include "../common/copyright.ftl">
		</div>
	</div>
</div>
	<script>
			var oTable;

			$(document).ready(function() {
				
				$("#n_test").addClass("active");
				
				$("#searchText").change(function() {
					searchTestList();
				});

				
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
				oTable = $("#testTable").dataTable({
					"bAutoWidth": false,
					"bFilter": false,
					"bLengthChange": false,
					"bInfo": false,
					"iDisplayLength": 15,
					"aaSorting": [[1, "asc"]],
					"bProcessing": true,
					"aoColumns": [{ "asSorting": []}, null, null, null, { "asSorting": []}, null, null, null, null, null, null, { "asSorting": []}],
					//"bJQueryUI": true,
					"sPaginationType": "full_numbers"
				});

				removeClick();
				</#if>
				
			});
			
			function searchTestList() {
				var isFinished = false;
				if ($("#onlyFinished")[0].checked) {
					isFinished = 1;
				}
				var searchWords = $("#searchText").val();
				document.location.href = "${req.getContextPath()}/perftest/list?query=" + $("#query").val() + "&isFinished=" + isFinished;
			}
			
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
		</script>
	</body>
</html>