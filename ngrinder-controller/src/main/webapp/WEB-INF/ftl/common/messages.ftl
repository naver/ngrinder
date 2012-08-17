<div class="alert messageDiv hidden" id="messageDiv"></div>
<div class="alert messageDiv alert-error hidden" id="errorDiv">
	<button class="close" id="errorDivBtn">&times;</button>
	<h4 class="alert-heading">Error!</h4>
	<span></span>
</div>
<script type="text/javascript">
	$(document).ready(function() {
		$("#errorDivBtn").click(function() {
			$("#errorDiv").fadeOut('fast');
		});
	});
	
	function showMsg(color, message) {
		var $msgDiv = $('#messageDiv');
		$msgDiv.hide();
		$msgDiv.addClass(color);
		$msgDiv.html(message);
		$msgDiv.fadeIn(100);
		setTimeout(function() {
			$msgDiv.fadeOut(100);
			$msgDiv.removeClass(color);
		}, 3000);
	}
	
	function showSuccessMsg(message) {
		showMsg("alert-success", message);
	}
	
	function showInformation(message) {
		showMsg("alert-info", message);
	}
	
	function showErrorMsg(message) {
		var $div = $("#errorDiv");
		$div.hide();
		$div.children("span").html(message);
		$div.fadeIn("fast");
	}
</script>