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
		width: 50px;
	}
	
	.control-label input {
		vertical-align: top;
		margin-left: 2px
	}
	
	.controls code {
		vertical-align: middle;
	}
	
	div.chart {
		border: 1px solid #878988;
		margin-bottom: 12px;
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
	
	.rampChart {
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
	
	.addhostbtn {
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
	
	#testName + span {
		float: left;
	}
	
	#queryDiv label {
		width: 100px;
	}
	
	.controls .span3 {
		margin-left: 0;
	}
	
	.control-group.success td > label[for="testName"] {
		color: #468847;
	}
	
	.control-group.error td > label[for="testName"] {
		color: #B94A48;
	}
	
	#scriptControl.error .select2-choice {
	    border-color: #B94A48;
	    color: #B94A48;
	}
	
	#scriptControl.success .select2-choice {
	    border-color: #468847;
	    color: #468847;
	}

	label.region {
		position: absolute;
		margin-top: 4px;
		display: inline; 
		color: #666666; 
		font-family: Tahoma,applegothic,sans-serif; 
		font-size: 12px; 
		font-weight: bold;
	}
	</style>
</head>

<body>
	<#include "../common/navigator.ftl">
	<div class="container">
		<form id="testContentForm" name="testContentForm" action="${req.getContextPath()}/perftest/create" method="POST">
			<div class="well">
				<input type="hidden" id="testId" name="id" value="${(test.id)!}"> 

				<div class="form-horizontal" id="queryDiv">
					<fieldset>
						<div class="control-group">
							<table>
								<colgroup>
									<col width="120px">
									<col width="210px">
									<col width="90px">
									<col width="210px">
									<col width="30px">
									<col width="220px">
								</colgroup>
								<tbody>
									<tr>
										<td>
											<label for="testName" class="control-label"><@spring.message "perfTest.configuration.testName"/></label>
										</td>
										<td>
											<#if test?? && test.testName??>
												<#assign initTestName = test.testName>
											<#elseif testName??>
												<#assign initTestName = testName>
											<#else>
			                     		   		<#assign initTestName = "">
			                    			</#if>
											<input class="required span3 left-float" maxlength="80" size="30" type="text" id="testName" name="testName" value="${(initTestName)!}">
										</td>
										<td>
											<label for="tagString" class="control-label" style="width:80px; margin-right:18px"><@spring.message "perfTest.configuration.tags"/></label>
										</td>
										<td>
											<input class="span3" size="60" type="text" id="tagString" name="tagString" value="${(test.tagString)!}">
										</td>
										<td>
											<#if test??> 
												<span id="teststatus_pop_over"
													rel="popover" 
													data-content='${"${test.progressMessage}<br/><b>${test.lastProgressMessage}</b>"?replace('\n', '<br>')?html}'  
													data-original-title="<@spring.message "${test.status.springMessageKey}"/>" type="toggle" placement="bottom">
													<img id="testStatus_img_id" src="${req.getContextPath()}/img/ball/${test.status.iconName}" />
												</span>
											</#if>
										</td>
										<td>
											<#if test??>
												<#if test.status != "SAVED" || test.createdUser.userId != currentUser.factualUser.userId>
													<#assign isClone = true/>
												<#else>
													<#assign isClone = false/> 
												</#if>
											<#else>
												<#assign isClone = false/> 
											</#if>
											<input type="hidden" name="isClone" value="${isClone?string}">
											<#--  Save/Clone is available only when the test owner is current user.   -->
											<#if test?? && test.createdUser.userId != currentUser.factualUser.userId>
												<#assign disabled = "disabled">
											</#if>
												<button type="submit" class="btn btn-success" id="saveTestBtn" style="margin-left:26px; width:55px" ${disabled!}>
													<#if isClone>
														<@spring.message "perfTest.detail.clone"/>
													<#else>
														<@spring.message "common.button.save"/>
													</#if> 
												</button>
												
												<button class="btn btn-primary" style="width:116px" data-toggle="modal" href="#scheduleModal" id="saveScheduleBtn" ${disabled!}>
													<#if isClone><@spring.message "perfTest.detail.clone"/><#else><@spring.message "common.button.save"/></#if>&nbsp;<@spring.message "perfTest.detail.andStart"/>
												</button>
										</td>
									</tr>
								</tbody>
							</table>
						</div>
						<div class="control-group" style="margin-bottom: 0">
							<table>
								<colgroup>
									<col width="120">
									<col width="210">
								</colgroup>
								<tbody>
									<tr>
										<td>
											<label for="description" class="control-label"><@spring.message "common.label.description"/></label>
										</td>
										<td>
											<textarea id="description" name="description" style="resize: none; width:751px; height:36px">${(test.description)!}</textarea>
										</td>
									</tr>
								</tbody>
							</table>
						</div>
					</fieldset>
				</div>
			</div>
			<!-- end well -->
			
			<div class="tabbable" style="margin-top: 20px">
				<ul class="nav nav-tabs" id="homeTab" style="margin-bottom: 5px">
					<li id="testContent_tab">
						<a href="#testContent" data-toggle="tab">
							<@spring.message "perfTest.configuration.testConfiguration"/>
						</a>
					</li> 
					<li id="runningContent_tab" style="display: none;">
						<a href="#runningContent" data-toggle="tab" id="runningContentLink">
							<@spring.message "perfTest.testRunning.title"/>
						</a>
					</li>
				
					<li id="reportContent_tab" style="display: none; ">
						<a href="#reportContent" data-toggle="tab" id="reportLnk">
							<@spring.message "perfTest.report.tab"/>
						</a>
					</li>
				</ul>
				<div class="tab-content">
					<div class="tab-pane" id="testContent">
						<#include "configDiv.ftl">
					</div>
					
					<div class="tab-pane" id="reportContent">
					</div>
					
					<div class="tab-pane" id="runningContent">
						<#include "runningDiv.ftl">
					</div>
				</div>
				<!-- end tab content -->
			</div>
			<!-- end tabbable -->
			<input type="hidden" id="scheduleInput" name="scheduledTime" /> 
			<#if test??> 
				<input type="hidden" id="testStatus" name="status" value="${(test.status)}">
				<input type="hidden" id="testStatusType" name="statusType" value="${(test.status.category)}"> 
			<#else>
				<input type="hidden" id="testStatus" name="status" value="SAVED">
			</#if>
		</form>
		<#include "../common/copyright.ftl">
	</div>
	<!--end container-->

	<div class="modal fade" id="scheduleModal">
		<div class="modal-header">
			<a class="close" data-dismiss="modal">&times;</a>
			<h3>
				<@spring.message "perfTest.testRunning.scheduleTitle"/> <small class="errorColor"></small>
			</h3>
		</div>
		<div class="modal-body">
			<div class="form-horizontal">
				<fieldset>
					<div class="control-group">
						<label class="control-label"><@spring.message "perfTest.testRunning.schedule"/></label>
						<div class="controls form-inline">
							<input type="text" class="input span2" id="sDateInput" value="" readyonly>&nbsp; 
							<select id="shSelect" class="select-item"></select> : <select id="smSelect" class="select-item"></select>
							<code>HH:MM</code>
						</div>
					</div>
				</fieldset>
			</div>
		</div>
		<div class="modal-footer">
			<a class="btn btn-primary" id="runNowBtn"><@spring.message "perfTest.testRunning.runNow"/></a> <a class="btn btn-primary" id="addScheduleBtn"><@spring.message "perfTest.testRunning.schedule"/></a>
		</div>
	</div>

