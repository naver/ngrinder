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
		grinder.sleep(5000)
		pass

	def test(self):
		grinder.logger.info("test")

	def __call__(self):
		self.test()
		grinder.sleep(100)
		grinder.statistics.forLastTest.success = 1

test1.record(TestRunner.test)