<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title>nGrinder Script List</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta name="description" content="nGrinder Test Result Detail">
		<meta name="author" content="AlexQin">

		<link rel="shortcut icon" href="favicon.ico"/>
		<link href="${req.getContextPath()}/css/bootstrap.min.css" rel="stylesheet">
		<style>
			body {
				padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
			}
		</style>
		
		<input type="hidden" id="contextPath" value="${req.getContextPath()}">
		<#setting number_format="computer">
	</head>

	<body>
    	<#include "../common/navigator.ftl">
		<div class="container">
			<div class="well form-inline" style="padding:5px;margin:10px 0">
				<input type="text" class="search-query" placeholder="Keywords" id="searchText" value="${keywords!}">
				<button type="submit" class="btn" id="searchBtn">Search</button>
				<label class="checkbox pull-right">
					<input type="checkbox" id="onlyFinished" <#if isFinished??&&isFinished>checked</#if>> Finished
				</label>
			</div>
			<table class="display ellipsis" id="scriptTable" style="margin-bottom:10px;">
				<colgroup>
					<col width="30">
					<col width="160">
					<col width="160">
					<col>
					<col width="170">
					<col width="100">
					<col width="100">
					<col width="100">
					<col width="100">
					<col width="100">
					<col width="80">
				</colgroup>
				<thead>
					<tr>
						<th class="center">ID</th>
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
						<td>${test.id}</td>
						<td class="left"><a href="${req.getContextPath()}/perftest/detail?id=${test.id}" target="_self">${test.testName}</a></td>
						<td>${test.scriptName}</td>
						<td class="left ellipsis" title="${(test.description)!}">${(test.description)!}</td>
						<td><#if test.startTime?exists>${test.startTime?string('yyyy-MM-dd HH:mm:ss')}</#if></td>
						<td>${(test.tps)!}</td>
						<td>${(test.meanTime)!0}</td>
						<td>${(test.errors)!0}</td>
						<td>${(test.vusers)!0}</td>
						<td>${(test.duration)!0}</td>
						<td><a href="javascript:void(0);"><i class="icon-remove script-remove" sid="${test.id}"></i></a></td>
					</tr>
					</#list>
					<#else>
						<tr>
							<td colspan="8">
								No data to display.
							</td>
						</tr>
					</#if>
				</tbody>
			</table>
		<!--content-->
		<!-- <#include "../common/copyright.ftl"> -->
		</div>
		<script src="${req.getContextPath()}/js/jquery-1.7.2.min.js"></script>
		<script src="${req.getContextPath()}/js/bootstrap.min.js"></script>
		<script src="${req.getContextPath()}/js/utils.js"></script>
		<script>
			$(document).ready(function() {
				
			});
		</script>
	</body>
</html>