<#if shareUserList?has_content>
<#list shareUserList as user>
	<option value="${user.userId}">${user.userName}</option>
</#list>
</#if>