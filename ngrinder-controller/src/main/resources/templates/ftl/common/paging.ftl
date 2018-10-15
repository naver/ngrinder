<#import "spring.ftl" as spring/>
<#macro paging totalCount pageNo countPerPage displayPageCount action>
	<#if totalCount ==0 || countPerPage == 0 || displayPageCount == 0 >
	<div class="paginate">&nbsp;</div>
		<#return>
	</#if>

	<#assign totalPage = (totalCount/countPerPage)?int>
	<#if totalCount % countPerPage != 0 >
		<#assign totalPage = totalPage + 1 >
	</#if>

	<#if totalPage == 1 >
		<#return>
	</#if>
	<#if ( pageNo lt 1 ) >
		<#assign pageNo = 1 >
	</#if>

	<#if ( pageNo > totalPage ) >
		<#assign pageNo = totalPage >
	</#if>

	<#assign phraseIndex =    (pageNo / displayPageCount)?int >
	<#if (pageNo % displayPageCount) != 0 >
		<#assign phraseIndex = phraseIndex + 1 >
	</#if>

	<#assign startPage = ( phraseIndex - 1 ) * displayPageCount + 1 >
	<#assign endPage =    phraseIndex * displayPageCount >

	<#if (endPage > totalPage) >
		<#assign endPage =    totalPage >
	</#if>
<div class="dataTables_paginate pagination">
	<ul>
		<li <#if pageNo == 1> class="disabled"</#if>>
			<a href="javascript:<#if pageNo == 1>void(0)<#else>doSubmit('${pageNo - 1}')</#if>">&larr; <@spring.message "common.paging.previous"/></a>
		</li>
		<#list startPage..endPage as i>
			<#if i == pageNo >
				<li class="active"><a class="pointer-cursor">${i}</a></li>
			<#else>
				<li><a href="javascript:doSubmit('${i}')">${i}</a></li>
			</#if>
		</#list>
		<li <#if pageNo == totalPage> class="disabled"</#if>>
			<a href="javascript:<#if pageNo == totalPage>void(0)<#else>doSubmit('${pageNo + 1}')</#if>"><@spring.message "common.paging.next"/> &rarr;</a>
		</li>

	</ul>
</div>
</#macro>