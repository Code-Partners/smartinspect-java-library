//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   This class is used by the SmartInspectListener.onControlCommand
//   event of the SmartInspect class.
// </summary>
// <remarks>
//   It has only one public class member named getControlCommand.
//   This member is a method, which just returns the sent packet.
// </remarks>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

public final class ControlCommandEvent extends java.util.EventObject
{
	private ControlCommand fControlCommand;

	// <summary>
	//   Creates and initializes a ControlCommandEvent instance.
	// </summary>
	// <param name="source">The object which fired the event.</param>
	// <param name="controlCommand">
	//    The ControlCommand which has just been sent.
	// </param>

	public ControlCommandEvent(Object source, ControlCommand controlCommand)
	{
		super(source);
		this.fControlCommand = controlCommand;
	}

	// <summary>
	//   Returns the ControlCommand packet, which has just been sent.
	// </summary>
	// <returns>
	//   The ControlCommand packet which has just been sent.
	// </returns>

	public ControlCommand getControlCommand()
	{
		return this.fControlCommand;
	}
}
