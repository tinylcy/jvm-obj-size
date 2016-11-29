package main;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * 对象占用字节大小工具类
 * 
 * @author tinylcy
 *
 */
public class ObjSizeFetcher {

	private static Instrumentation instrumentation;

	public static void premain(String agentArgs, Instrumentation inst) {
		instrumentation = inst;
	}

	/**
	 * 直接计算当前对象占用空间大小。
	 * 
	 * 包括： 1. 当前类以及超类的基本数据类型字段大小； 2. 引用数据类型字段引用大小； 3. 基本数据类型数组占用的空间； 4.
	 * 引用数据类型数组引用本身占用的空间。
	 * 
	 * 不包括： 1. 继承自超类以及当前类声明的引用数据类型字段所引用的对象占用的空间； 2. 引用数据类型数组的每个元素所引用的对象占用的空间。
	 *
	 * @param target
	 * @return
	 */
	public static long sizeOf(Object target) {
		if (instrumentation == null) {
			throw new IllegalStateException("Can't access instrumentation environment.\n"
					+ "Please check jar file containing ObjSizeFetcher class is\n"
					+ "specified in the java's \"-javaagent\" command line argument.");
		}
		return instrumentation.getObjectSize(target);
	}

	/**
	 * 递归计算对象占用空间大小。
	 * 
	 * 包括： 1. 当前类以及超类的基本数据类型和引用本身的大小； 2. 当前类以及超类引用数据类型所引用的对象的大小。
	 * 
	 * @param target
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static long fullSizeOf(Object target) throws IllegalArgumentException, IllegalAccessException {
		long size = 0;
		Set<Object> visited = new HashSet<Object>();
		Queue<Object> queue = new ArrayDeque<Object>();
		queue.add(target);
		while (!queue.isEmpty()) {
			Object obj = queue.poll();
			// System.out.println("current object class: " + obj.getClass().getName());
			size += skipVisitedElement(visited, obj) ? 0 : sizeOf(obj);
			visited.add(obj);
			Class<?> objClass = obj.getClass();

			if (objClass.isArray()) {
				if (objClass.getName().length() > 2) {
					for (int i = 0, len = Array.getLength(obj); i < len; i++) {
						Object elem = Array.get(obj, i);
						if (elem != null) {
							queue.add(elem);
						}
					}
				}
			} else {
				while (objClass != null) {
					Field[] fields = objClass.getDeclaredFields();
					for (Field field : fields) {
						if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive()) {
							continue;
						}
						field.setAccessible(true);
						Object fieldValue = field.get(obj);
						if (fieldValue == null) {
							continue;
						}
						queue.add(fieldValue);
					}
					objClass = objClass.getSuperclass();
				}
			}
		}

		return size;
	}

	private static boolean skipVisitedElement(Set<Object> visited, Object obj) {
		if (obj instanceof String && obj == ((String) obj).intern()) {
			return true;
		}
		return visited.contains(obj);
	}

}
