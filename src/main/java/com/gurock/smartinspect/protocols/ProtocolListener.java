/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.protocols;

import com.gurock.smartinspect.ErrorEvent;

/**
 * This listener interface is used in the Protocol class for error and
 * exception reporting.
 *
 * <p>The Protocol class provides an event for error reporting (see
 * Protocol.addListener). The error event is only used when operating
 * in asynchronous mode (see Protocol.isValidOption). When operating
 * in normal blocking mode, the error event is never fired and
 * exceptions are reported by throwing them.</p>
 */
public interface ProtocolListener {
	/**
	 * This event is fired after an error occurred when operating in asynchronous mode.
	 * <p>
	 * This event is fired when an error occurs in asynchronous mode (see {@link com.gurock.smartinspect.protocols.Protocol#isValidOption}).
	 * Instead of throwing exceptions when an operation has failed like in normal blocking mode, the asynchronous mode uses
	 * this error event for error reporting.
	 * <p>
	 * Please note: Keep in mind that adding code to the event handlers which can lead to the error event can cause a
	 * presumably undesired recursive behavior.
	 *
	 * @param e The event argument for the event handler
	 * @see com.gurock.smartinspect.ErrorEvent
	 */
	public void onError(ErrorEvent e);
}
