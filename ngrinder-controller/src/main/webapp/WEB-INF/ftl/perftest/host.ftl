<div class="div-host"></div>
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
					<label for="domainInput" class="control-label"><@spring.message "perfTest.configuration.domain"/></label>
					<div class="controls">
						<input type="text" id="domainInput"> <span class="help-inline"></span>
					</div>
				</div>
				<div class="control-group">
					<label for="ipInput" class="control-label">IP</label>
					<div class="controls">
						<input type="text" id="ipInput"> <span class="help-inline"></span>
					</div>
				</div>
			</fieldset>
		</div>
	</div>
	<div class="modal-footer">
		<a class="btn btn-primary" id="addHostBtn"><@spring.message "perfTest.configuration.add"/></a>
	</div>
</div>

<script>
	$(document).ready(function() {
	      $("#addHostBtn").click(function () {
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
      	  var content = [];
          $(".host").each(function(index, value) {
          		content.push($.trim($(this).text()));    
          });
          
          contentStr = content.join(",");
          $("#hostsHidden").val(contentStr);
      }

      function hostItem(content) {
          return "<p class='host'>" + content + "  <a href='javascript:void(0);'><i class='icon-remove-circle'></i></a></p><br style='line-height:0px'/>"
      }

      function initHosts() {
          if (checkEmptyByID("hostsHidden")) {
              return;
          }
		  var hosts = $("#hostsHidden").val().split(",");
		  $.each(hosts, function(index, each) {
		  	$(".div-host").html( $(".div-host").html() + hostItem(each) );
		  });
      }
	      
	  function deleteHost(element) {
		  var elem = element.parents("p");
		  elem.next("br").remove();
	      elem.remove();
	      updateHostHiddenValue();
	  }
</script>