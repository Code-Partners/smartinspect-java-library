//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Represents the adapter class for the ProtocolListener event
//   interface of the Protocol class.
// </summary>
// <remarks>
//   This class is added for convenience when dealing with the
//   Protocol event system. This class implements the ProtocolListener
//   interface by defining empty methods stubs. By deriving from this
//   class instead of implementing the ProtocolListener interface
//   directly, you can subscribe to certain events by overriding the
//   appropriate methods and ignore other events which are of no use
//   to you by keeping the empty default implementation of this class.
//
//   <b>Note:</b> Although the ProtocolListener interface currently
//   has a single method only (for reporting exceptions), it is safer
//   in terms of future API changes / improvements to derive from
//   this class instead of implementing the ProtocolListener interface
//   directly.
// </remarks>

public class ProtocolAdapter implements ProtocolListener
{
	// <summary>
	//   Provides an empty default implementation for the error event
	//   of the implemented ProtocolListener event interface.
	// </summary>
	// <param name="e">The event argument for the event handler.</param>
	// <remarks>
	//   This method provides an empty default implementation for the
	//   error event. When deriving from this class, override this
	//   method if you are interested in getting notified about occurred
	//   protocol errors. The error event is only used when operating
	//   in asynchronous mode (see Protocol.isValidOption). When
	//   operating in normal blocking mode, the error event is never
	//   fired and exceptions are reporting by throwing them.
	// </remarks>
	
	public void onError(ErrorEvent e)
	{
		
	}
}
