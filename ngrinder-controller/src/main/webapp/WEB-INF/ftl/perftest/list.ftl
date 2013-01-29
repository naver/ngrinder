<!DOCTYPE html>
<html>
	<head>
		<#include "../common/common.ftl">
		<#include "../common/datatables.ftl">	
		<title><@spring.message "perfTest.table.title"/></title>
		<style>
			td.today {
				background-image: url('${req.getContextPath()}/img/icon_today.png');
				background-repeat:no-repeat;
				background-position:left top;
			}
			td.yesterday {
				background-image: url('${req.getContextPath()}/img/icon_yesterday.png');
				background-repeat:no-repeat;
				background-position:left top;
			}
		</style>
	</head>

	<body>
    	<#include "../common/navigator.ftl">

		<div class="container">
			<img src="${req.getContextPath()}/img/bg_perftest_banner_<@spring.message "common.language"/>.png"/>
			
			<form id="listForm" class="well form-inline searchBar" action=""${req.getContextPath()}/perftest/list" method="POST">
				<input type="hidden" id="sortColumn" name="page.sort" value="${sortColumn!'lastModifiedDate'}">
				<input type="hidden" id="sortDirection" name="page.sort.dir" value="${sortDirection!'desc'}">
		
				<table style="width:100%">
					<colspan>
						<col width="*"/>
						<col width="300px"/> 
					</colspan>
					<tr>
						<td>
							<select id="tag" name="tag" style="width:150px"> 
							<option value=""></option>
							<#if availTags?has_content>
							    <#list availTags as eachTag> 
							  	   <option value="${eachTag}" <#if tag?? && eachTag == tag>selected </#if> >${eachTag}</option>
						        </#list>
						    </#if>
							</select> 
							<input type="text" class="search-query search-query-without-radios span2" placeholder="Keywords" name ="query" id="query" value="${query!}">
							
							<button type="submit" class="btn" id="searchBtn"><i class="icon-search"></i> <@spring.message "common.button.search"/></button>
							<label class="checkbox" style="position:relative; margin-left:5px">
								<input type="checkbox" id="finishedChk" name="queryFilter" <#if queryFilter?? && queryFilter == 'F'>checked</#if> value="F"> <@spring.message "perfTest.formInline.finished"/>
							</label>
							<label class="checkbox" style="position:relative; margin-left:5px">
								<input type="checkbox" id="scheduledChk" name="queryFilter" <#if queryFilter?? && queryFilter == 'S'>checked</#if> value="S"> <@spring.message "perfTest.formInline.scheduled"/>
							</label>
						</td>
						<td>
							<span class="pull-right">
								<a class="btn btn-primary" href="${req.getContextPath()}/perftest/detail" id="createBtn">
									<i class="icon-file icon-white"></i> 
									<@spring.message "perfTest.formInline.createTest"/>
								</a>
								<a class="btn btn-danger" href="javascript:void(0);" id="deleteBtn">
									<i class="icon-remove icon-white"></i>
									<@spring.message "perfTest.formInline.deletetSelectedTest"/>
								</a>
							</span>
						</td>
					</tr> 
				</table>
				<INPUT type="hidden" id="pageNumber" name="page.page" value="${page.pageNumber + 1}">
				<INPUT type="hidden" id="pageSize" name="page.size" value="${page.pageSize}">
			</form>
			
			<div class="pull-right"> 
				<code id="currentRunning" style="width:300px"></code>
			</div>
			<@security.authorize ifAnyGranted="A, S">
				<#assign isAdmin = true />
			</@security.authorize>
			<table class="table table-striped table-bordered ellipsis" id="testTable" style="width:940px">  
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
					<col width="62">
					<col width="75">
					<col width="30">
				</colgroup>
				<thead>
					<tr>
						<th class="nothing"><input id="chkboxAll" type="checkbox" class="checkbox" value=""></th>
						<th class="nothing"><@spring.message "common.label.status"/></th>
						<th id="testName"><@spring.message "perfTest.table.testName"/></th>
						<th id="scriptName"><@spring.message "perfTest.table.scriptName"/></th>
						<th class="nothing"><#if isAdmin??><@spring.message "perfTest.table.owner"/><#else><@spring.message "perfTest.table.modifier"/></#if></th>
						<#if clustered>
						<th id="region"><@spring.message "agent.table.region"/></th>
						</#if>
						<th id="startTime"><@spring.message "perfTest.table.startTime"/></th>
						<th class="nothing"><@spring.message "perfTest.table.threshold"/></th>
						<th id="tps"><@spring.message "perfTest.table.tps"/></th> 
						<th id="meanTestTime" title='<@spring.message "perfTest.table.meantime"/>' >MTT</th>
						<th id="errors"><@spring.message "perfTest.table.errors"/></th>
						<th class="nothing"><@spring.message "perfTest.table.vusers"/></th>
						<th class="nothing" title="<@spring.message "common.label.actions"/>"></th>
					</tr>
				</thead>
				<tbody>
					<#assign testList = testListPage.content/>
					<#if testList?has_content>
						<#list testList as test>
							<#assign vuserTotal = (test.vuserPerAgent) * (test.agentCount) />
							<#assign deletable = !(test.status.deletable) />
							<#assign stoppable = !(test.status.stoppable) />
							<tr id="tr${test.id}">
								<td class="center">
									<input id="check_${test.id}" type="checkbox" class="checkbox perf_test" value="${test.id}" status="${test.status}" <#if deletable>disabled</#if>>
								</td>
								<td class="center"  id="row_${test.id}">
									<div class="ball" id="ball_${test.id}" 
													rel="popover"
													data-content='${"${test.progressMessage}<br/><b>${test.lastProgressMessage}</b>"?replace('\n', '<br>')?html}'  
													data-original-title="<@spring.message "${test.status.springMessageKey}"/>" type="toggle"> 
										<img class="status" src="${req.getContextPath()}/img/ball/${test.status.iconName}"/> 
									</div>
								</td>
								<td class="ellipsis ${test.dateString}" data-content="${(test.description!"")?replace('\n', '<br/>')?html} &lt;p&gt;${test.testComment?replace('\n', '<br/>')?html}&lt;/p&gt;  &lt;p&gt;<#if test.scheduledTime?exists><@spring.message "perfTest.table.scheduledTime"/> : ${test.scheduledTime?string('yyyy-MM-dd HH:mm')}&lt;p&gt;</#if><@spring.message "perfTest.table.modifiedTime"/> : <#if test.lastModifiedDate?exists>${test.lastModifiedDate?string('yyyy-MM-dd HH:mm')}</#if>&lt;/p&gt;&lt;p&gt;<#if test.tagString?has_content><@spring.message "perfTest.configuration.tags"/> : ${test.tagString}&lt;/p&gt;</#if>"  
										 data-original-title="${test.testName}">
									<a href="${req.getContextPath()}/perftest/detail?id=${test.id}" target="_self">${test.testName}</a>
								</td>
								<td class="ellipsis"
									data-content="${test.scriptName} &lt;br&gt;&lt;br&gt; - <@spring.message "script.list.table.revision"/> : ${(test.scriptRevision)!'HEAD'}" 
									data-original-title="<@spring.message "perfTest.table.scriptName"/>">			
									<#if isAdmin??>
										<a href="${req.getContextPath()}/script/detail/${test.scriptName}?r=${(test.scriptRevision)!-1}&ownerId=${(test.createdUser.userId)!}">${test.scriptName}</a>
									<#else>
										<a href="${req.getContextPath()}/script/detail/${test.scriptName}?r=${(test.scriptRevision)!-1}">${test.scriptName}</a>
									</#if>
								</td>
		            			<td class="ellipsis" data-original-title="<@spring.message "perfTest.table.participants"/>" 
		            				data-content="<@spring.message "perfTest.table.owner"/> : ${test.createdUser.userName}&lt;br&gt; <@spring.message "perfTest.table.modifier.oneline"/> : ${test.lastModifiedUser.userName}">
		            				<#if isAdmin??>
		            					${test.lastModifiedUser.userName}
		            				<#else>
		            					${test.createdUser.userName}
		            				</#if>
		            			</td>
								<#if clustered>
								<td class="ellipsis" title="<@spring.message "agent.table.region"/>" data-content='<#if test.region?has_content><@spring.message "${test.region}"/></#if>'> <#if test.region?has_content><@spring.message "${test.region}"/></#if> 
								</td>
								</#if>
								<td>
									<#if test.startTime?exists>${test.startTime?string('yyyy-MM-dd HH:mm')}</#if>
								</td>
								<td	<#if test.threshold == "D">	>${(test.durationStr)!}<#else> title="<@spring.message "perfTest.configuration.runCount"/>" >${test.runCount}</#if>
								</td>
								<td><#if test.tps??>${(test.tps)?string("0.#")}</#if></td>  
								<td><#if test.meanTestTime??>${(test.meanTestTime)?string("0.##")}</#if></td>
								<td><#if test.errors??>${test.errors}</#if></td>
								<td>${vuserTotal}</td>
								<td class="center">
									<a href="javascript:void(0)" style="<#if deletable>display: none;</#if>"><i title="<@spring.message "common.button.delete"/>"id="delete_${test.id}" class="icon-remove test-remove" sid="${test.id}"></i></a>
									<a href="javascript:void(0)" style="<#if stoppable>display: none;</#if>"><i title="<@spring.message "common.button.stop"/>" id="stop_${test.id}" class="icon-stop test-stop" sid="${test.id}"></i></a>
								</td>  
							</tr>  
						</#list> 
					<#else>
						<tr>
							<td colspan="13" class="center">
								<@spring.message "common.message.noData"/>
							</td>
						</tr>
					</#if>
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
			<#include "../common/copyright.ftl">
		</div>
		<!--container-->
	</div>
