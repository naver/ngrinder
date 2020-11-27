package org.ngrinder.infra.spring.listener;

import com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ApplicationListener;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.util.Iterator;

import static org.ngrinder.starter.InstallationChecker.checkAll;

/**
 * @since 3.5.0
 * */
public class ApplicationPreparedListener implements ApplicationListener<ApplicationContextInitializedEvent> {

	@SuppressWarnings("NullableProblems")
	@Override
	public void onApplicationEvent(ApplicationContextInitializedEvent event) {
		removeCacheProviderExceptCaffeineCacheProvider();
		checkAll();
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
