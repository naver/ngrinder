<#if clustered == true>
	<select id="regions" class="pull-right" style="margin-top:-55px" name="regions">
		<option value="all"/>All</option>
		<#list regions as eachRegion> 
			<option value="${eachRegion}" <#if region?exists && eachRegion == region>selected</#if>><@spring.message "${eachRegion}"/></option>
		</#list>
	</select> 
	<script>
		$("#regions").change(function() {
			var selectedValue = $(this).val();
			var destUrl = "${req.getContextPath()}/agent/list";
			if (selectedValue != "all") {
				destUrl = destUrl + "?region=" + selectedValue;
			}
			window.location.href=destUrl;
		});
	</script>
</#if>