<script src="${req.getContextPath()}/plugins/datepicker/js/bootstrap-datepicker.js"></script>
<script src="${req.getContextPath()}/js/bootstrap-slider.min.js"></script>
<script src="${req.getContextPath()}/js/rampup.js?${nGrinderVersion}"></script>
<script src="${req.getContextPath()}/js/queue.js?${nGrinderVersion}"></script>
<script>
// vuser calc
${processthread_policy_script}

var jqplotObj;
var objTimer;
var test_tps_data = new Queue();
var durationMap = [];

$(document).ready(function () {
	$.ajaxSetup({
		cache : false //close AJAX cache
	});
	initTags();
	initDuration();
	initChartData();
	initScheduleDate();
	$("#tableTab a:first").tab('show');
	$("#testContent_tab a").tab('show');
	
	addValidation();
	bindEvent();
	updateScriptResources(true);
	updateVuserTotal();
	updateRampupChart();
	
	<#if test??>
		<#assign category = test.status.category>
		<#if category == "TESTING"> 
  			displayCfgAndTestRunning(); 
		<#elseif category == "FINISHED" || category == "STOP" || category == "ERROR"> 
			displayCfgAndTestReport(); 
		<#else>
			displayCfgOnly(); 
		</#if>
	<#else>
		displayCfgOnly();
	</#if>
});

