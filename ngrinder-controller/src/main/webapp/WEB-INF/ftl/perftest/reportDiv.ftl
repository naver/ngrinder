<#import "../common/spring.ftl" as spring/>
<div class="row">
	<div class="span4">
		<script type="text/javascript">
			
		</script>
		<legend>
			<@spring.message "perfTest.report.summary"/>
		</legend>
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
		<legend>
			<@spring.message "perfTest.report.tpsgraph"/>
			<a id="reportDetail" class="btn btn-primary pull-right">
				<@spring.message "perfTest.report.reportDetail"/>
			</a>
		</legend>
		<div id="tpsDiv" class="chart" style="width: 610px; height: 300px"></div> 
	</div>
</div>
<div class="row" >
	<div class="span4">

		<legend>
			<@spring.message "perfTest.report.logs"/>
			<span  style="margin-top:10px;margin-left:10px" rel="popover" data-content='<@spring.message "perfTest.report.logs.help"/>' data-original-title='<@spring.message "perfTest.report.logs"/>' type="toggle" id="log_comment"><i class="icon-question-sign"></i></span>
		</legend>
		<div style="mgin-left: 10px">
			<#if logs?has_content> 
				<#list logs as eachLog>
					<div style="width:100%;" class="ellipsis">
						<a href="${req.getContextPath()}/perftest/${test.id?c}/showLog/${eachLog}" target="log" title="open the log in the new window"><img src="${req.getContextPath()}/img/open_external.png" style="margin-top:-3px"></a>  <a href="${req.getContextPath()}/perftest/${test.id?c}/downloadLog/${eachLog}">${eachLog}</a>
					</div>
				</#list> 
			<#else> 
				<@spring.message "perfTest.report.message.noLog"/>
			</#if>
		</div>
	</div>
	<div class="span8">
		<legend>
			<@spring.message "perfTest.report.longtestcomment"/>
			<a id="leaveCommentButton" class="btn btn-primary pull-right">
				<@spring.message "perfTest.report.leaveComment"/>
			</a>
		</legend>
		<textarea class="span8" id="testComment" rows="3" name="testComment"
			style="resize: none"> ${(test.testComment)!} </textarea>
	</div>
</div>
<script>
	$("#log_comment").hover(popover, popunover);
	drawChart('tpsDiv', ${TPS![]}, null,  ${chartInterval!1}).replot(); 
	$("#leaveCommentButton").click(function(){
		var comment = $("#testComment").val();
		var tagString = buildTagString();
		$.post(
			"${req.getContextPath()}/perftest/${(test.id)?c}/leaveComment",
			{"testComment": comment, "tagString":tagString},
			function() {
				showSuccessMsg("<@spring.message "perfTest.report.message.leaveComment"/>");
			}
		);
	});

	$("#reportDetail").click(function () {
		window.open("${req.getContextPath()}/perftest/${(test.id)?c}/report");
	});
</script>
