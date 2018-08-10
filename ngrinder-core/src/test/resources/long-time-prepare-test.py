from net.grinder.script.Grinder import grinder
from net.grinder.script import Test
from net.grinder.plugin.http import HTTPRequest
from net.grinder.plugin.http import HTTPPluginControl

control = HTTPPluginControl.getConnectionDefaults()
test1 = Test(1, "test")
request1 = HTTPRequest()

class TestRunner:
	def __init__(self):
		grinder.statistics.delayReports=True
		grinder.logger.info("prepare")
`		grinder.sleep(10)
		pass

	def test(self):
		print "make the test inactive for a 10 sec"
		grinder.sleep(10000)

	def __call__(self):
		print "called"
		self.test()
		grinder.statistics.forLastTest.success = 1

test1.record(TestRunner.test)
