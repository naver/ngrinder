var formatMemory = function (format, value) {
    if (value === null) {
        return "";
    } else if (value < 1024) {
        return value.toFixed(1) + "K ";
    } else if (value < 1048576) { //1024 * 1024
        return (value / 1024).toFixed(1) + "M ";
    } else {
        return (value / 1048576).toFixed(2) + "G ";
    }
};

var formatNetwork = function (format, value) {
    if (value === null) {
        return "";
    } else if (value < 1024) {
        return value.toFixed(1) + "B ";
    } else if (value < 1048576) { //1024 * 1024
        return (value / 1024).toFixed(1) + "K ";
    } else {
        return (value / 1048576).toFixed(2) + "M ";
    }
};

var formatPercentage = function (format, value) {
    if (value === null) {
        return "";
    } else if (value < 10) {
        return value.toFixed(1) + "% ";
    } else {
        return value.toFixed(0) + "% ";
    }
};

// data is in Byte
var formatMemoryInByte = function (format, value) {
    if (value === null) {
        return "";
    } else if (value < 1024) {
        return value.toFixed(1) + "B ";
    } else if (value < 1048576) { //1024 * 1024
        return (value / 1024).toFixed(1) + "K ";
    } else if (value < 1073741824) { //1024 * 1024 * 1024
        return (value / 1048576).toFixed(2) + "M ";
    } else {
        return (value / 1073741824).toFixed(3) + "G ";
    }
};

function formatTimeForXaxis(timeInSecond) {
    if (timeInSecond < 0) {
        timeInSecond = 0;
    }
    var hour = parseInt((timeInSecond % (60 * 60 * 24)) / 3600);
    var min = parseInt((timeInSecond % 3600) / 60);
    var sec = parseInt(timeInSecond % 60);
    if (sec < 10) {
        sec = "0" + sec;
    }
    var display = min + ":" + sec;
    if (min < 10) {
        display = '0' + display;
    }
    if (hour > 0) {
        display = hour + ":" + display;
    }
    return display;
}

function getMaxValue(data) {
    if (data == undefined) {
        return undefined;
    }

    var values = preparedData(data);
    var ymax = 0;
    for (var i = 0; i < values.length; i++) {
        for (var j = 0; j < values[i].length; j++) {
            if (values[i][j] !== null && values[i][j] > ymax) {
                ymax = values[i][j];
            }
        }
    }
    return ymax;
}

function DummyChart() {

}

DummyChart.prototype.replot = function () {
    // Do nothing
};

var dummyChart = new DummyChart();

// check whether the data is proper
function checkDataForChart(data) {
    if (data == null || data == undefined || data.length == 0) {
        return false;
    } else {
        return true;
    }
}

function checkDataNotZeroForChart(data) {
    if (checkDataForChart(data)) {
        for (value in data) {
            if (value > 0) {
                return true;
            }
        }
    }
    return false;
}

function checkDataAndDraw(containerId, data, formatYaxis, interval) {
    if (checkDataNotZeroForChart(data)) {
        drawChart(containerId, data, formatYaxis, interval)
    }
}


