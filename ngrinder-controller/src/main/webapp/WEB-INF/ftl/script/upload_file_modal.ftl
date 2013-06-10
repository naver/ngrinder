<div class="modal hide fade" id="upload_file_modal">
	<div class="modal-header">
		<a class="close" data-dismiss="modal" id="upCloseBtn">&times;</a>
		<h4><@spring.message "script.list.button.upload"/></h4>
	</div>
	<div class="modal-body">
		<form class="form-horizontal" method="post" target="_self" action="${req.getContextPath()}/script/upload/${currentPath}"
				id="uploadForm" enctype="multipart/form-data">
			<fieldset>
				<input type="hidden" id="path" name="path"/>
				<div class="control-group">
					<label for="discriptionInput" class="control-label"><@spring.message "script.option.commit"/></label>
					<div class="controls">
					  <input type="text" id="discriptionInput" name="description">
					  <span class="help-inline"></span>
					</div>
				</div>
				<div class="control-group">
					<label for="fileInput" class="control-label"><@spring.message "script.list.label.file"/></label>
					<div class="controls" rel='upload_file_modal_popover' 
					  		title='<@spring.message "script.list.popover.upload.title"/>'
					  		data-html='true'
					   		data-content='<@spring.message "script.list.popover.upload.content"/>'>
					  <input type="file" class="input-file" id="fileInput" name="uploadFile"/> 
					  <span class="help-inline"></span>
					</div>
				</div>				
			</fieldset>
		</form>
	</div>
	<div class="modal-footer">
		<button class="btn btn-primary" id="upload_file_button"><@spring.message "script.list.button.upload"/></button>
		<button class="btn" data-dismiss="modal"><@spring.message "common.button.cancel"/></button>
	</div>
</div>
<script>
	$(document).ready(function() {

		$("div[rel='upload_file_modal_popover']").popover({trigger: 'focus', container:'#upload_file_modal'});
		$("#upload_file_button").click(function() {
			var $file = $("#fileInput");
			if (checkEmptyByObj($file)) {
				markInput($file, false, "<@spring.message "common.form.validate.empty"/>");
				return;
			}
			document.forms.uploadForm.submit();
		});
	});
</script>