var plotObj;

$(document).ready(function() {
	$("#use_ramp_up").on("click", function() {
		updateRampupChart();
	});
	
	$("#init_processes, #init_sleep_time, #process_increment, #process_increment_interval").on(
		"change", function() {
			updateRampupChart();
	});
});

function disableRampup() {
	$('#init_processes').val(0);
	$('#init_processes').attr("readonly", "readonly");
	$('#init_sleep_time').attr("readonly", "readonly");
	$('#process_increment').attr("readonly", "readonly");
	$('#process_increment_interval').attr("readonly", "readonly");
}

function enableRampup() {
	$('#init_processes').removeAttr("readonly");
	$('#init_sleep_time').removeAttr("readonly");
	$('#process_increment').removeAttr("readonly");
	$('#process_increment_interval').removeAttr("readonly");
}

function updateRampupChart() {
	var $processes = $('#processes');
	var $processInc = $('#process_increment');
	var $initialProcesses = $('#init_processes');
	var $internalTime = $('#process_increment_interval');

	var processes = parseInt($processes.val(), 10);
	var processInc = parseInt($processInc.val(), 10);
	var initialProcesses = parseInt($initialProcesses.val(), 10);
	var internalTime = parseInt($internalTime.val(), 10);
	if (isNaN(initialProcesses) || isNaN(processes) || isNaN(processInc) || isNaN(internalTime)) {
		return;
	}
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
		$("#message_div").empty();
		modified = false;
	}

	var steps = (processes - initialProcesses) / processInc;
	if (steps == 0) {
		steps = 1;
	}

	var initialSleepTime = parseInt($('#init_sleep_time').val());

	if (isNaN(initialSleepTime)) {
		return;
	}
	var maxY = parseInt((processes / 5) + 1) * 5;
	var seriesArray = [];
	
	if ($("#use_ramp_up")[0].checked) {
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
		
		$("#rampup_chart").empty();
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
	plotObj = $.jqplot("rampup_chart", [data], {
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
				min : 1,
				pad : 0,
				tickOptions : {
					show : true,
					formatter : function(format, value) {
                        value = value || 0;
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
                        value = value || 0;
						return (value).toFixed(0);
					}
				}
			}
	
		}
	});
}