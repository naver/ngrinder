<#import "spring.ftl" as spring/>
<#include "ngrinder_macros.ftl">
<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<!--suppress ALL -->
<input type="hidden" id="contextPath" value="${req.getContextPath()}"/>
<div class="navbar navbar-inverse navbar-fixed-top">
	<div class="navbar-inner" style="filter:none">
		<div class="container">
			<a class="brand" href="${req.getContextPath()}/home">
				<img src="${req.getContextPath()}/img/logo_ngrinder_a_header_inv.png" alt="nGrinder"/>
			</a>

			<div>
				<ul class="nav">
					<li id="nav_test">
						<a href="${req.getContextPath()}/perftest/"><@spring.message "navigator.perfTest"/></a>
					</li>
					<li id="nav_script">
						<a href="${req.getContextPath()}/script/"><@spring.message "navigator.script"/></a>
					</li>
				</ul>

				<ul class="nav pull-right">
				<#if clustered?? && clustered>
					<li style="padding-top:5px">
						<img src="${req.getContextPath()}/img/cluster_icon.png"
							 title="Cluster Mode" alt="Cluster Mode">
					</li>
					<li class="divider-vertical"></li>
				</#if>
					<li class="dropdown">
						<a data-toggle="dropdown" class="dropdown-toggle pointer-cursor"> ${(currentUser.userName)!}
						<#if (currentUser.ownerUser)??>(${currentUser.ownerUser.userName})</#if><b class="caret"></b>
						</a>
						<ul class="dropdown-menu">
						<#if (currentUser.ownerUser)??>
							<li>
								<a href="${req.getContextPath()}/user/switch?to="><@spring.message "common.button.return"/></a>
							</li>
						<#else>
							<li>
								<a id="user_profile_menu"
								   class="pointer-cursor"><@spring.message "navigator.dropDown.profile"/></a>
							</li>
							<li>
								<a id="switch_user_menu"
								   class="pointer-cursor"><@spring.message "navigator.dropDown.switchUser"/></a>
							</li>
						</#if>

						<li class="divider"></li>
						<@security.authorize access="hasRole('A')">
							<#if clustered==true>
								<li class="dropdown-submenu">
									<a class="pointer-cursor"><@spring.message "navigator.dropDown.downloadAgent"/></a>
									<ul class="dropdown-menu">
										<@list list_items=visibleRegions; region>
											<li>
												<a href="${req.getContextPath()}/agent/download?region=${region}">
												<@spring.message code="${region}"/>
												</a>
											</li>
										</@list>
									</ul>
								</li>
							<#else>
								<li>
									<a href="${req.getContextPath()}/agent/download">
										<@spring.message "navigator.dropDown.downloadAgent"/>
									</a>
								</li>
							</#if>
						</@security.authorize>

						<@security.authorize access="!hasAnyRole('A')">
							<#if clustered==true>
								<li class="dropdown-submenu">
									<a class="pointer-cursor"><@spring.message "navigator.dropDown.downloadPrivateAgent"/></a>
									<ul class="dropdown-menu">
										<@list list_items=visibleRegions; region>
											<li>
												<a href="${req.getContextPath()}/agent/download/${region}/${currentUser.userId}"/>
												<@spring.message code="${region}"/>
												</a>
											</li>
										</@list>
									</ul>
								</li>
							<#else>
								<li>
									<a href="${req.getContextPath()}/agent/download?owner=${currentUser.userId}"/>
									<@spring.message "navigator.dropDown.downloadPrivateAgent"/>
									</a>
								</li>
							</#if>
						</@security.authorize>
						<li>
							<a href="${req.getContextPath()}/monitor/download"><@spring.message "navigator.dropDown.downloadMonitor"/></a>
						</li>
						<li>
							<a href="https://github.com/naver/ngrinder/wiki/nGrinder-Recorder-Guide" target="_blank"><@spring.message "navigator.dropDown.downloadRecorder"/></a>
						</li>
						<li class="divider"></li>
						<@security.authorize access="hasRole('A')">
							<li>
								<a href="${req.getContextPath()}/user/"><@spring.message "navigator.dropDown.userManagement"/></a>
							</li>
						</@security.authorize>
							<li>
								<a href="${req.getContextPath()}/agent/"><@spring.message "navigator.dropDown.agentManagement"/></a>
							</li>
						<@security.authorize access="hasRole('A')">
							<#if clustered == false>
								<li>
									<a href="${req.getContextPath()}/operation/log"><@spring.message "navigator.dropDown.logMonitoring"/></a>
								</li>
							</#if>
							<#if enableScriptConsole == true>
							<li>
								<a href="${req.getContextPath()}/operation/script_console"><@spring.message "navigator.dropDown.scriptConsole"/></a>
							</li>
							</#if>
							<li>
								<a href="${req.getContextPath()}/operation/system_config"><@spring.message "navigator.dropDown.systemConfig"/></a>
							</li>
						</@security.authorize>
						<@security.authorize access="hasAnyRole('A', 'S')">
							<li class="divider"></li>
							<li>
								<a href="${req.getContextPath()}/operation/announcement"><@spring.message "navigator.dropDown.announcement"/></a>
							</li>
						</@security.authorize>
							<li class="divider"></li>
							<li>
								<a href="${req.getContextPath()}/logout"><@spring.message "navigator.dropDown.logout"/></a>
							</li>
						</ul>
					</li>
					<li class="divider-vertical"></li>
					<li><a href="${helpUrl}" target="_blank"><@spring.message "navigator.help"/></a></li>
				</ul>
			</div>
		</div>
	</div>
