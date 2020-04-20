// Copyright (C) 2009 - 2012 Philip Aston
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

import org.python.core.PyClass;
import org.python.core.PyFunction;
import org.python.core.PyInstance;
import org.python.core.PyMethod;
import org.python.core.PyObject;
import org.python.core.PyProxy;


/**
 * DCR instrumenter for Jython 2.1, 2.2.
 *
 * @author Philip Aston
 */
public final class Jython22Instrumenter extends AbstractJythonDCRInstrumenter {

	/**
	 * Constructor.
	 *
	 * @param context The DCR context.
	 */
	public Jython22Instrumenter(DCRContext context) {
		super(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public String getDescription() {
		return "byte code transforming instrumenter for Jython 2.1/2.2";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected void transform(Recorder recorder, PyInstance target)
		throws NonInstrumentableTypeException {

		instrumentPublicMethodsByName(target,
			"invoke",
			recorder,
			true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected void transform(Recorder recorder, PyFunction target)
		throws NonInstrumentableTypeException {

		instrumentPublicMethodsByName(target,
			"__call__",
			recorder,
			false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected void transform(Recorder recorder, PyClass target)
		throws NonInstrumentableTypeException {

		instrumentPublicMethodsByName(target,
			"__call__",
			recorder,
			false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected void transform(Recorder recorder, PyProxy target)
		throws NonInstrumentableTypeException {

		// For some reason, static linking to _getPyInstance fails.
		final PyObject pyInstance;

		try {
			pyInstance =
				(PyObject)
					target.getClass().getMethod("_getPyInstance").invoke(target);
		}
		catch (Exception e) {
			throw new NonInstrumentableTypeException(e.getMessage(), e);
		}

		instrumentPublicMethodsByName(pyInstance,
			"invoke",
			recorder,
			true);
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
				TargetSource.SECOND_PARAMETER,
				recorder,
				false);
		}
	}
}
