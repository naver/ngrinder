<!DOCTYPE html>
<html>
<head>
	<#include "../common/common.ftl">
	<title><@spring.message "user.detail.title"/></title>
</head>

<body>
	<#include "../common/navigator.ftl">

	<div class="container">
		<div class="row">
			<#include "leftButton.ftl"/>
			
			<div class="span10">
				<div class="page-header pageHeader">
					<h3><@spring.message "user.detail.header"/></h3>
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