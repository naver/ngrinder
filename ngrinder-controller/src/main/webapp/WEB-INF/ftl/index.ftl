<!DOCTYPE html>
<html>
	<head>
		<title>nGrinder Home</title>
		<#include "common/common.ftl">
		<style>
			.hero-unit p { margin-left:15px }
			.control-label h2 { line-height:18px }
			.controls a { margin-left:50px; }
		</style>
	</head>
	<body>
	<#include "common/navigator.ftl">
	<div class="container">
		<img src="${req.getContextPath()}/img/bg_main_banner.png"  style="margin-top:-20px;margin-bottom:10px"/>
		<div class="row">
			<div class="span6">
				<div class="page-header">
	 				 <h2>Q&A</h2> 
				</div>
				<div class="alert alert-info">
			  	You can ask a question and track the progress.
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
				  					<a href="https://github.com/nhnopensource/ngrinder/issues/new">
				  						Ask a question
				  					</a>  &nbsp;&nbsp;&nbsp; 
				  					<img src="${req.getContextPath()}/img/bug_icon.gif"/>
				  					<a href="https://github.com/nhnopensource/ngrinder/issues/new">
				  						Report a bug
				  					</a>	
				  				</td>
				  				<td><a href="https://github.com/nhnopensource/ngrinder/issues" target="_blank"><i class="icon-share-alt"></i>&nbsp;more...</a></td>
				  			</tr>
				  			</div>
		  				</table>
		   		 	 </#if> 	
  			    </div>
			</div>
			<div class="span6">
				<div class="page-header">
	 				 <h2>Developer Resources</h2> 
				</div> 
				<div class="alert alert-info">
			  	You can find ngrinder related documents in the followings.
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
				  				<td><a href="http://www.cubrid.org/wiki_ngrinder" target="_blank"><i class="icon-share-alt"></i>&nbsp;more...</a></td>
				  			</tr>
				  			</div>
		  				</table>
		   		 	 </#if> 
			  	</div>
			</div>
		</div>
		<#include "common/copyright.ftl">
	</div>
	</body>
</html>
