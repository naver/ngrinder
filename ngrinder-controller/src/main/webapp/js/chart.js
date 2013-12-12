function Chart(containerId, data, interval, opts) {
    function prepare(data) {
        var values = data;
        for (var i = 0; i < values.length; i++) {
            if (!(values[i] instanceof Array)) {
                values[i] = eval(values[i]);
            }
        }
        return values;
    }

    function formatTimeForXaxis(timeInSecond) {
        if (timeInSecond == null || timeInSecond < 0) {
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
    this.opts = opts || {};
    this.containerId = containerId;
    this.values = prepare(data);
    this.yAxisFormatter = this.opts.yAxisFormatter || function (format, value) {
        value = value || 0;
        return value.toFixed(0);
    };
    if (this.yAxisFormatter == formatPercentage) {
        this.yLimit = 100;
    }
    interval = interval || 1;
    this.that = this;
    this.xAxisFormatter = this.opts.xAxisFormatter || function (format, value) {
        return formatTimeForXaxis(parseInt((value - 1) * interval));
    };
    this.gridPadding = this.opts.gridPadding||{top: 20, right: 20, bottom: 35, left: 60};
    this.numXTicks = this.opts.numXTicks||10;
    if (this.opts.labels !== undefined) {
        this.legend = {
            renderer: $.jqplot.EnhancedLegendRenderer,
            show: true,
            placement: "insideGrid",
            labels: opts.labels,
            location: "ne",
            rowSpacing: "2px",
            marginTop: opts.legend_margin || null,
            marginRight: opts.legend_margin || null,
            rendererOptions: {
                seriesToggle: 'normal'
            }
        };
    } else {
        this.legend = {};
    }
}

Chart.prototype.calcYmax = function () {
    var ymax = 0;
    for (var i = 0; i < this.values.length; i++) {
        var series = this.values[i];
        for (var j = 0; j < series.length; j++) {
            var each = series[j];
            if (each !== null && each > ymax) {
                ymax = each;
            }
        }
    }
    if (ymax < 5) {
        ymax = 5;
    }
    ymax = parseInt((ymax / 5) + 1) * 6;
    if (this.yLimit !== undefined) {
        ymax = Math.min(ymax, this.yLimit)
    }
    return ymax;
};

Chart.prototype.isEmpty = function () {
    if (this.values[0].length == 0) {
        return true;
    }
    for (var i = 0; i < this.values.length; i++) {
        for (var j = 0; j < this.values[i].length; j++) {
            if (this.values[i][j] != null && this.values[i][j] != undefined) {
                return false;
            }
        }
    }
    return true;
}
Chart.prototype.plot = function () {
    if (this.isEmpty()) {
        return this;
    }
    var preYmax = this.ymax;
    var newYmax = this.calcYmax();
    var ymaxChanged = (newYmax != preYmax);
    this.ymax = this.calcYmax();
    if (this.plotObj === undefined) {
        this.plotObj = $.jqplot(this.containerId, this.values, {
            gridPadding: this.gridPadding,
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
                    max: this.values[0].length,
                    pad: 2,
                    numberTicks: this.numXTicks,
                    tickOptions: {
                        show: true,
                        formatter: this.xAxisFormatter
                    }
                },
                yaxis: {
                    labelOptions: {
                        fontFamily: 'Helvetica',
                        fontSize: '10pt'
                    },
                    tickOptions: {
                        formatter: this.yAxisFormatter
                    },
                    max: this.ymax,
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
            },
            legend: this.legend
        }).replot();
    } else {
        var axes = {};
        if (ymaxChanged) {
            axes = {
                xaxis: {
                    min: 1,
                        max: this.values[0].length,
                        pad: 2,
                        numberTicks: 10,
                        tickOptions: {
                        show: true,
                            formatter: this.xAxisFormatter
                    }
                },
                yaxis: {
                    labelOptions: {
                        fontFamily: 'Helvetica',
                            fontSize: '10pt'
                    },
                    tickOptions: {
                        formatter: this.yAxisFormatter
                    },
                    max: this.ymax,
                        min: 0,
                        numberTicks: 7,
                        pad: 0,
                        show: true
                }
            }
        }
        this.plotObj.replot({
            resetAxes: ymaxChanged,
            data: this.values,
            axes: axes
        });
    }
    return this;
}

var formatMemory = function (format, value) {
    value = value || 0;
    if (value < 1024) {
        return value.toFixed(1) + "K ";
    } else if (value < 1048576) { //1024 * 1024
        return (value / 1024).toFixed(1) + "M ";
    } else {
        return (value / 1048576).toFixed(2) + "G ";
    }
};

var formatNetwork = function (format, value) {
    value = value || 0;
    if (value < 1024) {
        return value.toFixed(1) + "B ";
    } else if (value < 1048576) { //1024 * 1024
        return (value / 1024).toFixed(1) + "K ";
    } else {
        return (value / 1048576).toFixed(2) + "M ";
    }
};

var formatPercentage = function (format, value) {
    value = value || 0;
    if (value < 10) {
        return value.toFixed(1) + "% ";
    } else {
        return value.toFixed(0) + "% ";
    }
};

// data is in Byte
var formatMemoryInByte = function (format, value) {
    value = value || 0;
    if (value < 1024) {
        return value.toFixed(1) + "B ";
    } else if (value < 1048576) { //1024 * 1024
        return (value / 1024).toFixed(1) + "K ";
    } else if (value < 1073741824) { //1024 * 1024 * 1024
        return (value / 1048576).toFixed(2) + "M ";
    } else {
        return (value / 1073741824).toFixed(3) + "G ";
    }
};

