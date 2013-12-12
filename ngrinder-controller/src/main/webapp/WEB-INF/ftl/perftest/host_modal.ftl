
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
		<div class="form-horizontal form-horizontal-4">
			<fieldset>

				<@control_group name="domainInput" label_style="text-align: right;"
					inline_help="true" label_message_key="perfTest.configuration.domain">
					<input type="text" class="input-medium" id="domain_input"
						rel='modal_popover'
						data-html='true'
						data-content='<@spring.message "perfTest.configuration.addHost.inputTargetDomain"/>'
						title='<@spring.message "perfTest.configuration.domain"/>'
						data-placement='right'
						/>
				</@control_group>


				<@control_group name="ipInput" label_style="text-align: right;"
					inline_help="true" label_message_key="agent.table.IP">
					<input type="text" class="input-medium"  id="ip_input"
						rel='modal_popover'
						data-html='true'
						data-content='<@spring.message "perfTest.configuration.addHost.inputTargetIp"/>'
						title='IP'
						data-placement='right'
						/>
				</@control_group>

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
		var successDomain = true;
		var $domain = $("#domain_input");
		var domainEmpty = checkEmptyByObj($domain);		
		if (!domainEmpty) {
			var rule = "^([a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,3}$";
			var str = $domain.val();
			successDomain = checkStringFormat(str, rule);
			if (successDomain) {
				arr.push(str);
			}
		} 
		markInput($domain, successDomain, '<@spring.message "perfTest.configuration.addHost.inputTargetDomain"/>');

		var successIp = true;
		var $ip = $("#ip_input");
		var ipEmpty = checkEmptyByObj($ip);
		if (!ipEmpty) {
			successIp = isIPByObj($ip);
			if (successIp) {
				arr.push($ip.val());
			}
		}
		markInput($ip, successIp, '<@spring.message "perfTest.configuration.addHost.inputTargetIp"/>');
		if (ipEmpty && domainEmpty) {
			return false;
		}
		return successDomain && successIp;
	}

	$(document).ready(function() {
		$("input[rel='modal_popover']").popover({trigger: 'focus', container:'#add_host_modal'});

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

		$(".div-host").on("click", ".icon-remove-circle", function() {
			deleteHost($(this));
		});


		$(".div-host").on("click", "p a[id='hostID']", function() {
			var url = "${req.getContextPath()}/monitor/info?ip=" + $.trim($(this).text());
			$("#target_info_modal_container").load(url, function() {
				$('#target_info_modal').modal('show').css({"margin-top":"-80px"});
			});
		});

		$('#target_info_modal').on('hidden', function() {
			if (timer) {
				window.clearInterval(timer);
			}
			$.get("${req.getContextPath()}/monitor/close?ip=" + $("#target_IP").val());
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
				+ " </a> <a class='pointer-cursor'><i class='icon-remove-circle'></i></a></p><br style='line-height:0px'/>"
	}

	function initHosts(newHosts) {
		if (newHosts != undefined) {
			newHosts = $.trim(newHosts);
			$("#target_hosts").val(newHosts);
			if (!newHosts) {
				$(".div-host").html("");
				return;
			}
		} else if (checkEmptyByID("target_hosts")) {
			return;
		}

		var hosts = $("#target_hosts").val().split(",");
		$(".div-host").html($.map(hosts, function(val) {
			val = $.trim(val);
			if (val) {
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
