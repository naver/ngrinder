<#if shareUserList?has_content>
<#list shareUserList as user>
	<option value="${user.userId}">${user.userName}<#if user.email??> (<#if (user.email?length \gt 15) >${user.email?substring(0, 12)}...<#else>${user.email}</#if>)</#if></option>
</#list>
</#if>  