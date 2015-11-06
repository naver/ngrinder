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

// Communication
var ports = {};

chrome.extension.onConnect.addListener(function(port) {
	ports[port.name] = port;
	port.onMessage.addListener(onMessageHandle);
	port.onDisconnect.addListener(function() {
		ports[port.name] = undefined;
		port.onMessage.removeListener(onMessageHandle);
	});
});

function onMessageHandle(msg, sender, sendResponse) {
	var port = ports[msg.moduleName];
	switch (msg.cmd) {
        case CMD.START_RECORD:
        	recorder.bRun = true;
            break;
        case CMD.STOP_RECORD:
        	recorder.bRun = false;
            break;
        case CMD.CLEAR_RECORDS:
        	recorder.clear();
            break;
        case CMD.CHANGE_URL_FILTERS:
        	recorder.urlFilter = msg.urlFilter;
        	break;
    }
    appendRecorderState(msg);
    broadcast(msg);
}

function broadcast(msg) {
	for (var moduleName in ports) {
		var port = ports[moduleName];
		if (port) {
			port.postMessage(msg);
		}
	}
}

function appendRecorderState(msg) {
	msg.bRun = recorder.bRun;
	msg.records = recorder.records;
	msg.recordCount = recorder.getRecordCount();
	msg.urlFilter = recorder.urlFilter;
}

// Recoder Object
function RequestRecorder() {
	this.bRun = false;
	this.records = {};
}

RequestRecorder.prototype = {
	record : function(request) {
		if (!this.bRun) {
			return;
		}

		var record = this._getRecord(request);
		if (record.headers === undefined && request.requestHeaders !== undefined) {
			record.headers = {};
			for (var i = 0; i < request.requestHeaders.length; i++) {
				record.headers[request.requestHeaders[i].name] = request.requestHeaders[i].value;
			}
		}

		if (record.formData === undefined && request.requestBody !== undefined) {
			if (request.requestBody.formData) {
				record.formData = request.requestBody.formData;
			} else if (request.requestBody.raw) {
				record.formData = "";
				for (var i = 0; i < request.requestBody.raw.length; i++) {
					var raw = request.requestBody.raw[i];
					if (raw.bytes) {	// raw.bytes is ArrayBuffer
						var sData = String.fromCharCode.apply(null, new Uint8Array(raw.bytes));
						record.formData += sData;
					} else if (raw.file) {
						record.formData += "<nGrinderRecorderFileName>"+ raw.file + "</nGrinderRecorderFileName>";
					}
				}
			}
		}
	},

	_getRecord : function(request) {
		var id = request.requestId;
		if (this.records[id] === undefined) {
			this.records[id] = { id : id, url : request.url, method : request.method, timestamp : request.timeStamp };
		}
		return this.records[id];
	},

	getRecordCount : function() {
		return Object.keys(this.records).length;
	},

	clear : function() {
		this.records = {};
	}
};

var recorder = new RequestRecorder();

// Extension
var allUrls = "*://*/*";
bindWebRequestEvent();

function bindWebRequestEvent() {
	chrome.webRequest.onBeforeRequest.addListener(
		recordRequest, { urls: [allUrls] }, ["blocking", "requestBody"]
	);
	chrome.webRequest.onBeforeSendHeaders.addListener(
		recordRequest, { urls: [allUrls] }, ["blocking", "requestHeaders"]
	);
	chrome.webRequest.onCompleted.addListener(
		broadcastRecorderState, { urls: [allUrls] }, ["responseHeaders"]
	);
}

function recordRequest(request) {
	if (request.url.match(recorder.urlFilter)) {
		recorder.record(request);
	}
}

function broadcastRecorderState(request) {
	var msg = new Message(Module.background, CMD.GET_RECORDS);
	appendRecorderState(msg);
	broadcast(msg);
}
