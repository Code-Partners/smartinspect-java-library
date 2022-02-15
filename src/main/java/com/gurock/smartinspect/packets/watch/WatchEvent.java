//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect.packets.watch;

// <summary>
//   This class is used by the SmartInspectListener.onWatch event of
//   the SmartInspect class.
// </summary>
// <remarks>
//   It has only one public class member named getWatch. This member is
//   a method, which just returns the sent packet.
// </remarks>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

public final class WatchEvent extends java.util.EventObject
{
	private Watch fWatch;

	// <summary>
	//   Creates and initializes a WatchEvent instance.
	// </summary>
	// <param name="source">The object which fired the event</param>
	// <param name="watch">The Watch which has just been sent</param>

	public WatchEvent(Object source, Watch watch)
	{
		super(source);
		this.fWatch = watch;
	}

	// <summary>
	//   Returns the Watch packet, which has just been sent.
	// </summary>
	// <returns>
	//   The Watch packet which has just been sent.
	// </returns>

	public Watch getWatch()
	{
		return this.fWatch;
	}
}

