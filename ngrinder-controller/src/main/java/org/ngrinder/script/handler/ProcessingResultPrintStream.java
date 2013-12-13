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

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.PrintStream;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * Processing Result PrintStream to store the result of the execution and logs.
 * 
 * @author JunHo Yoon
 * @since 3.2
 */
public class ProcessingResultPrintStream extends PrintStream {
	private boolean success = false;
	private final ByteArrayOutputStream byteArrayOutputStream;

	/**
	 * Constructor.
	 * 
	 * @param byteArrayOutputStream the output stream in which the logs are saved.
	 */
	public ProcessingResultPrintStream(ByteArrayOutputStream byteArrayOutputStream) {
		super(checkNotNull(byteArrayOutputStream));
		this.byteArrayOutputStream = byteArrayOutputStream;

	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * Get the log's byte array.
	 * 
	 * @return byte array
	 */
	public byte[] getLogByteArray() {
		return byteArrayOutputStream.toByteArray();
	}

}
