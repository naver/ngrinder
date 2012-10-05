		<div class="modal fade" id="uploadScriptModal">
			<div class="modal-header">
				<a class="close" data-dismiss="modal" id="upCloseBtn">&times;</a>
				<h3><@spring.message "script.list.button.upload"/></h3>
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
							<div class="controls">
							  <input type="file" class="input-file" id="fileInput" name="uploadFile" data-original-title="<@spring.message "script.list.popover.upload.title"/>" data-content="<@spring.message "script.list.popover.upload.content"/>">
							  <span class="help-inline"></span>
							</div>
						</div>				
					</fieldset>
				</form>
			</div>
			<div class="modal-footer">
				<a href="#" class="btn btn-primary" id="uploadBtn2"><@spring.message "script.list.button.upload"/></a>
				<a href="#uploadScriptModal" class="btn" data-toggle="modal"><@spring.message "common.button.cancel"/></a>
			</div>
		</div>
		<script>
			$(document).ready(function() {
				$("#uploadBtn2").click(function() {
					var $file = $("#fileInput");
					if (checkEmptyByObj($file)) {
						markInput($file, false, "<@spring.message "common.form.validate.empty"/>");
						return;
					}
					
					$("#path").val($("#upScriptNameInput").val());
					document.forms.uploadForm.submit();
				});
			});
		</script>