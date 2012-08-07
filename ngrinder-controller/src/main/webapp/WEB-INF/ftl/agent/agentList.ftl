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
        	<div class="page-header pageHeader" style="margin-bottom: 20px">
				<h3>Agent Management</h3>
			</div>
            <div class="row">
                <div class="span12">
	                <table class="table table-striped table-bordered" id="agentTable">
	                    <colgroup>
	                        <col width="130">
	                        <col width="200">
	                        <col>
	                        <col width="180">
	                        <col width="130">
	                    </colgroup>
	                    <thead>
	                        <tr>
	                            <th class="noClick">Number</th>
	                            <th>IP | Domain</th>
	                            <th>Name</th>
	                            <th>Region</th>
	                            <th>Status</th>
	                        </tr>
	                    </thead>
	                    <tbody>
	                        <#if agents?has_content>
	                        <#list agents as agent>
	                        <tr>
	                            <td>${(agent.number)!}</td>
	                            <td class="left"><a href="${req.getContextPath()}/agent/detail?id=${agent.id}" target="_self">${agent.ip}</a></td>
	                            <td class="left">${(agent.appName)!}</td>
	                            <td class="left">${(agent.region)!}</td>
	                            <td>${(agent.status)!}</td>
	                        </tr>
	                        </#list>
	                        <#else>
	                            <tr>
	                                <td colspan="5" class="noData">
	                                    No data to display.
	                                </td>
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