<#if userList?has_content>
<#list userList as user>
	<option value="${user.userId}">${user.userName}</option>
</#list>
</#if>