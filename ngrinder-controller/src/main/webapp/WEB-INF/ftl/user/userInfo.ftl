<script src="${req.getContextPath()}/js/jquery.validate.js"></script>
<script type="text/javascript">
	$(document).ready(function(){
		<#if !(user?has_content)>
			$(".collapse").collapse();
			$("#user_pw_head").attr("href","");
			
			$("#userName").click(function(){
				var userId = $("#userId").val();
				
				if(userId != null && userId.length > 0){
					$.ajax({
						  url: "${req.getContextPath()}/user/checkUserId?userId="+userId,
						  cache: false,
						  success: function(data){
	  						}
					}); 
				}
						
			});
		</#if>
		
		
		$('.collapse').on('hidden', function () {
  			$("#password").removeClass("required");
  			$("#cpwd").removeClass("required");
  			$("#cpwd").attr("equalTo","");
  			$("#password").val("");
  			$("#cpwd").val("");
		});
		
		$('.collapse').on('shown', function () {
			
  			$("#password").addClass("required");
  			$("#cpwd").addClass("required");
  			$("#cpwd").attr("equalTo","#password");

		});
		
	
		$('#registerUserForm input').hover(function() {
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
	            }
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
	            }
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
<form action="${req.getContextPath()}/user/save"
	class="form-horizontal form-horizontal-2" id="registerUserForm" method="POST">
	<fieldset>
		
		
		<div class="control-group">
			<label class="control-label">User ID</label>
			<div class="controls">
				<input type="text" class="span4" id="userId" name="userId"
				    rel="popover" value="${(user.userId)!}"
					data-content="User Id is a unique identifier and modified is forbidden  !"
					data-original-title="User Id"
					<#if user?? && user.userId??>disabled</#if> >
				<input type="hidden" id="id" name="id" value="${(user.id)!}">
			</div>
		</div>
		
		<div class="control-group">
			<label class="control-label">Name</label>
			<div class="controls">
				<input type="text" class="span4" id="userName"
					name="userName" rel="popover" value="${(user.userName)!}"
					data-content="Enter your first and last name."
					data-original-title="Full Name">
			</div>
		</div>

		<#if !(action?has_content)>
		<div class="control-group">
			<label class="control-label">User Role</label>
			<div class="controls">
				<select class="span4" name="role" id="role">
					<#list roleSet as role>
						<option value="${role}" <#if user?? &&	user.role==role>selected="selected"</#if>  >${role.fullName}</option>
					</#list>
				</select>
			</div>
		</div>
		</#if>

		<div class="control-group">
			<label class="control-label">Email</label>
			<div class="controls">
				<input type="text" class="span4" id="email"
					name="email" rel="popover" value="${(user.email)!}"
					data-content="What's your email address?"
					data-original-title="Email">
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">Description</label>
			<div class="controls">
				<textarea cols="30" id="description" name="description"
					rows="5" title="Description" class="tx_area span4" rel="popover"
					style="resize: none;">${(user.description)!}</textarea>
			</div>
		</div>

		<div class="control-group" >
			<label class="control-label">Phone number</label>
			<div class="controls">
				<input type="text" class="span4" id="mobilePhone"
					name="mobilePhone" rel="popover"
					value="${(user.mobilePhone)!}"
					data-content="Enter your phone number."
					data-original-title="Phone number">
			</div>
		</div>
		
  		<div class="control-group">
		
              <div class="accordion-heading"> 
                <a class="accordion-toggle" data-toggle="collapse" href="#user_password_head" id="user_pw_head"> 
                  User Password
                </a> 
              </div> 
              
              <div id="user_password_head" class="accordion-body collapse"> 
	              <div class="accordion-inner"> 
	            			 
	            			<div class="control-group" >
									<label class="control-label">Password</label>
									<div class="controls">
										<input type="password" class="span4" id="password"  minlength="4"
											name="password" rel="popover" value="${(user.psw)!}"
											data-content="4 characters or more! Be tricky"
											data-original-title="Password">
									</div>
							</div>
							
							<div class="control-group" >
									<label class="control-label">Confirm Password</label>
									<div class="controls">
										<input type="password" class="span4" id="cpwd" 
											name="cpwd" rel="popover" value="${(user.psw)!}"
											data-content="Re-enter your password for confirmation."
											data-original-title="Re-Password">
									</div>
							</div>
	            			 
	              </div> 
		 	  </div>
		

		<div class="control-group">
			<label class="control-label">
				<button type="submit" class="btn btn-success" rel="tooltip">Save User</button>
			</label>
		</div>
	</fieldset>
</form>










