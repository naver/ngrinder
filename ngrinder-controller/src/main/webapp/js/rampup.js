$(document).ready(function() {
	var $rampupCheckbox = $("#rampupCheckbox");
	
	$rampupCheckbox.on("click", function() {
		rampup($(this));
		updateChart();
	});
	
	rampup($rampupCheckbox);
	
	$("#initProcesses, #initSleepTime, #processIncrement, #processIncrementInterval").on(
		"change", function() {
			updateChart();
	});
});

function rampup(obj) {
	if (obj[0].checked) {
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

function updateChart() {
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

	var curX = initialSleepTime;
	var curY = initialProcesses;
	var seriesArray = [];
	if (initialSleepTime > 0) {
		seriesArray.push([ 0, 0 ]);
		seriesArray.push([ initialSleepTime, 0 ]);
	}
	seriesArray.push([ curX, curY ]);
	curX = curX + internalTime;
	seriesArray.push([ curX, curY ]);

	for ( var step = 1; step <= steps; step++) {
		curY = curY + processInc;
		if (curY > processes) {
			curY = processes;
		}
		seriesArray.push([ curX, curY ]);
		curX = curX + internalTime;
		seriesArray.push([ curX, curY ]);
	}

	$("#rampChart").empty();
	
	$.jqplot("rampChart", [ seriesArray ], {
		axesDefaults : {
			tickRenderer : $.jqplot.AxisTickRenderer,
			tickOptions : {
				showMark : false
			}
		},
		seriesDefaults : {
			showMarker : false,
			lineWidth : 1.5
		}
	});
}
