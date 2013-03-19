function generateImg(btnLabel, warningMsg) {
    if (!$.jqplot.use_excanvas) {
    	if (btnLabel == undefined) {
    		btnLabel = "View Plot Image";
    	}
    	if (warningMsg == undefined) {
    		warningMsg = "Right cick to save thie image as...";
    	}
    	
        $('div.jqplot-target').each(function(){
            var outerDiv = $(document.createElement('div'));
            var header = $(document.createElement('div'));
            var div = $(document.createElement('div'));

            outerDiv.append(header);
            outerDiv.append(div);

            outerDiv.addClass('jqplot-image-container');
            header.addClass('jqplot-image-container-header');
            div.addClass('jqplot-image-container-content');

            header.html(warningMsg);

            var close = $(document.createElement('a'));
            close.addClass('jqplot-image-container-close');
            close.html('&times;');
            close.attr('href', '#');
            close.click(function() {
                $(this).parents('div.jqplot-image-container').hide(500);
            });
            header.append(close);

            $(this).after(outerDiv);
            outerDiv.hide();

            outerDiv = header = div = close = null;
            if ($("#" + $(this).attr("id") + "ImgBtn")[0] == undefined) {
                var btn = $("<i class='icon-download' title='" + btnLabel + "' style='cursor:pointer;margin-top:-20px;margin-left:680px'></i></div>");
                btn.attr("id", $(this).attr("id") + "ImgBtn"); 
                btn.bind('click', {chart: $(this)}, function(evt) {   
                    var imgelem = evt.data.chart.jqplotToImageElem(); 
                    var div = $(this).nextAll('div.jqplot-image-container').first();
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
        });
    }
}

function cleanImgElem() {
	$("button.jqplot-image-button").each(function() {
		$(this).remove();
	});
	
	$("div.jqplot-image-container").each(function() {
		$(this).remove();
	});
}