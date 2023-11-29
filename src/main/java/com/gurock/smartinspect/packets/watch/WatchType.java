/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.packets.watch;

import com.gurock.smartinspect.Enum;

/**
 * Represents the type of Watch packet. The type of Watch
 * specifies its variable type.
 * <p>
 * For example, if a Watch packet has a type of WatchType.String,
 * the represented variable is treated as string in the Console.
 * </p>
 * <p>
 * This class is fully threadsafe.
 * </p>
 */
public final class WatchType extends Enum {
	/**
	 * Instructs the Console to treat a Watch value as char.
	 */
	public static final WatchType Char = new WatchType(0, "Char");

	/**
	 * Instructs the Console to treat a Watch value as string.
	 */
	public static final WatchType String = new WatchType(1, "String");

	/**
	 * Instructs the Console to treat a Watch value as integer.
	 */
	public static final WatchType Integer = new WatchType(2, "Integer");

	/**
	 * Instructs the Console to treat a Watch value as float.
	 */
	public static final WatchType Float = new WatchType(3, "Float");

	/**
	 * Instructs the Console to treat a Watch value as boolean.
	 */
	public static final WatchType Boolean = new WatchType(4, "Boolean");

	/**
	 * Instructs the Console to treat a Watch value as address.
	 */
	public static final WatchType Address = new WatchType(5, "Address");

	/**
	 * Instructs the Console to treat a Watch value as timestamp.
	 */
	public static final WatchType Timestamp = new WatchType(6, "Timestamp");

	/**
	 * Instructs the Console to treat a Watch value as object.
	 */
	public static final WatchType Object = new WatchType(7, "Object");

	private WatchType(int value, String name) {
		super(value, name);
	}
}
