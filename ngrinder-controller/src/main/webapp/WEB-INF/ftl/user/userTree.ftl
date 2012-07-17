<script src="${req.getContextPath()}/plugins/tree/jquery.ztree.all-3.2.js"></script>
<link rel="stylesheet" href="${req.getContextPath()}/plugins/tree/zTreeStyle.css" type="text/css">

<script type="text/javascript">
	var setting = {
		view: {
			showLine: true,
			showIcon : false
		},
		data: {
			simpleData: {
				enable: true
			}
		},
		callback: {
					onClick : zTreeOnClick
		}
	};

	function zTreeOnClick(event, treeId, treeNode){
		var nodeId = treeNode.id;
		if(nodeId == 'ADMIN'||nodeId == 'USER'||nodeId == 'SUPER'||nodeId == 'SUPER_USER' ||nodeId == 'SYSTEM_USER'){
			    document.location.href ="${req.getContextPath()}/user/list?roleName="+nodeId;
		}else if(nodeId != 'all'){
				document.location.href ="${req.getContextPath()}/user/detail?userId="+nodeId;
		}else{
				document.location.href ="${req.getContextPath()}/user/list";
		}
					
	}
	
	var treeData = ${jsonStr}
	
	$(document).ready(function(){
		$.fn.zTree.init($("#treeDemo"), setting, treeData);
	});
</script>
		
<div id="userTree_div_id">
	<ul id="treeDemo" class="ztree"></ul>	
</div>	
										