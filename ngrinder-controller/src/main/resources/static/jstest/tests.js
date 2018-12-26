test("formatTimeForXaxis", function() {
	equal(formatTimeForXaxis(59), "00:59", "Passed!");
	equal(formatTimeForXaxis(249), "04:09", "Passed!");
	equal(formatTimeForXaxis(659), "10:59", "Passed!");
	equal(formatTimeForXaxis(1072), "17:52", "Passed!");
	equal(formatTimeForXaxis(4828), "1:20:28", "Passed!");
});

test("getMaxValue", function() {
	equal(getMaxValue([1,3,7,9]), 9, "Passed!");
	equal(getMaxValue([1,3,11,9]), 11, "Passed!");
	equal(getMaxValue([1,109,7,9]), 109, "Passed!");
	equal(getMaxValue([1,109,109,9]), 109, "Passed!");
	equal(getMaxValue([10,11,100,101]), 101, "Passed!");
	equal(getMaxValue([10,111,100,101]), 111, "Passed!");

	equal(getMaxValue([]), 0, "Passed!");
	
});