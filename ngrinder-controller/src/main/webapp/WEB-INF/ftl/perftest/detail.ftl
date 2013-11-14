<!DOCTYPE html>
<html>
<head>
	<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
	<META HTTP-EQUIV="Expires" CONTENT="-1">
	<#include "../common/common.ftl"> 
	<#include "../common/jqplot.ftl">
	<title><@spring.message "perfTest.detail.title"/></title>
	<link href="${req.getContextPath()}/css/slider.css" rel="stylesheet">
	<link href="${req.getContextPath()}/plugins/datepicker/css/datepicker.css" rel="stylesheet">
	<style>
	div.div-resources {
		border: 1px solid #D6D6D6;
		height: 50px;
		margin-bottom: 8px;
		overflow-y: scroll;
		border-radius: 3px 3px 3px 3px;
	}
	
	div.div-resources .resource {
		width: 300px;
		color: #666666;
		display: block;
		margin-left: 7px;
		margin-top: 2px;
		margin-bottom: 2px;
	}
	
	.select-item {
		width: 60px;
	}
	
	.control-label input {
		vertical-align: top;
		margin-left: 2px
	}
	
	.controls code {
		vertical-align: middle;
	}
	
	.datepicker {
		z-index:1151;
	}
	
	
	div.chart {
		border: 1px solid #878988;
		margin-bottom: 12px;
	}
	
	
    div.modal-body div.chart {
		border:1px solid #878988; 
		height:250px; 
		min-width:500px; 
		margin-bottom:12px; 
		padding:5px 
    }
	
	.table thead th {
		vertical-align: middle;
	}
	
	.jqplot-yaxis {
	    margin-right: 20px; 
	}
	
	.jqplot-xaxis {
	    margin-top: 5px; 
	} 
	
	.rampup-chart {
		width: 430px;
		height: 355px
	}
	
	div.div-host {
		border: 1px solid #D6D6D6;
		height: 50px;
		margin-bottom: 8px;
		overflow-y: scroll;
		border-radius: 3px 3px 3px 3px;
	}
	
	div.div-host .host {
		color: #666666;
		display: inline-block;
		margin-left: 7px;
		margin-top: 2px;
		margin-bottom: 2px;
	}
	
	.add-host-btn {
		margin-right:20px;
		margin-top:-32px;
	}
	
	i.expand {
		background-image: url('${req.getContextPath()}/img/icon_expand.png');
		background-repeat:no-repeat;
		display: inline-block;
	    height: 16px;
	    width: 16px; 
	    line-height: 16px;
	    vertical-align: text-top;
	}
	
	i.collapse{
		background-image: url('${req.getContextPath()}/img/icon_collapse.png');
		background-repeat:no-repeat;
		display: inline-block;
	    height: 16px;
	    width: 16px;
	    line-height: 16px;
	    vertical-align: text-top;
	}
	
	#test_name + span {
		float: left;
	}
	
	#query_div label {
		width: 100px;
	}
	
	.form-horizontal .control-group {
		margin-bottom:10px;
	}
	
	.controls .span3 {
		margin-left: 0;
	}
	
	.control-group.success td > label[for="test_name"] {
		color: #468847;
	}
	
	.control-group.error td > label[for="test_name"] {
		color: #B94A48;
	}
	
	#script_control.error .select2-choice {
	    border-color: #B94A48;
	    color: #B94A48;
	}
	
	#script_control.success .select2-choice {
	    border-color: #468847;
	    color: #468847;
	}

	label.region {
		margin-left:-40px;
	}
	
	.monitor_state {
		line-height:12px \0/IE8+9;
	}
	.span4-5 {
		width: 340px;
	}
	.span3-4 {
		width: 260px;
	}
	.span2-3 {
		width: 180px;
	}
	</style>
</head>
  
