# _*_ coding: utf8 _*_
from HTTPClient import NVPair, Cookie, CookieModule, CookiePolicyHandler
from net.grinder.plugin.http import HTTPPluginControl, HTTPRequest
from net.grinder.script import Test
from net.grinder.script.Grinder import grinder
from java.util import Date
# Set up a cookie handler to log all cookies that are sent and received. 
class MyCookiePolicyHandler(CookiePolicyHandler): 
    def acceptCookie(self, cookie, request, response): 
        return 1

    def sendCookie(self, cookie, request): 
        return 1

CookieModule.setCookiePolicyHandler(MyCookiePolicyHandler()) 

test1 = Test(1, "checkout home")
request1 = test1.wrap(HTTPRequest(url="http://my.site.com"))
class TestRunner:
    def __init__(self):
        # Login URL
        request = HTTPRequest(url="https://login.site.com")
        ##### reset to the all cookies #####
        threadContext = HTTPPluginControl.getThreadHTTPClientContext() 
        self.cookies = CookieModule.listAllCookies(threadContext) 
        for c in self.cookies: CookieModule.removeCookie(c, threadContext)

        # do login
        request.POST("/login/do", ( NVPair("id", "my_id"),NVPair("pw", "my_passwd")));
        ##### save to the login info in cookies #####
        self.cookies = CookieModule.listAllCookies(threadContext)

    def __call__(self):
        grinder.statistics.delayReports = 1
        ##### Set to the cookies for login #####
        threadContext = HTTPPluginControl.getThreadHTTPClientContext()
        for c in self.cookies: CookieModule.addCookie(c,threadContext)

        ##### Request with login #####
        result = request1.GET("/mypage")

        if result.text.count("only my content data") < 0:
            grinder.statistics.forLastTest.success = 0
        else :
            grinder.statistics.forLastTest.success = 1