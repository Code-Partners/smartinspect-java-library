//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   This listener interface is used in the Protocol class for error and
//   exception reporting.
// </summary>
// <remarks>
//   The Protocol class provides an event for error reporting (see
//   Protocol.addListener). The error event is only used when operating
//   in asynchronous mode (see Protocol.isValidOption). When operating
//   in normal blocking mode, the error event is never fired and
//   exceptions are reporting by throwing them.
// </remarks>

public interface ProtocolListener
{
	// <summary>
	//   This event is fired after an error occurred when operating
	//   in asynchronous mode.
	// </summary>
	// <param name="e">The event argument for the event handler.</param>
	// <seealso cref="com.gurock.smartinspect.ErrorEvent"/>
	// <remarks>
	//   This event is fired when an error occurs in asynchronous mode
	//   (see Protocol.isValidOption). Instead of throwing exceptions
	//   when an operation has failed like in normal blocking mode, the
	//   asynchronous mode uses this error event for error reporting.
	//
	//   <b>Please note</b>: Keep in mind that adding code to the
	//   event handlers which can lead to the error event can cause a
	//   presumably undesired recursive behavior.
	// </remarks>
	
	public void onError(ErrorEvent e);
}
