<#import "../common/spring.ftl" as spring/>
<script src="${req.getContextPath()}/js/jquery.validate.js"></script>
<script type="text/javascript">
	
	$(document).ready(function(){
		<#if !(user?has_content)>
		$(".collapse").collapse();
		$("#user_pw_head").attr("href","");

		var userIdValidMsg;
		jQuery.validator.addMethod("userIdFmt", function(userId, element ) {
			var patrn = "^[a-zA-Z]{1}([a-zA-Z0-9]|[_]|[-]|[.]){0,19}$";
			var rule = new RegExp(patrn);
			if (!rule.test($.trim(userId))) {
				validateError(this);
				userIdValidMsg = "<@spring.message "user.info.warning.userId.intro"/>";
				return false;
			}
			return true;
		}, "<@spring.message 'user.info.warning.userId.invalid'/>" );
		
		
		jQuery.validator.addMethod("userPhoneNumber", function(mobilePhone, element ) {
			
			var patrn = /^\d{2}-\d{4}-\d{5}/;
			var rule = new RegExp(patrn);
			if (!rule.test($.trim($("#mobilePhone").val()))) {
				userIdValidMsg = "<@spring.message "user.info.warning.userId.intro"/>";
				return false;
			}
			return true;
		}, "<@spring.message 'user.info.warning.userId.invalid'/>" );
		
		
		

		jQuery.validator.addMethod("userIdExist", function( userId, element ) {
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
				return result;
			}
			return false;
		}, "<@spring.message 'user.info.warning.userId.exist'/>");
		
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
	
	function FormatPhone (e,input) { 
        /* to prevent backspace, enter and other keys from  
         interfering w mask code apply by attribute  
         onkeydown=FormatPhone(control) 
        */ 
        evt = e || window.event; /* firefox uses reserved object e for event */ 
		var pressedkey = evt.which || evt.keyCode; 
        var BlockedKeyCodes = new Array(8,27,13,9); //8 is backspace key 
        var len = BlockedKeyCodes.length; 
        var block = false; 
        var str = ''; 
        for (i=0; i<len; i++){ 
           str=BlockedKeyCodes[i].toString(); 
           if (str.indexOf(pressedkey) >=0 ) block=true;  
        } 
        if (block) return true; 
	 
       s = input.value; 
       if (s.charAt(0) =='+') return false; 
       filteredValues = '"`!@#$%^&*()_+|~-=\QWERT YUIOP{}ASDFGHJKL:ZXCVBNM<>?qwertyuiop[]asdfghjkl;zxcvbnm,./\\\'';  
       var i; 
       var returnString = ''; 
       /* Search through string and append to unfiltered values  
          to returnString. */ 
       for (i = 0; i < s.length; i++) {  
             var c = s.charAt(i); 
             if ((filteredValues.indexOf(c) == -1) & (returnString.length <  12 )) { 
        	     if (returnString.length==2) returnString +='-'; 
	             if (returnString.length==7) returnString +='-'; 
	             returnString += c; 
        	     } 
       	} 
       input.value = returnString; 
        
       return false 
   } 
	
</script>

<form action="${req.getContextPath()}/user/save"
	class="form-horizontal form-horizontal-left" id="registerUserForm" style="margin-left:30px" method="POST">
	<fieldset>
		<div class="control-group">
			<label class="control-label"><@spring.message "user.info.form.userId"/></label>
			<div class="controls">
				<input type="text" class="span4 required userIdFmt userIdExist" id="userId" name="userId"
				    rel="popover" value="${(user.userId)!}"
					data-content="<@spring.message "user.info.warning.userId.intro"/>"
					data-original-title="<@spring.message "user.info.form.userId"/>"
					<#if user?? && user.userId??>readonly="value"</#if> >
				<span id="userIdError_span_id" class="help-inline"> </span>
				<input type="hidden" id="id" name="id" value="${(user.id)!}">
			</div>
		</div>
		
		<div class="control-group">
			<label class="control-label"><@spring.message "user.option.name"/></label>
			<div class="controls">
				<input type="text" class="span4 required" id="userName"
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
				<input type="text" class="span4 required email" id="email"
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
				<input type="text" class="span4 userPhoneNumber" id="mobilePhone" onkeypress="FormatPhone (event,mobilePhone);" 
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










