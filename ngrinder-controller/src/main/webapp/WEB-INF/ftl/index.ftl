<!DOCTYPE html>
<html>
	<head>
		<#include "common/common.ftl">
		<title><@spring.message "home.title"/></title>
		<style>
			.wrap {
				height: 470px;
			}
			.hero-unit { 
				background-image: url('${req.getContextPath()}/img/bg_main_banner_en.png?${nGrinderVersion}');
				margin-bottom: 10px;
				height: 160px;
				padding: 0;
				margin-top: 0;
			}    
			.quick-start {
				padding-left: 160px;
				padding-top: 35px
			}
			.table {
				margin-bottom: 5px
			}
			.container .well {
				margin-top:0px;
			}
			#option_modal {
				background-image: url('${req.getContextPath()}/img/bg_main_banner_popup.png?${nGrinderVersion}');
				background-repeat: no-repeat;
			}
		</style> 
	</head>
	<body>
	<div id="wrap">
	<#include "common/navigator.ftl">
	<div class="container wrap">
		<div class="hero-unit"/>
			<form class="form-inline" name="quickStart" id="quick_start" action="${req.getContextPath()}/perftest/quickstart" method="POST">
				<div class="quick-start" data-original-title="<@spring.message "home.tip.url.title"/>" data-content="<@spring.message "home.tip.url.content"/>" data-placement="bottom" rel="popover">
					<input type="text" name="url" id="url" class="span6 url_ex required" placeholder="<@spring.message code="home.placeholder.url"/>"/>
					<input id="options" type="hidden" name="options">
					<select class="select-item span2" id="script_type" name="scriptType">
						<#list handlers as handler>
							<option value="${handler.key}">${handler.title}</option>
						</#list>
					</select>
					<button id="start_test_btn" class="btn btn-primary" ><@spring.message "home.button.startTest"/></button>
				</div> 
			</form>
		</div>
		<div class="row">
			<div class="span6">
				<div class="page-header">
	 				 <h4><@spring.message "home.qa.title"/></h4>  
				</div>
		   		<div class="well">
			  		<br/>
					<table class="table table-striped ellipsis">
						<colgroup>
							<col width="350px"/>
							<col />
						</colgroup>
						<@list list_items=left_panel_entries ; each_left_entry , each_left_entry_index>
							<#if each_left_entry_index lt 6>
							<tr>
								<td class="ellipsis">
									<#if each_left_entry.isNew()><span class="label label-info">new</span></#if>
									<a href="${each_left_entry.link }" target="_blank">${each_left_entry.title}
									</a>
								</td>
								<td>${each_left_entry.lastUpdatedDate?string("yyyy-MM-dd")}
								</td>
							</tr>
							</#if>
						</@list>

						<#if left_panel_entries?has_content>
						<tr>
							<td>
								<img src="${req.getContextPath()}/img/asksupport.gif"/>
								<a href="${ask_question_url}" target="_blank"><@spring.message "home.button.ask"/></a>
							</td>
							<td><a href="${see_more_question_url}" target="_blank"><i class="icon-share-alt"></i>&nbsp;<@spring.message "home.button.more"/></a></td>
						</tr>
						</#if>
					</table>
  				</div>
			</div>
			<div class="span6">
				<div class="page-header">
	 				 <h4><@spring.message "home.resources.title"/></h4>
				</div>

		   		<div class="well">
			  		<br/>
					<table class="table table-striped ellipsis">
						<colgroup>
							<col width="350px"/>
							<col />
						</colgroup>
						<@list list_items=right_panel_entries ; each_right_entry , each_right_entry_index>
							<#if each_right_entry_index lt 6>
							<tr>
								<td class="ellipsis">
									<#if each_right_entry.isNew()><span class="label label-info">new</span></#if>
									<a href="${each_right_entry.link}" target="_blank">${each_right_entry.title}</a>
								</td>
								<td>${each_right_entry.lastUpdatedDate?string("yyyy-MM-dd")}</td>
							</tr>
							</#if>
						</@list>
						<#if right_panel_entries?has_content>
						<tr>
							<td></td>
							<td><a href="${see_more_resources_url}"	target="_blank">
								<i	class="icon-share-alt"></i>&nbsp;<@spring.message "home.button.more"/></a></td>
						</tr>
						</#if>
					</table>
			  	</div>

			</div>
		</div>
	</div>
	<div class="modal hide fade modal-lg" id="option_modal">
		<div class="modal-dialog">
			<div class="modal-header" style="margin-top: 5px;padding-top: 23px;padding-left: 165px;">
				<select id="method" name="method" class="span2">
					<option value="GET">GET</option>
					<option value="POST">POST</option>
				</select>
				<input type="text" name="url" id="fixed_url" class="span5 test-url" placeholder="<@spring.message code="home.placeholder.url"/>"/>
				<select class="select-item span2" id="fixed_script_type" name="scriptType">
					<#list handlers as handler>
						<option value="${handler.key}">${handler.title}</option>
					</#list>
				</select>
			</div>
			<div class="modal-body" style="padding-left: 30px;">
				<#include "common/script_option.ftl">
			</div>
			<div class="modal-footer">
				<button id="quick_start_submit" class="btn btn-primary"><@spring.message "home.button.startTest"/></button>
				<button id="quick_start_cancle" class="btn btn-default"><@spring.message "common.button.cancel"/></button>
			</div>
		</div>
	</div>
	</div>
	<#include "common/copyright.ftl">

    <script>
		$(document).ready(function(){
			$.validator.addMethod('url_ex',
				    function (value) {
						return /^((https?|ftp):\/\/)?(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|(([\[]{1})([\da-fA-F]{1,4}:){7}[\da-fA-F]{1,4}([\]]{1}))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i.test(value);
					}, '');

			$("#start_test_btn").click(function() {
				var $url = $("#url");
				if ($url.valid()) {
					var urlValue = $url.val();
					if (!urlValue.match("^(http|ftp)")) {
						$url.val("http://" + urlValue);
					}
					$("#option_modal").modal("show");
					$("#fixed_url").val(urlValue);
					$("#fixed_script_type").val($("#script_type").val());
				}
				return false;
			});
			
			$("#quick_start").validate({
				errorPlacement: function(error, element) {
	            	$("div.quick-start").popover("show");
				}
			});

		    $("#url").change(function() {
		    	if ($(this).valid()) {
		    		$("div.quick-start").popover("hide");
		    	}
			});
			
			$("#method").change(function(e) {
				var methodName = $(e.target).val();
				changeHTTPMethod(methodName);
			});
			
			$("#quick_start_cancle").click(function() {
				$("#option_modal").modal("hide");
				$("#url").focus();
			});
			
			$("#quick_start_submit").click(function() {
				$("#option_modal").modal("hide");
				$("#options").val(options.toJson($("#method").val()));
				$("#url").val($("#fixed_url").val());
				$("#script_type").val($("#fixed_script_type").val());
				$("#quick_start").submit();
			});
		});
	</script>
	</body>
</html>
