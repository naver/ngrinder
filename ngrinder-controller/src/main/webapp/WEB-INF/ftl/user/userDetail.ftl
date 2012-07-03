<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title>nGrinder Script List</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta name="description" content="nGrinder Test Result Detail">
		<meta name="author" content="AlexQin">
		<link rel="shortcut icon" href="favicon.ico"/>
		<link href="${Request.getContextPath()}/css/bootstrap.min.css" rel="stylesheet">
		<link href="${Request.getContextPath()}/css/bootstrap-responsive.min.css" rel="stylesheet">
		<link href="${Request.getContextPath()}/plugins/datatables/css/demo_table.css" rel="stylesheet">
		<style>
			body {
				padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
			}
			.table th, .table td {text-align: center;}
			table.display thead th {padding: 3px 10px}
			table.display tbody .left {text-align: left}
		</style>
		
	</head>

	<body>
		  <#include "../common/navigator.ftl">
		
		  <div class="container">
				<div class="row">
					<div class="span10 offset1">
								<div class="well form-inline" style="padding:5px;margin:10px 0">
									<!--<legend>introduction</legend>-->
									<input type="text" class="input-medium search-query" placeholder="Keywords" id="searchText" value="${keywords!}">
									<button type="submit" class="btn" id="searchBtn">Search</button>
								</div>
								<div class="row">  
										        <form class="form-horizontal">  
										            <fieldset>  
										                <legend>User Detail</legend>  
										                <div class="control-group">  
										                    <label class="control-label">UserName:</label>  
										                    <div class="controls">  
										                        <input class="span6" type="text" placeholder=".span6">  
										                    </div>  
										                </div>  
										  
										                <div class="control-group">  
										                    <label class="control-label">Description:</label>  
										                    <div class="controls">  
										                        <input class="span6" type="text" placeholder=".span6">  
										                    </div> 
										                </div>  
										  
										                <div class="control-group">  
										                    <label class="control-label">Field three:</label>  
										                    <div class="input-append">  
										                        <input class="span6" type="text" placeholder"www.   ">  
										                        <span class="add-on">.com</span>  
										                    </div>  
										                </div>  
										  
										                <div class="form-actions">  
										                    <button type="submit" class="btn btn-primary">Save</button>  
										                    <button type="reset" class="btn">Reset</button>  
										                </div>  
										                  
										            </fieldset>  
										        </form>  
										    </div>  
																   
					</div>
				</div>	
		  </div>
	</body>
	
</html>