<body>
	<#include "../common/navigator.ftl">
	<div class="container">
		<form id="test_config_form" name="test_config_form" action="${req.getContextPath()}/perftest/create" method="POST" 
    		style="margin-bottom: 0px">
			<div class="well">
				<input type="hidden" id="test_id" name="id" value="${(test.id)!}"> 
				<div class="form-horizontal" id="query_div">
					<fieldset>
						<div class="control-group">
							<div class="row">
								<div class="span4-5">
									<div class="control-group">
										<label for="test_name" class="control-label"><@spring.message "perfTest.configuration.testName"/></label>
										<#if test?? && test.testName??>
											<#assign initTestName = test.testName>
										<#elseif testName??> 
											<#assign initTestName = testName> 
										<#else>
				                 	   		<#assign initTestName = "">
			                    		</#if>
			                    		<div class="controls" style="margin-left: 120px;">
											<input class="required span3 left-float" maxlength="80" size="30" type="text" id="test_name" name="testName" value="${(initTestName)!}"/>
										</div>  
									</div>
								</div>
								<div class="span3-4">
									<div class="control-group">
										<label for="tag_string" class="control-label" style="width:60px;"><@spring.message "perfTest.configuration.tags"/></label>
										<div class="controls" style="margin-left: 40px;"> 
											<input class="span2-3" size="50" type="text" id="tag_string" name="tagString" value="${(test.tagString)!}">
										</div> 
									</div>  
								</div>
								<div class="span1">
									<#if test??> 
										<img id="test_status_img" 
										src="${req.getContextPath()}/img/ball/${test.status.iconName}"
										rel='popover'
										data-html='true'
										data-content='${"${test.progressMessage}<br/><b>${test.lastProgressMessage}</b>"?replace('\n', '<br>')?html}'  
										title="<@spring.message "${test.status.springMessageKey}"/>"
										data-placement='bottom'
										/>
									</#if>
								</div>
								<#if test??>
									<#if test.status != "SAVED" || test.createdUser.userId != currentUser.factualUser.userId>
										<#assign isClone = true/>
									<#else>
										<#assign isClone = false/> 
									</#if>
								<#else>
									<#assign isClone = false/> 
								</#if>
								
								<div class="span2-3" style="margin-left:0px"> 
									<div class="control-group">
										<input type="hidden" name="isClone" value="${isClone?string}"/>
										<#--  Save/Clone is available only when the test owner is current user.   -->
										<#if test?? && test.createdUser.userId != currentUser.factualUser.userId>
											<#assign disabled = "disabled">
										</#if>
										<button type="submit" class="btn btn-success" id="save_test_btn" style="width:55px" ${disabled!}>
											<#if isClone>
												<@spring.message "perfTest.detail.clone"/>
											<#else>
												<@spring.message "common.button.save"/>
											</#if> 
										</button>
										<button type="button" class="btn btn-primary" style="width:116px" id="save_schedule_btn" ${disabled!}>
											<#if isClone><@spring.message "perfTest.detail.clone"/><#else><@spring.message "common.button.save"/></#if>&nbsp;<@spring.message "perfTest.detail.andStart"/>
										</button>
									</div>
								</div>
							</div>
						</div>
						<div class="control-group" style="margin-bottom: 0">
							<label for="description" class="control-label"><@spring.message "common.label.description"/></label>
							<div class="controls" style="margin-left: 120px;">
								<textarea id="description" name="description" style="resize: none; width:751px; height:36px">${(test.description)!}</textarea>
							</div>
						</div>
					</fieldset>
				</div>
			</div>
			<!-- end well -->
			<@security.authorize ifAnyGranted="A, S">
				<#if test?? && test.createdUser?? && currentUser.userId != test.createdUser.userId>
					<div class="pull-right">
						<@spring.message "perfTest.table.owner"/> : ${test.createdUser.userName!""} (${test.createdUser.userId!""})		
					</div>
				</#if>
			</@security.authorize >
			<div class="tabbable" style="margin-top: 0px"> 
				<ul class="nav nav-tabs" id="homeTab" style="margin-bottom: 5px">
					<li id="test_config_section_tab">
						<a href="#test_config_section" data-toggle="tab">
							<@spring.message "perfTest.configuration.testConfiguration"/>
						</a>
					</li> 
					<li id="running_section_tab" style="display: none;">
						<a href="#running_section" data-toggle="tab" id="running_section_btn">
							<@spring.message "perfTest.testRunning.title"/>
						</a>
					</li>
				
					<li id="report_section_tab" style="display: none; ">
						<a href="#report_section" data-toggle="tab" id="report_btn">
							<@spring.message "perfTest.report.tab"/>
						</a>
					</li>
				</ul>
				<div class="tab-content">
					<div class="tab-pane" id="test_config_section">
						<#include "config.ftl">
					</div>
					
					<div class="tab-pane" id="report_section">
					</div>
					
					<div class="tab-pane" id="running_section">
						<#include "running.ftl">
					</div>
				</div>
				<!-- end tab content -->
			</div>
			<!-- end tabbable -->
			<input type="hidden" id="scheduled_time" name="scheduledTime" /> 
			<#if test??> 
				<input type="hidden" id="test_status" name="status" value="${(test.status)}">
				<input type="hidden" id="test_status_type" name="statusType" value="${(test.status.category)}"> 
			<#else>
				<input type="hidden" id="test_status" name="status" value="SAVED">
			</#if>
		</form>
		<#include "../common/copyright.ftl">
	</div>
	<!--end container-->

	<div class="modal hide fade" id="schedule_modal">
		<div class="modal-header">
			<a class="close" data-dismiss="modal">&times;</a>
			<h3>
				<@spring.message "perfTest.testRunning.scheduleTitle"/> <small class="error-color"></small>
			</h3>
		</div>
		<div class="modal-body">
			<div class="form-horizontal">
				<fieldset>
					<div class="control-group">
						<label class="control-label"><@spring.message "perfTest.testRunning.schedule"/></label>
						<div class="controls form-inline">
							<input type="text" class="input span2" id="scheduled_date" value="" readyonly>&nbsp; 
							<select id="scheduled_hour" class="select-item"></select> : <select id="scheduled_min" class="select-item"></select>
							<code>HH:MM</code>
						</div>
					</div>
				</fieldset>
			</div>
		</div>
		<div class="modal-footer">
			<a class="btn btn-primary" id="run_now_btn"><@spring.message "perfTest.testRunning.runNow"/></a> <a class="btn btn-primary" id="add_schedule_btn"><@spring.message "perfTest.testRunning.schedule"/></a>
		</div>
	</div>
    <#include "host_modal.ftl">
