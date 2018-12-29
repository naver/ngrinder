<!DOCTYPE html>
<html>
<head>
<#include "../common/common.ftl">
	<title><@spring.message "user.info.title"/></title>
</head>

<body>
<div id="wrap">
<#include "../common/navigator.ftl">
<div class="container">
	<fieldSet>
		<legend class="header">
		<@spring.message "user.info.header"/>
			<button class="btn pull-right" onClick="window.history.back();">
			<@spring.message "common.button.return"/>
			</button>
		</legend>
	</fieldSet>
<#assign popover_place='right'/>
<#include "info.ftl">
</div>
</div>
<#include "../common/copyright.ftl">
<script type="text/javascript">
	$(document).ready(function () {
		$("#search_user").click(function () {
			document.location.href = "${req.getContextPath()}/user/?keywords=" + $("#searchText").val();
		});
	});
</script>
</body>
</html>
