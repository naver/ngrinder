<div class="row">
	<div class="span6">
		<div class="page-header">
			<h4><@spring.message "perfTest.configuration.basicConfiguration"/></h4>
		</div>
		<div class="form-horizontal form-horizontal-2">
			<fieldset>
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
											<label for="regionSelect" class="region" ><@spring.message "perfTest.configuration.region"/><span rel="popover" data-content='<@spring.message "perfTest.configuration.region.help"/>' data-original-title='<@spring.message "perfTest.configuration.region"/>' type="toggle"><i class="icon-question-sign"></i></span></label>
								 			<select id="regionSelect" name="region" class="pull-right" style="width:120px" >
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
										<option value="${test.scriptName}" selected validated="${(scriptItem.properties.validated)!"0"}">${test.scriptName} - <@spring.message "perfTest.configuration.script.belongTo" />${test.createdUser.userName}</option>
									<#else>
										<option value=""></option>
										<#if scriptList?? && scriptList?size &gt; 0> 
											<#list scriptList as scriptItem> 
												<option value="${scriptItem.path}" <#if  (test?? && scriptItem.path == test.scriptName) || (quickScript?? && quickScript == scriptItem.path)>selected<#assign showScriptVisible = true></#if> validated="${(scriptItem.properties.validated)!"0"}">${scriptItem.path}</option> 
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
									HEAD
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
					<label for="ignoreSampleCount" class="control-label"> <@spring.message "perfTest.configuration.ignoreSampleCount"/> </label>
					<div class="controls">
						<input type="text" class="input input-mini" 
							data-original-title="<@spring.message "perfTest.configuration.ignoreSampleCount"/>"
							data-content='<@spring.message "perfTest.configuration.ignoreSampleCount.help"/>'
							rel="popover"												
							id="ignoreSampleCount" name="ignoreSampleCount"
							value="${(test.ignoreSampleCount)!0}">
					</div>
				</div>
			</fieldset>
		</div>
	</div>
	<!-- end test content left -->
	
	<div class="span6">
		<div class="page-header">
			<label class="checkbox" style="margin-bottom: 0"> 
				<input type="checkbox" id="rampupCheckbox" name="useRampUp"
					<#if test?? && test.useRampUp?default(false) == true>checked</#if> 
				/>
				<h4>
					<@spring.message "perfTest.configuration.rampEnable"/>
				</h4>
			</label>
		</div>
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
		<div class="page-header center" style="padding-bottom:10px;">
			<strong><@spring.message "perfTest.configuration.rampUpDes"/></strong>
		</div>
		<div id="rampChart" class="rampChart" style="margin-left:20px"></div>
	</div>
	<!-- end test content right -->
</div>
