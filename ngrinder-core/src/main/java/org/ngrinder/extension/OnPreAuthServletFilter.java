package org.ngrinder.extension;

import org.pf4j.ExtensionPoint;

import javax.servlet.Filter;

/**
 * Plugin extension point Proxy filter which run combined preauth plugins
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public interface OnPreAuthServletFilter extends ExtensionPoint, Filter {

}
