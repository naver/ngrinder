<#import "../common/spring.ftl" as spring/>
<div class="row">
	<div class="span4">
		<script type="text/javascript">
			
		</script>
		<div class="page-header">
			<h4><@spring.message "perfTest.report.summary"/></h4>
		</div>
		<div class="form-horizontal form-horizontal-3"
			style="margin-left: 10px">
			<fieldset>
				<div class="control-group">
					<label for="agentInput" class="control-label control-label-1"><@spring.message "perfTest.report.tps"/></label>
					<div class="controls">
						<strong><#if test.tps??>${(test.tps)?string(",##0.#")}</#if></strong>
					</div>
				</div>
				<div class="control-group">
					<label for="agentInput" class="control-label"><@spring.message "perfTest.report.meantime"/></label>
					<div class="controls">
						${(test.meanTestTime!0)?string("0.##")}
						<code>MS</code>
					</div>
				</div>
				<div class="control-group">
					<label for="agentInput" class="control-label"><@spring.message "perfTest.report.peakTPS"/></label>
					<div class="controls">${test.peakTps!0}</div>
				</div>
				<div class="control-group">
					<label for="agentInput" class="control-label"><@spring.message "perfTest.report.finishedTest"/></label>
					<div class="controls">${test.tests!0}</div>
				</div>
				<div class="control-group">
					<label for="agentInput" class="control-label"><@spring.message "perfTest.report.errors"/></label>
					<div class="controls">${test.errors!0}</div>
				</div>
				<div class="control-group">
                    <label for="agentInput" class="control-label"><@spring.message "perfTest.report.runtime"/></label>
                    <div class="controls">${test.runtimeStr!""}</div>
                </div>
			</fieldset>
		</div>
	</div>
	<div class="span8">
		<div class="page-header">
			<h4><@spring.message "perfTest.report.tpsgraph"/></h4>
			<a id="reportDetail" class="btn pull-right btn-small btn-primary" style="margin-top: -25px;margin-right:10px">
				<@spring.message "perfTest.report.reportDetail"/></a>
		</div>
		<div id="tpsDiv" class="chart" style="width: 610px; height: 300px"></div> 
	</div>
</div>
<div class="row" >
	<div class="span4">
		<div class="page-header">
			<h4><@spring.message "perfTest.report.logs"/></h4>
			<span style="margin-top:-20px" class="pull-right" rel="popover" data-content='<@spring.message "perfTest.report.logs.help"/>' data-original-title='<@spring.message "perfTest.report.logs"/>' type="toggle" id="log_comment"><i class="icon-question-sign"></i></span>
		</div>
		<div style="mgin-left: 10px">
			<#if logs?has_content> 
				<#list logs as eachLog>
					<div style="width:100%;" class="ellipsis">
						<a href="${req.getContextPath()}/perftest/downloadLog/${eachLog}?testId=${test.id?c}">${eachLog}</a>
					</div>
				</#list> 
			<#else> 
				<@spring.message "perfTest.report.message.noLog"/>
			</#if>
		</div>
	</div>
	<div class="span8">
		<div class="page-header">
			<h4><@spring.message "perfTest.report.longtestcomment"/></h4>
		</div>
		<div class="control-group">
			<button class="btn btn-small btn-primary pull-right" type="button"
				 style="margin-top:-60px;margin-right:10px" id="leaveCommentButton"><@spring.message "perfTest.report.leaveComment"/></button>
			<textarea class="span8" id="testComment" rows="3" name="testComment"
				style="resize: none"> ${(test.testComment)!} </textarea>
			
		</div>
	</div>
</div>
<script>
	$("#log_comment").hover(popover, popunover);
	drawChart('TPS', 'tpsDiv', ${TPS![]}, null,  ${chartInterval!1});
	$("#leaveCommentButton").click(function(){
		var comment = $("#testComment").val();
		var tagString = buildTagString();
		$.post(
			"${req.getContextPath()}/perftest/leaveComment",
			{"testId": ${test.id?c}, "testComment": comment, "tagString":tagString},
			function() {
				showSuccessMsg("<@spring.message "perfTest.report.message.leaveComment"/>");
			}
		);
	});

	$("#reportDetail").click(function () {
		window.open("${req.getContextPath()}/perftest/report?testId=" + $("#testId").val());
	});
</script>
