//
// <!-- Copyright (C) Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Represents the type of a ControlCommand packet. The type of a
//   Control Commmand influences the way the Console interprets the packet.
// </summary>
// <remarks>
//   For example, if a Control Command packet has a type of
//   ControlCommandType.ClearAll, the entire Console is reset when
//   this packet arrives. Also have a look at the corresponding
//   Session.clearAll method.
// </remarks>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

public final class ControlCommandType extends Enum
{
	// <summary>
	//   Instructs the Console to clear all Log Entries.
	// </summary>

	public static final ControlCommandType
		ClearLog = new ControlCommandType(0, "ClearLog");

	// <summary>
	//   Instructs the Console to clear all Watches.
	// </summary>

	public static final ControlCommandType
		ClearWatches = new ControlCommandType(1, "ClearWatches");

	// <summary>
	//   Instructs the Console to clear all AutoViews.
	// </summary>

	public static final ControlCommandType
		ClearAutoViews = new ControlCommandType(2, "ClearAutoViews");

	// <summary>
	//   Instructs the Console to reset the whole Console.
	// </summary>

	public static final ControlCommandType 
		ClearAll = new ControlCommandType(3, "ClearAll");

	// <summary>
	//   Instructs the Console to clear all Process Flow entries.
	// </summary>

	public static final ControlCommandType
		ClearProcessFlow = new ControlCommandType(4, "ClearProcessFlow");

	private ControlCommandType(int value, String name)
	{
		super(value, name);
	}
}

