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

	<#setting number_format="number"> 
	<body>
		<div class="navbar-inner" style="width:912px; margin-left:auto; margin-right:auto; margin-bottom:0">
			<h3><@spring.message "perfTest.report.reportPage"/> ${test.testName}</h3>
		</div>
	<div class="container">
	   <form name="download_csv_form">
	       <input type="hidden" id="test_id" name="testId" value="${test.id}">
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
                       <td><span>${test.agentCount}</span>
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
	                   <th><@spring.message "perfTest.configuration.runtime"/></th>
	                   <td><span>${test.runtimeStr}</span> <code>HH:MM:SS</code></td>
	               </tr>
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
                       <th><@spring.message "perfTest.report.totalTests"/></th>
                       <td>${(test.tests + test.errors)!""}</td>
                   </tr>
                   <tr>
                       <th><@spring.message "perfTest.report.successfulTests"/></th> 
                       <td>${(test.tests)!""}</td>
                   </tr>
                   <tr>
                       <th><@spring.message "perfTest.report.errors"/></th>
                       <td>${test.errors!""}</td> 
                   </tr>
			   </table>
			   <ul class="nav nav-list">
					<li class="active">
						<a id="test_btn" href="javascript:void(0);">
							<i class="icon-tag icon-white"></i> <@spring.message "perfTest.report.performanceReport"/>
						</a>
					</li>
					<li><a><i class="icon-tags"></i> <@spring.message "perfTest.report.targetHost"/></a></li>
					<#if test.targetHostIP?exists>
					<li>
						<ul class="nav nav-list">
							<#list test.targetHostIP as targetIP>
							<li><a class="target-montor" href="javascript:void(0);" ip="${targetIP}"><i class="icon-chevron-right"></i> ${targetIP}</a></li>
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
			    <div id="test_panel">
					<legend>
						Performance
		                <button id="download_csv" class="btn btn-primary pull-right">
		                	<i class="icon-download-alt icon-white"></i> <@spring.message "perfTest.report.downloadCSV"/>
		                </button>
					</legend>
					<h6>TPS 
						<span 
							rel="popover" 
							data-content='<@spring.message "perfTest.report.tps.help"/>' 
							title='<@spring.message "perfTest.report.tps"/>'
							data-html='true' 
							><i class="icon-question-sign" style="vertical-align:middle;"></i></span></h6>
			    	<div class="bigchart" id="tps_chart"></div>
					<h6><@spring.message "perfTest.report.header.meantime"/>&nbsp;(ms)</h6>
    				<div class="chart" id="mean_time_chart"></div>
    				<h6 id="min_time_first_byte_header"><@spring.message "perfTest.report.header.meantimetofirstbyte"/>&nbsp;(ms)</h6>
    				<div class="chart" id="min_time_first_byte_chart"></div>
    				<h6 id="user_defined_header"><@spring.message "perfTest.report.header.userDefinedChart"/></h6>
    				<div class="chart" id="user_defined_chart"></div>
					<h6><@spring.message "perfTest.report.header.errors"/></h6>
    				<div class="chart" id="error_chart"></div>
				</div>
				<div id="monitor_panel" style="display:none">
	    			<div class="page-header page-header">
						<h4>System Monitoring</h4> 
					</div>
				    <h6>CPU</h6>
                    <div class="chart" id="cpu_usage_chart"></div>
					<h6>Used Memory</h6>
                    <div class="chart" id="mem_usage_chart"></div>
					<h6 id="recevied_byte_per_sec_header">Received Byte Per Second</h6>
                    <div class="chart" id="recevied_byte_per_sec_chart"></div>
					<h6 id="sent_byte_per_sec_header">Sent Per Second</h6>
                    <div class="chart" id="sent_byte_per_sec_chart"></div>
					<h6 id="custom_monitor_header_1">Custom Monitor Chart 1</h6>
                    <div class="chart" id="custom_monitor_chart_1"></div>
					<h6 id="custom_monitor_header_2">Custom Monitor Chart 2</h6>
                    <div class="chart" id="custom_monitor_chart_2"></div>
					<h6 id="custom_monitor_header_3">Custom Monitor Chart 3</h6>
                    <div class="chart" id="custom_monitor_chart_3"></div>
					<h6 id="custom_monitor_header_4">Custom Monitor Chart 4</h6>
                    <div class="chart" id="custom_monitor_chart_4"></div>
					<h6 id="custom_monitor_header_5">Custom Monitor Chart 5</h6>
                    <div class="chart" id="custom_monitor_chart_5"></div>
                </div>
			</div>
		</div>
		<#include "../common/copyright.ftl">
	</div>
	<#include "../common/messages.ftl">
	
	<!-- For jqplot legend -->
	<script src="${req.getContextPath()}/plugins/jqplot/plugins/jqplot.enhancedLegendRenderer.min.js"></script>
	<script src="${req.getContextPath()}/js/generate-img.js"></script>
	<script>
	    var performanceInit = false;
	    var targetMonitorData = {}; //save monitor data
	    var imgBtnLabel = "<@spring.message "perfTest.report.exportImg.button"/>";
	    var imgTitle = "<@spring.message "perfTest.report.exportImg.title"/>"
    	
		$(document).ready(function() {
		    $("#test_btn").click(function() {
		    	cleanImgElem();
		        $("#test_panel").show();
		        $("#monitor_panel").hide();
		        getPerformanceData();
		        changActiveLink($(this));
		    });
		    
		    $("a.target-montor").click(function() {
		    	cleanImgElem();
                $("#test_panel").hide();
                $("#monitor_panel").show();
                var $elem = $(this);
                getMonitorData($elem.attr("ip"), false);
                changActiveLink($elem);
            });

            $("#download_csv").click(function() {
                document.forms.download_csv_form.action = "${req.getContextPath()}/perftest/${(test.id)?c}/download_csv";
                document.forms.download_csv_form.submit();
            });
			
			$("#test_btn").click();
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
                url: "${req.getContextPath()}/perftest/api/${(test.id)?c}/graph",
                dataType:'json',
                cache: true,
                data: {'dataType':'TPS,Errors,Mean_Test_Time_(ms),Mean_time_to_first_byte,User_defined','imgWidth':700},
                success: function(res) {
					var result = drawMultiPlotChart('tps_chart', res.TPS.data, res.TPS.lables, res.chartInterval);
					if (result !== undefined){ result.replot(); }
					result = drawMultiPlotChart('mean_time_chart', res.Mean_Test_Time_ms.data, res.Mean_Test_Time_ms.lables, res.chartInterval);
					if (result !== undefined){ result.replot(); }

					if (res.Mean_time_to_first_byte !== undefined &&
							res.Mean_time_to_first_byte.data !== '[ ]') {
						drawMultiPlotChart('min_time_first_byte_chart', res.Mean_time_to_first_byte.data, res.Mean_time_to_first_byte.lables, res.chartInterval).replot();
					} else {
						$("#min_time_first_byte_chart").hide();
						$("#min_time_first_byte_header").hide();
					}
					if (res.User_defined !== undefined &&
							res.User_defined.lables !== undefined && res.User_defined.lables.length != 0) {
						drawMultiPlotChart('user_defined_chart', res.User_defined.data, res.User_defined.lables, res.chartInterval).replot();
					} else {
						$("#user_defined_chart").hide();
						$("#user_defined_header").hide();
					}
					drawMultiPlotChart('error_chart', res.Errors.data, res.Errors.lables, res.chartInterval);
					generateImg(imgBtnLabel, imgTitle);
					return true;
                },
                error: function() {
                    showErrorMsg("Failed to get graph!");
                    return false;
                }
            }); 
        }
		
		function clearPrePlot() {
			$("#monitor_panel div.jqplot-target").each(function() {
				$(this).removeClass("jqplot-target");
			});
        	$("#cpu_usage_chart").empty();
        	$("#mem_usage_chart").empty();
        	$("#recevied_byte_per_sec_chart").empty();
        	$("#sent_byte_per_sec_chart").empty();
        	$("#custom_monitor_chart_1").empty();
        	$("#custom_monitor_chart_2").empty();
        	$("#custom_monitor_chart_3").empty();
        	$("#custom_monitor_chart_4").empty();
        	$("#custom_monitor_chart_5").empty();
		}
		
		function drawPlot(ip) {
			clearPrePlot();
        	var currMonitorData = targetMonitorData[ip];
       		drawChart('cpu_usage_chart', currMonitorData.cpu, formatPercentage, currMonitorData.interval).replot();
       		drawChart('mem_usage_chart', currMonitorData.memory, formatMemory, currMonitorData.interval).replot();
       		drawExtMonitorData(currMonitorData);
       		generateImg(imgBtnLabel, imgTitle);
		}
		
		function drawExtMonitorData(systemData) {
            drawChartForMonitor(systemData.received, "recevied_byte_per_sec_chart", "recevied_byte_per_sec_header", systemData.interval, formatNetwork);
            drawChartForMonitor(systemData.sent, "sent_byte_per_sec_chart", "sent_byte_per_sec_header", systemData.interval, formatNetwork);
            drawChartForMonitor(systemData.customData1, "custom_monitor_chart_1", "custom_monitor_header_1", systemData.interval, formatNetwork);
            drawChartForMonitor(systemData.customData2, "custom_monitor_chart_2", "custom_monitor_header_2", systemData.interval, formatNetwork);
            drawChartForMonitor(systemData.customData3, "custom_monitor_chart_3", "custom_monitor_header_3", systemData.interval, formatNetwork);
            drawChartForMonitor(systemData.customData4, "custom_monitor_chart_4", "custom_monitor_header_4", systemData.interval, formatNetwork);
            drawChartForMonitor(systemData.customData5, "custom_monitor_chart_5", "custom_monitor_header_5", systemData.interval, formatNetwork);
		}
		
		function drawChartForMonitor(data, area, titleArea, interval, format) {
			if (data !== '[]') {
            	$("#" + area).show();	
            	$("#" + titleArea).show();
            	$("#" + area + "_img_btn").show();
            	plot = drawChart(area, data, format, interval);
            	plot.replot();
            } else {
            	$("#" + area).hide();	
            	$("#" + titleArea).hide();
				$("#" + area + "_img_btn").hide();
            }
		}
		
        function getMonitorData(ip){
        	if (targetMonitorData[ip]) {
        		drawPlot(ip);
				return;
        	}
        	
            $.ajax({
                url: "${req.getContextPath()}/perftest/api/${(test.id)?c}/monitor",
                dataType:'json',
                cache: true,
                data: {'targetIP': ip, 'imgWidth': 700},
                success: function(res) {\
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
                },
                error: function() {
                    showErrorMsg("Failed to get monitor graph data!");
                    return false;
                }
            });
        }
              

	</script>
	</body>
</html>
