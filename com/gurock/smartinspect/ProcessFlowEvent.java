//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   This class is used by the SmartInspectListener.onProcessFlow event
//   of the SmartInspect class.
// </summary>
// <remarks>
//   It has only one public class member named getProcessFlow. This
//   member is a method, which just returns the sent packet.
// </remarks>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

public final class ProcessFlowEvent extends java.util.EventObject
{
	private ProcessFlow fProcessFlow;

	// <summary>
	//   Creates and initializes a ProcessFlowEvent instance.
	// </summary>
	// <param name="source">The object which fired the event.</param>
	// <param name="processFlow">
	//   The ProcessFlow packet which has just been sent.
	// </param>

	public ProcessFlowEvent(Object source, ProcessFlow processFlow)
	{
		super(source);
		this.fProcessFlow = processFlow;
	}

	// <summary>
	//   Returns the ProcessFlow packet, which has just been sent.
	// </summary>
	// <returns>
	//   The ProcessFlow packet which has just been sent.
	// </returns>

	public ProcessFlow getProcessFlow()
	{
		return this.fProcessFlow;
	}
}
