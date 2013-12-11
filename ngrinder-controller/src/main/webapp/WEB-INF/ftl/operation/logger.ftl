<!DOCTYPE html>
<html>
<head>
<#include "../common/common.ftl">
<#include "../common/datatables.ftl">
<title><@spring.message "log.view.title"/></title>
</head>

<body>
	<#include "../common/navigator.ftl">
	<div class="container">
		<div class="row">
			<div class="span12">
				<legend class="header">
					<@spring.message "navigator.dropdown.logMonitoring"/>
				</legend>
				<table id="log_container">
				</table>
			</div>
		</div>
		<#include "../common/copyright.ftl">
	</div>
	<script>
		// Wrap this function in a closure so we don't pollute the namespace
		(function pollingLogs() {
			var ajaxObj = new AjaxObj("/operation/log/last");
			ajaxObj.success = function(data) {
				var eachLog = $("tr#" +data.index + " td");
				if (eachLog.size() != 0) {
					if (eachLog.attr("id") != data.modification) {
						eachLog.html(data.log);
						eachLog.attr("id", data.modification);
					}
				} else {
					var logEntries = $("#log_container tr");
					if (logEntries.size() > 5) {
						logEntries.first().remove();
					}
					$("#log_container").append($("<tr id='" + data.index + "'><td id='" + data.modification + "'>" + data.log + "</td></tr>"));
				}
				setTimeout(pollingLogs, 5000);
			};
            ajaxObj.call();
		})();
	</script>
</body>
</html>