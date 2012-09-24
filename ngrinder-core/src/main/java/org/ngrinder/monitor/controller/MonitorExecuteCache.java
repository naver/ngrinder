///*
// * Copyright (C) 2012 - 2012 NHN Corporation
// * All rights reserved.
// *
// * This file is part of The nGrinder software distribution. Refer to
// * the file LICENSE which is part of The nGrinder distribution for
// * licensing details. The nGrinder distribution is available on the
// * Internet at http://nhnopensource.org/ngrinder
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// * OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//package org.ngrinder.monitor.controller;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//import org.ngrinder.monitor.MonitorConstants;
//
///**
// * 
// * This class is used to cache the MonitorExecuteManager object, to avoid adding same monitoring for same key..
// *
// * @author Mavlarn
// * @since 3.0
// */
//public final class MonitorExecuteCache {
//	private Map<String, MonitorExecuteManager> cache;
//
//	private static MonitorExecuteCache instance = new MonitorExecuteCache(
//			MonitorConstants.DEFAULT_CONTROLLER_CACHE_SIZE);
//
//	private MonitorExecuteCache(final int capacity) {
//		cache = new ConcurrentHashMap<String, MonitorExecuteManager>(capacity);
//	}
//
//	public static MonitorExecuteCache getInstance() {
//		return instance;
//	}
//
//	public boolean containKey(final String key) {
//		return cache.containsKey(key);
//	}
//
//	public boolean setCache(final String key, final MonitorExecuteManager manager) {
//		if (containKey(key)) {
//			return false;
//		} else {
//			cache.put(key, manager);
//			return true;
//		}
//	}
//
//	public int size() {
//		return cache.size();
//	}
//
//	public MonitorExecuteManager getCache(final String key) {
//		return cache.get(key);
//	}
//
//	public MonitorExecuteManager remove(final String key) {
//		return cache.remove(key);
//	}
//}
