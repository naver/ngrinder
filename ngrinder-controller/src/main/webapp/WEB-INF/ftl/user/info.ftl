<#import "../common/spring.ftl" as spring/>
<#include "../common/ngrinder_macros.ftl">
<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<form class="form-horizontal form-horizontal-left" id="user_form" method="POST">
	<#if !(popover_place??)>
		<#assign popover_place='bottom'/>
	</#if>
	<fieldset>

		<@control_group name="userId" label_message_key="user.info.form.userId">
			<#assign userIdMsg>
				<@spring.message "user.info.warning.userId.intro"/> <@spring.message "common.form.rule.userId"/>
			</#assign>

			<#assign others>
				<#if user?? && user.userId??>readonly</#if>
			</#assign>

			<@input_append name = "userId" value = "${(user.userId)!}"
			class="span4" data_content=userIdMsg
			data_placement='${popover_place}'
			message="user.info.form.userId"
			others=others />
			<input type="hidden" id="id" name="id" value="${(user.id)!}"/>
		</@control_group>


		<@control_group name="userName" label_message_key="user.option.name">
			<@input_append name="userName" value="${(user.userName)!}"
				class="span4" data_placement='${popover_place}'
				message="user.option.name"/>
		</@control_group>

		<#if !(action?has_content)>
			<@control_group name="role" label_message_key="user.option.role">
				<select class="span4" name="role" id="role">
					<#list roleSet as role>
						<option value="${role}" <#if user?? &&	user.role==role>selected="selected"</#if>  >${role.fullName}</option>
					</#list>
				</select>
			</@control_group>
		</#if>

		<@control_group name="email" label_message_key="user.info.form.email">
			<@input_append name="email" value="${(user.email)!}"
				class="span4" data_placement='${popover_place}'
				message="user.info.form.email"/>
		</@control_group>

		<@control_group name="description" label_message_key="common.label.description">
			<textarea cols="30" id="description" name="description"
				rows="3" title="Description" class="tx_area span4"
				style="resize: none;">${(user.description)!}</textarea>
		</@control_group>

		<@control_group name="mobilePhone" label_message_key="user.info.form.phone">
			<@input_append name="mobilePhone" value="${(user.mobilePhone)!}"
				class="span4" data_placement='${popover_place}'
				message="user.info.form.phone"/>
		</@control_group>

		<#if user?exists>

			<@control_group label_message_key="user.share.title">
				<select id="user_switch_select" name="followersStr" style="width:300px" multiple>
					<#include "switch_options.ftl">
				</select>
			</@control_group>
		</#if>

		<#if !(demo!false)>
  		<div class="control-group">
  			<#if (selfRegistration)!false >
				<div class="accordion-heading"> 
					<a id="change_password_btn" class="pointer-cursor">
						<@spring.message "user.info.form.button.changePwd"/>
					</a>
             	</div> 
			</#if>
			
			<div id="user_password_section" style='display:none'>
				<div class="accordion-inner" style="padding:9px 0" >

					<@control_group name="password" label_message_key="user.info.form.pwd">
						<@input_append name="password" value="${(user.psw)!}"
							class="span4" type="password"
							data_placement='${popover_place}'
							message="user.info.form.pwd"/>
					</@control_group>

					<@control_group name="confirmPassword" label_message_key="user.info.form.cpwd">
						<@input_append name="confirmPassword" value="${(user.psw)!}"
							class="span4" type="password"
							data_placement='${popover_place}'
							message="user.info.form.cpwd"/>
					</@control_group>

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
					var result = false;
					var url = "/<#if (selfRegistration)!false>registration<#else>user</#if>/api/" + userId + "/check_duplication";
					var ajaxObj = new AjaxObj(url);
					ajaxObj.async = false;
					ajaxObj.success = function(res) {
                        result = res.success;
                        if (!result) {
                            removeSuccess(element);
                        }
					};
                    ajaxObj.call();
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
				confirmPassword: {
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
	            	required: "<@spring.message "user.option.name.help"/>"
				},
				email: {
					required:"<@spring.message "user.info.form.email.help"/>",
					email:"<@spring.message "user.info.warning.email.rule"/>"
				},
				password: {
					required:"<@spring.message "user.info.warning.pwd.required"/>"
				},
				confirmPassword: {
					required:"<@spring.message "user.info.warning.cpwd.required"/>",
					equalTo:"<@spring.message "user.info.form.cpwd.help"/>"
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
		<@list list_items = followers others = "no_message"  ; user >
			switchedUsers.push("${user.userId}");
		</@list>
		$("#user_switch_select").val(switchedUsers).select2();
	    
		$("#update_or_create_user_btn").click(function() {
			<#if (selfRegistration)!false>
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
