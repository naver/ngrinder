<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<title>nGrinder User List</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="nGrinder Test Result Detail">
	<meta name="author" content="AlexQin">
	<link rel="shortcut icon" href="favicon.ico" />
	<link href="${req.getContextPath()}/css/bootstrap.min.css" rel="stylesheet">
	<style>
		body {
			padding-top: 60px;
			/* 60px to make the container go all the way to the bottom of the topbar */
		}
		
		.table th,.table td {
			text-align: center;
		}
		
		table.display thead th {
			padding: 3px 10px
		}
		
		table.display tbody .left {
			text-align: left
		}
	</style>
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
				<div class="well form-inline" style="padding: 5px;">
					<!--<legend>introduction</legend>-->
					<input type="text" class="input-medium search-query"
						placeholder="Keywords" id="searchText" value="${keywords!}"
						style="width: 350px">
					<button type="submit" class="btn" id="searchBtn">Search</button>
				</div>

				<table class="table table-striped display" id="userTable">
					<thead>
						<tr>
							<th class="center"><input type="checkbox"
								class="checkbox noClick" value=""></th>
							<th>User Name</th>
							<th>Create Date</th>
							<th>Description</th>
							<th>Role</th>
							<th class="noClick">Edit</th>
							<th class="noClick">Del</th>
						</tr>
					</thead>
					<tbody>
						<#list userList as user>
						<tr>
							<td><input type="checkbox" id="user_info_check" <#if user.userId == "admin">disabled</#if> value="${user.userId}" /></td>
							<td class="center"><a
								href="${req.getContextPath()}/user/detail?userIds=${user.userId}">${user.userName}</a></td>
							<td>
								${user.createdDate!user.createdDate?string("yyyy/MM/dd
								hh:mm:ss")}</td>
							<td>${user.description!}</td>
							<td>${user.role}</td>
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
		<div class="row">
			<div class="span12">
				<a href="javascript:deleteCheckedUsers()" class="btn btn-danger pull-right">Delete</a>
			</div>
		</div>

		<#include "../common/copyright.ftl">
	</div>

	<script src="${req.getContextPath()}/js/bootstrap.min.js"></script>
	<script type="text/javascript">
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