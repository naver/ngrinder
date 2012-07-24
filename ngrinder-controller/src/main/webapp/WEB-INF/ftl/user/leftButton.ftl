<div class="span2">

	<div class="well" style="margin-top:20px;">
        <ul class="nav nav-list">
			<li class="nav-header <#if listPage?exists && !roleName?exists>active</#if>">
				<a href="${req.getContextPath()}/user/list">ALL</a>
			</li>
        	<#list roleSet as role>
			<li class="nav-header <#if roleName?exists && role.fullName == roleName>active</#if>">
				<a href="${req.getContextPath()}/user/list?roleName=${role.fullName}">${role.fullName}</a>
			</li>
			</#list>
        </ul>
	</div><!--/.well -->
</div>