<div class="modal hide fade" id="recorder_guide_modal" tabindex="-1" role="dialog"  aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
		<h4>nGrinder Recorder Install Guide</h4>
	</div>
	<div class="modal-body">
		<div>
			<p>Follow below step in Chrome browser:</p>
			<ol>
				<li>
					nGrinder Recorder <a href="${req.getContextPath()}/crx/ngrinder-recorder-1.0.crx">download here</a>
				</li>
				<li>
					Move to the Extensions management page by this URL: <b>chrome://extensions</b><br>
					(other way : Chrome menu -> Tools -> Extensions)
				</li>
				<li>
					Drags the <b>ngrinder-recorder.crx</b> file into the Extensions management page
				</li>
				<li>
					Accept in installing
				</li>
				<li>
					If completed, you will be able to see nGrinder Recorder Extension icon!
				</li>
			</ol>
			<p>If you want a how to usage, <a href="https://github.com/naver/ngrinder/tree/master/ngrinder-controller#ngrinder-recorder-chrome-extension">show nGrinder Recorder READMD.md</a></p>
		</div>
	</div>

	<div class="modal-footer">
		<button class="btn"  data-dismiss="modal"><@spring.message "common.button.cancel"/></button>
	</div>
</div>