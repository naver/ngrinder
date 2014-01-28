<!DOCTYPE html>
<html>
<head>
<#include "../common/common.ftl"/>
<#include "../common/datatables.ftl"/>
	<title><@spring.message "agent.title"/></title>
</head>
<body>
<div id="wrap">
<#include "../common/navigator.ftl">
<div class="container">
	<fieldSet>
		<legend class="header"> <@spring.message "agent.list.title"/> </legend>
	</fieldSet>
	<#include "region_selector.ftl">
	<div class="well search-bar">
		<button class="btn btn-success" id="update_agent_button">
			<i class="icon-arrow-up"></i> <@spring.message "agent.list.update"/>
		</button>
			<button class="btn" id="cleanup_agent_button">
				<i class="icon-trash"></i> <@spring.message "common.button.cleanup"/>
			</button>
		<button class="btn" id="stop_agent_button">
			<i class="icon-stop"></i> <@spring.message "common.button.stop"/>
		</button>

		<div class="input-prepend pull-right">
			<span class="add-on" style="cursor: default">
				<@spring.message "agent.list.download"/>
			</span>
			<span class="input-xlarge uneditable-input span6" style="cursor: text">
				<#if downloadLink??>
					<a href="${req.getContextPath()}${downloadLink}">${downloadLink}</a>
				<#else>
					Please select the region in advance to download agent.
				</#if>
			</span>
		</div>

	</div>

	<table class="table table-striped table-bordered ellipsis" id="agent_table">
		<colgroup>
			<col width="30">
			<col width="80">
			<col width="130">
			<col width="60">
			<col width="*">
			<col width="100">
			<col width="150">
			<col width="160">
		</colgroup>
		<thead>
		<tr>
			<th class="no-click"><input type="checkbox" class="checkbox" value=""></th>
			<th><@spring.message "agent.list.state"/></th>
			<th><@spring.message "agent.list.IPAndDns"/></th>
			<th class="no-click"><@spring.message "agent.list.port"/></th>
			<th class="ellipsis"><@spring.message "agent.list.name"/></th>
			<th><@spring.message "agent.list.version"/></th>
			<th><@spring.message "agent.list.region"/></th>
			<th class="no-click"><@spring.message "agent.list.approved"/></th>
		</tr>
		</thead>
		<tbody>
		<@list list_items=agents others="table_list" colspan="8"; agent>
		<tr>
			<td class="center">
				<input type="checkbox" class="agent-state checkbox" status="${(agent.state)!}" value="${agent.id}">
			</td>
			<td class="center" id="row_${agent.id}">
				<div class="ball" id="ball_${agent.id}"
					 data-html="true"
					 rel="popover">
					<img class="status" src="${req.getContextPath()}/img/ball/${agent.state.iconName}"/>
				</div>
			</td>
			<td>
				<div class="ellipsis" title="${agent.ip}">
					<a href="${req.getContextPath()}/agent/${agent.id}" target="_self" value="${agent.ip}">${agent.ip}</a>
				</div>
			</td>
			<td id="port_${agent.id}">${(agent.port)!}</td>
			<td class="ellipsis agent-name" title="${(agent.hostName)!}">${(agent.hostName)!}</td>
			<td class="ellipsis"><#if agent.version?has_content>${agent.version}<#else>Prior to 3.3</#if></td>
			<td class="ellipsis" <#if (agent.region)??>title="${(agent.region)!}"</#if> >${(agent.region)!}</td>
			<td>
				<div class="btn-group" data-toggle="buttons-radio">
					<button type="button"
							class="btn btn-mini btn-primary disapproved <#if agent.isApproved() == false>active</#if>"
							sid="${agent.id}">
						<@spring.message "agent.list.disapproved"/>
					</button>
					<button type="button"
							class="btn btn-mini btn-primary approved <#if agent.isApproved() == true>active</#if>"
							sid="${agent.id}">
						<@spring.message "agent.list.approved"/>
					</button>
				</div>
			</td>
		</tr>
		</@list>
		</tbody>
	</table>
	<!--content-->
