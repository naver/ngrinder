<#import "../common/spring.ftl" as spring/>
<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<form action="${req.getContextPath()}/user/save"
	class="form-horizontal form-horizontal-left" id="registerUserForm" method="POST">
	<fieldset>
		<div class="control-group">
			<label class="control-label"><@spring.message "user.info.form.userId"/></label>
			<div class="controls">
				<input type="text" class="span4" id="userId" name="userId"
				    rel="popover" value="${(user.userId)!}"
					data-content="<@spring.message "user.info.warning.userId.intro"/> <@spring.message "common.form.rule.userId"/>"
					data-original-title="<@spring.message "user.info.form.userId"/>"
					<#if user?? && user.userId??>readonly="value"</#if> >
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
				<input type="text" class="span4" id="email" maxlength="30"
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
					data-content="<@spring.message "common.form.rule.phoneNumber"/>"
					data-original-title="<@spring.message "user.info.form.phone"/>">
			</div>
		</div>
		<#if user?exists && user.userId == currentUser.userId>
		<@security.authorize ifAnyGranted="U">
		<div class="control-group" >
			<label class="control-label"><@spring.message "user.share.title"/></label>
			<div class="controls">
				<select id="userListSelect" name="followersStr" style="width:300px" multiple>
					<#include "userOptionGroup.ftl">
				</select>
			</div>
		</div>
		</@security.authorize>
		</#if>
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
									<input type="password" class="span4" id="password"
										name="password" rel="popover" value="${(user.psw)!}"
										data-content="<@spring.message "user.info.warning.pwd.rangeLength"/>"
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
				<button type="submit" class="btn btn-success"><@spring.message "user.info.form.button.saveUser"/></button>
			</label>
		</div>
	</fieldset>
</form>
<script type="text/javascript">	
	$(document).ready(function(){
		<#if !(user?has_content)>
		$(".collapse").collapse();
		$("#user_pw_head").attr("href","");
		
		jQuery.validator.addMethod("userIdFmt", function(userId, element ) {
			var patrn = /^[a-zA-Z]{1}[a-zA-Z0-9_]{3,19}$/;
			var rule = new RegExp(patrn);
			if (!rule.test($.trim(userId))) {
				removeSuccess(element);
				return false;
			}
			return true;
		}, "<@spring.message 'user.info.warning.userId.invalid'/>" );

		jQuery.validator.addMethod("userIdExist", function(userId, element) {
			if(userId != null && userId.length > 0){
				var result ;
				$.ajax({
					  url: "${req.getContextPath()}/user/checkUserId?userId="+userId,
					  async: false,
					  cache: false,
					  type: "GET",
					  dataType:'json',
					  success: function(res) {
					  	result = res.success;
  					  }
				});
				if (!result) {
					removeSuccess(element);
				}
				
				return result;
			}
			
			return false;
		}, "<@spring.message 'user.info.warning.userId.exist'/>");
		</#if>
	    	    
	    jQuery.validator.addMethod("userPhoneNumber", function(mobilePhone, element) {
			var patrn = /^\+?\d{2,3}-?\d{2,5}(-?\d+)?$/;
			var rule = new RegExp(patrn);
			if (!rule.test($.trim(mobilePhone))) {
				removeSuccess(element);
				return false;
			}
			return true;
		}, "<@spring.message 'user.info.warning.phone.intro'/>" );
		
	    $("#registerUserForm").validate({
	    	rules: {
	    		userId: {
	    			required: true,
	    			maxlength: 20,
	    			userIdFmt: true,
	    			userIdExist: true
	    		},
	    		userName: {
	    			required: true,
	    			maxlength: 20
	    		},
	    		mobilePhone: {
	    			userPhoneNumber: true
	    		},
	    		email: {
	    			required: true,
	    			email: true
	    		},
	    		password: {
	    			rangelength: [6,15]
	    		},
	    		cpwd: {
	    			rangelength: [6,15]
	    		}
	    	}, 
	        messages:{
	        	userId: {
	        		required: "<@spring.message "user.info.warning.userId.required"/>"
	        	},
	            userName: {
	            	required: "<@spring.message "user.info.warning.userName"/>"
	            },
	            email: {
	                required:"<@spring.message "user.info.warning.email.required"/>",
	                email:"<@spring.message "user.info.warning.email.rule"/>"
	            },
	            password: {
	                required:"<@spring.message "user.info.warning.pwd.required"/>"
	            },
	            cpwd: {
	                required:"<@spring.message "user.info.warning.cpwd.required"/>",
	                equalTo:"<@spring.message "user.info.warning.cpwd.equalTo"/>"
	            }
	        },
	        errorClass: "help-inline",
	        errorElement: "span",
	        highlight:function(element, errorClass, validClass) {
	            $(element).parents('.control-group').addClass('error');
	            $(element).parents('.control-group').removeClass('success');
	        },
	        unhighlight: function(element, errorClass, validClass) {
	            $(element).parents('.control-group').removeClass('error');
	            $(element).parents('.control-group').addClass('success');
	        }
	    });
		
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
	    
	    var switchedUsers = [];
	    <#if followers?has_content>
	    	<#list followers as user>
	    		switchedUsers.push("${user.userId}");
	    	</#list>
	    </#if>
	    $("#userListSelect").val(switchedUsers).select2();
	});
	
	function removeSuccess(elem) {
		var $elem = $(elem).parents(".control-group");
		$elem.removeClass("success");	
	}
</script>