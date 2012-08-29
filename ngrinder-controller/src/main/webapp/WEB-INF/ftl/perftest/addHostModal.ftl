
	<!-- modal -->
	<div class="modal fade" id="addHostModal">
		<div class="modal-header">
			<a class="close" data-dismiss="modal">&times;</a>
			<h3>
				Add Host <small>Please input one option at least.</small>
			</h3>
		</div>
		<div class="modal-body">
			<div class="form-horizontal">
				<fieldset>
					<div class="control-group">
						<label for="domainInput" class="control-label">Domain</label>
						<div class="controls">
							<input type="text" id="domainInput"> <span class="help-inline"></span>
						</div>
					</div>
					<div class="control-group">
						<label for="ipInput" class="control-label">IP</label>
						<div class="controls">
							<input type="text" id="ipInput"> <span class="help-inline"></span>
						</div>
					</div>
				</fieldset>
			</div>
		</div>
		<div class="modal-footer">
			<a class="btn btn-primary" id="addHostBtn"><@spring.message "perfTest.configuration.add"/></a>
		</div>
	</div>
