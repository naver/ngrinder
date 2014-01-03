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
package org.ngrinder.script.handler;

import org.python.core.*;
import org.springframework.stereotype.Component;

/**
 * Jython {@link ScriptHandler}.
 *
 * @author JunHo Yoon
 * @since 3.2
 */
@Component
public class JythonScriptHandler extends ScriptHandler {

	/**
	 * Constructor.
	 */
	public JythonScriptHandler() {
		super("jython", "py", "Jython", "python");
	}

	@Override
	protected Integer order() {
		return 100;
	}

	@Override
	public String checkSyntaxErrors(String path, String script) {
		try {
			org.python.core.ParserFacade.parse(script, CompileMode.exec, path, new CompilerFlags(
					CompilerFlags.PyCF_DONT_IMPLY_DEDENT | CompilerFlags.PyCF_ONLY_AST));

		} catch (PyException e) {
			try {
				PyTuple pyTuple = (PyTuple) ((PyTuple) e.value).get(1);
				Integer line = (Integer) pyTuple.get(1);
				Integer column = (Integer) pyTuple.get(2);
				String lineString = (String) pyTuple.get(3);
				StringBuilder buf = new StringBuilder(lineString);
				if (lineString.length() >= column) {
					buf.insert(column, "^");
				}
				return "Error occurred\n" + " - Invalid Syntax Error on line " + line + " / column " + column + "\n"
						+ buf.toString();
			} catch (Exception ex) {
				return "Error occurred while evaluation python syntax";
			}
		}
		return null;
	}

	@Override
	public Integer displayOrder() {
		return 300;
	}
}
