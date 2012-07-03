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
						
						       <div class="row">
								<div class="span10">
										<a class="btn" href="#createScriptModal" id="createBtn" data-toggle="modal">
											<i class="icon-user"></i>
											Create User
										</a>
									</div>	
								</div>
							
								<div class="well form-inline" style="padding:5px;margin:10px 0">
									<!--<legend>introduction</legend>-->
									<input type="text" class="input-medium search-query" placeholder="Keywords" id="searchText" value="${keywords!}">
									<button type="submit" class="btn" id="searchBtn">Search</button>
								</div>
							
								<table class="table table-striped display" id="userTable">
										<thead>
											<tr>
												<th class="center"><input type="checkbox" class="checkbox noClick" value=""></th>
												<th>User Name</th>
												<th>Create Date</th>
												<th>Description</th>
												<th>Role</th>
												<th class="noClick">Edit</th>
												<th class="noClick">Del</th>
											</tr>
										</thead>
										<tbody>
												  <tr>
														<td><input type="checkbox" value=""></td>
														<td class="center"><a href="${Request.getContextPath()}/user/detail"  target="_self">admin</a></td>
														<td>2012-06-27</td>
														<td>This is admin user</td>
														<td>A</td>
														<td><a href="javascript:void(0);"><i class="icon-edit"></i></a></td>
														<td><a href="javascript:void(0);"><i class="icon-remove" ></i></a></td>
													</tr>
												  <tr>
														<td><input type="checkbox" value=""></td>
														<td class="center"><a href="" target="_self">test</a></td>
														<td>2012-06-28</td>
														<td>This is test user</td>
														<td>U</td>
														<td><a href="javascript:void(0);"><i class="icon-edit"></i></a></td>
														<td><a href="javascript:void(0);"><i class="icon-remove" ></i></a></td>
													</tr>
										</tbody>
								 </table>
					
					</div>
				</div>	
		  </div>
	</body>
	
</html>