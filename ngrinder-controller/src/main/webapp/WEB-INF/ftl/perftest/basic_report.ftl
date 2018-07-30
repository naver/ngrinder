<#import "../common/spring.ftl" as spring/>
<#include "../common/ngrinder_macros.ftl">
<style>
	.form-horizontal-3 .control-label-wide {
		width:170px;
	}
</style>
<div class="row report">
	<div class="span4 intro" data-step="4" data-intro="<@spring.message 'intro.report.summary'/>">
		<fieldset>
			<legend>
				<@spring.message "perfTest.report.summary"/>
            </legend>
		</fieldset>
		<div class="form-horizontal form-horizontal-3"
			style="margin-left: 10px">
			<fieldset>

				<@control_group lable_extra_class="control-label-wide non-cursor" label_message_key="perfTest.report.totalVusers">
					<strong>${test.agentCount * test.vuserPerAgent}</strong>
				</@control_group>

				<@control_group lable_extra_class="control-label-wide non-cursor" label_message_key="perfTest.report.tps">
            		<strong>${(test.tps!0)?string(",##0.#")}</strong>
				</@control_group>

				<@control_group lable_extra_class="control-label-wide non-cursor" label_message_key="perfTest.report.peakTPS">
					${test.peakTps!0}
				</@control_group>


				<@control_group lable_extra_class="control-label-wide non-cursor" label_message_key="perfTest.report.meantime">
				${(test.meanTestTime!0)?string("0.##")}
					<code>MS</code>
				</@control_group>

				<@control_group lable_extra_class="control-label-wide non-cursor" label_message_key="perfTest.report.totalTests">
					${(test.tests!0) + (test.errors!0)}
				</@control_group>

				<@control_group lable_extra_class="control-label-wide non-cursor" label_message_key="perfTest.report.successfulTests">
					${test.tests!0}
				</@control_group>

				<@control_group lable_extra_class="control-label-wide non-cursor" label_message_key="perfTest.report.errors">
					${test.errors!0}
				</@control_group>

				<@control_group lable_extra_class="control-label-wide non-cursor" label_message_key="perfTest.report.runtime">
					${test.runtimeStr!""}
				</@control_group>
			</fieldset>
		</div>
	</div>
	<div class="span8 intro" data-step="5" data-intro="<@spring.message 'intro.report.tpsGraph'/>">
		<fieldSet>
			<legend>
				<@spring.message "perfTest.report.tpsGraph"/>
				<a id="detail_report_btn" class="btn btn-primary pull-right">
					<@spring.message "perfTest.report.detailedReport"/>
				</a>
			</legend>
		</fieldSet>
		<div id="tps_chart" class="chart" style="width: 610px; height: 300px"></div> 
	</div>
</div>
<div class="row report">
	<div class="span4">
		<fieldSet>
			<legend>
				<@spring.message "perfTest.report.logs"/>
				<span style="margin-top:10px;margin-left:10px" 
					rel="popover"
					data-html="true" 
					data-content='<@spring.message "perfTest.report.logs.help"/>' 
					title='<@spring.message "perfTest.report.logs"/>' id="log_comment"><i class="icon-question-sign pointer-cursor"></i></span>
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
	<div class="span8 intro" data-step="6" data-intro="<@spring.message 'intro.report.testComment'/>">
		<fieldSet>
			<legend>
				<@spring.message "perfTest.report.testComment"/>
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
	new Chart('tps_chart', [${TPS![]}], ${chartInterval!1}).plot();
	$("#leave_comment_btn").click(function(){
		var comment = $("#test_comment").val();
		var tagString = buildTagString();
		var ajaxObj = new AjaxPostObj("/perftest/${(test.id)?c}/leave_comment",
				{ "testComment": comment, "tagString":tagString },
				"<@spring.message "perfTest.report.message.leaveComment"/>"
		);
		ajaxObj.call();
	});

	$("#detail_report_btn").click(function () {
		window.open("${req.getContextPath()}/perftest/${(test.id)?c}/detail_report");
	});

	//@ sourceURL=/perftest/basic_report
</script>
