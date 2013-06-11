<div class="div-host" 
	rel='popover' 
	title='<@spring.message "perfTest.configuration.targetHost"/>' 
	data-html='true'
	data-content='<@spring.message "perfTest.configuration.targetHost.help"/>' 
	data-placement='bottom'>
</div>
<input type="hidden" name="targetHosts" id="target_hosts" value="${(targetHosts)!}">
<a class="btn pull-right btn-mini add-host-btn" data-toggle="modal" href="#add_host_modal"> 
	<@spring.message "perfTest.configuration.add"/> 
</a>
<!-- modal dialog -->
<div class="modal hide fade" id="add_host_modal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
		<h4>
			<@spring.message "perfTest.configuration.addHost"/>&nbsp;
			<small><@spring.message	"perfTest.configuration.pleaseInputOneOptionAtLeast"/></small>
		</h4>
	</div>
	<div class="modal-body">
		<div class="form-horizontal">
			<fieldset>
				<div class="control-group">
					<label for="domain_input" style="text-align: right;" class="control-label">
						<@spring.message "perfTest.configuration.domain"/>
					</label>
					<div class="controls">
						<input type="text" id="domain_input"> <span class="help-inline"></span>
					</div>
				</div>
				<div class="control-group">
					<label for="ip_input" style="text-align: right;" class="control-label">IP</label>
					<div class="controls">
						<input type="text" id="ip_input"> <span class="help-inline"></span>
					</div>
				</div>
			</fieldset>
		</div>
	</div>
	<div class="modal-footer">
		<button class="btn btn-primary" id="add_host_button"><@spring.message "perfTest.configuration.add"/></button>
		<button class="btn" data-dismiss="modal"><@spring.message "common.button.cancel"/></button>
	</div>
</div>

<div class="modal hide fade" id="target_info_modal" style="width: 580px" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-header" style="border: none;">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
	</div>
	<div class="modal-body" id="target_info_modal_container" style="max-height: 1200px; padding-left: 30px"></div>
</div>

<script>
	function validateHost(arr) {
		var success;

		var $domain = $("#domain_input");
		if (!checkEmptyByObj($domain)) {
			var rule = "^([a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,3}$";
			var str = $domain.val();
			success = checkStringFormat(str, rule);
			markInput($domain, success, '<@spring.message "perfTest.configuration.addHost.inputTargetDomain"/>');
			if (!success) {
				return false;
			}
			arr.push(str);
		}

		var $ip = $("#ip_input");
		if (!checkEmptyByObj($ip)) {
			success = isIPByObj($ip);
			markInput($ip, success, '<@spring.message "perfTest.configuration.addHost.inputTargetIp"/>');
			if (!success) {
				return false;
			}
			arr.push($ip.val());
		}
		return true;
	}

	$(document).ready(function() {
		$("#add_host_button").click(function() {
			var content = [];

			if (!validateHost(content)) {
				return;
			}
			if (content.length == 0) {
				$("#add_host_modal small").addClass("error-color");
				return;
			}

			var contentHTML = hostItem(content.join(":"));
			$(".div-host").html($(".div-host").html() + contentHTML);
			updateHostHiddenValue();

			$("#add_host_modal").modal("hide");
			$("#add_host_modal small").removeClass("error-color");
			$("#target_hosts").nextAll("span.help-inline").empty();
			return false;
		});

		$(".icon-remove-circle").live("click", function() {
			deleteHost($(this));
		});

		$(".icon-remove-circle").live("click", function() {
			deleteHost($(this));
		});

		$(".div-host p a[id='hostID']").live("click", function() {
			var url = "${req.getContextPath()}/monitor/info?ip=" + $.trim($(this).text());
			$("#target_info_modal_container").load(url, function() {
				$('#target_info_modal').modal('show').css({"margin-top":"-80px"});
			});
		});

		$('#target_info_modal').on('hidden', function() {
			if (timer) {
				window.clearInterval(timer);
			}
			$.get("${req.getContextPath()}/monitor/close?ip=" + $("#monitorIp").val());
		});

		initHosts();
	});

	function updateHostHiddenValue() {
		var contentStr = $("p.host").map(function() {
			return $.trim($(this).text());
		}).get().join(",");

		$("#target_hosts").val(contentStr);
	}

	function hostItem(content) {
		return "<p class='host'><a id='hostID' href='#target_info_modal' data-toggle='modal'> "
				+ content
				+ " </a> <a href='javascript:void(0);'><i class='icon-remove-circle'></i></a></p><br style='line-height:0px'/>"
	}

	function initHosts(newHosts) {
		if (newHosts != undefined) {
			newHosts = $.trim(newHosts);
			$("#target_hosts").val(newHosts);
			if (newHosts == "") {
				$(".div-host").html("");
				return;
			}
		} else if (checkEmptyByID("target_hosts")) {
			return;
		}

		var hosts = $("#target_hosts").val().split(",");
		$(".div-host").html($.map(hosts, function(val) {
			val = $.trim(val);
			if (val != "") {
				return hostItem(val);
			}
		}).join("\n"));
	}

	function deleteHost(element) {
		var elem = element.parents("p");
		elem.next("br").remove();
		elem.remove();
		updateHostHiddenValue();
	}
</script>
