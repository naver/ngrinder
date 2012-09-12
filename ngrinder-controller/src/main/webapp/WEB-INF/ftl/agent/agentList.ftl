<!DOCTYPE html>
<html>
<head>
<title>nGrinder Agent List</title>
<#include "../common/common.ftl"> 
<#include "../common/datatables.ftl">
</head>
<body>
	<#include "../common/navigator.ftl">
	<div class="container">
		<div class="page-header pageHeader" style="margin-bottom: 10px">
			<h3><@spring.message "agent.management.title"/></h3>
		</div>
		<div class="well searchBar">
			<input type="text" style="visibility:hidden; margin:0">
			<div class="input-prepend pull-right">
				<span class="add-on" style="cursor:default">Agent Download
				</span><span class="input-xlarge uneditable-input span6" style="cursor:text">
					<#if downloadLinks?has_content>
						<#list downloadLinks as each>  
							<div><a href="${each}">${each}</a></div>
						</#list>  
					</#if>  
				</span> 
			</div>
		</div>
		
		<table class="table table-striped table-bordered" id="agentTable">
			<colgroup>
				<col width="30">
				<col width="150">
				<col width="100">
				<col width="*">
				<col width="150">
				<col width="100">
				<col width="160">   
			</colgroup>
			<thead>
				<tr>
					<th class="noClick"><input type="checkbox" class="checkbox" value=""></th>
					<th><@spring.message "agent.table.IPAndDns"/></th>
					<th class="noClick"><@spring.message "agent.table.port"/></th>
					<th><@spring.message "agent.table.name"/></th>
					<th><@spring.message "agent.table.region"/></th>
					<th><@spring.message "common.label.status"/></th>
					<th class="noClick"><@spring.message "agent.table.approve"/></th>
				</tr>
			</thead>
			<tbody>
				<#if agents?has_content>
				<#list agents as agent>
				<tr>
					<td class="center"><input type="checkbox" value="${agent.id}"></td>
					<td>
						<a href="${req.getContextPath()}/agent/detail?id=${agent.id}" target="_self">${agent.ip}</a>
					</td>
					<td>${(agent.port)!}</td>
					<td>${(agent.hostName)!}</td>
					<td>${(agent.region)!}</td>
					<td>${(agent.status)!}</td>
					<td>
						<div class="btn-group" data-toggle="buttons-radio">
							<button type="button" class="btn btn-mini btn-primary unapproved <#if agent.isApproved() == false>active</#if>" sid="${agent.ip}"><@spring.message "agent.table.unapproved"/> </button>
							<button type="button" class="btn btn-mini btn-primary approved <#if agent.isApproved() == true>active</#if>" sid="${agent.ip}"><@spring.message "agent.table.approved"/> </button>
						</div>
					</td> 
					
				</tr> 
				</#list>
				<#else>
				<tr>
					<td colspan="7" class="center"><@spring.message "common.message.noData"/></td>
				</tr>
				</#if>
			</tbody>
		</table>
		<#include "../common/copyright.ftl">
		<!--content-->
	</div>
		<script>
        <#if agents?has_content>
            $(document).ready(function() {
				oTable = $("#agentTable").dataTable({
					"bAutoWidth": false,
					"bFilter": false,
					"bLengthChange": false,
					"bInfo": false,
					"iDisplayLength": 15,
					"aaSorting": [[1, "asc"]],
					"aoColumns": [{"asSorting": []}, null, {"asSorting": []}, null, null, null, {"asSorting": []}],
					"sPaginationType": "bootstrap",
					"oLanguage": {
						"oPaginate": {
							"sPrevious": "<@spring.message "common.paging.previous"/>",
							"sNext":     "<@spring.message "common.paging.next"/>"
						}
					}
				});
				
				removeClick();
				
				$(".approved").click(function() {
					var sid = $(this).attr("sid");
					$.post(
				  		"${req.getContextPath()}/agent/approve",
				  		{ 
				  			"ip": sid,
				  			"approve": "true"
				  		},
				  		function() {
				  			showSuccessMsg("Agent is approved");
				  			window.location.reload();
				  		}
				     );
				});
				
				$(".unapproved").click(function() {
					var sid = $(this).attr("sid");
					$.post(
				  		"${req.getContextPath()}/agent/approve",
				  		{ 
				  			"ip": sid,
				  			"approve": "false"
				  		},
				  		function() {
				  			showSuccessMsg("Agent is unapproved");
				  			window.location.reload();
				  		}
				     );					
				});
            });
		</#if>
     </script>
</body>
</html>
