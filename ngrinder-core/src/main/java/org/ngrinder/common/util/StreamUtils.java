package org.ngrinder.common.util;

import java.util.function.Function;

public class StreamUtils {
	public static <T, R> Function<T, R> exceptionWrapper(ExceptionFunction<T, R> fe) {
		return arg -> {
			try {
				return fe.apply(arg);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	@FunctionalInterface
	public interface ExceptionFunction<T, R> {
		R apply(T arg) throws Exception;
	}
}