function drawChart(containerId, data, formatYaxis, interval) {
    //title, containerId and data is necessary.
    //formatYaxis is the formatter function for y-axis, can be set undefined means don't format.
    //interval is second amount, interval is optional.
    if (data == undefined) {
        return dummyChart;
    }
    var values = preparedData(data);
    var dataCnt = values[0].length;
    if (dataCnt == 0) {
        return dummyChart;
    }
    var ymax = getMaxValue(data);
    if (ymax < 5) {
        ymax = 5;
    }

    ymax = parseInt((ymax / 5) + 1) * 6;

    if (formatYaxis === undefined || formatYaxis == null) {
        formatYaxis = function (format, value) {
            return value.toFixed(0);
        };
    }

    if (interval == undefined || interval == 0 || !$.isNumeric(interval)) {
        interval = 1;
    }

    var plotObj = $.jqplot(containerId, values, {

        gridPadding: {top: 20, right: 20, bottom: 35, left: 60},

        seriesDefaults: {
            markerRenderer: $.jqplot.MarkerRenderer,
            markerOptions: {
                size: 2.0,
                color: '#555555'
            },
            lineWidth: 1.0
        },
        axes: {
            xaxis: {
                min: 1,
                max: dataCnt,
                pad: 2,
                numberTicks: 10,
                tickOptions: {
                    show: true,
                    formatter: function (format, value) {
                        return formatTimeForXaxis(parseInt((value - 1) * interval));
                    }
                }
            },
            yaxis: {
                labelOptions: {
                    fontFamily: 'Helvetica',
                    fontSize: '10pt'
                },
                tickOptions: {
                    formatter: formatYaxis
                },
                max: ymax,
                min: 0,
                numberTicks: 7,
                pad: 0,
                show: true
            }
        },
        highlighter: {
            show: true,
            sizeAdjust: 3,
            tooltipAxes: 'y',
            formatString: '<table class="jqplot-highlighter"><tr><td>%s</td></tr></table>'
        },
        cursor: {
            showTooltip: false,
            show: true,
            zoom: true
        }
    });

    return plotObj;
}

//data is an array object.
function replotChart(plotObj, data, ymax, interval) {
    if (data == undefined) {
        return dummyChart;
    }
    var cache = [];
    var dataCnt = data.length;
    for (var i = 0; i < dataCnt; i++) {
        cache.push([i + 1, data[i]]);
    }
    plotObj.series[0].data = cache;

    var xFormatter;
    if (interval) {
        xFormatter = function (format, value) {
            return formatTimeForXaxis(parseInt((value - 1) * interval));
        };
    } else {
        xFormatter = plotObj.axes.xaxis.tickOptions.formatter;
    }
    var prevYFormatter = plotObj.axes.yaxis.tickOptions.formatter;
    plotObj.resetAxesScale();

    if (ymax < 5) {
        ymax = 5;
    }

    ymax = parseInt((ymax / 5) + 1) * 6;

    plotObj.axes.yaxis.min = 0;
    plotObj.axes.yaxis.max = ymax;
    plotObj.axes.yaxis.numberTicks = 7;
    plotObj.axes.yaxis.tickOptions = {
        show: true,
        formatter: prevYFormatter
    };

    plotObj.axes.xaxis.min = 0;
    plotObj.axes.xaxis.max = dataCnt;
    plotObj.axes.xaxis.numberTicks = 10;
    plotObj.axes.xaxis.tickOptions = {
        show: true,
        formatter: xFormatter
    };

    plotObj.replot();
}

function preparedData(data) {
    var values = [];
    if ((data instanceof Array) == true) {
        values = [ data ];
    } else {
        values = [ eval(data) ];
    }

    return values;
}

function getMultiPlotMaxValue(data) {
    var ymax = 0;
    for (var i = 0; i < data.length; i++) {
        for (var j = 0; j < data[i].length; j++) {
            if (data[i][j] > ymax) {
                ymax = data[i][j];
            }
        }
    }
    return ymax;

}


