<link href="${req.getContextPath()}/css/jquery-ui-1.10.4.min.css?${nGrinderVersion}" rel="stylesheet"/>
<script type="text/javascript" src="${req.getContextPath()}/js/jquery-ui-1.10.4.min.js?${nGrinderVersion}"></script>
<style type="text/css">
	.ui-front {
		z-index: 1100;
	}
	.option-title {
		margin-left: 50px;
	}
	.option-data {
		padding-top: 10px;
	}
	.span2-3 {
		width: 180px;
	}
</style>
<div class="row form-horizontal">
	<div class="option-title span1"><h5><@spring.message "script.option.header.title"/></h5></div>
	<div id="headers" class="option-data span9">	
		<i title="Add header" id="addHeaderBtn" class="icon-plus pointer-cursor"></i>				
	</div>
</div>
<hr class="small">
<div class="row form-horizontal">
	<div class="option-title span1"><h5><@spring.message "script.option.cookie.title"/></h5></div>
	<div id="cookies" class="option-data span9">
		<i title="Add cookie" id="addCookieBtn" class="icon-plus pointer-cursor"></i>
	</div>
</div>
<hr class="small">
<div id="paramRow" class="row form-horizontal">
	<div class="option-title span1"><h5><@spring.message "script.option.param.title"/></h5></div>
	<div id="params" class="option-data span9">
		<i title="Add param" id="addParamBtn" class="icon-plus pointer-cursor"></i>
	</div>
</div>
<div id="bodyRow" class="row hide">
	<div class="option-title span1"><h5><@spring.message "script.option.reqBody.title"/></h5></div>
	<div class="span9">
		<textarea id="reqBodyText" class="span9" rows="6" cols="95"></textarea>
	</div>
</div>


