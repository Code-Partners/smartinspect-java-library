//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Represents the type of a ProcessFlow packet. The type of a Process
//   Flow entry specifies the way the Console interprets this packet.
// </summary>
// <remarks>
//  For example, if a Process Flow entry has a type of
//  ProcessFlowType.EnterThread, the Console interprets this packet as
//  information about a new thread of your application.
// </remarks>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

public final class ProcessFlowType extends Enum
{
	// <summary>
	//   Instructs the Console to enter a new method.
	// </summary>

	public static final ProcessFlowType EnterMethod = 
		new ProcessFlowType(0, "EnterMethod");

	// <summary>
	//   Instructs the Console to leave a method.
	// </summary>

	public static final ProcessFlowType LeaveMethod = 
		new ProcessFlowType(1, "LeaveMethod");

	// <summary>
	//   Instructs the Console to enter a new thread.
	// </summary>

	public static final ProcessFlowType EnterThread = 
		new ProcessFlowType(2, "EnterThread");

	// <summary>
	//   Instructs the Console to leave a thread.
	// </summary>

	public static final ProcessFlowType LeaveThread = 
		new ProcessFlowType(3, "LeaveThread");

	// <summary>
	//   Instructs the Console to enter a new process.
	// </summary>

	public static final ProcessFlowType EnterProcess = 
		new ProcessFlowType(4, "EnterProcess");

	// <summary>
	//   Instructs the Console to leave a process.
	// </summary>

	public static final ProcessFlowType LeaveProcess = 
		new ProcessFlowType(5, "LeaveProcess");

	private ProcessFlowType(int value, String name)
	{
		super(value, name);
	}
}
