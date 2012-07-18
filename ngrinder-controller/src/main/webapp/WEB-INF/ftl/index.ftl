<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title>nGrinder Home</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta name="description" content="nGrinder Home">
		<meta name="author" content="AlexQin">

		<link rel="shortcut icon" href="favicon.ico"/>
		<link href="${req.getContextPath()}/css/bootstrap.min.css" rel="stylesheet">
		<link href="${req.getContextPath()}/css/ngrinder.css" rel="stylesheet">
		<script src="${req.getContextPath()}/js/jquery-1.7.2.min.js"></script>
		<script src="${req.getContextPath()}/js/bootstrap.min.js"></script>
		<script src="${req.getContextPath()}/js/utils.js"></script>
		<style>
			.hero-unit p { margin-left:15px }
			.control-label h2 { line-height:18px }
			.controls a { margin-left:50px; }
		</style>
		
		<input type="hidden" id="contextPath" value="${req.getContextPath()}">
		<#setting number_format="computer">
	</head>

	<body>
	<#include "common/navigator.ftl">
	<div class="container">
		<div class="hero-unit">
		    <div class="form-horizontal">
				<fieldset>
					<div class="control-group">
						<label for="searchTest_url" class="control-label"><h2>Quick Start</h2></label>
						<div class="controls">
						  <input type="text" class="span6"  id="searchTest_url">
						  <!--<span class="help-inline"></span>-->
						  <a class="btn btn-success">Create Test</a>
						</div>
					</div>
				</fieldset>
			</div> 
		    <p>Type the URL you want to run performance test !</p>
		</div>

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