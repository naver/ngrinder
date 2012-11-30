<#import "spring.ftl" as spring/>
<#include "select2.ftl"/>
<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container">
			<a class="brand" href="${req.getContextPath()}/home"><img src="${req.getContextPath()}/img/logo_ngrinder_a_header_inv.png" alt="nGrinder"></img></a>
			<div>
				<ul class="nav">
				  <li id="n_test"><a href="${req.getContextPath()}/perftest/list"><@spring.message "navigator.perfTest"/></a></li>
				  <li id="n_script"><a href="${req.getContextPath()}/script/list"><@spring.message "navigator.script"/></a></li>
				</ul>
				
				<ul class="nav pull-right">
					<li class="dropdown">
		            	<a data-toggle="dropdown" class="dropdown-toggle" href="javascript:void(0);">${(currentUser.userName)!}<#if (currentUser.ownerUser)?exists> (${currentUser.ownerUser.userName})<#else></#if><b class="caret"></b></a>
		            	<ul class="dropdown-menu">
		                	<@security.authorize ifAnyGranted="U">
		            		<#if (currentUser.ownerUser)?exists>
		            		<li><a href="${req.getContextPath()}/user/switchUser?switchUserId=${(currentUser.userId)!}"><@spring.message "common.button.return"/></a></li>
		            		<#else>
		                	<li><a id="user_profile_id" href="#"><@spring.message "navigator.dropdown.profile"/></a></li>
		                	<li><a id="switch_user_id" href="#"><@spring.message "navigator.dropdown.switchUser"/></a></li>
		                	</#if>
		                	</@security.authorize>
			            	<@security.authorize ifAnyGranted="A, S">
		               			<li><a href="${req.getContextPath()}/user/list"><@spring.message "navigator.dropdown.userManagement"/></a></li>
				                <li><a href="${req.getContextPath()}/agent/list"><@spring.message "navigator.dropdown.agentManagement"/></a></li>
				                <li><a href="${req.getContextPath()}/operation/log"><@spring.message "navigator.dropdown.logMonitoring"/></a></li>
				                <li><a href="${req.getContextPath()}/operation/scriptConsole"><@spring.message "navigator.dropdown.scriptConsole"/></a></li>  
			                	<li><a href="${req.getContextPath()}/operation/systemConfig"><@spring.message "navigator.dropdown.systemConfig"/></a></li>
			                	<li><a href="${req.getContextPath()}/operation/announcement"><@spring.message "navigator.dropdown.announcement"/></a></li>
			            	</@security.authorize>
		                	<li class="divider"/> 
			          		<li><a href="${req.getContextPath()}/logout"><@spring.message "navigator.dropdown.signout"/></a></li>
		            	</ul>
		            </li>
					<li class="divider-vertical"></li>
					<li><a href="http://www.cubrid.org/wiki_ngrinder/entry/user-guide"><@spring.message "navigator.help"/></a></li>
         		</ul>      		    
			</div>
		</div>
	</div>
</div>

<div class="alert alert-block <#if announcement?has_content><@security.authorize ifAnyGranted="A, S">hidden</@security.authorize><#else>hidden</#if>" style="padding:10px 20px" id="announcementDiv">
	<div class="page-header" style="margin:0; padding-bottom:2px">
		<h3><@spring.message "announcement.alert.title"/></h3>
	</div>
	<div style="margin:10px 5px 0;" id="ancemtContentDiv">
		<#if announcement?has_content>
		<#if announcement?index_of('</') gt 0 || announcement?index_of('<br>') gt 0>
		${announcement}
		<#else>
		${announcement?replace('\n', '<br>')?replace('\t', '&nbsp;&nbsp;&nbsp;&nbsp;')}
		</#if>
		</#if>
	</div>
</div>

<div class="modal fade" id="userProfileModal">
	<div class="modal-header">
		<a class="close" data-dismiss="modal" id="upCloseBtn">&times;</a>
		<h3><@spring.message "navigator.dropdown.profile.title"/></h3>
	</div>
	<div class="modal-body" id="user_profile_modal" style="max-height:540px; padding-left:45px"> 
	</div>	
</div>

<div class="modal fade" id="userSwitchModal">
	<div class="modal-header" style="border: none;">
		<a class="close" data-dismiss="modal" id="upCloseBtn">&times;</a>
	</div>
	<div class="modal-body" style="height:47px;">
		<div class="form-horizontal" style="margin-left:20px">
			<fieldset>
				<div class="control-group">
					<label class="control-label"><@spring.message "user.switch.title"/></label>
					<div class="controls">
						<select id="switchUserSelect" class="span3">
						</select>
					</div>
				</div>
			</fieldset>
		</div>
	</div>	
</div>

<#include "messages.ftl">

<script type="text/javascript">
	function init() {
		myProfile();
		switchUser();
		showExceptionMsg();
	}
	
	function myProfile(){
		var url = "${req.getContextPath()}/user/profile";
		$("#user_profile_id").click(function() {
			$("#user_profile_modal").load(url, function(){
				$('#userProfileModal').modal('show');
			});
		});
	};
	
	function switchUser() {
		$("#switchUserSelect").change(function() {
			document.location.href = "${req.getContextPath()}/user/switchUser?switchUserId=" + $(this).val();
		});
		
		var url = "${req.getContextPath()}/user/switchUserList";
		$("#switch_user_id").click(function() {
			$("#switchUserSelect").load(url, function(){
				$(this).prepend($("<option value=''></option>"));
				$(this).val("");
				$(this).select2({
					placeholder: "<@spring.message "user.switch.select.placeholder"/>"
				});
				$('#userSwitchModal').modal('show');
			});
		});
	}
	
	function showExceptionMsg() {
		<#if exception??>
			showErrorMsg("Error:${(exception.message)!}");
		</#if> 
	}
	
	if(document.loaded) {
		init();
	} else {
	    if (window.addEventListener) {  
	        window.addEventListener('load', init, false);
	    } else {
	        window.attachEvent('onload', init);
	    }
	}
</script>
