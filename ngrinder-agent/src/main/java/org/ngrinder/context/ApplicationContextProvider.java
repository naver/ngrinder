package org.ngrinder.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * Used to get applicationContext injected from spring context, and set it into
 * a static object. Then I can get and use it in anywhere in application.
 * 
 * @author Mavlarn
 */
@Service
public class ApplicationContextProvider implements ApplicationContextAware {

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		AppContext.setCtx(applicationContext);
	}

}
