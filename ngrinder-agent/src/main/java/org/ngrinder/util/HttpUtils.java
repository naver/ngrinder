package org.ngrinder.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is used to provide the functions to connect and call some methods 
 * in remote Agent module. It will call with http request and send 
 * parameters in JSON format. 
 * 
 * @author Mavlarn & liuzhifei
 *
 */
public class HttpUtils {

	private static final Logger LOG = LoggerFactory.getLogger(HttpUtils.class);
	
	private static HttpClient client;
	
	private static String address;

	/**
	 * connect to a remote BO module.
	 * @param address is the address of remote module, format is "http://localhost:5011".
	 * port should be same as that in apiContext.xml 
	 * @throws URISyntaxException 
	 */
	private static void open(String newAddress) throws URISyntaxException{
		if (client == null || address == null ||
				!address.equalsIgnoreCase(newAddress)) {
			
				URI uri;
				try {
					uri = new URI(newAddress);
				} catch (URISyntaxException e) {
					LOG.error("URISyntaxException happened in constructing URI: " + newAddress);
					throw e;
				}
			
			client = new HttpClient();
			HttpConnectionManagerParams params = client.getHttpConnectionManager().getParams();
			params.setConnectionTimeout(500); // connection timeout value is 500ms
			params.setSoTimeout(1000); //1000ms, timeout value for request data
			params.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
			client.getHostConfiguration().setHost(uri.getHost(), uri.getPort());

			address = newAddress;
		}
	}


	/**
	 * this method is used to call a method of a BO in remote bloc module.
	 * @param ip is the ip address of module.
	 * @param port is the port of module.
	 * @param module is module name of remote bloc
	 * @param resource is the BO name in remote module
	 * @param procedure is the function name of BO resource.
	 * @param paramObj is the parameters. 
	 * @return JSON formated string returned  from BO function.
	 * @throws Exception
	 */
	public static String execute(String ip, int port, String appName, String controllerName, String methodName,
			Object paramObj) throws Exception {
		String paramStr = null;
		if (paramObj instanceof Map) {
			StringBuilder params = new StringBuilder();
			for (Map.Entry<?, ?> kv : ((Map<?, ?>) paramObj).entrySet()) {
				params.append(kv.getKey()).append("=").append(kv.getValue()).append("&");
			}
			paramStr = params.toString();
		} else if (paramObj instanceof String) {
			paramStr = (String) paramObj;
		} else {
			paramStr = "";
		}
		return execute(ip, port, appName, controllerName, methodName, paramStr);
	}

	private static String execute(String ip, int port, String appName, String controllerName, String methodName,
			String query) throws Exception {
		String newAddress = "http://" + ip + ":" + port;

		PostMethod method = null;
		String methodStr = ("".equals(appName.trim()) ? "/" : "/" + appName + "/") + controllerName + "/" + methodName;
		try {
			open(newAddress);
			method = new PostMethod(methodStr);
			method.setQueryString(query);
			client.executeMethod(method);
			return method.getResponseBodyAsString();
		} catch (Exception e) {
			String errMsg = "Exception happened in executing HTTP method:" + newAddress + "/" + methodStr + query
					+ ". Reason:" + e.getMessage();
			LOG.error(errMsg);
			throw new Exception(errMsg, e);
		} finally {
			if (method != null) {
				method.releaseConnection();
			}
		}
	}

	/**
	 * convert a JSON string to Map and return.
	 * @param str is a JSON formated string.
	 * @return converted Map. If the string is not a valid JSON string, return null. 
	 */
	@SuppressWarnings("rawtypes")
	public static Map getAsMap(String str) {
		try {
			JSONObject json = (JSONObject)JSONValue.parse(str);
			return json;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
		
	}
}
