<div class="row">
	<div class="span6">
		<div class="form-horizontal form-horizontal-2">
			<fieldset>
				<legend><@spring.message "perfTest.configuration.basicConfiguration"/></legend>
				<div class="control-group">
					<label for="agentCount" class="control-label"><@spring.message "perfTest.configuration.agent"/></label>
					<div class="controls">
						<table width="100%">
							<colgroup>
								<col width="160px">
								<col>
							</colgroup>
							<tbody>
								<tr>
									<td>
										<div class="input-append">
											<input type="text" class="input input-mini" rel="popover"
												id="agentCount" name="agentCount" value="${(test.agentCount)!0}" 
												data-content='<@spring.message "perfTest.configuration.agent.help"/>' 
												data-original-title="<@spring.message "perfTest.configuration.agent"/>"><span class="add-on"><@spring.message "perfTest.configuration.max"/><span id="maxAgentCount"></span></span>
										</div>
									</td>
									<td>
										<#if clustered == true>											
											<label for="regionSelect" class="region" ><@spring.message "perfTest.configuration.region"/><span rel="popover" data-content='<@spring.message "perfTest.configuration.region.help"/>' data-original-title='<@spring.message "perfTest.configuration.region"/>' type="toggle"> <i class="icon-question-sign" style="vertical-align:middle;"></i></span></label>
								 			<select id="regionSelect" name="region" class="pull-right" style="width:110px" >
												<#list regionAgentCountMap?keys as eachRegion>
													<option value="${eachRegion}" <#if (test?? && test.region?? && test.region == eachRegion)>selected </#if> > <@spring.message "${eachRegion}"/></option>
												</#list> 
											</select>
										</#if>
									</td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
				<div class="control-group">
					<label for="vuserPerAgent" class="control-label"><@spring.message "perfTest.configuration.vuserPerAgent"/></label>
					<div class="controls">
						<table style="width:100%">
							<colgroup>
								<col width="300px"/>
								<col width="*"/>
							</colgroup>
							<tr>
								<td>
									<div class="input-append">
										<input type="text" class="input input-mini" rel="popover"
											id="vuserPerAgent" name="vuserPerAgent"	value="${(test.vuserPerAgent)!1}" rel="popover"	
											data-content='<@spring.message "perfTest.configuration.vuserPerAgent.help"/>'
											data-original-title="<@spring.message "perfTest.configuration.vuserPerAgent"/>"><span class="add-on"><@spring.message "perfTest.configuration.max"/>${(maxVuserPerAgent)}</span>
									</div>
									<a href="javascript:void(0)"><i class="expand" id="expandAndCollapse"></i></a>	
								</td> 
								<td>
									<div class="pull-right">
										<span class="badge badge-info pull-right" ><span id="vuserlabel"><@spring.message "perfTest.configuration.availVuser"/></span><span id="vuserTotal"></span></span>
									</div> 
								</td>
							</tr>
							<tr id="processAndThreadPanel" style="display:none;">
								<td colspan="2">
									<span>
									<div class="input-prepend control-group" style="margin-bottom:0">
										<span class="add-on" title='<@spring.message "perfTest.report.process"/>'><@spring.message "perfTest.report.process"/></span><input class="input span1" type="text" id="processes" name="processes" value="${(test.processes)!1}"/> 
									</div>
									<div class="input-prepend control-group" style="margin-bottom:0">
										<span class="add-on" title='<@spring.message "perfTest.report.thread"/>'><@spring.message "perfTest.report.thread"/></span><input class="input span1" type="text" id="threads" name="threads" value="${(test.threads)!1}"/>
									</div>
									</span>
								</td>
							</tr>
							<tr>
						   		<td class="vuserPerAgent processes threads">
								</td>
							</tr>
						</table>
					</div>
				</div>
				<div class="control-group" id="scriptControl">
					<label for="scriptName" class="control-label"><@spring.message "perfTest.configuration.script"/></label>
					<div class="controls">
						<table style="width:100%">
							<colgroup>
								<col width="*"/>
								<col width="100px"/>
							</colgroup>
							<tr>
							<td>
								<select id="scriptName" class="required" name="scriptName" style="width:275px" oldScript="${(test.scriptName)!}"> 
									<#if test?? && test.createdUser.userId != currentUser.factualUser.userId>
										<#assign showScriptVisible = true>
										<option value="${test.scriptName}" selected validated="${(scriptItem.properties.validated)!"0"}"></option>
									<#else>
										<option value=""></option>
										<#if scriptList?? && scriptList?size &gt; 0> 
											<#list scriptList as scriptItem> 
												<option value="${scriptItem.path}" <#if  (test?? && scriptItem.path == test.scriptName) || (quickScript?? && quickScript == scriptItem.path)>selected<#assign showScriptVisible = true></#if> validated="${(scriptItem.properties.validated)!"0"}"></option> 
											</#list> 
										</#if>
									</#if>
								</select>
							</td>
							<td>
								<input type="hidden" id="scriptRevision" name="scriptRevision" value="${(test.scriptRevision)!-1}" oldRevision="${(test.scriptRevision)!-1}">
								<button class="btn btn-mini btn-info pull-right" type="button" id="showScript" style="margin-top:3px; <#if !(showScriptVisible??)>display:none;</#if>">
								R
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
					<label for="Script Resources" class="control-label"><@spring.message "perfTest.configuration.scriptResources"/></label>
					<div class="controls">
						<div class="div-resources" id="scriptResources"></div>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label"><@spring.message "perfTest.configuration.targetHost"/></label>
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
					<label class="control-label"> <input type="radio" id="durationRadio" name="threshold" value="D" <#if (test?? && test.threshold == "D")||!(test??) >checked</#if>> <@spring.message "perfTest.configuration.duration"/>
					</label>
					<div class="controls docs-input-sizes">
						<select class="select-item" id="hSelect"></select> : 
						<select class="select-item" id="mSelect"></select> : 
						<select class="select-item" id="sSelect"></select> &nbsp;&nbsp;
						<code>HH:MM:SS</code>
						<input type="hidden" id="duration" name="duration" value="${(test.duration)!60000}">
						<input type="hidden" id="durationHour" name="durationHour" value="0">
						<div id="durationSlider" class="slider" style="margin-left: 0; width: 255px"></div>
						<input id="hiddenDurationInput" class="hide" data-step="1">
					</div>
				</div>
				<div class="control-group">
					<label for="runCount" class="control-label"> <input type="radio" id="runCountRadio" name="threshold" value="R" <#if test?? && test.threshold == "R" >checked</#if>> 
						<@spring.message "perfTest.configuration.runCount"/>
					</label>
					<div class="controls">
						<div class="input-append">
							<input type="text" 
								data-original-title="<@spring.message "perfTest.configuration.runCount"/>"
								data-content="<@spring.message "perfTest.configuration.runCount.help"/>"	
								rel="popover"												
								id="runCount" class="input input-mini" number_limit="${(maxRunCount)}" name="runCount"
								value="${(test.runCount)!0}"><span class="add-on"><@spring.message "perfTest.configuration.max"/>${(maxRunCount)}</span>
						</div>
					</div>
				</div>
				<div class="control-group">
					<label for="samplingInterval" class="control-label"> <@spring.message "perfTest.configuration.samplingInterval"/> </label>
					<div class="controls">
						<table width="100%">
							<colgroup>
								<col width="100px">
								<col>
							</colgroup>
							<tbody>
								<tr>
									<td>
										<#assign samplingIntervalArray = [1,2,3,4,5]>
										<select class="select-item" id="samplingInterval" name="samplingInterval">
											<#list samplingIntervalArray as eachInterval>
												<option value="${eachInterval}" 
													<#if test?? && test.samplingInterval != 0>
														<#if eachInterval == test.samplingInterval>
															selected="selected"
														</#if>
													<#else>
														<#if eachInterval == 2>
															selected="selected"
														</#if>
													</#if>
													>${eachInterval}</option>
											</#list>
										</select>
									</td>
									<td>
										<label for="ignoreSampleCount" class="control-label" style="width:150px"> <@spring.message "perfTest.configuration.ignoreSampleCount"/> </label>
										<div class="controls">
											<input type="text" class="input input-mini" 
												data-original-title="<@spring.message "perfTest.configuration.ignoreSampleCount"/>"
												data-content='<@spring.message "perfTest.configuration.ignoreSampleCount.help"/>'
												rel="popover"												
												id="ignoreSampleCount" name="ignoreSampleCount"
												value="${(test.ignoreSampleCount)!0}">
										</div>
									</td>
								</tr>
							</tbody>
						</table>
						
					
					</div>
				</div>
				<div class="control-group">
					<label for="safeDistribution" class="control-label"> <@spring.message "perfTest.configuration.safeDistribution"/> </label>
					<div class="controls">
						<input type="checkbox" id="safeDistributionCheckBox" name="safeDistribution"
							<#if test?? && test.safeDistribution?default(false) == true>
								checked
							<#else>
								<#if safeFileDistribution?default(false) == true>checked</#if>	
							</#if> 
						/>
						<span style="margin-top:10px;margin-left:10px" rel="popover" 
							data-content="<@spring.message "perfTest.configuration.safeDistribution.help"/>" 
							data-original-title="<@spring.message "perfTest.configuration.safeDistribution"/>" type="toggle" id="dist_comment">
							<i class="icon-question-sign" style="margin-top:5px"></i>
						</span> 
					</div>
				</div>
			</fieldset>
		</div>
	</div>
	<!-- end test content left -->
	
	<div class="span6">
		<fieldset>
			<legend>
				<input type="checkbox" id="rampupCheckbox" name="useRampUp" style="vertical-align:middle"
					<#if test?? && test.useRampUp?default(false) == true>checked</#if>/>
				<@spring.message "perfTest.configuration.rampEnable"/>
			</legend>
		</fieldset>
		<table>
			<tr>
				<td style="width: 50%">
					<div class="form-horizontal form-horizontal-2">
						<fieldset>
							<div class="control-group">
								<label for="initProcesses" class="control-label"> <@spring.message "perfTest.configuration.initalProcesses"/> </label>
								<div class="controls">
									<input type="text" class="input input-mini" id="initProcesses" name="initProcesses"
										value="${(test.initProcesses)!0}"/>
								</div>
							</div>
							<div class="control-group">
								<label for="processIncrement" class="control-label"> <@spring.message "perfTest.configuration.rampup"/> </label>
								<div class="controls">
									<input type="text" class="input input-mini" id="processIncrement"
										name="processIncrement" value="${(test.processIncrement)!1}">
								</div>
							</div>
						</fieldset>
					</div>
				</td>
				<td>
					<div class="form-horizontal form-horizontal-2">
						<fieldset>
							<div class="control-group">
								<label for="initSleepTime" class="control-label"> <@spring.message "perfTest.configuration.initalSleepTime"/> </label>
								<div class="controls">
									<input type="text" class="input input-mini" id="initSleepTime" name="initSleepTime"
										value="${(test.initSleepTime)!0}">
									<code>MS</code>
								</div>
							</div>
							<div class="control-group">
								<label for="processIncrementInterval" class="control-label"> 
									<@spring.message "perfTest.configuration.processesEvery"/> 
								</label>
								<div class="controls">
									<input type="text" class="input input-mini" id="processIncrementInterval"
										name="processIncrementInterval" value="${(test.processIncrementInterval)!1000}">
									<code>MS</code>
								</div>
							</div>
						</fieldset>
					</div>
				</td>
			</tr>
		</table>
		<legend class="center">
			<@spring.message "perfTest.configuration.rampUpDes"/>
		</legend>
		<div id="rampChart" class="rampChart" style="margin-left:20px"></div>
	</div>
	<!-- end test content right -->
</div>
