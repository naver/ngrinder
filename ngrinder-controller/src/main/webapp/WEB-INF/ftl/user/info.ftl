<#import "../common/spring.ftl" as spring/>
<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<form class="form-horizontal form-horizontal-left" id="user_form" method="POST">
	<#if !(popover_place??)>
		<#assign popover_place='bottom'/>
	</#if>
	<fieldset>
		<div class="control-group">
			<label class="control-label" for="user_id"><@spring.message "user.info.form.userId"/></label>
			<div class="controls">
				<#assign userIdMsg>
					<@spring.message "user.info.warning.userId.intro"/> <@spring.message "common.form.rule.userId"/> 
				</#assign>
				<input type="text" class="span4"  
					name="userId" value="${(user.userId)!}"
					id="user_id"
				    rel="popover" 
					data-placement='${popover_place}'
					data-html="true"
					data-content="${userIdMsg?html}"
					title='<@spring.message "user.info.form.userId"/>'
					<#if user?? && user.userId??>readonly</#if> />
				<input type="hidden" id="id" name="id" value="${(user.id)!}"/>
			</div>
		</div>
		
		<div class="control-group">
			<label class="control-label" for="user_name"><@spring.message "user.option.name"/></label>
			<div class="controls">
				<input type="text" class="span4" 
					name="userName" value="${(user.userName)!}"
					id="user_name" 
					rel="popover"
					data-placement="${popover_place}" 
					data-content='<@spring.message "user.info.warning.userName"/>'
					data-placement='bottom'
					title='<@spring.message "user.option.name"/>'/>
			</div>
		</div>

		<#if !(action?has_content)>
		<div class="control-group">
			<label class="control-label" for="role"><@spring.message "user.option.role"/></label>
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
			<label class="control-label" for="email"><@spring.message "user.info.form.email"/></label>
			<div class="controls">
				<input type="text" class="span4" id="email" maxlength="30"
					name="email" value="${(user.email)!}"
					rel="popover" 
					data-content='<@spring.message "user.info.warning.email.required"/>'
					data-placement='${popover_place}'
					title='<@spring.message "user.info.form.email"/>'/>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label" for="description"><@spring.message "common.label.description"/></label>
			<div class="controls">
				<textarea cols="30" id="description" name="description"
					rows="3" title="Description" class="tx_area span4" 
					style="resize: none;">${(user.description)!}</textarea>
			</div>
		</div>

		<div class="control-group" >
			<label class="control-label" for="mobile_phone"><@spring.message "user.info.form.phone"/></label>
			<div class="controls">
				<input type="text" class="span4"   
					name="mobilePhone" value="${(user.mobilePhone)!}"
					id="mobile_phone" rel="popover"
					data-content='<@spring.message "common.form.rule.phoneNumber"/>'
					data-placement='${popover_place}'
					title="<@spring.message "user.info.form.phone"/>">
			</div>
		</div>
		<#if user?exists>
		<div class="control-group" >
			<label class="control-label" for=""><@spring.message "user.share.title"/></label>
			<div class="controls">
				<select id="user_switch_select" name="followersStr" style="width:300px" multiple>
					<#include "switch_options.ftl">
				</select>
			</div>
		</div>
		</#if>
		<#if !(demo!false)>
  		<div class="control-group">
  			<#if !(isSelfRegistration?? && isSelfRegistration)>
				<div class="accordion-heading"> 
	               	<a id="change_password_btn" class="pointer-cursor"> 
	                 	<@spring.message "user.info.form.button.changePwd"/>
	               	</a> 
             	</div> 
			</#if>
			
             <div id="user_password_section" style='display:none'> 
	            <div class="accordion-inner" style="padding:9px 0" > 
	           		<div class="control-group" >
						<label class="control-label" for="password"><@spring.message "user.info.form.pwd"/></label>
						<div class="controls">
							<input type="password" class="span4"  
								name="password" value="${(user.psw)!}"
								id="password" rel="popover"
								data-content='<@spring.message "user.info.warning.pwd.rangeLength"/>'
								data-placement='${popover_place}'
								title='<@spring.message "user.info.form.pwd"/>'>
						</div>
					</div>
						
					<div class="control-group" >
						<label class="control-label" for="confirm_password"><@spring.message "user.info.form.cpwd"/></label>
						<div class="controls">
							<input type="password" class="span4" 
								name="cpwd" value="${(user.psw)!}"
								id="confirm_password" rel="popover"
								data-content='<@spring.message "user.info.warning.cpwd.equalTo"/>'
								data-placement='${popover_place}'
								title='<@spring.message "user.info.form.cpwd"/>'>
						</div>
					</div>
	             </div> 
	 	 	 </div>
		</div>
		</#if>
		<div class="control-group">
			<label class="control-label pull-right">
				<a class="btn btn-success" id="update_or_create_user_btn"><@spring.message "user.info.form.button.saveUser"/></a>
			</label>
		</div>
	</fieldset>
