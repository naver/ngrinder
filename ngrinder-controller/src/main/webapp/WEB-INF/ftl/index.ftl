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
			  <h2>Heading</h2>
			   <p>Donec id elit non mi porta gravida at eget metus. Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus. Etiam porta sem malesuada magna mollis euismod. Donec sed odio dui. </p>
			  <p><a class="btn" href="#">View details &raquo;</a></p>
			</div>
			<div class="span6">
			  <h2>Heading</h2>
			  <p>Donec sed odio dui. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Vestibulum id ligula porta felis euismod semper. Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.</p>
			  <p><a class="btn" href="#">View details &raquo;</a></p>
			</div>
		</div>
		<#include "common/copyright.ftl">
	</div>
	</body>
</html>
