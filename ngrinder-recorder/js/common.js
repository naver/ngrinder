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

var Module = {
	popup : "popup",
	background : "background",
	devtools : "devtools",
	panel : "panel",
	generate : "generate"
};

var CMD = {
	GET_RECORDER_STATE : 0,
	START_RECORD : 1,
	STOP_RECORD : 2,
	CLEAR_RECORDS : 3,
	GET_RECORDS : 4,
	CHANGE_URL_FILTERS : 5
};

var ScriptType = {
	jython : {
		name : "jython",
		codeMirrorMode : "python",
		scope : ["__init__", "__call__"],
		defaultScope : "__call__"
	},
	groovy : {
		name : "groovy",
		codeMirrorMode : "groovy",
		scope : ["BeforeThread", "Test", "AfterThread"],
		defaultScope : "Test"
	}
}

function Message(moduleName, cmd) {
	this.moduleName = moduleName;
	this.cmd = cmd;
}

var invalidCommaArrayReg = /,\s*\]/g;
var invalidCommaObjectReg = /,\s*\}/g;
function deleteInvalidComman(str) {
	str = str.replace(invalidCommaArrayReg, "]");
	str = str.replace(invalidCommaObjectReg, "}");
	return str;
}

function objectToString(obj) {
	if (!(typeof obj === "object")) {
		return obj;
	}
	var str = "{\n";

	for (var key in obj) {
		var value = obj[key];
		if (value instanceof Array) {
			str += "\t" + key + ": [";
			for (var i = 0; i < value.length; i++) {
				str += value[i] + ", ";
			}
			str += "]\n";
		} else {
			str += "\t" + key + ": " + value + "\n";
		}
	}

	str += "}";
	return deleteInvalidComman(str);
}
