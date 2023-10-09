/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.packets.logentry;

import com.gurock.smartinspect.Enum;

/**
 * Represents the type of LogEntry packet. Instructs the Console to choose the correct icon
 * and to perform additional actions, like, for example, enter a new method or draw a separator.
 * <p>
 * This class is fully threadsafe.
 */
public final class LogEntryType extends Enum {
	/**
	 * Instructs the Console to draw a separator.
	 */
	public static final LogEntryType Separator =
			new LogEntryType(0, "Separator");

	/**
	 * Instructs the Console to enter a new method.
	 */
	public static final LogEntryType EnterMethod =
			new LogEntryType(1, "EnterMethod");

	/**
	 * Instructs the Console to leave a method.
	 */
	public static final LogEntryType LeaveMethod =
			new LogEntryType(2, "LeaveMethod");

	/**
	 * Instructs the Console to reset the current call stack.
	 */
	public static final LogEntryType ResetCallstack =
			new LogEntryType(3, "ResetCallstack");

	/**
	 * Instructs the Console to treat a Log Entry as simple message.
	 */
	public static final LogEntryType Message =
			new LogEntryType(100, "Message");

	/**
	 * Instructs the Console to treat a Log Entry as warning message.
	 */
	public static final LogEntryType Warning =
			new LogEntryType(101, "Warning");

	/**
	 * Instructs the Console to treat a Log Entry as an error message.
	 */
	public static final LogEntryType Error =
			new LogEntryType(102, "Error");

	/**
	 * Instructs the Console to treat a Log Entry as internal error.
	 */
	public static final LogEntryType InternalError =
			new LogEntryType(103, "InternalError");

	/**
	 * Instructs the Console to treat a Log Entry as comment.
	 */
	public static final LogEntryType Comment =
			new LogEntryType(104, "Comment");

	/**
	 * Instructs the Console to treat a Log Entry as a variable value.
	 */
	public static final LogEntryType VariableValue =
			new LogEntryType(105, "VariableValue");

	/**
	 * Instructs the Console to treat a Log Entry as a checkpoint.
	 */
	public static final LogEntryType Checkpoint =
			new LogEntryType(106, "Checkpoint");

	/**
	 * Instructs the Console to treat a Log Entry as debug message.
	 */
	public static final LogEntryType Debug =
			new LogEntryType(107, "Debug");

	/**
	 * Instructs the Console to treat a Log Entry as verbose message.
	 */
	public static final LogEntryType Verbose =
			new LogEntryType(108, "Verbose");

	/**
	 * Instructs the Console to treat a Log Entry as fatal error message.
	 */
	public static final LogEntryType Fatal =
			new LogEntryType(109, "Fatal");

	/**
	 * Instructs the Console to treat a Log Entry as conditional message.
	 */
	public static final LogEntryType Conditional =
			new LogEntryType(110, "Conditional");

	/**
	 * Instructs the Console to treat a Log Entry as assert message.
	 */
	public static final LogEntryType Assert =
			new LogEntryType(111, "Assert");

	/**
	 * Instructs the Console to treat the Log Entry as Log Entry with text.
	 */
	public static final LogEntryType Text =
			new LogEntryType(200, "Text");

	/**
	 * Instructs the Console to treat the Log Entry as Log Entry with binary data.
	 */
	public static final LogEntryType Binary =
			new LogEntryType(201, "Binary");

	/**
	 * Instructs the Console to treat the Log Entry as Log Entry with a picture as data.
	 */
	public static final LogEntryType Graphic =
			new LogEntryType(202, "Graphic");

	/**
	 * Instructs the Console to treat the Log Entry as a Log Entry with source code data.
	 */
	public static final LogEntryType Source =
			new LogEntryType(203, "Source");

	/**
	 * Instructs the Console to treat the Log Entry as Log Entry with object data.
	 */
	public static final LogEntryType Object =
			new LogEntryType(204, "Object");

	/**
	 * Instructs the Console to treat the Log Entry as Log Entry
	 * with web data.
	 */
	public static final LogEntryType WebContent =
			new LogEntryType(205, "WebContent");

	/**
	 * Instructs the Console to treat the Log Entry as Log Entry
	 * with system information.
	 */
	public static final LogEntryType System =
			new LogEntryType(206, "System");

	/**
	 * Instructs the Console to treat the Log Entry as Log Entry with memory statistics.
	 */
	public static final LogEntryType MemoryStatistic =
			new LogEntryType(207, "MemoryStatistic");

	/**
	 * Instructs the Console to treat the Log Entry as Log Entry with a database result.
	 */
	public static final LogEntryType DatabaseResult =
			new LogEntryType(208, "DatabaseResult");

	/**
	 * Instructs the Console to treat the Log Entry as Log Entry with a database structure.
	 */
	public static final LogEntryType DatabaseStructure =
			new LogEntryType(209, "DatabaseStructure");

	private LogEntryType(int value, String name) {
		super(value, name);
	}
}
