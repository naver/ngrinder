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
			   <h3>Announcements</h3> 
			   <p>Donec id elit non mi porta gravida at eget metus. Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus. Etiam porta sem malesuada magna mollis euismod. Donec sed odio dui. </p>
			  <p><a class="btn" href="#">View details &raquo;</a></p>
			</div>
			<div class="span6">
			  <h3>Developer Resources</h3>
			  <#if ngrinder_wiki_rss_list?has_content>
			  	<ul>
			  		<#list ngrinder_wiki_rss_list as rss_entry>
			  			<li><a href="${rss_entry.link }" target="_blank">${rss_entry.title}</a></li>
			  		</#list>
			  	</ul>
			  </#if>
			</div>
		</div>
		<#include "common/copyright.ftl">
	</div>
	</body>
</html>
