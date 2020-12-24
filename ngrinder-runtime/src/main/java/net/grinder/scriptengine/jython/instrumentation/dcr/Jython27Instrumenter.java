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

package net.grinder.scriptengine.jython.instrumentation.dcr;

import net.grinder.script.NonInstrumentableTypeException;
import net.grinder.scriptengine.DCRContext;
import net.grinder.scriptengine.Recorder;
import net.grinder.util.weave.Weaver.TargetSource;
import net.grinder.util.weave.WeavingException;
import org.python.core.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * DCR instrumenter for Jython 2.7 (modified for nGrinder)
 *
 * @author Philip Aston
 */
public final class Jython27Instrumenter extends AbstractJythonDCRInstrumenter {

	private final Transformer<PyInstance> m_pyInstanceTransformer;
	private final Transformer<PyFunction> m_pyFunctionTransformer;
	private final Transformer<PyProxy> m_pyProxyTransformer;
	private final Transformer<PyClass> m_pyClassTransformer;

	/**
	 * Constructor.
	 *
	 * @param context The DCR context.
	 * @throws WeavingException If it looks like Jython 2.7 isn't available.
	 */
	public Jython27Instrumenter(final DCRContext context)
		throws WeavingException  {

		super(context);

		try {
			final List<Method> methodsForPyFunction = new ArrayList<Method>();

			for (Method method : PyFunction.class.getDeclaredMethods()) {
				// Roughly identify the fundamental __call__ methods, i.e. those
				// that call the actual func_code.
				if (("__call__".equals(method.getName()) ||
					// Add function__call__ for refactoring in Jython 2.5.2.
					"function___call__".equals(method.getName())) &&
					method.getParameterTypes().length >= 1 &&
					method.getParameterTypes()[0] == ThreadState.class) {
					methodsForPyFunction.add(method);
				}
			}

			assertAtLeastOneMethod(methodsForPyFunction);

			m_pyFunctionTransformer = new Transformer<PyFunction>() {
				public void transform(Recorder recorder, PyFunction target)
					throws NonInstrumentableTypeException {

					for (Method method : methodsForPyFunction) {
						context.add(target,
							method,
							TargetSource.FIRST_PARAMETER,
							recorder);
					}
				}
			};

			final List<Method> methodsForPyInstance = new ArrayList<Method>();

			for (Method method : PyFunction.class.getDeclaredMethods()) {
				// Here we're finding the fundamental __call__ methods that also
				// take an instance argument.
				if ("__call__".equals(method.getName()) &&
					method.getParameterTypes().length >= 2 &&
					method.getParameterTypes()[0] == ThreadState.class &&
					method.getParameterTypes()[1] == PyObject.class) {
					methodsForPyInstance.add(method);
				}
			}

			assertAtLeastOneMethod(methodsForPyInstance);

			m_pyInstanceTransformer = new Transformer<PyInstance>() {
				public void transform(Recorder recorder, PyInstance target)
					throws NonInstrumentableTypeException {

					for (Method method : methodsForPyInstance) {
						context.add(target,
							method,
							TargetSource.THIRD_PARAMETER,
							recorder);
					}
				}
			};

			final List<Method> methodsForPyMethod = new ArrayList<Method>();

			for (Method method : PyMethod.class.getDeclaredMethods()) {
				// Roughly identify the fundamental __call__ methods, i.e. those
				// that call the actual func_code.
				if (("__call__".equals(method.getName()) ||
					// Add instancemethod___call__ for refactoring in Jython 2.5.2.
					"instancemethod___call__".equals(method.getName())) &&
					method.getParameterTypes().length >= 1 &&
					method.getParameterTypes()[0] == ThreadState.class) {
					methodsForPyMethod.add(method);
				}
			}

			assertAtLeastOneMethod(methodsForPyMethod);

			final Method pyReflectedFunctionCall =
				PyReflectedFunction.class.getDeclaredMethod("__call__",
					PyObject.class,
					PyObject[].class,
					String[].class);

			// PyProxy is used for Jython objects that extend a Java class.
			// We can't just use the Java wrapping, since then we'd miss the
			// Jython methods.

			// Need to look up this method dynamically, the return type differs
			// between 2.2 and 2.5.
			final Method pyProxyPyInstanceMethod =
				PyProxy.class.getDeclaredMethod("_getPyInstance");

			m_pyProxyTransformer = new Transformer<PyProxy>() {
				public void transform(Recorder recorder, PyProxy target)
					throws NonInstrumentableTypeException {
					final PyObject pyInstance;

					try {
						pyInstance = (PyObject) pyProxyPyInstanceMethod.invoke(target);
					}
					catch (Exception e) {
						throw new NonInstrumentableTypeException(
							"Could not call _getPyInstance", e);
					}

					for (Method method : methodsForPyInstance) {
						context.add(pyInstance,
							method,
							TargetSource.THIRD_PARAMETER,
							recorder);
					}

					context.add(pyInstance,
						pyReflectedFunctionCall,
						TargetSource.SECOND_PARAMETER,
						recorder);
				}
			};

			final Method pyClassCall =
				PyClass.class.getDeclaredMethod("__call__",
					PyObject[].class,
					String[].class);

			m_pyClassTransformer = new Transformer<PyClass>() {
				public void transform(Recorder recorder, PyClass target)
					throws NonInstrumentableTypeException {
					context.add(target,
						pyClassCall,
						TargetSource.FIRST_PARAMETER,
						recorder);
				}
			};
		}
		catch (NoSuchMethodException e) {
			throw new WeavingException("Jython 2.7 not found", e);
		}
	}

	private static void assertAtLeastOneMethod(List<Method> methods)
		throws WeavingException {
		if (methods.size() == 0) {
			throw new WeavingException("Jython 2.7 not found");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public String getDescription() {
		return "byte code transforming instrumenter for Jython 2.7";
	}

	private interface Transformer<T> {
		void transform(Recorder recorder, T target)
			throws NonInstrumentableTypeException;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected void transform(Recorder recorder, PyInstance target)
		throws NonInstrumentableTypeException {
		m_pyInstanceTransformer.transform(recorder, target);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected void transform(Recorder recorder, PyFunction target)
		throws NonInstrumentableTypeException {
		m_pyFunctionTransformer.transform(recorder, target);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected void transform(Recorder recorder, PyClass target)
		throws NonInstrumentableTypeException {
		m_pyClassTransformer.transform(recorder, target);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected void transform(Recorder recorder, PyProxy target)
		throws NonInstrumentableTypeException {
		m_pyProxyTransformer.transform(recorder, target);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected void transform(Recorder recorder, PyMethod target)
		throws NonInstrumentableTypeException {

		// PyMethod is a wrapper around a callable. Sometimes Jython bypasses
		// the PyMethod (e.g. dispatch of self.foo() calls). Sometimes there
		// are multiple PyMethods that refer to the same callable.

		// In the common case, the callable is a PyFunction wrapping some PyCode.
		// Experimentation shows that there'll be  a single PyFunction. However,
		// there's nothing that forces this to be true - some code path might
		// create a different PyFunction referring to the same code. Also, we must
		// cope with other types of callable. I guess I could identify
		// PyFunction's and dispatch on their im_code should this become an issue.

		if (target.__self__ == null) {
			// Unbound method.
			instrumentPublicMethodsByName(target.__func__,
				"__call__",
				recorder,
				false);
		}
		else {
			instrumentPublicMethodsByName(target.__func__.getClass(),
				target.__self__,
				"__call__",
				TargetSource.THIRD_PARAMETER,
				recorder,
				false);
		}
	}
}
