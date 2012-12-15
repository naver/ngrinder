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
package org.ngrinder.script.svnkitdav;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.server.dav.handlers.DAVHandlerFactory;
import org.tmatesoft.svn.core.internal.server.dav.handlers.ServletDAVHandler;

/**
 * nGrinder customized version of DAVHandler Factory. It dispatches {@link DAVPropfindExHandler}
 * instead of {@link DAVPropfindHandler} which fixes the bug.
 * 
 * @author JunHo Yoon
 * @since 3.0.4
 */
public final class DAVHandlerExFactory {

	public static final String METHOD_PROPFIND = "PROPFIND";
	
	private DAVHandlerExFactory() {
	}

	/**
	 * Create a servlet DAV handler.
	 * 
	 * @param manager
	 * 			manager
	 * @param request
	 * 			servlet request
	 * @param response
	 * 			servlet response
	 * 
	 * @return handler
	 * 
	 * @throws SVNException
	 * 			method is not PROPFIND
	 */
	public static ServletDAVHandler createHandler(DAVRepositoryManager manager, HttpServletRequest request,
					HttpServletResponse response) throws SVNException {
		String methodName = request.getMethod();

		if (METHOD_PROPFIND.equals(methodName)) {
			return new DAVPropfindExHandler(manager, request, response);
		} else {
			return DAVHandlerFactory.createHandler(manager, request, response);
		}
	}

}