</form>
<script type="text/javascript">	
	$(document).ready(function(){
		$('#user_form input[rel="popover"]').popover( { trigger: 'hover', container:'#user_form' } );
		<#if !(user?has_content)>

			$.validator.addMethod("userIdFmt", function(userId, element ) {
				var patrn = /^[a-zA-Z]{1}[a-zA-Z0-9_\.]{3,20}$/;
				var rule = new RegExp(patrn);
				if (!rule.test($.trim(userId))) {
					removeSuccess(element);
					return false;
				}
				return true;
			}, "<@spring.message 'user.info.warning.userId.invalid'/>" );
	
			$.validator.addMethod("userIdExist", function(userId, element) {
				if(userId != null && userId.length > 0){
					<#if isSelfRegistration?? && isSelfRegistration>
						url = "${req.getContextPath()}/api/registration/" + userId + "/check_duplication";
					<#else>
						url = "${req.getContextPath()}/api/user/" + userId + "/check_duplication";
					</#if>
					var result ;
					$.ajax({
						  url: url,
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
	    	    
	    $.validator.addMethod("userPhoneNumber", function(mobilePhone, element) {
			var patrn = /^\+?\d{2,3}-?\d{2,5}(-?\d+)?$/;
			var rule = new RegExp(patrn);
			if (!rule.test($.trim(mobilePhone))) {
				removeSuccess(element);
				return false;
			}
			return true;
		}, "<@spring.message 'user.info.warning.phone.intro'/>" );
		
	    $("#user_form").validate({
	    	rules: {
	    		userId: {
	    			required: true,
	    			<#if !(user?has_content)>
	    			userIdFmt: true,
	    			userIdExist: true,
	    			</#if>
	    			maxlength: 20
	    		},
	    		userName: {
	    			required: true,
	    			maxlength: 20
	    		},
	    		<#if userSecurity?? && userSecurity==true>
	    		mobilePhone: {
	    			userPhoneNumber: true
	    		},
	    		email: {
	    			required: true,
	    			email: true
	    		},
	    		</#if>
	    		password: {
	    			<#if !(user?has_content)>
	    			required: true,
	    			</#if>
	    			rangelength: [6,15]
	    		},
	    		cpwd: {
	    			<#if !(user?has_content)>
	    			required: true,
	    			</#if>
	    			rangelength: [6,15]
	    		}
	    	}, 
	        messages:{
	        	user_id: {
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
		
	    <#if !(user?has_content)>
	    	showPassword();
	    <#else>
	    	hidePassword();
	    </#if>
	    
	    $("#change_password_btn").click(function() {
	    	if ($("#user_password_section").is(":hidden")) {
				showPassword();
			} else {
				hidePassword();
			}
	    });
		
	    var switchedUsers = [];
	    <#if followers?has_content>
	    	<#list followers as user>
	    		switchedUsers.push("${user.userId}");
	    	</#list>
	    </#if>
	    $("#user_switch_select").val(switchedUsers).select2();
	    
	    $("#update_or_create_user_btn").click(function() {
			<#if isSelfRegistration?? && isSelfRegistration>
				url = "${req.getContextPath()}/registration/save";
			<#else>
				url = "${req.getContextPath()}/user/save";
			</#if>	
			document.forms.user_form.action = url;
			if($("#user_form").valid())
				document.forms.user_form.submit();
		});
	    
	});
	
	function showPassword() {
		$("#user_password_section").show("slow");
		$("#password").addClass("required");
		$("#confirm_password").addClass("required");
		$("#confirm_password").attr("equalTo","#password");
	}
	
	function hidePassword() {
		$("#user_password_section").slideUp();
		$("#password").removeClass("required");
		$("#confirm_password").removeClass("required");
		$("#confirm_password").attr("equalTo","");
		$("#password").val("");
		$("#confirm_password").val("");
	}
	function removeSuccess(elem) {
		var $elem = $(elem).parents(".control-group");
		$elem.removeClass("success");	
	}
</script>
