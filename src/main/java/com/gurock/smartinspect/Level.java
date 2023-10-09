//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect;

/**
 * Represents the log level in the SmartInspect Java library.
 * Please see the SmartInspect.setLevel and SmartInspect.setDefaultLevel methods for
 * detailed examples and more information on how to use the Level enum.
 */
public class Level extends Enum {
	/**
	 * Represents the Debug log level. This log level is mostly intended
	 * to be used in the debug and development process.
	 */
	public static final Level Debug = new Level(0, "Debug");

	/**
	 * Represents the Verbose log level. This log level is intended to
	 * track the general progress of applications at a fine-grained
	 * level.
	 */
	public static final Level Verbose = new Level(1, "Verbose");

	/**
	 * Represents the Message log level. This log level is intended to
	 * track the general progress of applications at a coarse-grained
	 * level.
	 */
	public static final Level Message = new Level(2, "Message");

	/**
	 * Represents the Warning log level. This log level designates
	 * potentially harmful events or situations.
	 */
	public static final Level Warning = new Level(3, "Warning");

	/**
	 * Represents the Error log level. This log level designates error
	 * events or situations which are not critical to the entire system.
	 * This log level thus describes recoverable or less important errors.
	 */
	public static final Level Error = new Level(4, "Error");

	/**
	 * Represents the Fatal log level. This log level designates errors
	 * which are not recoverable and eventually stop the system or
	 * application from working.
	 */
	public static final Level Fatal = new Level(5, "Fatal");
	/**
	 * This log level represents a special log level which is only used
	 * by the ControlCommand class and is not intended to be used
	 * directly.
	 */
	public static final Level Control = new Level(6, "Control");

	private Level(int value, String name) {
		super(value, name);
	}
}
