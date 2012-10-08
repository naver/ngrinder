<select id="roles" class="pull-right" style="margin-top:-55px" name="roles">
	<option value="all" <#if listPage?exists && !roleName?exists>selected</#if>"><@spring.message "user.left.all"/></option>
	<#list roleSet as role> 
		<option value="${role.fullName}" <#if roleName?exists && role.fullName == roleName>selected</#if>>${role.fullName}</option>
	</#list>
</select> 
<script>
	$("#roles").change(function() {
		var selectedValue = $(this).val();
		var destUrl = "${req.getContextPath()}/user/list";
		if (selectedValue != "all") {
			destUrl = destUrl + "?roleName=" + selectedValue;
		}
		window.location.href=destUrl;
	});
</script>
