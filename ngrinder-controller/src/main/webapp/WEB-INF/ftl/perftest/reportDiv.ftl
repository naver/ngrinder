<#import "../common/spring.ftl" as spring/>
<div class="row">
	<div class="span4">
		<input type="hidden" id="tpsData" name="tpsData" value="${(TPS)!}">
		<div class="page-header">
			<h4><@spring.message "perfTest.report.summary"/></h4>
		</div>
		<div class="form-horizontal form-horizontal-3"
			style="margin-left: 10px">
			<fieldset>
				<div class="control-group">
					<label for="agentInput" class="control-label control-label-1"><@spring.message "perfTest.table.tps"/></label>
					<div class="controls">
						<strong>Total ${(test.tps)!}</strong>
					</div>
				</div>
				<div class="control-group">
					<label for="agentInput" class="control-label"><@spring.message "perfTest.table.meantime"/></label>
					<div class="controls">
						${(test.meanTestTime)!}
						<code>MS</code>
					</div>
				</div>
				<div class="control-group">
					<label for="agentInput" class="control-label"><@spring.message "perfTest.detail.peakTPS"/></label>
					<div class="controls">${(test.peakTps)!}</div>
				</div>
				<div class="control-group">
					<label for="agentInput" class="control-label"><@spring.message "perfTest.report.finishedTest"/></label>
					<div class="controls">${(test.tests)!}</div>
				</div>
				<div class="control-group">
					<label for="agentInput" class="control-label"><@spring.message "perfTest.table.errors"/></label>
					<div class="controls">${(test.errors)!}</div>
				</div>
			</fieldset>
		</div>
	</div>
	<div class="span8">
		<div class="page-header">
			<h4><@spring.message "perfTest.report.tpsgraph"/></h4>
			<a id="reportDetail" class="btn pull-right" style="margin-top: -25px">
				<@spring.message "perfTest.report.reportDetail"/></a>
		</div>
		<div id="tpsDiv" class="chart" style="width: 610px; height: 240px"></div>
	</div>
</div>
<div class="row" style="margin-top: 10px;">
	<div class="span4">
		<div class="page-header">
			<h4><@spring.message "perfTest.report.logs"/></h4>
		</div>
		<div style="margin-left: 10px">
			<#if logs?has_content> <#list logs as eachLog>
			<div>
				<a
					href="${req.getContextPath()}/perftest/downloadLog/${eachLog}?testId=${test.id}">${eachLog}</a>
			</div>
			</#list> <#else> <@spring.message "common.message.noData"/>
			</#if>
		</div>
	</div>
	<div class="span8">
		<div class="page-header">
			<h4><@spring.message "perfTest.report.testcomment"/></h4>
		</div>
		<div class="control-group">
			<textarea class="span8" id="testComment" rows="3" name="testComment"
				style="resize: none"> ${(test.testComment)!} </textarea>
			<button class="btn btn-small btn-primary pull-right" type="button"
				id="leaveCommentButton"><@spring.message
				"perfTest.report.leaveComment"/></button>
		</div>
	</div>
</div>
<script>
	$("#leaveCommentButton").click(function(){
		var comment = $("#testComment").val();
		$.post("${req.getContextPath()}/perftest/leaveComment",
					{"testId": ${test.id}, "testComment": comment},
				function() {
					showSuccessMsg("<@spring.message "perfTest.report.message.leaveComment"/>");
				}
		);
	});

	$("#reportDetail").click(function () {
		window.open("${req.getContextPath()}/perftest/report?testId=" + $("#testId").val());
	});
</script>
