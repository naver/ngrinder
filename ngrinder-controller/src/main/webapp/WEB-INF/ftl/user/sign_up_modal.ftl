<#setting number_format="computer">
<style>
    .modal.fade.in {
        top: 2%;
    }
</style>
<#import "../common/spring.ftl" as spring/>
<div class="modal hide fade" id="sign_up_modal">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4><@spring.message "user.signup.header"/></h4>
    </div>

    <div class="modal-body" id="create_form_div" style="max-height:740px;padding-left:30px">
		<#assign basePath>sign_up</#assign>
		<#include "info.ftl">
    </div>
</div>
