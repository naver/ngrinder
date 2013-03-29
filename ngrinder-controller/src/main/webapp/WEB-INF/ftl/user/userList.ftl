<!DOCTYPE html>
<html>
<head>
	<#include "../common/common.ftl"> 
	<#include "../common/datatables.ftl">
	<title><@spring.message "user.list.title"/></title>
</head>

<body>
	<#include "../common/navigator.ftl">
	<div class="container">
		<fieldSet>
			<legend class="header">
				<@spring.message "navigator.dropdown.userManagement"/>
				<select id="roles" class="pull-right" name="roles">
					<option value="all" <#if listPage?exists && !roleName?exists>selected</#if>"><@spring.message "user.left.all"/></option>
					<#list roleSet as role> 
						<option value="${role.fullName}" <#if roleName?exists && role.fullName == roleName>selected</#if>>${role.fullName}</option>
					</#list>
				</select>
			</legend> 
		</fieldSet>
		<form id="userListForm" action="${req.getContextPath()}/user" method="POST">
			<div class="well form-inline searchBar">
			 
				<input type="text" class="search-query search-query-without-radios" placeholder="Keywords" id="searchText" name="keywords"
					value="${keywords!}">
				<a class="btn" id="search_user">
					<i class="icon-search"></i> <@spring.message "common.button.search"/>
				</a>
				<span class="pull-right">
					<a class="btn" href="${req.getContextPath()}/user/new" id="createBtn" data-toggle="modal">
						<i class="icon-user"></i> <@spring.message "user.list.button.create"/>
					</a>
					<a href="javascript:deleteCheckedUsers()" class="btn btn-danger">
						<i class="icon-remove icon-white"></i> <@spring.message "user.list.button.delete"/>
					</a>
				</span>
			</div>
		</form>
		<table class="table table-striped table-bordered ellipsis" id="userTable">
			<colgroup>
				<col width="30">
				<col width="120">
				<col width="120">
				<col width="160"> 
				<col>
				<col width="120">
				<col width="45">
				<col width="45">
			</colgroup>
			<thead>
				<tr>
					<th class="noClick"><input type="checkbox" class="checkbox" value=""></th>
					<th><@spring.message "user.option.name"/></th>
					<th><@spring.message "user.option.role"/></th>
					<th><@spring.message "user.option.email"/></th>
					<th class="noClick"><@spring.message "common.label.description"/></th>
					<th><@spring.message "user.list.table.date"/></th>
					<th class="noClick"><@spring.message "user.list.table.edit"/></th>
					<th class="noClick"><@spring.message "user.list.table.delete"/></th>
				</tr>
			</thead>
			<tbody>
				<#list userList as user>
				<tr>
					<td class="center"><input type="checkbox" id="user_info_check"<#if user.userId == "admin">disabled</#if>
						value="${user.userId}" uname="${user.userName}"/></td>
					<td class="ellipsis"><a href="${req.getContextPath()}/user/${user.userId}">${user.userName}</a></td>
					<td title="${user.role.fullName}">${user.role.fullName}</td>
					<td class="ellipsis">${user.email!""}</td>
					<td class="ellipsis">${user.description!}</td>
					<td><#if user.createdDate?has_content> ${user.createdDate?string("yyyy-MM-dd HH:mm")} </#if></td>
					<td class="center">
						<a href="${req.getContextPath()}/user/${user.userId}">
							<i class="icon-edit"></i>
						</a>
					</td>
					<td class="center">
						<#if user.userId != "admin">
						<a href="javascript:deleteUsers('${user.userId}', '${user.userName}');">
							<i class="icon-remove"></i>
						</a>
						</#if>
					</td>
				</tr>
				</#list>
			</tbody>
		</table>
		<#include "../common/copyright.ftl">
	</div>

	<script type="text/javascript">
		$(document).ready(function(){
			$("#search_user").click(function() {
				$("#userListForm").submit();
			});
			
			$("#roles").change(function() {
				var selectedValue = $(this).val();
				var destUrl = "${req.getContextPath()}/user/";
				if (selectedValue != "all") {
					destUrl = destUrl + "?roleName=" + selectedValue;
				}
				window.location.href=destUrl;
			});
			
		    <#if userList?has_content>
			oTable = $("#userTable").dataTable({
				"bAutoWidth": false,
				"bFilter": false,
				"bLengthChange": false,
				"bInfo": false,
				"iDisplayLength": 10,
				"aaSorting": [[1, "asc"]],
				"aoColumns": [{"asSorting": []}, null, null, null, {"asSorting": []}, null, {"asSorting": []}, {"asSorting": []}],
				"sPaginationType": "bootstrap",
				"oLanguage": {
					"oPaginate": {
						"sPrevious": "<@spring.message "common.paging.previous"/>",
						"sNext":     "<@spring.message "common.paging.next"/>"
					}
				}
			});
			
			removeClick();
			
			enableChkboxSelectAll("userTable");
			</#if>
		});
	
		function deleteCheckedUsers() {
			var list = $("input[id='user_info_check']:checked");
			if(list.length == 0) {
				bootbox.alert("<@spring.message "user.list.alert.delete"/>", "<@spring.message "common.button.ok"/>");
				return;
			}
			var checkedUserNm = [];
			var checkedUserId = [];
			var $elem;
			list.each(function() {
				$elem = $(this);
				checkedUserNm.push($elem.attr("uname"));
				checkedUserId.push($elem.val());
			});
			
			deleteUsers(checkedUserId.join(","), checkedUserNm.join(", "));	
		}
		
		function deleteUsers(ids, names) {
			bootbox.confirm("<@spring.message "user.list.confirm.delete"/> " + names + "?", "<@spring.message "common.button.cancel"/>", "<@spring.message "common.button.ok"/>", function(result) {
				if (result) {
					document.location.href="${req.getContextPath()}/user/delete?userIds=" + ids;
				}
			});
		}
	</script>
</body>
</html>
