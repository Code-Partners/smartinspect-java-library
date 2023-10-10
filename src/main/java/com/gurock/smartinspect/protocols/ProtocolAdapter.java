/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.protocols;

import com.gurock.smartinspect.ErrorEvent;

/**
 * Represents the adapter class for the ProtocolListener event
 * interface of the Protocol class.
 * <p>
 * This class is added for convenience when dealing with the
 * Protocol event system. This class implements the ProtocolListener
 * interface by defining empty methods stubs. By deriving from this
 * class instead of implementing the ProtocolListener interface
 * directly, you can subscribe to certain events by overriding the
 * appropriate methods and ignore other events which are of no use
 * to you by keeping the empty default implementation of this class.
 * <p>
 * <b>Note:</b> Although the ProtocolListener interface currently
 * has a single method only (for reporting exceptions), it is safer
 * in terms of future API changes / improvements to derive from
 * this class instead of implementing the ProtocolListener interface
 * directly.
 */
public class ProtocolAdapter implements ProtocolListener {
	/**
	 * Provides an empty default implementation for the error event
	 * of the implemented ProtocolListener event interface.
	 * <p>
	 * This method provides an empty default implementation for the
	 * error event. When deriving from this class, override this
	 * method if you are interested in getting notified about occurred
	 * protocol errors. The error event is only used when operating
	 * in asynchronous mode (see Protocol.isValidOption). When
	 * operating in normal blocking mode, the error event is never
	 * fired and exceptions are reporting by throwing them.
	 *
	 * @param e The event argument for the event handler
	 */
	public void onError(ErrorEvent e) {

	}
}
