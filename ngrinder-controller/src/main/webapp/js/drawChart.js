function drawChart(title, containerId, data, yLabel, startTime, interval) {
	//startTime is a Date object.
	//interval is second amount.
	//startTime and interval are optional.
	var plotObj = $.jqplot(containerId, [eval(data)], {
        title: {
			text: title,
			fontSize: '16pt'
		},
		axes: {
            xaxis: {
				tickRenderer : $.jqplot.AxisTickRenderer,
                tickOptions: {
					show: false,
					formatter: function(format, value) {
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
            yaxis: {
				tickRenderer : $.jqplot.CanvasAxisTickRenderer,
				labelRenderer : $.jqplot.CanvasAxisLabelRenderer,
				label : yLabel,
				labelOptions : {
					fontFamily : 'Helvetica',
					fontSize : '12pt'
				},
				tickOptions : {
					angle : -30
				},
				pad: 0,
				numberTicks: 8
            },
			
        },
		highlighter: {
			show: true,
			sizeAdjust: 3
		},
        cursor:{
			showTooltip:false,
            show: true,
            zoom: true
        }
    });
	
	return plotObj;
}