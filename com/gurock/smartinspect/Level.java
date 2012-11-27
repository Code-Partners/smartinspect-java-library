//
// <!-- Copyright (C) Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Represents the log level in the SmartInspect Java library.
// </summary>
// <remarks>
//   Please see the SmartInspect.setLevel and SmartInspect.setDefaultLevel
//   methods for detailed examples and more information on how to use the
//   Level enum.
// </remarks>

public class Level extends Enum
{
	// <summary>
	//   Represents the Debug log level. This log level is mostly intended
	//   to be used in the debug and development process.
	// </summary>

	public static final Level Debug = new Level(0, "Debug");
	
	// <summary>
	//   Represents the Verbose log level. This log level is intended to
	//   track the general progress of applications at a fine-grained
	//   level.
	// </summary>

	public static final Level Verbose = new Level(1, "Verbose");
	
	// <summary>
	//   Represents the Message log level. This log level is intended to
	//   track the general progress of applications at a coarse-grained
	//   level.
	// </summary>

	public static final Level Message = new Level(2, "Message");

	// <summary>
	//   Represents the Warning log level. This log level designates
	//   potentially harmful events or situations.
	// </summary>
	
	public static final Level Warning = new Level(3, "Warning");

	// <summary>
	//   Represents the Error log level. This log level designates error
	//   events or situations which are not critical to the entire system.
	//   This log level thus describes recoverable or less important
	//   errors.
	// </summary>

	public static final Level Error = new Level(4, "Error");
	
	// <summary>
	//   Represents the Fatal log level. This log level designates errors
	//   which are not recoverable and eventually stop the system or
	//   application from working.
	// </summary>	
	
	public static final Level Fatal = new Level(5, "Fatal");

	// <summary>
	//   This log level represents a special log level which is only used
	//   by the ControlCommand class and is not intended to be used
	//   directly.
	// </summary>
	
	public static final Level Control = new Level(6, "Control");
	
	private Level(int value, String name)
	{
		super(value, name);
	}
}