function formatTags(e) {
    if (e.added && (e.added.id.indexOf(",") >= 0 || e.added.id.indexOf(" ") >= 0)) {
        var tagControl = $("#tagString");
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
	$("#tagString").select2({	
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
				url : "${req.getContextPath()}/perftest/tagSearch",
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
	$("#scriptName").select2({
		placeholder: '<@spring.message "perfTest.configuration.scriptInput"/>'
	});
}

function initScheduleDate() {
	var date = new Date();
	var year = date.getFullYear();
	var month = date.getMonth() + 1;
	var day = date.getDate();
	$("#sDateInput").val(year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day));
	
	$('#sDateInput').datepicker({
		format : 'yyyy-mm-dd'
	});
	
	$("#shSelect").append(getOption(24));
	$("#smSelect").append(getOption(60));
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
	$("#hiddenDurationInput").attr("data-slider", "#durationSlider");
	$("#hiddenDurationInput").slider({min:1, max:sliderMax});
	for ( var i = 0; i <= sliderMax; i++) {
		if (durationMap[i] * 60000 == durationVal) {
			$("#hiddenDurationInput").val(i);
			break;
		}
	}
	
	var durationHour = parseInt(durationVal / 3600000) + 1;
	var durationMaxHour = durationHour > ${maxRunHour} ? durationHour : ${maxRunHour};
	$("#hSelect").append(getOption(durationMaxHour));
	$("#hSelect").change(getDurationMS);
	
	$("#mSelect").append(getOption(60));
	$("#mSelect").change(getDurationMS);
	
	$("#sSelect").append(getOption(60));
	$("#sSelect").change(getDurationMS);
	
	setDuration();
	setDurationHour(durationVal);
}

var validationOptions = {};
function addValidation() {
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
			}
		},
	    messages: { 
	        testName: {
	        	required: "<@spring.message "perfTest.warning.testName"/>"
	        },
	        agentCount: {
	        	required: "<@spring.message "perfTest.warning.agentNumber"/>"
	        },
	        vuserPerAgent: {
	        	required: "<@spring.message "perfTest.warning.vuserPerAgent"/>"
	        },
	        scriptName: {
	        	required: "<@spring.message "perfTest.warning.script"/>"
	        },
	        durationHour: {
	        	max: "<@spring.message "perfTest.warning.duration.maxHour"/>"
	        },
	        runCount: {
	        	required: "<@spring.message "perfTest.warning.runCount"/>"
	        },
	        processes: {
	        	required: "<@spring.message "perfTest.warning.processes"/>"
	        },
	        threads: {
	        	required: "<@spring.message "perfTest.warning.threads"/>"
	        },
	        targetHosts: {
	        	required: "<@spring.message "perfTest.warning.hostString"/>"
	        }
	    },
		ignore : "", // make the validation on hidden input work
		errorClass : "help-inline",
		errorElement : "span",
		errorPlacement : function(error, element) {
			var errorPlace = $("td." + element.attr("id"));
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
	
	$("#testContentForm").validate(validationOptions);
}

function bindEvent() {
	$('#testContentForm input').hover(function() {
		$(this).popover('show');
	});
	
	$("#scriptName").change(function() {
		var $this = $(this);
		var $showScript = $("#showScript");
		var $scriptRevision = $("#scriptRevision");
		var oldRevision = $scriptRevision.attr("oldRevision");
		if ($this.val() == $this.attr("oldScript") && oldRevision != -1) {
			$showScript.text("R " + oldRevision);
			$scriptRevision.val(oldRevision);
		} else {
			$showScript.text("R HEAD");
			$scriptRevision.val(-1);
		}
		$showScript.show();
		
		updateScriptResources(false);
	});
	
	$("#hiddenDurationInput").bind("slide", function(e) {
		var maxIndex = durationMap.length - 1;
		var $duration = $("#duration");
		if (maxIndex == this.value) {
			$duration.val((durationMap[maxIndex] + 59) * 60000 + 59000);
		} else {
			$duration.val(durationMap[this.value] * 60000);
		}
		setDuration(); 
		$("#durationRadio").click();
	});
	
	$("#saveScheduleBtn").click(function() {		
		$("#agentCount").rules("add", {
			min:1
		});
		
		if (!validateForm()) {
			return false;
		}

		var $agentCount = $("#agentCount");
		if ($agentCount.val() == 0) {
			var $controlGrp = $agentCount.parents('.control-group');
			$controlGrp.removeClass('success');
			$controlGrp.addClass("error");
			showErrorMsg("<@spring.message "perfTest.warning.agent0"/>");
			return false;
		}

		if (typeof(scheduleTestHook) != "undefined") {
			if (!scheduleTestHook()) {
				return false;
			}
		}
		
		if ($("#scriptName option:selected").attr("validated") == "0") {
			$("small.errorColor").text("<@spring.message "perfTest.detail.message.validatedScript"/>");
		} else {
			$("small.errorColor").text("");
		}
	    
	   	initScheduleTime();
		
		$("#tagString").val(buildTagString());
	});
	
	$("#saveTestBtn").click(function() {
		$("#agentCount").rules("add", {
			min:0
		});
		
		if (!validateForm()) {
			return false;
		}

		$("#testStatus").val("SAVED");
		$("#scheduleInput").attr('name', '');
		$("#tagString").val(buildTagString());
		
		return true;
	});
	
	$("#runNowBtn").click(function() {
		$("#scheduleModal").modal("hide");
		$("#scheduleModal small").html("");
		$("#scheduleInput").attr('name', '');
		$("#testStatus").val("READY");
		document.testContentForm.submit();
	});

	$("#addScheduleBtn").click(function() {
		if (checkEmptyByID("sDateInput")) {
			$("#scheduleModal small").html("<@spring.message "perfTest.detail.message.setScheduleDate"/>");
			return;
		}
	
		var timeStr = $("#sDateInput").val() + " " + $("#shSelect").val() + ":" + $("#smSelect").val() + ":0";
		var scheduledTime = new Date(timeStr.replace(/-/g, "/"));
		if (new Date() > scheduledTime) {
			$("#scheduleModal small").html("<@spring.message "perfTest.detail.message.errScheduleDate"/>");
			return;
		}
		$("#scheduleInput").val(scheduledTime);
		$("#scheduleModal").modal("hide");
		$("#scheduleModal small").html("");
		$("#testStatus").val("READY");
		document.testContentForm.submit();
	});
	
	$("#runCountRadio").click(function() {
		if ($(this).attr("checked") == "checked") {
			var $runCnt = $("#runCount");
			$runCnt.rules("add", {
				min:1
			});
			$runCnt.valid();

			var $durationHour = $("#durationHour");
			if (!$durationHour.valid()) {
				var maxVal = ${maxRunHour} * 3600000;
				$("#duration").val(maxVal);
				setDuration();
				setDurationHour(maxVal);
			}
			$durationHour.valid();
		}
	});
	
	$("#durationRadio").click(function() {
		if ($(this).attr("checked") == "checked") {
			setDurationHour($("#duration").val());
			$("#durationHour").valid();
			var $duration = $("#duration");
			$duration.addClass("positiveNumber");
			$duration.valid();
			
			var $runCnt = $("#runCount");
			$runCnt.rules("add", {
				min:0
			});
			if (!$runCnt.valid()) {
				$runCnt.val(0);
			}
			$runCnt.valid();
		}
	});
	
	$("#ignoreSampleCount, #runCount").blur(function() {
		if ($.trim($(this).val()) == "") {
			$(this).val(0);
		}
	});
	
	$("#agentCount").change(function() {
		updateVuserTotal();
	});
	
	$("#threads, #processes").change(function() {
		var $vuer = $("#vuserPerAgent");
		$vuer.val($("#processes").val() * $("#threads").val());
		if ($vuer.valid()) {
			updateVuserGraph();
			updateVuserTotal();
		}
	});
	
	$("#vuserPerAgent").change(function() {
		var $vuserElement = $(this);
		var processCount = $("#processes").val();
		if ($vuserElement.valid()) {
			var result = updateVuserPolicy($vuserElement.val());
			$(this).val(result[0] * result[1]);
			if (processCount != result[0]) {
				updateVuserGraph();
			}
			updateVuserTotal();
		}
	});
	
	$("#reportLnk").click(function() {
		$("#footer").hide();
		openReportDiv(function() {
			$("#footer").show();
		});
	});
	
	$('#tableTab a').click(function(e) {
		e.preventDefault();
		$(this).tab('show');
	});

	$("#showScript").click(function() {
		var currentScript = $("#scriptName").val();
		if (currentScript != "") {
			var ownerId = ""; 
			<@security.authorize ifAnyGranted="A, S">					
				<#if test??>
					ownerId = "&ownerId=${(test.createdUser.userId)!}";
				</#if>
			</@security.authorize>
			var scriptRevision = $("#scriptRevision").val();
			var openedWindow = window.open("${req.getContextPath()}/script/detail/" + currentScript + "?r=" + scriptRevision + ownerId, "scriptSource");
			openedWindow.focus(); 
		}
	});
	
	$("#expandAndCollapse").click(function() {
		$(this).toggleClass("collapse");
		$("#processAndThreadPanel").toggle();
	});
	
	$("#hSelect, #mSelect, #sSelect").change(function() {
		$("#durationRadio").click();
	});
	
	$("#runCount").focus(function() {
		$("#runCountRadio").click();
	});
	
<#if clustered>
	var $regionSelect = $("#regionSelect");
	$regionSelect.select2();
	$regionSelect.change(function(){
		changeAgentMaxCount($(this).val(), true);
	});
	changeAgentMaxCount($regionSelect.val(), false);
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

	var $agentCountObj = $("#agentCount");
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
			if (!$("#" + key).valid() && result == true) {
				result = false;
			}
		});		
	} else {
		result = $("#testContentForm").valid();
	}
	if (result == false) {
		$("#testContent_tab a").tab('show');
	}
	
	return result;
}

