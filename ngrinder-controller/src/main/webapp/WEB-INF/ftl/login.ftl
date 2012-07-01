<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-Style-Type" content="text/css">

<title>nGrinder ::  NHN Performance Center</title>
<input type="hidden" id="contextPath" value="${Request.getContextPath()}"/>
<link rel="stylesheet" type="text/css" href="${Request.getContextPath()}/css/ngrinder.css">
<script language="javascript" type="text/javascript" src="${Request.getContextPath()}/js/jquery-1.7.min.js"></script>
<script language="javascript" type="text/javascript" src="${Request.getContextPath()}/js/utils.js"></script>
<script language="javascript" type="text/javascript" src="${Request.getContextPath()}/js/i18n/i18nTool.js"></script>
<#import "spring.ftl" as spring/>
<link rel="shortcut icon" href="favicon.ico"/>
<script language="javascript">
	$.ajaxSetup({ cache: false });
	$(function(){
		var language=getCookie("org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE");
		$("#user_locale").val("${defaultTime!''}");
		if (language.indexOf("kr") > -1){
			$("#native_language").val("kr");
		} else if (language.indexOf("cn") > -1) {
			$("#native_language").val("cn");
		} else {
			$("#native_language").val("en");
		}
		
		if ($("#j_username").val() == ""){ 
			$("#j_username").css("background-image", "url('/img/bg_j_username.png')");
			$("#j_username").css("background-repeat", "no-repeat");
		}
		
		if ($("#j_password").val() == "") {
			$("#j_password").css("background-image", "url('/img/bg_j_password.png')");
			$("#j_password").css("background-repeat", "no-repeat");
		}
		
		$("#j_username").focus(function(){
			$(this).css("background-image", "");
		});
		
		$("#j_username").focusout(function(){
			hideBG($(this));
			if ($.trim($("#j_password").val()) != "") {
				$("#j_password").css("background-image", "");
			} else {
				$("#j_password").focus();
			}
		});
		
		$("#j_password").focus(function(){
			$("#j_password").css("background-image", "");
		});
		
		$("#j_password").focusout(function(){
			hideBG($(this));
		});
	});
	
	function hideBG(ele) {
		if (ele.val() == "") {
			ele.css("background-image", "url('/img/bg_" + ele.attr("id") + ".png')");
			ele.css("background-repeat", "no-repeat");
		} else {
			ele.css("background-image", "ss");	
		}
	}
	
	function getAllLocation() {
		if ($("#user_locale option:selected").val() == "all") {
			$("#user_locale").load("${Request.getContextPath()}/allTimeZone");
		}
	}
</script>
</head>
<body style="overflow-y:hidden;background-color:white">
<#import "spring.ftl" as spring/>
<div id="wrap" class="login_wrap">
	<div class="logo"><img src="${Request.getContextPath()}/img/logo_ngrinder_a.png" width="400" height="106" alt="nGrinder"></div>
	<div class="content">
		<form action="${Request.getContextPath()}/form_Login" method="POST">
		<fieldset>
		<legend class="blind"><@spring.message "login.button"/></legend>
		<div class="login">
			<span class="lgn_ipt">
			<input type="text" class="id" name="j_username" id="j_username"><br>
			<input type="password" class="password" name="j_password" id="j_password">			
			</span>
			<input id="loginBtn" type="image" src="${Request.getContextPath()}/img/login.gif" alt="Login" class="btn_lgn" width="60px" >
		</div>
		
		<div class="prompt">
			<input type="checkbox" class="chk" name='_spring_security_remember_me'><@spring.message "login.remember"/>
			<select  id="native_language" name="native_language" style="margin-left:25px;width:62px;">
              <option value="en">English</option>
              <option value="kr">한국어</option>
              <option value="cn">中文</option>
		    </select>
		</div>
		
		<div class="prompt">
			   Regional Location：       
			<select  id="user_locale"  name="user_locale" style="margin-left:15px;width:62px;" onChange="getAllLocation()">
              <option value="Asia/Seoul">한국</option>
              <option value="Asia/Shanghai">中国</option>
              <option value="America/New_York">USA</option>
              <option value="all">All</option>
		    </select>
		</div>
		
		<div class="addr">
			<a href="http://nhnopensource.org/ngrinder">Ver. ${version}</a>, Copyright © <span>NHN Corp. </span>All Rights Reserved.
		</div>
		</fieldset>
		</form>
	</div>
</div>
</body>
</html>