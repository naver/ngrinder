<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<#import "/spring.ftl" as spring />
<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container">
			<a class="brand" href="#" style="padding:0 20px"><img src="${Request.getContextPath()}/img/logo_ngrinder_a_header.png" alt="nGrinder"></img></a>
			<div class="nav-collapse">
				<ul class="nav">
				  <li id="n_test"><a href="${Request.getContextPath()}/perftest/list">Load Test</a></li>
				  <li id="n_script"><a href="${Request.getContextPath()}/script/list">Script</a></li>
  			  	  <li id="n_user_manager"><a href="${Request.getContextPath()}/user/list">User Manager</a></li>

				  <li id="n_alert"><a href="#contact">Alert</a></li>
				</ul>
				<ul class="nav pull-right">
					<li><a href="#">Login</a></li>
					<li class="divider-vertical"></li>
					<li class="dropdown"><a href="#">Help</a></li>
				</ul>
			</div>
		</div>
	</div>
</div>