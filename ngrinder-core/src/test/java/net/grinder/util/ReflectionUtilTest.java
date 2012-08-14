//package net.grinder.util;
//
//import static org.hamcrest.Matchers.notNullValue;
//import static org.junit.Assert.assertThat;
//
//import java.io.File;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLClassLoader;
//import java.util.Map.Entry;
//
//import org.junit.Test;
//import org.ngrinder.NGrinderStarter;
//
//public class ReflectionUtilTest {
//	@Test
//	public void testReflectionUtil() throws MalformedURLException {
//		for (Entry<Object, Object> each : System.getProperties().entrySet()) {
//			System.out.println(each.getKey() + "=" + each.getValue());
//		}
//		URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
//		ReflectionUtil.invokePrivateMethod(urlClassLoader, "addURL", new Object[] { new File("c:/hello").toURI()
//				.toURL() });
//
//	}
//
//	@Test
//	public void testToolsJarPath() {
//		NGrinderStarter starter = new NGrinderStarter();
//		URL findToolsJarPath = starter.findToolsJarPath();
//		assertThat(findToolsJarPath, notNullValue());
//	}
//}
