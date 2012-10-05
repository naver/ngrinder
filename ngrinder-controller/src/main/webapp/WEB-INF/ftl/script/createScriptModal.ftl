		<div class="modal fade" id="createScriptModal">
			<div class="modal-header">
				<a class="close" data-dismiss="modal" id="createCloseBtn">&times;</a>
				<h3><@spring.message "script.list.button.createScript"/></h3>
			</div>
			<div class="modal-body">
				<form class="form-horizontal form-horizontal-4" method="post" target="_self" id="createForm" action="${req.getContextPath()}/script/create/${currentPath}">
					<fieldset>
						<div class="control-group">
							<label for="scriptNameInput" class="control-label"><@spring.message "script.option.name"/></label>
							<div class="controls">
							  <input type="text" id="scriptNameInput" name="fileName">
							  <span class="help-inline"></span>
							</div>
						</div>
						<div class="control-group">
							<label for="languageSelect" class="control-label"><@spring.message "script.list.label.type"/></label>
							<div class="controls">
								<input type="hidden" name="type" value="script"/>
								<select id="languageSelect" name="scriptType">
									<option value="py">Python</option>
								</select>
							  <span class="help-inline"></span>
							</div>
						</div>
						<div class="control-group">
							<label for="urlInput" class="control-label"><@spring.message "script.list.label.url"/></label>
							<div class="controls">
							  <input type="text" id="urlInput" class="url" 
							         placeholder="<@spring.message "home.placeholder.url"/>" 
							         name="testUrl" data-original-title="<@spring.message "home.tip.url.title"/>" 
							         data-content="<@spring.message "home.tip.url.content"/>"/>
							  <span class="help-inline"></span>
							</div>
						</div>
					</fieldset>
				</form>
			</div>
			
			<div class="modal-footer">
				<a href="#" class="btn btn-primary" id="createBtn2"><@spring.message "common.button.create"/></a>
				<a href="#createScriptModal" class="btn" data-toggle="modal"><@spring.message "common.button.cancel"/></a>
			</div>
		</div>
		<script>
			$(document).ready(function() {
				$("#createBtn2").on('click', function() {
				var $name = $("#scriptNameInput");
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
				
				var name = $name.val();
				var extension = "." + $("#languageSelect").val().toLowerCase();
				var idx = name.toLowerCase().indexOf(extension);
				if (name.length < 3 || idx == -1 || idx < name.length - 3) {
					$name.val(name + extension);
				}
				
				var urlValue = $("#urlInput");
				if (!checkEmptyByObj(urlValue)) {
				
					if (!urlValue.valid()) {
						markInput(urlValue, false, "<@spring.message "common.form.validate.format"/>");
						return;
					}
					
					markInput(urlValue, true);
				}
				
				document.forms.createForm.submit();
			});
		});
		</script>