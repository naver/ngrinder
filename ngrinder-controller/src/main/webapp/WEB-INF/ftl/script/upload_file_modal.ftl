<div class="modal hide fade" id="upload_file_modal">
	<div class="modal-header">
		<a class="close" data-dismiss="modal" id="upCloseBtn">&times;</a>
		<h4><@spring.message "script.action.upload"/></h4>
	</div>
	<div class="modal-body">
		<form class="form-horizontal" method="post" target="_self" action="${req.getContextPath()}/script/upload/${currentPath}"
				id="uploadForm" enctype="multipart/form-data">
			<fieldset>
				<input type="hidden" id="path" name="path"/>

				<@control_group name="descriptonInput" inline_help="true" label_message_key="script.action.commit">
					<input type="text" id="description_input" name="description">
				</@control_group>

				<@control_group name="fileInput" label_message_key="script.info.file">
					<div rel='upload_file_modal_popover' data-html='true'
						 title='<@spring.message "script.message.upload.title"/>'
						 data-content='<@spring.message "script.message.upload.content"/>'>
						<input type="file" class="input-file" id="file_input" name="uploadFile"/>
						<span class="help-inline"></span>
					</div>
				</@control_group>

			</fieldset>
		</form>
	</div>
	<div class="modal-footer">
		<button class="btn btn-primary" id="upload_file_button"><@spring.message "script.action.upload"/></button>
		<button class="btn" data-dismiss="modal"><@spring.message "common.button.cancel"/></button>
	</div>
</div>
<script>
	$(document).ready(function() {

		$("div[rel='upload_file_modal_popover']").popover({trigger: 'focus', container:'#upload_file_modal'});
		$("#upload_file_button").click(function() {
			var $file = $("#file_input");
			if (checkEmptyByObj($file)) {
				markInput($file, false, "<@spring.message "common.message.validate.empty"/>");
				return;
			}
			document.forms.uploadForm.submit();
		});
	});
</script>