// Copyright (C) 2009 - 2011 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.engine.process.dcr;

import net.grinder.script.NonInstrumentableTypeException;
import net.grinder.scriptengine.DCRContext;
import net.grinder.scriptengine.Recorder;
import net.grinder.util.weave.Weaver;
import net.grinder.util.weave.Weaver.TargetSource;
import net.grinder.util.weave.WeavingException;
import net.grinder.util.weave.agent.ExposeInstrumentation;
import net.grinder.util.weave.j2se8.ASMTransformerFactory;
import net.grinder.util.weave.j2se8.DCRWeaver;
import org.slf4j.Logger;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Wrap up the DCR context for use by
 * {@link net.grinder.scriptengine.AbstractDCRInstrumenter}
 * implementations.
 *
 * @author Philip Aston
 */
public final class DCRContextImplementation implements DCRContext {

	private static final String[] NON_INSTRUMENTABLE_PACKAGES = {
		"net.grinder.engine.process",
		"extra166y",
		"org.objectweb.asm",
	};

	private static final ClassLoader BOOTSTRAP_CLASSLOADER =
		Object.class.getClassLoader();

	private final Weaver m_weaver;
	private final RecorderRegistry m_recorderRegistry;

	/**
	 * Attempt to create a context.
	 *
	 * @param logger A logger to complain to if problems are found.
	 * @return The context, or {@code null} if one could not be created.
	 */
	public static DCRContextImplementation create(Logger logger) {
		final Instrumentation instrumentation =
			ExposeInstrumentation.getInstrumentation();

		try {
			final Method m =
				Instrumentation.class.getMethod("isRetransformClassesSupported");

			if (!(Boolean)m.invoke(instrumentation)) {
				logger.info(
					"Java VM does not support class retransformation, DCR unavailable");

				return null;
			}
		}
		catch (Exception e1) {
			// Also catches case where instrumentation == null.
			logger.info("Java VM does not support instrumentation, DCR unavailable");

			return null;
		}

		return new DCRContextImplementation(instrumentation,
			RecorderLocator.class,
			RecorderLocator.getRecorderRegistry());
	}

	/**
	 * Constructor.
	 *
	 * <p>Package scope for unit tests.</p>
	 */
	DCRContextImplementation(Instrumentation instrumentation,
							 Class<?> recorderLocatorClass,
							 RecorderRegistry recorderRegistry) {

		final ASMTransformerFactory transformerFactory;

		try {
			transformerFactory = new ASMTransformerFactory(recorderLocatorClass);
		}
		catch (WeavingException e) {
			throw new AssertionError(e);
		}

		m_weaver = new DCRWeaver(transformerFactory, instrumentation);

		m_recorderRegistry = recorderRegistry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void add(Object target,
							  Constructor<?> constructor,
							  Recorder recorder)
		throws NonInstrumentableTypeException {

		checkWrappable(constructor.getDeclaringClass());

		final String location = m_weaver.weave(constructor);

//    System.out.printf("add(%s, %s, %s, %s)%n",
//                      target.hashCode(), location,
//                      target,
//                      constructor);

		m_recorderRegistry.register(target, location, recorder);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void add(Object target,
							  Method method,
							  TargetSource targetSource,
							  Recorder recorder)
		throws NonInstrumentableTypeException {

		checkWrappable(method.getDeclaringClass());

		try {
			final String location = m_weaver.weave(method, targetSource);

			m_recorderRegistry.register(target, location, recorder);
		}
		catch (WeavingException e) {
			throw new NonInstrumentableTypeException("Weaving failed", e);
		}

//    System.out.printf("add(%s, %s, %s, %s)%n",
//                      target.hashCode(), location,
//                      target,
//                      method);


	}

	private void checkWrappable(Class<?> theClass)
		throws NonInstrumentableTypeException {

		final String whyNot = whyCantIInstrument(theClass);

		if (whyNot != null) {
			throw new NonInstrumentableTypeException(
				"Cannot instrument " + theClass + " because " + whyNot);
		}
	}

	private String whyCantIInstrument(Class<?> targetClass) {

		// We disallow instrumentation of these classes to avoid the need for
		// complex protection against recursion in the engine itself.
		// Also, classes from the bootstrap classloader can't statically
		// refer to RecorderLocator.
		if (targetClass.getClassLoader() == BOOTSTRAP_CLASSLOADER) {
			return "it belongs to the bootstrap classloader";
		}

		// Hack to allow the classic hello world examples work.
		// See bug 3411728.
		if ("net.grinder.engine.process.ExternalLogger".equals(
			targetClass.getName())) {
			return null;
		}

		final Package thePackage = targetClass.getPackage();

		if (thePackage != null) {
			final String packageName = thePackage.getName();

			for (String prefix : NON_INSTRUMENTABLE_PACKAGES) {
				if (packageName.startsWith(prefix)) {
					return "it belongs to the " + prefix + " package";
				}
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean isInstrumentable(Class<?> targetClass) {
		return whyCantIInstrument(targetClass) == null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void applyChanges() throws WeavingException {
		m_weaver.applyChanges();
	}
}
