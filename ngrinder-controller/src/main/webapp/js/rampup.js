$(document).ready(function() {
    var $rampupCheckbox = $("#rampupCheckbox");
	$rampupCheckbox.on("click", function() {
		rampup($(this)[0]);
		updateChart();
	});
	
	rampup($rampupCheckbox[0]);
	
	$("#initProcesses, #initSleepTime, #processIncrement, #processIncrementInterval").on("change", function() {
		updateChart();
	});
    
    updateChart();
});

function rampup(obj) {
	if (obj.checked) {
		enableRampup();
	} else {
		disableRampup();
	}
}

function disableRampup() {
	$('#initProcesses').val($('#processes').val());
	$('#initProcesses').attr("readonly", "readonly");
	$('#initSleepTime').attr("readonly", "readonly");
	$('#processIncrement').attr("readonly", "readonly");
	$('#processIncrementInterval').attr("readonly", "readonly");
}

function enableRampup() {
	$('#initProcesses').removeAttr("readonly");
	$('#initSleepTime').removeAttr("readonly");
	$('#processIncrement').removeAttr("readonly");
	$('#processIncrementInterval').removeAttr("readonly");
}

function updateChart(){
	var $processes = $('#processes');
	var $processInc = $('#processIncrement');
	var $initialProcesses = $('#initProcesses');
	var $internalTime = $('#processIncrementInterval');
	
    var processes = parseInt($processes.val(), 10); 
    var processInc = parseInt($processInc.val(), 10);  
    var initialProcesses = parseInt($initialProcesses.val(), 10);
    var internalTime = parseInt($internalTime.val(), 10);
    
    var modified = false;
	if (initialProcesses > processes) {
		$initialProcesses.val(1);
		modified = true;
		return;
	}
	if(initialProcesses < processes && processInc == 0){
		$processInc.val(1);
		modified = true;
		return;
    }
    
    if (modified) {
    	$("#messageDiv").empty();
    	modified = false;
	}
	var curChartDiv = "#rampChart1";
	var oldChartDiv = "#rampChart2";
    $(oldChartDiv).gchart('destroy');
    
    var temp = curChartDiv;
    curChartDiv = oldChartDiv;
    oldChartDiv = temp;
    
    var steps = (processes - initialProcesses) / processInc;
    if (steps == 0) {
    	steps = 1;
    }
    
    var initialSleepTime = parseInt($('#initSleepTime').val());
    var curX = initialSleepTime;
    var curY = initialProcesses;
    var seriesArray = [];
    if (initialSleepTime > 0) {
    	seriesArray.push([0,0]);
    	seriesArray.push([initialSleepTime,0]);
    }
    seriesArray.push([curX,curY]);
    curX = curX+internalTime;
    seriesArray.push([curX,curY]);
    
    for(var step=1; step<=steps; step++){
        curY = curY + processInc;
        if (curY > processes) {
        	curY = processes
        }
        seriesArray.push([curX, curY]);
    	curX = curX+internalTime;
        seriesArray.push([curX, curY]);
    }
    
    var lastEle = seriesArray[seriesArray.length-1];
  	var ratio = lastEle[1]/(lastEle[0]);
  	for(var index=0; index<seriesArray.length; index++){
  		seriesArray[index] = [seriesArray[index][0]*ratio, seriesArray[index][1]];
  	}
    
	var xySeries = jQuery.gchart.seriesForXYLines( 
	    [jQuery.gchart.series(seriesArray, 'blue','green')]);
	    
	var bgColor = 'ccffff';
	
	var chartAxes = [jQuery.gchart.axis('bottom', 0, lastEle[1], 'red', 'right'), 
    				 jQuery.gchart.axis('right', 0, processes, 1, 'red', 'right')];
    
    jQuery(curChartDiv).gchart( 
                            {title:"Ramp-Up Chart",
                             type: 'lineXY',
                             backgroundColor: bgColor, 
                             maxValue:jQuery.gchart.calculate, 
                             series: xySeries,
                             axes:chartAxes
                            }
                );
    jQuery(oldChartDiv).css("display","none");          
    jQuery(curChartDiv).css("display","block");         
}

function checkRampUpForm() {
	if ($('#rampupCheckbox')[0].checked) {
		
	}
	
	return true;
}