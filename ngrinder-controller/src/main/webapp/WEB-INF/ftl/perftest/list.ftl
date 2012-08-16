<!DOCTYPE html>
<html>
	<head>
		<title>nGrinder Performance Test List</title>
		<#include "../common/common.ftl">
		<#include "../common/datatables.ftl">
	</head>

	<body>
    	<#include "../common/navigator.ftl">

		<div class="container">
			<img src="${req.getContextPath()}/img/bg_perftest_banner.png" style="margin-top:-20px;margin-bottom:10px"/>
			
			<form id="listForm" action=""${req.getContextPath()}/perftest/list" method="POST">
				<input type="hidden" id="sortColumn" name="page.sort" value="${(sortColumn)!'lastModifiedDate'}">
				<input type="hidden" id="sortDirection" name="page.sort.dir" value="${(sortDirection)!'desc'}">
		
				<div class="well form-inline searchBar">
					<input type="text" class="search-query" placeholder="Keywords" name ="query" id="query" value="${query!}">
					<button type="submit" class="btn" id="searchBtn"><i class="icon-search"></i> <@spring.message "perfTest.formInline.search"/></button>
					<label class="checkbox" style="position:relative;">
						<input type="checkbox" id="onlyFinished" name="onlyFinished" <#if isFinished??&&isFinished>checked</#if>> <@spring.message "perfTest.formInline.onlyFished"/>
					</label>
					<span class="pull-right">
						<a class="btn btn-primary" href="${req.getContextPath()}/perftest/detail" id="createBtn" data-toggle="modal">
							<i class="icon-file"></i>
							<@spring.message "perfTest.formInline.createTest"/>
						</a>
						<a class="btn btn-danger" href="javascript:void(0);" id="deleteBtn">
							<i class="icon-remove"></i>
							<@spring.message "perfTest.formInline.deletetSelectedTest"/>
						</a>
					</span>
				</div>
				<table class="table table-striped table-bordered ellipsis" id="testTable">
					<colgroup>
						<col width="30">
						<col width="40">  
						<col>
						<col>
						<col width="130">
						<col width="85">
						<col width="60">
						<col width="95">
						<col width="70">
						<col width="80">
					</colgroup>
					<thead>
						<tr>
							<th class="nothing"><input id="chkboxAll" type="checkbox" class="checkbox" value=""></th>
							<th class="nothing"style="text-align:center"> </th>
							<th id="testName"><@spring.message "perfTest.table.testName"/></th>
							<th id="scriptName"><@spring.message "perfTest.table.scriptName"/></th>
							<th id="startTime"><@spring.message "perfTest.table.startTime"/></th>
							<th id="duration"><@spring.message "perfTest.table.duration"/></th>
							<th id="tps"><@spring.message "perfTest.table.tps"/></th>
							<th id="meanTestTime"><@spring.message "perfTest.table.meantime"/></th>
							<th id="errors"><@spring.message "perfTest.table.errors"/></th>
							<th class="nothing"><@spring.message "perfTest.table.vusers"/></th> 
						</tr>
					</thead>
					<tbody>
						<#assign testList = testListPage.content/>
						<#if testList?has_content>
							<#list testList as test>
								<#assign vuserTotal = (test.vuserPerAgent)!0 * (test.agentCount)!0 />
								<tr id="tr${test.id}">
									<td style="text-align:center">
										<input type="checkbox" class="checkbox perf_test" value="${test.id}" 
											<#if !(test.status.isDeletable())>disabled</#if>>
									</td>
									<td class="ellipsis"  style="text-align:center" id="row_${test.id}">
										<div class="ball" id="ball_${test.id}"
										<#if test.status == 'STOP_ON_ERROR'>
											 rel="popover"
											 data-content="Error on ${test.testErrorCause} phase. ${(test.testErrorStackTrace)! ?replace('\n', '<br/>')?html}" 
											 data-original-title="${test.status}"
										<#else>
											 rel="popover"
											 data-content="${test.createdDate}" 
											 data-original-title="${test.status}"
										</#if>
										>
											<img src="${req.getContextPath()}/img/ball/${test.status.iconName}"/>
										</div>
									</td>
									<td class="ellipsis" >   
										<div rel="popover"
											 data-content="${test.description?replace('\n', '<br/>')?html}  &lt;br&gt;&lt;br&gt; modified at <#if test.lastModifiedDate?exists>${test.lastModifiedDate?string('yyyy-MM-dd HH:mm:ss')}</#if>"  
											 data-original-title="${test.testName}">
											<a href="${req.getContextPath()}/perftest/detail?id=${test.id}" target="_self">${test.testName}</a>
											<#if test.status.isDeletable()><a href="javascript:void(0);"><i class="icon-remove test-remove" sid="${test.id}"></i></a></#if>
											<#if test.status.isStoppable()><a href="javascript:void(0);"><i class="icon-remove test-remove" sid="${test.id}"></i></a></#if>
										</div>

									</td>
									<td class="ellipsis">
										<a href="${req.getContextPath()}/script/detail/${test.scriptName}" title="${test.scriptName}">${test.scriptName}</a> 
									</td>
									<td><#if test.startTime?exists>${test.startTime?string('yyyy-MM-dd HH:mm')}</#if></td>
									<td>${(test.durationStr)!}</td> 
									<td>${(test.tps)!}</td>  
									<td>${(test.meanTestTime)!0}</td>
									<td>${(test.errors)!0}</td>
									<td>${vuserTotal}</td>
								</tr> 
							</#list> 
						<#else>
							<tr>
								<td colspan="12" class="noData">
									<@spring.message "perfTest.table.noData"/>
								</td>
							</tr>
						</#if>
					</tbody>
				</table>
					<#include "../common/paging.ftl">
					<@paging  testListPage.totalElements!0 testListPage.number+1 testListPage.size 10 ""/>
					<INPUT type="hidden" id="pageNumber" name="page.page" value="${page.pageNumber + 1}">
					<INPUT type="hidden" id="pageSize" name="page.size" value="${page.pageSize!10}">
					<script type="text/javascript">
						function doSubmit(page) {
							getList(page);
						}
					</script>
			</form>
					<!--content-->
			<#include "../common/copyright.ftl">
		</div>
	</div>
