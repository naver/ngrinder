<!DOCTYPE html>
<html>
	<head>
		<#include "../common/common.ftl">
		<#include "../common/datatables.ftl">
		<title><@spring.message "script.list.title"/></title>
	</head>

	<body>
    <#include "../common/navigator.ftl">
	<div class="container">
		<img src="${req.getContextPath()}/img/bg_script_banner_en.png?${nGrinderVersion}"/>
		<div class="well form-inline search-bar" style="margin-top:0px" > 
			<table style="width:100%">
				<tr>
					<td>
						<table style="width:100%">
							<colgroup>
								<col width="400px"/>
								<col width="*"/>
							</colgroup>
							<tr>
								<td>
									<input type="text" class="search-query span3" placeholder="Keywords" id="searchText" value="${query!}">
									<button type="submit" class="btn" id="search_btn"><i class="icon-search"></i> <@spring.message "common.button.search"/></button>
								</td>
								<td>
								<#if svnUrl?has_content>
									<div class="input-prepend pull-right" rel="popover" 
										title="Subversion" data-placement="bottom"
										data-content='<@spring.message "script.list.message.svn"/>'
										data-html="true">
										<span class="add-on" style="cursor:default">SVN</span><span class="input-xlarge uneditable-input span7" style="cursor:text">${svnUrl}</span>
						        	</div>  
					        	</#if>
				        		</td>
				        	</tr>
			        	</table>
					</td>
				</tr>
				<tr>
			     	<td>
						<table  style="width:100%; margin-top:5px">
							<colgroup>
								<col width="600px"/>
								<col width="340px"/>
							</colgroup>
							<tr>
								<td>
									<#if !(query??)>
										<a class="btn btn-primary" href="#create_script_modal" data-toggle="modal">
											<i class="icon-file icon-white"></i>
											<@spring.message "script.list.button.createScript"/>
										</a>
										<a class="btn" href="#create_folder_modal" data-toggle="modal">
											<i class=" icon-folder-open"></i>
											<@spring.message "script.list.button.createFolder"/>
										</a>
										<a class="btn" href="#upload_file_modal" data-toggle="modal">
											<i class="icon-upload"></i>
											<@spring.message "script.list.label.upload"/>
										</a>
									</#if>
								</td>
								<td>
									<a class="pointer-cursor btn btn-danger pull-right" id="delete_script_button">
										<i class="icon-remove icon-white"></i>
										<@spring.message "script.list.button.delete"/>
									</a>
								</td> 
							</tr>
						</table>
					</td>
				</tr>
			</table>	
		</div>
		
		<table class="table table-striped table-bordered ellipsis" id="script_list_table" style="width:940px">
			<colgroup>
				<col width="30">
				<col width="32">
				<col width="230"> 
				<col>
				<col width="150">
				<col width="80">
				<col width="80">
				<col width="80">
			</colgroup> 
			<thead>
				<tr>
					<th><input type="checkbox" class="checkbox" value=""></th>
					<th class="no-click"><a href="${req.getContextPath()}/script/list/${currentPath}/../" target="_self"><img src="${req.getContextPath()}/img/up_folder.png"/></a> 
					</th>
					<th><@spring.message "script.list.table.name"/></th>
					<th class="no-click"><@spring.message "script.option.commit"/></th>
					<th><@spring.message "script.list.table.lastDate"/></th>
					<th><@spring.message "script.list.table.revision"/></th>
					<th><@spring.message "script.list.table.size"/></th>
					<th class="no-click"><@spring.message "script.list.table.download"/></th>
				</tr>
			</thead>
			<tbody>
				<@list list_items=files ; script>
					<tr>
						<td><#if script.fileName != ".."><input type="checkbox" class="checkbox"  value="${script.fileName}"></#if></td>
						<td>
							<#if script.fileType.fileCategory.isEditable()>
								<i class="icon-file"></i>
							<#elseif script.fileType == "dir">
								<i class="icon-folder-open"></i>
							<#else>
								<i class="icon-briefcase"></i>
							</#if>
						</td>
						<td class="ellipsis">
							<#if script.fileType.fileCategory.isEditable()>
								<a href="${req.getContextPath()}/script/detail/${script.path}" target="_self" title="${script.path}">${script.fileName}</a>
							<#elseif script.fileType == "dir">
								<a href="${req.getContextPath()}/script/list/${script.path}" target="_self" title="${script.path}">${script.fileName}</a>
							<#else>
								<a href="${req.getContextPath()}/script/download/${script.path}" target="_blank" title="${script.path}">${script.fileName}</a>
							</#if>
						</td>
						<td class="ellipsis" title="${(script.description)!?html}">${(script.description)!}</td>
						<td><#if script.lastModifiedDate?exists>${script.lastModifiedDate?string('yyyy-MM-dd HH:mm')}</#if></td>
						<td>${script.revision}</td>
						<td>
							<#if script.fileType != "dir">
								<#assign floatSize = script.fileSize?number/1024>${floatSize?string("0.##")}
							</#if>
						</td>
						<td class="center">
							<#if script.fileType != "dir">
								<i class="pointer-cursor icon-download-alt script-download" spath="${script.path}"></i>
							</#if>
						</td>
					</tr>
				</@list>
			</tbody>
		</table>
	<#include "../common/copyright.ftl">
	</div>
	<#if !(query??)>
	<#include "create_script_modal.ftl">
	<#include "create_folder_modal.ftl">
	<#include "upload_file_modal.ftl">
	</#if>
	
	<script type="text/javascript">
		$(document).ready(function() {
			$("#nav_script").addClass("active");
			$("#delete_script_button").click(function() {
				var list = $("td input:checked");
				if(list.length == 0) {
					bootbox.alert("<@spring.message "script.list.alert.delete"/>", "<@spring.message "common.button.ok"/>");
					return;
				}
	      		bootbox.confirm("<@spring.message "script.list.confirm.delete"/>", "<@spring.message "common.button.cancel"/>", "<@spring.message "common.button.ok"/>", function(result) {
					if (result) {
						var scriptsStr = list.map(function() {
							return $(this).val();
						}).get().join(",");

						var ajaxObj = new AjaxObj("${req.getContextPath()}/script/delete/${currentPath}");
						ajaxObj.type = "POST"
						ajaxObj.params = {'filesString': scriptsStr};
						ajaxObj.success = function (res) {
							document.location.reload();
						};
						ajaxObj.call();
					}
				});
			});
			
			$("#search_btn").on('click', function() {
				searchScriptList();
			});
			
			enableChkboxSelectAll("script_list_table");
			
			$("i.script-download").on('click', function() {
				window.location  = "${req.getContextPath()}/script/download/" + $(this).attr("spath");
			});

			<#if files?has_content>
				$("#script_list_table").dataTable({
					"bAutoWidth": false,
					"bFilter": false,
					"bLengthChange": false,
					"bInfo": false,
					"iDisplayLength": 10, 
					"aaSorting": [],
					"aoColumns": [{"asSorting": []}, {"asSorting": []}, null, {"asSorting": []}, null, null, null, {"asSorting": []}],
					"sPaginationType": "bootstrap",
					"oLanguage": {
						"oPaginate": {
							"sPrevious": "<@spring.message "common.paging.previous"/>",
							"sNext":     "<@spring.message "common.paging.next"/>"
						}
					}
				});
				$(".no-click").off('click');
			</#if>
		});
		
		function searchScriptList() {
			document.location.href = "${req.getContextPath()}/script/search?query=" + $("#searchText").val();
		}
	</script>
	</body>
</html>
