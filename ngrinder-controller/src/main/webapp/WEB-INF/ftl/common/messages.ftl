<div class="alert messageDiv hidden" id="messageDiv"></div>
<div class="alert messageDiv alert-error hidden" id="errorDiv">
	<button class="close" id="errorDivBtn">&times;</button>
	<h4 class="alert-heading">Warning!</h4>
	<span></span>
</div>
<script type="text/javascript">
	$(document).ready(function() {
		$("#errorDivBtn").click(function() {
			$("#errorDiv").fadeOut();
		});
	});
	
	function showMsg(color, message) {
		var $msgDiv = $('#messageDiv');
		$msgDiv.addClass(color);
		$msgDiv.html(message);
		$msgDiv.fadeIn("fast");
		setTimeout(function() {
			$msgDiv.fadeOut('fast');
			$msgDiv.html("");
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
		var $msgDiv = $('#errorDiv span');
		$msgDiv.html(message);
		$msgDiv.fadeIn("fast");
	}
</script>