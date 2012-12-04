/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.script.svnkitdav;

import static org.ngrinder.common.util.NoOp.noOp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.io.fs.FSRevisionRoot;
import org.tmatesoft.svn.core.internal.server.dav.DAVConfig;
import org.tmatesoft.svn.core.internal.server.dav.DAVDepth;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;
import org.tmatesoft.svn.core.internal.server.dav.DAVLock;
import org.tmatesoft.svn.core.internal.server.dav.DAVLockScope;
import org.tmatesoft.svn.core.internal.server.dav.DAVPathUtil;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceKind;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceState;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceType;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceURI;
import org.tmatesoft.svn.core.internal.server.dav.DAVServlet;
import org.tmatesoft.svn.core.internal.server.dav.DAVServletUtil;
import org.tmatesoft.svn.core.internal.server.dav.DAVXMLUtil;
import org.tmatesoft.svn.core.internal.server.dav.handlers.DAVElementProperty;
import org.tmatesoft.svn.core.internal.server.dav.handlers.DAVLockInfoProvider;
import org.tmatesoft.svn.core.internal.server.dav.handlers.DAVPropertiesProvider;
import org.tmatesoft.svn.core.internal.server.dav.handlers.DAVPropfindRequest;
import org.tmatesoft.svn.core.internal.server.dav.handlers.DAVPropsResult;
import org.tmatesoft.svn.core.internal.server.dav.handlers.DAVRequest;
import org.tmatesoft.svn.core.internal.server.dav.handlers.DAVResourceWalker;
import org.tmatesoft.svn.core.internal.server.dav.handlers.DAVResponse;
import org.tmatesoft.svn.core.internal.server.dav.handlers.IDAVResourceWalkHandler;
import org.tmatesoft.svn.core.internal.server.dav.handlers.LivePropertySpecification;
import org.tmatesoft.svn.core.internal.server.dav.handlers.ServletDAVHandler;
import org.tmatesoft.svn.core.internal.util.SVNDate;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.util.SVNXMLUtil;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.core.internal.wc.SVNPropertiesManager;
import org.tmatesoft.svn.util.SVNLogType;

