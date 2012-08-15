function checkStringFormat(str, ruleStr) {
	var rule = new RegExp(ruleStr);
	
	return rule.test(str);
}

function checkEmptyByID(id) {
	return checkEmpty($('#' + id));
}

function checkEmpty(obj) {
	if(checkEmptyByStr(obj.val())) {
		obj.val('');
		obj.focus();
		return true;
	}
	
	return false;
}

function checkEmptyByStr(str) {
	if($.trim(str) == "") {
		return true;
	}
	
	return false;
}

function isIPByID(id) {
	var $elem = $("#" + id);
	var success = isIP($elem.val());
	
	if (!success) {
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

function popover() { 
	var el = $(this);
	var placementStr = el.attr("placement");
	if (placementStr == "") {
		placementStr = "right";
	}  
	
	el.popover({
		placement:placementStr,
		trigger:"manual",
  	    template: '<div class="popover" ><div class="arrow"></div><div class="popover-inner myclass" style="width:400px"><h3 class="popover-title"></h3><div class="popover-content"><p></p></div></div></div>'	});
	if (el.attr("type") == "toggle") {
		el.popover('toggle');
	} else {
		el.popover('show');
	}
}


function popunover() { 
	var el = $(this);
	el.popover('hide');
}

function deleteSelection() {
    if (window.getSelection) {
        // Mozilla
        var selection = window.getSelection();
        if (selection.type != "Range") {
        	return;
        }
        if (selection.rangeCount > 0) {
            window.getSelection().deleteFromDocument();
            window.getSelection().removeAllRanges();
        }
    } else if (document.selection) {
        // Internet Explorer
        var ranges = document.selection.createRangeCollection();
        for (var i = 0; i < ranges.length; i++) {
            ranges[i].text = "";
        }
    }
}

$(document).ready(function() {
	$("input[number_limit]").keypress(function(e) {
		//deleteSelection($(this));
		if (e.charCode == 0) {
			return true;
		}
		if (e.charCode >= 48 && e.charCode < 58) {
			var curValue = Number($(this).val() + String(e.charCode - 48));
			var limit = Number($(this).attr("number_limit"));
			if (curValue <= limit) {
				return true;
			}
		} 
		return false;
	});
	
	
	$("div[rel=popover], span[rel=popover]").hover(popover, popunover);
});