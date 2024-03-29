/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Represents the timestamp resolution mode for the Clock class.
 * <p>
 * SmartInspect currently supports two different kinds of timestamp
 * resolutions. The {@code standard} resolution is the default timestamp
 * behavior of the SmartInspect Java library and usually provides
 * a maximum resolution of 10-55 milliseconds (depending on the
 * Windows version). This is the recommended option for production
 * systems.
 * <p>
 * {@code High-resolution} timestamps, on the other hand, can provide
 * a microseconds resolution but are only intended to be used on
 * development machines.
 *
 * @see SmartInspect#setResolution
 */
public class ClockResolution extends Enum {
	/**
	 * Represents the standard timestamp resolution. This is the
	 * default timestamp behavior of the SmartInspect Java library
	 * and the recommended option for production systems.
	 */
	public static final ClockResolution Standard =
			new ClockResolution(0, "Standard");

	/**
	 * Represents timestamps with a very high resolution (microseconds).
	 * This option is not intended to be used on production systems. See
	 * {@link SmartInspect#setResolution} for details.
	 */
	public static final ClockResolution High =
			new ClockResolution(1, "High");

	private ClockResolution(int value, String name) {
		super(value, name);
	}
}
