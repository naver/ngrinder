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

var lastRecords = undefined;
var sUrlFilter = undefined;

var port = chrome.extension.connect({name : Module.popup});
port.onMessage.addListener(onMessageHandle);
port.onDisconnect.addListener(function() {
    port.onMessage.removeListener(onMessageHandle);
});

function onMessageHandle(msg) {
    switch (msg.cmd) {
        case CMD.GET_RECORDER_STATE:
        	sUrlFilter = msg.urlFilter;
            msg.bRun ? $("#recordOnBtn").click() : $("#recordOffBtn").click();
            $("#recordCountBadge").html(msg.recordCount);
            if (!$("#urlFilter").data("init")) {
            	$("#urlFilter").data("init", true);
            	$("#urlFilter").val(sUrlFilter);
            }
            break;
        case CMD.GET_RECORDS:
            $("#recordCountBadge").html(msg.recordCount);
            break;
    }
    lastRecords = msg.records;
}

$(document).ready(function() {
    port.postMessage(new Message(Module.popup, CMD.GET_RECORDER_STATE));

    $("button").bind("click", function(evt) {
        evt.target.blur();
    });
    
    $("#urlFilter").keyup(function() {
    	highlightFilterBtn($(this).val() !== sUrlFilter);
    });

    $("#recordOnBtn").click(function() {
        changeSwitch($("#recordOnBtn"), true);
        changeSwitch($("#recordOffBtn"), false);
        port.postMessage(new Message(Module.popup, CMD.START_RECORD));
    });

    $("#recordOffBtn").click(function() {
        changeSwitch($("#recordOnBtn"), false);
        changeSwitch($("#recordOffBtn"), true);
        port.postMessage(new Message(Module.popup, CMD.STOP_RECORD));
    });

    $("#applyUrlFilterBtn").click(function() {
        var msg = new Message(Module.popup, CMD.CHANGE_URL_FILTERS);
        sUrlFilter = $("#urlFilter").val();
        msg.urlFilter = sUrlFilter;
        port.postMessage(msg);
        alert("Applied url filter.");
        highlightFilterBtn(false);
    });

    $("#clearBtn").click(function() {
    	if (confirm("Do you want to clear recroded all request?")) {
            $("#recordCountBadge").html(0);
            port.postMessage(new Message(Module.popup, CMD.CLEAR_RECORDS));	
    	}
    });

    $("#generateGroovyBtn").click(function() {
        $("#recordOffBtn").click();
        var generateWin = window.open("generate.html");
        generateWin.records = lastRecords;
        generateWin.scriptType = ScriptType.groovy;
        window.close();
    });

    $("#generateJythonBtn").click(function() {
        $("#recordOffBtn").click();
        var generateWin = window.open("generate.html");
        generateWin.records = lastRecords;
        generateWin.scriptType = ScriptType.jython;
        window.close();
    });
});

function highlightFilterBtn(bOn) {
	if (bOn) {
		$("#applyUrlFilterBtn").removeClass("btn-default").addClass("btn-primary");
	} else {
		$("#applyUrlFilterBtn").removeClass("btn-primary").addClass("btn-default");
	}
}

function changeSwitch(elBtn, bMode) {
    if (bMode) {
        elBtn.removeClass("btn-default").addClass("btn-success");
    } else {
        elBtn.removeClass("btn-success").addClass("btn-default");
    }
}