<script src="${req.getContextPath()}/plugins/datepicker/js/bootstrap-datepicker.js"></script>
<script src="${req.getContextPath()}/js/bootstrap-slider.min.js"></script>
<script src="${req.getContextPath()}/js/rampup.js?${nGrinderVersion}"></script>
<script src="${req.getContextPath()}/js/queue.js?${nGrinderVersion}"></script>
<script>
// vuser calc
${processthread_policy_script}

var jqplotObj;
var objTimer;
var testTpsData = new Queue();
var durationMap = [];

$(document).ready(function () {
	$.ajaxSetup({
		cache : false //close AJAX cache
	});
	initTags();
	initDuration();
	initScheduleDate();
	$("#sample_tab a:first").tab('show');
	$("#test_config_section_tab a").tab('show');
	
	addValidation();
	bindEvent();
	updateScript();
	updateTotalVuser();
	updateRampupChart();
	
	<#if test??>
		<#assign category = test.status.category>
		<#if category == "TESTING"> 
  			displayConfigAndRunningSection(); 
		<#elseif category == "FINISHED" || category == "STOP" || category == "ERROR"> 
			finished = true;
			displayConfigAndReportSection();
		<#else>
			displayConfigOnly(); 
		</#if>
	<#else>
		displayConfigOnly();
	</#if>
	(function refreshContent() {
		var ids = [];
		if (!testId || finished == true) {
			return;
		}

		$.ajax({
			url : '${req.getContextPath()}/perftest/api/<#if test??>${(test.id)?c}</#if>/status',
			type : 'GET',
			success : function(perfTestData) {
				perfTestData = eval(perfTestData);
				data = perfTestData.statusList;
				for ( var i = 0; i < data.length; i++) {
					updateStatus(data[i].id, data[i].status_type, data[i].name, data[i].icon, data[i].deletable, data[i].stoppable, data[i].message);
				}
			},
			complete : function() {
				setTimeout(refreshContent, 3000);
			}
		});
	})();
});

function formatTags(e) {
    if (e.added && (e.added.id.indexOf(",") >= 0 || e.added.id.indexOf(" ") >= 0)) {
        var tagControl = $("#tag_string");
        var values = tagControl.select2("val");
        var newValues = [];
        for (var i = 0; i < values.length; i++) {
        	var splitted = values[i].split(/[\s,]+/);
        	for (var j = 0; j < splitted.length; j++) {
        		newValues.push(splitted[j].replace("q_", "")); 
        	}
        } 
        
        tagControl.select2("val", newValues);
    }
}

function initTags() {
	$("#tag_string").select2({	
        tokenSeparators: [",", " "],
		tags:[""],
		placeholder: '<@spring.message "perfTest.configuration.tagInput"/>',
		initSelection : function (element, callback) {
   			var data = [];
  			$(element.val().split(",")).each(function () {
  			 	if (this.indexOf("q_") !== 0) {
   			    	data.push({id: "q_" + this, text: this});
   			    }
  			});
    		callback(data);
		},
		maximumSelectionSize: 5,
		query: function(query) {
			var data = {results:[]};
			$.ajax({
				url : "${req.getContextPath()}/perftest/search_tag",
				dataType : 'json',
				type : 'POST',
				cache : true,
				data : {
					'query' : query.term
				},
				success : function(res) {
					for (var i = 0; i < res.length; i++) {
						data.results.push({id:"q_" + res[i], text:res[i]});
					} 
					query.callback(data);
				}
			});
		}
	}).change(formatTags);
	
	$("#script_name").select2({
		placeholder: '<@spring.message "perfTest.configuration.scriptInput"/>'
	});
}

