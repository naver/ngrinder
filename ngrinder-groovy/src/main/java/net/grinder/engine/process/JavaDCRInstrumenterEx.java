/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package net.grinder.engine.process;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.grinder.script.NonInstrumentableTypeException;
import net.grinder.script.Test.InstrumentationFilter;
import net.grinder.scriptengine.AbstractDCRInstrumenter;
import net.grinder.scriptengine.DCRContext;
import net.grinder.scriptengine.Recorder;
import net.grinder.util.weave.Weaver.TargetSource;

/**
 * DCR instrumenter for Java.
 * 
 * This is modified from JavaDCRInstrumenter due to it's package
 * protected visibility.
 * 
 * @author Philip Aston
 * @author JunHo Yoon (modified by)
 */
public class JavaDCRInstrumenterEx extends AbstractDCRInstrumenter {

	/**
	 * Constructor.
	 * 
	 * @param context	The DCR context.
	 */
	public JavaDCRInstrumenterEx(DCRContext context) {
		super(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return "byte code transforming instrumenter for Java";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean instrument(Object target, Recorder recorder,
			InstrumentationFilter filter) throws NonInstrumentableTypeException {

		if (target instanceof Class<?>) {
			instrumentClass((Class<?>) target, recorder, filter);
		} else if (target != null) {
			instrumentInstance(target, recorder, filter);
		}

		return true;
	}

	private void instrumentClass(Class<?> targetClass, Recorder recorder,
			InstrumentationFilter filter) throws NonInstrumentableTypeException {

		if (targetClass.isArray()) {
			throw new NonInstrumentableTypeException("Can't instrument arrays");
		}

		for (Constructor<?> constructor : targetClass.getDeclaredConstructors()) {
			getContext().add(targetClass, constructor, recorder);
		}

		// Instrument the static methods declared by the target class. Ignore
		// any parent class.
		for (Method method : targetClass.getDeclaredMethods()) {
			if (Modifier.isStatic(method.getModifiers())
					&& filter.matches(method)) {
				getContext().add(targetClass, method, TargetSource.CLASS,
						recorder);
			}
		}
	}

	private void instrumentInstance(Object target, Recorder recorder,
			InstrumentationFilter filter) throws NonInstrumentableTypeException {

		Class<?> c = target.getClass();

		if (c.isArray()) {
			throw new NonInstrumentableTypeException("Can't instrument arrays");
		}

		do {
			for (Method method : c.getDeclaredMethods()) {
				if (!Modifier.isStatic(method.getModifiers())
						&& filter.matches(method)) {
					getContext().add(target, method,
							TargetSource.FIRST_PARAMETER, recorder);
				}
			}

			c = c.getSuperclass();
		} while (getContext().isInstrumentable(c));
	}
}
