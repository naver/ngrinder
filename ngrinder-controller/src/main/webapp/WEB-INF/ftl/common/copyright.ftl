<div class="row" id="footDiv" >
	<div class="span12" style="text-align:center">
		<hr>
		<footer>
			<p> © 2012 <a href="http://www.nhncorp.com/nhn/index.nhn" target="_blank"><@spring.message "common.nhnCorp"/></a> <@spring.message "common.nhnReserved"/></p>
		</footer>
	</div>
</div>

<script type="text/javascript">
	$(document).ready(function() {
		resetFooter();
	});
	
	function resetFooter() {
		var $elem = $("#footDiv");
		var i = $elem[0];
		if (i) {
			var ll = i.offsetTop;
			while(i != null && i != i.offsetParent){
				ll += i.offsetTop;
				i = i.offsetParent;
			}
			
			if (ll < 800) {
				$elem.addClass("footDiv");
			} else {
				$elem.removeClass("footDiv");
			}	
		}	
	}
</script>