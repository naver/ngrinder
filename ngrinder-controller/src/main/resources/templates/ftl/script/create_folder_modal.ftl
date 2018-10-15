<div class="modal hide fade" id="create_folder_modal" tabindex="-1" role="dialog"  aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-header">
    	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
		<h4><@spring.message "script.action.createFolder"/></h4>
	</div>
	<div class="modal-body">
		<form class="form-horizontal form-horizontal-4" method="post" target="_self" id="createFolderForm" onsubmit="return false;" action="${req.getContextPath()}/script/new/${currentPath}">
			<fieldset>
				<@control_group name="folderName" inline_help="true" label_message_key="script.info.folderName">
					<#assign content_message>
						<@spring.message "script.info.folderName.help"/>
					</#assign>
					<input type="hidden" name="type" value="folder"/>
					<@input_popover name="folderName" rel="create_folder_modal_popover"
						data_placement="right"
						message="script.info.folderName"
						message_content="${content_message?js_string}"
						extra_css="input-medium" />
				</@control_group>
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
			var $name = $("#folder_name");
			if (checkEmptyByObj($name)) {
				markInput($name, false, "<@spring.message "common.message.validate.empty"/>");
				return;
			} else {
				if (!checkSimpleNameByObj($name)) {
					markInput($name, false, "<@spring.message "common.message.validate.format"/>");
					return;
				}
				markInput($name, true);
			}
			document.forms.createFolderForm.submit();
		});
	});
</script>