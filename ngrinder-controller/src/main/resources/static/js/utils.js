function isRunningStatusType(statusType) {
	return statusType == "TESTING";
}
function isFinishedStatusType(statusType) {
	return statusType == "FINISHED" || statusType == "STOP_BY_ERROR" || statusType == "STOP_ON_ERROR" || statusType == "CANCELED";
}

function checkStringFormat(str, ruleStr) {
	return new RegExp(ruleStr).test(str);
}

function checkSimpleNameByObj(obj) {
	var success = checkSimpleName(obj.val());
	if (!success) {
		obj.focus();
	}

	return success;
}

function checkSimpleName(str) {
	return checkStringFormat(str, "^[a-zA-Z]{1}([a-zA-Z0-9]|[_]|[-]|[.]){2,19}$");
}

function checkEmptyByID(id) {
	return checkEmptyByObj($('#' + id));
}

function checkEmptyByObj(obj) {
	if (checkEmpty(obj.val())) {
		obj.val('');
		obj.focus();
		return true;
	}

	return false;
}

function checkEmpty(str) {
	return $.trim(str) == "";


}

function isIPByObj(obj) {
	var success = isIP(obj.val());
	if (!success) {
		obj.focus();
	}
	return success;
}

function isIPv4(str) {
	var check = function (v) {
		try {
			var num = parseInt(v);
			if (v == num + "") {
				return (num <= 255 && num >= 0);
			} else {
				return false;
			}
		} catch (e) {
			return false;
		}
	};

	var re = $.trim(str).split(".");

	return (re.length == 4) ? (check(re[0]) && check(re[1]) && check(re[2]) && check(re[3])) : false;
}

function isIP(str) {
    return isIPv4(str) || isIPv6(str);
}

