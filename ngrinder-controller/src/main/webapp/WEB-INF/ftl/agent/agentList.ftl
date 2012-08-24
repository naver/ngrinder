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
			<span>
			<h3><@spring.message "agent.managerment.title"/></h3>
			</span>
			
		</div>
		<div class="row">
			<div class="span12">

				<div class="input-prepend pull-right">
		              <span class="add-on" style="cursor:default">Agent Download</span><span class="input-xlarge uneditable-input span6" style="cursor:text">
		              		<#if downloadLinks?has_content>
								<#list downloadLinks as each>  
				           			<div><a href="${each}">${each}</a></div>
			           			</#list>  
		           			</#if>  
		            		</span> 
		              </span>
		        </div>  

				<table class="table table-striped table-bordered" id="agentTable">
					<colgroup>
						<col width="30">
						<col width="180">
						<col width="100">
						<col>
						<col width="180">
						<col width="80">
					</colgroup>
					<thead>
						<tr>
							<th><input type="checkbox" class="checkbox" value=""></th>
							<th><@spring.message "agent.table.IPAndDns"/></th>
							<th class="noClick"><@spring.message "agent.table.port"/></th>
							<th><@spring.message "agent.table.name"/></th>
							<th><@spring.message "agent.table.region"/></th>
							<th><@spring.message "agent.table.status"/></th>
						</tr>
					</thead>
					<tbody>
						<#if agents?has_content>
						<#list agents as agent>
						<tr>
							<td><input type="checkbox" value="${agent.id}"></td>
							<td>
								<a href="${req.getContextPath()}/agent/detail?id=${agent.id}" target="_self">${agent.ip}</a>
							</td>
							<td>${(agent.port)!}</td>
							<td>${(agent.hostName)!}</td>
							<td>${(agent.region)!}</td>
							<td>${(agent.status)!}</td>
						</tr>
						</#list>
						<#else>
						<tr>
							<td colspan="7" class="center"><@spring.message "common.message.noData"/></td>
						</tr>
						</#if>
					</tbody>
				</table>
		</div>
		<#include "../common/copyright.ftl">
		<!--content-->
	</div>
		<script>
            <#if agentList?has_content>
            $(document).ready(function() {
				oTable = $("#agentTable").dataTable({
					"bAutoWidth": false,
					"bFilter": false,
					"bLengthChange": false,
					"bInfo": false,
					"iDisplayLength": 15,
					"aaSorting": [[1, "asc"]],
					"bProcessing": true,
					"aoColumns": [{ "asSorting": []}, null, { "asSorting": []}, null, null, null, { "asSorting": []}],
					"sPaginationType": "bootstrap",
					"oLanguage": {
						"oPaginate": {
							"sPrevious": "<@spring.message "common.paging.previous"/>",
							"sNext":     "<@spring.message "common.paging.next"/>"
						}
					}
				});
				
				removeClick();
            });
			</#if>
        </script>
</body>
</html>
