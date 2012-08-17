<#import "../common/spring.ftl" as spring/>
<script type="text/javascript">
	$(document).ready(function(){
		<#if !(user?has_content)>
		$(".collapse").collapse();
		$("#user_pw_head").attr("href","");
		
		$("#userId").blur(function(){
			var userId = $("#userId").val();
			
			var patrn = "^[a-zA-Z]{1}([a-zA-Z0-9]|[_]|[-]|[.]){0,19}$";
				var rule = new RegExp(patrn);
				if (!rule.test($.trim($("#userId").val()))) {
						$("userId").parents('.control-group').addClass("error");
						$("#userIdError_span_id").html("<@spring.message "user.info.warning.userIdError"/>");
						$("#userIdError_span_id").show();
					return;
			}
			
			if(userId != null && userId.length > 0){
				$.ajax({
					  url: "${req.getContextPath()}/user/checkUserId?userId="+userId,
					  async: false,
					  cache: false,
					  type: "GET",
					  dataType:'json',
					  success: function(res) {
					  	  if(!res.success) {
						  	$("userId").parents('.control-group').addClass("error");
						  	$("#userIdError_span_id").html("<@spring.message "user.info.warning.userIdExist"/>");
						  	$("#userIdError_span_id").show();
					  	  }else{
					  	  	 $("userId").parents('.control-group').addClass("success");
					  	  	 $("#userIdError_span_id").html("");
					  	  	 $("#userIdError_span_id").hide();
					  	  }
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
	            userName:"<@spring.message "user.info.warning.userName"/>",
	            email:{
	                required:"<@spring.message "user.info.warning.email.required"/>",
	                email:"<@spring.message "user.info.warning.email.rule"/>"
	            },
	          
	            password:{
	                required:"<@spring.message "user.info.warning.pwd.required"/>",
	                minlength:"<@spring.message "user.info.warning.pwd.minLength"/>"
	            },
	            cpwd:{
	                required:"<@spring.message "user.info.warning.cpwd.required"/>",
	                equalTo:"<@spring.message "user.info.warning.cpwd.equalTo"/>"
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
	class="form-horizontal form-horizontal-left" id="registerUserForm" style="margin-left:30px" method="POST">
	<fieldset>
		<div class="control-group">
			<label class="control-label"><@spring.message "user.info.form.userId"/></label>
			<div class="controls">
				<input type="text" class="span4" id="userId" name="userId"
				    rel="popover" value="${(user.userId)!}"
					data-content="<@spring.message "user.info.warning.userId.intro"/>"
					data-original-title="<@spring.message "user.info.form.userId"/>"
					<#if user?? && user.userId??>disabled</#if> >
				<span id="userIdError_span_id" class="help-inline"> </span>
				<input type="hidden" id="id" name="id" value="${(user.id)!}">
			</div>
		</div>
		
		<div class="control-group">
			<label class="control-label"><@spring.message "user.option.name"/></label>
			<div class="controls">
				<input type="text" class="span4" id="userName"
					name="userName" rel="popover" value="${(user.userName)!}"
					data-content="<@spring.message "user.info.warning.userName"/>"
					data-original-title="<@spring.message "user.option.name"/>">
			</div>
		</div>

		<#if !(action?has_content)>
		<div class="control-group">
			<label class="control-label"><@spring.message "user.option.role"/></label>
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
			<label class="control-label"><@spring.message "user.info.form.email"/></label>
			<div class="controls">
				<input type="text" class="span4" id="email"
					name="email" rel="popover" value="${(user.email)!}"
					data-content="<@spring.message "user.info.warning.email.required"/>"
					data-original-title="<@spring.message "user.info.form.email"/>">
			</div>
		</div>

		<div class="control-group">
			<label class="control-label"><@spring.message "common.label.description"/></label>
			<div class="controls">
				<textarea cols="30" id="description" name="description"
					rows="3" title="Description" class="tx_area span4" rel="popover"
					style="resize: none;">${(user.description)!}</textarea>
			</div>
		</div>

		<div class="control-group" >
			<label class="control-label"><@spring.message "user.info.form.phone"/></label>
			<div class="controls">
				<input type="text" class="span4" id="mobilePhone"
					name="mobilePhone" rel="popover"
					value="${(user.mobilePhone)!}"
					data-content="<@spring.message "user.info.warning.phone.intro"/>"
					data-original-title="<@spring.message "user.info.form.phone"/>">
			</div>
		</div>
		
  		<div class="control-group">
              <div class="accordion-heading"> 
                <a class="accordion-toggle" data-toggle="collapse" href="#user_password_head" id="user_pw_head" style="padding: 8px 0"> 
                  <@spring.message "user.info.form.button.changePwd"/>
                </a> 
              </div> 
              
              <div id="user_password_head" class="accordion-body collapse"> 
	              <div class="accordion-inner" style="padding:9px 0"> 
            			<div class="control-group" >
								<label class="control-label"><@spring.message "user.info.form.pwd"/></label>
								<div class="controls">
									<input type="password" class="span4" id="password"  minlength="4"
										name="password" rel="popover" value="${(user.psw)!}"
										data-content="<@spring.message "user.info.warning.pwd.minLength"/>"
										data-original-title="<@spring.message "user.info.form.pwd"/>">
								</div>
						</div>
						
						<div class="control-group" >
								<label class="control-label"><@spring.message "user.info.form.cpwd"/></label>
								<div class="controls">
									<input type="password" class="span4" id="cpwd" 
										name="cpwd" rel="popover" value="${(user.psw)!}"
										data-content="<@spring.message "user.info.warning.cpwd.equalTo"/>"
										data-original-title="<@spring.message "user.info.form.cpwd"/>">
								</div>
						</div>
	              </div> 
		 	  </div>
		</div>
		<div class="control-group">
			<label class="control-label pull-right">
				<button type="submit" class="btn btn-success" rel="tooltip"><@spring.message "user.info.form.button.saveUser"/></button>
			</label>
		</div>
	</fieldset>
</form>










