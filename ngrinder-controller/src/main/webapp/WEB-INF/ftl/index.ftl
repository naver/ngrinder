<!DOCTYPE html>
<html>
	<head>
		<title>nGrinder Home</title>
		<#include "common/common.ftl">
		<style>
			.hero-unit { margin-left:0px;background-image:url('${req.getContextPath()}/img/bg_main_banner.png');margin-top:-20px;margin-bottom:10px;height:200px;padding:0px}    
			.control-label h2 { line-height:px }
			.controls a { margin-left:50px; }
			.quickStart  { padding-left:160px; padding-top:35px }  
		</style> 

	</head>
	<body>
	<#include "common/navigator.ftl">
	<div class="container">
		<div class="hero-unit"/>	
			<form class="form-inline" name="quickStart" id="quickStart" action="${req.getContextPath()}/perftest/quickStart" method="POST">
				<div class="quickStart" 	data-original-title="Please input valid URL" 
											data-content="URL should start with http(s)://">
					<input type="text" name="url" id="url" class="span7 url required" placeholder="Type URL..."/> 
					<button id="startTestBtn" class="btn btn-primary"  type="submit">Start Test</button>
				</div>
			</form>
		</div>
		<div class="row">
			<div class="span6">
				<div class="page-header">
	 				 <h2><@spring.message "home.qa.title"/></h2> 
				</div>
				<div class="alert alert-info">
			  	<@spring.message "home.qa.message"/>
			  	</div> 
		   		<div class="well">
			  		<br/>
				  	<#if right_panel_entries?has_content>
					  	<table class="table table-striped">
					  		<#list right_panel_entries as each_right_entry>
					  			<tr>
					  				<td>
					  					<#if each_right_entry.isNew()><span class="label label-info">new</span></#if>
					  					<a href="${each_right_entry.link }" target="_blank">${each_right_entry.title}
					  					</a> 
					  				</td>
					  				<td>${each_right_entry.lastUpdatedDate?string("yyyy-MM-dd")} 
					  				</td>
					  			</tr>
					  		</#list>
				  			<tr>
				  				<td>
				  					<img src="${req.getContextPath()}/img/asksupport.gif"/> 
				  					<a href="http://github.com/nhnopensource/ngrinder/issues/new?labels=question">
				  						<@spring.message "home.button.ask"/>
				  					</a>&nbsp;&nbsp;&nbsp; 
				  					<img src="${req.getContextPath()}/img/bug_icon.gif"/>
				  					<a href="http://github.com/nhnopensource/ngrinder/issues/new?labels=bug">
				  						<@spring.message "home.button.bug"/>
				  					</a>	
				  				</td>
				  				<td><a href="http://github.com/nhnopensource/ngrinder/issues" target="_blank"><i class="icon-share-alt"></i>&nbsp;<@spring.message "home.button.more"/></a></td>
				  			</tr>
				  			</div>
		  				</table>
		   		 	 </#if> 	
  			    </div>
			</div>
			<div class="span6">
				<div class="page-header">
	 				 <h2><@spring.message "home.developerResources.title"/></h2> 
				</div> 
				<div class="alert alert-info">
			  		<@spring.message "home.developerResources.message"/>
			  	</div> 
		   		<div class="well">
			  		<br/>
				  	<#if left_panel_entries?has_content>
					  	<table class="table table-striped">
					  		<#list left_panel_entries as each_left_entry>
					  			<tr>
					  				<td> 
					  					<#if each_left_entry.isNew()><span class="label label-info">new</span></#if>
					  					<a href="${each_left_entry.link }" target="_blank">${each_left_entry.title}</a>
					  				</td>
					  				<td>${each_left_entry.lastUpdatedDate?string("yyyy-MM-dd")}</td>
					  			</tr>
					  		</#list>
				  			<tr>
				  				<td></td>
				  				<td><a href="http://www.cubrid.org/wiki_ngrinder" target="_blank"><i class="icon-share-alt"></i>&nbsp;<@spring.message "home.button.more"/></a></td>
				  			</tr>
				  			</div>
		  				</table>
		   		 	 </#if> 
			  	</div>
			</div>
		</div>
		<script>
			$(document).ready(function(){
		        $("div.quickStart").popover(
		          	{
		          		placement:"bottom",
		           		trigger:"manual",
		           		animation:false
		           	}
		        );
			
		        $("form#quickStart").validate({
		            errorPlacement: function(error, element) {
		            	$("div.quickStart").popover("show");
			        }
			    });
			   	
			    $("#url").change(function() {
			    	var valid = $("form#quickStart").valid();
			    	if (valid == true) {
			    		$("div.quickStart").popover("hide");
			    	}
			    });
		    });
		</script>
		<#include "common/copyright.ftl">
	</div>
	</body>
</html>
