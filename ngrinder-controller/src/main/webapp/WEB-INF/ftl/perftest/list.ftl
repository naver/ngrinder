<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title>nGrinder Performance Test List</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta name="description" content="nGrinder Performance Test List">
		<meta name="author" content="AlexQin">

		<link rel="shortcut icon" href="favicon.ico"/>
		<link href="${req.getContextPath()}/css/bootstrap.min.css" rel="stylesheet">
		<link href="${req.getContextPath()}/plugins/datatables/css/demo_table.css" rel="stylesheet">
		<link href="${req.getContextPath()}/plugins/datatables/css/demo_page.css" rel="stylesheet">
		<link href="${req.getContextPath()}/plugins/datatables/css/demo_table_jui.css" rel="stylesheet">
		<link href="${req.getContextPath()}/css/ngrinder.css" rel="stylesheet">
		
		<input type="hidden" id="contextPath" value="${req.getContextPath()}">
		<#setting number_format="computer">
	</head>

	<body>
    	<#include "../common/navigator.ftl">
		<div class="container">
			<div class="row">
				<div class="span12">
							<a class="btn" href="${req.getContextPath()}/perftest/detail" id="createBtn" data-toggle="modal">
								<i class="icon-file"></i>
								Create test
							</a>
							<a class="btn pull-right" href="javascript:void(0);" id="deleteBtn">
								<i class="icon-remove"></i>
								Delete selected tests
							</a>
						</div>
					</div>
					<div class="well form-inline" style="padding:5px;margin:10px 0">
					
						<input type="text" class="search-query" placeholder="Keywords" id="searchText" value="${keywords!}">
						<button class="btn" id="clearBtn">Reset</button>
						<label class="checkbox pull-right" style="position:relative;top:5px">
							<input type="checkbox" id="onlyFinished" <#if isFinished??&&isFinished>checked</#if>> Finished
						</label>
					</div>
					<table class="display ellipsis" id="testTable" style="margin-bottom:10px;">
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
									<td colspan="11">
										No data to display.
									</td>
								</tr>
							</#if>
						</tbody>
					</table>
					<span class="help-inline" id="messageDiv"></span>
					<!--content-->
					<#include "../common/copyright.ftl">
				</div>
			</div>
		</div>
		<script src="${req.getContextPath()}/js/jquery-1.7.2.min.js"></script>
		<script src="${req.getContextPath()}/js/bootstrap.min.js"></script>
		<script src="${req.getContextPath()}/js/utils.js"></script>
		<script src="${req.getContextPath()}/plugins/datatables/js/jquery.dataTables.min.js"></script>
		<script>
			var oTable;

			$(document).ready(function() {
				
				$("#n_test").addClass("active");
				
				$("#searchText").change(function() {
					searchTestList();
				});

				$("#onlyFinished").on('click', function() {
					searchTestList();
				});
				
				$("#clearBtn").on('click', function() {
					$("#searchText").val("");
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
					if (confirm("Do you want to delete this test?")) {
						var delUrl = "${req.getContextPath()}/perftest/deleteTest?id=" + $(this).attr("sid");
						deleteTests(delUrl);
						oTable.fnDeleteRow($(this).parent().parent().parent().get());
					}
				});
				
				<#if testList?has_content>
				oTable = $("#testTable").dataTable({
					"bAutoWidth": false,
					"bFilter": true,
					"bLengthChange": false,
					"bInfo": false,
					"iDisplayLength": 15,
					"aaSorting": [[1, "asc"]],
					"bProcessing": true,
					"aoColumns": [{ "asSorting": []}, null, null, null, { "asSorting": []}, null, null, null, null, null, null, { "asSorting": []}],
					//"bJQueryUI": true,
					"sPaginationType": "full_numbers"
				});
				</#if>
				
			});
			
			function searchTestList() {
				var isFinished = false;
				if ($("#onlyFinished")[0].checked) {
					isFinished = 1;
				}
				var searchWords = $("#searchText").val();
				oTable.fnFilter(searchWords);
				//document.location.href = "${req.getContextPath()}/perftest/list?keywords=" + $("#searchText").val() + "&isFinished=" + isFinished;
			}
			
			function deleteTests(delUrl) {
				$.ajax({
			  		url: delUrl,
					dataType:'json',
			    	success: function(res) {
			    		if (res.success) {
				    		showMsg($('#messageDiv'), "The test(s) deleted successfully.");
							return true;
			    		} else {
				    		showMsg($('#messageDiv'), "test(s) deletion failed:" + res.message);
							return false;
			    		}
			    	},
			    	error: function() {
			    		showMsg($('#messageDiv'), "test(s) deletion failed!");
						return false;
			    	}
			  	});
			}
			
			function showMsg($megDiv, message) {
	    		$('#messageDiv').html("");
	        	$megDiv.html(message);
	        	$megDiv.fadeIn("fast");
	    		setTimeout(function(){$autoMsg.fadeOut('fast')}, 3000);
			}
			
		</script>
	</body>
</html>