</div>
	<script>
		$(document).ready(function() {
			$("#tag").select2({
				placeholder: '<@spring.message "perfTest.table.selectATag"/>',
				allowClear: true
			}).change(function() {
				document.forms.listForm.submit();
			});
			
			$('td.ellipsis').hover(function () {
	          $(this).popover('show');
	      	});
	      	
			$("#nav_test").addClass("active");
			
			enableChkboxSelectAll("testTable");
			
			$("#deleteBtn").click(function() {
				var list = $("td input:checked");
				if(list.length == 0) {
					bootbox.alert("<@spring.message "perfTest.table.message.alert.delete"/>", "<@spring.message "common.button.ok"/>");
					return;
				}
				
				bootbox.confirm("<@spring.message "perfTest.table.message.confirm.delete"/>", "<@spring.message "common.button.cancel"/>", "<@spring.message "common.button.ok"/>", function(result) {
				    if (result) {
				    	var ids = list.map(function() {
							return $(this).val();
						}).get().join(",");
						
						deleteTests(ids);
				    }
				});
			});
			
			$("i.test-remove").click(function() {
				var id = $(this).attr("sid");
				bootbox.confirm("<@spring.message "perfTest.table.message.confirm.delete"/>", "<@spring.message "common.button.cancel"/>", "<@spring.message "common.button.ok"/>", function(result) {
				    if (result) {
				    	deleteTests(id);
				    }
				});
			});
			
			$("i.test-stop").click(function() {
				var id = $(this).attr("sid");
				bootbox.confirm("<@spring.message "perfTest.table.message.confirm.stop"/>", "<@spring.message "common.button.cancel"/>", "<@spring.message "common.button.ok"/>", function(result) {
					if (result) {
						stopTests(id);
					}
				});
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

			$("th.sorting").click(function() {
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
			
			$("#currentRunning").click(function() {
				$("#currentRunningDiv").toggle();
			});
			
			$("#finishedChk, #scheduledChk").click(function() {
				var $this = $(this);
				var $temp;
				if ($this.attr("id") == "finishedChk") {
					checkboxReject($this, $("#scheduledChk"));
				} else {
					checkboxReject($this, $("#finishedChk"));
				}
				
				document.forms.listForm.submit();
			});
		});
		
		function checkboxReject(obj1, obj2) {
			if (obj1.attr("checked") == "checked" && obj2.attr("checked") == "checked") {
				obj2.attr("checked", false);
			}
		}
		
		function deleteTests(ids) {
			$.ajax({
		  		url: "${req.getContextPath()}/perftest/deleteTests",
		  		type: "POST",
		  		data: {"ids" : ids},
				dataType:'json',
		    	success: function(res) {
		    		if (res.success) {
			    		showSuccessMsg("<@spring.message "perfTest.table.message.success.delete"/>");
							setTimeout(function() {
								getList(1);
							}, 500);
		    		} else {
			    		showErrorMsg("<@spring.message "perfTest.table.message.error.delete"/>:" + res.message);
		    		}
		    	},
		    	error: function() {
		    		showErrorMsg("<@spring.message "perfTest.table.message.error.delete"/>!");
		    	}
		  	});
		}
		
		function stopTests(ids) {
			$.ajax({
		  		url: "${req.getContextPath()}/perftest/stopTests",
				type: "POST",
		  		data: {"ids":ids},
				dataType:'json',
		    	success: function(res) {
		    		if (res.success) {
			    		showSuccessMsg("<@spring.message "perfTest.table.message.success.stop"/>");
		    		} else {
			    		showErrorMsg("<@spring.message "perfTest.table.message.error.stop"/>:" + res.message);
		    		}
		    	},
		    	error: function() {
		    		showErrorMsg("<@spring.message "perfTest.table.message.error.stop"/>!");
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
		
		function updateStatus(id, status, icon, stoppable, deletable, message) {
			var ballImg = $("#ball_" + id + " img");
			if (ballImg.attr("src") != "${req.getContextPath()}/img/ball/" + icon) { 
				ballImg.attr("src", "${req.getContextPath()}/img/ball/" + icon);
				$(".icon-remove[sid=" + id + "]").remove();
			}
			$("#ball_" + id).attr("data-original-title", status);
			$("#ball_" + id).attr("data-content", message);
			if (stoppable == true) {
				$("#stop_" + id).parent().show();
			} else {
				$("#stop_" + id).parent().hide();
			}
			if (deletable == true) {
				$("#delete_" + id).parent().show();
			} else { 
				$("#check_" + id).attr("disabled", true);
				$("#delete_" + id).parent().hide(); 
			}
		}
		// Wrap this function in a closure so we don't pollute the namespace
		(function refreshContent() {
			var ids = $('input.perf_test').map(function() {
		    	var perTestStatus = $(this).attr("status")
				if(!(perTestStatus == "FINISHED" || perTestStatus == "STOP_ON_ERROR" || perTestStatus == "CANCELED"))
					return this.value;
		  	}).get();
		    $.ajax({
			    url: '${req.getContextPath()}/perftest/updateStatus', 
			    type: 'POST',
			    cache: false,
			    data: {"ids": ids.join(",")},
			    success: function(perfTestData) {
			    	perfTestData = eval(perfTestData); 
			    	data = perfTestData.statusList
			    	var perfTest = perfTestData.perfTestInfo;
			    	var springMessage = perfTest.length + " <@spring.message "perfTest.currentRunning.summary"/>";
			    	
			    	$("#currentRunning").text(springMessage);
			    	var testStatus;
			    	for (var i = 0; i < data.length; i++) { 
			    		testStatus = data[i].status_id;
			    	    $("#check_" + data[i].id).attr("status", testStatus);
			    	    
			    		if(testStatus == "FINISHED" || testStatus == "STOP_ON_ERROR" || testStatus == "CANCELED"){
			    			location.reload();
			    		}
			    		updateStatus(data[i].id, data[i].name, data[i].icon, data[i].stoppable, data[i].deletable, data[i].message);
			    	}
			    	setTimeout(refreshContent, 5000);
			    }
		    });
	  })();
	</script>
	</body>
</html>
