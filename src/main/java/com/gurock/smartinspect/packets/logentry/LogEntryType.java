//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect.packets.logentry;

// <summary>
//   Represents the type of a LogEntry packet. Instructs the Console to
//   choose the correct icon and to perform additional actions, like, for
//   example, enter a new method or draw a separator.
// </summary>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

import com.gurock.smartinspect.Enum;

public final class LogEntryType extends Enum
{
	// <summary>
	//   Instructs the Console to draw a separator.
	// </summary>

	public static final LogEntryType Separator = 
		new LogEntryType(0, "Separator");

	// <summary>
	//   Instructs the Console to enter a new method.
	// </summary>

	public static final LogEntryType EnterMethod = 
		new LogEntryType(1, "EnterMethod");

	// <summary>
	//   Instructs the Console to leave a method.
	// </summary>

	public static final LogEntryType LeaveMethod = 
		new LogEntryType(2, "LeaveMethod");

	// <summary>
	//   Instructs the Console to reset the current call stack.
	// </summary>

	public static final LogEntryType ResetCallstack = 
		new LogEntryType(3, "ResetCallstack");

	// <summary>
	//   Instructs the Console to treat a Log Entry as simple message.
	// </summary>

	public static final LogEntryType Message = 
		new LogEntryType(100, "Message");

	// <summary>
	//   Instructs the Console to treat a Log Entry as warning message.
	// </summary>

	public static final LogEntryType Warning = 
		new LogEntryType(101, "Warning");

	// <summary>
	//   Instructs the Console to treat a Log Entry as error message.
	// </summary>

	public static final LogEntryType Error = 
		new LogEntryType(102, "Error");

	// <summary>
	//   Instructs the Console to treat a Log Entry as internal error.
	// </summary>

	public static final LogEntryType InternalError = 
		new LogEntryType(103, "InternalError");

	// <summary>
	//   Instructs the Console to treat a Log Entry as comment.
	// </summary>

	public static final LogEntryType Comment = 
		new LogEntryType(104, "Comment");

	// <summary>
	//   Instructs the Console to treat a Log Entry as a variable value.
	// </summary>

	public static final LogEntryType VariableValue = 
		new LogEntryType(105, "VariableValue");

	// <summary>
	//   Instructs the Console to treat a Log Entry as checkpoint.
	// </summary>

	public static final LogEntryType Checkpoint = 
		new LogEntryType(106, "Checkpoint");

	// <summary>
	//   Instructs the Console to treat a Log Entry as debug message.
	// </summary>

	public static final LogEntryType Debug = 
		new LogEntryType(107, "Debug");

	// <summary>
	//   Instructs the Console to treat a Log Entry as verbose message.
	// </summary>

	public static final LogEntryType Verbose = 
		new LogEntryType(108, "Verbose");
	
	// <summary>
	//   Instructs the Console to treat a Log Entry as fatal error
	//   message.
	// </summary>

	public static final LogEntryType Fatal = 
		new LogEntryType(109, "Fatal");

	// <summary>
	//   Instructs the Console to treat a Log Entry as conditional
	//   message.
	// </summary>

	public static final LogEntryType Conditional = 
		new LogEntryType(110, "Conditional");

	// <summary>
	//   Instructs the Console to treat a Log Entry as assert message.
	// </summary>

	public static final LogEntryType Assert = 
		new LogEntryType(111, "Assert");

	// <summary>
	//   Instructs the Console to treat the Log Entry as Log Entry
	//   with text.
	// </summary>

	public static final LogEntryType Text = 
		new LogEntryType(200, "Text");

	// <summary>
	//   Instructs the Console to treat the Log Entry as Log Entry
	//   with binary data.
	// </summary>

	public static final LogEntryType Binary = 
		new LogEntryType(201, "Binary");

	// <summary>
	//   Instructs the Console to treat the Log Entry as Log Entry
	//   with a picture as data.
	// </summary>

	public static final LogEntryType Graphic = 
		new LogEntryType(202, "Graphic");

	// <summary>
	//   Instructs the Console to treat the Log Entry as Log Entry
	//   with source code data.
	// </summary>

	public static final LogEntryType Source = 
		new LogEntryType(203, "Source");

	// <summary>
	//   Instructs the Console to treat the Log Entry as Log Entry
	//   with object data.
	// </summary>

	public static final LogEntryType Object = 
		new LogEntryType(204, "Object");

	// <summary>
	//   Instructs the Console to treat the Log Entry as Log Entry
	//   with web data.
	// </summary>

	public static final LogEntryType WebContent = 
		new LogEntryType(205, "WebContent");

	// <summary>
	//   Instructs the Console to treat the Log Entry as Log Entry
	//   with system information.
	// </summary>

	public static final LogEntryType System = 
		new LogEntryType(206, "System");

	// <summary>
	//   Instructs the Console to treat the Log Entry as Log Entry
	//   with memory statistics.
	// </summary>

	public static final LogEntryType MemoryStatistic = 
		new LogEntryType(207, "MemoryStatistic");

	// <summary>
	//   Instructs the Console to treat the Log Entry as Log Entry
	//   with a database result.
	// </summary>

	public static final LogEntryType DatabaseResult = 
		new LogEntryType(208, "DatabaseResult");
	
	// <summary>
	//   Instructs the Console to treat the Log Entry as Log Entry
	//   with a database structure.
	// </summary>

	public static final LogEntryType DatabaseStructure = 
		new LogEntryType(209, "DatabaseStructure");
	
	private LogEntryType(int value, String name)
	{
		super(value, name);
	}
}
