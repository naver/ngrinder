var plotObj;

$(document).ready(function () {
	$("#use_ramp_up").on("click", function () {
		updateRampupChart();
	});

	$("#init_processes, #init_sleep_time, #process_increment, #process_increment_interval, #ramp_up_type").on(
		"change", function () {
			updateRampupChart();
		});
});

function disableRampup() {
	var $initProcesses = $('#init_processes');
	$initProcesses.val(0);
	$initProcesses.attr("readonly", "readonly");
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
	var $base;
	var $factor;
	if ($("#ramp_up_type").val() == "PROCESS") {
		$base = $("#processes");
		$factor = $("#threads");
	} else {
		$base = $('#threads');
		$factor = $("#processes");
    }
	var $processInc = $('#ramp_up_step');
	var $initialProcesses = $('#ramp_up_init_count');
	var $internalTime = $('#ramp_up_increment_interval');

	var factorVar =  parseInt($factor.val(), 10);
	var destination = parseInt($base.val(), 10) * factorVar;
	var increment = parseInt($processInc.val(), 10) * factorVar;
	var initialCount = parseInt($initialProcesses.val(), 10) * factorVar;
	var internalTime = parseInt($internalTime.val(), 10);
	if (isNaN(initialCount) || isNaN(destination) || isNaN(increment) || isNaN(internalTime)) {
		return;
	}
	var modified = false;
	if (initialCount > destination) {
		$initialProcesses.val(1);
		modified = true;
		return;
	}
	if (initialCount < destination && increment == 0) {
		$processInc.val(1);
		modified = true;
		return;
	}

	if (modified) {
		$("#message_div").empty();
	}

	var steps = (destination - initialCount) / increment;
	if (steps == 0) {
		steps = 1;
	}

	var initialSleepTime = parseInt($('#ramp_up_init_sleep_time').val());

	if (isNaN(initialSleepTime)) {
		return;
	}

	var seriesArray = [];

	if ($("#use_ramp_up")[0].checked) {
		enableRampup();
		var curX = initialSleepTime;
		var curY = initialCount;
		if (initialSleepTime > 0) {
			seriesArray.push([0, 0]);
			seriesArray.push([initialSleepTime, 0]);
		}
		seriesArray.push([curX + 0.01, curY]);
		curX = curX + internalTime;
		seriesArray.push([curX, curY]);

		for (var step = 1; step <= Math.ceil(steps); step++) {
			curY = curY + increment;
			if (curY > destination) {
				curY = destination;
			}
			seriesArray.push([curX + 0.01, curY]);
			curX = curX + internalTime;
			seriesArray.push([curX, curY]);
		}

		$("#rampup_chart").empty();

		var maxX = seriesArray[seriesArray.length-1][0];
		var maxY = seriesArray[seriesArray.length-1][1];
		drawRampup(seriesArray, internalTime, maxX, maxY);
	} else {
		disableRampup();

		var curX = 0;
		for (var step = 0; step <= steps; step++) {
			seriesArray.push([curX + 0.01, destination]);
			curX = curX + internalTime;
			seriesArray.push([curX, destination]);
		}

		if (plotObj) {
			plotObj.series[0].data = seriesArray;
			plotObj.replot();
		} else {
			var maxX = seriesArray[seriesArray.length-1][0];
			var maxY = seriesArray[seriesArray.length-1][1];
			drawRampup(seriesArray, internalTime, maxX, maxY);
		}
	}
}

function drawRampup(data, intervalTime, maxX, maxY, snapX) {
	var numTicks = (Math.min(parseInt(data.length / 2) + 1, 8));
	var pointCutter = 1;
	if (parseInt(intervalTime / 1000) == (intervalTime / 1000)){
		pointCutter = 0;
	}
	plotObj = $.jqplot("rampup_chart", [data], {
		axesDefaults: {
			tickRenderer: $.jqplot.AxisTickRenderer,
			tickOptions: {
				showMark: false
			}
		},
		seriesDefaults: {
			showMarker: false,
			lineWidth: 1.5
		},

		axes: {
			xaxis: {
				min: 1,
				max: maxX,
				pad: 0,
				numberTicks: numTicks,
				tickOptions: {
					show: true,
					formatter: function (format, value) {
						value = value || 0;
						return (value / 1000).toFixed(pointCutter);
					}
				}
			},
			yaxis: {
				min: 0,
				pad: 10,
				max: maxY,
				numberTicks: numTicks - 1,
				tickOptions: {
					show: true,
					formatter: function (format, value) {
						value = value || 0;
						return (value).toFixed(0);
					}
				}
			}

		}
	});
}