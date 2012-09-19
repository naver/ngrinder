test("formatTimeForXaxis", function() {
	equal(formatTimeForXaxis(59), "00:59", "Passed!");
	equal(formatTimeForXaxis(249), "04:09", "Passed!");
	equal(formatTimeForXaxis(659), "10:59", "Passed!");
	equal(formatTimeForXaxis(1072), "17:52", "Passed!");
	equal(formatTimeForXaxis(4828), "1:20:28", "Passed!");
});