<#if resultsub?exists>
	<script>
			curPeakTps = ${(resultsub.peakTpsForGraph!0)?c};
	  		curTps = ${(resultsub.tpsChartData!0)?c};
	  		curRunningTime = ${(resultsub.test_time!0)?c};
	  		curRunningProcesses = ${(resultsub.process!0)?c};
	  		curRunningThreads = ${(resultsub.thread!0)?c};
	  		curStatus = <#if resultsub.success?? && resultsub.success>true<#else>false</#if>
	  		<#if resultsub.totalStatistics?exists>
	  		curRunningCount = ${((resultsub.totalStatistics.Tests!0)+(resultsub.totalStatistics.Errors!0))?c};
	  		</#if>
	  		curAgentPerfStates = [
	  			${result_agent_perf}
	  		];
	</script>
	<table >
		<tbody>	 
		    <#list resultsub?keys as mKey>
				<#if mKey=='lastSampleStatistics'>
						<#assign item = resultsub[mKey]>   
						<#list item as statistics>
						<tr id="lsTableItem">
							<td >${statistics.testNumber!'&nbsp;'}</td>
							<td class="ellipsis">${statistics.testDescription!'&nbsp;'}</td>
							<td>${statistics.Tests!'&nbsp;'}</td>
							<td>${statistics.Errors!'&nbsp;'}</td>
							<td>${(statistics['Mean_Test_Time_(ms)']!0)?string("0.##")}</td>
							<td>${statistics.TPS!'0'}</td>
							<td>-</td>
							<td>${(statistics['Test_Time_Standard_Deviation_(ms)']!0)?string("0.##")}</td>
						</tr>
						</#list>
				</#if>
				<#if mKey=='cumulativeStatistics'>
						<#assign item = resultsub[mKey]>   
						<#list item as statistics>
						<tr id="asTableItem">
							<td >${statistics.testNumber!'&nbsp;'}</td>
							<td  class="ellipsis">${statistics.testDescription!'&nbsp;'}</td>
							<td>${statistics.Tests!'&nbsp;'}</td>
							<td>${statistics.Errors!'&nbsp;'}</td>
							<td>${(statistics['Mean_Test_Time_(ms)']!0)?string("0.##")}</td>
							<td>${(statistics.TPS!0)?string("0.##")}</td>
							<td>${statistics.Peak_TPS!'-'}</td>
							<td>${(statistics['Test_Time_Standard_Deviation_(ms)']!0)?string("0.##")}</td>
						</tr>
						</#list>
				</#if>
			</#list>
		</tbody>
	</table>
</#if>