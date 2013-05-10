function getProcessCount(total) {
	if (total < 2) {
		return 1;
	}
	
	var processCount = 2;

	if (total > 80) {
		processCount = parseInt(total / 40) + 1;
	}
	
	if (processCount > 10) {
		processCount = 10;
	}
	return processCount;
}

function getThreadCount(total) {
	var processCount = getProcessCount(total);
	return parseInt(total / processCount);
}
