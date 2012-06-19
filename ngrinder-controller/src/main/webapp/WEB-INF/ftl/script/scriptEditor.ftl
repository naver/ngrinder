<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title>nGrinder Script Editor</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta name="description" content="nGrinder Test Result Detail">
		<meta name="author" content="AlexQin">

		<link rel="shortcut icon" href="favicon.ico"/>
		<link href="/css/bootstrap.min.css" rel="stylesheet">
		<style>
			body {
				padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
			}
		</style>
		<link href="/css/bootstrap-responsive.css" rel="stylesheet">
		
		<!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
		<!--[if lt IE 9]>
		  <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
		<![endif]-->
	</head>

	<body>
    <div class="navbar navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container">
          <a class="brand" href="#" style="padding:0 20px"><img src="/img/logo_ngrinder_a_header.png" alt="nGrinder"></img></a>
          <div class="nav-collapse">
            <ul class="nav">
              <li class="active"><a href="#">Monitoring</a></li>
              <li><a href="#about">Load Test</a></li>
              <li><a href="#contact">Script</a></li>
              <li><a href="#contact">Alert</a></li>
            </ul>
			<ul class="nav pull-right">
				<li><a href="#">Login</a></li>
				<li class="divider-vertical"></li>
				<li class="dropdown"><a href="#">Help</a></li>
			</ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
    </div>
	<div class="container">
		<div class="row">
			<div class="span10 offset1">
				<div class="tabbable">
					<ul class="nav nav-tabs">
					  <li>
						<a href="#">Monitoring</a>
					  </li>
					  <li><a href="#">Load Test</a></li>
					  <li class="active"><a href="#scriptList">Script</a></li>
					  <li><a href="#">Alert</a></li>
					</ul>
					<div class="tab-content">
						<div class="tab-pane active" id="scriptList">
							<div class="well form-inline" style="padding:5px;margin:5px 0">
								<label class="label" for="scriptNameInput">
									Script Name
								</label>
								<input type="text" id="scriptNameInput">
								<div class="pull-right">
								<label class="label" for="historySelect">
									History
								</label>
								<select id="historySelect">
									<option value="">Select History</option>
								</select>
								<a class="btn" id="compareBtn">Compare</a>
								</div>
							</div>
							<table style="border:none;width:100%">
								<tr>
									<td>
										<div id="script_1" style="width:100%">
											<textarea id="display_content" name="scriptContent" style="height:550px;width:100%;">test</textarea>
										</div>
									</td>
									<td>
										<div id="script_2" style="width:100%">
											<textarea id="display_content_2" name="scriptContent2" style="height:550px;width:100%;">test2</textarea>
										</div>
									</td>
							</table>
							
							<div class="well form-inline" style="padding:5px;margin:5px 0">
								<label class="label" for="tagsInput">
									Tags
								</label>
								<input type="text" id="tagsInput">&nbsp;&nbsp;
								<label class="label" for="descInput">
									Description
								</label>
								<input type="text" id="descInput" class="span6" style="width:600px">
							</div>
							<a class="btn">Save</a>
							<a class="btn">Validate Script</a>
							<span class="help-inline">Validation message goes here........</span>
							<div class="alert alert-info" style="margin-top:5px;">
								Or "Validation message goes here........"
							</div>
							<div class="alert alert-info fade in">
								<button class="close" data-dismiss="alert">&times;</button>
								<strong>Or</strong> "Validation message goes here........" (remove "in" can hide alert.)
							</div>
						</div>
					</div>
				</div>				
			</div>
		</div>
	</div>

	<script src="/js/jquery-1.7.2.min.js"></script>
	<script src="/js/bootstrap.min.js"></script>
	<script src="/plugins/editarea/edit_area.js"></script>
	<script>
		$(document).ready(function() {
			editAreaLoader.baseURL = "editarea/";
			
			editAreaLoader.init({
				id: "display_content"
				,is_editable: true
				,start_highlight: true
				,allow_resize: true
				,allow_toggle: false
				,language: "en"
				,syntax: "python" 
				,replace_tab_by_spaces: 4
				,font_size: "10"
				,font_family: "verdana, monospace"
				,EA_load_callback: "listenEditArea"
			});
			
			editAreaLoader.init({
				id: "display_content_2"
				,is_editable: true
				,start_highlight: true
				,allow_resize: true
				,allow_toggle: false
				,language: "en"
				,syntax: "python" 
				,replace_tab_by_spaces: 4
				,font_size: "10"
				,font_family: "verdana, monospace"
			});
			
			$("#compareBtn").on('click', function() {
				var $div = $("#script_2");
				if ($div.hasClass("hidden")) {
					$div.removeClass("hidden");
				} else {
					$div.addClass("hidden");
				}
			});
		});
		
		function listenEditArea() {
			clearInterval(window.interval);
			window.lastContent = editAreaLoader.getValue("display_content");
			//window.interval = setInterval(function(){autoSave()}, 30000);
		}
	</script>
	</body>
</html>