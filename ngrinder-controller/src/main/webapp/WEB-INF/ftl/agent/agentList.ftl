<!DOCTYPE html>
<html>
<head>
<title>nGrinder Agent List</title> <#include "../common/common.ftl"> <#include "../common/datatables.ftl">
</head>

<body>
	<#include "../common/navigator.ftl">
	<div class="container">
		<div class="page-header pageHeader" style="margin-bottom: 10px">
			<h3>Agent Management</h3>
		</div>
		<div class="row">
			<div class="span12">

				<table class="table table-striped table-bordered" id="agentTable">
					<colgroup>
						<col width="30">
						<col width="180">
						<col width="100">
						<col>
						<col width="180">
						<col width="80">
						<col width="40">
					</colgroup>
					<thead>
						<tr>
							<th><input type="checkbox" class="checkbox" value=""></th>
							<th>IP | Domain</th>
							<th class="noClick">Port</th>
							<th>Name</th>
							<th>Region</th>
							<th>Status</th>
						</tr>
					</thead>
					<tbody>
						<#if agents?has_content> <#list agents as agent>
						<tr>
							<td><input type="checkbox" value="${agent.id}"></td>
							<td class="left"><a href="${req.getContextPath()}/agent/detail?id=${agent.id}" target="_self">${agent.ip}</a>
								<a href="${req.getContextPath()}/agent/delete?ids=${agent.id}" title="Delete this agent">
								<iclass="icon-remove"></i></a></td>
							<td>${(agent.port)!}</td>
							<td class="left">${(agent.hostName)!}</td>
							<td class="left">${(agent.region)!}</td>
							<td>${(agent.status)!}</td>
						</tr>
						</#list> <#else>
						<tr>
							<td colspan="7" class="noData">No data to display.</td>
						</tr>
						</#if>
					</tbody>
				</table>
			</div>
		</div>
		<#include "../common/copyright.ftl">
		<!--content-->
	</div>
	<script>
            $(document).ready(function() {
                $("#connectBtn").on('click', function() {
                    var ids = "";
                    var list = $("td input:checked");
                    if(list.length == 0) {
                        alert("Please select any agents first.");
                        return;
                    }
                    var agentArray = [];
                    list.each(function() {
                        agentArray.push($(this).val());
                    });
                    ids = agentArray.join(",");
                    document.location.href = "${req.getContextPath()}/agent/connect?ids=" + ids + "&isConnect=true";
                });
                $("#disconnectBtn").on('click', function() {
                    var ids = "";
                    var list = $("td input:checked");
                    if(list.length == 0) {
                        alert("Please select any agents first.");
                        return;
                    }
                    var agentArray = [];
                    list.each(function() {
                        agentArray.push($(this).val());
                    });
                    ids = agentArray.join(",");
                    document.location.href = "${req.getContextPath()}/agent/connect?ids=" + ids + "&isConnect=false";
                });
                $("#searchBtn").on('click', function() {
                    var keywords =  $("#searchText").val();
                    document.location.href = "${req.getContextPath()}/agent/list?keywords=" + keywords;
                });
                
                enableChkboxSelectAll();
                
                <#if agentList?has_content>
				oTable = $("#agentTable").dataTable({
					"bAutoWidth": false,
					"bFilter": false,
					"bLengthChange": false,
					"bInfo": false,
					"iDisplayLength": 15,
					"aaSorting": [[1, "asc"]],
					"bProcessing": true,
					"aoColumns": [{ "asSorting": []}, null, { "asSorting": []}, null, null, null, { "asSorting": []}],
					"sPaginationType": "bootstrap"
				});
				
				removeClick();
				</#if>
            });
        </script>
</body>
</html>