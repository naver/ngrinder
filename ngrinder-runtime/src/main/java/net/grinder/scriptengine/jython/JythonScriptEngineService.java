package net.grinder.scriptengine.jython;

import java.util.ArrayList;
import java.util.List;

import net.grinder.common.GrinderProperties;
import net.grinder.engine.common.EngineException;
import net.grinder.engine.common.ScriptLocation;
import net.grinder.scriptengine.DCRContext;
import net.grinder.scriptengine.Instrumenter;
import net.grinder.scriptengine.ScriptEngineService;
import net.grinder.scriptengine.jython.instrumentation.dcr.Jython22Instrumenter;
import net.grinder.scriptengine.jython.instrumentation.dcr.Jython25Instrumenter;
import net.grinder.scriptengine.jython.instrumentation.traditional.TraditionalJythonInstrumenter;
import net.grinder.util.FileExtensionMatcher;
import net.grinder.util.weave.WeavingException;

/**
 * Jython {@link net.grinder.scriptengine.ScriptEngineService} implementation.
 *
 * @author Philip Aston
 * @author JunHo Yoon (modified by)
 */
@SuppressWarnings("UnusedDeclaration")
public final class JythonScriptEngineService implements ScriptEngineService {

	private final FileExtensionMatcher m_pyFileMatcher = new FileExtensionMatcher(".py");

	private final boolean m_forceDCRInstrumentation;
	private final DCRContext m_dcrContext;

	/**
	 * Constructor.
	 *
	 * @param properties     Properties.
	 * @param dcrContext     DCR context.
	 * @param scriptLocation Script location.
	 */
	public JythonScriptEngineService(GrinderProperties properties, DCRContext dcrContext, ScriptLocation scriptLocation) {

		// This property name is poor, since it really means "If DCR
		// instrumentation is available, avoid the traditional Jython
		// instrumenter". I'm not renaming it, since I expect it only to last
		// a few releases, until DCR becomes the default.
		m_forceDCRInstrumentation = properties.getBoolean("grinder.dcrinstrumentation", false) ||
				// Hack: force DCR instrumentation for non-Jython scripts.
				!m_pyFileMatcher.accept(scriptLocation.getFile());

		m_dcrContext = dcrContext;
	}

	/**
	 * Constructor used when DCR is unavailable.
	 */
	public JythonScriptEngineService() {
		m_dcrContext = null;
		m_forceDCRInstrumentation = false;
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
			if (!m_forceDCRInstrumentation) {
				try {
					instrumenters.add(new TraditionalJythonInstrumenter());
				} catch (EngineException e) {
					noOp();
				} catch (VerifyError e) {
					noOp();
				}
			}

			if (m_dcrContext != null) {
				if (instrumenters.size() == 0) {
					try {
						instrumenters.add(new Jython25Instrumenter(m_dcrContext));
					} catch (WeavingException e) {
						// Jython 2.5 not available, try Jython 2.1/2.2.
						instrumenters.add(new Jython22Instrumenter(m_dcrContext));
					}
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
