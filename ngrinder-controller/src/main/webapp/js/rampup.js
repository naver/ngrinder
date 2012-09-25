var plotObj;

$(document).ready(function() {
	$("#rampupCheckbox").on("click", function() {
		updateRampupChart();
	});
	
	$("#initProcesses, #initSleepTime, #processIncrement, #processIncrementInterval").on(
		"change", function() {
			updateRampupChart();
	});
});

function disableRampup() {
	$('#initProcesses').val(0);
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

function updateRampupChart() {
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
	if (initialProcesses < processes && processInc == 0) {
		$processInc.val(1);
		modified = true;
		return;
	}

	if (modified) {
		$("#messageDiv").empty();
		modified = false;
	}

	var steps = (processes - initialProcesses) / processInc;
	if (steps == 0) {
		steps = 1;
	}

	var initialSleepTime = parseInt($('#initSleepTime').val());
	var maxY = parseInt((processes / 5) + 1) * 5;
	var seriesArray = [];
	
	if ($("#rampupCheckbox")[0].checked) {
		enableRampup();

		var curX = initialSleepTime;
		var curY = initialProcesses;
		if (initialSleepTime > 0) {
			seriesArray.push([0, 0]);
			seriesArray.push([initialSleepTime, 0]);
		}
		seriesArray.push([curX  + 0.01, curY]);
		curX = curX + internalTime;
		seriesArray.push([curX, curY]);

		for ( var step = 1; step <= steps; step++) {
			curY = curY + processInc;
			if (curY > processes) {
				curY = processes;
			}
			seriesArray.push([curX  + 0.01, curY]);
			curX = curX + internalTime;
			seriesArray.push([curX, curY]);
		}
		
		$("#rampChart").empty();
		drawRampup(seriesArray, internalTime, maxY);
	} else {
		disableRampup();
		
		var curX = 0;
		for ( var step = 0; step <= steps; step++) {
			seriesArray.push([curX  + 0.01, processes]);
			curX = curX + internalTime;
			seriesArray.push([curX, processes]);
		}
		
		if (plotObj) {
			plotObj.series[0].data = seriesArray;
			plotObj.replot();
		} else {
			drawRampup(seriesArray, internalTime, maxY);
		}
	}
}

function drawRampup(data, internalTime, maxY) {
	plotObj = $.jqplot("rampChart", [data], {
		axesDefaults : {
			tickRenderer : $.jqplot.AxisTickRenderer,
			tickOptions : {
				showMark : false
			}
		},
		seriesDefaults : {
			showMarker : false,
			lineWidth : 1.5
		},
		
		axes : {
			xaxis : {
				min : 0,
				pad : 0,
				tickOptions : {
					show : true,
					formatter : function(format, value) {
						if (internalTime < 1000) {
							return (value / 1000).toFixed(1);							
						} else {
							return (value / 1000).toFixed(0);
						}
					}
				}
			},
			yaxis : {
				min : 0,
				pad : 10,
				max : maxY,
				tickOptions : {
					show : true,
					formatter : function(format, value) {
						return (value).toFixed(0);
					}
				}
			}
	
		}
	});
}