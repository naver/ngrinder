package org.ngrinder.script.handler;

import org.python.core.CompileMode;
import org.python.core.CompilerFlags;
import org.python.core.PySyntaxError;
import org.python.core.PyTuple;
import org.springframework.stereotype.Component;

@Component
public class JythonScriptHandler extends ScriptHandler {

	public JythonScriptHandler() {
		super("py", "Jython", "python");
	}

	@Override
	protected Integer order() {
		return 100;
	}

	@Override
	public String checkSyntaxErrors(String script) {
		try {
			org.python.core.ParserFacade.parse(script, CompileMode.exec, "unnamed", new CompilerFlags(CompilerFlags.PyCF_DONT_IMPLY_DEDENT
					| CompilerFlags.PyCF_ONLY_AST));

		} catch (PySyntaxError e) {
			try {
				PyTuple pyTuple = (PyTuple) ((PyTuple) e.value).get(1);
				Integer line = (Integer) pyTuple.get(1);
				Integer column = (Integer) pyTuple.get(2);
				String lineString = (String) pyTuple.get(3);
				StringBuilder buf = new StringBuilder(lineString);
				if (lineString.length() >= column) {
					buf.insert(column, "^");
				}
				return "Error occured\n" + " - Invalid Syntax Error on line " + line + " / column " + column + "\n" + buf.toString();
			} catch (Exception ex) {
				return "Error occured while evalation PySyntaxError";
			}
		}
		return null;
	}
}
