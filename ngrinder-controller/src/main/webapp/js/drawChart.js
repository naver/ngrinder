var formatAmount = function(format, value) {
	if (value < 1024) {
		return value;
	} else if (value < 1048576) {
		return (value/1024).toFixed(2) + "K";
	} else if (value < 1073741824) {
		return (value/1048576).toFixed(2) + "M";
	} else {
		return (value/1073741824).toFixed(2) + "G";
	}	
};

var formatPercentage = function(format, value) {
	return value.toFixed(2) + "%";
};

function drawChart(title, containerId, data, formatYaxis, yLabel, startTime, interval) {
	//title, containerId and data is necessary.
	//formatYaxis is the formatter function for y-axis, can be set undefined means don't format.
	//startTime is a Date object.
	//interval is second amount.
	//startTime and interval are optional.
	var plotObj = $.jqplot(containerId, [ eval(data) ], {
		title : {
			text : title,
			fontSize : '16pt'
		},
		seriesDefaults : {
			markerRenderer : $.jqplot.MarkerRenderer,
			markerOptions : {
				size : 5.0,
				color : '#555555'
			},
			lineWidth : 2.0
		},
		axes : {
			xaxis : {
				tickRenderer : $.jqplot.AxisTickRenderer,
				tickOptions : {
					show : false,
					formatter : function(format, value) {
						if (startTime) {
							if (interval) {
								return new Date(startTime.getTime() + value * interval * 1000).toLocaleString();
							} else {
								return new Date(startTime.getTime() + value * 1000).toLocaleString();
							}
						} else {
							return value;
						}
					}
				}
			},
			yaxis : {
				tickRenderer : $.jqplot.CanvasAxisTickRenderer,
				labelRenderer : $.jqplot.CanvasAxisLabelRenderer,
				label : yLabel,
				labelOptions : {
					fontFamily : 'Helvetica',
					fontSize : '12pt'
				},
				tickOptions : {
					angle : -30,
					formatter : formatYaxis
				},
				pad : 0,
				numberTicks : 8
			},

		},
		highlighter : {
			show : true,
			sizeAdjust : 3
		},
		cursor : {
			showTooltip : false,
			show : true,
			zoom : true
		}
	});

	return plotObj;
}