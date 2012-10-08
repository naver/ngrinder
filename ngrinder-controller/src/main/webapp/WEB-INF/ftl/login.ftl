<!DOCTYPE html>
<html lang="en">
	<head>
		<title>nGrinder Login</title>
		
		<#include "common/common.ftl">
		<link href="${req.getContextPath()}/js/select2/select2.css" rel="stylesheet"/>
		<script src="${req.getContextPath()}/js/select2/select2.js"></script>
		<script src="${req.getContextPath()}/js/detect_timezone.js"></script>
		<style>
			body { overflow-y:hidden; background-color:white }
			.content { height:635px; margin-top:30px; padding-top:30px; background-color:#f5f4f2 }
			.logo { margin-top:150px; text-align:center }
			.login { text-align:center }
			.login input { border:1px solid #e0e0e0 }
			.login .input { margin-bottom:6px; }
			.lgn_ipt { display:inline-block }
			.btn_lgn { display:inline-block; *margin-left:5px; vertical-align:top }
			.prompt { text-align:center; margin-top:5px; *margin-top:2px; height:30px; }
			.prompt select { width:75px }
			.prompt .chk { vertical-align:middle; margin-right:5px; *margin-right:2px }
		</style>
	</head>
<body>
	<div>
		<script language="javascript">
				if (top.location.href.indexOf("login") == -1) {
					top.location = "${req.getContextPath()}/login";
				}
		</script>	
		<div class="logo"><img src="${req.getContextPath()}/img/logo_ngrinder_a.png" width="400" height="106" alt="nGrinder"></div>
		<div class="content">
			<form action="${req.getContextPath()}/form_Login" method="POST">
				<fieldset>
					<div class="login">
						<span class="lgn_ipt">
							<input type="text" class="span2 input" name="j_username" id="j_username" placeholder="User ID"><br>
							<input type="password" class="span2 input" name="j_password" id="j_password" placeholder="Password"> 	 		
						</span>
						<input id="loginBtn" type="image" src="${req.getContextPath()}/img/login.gif" alt="Login" class="btn_lgn" width="60px" style="margin-top:0px; margin-left:25px"> 
					</div>
					
					<div class="prompt">
						<input type="checkbox" class="chk" name='_spring_security_remember_me'>Remeber me :
						<select  id="native_language" name="native_language" style="margin-left:60px;"> 
							  <option value="en">English</option>
							  <option value="kr">한국어</option>
							  <option value="cn">中文</option>
						</select> 
					</div>
					
					<div class="prompt">
						<select  id="user_timezone"  name="user_timezone" style="width:240px">
						  <#list timezones as eachtimezone>
							  <option value="${eachtimezone.ID}">${eachtimezone.ID} - ${eachtimezone.displayName}</option>
					      </#list>
						</select> 
					</div> 
				</fieldset>
			</form>
				<div class="row" id="footDiv" >
					<div class="center">
						<hr>
						<footer>
						    <p> nGrinder ver ${nGrinderVersion} © 2012 <a href="http://www.nhncorp.com/nhn/index.nhn" target="_blank"><@spring.message "common.nhnCorp"/></a></p>
						</footer>
					</div>
				</div>
		</div> 
	</div>

	<script language="javascript">
		$.ajaxSetup({ cache: false });
		$(function(){
			var language=getCookie("org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE");
			if (language.indexOf("kr") > -1){
				$("#native_language").val("kr");
			} else if (language.indexOf("cn") > -1) {
				$("#native_language").val("cn");
			} else {
				$("#native_language").val("en");
			}
			
			$("#user_timezone").change(function() {
				if ($("#user_timezone option:selected").val() == "all") {
					$("#user_timezone").load("${req.getContextPath()}/allTimeZone");
				}
			});
		});
		
		
		$(document).ready(function() {
			$("#user_timezone").select2({
				placeholder: "Select a Timezone"
			});
			var timezone = jstz.determine();
			$("#user_timezone").select2("val", timezone.name());
		});
	</script>
</body>
</html>
