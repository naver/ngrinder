<#setting number_format="computer">
<#import "../common/spring.ftl" as spring/>
<#include "../common/ngrinder_macros.ftl">
<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<form class="form-horizontal form-horizontal-left" id="user_form" name="user_form" method="POST">
<#if !(popover_place??)><#assign popover_place='bottom'/></#if>
<#if !(basePath??)><#assign basePath>user</#assign></#if>
<#if !(allowRoleChange??)><#assign allowRoleChange=false/></#if>
<#if !(allowShareChange??)><#assign allowShareChange=false/></#if>
<#if !(allowPasswordChange??)><#assign allowPasswordChange=false/></#if>
<#if !(showPasswordByDefault??)><#assign showPasswordByDefault=false/><</#if>
<#if !(allowUserIdChange??)><#assign allowUserIdChange=false/></#if>
<#if !(userSecurityEnabled??)><#assign userSecurityEnabled=true/><</#if>
<#if !(newUser??)><#assign newUser=false/></#if>
<#if !(followers??)><#assign followers=[]/></#if>
	<fieldset>

	<@control_group name="userId" label_message_key="user.info.userId">
		<#assign others><#if user?? && user.userId??>readonly</#if></#assign>
		<@input_append class="span4" name="userId" value="${(user.userId)!}" others=others
				data_placement='${popover_place}' message="user.info.userId"/>
		<input type="hidden" id="id" name="id" value="${(user.id)!}"/>
	</@control_group>

	<@control_group name="userName" label_message_key="user.info.name">
		<@input_append class="span4" name="userName" value="${(user.userName)!}"
			data_placement='${popover_place}' message="user.info.name"/>
	</@control_group>

	<#if allowRoleChange>
		<@control_group name="role" label_message_key="user.info.role">
			<select class="span4" name="role" id="role">
				<#list roleSet as role>
					<option value="${role}" <#if user?? && user.role==role>selected="selected"</#if>  >${role.fullName}</option>
				</#list>
			</select>
		</@control_group>
	</#if>

	<@control_group name="email" label_message_key="user.info.email">
		<@input_append class="span4" name="email" value="${(user.email)!}"
			data_placement='${popover_place}' message="user.info.email"/>
	</@control_group>

	<@control_group name="description" label_message_key="common.label.description">
		<textarea cols="30" id="description" name="description"
				  rows="3" title="Description" class="tx_area span4" style="resize: none;">${(user.description)!}</textarea>
	</@control_group>

	<@control_group name="mobilePhone" label_message_key="user.info.phone">
		<@input_append class="span4" name="mobilePhone" value="${(user.mobilePhone)!}"
			data_placement='${popover_place}' message="user.info.phone"/>
	</@control_group>

	<#if allowShareChange>
		<@control_group label_message_key="user.share.title">
			<input type="hidden" id="user_switch_select" name="followersStr" style="width:300px" >
		</@control_group>
	</#if>
	<#if allowPasswordChange>
		<div class="control-group">
			<#if !showPasswordByDefault>
				<div class="accordion-heading">
					<a id="change_password_btn" class="pointer-cursor">
						<@spring.message "user.info.button.changePwd"/>
					</a>
				</div>
			</#if>
			<div id="user_password_section" <#if !showPasswordByDefault>style='display:none'</#if> >
				<div class="accordion-inner" style="padding:9px 0">
					<@control_group name="password" label_message_key="user.info.pwd">
						<@input_append class="span4" name="password" value=""
							type="password" data_placement='${popover_place}'message="user.info.pwd"/>
					</@control_group>

					<@control_group name="confirmPassword" label_message_key="user.info.cpwd">
						<@input_append class="span4" name="confirmPassword" value=""
							type="password" data_placement="${popover_place}" message="user.info.cpwd"/>
					</@control_group>

				</div>
			</div>
		</div>
	</#if>
		<div class="control-group">
			<div class="controls pull-right">
				<a class="btn btn-success" id="save_user_btn">
				<@spring.message "user.info.button.saveUser"/></a>
			</div>
		</div>
	</fieldset>