function initScheduleDate() {
	var date = getBrowserTimeApplyingTimezone();
	var year = date.getFullYear();
	var month = date.getMonth() + 1;
	var day = date.getDate();
	$("#scheduled_date").val(year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day));
	
	$('#scheduled_date').datepicker({
		format : 'yyyy-mm-dd'
	});
	
	$("#scheduled_hour").append(getOption(24));
	$("#scheduled_min").append(getOption(60));
}

function initDuration() {
	var sliderMax = 1000;
	durationMap[0] = 0;
	
	for ( var i = 1; i <= sliderMax; i++) {
		if (i <= 10) {
			durationMap[i] = durationMap[i - 1] + 1;
		} else if (i <= 20) {
			durationMap[i] = durationMap[i - 1] + 5;
		} else if (i <= 32) { //untill 180 min
			durationMap[i] = durationMap[i - 1] + 10;
		} else if (i <= 38) { //360 min
			durationMap[i] = durationMap[i - 1] + 30;
		} else if (i <= 56) { //24 hours
			durationMap[i] = durationMap[i - 1] + 60;
		} else if (i <= 72) {
			durationMap[i] = durationMap[i - 1] + 60 * 6;
		} else if (i <= 78) {
			durationMap[i] = durationMap[i - 1] + 60 * 12;
		} else {
			durationMap[i] = durationMap[i - 1] + 60 * 24;
		}
		if ((durationMap[i]/60) >= ${maxRunHour}) {
			 sliderMax = i;
		     durationMap[i] = (${maxRunHour} - 1) * 60;
		     break;
		}
	}
	
	var durationVal = $("#duration").val();
	$("#hidden_duration_input").attr("data-slider", "#duration_slider");
	$("#hidden_duration_input").slider({min:1, max:sliderMax});
	for ( var i = 0; i <= sliderMax; i++) {
		if (durationMap[i] * 60000 == durationVal) {
			$("#hidden_duration_input").val(i);
			break;
		}
	}
	
	var durationHour = parseInt(durationVal / 3600000) + 1;
	var durationMaxHour = durationHour > ${maxRunHour} ? durationHour : ${maxRunHour};
	$("#select_hour").append(getOption(durationMaxHour));
	$("#select_hour").change(getDurationMS);
	
	$("#select_min").append(getOption(60));
	$("#select_min").change(getDurationMS);
	
	$("#select_sec").append(getOption(60));
	$("#select_sec").change(getDurationMS);
	
	setDuration();
	setDurationHour(durationVal);
}

