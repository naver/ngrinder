<#if resultsub?exists>
	<INPUT type="hidden" id="input_process"  value="${resultsub.process!'&nbsp;'}">
	<INPUT type="hidden" id="input_thread" value="${resultsub.thread!'&nbsp;'}">
	<INPUT type="hidden" id="tpsChartData"  value="${resultsub.tpsChartData!}">
	
	<INPUT type="hidden" id="input_status" value='<#if resultsub.success?? && resultsub.success>SUCCESS<#else>FAIL'</#if>>
	
	<div id="tableItem">
	    <#list resultsub?keys as mKey>
			<#if mKey=='lastSampleStatistics'>
				<#assign item = resultsub[mKey]>   
				<#list item as statistics>
				<tr id="first_div_last">
					<td class="td_l" ><div style="width:35px;white-space:nowrap;">Test${statistics.testNumber!'&nbsp;'}</div></td>
					<td class="td_l" >
						<div style="width:100px;white-space:nowrap;">
						<#if statistics.testDescription?has_content && statistics.testDescription?length gt 25>
	        				${statistics.testDescription?substring(0,24)}...
	      				<#else>
	        				${statistics.testDescription!'&nbsp;'}
						</#if>
						</div>
					</td>
					<td class="td_l"><div style="width:60px;white-space:nowrap;">${statistics.TestsStr!'&nbsp;'}</div></td>
					<td class="td_l"><div style="width:40px;white-space:nowrap;">${statistics.ErrorsStr!'&nbsp;'}</div></td>
					<td class="td_l"><div style="width:65px;white-space:nowrap;">${statistics['Mean_Test_Time_(ms)']!'&nbsp;'}</div></td>
					<td class="td_l"><div style="width:50px;white-space:nowrap;">${statistics.TPS!'&nbsp;'}</div></td>
					<td class="td_l"><div style="width:60px;white-space:nowrap;">${statistics.Peak_TPS!'&nbsp;'}</div></td>
					<td class="td_l"><div style="width:60px;white-space:nowrap;">${statistics['Test_Time_Standard_Deviation_(ms)']!'&nbsp;'}</div></td>
				</tr>
				</#list>
			</#if>
		</#list>
	</div>
</#if>