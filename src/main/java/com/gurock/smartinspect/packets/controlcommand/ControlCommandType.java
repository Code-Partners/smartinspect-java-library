/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.packets.controlcommand;

import com.gurock.smartinspect.Enum;

/**
 * Represents the type of a ControlCommand packet. The type of a Control
 * Command influences the way the Console interprets the packet.
 * <p>
 * For example, if a Control Command packet has a type of
 * ControlCommandType.ClearAll, the entire Console is reset when
 * this packet arrives. Also have a look at the corresponding
 * Session.clearAll method.
 * <p>
 * This class is fully threadsafe.
 */
public final class ControlCommandType extends Enum {
	/**
	 * Instructs the Console to clear all Log Entries.
	 */
	public static final ControlCommandType
			ClearLog = new ControlCommandType(0, "ClearLog");

	/**
	 * Instructs the Console to clear all Watches.
	 */
	public static final ControlCommandType
			ClearWatches = new ControlCommandType(1, "ClearWatches");

	/**
	 * Instructs the Console to clear all AutoViews.
	 */
	public static final ControlCommandType
			ClearAutoViews = new ControlCommandType(2, "ClearAutoViews");

	/**
	 * Instructs the Console to reset the whole Console.
	 */
	public static final ControlCommandType
			ClearAll = new ControlCommandType(3, "ClearAll");

	/**
	 * Instructs the Console to clear all Process Flow entries.
	 */
	public static final ControlCommandType
			ClearProcessFlow = new ControlCommandType(4, "ClearProcessFlow");

	private ControlCommandType(int value, String name) {
		super(value, name);
	}
}

