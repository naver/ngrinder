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
package net.grinder.util;

import HTTPClient.NVPair;
import net.grinder.common.GrinderProperties;
import net.grinder.script.Grinder;
import net.grinder.script.InternalScriptContext;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.csvreader.CsvReader;

/**
 * Convenient NGrinder utilities.
 *
 * @author JunHo Yoon
 * @since 3.2.3
 */
@SuppressWarnings({"UnusedDeclaration", "SpellCheckingInspection"})
public abstract class GrinderUtils {

	/**
	 * 读取csv文件方法
	 * @param filePath 文件路径
	 * @param columnName 列名称
	 * @return
	 */
	public static List getFileParams(String filePath , String columnName) {
		List list = new ArrayList();
		CsvReader reader = null;
		try {
			//初始化CsvReader并指定列分隔符和字符编码
			reader = new CsvReader(filePath, ',', Charset.forName("UTF-8"));
			reader.readHeaders();
			while (reader.readRecord()) {
				if (reader.get(columnName) != null && !"".equals(reader.get(columnName)))
					list.add(reader.get(columnName));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
				//关闭CsvReader
				reader.close();
		}
		return list;
	}

	/**
	 * 读取csv文件方法
	 * @param filePath 文件路径
	 * @param columnNum 列序号，从0开始
	 * @return
	 */
	public static List getFileParams(String filePath , int columnNum) {
		List list = new ArrayList();
		CsvReader reader = null;
		try {
			//初始化CsvReader并指定列分隔符和字符编码
			reader = new CsvReader(filePath, ',', Charset.forName("UTF-8"));
			reader.readHeaders();
			while (reader.readRecord()) {
				if (reader.get(columnNum) != null && !"".equals(reader.get(columnNum)))
					list.add(reader.get(columnNum));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
				//关闭CsvReader
				reader.close();
		}
		return list;
	}

	/**
	 * 获取所有场景执行中的唯一值，可结合grinder.runNumber使用。
	 * @return totalRunCount
	 * @author lingj
	 */
	public static int getRunsUniqId() {
		InternalScriptContext grinder = getGrinderInstance();
		GrinderProperties properties = grinder.getProperties();
		if (properties != null) {
			int totalProcessCount = properties.getInt("grinder.processes", 1);
			int totalThreadCount = properties.getInt("grinder.threads", 1);
			int totalRunCount = properties.getInt("grinder.runs", 1);
			int agentNumber = grinder.getAgentNumber();
			int processNumber = grinder.getProcessNumber();
			int threadNumber = grinder.getThreadNumber();
			// Calc the current total sence unique id
			return ((agentNumber * totalProcessCount * totalThreadCount) + (processNumber * totalThreadCount) + threadNumber) * totalRunCount;
		}
		return 0;
	}

	/**
	 * 获取每次运行均生成唯一Id的方法，与进程数无关，可结合grinder.runNumber使用。
	 * @return uniqNumExpProId
	 * @author lingj
	 */
	public static int getRunsUniqNumExpProId() {
		InternalScriptContext grinder = getGrinderInstance();
		GrinderProperties properties = grinder.getProperties();
		if (properties != null) {
			int totalThreadCount = properties.getInt("grinder.threads", 1);
			int totalRunCount = properties.getInt("grinder.runs", 1);
			int agentNumber = grinder.getAgentNumber();
			int threadNumber = grinder.getThreadNumber();
			// Calc the current run number unique id independent of process number
			return ((agentNumber * totalThreadCount)  + threadNumber) * totalRunCount;
		}
		return 0;
	}

	/**
	 * 获取每次运行均生成唯一Id的方法，与进程数无关
	 * @return uniqNumExpProId
	 * @author lingj
	 */
	public static int getUniqNumExpProId() {
		InternalScriptContext grinder = getGrinderInstance();
		GrinderProperties properties = grinder.getProperties();
		if (properties != null) {
			int totalThreadCount = properties.getInt("grinder.threads", 1);
			int agentNumber = grinder.getAgentNumber();
			int threadNumber = grinder.getThreadNumber();
			// Calc the current thread's unique id independent of process number
			return (agentNumber * totalThreadCount)  + threadNumber;
		}
		return 0;
	}

	/**
	 * 获取测试配置中运行的总次数
	 * @return totalRunCount
	 * @author lingj
	 */
	public static int getTotalRunCount() {
		InternalScriptContext grinder = getGrinderInstance();
		GrinderProperties properties = grinder.getProperties();
		if (properties != null) {
			// Get the total run count
			return properties.getInt("grinder.runs", 1);
		}
		return 0;
	}

	/**
	 * 截取动态字符串，获取动态数据
	 * @param inputStr 输入字符串
	 * @param startStr 字符串左边界
	 * @param startStrLength 截取长度
	 * @return 截取字符串
	 */
	public static String getCutStr(String inputStr, String startStr, int startStrLength){
		int startIdx = inputStr.indexOf(startStr)+startStr.length();
		return inputStr.substring(startIdx,startIdx+startStrLength);
	}

	/**
	 * 截取动态字符串，获取动态数据
	 * @param inputStr 输入字符串
	 * @param startStr 字符串左边界
	 * @param endStr 字符串右边界
	 * @return 截取字符串
	 */
	public static String getCutStrLR(String inputStr, String startStr, String endStr){
		int startIdx = inputStr.indexOf(startStr)+startStr.length();
		int endIdx = inputStr.indexOf(endStr);
		return inputStr.substring(startIdx,endIdx);
	}

	//*********************add by lingj

	/**
	 * Get this thread unique id among all threads in the all agents.
	 *
	 * @return unique id b/w from 0 to total thread count.
	 * @since 3.2.3
	 */
	public static int getThreadUniqId() {
		InternalScriptContext grinder = getGrinderInstance();
		GrinderProperties properties = grinder.getProperties();
		if (properties != null) {
			int totalProcessCount = properties.getInt("grinder.processes", 1);
			int totalThreadCount = properties.getInt("grinder.threads", 1);
			int agentNumber = grinder.getAgentNumber();
			int processNumber = grinder.getProcessNumber();
			int threadNumber = grinder.getThreadNumber();
			// Calc the current thread's unique id
			return (agentNumber * totalProcessCount * totalThreadCount) + (processNumber * totalThreadCount) + threadNumber;
		}
		return 0;
	}

	private static InternalScriptContext getGrinderInstance() {
		InternalScriptContext grinder = Grinder.grinder;
		if (grinder == null || grinder.getThreadNumber() == -1) {
			throw new RuntimeException("This method should be called in the worker thread context.");
		}
		return grinder;
	}

	private static Random random = new Random();

	/**
	 * Convert nvpair map to array. With this user can create NVPair array using
	 * following code.
	 *
	 *
	 * <code>
	 * import static net.ngrinder.util.GrinderUtil.*
	 * ...
	 * request1.POST("http://www.google.com", nvs(["key1":"value1", "key2":"value2"]))
	 * </code>
	 *
	 * @param nvpairMap map of the
	 * @return converted array
	 */
	public static NVPair[] nvs(Map<Object, Object> nvpairMap) {
		NVPair[] result = new NVPair[nvpairMap.size()];
		int i = 0;
		for (Entry<Object, Object> each : nvpairMap.entrySet()) {
			result[i++] = new NVPair(each.getKey().toString(), each.getValue().toString());
		}
		return result;
	}

	/**
	 * Get the any element from list.
	 *
	 *
	 * <code>
	 * import static net.ngrinder.util.GrinderUtil.*
	 * ...
	 * def values = [1,2,3,4,5]
	 * def selected = any(values)
	 * </code>
	 *
	 * @param from list
	 * @param <T>  element type
	 * @return any element in the list
	 * @since 3.2.3
	 */
	public static <T> T any(List<T> from) {
		return from.get(random.nextInt(from.size()));
	}

	/**
	 * Get the any element from araay.
	 *
	 * @param from list
	 * @param <T>  element type
	 * @return any element in the list
	 * @since 3.2.3
	 */
	public static <T> T any(T[] from) {
		return from[random.nextInt(from.length)];
	}

	/**
	 * Get the parameter passed by controller. When it's executed in the
	 * validation mode, always returns empty string.
	 *
	 * @return param. empty string if none.
	 * @since 3.2.3
	 */
	public static String getParam() {
		return getParam("");
	}

	/**
	 * Get the parameter passed by controller. When it's executed in the
	 * validation mode, always returns the given default value.
	 *
	 * @param defaultValue default value
	 * @return param. default value string if the param was not provided.
	 * @since 3.2.3
	 */
	public static String getParam(String defaultValue) {
		return System.getProperty("param", defaultValue);
	}

	/**
	 * Get the parameter passed by controller. When it's executed in the
	 * validation mode, always returns the given default value 0.
	 *
	 * @return param. 0 if the param was not provided.
	 * @since 3.2.3
	 */
	public static int getParamInt() {
		return NumberUtils.toInt(getParam("0"), 0);
	}

	/**
	 * Get the parameter passed by controller. When it's executed in the
	 * validation mode, always returns the given default value 0.
	 *
	 * @return param. 0 if the param was not provided.
	 * @since 3.2.3
	 */
	public static long getParamLong() {
		return NumberUtils.toLong(getParam("0"), 0);
	}

	/**
	 * Get the parameter passed by controller. When it's executed in the
	 * validation mode, always returns the given default value 0.
	 *
	 * @return param. 0 if the param was not provided.
	 * @since 3.2.3
	 */
	public static float getParamFloat() {
		return NumberUtils.toFloat(getParam("0"), 0f);
	}

	/**
	 * Get the parameter passed by controller. When it's executed in the
	 * validation mode, always returns the given default value 0.
	 *
	 * @return param. 0 if the param was not provided.
	 * @since 3.2.3
	 */
	public static double getParamDouble() {
		return NumberUtils.toDouble(getParam("0"), 0);
	}

	/**
	 * Get the parameter passed by controller. When it's executed in the
	 * validation mode, always returns the given default value(false).
	 *
	 * @return param. false if the param was not provided.
	 * @since 3.2.3
	 */
	public static boolean getParamBoolean() {
		return BooleanUtils.toBoolean(getParam("false"));
	}

	/**
	 * Get the total agent count.
	 *
	 * @return agent count.
	 */
	public static int getAgentCount() {
		return getGrinderInstance().getProperties().getInt("grinder.agents", 1);
	}

	/**
	 * Get the process count per an agent.
	 *
	 * @return process assigned per an agent
	 */
	public static int getProcessCount() {
		return getGrinderInstance().getProperties().getInt("grinder.processes", 1);
	}

	/**
	 * Get the thread count per a process.
	 *
	 * @return thread count assigned per a process
	 */
	public static int getThreadCount() {
		return getGrinderInstance().getProperties().getInt("grinder.threads", 1);
	}
}
