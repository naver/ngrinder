import 'jqplot/jquery.jqplot.min.js';
import 'jqplot/jqplot.cursor.js';
import 'jqplot/jqplot.donutRenderer.js';
import 'jqplot/jqplot.highlighter.js';

import '../plugins/jqplot/jqplot.canvasAxisTickRenderer.js';
import '../plugins/jqplot/jqplot.canvasTextRenderer.js';
import '../plugins/jqplot/jqplot.categoryAxisRenderer.js';
import '../plugins/jqplot/jqplot.enhancedLegendRenderer.js';

export default class Chart {
    constructor(containerId, data, interval, opts) {
        this.containerId = containerId;
        this.data = data;
        this.interval = interval;
        this.opts = opts;

        this.opts = opts || {};
        this.containerId = containerId;
        this.values = this.prepare(data);
        this.yAxisFormatter = this.opts.yAxisFormatter || ((format, value) => {
                value = value || 0;
                return value.toFixed(0);
            });
        if (this.yAxisFormatter === formatPercentage) {
            this.yLimit = 100;
        }
        this.interval = interval || 1;
        this.xAxisFormatter = this.opts.xAxisFormatter || ((format, value) => {
                return this.formatTimeForXaxis(parseInt((value - 1) * this.interval));
            });
        this.gridPadding = this.opts.gridPadding || {top: 20, right: 20, bottom: 35, left: 60};
        this.numXTicks = this.opts.numXTicks || 10;
        if (this.opts.labels !== undefined) {
            //noinspection JSUnresolvedVariable
            this.legend = {
                renderer: $.jqplot.EnhancedLegendRenderer,
                show: true,
                placement: "insideGrid",
                labels: opts.labels,
                location: opts.legend_location || "ne",
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

    prepare(data) {
        for (let i = 0; i < data.length; i++) {
            if (!(data[i] instanceof Array)) {
                data[i] = eval(data[i]);
            }
        }

        if (data[0] === undefined) {
            return [];
        }
        return data;
    }

    formatTimeForXaxis(timeInSecond) {
        if (timeInSecond === null || timeInSecond < 0) {
            timeInSecond = 0;
        }
        let hour = parseInt((timeInSecond % (60 * 60 * 24)) / 3600);
        let min = parseInt((timeInSecond % 3600) / 60);
        let sec = parseInt(timeInSecond % 60);
        if (sec < 10) {
            sec = "0" + sec;
        }
        let display = min + ":" + sec;
        if (min < 10) {
            display = '0' + display;
        }
        if (hour > 0) {
            display = hour + ":" + display;
        }
        return display;
    }

    calcYmax() {
        let ymax = 0;
        for (let i = 0; i < this.values.length; i++) {
            let series = this.values[i];
            for (let j = 0; j < series.length; j++) {
                let each = series[j];
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
    }

    isEmpty() {
        if (this.values.length === 0 || this.values[0].length === 0) {
            return true;
        }
        for (let i = 0; i < this.values.length; i++) {
            for (let j = 0; j < this.values[i].length; j++) {
                if (this.values[i][j] !== null && this.values[i][j] !== undefined) {
                    return false;
                }
            }
        }
        return true;
    }

    plot() {
        if (this.isEmpty()) {
            return this;
        }
        const preYmax = this.ymax;
        const newYmax = this.calcYmax();
        const ymaxChanged = (newYmax !== preYmax);
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
            let axes = {};
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

    static createChartExportButton(btnLabel, title) {
        if (!$.jqplot.use_excanvas) {
            if (btnLabel === undefined) {
                btnLabel = "View Plot Image";
            }
            if (title === undefined) {
                title = "Image";
            }

            $('div.jqplot-target').each(function () {
                let outerDiv = $(document.createElement('div'));
                let header = $(document.createElement('div'));
                let div = $(document.createElement('div'));

                outerDiv.append(header);
                outerDiv.append(div);

                outerDiv.addClass('jqplot-image-container');
                header.addClass('jqplot-image-container-header');
                div.addClass('jqplot-image-container-content');

                header.html(title);

                let close = $(document.createElement('a'));
                close.addClass('jqplot-image-container-close');
                close.html('&times;');
                close.attr('href', '#');
                close.click(function () {
                    $(this).parents('div.jqplot-image-container').hide(500);
                });
                header.append(close);

                $(this).after(outerDiv);
                outerDiv.hide();

                outerDiv = null;
                header = null;
                div = null;
                close = null;
                if ($(`#${$(this).attr("id")}_img_btn`)[0] === undefined) {
                    let btn = $(`<a class='pointer-cursor' title='${btnLabel}'><i class='icon-download'  style='cursor:pointer;margin-top:-20px;margin-left:680px'></i></a>`);
                    btn.attr("id", $(this).attr("id") + "_img_btn");
                    btn.bind('click', {chart: $(this)}, function (evt) {
                        const imgelem = evt.data.chart.jqplotToImageElem();
                        let div = $(this).nextAll('div.jqplot-image-container').first();
                        if ($(div).is(":visible")) {
                            div.hide(500);
                        } else {
                            div.children('div.jqplot-image-container-content').empty();
                            div.children('div.jqplot-image-container-content').append(imgelem);
                            div.show(500);
                        }

                        div = null;
                    });
                    $(this).before(btn);
                    btn = null;
                }
            })
        }
    }

    static getFormatMemory(format, value) {
        return formatMemory(format, value);
    }

    static getFormatNetwork(format, value) {
        return formatNetwork(format, value);
    }

    static getFormatPercentage(format, value) {
        return formatPercentage(format, value);
    }

    static getFormatMemoryInByte(format, value) {
        return formatMemoryInByte(format, value);
    }
}

const formatMemory = (format, value) => {
    value = value || 0;
    if (value < 1024) {
        return value.toFixed(1) + "K ";
    } else if (value < 1048576) { //1024 * 1024
        return (value / 1024).toFixed(1) + "M ";
    } else {
        return (value / 1048576).toFixed(2) + "G ";
    }
};

const formatNetwork = (format, value) => {
    value = value || 0;
    if (value < 1024) {
        return value.toFixed(1) + "B ";
    } else if (value < 1048576) { //1024 * 1024
        return (value / 1024).toFixed(1) + "K ";
    } else {
        return (value / 1048576).toFixed(2) + "M ";
    }
};

const formatPercentage = (format, value) => {
    value = value || 0;
    if (value < 10) {
        return value.toFixed(1) + "% ";
    } else {
        return value.toFixed(0) + "% ";
    }
};

// data is in Byte
const formatMemoryInByte = (format, value) => {
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

