<!DOCTYPE html>
<html>
    <head>
        <title>nGrinder Agent Info</title>
       	<#include "../common/common.ftl">
       	<#include "../common/jqplot.ftl">
       	<style>
            body {
                padding-top: 60px;
            }   
            .left { border-right: 1px solid #878988 }
            div.chart { border: 1px solid #878988; height:250px; min-width:615px; margin-bottom:12px; padding: 5px }
        </style>
    </head>

    <body>
        <#include "../common/navigator.ftl">
        <div class="container">
            <div class="row" style="margin-bottom:10px;">
                <div class="span9">
                   <h3>Agent Info</h3>
                </div>
                <div class="span2 offset1">
                    <button class="btn pull-right" title="Return" id="returnBtn">Return</button>&nbsp;&nbsp;
                    <button class="btn" title="Refresh monitor data" id="refreshBtn">Refresh</button>
                </div>
            </div>
            <div class="row">
                <div class="span3">
                    <table class="table table-bordered table-striped" style="border-top:#cccccc solid 1px">
				    <tbody>
				    <tr>
				    <th>IP</th>
				    </tr>
                    <tr>
				    <td>${(agent.ip)!}</td>
				    </tr>
				    <tr>
				    <th>Number</th>
				    </tr>
                    <tr>
				    <td>${(agent.number)!}</td>
				    </tr>
				    <tr>
				    <th>Name</th>
				    </tr>
                    <tr>
				    <td>${(agent.appName)!}</td>
				    </tr>
				    <th>Region</th>
				    </tr>
                    <tr>
				    <td>${(agent.region)!}</td>
				    </tr>
				    </tbody>
				    </table>
				    <label>Refresh interval (second)</label>
                    <input id="rinterval" type="text" class="span3" placeholder="number" value="30">
                </div>
                <div class="span9">
					<div class="tabbable" style="margin-left:20px">
                        <ul class="nav nav-tabs" id="chartTab">
                            <li class="active"><a href="#systemData" data-toggle="tab">System Data</a></li>
                            <li><a href="#javaData" data-toggle="tab">Java Data</a></li>
                        </ul>
                        <div class="tab-content">
                            <div class="tab-pane active" id="systemData">
                                <div class="chart" id="cpuDiv"></div>
                                <div class="chart" id="memoryDiv"></div>
                            </div>
                            <div class="tab-pane" id="javaData">
                                <div class="chart" id="heapMemoryDiv"></div>
                                <div class="chart" id="nonHeapMemoryDiv"></div>
                                <div class="chart" id="threadCountDiv"></div>
                                <div class="chart" id="jvmCpuDiv"></div>
                            </div>
					     </div>
                    </div>
                </div>
            </div>
        	<#include "../common/copyright.ftl">
    	<!--content-->
        </div>
        <script>
            var interval;
            var timer;
            $(document).ready(function() {
                $("#returnBtn").on('click', function() {
                    history.back();
                });
                $("#refreshBtn").on('click', function() {
                    getMonitorData();
                });
                $("#rinterval").keyup(function() {
                    var number = $(this).val();
                    $(this).val(number.replace(/[\D]/g,""))
                });
                $("#rinterval").blur(function() {
                    if(timer){
                        window.clearInterval(timer);
                    }
                    interval = $(this).val();
                    if(interval == 0){
                        interval = 30;
                    }
                    timer=window.setInterval("getMonitorData()",interval * 1000);
                });
                getMonitorData();
                $("#rinterval").change();
                $('#chartTab a').click(function () {
					resetFooter();
				});
            });
            function getMonitorData(){
                $.ajax({
                    url: "${req.getContextPath()}/monitor/getMonitorData",
                    dataType:'json',
                    data: {'ip': '${(agent.ip)!}',
                           'imgWidth':700},
                    success: function(res) {
                        if (res.success) {
                            showChart('CPU', 'cpuDiv', res.cpu);
                            showChart('Memory', 'memoryDiv', res.memory);
                            showChart('Heap Memory', 'heapMemoryDiv', res.heap_memory);
                            showChart('NonHeap Memory', 'nonHeapMemoryDiv', res.non_heap_memory);
                            showChart('Thread Count', 'threadCountDiv', res.thread_count);
                            showChart('CPU', 'jvmCpuDiv', res.jvm_cpu);
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
            
            function showChart(title, id, data) {
                $("#" + id).empty();
                drawChart(title, id, data);
            }
        </script>
    </body>
</html>