# _*_ coding: utf8 _*_
# WebPlatformTeam
# kwangsub.kim@nhn.com

from net.grinder.script import Test
from net.grinder.script.Grinder import grinder
from ngrinder import HelloNGrinder
from net.grinder.plugin.http import HTTPRequest 


test1 = Test(1, "Request resource")
request1 = test1.wrap(HTTPRequest())

test2 = Test(100, "Hello")
logger = grinder.logger

class TestRunner:
    def __init__(self):
        print "Initialize..." 
        grinder.sleep(100)

    def __call__(self):
       result = request1.GET("http://finance.naver.com")
       grinder.sleep(100)
       hi =  HelloNGrinder.hi("Get naver finance home page")
       print hi
            

        