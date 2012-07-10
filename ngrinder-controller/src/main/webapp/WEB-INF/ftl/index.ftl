<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title>nGrinder Script Editor</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta name="description" content="nGrinder Test Result Detail">
		<meta name="author" content="AlexQin">

		<link rel="shortcut icon" href="favicon.ico"/>
		<link href="${req.getContextPath()}/css/bootstrap.min.css" rel="stylesheet">
		<link href="${req.getContextPath()}/css/bootstrap-responsive.min.css" rel="stylesheet">
		<style>
			body {
				padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
			}
		</style>
		
		<input type="hidden" id="contextPath" value="${req.getContextPath()}">
		<#setting number_format="computer">
	</head>

	<body>
	<#include "common/navigator.ftl">
	<div class="container">
			<div class="hero-unit">
					<div class="row">
					  <div class="span2"><h3>Quick Start </h3></div>
					  <div class="span6"><input type="text" class="input-xlarge"  id="searchTest_url" ></div>
					   
					  <div class="span2" offset="span4"><button type="button" class="btn btn-success">Create Test</button></div>
					</div>
			       
			        <p> Type the URL you want to run performance test !</p>
			</div>

			<div class="row">
				<div class="span7">
				  <h2>Heading</h2>
				   <p>Donec id elit non mi porta gravida at eget metus. Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus. Etiam porta sem malesuada magna mollis euismod. Donec sed odio dui. </p>
				  <p><a class="btn" href="#">View details &raquo;</a></p>
				</div>
				<div class="span5">
				  <h2>Heading</h2>
				  <p>Donec sed odio dui. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Vestibulum id ligula porta felis euismod semper. Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.</p>
				  <p><a class="btn" href="#">View details &raquo;</a></p>
				</div>
			</div>

	</div>

	<script src="${req.getContextPath()}/js/jquery-1.7.2.min.js"></script>
	<script src="${req.getContextPath()}/js/bootstrap.min.js"></script>
	<script src="${req.getContextPath()}/plugins/editarea/edit_area.js"></script>
	
	</body>
</html>