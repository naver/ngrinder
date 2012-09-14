<#if resultsub?exists>
	<script>
			curPeakTps = ${resultsub.peakTpsForGraph!0};
	  		curTps = ${(resultsub.tpsChartData!0)?c};
	  		curRunningTime = ${resultsub.test_time!0};
	  		curRunningProcesses = ${resultsub.process!0};
	  		curRunningThreads = ${resultsub.thread!0};
	  		curStatus = <#if resultsub.success?? && resultsub.success>true<#else>false</#if>
	  		curAgentPerfStates = [
	  			${result_agent_perf}
	  		];
	</script>
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
							<td>${(statistics['Mean_Test_Time_(ms)']!0)?string("0.##")}</td>
							<td>${statistics.TPS!'&nbsp;'}</td>
							<td>-</td>
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