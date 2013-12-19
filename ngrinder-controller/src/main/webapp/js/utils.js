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

function isIP(str) {
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

function enableChkboxSelectAll(containerId) {
	var idStr = "#" + containerId + " ";
	$(idStr + "td > input[disabled!='disabled']").click(function (event) {
		if ($(idStr + "td > input[disabled!='disabled']").size() == $(idStr + "td > input:checked").size()) {
			$(idStr + "th > input").attr("checked", "checked");
		} else {
			$(idStr + "th > input").removeAttr("checked");
		}

		event.stopImmediatePropagation();
	});

	$(idStr + "th > input").click(function (event) {
		if ($(this)[0].checked) {
			$(idStr + "td > input[disabled!='disabled']").each(function () {
				if ($(this))
					$(this).attr("checked", "checked");
			});
		} else {
			$("td > input").each(function () {
				$(this).removeAttr("checked");
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
});


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
	ajaxObj.params = params;
	return ajaxObj;
}

function AjaxObj(url, successMessage, errorMessage) {
	this.url = url;
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
	var path = ajaxCallContextPath + this.url;
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