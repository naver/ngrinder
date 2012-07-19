<!DOCTYPE html>
<html>
	<head>
		<title>nGrinder Home</title>
		<#include "../common/common.ftl">
		<style>
			.hero-unit p { margin-left:15px }
			.control-label h2 { line-height:18px }
			.controls a { margin-left:50px; }
		</style>
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