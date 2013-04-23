package net.grinder.lang;

import groovy.lang.GroovyClassLoader;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.python.core.CompileMode;
import org.python.core.CompilerFlags;
import org.python.core.PySyntaxError;
import org.python.core.PyTuple;

import net.grinder.util.GrinderClassPathProcessor;

public enum Lang {
	/** Groovy */
	Groovy("groovy", "Groovy", "groovy", new GroovyGrinderClassPathUtils()) {
		@Override
		public String checkSyntaxErrors(String script) {
			GroovyClassLoader loader = new GroovyClassLoader();
			try {
				loader.parseClass(script);
			} catch (Exception e) {
				return e.getMessage();
			}
			return null;
		}
	},
	/** Jython */
	Jython("py", "Jython", "python", new JythonGrinderClassPathUtils()) {
		@Override
		public String checkSyntaxErrors(String script) {
			try {
				org.python.core.ParserFacade.parse(script, CompileMode.exec, "unnamed", new CompilerFlags(
								CompilerFlags.PyCF_DONT_IMPLY_DEDENT | CompilerFlags.PyCF_ONLY_AST));

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
					return "Error occured\n" + " - Invalid Syntax Error on line " + line + " / column " + column + "\n"
									+ buf.toString();
				} catch (Exception ex) {
					return "Error occured while evalation PySyntaxError";
				}
			}
			return null;
		}
	};

	private final String extension;
	private final GrinderClassPathProcessor classPathProcessor;
	private final String title;
	private final String codemirror;

	Lang(String extension, String title, String codemirror, GrinderClassPathProcessor classPathProcessor) {
		this.extension = extension;
		this.title = title;
		this.codemirror = codemirror;
		this.classPathProcessor = classPathProcessor;
	}

	public GrinderClassPathProcessor getGrinderClassPathProcessor() {
		return classPathProcessor;
	}

	public String getExtension() {
		return extension;
	}

	public static Lang getByFileName(String fileName) {
		String extension = FilenameUtils.getExtension(fileName);
		for (Lang each : values()) {
			if (each.getExtension().equals(extension)) {
				return each;
			}
		}
		return Jython;
	}

	public static Lang getByFileName(File file) {
		return getByFileName(file.getName());
	}

	public String getTitle() {
		return title;
	}

	public String getCodemirror() {
		return codemirror;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.script.service.IScriptValidationService#checkSyntaxErrors(java.lang.String)
	 */

	public abstract String checkSyntaxErrors(String script);
}
