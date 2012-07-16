<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <title>nGrinder Agent List</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="description" content="nGrinder Agent List">
        <meta name="author" content="Tobi">

        <link rel="shortcut icon" href="favicon.ico"/>
        <link href="${req.getContextPath()}/css/bootstrap.min.css" rel="stylesheet">
        <link href="${req.getContextPath()}/css/ngrinder.css" rel="stylesheet">
        
        <input type="hidden" id="contextPath" value="${req.getContextPath()}">
        <#setting number_format="computer">
    </head>

    <body>
        <#include "../common/navigator.ftl">
        <div class="container">
            <div class="row">
                <div class="span12">
                    <h3>Agent List</h3>
                    <div class="well form-inline" style="padding:5px;margin:10px 0">
                        <input type="text" class="search-query" placeholder="Keywords" id="searchText" value="${keywords!}">
                        <button type="submit" class="btn" id="searchBtn"><i class="icon-search"></i>Search</button>
                    </div>
	                <table class="table" id="scriptTable" style="margin-bottom:10px;">
	                    <colgroup>
	                        <col width="30">
	                        <col>
	                        <col width="150">
	                        <col width="150">
	                        <col width="100">
	                        <col width="80">
	                        <col width="20">
	                    </colgroup>
	                    <thead>
	                        <tr>
	                            <th class="center"><input type="checkbox" class="checkbox" value=""></th>
	                            <th>IP | Domain</th>
	                            <th>Port</th>
	                            <th>Name</th>
	                            <th>Region</th>
	                            <th>Status</th>
	                            <th>Del</th>
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
	                            <td>${(agent.appName)!}</td>
	                            <td>${(agent.region)!}</td>
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
        <script src="${req.getContextPath()}/js/jquery-1.7.2.min.js"></script>
        <script src="${req.getContextPath()}/js/bootstrap.min.js"></script>
        <script src="${req.getContextPath()}/js/utils.js"></script>
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
            });
            
        </script>
    </body>
</html>