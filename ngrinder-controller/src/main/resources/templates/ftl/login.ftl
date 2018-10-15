<!DOCTYPE html>
<!--suppress HtmlUnknownTarget -->
<#setting locale="en">
<html lang="en">
<head>
	<title>nGrinder Login</title>
<#include "common/common.ftl">
<#include "common/select2.ftl">
	<script src="${req.getContextPath()}/js/detect_timezone.js?${nGrinderVersion}"></script>
	<script type="text/javascript"
			src="${req.getContextPath()}/js/jquery.placeholder.js?${nGrinderVersion}"></script>
	<style>
		body {
			overflow-y: hidden;
			background-color: white
		}

		.content {
			height: 635px;
			margin-top: 30px;
			padding-top: 30px;
			background-color: #f5f4f2
		}

		.logo {
			margin-top: 150px;
			text-align: center
		}

		.login {
			text-align: center
		}

		.login input {
			border: 1px solid #e0e0e0
		}

		.login .input {
			margin-bottom: 6px;
		}

		.lgn_ipt {
			display: inline-block
		}

		.btn_lgn {
			display: inline-block;
			*margin-left: 5px;
			vertical-align: top
		}

		.prompt {
			text-align: center;
			margin-top: 5px;
			height: 30px;
		}

		.prompt select {
			width: 75px
		}

		.select2-result-label {
			text-overflow: ellipsis;
			display:block;
			white-space:nowrap;
			overflow:hidden;
		}
	</style>
</head>
<body>
<#include "common/messages.ftl"/>
<script language="javascript">
	if (top.location.href.indexOf("login") == -1) {
		top.location.href = "${req.getContextPath()}/login";
	}
</script>
<div class="logo">
	<img src="${req.getContextPath()}/img/logo_ngrinder_a.png" width="400" height="106" alt="nGrinder"/>
</div>
<div class="content">
	<form action="${req.getContextPath()}/form_login" method="POST">
		<fieldset>
			<div class="login">
					<span class="lgn_ipt">
						<input type="text" class="span2 input" name="j_username" id="j_username"
							   placeholder="User ID"><br>
						<input type="password" class="span2 input" name="j_password" id="j_password"
							   placeholder="Password">
					</span>
				<input id="loginBtn" type="image"
					   src="${req.getContextPath()}/img/login.gif" alt="Login"
					   class="btn_lgn" style="margin-top:0; margin-left:20px"/>
			</div>

			<div class="prompt">
				<input type="checkbox" class="checkbox" style="margin-top:-4px"
					   name='_spring_security_remember_me'/>
				<span>Remember Me</span>
				<select id="native_language" name="native_language" style="margin-left:60px;width:80px">
					<option value="en">English</option>
					<option value="kr">한국어</option>
					<option value="cn">中文</option>
				</select>
			</div>

			<div class="prompt">
				<select id="user_timezone" name="user_timezone" style="width:240px">
				<#list timezones as each>
					<option value="${each.ID}">${each.ID} - ${each.displayName}</option>
				</#list>
				</select>
			</div>

		<#if signUpEnabled??>
			<div class="prompt">
				<a id="sign_up" class="pointer-cursor" style="margin-left:200px;">Sign Up</a>
			</div>
		</#if>

		</fieldset>
	</form>
<#include "common/copyright.ftl">
</div>

<div id="sign_up_modal_container"></div>
<script language="javascript">
	$(document).ready(function () {
		$('input[placeholder]').placeholder();
		$.ajaxSetup({ cache: false });
		var language = getCookie("org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE");
		if (language.indexOf("kr") > -1) {
			$("#native_language").val("kr");
		} else if (language.indexOf("cn") > -1) {
			$("#native_language").val("cn");
		} else if (language.indexOf("cn") > -1) {
			$("#native_language").val("en");
		} else {
			$("#native_language").val("${defaultLang}");
		}

		var $userTimeZone = $("#user_timezone");
		$userTimeZone.select2({
			placeholder: "Select a Timezone"
		});
		var timezone = jstz.determine();
		$userTimeZone.select2("val", timezone.name());

		$("#sign_up").click(function () {
			var url = "${req.getContextPath()}/sign_up/new";
			$("#sign_up_modal_container").load(url, function () {
				$(this).find("#sign_up_modal").modal('show');
			});
		});
	});
</script>
</body>
</html>
