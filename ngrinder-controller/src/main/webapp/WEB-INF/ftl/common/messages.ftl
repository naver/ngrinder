<div class="alert message-div" id="message_div" style="display:none"></div>
<div class="alert message-div alert-error" id="error_msg_div" style="display:none">
	<button class="close" id="error_msg_div_btn">&times;</button>
	<h4 class="alert-heading">ERROR</h4>
	<span style="margin-left:20px"></span>
</div>
<div class="progress progress-striped active message-div" id="progress_bar_div" style="display:none">
  <div class="bar" style="width: 100%;"></div>
</div>
<script type="text/javascript">
	var msgTimeout;
	$(document).ready(function() {
		$("#error_msg_div_btn").click(function() {
			var $div = $("#error_msg_div");
			$div.fadeOut(100);
			$div.children("span").html("");
		});
	});
	
	function showMsg(color, message) {
		var $msgDiv = $('#message_div');
		$msgDiv.hide();
		$msgDiv.addClass(color);
		$msgDiv.html(message);
		$msgDiv.fadeIn(100);
		clearTimeout(msgTimeout);
		msgTimeout = setTimeout(function() {
			$msgDiv.fadeOut(100);
			$msgDiv.removeClass(color);
		}, 3000);
	}
	
	function hideMsg() {
		if ($('#message_div:visible')[0]) {
			clearTimeout(msgTimeout);
			var $msgDiv = $('#message_div');
			$msgDiv.fadeOut(100);
			$msgDiv.removeClass("alert-success alert-info alert-block");
		}
	}
	
	function showSuccessMsg(message) {
		showMsg("alert-success", message);
	}
	
	function showInformation(message) {
		showMsg("alert-info", message);
	}
	
	function showWarning(message) {
		showMsg("alert-block", message);
	}
	
	function showErrorMsg(message) {
		var $div = $("#error_msg_div");
		$div.hide();
		$div.children("span").html(message);
		$div.fadeIn(100);
	}
	
	function showProgressBar(msg) {
		$("#progress_bar_div div").text(msg);
		$("#progress_bar_div").fadeIn(500);
	}
	
	function hideProgressBar() {
		$("#progress_bar_div").fadeOut(500);
		$("#progress_bar_div").children("div").text("");
	}
</script>