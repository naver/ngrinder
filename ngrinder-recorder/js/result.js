/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var sScriptTemplateElementId = scriptType.name === "jython" ? "jythonScriptText" : "groovyScriptText";

$(document).ready(function() {
	if (scriptType.name === "jython") {
		$("#groovyArea").remove();
	} else {
		$("#jythonArea").remove();
	}
	
	$("#saveJson").click(function() {
		saveToFile(requestJsonEditor.getValue(), "request.json");
	});
	
	$("#saveGroovy").click(function() {
		saveToFile(scriptEditor.getValue(), "TestRunner.groovy");
	});
	
	$("#saveJython").click(function() {
		saveToFile(scriptEditor.getValue(), "TestRunner.py");
	});
	
	$("#requestJsonText").val(JSON.stringify(requestJson, undefined, "\t"));
	initScriptTemplate();

	requestJsonEditor = CodeMirror.fromTextArea(document.getElementById("requestJsonText"), {
		mode: "shell",
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
			requestJsonEditor.setLineClass(reshlLine, null, null);
			reshlLine = requestJsonEditor.setLineClass(requestJsonEditor.getCursor().line, null, "activeline");
		}
	});

	scriptEditor = CodeMirror.fromTextArea(document.getElementById(sScriptTemplateElementId), {
		mode: scriptType.codeMirrorMode,
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
			scriptEditor.setLineClass(scripthlLine, null, null);
			scripthlLine = scriptEditor.setLineClass(scriptEditor.getCursor().line, null, "activeline");
		}
	});

	var reshlLine = requestJsonEditor.setLineClass(0, "activeline");
	var scripthlLine = scriptEditor.setLineClass(0, "activeline");
	requestJsonEditor.setSize("", "350");
	scriptEditor.setSize("", "350");
});

function initScriptTemplate() {
	for (var i = 0; i < scriptType.scope.length; i++) {
		replaceCodeForSend(scriptType.scope[i]);
	}
}

function replaceCodeForSend(sScope) {
	var template = $("#" + sScriptTemplateElementId).val();
	var sSendCode = "";
	for (var key in requestJson[sScope]) {
		if (scriptType.name === "jython") {
			sSendCode += "\r\n		result = RecorderUtils.sendBy(request, self.requestJson.get(\"" + sScope + "\").get(\"" + key + "\"))";
		} else {
			sSendCode += "\r\n		result = RecorderUtils.sendBy(request, requestJson." + sScope + "." + key + ")";
		}
	}
	sSendCode = sSendCode.replace(/^\r\n\t\t/, "");
	template = template.replace("<nGrinderRecorder" + sScope + "RequestSend>", sSendCode);
	$("#" + sScriptTemplateElementId).val(template);
}

function saveToFile(content, filename) {
	var blob = new Blob([content], { type : "text/plain;charset=UTF-8" });
	saveAs(blob, filename, true);
}
