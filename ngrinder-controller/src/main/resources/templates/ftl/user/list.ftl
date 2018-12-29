<!DOCTYPE html>
<html>
<head>
	<#include "../common/common.ftl"> 
	<#include "../common/datatables.ftl">
	<title><@spring.message "user.list.title"/></title>
</head>

<body>
<div id="wrap">
	<#include "../common/navigator.ftl">
	<div class="container">
		<fieldSet>
			<legend class="header">
				<@spring.message "navigator.dropDown.userManagement"/>
				<select id="roles" class="pull-right" name="roles">
					<option value="all" <#if listPage?? && !role??>selected</#if> >
						<@spring.message "user.left.all"/>
					</option>
					<#list roleSet as each>
						<option value="${each}" <#if role?? && role == each>selected</#if>>${each.fullName}</option>
					</#list>
				</select>
			</legend> 
		</fieldSet>
		<!--suppress HtmlUnknownTarget -->
		<form id="user_list_form" action="${req.getContextPath()}/user" method="GET">
			<div class="well form-inline search-bar">			 
				<input type="text" class="search-query search-query-without-radios" placeholder="Keywords" id="search_text" name="keywords" value="${keywords!}">
				<a class="btn" id="search_user">
					<i class="icon-search"></i> <@spring.message "common.button.search"/>
				</a>
				<span class="pull-right">
					<a class="btn" href="${req.getContextPath()}/user/new">
						<i class="icon-user"></i> <@spring.message "user.list.button.create"/>
					</a>
					<a href="javascript:deleteCheckedUsers()" class="btn btn-danger">
						<i class="icon-remove icon-white"></i> <@spring.message "user.list.button.delete"/>
					</a>
				</span>
			</div>
			<input type="hidden" id="page_number" name="page.page" value="${page.pageNumber}">
			<input type="hidden" id="page_size" name="page.size" value="${page.pageSize}">
			<input type="hidden" id="sort" name="sort" value="${sort!'lastModifiedDate,DESC'}">

		</form>
		<table class="table table-striped table-bordered ellipsis" id="user_table">
			<#assign userList = users.content/>
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
					<th class="no-click nothing"><input type="checkbox" class="checkbox" value=""></th>
					<th name="userName"><@spring.message "user.info.name"/></th>
					<th class="no-click nothing"><@spring.message "user.info.role"/></th>
					<th name="email"><@spring.message "user.info.email"/></th>
					<th class="no-click nothing"><@spring.message "common.label.description"/></th>
					<th name="createdDate"><@spring.message "user.list.table.date"/></th>
					<th class="no-click nothing"><@spring.message "user.list.table.edit"/></th>
					<th class="no-click nothing"><@spring.message "user.list.table.delete"/></th>
				</tr>
			</thead>
			<tbody>

				<@list list_items=userList others="table_list" colspan="8"; user>
				<tr>
					<td class="center">
						<input type="checkbox" class="checkbox" id="user_info_check"
							<#if user.userId == "admin">disabled</#if>
							value="${user.userId}" uname="${user.userName}"/>
					</td>
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
				</@list>
			</tbody>
		</table>
		<#if userList?has_content>
			<#include "../common/paging.ftl">
			<@paging  users.totalElements users.number+1 users.size 10 ""/>
			<script type="text/javascript">
				function doSubmit(page) {
					getList(Math.max(page - 1, 0));
				}
			</script>
		</#if>
	</div>
</div>
<#include "../common/copyright.ftl">

	<script type="text/javascript">
		function getList(page) {
			if (page !== undefined) {
				$("#page_number").val(page);
			}
			document.forms.user_list_form.submit();
		}

		$(document).ready(function(){
			$("#search_user").click(function() {
				$("#user_list_form").submit();
			});

			$("#roles").change(function() {
				var selectedValue = $(this).val();
				var destUrl = "${req.getContextPath()}/user/";
				if (selectedValue != "all") {
					destUrl = destUrl + "?role=" + selectedValue;
				}
				window.location.href=destUrl;
			});
			removeClick();
			enableCheckboxSelectAll("user_table");
			$("th").each(function() {
				var $this = $(this);
				if (!$this.hasClass("nothing")) {
					$this.addClass("sorting");
				}
			});
			var sort = $("#sort").val().split(",");
			var sortColumn = sort[0];
			var sortDir = sort[1];

			$("th[name='" + sortColumn + "']").addClass("sorting_" + sortDir.toLowerCase());

			$("th.sorting").click(function() {
				var $currObj = $(this);
				var sortDirection = "ASC";
				if ($currObj.hasClass("sorting_asc")) {
					sortDirection = "DESC";
				}

				$("#sort").val($currObj.attr('name') + "," + sortDirection);
				getList(0);
			});

		});

		function deleteCheckedUsers() {
			var list = $("input[id='user_info_check']:checked");
			if(list.length == 0) {
				bootbox.alert('<@spring.message "user.list.alert.delete"/>', '<@spring.message "common.button.ok"/>');
				return;
			}
			var checkedUserName = [];
			var checkedUserId = [];
			list.each(function() {
				var $elem = $(this);
				checkedUserName.push($elem.attr("uname"));
				checkedUserId.push($elem.val());
			});

			deleteUsers(checkedUserId.join(","), checkedUserName.join(", "));	
		}

		function deleteUsers(ids, names) {
			bootbox.confirm('<@spring.message "user.list.confirm.delete"/> ' + names + '?', '<@spring.message "common.button.cancel"/>', '<@spring.message "common.button.ok"/>', function(result) {
				if (result) {
					document.location.href="${req.getContextPath()}/user/delete?userIds=" + ids;
				}
			});
		}
	</script>
</body>
</html>
