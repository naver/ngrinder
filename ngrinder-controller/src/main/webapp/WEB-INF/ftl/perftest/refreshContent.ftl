<#if resultsub?exists>
	<INPUT type="hidden" id="input_process" value="${resultsub.process!'&nbsp;'}">
	<INPUT type="hidden" id="input_thread"  value="${resultsub.thread!'&nbsp;'}">
	<INPUT type="hidden" id="tpsChartData"  value="${resultsub.tpsChartData!}">
	<INPUT type="hidden" id="test_time"  value="${resultsub.test_time!}">
	<INPUT type="hidden" id="input_status"  value="<#if resultsub.success?? && resultsub.success>SUCCESS<#else>FAIL</#if>">

	<table>
		<tbody>	
		    <#list resultsub?keys as mKey>
				<#if mKey=='lastSampleStatistics'>
						<#assign item = resultsub[mKey]>   
						<#list item as statistics>
						<tr id="lsTableItem">
							<td>${statistics.testNumber!'&nbsp;'}</td>
							<td>${statistics.testDescription!'&nbsp;'}</td>
							<td>${statistics.Tests!'&nbsp;'}</td>
							<td>${statistics.Errors!'&nbsp;'}</td>
							<td>${statistics['Mean_Test_Time_(ms)']!'&nbsp;'}</td>
							<td>${statistics.TPS!'&nbsp;'}</td>
							<td>${statistics.Peak_TPS!'&nbsp;'}</td>
							<td>${statistics['Test_Time_Standard_Deviation_(ms)']!'&nbsp;'}</td>
						</tr>
						</#list>
				</#if>
				<#if mKey=='cumulativeStatistics'>
						<#assign item = resultsub[mKey]>   
						<#list item as statistics>
						<tr id="asTableItem">
							<td>${statistics.testNumber!'&nbsp;'}</td>
							<td>${statistics.testDescription!'&nbsp;'}</td>
							<td>${statistics.Tests!'&nbsp;'}</td>
							<td>${statistics.Errors!'&nbsp;'}</td>
							<td>${statistics['Mean_Test_Time_(ms)']!'&nbsp;'}</td>
							<td>${statistics.TPS!'&nbsp;'}</td>
							<td>${statistics.Peak_TPS!'&nbsp;'}</td>
							<td>${statistics['Test_Time_Standard_Deviation_(ms)']!'&nbsp;'}</td>
						</tr>
						</#list>
				</#if>
			</#list>
		</tbody>
	</table>
</#if>