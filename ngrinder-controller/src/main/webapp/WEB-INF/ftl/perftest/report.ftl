<!DOCTYPE html>
<html>
	<head>
		<#include "../common/common.ftl">
		<#include "../common/jqplot.ftl">
		
		<title><@spring.message "perfTest.report.title"/></title>
		
		<!-- For jqplot legend -->
		<link href="${req.getContextPath()}/plugins/jqplot/syntaxhighlighter/styles/shCoreDefault.min.css" rel="stylesheet"/>
    	<link href="${req.getContextPath()}/plugins/jqplot/syntaxhighlighter/styles/shThemejqPlot.min.css" rel="stylesheet"/>
    	
		<style> 
			body {
				padding-top: 0;
			}	
			.left {
				border-right: 1px solid #878988
			}
			div.chart {
				border: 1px solid #878988; 
				height: 200px; 
				min-width: 615px; 
			}
			div.bigchart {
				border: 1px solid #878988; 
				height: 300px; 
				min-width: 615px; 
			}
			h6 {
				margin-top: 20px;
			}
			td strong {
				color: #6DAFCF
			}
			.jqplot-yaxis {
			    margin-right: 10px;
			}
			.jqplot-xaxis {
			    margin-right: 5px;
       			margin-top: 5px; 
			}
			.compactpadding th {
				padding: 8px 5px;
				vertical-align: middle;
			}
			.jqplot-image-button {
			    margin-top: 5px;
			    margin-bottom: 5px;
			}
			div.jqplot-image-container {
			    position: relative;
			    z-index: 11;
			    margin: auto;
			    display: none;
			    background-color: #ffffff;
			    border: 1px solid #999;
			    display: inline-block;
			    min-width: 698px;   
			}
			div.jqplot-image-container-header {
			    font-size: 1.0em;
			    font-weight: bold;
			    padding: 5px 15px;
			    background-color: #eee;
			}
			div.jqplot-image-container-content {
			    background-color: #ffffff;
			}
			a.jqplot-image-container-close {
			    float: right;
			}
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
	                       <th><@spring.message "perfTest.configuration.runCount"/></th>
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
			   <ul class="nav nav-list">
					<li class="active">
						<a id="testPerformance" href="javascript:void(0);">
							<i class="icon-tag icon-white"></i> <@spring.message "perfTest.report.performanceReport"/>
						</a>
					</li>
					<li><a><i class="icon-tags"></i> <@spring.message "perfTest.report.targetHost"/></a></li>
					<#if test.targetHostIP?exists>
					<li>
						<ul class="nav nav-list">
							<#list test.targetHostIP as targetIp>
							<li><a class="targetMontor" href="javascript:void(0);" ip="${targetIp}"><i class="icon-chevron-right"></i> ${targetIp}</a></li>
							</#list>
						</ul>
					</li>
					</#if>
			   </ul>
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
					<legend>
						Performance
		                <button id="downloadReportData" class="btn btn-primary pull-right">
		                	<i class="icon-download-alt icon-white"></i> <@spring.message "perfTest.report.downloadCSV"/>
		                </button>
					</legend>
					<h6>TPS <span rel="popover" data-content='<@spring.message "perfTest.report.tps.help"/>' data-original-title='<@spring.message "perfTest.report.tps"/>' type="toggle"><i class="icon-question-sign" style="vertical-align:middle;"></i></span></h6>
			    	<div class="bigchart" id="tpsDiv"></div>
					<h6><@spring.message "perfTest.report.header.meantime"/>&nbsp;(ms)</h6>
    				<div class="chart" id="meanTimeDiv"></div>
    				<h6 id="minTimeFirstByteHeader"><@spring.message "perfTest.report.header.meantimetofirstbyte"/>&nbsp;(ms)</h6>
    				<div class="chart" id="minTimeFirstByte"></div>
    				<h6 id="userDefinedChartHeader"><@spring.message "perfTest.report.header.userDefinedChart"/></h6>
    				<div class="chart" id="userDefinedChart"></div>
					<h6><@spring.message "perfTest.report.header.errors"/></h6>
    				<div class="chart" id="errorDiv"></div>
				</div>
				<div id="monitorDiv" style="display:none">
	    			<div class="page-header pageHeader">
						<h4>System Monitoring</h4> 
					</div>
				    <h6>CPU</h6>
                    <div class="chart" id="cpuDiv"></div>
					<h6>Used Memory</h6>
                    <div class="chart" id="memoryDiv"></div>
					<h6 id="receivedDivHeader">Received Per Second</h6>
                    <div class="chart" id="receivedDiv"></div>
					<h6 id="sentDivHeader">Sent Per Second</h6>
                    <div class="chart" id="sentDiv"></div>
					<h6 id="customMonitorData1Header">Custom Monitor Data 1</h6>
                    <div class="chart" id="customMonitorData1"></div>
					<h6 id="customMonitorData2Header">Custom Monitor Data 2</h6>
                    <div class="chart" id="customMonitorData2"></div>
					<h6 id="customMonitorData3Header">Custom Monitor Data 3</h6>
                    <div class="chart" id="customMonitorData3"></div>
					<h6 id="customMonitorData4Header">Custom Monitor Data 4</h6>
                    <div class="chart" id="customMonitorData4"></div>
					<h6 id="customMonitorData5Header">Custom Monitor Data 5</h6>
                    <div class="chart" id="customMonitorData5"></div>
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
	
	<!-- For jqplot legend -->
	<script src="${req.getContextPath()}/plugins/jqplot/plugins/jqplot.enhancedLegendRenderer.min.js"></script>
	<script src="${req.getContextPath()}/js/generateImg.js"></script>
	<script>
	    var performanceInit = false;
	    var targetMonitorData = {}; //save monitor data
	    var imgBtnLabel = "<@spring.message "perfTest.report.exportImg.button"/>";
	    var imgTitle = "<@spring.message "perfTest.report.exportImg.title"/>"

	    //used to save the plot object. Then for every target, just use the new data to replot.
    	var plotKeyCpu = "plotcpu";
    	var plotKeyMem = "plotmem";
    	var plotKeyReceived = "plotreceived";
    	var plotKeySent = "plotsent";
    	
		$(document).ready(function() {
		    $("#testPerformance").click(function() {
		    	cleanImgElem();
		        $("#performanceDiv").show();
		        $("#monitorDiv").hide();
		        getPerformanceData();
		        changActiveLink($(this));
		    });
		    
		    $("a.targetMontor").click(function() {
		    	cleanImgElem();
                $("#performanceDiv").hide();
                $("#monitorDiv").show();
                var $elem = $(this);
                getMonitorData($elem.attr("ip"), false);
                changActiveLink($elem);
            });

            $("#downloadReportData").click(function() {
                document.forms.downloadForm.action = 
                	"${req.getContextPath()}/perftest/${(test.id)?c}/downloadReportData";
                document.forms.downloadForm.submit();
            });
			
			$("#testPerformance").click();
		});
		
		function changActiveLink(obj) {
			$("li").removeClass("active");
			$("ul.nav-list i.icon-white").removeClass("icon-white");
			obj.parent("li").addClass("active");
			obj.children("i").addClass("icon-white");
		}

		function getPerformanceData(){
		    if(performanceInit){
		    	generateImg(imgBtnLabel, imgTitle);
		        return;
		    }
		    performanceInit = true;
            $.ajax({
                url: "${req.getContextPath()}/perftest/${(test.id)?c}/graph",
                dataType:'json',
                cache: true,
                data: {'dataType':'TPS,Errors,Mean_Test_Time_(ms),Mean_time_to_first_byte,User_defined',
                       'imgWidth':700},
                success: function(res) {
                    if (res.success) {
                        drawMultiPlotChart('tpsDiv', res.TPS, res.lables, res.chartInterval);
                        drawChart('meanTimeDiv', res.Mean_Test_Time_ms, undefined, res.chartInterval);
                        if (res.Mean_time_to_first_byte !== undefined && 
                        		res.Mean_time_to_first_byte !== '[ ]') {
                        	drawChart('minTimeFirstByte', res.Mean_time_to_first_byte, undefined, res.chartInterval);
                        } else {
                        	$("#minTimeFirstByte").hide();	
                        	$("#minTimeFirstByteHeader").hide();
                        }
                        if (res.User_defined !== undefined && 
                        		res.User_defined !== '[ ]') {
                        	drawChart('userDefinedChart', res.User_defined, undefined, res.chartInterval);
                        } else {
                        	$("#userDefinedChart").hide();	
                        	$("#userDefinedChartHeader").hide();
                        }
                        drawChart('errorDiv', res.Errors, undefined, res.chartInterval);
                        generateImg(imgBtnLabel, imgTitle); 
                        return true;
                    } else {
                        showErrorMsg("Get report data failed.");
                        return false;
                    }
                },
                error: function() {
                    showErrorMsg("Unknow Error occured!");
                    return false;
                }
            }); 
        }
		
		function clearPrePlot() {
			$("#monitorDiv div.jqplot-target").each(function() {
				$(this).removeClass("jqplot-target");
			});
        	$("#cpuDiv").empty();
        	$("#memoryDiv").empty();
        	$("#receivedDiv").empty();
        	$("#sentDiv").empty();
        	$("#customMonitorData1").empty();
        	$("#customMonitorData2").empty();
        	$("#customMonitorData3").empty();
        	$("#customMonitorData4").empty();
        	$("#customMonitorData5").empty();
		}
		
		function drawPlot(ip) {
			clearPrePlot();
        	var currMonitorData = targetMonitorData[ip];
       		drawChart('cpuDiv', currMonitorData.cpu, formatPercentage, currMonitorData.interval);
       		drawChart('memoryDiv', currMonitorData.memory, formatMemory, currMonitorData.interval);
       		drawExtMonitorData(currMonitorData);
       		generateImg(imgBtnLabel, imgTitle);
		}
		
		function drawExtMonitorData(systemData) {
            drawChartForMonitor(systemData.received, "receivedDiv", "receivedDivHeader", systemData.interval);
            drawChartForMonitor(systemData.sent, "sentDiv", "sentDivHeader", systemData.interval);
            drawChartForMonitor(systemData.customData1, "customMonitorData1", "customMonitorData1Header", systemData.interval);
            drawChartForMonitor(systemData.customData2, "customMonitorData2", "customMonitorData2Header", systemData.interval);
            drawChartForMonitor(systemData.customData3, "customMonitorData3", "customMonitorData3Header", systemData.interval);
            drawChartForMonitor(systemData.customData4, "customMonitorData4", "customMonitorData4Header", systemData.interval);
            drawChartForMonitor(systemData.customData5, "customMonitorData5", "customMonitorData5Header", systemData.interval);
		}
		
		function drawChartForMonitor(data, area, titleArea, interval) {
			if (data !== undefined && data !== '[]') {
            	$("#" + area).show();	
            	$("#" + titleArea).show();
            	drawChart(area, data, undefined, interval);
            } else {
            	$("#" + area).hide();	
            	$("#" + titleArea).hide();
            }
		}
		
        function getMonitorData(ip){
        	if (targetMonitorData[ip]) {
        		drawPlot(ip);
				return;
        	}
        	
            $.ajax({
                url: "${req.getContextPath()}/perftest/${(test.id)?c}/monitor",
                dataType:'json',
                cache: true,
                data: {'monitorIP': ip, 'imgWidth': 700},
                success: function(res) {
                    if (res.success) {
                    	if ($.isEmptyObject(res.SystemData)) {
                    		showErrorMsg("<@spring.message "perfTest.report.message.noMonitorData"/>");
                    		res.SystemData.cpu = [0];
                    		res.SystemData.memory = [0];
                    		res.SystemData.received = [0];
                    		res.SystemData.sent = [0];
                    	}
                    	targetMonitorData[ip] = res.SystemData;
                    	drawPlot(ip);
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