function isIPv6(str) {
    var idx = str.indexOf("::");
    if (idx == -1) {
        var items = str.split(":");
        if (items.length != 8) {
            return false;
        } else {
            for (i in items) {
                if (!isHex(items[i])) {
                    return false;
                }
            }
            return true;
        }
    } else {
        if (idx != str.lastIndexOf("::")) {
            return false;
        } else {
            var items = str.split("::");
            var items0 = items[0].split(":");
            var items1 = items[1].split(":");
            if ((items0.length + items1.length) > 7) {
                return false;
            } else {
                for (i in items0) {
                    if (!isHex(items0[i])) {
                        return false;
                    }
                }
                for (i in items1) {
                    if (!isHex(items1[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
    }
}

function isHex(str) {
    if(str.length == 0 || str.length > 4) {
        return false;
    }
    str = str.toLowerCase();
    var ch;
    for(var i=0; i< str.length; i++) {
        ch = str.charAt(i);
        if(!(ch >= '0' && ch <= '9') && !(ch >= 'a' && ch <= 'f')) {
            return false;
        }
    }
    return true;
}


function getCookie(objName) {//get cookie value
	var arrStr = document.cookie.split("; ");
	for (var i = 0; i < arrStr.length; i++) {
		var temp = arrStr[i].split("=");
		if (temp[0] == objName) {
			return unescape(temp[1]);
		}
	}

	return "";
}

function enableCheckboxSelectAll(containerId) {
	var idStr = "#" + containerId + " ";
	$(idStr + "td > input[disabled!='disabled']").click(function (event) {
		if ($(idStr + "td > input[disabled!='disabled']").size() == $(idStr + "td > input:checked").size()) {
			$(idStr + "th > input").attr("checked", true);
		} else {
			$(idStr + "th > input").attr("checked", false);
		}
        event.stopImmediatePropagation();
	});

	$(idStr + "th > input").click(function (event) {
		if ($(this)[0].checked) {
			$(idStr + "td > input[disabled!='disabled',type='checkbox']").each(function () {
				this.checked = true
			});
		} else {
			$("td > input[type='checkbox']").each(function () {
                this.checked = false
			});
		}

		event.stopImmediatePropagation();
	});
}

function removeClick() {
	$(".no-click").off('click');
}


function markInput(obj, success, message) {
	if (success) {
		obj.next("span").empty();
		obj.parents('.control-group').addClass('success');
		obj.parents('.control-group').removeClass('error');
	} else {
		obj.next("span").html(message);
		obj.parents('.control-group').addClass('error');
		obj.parents('.control-group').removeClass('success');
	}
}


$(document).ready(function () {
	$("[rel='popover']").popover({trigger: 'hover', container: 'body'});
	
	$(".ball[rel='popover']").on('shown.bs.popover', function(evt) {
		new PopoverEventHandler($(evt.target));
	});
});

function PopoverEventHandler(elTarget) {
	this.hideCheckInterval = 1000;
	this.elTarget = elTarget;
	this.aPopover = $(".popover");
	this.bDisableHide = false;
	this.bMouseoverInPopover = false;
	
	this.bindEvnet();
}
PopoverEventHandler.prototype = {
	bindEvnet : function() {
		this.aPopover.bind("mouseover", $.proxy(this.setMouseoverInPopover, this, true));
		this.aPopover.bind("mouseout", $.proxy(this.setMouseoverInPopover, this, false));
		
		$(this.elTarget).bind("click", $.proxy(this.onDisableHide, this));
		$(this.elTarget).on("hide.bs.popover", $.proxy(this.hidePopover, this));
	},
	unbindEvent : function() {
		$(this.elTarget).off("hide.bs.popover");
		$(this.elTarget).unbind("click", this.onDisableHide, this);
		
		this.aPopover.unbind("mouseover", this.setMouseoverInPopover);
		this.aPopover.unbind("mouseout", this.setMouseoverInPopover);
	},
	isDisableHide : function() {
		return this.bDisableHide || this.bMouseoverInPopover;
	},
	setMouseoverInPopover : function(bOver) {
		this.bMouseoverInPopover = bOver;
	},
	onDisableHide : function() {
		this.bDisableHide = true;
	},
	hidePopover : function() {
		if (this.isDisableHide()) {
			this.bDisableHide = false;
			setTimeout($.proxy(this.hidePopover, this), this.hideCheckInterval);
			return false;
		} else {
			this.unbindEvent();
			this.elTarget.popover("hide");
		}
	}
};


function cookie(name, value, expiredays) {
	var today = new Date();
	today.setDate(today.getDate() + expiredays);
	document.cookie = name + "=" + escape(value) + "; path=/; expires=" + today.toGMTString() + ";";
}
var ajaxCallContextPath = "";
function setAjaxContextPath(contextPath) {
	ajaxCallContextPath = contextPath;
}

function AjaxPostObj(url, params, successMessage, errorMessage) {
	var ajaxObj = new AjaxObj(url, successMessage, errorMessage);
	ajaxObj.type = "POST";
	ajaxObj.params = params || {};
	return ajaxObj;
}

function AjaxPutObj(url, params, successMessage, errorMessage) {
    var ajaxObj = new AjaxObj(url, successMessage, errorMessage);
    ajaxObj.type = "PUT";
    ajaxObj.params = params || {};
    return ajaxObj;
}

function AjaxObj(url, successMessage, errorMessage) {
    if (url.indexOf("/") == 0) {
        this.url = url.substring(1);
    } else {
        this.url = url;
    }
	this.type = "GET";
	this.params = {};
	this.cache = false;
	this.dataType = 'json';
	this.async = true;
	this.successMessage = successMessage || null;
	this.errorMessage = errorMessage || null;
	this.complete = function () {
	};
	this.success = function (res) {
	};
	this.error = function (xhr, res) {
	};
}

AjaxObj.prototype.call = function () {
	var that = this;
	var path = ajaxCallContextPath + "/" + this.url;
	var filteredParam = {};

	$.each(this.params, function (key, value) {
		var variable = "{" + key + "}";
		if (path.indexOf(variable) != -1) {
			path = path.replace("{" + key + "}", value);
		} else {
			filteredParam[key] = value;
		}
	});
	if (this.async) {
		$.ajax({
			url: path,
			type: this.type,
			async: this.async,
			cache: this.cache,
			data: filteredParam,
			dataType: this.dataType
		})
			.done(function (res) {
				if (that.successMessage != null) {
					showSuccessMsg(that.successMessage);
				}
				that.success(res);
			})
			.fail(function (xhr, res) {
				if (xhr.status != 0) {
					if (that.errorMessage != null) {
						showErrorMsg(that.errorMessage);
					} else if (res.message !== undefined) {
						showErrorMsg(res.message);
					}
				}
				that.error(xhr, res);
			})
			.always(function () {
				that.complete();
			});
	} else {
		$.ajax({
			url: path,
			type: this.type,
			async: false,
			cache: this.cache,
			data: filteredParam,
			dataType: this.dataType,
			success: function (res) {
				if (that.successMessage != null) {
					showSuccessMsg(that.successMessage);
				}
				that.success(res);
			},
			error: function (xhr, res) {
				if (xhr.status != 0) {
					if (that.errorMessage != null) {
						showErrorMsg(that.errorMessage);
					} else if (res.message !== undefined) {
						showErrorMsg(res.message);
					}
				}
				that.error(xhr, res);
			},
			complete: function () {
				that.complete()
			}

		});
	}
};

function getDocHeight(offset) {

    var height = Math.max(
        document.body.scrollHeight, document.documentElement.scrollHeight,
        document.body.offsetHeight, document.documentElement.offsetHeight,
        document.body.clientHeight, document.documentElement.clientHeight
    );

    if (typeof(offset) != "undefined"){
        height += offset;
    }

    return height;
}

function getShortenString(str, start, end) {
	if (typeof(start) == "undefined"){
    	start = 0;
	}
	if (typeof(end) == "undefined"){
    	end = 20;
	}
	if (str.length >= end) {
    	str = str.substr(start, end - 4);
    	str +="...";
	}
	return str;
}