</div>
<div class="container" style="padding-top: 40px;">
	<div class="<#if announcement?has_content><#else>hidden</#if>" id="announcement_container">
		<div class="alert alert-block"
			style="padding:5px 20px; margin-bottom:0;">
			<div class="page-header" style="margin:0; padding-bottom:2px">
				<span>
					<#if announcement_new?? && announcement_new==true>
						<span class="label label-important">new</span>
					</#if>
						<span style="margin-top:0; margin-bottom:0; font-size: 15px">
						<@spring.message "announcement.title"/>
						</span>
						<span class="pointer-cursor pull-right" id="hide_announcement">
							<i class="<#if announcement_hide?has_content && announcement_hide == true>icon-plus<#else>icon-minus</#if>"
							   id="announcement_icon">
							</i>
						</span>
					</span>
			</div>
			<div style="margin:10px 5px 0;<#if announcement_hide?? && announcement_hide>display:none;</#if>"
				 id="announcement_content">
			<#if announcement?has_content>
				${announcement?replace('\r\n\r\n', '<br />')?replace('\t', '    ')}
			</#if>
			</div>
		</div>
	</div>
</div>
<div class="modal hide fade" id="user_profile_modal" role="dialog">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
		<h4><@spring.message "navigator.dropDown.profile.title"/></h4>
	</div>
	<div class="modal-body" id="user_profile_modal_content" style="max-height:640px; padding-left:45px">
	</div>
</div>

<div class="modal hide fade" id="user_switch_modal" role="dialog">
	<div class="modal-header" style="border: none;">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
	</div>
	<div class="modal-body">
		<div class="form-horizontal" style="margin-left:20px;overflow-y:hidden">
			<fieldset>
			<@control_group label_style="width:100px" controls_style = "margin-left:140px" label_message_key = "user.switch.title">
				<@security.authorize access="!hasAnyRole('A')">
					<select id="switch_user_select" style="width:310px">
					</select>
				</@security.authorize>
				<@security.authorize access="hasRole('A')">
					<div id="switch_user_select" style="width:310px">
					</div>
				</@security.authorize>
			</@control_group>
			</fieldset>
		</div>
	</div>
</div>

<#include "messages.ftl">

<script type="text/javascript">
	$(document).ready(function () {
		$.ajaxSetup({ cache: false });
		myProfile();
		switchUser();
		showExceptionMsg();
		showInitialMsg();
		$("#hide_announcement").click(function () {
			var $announcementContent = $("#announcement_content");
			if ($announcementContent.is(":hidden")) {
				$announcementContent.show("slow");
				$("#announcement_icon").removeClass("icon-plus").addClass("icon-minus");
				cookie("announcement_hide", "false", 6);
			} else {
				$announcementContent.slideUp();
				$("#announcement_icon").removeClass("icon-minus").addClass("icon-plus");
				cookie("announcement_hide", "true", 6);
			}
		});

	});

	function myProfile() {
		var url = "${req.getContextPath()}/user/profile";
		$("#user_profile_menu").click(function () {
			$("#user_profile_modal_content").load(url, function () {
				$('#user_profile_modal').modal('show');
			});
		});
	}

	function switchUser() {
		$("#switch_user_select").change(function () {
			document.location.href = "${req.getContextPath()}/user/switch?to=" + $(this).val();
		});
		$("#switch_user_menu").click(function () {
			<@security.authorize access="!hasAnyRole('A')">
			$("#switch_user_select").load("${req.getContextPath()}/user/switch_options", function () {
				$(this).val("");
				$("#switch_user_select").select2();
			});
			</@security.authorize>
			<@security.authorize access="hasRole('A')">
			$("#switch_user_select").select2({
				minimumInputLength: 2,
				ajax: {
					url: "${req.getContextPath()}/user/api/switch_options",
					dataType: "json",
                    quietMillis: 1000,
					data: function (term) {
						return {
							keywords: term
						}
					},
					results: function (data) {
						return {results: data};
					}
				},
				formatSelection: function (data) {
					return data.text;
				},
				formatResult: function(data) {
					return data.text
				}
			});
			</@security.authorize>
			$('#user_switch_modal').modal('show');
		});
	}

	function showExceptionMsg() {
	<#if exception??>
		showErrorMsg("${(exception)}");
	</#if>
	}

	function showInitialMsg() {
	<#if message??>
		showSuccessMsg("${(message)}");
	</#if>
	}
</script>
