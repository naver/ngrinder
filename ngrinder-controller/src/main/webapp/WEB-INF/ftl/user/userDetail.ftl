<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<title>nGrinder Script List</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="nGrinder Test Result Detail">
	<meta name="author" content="AlexQin">
	<link rel="shortcut icon" href="favicon.ico" />


	<style>
		body {
			padding-top: 60px;
			/* 60px to make the container go all the way to the bottom of the topbar */
		}
		
		.table th,.table td {
			text-align: center;
		}
		
		table.display thead th {
			padding: 3px 10px
		}
		
		table.display tbody .left {
			text-align: left
		}
	</style>
	<script src="${req.getContextPath()}/js/jquery-1.7.2.min.js"></script>
	<link href="${req.getContextPath()}/css/bootstrap.min.css" rel="stylesheet">
	<link href="${req.getContextPath()}/css/bootstrap-responsive.min.css" rel="stylesheet">
	<script src="${req.getContextPath()}/js/bootstrap.min.js"></script>
</head>

<body>
	<#include "../common/navigator.ftl">

	<div class="container">
		<div class="row">
			<div class="span10 offset1">

				<div class="row">
					<div class="span2">
						<a class="btn" href="${req.getContextPath()}/user/detail"
							id="createBtn" data-toggle="modal"> <i class="icon-user"></i>
							Create User
						</a> <#include "userTree.ftl">
					</div>

					<div class="span8">

						<div class="well form-inline" style="padding: 5px;">
							<!--<legend>introduction</legend>-->
							<input type="text" class="input-medium search-query"
								placeholder="Keywords" id="searchText" value="${keywords!}"
								style="width: 350px">
							<button type="submit" class="btn" id="searchBtn">Search</button>
						</div>

						<form action="${req.getContextPath()}/user/save"
							class="form-horizontal" id="registerUserForm" method="POST">
							<fieldset>
								<legend>Registration</legend>
								<div class="control-group">
									<label class="control-label" for="input01">User Id</label>
									<div class="controls">
										<input type="text" class="input-xlarge" id="userId"
											name="userId" rel="popover" value="${(user.userId)!}"
											data-content="Enter User Id."
											data-original-title="User Id"
											<#if user?? && user.userId??>disabled</#if> >
										<input type="hidden" id="id" name="id" value="${(user.id)!}">
									</div>
								</div>
								<div class="control-group">
									<label class="control-label" for="input01">Name</label>
									<div class="controls">
										<input type="text" class="input-xlarge" id="userName"
											name="userName" rel="popover" value="${(user.userName)!}"
											data-content="Enter your first and last name."
											data-original-title="Full Name"> <input type="hidden"
											id="userId" name="userId" value="${(user.userId)!}">
									</div>
								</div>


								<div class="control-group">
									<label class="control-label" for="input01">User Role</label>
									<div class="controls">
										<select name="role.name" id="role.name">
											<option value="USER"<#if user?? &&
												user.role=="USER">selected="selected"</#if> >General</option>
											<option value="ADMIN"<#if user?? &&
												user.role=="ADMIN">selected="selected"</#if> >Administrator</option>
											<option value="SUPER"<#if user?? &&
												user.role=="SUPER">selected="selected"</#if> >Super</option>
										</select>
									</div>
								</div>

								<div class="control-group">
									<label class="control-label" for="input01">Email</label>
									<div class="controls">
										<input type="text" class="input-xlarge" id="email"
											name="email" rel="popover" value="${(user.email)!}"
											data-content="What's your email address?"
											data-original-title="Email">
									</div>
								</div>

								<div class="control-group">
									<label class="control-label" for="input01">Descriptoin</label>
									<div class="controls">
										<textarea cols="30" id="description" name="description"
											rows="5" title="Description" class="tx_area" rel="popover"
											style="resize: none;">${(user.description)!}</textarea>

									</div>
								</div>

								<div class="control-group">
									<label class="control-label" for="input01">Phone number</label>
									<div class="controls">
										<input type="text" class="input-xlarge" id="mobilePhone"
											name="mobilePhone" rel="popover"
											value="${(user.mobilePhone)!}"
											data-content="Enter yourPhone number."
											data-original-title="Phone number">
									</div>
								</div>

								<div class="control-group">
									<label class="control-label" for="input01">Password</label>
									<div class="controls">
										<input type="password" class="input-xlarge" id="password"
											name="password" rel="popover" value="${(user.psw)!}"
											data-content="6 characters or more! Be tricky"
											data-original-title="Password">

									</div>
								</div>
								<div class="control-group">
									<label class="control-label" for="input01">Confirm
										Password</label>
									<div class="controls">
										<input type="password" class="input-xlarge" id="cpwd"
											name="cpwd" rel="popover" value="${(user.psw)!}"
											data-content="Re-enter your password for confirmation."
											data-original-title="Re-Password">

									</div>
								</div>

								<div class="control-group">
									<label class="control-label" for="input01"></label>
									<div class="controls">
										<a href="javascript:save()" class="btn_sub">
											<button type="submit" class="btn btn-success" rel="tooltip"
												title="first tooltip">Save User</button>
										</a>

									</div>

								</div>

							</fieldset>
						</form>
					</div>
				</div>


			</div>
		</div>
	</div>
</body>
<script src="${req.getContextPath()}/js/jquery.validate.js"></script>
<script type="text/javascript">
	$(document).ready(function(){
	    $('#registerUserForm input').hover(function()
	    {
	        $(this).popover('show')
	    });
	    
	    
	    $("#registerUserForm").validate({
	        rules:{
	            userName:"required",
	            email:{
	                required:true,
	                email: true
	            },
	            mobilePhone:{
	                required:false,
	                number: true
	            },
	            password:{
	                required:true,
	                minlength:4
	            },
	            cpwd:{
	                required:true,
	                equalTo: "#password"
	            },
	            gender:"required"
	        },
	        messages:{
	            userName:"Enter your first and last name",
	            email:{
	                required:"Enter your email address",
	                email:"Enter valid email address"
	            },
	            password:{
	                required:"Enter your password",
	                minlength:"Password must be minimum 6 characters"
	            },
	            cpwd:{
	                required:"Enter confirm password",
	                equalTo:"Password and Confirm Password must match"
	            },
	            gender:"Select Gender"
	        },
	        errorClass: "help-inline",
	        errorElement: "span",
	        highlight:function(element, errorClass, validClass) {
	            $(element).parents('.control-group').addClass('error');
	        },
	        unhighlight: function(element, errorClass, validClass) {
	            $(element).parents('.control-group').removeClass('error');
	            $(element).parents('.control-group').addClass('success');
	        }
	    });
	    
	    
	});
</script>
</html>