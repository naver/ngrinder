function checkSimpleNameByID(id, name) {
	return checkSimpleNameByObj($("#" + id), name);
}

function checkSimpleNameByObj(obj, name) {
	var success = checkSimpleName(obj.val(), name);
	if (!success) {
		obj.focus();
	}
	
	return success;
}

function checkSimpleName(str, name) {
	var patrn = "^[a-zA-Z]{1}([a-zA-Z0-9]|[_]|[-]|[.]){0,19}$";
	
	if (checkEmptyByStr(str, name)) {
		return false;
	}
	
	if (!checkLengthByStr(str, 20, name)) {
		return false;
	}
	
	if (name) {
		var msg = getI18nMsg("simpleNameError", [name]);
		return checkStringFormat(str, patrn, msg);
	}
	
	return checkStringFormat(str, patrn);
}

function checkScriptFileExtension(str, ext) {
	var patrn = "." + ext + "$";
	return checkStringFormat(str, patrn, "");
}

function checkEmailFormat(id) {
	var obj = $("#" + id);
	var addr = obj.val();
	var patrn = /^[0-9a-zA-Z_\-\.]+@[0-9a-zA-Z_\-]+(\.[0-9a-zA-Z_\-]+)*$/;
	var success = checkStringFormat(addr, patrn, getI18nMsg("emailForamtErr"));
	if (!success) {
		obj.focus();
	}
	
	return success;
}

function checkStringFormat(str, ruleStr, msg) {
	var rule = new RegExp(ruleStr);
	
	if (rule.test(str)) {
		return true;
	}
	if (msg) {
		alert(msg);
	}
	
	return false;
}

function checkLength(id, len, name) {
	var $elem = $("#" + id);
	var str = $elem.val();
	var success = checkLengthByStr(str, len, name);
	
	if (!success) {
		$elem.focus();
	}
	
	return success;
}

function checkLengthByStr(str, len, name) {
	try {
		if (str == "" || str.length <= len) {
			return true;
		}
	} catch(e) {
	}

	alert(getI18nMsg("strMaxLength", [name, len]));
	return false;
}
function checkIsInteger(str)
{
	if(str == "")
	return false;
	if(str.match("/^(\\-?)(\\d+)$/")==null)
	{
		return false;
	}
	else
	{
		return true;
	}
}

function checkDigit(id, min, max, name, unit) {
	var $elem = $("#" + id);
	var val = $elem.val();
	var success = checkDigitValue(val, min, max, name, unit);
	
	if (!success) {
		$elem.focus();
	}
	
	return success;
}

function checkDigitValue(val, min, max, name, unit) {
	try {
		var i = parseInt(val);
		
		if (i >= min) {
			if (max == 0) {
				return true;
			}
			if (i <= max) {
				return true;
			}
		}
	} catch(e) {
	}
	
	var msg = "";
	
	if (max == 0) {
		msg = getI18nMsg("greaterDigitValue", [name, min]);
	} else {
		msg = getI18nMsg("betweenDigitValue", [name, min, max]);
	}
	
	if (typeof unit != "undefined") {
		msg = msg + " " + unit;
	}
	
	alert(msg + ".");
	
	return false;
}

function checkEmptyByID(id, name) {
	return checkEmpty($('#' + id), name);
}

function checkEmpty(obj, name) {
	if(checkEmptyByStr(obj.val(), name)) {
		obj.val('');
		obj.focus();
		return true;
	}
	
	return false;
}

function checkEmptyByStr(str, name) {
	if($.trim(str) == "") {
		if (name) {
			alert(getI18nMsg("cantEmpty", [name]));
		}
		
		return true;
	}
	
	return false;
}

function showErrMsg(obj, message) {
	obj.next().html(message);
}

function cleanErrMsg(obj) {
	obj.next().html("");
}

function isIPByID(id) {
	var $elem = $("#" + id);
	var success = isIP($elem.val());
	
	if (!success) {
		alert(getI18nMsg("errorIP"));
		$elem.focus();
	}
	
	return success;
}

function isIP(str) {
	var check = function(v) {
		try {
			var num = parseInt(v);
			if (v == num + "") {
				return (num <= 255 && num >= 0);
			} else {
				return false;
			}
		} catch(e) {
			return false;
		}
	};
	
	var re = $.trim(str).split(".");
	
	return (re.length == 4) ? (check(re[0])&&check(re[1])&&check(re[2])&&check(re[3])) : false;
}

function isPortByID(id) {
	var $elem = $("#" + id);
	var success = isPort($elem.val());
	
	if (!success) {
		alert(getI18nMsg("errorPort"));
		$elem.focus();
	}
	
	return success;
}

function isPort(str) {
	try {
		var port = parseInt($.trim(str));
		return port < 65536 && port > 0 ? true : false;
	} catch(e) {
		return false;
	}
}

function fs_getLeft(w){
	if (screen.width < 801){
		vleft=0;
	}
	else {
		vleft=(screen.width)?(screen.width-w)/2:100;
	}
	
	return Math.round(vleft);
}

function fs_getTop(h){
	if (screen.width < 801){
		vtop=0;
	}
	else {
		vtop=(screen.height)?(screen.height-h)/2:100;
	}
	
	return Math.round(vtop);
}