var validationOptions = {};
function addValidation() {
	$.validator.addMethod("paramFmt", function(param, element) {
		var pattern = /^[a-zA-Z0-9_\,\|]{0,30}$/;
		var rule = new RegExp(pattern);
		return rule.test($.trim(param));
	});
	
	validationOptions = {
		rules: {
			testName: { 
			 	required: true
			},
			agentCount: {
				required: true,
				digits: true,
				min: 0
			},
			vuserPerAgent: {
				required: true,
				digits: true,
				range: [1, ${(maxVuserPerAgent)}]
			},
			scriptName: {
	        	required: true
	        },
			durationHour: {
				max: ${maxRunHour}
			},
			ignoreSampleCount: {
				required: false,
				digits: true,
				min: 0
			},
			<#if securityMode?? && securityMode == true>
			targetHosts: {
				required: true
			},
			</#if>
			initProcesses: {
				required: true,
				digits: true
			},
			initSleepTime: {
				required: true,
				digits: true
			},
			processIncrement: {
				required: true,
				digits: true,
				min: 1
			},
			processIncrementInterval: {
				required: true,
				digits: true,
				min: 1
			},				
			runCount: {
				digits: true,
				max: ${maxRunCount}
			},
			param : {
				required:false,
				paramFmt:true
			}
		},
	    messages: { 
	        testName: {
	        	required: "<@spring.message 'perfTest.warning.testName'/>"
	        },
	        agentCount: {
	        	required: "<@spring.message 'perfTest.warning.agentNumber'/>"
	        },
	        vuserPerAgent: {
	        	required: "<@spring.message 'perfTest.warning.vuserPerAgent'/>"
	        },
	        scriptName: {
	        	required: "<@spring.message 'perfTest.warning.script'/>"
	        },
	        durationHour: {
	        	max: "<@spring.message 'perfTest.warning.duration.maxHour'/>"
	        },
	        runCount: {
	        	required: "<@spring.message 'perfTest.warning.runCount'/>"
	        },
	        processes: {
	        	required: "<@spring.message 'perfTest.warning.processes'/>"
	        },
	        threads: {
	        	required: "<@spring.message 'perfTest.warning.threads'/>"
	        },
	        targetHosts: {
	        	required: "<@spring.message 'perfTest.warning.hostString'/>"
	        },
	        param : {
	        	paramFmt: "<@spring.message 'perfTest.warning.param'/>"
	        }
	        
	    },
		ignore : "", // make the validation on hidden input work
		errorClass : "help-inline",
		errorElement : "span",
		errorPlacement : function(error, element) {
			var errorPlace = $("td." + element.attr("id"));
			if (errorPlace[0]) {
				errorPlace.html(error);
				return;
			}
			errorPlace = $("#err_"+element.attr("id"));
			if (errorPlace[0]) {
				errorPlace.html(error);
				return;
			} else {
				var $elem = element.closest(".controls");
				if ($elem[0]) {
					$elem.append(error);
				} else {
					element.parent().append(error);
				}
			}
		},
		highlight : function(element, errorClass, validClass) {
			var controlGroup = $(element).parents('.control-group');
			if (controlGroup.length >= 1) {
				$(controlGroup[0]).removeClass("success");
				$(controlGroup[0]).addClass("error");
			}
		},
		unhighlight : function(element, errorClass, validClass) {
			var $elem = $(element);
			var controlGroup = $elem.parents('.control-group');
			if (controlGroup.length >= 1) {
				var isSuccess = true;
				$elem.siblings("span.help-inline:visible").each(function() {
					if ($(this).attr("for") != $elem.attr("id")) {
						isSuccess = false;
						return;
					}
				});
				if (isSuccess) {
					$(controlGroup[0]).removeClass("error");
					$(controlGroup[0]).addClass("success");
				}
			}
		}
	};
	
	$("#test_config_form").validate(validationOptions);
}

function bindNewScript(target, first) {
		var $this = target;
		var $showScript = $("#show_script_btn");
		var $scriptRevision = $("#script_revision");
		var oldRevision = $scriptRevision.attr("old_revision");
		if ($this.val() == $this.attr("old_script") && oldRevision != -1) {
			$showScript.text("R " + oldRevision);
			$scriptRevision.val(oldRevision);
		} else {
			$showScript.text("R HEAD");
			$scriptRevision.val(-1);
		}
		$showScript.show();
		updateScriptResources(first);
}

function showScheduleModal() {
	if ($("#script_name option:selected").attr("validated") == "0") {
		$("small.error-color").text("<@spring.message "perfTest.detail.message.notValidatedScript"/>");
	} else {
		$("small.error-color").text("");
	}
  	initScheduleTime();
	$("#tag_string").val(buildTagString());
	$('#schedule_modal').modal('show');
}


