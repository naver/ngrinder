<div class="modal hide fade" id="create_folder_modal" tabindex="-1" role="dialog"  aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-header">
    	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
		<h4><@spring.message "script.list.button.createFolder"/></h4>
	</div>
	<div class="modal-body">
		<form class="form-horizontal form-horizontal-4" method="post" target="_self" id="createFolderForm" action="${req.getContextPath()}/script/new/${currentPath}">
			<fieldset>
				<div class="control-group">
					<label for="folder_name_input" class="control-label">
						<@spring.message "script.list.label.folderName"/>
					</label>
					<div class="controls"> 
					  <#assign content_message>
					  	<@spring.message "common.form.rule.sampleName"/>
					  </#assign>
					  <input type="hidden" name="type" value="folder"/>
					  <input type="text" id="folder_name_input" name="folderName"
					  		class="span2" 
					  		rel='create_folder_modal_popover'
							title='<@spring.message "script.list.label.folderName"/>'
							data-html='true'
							data-placement="right"
					  		data-content="${content_message?js_string}"
					  />
					  <span class="help-inline"></span>
					</div>
				</div>					
			</fieldset> 
		</form>
	</div>
	
	<div class="modal-footer">
		<button class="btn btn-primary" id="create_folder_button"><@spring.message "common.button.create"/></button>
		<button class="btn"  data-dismiss="modal"><@spring.message "common.button.cancel"/></button>
	</div>
</div>
<script>
	$(document).ready(function() {
		$("input[rel='create_folder_modal_popover']").popover({trigger: 'focus', container:'#create_folder_modal'});
		$("#create_folder_button").click(function() {
			var $name = $("#folder_name_input");
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