<!DOCTYPE html>
<html>
<head>
	<#include "../common/common.ftl">
	<title><@spring.message "user.detail.title"/></title>
</head>

<body>
	<#include "../common/navigator.ftl">
	<div class="container">
		<fieldSet>
			<legend class="header">
				<@spring.message "user.detail.header"/>
				<button class="btn pull-right" onClick="window.history.back();">
					<@spring.message "common.button.return"/>
				</button>
			</legend>
		</fieldSet>
		<#assign popover_place='right'/>
		<#include "info.ftl"> 
		<#include "../common/copyright.ftl">
	</div>
	
	<script type="text/javascript">
		$(document).ready(function(){
			$("#search_user").click(function() {
				document.location.href = "${req.getContextPath()}/user/?keywords=" + $("#searchText").val() ;
			});
		});
	</script>
</body>
</html>
