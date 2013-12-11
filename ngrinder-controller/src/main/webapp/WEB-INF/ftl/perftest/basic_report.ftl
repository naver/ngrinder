<#import "../common/spring.ftl" as spring/>
<#include "../common/ngrinder_macros.ftl">
<style>
	.form-horizontal-3 .control-label-wide {
		width:170px;
	}
</style>
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
				<@control_group lable_extra_class="control-label-wide non-cursor" label_message_key="perfTest.report.tps">
            		<strong><#if test.tps??>${(test.tps)?string(",##0.#")}</#if></strong>
				</@control_group>

				<@control_group lable_extra_class="control-label-wide non-cursor" label_message_key="perfTest.report.meantime">
					${(test.meanTestTime)?string("0.##")}
					<code>MS</code>
				</@control_group>

				<@control_group lable_extra_class="control-label-wide non-cursor" label_message_key="perfTest.report.peakTPS">
					${test.peakTps}
				</@control_group>

				<@control_group lable_extra_class="control-label-wide non-cursor" label_message_key="perfTest.report.totalTests">
					${test.tests + test.errors}
				</@control_group>

				<@control_group lable_extra_class="control-label-wide non-cursor" label_message_key="perfTest.report.successfulTests">
					${test.tests}
				</@control_group>

				<@control_group lable_extra_class="control-label-wide non-cursor" label_message_key="perfTest.report.errors">
					${test.errors}
				</@control_group>

				<@control_group lable_extra_class="control-label-wide non-cursor" label_message_key="perfTest.report.runtime">
					${test.runtimeStr}
				</@control_group>
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
			<@list list_items=logs others="message" message="perfTest.report.message.noLog"; eachLog >
				<div style="width:100%;" class="ellipsis">
					<a href="${req.getContextPath()}/perftest/${test.id?c}/show_log/${eachLog}" target="log"
					   title="open the log in the new window">
					<img src="${req.getContextPath()}/img/open_external.png" style="margin-top:-3px"></a>
					<a href="${req.getContextPath()}/perftest/${test.id?c}/download_log/${eachLog}">${eachLog}</a>
				</div>
			</@list>
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
