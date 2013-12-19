<#import "../common/spring.ftl" as spring>
<#include "../common/ngrinder_macros.ftl">
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

</style>
<!--suppress ALL -->
<div class="row">
	<div class="span6">
		<fieldset>
			<legend><@spring.message "perfTest.config.basicConfiguration"/></legend>
		</fieldset>
		<div class="form-horizontal form-horizontal-2">

			<div class="control-group">
				<div class="row">
					<div class="span4">
						<@control_group name="agentCount" label_message_key="perfTest.config.agent" err_style="margin-left:120px;">
							<@input_append name="agentCount"
								value="${test.agentCount}"
								message="perfTest.config.agent"
								append_prefix="perfTest.config.max" append='<span id="maxAgentCount"></span>' />
						</@control_group>
					</div>

					<div class="span2">
						<#if clustered == true>
							<label for="region" class="control-label" style="margin-left:-50px;width:80px">
								<@spring.message "perfTest.config.region"/>
								<span rel="popover" data-html="true"
									data-content='<@spring.message "perfTest.config.region.help"/>'
									data-placement='top' title='<@spring.message "perfTest.config.region"/>' >
									<i class="icon-question-sign" style="vertical-align: middle;"></i>
								</span>
							</label>
							<select id="region" name="region" class="pull-right required" style="width: 110px">
								<option value=""></option>
								<#list regions as each>
									<option value="${each}" <#if (test.region?? && test.region == each)>selected</#if> >
										<@spring.message "${each}"/>
									</option>
								</#list>
							</select>
							<div id="err_region">
							</div>
						</#if>
					</div>
				</div>
			</div>

			<@control_group  name="vuserPerAgent" label_message_key="perfTest.config.vuserPerAgent">
				<@input_append name="vuserPerAgent"
					value="${(test.vuserPerAgent)!1}"
					message="perfTest.config.vuserPerAgent"
					append_prefix="perfTest.config.max" append="${maxVuserPerAgent!0}" />
				<i class="pointer-cursor expand" id="expand_collapse_btn"></i>
				<div class="pull-right">
					<span class="badge badge-info pull-right"
						  style="padding:7px 20px 7px 20px;-webkit-border-radius:20px;border-radius:20px;-moz-border-radius:20px">
						<span id="vuser_label"><@spring.message "perfTest.config.availVuser"/></span>
						<span id="total_vuser"></span>
					</span>
				</div>
				<div id="process_thread_config_panel" style="display: none;">
					<@input_prepend name="processes" value="${test.processes}" message="perfTest.config.process"
						extra_css="control-group" />
					<@input_prepend name="threads" value="${test.threads}" message="perfTest.config.thread"
						extra_css="control-group" />
				</div>
			</@control_group>

			<@control_group group_id="script_control" name="scriptName" label_message_key="perfTest.config.script">
				<select id="script_name" class="required" name="scriptName" style="width: 275px"
				        old_script="<#if quickScript??>${quickScript}<#else>${(test.scriptName)!}</#if>" ></select>
				<input type="hidden" id="script_revision"
					name="scriptRevision"
					value="${(test.scriptRevision)!-1}"
					old_revision="${(test.scriptRevision)!-1}"/>
				<button class="btn btn-mini btn-info pull-right" type="button"
						id="show_script_btn"
						style="margin-top: 3px; display: none;">R
					<#if test.scriptRevision != -1>
					${test.scriptRevision}
					<#else>
						<#if quickScriptRevision??>${quickScriptRevision}<#else>HEAD</#if>
					</#if>
				</button>
			</@control_group>

			<@control_group name="scriptResources" label_message_key="perfTest.config.scriptResources">
            	<div class="div-resources" id="script_resources"></div>
			</@control_group>

			<#assign targetHosts = test.targetHosts>

			<@control_group label_message_key="perfTest.config.targetHost">
				<#include "host.ftl">
			</@control_group>
			<hr>

			<#assign duration_checked><#if test.threshold == "D">checked</#if></#assign>

			<@control_group label_message_key="perfTest.config.duration" controls_extra_class="docs-input-sizes"
				input_id="duration_ratio" input_name="threshold" input_value="D" radio_checked="${duration_checked}" >

				<select class="select-item" id="select_hour"></select> :
				<select class="select-item" id="select_min"></select> :
				<select	class="select-item" id="select_sec"></select> &nbsp;&nbsp;
				<code>HH:MM:SS</code>
				<input type="hidden" id="duration" name="duration" value="${test.duration}"/>
				<input type="hidden" id="duration_hour" name="durationHour" value="0"/>
				<div id="duration_slider" class="slider" style="margin-left: 0; width: 255px"></div>
				<input id="hidden_duration_input" class="hide" data-step="1"/>

			</@control_group>

			<#assign count_checked><#if test.threshold == "R">checked</#if></#assign>

			<@control_group label_message_key="perfTest.config.runCount"
				input_id="run_count_radio" input_name="threshold" input_value="R" radio_checked="${count_checked}" >
					<@input_append  name="runCount"
						value="${test.runCount}"
						message="perfTest.config.runCount"
						others='number_limit="${maxRunCount}"'
						append_prefix="perfTest.config.max" append="${maxRunCount}" />
			</@control_group>

			<div class="control-group">
				<div class="row">
					<div class="span3">
						<@control_group name="samplingInterval" label_message_key="perfTest.config.samplingInterval">
							<#assign samplingIntervalArray = [1,2,3,4,5,10,30,60]>
							<select class="select-item" id="sampling_interval" name="samplingInterval">
								<#list samplingIntervalArray as eachInterval>
									<option value="${eachInterval}"
										<#if test.samplingInterval != 0>
											<#if eachInterval == test.samplingInterval> selected="selected" </#if>
										<#else>
											<#if eachInterval == 2>
											selected="selected"
											</#if>
										</#if> >
									${eachInterval}
									</option>
								</#list>
							</select>
						</@control_group>
					</div>
					<div class="span3">
						<@control_group name="ignoreSampleCount" label_message_key="perfTest.config.ignoreSampleCount"
							label_style="width:150px;margin-left:-20px"
							err_style="margin-left:100px">
							<@input_popover name="ignoreSampleCount"
								value="${test.ignoreSampleCount}"
								message="perfTest.config.ignoreSampleCount"
								extra_css="input-mini" />
						</@control_group>
					</div>
				</div>
			</div>
			<div class="control-group">
				<div class="row">
					<div class="span3">
						<@control_group name="safeDistribution" label_message_key="perfTest.config.safeDistribution">
							<input type="checkbox" id="safe_distribution_checkbox" name="safeDistribution"
							<#if test.safeDistribution>checked<#else><#if safeFileDistribution!false>checked</#if> </#if> />
							<span style="margin-top: 10px; margin-left: 10px"
								rel='popover' data-html='true'
								data-content='<@spring.message "perfTest.config.safeDistribution.help"/>'
								title='<@spring.message "perfTest.config.safeDistribution"/>'
								id="dist_comment">
								<i class="pointer-cursor icon-question-sign" style="margin-top:5px"></i>
							</span>
						</@control_group>
					</div>
					<div class="span3">
						<@control_group name="param" label_message_key="perfTest.config.param"
							label_style="width:70px;margin-left:-20px"
							err_style="margin-left:-25px"
							controls_style="margin-left:70px">
							<@input_popover name="param"
								value="${(test.param?html)}"
								message="perfTest.config.param"
								others='style="width:120px"'/>
						</@control_group>
					</div>
				</div>
			</div>
		</div>
	</div>
	<!-- end test content left -->

	<div class="span6">
		<fieldset>
			<legend>
                <label for="use_ramp_up"></label><input type="checkbox" id="use_ramp_up" name="useRampUp" style="vertical-align: middle; margin-bottom:5px"
					<#if test.useRampUp == true>checked</#if> />
				<@spring.message "perfTest.config.rampEnable"/>
			</legend>
		</fieldset>
		<div class="form-horizontal form-horizontal-2">
			<div class="control-group">
				<div class="row">
					<div class="span3">
						<@input_label name="initProcesses"
							value="${test.initProcesses}" message="perfTest.config.initialProcesses" />
					</div>

					<div class="span3">
						<@input_label name="processIncrement"
							value="${test.processIncrement}" message="perfTest.config.rampUp" />
					</div>
				</div>
				<div class="row">
					<div class="span3">
						<@input_label name="initSleepTime"
							value="${test.initSleepTime}"
							message="perfTest.config.initialSleepTime" others="<code>MS</code>" />
					</div>
					<div class="span3">
						<@input_label name="processIncrementInterval"
							value="${test.processIncrementInterval}"
							message="perfTest.config.processesEvery" others="<code>MS</code>" />
					</div>
				</div>
			</div>
		</div>
		<legend class="center" style="margin-top:0;padding-top:0"> <@spring.message "perfTest.config.rampUpDes"/> </legend>
		<div id="rampup_chart" class="rampup-chart" style="margin-left: 20px"></div>
	</div>
	<!-- end test content right -->
</div>