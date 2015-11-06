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

// data init
var aRecords = [];

function Record(recordJson) {
	this.id = recordJson.id;
	this.scope = "None";
	this.method = recordJson.method;
	this.url = recordJson.url;
	this.headers = recordJson.headers;
	this.formData = recordJson.formData;
	this.timestamp = recordJson.timestamp;
	this.hasTimeGap = true;
}

var beforeRecord = undefined;
for (var k in records) {
	var record = new Record(records[k]);
	aRecords.push(record);
	if (beforeRecord !== undefined) {
		record.hasTimeGap = record.timestamp - beforeRecord.timestamp > 300;
	}
	beforeRecord = record;
}
beforeRecord = undefined;

$(document).ready(function() {
	initDataTable();	
	bindEvent();
});

function initDataTable() {
	$dataTable = $("#recordListTbl").DataTable({
		"dom" : "<\"toolbar\">frtip",
		"bSort" : false,
		 paging: false,
		 data : aRecords,
		 columns : [
		 	{ data : "id", "visible" : false },
		 	{ data : "scope", "sClass" : "align-center" },
		 	{ data : "method", "sClass" : "align-center" },
		 	{ data : "url" },
		 	{ data : "headers" },
		 	{ data : null, "sClass" : "align-center" },
		 	{ data : "formData" },
		 ],
		 "columnDefs": [
		 	{
		 		"render" : function(data, type, row) {
		 			var el = $("<select>", { class : "scope" }).append($("<option>", { value : "None", html : "None" }));
		 			for (var i = 0; i < scriptType.scope.length; i++) {
		 				el.append($("<option>", { value : scriptType.scope[i], html : scriptType.scope[i] }))
		 			}
					el.find("[value=" + data +"]").attr("selected", "selected");
					return el[0].outerHTML;
		 		},
		 		"targets" : 1
		 	},
		 	{
		 		"render" : function(data, type, row) {
		 			var el = $("<div>", { class : "ellipsis", title : data, html : data });
		 			return el[0].outerHTML;
		 		},
		 		"targets" : 3
		 	},
		 	{
		 		"render" : function(data, type, row) {
		 			var el = $("<div>", { class : "ellipsis", title : objectToString(data), html : JSON.stringify(data) });
		 			return el[0].outerHTML;
		 		},
		 		"targets" : 4
		 	},
		 	{
		 		"render" : function(data, type, row) {		 			
		 			var el = $("<input>", { class : "checkUseCookie", type : "checkbox" });
		 			return el[0].outerHTML;
		 		},
		 		"targets" : 5
		 	},
		 	{
		 		"render" : function(data, type, row) {
		 			if (data === undefined) {
		 				return "";
		 			}
		 			var el = $("<div>", {
		 				class : "ellipsis", title : objectToString(data),
		 				html : JSON.stringify(data).replace(/</g, "&lt;").replace(/>/g, "&gt;")
		 			});
		 			return el[0].outerHTML;
		 		},
		 		"targets" : 6
		 	},
		 ],
		 "fnRowCallback": function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
		 	if (aData.hasTimeGap) {
		 		$(nRow).css("font-weight", "bold");
		 	}
		 }
	});
	var hideResourceBtn = $("<button>", { id : "hideResourceBtn", type : "button", width : "175px", class : "btn btn-warning btn-xs", html :"Hide resource request" });
	var showResourceBtn = $("<button>", { id : "showResourceBtn", type : "button", width : "175px", class : "btn btn-info btn-xs hide", html :"Show resource request" });
	$("div.toolbar").addClass("clearfix").css("float", "left").append(hideResourceBtn).append(showResourceBtn);
}

function bindEvent() {
	var resourcePattern = /.*\.(js|css|png|jpg|jpeg|gif|bmp)(\?.*)?$/;
	$("#hideResourceBtn").click(function() {
		$dataTable.rows().data().each(function(record, index) {
			if (record.url.match(resourcePattern)) {
				var tr = $dataTable.row(index).node();
				$(tr).addClass("hide");
			}
		});
		$("#showResourceBtn").removeClass("hide");
		$(this).addClass("hide");
	});
	$("#showResourceBtn").click(function() {
		$dataTable.rows().data().each(function(record, index) {
			if (record.url.match(resourcePattern)) {
				var tr = $dataTable.row(index).node();
				$(tr).removeClass("hide");
			}
		});
		$("#hideResourceBtn").removeClass("hide");
		$(this).addClass("hide");
	});
	
	$("#recordListTbl tbody").on("click", "tr", function() {
		if ($(this).hasClass("selected")) {
			$(this).removeClass("selected");
			$(this).find("select").val("None");
		} else {
			$(this).addClass("selected");
			if ($(this).find("select").val() === "None") {
				$(this).find("select").val(scriptType.defaultScope);
			}
		}
		var data = $dataTable.row(this).data();
		data.scope = $(this).find("select").val();
	});

	$("#recordListTbl tbody").on("click", ".scope", function(evt) {
		evt.stopPropagation();
	});

	$("#recordListTbl tbody").on("change", ".scope", function(evt) {
		var tr = $(this).closest("tr");
		var data = $dataTable.row(tr).data();
		data.scope = $(this).val();
		if ($(this).val() === "None") {
			$(this).closest("tr").removeClass("selected");
		} else {
			$(this).closest("tr").addClass("selected");
		}
		evt.stopPropagation();
	});

	$("#recordListTbl tbody").on("click", ".checkUseCookie", function(evt) {
		var tr = $(this).closest("tr");
		var data = $dataTable.row(tr).data();
		data.useCookie = this.checked;
		evt.stopPropagation();
	});

	$("#deleteBtn").click(function() {
		var aNotSelected = $("#recordListTbl tbody tr").not(".selected");
		if (confirm("Delete not selected " + aNotSelected.length + " row?")) {
			$dataTable.rows(aNotSelected).remove();
			$dataTable.draw();
		}
	});

	$("#createBtn").click(function() {
		var requestJson = {};
		for (var i = 0; i < scriptType.scope.length; i++) {
			requestJson[scriptType.scope[i]] = {};
		}
		$dataTable.rows(function(idx, data, node) {
			var $node = $(node);
			return $node.hasClass("selected") && !$node.hasClass("hide");
		}).every(function() {
			 var data = this.data();
			requestJson[data.scope]["REQ_" + data.id] = data;
			delete data.id;
			delete data.scope;
			delete data.timestamp;
			delete data.hasTimeGap;
		});
		var resultWin = window.open("result.html");
		resultWin.scriptType = scriptType;
		resultWin.requestJson = requestJson;
		window.close();
	});
}