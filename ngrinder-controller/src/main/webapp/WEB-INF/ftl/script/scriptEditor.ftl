<!DOCTYPE html>
<html>
	<head>	
		<#include "../common/common.ftl">
		<title><@spring.message "script.editor.title"/></title>
		<style>
			div.div-host {
				background-color: #FFFFFF;
				border: 1px solid #D6D6D6;
				height: 63px;
				overflow-y: scroll;
				border-radius: 3px 3px 3px 3px;
				width:250px;
			}

			div.div-host .host {
				color: #666666;
				display: inline-block;
				margin-left: 7px;
				margin-top: 2px;
				margin-bottom: 2px;
			}
			
			.addhostbtn {
				margin-top:-25px;
				margin-right:67px 
			}
		</style>
	</head>

	<body>
	<#include "../common/navigator.ftl">
	<div class="container">
		<div class="row">
			<div class="span12">
				<form id="contentForm" method="post" target="_self" style="margin-bottom: 0px;"> 	
					<div class="well" style="margin-bottom: 10px;">
						<div class="form-horizontal">
							<fieldset>
								<div class="control-group">
										<table style="width:100%">
											<colgroup>
												<col width="150px"/>
												<col width="*"/>
												<col width="300px"/>
											</colgroup>
											<tr>
												<td>
													<label class="control-label" for="testName"><@spring.message "script.option.name"/></label>
												</td>
												<td>
													<input type="text" id="scriptNameInput" class="span6" name="path" value="${(file.path)!}" readonly/>
												</td>
												<td>
													<a class="btn btn-info" href="#scriptSampleModal" data-toggle="modal" id="sampleBtn" style="margin-left:0; width:48px;">Sample</a>
													<a class="btn btn-success" href="javascript:void(0);" id="saveBtn" style="width:39px;"><@spring.message "common.button.save"/></a>
													<a class="btn btn-primary" href="javascript:void(0);" id="validateBtn" style="width:90px;"><@spring.message "script.editor.button.validate"/></a>
												</td>
											</tr> 
										</table>
								</div>
								<div style="margin-bottom: 0" class="control-group">
									<table style="width:100%"> 
										<colgroup> 
											<col width="150px"/>
											<col width="*"/> 
											<col width="300px"/>
										</colgroup>
										<tr>
											<td>
												<label class="control-label" for="description"><@spring.message "script.option.commit"/></label>
											</td>
											<td>
												<textarea class="span6" id="descInput" name="description" style="resize:none; height:55px" >${(file.description)!}</textarea>
											</td> 
											<td>  
												<#include "../perftest/host.ftl"/>
											</td> 
										</tr>
									</table>           
								</div>
							</fieldset>
						</div>
					</div>
					
					<input type="hidden" id="createLibAndResource" name="createLibAndResource" value="<#if createLibAndResource?? && createLibAndResource==true>true<#else>false</#if>"/>
					<@security.authorize ifAnyGranted="A, S">
						<#if ownerId??>					
							<input type="hidden" id="ownerId" name="ownerId" value="${ownerId}"/>
						</#if>
					</@security.authorize>
					<textarea id="codemirrorContent" name="content">${(file.content)!}</textarea>
				</form>
				<div class="pull-right" rel="popover" style="position:float;margin-top:-20px;margin-right:-30px" data-original-title="Tip" data-content="
			      Ctrl-F / Cmd-F : <@spring.message "script.editor.tip.startSearching"/>&lt;br&gt; 
			      Ctrl-G / Cmd-G : <@spring.message "script.editor.tip.findNext"/>&lt;br&gt;
			      Shift-Ctrl-G / Shift-Cmd-G : <@spring.message "script.editor.tip.findPrev"/>&lt;br&gt;
			      Shift-Ctrl-F / Cmd-Option-F : <@spring.message "script.editor.tip.replace"/>&lt;br&gt;
			      Shift-Ctrl-R / Shift-Cmd-Option-F : <@spring.message "script.editor.tip.replaceAll"/>&lt;br&gt;
			      F12 : <@spring.message "script.editor.tip.fullScreen"/>&lt;br&gt;
			      ESC : <@spring.message "script.editor.tip.back"/>&lt;br&gt;
			      " placement="left"
			    ><code>Tip</code></div> 
			    
				<pre style="height:100px; margin-top:5px;" class="prettyprint pre-scrollable hidden" id="validateRsPre">
				</pre>
			</div>
		</div>
		<div class="modal fade" id="scriptSampleModal">
			<div class="modal-header">
				<a class="close" data-dismiss="modal" id="createCloseBtn">&times;</a>
				<h3>Script Sample List</h3>
			</div>
			<div class="modal-body">
				<div class="alert" style="margin-bottom:5px">
					<!--<button type="button" class="close" data-dismiss="alert">&times;</button>-->
					<strong>Warning!</strong> Click sample will use its content to rewrite your script editor.
				</div>
				<table class="table table-striped table-bordered ellipsis" id="sampleTable" style="width: 100%">
					<colgroup>
						<col width="180"> 
						<col>
					</colgroup> 
					<thead>
						<tr>
							<th><@spring.message "perfTest.table.scriptName"/></th>
							<th class="noClick"><@spring.message "common.label.description"/></th>
						</tr>
					</thead>
					<tbody>
						<!-- demo -->
						<tr>
							<td  class="ellipsis">
								<a href="javascript:void(0);" class="sample" title="Sample1.py" sname="Sample1.py">Sample1.py</a>
							</td>
							<td class="ellipsis" title="This is a demo.">This is a demo.</td>
						</tr>
						<#if sampleFiles?has_content>
							<#list sampleFiles as script>
								<tr>
									<td class="ellipsis" title="${script.fileName}">
										<a href="${req.getContextPath()}/script/detail/${script.path}" target="_self">${script.fileName}</a>
									</td>
									<td class="ellipsis" title="${(script.description)!}">${(script.description)!}</td>
								</tr>
							</#list>
						<#else>
							<tr>
								<td colspan="2" class="center">
									<@spring.message "common.message.noData"/>
								</td>
							</tr>
						</#if>		
					</tbody>
				</table>
			</div>
		</div>
		<#include "../common/copyright.ftl">
	</div>
	
	<#include "../common/codemirror.ftl">
	<script src="${req.getContextPath()}/plugins/codemirror/lang/python.js"></script>
    <#include "../common/datatables.ftl">
    <script>
    	$(document).ready(function() {
			var editor = CodeMirror.fromTextArea(document.getElementById("codemirrorContent"), {
			   mode: "python",
			   theme: "eclipse",
			   lineNumbers: true,
			   lineWrapping: true,
			   indentUnit:4,
			   tabSize:4,
			   indentWithTabs:true,
			   smartIndent:false,
			   extraKeys: {
		         "F11": function(cm) {
		           setFullScreen(cm, !isFullScreen(cm));
		         },
		         "Esc": function(cm) {
		           if (isFullScreen(cm)) setFullScreen(cm, false);
		         },
		         Tab: "indentMore"
		       },
			   onCursorActivity: function() {
			     editor.setLineClass(hlLine, null, null);
			     hlLine = editor.setLineClass(editor.getCursor().line, null, "activeline");
			   }
			});
			var hlLine = editor.setLineClass(0, "activeline");

			$("#saveBtn").click(function() {
				$('#codemirrorContent').text(editor.getValue());
				document.forms.contentForm.action = "${req.getContextPath()}/script/save";
				document.forms.contentForm.submit();
			});

			$("#validateBtn").click(function() {
				showInformation("<@spring.message "script.editor.message.validate"/>");
				var scriptPath = $("#scriptNameInput").val();
				var hostString = $("#hostsHidden").val();
				$('#validateRsPre').hide();
				$.ajax({
			  		url: "${req.getContextPath()}/script/validate",
			    	async: true,
			    	type: "POST",
					data: {'path':scriptPath, 'content': editor.getValue(), 'hostString' : hostString
						<@security.authorize ifAnyGranted="A, S">
							<#if ownerId??>	
				  				, 'ownerId': "${ownerId}"
							</#if>
						</@security.authorize>
					},
			    	success: function(res) {
						$('#validateRsPre').text(res);
						$('#validateRsPre').show();
			    	},
			    	error: function() {
			    		showErrorMsg("<@spring.message "script.editor.error.validate"/>");
			    	}
			  	});
			});
			
			$("a.sample").click(function() {
				var sampleName = $(this).attr("sname");
				editor.setValue(sampleName + " content.");
				$("#scriptSampleModal").modal("hide");
				return true;
				$.ajax({
			  		url: "${req.getContextPath()}/script/sample",
			    	async: true,
			    	type: "POST",
					data: {'sampleName': sampleName},
			    	success: function(res) {
						editor.setValue(res);
						$("#scriptSampleModal").modal("hide");
			    	},
			    	error: function() {
			    		showErrorMsg("Copy script sample error.");
			    	}
			  	});
			});
			
	      $("#contentForm").validate({
	          rules: {
	          },
	          messages: {
	              
	          },
	          ignore: "", //make the validation on hidden input work
	          errorClass: "help-inline",
	          errorElement: "span",
	          errorPlacement: function (error, element) {
	              if (element.next().attr("class") == "add-on") {
	                  error.insertAfter(element.next());
	              } else {
	                  error.insertAfter(element);
	              }
	          },
	          highlight: function (element, errorClass, validClass) {
	              $(element).parents('.control-group').addClass('error');
	              $(element).parents('.control-group').removeClass('success');
	          },
	          unhighlight: function (element, errorClass, validClass) {
	              $(element).parents('.control-group').removeClass('error');
	              $(element).parents('.control-group').addClass('success');
	          }
	      });
	      
	      <#if sampleFiles?has_content>
			$("#sampleTable").dataTable({
				"bAutoWidth": false,
				"bFilter": false,
				"bLengthChange": false,
				"bInfo": false,
				"iDisplayLength": 5, 
				"aaSorting": [],
				"aoColumns": [null, {"asSorting": []}],
				"sPaginationType": "bootstrap",
				"oLanguage": {
					"oPaginate": {
						"sPrevious": "<@spring.message "common.paging.previous"/>",
						"sNext": "<@spring.message "common.paging.next"/>"
					}
				}
			});
			$(".noClick").off('click');
	      </#if>
		});
		</script>
	</body>
</html>
