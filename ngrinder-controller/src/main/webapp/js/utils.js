function checkFormatByObj(obj, ruleStr) {
    var success = checkStringFormat(obj.val(), ruleStr);

    if (!success) {
        obj.focus();
    }

    return success;
}

function checkStringFormat(str, ruleStr) {
    var rule = new RegExp(ruleStr);

    return rule.test(str);
}

function checkSimpleNameByID(id) {
    return checkSimpleNameByObj($("#" + id));
}

function checkSimpleNameByObj(obj) {
    var success = checkSimpleName(obj.val());
    if (!success) {
        obj.focus();
    }

    return success;
}

function checkSimpleName(str) {
    var patrn = "^[a-zA-Z]{1}([a-zA-Z0-9]|[_]|[-]|[.]){2,19}$";

    return checkStringFormat(str, patrn);
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
    if ($.trim(str) == "") {
        return true;
    }

    return false;
}

function isIPByID(id) {
    return isIPByObj($("#" + id));
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

function isPortByID(id) {
    return isPortByObj($("#" + id));
}

function isPortByObj(obj) {
    var success = isPort(obj.val());

    if (!success) {
        alert(getI18nMsg("errorPort"));
        obj.focus();
    }

    return success;
}

function isPort(str) {
    try {
        var port = parseInt($.trim(str));
        return port < 65536 && port > 0 ? true : false;
    } catch (e) {
        return false;
    }
}

function fs_getLeft(w) {
    if (screen.width < 801) {
        vleft = 0;
    }
    else {
        vleft = (screen.width) ? (screen.width - w) / 2 : 100;
    }

    return Math.round(vleft);
}

function fs_getTop(h) {
    if (screen.width < 801) {
        vtop = 0;
    }
    else {
        vtop = (screen.height) ? (screen.height - h) / 2 : 100;
    }

    return Math.round(vtop);
}

function fs_open(openUrl, winName, w, h, scrollYN, resizeYN) {
    window.open(openUrl, winName, 'width=' + w + ', height=' + h + ', scrollbars=' + scrollYN + ', resizable=' + resizeYN + ', left=' + fs_getLeft(w) + ', top=' + fs_getTop(h) + ', dependent=yes, toolbar=no, status=no');
}

function fs_quickopen(openUrl, w, h) {
    fs_open(openUrl, w, h, "", "yes", "yes");
}

function addCookie(objName, objValue, objHours) {//add cookie
    var str = objName + "=" + escape(objValue);
    if (objHours > 0) {//if 0, when close browser, cookie removed auto.
        var date = new Date();
        var ms = objHours * 3600 * 1000;
        date.setTime(date.getTime() + ms);
        str += "; path=/; expires=" + date.toGMTString();
    }

    document.cookie = str;
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
    var IdStr = "#" + containerId + " ";
    $(IdStr + "td > input[disabled!='disabled']").click(function (event) {
        if ($(IdStr + "td > input[disabled!='disabled']").size() == $(IdStr + "td > input:checked").size()) {
            $(IdStr + "th > input").attr("checked", "checked");
        } else {
            $(IdStr + "th > input").removeAttr("checked");
        }

        event.stopImmediatePropagation();
    });

    $(IdStr + "th > input").click(function (event) {
        if ($(this)[0].checked) {
            $(IdStr + "td > input[disabled!='disabled']").each(function () {
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

function getValueByID(id) {
    return $.trim($("#" + id).val());
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
    this.successMessage = successMessage;
    this.errorMessage = errorMessage;
    this.complete = function() {
    };
    this.success = function() {
    };
    this.error = function () {
        return false;
    };
};

AjaxObj.prototype.call = function () {
    var that = this;
    $.ajax({
        url: contextPath + this.url,
        type: this.type,
        async: this.async,
        cache: this.cache,
        data: this.params,
        dataType: this.dataType,
        success: function(res) {
            if (that.successMessage != null) {
                showSuccessMsg(that.successMessage);
            }
            that.success(res);
        },
        complete : this.complete,
        error: function(xhr, res) {
            if (xhr.status != 0) {
                if (that.errorMessage != null) {
                    showErrorMsg(that.errorMessage);
                } else if (res.message !== undefined) {
                    showErrorMsg(res.message);
                }
            }
            that.error();
        }
    });
}

