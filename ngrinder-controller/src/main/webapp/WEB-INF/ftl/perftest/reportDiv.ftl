<#import "../common/spring.ftl" as spring/>
<div class="row">
	<div class="span4">
		<input type="hidden" id="tpsData" name="tpsData" value="${TPS!}">
		<div class="page-header">
			<h4><@spring.message "perfTest.report.summary"/></h4>
		</div>
		<div class="form-horizontal form-horizontal-3"
			style="margin-left: 10px">
			<fieldset>
				<div class="control-group">
					<label for="agentInput" class="control-label control-label-1"><@spring.message "perfTest.report.tps"/></label>
					<div class="controls">
						<strong>Total <#if test.tps??>${(test.tps)?string("0.#")}</#if></strong>
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
		</div>
		<div style="margin-left: 10px">
			<#if logs?has_content> 
				<#list logs as eachLog>
					<div style="width:100%;" class="ellipsis">
						<a href="${req.getContextPath()}/perftest/downloadLog/${eachLog}?testId=${test.id?c}">${eachLog}</a>
					</div>
				</#list> 
			<#else> 
				<@spring.message "common.message.noData"/>
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
	function buildTagString() {
		var k = $("#tagString").select2("data");
		var tagString = [];
		for (var i = 0; i < k.length; i++) {
		    if (jQuery.inArray(k[i].text, tagString) == -1) {
		    	tagString.push(k[i].text);
		    }
		}
		return tagString.join(",");
	}

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
