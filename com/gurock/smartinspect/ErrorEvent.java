//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   This class is used by the SmartInspectListener.onError event of the
//   SmartInspect class and the ProtocolListener.onError event of the
//   Protocol class.
// </summary>
// <remarks>
//   It has only one public class member named getException. This member
//   is a method, which just returns the occurred exception.
// </remarks>
// <threadsafety>
//   This class is full threadsafe.
// </threadsafety>

public final class ErrorEvent extends java.util.EventObject
{
	private Exception fException;

	// <summary>
	//   Creates and initializes an ErrorEvent instance.
	// </summary>
	// <param name="source">The object which fired the event.</param>
	// <param name="e">The occurred exception.</param>

	public ErrorEvent(Object source, Exception e)
	{
		super(source);
		this.fException = e;
	}

	// <summary>
	//   Returns the occurred exception.
	// </summary>
	// <returns>The occurred exception.</returns>

	public Exception getException()
	{
		return this.fException;
	}
}

