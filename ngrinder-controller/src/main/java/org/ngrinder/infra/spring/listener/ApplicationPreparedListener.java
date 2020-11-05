package org.ngrinder.infra.spring.listener;

import com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.util.Iterator;

/**
 * @since 3.5.0
 * */
public class ApplicationPreparedListener implements ApplicationListener<ApplicationPreparedEvent> {

	@SuppressWarnings("NullableProblems")
	@Override
	public void onApplicationEvent(ApplicationPreparedEvent event) {
		removeCacheProviderExceptCaffeineCacheProvider();
	}

	/**
	 * remove all cache provider except caffeine cache provider for using JCacheRegionFactory in hibernate second level cache.
	 */
	private static void removeCacheProviderExceptCaffeineCacheProvider() {
		Iterator<CachingProvider> iterator = Caching.getCachingProviders().iterator();
		while (iterator.hasNext()) {
			CachingProvider cachingProvider = iterator.next();
			if (!(cachingProvider instanceof CaffeineCachingProvider)) {
				iterator.remove();
			}
		}
	}
}
