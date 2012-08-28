<!DOCTYPE html>
<html>
<head><#include "../common/common.ftl"> <#include "../common/datatables.ftl">
<title><@spring.message "user.list.title"/></title>
</head>

<body>
	<#include "../common/navigator.ftl">

	<div class="container">
		<div class="row">
			<#assign listPage = true> <#include "leftButton.ftl"/>

			<div class="span10">
				<div class="page-header pageHeader">
					<h3><@spring.message "user.list.header"/></h3>
				</div>
				<div class="well form-inline searchBar">
					<input type="text" class="input-medium search-query span4" placeholder="Keywords" id="searchText"
						value="${keywords!}">
					<button type="submit" class="btn" id="search_user">
						<i class="icon-search"></i> <@spring.message "common.button.search"/>
					</button>
					<span class="pull-right"> <a class="btn" href="${req.getContextPath()}/user/detail" id="createBtn"
						data-toggle="modal"> <i class="icon-user"></i> <@spring.message "user.list.button.create"/>
					</a> <a href="javascript:deleteCheckedUsers()" class="btn btn-danger"> <i class="icon-remove"></i> <@spring.message
							"user.list.button.delete"/>
					</a>
					</span>

				</div>
				<table class="table table-striped table-bordered ellipsis" id="userTable">
					<colgroup>
						<col width="30">
						<col width="150">
						<col width="130">
						<col>
						<col width="120">
						<col width="45">
						<col width="45">
					</colgroup>
					<thead>
						<tr>
							<th><input type="checkbox" class="checkbox" value=""></th>
							<th><@spring.message "user.option.name"/></th>
							<th><@spring.message "user.option.role"/></th>
							<th class="noClick"><@spring.message "common.label.description"/></th>
							<th><@spring.message "user.list.table.date"/></th>
							<th class="noClick"><@spring.message "user.list.table.edit"/></th>
							<th class="noClick"><@spring.message "user.list.table.delete"/></th>
						</tr>
					</thead>
					<tbody>
						<#list userList as user>
						<tr>
							<td><input type="checkbox" id="user_info_check"<#if user.userId == "admin">disabled</#if>
								value="${user.userId}" /></td>
							<td><a href="${req.getContextPath()}/user/detail?userId=${user.userId}">${user.userName!}</a></td>
							<td>${user.role}</td>
							<td class="ellipsis">${user.description!}</td>
							<td><#if user.createdDate?has_content> ${user.createdDate?string("yyyy-MM-dd HH:mm")} </#if></td>
							<td><a href="${req.getContextPath()}/user/detail?userId=${user.userId}"> <i class="icon-edit"></i>
							</a></td>
							<td><#if user.userId != "admin"> <a href="javascript:deleteUsers('${user.userId}');"> <i
									class="icon-remove"></i>
							</a> </#if>
							</td>
						</tr>
						</#list>
					</tbody>
				</table>
			</div>
		</div>
		<#include "../common/copyright.ftl">
	</div>

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
				"sPaginationType": "bootstrap",
				"oLanguage": {
					"oPaginate": {
						"sPrevious": "<@spring.message "common.paging.previous"/>",
						"sNext":     "<@spring.message "common.paging.next"/>"
					}
				}
			});
			
			removeClick();
			</#if>
		});
	
		function deleteCheckedUsers() {
			var list = $("input[id='user_info_check']:checked");
			if(list.length == 0) {
				bootbox.alert("<@spring.message "user.list.alert.delete"/>", "<@spring.message "common.button.ok"/>");
				return;
			}
			var checkedUser = [];
			var $elem;
			list.each(function() {
				$elem = $(this);
				checkedUser.push($elem.val());
			});
			
			deleteUsers(checkedUser.join(","));	
		}
		
		function deleteUsers(ids) {
			bootbox.confirm("<@spring.message "user.list.confirm.delete"/> " + ids + "?", "<@spring.message "common.button.cancel"/>", "<@spring.message "common.button.ok"/>", function(result) {
				if (result) {
					document.location.href="${req.getContextPath()}/user/delete?userIds=" + ids;
				}
			});
		}
	</script>
</body>
</html>