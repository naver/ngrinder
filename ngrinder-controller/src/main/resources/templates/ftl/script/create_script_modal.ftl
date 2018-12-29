<div class="modal hide fade modal-lg" id="create_script_modal" tabindex="-1" role="dialog"  aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
		<div><h4><@spring.message "script.action.createScript"/></h4></div>
		<div style="float;margin-top:-35px;padding-left:753px"
			title="Sample Script Link" data-html="ture"
			data-placement="left"
			data-content="<@spring.message 'script.editor.sample.message'/>">
			<code><a target="_blank" href="https://github.com/naver/ngrinder/tree/master/script-sample">Script Samples</a></code>
		</div>
	</div>
	<div class="modal-body" style="max-height:500px;">
		<form class="form-horizontal form-horizontal-4" method="post" target="_self" id="createForm" action="${req.getContextPath()}/script/new/${currentPath}">
			<fieldset style="padding-left: 50px;">

				<@control_group name="fileName" inline_help="true" label_message_key="script.info.name">
					<#assign name_message>
						<@spring.message "script.info.name.help"/>
					</#assign>
					
					<select id="script_type" name="scriptType" class="span2">
						<#list handlers as handler>
							<option value="${handler.key}" extension="${handler.extension}" project_handler="${handler.isProjectHandler()?string}">${handler.title}</option>
						</#list>
					</select>

					<@input_popover name="fileName" rel="create_script_modal_popover"
						data_placement="right"
						message="script.info.name"
						message_content="${name_message?js_string}"
						extra_css="input-large span5" />
					<input type="hidden" name="type" value="script"/>
				</@control_group>

				<@control_group name="testUrl" inline_help="true" label_message_key="script.info.url">
					<#assign url_message>
						<@spring.message "home.tip.url.content"/>
					</#assign>

					<select id="method" name="method" class="span2">
						<option value="GET">GET</option>
						<option value="POST">POST</option>
					</select>

					<@input_popover name="testUrl" rel="create_script_modal_popover"
						data_placement="bottom"
						message="home.tip.url.title"
						message_content="${url_message}"
						placeholder="home.placeholder.url"
						extra_css="input-large span5 test-url" />
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
			<div class="text-center">
				<span>
					<a id="detail_config_section_btn" class="pointer-cursor">
						<@spring.message "perfTest.config.showAdvancedConfig"/>
					</a>
				</span>
			</div>
			<input id="options" type="hidden" name="options">
			<div id="detail_config_section" class="well hide">
				<#include "../common/script_option.ftl">
			</div>
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
			$("#options").val(options.toJson($("#method").val()));
			document.forms.createForm.submit();
		});
		$("#detail_config_section_btn").click(function() {
			$("#detail_config_section").toggle();
		});
		$("#method").change(function(e) {
			var methodName = $(e.target).val();
			changeHTTPMethod(methodName);
		});
	});
</script>
