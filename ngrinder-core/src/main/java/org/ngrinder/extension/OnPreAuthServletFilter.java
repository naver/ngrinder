package org.ngrinder.extension;

import javax.servlet.Filter;

import ro.fortsoft.pf4j.ExtensionPoint;

/**
 * Plugin extension point Proxy filter which run combined preauth plugins
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public interface OnPreAuthServletFilter extends ExtensionPoint, Filter {

}
