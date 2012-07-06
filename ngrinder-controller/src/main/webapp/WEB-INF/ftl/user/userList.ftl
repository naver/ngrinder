<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title>nGrinder Script List</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta name="description" content="nGrinder Test Result Detail">
		<meta name="author" content="AlexQin">
		<link rel="shortcut icon" href="favicon.ico"/>
		 <script src="${Request.getContextPath()}/js/jquery-1.7.2.min.js"></script>
		<link href="${Request.getContextPath()}/css/bootstrap.min.css" rel="stylesheet">
		<link href="${Request.getContextPath()}/css/bootstrap-responsive.min.css" rel="stylesheet">
		<link href="${Request.getContextPath()}/plugins/datatables/css/demo_table.css" rel="stylesheet">
		<script src="${Request.getContextPath()}/plugins/tree/jquery.ztree.all-3.2.js"></script>
	    <link rel="stylesheet" href="${Request.getContextPath()}/plugins/tree/zTreeStyle.css" type="text/css">
		<style>
			body {
				padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
			}
			.table th, .table td {text-align: center;}
			table.display thead th {padding: 3px 10px}
			table.display tbody .left {text-align: left}
		</style>
		
		
			<script type="text/javascript">
		
						var setting = {
							view: {
								showLine: true,
								showIcon : false
							},
							data: {
								simpleData: {
									enable: true
								}
							}
						};
		
						var treeData = ${jsonStr}
						
						$(document).ready(function(){
									$.fn.zTree.init($("#treeDemo"), setting, treeData);
						});
			
			</script>
		
	</head>

	<body>
		  <#include "../common/navigator.ftl">
		
		  <div class="container">
				<div class="row">
					<div class="span10 offset1">
					
										<div class="row">
													<div class="span3">
																<a class="btn" href="${Request.getContextPath()}/user/detail" id="createBtn" data-toggle="modal">
																				<i class="icon-user"></i>
																				Create User
																</a>
																<ul id="treeDemo" class="ztree"></ul>		
													</div>
													
													<div class="span7">
														    <div class="row">
																	<div class="span10">
																			
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
																					<#list userList as user>
																					  <tr>
																							<td><input type="checkbox" value=""></td>
																							<td class="center"><a href="${Request.getContextPath()}/user/detail?userId=${user.userId}" >${user.userName}</a></td>
																							<td>2012-06-27</td>
																							<td>${user.description!}</td>
																							<td>${user.role.name}</td>
																							<td><a href="javascript:void(0);"><i class="icon-edit"></i></a></td>
																							<td><a href="javascript:void(0);"><i class="icon-remove" ></i></a></td>
																						</tr>
																						</#list>
																			</tbody>
																	 </table>
																												
															</div>
										</div>
					
					
					</div>
				</div>	
		  </div>
	</body>
	
</html>