/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Responsible for creating a string representation of any arbitrary object.
 * <p>
 * This class provides only one method, renderObject, which is capable of creating a string representation of an object.
 * It renders maps, collections, arrays or any other object.
 * <p>
 * The public static members of this class are thread-safe.
 */
final class ObjectRenderer {
	private ObjectRenderer() {
	}

	/**
	 * Creates a string representation of an object.
	 * This method is capable of creating a string representation
	 * of an object. For most types this method simply calls the
	 * toString method of the supplied object. Some objects, like
	 * maps, collections or arrays, are handled special.
	 *
	 * @param o The object to render. Can be null
	 * @return A string representation of the supplied object
	 */
	public static String renderObject(Object o) {
		if (o != null) {
			if (o instanceof Collection) {
				return renderCollection((Collection) o);
			} else if (o instanceof Map) {
				return renderMap((Map) o);
			} else if (o.getClass().isArray()) {
				return renderArray((Object[]) o);
			} else {
				return o.toString().trim();
			}
		} else {
			return "<null>";
		}
	}

	private static String renderCollection(Collection c) {
		Iterator it = c.iterator();
		StringBuffer sb = new StringBuffer(64);

		sb.append("[");
		while (it.hasNext()) {
			Object o = it.next();

			sb.append(o == c ? "<cycle>" : renderObject(o));

			if (it.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("]");
		return sb.toString();
	}

	private static String renderMap(Map map) {
		Iterator it = map.keySet().iterator();
		StringBuffer sb = new StringBuffer(64);

		sb.append("{");
		while (it.hasNext()) {
			Object key = it.next();
			Object val = map.get(key);

			sb.append(
					(key == map ? "<cycle>" : renderObject(key)) + "=" +
							(val == map ? "<cycle>" : renderObject(val))
			);

			if (it.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");
		return sb.toString();
	}

	private static String renderArray(Object[] array) {
		int len = array.length;
		StringBuffer sb = new StringBuffer(64);

		sb.append("[");
		for (int i = 0; i < len; i++) {
			Object o = array[i];

			sb.append(o == array ? "<cycle>" : renderObject(o));

			if (i != len - 1) {
				sb.append(", ");
			}
		}

		sb.append("]");
		return sb.toString();
	}
}
