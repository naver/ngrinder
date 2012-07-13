<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <title>nGrinder Agent Info</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="description" content="nGrinder Agent Info">
        <meta name="author" content="AlexQin">

        <link rel="shortcut icon" href="favicon.ico"/>
        <link href="${req.getContextPath()}/css/bootstrap.min.css" rel="stylesheet">
        <style>
            body {
                padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
            }
        </style>
        
        <input type="hidden" id="contextPath" value="${req.getContextPath()}">
        <#setting number_format="computer">
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
                    <table class="table table-bordered" style="border-top:#cccccc solid 1px">
				    <tbody>
				    <tr>
				    <th>IP</th>
				    </tr>
                    <tr>
				    <td>${(agent.ip)!}</td>
				    </tr>
				    <tr>
				    <th>Port</th>
				    </tr>
                    <tr>
				    <td>${(agent.appPort)!}</td>
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
                </div>
                <div class="span9">
					<div class="tabbable">
                        <ul class="nav nav-tabs">
                            <li class="active"><a href="#systemData" data-toggle="tab">System Data</a></li>
                            <li><a href="#javaData" data-toggle="tab">Java Data</a></li>
                        </ul>
                        <div class="tab-content">
                            <div class="tab-pane active" id="systemData">
                                <ul class="thumbnails">
                                  <li class="span3">
                                    <div class="thumbnail">
                                      <img src="../img/260x180.gif" alt="">
                                      <h5>Cpu</h5>
                                    </div>
                                  </li>
                                  <li class="span3">
                                    <div class="thumbnail">
                                      <img src="../img/260x180.gif" alt="">
                                      <h5>Memory</h5>
                                    </div>
                                  </li>
                                </ul>
                            </div>
                            <div class="tab-pane" id="javaData">
                                <ul class="thumbnails">
								  <li class="span3">
								    <div class="thumbnail">
								      <img src="../img/260x180.gif" alt="">
								      <h5>Heap Memory</h5>
								    </div>
								  </li>
								  <li class="span3">
                                    <div class="thumbnail">
                                      <img src="../img/260x180.gif" alt="">
                                      <h5>NonHeap Memory</h5>
                                    </div>
                                  </li>
                                  <li class="span3">
                                    <div class="thumbnail">
                                      <img src="../img/260x180.gif" alt="">
                                      <h5>Thread Count</h5>
                                    </div>
                                  </li>
                                  <li class="span3">
                                    <div class="thumbnail">
                                      <img src="../img/260x180.gif" alt="">
                                      <h5>Cpu</h5>
                                    </div>
                                  </li>
								</ul>
                            </div>
					     </div>
                    </div>
                </div>
            </div>
        	<#include "../common/copyright.ftl">
    	<!--content-->
        </div>
        <script src="${req.getContextPath()}/js/jquery-1.7.2.min.js"></script>
        <script src="${req.getContextPath()}/js/bootstrap.min.js"></script>
        <script src="${req.getContextPath()}/js/utils.js"></script>
        <script>
            $(document).ready(function() {
                $("#returnBtn").on('click', function() {
                    history.back();
                });
                $("#refreshBtn").on('click', function() {
                    $.ajax({
                        url: "#",
                        context: document.body,
                        dataObject: {id: "001"},
                        success: function(){
                            alert("Refresh success!");
                            }
                    });
                });
            });
        </script>
    </body>
</html>