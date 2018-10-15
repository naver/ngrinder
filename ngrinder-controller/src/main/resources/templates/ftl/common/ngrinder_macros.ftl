<#ftl strip_whitespace = true>
<#--
 * ngrinder_macros.ftl
 *
 * This file consists of a collection of FreeMarker macros aimed at easing
 * some of the common html component blocks.
 *
 -->

<#function toUnderscore camelCase>
	<#return camelCase?replace("[A-Z]", "_$0", 'r')?lower_case>
</#function>


<#macro input_append name, value, message, data_placement="right", class="input input-mini",
		type = "text", data_content="", others="", append="", append_prefix="">

	<#if append!=""><div class="input-append"></#if>

	<input type="${type}" class="${class}"
				 rel="popover" id ="${toUnderscore(name)}" name="${name}"
				 value="${value}" data-html="true"
				 data-placement='${data_placement}'
		data-content=<#if data_content =="">"<@spring.message "${message}.help"/>"<#else>"${data_content}"</#if>
	title='<@spring.message "${message}"/>'
	<#if others!="">${others}</#if> />

	<#if append!="">
		<span class="add-on">
			<@spring.message "${append_prefix}"/>
			${append}
		</span>
	</#if>

	<#if append!=""></div></#if>
</#macro>

<#macro input_prepend name, value, message, extra_css>
	<div class="input-prepend ${extra_css}" style="margin-bottom: 0">
		<span class="add-on" title='<@spring.message "${message}"/>'>
			<@spring.message "${message}"/>
		</span>
		<input class="input span1" type="text" id="${toUnderscore(name)}" name="${name}" value="${value}" />
	</div>
</#macro>

<#macro input_label name, value, message,  err_style="", others="">
	<div class="control-group">
		<label for="${toUnderscore(name)}" class="control-label"><@spring.message "${message}"/></label>
		<div class="controls">
			<input type="text" class="input input-mini" id="${toUnderscore(name)}" name="${name}"
				   value="${value}" style="width:40px"/>
			<#if others!="">${others}</#if>
			<div id="err_${toUnderscore(name)}" style="${err_style}"></div>
		</div>

	</div>
</#macro>

<#macro input_popover name, message, value = "", type = "text", rel = "popover", extra_css = "", message_content = "",
	data_placement = "top", others = "", placeholder = "">
	<input type="${type}" class="input ${extra_css}"  <#if others!="">${others}</#if>
		id="${toUnderscore(name)}"
		name="${name}"
		rel="${rel}"
        placeholder = <#if placeholder!="">'<@spring.message "${placeholder}"/>'<#else>''</#if>
		title='<@spring.message "${message}"/>'
		data-content= <#if message_content!="">"${message_content}"<#else>"<@spring.message "${message}.help"/>"</#if>
		data-html="true"
		data-placement="${data_placement}"
		<#if value!="">value="${value}"</#if> />
</#macro>

<#macro list list_items others="none", colspan = "8", message = "" >
	<#if list_items?has_content>
		<#list list_items as each>
			<#nested each each_index>
		</#list>
	<#elseif others="table_list">
		<tr>
			<td colspan="${colspan}" class="center"><@spring.message "common.message.noData"/></td>
		</tr>
	<#elseif others="message">
		<@spring.message "${message}"/>
	</#if>
</#macro>

<#macro control_group name = "", group_id = "", label_message_key = "", lable_extra_class = ""
	controls_style = "", label_style = "", err_style = "", inline_help="false" controls_extra_class = ""
	label_help_message_key="" data_step="" data_intro="">

<div class="control-group <#if data_step != ''>intro</#if>" <#if group_id!="">id="${group_id}"</#if>
	<#if data_step!="">data-step="${data_step}"</#if> <#if data_intro!="">data-intro="<@spring.message '${data_intro}'/>"</#if>>
	<label class="control-label ${lable_extra_class}" <#if name!="">for="${toUnderscore(name)}"</#if> style="${label_style}">
		<@spring.message "${label_message_key}"/>
		<#if label_help_message_key != "">
			<span rel="popover" data-html="true"
				  data-content='<@spring.message "${label_help_message_key}.help"/>'
				  data-placement='top' title='<@spring.message "${label_help_message_key}"/>'
				>
				<i class="icon-question-sign" style="vertical-align: middle;"></i>
			</span>
		</#if>
	</label>
	<div class="controls ${controls_extra_class}" style="${controls_style}">
		<#nested>
		<#if name != "">
			<#if inline_help=="true">
				<span id="err_${toUnderscore(name)}" class="small-error-box" style="${err_style}">
				</span>
			<#else>
				<div id="err_${toUnderscore(name)}" class="small-error-box" style="${err_style}">
				</div>
			</#if>
		</#if>
	</div>

</div>
</#macro>




<#macro control_group_with_radio name="", group_id="", label_message_key="", lable_extra_class=""
	controls_style="", label_style="", err_style="", inline_help="false" controls_extra_class=""
	input_id="", input_name="", input_value="" radio_checked="" data_step="" data_intro="">
	
<div class="control-group <#if data_step != ''>intro</#if>" id="${group_id}"
	<#if data_step!="">data-step="${data_step}"</#if> <#if data_intro!="">data-intro="<@spring.message '${data_intro}'/>"</#if>>
	<label class="control-label ${lable_extra_class}" <#if name!="">for="${toUnderscore(name)}"</#if> style="${label_style}">
		<input type="radio" id="${input_id}" name="${input_name}" value="${input_value}" ${radio_checked}/>
		<@spring.message "${label_message_key}"/>
	</label>
	<div class="controls ${controls_extra_class}" style="${controls_style}">
		<#nested>
		<#if name != "">
			<#if inline_help=="true">
				<span id="err_${toUnderscore(name)}" class="help-inline" style="${err_style}">
				</span>
			<#else>
				<div id="err_${toUnderscore(name)}" class="small-error-box" style="${err_style}">
				</div>
			</#if>
		</#if>
	</div>

</div>
</#macro>