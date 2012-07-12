<#import "/spring.ftl" as spring />
<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container">
			<a class="brand" href="#" style="padding:5px 20px"><img src="${req.getContextPath()}/img/logo_ngrinder_a_header_inv.png" alt="nGrinder"></img></a>
			<div class="" style="height:auto">
				<ul class="nav">
				  <li id="n_test"><a href="${req.getContextPath()}/perftest/list">Load Test</a></li>
				  <li id="n_script"><a href="${req.getContextPath()}/script/list">Script</a></li>
				  <li id="n_alert"><a href="#contact">Alert</a></li>
				</ul>
				
				
				<ul class="nav pull-right">
					<li class="dropdown"><a href="#">Help</a></li>
         		</ul>
				

				 
				<@security.authorize ifAnyGranted="U, A, S">
					<div class="btn-group pull-right">
			            <a class="btn dropdown-toggle" data-toggle="dropdown" href="#">
			              <i class="icon-user"></i>Username
			              <span class="caret"></span>
			            </a>
			            <ul class="dropdown-menu"> 
			              <li><a href="${req.getContextPath()}/user/profile">Profile</a></li>
			              <li class="divider"></li>
			              <li><a href="${req.getContextPath()}/logout">Sign Out</a></li>
			            </ul> 
	      		    </div>
      		    </@security.authorize>
      		    <@security.authorize ifAnyGranted="A, S">
					<div class="btn-group pull-right">
  			  	  		<a href="#" class="btn dropdown-toggle" data-toggle="dropdown">Admin <span class="caret"></span></a>
  			  	  		<ul class="dropdown-menu">
	               			<li><a href="${req.getContextPath()}/user/list">User Management</a></li>
	               			<li><a href="${req.getContextPath()}/config/view">Test Default Options</a></li>
			                <li><a href="${req.getContextPath()}/agent/list">Agent Management</a></li>
			            </ul>
					</div>
  			  	</@security.authorize>
      		    
			</div>
		</div>
	</div>
</div>