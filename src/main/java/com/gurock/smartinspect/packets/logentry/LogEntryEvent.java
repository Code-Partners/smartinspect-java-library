//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect.packets.logentry;

// <summary>
//   This class is used by the SmartInspectListener.onLogEntry event of
//   the SmartInspect class.
// </summary>
// <remarks>
//   It has only one public class member named getLogEntry. This member
//   is a method, which just returns the sent packet.
// </remarks>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

public final class LogEntryEvent extends java.util.EventObject
{
	private LogEntry fLogEntry;

	// <summary>
	//   Creates and initializes a LogEntryEvent instance.
	// </summary>
	// <param name="source">The object which fired the event.</param>
	// <param name="logEntry">
	//   The LogEntry packet which has just been sent.
	// </param>

	public LogEntryEvent(Object source, LogEntry logEntry)
	{
		super(source);
		this.fLogEntry = logEntry;
	}

	// <summary>
	//   Returns the LogEntry packet, which has just been sent.
	// </summary>
	// <returns>
	//   The LogEntry packet which has just been sent.
	// </returns>

	public LogEntry getLogEntry()
	{
		return this.fLogEntry;		
	}
}
