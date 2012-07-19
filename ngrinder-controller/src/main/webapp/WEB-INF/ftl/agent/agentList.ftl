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
				<h3>Agent Management</h3>
			</div>
            <div class="row">
                <div class="span12">
                    <div class="well form-inline searchBar">
                        <input type="text" class="search-query" placeholder="Keywords" id="searchText" value="${keywords!}">
                        <button type="submit" class="btn" id="searchBtn"><i class="icon-search"></i>Search</button>
                    </div>
	                <table class="display ellipsis jsTable" id="agentTable">
	                    <colgroup>
	                        <col width="35">
	                        <col width="180">
	                        <col width="100">
	                        <col>
	                        <col width="180">
	                        <col width="100">
	                        <col width="50">
	                    </colgroup>
	                    <thead>
	                        <tr>
	                            <th><input type="checkbox" class="checkbox" value=""></th>
	                            <th>IP | Domain</th>
	                            <th class="noClick">Port</th>
	                            <th>Name</th>
	                            <th>Region</th>
	                            <th>Status</th>
	                            <th class="noClick">Del</th>
	                        </tr>
	                    </thead>
	                    <tbody>
	                        <#assign agentList = agents.content/>
	                        <#if agentList?has_content>
	                        <#list agentList as agent>
	                        <tr>
	                            <td><input type="checkbox" value="${agent.id}"></td>
	                            <td class="left"><a href="${req.getContextPath()}/agent/detail?id=${agent.id}" target="_self">${agent.ip}</a></td>
	                            <td>${(agent.appPort)!}</td>
	                            <td class="left">${(agent.appName)!}</td>
	                            <td class="left">${(agent.region)!}</td>
	                            <td>${(agent.status)!}</td>
	                            <td>
	                                <a href="${req.getContextPath()}/agent/delete?ids=${agent.id}" title="Delete this agent"><i class="icon-remove"></i></a>
	                            </td>
	                        </tr>
	                        </#list>
	                        <#else>
	                            <tr>
	                                <td colspan="7">
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
					//"bJQueryUI": true,
					"sPaginationType": "full_numbers"
				});
				
				removeClick();
				</#if>
            });
        </script>
    </body>
</html>