function getBrowserTimeApplyingTimezone(time) {
	var resultDate;
	if (time === undefined) {
		var date = new Date();
		return new Date(date.getTime() + (date.getTimezoneOffset() * 60 * 1000) + ${timezone_offset});
	} else {
		resultDate = new Date(time - ${timezone_offset});
		// Now it's browser time reflecting the timezone difference.
		return new Date(Date.UTC(resultDate.getFullYear(), resultDate.getMonth(), resultDate.getDate(), resultDate.getHours(), resultDate.getMinutes())); 
	}
}	
	
	
function bindEvent() {
	$("#script_name").change(function() {
		bindNewScript($(this), false);
	});
	
	$("#hidden_duration_input").bind("slide", function(e) {
		var maxIndex = durationMap.length - 1;
		var $duration = $("#duration");
		if (maxIndex == this.value) {
			$duration.val((durationMap[maxIndex] + 59) * 60000 + 59000);
		} else {
			$duration.val(durationMap[this.value] * 60000);
		}
		setDuration(); 
		$("#duration_ratio").click();
	});
	
	$("#save_schedule_btn").click(function() {		
		$("#agent_count").rules("add", {
			min:1
		}); 
		if (!validateForm()) {
			return false;
		}
		if (typeof(scheduleTestHook) != "undefined") {
			if (scheduleTestHook()) {
				showScheduleModal();
			}
		} else if (typeof(scheduleTestUnBlockingHook) != "undefined") {
			scheduleTestUnBlockingHook(showScheduleModal);
		} else {
			showScheduleModal(); 
		}
	});
	
	
	$("#save_test_btn").click(function() {
		$("#agent_count").rules("add", {
			min:0
		});
		
		if (!validateForm()) {
			return false;
		}

		$("#test_status").val("SAVED");
		$("#scheduled_time").attr('name', '');
		$("#tag_string").val(buildTagString());
		
		return true;
	});
	
	$("#run_now_btn").click(function() {
		$("#schedule_modal").modal("hide");
		$("#schedule_modal small").html("");
		$("#scheduled_time").attr('name', '');
		$("#test_status").val("READY");
		document.test_config_form.submit();
	});

	
	
	$("#add_schedule_btn").click(function() {
		if (checkEmptyByID("scheduled_date")) {
			$("#schedule_modal small").html("<@spring.message "perfTest.detail.message.setScheduleDate"/>");
			return;
		}
	
		var timeStr = $("#scheduled_date").val() + " " + $("#scheduled_hour").val() + ":" + $("#scheduled_min").val() + ":0";
		// User input date time.
		var scheduledTime = new Date(timeStr.replace(/-/g, "/"));
		scheduledTime = getBrowserTimeApplyingTimezone(scheduledTime.getTime());
		if (new Date() > scheduledTime) {
			$("#schedule_modal small").html("<@spring.message "perfTest.detail.message.errScheduleDate"/>");
			return;
		}
		$("#scheduled_time").val(scheduledTime);
		$("#schedule_modal").modal("hide");
		$("#schedule_modal small").html("");
		$("#test_status").val("READY");
		document.test_config_form.submit();
	});
	
	$("#run_count_radio").click(function() {
		if ($(this).attr("checked") == "checked") {
			var $runCount = $("#run_count");
			$runCount.rules("add", {
				min:1
			});
			$runCount.valid();

			var $durationHour = $("#duration_hour");
			if (!$durationHour.valid()) {
				var maxVal = ${maxRunHour} * 3600000;
				$("#duration").val(maxVal);
				setDuration();
				setDurationHour(maxVal);
			}
			$durationHour.valid();
		}
	});
	
	$("#duration_ratio").click(function() {
		if ($(this).attr("checked") == "checked") {
			setDurationHour($("#duration").val());
			$("#duration_hour").valid();
			var $duration = $("#duration");
			$duration.addClass("positiveNumber");
			$duration.valid();
			
			var $runCount = $("#run_count");
			$runCount.rules("add", {
				min:0
			});
			if (!$runCount.valid()) {
				$runCount.val(0);
			}
			$runCount.valid();
		}
	});
	
	$("#ignore_sample_count, #run_count").blur(function() {
		if (!($.trim($(this).val()))) {
			$(this).val(0);
		}
	});
	
	$("#agent_count").change(function() {
		updateTotalVuser();
	});
	
	$("#threads, #processes").change(function() {
		var $vuer = $("#vuser_per_agent");
		$vuer.val($("#processes").val() * $("#threads").val());
		if ($vuer.valid()) {
			updateVuserGraph();
			updateTotalVuser();
		}
	});
	
	$("#vuser_per_agent").change(function() {
		var $vuserElement = $(this);
		var processCount = $("#processes").val();
		if ($vuserElement.valid()) {
			var result = updateVuserPolicy($vuserElement.val());
			$(this).val(result[0] * result[1]);
			if (processCount != result[0]) {
				updateVuserGraph();
			}
			updateTotalVuser();
		}
	});
	
	$("#report_btn").click(function() {
		$("#footer").hide();
		openReportDiv(function() {
			$("#footer").show();
		});
	});
	
	$('#sample_tab a').click(function(e) {
		e.preventDefault();
		$(this).tab('show');
	});

	$("#show_script_btn").click(function() {
		var currentScript = $("#script_name").val();
		if (currentScript) {
			var ownerId = ""; 
			<@security.authorize ifAnyGranted="A, S">					
				<#if test??>
					ownerId = "&ownerId=${(test.createdUser.userId)!}";
				</#if>
			</@security.authorize>
			var scriptRevision = $("#script_revision").val();
			var openedWindow = window.open("${req.getContextPath()}/script/detail/" + currentScript + "?r=" + scriptRevision + ownerId, "scriptSource");
			openedWindow.focus(); 
		}
	});
	
	$("#expand_collapse_btn").click(function() {
		$(this).toggleClass("collapse");
		$("#process_thread_config_panel").toggle();
	});
	
	$("#select_hour, #select_min, #select_sec").change(function() {
		$("#duration_ratio").click();
	});
	
	$("#run_count").focus(function() {
		$("#run_count_radio").click();
	});
	
	$('#message_div').ajaxSend(function(e, xhr, settings) {
		var url = settings.url;
		if ((url.indexOf("resource") > 0 || url.indexOf("script") > 0)) {
			showProgressBar("<@spring.message "perfTest.detail.message.updateResource"/>");
		}
	});
<#if clustered>
	var $region = $("#region");
	$region.select2();
	$region.change(function(){
		changeAgentMaxCount($(this).val(), true);
	});
	changeAgentMaxCount($region.val(), false);
<#else>
	changeAgentMaxCount("NONE", false);
</#if>	
}
var agentCountMap = {};
<#list regionAgentCountMap?keys as key>
agentCountMap["${key}"] = ${regionAgentCountMap[key]};
</#list>

