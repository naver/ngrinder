<#setting number_format="computer">
<#include "../common/ngrinder_macros.ftl">
	<option value=""></option>
<@list list_items=switchableUsers others="options"; each>
	<option value="${each.id}">${each.text}</option>
</@list>