</form>
<script type="text/javascript">
	//@ sourceURL=/user/info
	$(document).ready(function () {
		$('#user_form').find('input[rel="popover"]').popover({ trigger: 'hover', container: '#user_form' });
		var allowUserIdChange = ${allowUserIdChange?string};
		var userSecurityEnabled = ${userSecurityEnabled?string};
		var allowPasswordChange = ${allowPasswordChange?string};
		$.validator.addMethod("userIdFmt", function (userId, element) {
			if (!allowUserIdChange) {
				 return true;
			}
			var rule = new RegExp(/^[a-zA-Z]{1}[a-zA-Z0-9_\.]{3,20}$/);
			if (!rule.test($.trim(userId))) {
				removeSuccess(element);
				return false;
			}
			return true;
		}, "<@spring.message 'user.info.userId.invalid'/>");

		$.validator.addMethod("userIdExist", function (userId, element) {
			if (!allowUserIdChange) {
				return true;
			}
			if (userId != null && userId.length > 0) {
				var result = false;
				var url = "${basePath}/api/" + userId + "/check_duplication";
				var ajaxObj = new AjaxObj(url);
				ajaxObj.async = false;
				ajaxObj.success = function (res) {
					result = res.success;
					if (!result) {
						removeSuccess(element);
					}
				};
				ajaxObj.call();
				return result;
			}
			return false;
		}, "<@spring.message 'user.info.userId.exist'/>");

		$.validator.addMethod("userPhoneNumber", function (phoneNumber, element) {
			phoneNumber = $.trim(phoneNumber);
			if (phoneNumber == "") {
				return true;
			}
			//noinspection JSValidateTypes
			var rule = new RegExp(/^\+?\d{2,3}-?\d{2,5}(-?\d+)?$/);
			if (!rule.test(phoneNumber)) {
				removeSuccess(element);
				return false;
			}
			return true;
		}, "<@spring.message 'user.info.phone.help'/>");

		//noinspection JSUnusedAssignment
		$("#user_form").validate({
			rules: {
				userId: {
					required: true,
					userIdFmt: true,
					userIdExist: true,
					maxlength: 20
				},
				userName: {
					required: true,
					maxlength: 20
				},
				mobilePhone: {
					required: userSecurityEnabled,
					userPhoneNumber: true
				},
				email: {
					required: userSecurityEnabled,
					email: true
				},

				password: {
					required: allowPasswordChange,
					rangelength: [6, 15]
				},
				confirmPassword: {
 					required: allowPasswordChange,
					equalTo: "#password",
					rangelength: [6, 15]
				}
			},
			messages: {
				user_id: {
					required: "<@spring.message "user.info.warning.userId.required"/>"
				},
				userName: {
					required: "<@spring.message "user.info.name.help"/>"
				},
				email: {
					required: "<@spring.message "user.info.email.help"/>",
					email: "<@spring.message "user.info.email.help"/>"
				},
				password: {
					required: "<@spring.message "user.info.pwd.required"/>"
				},
				confirmPassword: {
					required: "<@spring.message "user.info.cpwd.required"/>",
					equalTo: "<@spring.message "user.info.cpwd.help"/>"
				}
			},
			errorClass: "help-inline",
			errorElement: "span",
			highlight: function (element, errorClass, validClass) {
				$(element).parents('.control-group').addClass('error');
				$(element).parents('.control-group').removeClass('success');
			},
			unhighlight: function (element, errorClass, validClass) {
				$(element).parents('.control-group').removeClass('error');
				$(element).parents('.control-group').addClass('success');
			}
		});

		$("#change_password_btn").click(function () {
			if ($("#user_password_section").is(":hidden")) {
				showPassword();
			} else {
				hidePassword();
			}
		});
		var switchedUsers = [];
		<@list list_items = followers others = "no_message" ; user >
			switchedUsers.push({id:"${user.id}", text:"${user.text}"});
		</@list>
		$("#user_switch_select").select2({
			multiple: true,
			minimumInputLength: 3,
			ajax: {
				url: "${req.getContextPath()}/user/api/search",
				dataType: "json",
				data: function (term, page) {
					return {
						keywords: term,
						pageNumber: page,
						pageSize: 10
					}
				},
				results: function (data) {
					return {results: data};
				}
			},
			formatSelection: function (data) {
				return data.text;
			}
		});

		$("#user_switch_select").select2("data", switchedUsers);

		$("#save_user_btn").click(function () {
			document.forms.user_form.action = "${req.getContextPath()}/${basePath}/save";
			if ($("#user_form").valid()) {
				<#if newUser>
					showSuccessMsg("new user " + $("#user_id").val() + " is signing up.");
					setTimeout(function() {
						document.forms.user_form.submit();
					}, 1000);
				<#else>
					document.forms.user_form.submit();
				</#if>
			}
		});
	});

	function showPassword() {
		$("#user_password_section").show("slow");
	}

	function hidePassword() {
		$("#user_password_section").slideUp();
		$("#password").val("");
		$("#confirm_password").val("");
	}
	function removeSuccess(elem) {
		var $elem = $(elem).parents(".control-group");
		$elem.removeClass("success");
	}
</script>
