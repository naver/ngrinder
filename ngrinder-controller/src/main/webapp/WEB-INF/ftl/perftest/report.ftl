<!DOCTYPE html>
<html>
	<head>
		<title>nGrinder Test Report</title>
		<#include "../common/common.ftl">
		<#include "../common/jqplot.ftl">
		
		<style>
			body {
				padding-top: 0;
			}	
			.left { border-right: 1px solid #878988 }
			div.chart { border: 1px solid #878988; height:195px; min-width:615px; margin-bottom:12px }
			td strong { color: #6DAFCF }
		</style>

		<input type="hidden" id="contextPath" value="${req.getContextPath()}">
		<#setting number_format="computer">
	</head>

	<body>
	<ul class="breadcrumb">
		<li>
			<h3>nGrinder Report</h3>
		</li>
	</ul>
	<div class="container">
	   
	   <input type="hidden" id="startTime" name="startTime" value="${(test.startTime)!}">
	   <input type="hidden" id="finishTime" name="finishTime" value="${(test.finishTime)!}">
	   <form name="downloadForm">
	       <input type="hidden" id="testId" name="testId" value="${(test.id)!}">
	   </form>
		<div class="row">
			<div class="span4">
					   <table class="table table-bordered">
					       <tr>
					           <th>Vuser</th>
					           <td><strong>${(test.vuserPerAgent)!}</strong></td>
					       </tr>
					       <tr>
                               <th>Agents</th>
                               <td><span>${(test.agentCount)!}</span>&nbsp;&nbsp;<a class="btn btn-mini btn-info" id="agentInfoBtn" href="#agentListModal" data-toggle="modal">Info</a></td>
                           </tr>
                           <tr>
                               <th>Processes</th>
                               <td>${(test.processes)!0}</td>
                           </tr>
                           <tr>
                               <th>Threads</th>
                               <td>${(test.threads)!0}</td>
                           </tr>
                           <tr>
                                <td colspan=2></td>
                           </tr>
                           <tr>
                               <th>Duration</th>
                               <td><span>${(test.durationStr)!}</span><code>HH:MM:SS</code></td>
                           </tr>
                           <tr>
                               <th>Ignore Count</th>
                               <td><span>${(test.ignoreSampleCount)!0}</span></td>
                           </tr>
                           <tr>
                                <td colspan=2></td>
                           </tr>
                           <tr>
                               <th>Sample Interval</th>
                               <td><span>${(test.sampleInterval)!1000}</span><code>ms</code></td>
                           </tr>
                           <tr>
                                <td colspan=2></td>
                           </tr>
                           <tr>
                               <th>Test Comment</th>
                               <td>Copied</td>
                           </tr>
                           <tr>
                               <th>Vuser</th>
                               <td><strong>${(test.vuserPerAgent)!}</strong></td>
                           </tr>
                           <tr>
                                <td colspan=2></td>
                           </tr>
                           <tr>
                               <th>TPS</th>
                               <td><strong>Total ${(test.tps)!}</strong></td>
                           </tr>
                           <tr>
                               <th>Mean Time</th>
                               <td><span>${(test.meanTestTime)!}</span><code>ms</code></td>
                           </tr>
                           <tr>
                               <th>Peak TPS</th>
                               <td><strong>${(test.peakTps)!}</strong></td>
                           </tr>
                           <tr>
                               <th>Finished Tests</th>
                               <td>${(test.tests)!}</td>
                           </tr>
					   </table>
					   <ul class="unstyled">
                         <li><i class="icon-tag"></i> <a id="testPerformance" href="javascript:void(0);">Performance Report</a></li>
                       </ul>
					   <ul class="unstyled"><i class="icon-tags"></i> Target Hosts
                         <#if (test.targetHosts)?exists><li><i class="icon-chevron-right"></i><a id="targetMontor" href="javascript:void(0);" ip="${test.targetHosts}">${test.targetHosts}</a></li></#if>
                       </ul>
			</div>
			<div class="span8">
			    <table class="table table-bordered" style="margin-bottom:10px">
			    	<colgroup>
						<col width="100">
						<col width="220">
						<col width="100">
						<col>
					</colgroup>
                   <tr>
                       <th>Start Time</th>
                       <td><span>${(test.startTime)!'&nbsp;'}</span></td>
                       <th>Finish Time</th>
                       <td><span>${(test.finishTime)!'&nbsp;'}</span></td>
                   </tr>
               </table>
               <div class="row" style="margin-bottom:10px">
	                <button class="btn btn-large pull-right" id="downloadReportData"><i class="icon-download-alt"></i><strong>Download CSV</strong></button>
	           </div>
			    <div id="performanceDiv">
    				<div class="chart" id="tpsDiv"></div>
    				<div class="chart" id="rpsDiv"></div>
    				<div class="chart" id="vuserDiv"></div>
    				<div class="chart" id="errorDiv"></div>
				</div>
				<div id="monitorDiv" style="display:none">
				    <h6>System Data</h6>
                    <div class="chart" id="cpuDiv"></div>
                    <div class="chart" id="memoryDiv"></div>
                    <h6>Java Data</h6>
                    <div class="chart" id="heapMemoryDiv"></div>
                    <div class="chart" id="nonHeapMemoryDiv"></div>
                    <div class="chart" id="threadCountDiv"></div>
                    <div class="chart" id="jvmCpuDiv"></div>
                </div>
			</div>
		</div>
		<#include "../common/copyright.ftl">
	</div>
	<div class="modal fade" id="agentListModal">
		<div class="modal-header">
			<a class="close" data-dismiss="modal" id="upCloseBtn">&times;</a>
			<h3>Agent List</h3>
		</div>
		<div class="modal-body">
			Agent List
		</div>
	</div>
	<script>
	    var performanceInit = false;
	    var monitorInit = new Array();
		$(document).ready(function() {
		    // TODO need to add cache here
		    $("#testPerformance").click(function() {
		        $("#performanceDiv").show();
		        $("#monitorDiv").hide();
		        getPerformanceData();
		    });
		    $("#targetMontor").click(function() {
                $("#performanceDiv").hide();
                $("#monitorDiv").show();
                var $elem = $(this);
                getMonitorData($elem.attr("ip"));
            });

            $("#downloadReportData").click(function() {
                var url = "${req.getContextPath()}/perftest/downloadReportData?testId=" + $("#testId").val();
                document.forms.downloadForm.action = url;
                document.forms.downloadForm.submit();
            });
			$("#testPerformance").click();
		});
		function getPerformanceData(){
		    if(performanceInit){
		        return;
		    }
		    performanceInit = true;
            $.ajax({
                url: "${req.getContextPath()}/perftest/getReportData",
                dataType:'json',
                data: {'testId': $("#testId").val(),
                       'dataType':'tps_total,tps_failed,vuser,response_time',
                       'imgWidth':700},
                success: function(res) {
                    if (res.success) {
                        drawChart('Transactions Per Second', 'tpsDiv', res.tps_total);
                        drawChart('Responses Per Second', 'rpsDiv', res.response_time);
                        drawChart('Running Vusers', 'vuserDiv', res.vuser);
                        drawChart('Errors Per Second', 'errorDiv', res.tps_failed);
                        return true;
                    } else {
                        showErrorMsg("Get report data failed.");
                        return false;
                    }
                },
                error: function() {
                    showErrorMsg("Error!");
                    return false;
                }
            });
        }
        function getMonitorData(ip){
            if(monitorInit[ip]){
                return;
            }
            monitorInit[ip] = true;
            $.ajax({
                url: "${req.getContextPath()}/monitor/getMonitorData",
                dataType:'json',
                data: {'ip': ip,
                       'startTime': $("#startTime").val(),
                       'finishTime': $("#finishTime").val(),
                       'imgWidth':700},
                success: function(res) {
                    if (res.success) {
                        drawChart('System CPU', 'cpuDiv', res.SystemData.cpu, formatPercentage);
                        drawChart('System Memory', 'memoryDiv', res.SystemData.memory, formatAmount);
                        drawChart('Heap Memory', 'heapMemoryDiv', res.JavaData.heap_memory, formatAmount);
                        drawChart('NonHeap Memory', 'nonHeapMemoryDiv', res.JavaData.non_heap_memory, formatAmount);
                        drawChart('Thread Count', 'threadCountDiv', res.JavaData.thread_count);
                        drawChart('JVM Cpu', 'jvmCpuDiv', res.JavaData.jvm_cpu, formatPercentage);
                        return true;
                    } else {
                        showErrorMsg("Get monitor data failed.");
                        return false;
                    }
                },
                error: function() {
                    showErrorMsg("Display Error!");
                    return false;
                }
            });
        }
	</script>
	</body>
</html>