package com.xuhailiang5794.clearlyknow.manager.utils;

public final class IndexUtils {

	public static boolean isNumberClass(Class<?> clazz) {
		if (Number.class.isAssignableFrom(clazz)) {
			return true;
		}
		return false;
	}

	public static boolean isNumericType(Class<?> clazz) {
		if (clazz == Double.class || clazz == Float.class
				|| clazz == Integer.class || clazz == Long.class)
			return true;
		return false;
	}

}
