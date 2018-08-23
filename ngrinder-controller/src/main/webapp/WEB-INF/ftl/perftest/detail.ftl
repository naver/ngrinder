<!DOCTYPE html>
<html>
<head>
	<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
	<META HTTP-EQUIV="Expires" CONTENT="-1">
	<#include "../common/common.ftl"> 
	<#include "../common/jqplot.ftl">
	<title><@spring.message "perfTest.title"/></title>
	<link href="${req.getContextPath()}/css/slider.css" rel="stylesheet">
	<link href="${req.getContextPath()}/plugins/datepicker/css/datepicker.css" rel="stylesheet">
	<style>
	.popover {
		width: auto;
		min-width: 200px;
		max-width: 600px;
		max-height: 500px;
	}
	
	.select-item {
		width: 60px;
	}

	.control-label input {
		vertical-align: top;
		margin-left: 2px
	}

	li.monitor-state {
		height: 10px;
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
		width: 400px;
		height: 300px
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
        margin-top:27px;
        margin-left:287px;
        position:absolute
    }

	i.expand {
        background: url('${req.getContextPath()}/img/icon_expand.png') no-repeat;
        display: inline-block;
		height: 16px;
		width: 16px;
		line-height: 16px;
		vertical-align: text-top;
	}

	i.collapse{
        background: url('${req.getContextPath()}/img/icon_collapse.png') no-repeat;
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

	legend {
		padding-top: 10px;
	}

	label.region {
		margin-left:-40px;
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
<div id="wrap">
	<#include "../common/navigator.ftl">
	<div class="container">
		<form id="test_config_form" name="test_config_form" action="${req.getContextPath()}/perftest/new"  method="POST">
			<div class="well" style="margin-bottom: 5px;margin-top:0">
				<input type="hidden" id="test_id" name="id" value="${(test.id)!}">
				<div class="form-horizontal" id="query_div">
					<fieldset>
						<div class="control-group">
							<div class="row">
								<div class="span4-5" data-step="1" data-intro="<@spring.message 'intro.detail.testName'/>">
									<@control_group name = "testName" controls_style = "margin-left: 120px;" label_message_key = "perfTest.config.testName">
										<input class="required span3 left-float" maxlength="80" size="30" type="text" id="test_name" name="testName" value="${test.testName}"/>
									</@control_group>
								</div>
								<div class="span3-4" data-step="2" data-intro="<@spring.message 'intro.detail.tags'/>">
									<@control_group name = "tagString" label_style = "width:60px;" controls_style = "margin-left: 40px;" label_message_key = "perfTest.config.tags">
										<input class="span2-3" size="50" type="text" id="tag_string" name="tagString" value="${test.tagString}">
									</@control_group>
								</div>
								<div class="span1">
									<#if test.id??>
										<img id="test_status_img" class="ball" 
										src="${req.getContextPath()}/img/ball/${test.status.iconName}"
										rel='popover'
										data-html='true'
										data-content='${"${test.progressMessage}<br/><b>${test.lastProgressMessage}</b>"?replace('\n', '<br>')?html}'  
										title="<@spring.message "${test.status.springMessageKey}"/>"
										data-placement='bottom'
										/>
									</#if>
								</div>
								<#if test.status != "SAVED" ||
									(test.createdUser?? && test.createdUser.userId !=	currentUser.factualUser.userId)>
									<#assign isClone = true/>
								<#else>
									<#assign isClone = false/>
								</#if>

								<div class="span2-3" style="margin-left:0" data-step="3" data-intro="<@spring.message 'intro.detail.startbutton'/>">
									<div class="control-group">
										<input type="hidden" name="isClone" value="${isClone?string}"/>
										<#--  Save/Clone is available only when the test owner is current user.   -->
										<#if !(test.createdUser??) || test.createdUser.userId != currentUser.factualUser.userId>
											<#assign disabled = "disabled">
										</#if>
										<button type="submit" class="btn btn-success" id="save_test_btn" style="width:55px" ${disabled!}>
											<#if isClone>
												<@spring.message "perfTest.action.clone"/>
											<#else>
												<@spring.message "common.button.save"/>
											</#if> 
										</button>
										<button type="button" class="btn btn-primary" style="width:116px" id="save_schedule_btn" ${disabled!}>
											<#if isClone><@spring.message "perfTest.action.clone"/><#else><@spring.message "common.button.save"/></#if>&nbsp;<@spring.message "perfTest.action.andStart"/>
										</button>
									</div>
								</div>
							</div>
						</div>
						<div class="control-group" style="margin-bottom: 0">
							<label for="description" class="control-label"><@spring.message "common.label.description"/></label>
							<div class="controls" style="margin-left: 120px;">
								<textarea id="description" name="description" style="resize: none; width:751px; height:36px">${test.description}</textarea>
							</div>
						</div>
					</fieldset>
				</div>
			</div>
			<!-- end well -->
			<@security.authorize access="hasAnyRole('A', 'S')">
				<#if test.createdUser?? && currentUser.userId != test.createdUser.userId>
					<div class="pull-right">
						<a href="${req.getContextPath()}/user/switch?to=${test.createdUser.userId!""}">
						<@spring.message "perfTest.list.owner"/> : ${test.createdUser.userName!""} (${test.createdUser.userId!""})
						</a>
					</div>
				</#if>
			</@security.authorize >
			<div class="tabbable" style="margin-top: 0;margin-bottom: 50px">
				<ul class="nav nav-tabs" id="homeTab" style="margin-bottom: 5px">
					<li id="test_config_section_tab">
						<a href="#test_config_section" data-toggle="tab">
							<@spring.message "perfTest.config.testConfiguration"/>
						</a>
					</li> 
					<li id="running_section_tab" style="display: none;">
						<a href="#running_section" data-toggle="tab" id="running_section_btn">
							<@spring.message "perfTest.running.title"/>
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
					</div>
				</div>
				<!-- end tab content -->
				<div class="pull-right" rel="popover" style="float;margin-top:-30px;margin-right:-30px;cursor: pointer"
					title="Tip" data-html="ture" data-placement="left"
					data-content="<@spring.message "intro.public.button.show"/>"
					id="introButton"	>
					<code>Tip</code>
				</div>
			</div>
			<!-- end tabbable -->
			<input type="hidden" id="scheduled_time" name="scheduledTime" /> 
			<#if test.id??>
				<input type="hidden" id="test_status" name="status" value="${(test.status)}">
				<input type="hidden" id="test_status_type" name="statusType" value="${(test.status.category)}"> 
			<#else>
				<input type="hidden" id="test_status" name="status" value="SAVED">
			</#if>
		</form>
	</div>
	<!--end container-->

	<div class="modal hide fade" id="schedule_modal">
		<div class="modal-header">
			<a class="close" data-dismiss="modal">&times;</a>
			<h4>
				<@spring.message "perfTest.running.scheduleTitle"/>
			</h4>
		</div>
		<div class="modal-body">
			<div class="form-horizontal">
				<fieldset>
					<div class="control-group">
						<label class="control-label"><@spring.message "perfTest.running.schedule"/></label>
						<div class="controls form-inline">
							<input type="text" class="input span2" id="scheduled_date" value="" readyonly>&nbsp; 
							<select id="scheduled_hour" class="select-item"></select> : <select id="scheduled_min" class="select-item"></select>
							<code>HH:MM</code>
							<div class="help-inline" class="margin-left:30px"></div>
						</div>
					</div>
				</fieldset>
			</div>
		</div>
		<div class="modal-footer">
			<a class="btn btn-primary" id="run_now_btn"><@spring.message "perfTest.running.runNow"/></a> <a class="btn btn-primary" id="add_schedule_btn"><@spring.message "perfTest.running.schedule"/></a>
		</div>
	</div>
	<#include "host_modal.ftl">
</div>
<#include "../common/copyright.ftl">
<script src="${req.getContextPath()}/plugins/datepicker/js/bootstrap-datepicker.js"></script>
<script src="${req.getContextPath()}/js/bootstrap-slider.min.js"></script>
<script src="${req.getContextPath()}/js/ramp_up.js?${nGrinderVersion}"></script>
<script>
// vuser calc
${vuserCalcScript};

var objTimer;
var durationMap = [];

$(document).ready(function () {
	$.ajaxSetup({
		cache : false //close AJAX cache
	});
	initTags();
	initDuration();
	initScheduleDate();
	$("#sample_tab").find("a:first").tab('show');
	$("#test_config_section_tab").find("a").tab('show');

	addValidation();
	bindEvent();
	updateScript();
	updateTotalVuser();
	updateRampUpChart();
	callUpdateAvailableAgentInfo();
<#assign category = test.status.category>
<#if category == "TESTING">
	displayConfigAndRunningSection();
<#elseif category == "FINISHED" || category == "STOP" || category == "ERROR">
	finished = true;
	displayConfigAndReportSection();
<#else>
	displayConfigOnly();
</#if>

	(function refreshContent() {
		if (!testId || finished == true) {
			return;
		}

		var ajaxObj = new AjaxObj("/perftest/api/{testId}/status");
		ajaxObj.params = { testId : "${test.id!""}"};
		ajaxObj.success = function(data) {
			data = eval(data);
			data = data.status;
			for ( var i = 0; i < data.length; i++) {
				//noinspection JSUnresolvedVariable
                updateStatus(data[i].id, data[i].status_type, data[i].name, data[i].icon, data[i].deletable, data[i].stoppable, data[i].message);
			}
		};
		ajaxObj.complete = function() {
			setTimeout(refreshContent, 3000);
		};
		ajaxObj.call();
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
		placeholder: '<@spring.message "perfTest.config.tagInput"/>',
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
			var ajaxObj = new AjaxPostObj("/perftest/search_tag", {'query' : query.term});
			ajaxObj.success = function(res) {
				for (var i = 0; i < res.length; i++) {
					data.results.push({id:"q_" + res[i], text:res[i]});
				}
				query.callback(data);
			};
			ajaxObj.call();
		}
	}).change(formatTags);

	$("#script_name").select2({
		placeholder: '<@spring.message "perfTest.config.scriptInput"/>'
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
		} else if (i <= 32) { //until 180 min
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
    var $hiddenDurationInput = $("#hidden_duration_input");
    $hiddenDurationInput.attr("data-slider", "#duration_slider");
	for (i = 0; i <= sliderMax; i++) {
		if (durationMap[i] * 60000 >= durationVal) {
			$hiddenDurationInput.attr("value", i);
			break;
		}
		if (i == sliderMax) {
			$hiddenDurationInput.attr("value", sliderMax);
		}
	}
	$hiddenDurationInput.slider(
			{
				max:sliderMax,
				min:1,
				template:
					"<div class='input-slider' style='width:255px'>" +
					"<div class='input-slider-knob js-slider-knob'></div>" +
					"</div>"
			});

	var durationHour = parseInt(durationVal / 3600000) + 1;
	var durationMaxHour = durationHour > ${maxRunHour} ? durationHour : ${maxRunHour};
    var $selectHour = $("#select_hour");
    var $selectSec = $("#select_sec");
    var $selectMin = $("#select_min");
    $selectHour.append(getOption(durationMaxHour));
    $selectHour.change(getDurationMS);
    $selectMin.append(getOption(60));
	$selectMin.change(getDurationMS);
    $selectSec.append(getOption(60));
	$selectSec.change(getDurationMS);
	setDuration();
	setDurationHour(durationVal);
}

var validationOptions = {};
function addValidation() {
	$.validator.addMethod("paramFmt", function (param) {
		var rule = /^[a-zA-Z0-9_\.,\|=]{0,50}$/;
		return rule.test($.trim(param));
	});
	var $runCountRadio = $("#run_count_radio");
	$.validator.addMethod("runCount", function(value, element) {
		if ($runCountRadio.is(":checked")) {
			return value > 0;
		} else {
			return true;
		}
	});
    //noinspection JSUnusedLocalSymbols,JSUnusedLocalSymbols
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
			<#if clustered>
			region : {
				required: true
			},
			</#if>
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
			<#if securityLevel?? && securityLevel == "normal">
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
				max: ${maxRunCount},
				runCount: true
			},
			param : {
				required:false,
				paramFmt:true
			}
		},
		messages: {
			testName: {
				required: "<@spring.message 'perfTest.message.testName'/>"
			},
		<#if clustered>
			region : {
				required: "<@spring.message 'perfTest.message.region'/>"
			},
		</#if>
			agentCount: {
				required: "<@spring.message 'perfTest.message.agentNumber'/>"
			},
			vuserPerAgent: {
				required: "<@spring.message 'perfTest.message.vuserPerAgent'/>"
			},
			scriptName: {
				required: "<@spring.message 'perfTest.message.script'/>"
			},
			durationHour: {
				max: "<@spring.message 'perfTest.message.duration.maxHour'/>"
			},
			runCount: {
				required: "<@spring.message 'perfTest.message.runCount'/>",
				runCount: "<@spring.message 'perfTest.message.runCount'/>"
			},
			processes: {
				required: "<@spring.message 'perfTest.message.processes'/>"
			},
			threads: {
				required: "<@spring.message 'perfTest.message.threads'/>"
			},
			targetHosts: {
				required: "<@spring.message 'perfTest.message.hostString'/>"
			},
			param : {
				paramFmt: "<@spring.message 'perfTest.message.param'/>"
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
			var $detailedSection = $(element).parents("#detail_config_section");
			if ($detailedSection.length >= 1) {
				$detailedSection.show();
			}
			var $controlGroup = $(element).parents('.control-group');
			if ($controlGroup.length >= 1) {
				$($controlGroup[0]).removeClass("success");
				$($controlGroup[0]).addClass("error");
			}
		},
		unhighlight : function(element, errorClass, validClass) {
			var $elem = $(element);
			var $controlGroup = $elem.parents('.control-group');
			if ($controlGroup.length >= 1) {
				var isSuccess = true;
				$elem.siblings("span.help-inline:visible").each(function() {
					if ($(this).attr("for") != $elem.attr("id")) {
						isSuccess = false;
					}
				});
				if (isSuccess) {
					$($controlGroup[0]).removeClass("error");
					$($controlGroup[0]).addClass("success");
				}
			}
		}
	};

	$("#test_config_form").validate(validationOptions);
}

function bindNewScript(target, first) {
	var $showScript = $("#show_script_btn");
	var $scriptRevision = $("#script_revision");
	var oldRevision = $scriptRevision.attr("old_revision");
	if (target.val() == target.attr("old_script") && oldRevision != -1) {
		$showScript.text("R " + oldRevision);
		$scriptRevision.val(oldRevision);
	} else {
		$showScript.text("R HEAD");
		$scriptRevision.val(-1);
	}
	$showScript.show();
	updateScriptResources(first);
	if (target.val() != "") {
		target.valid();
	}
}

function showScheduleModal() {
	initScheduleTime();
	$("#tag_string").val(buildTagString());
	$('#schedule_modal').modal('show');
}


function getBrowserTimeApplyingTimezone(time) {
	var date = new Date();
	if (time === undefined) {
		return new Date(date.getTime() + (date.getTimezoneOffset() * 60 * 1000) + ${timezone_offset});
	} else {
		date = new Date(time - ${timezone_offset});
		// Now it's browser time reflecting the timezone difference.
		return new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours(), date.getMinutes()));
	}
}	


