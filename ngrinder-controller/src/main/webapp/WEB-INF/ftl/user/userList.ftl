<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<title>nGrinder User List</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="nGrinder User List">
	<meta name="author" content="AlexQin">
	<link rel="shortcut icon" href="favicon.ico" />
	<link href="${req.getContextPath()}/css/bootstrap.min.css" rel="stylesheet">
	<link href="${req.getContextPath()}/plugins/datatables/css/demo_table.css" rel="stylesheet">
	<link href="${req.getContextPath()}/plugins/datatables/css/demo_page.css" rel="stylesheet">
	<link href="${req.getContextPath()}/plugins/datatables/css/demo_table_jui.css" rel="stylesheet">
	<link href="${req.getContextPath()}/css/ngrinder.css" rel="stylesheet">
	<script src="${req.getContextPath()}/js/jquery-1.7.2.min.js"></script>
</head>

<body>
	<#include "../common/navigator.ftl">

	<div class="container">
		<div class="row">
			<div class="span2">
				<a class="btn" href="${req.getContextPath()}/user/detail"
					id="createBtn" data-toggle="modal"> <i class="icon-user"></i>
					Create User
				</a> <#include "userTree.ftl">
			</div>

			<div class="span10">
				<div class="page-header pageHeader">
					<h3>User List</h3>
				</div>
				<div class="row">
					<div class="span10">
						<a href="javascript:deleteCheckedUsers()" class="btn btn-danger pull-right">
							<i class="icon-remove"></i>
							Delete selected Users
						</a>
					</div>
				</div>
				<div class="well form-inline searchBar" style="padding: 5px;">
					<input type="text" class="input-medium search-query"
						placeholder="Keywords" id="searchText" value="${keywords!}"
						style="width: 350px">
					<button type="submit" class="btn" id="search_user">Search</button>
				</div>
				<table class="display ellipsis jsTable" id="userTable">
					<colgroup>
                        <col width="35">
                        <col width="150">
                        <col width="150">
                        <col>
                        <col width="110">
                        <col width="50">
                        <col width="50">
                    </colgroup>
					<thead>
						<tr>
							<th><input type="checkbox" class="checkbox" value=""></th>
							<th>User Name</th>
							<th>Create Date</th>
							<th class="noClick">Description</th>
							<th>Role</th>
							<th class="noClick">Edit</th>
							<th class="noClick">Del</th>
						</tr>
					</thead>
					<tbody>
						<#list userList as user>
						<tr>
							<td><input type="checkbox" id="user_info_check" <#if user.userId == "admin">disabled</#if> value="${user.userId}" /></td>
							<td class="left"><a
										href="${req.getContextPath()}/user/detail?userId=${user.userId}">${user.userName!}</a></td>
							<td>
								${user.createdDate?string("yyyy/MM/dd HH:mm:ss")}</td>
							<td class="left ellipsis">${user.description!}</td>
							<td class="left">${user.role}</td>
							<td><a
								href="${req.getContextPath()}/user/detail?userId=${user.userId}"><i
									class="icon-edit"></i></a></td>
							<td><a
								href="${req.getContextPath()}/user/delete?userIds=${user.userId}"><i
									class="icon-remove"></i></a></td>
						</tr>
						</#list>
					</tbody>
				</table>
			</div>
		</div>
		<#include "../common/copyright.ftl">
	</div>

	<script src="${req.getContextPath()}/js/bootstrap.min.js"></script>
	<script src="${req.getContextPath()}/plugins/datatables/js/jquery.dataTables.min.js"></script>
	<script src="${req.getContextPath()}/js/utils.js"></script>
	<script type="text/javascript">
		$(document).ready(function(){
			$("#search_user").on('click', function() {
				document.location.href = "${req.getContextPath()}/user/list?keywords=" + $("#searchText").val() ;
			});
			
			enableChkboxSelectAll();
			
		    <#if userList?has_content>
			oTable = $("#userTable").dataTable({
				"bAutoWidth": false,
				"bFilter": false,
				"bLengthChange": false,
				"bInfo": false,
				"iDisplayLength": 15,
				"aaSorting": [[1, "asc"]],
				"bProcessing": true,
				"aoColumns": [{ "asSorting": []}, null, null, { "asSorting": []}, null, { "asSorting": []}, { "asSorting": []}],
				//"bJQueryUI": true,
				"sPaginationType": "full_numbers"
			});
			
			removeClick();
			</#if>
		});
	
		function deleteCheckedUsers() {
			var list = $("input[id='user_info_check']:checked");
			if(list.length == 0) {
				alert("Please select at least 1 user.");
				return;
			}
			var checkedUser = [];
			var $elem;
			list.each(function() {
				$elem = $(this);
				checkedUser.push($elem.val());
			});
			
			var ids = checkedUser.join(",");
			
			if (!confirm("Do you want to delete user: "+ ids +" ?")) {
				return;
			}
			
			document.location.href="${req.getContextPath()}/user/delete?userIds=" + ids;		
		}
	</script>
</body>
</html>