function changeAgentMaxCount(region, isValid) {
	var count = agentCountMap[region];
	if (count === undefined) {
		count = 0;
	}
	$("#maxAgentCount").text(count);

	var $agentCountObj = $("#agent_count");
	$agentCountObj.rules("add", {
		max: count
	});
	
	if (isValid) {
		$agentCountObj.valid();
	}
}

function validateForm() {
	var result = true;
	// For IE 7, 8
	if ($.browser.msie  && parseInt($.browser.version, 10) <= 8) {
		var rules = validationOptions["rules"];
		$.each(rules, function(key, value) {
			if (!$("select[name='" + key +"'],input[name='" + key +"']").valid() && result == true) { 
				result = false;
			}
		});		
	} else {
		result = $("#test_config_form").valid();
	}
	if (!result) {
		$("#test_config_section_tab a").tab('show');
	}
	
	return result;
}

function buildTagString() {
	return $.map($("#tag_string").select2("data"), function(obj) {
		return obj.text;
	}).join(",");
}
	
function updateTotalVuser() {
	$("#total_vuser").text($("#agent_count").val() * $("#vuser_per_agent").val());
}

function initChartData(size) {
	for ( var i = 0; i < size; i++) {
		testTpsData.enQueue(0);
	}
}

function updateScript() {
	$.ajax({
		url : "${req.getContextPath()}/perftest/api/script",
		dataType : 'json',
		data : {
			<@security.authorize ifAnyGranted="A, S">
			<#if test??>'ownerId' : '${test.createdUser.userId}'</#if> 
			</@security.authorize>
		},
		success : function(res) {
			$scriptSelection = $("#script_name");
			var selectedScript = $scriptSelection.attr("old_script");
			var exists = false;
			for (var i = 0; i < res.length; i++) {
				if (selectedScript == res[i].path) {
					exists = true;
				}
				$scriptSelection.append($("<option value='" + res[i].path + "' revision='" + res[i].revision + "' validated='" + res[i].validated + "'>" + res[i].pathInShort + "</option>"));	
			}
			if (exists) {
				$scriptSelection.select2("val", selectedScript);
			} else if (selectedScript) {
				$scriptSelection.append($("<option value='' revision='-1' validated='false'>(deleted)" + selectedScript +"</option>"));
				$scriptSelection.select2("val", ""); 
			} else {
				$scriptSelection.append($("<option value='' revision='-1' validated='false'>" + selectedScript +"</option>"));
				$scriptSelection.select2("val", ""); 
			}
		
			bindNewScript($scriptSelection, true);
			hideProgressBar();
		},
		error : function() {
			showErrorMsg("<@spring.message "common.error.error"/>");
			return false;
		}
	});
}

function updateScriptResources(first) {
	var scriptName = $("#script_name").val();
	if (!scriptName) {
		return;
	}
	
	$.ajax({
		url : "${req.getContextPath()}/perftest/api/resource",
		dataType : 'json',
		data : {
			'scriptPath' : scriptName,
			'r' : $("#script_revision").val()
			<@security.authorize ifAnyGranted="A, S">
			<#if test??>,'ownerId' : '${test.createdUser.userId}'</#if> 
			</@security.authorize>
		},
		success : function(res) {
			var html = "";
			var len = res.resources.length;
			if (first == false) {
				initHosts(res.targetHosts);
			}
			for ( var i = 0; i < len; i++) {
				var value = res.resources[i];
				html = html + "<div class='resource ellipsis' title='" + value + "'>" + value + "</div>";
			}
			$("#scriptResources").html(html);
		},
		complete : function() {
			hideProgressBar();
		},
		error : function() {
			showErrorMsg("<@spring.message "common.error.error"/>");
			return false;
		}
	});
}