function buildTagString() {
	return $.map($("#tagString").select2("data"), function(obj) {
		return obj.text;
	}).join(",");
}
	
function updateVuserTotal() {
	$("#vuserTotal").text($("#agentCount").val() * $("#vuserPerAgent").val());
}

function initChartData() {
	for ( var i = 0; i < 60; i++) {
		test_tps_data.enQueue(0);
	}
}

function updateScriptResources(first) {
	var scriptName = $("#scriptName").val();
	if (scriptName == "") {
		return;
	}
	
	$('#messageDiv').ajaxSend(function(e, xhr, settings) {
		var url = settings.url;
		if (url.indexOf("getResourcesOnScriptFolder") > 0 && first == false) {
			showProgressBar("<@spring.message "perfTest.detail.message.updateResource"/>");
		}
	});
	
	$.ajax({
		url : "${req.getContextPath()}/perftest/getResourcesOnScriptFolder",
		dataType : 'json',
		data : {
			'scriptPath' : scriptName,
			'r' : $("#scriptRevision").val()
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
	if ($("#rampupCheckbox")[0].checked) {
		updateRampupChart();
	}
}

function setDuration() {
	var duration = $("#duration").val();
	var durationInSec = parseInt(duration / 1000);
	var durationH = parseInt(durationInSec / 3600);
	var durationM = parseInt((durationInSec % 3600) / 60);
	var durationS = durationInSec % 60;

	$("#hSelect").val(durationH);
	$("#mSelect").val(durationM);
	$("#sSelect").val(durationS);
}

function getDurationMS() {
	var durationH = parseInt($("#hSelect").val());
	var durationM = parseInt($("#mSelect").val());
	var durationS = parseInt($("#sSelect").val());
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
	$("#reportContent").load("${req.getContextPath()}/perftest/loadReportDiv?testId=" + $("#testId").val() + "&imgWidth=600",
		function() {
			if (onFinishHook !== undefined) {
				onFinishHook();
			}
		}
	);
}

function updateStatus(id, status_type, status_name, icon, deletable, stoppable, message) {
	if ($("#testStatusType").val() == status_type) {
		return;
	}

	$("#teststatus_pop_over").attr("data-original-title", status_name);
	$("#teststatus_pop_over").attr("data-content", message);
	$("#testStatusType").val(status_type);

	var ballImg = $("#testStatus_img_id");
	if (ballImg.attr("src") != "${req.getContextPath()}/img/ball/" + icon) {
		ballImg.attr("src", "${req.getContextPath()}/img/ball/" + icon);
	}

	if (status_type == "TESTING") {
		displayCfgAndTestRunning();
	} else if (status_type == "FINISHED" || status_type == "STOP_ON_ERROR" || status_type == "CANCELED") {
		isFinished = true;
		displayCfgAndTestReport();
	} else {
		displayCfgOnly();
	}
}

var isFinished = false;
var testId = $('#testId').val();
// Wrap this function in a closure so we don't pollute the namespace
(function refreshContent() {
	var ids = [];
	if (testId == "" || isFinished) {
		return;
	}

	$.ajax({
		url : '${req.getContextPath()}/perftest/updateStatus',
		type : 'GET',
		data : {
			"ids" : testId
		},
		success : function(perfTestData) {
			perfTestData = eval(perfTestData);
			data = perfTestData.statusList
			for ( var i = 0; i < data.length; i++) {
				updateStatus(data[i].id, data[i].status_type, data[i].name, data[i].icon, data[i].deletable, data[i].stoppable, data[i].message);
			}
		},
		complete : function() {
			setTimeout(refreshContent, 5000);
		}
	});
})();

function displayCfgOnly() {
	$("#testContent_tab a").tab('show');
	$("#runningContent_tab").hide();
	$("#reportContent_tab").hide();
}

function displayCfgAndTestRunning() {
	$("#runningContent_tab").show();
	$("#runningContent_tab a").tab('show');
	$("#runningContent").show();
	$("#reportContent_tab").hide();
	objTimer = window.setInterval("refreshData()", 1000);
}

function displayCfgAndTestReport() {
	$("#footDiv").hide();
	$("#runningContent_tab").hide();
	$("#reportContent_tab").show();
	$("#reportContent_tab a").tab('show');
	openReportDiv(function() {
		$("#footDiv").show();
	});
	if (objTimer) {
		window.clearInterval(objTimer);
	}
}

function initScheduleTime() {
	var date = new Date();
	$("#shSelect").val(date.getHours());
	$("#smSelect").val(date.getMinutes());
}

function setDurationHour(durationVal) {
	var durationHour = parseInt(durationVal / 3600000);
	durationHour = durationVal % 3600000 == 0 ? durationHour : durationHour + 1;
	$("#durationHour").val(durationHour);
}
</script>
	</body>
</html>
