<!DOCTYPE html>
<html>
	<head>
		<#include "../common/common.ftl">
		<#include "../common/jqplot.ftl">
		<title><@spring.message "perfTest.report.title"/></title>
		
		<style>
			body {
				padding-top: 0;
			}	
			.left { border-right: 1px solid #878988 }
			div.chart { border: 1px solid #878988; height:200px; min-width:615px; margin-bottom:30px }
			td strong { color: #6DAFCF }
			.jqplot-yaxis {
			    margin-right: 10px;
			}
			.jqplot-xaxis {
			    margin-right: 5px;
       			margin-top: 5px; 
			}
			.compactpadding th { padding:8px 5px; vertical-align:middle; }
			li a { color:#005580 }
			li a:hover { color:#0088CC; text-decoration:underline; }
			li a.active { color:#0088CC }
		</style>
	</head>

	<body>
		<ul class="breadcrumb" style="width:912px; margin-left:auto; margin-right:auto; margin-bottom:0">
			<li>
				<h3><@spring.message "perfTest.report.reportPage"/> ${test.testName}</h3>
			</li>
		</ul>
	<div class="container">
	   <input type="hidden" id="startTime" name="startTime" value="${(test.startTime)!}">
	   <input type="hidden" id="finishTime" name="finishTime" value="${(test.finishTime)!}">
	   <form name="downloadForm">
	       <input type="hidden" id="testId" name="testId" value="${test.id}">
	   </form>
		<div class="row">
			<div class="span3">
			   <table class="table table-bordered compactpadding">
				   <colgroup>
						<col width="120px">
						<col> 
				   </colgroup>
			       <tr>
			       	   <th><@spring.message "perfTest.report.vusersPerAgent"/></th>
			           <td><strong>${test.vuserPerAgent}</strong></td>
			       </tr>
			       <tr>
                       <th><@spring.message "perfTest.report.agent"/></th>
                       <td><span>${test.agentCount}</span>&nbsp;&nbsp;<a class="btn btn-mini btn-info hidden" id="agentInfoBtn" href="#agentListModal" data-toggle="modal">Info</a></td>
                   </tr>
                   <tr>
                       <th><@spring.message "perfTest.report.process"/></th>
                       <td>${test.processes}</td>
                   </tr>
                   <tr>
                       <th><@spring.message "perfTest.report.thread"/></th>
                       <td>${test.threads}</td>
                   </tr>
                   <tr>
                        <td colspan="2" class="divider"></td>
                   </tr>
                   <#if test.threshold?? && test.threshold == "D"> 
	                   <tr> 
	                       <th><@spring.message "perfTest.configuration.duration"/></th>
	                       <td><span>${test.durationStr}</span> <code>HH:MM:SS</code></td>
	                   </tr>
                   <#else>
                   		<tr> 
	                       <th><@spring.message "perfTest.table.runcount"/></th>
	                       <td><span>${test.runCount}</td>
	                   </tr>
                   </#if>
                   <tr>
                       <th><@spring.message "perfTest.configuration.ignoreSampleCount"/></th>
                       <td><span>${test.ignoreSampleCount}</span></td> 
                   </tr>
                   <tr>
                        <td colspan=2></td>
                   </tr>
                   <tr>
                       <th>TPS</th>
                       <td><strong><#if test.tps??>${(test.tps)?string(",##0.#")}</#if></strong></td>
                   </tr>
                   <tr>
                       <th><@spring.message "perfTest.report.meantime"/></th>
                       <td><span>${(test.meanTestTime!0)?string(",##0.##")}</span>&nbsp;&nbsp; <code>ms</code></td>
                       
                   </tr>
                   <tr>
                       <th><@spring.message "perfTest.report.peakTPS"/></th> 
                       <td><strong>${test.peakTps!""}</strong></td>
                   </tr>
                   <tr>
                       <th><@spring.message "perfTest.report.finishedTest"/></th>
                       <td>${test.tests!""}</td>
                   </tr>
                   <tr>
                       <th><@spring.message "perfTest.report.errors"/></th>
                       <td>${test.errors!""}</td> 
                   </tr>
			   </table>
			   <ul class="unstyled">
                 <li><i class="icon-tag"></i> <a id="testPerformance" href="javascript:void(0);" class="active"><@spring.message "perfTest.report.performanceReport"/></a></li>
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
			    <table class="table table-bordered" style="margin-bottom:35px">
			    	<colgroup>
						<col width="120">
						<col width="220">
						<col width="120">
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
			    <div id="performanceDiv">
			    	<div class="page-header pageHeader">
			    		<h4>Performance</h4>
					</div>
	                <button class="btn btn-middle btn-primary pull-right" id="downloadReportData" style="margin-top:-55px"><i class="icon-download-alt"></i><strong><@spring.message "perfTest.report.downloadCSV"/></strong></button>
					<h6>TPS</h6>
			    	<div class="chart" id="tpsDiv"></div>
					<h6><@spring.message "perfTest.report.header.meantime"/>&nbsp;(ms)</h6>
    				<div class="chart" id="meanTimeDiv"></div>
    				<h6 id="minTimeFirstByteHeader"><@spring.message "perfTest.report.header.meantimetofirstbyte"/>&nbsp;(ms)</h6>
    				<div class="chart" id="minTimeFirstByte"></div>
					<h6><@spring.message "perfTest.report.header.errors"/></h6>
    				<div class="chart" id="errorDiv"></div>
				</div>
				<div id="monitorDiv" style="display:none">
	    			<div class="page-header pageHeader">
						<h4>System Data <i id="ipMark"></i></h4>
					</div>
				    <h6>CPU</h6>
                    <div class="chart" id="cpuDiv"></div>
					<h6>Used Memory</h6>
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
	<#include "../common/messages.ftl">
	<script>
	    var performanceInit = false;
	    var targetMonitorPlot = {};
		$(document).ready(function() {
		    // TODO need to add cache here
		    $("#testPerformance").click(function() {
		        $("#performanceDiv").show();
		        $("#monitorDiv").hide();
		        getPerformanceData();
		        changActiveLink($(this));
		    });
		    
		    $("a.targetMontor").click(function() {
                $("#performanceDiv").hide();
                $("#monitorDiv").show();
                var $elem = $(this);
                getMonitorData($elem.attr("ip"), false);
                changActiveLink($elem);
            });

            $("#downloadReportData").click(function() {
                document.forms.downloadForm.action = 
                	"${req.getContextPath()}/perftest/downloadReportData?testId=" + $("#testId").val();
                document.forms.downloadForm.submit();
            });
			
			$("#testPerformance").click();
		});
		
		function changActiveLink(obj) {
			$("li > a.active").removeClass("active");
			obj.addClass("active");
		}
		
		function getPerformanceData(){
		    if(performanceInit){
		        return;
		    }
		    performanceInit = true;
            $.ajax({
                url: "${req.getContextPath()}/perftest/getReportData",
                dataType:'json',
                cache: true,
                data: {'testId': $("#testId").val(),
                       'dataType':'TPS,Errors,Mean_Test_Time_(ms),Mean_time_to_first_byte',
                       'imgWidth':700},
                success: function(res) {
                    if (res.success) {
                    	var st = new Date($('#startTime').val());
                        drawChart('Transactions Per Second', 'tpsDiv', res.TPS, undefined, res.chartInterval);
                        drawChart('Mean Time', 'meanTimeDiv', res.Mean_Test_Time_ms, undefined, res.chartInterval);
                        if (res.Mean_time_to_first_byte !== undefined && 
                        		res.Mean_time_to_first_byte !== '[ ]') {
                        	drawChart('Mean Time To First Byte', 'minTimeFirstByte', res.Mean_time_to_first_byte, undefined, res.chartInterval);
                        } else {
                        	$("#minTimeFirstByte").hide();	
                        	$("#minTimeFirstByteHeader").hide();
                        }
                        drawChart('Errors Per Second', 'errorDiv', res.Errors, undefined, res.chartInterval);
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
            $.ajax({
                url: "${req.getContextPath()}/monitor/getMonitorData",
                dataType:'json',
                cache: true,
                data: {'monitorIP': ip,
                	   'testId': $("#testId").val(),
                       'imgWidth' : 700},
                success: function(res) {
                    if (res.success) {
                    	var plotKeyCpu = ip + "-cpu";
                    	var plotKeyMem = ip + "-mem";
                    	var ymax = 0;
                    	$("#ipMark").html("[" + ip + "]");
                    	var rs = true;
                    	
                    	if (res.SystemData.cpu == undefined) {
                    		showErrorMsg("<@spring.message "perfTest.report.message.noMonitorData"/>");
                    		res.SystemData.cpu = [0];
                    		res.SystemData.memory = [0];
                    		rs = false;
                    	}
                    	
                    	if (targetMonitorPlot.plotKeyCpu) {
                    		ymax = getMaxValue(res.SystemData.cpu);
                    		replotChart(targetMonitorPlot.plotKeyCpu, res.SystemData.cpu, ymax);
                    	} else {
                    		targetMonitorPlot.plotKeyCpu = drawChart('System CPU', 'cpuDiv', res.SystemData.cpu, formatPercentage, res.SystemData.interval);
                    	}
                    	
                    	if (targetMonitorPlot.plotKeyMem) {
                    		ymax = getMaxValue(res.SystemData.memory);
                    		replotChart(targetMonitorPlot.plotKeyMem, res.SystemData.memory, ymax);
                    	} else {
                    		targetMonitorPlot.plotKeyMem = drawChart('System Used Memory', 'memoryDiv', res.SystemData.memory, formatMemory, res.SystemData.interval);
                    	}
                    	
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
