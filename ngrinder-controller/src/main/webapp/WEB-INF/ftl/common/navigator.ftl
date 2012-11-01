<#import "/spring.ftl" as spring/>
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
		            	<a data-toggle="dropdown" class="dropdown-toggle" href="javascript:void(0);">${(currentUser.userId)!}<b class="caret"></b></a>
		            	<ul class="dropdown-menu"> 
		                	<li><a id="user_profile_id" href="#"><@spring.message "navigator.dropdown.profile"/></a></li>
			            	<@security.authorize ifAnyGranted="A, S">
				            	<li class="divider"/>
		               			<li><a href="${req.getContextPath()}/user/list"><@spring.message "navigator.dropdown.userManagement"/></a></li>
				                <li><a href="${req.getContextPath()}/agent/list"><@spring.message "navigator.dropdown.agentManagement"/></a></li>
				                <li><a href="${req.getContextPath()}/operation/log"><@spring.message "navigator.dropdown.logMonitoring"/></a></li>
				                <li><a href="${req.getContextPath()}/operation/scriptConsole"><@spring.message "navigator.dropdown.scriptConsole"/></a></li>  
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
<#include "messages.ftl">

<div class="modal fade" id="userProfileModal">
	<div class="modal-header">
		<a class="close" data-dismiss="modal" id="upCloseBtn">&times;</a>
		<h3><@spring.message "navigator.dropdown.profile.title"/></h3>
	</div>
	<div class="modal-body" id="user_profile_modal" style="max-height: 470px">
	</div>	
</div>
<script type="text/javascript">
	function init() {
		myProfile();
		showExceptionMsg();
	}
	
	var myProfile = function(){
		var url = "${req.getContextPath()}/user/profile";
		$("#user_profile_id").on('click', function() {
			$("#user_profile_modal").load(url, function(){
				$('#userProfileModal').modal('show')
			});
		});
	};
	
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
