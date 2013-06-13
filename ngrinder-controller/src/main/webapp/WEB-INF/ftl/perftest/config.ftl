<div class="row">
	<div class="span6">
		<fieldset>
			<legend><@spring.message "perfTest.configuration.basicConfiguration"/></legend>
		</fieldset>
		<div class="form-horizontal form-horizontal-2">

			<div class="control-group">
				
				<div class="row">
					<div class="span4">
						<div class="control-group">
							<label for="agent_count" class="control-label">
								<@spring.message "perfTest.configuration.agent"/>
							</label>
							<div class="controls">
								<div class="input-append">
									<input type="text" class="input input-mini" 
										rel="popover" id="agent_count" name="agentCount"
										value="${(test.agentCount)!0}" data-html="true"
										data-content='<@spring.message "perfTest.configuration.agent.help"/>' 
										title='<@spring.message "perfTest.configuration.agent"/>'/>
									<span class="add-on">
										<@spring.message "perfTest.configuration.max"/>
										<span id="maxAgentCount"></span>
									</span>
								</div>
							
							</div>
							<div id="err_agent_count" class="small_error_box" style="margin-left:120px;">
							</div>
						</div>
					</div>
					<div class="span2">
						<#if clustered == true>
							<label for="region" class="control-label" style="margin-left:-50px;width:80px">
								<@spring.message "perfTest.configuration.region"/>
								<span rel="popover" data-html="true"
									data-content='<@spring.message "perfTest.configuration.region.help"/>' 
									data-placement='top' title='<@spring.message "perfTest.configuration.region"/>' > 
									<i class="icon-question-sign" style="vertical-align: middle;"></i>
								</span>
							</label> 
							<select id="region" name="region" class="pull-right" style="width: 110px">
								<#list regionAgentCountMap?keys as eachRegion>
									<option value="${eachRegion}" <#if (test?? && test.region?? && test.region == eachRegion)>selected</#if> > 
										<@spring.message "${eachRegion}"/>
									</option> 
								</#list>
							</select> 
							<div id="err_aregion">
							</div>
						</#if>
					</div>
				</div>
			</div>
			<div class="control-group">
				<label for="vuser_per_agent" class="control-label">
					<@spring.message "perfTest.configuration.vuserPerAgent"/>
				</label>
				<div class="controls">
					<table style="width: 100%">
						<colgroup>
							<col width="300px" />
							<col width="*" />
						</colgroup>
						<tr>
							<td>
								<div class="input-append">
									<input type="text" class="input input-mini" id="vuser_per_agent" 
										name="vuserPerAgent"
										value="${(test.vuserPerAgent)!1}" 
										rel="popover" 
										data-html="true"
										data-content='<@spring.message "perfTest.configuration.vuserPerAgent.help"/>' 
										title="<@spring.message "perfTest.configuration.vuserPerAgent"/>"/>
									<span class="add-on"><@spring.message "perfTest.configuration.max"/>${(maxVuserPerAgent)}</span>
								</div> 
								<a href="javascript:void(0)">
									<i class="expand" id="expand_collapse_btn"></i>
								</a>
							</td>
							<td>
								<div class="pull-right">
									<span class="badge badge-info pull-right" style="padding:7px 20px 7px 20px;-webkit-border-radius:20px;border-radius:20px;-moz-border-radius:20px">
										<span id="vuserlabel"><@spring.message "perfTest.configuration.availVuser"/></span><span id="total_vuser"></span>
									</span>
								</div>
							</td>
						</tr>
						<tr id="process_thread_config_panel" style="display: none;">
							<td colspan="2">
								<span>
									<div class="input-prepend control-group" style="margin-bottom: 0">
										<span class="add-on" title='<@spring.message "perfTest.report.process"/>'>
											<@spring.message "perfTest.report.process"/>
										</span>
										<input class="input span1" type="text" id="processes" name="processes" value="${(test.processes)!1}" />
									</div>
									<div class="input-prepend control-group" style="margin-bottom: 0">
										<span class="add-on" title='<@spring.message "perfTest.report.thread"/>'>
											<@spring.message "perfTest.report.thread"/>
										</span>
										<input class="input span1" type="text" id="threads" name="threads" value="${(test.threads)!1}" />
									</div>
								</span>
							</td>
						</tr>
						<tr>
							<td class="vuser-per-agent processes threads"></td>
						</tr>
					</table>
				</div>
			</div>
			<div class="control-group" id="script_control">
				<label for="script_name" class="control-label">
					<@spring.message "perfTest.configuration.script"/>
				</label>
				<div class="controls">
					<table style="width: 100%">
						<colgroup>
							<col width="*" />
							<col width="100px" />
						</colgroup>
						<tr>
							<td>
								<select id="script_name" class="required" name="scriptName" style="width: 275px" old_script="<#if quickScript??>${quickScript}<#else>${(test.scriptName)!}</#if>">
								</select>
							</td>
							<td>
								<input type="hidden" id="script_revision" 
									name="scriptRevision" 
									value="${(test.scriptRevision)!-1}"
									old_revision="${(test.scriptRevision)!-1}"/>
								<button class="btn btn-mini btn-info pull-right" type="button" 
									id="show_script_btn"
									style="margin-top: 3px; &lt;# if !(showScriptVisible??)&gt;display: none;">R 
									<#if test?? && test.scriptRevision != -1> 
										${test.scriptRevision} 
									<#else> 
										<#if quickScriptRevision??>${quickScriptRevision}<#else>HEAD</#if> 
									</#if>
								</button>
							</td>
						</tr>
					</table>
				</div>
			</div>
			<div class="control-group">
				<label for="Script Resources" class="control-label">
					<@spring.message "perfTest.configuration.scriptResources"/>
				</label>
				<div class="controls">
					<div class="div-resources" id="scriptResources"></div>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">
					<@spring.message "perfTest.configuration.targetHost"/></label>
					<#if test?? && test.targetHosts??>
						<#assign targetHosts = test.targetHosts>
					<#elseif targetHostString??>
						<#assign targetHosts = targetHostString> 
					<#else>
						<#assign targetHosts = "">
					</#if>
				<div class="controls">
					<#include "host.ftl">
				</div>
			</div>
			<hr>
			<div class="control-group">
				<label class="control-label"> 
					<input type="radio" id="duration_ratio" name="threshold" value="D"
						<#if (test?? && test.threshold == "D")||!(test??) >checked</#if>/> 
					<@spring.message "perfTest.configuration.duration"/>
				</label>
				<div class="controls docs-input-sizes">
					<select class="select-item" id="select_hour"></select> : 
					<select class="select-item" id="select_min"></select> : 
					<select	class="select-item" id="select_sec"></select> &nbsp;&nbsp;
					<code>HH:MM:SS</code>
					<input type="hidden" id="duration" name="duration" value="${(test.duration)!60000}"/>
					<input type="hidden" id="duration_hour" name="durationHour" value="0"/>
					<div id="duration_slider" class="slider" style="margin-left: 0; width: 255px"></div>
					<input id="hidden_duration_input" class="hide" data-step="1"/>
				</div>
			</div>
			<div class="control-group">
				<label for="run_count" class="control-label"> 
					<input type="radio" id="run_count_radio" name="threshold" value="R"<#if test?? && test.threshold == "R" >checked</#if>/>
					<@spring.message "perfTest.configuration.runCount"/>
				</label>
				<div class="controls">
					<div class="input-append">
						<input type='text' 
							rel='popover' data-html='true'
							title='<@spring.message "perfTest.configuration.runCount"/>'
							data-content='<@spring.message "perfTest.configuration.runCount.help"/>' 
							id="run_count" class="input input-mini" number_limit="${(maxRunCount)}" name="runCount" value="${(test.runCount)!0}">
						<span class="add-on"><@spring.message "perfTest.configuration.max"/>${(maxRunCount)}</span>
					</div>
				</div>
			</div>
			<div class="control-group">
				<div class="row">
					<div class="span3">
						<div class="control-group">
							<label for="sampling_interval" class="control-label">
								<@spring.message "perfTest.configuration.samplingInterval"/>
							</label>
							<div class="controls">
								<#assign samplingIntervalArray = [1,2,3,4,5]> 
								<select class="select-item" id="sampling_interval" name="samplingInterval"> 
									<#list samplingIntervalArray as eachInterval>
										<option value="${eachInterval}"
											<#if test?? && test.samplingInterval != 0> 
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
							</div>
						</div>
					</div>
					<div class="span3">
						<div class="control-group">
							<label for="ignore_sample_count" class="control-label" style="width: 150px;margin-left:-20px">
								<@spring.message "perfTest.configuration.ignoreSampleCount"/>
							</label>
							<div class="controls">
								<input type="text" class="input input-mini" 
										id="ignore_sample_count" name="ignoreSampleCount" 
										rel='popover'
										title='<@spring.message "perfTest.configuration.ignoreSampleCount"/>' 
										data-html='true'
										data-content='<@spring.message "perfTest.configuration.ignoreSampleCount.help"/>' 
										data-placement='top'
										value="${(test.ignoreSampleCount)!0}">
							</div>
							<div id="err_ignore_sample_count" class="small_error_box" style="margin-left:100px">
							</div>
						</div>
					</div>
				</div>
			</div>
			<div class="control-group">
				<label for="safeDistribution" class="control-label"> 
					<@spring.message "perfTest.configuration.safeDistribution"/>
				</label>
				<div class="controls">
					<input type="checkbox" id="safe_distribution_checkbox" name="safeDistribution"
					<#if test?? && test.safeDistribution?default(false) == true>checked<#else><#if safeFileDistribution?default(false)==true>checked</#if> </#if> /> 
					<span style="margin-top: 10px; margin-left: 10px" 
						rel='popover' data-html='true'
						data-content='<@spring.message "perfTest.configuration.safeDistribution.help"/>' 
						title='<@spring.message "perfTest.configuration.safeDistribution"/>'
						id="dist_comment"> 
						<i class="icon-question-sign" style="margin-top:5px"></i>
					</span> 
				</div>
			</div>
		</div>
	</div>
	<!-- end test content left -->

	<div class="span6">
		<fieldset>
			<legend>
				<input type="checkbox" id="use_ramp_up" name="useRampUp" style="vertical-align: middle; margin-bottom:5px"
					<#if test?? && test.useRampUp?default(false) == true>checked</#if> /> 
				<@spring.message "perfTest.configuration.rampEnable"/>
			</legend>
		</fieldset>
		<table>
			<tr>
				<td style="width: 50%">
					<div class="form-horizontal form-horizontal-2">
						<fieldset>
							<div class="control-group">
								<label for="init_processes" class="control-label"> 
									<@spring.message "perfTest.configuration.initalProcesses"/> 
								</label>
								<div class="controls">
									<input type="text" class="input input-mini" id="init_processes" name="initProcesses" value="${(test.initProcesses)!0}" />
								</div>
								<div id="err_init_processes" style="margin-bottom: 0px;height: 15px;line-height:15px"></div>
							</div>
							<div class="control-group">
								<label for="process_increment" class="control-label"> 
									<@spring.message "perfTest.configuration.rampup"/>
								</label>
								<div class="controls">
									<input type="text" class="input input-mini" id="process_increment" name="processIncrement"
										value="${(test.processIncrement)!1}">
								</div>
								<div id="err_process_increment" style="margin-bottom: 0px;height: 15px;line-height:15px"></div>							
							</div>
						</fieldset>
					</div>
				</td>
				<td>
					<div class="form-horizontal form-horizontal-2">
						<fieldset>
							<div class="control-group">
								<label for="init_sleep_time" class="control-label"> <@spring.message
									"perfTest.configuration.initalSleepTime"/> </label>
								<div class="controls">
									<input type="text" class="input input-mini" id="init_sleep_time" name="initSleepTime"
										value="${(test.initSleepTime)!0}">
									<code>MS</code>
								</div>
								<div id="err_init_sleep_time" style="margin-bottom: 0px;height: 15px;line-height:15px">
								</div>
							</div>
							<div class="control-group">
								<label for="process_increment_interval" class="control-label"> 
									<@spring.message "perfTest.configuration.processesEvery"/>
								</label>
								<div class="controls">
									<input type="text" class="input input-mini" id="process_increment_interval" name="processIncrementInterval"
										value="${(test.processIncrementInterval)!1000}">
									<code>MS</code>
								</div>
								<div id="err_process_increment_interval" style="margin-bottom: 0px;height: 15px;line-height:15px">
								</div>
							</div>
						</fieldset>
					</div>
				</td>
			</tr>
		</table>
		<legend class="center"> <@spring.message "perfTest.configuration.rampUpDes"/> </legend>
		<div id="rampup_chart" class="rampup-chart" style="margin-left: 20px"></div>
	</div>
	<!-- end test content right -->
</div>
