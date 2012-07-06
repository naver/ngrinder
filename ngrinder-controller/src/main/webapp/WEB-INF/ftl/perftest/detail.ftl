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
		
		<style>
			body {
				padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
			}
		</style>
		
		<input type="hidden" id="contextPath" value="${Request.getContextPath()}">
		<#setting number_format="computer">
	</head>

	<body>
    	<#include "navigator.ftl">
		<div class="container">
			<div class="row">
				<div class="span10 offset1">
					<div class="row">
						<div class="span3">
						</div>
						<div class="span7">
						</div>
					</div>
				<!--content-->
				<#include "copyright.ftl">
				</div>
			</div>
		</div>
		<script src="${Request.getContextPath()}/js/jquery-1.7.2.min.js"></script>
		<script src="${Request.getContextPath()}/js/bootstrap.min.js"></script>
		<script src="${Request.getContextPath()}/js/utils.js"></script>
		<script>
			$(document).ready(function() {
				
			});
		</script>
	</body>
</html>