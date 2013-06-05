<div class="div-host" 
	rel='popover' 
	title='<@spring.message "perfTest.configuration.targetHost"/>'
	data-html='true'
	data-content='<@spring.message "perfTest.configuration.targetHost.help"/>'
	data-placement='bottom'
	>
</div>
<input type="hidden" name="targetHosts" id="targetHosts" value="${(targetHosts)!}"> 
<a class="btn pull-right btn-mini addhostbtn" data-toggle="modal" href="#addHostModal">   
	<@spring.message "perfTest.configuration.add"/>
</a>

<!-- modal dialog -->
<div class="modal hide fade" id="addHostModal" tabindex="-1" role="dialog"  aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-header">
    	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
		<h4>
			<@spring.message "perfTest.configuration.addHost"/> <small><@spring.message "perfTest.configuration.pleaseInputOneOptionAtLeast"/></small>
		</h4>
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
		<button class="btn btn-primary" id="addHostBtn"><@spring.message "perfTest.configuration.add"/></button>
		<button class="btn" data-dismiss="modal"><@spring.message "common.button.cancel"/></button>
	</div>
</div>

<div class="modal hide fade" id="targetInfoModal" style="width:580px">
		<div class="modal-header" style="border: none;">
			<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		</div>
		<div class="modal-body" id="targetInfoModalContainer" style="max-height:1200px; padding-left:30px"></div>	
</div>

<script>
	function validateHost(arr) {
		  var success;
		  
          var $domain = $("#domainInput");
          if (!checkEmptyByObj($domain)) {
              var rule = "^([a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,3}$";
              var str = $domain.val();
              success = checkStringFormat(str, rule);
              markInput($domain, success, "<@spring.message "perfTest.configuration.addHost.inputTargetDomain"/>");
              if(!success){
                  return false;
              }
              arr.push(str);
          }
          
	      var $ip = $("#ipInput");
          if (!checkEmptyByObj($ip)) {
          	  success = isIPByObj($ip);
          	  markInput($ip, success, "<@spring.message "perfTest.configuration.addHost.inputTargetIp"/>");
              if(!success){
                  return false;
              }
              arr.push($ip.val());
          }
          
          
          return true;
    }
	  
	$(document).ready(function() {
	      $("#addHostBtn").click(function () {
	          var content = [];
	          
	          if(!validateHost(content)){
	              return;
	          }
	          if (content.length == 0) {
	              $("#addHostModal small").addClass("errorColor");
	              return;
	          }
	           
	          var contentHTML = hostItem(content.join(":"));
	          $(".div-host").html($(".div-host").html() + contentHTML);
	          updateHostHiddenValue();
	          
	          $("#addHostModal").modal("hide");
	          $("#addHostModal small").removeClass("errorColor");
	          $("#targetHosts").nextAll("span.help-inline").empty();
	      });
	      
	      $(".icon-remove-circle").live("click", function() {
	      	deleteHost($(this));
	      });
	      
	      $(".icon-remove-circle").live("click", function() {
	      	deleteHost($(this));
	      });
	      
	      $(".div-host p a[id='hostID']").live("click", function() {
	      	var url = "${req.getContextPath()}/monitor/info?ip="+$.trim($(this).text());
	      	
	      	$("#targetInfoModalContainer").load(url, function(){
				$('#targetInfoModal').modal('show');
			});
			
	      });
	      
	      $('#targetInfoModal').on('hidden', function () {
    			if(timer){
                   window.clearInterval(timer);
                }
                $.get("${req.getContextPath()}/monitor/close?ip="+$("#monitorIp").val());
    	  });
	          
	      initHosts();
	  });

      function updateHostHiddenValue() {
          var contentStr = $("p.host").map(function() {
          		return $.trim($(this).text());    
          }).get().join(",");
          
          $("#targetHosts").val(contentStr);
      }

      function hostItem(content) {
          return "<p class='host'><a id='hostID' href='#targetInfoModal' data-toggle='modal'> " + content + " </a> <a href='javascript:void(0);'><i class='icon-remove-circle'></i></a></p><br style='line-height:0px'/>"
      }

      function initHosts(newHosts) {
      	  if (newHosts != undefined) {
      	  	  newHosts = $.trim(newHosts);
      	  	  $("#targetHosts").val(newHosts);
      	  	  if (newHosts == "") {
      	  	  	  $(".div-host").html("");
      	  	  	  return;
      	  	  }
      	  } else if (checkEmptyByID("targetHosts")) {
              return;
          }
          
		  var hosts = $("#targetHosts").val().split(",");
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
