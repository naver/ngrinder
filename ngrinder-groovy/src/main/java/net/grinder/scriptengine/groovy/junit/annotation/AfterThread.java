package net.grinder.scriptengine.groovy.junit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When nGrinder Groovy test, it might be good if you can provide the per thread initiation code.
 * 
 * Here is a simple example:
 * 
 * <pre>
 * public class Example {
 * 	List empty;
 * 
 * 	&#064;BeforeThread
 * 	public void initialize() {
 * 		empty = new ArrayList();
 * 	}
 * 
 * 	&#064;Test public void size() {
 *       ...
 *    }
 * 
 * 	&#064;Test public void remove() {
 *       ...
 *    }
 * }
 * </pre>
 * 
 * @see org.junit.BeforeClass
 * @see org.junit.After
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AfterThread {
}
