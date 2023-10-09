//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect;
/**
 * Represents the type of a ProcessFlow packet. The type of a Process
 * Flow entry specifies the way the Console interprets this packet. For example, 
 * if a Process Flow entry has a type of
 * ProcessFlowType.EnterThread, the Console interprets this packet as
 * information about a new thread of your application.
 * <p>
 * This class is fully threadsafe.
 */

public final class ProcessFlowType extends Enum
{
/**
 * Instructs the Console to enter a new method.
 */

	public static final ProcessFlowType EnterMethod = 
		new ProcessFlowType(0, "EnterMethod");
    /**
     * Instructs the Console to leave a method.
     */

	public static final ProcessFlowType LeaveMethod = 
		new ProcessFlowType(1, "LeaveMethod");
/**
 * Instructs the Console to enter a new thread.
 */

	public static final ProcessFlowType EnterThread = 
		new ProcessFlowType(2, "EnterThread");
    /**
    * Instructs the Console to leave a thread.
    */

	public static final ProcessFlowType LeaveThread = 
		new ProcessFlowType(3, "LeaveThread");
/**
 * Instructs the Console to enter a new process.
 */

	public static final ProcessFlowType EnterProcess = 
		new ProcessFlowType(4, "EnterProcess");
    /**
     * Instructs the Console to leave a process.
     */

	public static final ProcessFlowType LeaveProcess = 
		new ProcessFlowType(5, "LeaveProcess");

	private ProcessFlowType(int value, String name)
	{
		super(value, name);
	}
}
