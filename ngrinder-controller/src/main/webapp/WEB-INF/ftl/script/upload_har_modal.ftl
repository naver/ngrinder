<#include "../common/ngrinder_macros.ftl">
<div class="modal hide fade" id="upload_file_modal" xmlns="http://www.w3.org/1999/html">
	<div class="modal-header">
		<a class="close" data-dismiss="modal">&times;&nbsp;</a>
		<h4><@spring.message "script.action.convert.uploadHAR2Script"/></h4>
	</div>

	<div class="tabbable modal-body" id="input_form" style="display:; overflow-x:hidden; overflow-y:hidden; max-height: 600px;">
		<div>
			<span class="pull-left">
				<input type="checkbox" name="removeStaticResource" id="removeStaticResource" value="true">
				<label class="checkbox" style="position:relative; margin-left:20px; margin-top: -18px;" for="removeStaticResource">
					Remove Static Resource Call
				</label>
			</span>
			<spna class="pull-right" >
				<input type="file" class="input-file" id="file_input" name="fileInput"/>
				<button class="btn btn-primary" id="har-load">SHOW</button>
				<span class="help-inline"></span>
			</spna>
		</div>
		<div style="margin-top: 30px; height: 535px;">
			<form method="post" action="/script/har-load" id="upload_textrea_form" name="uploadTextareaForm">
			<@control_group name="har_textarea" label_message_key="HAR Content Area">
				<textarea id="har_textarea" name="har_textarea" title="har_textarea" class="tx_area" style="resize: none; height:470px; width:98%;"></textarea>
			</@control_group>
			</form>
		</div>
	</div>

	<div class="tabbable modal-body" id="result_form" style="display:none; overflow-x:hidden; overflow-y:hidden; max-height: 600px;">
		<ul class="nav nav-tabs" id="result_tab">
			<li class="active">
				<a href="#groovy_type"><@spring.message "script.action.convert.groovy"/></a>
			</li>
			<li>
				<a href="#jython_type"><@spring.message "script.action.convert.jython"/></a>
			</li>
		</ul>
		<div class="tab-content">
			<div class="tab-pane active" style="height: 600px;" id="groovy_type">
				<textarea name="groovy_textarea" class="tx_area" style="resize: none; height:520px; width:98%;"></textarea>
			</div>
			<div class="tab-pane" style="height: 600px;" id="jython_type">
				<textarea name="jython_textarea" class="tx_area" style="resize: none; height:520px; width:98%;"></textarea>
			</div>
		</div>
	</div>
	<div class="modal-footer">
		<button class="btn btn-primary" id="textarea_convert_button"><@spring.message "script.action.convert"/></button>
		<button class="btn" data-dismiss="modal"><@spring.message "common.button.cancel"/></button>
	</div>
</div>

<script>
	$(document).ready(function() {
		//validate textarea
		$("#upload_textrea_form").validate({
			rules: {
				har_textarea: {
					required: true
				},
			},
			messages: {
				har_textarea: {
					required: "<@spring.message "common.message.validate.empty"/>"
				}
			},
			errorClass: "help-inline",
			errorElement: "span",
			highlight: function (element, errorClass, validClass) {
				$('#upload_textrea_form').find('.control-group').addClass('error').removeClass('success');
			},
			unhighlight: function (element, errorClass, validClass) {
				$('#upload_textrea_form').find('.control-group').removeClass('error').addClass('success');
			}
		});

		$("#textarea_convert_button").click(function() {
			var data = new FormData();
			data.append("removeStaticResource", $('input:checkbox[name="removeStaticResource"]').is(":checked"));
			if ($('#upload_textrea_form').valid()) {
				data.append('har', $('#har_textarea').val());
				ajaxFormData(data,'/script/har-convert');
			}
		});

		$("#har-load").click(function() {
			var data = new FormData();
			data.append("removeStaticResource", $('input:checkbox[name="removeStaticResource"]').is(":checked"));
			if ($('#file_input')[0].files[0] != null) {
				data.append('uploadFile', $('#file_input')[0].files[0]);
				ajaxFormData(data, '/script/har-load');
			}
		});

		//show tab navi
		var $inputTab = $('#input_tab');
		$inputTab.find('a').click(function (e) {
			e.preventDefault();
			$formType = $(this).attr('href');
			$(this).tab('show');
		});
		$inputTab.find('a:first').tab('show');

		var $resultTab = $('#result_tab');
		$resultTab.find('a').click(function (e) {
			e.preventDefault();
			$(this).tab('show');
		});
		$resultTab.find('a:first').tab('show');

	});

	function ajaxFormData(data, url){
		showProgressBar("<@spring.message 'script.action.convert.trans'/>");
		$.ajax({
			url: url,
			processData: false,
			contentType: false,
			data: data,
			type: 'POST',
			success: function(result){
				if(url == '/script/har-load'){
					$('textarea[name="har_textarea"]').val(result);
					$('#upload_textrea_form').find('.control-group').removeClass('error').addClass('success');
				} else {
					$('#input_form').hide();
					$('#result_form').show();
					$('#textarea_convert_button').hide();
					$('.control-group').removeClass('error');
					$('textarea[name="groovy_textarea"]').val(result['groovy']);
					$('textarea[name="jython_textarea"]').val(result['jython']);
				}
				$('.help-inline').text('');
			},
			complete:function () {
				hideProgressBar();
			},error:function () {
				showErrorMsg("<@spring.message 'common.error.error'/>");
			}
		});
	}

</script>
