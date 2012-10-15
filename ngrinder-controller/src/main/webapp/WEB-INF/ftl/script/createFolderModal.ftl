<div class="modal fade" id="createFolderModal">
			<div class="modal-header">
				<a class="close" data-dismiss="modal" id="createCloseBtn">&times;</a>
				<h3><@spring.message "script.list.button.createFolder"/></h3>
			</div>
			<div class="modal-body">
				<form class="form-horizontal" method="post" target="_self" id="createFolderForm" action="${req.getContextPath()}/script/create/${currentPath}">
					<fieldset>
						<div class="control-group">
							<label for="folderNameInput" class="control-label"><@spring.message "script.list.label.folderName"/></label>
							<div class="controls">
							  <input type="hidden" name="type" value="folder"/>
							  <input type="text" id="folderNameInput" name="folderName" 
							  		data-content="<@spring.message "common.form.rule.sampleName"/>"
									data-original-title="<@spring.message "script.list.label.folderName"/>"/>
							  <span class="help-inline"></span>
							</div>
						</div>					
					</fieldset>
				</form>
			</div>
			
			<div class="modal-footer">
				<a href="#" class="btn btn-primary" id="createFolderBtn"><@spring.message "common.button.create"/></a>
				<a href="#createFolderModal" class="btn" data-toggle="modal"><@spring.message "common.button.cancel"/></a>
			</div>
		</div>
		<script>
			$(document).ready(function() {
				$("#createFolderBtn").click(function() {
					var $name = $("#folderNameInput");
					if (checkEmptyByObj($name)) {
						markInput($name, false, "<@spring.message "common.form.validate.empty"/>");
						return;
					} else {
						if (!checkSimpleNameByObj($name)) {
							markInput($name, false, "<@spring.message "common.form.validate.format"/>");
							return;
						}
						markInput($name, true);
					}
					
					document.forms.createFolderForm.submit();
				});
			});
		</script>