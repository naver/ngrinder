<div class="row" id="footDiv" >
	<div class="span12 center">
		<hr>
		<footer>
			<p> nGrinder ver ${nGrinderVersion} © 2012 <a href="http://www.nhncorp.com/nhn/index.nhn" target="_blank"><@spring.message "common.nhnCorp"/></a> <@spring.message "common.nhnReserved"/></p>
		</footer>
	</div>
</div>

<script type="text/javascript">
	$(document).ready(function() {
		resetFooter();
	});
	
	function resetFooter() {
		setTimeout("reset()", 500);
	}
	
	function reset() {
		var $elem = $("#footDiv");
		var i = $elem[0];
		if (i) {
			var ll = i.offsetTop;
			var prt = i.offsetParent;
			while(i != null && i != prt){
				ll += prt.offsetTop;
				i = prt;
			}
			
			if (ll < 800) {
				$elem.addClass("footDiv");
			} else {
				$elem.removeClass("footDiv");
			}	
		}	
	}
</script>
