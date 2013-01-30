<div class="alert messageDiv" id="messageDiv" style="display:none"></div>
<div class="alert messageDiv alert-error" id="errorMsgDiv" style="display:none">
	<button class="close" id="errorMsgDivBtn">&times;</button>
	<h4 class="alert-heading">ERROR</h4>
	<span style="margin-left:20px"></span>
</div>
<div class="progress progress-striped active messageDiv" id="progressBarDiv" style="display:none">
  <div class="bar" style="width: 100%;"></div>
</div>
<script type="text/javascript">
	var msgTimeout;
	$(document).ready(function() {
		$("#errorMsgDivBtn").click(function() {
			var $div = $("#errorMsgDiv");
			$div.fadeOut(100);
			$div.children("span").html("");
		});
	});
	
	function showMsg(color, message) {
		var $msgDiv = $('#messageDiv');
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
		if ($('#messageDiv:visible')[0]) {
			clearTimeout(msgTimeout);
			var $msgDiv = $('#messageDiv');
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
		var $div = $("#errorMsgDiv");
		$div.hide();
		$div.children("span").html(message);
		$div.fadeIn(100);
	}
	
	function showProgressBar(msg) {
		$("#progressBarDiv div").text(msg);
		$("#progressBarDiv").fadeIn(500);
	}
	
	function hideProgressBar() {
		$("#progressBarDiv").fadeOut(500);
		$("#progressBarDiv").children("div").text("");
	}
</script>