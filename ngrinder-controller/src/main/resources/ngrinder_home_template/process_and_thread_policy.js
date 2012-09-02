
function getProcessCount(total) {
	if (total < 2) {
		return 1;
	}
	
	return 2;
}

function getThreadCount(total) {
	if (total < 2) {
		return 1;
	}
	return parseInt(total / 2 + 0.5);
}
