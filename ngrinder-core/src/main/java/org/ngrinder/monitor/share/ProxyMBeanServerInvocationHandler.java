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
package org.ngrinder.monitor.share;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;

public class ProxyMBeanServerInvocationHandler implements InvocationHandler {

	private final MBeanServerConnection conn;
	private Map<ObjectName, NameValueMap> cachedValues = newMap();
	private Map<ObjectName, Set<String>> cachedNames = newMap();

	@SuppressWarnings("serial")
	private static final class NameValueMap extends HashMap<String, Object> {
	}

	public ProxyMBeanServerInvocationHandler(MBeanServerConnection conn) {
		this.conn = conn;
	}

	private synchronized void flush() {
		cachedValues = newMap();
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		final String methodName = method.getName();

		if ("getAttribute".equals(methodName)) {
			return getAttribute((ObjectName) args[0], (String) args[1]);
		} else if ("getAttributes".equals(methodName)) {
			return getAttributes((ObjectName) args[0], (String[]) args[1]);
		} else if ("flush".equals(methodName)) {
			flush();
			return null;
		} else {
			try {
				return method.invoke(conn, args);
			} catch (InvocationTargetException e) {
				throw e.getCause();
			}
		}
	}

	private Object getAttribute(ObjectName objName, String attrName) throws MBeanException, InstanceNotFoundException,
			AttributeNotFoundException, ReflectionException, IOException {
		final NameValueMap values = getCachedAttributes(objName, Collections.singleton(attrName));
		Object value = values.get(attrName);

		if (value != null || values.containsKey(attrName)) {
			return value;
		}

		return conn.getAttribute(objName, attrName);
	}

	private AttributeList getAttributes(ObjectName objName, String[] attrNames) throws InstanceNotFoundException,
			ReflectionException, IOException {
		final NameValueMap values = getCachedAttributes(objName, new TreeSet<String>(Arrays.asList(attrNames)));
		final AttributeList list = new AttributeList();

		for (String attrName : attrNames) {
			final Object value = values.get(attrName);
			if (value != null || values.containsKey(attrName)) {
				list.add(new Attribute(attrName, value));
			}
		}

		return list;
	}

	private synchronized NameValueMap getCachedAttributes(ObjectName objName, Set<String> attrNames)
			throws InstanceNotFoundException, ReflectionException, IOException {
		NameValueMap values = cachedValues.get(objName);
		if (values != null && values.keySet().containsAll(attrNames)) {
			return values;
		}

		Set<String> tempSet = new TreeSet<String>(attrNames);
		Set<String> oldNames = cachedNames.get(objName);
		if (oldNames != null) {
			tempSet.addAll(oldNames);
		}

		values = new NameValueMap();
		final AttributeList attrs = conn.getAttributes(objName, tempSet.toArray(new String[tempSet.size()]));

		for (Attribute attr : attrs.asList()) {
			values.put(attr.getName(), attr.getValue());
		}

		cachedValues.put(objName, values);
		cachedNames.put(objName, tempSet);

		return values;
	}

	private static <K, V> Map<K, V> newMap() {
		return new HashMap<K, V>();
	}
}