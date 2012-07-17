<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<title>nGrinder User Detail</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="nGrinder User Detail">
	<meta name="author" content="AlexQin">
	
	<link rel="shortcut icon" href="favicon.ico" />
	<link href="${req.getContextPath()}/css/bootstrap.min.css" rel="stylesheet">
	<link href="${req.getContextPath()}/css/ngrinder.css" rel="stylesheet">
	<script src="${req.getContextPath()}/js/jquery-1.7.2.min.js"></script>
	<script src="${req.getContextPath()}/js/bootstrap.min.js"></script>
	<script src="${req.getContextPath()}/js/utils.js"></script>
</head>

<body>
	<#include "../common/navigator.ftl">

	<div class="container">
		<div class="row">
			<div class="span2">
				<a class="btn" href="${req.getContextPath()}/user/detail"
					id="createBtn" data-toggle="modal"> <i class="icon-user"></i>
					Create User
				</a>
				<#include "userTree.ftl">
			</div>

			<div class="span10">
				<#include "userInfo.ftl">
			</div>
		</div>
		<#include "../common/copyright.ftl">
	</div>
	
	<script type="text/javascript">
		$(document).ready(function(){
			$("#search_user").on('click', function() {
				document.location.href = "${req.getContextPath()}/user/list?keywords=" + $("#searchText").val() ;
			});
		});
	</script>
</body>
</html>