function fs_open(openUrl, winName, w, h, scrollYN, resizeYN) {
	window.open(openUrl, winName, 'width=' + w + ', height=' + h + ', scrollbars=' + scrollYN + ', resizable=' + resizeYN + ', left=' + fs_getLeft(w) + ', top=' + fs_getTop(h) + ', dependent=yes, toolbar=no, status=no');
}

function fs_quickopen(openUrl, w, h) {
	fs_open(openUrl, w, h, "", "yes", "yes");
}

function addHover(objs) {
  	objs.hover(
		function () {
			$(this).addClass("hover");
		}, 
		function () {
			$(this).removeClass("hover");
		}
	);
}

function makeModal(divId) {
	var $elem = $("<div class='modal hiddden' id='modalDiv'></div>");
	$("#" + divId).before($elem);
    var sWidth=document.body.offsetWidth;
    var sHeight=screen.height;
    var bgObj = $elem[0];
    bgObj.style.width=sWidth + "px";
    bgObj.style.height=sHeight + "px";
    $elem.fadeIn();
}

function removeModal() {
	$("#modalDiv").fadeOut(function() {
		$(this).remove();
	});
}

function showTooltip(message) {
	if ($("#isTooltip_chk:checked")[0]) {
		tooltip.show(message);
	}
}

function hideTooltip() {
	if ($("#isTooltip_chk:checked")[0]) {
		tooltip.hide();
	}
}

function bindTooltip(obj, namespace) {
	obj.bind("mouseover", function(event) {
    	if ($(this).attr("ttNo")) {
    		showTooltip(getTooltipMsg(namespace + "_" + $(this).attr("ttNo")));
    	}
    });
	
	obj.bind("mouseout", function() {
    	if ($(this).attr("ttNo")) {
    		hideTooltip();
    	}
    });
}

function bindShownMsg(obj, namespace, type) {
	obj.each(function() {
		var $elem = $(this);
		if ($elem.attr("msgNo")) {
			alert($elem.attr("msgNo"));
			$elem.attr("title", getShownMsg(namespace + "_" + $(this).attr("msgNo"), type));
			$elem.tipTip();
		}
	});
}

function showAdvance(message) {
	if ($("#isAdvance_chk:checked")[0]) {
		tooltip.show(message);
		//advanceMsg.aShow(message);
	}
}

function hideAdvance() {
	if ($("#isAdvance_chk:checked")[0]) {
		tooltip.hide();
		//advanceMsg.aHide();
	}
}

function bindAdvance(obj, namespace) {
	obj.bind("mouseover", function(event) {
    	if ($(this).attr("amNo")) {
    		showAdvance(getAdvanceMsg(namespace + "_" + $(this).attr("amNo")));
    	}
    });
	
	obj.bind("mouseout", function() {
    	if ($(this).attr("amNo")) {
    		hideAdvance();
    	}
    });
}

function addCookie(objName, objValue, objHours){//add cookie
	var str = objName + "=" + escape(objValue);
	if(objHours > 0){//if 0, when close browser, cookie removed auto.
		var date = new Date();
		var ms = objHours * 3600 * 1000;
		date.setTime(date.getTime() + ms);
		str += "; path=/; expires=" + date.toGMTString();
	}
	
	document.cookie = str;
}

function getCookie(objName){//get cookie value
	var arrStr = document.cookie.split("; ");
	for(var i = 0;i < arrStr.length;i ++){
		var temp = arrStr[i].split("=");
		if(temp[0] == objName){
			return unescape(temp[1]);
		}
	}
	
	return "";
}

function enableChkboxSelectAll() {
	$("td input").on("click", function() {
		if($("td input").size() == $("td input:checked").size()) {
				$("th input").attr("checked", "checked");
		} else {
			$("th input").removeAttr("checked");
		}
	});
	
	$("th input").on('click', function(event) {
		if($(this)[0].checked) {
			$("td input[disabled!='disabled']").each(function(){
				if ($(this))
				$(this).attr("checked", "checked");
			});
		} else {
			$("td input").each(function() {
				$(this).removeAttr("checked");
			});
		}
		
		event.stopImmediatePropagation();
	});
}

function removeClick() {
	$(".noClick").off('click');
}

function getValueByID(id) {
	return $.trim($("#" + id).val());
}

function popover(element) {
	if ($(this).attr("type") == "toggle") {
		$(this).popover('toggle');
	} else {
		$(this).popover('show');
	}
}
$(document).ready(function() {
	$("input[number_limit]").keypress(function(e) {
		if (e.keyCode >= 48 && e.keyCode < 58) {
			var curValue = Number($(this).val() + String(e.keyCode - 48));
			var limit = Number($(this).attr("number_limit"));
			if (curValue <= limit) {
				return true;
			}
		} 
		return false;
	});
	
	$("div[rel=popover]").hover(function() {
		if ($(this).attr("type") == "toggle") {
			$(this).popover('toggle');
		} else {
			$(this).popover('show');
		}
    });
	$("span[rel=popover]").hover(function() {
		if ($(this).attr("type") == "toggle") {
			$(this).popover('toggle');
		} else {
			$(this).popover('show');
		}
    });
});