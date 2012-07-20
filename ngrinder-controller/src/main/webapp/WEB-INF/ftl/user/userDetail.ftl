<!DOCTYPE html>
<html>
<head>
	<title>nGrinder User Detail</title>
	<#include "../common/common.ftl">
</head>

<body>
	<#include "../common/navigator.ftl">

	<div class="container">
		<div class="row">
			<div class="span2">
				<a class="btn" href="${req.getContextPath()}/user/detail"
					id="createBtn" data-toggle="modal"> <i class="icon-user"></i>
					Create User
				</a>
				<div class="well sidebar-nav">
		            <ul class="nav nav-list">
						<li class="active nav-header">
							<a href="${req.getContextPath()}/user/list">ALL</a>
						</li>
		            	<#list roleSet as role>
						<li class="active nav-header">
							<a href="${req.getContextPath()}/user/list?roleName=${role.fullName}">${role.fullName}</a>
						</li>
						</#list>
		            </ul>
				</div><!--/.well -->
			</div>

			<div class="span10">
				<div class="page-header pageHeader">
					<h3>User Infomation</h3>
				</div>
				<#include "userInfo.ftl">
			</div>
		</div>
		<#include "../common/copyright.ftl">
	</div>
	
	<script type="text/javascript">
		$(document).ready(function(){
			$("#search_user").on('click', function() {
				document.location.href = "${req.getContextPath()}/user/list?keywords=" + $("#searchText").val() ;
			});
		});
	</script>
</body>
</html>