<script type="text/javascript">
	var headerNames = ['Connection', 'User-Agent'];
	var headerValues = {};
	headerValues[headerNames[0]] = ['keep-alive'];
	headerValues[headerNames[1]] = ['Mozilla/5.0 (X11; Linux x86_64; rv:12.0) Gecko/20100101 Firefox/21.0'];

	$(document).ready(function(){
		$("#addHeaderBtn").click(function() {
			var header = new Pair("header");
			options.headers.push(header);
			$("#addHeaderBtn").before(header.getHtml());			
			$(header.name).autocomplete({
					minLength : 0,
					source : headerNames,
					select : function(event, ui) {
						// destroy old autocomplete
						var elValueInput = $(event.target).siblings(".value");
						elValueInput.autocomplete();
						elValueInput.autocomplete("destroy");
						// init new autocomplete
						elValueInput.autocomplete({
							minLength : 0,
							source : headerValues[ui.item.value]
						}).focus(autoSearch);
						elValueInput.val("");
					}
				}).focus(autoSearch);
		});
		
		$("#addCookieBtn").click(function() {
			var url = $(".test-url").val();
			var urlPattern = /^((https?|ftp):\/\/)?([^\/:]*)(:[0-9].*)?(\/)?.*$/i;
			var domain = url.match(urlPattern)[3];
			var cookie = new Cookie("cookie", domain);
			options.cookies.push(cookie);
			$("#addCookieBtn").before(cookie.getHtml());
		});
		
		$("#addParamBtn").click(function() {
			var param = new Pair("param");
			options.params.push(param);
			$("#addParamBtn").before(param.getHtml());
		});
	});
	
	function autoSearch() {
		$(this).autocomplete('search', $(this).val());
	}

	var options = {
		headers : [],
		params : [],
		cookies : [],
		remove : function(pair) {
			pair.root.remove();
			for (key in this) {
				var option = this[key];
				if (option instanceof Array) {
					if (option.indexOf(pair) !== -1) {
						option.splice(option.indexOf(pair), 1);
						break;
					}
				}
			}
		},
		toJson : function(method) {
			var json = {
				method : method,
				headers : [],
				params : [],
				cookies : []
			};
			this._appendHeaderJson(json);
			this._appendParamJson(json);
			this._appendCookieJson(json);
			return JSON.stringify(json);
		},
		_appendHeaderJson : function(json) {
			for (var i = 0; i < this.headers.length; i++) {
				json.headers.push({
					name : this.headers[i].getName(),
					value : this.headers[i].getValue()
				});
			}
		},
		_appendParamJson : function(json) {
			if ($("#bodyRow").hasClass("hide")) {
				for (var i = 0; i < this.params.length; i++) {
					json.params.push({
						name : this.params[i].getName(),
						value : this.params[i].getValue()
					});
				}
			} else {
				json.body = $("#reqBodyText").val();
			}
		},
		_appendCookieJson : function(json) {
			for (var i = 0; i < this.cookies.length; i++) {
				json.cookies.push({
					name : this.cookies[i].getName(),
					value : this.cookies[i].getValue(),
					domain : this.cookies[i].getDomain(),
					path : this.cookies[i].getPath()
				});
			}
		}
	};
	function Pair(cls) {
		this.root = $("<div>", {class : cls});
		this.name = $("<input>", {class : "input name span2", placeholder : "name", type : "text"});
		this.value = $("<input>", {class : "input value span2-3", placeholder : "value", type : "text"});
		this.name.appendTo(this.root);
		this.root.append($("<i>", { html : "&nbsp;=&nbsp;" }));
		this.value.appendTo(this.root);
	}
	Pair.prototype = {
		getHtml : function() {
			var deleteBtn = $("<i>", {class : "icon-minus pointer-cursor", title : "Delete", style : "margin-left: 10px;"});
			deleteBtn.click({
				targetPair : this
			}, function(evt) {
				options.remove(evt.data.targetPair);
			});
			this.root.append(deleteBtn);
			return this.root;
		},
		remove : function() {
			this.root.remove();
		},
		getName : function() {
			return this.name.val();
		},
		getValue : function() {
			return this.value.val();
		}
	};
	function Cookie(cls, domain) {
		Pair.call(this, cls);
		this.domain = $("<input>", {class : "input domain span2", placeholder : "domain", type : "text", value : domain, style : "margin-left:15px;"});
		this.path = $("<input>", {class : "input path span2", placeholder : "path", type : "text"});
		this.domain.appendTo(this.root);
		this.path.appendTo(this.root);
	}
	Cookie.prototype = Object.create(Pair.prototype, {
		constructor : Cookie
	});
	Cookie.prototype.getDomain = function() {
		return this.domain.val();
	};
	Cookie.prototype.getPath = function() {
		return this.path.val();
	}
	
	function changeHTTPMethod(method) {
		var i = findHeaderIndex("Content-Type");
		if (i !== -1) {
			options.remove(options.headers[i]);
		}
		switch (method) {
			case "GET":
				formMode(true);
				break;
			case "POST":
				var header = new Pair("header");
				header.name.attr("disabled", true);
				header.name.val("Content-Type");
				header.value.val("application/x-www-form-urlencoded");
				options.headers.unshift(header);
				$("#headers").prepend(header.getHtml());			
				$(header.value).autocomplete({
						minLength : 0,
						source : ["application/x-www-form-urlencoded", "application/json"],
						select : function(event, ui) {
							if (ui.item.value === "application/x-www-form-urlencoded") {
								formMode(true);
							} else {
								formMode(false);
							}
							$(event.target).blur();
						}
					}).focus(function() {
						$(this).autocomplete('search', "");
					});
				break;
		}
	}
	
	function findHeaderIndex(name) {
		var headers = options.headers;
		for (var i in headers) {
			var header = headers[i];
			if (header.name.val().toUpperCase() === name.toUpperCase()) {
				return i;
			}
		}
		return -1;
	}
	
	function formMode(bOn) {
		if (bOn) {
			$("#paramRow").removeClass("hide");
			$("#bodyRow").addClass("hide");
		} else {
			$("#paramRow").addClass("hide");
			$("#bodyRow").removeClass("hide");
		}
	}
</script>