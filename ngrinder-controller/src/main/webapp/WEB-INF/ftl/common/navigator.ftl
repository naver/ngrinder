<#import "spring.ftl" as spring/>
<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<style>
	#error_msg_div {
		z-index:1152;
	}
</style>
<div class="navbar navbar-inverse navbar-fixed-top">
	<div class="navbar-inner" style="filter:none">
		<div class="container">
			<a class="brand" href="${req.getContextPath()}/home"><img src="${req.getContextPath()}/img/logo_ngrinder_a_header_inv.png" alt="nGrinder"/></a>
			<div>
				<ul class="nav">
				  <li id="nav_test"><a href="${req.getContextPath()}/perftest/"><@spring.message "navigator.perfTest"/></a></li>
				  <li id="nav_script"><a href="${req.getContextPath()}/script/"><@spring.message "navigator.script"/></a></li>
				</ul>
				
				<ul class="nav pull-right">
					<#if clustered?? && clustered>
						<li style="padding-top:5px"><img src="${req.getContextPath()}/img/cluster_icon.png" title="Cluster Mode" alt="Cluster Mode"></li>  
						<li class="divider-vertical"></li>
					</#if>
					<li class="dropdown">
		            	<a data-toggle="dropdown" class="dropdown-toggle pointer-cursor">${(currentUser.userName)!}<#if (currentUser.ownerUser)?exists> (${currentUser.ownerUser.userName})<#else></#if><b class="caret"></b></a>
		            	<ul class="dropdown-menu">
		            		<#if (currentUser.ownerUser)?exists>
			            		<li><a href="${req.getContextPath()}/user/switch?to="><@spring.message "common.button.return"/></a></li>
		            		<#else>
			                	<li><a id="user_profile_menu" href="javascript:void(0)"><@spring.message "navigator.dropdown.profile"/></a></li>
			                	<li><a id="switch_user_menu" href="javascript:void(0)"><@spring.message "navigator.dropdown.switchUser"/></a></li>
		                	</#if>
		                	 
			            	<@security.authorize ifAnyGranted="A">
			            		<li class="divider"></li> 
		               			<li><a href="${req.getContextPath()}/user/"><@spring.message "navigator.dropdown.userManagement"/></a></li>
				                <li><a href="${req.getContextPath()}/agent/"><@spring.message "navigator.dropdown.agentManagement"/></a></li>
							<#if clustered == false>
				            	<li><a href="${req.getContextPath()}/operation/log"><@spring.message "navigator.dropdown.logMonitoring"/></a></li>
				            </#if>
				                <li><a href="${req.getContextPath()}/operation/script_console"><@spring.message "navigator.dropdown.scriptConsole"/></a></li>  
			                	<li><a href="${req.getContextPath()}/operation/system_config"><@spring.message "navigator.dropdown.systemConfig"/></a></li>
			            	</@security.authorize>
			            	<@security.authorize ifAnyGranted="S, A">
			            		<li class="divider"></li>  
			            		<li><a href="${req.getContextPath()}/operation/announcement"><@spring.message "navigator.dropdown.announcement"/></a></li>
			            	</@security.authorize>
		                	<li class="divider"></li> 
			          		<li><a href="${req.getContextPath()}/logout"><@spring.message "navigator.dropdown.signout"/></a></li>
		            	</ul>
		            </li>
					<li class="divider-vertical"></li>
					<li><a href="${helpUrl}" target="_blank"><@spring.message "navigator.help"/></a></li>
         		</ul>      		    
			</div>
		</div>
	</div>
</div>
<div class="container <#if announcement?has_content><#else>hidden</#if>" style=" margin-bottom:-20px" id="announcement_container">
	<div class="alert alert-block" style="padding:5px 20px;">  
		<div class="page-header" style="margin:0; padding-bottom:2px">
			<span>
				<h5 style="margin-top:0px; margin-bottom:0px"><#if announcement_new?? && announcement_new==true><span class="label label-important">new</span> </#if><@spring.message "announcement.alert.title"/></h5>
				<a  class="pointer-cursor" id="hide_announcement"><i class="<#if announcement_hide?has_content && announcement_hide == true>icon-plus<#else>icon-minus</#if> pull-right" id="announcement_icon" style="margin-top:-20px"></i>
			</a></span> 
		</div>
		<div style="margin:10px 5px 0;<#if announcement_hide?? && announcement_hide>display:none;</#if>" id="announcement_content">
			<#if announcement?has_content>
				<#if announcement?index_of('</') gt 0 || announcement?index_of('<br>') gt 0> 
					${announcement}
				<#else>
					${announcement?replace('\n', '<br>')?replace('\t', '&nbsp;&nbsp;&nbsp;&nbsp;')} 
				</#if>
			</#if> 
		</div>
	</div>
</div>
<div class="modal hide fade" id="user_profile_modal"  role="dialog">
	<div class="modal-header"> 
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
		<h4><@spring.message "navigator.dropdown.profile.title"/></h4> 
	</div>
	<div class="modal-body" id="user_profile_modal_content" style="max-height:640px; padding-left:45px"> 
	</div>	
</div>

<div class="modal hide fade" id="user_switch_modal"  role="dialog">
	<div class="modal-header" style="border: none;">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
	</div>
	<div class="modal-body" > 
		<div class="form-horizontal" style="margin-left:20px">
			<fieldset>
				<div class="control-group">
					<label class="control-label" style="width:100px"><@spring.message "user.switch.title"/></label>
					<div class="controls" style="margin-left:140px">
						<select id="switch_user_select" style="width:310px">  
						</select>
					</div>
				</div>
			</fieldset>
		</div>
	</div>	
</div>

<#include "messages.ftl">

<script type="text/javascript">
	$(document).ready(function() {
		$.ajaxSetup({ cache: false });
		myProfile();
		switchUser();
		showExceptionMsg();
		showInitialMsg();
		$("#hide_announcement").click( function() {
			if ($("#announcement_content").is(":hidden")) {
				$("#announcement_content").show("slow");
				$("#announcement_icon").removeClass("icon-plus").addClass("icon-minus");
				cookie("announcement_hide", "false", 6);
			} else {
				$("#announcement_content").slideUp();
				$("#announcement_icon").removeClass("icon-minus").addClass("icon-plus");
				cookie("announcement_hide", "true", 6);
			}
		});
		
	});
	
	function myProfile(){
		var url = "${req.getContextPath()}/user/profile";
		$("#user_profile_menu").click(function() {
			$("#user_profile_modal_content").load(url, function(){
				$('#user_profile_modal').modal('show');
			});
		});
	};
	
	function switchUser() {
		$("#switch_user_select").change(function() {
			document.location.href = "${req.getContextPath()}/user/switch?to=" + $(this).val();
		});
		var url = "${req.getContextPath()}/user/switch_options";
		$("#switch_user_menu").click(function() {
			$("#switch_user_select").load(url, function(){
				$(this).prepend($("<option value=''></option>"));
				$(this).val("");
				$("#switch_user_select").select2();
				$('#user_switch_modal').modal('show');
				
			});
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
