<#include "../common/ngrinder_macros.ftl">
<@list list_items = shareUserList  ; user >
	<#assign maxlength = 33 - user.userName?length>
	<option value="${user.userId}">${user.userName}<#if user.email?? && user.email != ""> (<#if user.email?length \gt maxlength>${user.email?substring(0, maxlength)}...<#else>${user.email}  / ${user.userId})</#if><#else>(${user.userId})</#if></option>
</@list>