<!DOCTYPE html>
<html>
	<head>	
		<#include "../common/common.ftl">
		<title><@spring.message "script.editor.title"/></title>
		<style>

			div.div-host {
				background-color: #FFFFFF;
				border: 1px solid #D6D6D6;
				height: 55px;
				overflow-y: scroll;
				border-radius: 3px 3px 3px 3px;
				width:250px; 
				margin-bottom:-5px;
			}

			div.div-host .host {
				color: #666666;
				display: inline-block;
				margin-left: 7px;
				margin-top: 2px;
				margin-bottom: 2px;
			}
			
			.addhostbtn {
				margin-top:-20px;
				margin-right:70px 
			}
			.CodeMirror-scroll {
			    height: 500px !important;   
			}
		</style>
	</head>

	<body>
	<#include "../common/navigator.ftl">
	<div class="container">
		<div class="row">
			<div class="span12">
				<form id="contentForm" method="post" target="_self" style="margin-bottom: 0px;"> 	
					<div class="well" style="margin-bottom: 0px;">
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
													<input type="text" id="scriptNameInput" class="span7" name="path" value="${(file.path)!}" readonly/>
												</td>
												<td>
													<a class="btn btn-success" href="javascript:void(0);" id="saveBtn" style="margin-left:27px; width:35px;"><@spring.message "common.button.save"/></a>
													<a class="btn btn-primary" href="javascript:void(0);" id="validateBtn" style="width:85px;"><@spring.message "script.editor.button.validate"/></a>
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
												<textarea class="input-xlarge span6" id="descInput" rows="3" name="description" style="resize: none" >${(file.description)!}</textarea>
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
					<input type="hidden" id="contentHidden" name="content" value=""/>
				</form>
				<textarea id="codemirrorContent" style="position:relative;width:940px;margin-top:0px">${(file.content)!}</textarea>
				<div class="pull-right" rel="popover" data-original-title="Tip" data-content="
			      Ctrl-F / Cmd-F : <@spring.message "script.editor.tip.startSearching"/>&lt;br&gt;
			      Ctrl-G / Cmd-G : <@spring.message "script.editor.tip.findNext"/>&lt;br&gt;
			      Shift-Ctrl-G / Shift-Cmd-G : <@spring.message "script.editor.tip.findPrev"/>&lt;br&gt;
			      Shift-Ctrl-F / Cmd-Option-F : <@spring.message "script.editor.tip.replace"/>&lt;br&gt;
			      Shift-Ctrl-R / Shift-Cmd-Option-F : <@spring.message "script.editor.tip.replaceAll"/>&lt;br&gt;
			      F12 : <@spring.message "script.editor.tip.fullScreen"/>&lt;br&gt;
			      ESC : <@spring.message "script.editor.tip.back"/>&lt;br&gt;
			      " placement="top"
			    ><code>Tip</code></div> 
			    
				<pre style="height:100px; margin-top:5px;" class="prettyprint pre-scrollable hidden" id="validateRsPre">
				</div>
			</div>
			<#include "../common/copyright.ftl">	
		</div>
		
	</div>
	<script src="${req.getContextPath()}/js/codemirror/codemirror.js" type="text/javascript" charset="utf-8"></script>
	<link rel="stylesheet" href="${req.getContextPath()}/js/codemirror/codemirror.css"/>
	<link rel="stylesheet" href="${req.getContextPath()}/js/codemirror/eclipse.css">
	<script src="${req.getContextPath()}/js/codemirror/lang/python.js"></script>
	<script src="${req.getContextPath()}/js/codemirror/util/dialog.js"></script>
	
    <link rel="stylesheet" href="${req.getContextPath()}/js/codemirror/util/dialog.css">
    <script src="${req.getContextPath()}/js/codemirror/util/searchcursor.js"></script>
    <script src="${req.getContextPath()}/js/codemirror/util/search.js"></script>
    <script src="${req.getContextPath()}/js/codemirror/util/foldcode.js"></script> 
    
    <script>
    	function isFullScreen(cm) {
	      return /\bCodeMirror-fullscreen\b/.test(cm.getWrapperElement().className);
	    }
	    function winHeight() {
	      return window.innerHeight || (document.documentElement || document.body).clientHeight;
	    }
	    function setFullScreen(cm, full) {
	      var wrap = cm.getWrapperElement(), scroll = cm.getScrollerElement();
	      if (full) {
	        wrap.className += " CodeMirror-fullscreen";
	        scroll.style.height = winHeight() + "px";
	        document.documentElement.style.overflow = "hidden";
	      } else {
	        wrap.className = wrap.className.replace(" CodeMirror-fullscreen", "");
	        scroll.style.height = "";
	        document.documentElement.style.overflow = "";
	      }
	      cm.refresh(); 
	    }
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
				$('#contentHidden').val(editor.getValue());
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
					data: {'path':scriptPath, 'content': editor.getValue(), 'hostString' : hostString},
			    	success: function(res) {
						$('#validateRsPre').text(res);
						$('#validateRsPre').show();
						$("#footDiv").remove();
			    	},
			    	error: function() {
			    		showErrorMsg("<@spring.message "script.editor.error.validate"/>");
			    	}
			  	});
			});
		});
		</script>
	</body>
</html>
