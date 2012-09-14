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
			div.chart { border: 1px solid #878988; height:250px; min-width:615px; margin-bottom:12px }
			td strong { color: #6DAFCF }
			.jqplot-yaxis {
			    margin-right: 10px;
			}
			.jqplot-xaxis {
			    margin-right: 5px;
       			margin-top: 5px; 
			}
			.compactpadding th {padding-left:5px;padding-right:5px} 
		</style>

		<input type="hidden" id="contextPath" value="${req.getContextPath()}">
	</head>

	<body>
	<ul class="breadcrumb">
		<li>
			<h3><@spring.message "perfTest.report.reportPage"/> ${(test.testName!)}</h3>
		</li>
	</ul>
	<div class="container">
	   
	   <input type="hidden" id="startTime" name="startTime" value="${(test.startTime)!}">
	   <input type="hidden" id="finishTime" name="finishTime" value="${(test.finishTime)!}">
	   <form name="downloadForm">
	       <input type="hidden" id="testId" name="testId" value="${(test.id)!}">
	   </form>
		<div class="row">
			<div class="span3">
					   <table class="table table-bordered compactpadding">
					       <tr>
					       	   <th><@spring.message "perfTest.report.vusersPerAgent"/></th>
					           <td><strong>${(test.vuserPerAgent)!}</strong></td>
					       </tr>
					       <tr>
                               <th><@spring.message "perfTest.report.agent"/></th>
                               <td><span>${(test.agentCount)!}</span>&nbsp;&nbsp;<a class="btn btn-mini btn-info hidden" id="agentInfoBtn" href="#agentListModal" data-toggle="modal">Info</a></td>
                           </tr>
                           <tr>
                               <th><@spring.message "perfTest.report.process"/></th>
                               <td>${(test.processes)!0}</td>
                           </tr>
                           <tr>
                               <th><@spring.message "perfTest.report.thread"/></th>
                               <td>${(test.threads)!0}</td>
                           </tr>
                           <tr>
                                <td colspan=2></td>
                           </tr>
                           <tr> 
                               <th><@spring.message "perfTest.table.duration"/></th>
                               <td><span>${(test.durationStr)!}</span> <code>HH:MM:SS</code></td>
                           </tr>
                           <tr>
                               <th><@spring.message "perfTest.configuration.ignoreSampleCount"/></th>
                               <td><span>${(test.ignoreSampleCount)!0}</span></td> 
                           </tr>
                           <tr>
                                <td colspan=2></td>
                           </tr>
                           <tr>
                               <th>TPS</th>
                               <td><strong>Total ${(test.tps)!}</strong></td>
                           </tr>
                           <tr>
                               <th><@spring.message "perfTest.report.meantime"/></th>
                               <td><span>${(test.meanTestTime)!0}</span>&nbsp;&nbsp; <code>ms</code></td>
                               
                           </tr>
                           <tr>
                               <th>Peak TPS</th>
                               <td><strong>${(test.peakTps)!}</strong></td>
                           </tr>
                           <tr>
                               <th><@spring.message "perfTest.report.finishedTest"/></th>
                               <td>${(test.tests)!}</td>
                           </tr>
					   </table>
					   <ul class="unstyled">
                         <li><i class="icon-tag"></i> <a id="testPerformance" href="javascript:void(0);"><@spring.message "perfTest.report.performanceReport"/></a></li>
                       </ul>
                       <#if test.targetHostIP?exists>	
						   <ul class="unstyled"><i class="icon-tags"></i> <@spring.message "perfTest.report.targetHost"/>
                         		<#list test.targetHostIP as targetIp>
	                         		<li><i class="icon-chevron-right"></i><a class="targetMontor" href="javascript:void(0);" ip="${targetIp}">${targetIp}</a></li>
	                         	</#list> 
                           </ul> 
                       </#if>
			</div>
			<div class="span9">
			    <table class="table table-bordered" style="margin-bottom:10px">
			    	<colgroup>
						<col width="100">
						<col width="220">
						<col width="100">
						<col>
					</colgroup>
                   <tr>
                       <th><@spring.message "perfTest.table.startTime"/></th>
                       <td><span><#if test.startTime??>${test.startTime?string('yyyy-MM-dd HH:mm:ss')}<#else>&nbsp;</#if></span></td>
                       <th><@spring.message "perfTest.table.finishTime"/></th>
                       <td><span><#if test.finishTime??>${test.finishTime?string('yyyy-MM-dd HH:mm:ss')}<#else>&nbsp;</#if></span></td>
                   </tr>
                   <tr>
                     	<th><@spring.message "perfTest.report.testcomment"/></th>
                    	<td colspan="3">${(test.testComment)!?html?replace('\n', '<br>')}</td>
                   </tr>  
               </table>
               <div class="row" style="margin-bottom:10px">
	                <button class="btn btn-middle btn-primary pull-right" id="downloadReportData"><i class="icon-download-alt"></i><strong><@spring.message "perfTest.report.downloadCSV"/></strong></button>
	           </div>
			    <div id="performanceDiv">
			    	<div class="page-header">
						<h4>TPS</h4>
					</div>
			    	<div class="chart" id="tpsDiv"></div>
    				<div class="page-header">
						<h4><@spring.message "perfTest.report.header.meantime"/>&nbsp;&nbsp;<code>ms</code></h4>
					</div>
    				<div class="chart" id="meanTimeDiv"></div>
    				<div class="page-header">
						<h4><@spring.message "perfTest.report.header.errors"/></h4>
					</div>
    				<div class="chart" id="errorDiv"></div>
				</div>
				<div id="monitorDiv" style="display:none">
	    			<div class="page-header">
						<h4>System Data</h4>
					</div>
				    <h6>CPU</h6>
                    <div class="chart" id="cpuDiv"></div>
					<h6>Memory</h6>
                    <div class="chart" id="memoryDiv"></div>
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
		    $("a.targetMontor").click(function() {
                $("#performanceDiv").hide();
                $("#monitorDiv").show();
                var $elem = $(this);
                getMonitorData($elem.attr("ip"), false);
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
                       'dataType':'TPS,Errors,Mean_Test_Time_(ms)',
                       'imgWidth':700},
                success: function(res) {
                    if (res.success) {
                    	var st = new Date($('#startTime').val());
                        drawChart('Transactions Per Second', 'tpsDiv', res.TPS, undefined, undefined, undefined, res.chartInterval);
                        drawChart('Mean Time', 'meanTimeDiv', res.Mean_Test_Time_ms, undefined, undefined, undefined, res.chartInterval);
                        //drawChart('Running Vusers', 'vuserDiv', res.vuser);
                        drawChart('Errors Per Second', 'errorDiv', res.Errors, undefined, undefined, undefined, res.chartInterval);
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
            var startdate = new Date($("#startTime").val());
            $.ajax({
                url: "${req.getContextPath()}/monitor/getMonitorData",
                dataType:'json',
                data: {'ip': ip,
                       'startTime': $("#startTime").val(),
                       'finishTime': $("#finishTime").val(),
                       'imgWidth':700},
                success: function(res) {
                    if (res.success) {
                        drawChart('System CPU', 'cpuDiv', res.SystemData.cpu, formatPercentage, undefined, undefined, res.SystemData.interval);
                        drawChart('System Memory', 'memoryDiv', res.SystemData.memory, formatMemory, undefined, undefined, res.SystemData.interval);
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