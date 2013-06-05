<#if shareUserList?has_content>
<#list shareUserList as user>
	<#assign maxlength = 33 - user.userName?length>
	<option value="${user.userId}">${user.userName}<#if user.email??> (<#if user.email?length \gt maxlength>${user.email?substring(0, maxlength)}...<#else>${user.email}</#if>)</#if></option>
</#list>
</#if>  