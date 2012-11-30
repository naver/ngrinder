<div class="div-host" 
data-original-title="<@spring.message "perfTest.configuration.targetHost"/>"
data-content='<@spring.message "perfTest.configuration.targetHost.help"/>'
rel="popover" placement="bottom"></div>
<input type="hidden" name="targetHosts" id="hostsHidden" value="${(targetHosts)!}"> 
<a class="btn pull-right btn-mini addhostbtn" data-toggle="modal" href="#addHostModal">   
	<@spring.message "perfTest.configuration.add"/>
</a>

<!-- modal dialog -->
<div class="modal fade" id="addHostModal">
	<div class="modal-header">
		<a class="close" data-dismiss="modal">&times;</a>
		<h3>
			<@spring.message "perfTest.configuration.addHost"/> <small><@spring.message "perfTest.configuration.pleaseInputOneOptionAtLeast"/></small>
		</h3>
	</div>
	<div class="modal-body">
		<div class="form-horizontal">
			<fieldset>
				<div class="control-group">
					<label for="domainInput" style="text-align: right;" class="control-label"><@spring.message "perfTest.configuration.domain"/></label>
					<div class="controls" >
						<input type="text" id="domainInput"> <span class="help-inline"></span>
					</div>
				</div>
				<div class="control-group">
					<label for="ipInput" style="text-align: right;" class="control-label">IP</label>
					<div class="controls" >
						<input type="text" id="ipInput"> <span class="help-inline"></span>
					</div>
				</div>
			</fieldset>
		</div>
	</div>
	<div class="modal-footer">
		<a class="btn btn-primary" id="addHostBtn"><@spring.message "perfTest.configuration.add"/></a>
		<a href="#addHostModal" class="btn" data-toggle="modal"><@spring.message "common.button.cancel"/></a>
	</div>
</div>

<script>
	 /*function validateHostForm() {
	      $("#ipInput").blur(function () {
	          var $this = $(this);
	          if (!checkEmptyByObj($this)) {
	              markInput($this, isIPByObj($this), "<@spring.message "perfTest.configuration.addHost.inputTargetIp"/>");
	          }
	      });
	      $("#domainInput").blur(function () {
	          var $this = $(this);
	          if (!checkEmptyByObj($this)) {
	              var rule = "^([a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,6}$";
	              var str = $this.val();
	              markInput($this, checkStringFormat(str, rule), "<@spring.message "perfTest.configuration.addHost.inputTargetDomain"/>");
	          }
	      });
	}*/
	function validateHost() {
	      var ipValue = $("#ipInput");
          if (!checkEmptyByObj(ipValue)) {
              if(!isIPByObj(ipValue)){
                  markInput(ipValue, false, "<@spring.message "perfTest.configuration.addHost.inputTargetIp"/>");
                  return false;
              }
          }
          var domainValue = $("#domainInput");
          if (!checkEmptyByObj(domainValue)) {
              var rule = "^([a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,3}$";
              var str = domainValue.val();
              if(!checkStringFormat(str, rule)){
                  markInput(domainValue, false, "<@spring.message "perfTest.configuration.addHost.inputTargetDomain"/>");
                  return false;
              }
          }
          return true;
    }
	  
	$(document).ready(function() {
	      // validateHostForm();
		  
	      $("#addHostBtn").click(function () {
	          if(!validateHost()){
	              return;
	          }
	          
	          var content = [];
	             
	          if (!checkEmptyByID("domainInput")) {
	              content.push(getValueByID("domainInput"));
	          }
	          if (!checkEmptyByID("ipInput")) {
	              content.push(getValueByID("ipInput"));
	          }

	          if (content.length == 0) {
	              $("#addHostModal small").addClass("errorColor");
	              return;
	          }

			  if (!$("#domainInput").valid() &&  !$("#ipInput").valid()) {
		  	     return;
		      } 
	          var contentStr = content.join(":");
	          
	          $(".div-host").html($(".div-host").html() + hostItem(contentStr));
	          
	          updateHostHiddenValue();
	          $("#addHostModal").modal("hide");
	          $("#addHostModal small").removeClass("errorColor");
	      });
	      
	      
	      $(".icon-remove-circle").live("click", function() {
	      	deleteHost($(this));
	      });
	      initHosts();
	  });

      function updateHostHiddenValue() {
          var contentStr = $(".host").map(function() {
          		return $.trim($(this).text());    
          }).get().join(",");
          
          $("#hostsHidden").val(contentStr);
      }

      function hostItem(content) {
          return "<p class='host'>" + content + "  <a href='javascript:void(0);'><i class='icon-remove-circle'></i></a></p><br style='line-height:0px'/>"
      }

      function initHosts(newHosts) {
      	  if (newHosts != undefined) {
      	  	  newHosts = $.trim(newHosts);
      	  	  $("#hostsHidden").val(newHosts);
      	  	  if (newHosts == "") {
      	  	  	  $(".div-host").html("");
      	  	  	  return;
      	  	  }
      	  } else if (checkEmptyByID("hostsHidden")) {
              return;
          }
          
		  var hosts = $("#hostsHidden").val().split(",");
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