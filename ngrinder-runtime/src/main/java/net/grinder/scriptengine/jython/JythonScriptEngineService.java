package net.grinder.scriptengine.jython;

import net.grinder.common.GrinderProperties;
import net.grinder.engine.common.EngineException;
import net.grinder.engine.common.ScriptLocation;
import net.grinder.scriptengine.DCRContext;
import net.grinder.scriptengine.Instrumenter;
import net.grinder.scriptengine.ScriptEngineService;
import net.grinder.scriptengine.jython.instrumentation.dcr.Jython22Instrumenter;
import net.grinder.scriptengine.jython.instrumentation.dcr.Jython25Instrumenter;
import net.grinder.util.FileExtensionMatcher;
import net.grinder.util.weave.WeavingException;

import java.util.ArrayList;
import java.util.List;

/**
 * Jython {@link net.grinder.scriptengine.ScriptEngineService} implementation.
 *
 * @author Philip Aston
 * Modified for nGrinder.
 */
public final class JythonScriptEngineService implements ScriptEngineService {

	private final FileExtensionMatcher m_pyFileMatcher = new FileExtensionMatcher(".py");

	private final DCRContext m_dcrContext;

	/**
	 * Constructor.
	 *
	 * @param properties     Properties.
	 * @param dcrContext     DCR context.
	 * @param scriptLocation Script location.
	 */
	public JythonScriptEngineService(GrinderProperties properties,
									 DCRContext dcrContext,
									 ScriptLocation scriptLocation) {
		m_dcrContext = dcrContext;
	}

	/**
	 * Constructor used when DCR is unavailable.
	 */
	public JythonScriptEngineService() {
		m_dcrContext = null;
	}

	public void noOp() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Instrumenter> createInstrumenters() throws EngineException {

		final List<Instrumenter> instrumenters = new ArrayList<Instrumenter>();

		try {
			if (m_dcrContext != null) {
				try {
					instrumenters.add(new Jython25Instrumenter(m_dcrContext));
				}
				catch (WeavingException e) {
					// Jython 2.5 not available, try Jython 2.1/2.2.
					instrumenters.add(new Jython22Instrumenter(m_dcrContext));
				}
			}
		} catch (NoClassDefFoundError e) {
			noOp();
		}

		return instrumenters;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ScriptEngine createScriptEngine(ScriptLocation script) throws EngineException {

		if (m_pyFileMatcher.accept(script.getFile())) {
			return new JythonScriptEngine(script);
		}

		return null;
	}
}
