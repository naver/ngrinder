<div class="modal hide fade" id="create_script_modal" tabindex="-1" role="dialog"  aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
		<h4><@spring.message "script.action.createScript"/></h4>
	</div>
	<div class="modal-body">
		<form class="form-horizontal form-horizontal-4" method="post" target="_self" id="createForm" action="${req.getContextPath()}/script/new/${currentPath}">
			<fieldset>

				<@control_group name="fileName" inline_help="true" label_message_key="script.info.name">
					<#assign name_message>
						<@spring.message "script.info.name.help"/>
					</#assign>

					<@input_popover name="fileName" rel="create_script_modal_popover"
						data_placement="right"
						message="script.info.name"
						message_content="${name_message?js_string}"
						extra_css="input-large" />
				</@control_group>

				<@control_group name="scriptType" inline_help="true" label_message_key="script.info.type">
					<input type="hidden" name="type" value="script"/>
					<select id="script_type" name="scriptType">
						<#list handlers as handler>
							<option value="${handler.key}" extension="${handler.extension}" project_handler="${handler.isProjectHandler()?string}">${handler.title}</option>
						</#list>
					</select>
				</@control_group>

				<@control_group name="testUrl" inline_help="true" label_message_key="script.info.url">
					<#assign url_message>
						<@spring.message "home.tip.url.content"/>
					</#assign>

					<@input_popover name="testUrl" rel="create_script_modal_popover"
						data_placement="right"
						message="home.tip.url.title"
						message_content="${url_message}"
						placeholder="home.placeholder.url"
						extra_css="input-large" />
				</@control_group>

				<div class="control-group">
					<div class="controls">
						<label class="checkbox">
						<#assign lib_message>
							<@spring.message "script.message.libAndResource"/>
						</#assign>

						<@input_popover name="createLibAndResource"
							rel="create_script_modal_popover"
							data_placement="right"
							type="checkbox"
							message="script.action.createResourceAndLib"
							message_content="${lib_message}"
							extra_css="input-medium" />
							<@spring.message "script.action.createResourceAndLib"/>
						</label>
						<span class="help-inline well"><@spring.message "script.action.createResourceAndLib.help"/>
						<a href="http://www.cubrid.org/wiki_ngrinder/entry/how-to-use-lib-and-resources" target="blank"><i class="icon-question-sign" style="margin-top:2px"></i></a>
						</span>
					</div> 
				</div>
			</fieldset>
		</form>
	</div>
	
	<div class="modal-footer">
		<button class="btn btn-primary" id="create_script_btn"><@spring.message "common.button.create"/></button>
		<button class="btn" data-dismiss="modal"><@spring.message "common.button.cancel"/></button>
	</div>
</div>
<script>
	$(document).ready(function() {
		$("input[rel='create_script_modal_popover']").popover({trigger: 'focus', container:'#create_script_modal'});
		$("#file_name").val("");
		$("#test_url").val("");
		$("#create_script_btn").on('click', function() {
			var $name = $("#file_name");
			if (checkEmptyByObj($name)) {
				markInput($name, false, "<@spring.message "common.message.validate.format"/>");
				return;
			} else {
				if (!checkSimpleNameByObj($name)) {
					markInput($name, false, "<@spring.message "common.message.validate.format"/>");
					return;
				}
				
				markInput($name, true);
			}
			
			var name = $name.val();
			var $selectedElement = $("#script_type").find("option:selected");
			var extension = $selectedElement.attr("extension").toLowerCase();
			var projectHandler = $selectedElement.attr("project_handler");
			if (projectHandler != "true") {
				extension = "." + extension;
				var idx = name.toLowerCase().lastIndexOf(extension);
				if (idx == -1) {
					$name.val(name + extension);
				}
				var $testUrl = $("#test_url");
				if ($testUrl.val() == "Type URL...") {
					$testUrl.val("");
				}
				if (!checkEmptyByObj($testUrl)) {
					if (!$testUrl.valid()) {
						markInput($testUrl, false, "<@spring.message "common.form.validate.format"/>");
						return;
					}
					markInput($testUrl, true);
				}
			}
			document.forms.createForm.submit();
		});
	});
</script>