function bindEvent() {
	$("#script_name").change(function() {
		bindNewScript($(this), false);
	});

	$("#hidden_duration_input").bind("slide", function() {
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

	$("#detail_config_section_btn").click(function() {
		if ($("#detail_config_section").is(":hidden")) {
			$("#detail_config_section").show("slow");
		} else {
			$("#detail_config_section").slideUp();
		}
	});

	$("#save_schedule_btn").click(function() {		
		$("#agent_count").rules("add", {
			min:1
		}); 
		if (!validateForm()) {
			return false;
		}
		//noinspection JSUnresolvedVariable
        if (typeof(scheduleTestHook) != "undefined") {
			//noinspection JSUnresolvedFunction
            if (scheduleTestHook()) {
				showScheduleModal();
			}
		} else { //noinspection JSUnresolvedVariable
            if (typeof(scheduleTestUnBlockingHook) != "undefined") {
	            //noinspection JSUnresolvedFunction
                scheduleTestUnBlockingHook(showScheduleModal);
            } else {
	            showScheduleModal();
            }
        }
		return true;
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
        var $scheduleModal = $("#schedule_modal");
        $scheduleModal.modal("hide");
		$scheduleModal.find("small").html("");
		$("#scheduled_time").attr('name', '');
		$("#test_status").val("READY");
		showSuccessMsg("<@spring.message 'perfTest.message.testStart'/>");
		setTimeout(function() {
			document.test_config_form.submit();
		}, 1000);
	});



	$("#add_schedule_btn").click(function() {
        var $scheduleModal = $("#schedule_modal");
        if (checkEmptyByID("scheduled_date")) {
			$scheduleModal.find(".control-group").addClass("error");
			$scheduleModal.find(".help-inline").html("<@spring.message "perfTest.message.setScheduleDate.alert"/>");
			return;
		}

		var timeStr = $("#scheduled_date").val() + " " + $("#scheduled_hour").val() + ":" + $("#scheduled_min").val() + ":0";
		// User input date time.
		var scheduledTime = new Date(timeStr.replace(/-/g, "/"));
		scheduledTime = getBrowserTimeApplyingTimezone(scheduledTime.getTime());
        if (new Date() > scheduledTime) {
			$scheduleModal.find(".control-group").addClass("error");
			$scheduleModal.find(".help-inline").html("<@spring.message "perfTest.message.scheduleDate.error"/>");
			return;
		}
		$scheduleModal.find(".control-group").removeClass("error");
		$("#scheduled_time").val(scheduledTime);
        $scheduleModal.modal("hide");
		$scheduleModal.find("small").html("");
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
                var maxVal = 3600000 * ${maxRunHour};
                $("#duration").val(maxVal);
                setDuration();
                //noinspection JSUnusedAssignment
                setDurationHour(maxVal);
			}
			$durationHour.valid();
		}
	});

	$("#duration_ratio").click(function() {
		if ($(this).attr("checked") == "checked") {
            var $duration = $("#duration");
            setDurationHour($duration.val());
			$("#duration_hour").valid();
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
		var $vuser = $("#vuser_per_agent");
		$vuser.val($("#processes").val() * $("#threads").val());
		if ($vuser.valid()) {
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
		switchIntroData('report');
	});
	
	$("#test_config_section_tab").click(function() {
		switchIntroData('config');		
	});

	$("#running_section_tab").click(function() {
		switchIntroData('running');		
	});

	$("#sample_tab").find("a").click(function(e) {
		e.preventDefault();
		$(this).tab('show');
	});

	$("#show_script_btn").click(function() {
		var currentScript = $("#script_name").val();
		if (currentScript) {
			//noinspection JSUnusedAssignment
            var ownerId = "";
			<@security.authorize access="hasAnyRole('A', 'S')">
				<#if test.id??>
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
		var $panel = $("#process_thread_config_panel");
		if ($panel.is(":hidden")) {
			$panel.show("slow");
		} else {
			$panel.slideUp();
		}
	});
	
	$("#expand_ready_agent_cnt_btn").click(function() {
		$(this).toggleClass("collapse");
		var $panel = $("#div_ready_agent_cnt");
		if ($panel.is(":hidden")) {
			$panel.show("slow");
		} else {
			$panel.slideUp();
			
		}
	});

	$("#select_hour, #select_min, #select_sec").change(function() {
		$("#duration_ratio").click();
	});

	$("#run_count").focus(function() {
		$("#run_count_radio").click();
	});

	$(document).ajaxSend(function(e, xhr, settings) {
		var url = settings.url;
		if ((url.indexOf("resource") > 0 || url.indexOf("script") > 0)) {
			showProgressBar("<@spring.message "perfTest.message.updateResource"/>");
		}
	});
<#if clustered>
	var $region = $("#region");
	$region.select2();
	$region.change(function(){
		changeAgentMaxCount($(this).val(), true);
		updateAvailableAgentInfo($(this).val());
	});
	changeAgentMaxCount($region.val(), false);
<#else>
	changeAgentMaxCount("NONE", false);
</#if>	
	$("#introButton").click(function() {
		introJs().start();
	});
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
	var result = $("#test_config_form").valid();
	if (!result) {
		$("#test_config_section_tab").find("a").tab('show');
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

function updateScript() {
	var ajaxObj = new AjaxObj("/perftest/api/script", null, "<@spring.message "common.error.error"/>");
	ajaxObj.params = {
		<@security.authorize access="hasAnyRole('A', 'S')">
				<#if test.id?? && test.createdUser??>'ownerId' : '${test.createdUser.userId}'</#if>
		</@security.authorize>
	};
	ajaxObj.success = function(res) {
		var $scriptSelection = $("#script_name");
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
	};
	ajaxObj.complete = function() {
		hideProgressBar();
	};

	ajaxObj.call();
}

function updateScriptResources(first) {
	var scriptName = $("#script_name").val();
	if (!scriptName) {
		return;
	}

	var ajaxObj = new AjaxObj("/perftest/api/resource", null, "<@spring.message "common.error.error"/>");
	ajaxObj.params = {
		'scriptPath' : scriptName,
		'r' : $("#script_revision").val()
		<@security.authorize access="hasAnyRole('A', 'S')">
			<#if test.id??>,'ownerId' : '${(test.createdUser.userId)!}'</#if>
		</@security.authorize>
	};
	ajaxObj.success = function(res) {
		var html = "";
		var len = res.resources.length;
		if (first == false) {
			initHosts(res.targetHosts);
		}
		for ( var i = 0; i < len; i++) {
			var value = res.resources[i];
			html = html + "<div class='resource ellipsis' title='" + value + "'>" + value + "</div>";
		}
		$("#script_resources").html(html);
	};
	ajaxObj.complete = function() {
		hideProgressBar();
	};
	ajaxObj.call();
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
		updateRampUpChart();
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
	for (var i = 0; i < cnt; i++) {
		contents.push("<option value='" + i + "'>" + (i < 10 && cnt > 9 ? "0" + i : i) + "</option>");
	}
	return contents.join("\n");
}

function openRunningDiv(onFinishHook) {
	$("#running_section").load("${req.getContextPath()}/perftest/${(test.id!0)?c}/running_div",
		function() {
			if (onFinishHook !== undefined) {
				onFinishHook();
			}
		}
	);
}

function openReportDiv(onFinishHook) {
	$("#report_section").load("${req.getContextPath()}/perftest/${(test.id!0)?c}/basic_report?imgWidth=600",
		function() {
			if (onFinishHook !== undefined) {
				onFinishHook();
			}
		}
	);
}

//noinspection JSUnusedLocalSymbols
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
	if (isRunningStatusType(statusType)) {
		displayConfigAndRunningSection();
	} else if (isFinishedStatusType(statusType)) {
		finished = true; 
		// Wait and run because it takes time to transfer logs.
		setTimeout('displayConfigAndReportSection()', 3000);
	} else {
		displayConfigOnly();
	}
	
	if (statusType == "STOP_BY_ERROR"){
		$("#test_status_img").trigger('mouseover');
	}
}

var finished = false;
var testId = $('#test_id').val();
// Wrap this function in a closure so we don't pollute the namespace
function displayConfigOnly() {
    $("#test_config_section_tab").find("a").tab('show');
    $("#running_section_tab").hide();
    $("#report_section_tab").hide();
}

var samplingInterval = 1;
var $reportSectionTab = $("#report_section_tab");
var $runningSectionTab = $("#running_section_tab");

function displayConfigAndRunningSection() {
    $runningSectionTab.show();
    $runningSectionTab.find("a").tab('show');
    $("#running_section").show();
    $("#report_section_tab").hide();
    openRunningDiv(function() {
		$("#foot_div").show();
	});
	switchIntroData('running');
}

function displayConfigAndReportSection() {
    $("#foot_div").hide();
    $("#running_section_tab").hide();
    $reportSectionTab.show();
	$reportSectionTab.find("a").tab('show');
	openReportDiv(function() {
		$("#foot_div").show();
	});
	switchIntroData('report');
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

function callUpdateAvailableAgentInfo() {
	var targetRegion;
<#if clustered>
	targetRegion = $("#region").val();
<#else>
	targetRegion = "NONE";
</#if>	
	if(targetRegion != '') {
		updateAvailableAgentInfo(targetRegion);	
	}
	setTimeout(callUpdateAvailableAgentInfo, 2000);
}

function updateAvailableAgentInfo(targetRegion) {
    var ajaxObj = new AjaxObj("/agent/api/availableAgentCount");
    ajaxObj.type = "GET";
    ajaxObj.params = {"targetRegion": targetRegion };
    ajaxObj.success = function (data) {
        $("#availableAgentCount").text(data.availableAgentCount);
    };
    ajaxObj.error = function () {
        $("#availableAgentCount").text('');
        $("#div_ready_agent_cnt").text('<@spring.message "common.error.error"/>');
    };
    ajaxObj.call();
}

function switchIntroData(showArea){
	$(".intro").each(function(index , value){
        $(this).attr("temp_data-step", $(this).attr("data-step")).removeAttr("data-step");
        $(this).attr("temp_data-intro", $(this).attr("data-intro")).removeAttr("data-intro");
	});
	$("." + showArea + " .intro").each(function(index , value){
        $(this).attr("data-step", $(this).attr("temp_data-step")).removeAttr("temp_data-step");
        $(this).attr("data-intro", $(this).attr("temp_data-intro")).removeAttr("temp_data-intro");
	});	
}

</script>
</body>
</html>
