<div class="modal hide fade" id="create_script_modal" tabindex="-1" role="dialog"  aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
		<h4><@spring.message "script.list.button.createScript"/></h4>
	</div>
	<div class="modal-body">
		<form class="form-horizontal form-horizontal-4" method="post" target="_self" id="createForm" action="${req.getContextPath()}/script/new/${currentPath}">
			<fieldset>
				<div class="control-group">
					<label for="script_name_input" class="control-label"><@spring.message "script.option.name"/></label>
					<div class="controls">
						<#assign sample_name_message>
						  	<@spring.message "common.form.rule.sampleName"/>
						</#assign>	
					  	<input type="text" id="script_name_input" name="fileName"
					  		class="input-medium" 
					  		rel="create_script_modal_popover"
					  		data-html="true"
					  		data-content="${sample_name_message?html}"
							title="<@spring.message "script.option.name"/>">
							
					  <span class="help-inline"></span>
					</div>
				</div>
				<div class="control-group">
					<label for="language_select" class="control-label"><@spring.message "script.list.label.type"/></label>
					<div class="controls">
						<input type="hidden" name="type" value="script"/>
						<select id="language_select" name="scriptType">
							<#list handlers as handler>
							<option value="${handler.key}" extension="${handler.extension}" project_handler="${handler.isProjectHandler()?string}">${handler.title}</option>
							</#list>
						</select> 
					  <span class="help-inline"></span>
					</div>
				</div>
				<div class="control-group">
					<label for="url_input" class="control-label"><@spring.message "script.list.label.url"/></label>
					<div class="controls">
					  <input type="text" id="url_input" class="url" 
					         placeholder='<@spring.message "home.placeholder.url"/>' 
					         class="input-medium" 
					         name="testUrl"
					         rel="create_script_modal_popover"
					  		 data-html="true"
					         title='<@spring.message "home.tip.url.title"/>'
					         data-content="<@spring.message "home.tip.url.content"/>"/>
					  <span class="help-inline"></span>
					</div>
				</div>
				<div class="control-group">
					<div class="controls">
						<label class="checkbox">
					    <input type="checkbox" id="create_lib_and_resource" 
					         name="createLibAndResource"
					         rel="create_script_modal_popover"
					  		 data-html="true" 
					         title='<@spring.message "script.list.label.createResourceAndLib"/>' 
					         data-content='<@spring.message "script.tip.libAndResource"/>'/> 
					         <@spring.message "script.list.label.createResourceAndLib"/>
					  	</label>
					  <span class="help-inline well"><@spring.message "script.list.label.createResourceAndLib.help"/>
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
		$("#script_name_input").val("");
		$("#url_input").val("");
		$("#create_script_btn").on('click', function() {
			var $name = $("#script_name_input");
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
			var $selectedElement = $("#language_select option:selected");
			var extension = $selectedElement.attr("extension").toLowerCase();
			var projectHandler = $selectedElement.attr("project_handler");
			if (projectHandler != "true") {
				extension = "." + extension;
				var idx = name.toLowerCase().lastIndexOf(extension);
				if (idx == -1) {
					$name.val(name + extension);
				}
				var urlValue = $("#url_input");
				if (urlValue.val() == "Type URL...") {
					$("#url_input").val("");
				}
				if (!checkEmptyByObj(urlValue)) {
					if (!urlValue.valid()) {
						markInput(urlValue, false, "<@spring.message "common.form.validate.format"/>");
						return;
					}
					markInput(urlValue, true);
				}
			}
			document.forms.createForm.submit();
		});
	});
</script>
