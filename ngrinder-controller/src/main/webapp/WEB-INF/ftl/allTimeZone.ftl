<#list timeZones?keys as key>
	<#assign value = timeZones[key]>
	<option value="${key}">${value}</option>
</#list>