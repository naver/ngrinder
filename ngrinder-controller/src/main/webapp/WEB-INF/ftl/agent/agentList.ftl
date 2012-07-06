<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <title>nGrinder Agent List</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="description" content="nGrinder Agent List">
        <meta name="author" content="Tobi">

        <link rel="shortcut icon" href="favicon.ico"/>
        <link href="${Request.getContextPath()}/css/bootstrap.min.css" rel="stylesheet">
        <link href="${Request.getContextPath()}/css/bootstrap-responsive.min.css" rel="stylesheet">
        
        <style>
            body {
                padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
            }
        </style>
        
        <input type="hidden" id="contextPath" value="${Request.getContextPath()}">
        <#setting number_format="computer">
    </head>

    <body>
        <#include "../common/navigator.ftl">
        <div class="container">
            <div class="row">
                <div class="span10 offset1">
                    <div class="row">
                        <div class="span10">
                            <a class="btn" href="#uploadScriptModal" id="connectBtn" data-toggle="modal">
                                <i class="icon-ok-circle"></i>
                                Connect
                            </a>
                            <a class="btn" href="#uploadScriptModal" id="disconnectBtn" data-toggle="modal">
                                <i class="icon-remove-circle"></i>
                                Disconnect
                            </a>
                            <div class="form-inline pull-right" >
                                <input type="text" class="search-query" placeholder="Keywords" id="searchText" value="${keywords!}">
                                <button type="submit" class="btn" id="searchBtn"><i class="icon-search"></i>Search</button>
                            </div>
			                <table class="table table-condensed" id="scriptTable" style="margin-bottom:10px;">
			                    <colgroup>
			                        <col width="30">
			                        <col width="100">
			                        <col width="110">
			                        <col width="110">
			                        <col width="60">
			                        <col width="60">
			                        <col width="60">
			                        <col width="160">
			                        <col width="20">
			                    </colgroup>
			                    <thead>
			                        <tr>
			                            <th class="center"><input type="checkbox" class="checkbox" value=""></th>
			                            <th>IP | Domain</th>
			                            <th>Application Port</th>
			                            <th>Application Name</th>
			                            <th>Type</th>
			                            <th>Region</th>
			                            <th>Status</th>
			                            <th class="noClick">Operation</th>
			                            <th></th>
			                            <th></th>
			                        </tr>
			                    </thead>
			                    <tbody>
			                        <#assign agentList = agents.content/>
			                        <#if agentList?has_content>
			                        <#list agentList as agent>
			                        <tr>
			                            <td><input type="checkbox" value="${agent.id}"></td>
			                            <td class="left"><a href="${Request.getContextPath()}/agent/detail?id=${agent.id}" target="_self">${agent.ip}</a></td>
			                            <td>${(agent.appPort)!}</td>
			                            <td>${(agent.appName)!}</td>
			                            <td>${(agent.type)!}</td>
			                            <td>${(agent.region)!}</td>
			                            <td>${(agent.status)!}</td>
			                            <td>
											<div class="btn-group" data-toggle="buttons-radio">
											    <button class="btn btn-small" title="Connect this agent">Connect</button>
											    <button class="btn btn-small" title="Disconnect this agent">Disconnect</button>
											</div>
			                            </td>
			                            <td>
			                                <a href="javascript:void(0);" title="Delete this agent"><i class="icon-remove"></i></a>
			                            </td>
			                        </tr>
			                        </#list>
			                        <#else>
			                            <tr>
			                                <td colspan="8">
			                                    No data to display.
			                                </td>
			                            </tr>
			                        </#if>
			                    </tbody>
			                </table>
                        </div>
                    </div>
                <!--content-->
                <#include "../common/copyright.ftl">
                </div>
            </div>
        </div>
        <script src="${Request.getContextPath()}/js/jquery-1.7.2.min.js"></script>
        <script src="${Request.getContextPath()}/js/jquery.form.js"></script>
        <script src="${Request.getContextPath()}/js/bootstrap.min.js"></script>
        <script src="${Request.getContextPath()}/js/utils.js"></script>
        <script>
            $(document).ready(function() {
            
                function showRequest(formData, jqForm, options) { 
                    alert('About to submit: \n\n' ); 
                    return true; 
                } 
                function showResponse(responseText, statusText)  { 
                    alert('status: ' + statusText + '\n\nresponseText: \n' + responseText + 
                        '\n\nThe output div should have already been updated with the responseText.'); 
                } 
                
	            var options = { 
			        //target:        '#output2',   // target element(s) to be updated with server response 
			        beforeSubmit:  showRequest,  // pre-submit callback 
			        success:       showResponse  // post-submit callback 
			 
			        // other available options: 
			        //url:       url         // override for form's 'action' attribute 
			        //type:      type        // 'get' or 'post', override for form's 'method' attribute 
			        //dataType:  null        // 'xml', 'script', or 'json' (expected server response type) 
			        //clearForm: true        // clear all form fields after successful submit 
			        //resetForm: true        // reset the form after successful submit 
			 
			        // $.ajax options can be used here too, for example: 
			        //timeout:   3000 
			    }; 
			    
			    
                $("#saveBtn").on('click', function() {
	                document.forms.createForm.submit();
	                //$('#createForm').ajaxSubmit(options); 
	                //return false; 
	            });
                
            });
        </script>
    </body>
</html>