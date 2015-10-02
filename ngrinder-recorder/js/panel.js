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

var win = chrome.devtools.inspectedWindow;
var moduleName = Module.panel + "_" + win.tabId;

var port = chrome.runtime.connect({name : moduleName});
port.onMessage.addListener(onMessageHandle);
port.onDisconnect.addListener(function() {
    port.onMessage.removeListener(onMessageHandle);
});
port.postMessage(new Message(moduleName, CMD.GET_RECORDS));

function onMessageHandle(msg, sender, sendResponse) {
	switch (msg.cmd) {
		case CMD.CLEAR_RECORDS:
			$dataTable.rows().clear();
			break;
	}
	$("#urlFilter").html(msg.urlFilter === "" ? "No have(record all url)" : msg.urlFilter);
	$("#recordEnable").html(msg.bRun ? "True" : "False");
	$("#recordCount").html(msg.recordCount);
	var records = msg.records;
	for (var key in records) {
		var record = records[key];
		if (recordElementManager.getCaching(record.id, record)) {
			continue;
		}
		$dataTable.row.add(recordElementManager.getElTr(record));
	}
	$dataTable.draw(false);
}

function RecordElementManager() {
	this.elRecordCaches = {};
}
RecordElementManager.prototype = {
	getElTr : function(record) {
		var tr = this.getCaching(record.id);
		if (tr !== undefined) {
			return tr;
		}
		tr = $("<tr>");
		tr.append($("<td>", { html : record.method }));
		tr.append($("<td>", { class : "ellipsis", html : $("<span>", {title : record.url, html : record.url}) }));
		tr.append(this._createEl(record.headers));
		tr.append(this._createEl(record.formData ? record.formData : record.raw));
		this._setCaching(record.id, tr);
		return tr;
	},

	getCaching : function(id, record) {
		var cache = this.elRecordCaches[id];
		if (cache && record) {
			if (!this._updatedHeaders(cache) && record.headers !== undefined) {
				cache.find(".headers").append(this._createEl(record.headers, "headers").html());
			}
			if (!this._updatedFormData(cache) && record.formData !== undefined) {
				cache.find(".formData").append(this._createEl(record.formData, "formData").html());
			}
		}
		return cache;
	},

	_setCaching : function(id, elRecordTr) {
		this.elRecordCaches[id] = elRecordTr;
	},

	_createEl : function(obj, cls) {
		var td = $("<td>", { class : "ellipsis " + cls });
		if (obj === undefined) {
			return td;
		}
		var html = objectToString(obj);
		return td.append($("<span>", { title : html, html : html }));
	},

	_updatedHeaders : function(elRecordTr) {
		return elRecordTr.find(".headers span").size() === 1;
	},

	_updatedFormData : function(elRecordTr) {
		return elRecordTr.find(".formData span").size() === 1;
	}
};

var recordElementManager = new RecordElementManager();

$(document).ready(function() {
	$dataTable = $("#recordListTbl").DataTable({
		"bSort" : false
	});
});