function drawMultiPlotChart(containerId, data, labels, interval) {
    if (data == undefined || !(data instanceof Array) || data.length == 0) {
        return dummyChart;
    }

    var values;
    if (data[0] instanceof Array) {
        values = data;
    } else {
        var temp = [];
        for (var i = 0; i < data.length; i++) {
            temp.push(eval(data[i]));
        }
        values = temp;
    }

    var dataCnt = values[values.length - 1].length;
    if (dataCnt == 0) {
        return dummyChart;
    }

    var ymax = getMultiPlotMaxValue(values);
    if (ymax < 5) {
        ymax = 5;
    }
    ymax = parseInt((ymax / 5) + 1) * 6;

    if (interval == undefined || interval == 0 || !$.isNumeric(interval)) {
        interval = 1;
    }

    var plotObj = $.jqplot(containerId, values, {

        gridPadding: {top: 20, right: 20, bottom: 35, left: 60},
        seriesDefaults: {
            markerRenderer: $.jqplot.MarkerRenderer,
            markerOptions: {
                size: 2.0,
                color: '#555555'
            },
            lineWidth: 1.0
        },
        axes: {
            xaxis: {
                min: 1,
                max: dataCnt,
                pad: 0,
                numberTicks: 10,
                tickOptions: {
                    show: true,
                    formatter: function (format, value) {
                        return formatTimeForXaxis(parseInt((value - 1) * interval));
                    }
                }
            },
            yaxis: {
                labelOptions: {
                    fontFamily: 'Helvetica',
                    fontSize: '10pt'
                },
                tickOptions: {
                    formatter: function (format, value) {
                        return value.toFixed(0);
                    }
                },
                max: ymax,
                min: 0,
                numberTicks: 7,
                pad: 3,
                show: true
            }
        },
        highlighter: {
            show: true,
            sizeAdjust: 3,
            tooltipAxes: 'y',
            formatString: '<table class="jqplot-highlighter"><tr><td>%s</td></tr></table>'
        },
        cursor: {
            showTooltip: false,
            show: true,
            zoom: true
        },
        legend: {
            renderer: $.jqplot.EnhancedLegendRenderer,
            show: true,
            placement: "insideGrid",
            labels: labels,
            location: "ne",
            rowSpacing: "2px",
            rendererOptions: {
                seriesToggle: 'normal'
            }
        }
    });

    return plotObj;
}


function drawListPlotChart(containerId, data, labels, interval) {
    if (data == undefined || !(data instanceof Array) || data.length == 0) {
        return dummyChart;
    }

    var values;
    if (data[0] instanceof Array) {
        values = data;
    } else {
        var temp = [];
        for (var i = 0; i < data.length; i++) {
            temp.push(eval(data[i]));
        }
        values = temp;
    }

    var dataCnt = values[values.length - 1].length;
    if (dataCnt == 0) {
        return dummyChart;
    }

    var ymax = getMultiPlotMaxValue(values);
    if (ymax < 5) {
        ymax = 5;
    }
    ymax = parseInt((ymax / 5) + 1) * 6;

    if (interval == undefined || interval == 0 || !$.isNumeric(interval)) {
        interval = 1;
    }

    var plotObj = $.jqplot(containerId, values, {

        gridPadding: {top: 15, right: 10, bottom: 30, left: 40},
        seriesDefaults: {
            markerRenderer: $.jqplot.MarkerRenderer,
            markerOptions: {
                size: 2.0,
                color: '#555555'
            },
            lineWidth: 1.0
        },
        axes: {
            xaxis: {
                min: 1,
                max: dataCnt,
                pad: 0,
                numberTicks: 10,
                tickOptions: {
                    show: true,
                    formatter: function (format, value) {
                        return formatTimeForXaxis(parseInt((value - 1) * interval));
                    }
                }
            },
            yaxis: {
                labelOptions: {
                    fontFamily: 'Helvetica',
                    fontSize: '10pt'
                },
                tickOptions: {
                    formatter: function (format, value) {
                        return value.toFixed(0);
                    }
                },
                max: ymax,
                min: 0,
                numberTicks: 7,
                pad: 3,
                show: true
            }
        },
        highlighter: {
            show: true,
            sizeAdjust: 3,
            tooltipAxes: 'y',
            formatString: '<table class="jqplot-highlighter"><tr><td>%s</td></tr></table>'
        },
        cursor: {
            showTooltip: false,
            show: true,
            zoom: true
        },
        legend: {
            renderer: $.jqplot.EnhancedLegendRenderer,
            show: true,
            placement: "outsideGrid",
            labels: labels,
            location: "ne",
            rendererOptions: {
                seriesToggle: 'normal'
            }
        }
    });

    return plotObj;
}