</div>
	<script>
		console.log(${testListPage.totalElements} + "," + ${testListPage.number} + "," + ${testListPage.size});
			
		$(document).ready(function() {
			$("#n_test").addClass("active");
			
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
					
					deleteTests(idArray.join(","));
				}
			});
			
			$("i.test-remove").click(function() {
				if (confirm("Do you want to delete this test(s)?")) {
					deleteTests($(this).attr("sid"));
				}
			});
			
			$("i.test-stop").click(function() {
				if (confirm("Do you want to stop this test(s)?")) {
					stopTests($(this).attr("sid"));
				}
			});
			
			<#if testList?has_content>
			$("th").each(function() {
				var $this = $(this);
				if (!$this.hasClass("nothing")) {
					$this.addClass("sorting");
				}
			});
			
			var sortColumn = $("#sortColumn").val();
			var sortDir = $("#sortDirection").val().toLowerCase();
			
			$("#" + sortColumn).addClass("sorting_" + sortDir);

			$("th.sorting").on('click', function() {
				var $currObj = $(this);
				var sortDirection = "ASC";
				if ($currObj.hasClass("sorting_asc")) {
					sortDirection = "DESC";
				}
				
				$("#sortColumn").val($currObj.attr('id'));
				$("#sortDirection").val(sortDirection);
				
				getList(1);
			});
			</#if>
		});
		
		function deleteTests(ids) {
			$.ajax({
		  		url: "${req.getContextPath()}/perftest/deleteTests",
		  		type: "POST",
		  		data: {"ids" : ids},
				dataType:'json',
		    	success: function(res) {
		    		if (res.success) {
			    		showSuccessMsg("The test(s) deleted successfully.");
							setTimeout(function() {
								getList(1);
							}, 1000);
		    		} else {
			    		showErrorMsg("Test(s) deletion failed:" + res.message);
		    		}
		    	},
		    	error: function() {
		    		showErrorMsg("Test(s) deletion failed!");
		    	}
		  	});
		}
		
		function stopTests(ids) {
			$.ajax({
		  		url: "${req.getContextPath()}/perftest/stopTests",
				type: "POST",
		  		data: {"data":ids},
				dataType:'json',
		    	success: fu\nction(res) {
		    		if (res.success) {
			    		showSuccessMsg("The stop is requested");
							setTimeout(function() {
								getList(1);
							}, 1000);
		    		} else {
			    		showErrorMsg("Test(s) deletion failed:" + res.message);
		    		}
		    	},
		    	error: function() {
		    		showErrorMsg("Test(s) deletion failed!");
		    	}
		  	});
		}
		
		function getSortColumn(colNum) {
			return perfTestSortColumnMap[colNum];
		}
		
		function getList(page) {
			$("#pageNumber").val(page);
			document.forms.listForm.submit();
		}
		
		function updateStatus(id, status, icon, message) {
			var ballImg = $("#ball_" + id + " img");
			if (ballImg.attr("src") != "${req.getContextPath()}/img/ball/" + icon) { 
				ballImg.attr("src", "${req.getContextPath()}/img/ball/" + icon);
				$(".icon-remove[sid=" + id + "]").remove();
			}
			$("#ball_" + id).attr("data-original-title", status);
			$("#ball_" + id).attr("data-content", message);
			
		}
		// Wrap this function in a closure so we don't pollute the namespace
		(function refreshContent() {
			var ids = [];
			$('.perf_test').map(function(i,n) {
		        	return ids.push($(n).val());
		  	});
			if (ids.length == 0) {
				return;
			}
		    $.ajax({
			    url: '${req.getContextPath()}/perftest/updateStatus', 
			    type: 'POST',
			    data: {"ids": ids.join(",")},
			    success: function(data) {
			    	data = eval(data); 
			    	for (var i = 0; i < data.length; i++) {
			    		updateStatus(data[i].id, data[i].name, data[i].icon, data[i].message);
			    	}
			    },
			    complete: function() {
			        setTimeout(refreshContent, 5000);
			    }
		    });
	  })();
	</script>
	</body>
</html>