function updateVuserPolicy(vuser) {
	var processCount = getProcessCount(vuser);
	var threadCount = getThreadCount(vuser);
	var $processes = $('#processes');
	$processes.val(processCount);
	$processes.valid();
	var threads = $('#threads');
	threads.val(threadCount);
	threads.valid();
	return [ processCount, threadCount ];
}

function updateVuserGraph() {
	//if ramp-up chart is not enabled, update init process count as total 
	if ($("#use_ramp_up")[0].checked) {
		updateRampupChart();
	}
}

function setDuration() {
	var duration = $("#duration").val();
	var durationInSec = parseInt(duration / 1000);
	var durationH = parseInt(durationInSec / 3600);
	var durationM = parseInt((durationInSec % 3600) / 60);
	var durationS = durationInSec % 60;

	$("#select_hour").val(durationH);
	$("#select_min").val(durationM);
	$("#select_sec").val(durationS);
}

function getDurationMS() {
	var durationH = parseInt($("#select_hour").val());
	var durationM = parseInt($("#select_min").val());
	var durationS = parseInt($("#select_sec").val());
	var durationMs = (durationS + durationM * 60 + durationH * 3600) * 1000;
	var $durationObj = $("#duration");
	$durationObj.val(durationMs);
	$durationObj.valid(); //trigger validation
	return durationMs;
}

function getOption(cnt) {
	var contents = [];
	var index;
	for (i = 0; i < cnt; i++) {
		contents.push("<option value='" + i + "'>" + (i < 10 && cnt > 9 ? "0" + i : i) + "</option>");
	}
	return contents.join("\n");
}

function openReportDiv(onFinishHook) {
	$("#report_section").load("${req.getContextPath()}/perftest/<#if test??>${(test.id)?c}<#else>0</#if>/basic_report?imgWidth=600",
		function() {
			if (onFinishHook !== undefined) {
				onFinishHook();
			}
		}
	);
}

function updateStatus(id, statusType, statusName, icon, deletable, stoppable, message) {
	var $testStatusImg = $("#test_status_img");
	if ($testStatusImg.attr("data-content") != message) {
		$testStatusImg.attr("data-content", message);
	}
	var $testStatusType = $("#test_status_type");
	if ($testStatusType.val() == statusType) {
		return;
	}
	var testStatusImgPopover = $testStatusImg.data('popover');
	$testStatusImg.attr("data-original-title", statusName);
	testStatusImgPopover.options.content = message;
	

	$testStatusType.val(statusType);

	if ($testStatusImg.attr("src") != "${req.getContextPath()}/img/ball/" + icon) {
		$testStatusImg.attr("src", "${req.getContextPath()}/img/ball/" + icon);
	}

	if (statusType == "TESTING") {
		displayConfigAndRunningSection();
	} else if (statusType == "FINISHED" || statusType == "STOP_BY_ERROR"|| statusType == "STOP_ON_ERROR" || statusType == "CANCELED") {
		finished = true; 
		// Wait and run because it takes time to transfer logs.
		setTimeout('displayConfigAndReportSection()', 3000);
	} else {
		displayConfigOnly();
	}
}

var finished = false;
var testId = $('#test_id').val();
// Wrap this function in a closure so we don't pollute the namespace


function displayConfigOnly() {
	$("#test_config_section_tab a").tab('show');
	$("#running_section_tab").hide();
	$("#report_section_tab").hide();
}

var samplingInterval = 1;

function displayConfigAndRunningSection() {
	$("#running_section_tab").show();
	$("#running_section_tab a").tab('show');
	$("#running_section").show();
	$("#report_section_tab").hide();
	samplingInterval = $("#sampling_interval").val();
	initChartData(60 / samplingInterval);
	refreshData();
	objTimer = window.setInterval("refreshData()", 1000 * samplingInterval);
}

function displayConfigAndReportSection() {
	$("#foot_div").hide();
	$("#running_section_tab").hide();
	$("#report_section_tab").show();
	$("#report_section_tab a").tab('show');
	openReportDiv(function() {
		$("#foot_div").show();
	});
	if (objTimer) {
		window.clearInterval(objTimer);
	}
}

function initScheduleTime() {
	var date = getBrowserTimeApplyingTimezone();
	$("#scheduled_hour").val(date.getHours());
	$("#scheduled_min").val(date.getMinutes());
}

function setDurationHour(durationVal) {
	var durationHour = parseInt(durationVal / 3600000);
	durationHour = durationVal % 3600000 == 0 ? durationHour : durationHour + 1;
	$("#duration_hour").val(durationHour);
}
</script>
	</body>
</html>
