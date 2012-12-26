function getProcessCount(total) {
	if (total < 2) {
		return 1;
	}
	if (total > 80) {
	    return parseInt(total / 30);
	}
	return 2;
}

function getThreadCount(total) {
	if (total < 2) {
		return 1;
	}
        if (total > 80) {
	    return parseInt(total / (parseInt(total / 30)));
        }
	return parseInt(total / 2 + 0.5);
}
