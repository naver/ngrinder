<!DOCTYPE html>
<html>
<head>
<#include "../common/common.ftl">
<#include "../common/datatables.ftl">
<title><@spring.message "log.view.header"/></title>
</head>

<body>
	<#include "../common/navigator.ftl">
	<div class="container">
		<div class="row">
			<div class="span12">
				<div class="page-header pageHeader">
					<h3><@spring.message "log.view.header"/></h3>
				</div> 
				<table id="log_container">
				</table>
			</div>
		</div>
		<#include "../common/copyright.ftl">
	</div>
	<script>
		// Wrap this function in a closure so we don't pollute the namespace
		(function pollingLogs() {
		    $.ajax({
			    url: '${req.getContextPath()}/operation/log/last', 
			    type: 'GET',
			    cache: false,
			    success: function(data) {
			    	var eachLog = $("tr#" +data.index + " td");
			    	if (eachLog.size() != 0) {
			    		if (eachLog.attr("id") != data.modification) {
			    			eachLog.html(data.log);
			    			eachLog.attr("id", data.modification);
			    		}
			    	} else {
			    		var logentries = $("#log_container tr");
			    		if (logentries.size() > 5) {
			    			logentries.first().remove();
			    		}
			    		$("#log_container").append($("<tr id='" + data.index + "'><td id='" + data.modification + "'>" + data.log + "</td></tr>"));
			    	}
			    	
			    	setTimeout(pollingLogs, 5000);
			    }
		    });
	  })();
	</script>
</body>
</html>