<#import "../common/spring.ftl" as spring/>
<div class="row">
	<div class="span4">
		<fieldset>
			<legend>
				<@spring.message "perfTest.report.summary"/>
			</legend>
		</fieldset>
		<div class="form-horizontal form-horizontal-3"
			style="margin-left: 10px">
			<fieldset>
				<div class="control-group">
					<label class="control-label non-cursor"><@spring.message "perfTest.report.tps"/></label>
					<div class="controls">
						<strong><#if test.tps??>${(test.tps)?string(",##0.#")}</#if></strong>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label non-cursor"><@spring.message "perfTest.report.meantime"/></label>
					<div class="controls">
						${(test.meanTestTime!0)?string("0.##")}
						<code>MS</code>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label non-cursor"><@spring.message "perfTest.report.peakTPS"/></label>
					<div class="controls">${test.peakTps!0}</div>
				</div>
				<div class="control-group">
					<label class="control-label non-cursor"><@spring.message "perfTest.report.totalTests"/></label>
					<div class="controls">${(test.tests + test.errors)!0}</div>
				</div>
				<div class="control-group">
					<label class="control-label non-cursor"><@spring.message "perfTest.report.successfulTests"/></label>
					<div class="controls">${test.tests!0}</div>
				</div>
				<div class="control-group">
					<label  class="control-label non-cursor"><@spring.message "perfTest.report.errors"/></label>
					<div class="controls">${test.errors!0}</div>
				</div>
				<div class="control-group">
                    <label class="control-label non-cursor"><@spring.message "perfTest.report.runtime"/></label>
                    <div class="controls">${test.runtimeStr!""}</div>
                </div>
			</fieldset>
		</div>
	</div>
	<div class="span8">
		<fieldSet>
			<legend>
				<@spring.message "perfTest.report.tpsgraph"/>
				<a id="detail_report_btn" class="btn btn-primary pull-right">
					<@spring.message "perfTest.report.reportDetail"/>
				</a>
			</legend>
		</fieldSet>
		<div id="tps_chart" class="chart" style="width: 610px; height: 300px"></div> 
	</div>
</div>
<div class="row" >
	<div class="span4">
		<fieldSet>
			<legend>
				<@spring.message "perfTest.report.logs"/>
				<span style="margin-top:10px;margin-left:10px" 
					rel="popover"
					data-html="true" 
					data-content='<@spring.message "perfTest.report.logs.help"/>' 
					title='<@spring.message "perfTest.report.logs"/>' type="toggle" id="log_comment"><i class="icon-question-sign pointer-cursor"></i></span>
			</legend>
		</fieldSet>
		<div style="mgin-left: 10px">
			<#if logs?has_content> 
				<#list logs as eachLog>
					<div style="width:100%;" class="ellipsis">
						<a href="${req.getContextPath()}/perftest/${test.id?c}/show_log/${eachLog}" target="log" title="open the log in the new window"><img src="${req.getContextPath()}/img/open_external.png" style="margin-top:-3px"></a>  <a href="${req.getContextPath()}/perftest/${test.id?c}/download_log/${eachLog}">${eachLog}</a>
					</div>
				</#list> 
			<#else> 
				<@spring.message "perfTest.report.message.noLog"/>
			</#if>
		</div>
	</div>
	<div class="span8">
		<fieldSet>
			<legend>
				<@spring.message "perfTest.report.longtestcomment"/>
				<a id="leave_comment_btn" class="btn btn-primary pull-right">
					<@spring.message "perfTest.report.leaveComment"/>
				</a>
			</legend>
		</fieldSet>
		<textarea class="span8" id="test_comment" rows="3" name="testComment" style="resize: none"> ${(test.testComment)!} </textarea>
	</div>
</div>
<script>
	$("#log_comment").popover({trigger: 'hover', container:'body'});
	drawChart('tps_chart', ${TPS![]}, null,  ${chartInterval!1}).replot();
	$("#leave_comment_btn").click(function(){
		var comment = $("#test_comment").val();
		var tagString = buildTagString();
		$.post(
			"${req.getContextPath()}/perftest/${(test.id)?c}/leave_comment",
			{"testComment": comment, "tagString":tagString},
			function() {
				showSuccessMsg("<@spring.message "perfTest.report.message.leaveComment"/>");
			}
		);
	});

	$("#detail_report_btn").click(function () {
		window.open("${req.getContextPath()}/perftest/${(test.id)?c}/detail_report");
	});
</script>
