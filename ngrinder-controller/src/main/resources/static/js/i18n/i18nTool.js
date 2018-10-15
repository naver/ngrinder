insertI18nJS();

function insertI18nJS(){
	var language="en";
	var ctx = $("#contextPath").val();
	if (language.indexOf("ko") > -1){
		document.writeln('<script type="text/javascript" src="' + ctx + '/js/i18n/message/MessageResource_ko.js"></script>');
		document.writeln('<script type="text/javascript" src="' + ctx + '/js/i18n/message/TooltipMessage_ko.js"></script>');
	} else if (language.indexOf("cn") > -1) {
		document.writeln('<script type="text/javascript" src="' + ctx + '/js/i18n/message/MessageResource_cn.js"></script>');
		document.writeln('<script type="text/javascript" src="' + ctx + '/js/i18n/message/TooltipMessage_cn.js"></script>');
	} else {
		document.writeln('<script type="text/javascript" src="' + ctx + '/js/i18n/message/MessageResource_en.js"></script>');
		document.writeln('<script type="text/javascript" src="' + ctx + '/js/i18n/message/TooltipMessage_en.js"></script>');
	}
}

function getI18nMsg(key, args){
	var result = resource[key];
	if (args && args != null) {
		var i;
		for(i = 0; i < args.length; i++) {
			result = result.replace("{" + i + "}", args[i]);
		}
	}
	
	return result; 
}

function getTooltipMsg(key) {
	return tooltips[key];
}

function getAdvanceMsg(key) {
	return advances[key];
}

function getShownMsg(key, type) {
	var msg = "";
	
	if (type == "tt") {
		msg = tooltips[key];
	} else if (type = "ad") {
		msg = advances[key];
	}
	
	return msg;
}