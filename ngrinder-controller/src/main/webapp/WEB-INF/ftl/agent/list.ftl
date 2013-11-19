<!DOCTYPE html>
<html>
<head><#include "../common/common.ftl"> <#include "../common/datatables.ftl">
<title><@spring.message "agent.table.title"/></title>
</head>
<body>
	<#include "../common/navigator.ftl">
	<div class="container">
		<fieldSet>
			<legend class="header"> <@spring.message "agent.management.title"/> </legend>
		</fieldSet>
		<#include "region_selector.ftl">
		<div class="well search-bar">
            <button class="btn btn-success" id="update_agent_button">
                <i class="icon-thumbs-up"></i> <@spring.message "agent.management.agentUpdate"/>
            </button>
            <button class="btn" id="stop_agent_button">
                <i class="icon-stop"></i> <@spring.message "common.button.stop"/>
            </button>
			<div class="input-prepend pull-right">
				<span class="add-on" style="cursor: default"><@spring.message "agent.management.agentDownload"/> </span>
				<span class="input-xlarge uneditable-input span6" style="cursor: text"> 
					<#if downloadLinks?has_content> 
						<#list downloadLinks as each>
							<div>
								<a href="${each}">${each}</a>
							</div> 
						</#list>
					</#if>
				</span>
			</div>
		</div>

		<table class="table table-striped table-bordered ellipsis" id="agent_table">
			<colgroup>
				<col width="30">
				<col width="150">
				<col width="60">
				<col width="*">
				<col width="200">
				<col width="100">
				<col width="165">
			</colgroup>
			<thead>
				<tr>
					<th class="no-click"><input type="checkbox" class="checkbox" value=""></th>
					<th><@spring.message "agent.table.IPAndDns"/></th>
					<th class="no-click"><@spring.message "agent.table.port"/></th>
					<th class="ellipsis"><@spring.message "agent.table.name"/></th>
					<th><@spring.message "agent.table.region"/></th>
					<th><@spring.message "agent.table.state"/></th>
					<th class="no-click"><@spring.message "agent.table.approve"/></th>
				</tr>
			</thead>
			<tbody>
				<#if agents?has_content>
					<#list agents as agent>
						<tr>
							<td class="center"><input type="checkbox" class="checkbox" value="${agent.id}"></td>
							<td><a href="${req.getContextPath()}/agent/${agent.id}" target="_self">${agent.ip}</a></td>
							<td>${(agent.port)!}</td>
							<td class="ellipsis" title="${(agent.hostName)!}">${(agent.hostName)!}</td>
							<td>${(agent.region)!}</td>
							<td>${(agent.state)!}</td>
							<td>
								<div class="btn-group" data-toggle="buttons-radio">
									<button type="button" 
									    class="btn btn-mini btn-primary disapproved <#if agent.isApproved() == false>active</#if>"
										sid="${agent.id}">
										<@spring.message "agent.table.disapproved"/>
									</button>
									<button type="button" 
									    class="btn btn-mini btn-primary approved <#if agent.isApproved() == true>active</#if>"
										sid="${agent.id}">
										<@spring.message "agent.table.approved"/>
									</button>
								</div>
							</td>
						</tr>
					</#list> 
				<#else>
					<tr>
						<td colspan="7" class="center"><@spring.message "common.message.noData"/></td>
					</tr>
				</#if>
			</tbody>
		</table>
		<#include "../common/copyright.ftl">
		<!--content-->
	</div>
	<script>
            $(document).ready(function() {
		        <#if agents?has_content>

                    var oTable = $("#agent_table").dataTable({
                        "bAutoWidth": false,
                        "bFilter": false,
                        "bLengthChange": false,
                        "bInfo": false,
                        "iDisplayLength": 10,
                        "aaSorting": [[1, "asc"]],
                        "aoColumns": [{"asSorting": []}, null, {"asSorting": []}, null, null, null, {"asSorting": []}],
                        "sPaginationType": "bootstrap",
                        "oLanguage": {
                            "oPaginate": {
                                "sPrevious": "<@spring.message "common.paging.previous"/>",
                                "sNext":     "<@spring.message "common.paging.next"/>"
                            }
                        }
                    });

                    removeClick();
                    enableChkboxSelectAll("agent_table");

                    $(".approved").live("click", function() {
                        var sid = $(this).attr("sid");
                        $.post(
                            "${req.getContextPath()}/agent/" + sid + "/approve",
                            {
                                "approve": "true"
                            },
                            function() {
                                showSuccessMsg("<@spring.message "agent.management.toBeApproved"/>");
                            }
                         );
                    });

                    $(".disapproved").live("click", function() {
                        var sid = $(this).attr("sid");
                        $.post(
                            "${req.getContextPath()}/agent/" + sid + "/approve",
                            {
                                "approve": "false"
                            },
                            function() {
                                showSuccessMsg("<@spring.message "agent.management.toBeDisapproved"/>");
                            }
                         );
                    });
				</#if>
				
				$("#stop_agent_button").click(function() {
					var ids = "";
					var list = $("td input:checked");
					if(list.length == 0) {
						bootbox.alert("<@spring.message "agent.table.message.alert.stop"/>", "<@spring.message "common.button.ok"/>");
						return;
					}
					
					var $confirm = bootbox.confirm("<@spring.message "agent.table.message.confirm.stop"/>", "<@spring.message "common.button.cancel"/>", "<@spring.message "common.button.ok"/>", function(result) {
					    if (result) {
							stopAgents(list.map(function() {
								return $(this).val();
							}).get().join(","));
					    }
					});
					$confirm.children(".modal-body").addClass("error-color");
				});

                $("#update_agent_button").click(function() {
                    var list = $("td input");
                    if(list.length == 0) {
                        bootbox.alert("there is no agent should be updated", "<@spring.message "common.button.ok"/>");
                        return;
                    }
                    updateAgents();
                });
				
            });
			
			function stopAgents(ids) {
				$.ajax({
			  		url: "${req.getContextPath()}/agent/api/stop",
			  		type: "POST",
			  		data: {"ids" : ids},
			  		cache: false,
					dataType:'json',
			    	success: function(res) {
						showSuccessMsg("<@spring.message "agent.table.message.success.stop"/>");
						setTimeout(function() {
							location.reload();
						}, 2000);
			    	},
			    	error: function() {
			    		showErrorMsg("<@spring.message "agent.table.message.error.stop"/>!");
			    	}
			  	});
			}

            function updateAgents() {
                $.ajax({
                    url: "${req.getContextPath()}/agent/api/update",
                    type: "GET",
                    cache: false,
                    dataType:'json',
                    success: function(res) {
                        if (res.success) {
                            showSuccessMsg("<@spring.message "agent.table.message.success.update"/>");
                        } else {
                            showErrorMsg("<@spring.message "agent.table.message.error.update"/>:" + res.message);
                        }
                    },
                    error: function() {
                        showErrorMsg("<@spring.message "agent.table.message.error.update"/>");
                    }
                });
            }
			
	     </script>
</body>
</html>