/**
 * nGrinder customized version of {@link DAVPropfindHandler}. This returns error by set up status
 * code instead of calling sendError() which causes 501 error.
 * 
 * @author JunHo Yoon
 * @since 3.0.4
 * @see DAVPropfindHandler
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class DAVPropfindExHandler extends ServletDAVHandler implements IDAVResourceWalkHandler {
	public static final List<String> NAMESPACES = new LinkedList<String>();
	static {
		NAMESPACES.add(DAVElement.DAV_NAMESPACE);
		NAMESPACES.add(DAVElement.SVN_DAV_PROPERTY_NAMESPACE);
	}

	private static final String DEFAULT_AUTOVERSION_LINE = "DAV:checkout-checkin";

	private DAVPropfindRequest myDAVRequest;
	private boolean myIsAllProp;
	// private boolean myIsPropName;
	private boolean myIsProp;
	private DAVElementProperty myDocRoot;
	private StringBuffer myPropStat404;
	private StringBuffer myResponseBuffer;
	private DAVLockInfoProvider myLocksProvider;

	/**
	 * Constructor.
	 * @param connector
	 * 			repository manager
	 * @param request
	 * 			servlet request
	 * @param response
	 * 			servlet response
	 */
	public DAVPropfindExHandler(
			DAVRepositoryManager connector, HttpServletRequest request, HttpServletResponse response) {
		super(connector, request, response);
	}

	/**
	 * Get DAV request.
	 */
	protected DAVRequest getDAVRequest() {
		return getPropfindRequest();
	}

	/**
	 * Execute.
	 */
	public void execute() throws SVNException {
		DAVResource resource = getRequestedDAVResource(true, false);

		DAVResourceState resourceState = getResourceState(resource);
		if (resourceState == DAVResourceState.NULL) {
			// NHN FIX
			setResponseContentType("Application/octet-stream");
			setResponseStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		DAVDepth depth = getRequestDepth(DAVDepth.DEPTH_INFINITY);
		// TODO: check the depth is not less than 0; if it is, send BAD_REQUEST

		if (depth == DAVDepth.DEPTH_INFINITY && resource.isCollection()) {
			DAVConfig config = getConfig();
			if (!config.isAllowDepthInfinity()) {
				String message = "PROPFIND requests with a Depth of \"infinity\" are not allowed for "
								+ SVNEncodingUtil.xmlEncodeCDATA(getURI()) + ".";
				response(message, DAVServlet.getStatusLine(HttpServletResponse.SC_FORBIDDEN),
								HttpServletResponse.SC_FORBIDDEN);
				return;
			}
		}

		long readCount = readInput(false);
		DAVPropfindRequest request = getPropfindRequest();
		DAVElementProperty rootElement = request.getRootElement();

		if (readCount > 0 && rootElement.getName() != DAVElement.PROPFIND) {
			// TODO: maybe add logging here later
			// TODO: what body should we send?
			setResponseContentType("Application/octet-stream");
			setResponseStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		myIsAllProp = false;
		// myIsPropName = false;
		myIsProp = false;
		if (readCount == 0 || rootElement.hasChild(DAVElement.ALLPROP)) {
			myIsAllProp = true;
		} else if (rootElement.hasChild(DAVElement.PROPNAME)) {
			// myIsPropName = true;
			noOp();
		} else if (rootElement.hasChild(DAVElement.PROP)) {
			myIsProp = true;
		} else {
			// TODO: what body should we send?
			// TODO: maybe add logging here later
			setResponseStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		myLocksProvider = null;
		try {
			myLocksProvider = DAVLockInfoProvider.createLockInfoProvider(this, false);
		} catch (SVNException svne) {
			throw DAVException
							.convertError(svne.getErrorMessage(),
											HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
											"The lock database could not be opened, "
											+ "preventing access to the various lock properties for the PROPFIND.",
											null);
		}

		myResponseBuffer = new StringBuffer();
		DAVXMLUtil.beginMultiStatus(getHttpServletResponse(), SC_MULTISTATUS, getNamespaces(), myResponseBuffer);

		int walkType = DAVResourceWalker.DAV_WALKTYPE_NORMAL | DAVResourceWalker.DAV_WALKTYPE_AUTH
						| DAVResourceWalker.DAV_WALKTYPE_LOCKNULL;

		DAVResourceWalker walker = new DAVResourceWalker();
		DAVException error = null;
		try {
			walker.walk(myLocksProvider, resource, null, 0, null, walkType, this, depth);
		} catch (DAVException dave) {
			error = dave;
		}

		if (error != null) {
			throw new DAVException("Provider encountered an error while streaming", error.getResponseCode(), error, 0);
		}

		SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.MULTISTATUS.getName(), myResponseBuffer);
		String responseBody = myResponseBuffer.toString();

		try {
			setResponseContentLength(responseBody.getBytes(UTF8_ENCODING).length);
		} catch (UnsupportedEncodingException e) {
			setResponseContentLength(responseBody.getBytes().length);
		}

		setResponseStatus(SC_MULTISTATUS);
		try {
			getResponseWriter().write(responseBody);
		} catch (IOException e) {
			SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, e), e, SVNLogType.NETWORK);
		}
	}

	/**
	 * Handle resource.
	 */
	public DAVResponse handleResource(DAVResponse response, DAVResource resource, DAVLockInfoProvider lockInfoProvider,
					LinkedList ifHeaders, int flags, DAVLockScope lockScope, CallType callType) throws DAVException {
		DAVPropertiesProvider propsProvider = null;
		try {
			propsProvider = DAVPropertiesProvider.createPropertiesProvider(resource, this);
		} catch (DAVException dave) {
			if (myIsProp) {
				cacheBadProps();
				DAVPropsResult badProps = new DAVPropsResult();
				badProps.addPropStatsText(myPropStat404.toString());
				streamResponse(resource, 0, badProps);
			} else {
				streamResponse(resource, HttpServletResponse.SC_OK, null);
			}
			return null;
		}

		DAVPropsResult result = null;
		if (myIsProp) {
			result = getProps(propsProvider, getPropfindRequest().getRootElement());
		} else {
			DAVInsertPropAction action = myIsAllProp ? DAVInsertPropAction.INSERT_VALUE
							: DAVInsertPropAction.INSERT_NAME;
			result = getAllProps(propsProvider, action);
		}

		streamResponse(resource, 0, result);

		return null;
	}

	private DAVPropsResult getAllProps(DAVPropertiesProvider propsProvider, DAVInsertPropAction action)
					throws DAVException {
		boolean foundContentType = false;
		boolean foundContentLang = false;
		DAVPropsResult result = new DAVPropsResult();
		StringBuffer buffer = new StringBuffer();
		if (action != DAVInsertPropAction.INSERT_SUPPORTED) {
			if (propsProvider.isDeferred()) {
				propsProvider.open(true);
			}
			SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROPSTAT.getName(),
							SVNXMLUtil.XML_STYLE_NORMAL, null, buffer);
			SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROP.getName(),
							SVNXMLUtil.XML_STYLE_NORMAL, null, buffer);

			Map namespacesToPrefixes = new HashMap();
			propsProvider.defineNamespaces(namespacesToPrefixes);
			Collection propNames = propsProvider.getPropertyNames();
			int ind = 0;
			for (Iterator propNamesIter = propNames.iterator(); propNamesIter.hasNext();) {
				DAVElement propNameElement = (DAVElement) propNamesIter.next();
				if (DAVElement.DAV_NAMESPACE.equals(propNameElement.getNamespace())) {
					if (DAVElement.GET_CONTENT_TYPE.getName().equals(propNameElement.getName())) {
						foundContentType = true;
					} else if (DAVElement.GET_CONTENT_LANGUAGE.getName().equals(propNameElement.getName())) {
						foundContentLang = true;
					}
				}
				if (action == DAVInsertPropAction.INSERT_VALUE) {
					try {
						propsProvider.outputValue(propNameElement, buffer);
					} catch (DAVException dave) {
						// TODO: probably change this behavior in future
						continue;
					}
				} else {
					ind = outputPropName(propNameElement, namespacesToPrefixes, ind, buffer);
				}

			}

			generateXMLNSNamespaces(result, namespacesToPrefixes);
		}

		addAllLivePropNamespaces(result);
		insertAllLiveProps(propsProvider.getResource(), action, buffer);

		LivePropertySpecification suppLockSpec = (LivePropertySpecification) OUR_CORE_LIVE_PROPS
						.get(DAVElement.SUPPORTED_LOCK);
		insertCoreLiveProperty(propsProvider.getResource(), action, suppLockSpec, buffer);

		LivePropertySpecification lockDiscoverySpec = (LivePropertySpecification) OUR_CORE_LIVE_PROPS
						.get(DAVElement.LOCK_DISCOVERY);
		insertCoreLiveProperty(propsProvider.getResource(), action, lockDiscoverySpec, buffer);

		if (!foundContentType) {
			LivePropertySpecification getContentTypeSpec = (LivePropertySpecification) OUR_CORE_LIVE_PROPS
							.get(DAVElement.GET_CONTENT_TYPE);
			insertCoreLiveProperty(propsProvider.getResource(), action, getContentTypeSpec, buffer);
		}

		if (!foundContentLang) {
			LivePropertySpecification getContentLanguageSpec = (LivePropertySpecification) OUR_CORE_LIVE_PROPS
							.get(DAVElement.GET_CONTENT_LANGUAGE);
			insertCoreLiveProperty(propsProvider.getResource(), action, getContentLanguageSpec, buffer);
		}

		if (action != DAVInsertPropAction.INSERT_SUPPORTED) {
			SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROP.getName(), buffer);
			SVNXMLUtil.openCDataTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.STATUS.getName(), "HTTP/1.1 200 OK",
							null, false, false, buffer);
			SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROPSTAT.getName(), buffer);
		}
		result.addPropStatsText(buffer.toString());
		return result;
	}

	private void insertAllLiveProps(DAVResource resource, DAVInsertPropAction propAction, StringBuffer buffer)
					throws DAVException {
		if (!resource.exists()) {
			return;
		}

		for (Iterator livePropsIter = OUR_LIVE_PROPS.keySet().iterator(); livePropsIter.hasNext();) {
			DAVElement propElement = (DAVElement) livePropsIter.next();
			if (propElement == DAVElement.COMMENT || propElement == DAVElement.DISPLAY_NAME
							|| propElement == DAVElement.SOURCE) {
				// only RESOURCETYPE core prop should be inserted
				continue;
			}
			LivePropertySpecification lps = (LivePropertySpecification) OUR_LIVE_PROPS.get(propElement);
			insertLiveProp(resource, lps, propAction, buffer);
		}
	}

	private DAVPropsResult getProps(DAVPropertiesProvider propsProvider, DAVElementProperty docRootElement)
					throws DAVException {
		StringBuffer buffer = new StringBuffer();
		SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROPSTAT.getName(),
						SVNXMLUtil.XML_STYLE_NORMAL, null, buffer);
		SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROP.getName(), SVNXMLUtil.XML_STYLE_NORMAL,
						null, buffer);

		StringBuffer badRes = null;
		Collection<String> xmlnses = new LinkedList<String>();
		boolean haveGood = false;
		boolean definedNamespaces = false;
		Map namespacesToPrefixes = new HashMap();
		int prefixInd = 0;

		DAVElementProperty propElement = docRootElement.getChild(DAVElement.PROP);
		List childrenElements = propElement.getChildren();
		boolean filledNamespaces = false;
		for (Iterator childrenIter = childrenElements.iterator(); childrenIter.hasNext();) {
			DAVElementProperty childElement = (DAVElementProperty) childrenIter.next();
			LivePropertySpecification livePropSpec = findLiveProperty(childElement.getName());
			if (livePropSpec != null) {
				DAVInsertPropAction doneAction = insertLiveProp(propsProvider.getResource(), livePropSpec,
								DAVInsertPropAction.INSERT_VALUE, buffer);
				if (doneAction == DAVInsertPropAction.INSERT_VALUE) {
					haveGood = true;
					if (!filledNamespaces) {
						int ind = 0;
						for (Iterator namespacesIter = NAMESPACES.iterator(); namespacesIter.hasNext();) {
							String namespace = (String) namespacesIter.next();
							String xmlns = " xmlns:lp" + ind + "=\"" + namespace + "\"";
							xmlnses.add(xmlns);
							ind++;
						}
						filledNamespaces = true;
					}
					continue;
					// skip not supported properties
				} else if (doneAction == DAVInsertPropAction.NOT_SUPP) {
					continue;
				}
			}

			if (propsProvider.isDeferred()) {
				propsProvider.open(true);
			}

			boolean found = false;
			try {
				found = propsProvider.outputValue(childElement.getName(), buffer);
			} catch (DAVException dave) {
				continue;
			}

			if (found) {
				haveGood = true;
				if (!definedNamespaces) {
					propsProvider.defineNamespaces(namespacesToPrefixes);
					definedNamespaces = true;
				}
				continue;
			}

			if (badRes == null) {
				badRes = new StringBuffer();
				SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROPSTAT.getName(),
								SVNXMLUtil.XML_STYLE_NORMAL, null, badRes);
				SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROP.getName(),
								SVNXMLUtil.XML_STYLE_NORMAL, null, badRes);
			}

			prefixInd = outputPropName(childElement.getName(), namespacesToPrefixes, prefixInd, buffer);
		}

		SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROP.getName(), buffer);
		SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.STATUS.getName(),
						SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
		buffer.append("HTTP/1.1 200 OK");
		SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.STATUS.getName(), buffer);
		SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROPSTAT.getName(), buffer);

		DAVPropsResult result = new DAVPropsResult();
		if (badRes != null) {
			SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROP.getName(), badRes);
			SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.STATUS.getName(),
							SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, badRes);
			badRes.append("HTTP/1.1 404 Not Found");
			SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.STATUS.getName(), badRes);
			SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROPSTAT.getName(), badRes);
			if (!haveGood) {
				result.addPropStatsText(badRes.toString());
			} else {
				result.addPropStatsText(buffer.toString());
				result.addPropStatsText(badRes.toString());
			}
		} else {
			result.addPropStatsText(buffer.toString());
		}

		addNamespaces(result, xmlnses);
		generateXMLNSNamespaces(result, namespacesToPrefixes);
		return result;
	}

	private void addNamespaces(DAVPropsResult result, Collection xmlnses) {
		for (Iterator xmlnsesIter = xmlnses.iterator(); xmlnsesIter.hasNext();) {
			String xmlnsString = (String) xmlnsesIter.next();
			result.addNamespace(xmlnsString);
		}
	}

	private void addAllLivePropNamespaces(DAVPropsResult result) {
		for (Iterator namespacesIter = NAMESPACES.iterator(); namespacesIter.hasNext();) {
			String namespace = (String) namespacesIter.next();
			int ind = NAMESPACES.indexOf(namespace);
			String xmlnsStr = " xmlns:lp" + ind + "=\"" + namespace + "\"";
			result.addNamespace(xmlnsStr);
		}
	}

	private void generateXMLNSNamespaces(DAVPropsResult result, Map namespacesToPrefixes) {
		for (Iterator prefixesIter = namespacesToPrefixes.keySet().iterator(); prefixesIter.hasNext();) {
			String uri = (String) prefixesIter.next();
			String prefix = (String) namespacesToPrefixes.get(uri);
			result.addNamespace(" xmlns:" + prefix + "=\"" + uri + "\"");
		}
	}

	private int outputPropName(DAVElement propName, Map namespacesToPrefixes, int ind, StringBuffer buffer) {
		if ("".equals(propName.getNamespace())) {
			SVNXMLUtil.openXMLTag(null, propName.getName(), SVNXMLUtil.XML_STYLE_SELF_CLOSING, null, buffer);
		} else {
			String prefix = namespacesToPrefixes != null ? (String) namespacesToPrefixes.get(propName.getNamespace())
							: null;
			if (prefix == null) {
				prefix = "g" + ind;
				namespacesToPrefixes.put(propName.getNamespace(), prefix);
			}
			SVNXMLUtil.openXMLTag((String) namespacesToPrefixes.get(propName.getNamespace()), propName.getName(),
							SVNXMLUtil.XML_STYLE_SELF_CLOSING, null, buffer);
		}
		return ++ind;
	}

	private DAVInsertPropAction insertCoreLiveProperty(DAVResource resource, DAVInsertPropAction propAction,
					LivePropertySpecification livePropSpec, StringBuffer buffer) throws DAVException {
		DAVInsertPropAction inserted = DAVInsertPropAction.NOT_DEF;
		DAVElement livePropElement = livePropSpec.getPropertyName();
		String value = null;
		if (livePropElement == DAVElement.LOCK_DISCOVERY) {
			if (myLocksProvider != null) {
				DAVLock lock = null;
				try {
					lock = myLocksProvider.getLock(resource);
				} catch (DAVException dave) {
					throw new DAVException(
									"DAV:lockdiscovery could not be determined due to"
									+ " a problem fetching the locks for this resource.",
									dave.getResponseCode(), dave, 0);
				}

				if (lock == null) {
					value = "";
				} else {
					value = DAVLockInfoProvider.getActiveLockXML(lock);
				}
			}
		} else if (livePropElement == DAVElement.SUPPORTED_LOCK) {
			if (myLocksProvider != null) {
				value = myLocksProvider.getSupportedLock(resource);
			}
		} else if (livePropElement == DAVElement.GET_CONTENT_TYPE) {
			// TODO: get content type from a response when imitating a GET request?
			noOp();
		} else if (livePropElement == DAVElement.GET_CONTENT_LANGUAGE) {
			// TODO: get Content-Language from a response when imitating a GET request?
			noOp();
		}

		if (value != null) {
			if (propAction == DAVInsertPropAction.INSERT_SUPPORTED) {
				SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.SUPPORTED_LIVE_PROPERTY.getName(),
								SVNXMLUtil.XML_STYLE_NORMAL, "D:name", livePropElement.getName(), buffer);
			} else if (propAction == DAVInsertPropAction.INSERT_VALUE && !"".equals(value)) {
				SVNXMLUtil.openCDataTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, livePropElement.getName(), value, null, false,
								false, buffer);
			} else {
				SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, livePropElement.getName(),
								SVNXMLUtil.XML_STYLE_SELF_CLOSING, null, buffer);
			}
			inserted = propAction;
		}
		return inserted;
	}

	private DAVInsertPropAction insertLiveProp(DAVResource resource, LivePropertySpecification livePropSpec,
					DAVInsertPropAction propAction, StringBuffer buffer) throws DAVException {
		if (!livePropSpec.isSVNSupported()) {
			// this is a core WebDAV live prop
			return insertCoreLiveProperty(resource, propAction, livePropSpec, buffer);
		}

		DAVElement livePropElement = livePropSpec.getPropertyName();
		if (!resource.exists() && livePropElement != DAVElement.VERSION_CONTROLLED_CONFIGURATION
						&& livePropElement != DAVElement.BASELINE_RELATIVE_PATH) {
			return DAVInsertPropAction.NOT_SUPP;
		}

		String value = null;
		DAVResourceURI uri = resource.getResourceURI();
		if (livePropElement == DAVElement.GET_LAST_MODIFIED || livePropElement == DAVElement.CREATION_DATE) {
			if (resource.getType() == DAVResourceType.PRIVATE && resource.getKind() == DAVResourceKind.VCC) {
				return DAVInsertPropAction.NOT_SUPP;
			}

			if (livePropElement == DAVElement.CREATION_DATE) {
				try {
					value = SVNDate.formatDate(getLastModifiedTime2(resource));
				} catch (SVNException svne) {
					return DAVInsertPropAction.NOT_DEF;
				}
			} else if (livePropElement == DAVElement.GET_LAST_MODIFIED) {
				try {
					value = SVNDate.formatRFC1123Date(getLastModifiedTime2(resource));
				} catch (SVNException svne) {
					return DAVInsertPropAction.NOT_DEF;
				}
			}
			value = SVNEncodingUtil.xmlEncodeCDATA(value, true);
		} else if (livePropElement == DAVElement.CREATOR_DISPLAY_NAME) {
			if (resource.getType() == DAVResourceType.PRIVATE && resource.getKind() == DAVResourceKind.VCC) {
				return DAVInsertPropAction.NOT_SUPP;
			}

			long committedRev = -1;
			if (resource.isBaseLined() && resource.getType() == DAVResourceType.VERSION) {
				committedRev = resource.getRevision();
			} else if (resource.getType() == DAVResourceType.REGULAR || resource.getType() == DAVResourceType.WORKING
							|| resource.getType() == DAVResourceType.VERSION) {
				try {
					committedRev = resource.getCreatedRevisionUsingFS(null);
				} catch (SVNException svne) {
					value = "###error###";
				}
			} else {
				return DAVInsertPropAction.NOT_SUPP;
			}

			String lastAuthor = null;
			try {
				lastAuthor = resource.getAuthor(committedRev);
			} catch (SVNException svne) {
				value = "###error###";
			}

			if (lastAuthor == null) {
				return DAVInsertPropAction.NOT_DEF;
			}

			value = SVNEncodingUtil.xmlEncodeCDATA(lastAuthor, true);
		} else if (livePropElement == DAVElement.GET_CONTENT_LANGUAGE) {
			return DAVInsertPropAction.NOT_SUPP;
		} else if (livePropElement == DAVElement.GET_CONTENT_LENGTH) {
			if (resource.isCollection() || resource.isBaseLined()) {
				return DAVInsertPropAction.NOT_SUPP;
			}

			long fileSize = 0;
			try {
				fileSize = resource.getContentLength(null);
				value = String.valueOf(fileSize);
			} catch (SVNException e) {
				value = "0";
			}
		} else if (livePropElement == DAVElement.GET_CONTENT_TYPE) {
			if (resource.isBaseLined() && resource.getType() == DAVResourceType.VERSION) {
				return DAVInsertPropAction.NOT_SUPP;
			}

			if (resource.getType() == DAVResourceType.PRIVATE && resource.getKind() == DAVResourceKind.VCC) {
				return DAVInsertPropAction.NOT_SUPP;
			}

			if (resource.isCollection()) {
				value = DAVResource.DEFAULT_COLLECTION_CONTENT_TYPE;
			} else {
				SVNPropertyValue contentType = null;
				try {
					contentType = resource.getProperty(null, SVNProperty.MIME_TYPE);
				} catch (SVNException svne) {
					noOp();
				}

				if (contentType != null) {
					value = contentType.getString();
				} else if (!resource.isSVNClient() && getRequest().getContentType() != null) {
					value = getRequest().getContentType();
				} else {
					value = DAVResource.DEFAULT_FILE_CONTENT_TYPE;
				}

				try {
					SVNPropertiesManager.validateMimeType(value);
				} catch (SVNException svne) {
					return DAVInsertPropAction.NOT_DEF;
				}
			}
		} else if (livePropElement == DAVElement.GET_ETAG) {
			if (resource.getType() == DAVResourceType.PRIVATE && resource.getKind() == DAVResourceKind.VCC) {
				return DAVInsertPropAction.NOT_SUPP;
			}

			value = resource.getETag();
		} else if (livePropElement == DAVElement.AUTO_VERSION) {
			if (getConfig().isAutoVersioning()) {
				value = DEFAULT_AUTOVERSION_LINE;
			} else {
				return DAVInsertPropAction.NOT_DEF;
			}
		} else if (livePropElement == DAVElement.BASELINE_COLLECTION) {
			if (resource.getType() != DAVResourceType.VERSION || !resource.isBaseLined()) {
				return DAVInsertPropAction.NOT_SUPP;
			}
			value = DAVPathUtil.buildURI(uri.getContext(), DAVResourceKind.BASELINE_COLL, resource.getRevision(), null,
							true);
		} else if (livePropElement == DAVElement.CHECKED_IN) {
			String s = null;
			if (resource.getType() == DAVResourceType.PRIVATE && resource.getKind() == DAVResourceKind.VCC) {
				long revNum = -1;
				try {
					revNum = resource.getLatestRevision();
					s = DAVPathUtil.buildURI(uri.getContext(), DAVResourceKind.BASELINE, revNum, null, false);
					StringBuffer buf = SVNXMLUtil.openCDataTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX,
									DAVElement.HREF.getName(), s, null, true, true, null);
					value = buf.toString();
				} catch (SVNException svne) {
					value = "###error###";
				}
			} else if (resource.getType() != DAVResourceType.REGULAR) {
				return DAVInsertPropAction.NOT_SUPP;
			} else {
				long revToUse = DAVServletUtil.getSafeCreatedRevision((FSRevisionRoot) resource.getRoot(),
								uri.getPath());
				s = DAVPathUtil.buildURI(uri.getContext(), DAVResourceKind.VERSION, revToUse, uri.getPath(), false);
				StringBuffer buf = SVNXMLUtil.openCDataTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.HREF.getName(),
								s, null, true, true, null);
				value = buf.toString();
			}
		} else if (livePropElement == DAVElement.VERSION_CONTROLLED_CONFIGURATION) {
			if (resource.getType() != DAVResourceType.REGULAR) {
				return DAVInsertPropAction.NOT_SUPP;
			}
			value = DAVPathUtil.buildURI(uri.getContext(), DAVResourceKind.VCC, -1, null, true);
		} else if (livePropElement == DAVElement.VERSION_NAME) {
			if (resource.getType() != DAVResourceType.VERSION && !resource.isVersioned()) {
				return DAVInsertPropAction.NOT_SUPP;
			}

			if (resource.getType() == DAVResourceType.PRIVATE && resource.getKind() == DAVResourceKind.VCC) {
				return DAVInsertPropAction.NOT_SUPP;
			}

			if (resource.isBaseLined()) {
				value = String.valueOf(resource.getRevision());
			} else {
				try {
					long committedRev = resource.getCreatedRevisionUsingFS(null);
					value = String.valueOf(committedRev);
					value = SVNEncodingUtil.xmlEncodeCDATA(value, true);
				} catch (SVNException svne) {
					value = "###error###";
				}
			}
		} else if (livePropElement == DAVElement.BASELINE_RELATIVE_PATH) {
			if (resource.getType() != DAVResourceType.REGULAR) {
				return DAVInsertPropAction.NOT_SUPP;
			}
			value = SVNEncodingUtil.xmlEncodeCDATA(DAVPathUtil.dropLeadingSlash(uri.getPath()), true);
		} else if (livePropElement == DAVElement.MD5_CHECKSUM) {
			if (!resource.isCollection()
							&& !resource.isBaseLined()
							&& (resource.getType() == DAVResourceType.REGULAR
											|| resource.getType() == DAVResourceType.VERSION 
											|| resource.getType() == DAVResourceType.WORKING)) {
				try {
					value = resource.getMD5Checksum(null);
					if (value == null) {
						return DAVInsertPropAction.NOT_SUPP;
					}
				} catch (SVNException svne) {
					value = "###error###";
				}
			} else {
				return DAVInsertPropAction.NOT_SUPP;
			}
		} else if (livePropElement == DAVElement.REPOSITORY_UUID) {
			try {
				value = resource.getRepositoryUUID(false);
			} catch (SVNException svne) {
				value = "###error###";
			}
		} else if (livePropElement == DAVElement.DEADPROP_COUNT) {
			if (resource.getType() != DAVResourceType.REGULAR) {
				return DAVInsertPropAction.NOT_SUPP;
			}

			SVNProperties props = null;
			try {
				props = resource.getSVNProperties(null);
				int deadPropertiesCount = props.size();
				value = String.valueOf(deadPropertiesCount);
			} catch (SVNException svne) {
				value = "###error###";
			}
		} else if (livePropElement == DAVElement.RESOURCE_TYPE) {
			if (resource.getType() == DAVResourceType.VERSION) {
				if (resource.isBaseLined()) {
					StringBuffer buf = SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX,
									DAVElement.BASELINE.getName(), SVNXMLUtil.XML_STYLE_SELF_CLOSING
													| SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, null);
					value = buf.toString();

				}
			} else if (resource.getType() == DAVResourceType.REGULAR || resource.getType() == DAVResourceType.WORKING) {
				if (resource.isCollection()) {
					StringBuffer buf = SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX,
									DAVElement.COLLECTION.getName(), SVNXMLUtil.XML_STYLE_SELF_CLOSING
													| SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, null);
					value = buf.toString();
				} else {
					value = "";
				}
			} else if (resource.getType() == DAVResourceType.HISTORY) {
				StringBuffer buf = SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX,
								DAVElement.VERSION_HISTORY.getName(), SVNXMLUtil.XML_STYLE_SELF_CLOSING
												| SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, null);
				value = buf.toString();
			} else if (resource.getType() == DAVResourceType.WORKSPACE) {
				StringBuffer buf = SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX,
								DAVElement.COLLECTION.getName(), SVNXMLUtil.XML_STYLE_SELF_CLOSING
												| SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, null);
				value = buf.toString();
			} else if (resource.getType() == DAVResourceType.ACTIVITY) {
				StringBuffer buf = SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX,
								DAVElement.ACTIVITY.getName(), SVNXMLUtil.XML_STYLE_SELF_CLOSING
												| SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, null);
				value = buf.toString();
			} else {
				return DAVInsertPropAction.NOT_DEF;
			}
		} else if (livePropElement == DAVElement.COMMENT || livePropElement == DAVElement.CREATOR_DISPLAY_NAME
						|| livePropElement == DAVElement.DISPLAY_NAME || livePropElement == DAVElement.SOURCE) {
			return DAVInsertPropAction.NOT_DEF;
		} else {
			return DAVInsertPropAction.NOT_SUPP;
		}

		int ind = NAMESPACES.indexOf(livePropElement.getNamespace());
		String prefix = "lp" + ind;
		if (propAction == DAVInsertPropAction.INSERT_NAME
						|| (propAction == DAVInsertPropAction.INSERT_VALUE && (value == null || value.length() == 0))) {
			SVNXMLUtil.openXMLTag(prefix, livePropElement.getName(), SVNXMLUtil.XML_STYLE_SELF_CLOSING, null, buffer);
		} else if (propAction == DAVInsertPropAction.INSERT_VALUE) {
			SVNXMLUtil.openCDataTag(prefix, livePropElement.getName(), value, null, false, false, buffer);
		} else {
			Map attrs = new HashMap();
			attrs.put("D:name", livePropElement.getName());
			attrs.put("D:namespace", livePropElement.getNamespace());
			SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.SUPPORTED_LIVE_PROPERTY.getName(),
							SVNXMLUtil.XML_STYLE_SELF_CLOSING, attrs, buffer);
		}

		return propAction;
	}

	private Date getLastModifiedTime2(DAVResource resource) throws SVNException {
		long revision = -1;
		if (resource.isBaseLined() && resource.getType() == DAVResourceType.VERSION) {
			revision = resource.getRevision();
		} else if (resource.getType() == DAVResourceType.REGULAR || resource.getType() == DAVResourceType.WORKING
						|| resource.getType() == DAVResourceType.VERSION) {
			revision = resource.getCreatedRevisionUsingFS(null);
		} else {
			SVNErrorManager.error(
							SVNErrorMessage.create(SVNErrorCode.RA_DAV_PROPS_NOT_FOUND, "Failed to determine property"),
							SVNLogType.NETWORK);
		}
		return resource.getRevisionDate(revision);
	}

	private void streamResponse(DAVResource resource, int status, DAVPropsResult propStats) {
		DAVResponse response = new DAVResponse(
				null, resource.getResourceURI().getRequestURI(), null, propStats, status);
		DAVXMLUtil.sendOneResponse(response, myResponseBuffer);
	}

	private void cacheBadProps() {
		if (myPropStat404 != null) {
			return;
		}

		myPropStat404 = new StringBuffer();
		SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROPSTAT.getName(),
						SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, myPropStat404);
		SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROP.getName(),
						SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, myPropStat404);
		DAVElementProperty elem = myDocRoot.getChild(DAVElement.PROP);
		List childrenElements = elem.getChildren();
		for (Iterator childrenIter = childrenElements.iterator(); childrenIter.hasNext();) {
			DAVElementProperty childElement = (DAVElementProperty) childrenIter.next();
			DAVXMLUtil.addEmptyElement(
					DAVPropfindExHandler.this.getNamespaces(), childElement.getName(), myPropStat404);
		}

		SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROP.getName(), myPropStat404);
		SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.STATUS.getName(),
						SVNXMLUtil.XML_STYLE_NORMAL, null, myPropStat404);
		myPropStat404.append("HTTP/1.1 404 Not Found");
		SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.STATUS.getName(), myPropStat404);
		SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROPSTAT.getName(), myPropStat404);
	}

	private DAVPropfindRequest getPropfindRequest() {
		if (myDAVRequest == null) {
			myDAVRequest = new DAVPropfindRequest();
		}
		return myDAVRequest;
	}

	private static final class DAVInsertPropAction {
		public static final DAVInsertPropAction NOT_DEF = new DAVInsertPropAction();
		public static final DAVInsertPropAction NOT_SUPP = new DAVInsertPropAction();
		public static final DAVInsertPropAction INSERT_VALUE = new DAVInsertPropAction();
		public static final DAVInsertPropAction INSERT_NAME = new DAVInsertPropAction();
		public static final DAVInsertPropAction INSERT_SUPPORTED = new DAVInsertPropAction();

		private DAVInsertPropAction() {
		}
	}
}
