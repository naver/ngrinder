/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.agent.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.FactoryConfiguration;
import net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory;
import net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory;

import org.apache.commons.io.IOUtils;
import org.hibernate.annotations.OnDelete;
import org.ngrinder.infra.annotation.TestOnlyComponent;
import org.ngrinder.infra.config.DynamicCacheConfig;
import org.ngrinder.infra.logger.CoreLogger;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.core.io.ClassPathResource;

/**
  * Mock DynamicCacheConfig which use cluster mode.
 */
@TestOnlyComponent
public class MockDynamicCacheConfig extends DynamicCacheConfig {
	
	@SuppressWarnings("rawtypes")
	@Override
	public EhCacheCacheManager dynamicCacheManager() {
		EhCacheCacheManager cacheManager = new EhCacheCacheManager();
		Configuration cacheManagerConfig;
		InputStream inputStream = null;
		try {
		
			CoreLogger.LOGGER.info("In cluster mode.");
			inputStream = new ClassPathResource("ehcache-dist.xml").getInputStream();
			cacheManagerConfig = ConfigurationFactory.parseConfiguration(inputStream);

			FactoryConfiguration peerProviderConfig = new FactoryConfiguration();
			peerProviderConfig.setClass(RMICacheManagerPeerProviderFactory.class.getName());
			List<String> replicatedCacheNames = getReplicatedCacheNames(cacheManagerConfig);
			String properties = createCacheProperties(replicatedCacheNames);
			peerProviderConfig.setProperties(properties);
			cacheManagerConfig.addCacheManagerPeerProviderFactory(peerProviderConfig);

			FactoryConfiguration peerListenerConfig = new FactoryConfiguration();
			peerListenerConfig.setClass(RMICacheManagerPeerListenerFactory.class.getName());
			String peerListenerProperties = createPearListenerProperties(replicatedCacheNames);
			peerListenerConfig.setProperties(peerListenerProperties);
			cacheManagerConfig.addCacheManagerPeerListenerFactory(peerListenerConfig);
			CoreLogger.LOGGER.info("clusterURLs:{}", peerListenerProperties);
	
			cacheManagerConfig.setName("TestCluster");
			CacheManager mgr = CacheManager.create(cacheManagerConfig);
			cacheManager.setCacheManager(mgr);
		} catch (IOException e) {
			CoreLogger.LOGGER.error("Error while setting up cache", e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		return cacheManager;
	}

}
