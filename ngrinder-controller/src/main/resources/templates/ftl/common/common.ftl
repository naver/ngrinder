<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<meta http-equiv="Cache-Control" content="no-cache"/>
<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
<link rel="shortcut icon" type="image/png" href="${req.getContextPath()}/img/favicon.png"/>
<link href="${req.getContextPath()}/css/bootstrap.min.css?${nGrinderVersion}" rel="stylesheet"/>
<link href="${req.getContextPath()}/css/ngrinder.css?${nGrinderVersion}" rel="stylesheet"/>
<link href="${req.getContextPath()}/css/introjs.min.css?${nGrinderVersion}" rel="stylesheet"/>
<script type="text/javascript" src="${req.getContextPath()}/js/jquery-1.10.2.min.js?${nGrinderVersion}"></script>
<script type="text/javascript" src="${req.getContextPath()}/js/jquery.number.min.js?${nGrinderVersion}"></script>
<script type="text/javascript" src="${req.getContextPath()}/js/bootstrap.min.js?${nGrinderVersion}"></script>
<script type="text/javascript" src="${req.getContextPath()}/js/bootbox.min.js?${nGrinderVersion}"></script>
<script type="text/javascript" src="${req.getContextPath()}/js/utils.js?${nGrinderVersion}"></script>
<script type="text/javascript" src="${req.getContextPath()}/js/jquery.validate.min.js?${nGrinderVersion}"></script>
<script type="text/javascript" src="${req.getContextPath()}/js/intro.min.js?${nGrinderVersion}"></script>
<#include "select2.ftl"/>
<#import "spring.ftl" as spring/>
<#include "ngrinder_macros.ftl">
<script type="text/javascript">
	setAjaxContextPath("${req.getContextPath()}");
	//common validation function and options.
	$.validator.addMethod('positiveNumber',
			function (value) {
				return Number(value) > 0;
			}, '<@spring.message "common.message.validate.positiveNumber"/>');
	$.validator.addMethod('countNumber',
			function (value) {
				return Number(value) >= 0;
			}, '<@spring.message "common.form.validate.countNumber"/>');

	$.extend(jQuery.validator.messages, {
		required: "<@spring.message "common.message.validate.empty"/>",
		digits: "<@spring.message "common.message.validate.digits"/>",
		range: $.validator.format("<@spring.message "common.message.validate.range"/>"),
		max: $.validator.format("<@spring.message "common.message.validate.max"/>"),
		min: $.validator.format("<@spring.message "common.message.validate.min"/>"),
		maxlength: $.validator.format("<@spring.message "common.message.validate.maxLength"/>"),
		rangelength: $.validator.format("<@spring.message "common.message.validate.rangeLength"/>")
	});

</script>
<#setting number_format="computer">
<#if currentUser?? && currentUser.timeZone??>
	<#setting time_zone="${currentUser.timeZone}">
</#if>  
