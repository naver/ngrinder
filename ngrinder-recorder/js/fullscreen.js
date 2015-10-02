function isFullScreen(cm) {
	return /\bCodeMirror-fullscreen\b/.test(cm.getWrapperElement().className);
}

function winHeight() {
	return window.innerHeight
			|| (document.documentElement || document.body).clientHeight;
}

function setFullScreen(cm, full) {
	var wrap = cm.getWrapperElement(), scroll = cm.getScrollerElement();
	if (full) {
		wrap.className += " CodeMirror-fullscreen";
		scroll.style.height = winHeight() + "px";
		document.documentElement.style.overflow = "hidden";
	} else {
		wrap.className = wrap.className.replace(" CodeMirror-fullscreen", "");
		scroll.style.height = "";
		document.documentElement.style.overflow = "";
	}
	cm.refresh();
}