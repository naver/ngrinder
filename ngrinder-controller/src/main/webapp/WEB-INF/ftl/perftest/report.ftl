<!DOCTYPE html>
<html>
	<head>
		<title>nGrinder Test Report</title>
		<#include "../common/common.ftl">
		
		<style>
			body {
				padding-top: 0;
			}	
			.left { border-right: 1px solid #878988 }
			div.chart { border: 1px solid #878988; height:195px; min-width:615px; margin-bottom:12px}
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
	   <input type="hidden" id="testId" name="id" value="${(test.id)!}">
	   <input type="hidden" id="startTime" name="startTime" value="${(test.startTime)!}">
	   <input type="hidden" id="finishTime" name="finishTime" value="${(test.finishTime)!}">
		<div class="row">
			<div class="span12" style="margin-bottom:10px;">
				<button class="btn btn-large pull-right"><i class="icon-download-alt"></i><strong>Download CSV</strong></button>
			</div>
		</div>
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
                               <td><span>${(test.duration)!0}</span><code>sec</code></td>
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
                         <li><a id="testPerformance" href="javascript:void(0);">Performance Report</a></li>
                       </ul>
					   <ul class="unstyled">Target Hosts
                         <li><a id="targetMontor" href="javascript:void(0);" ip="${(test.targetHosts)!}">${(test.targetHosts)!}</a></li>
                       </ul>
                       <ul class="unstyled">Agent servers
                         <li><a id="agentMontor" href="javascript:void(0);" ip="${(test.targetHosts)!}">${(test.agentServer)!}</a></li>
                       </ul>
			</div>
			<div class="span7">
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
				<!--
				<img src="image01.jpg" height="210" width="800" border="0">
				<img src="image01.jpg" height="210" width="800" border="0">
				<img src="image01.jpg" height="210" width="800" border="0">
				<img src="image01.jpg" height="210" width="800" border="0">
				-->
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
            $("#agentMontor").click(function() {
                $("#performanceDiv").hide();
                $("#monitorDiv").show();
                var $elem = $(this);
                getMonitorData($elem.attr("ip"));
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
                        $('#tpsDiv').text('tps data:'+res.tps_total);
                        $('#rpsDiv').text('response time data:'+res.response_time);
                        $('#vuserDiv').text('vuser data:'+res.vuser);
                        $('#errorDiv').text('error data:'+res.tps_failed);
                        return true;
                    } else {
                        showErrorMsg("Get report data failed.");
                        return false;
                    }
                },
                error: function() {
                    alert(2);
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
                        $('#cpuDiv').text('system cpu data:'+res.cpu);
                        $('#memoryDiv').text('system memory data:'+res.memory);
                        $('#heapMemoryDiv').text('heap memory data:'+res.heap_memory);
                        $('#nonHeapMemoryDiv').text('non heap memory data:'+res.non_heap_memory);
                        $('#threadCountDiv').text('thread count data:'+res.thread_count);
                        $('#jvmCpuDiv').text('jvm cpu data:'+res.jvm_cpu);
                        return true;
                    } else {
                        showErrorMsg("Get monitor data failed.");
                        return false;
                    }
                },
                error: function() {
                    alert(2);
                    showErrorMsg("Error!");
                    return false;
                }
            });
        }
	</script>
	</body>
</html>