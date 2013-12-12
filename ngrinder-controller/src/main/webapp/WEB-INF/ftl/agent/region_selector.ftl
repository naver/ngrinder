<#if clustered == true>
<select id="regions" class="pull-right" style="margin-top:-45px" name="regions">
	<option value="all">All</option>
	<#list regions as each>
		<option value="${each}" <#if region?? && each == region>selected</#if>><@spring.message "${each}"/></option>
	</#list>
</select>
<script>
	$("#regions").change(function () {
		var selectedValue = $(this).val();
		var destUrl = "${req.getContextPath()}/agent/";
		if (selectedValue != "all") {
			destUrl = destUrl + "?region=" + selectedValue;
		}
		window.location.href = destUrl;
	});
</script>
</#if>