</div>
</div>
<#include "../common/copyright.ftl">
<script>
	$(document).ready(function () {
		var $agentTable = $("#agent_table");
	<#if agents?has_content>
		$agentTable.dataTable({
			"bAutoWidth": false,
			"bFilter": false,
			"bLengthChange": false,
			"bInfo": false,
			"iDisplayLength": 10,
			"aaSorting": [
				[2, "asc"]
			],
			"aoColumns": [null, null, {"asSorting": []}, {"asSorting": []}, null, null, null, {"asSorting": []}],
			"sPaginationType": "bootstrap",
			"oLanguage": {
				"oPaginate": {
					"sPrevious": "<@spring.message 'common.paging.previous'/>",
					"sNext": "<@spring.message 'common.paging.next'/>"
				}
			}
		});

		removeClick();
		enableCheckboxSelectAll("agent_table");

		$agentTable.on("click", ".approved", function () {
			new AjaxPutObj("/agent/api/" + $(this).attr("sid") + "?action=approve",
					{},
					"<@spring.message 'agent.message.approve'/>"
			).call();
		});

		$agentTable.on("click", ".disapproved", function () {
			new AjaxPutObj("/agent/api/" + $(this).attr("sid") + "?action=disapprove",
					{},
					"<@spring.message 'agent.message.disapprove'/>"
			).call();
		});
	</#if>

		$("#stop_agent_button").click(function () {
			var list = $("td input:checked");
			if (list.length == 0) {
				bootbox.alert(
						"<@spring.message 'agent.message.common.noagent'/>",
						"<@spring.message 'common.button.ok'/>");
				return;
			}

			var $confirm = bootbox.confirm("<@spring.message 'agent.message.stop.confirm'/>",
					"<@spring.message 'common.button.cancel'/>", "<@spring.message 'common.button.ok'/>", function (result) {
				if (result) {
					stopAgents(list.map(function () {
						return $(this).val();
					}).get().join(","));
				}
			});
			$confirm.children(".modal-body").addClass("error-color");
		});

		$("#cleanup_agent_button").click(function() {
			var $confirm = bootbox.confirm("<@spring.message 'agent.message.cleanup.confirm'/>",
				"<@spring.message 'common.button.cancel'/>", "<@spring.message 'common.button.ok'/>", function (result) {
					if (result) {
						cleanup();
					}
				});
		});
		$("#update_agent_button").click(function () {
			var list = $("td input:checked");
			if (list.length == 0) {
				bootbox.alert("<@spring.message 'agent.message.common.noagent'/>",
						"<@spring.message 'common.button.ok'/>");
				return;
			}
			var $confirm = bootbox.confirm("<@spring.message 'agent.message.update.confirm'/>",
					"<@spring.message 'common.button.cancel'/>", "<@spring.message 'common.button.ok'/>", function (result) {
				if (result) {
					updateAgents(list.map(function () {
						return $(this).val();
					}).get().join(","));
				}
			});
			$confirm.children(".modal-body").addClass("error-color");
		});
	});

	function cleanup() {
		var ajaxObj = new AjaxPostObj("/agent/api?action=cleanup",
				{},
				"<@spring.message 'agent.message.cleanup.success'/>",
				"<@spring.message 'agent.message.cleanup.error'/>");
		ajaxObj.success = function () {
			setTimeout(function () {
				location.reload();
			}, 2000);
		};
		ajaxObj.call();
	}
	function stopAgents(ids) {
		var ajaxObj = new AjaxPutObj("/agent/api?action=stop",
				{ ids : ids },
				"<@spring.message 'agent.message.stop.success'/>",
				"<@spring.message 'agent.message.stop.error'/>");
		ajaxObj.success = function () {
			setTimeout(function () {
				location.reload();
			}, 2000);
		};
		ajaxObj.call();
	}

	function updateAgents(ids) {
		var ajaxObj = new AjaxPutObj("/agent/api?action=update",
				{ ids : ids },
				"<@spring.message 'agent.message.update.success'/>",
				"<@spring.message 'agent.message.update.error'/>");
		ajaxObj.call();
	}

	(function updateStatuses() {
		var ids = $('input.agent-state').map(function () {
			return this.value;
		}).get();

		var ajaxObj = new AjaxObj("/agent/api/states", null, "<@spring.message 'common.error.error'/>");
		ajaxObj.success = function (data) {
			for (var i = 0; i < data.length; i++) {
				updateStatus(data[i].id, data[i].icon, data[i].port, data[i].state);
			}
			if (ids.length == 0) {
				return;
			}
			setTimeout(updateStatuses, 2000);
		};
		ajaxObj.call();
	})();

	function updateStatus(id, icon, port, state) {
		var $ballImg = $("#ball_" + id + " img");
		if ($ballImg.attr("src") != "${req.getContextPath()}/img/ball/" + icon) {
			$ballImg.attr("src", "${req.getContextPath()}/img/ball/" + icon);
			$("#port_" + id).html(port);
		}
		$("#ball_" + id).attr("data-original-title", state);
	}
</script>
</body>
</html>
