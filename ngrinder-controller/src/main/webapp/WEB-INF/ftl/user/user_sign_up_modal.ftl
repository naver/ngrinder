<style>
	.modal.fade.in {
		top: 2%;
	}
</style>
<#import "../common/spring.ftl" as spring/>
<div class="modal hide fade" id="user_sign_up_modal" >
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		<h4><@spring.message "user.detail.header"/></h4>
	</div>

	<div class="modal-body" id="create_form_div" style="max-height:740px;">
		<#include "info.ftl">
	</div>

	<div class="modal-footer">
		<a class="btn" data-dismiss="modal" aria-hidden="true" id="cancel_ensemble_member_btn"><@spring.message "common.button.cancel"/></a>
	</div>
</div>
<script>
	$(document).ready(function() {
	});
		
</script>
