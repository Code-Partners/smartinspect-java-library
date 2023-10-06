/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * This interface is used as callback for the ConnectionsParser.parse method.
 *
 * <p>This interface provides only a single method, called onProtocol,
 * which is called for each found protocol in the parse method of
 * the ConnectionsParser class. Please see the documentation of the
 * onProtocol method for more information. </p>
 */
public interface ConnectionsParserListener {
	/**
	 * Represents the callback function for the ConnectionsParser class.
	 * The ConnectionsParser.parse method calls this callback function for each found protocol in the
	 * supplied connections string. It is safe to throw exceptions of type SmartInspectException in this
	 * callback.
	 *
	 * @param e A ConnectionsParserEvent argument which offers the possibility of retrieving information about the
	 *          found protocol and its options.
	 * @throws SmartInspectException If an error occurred or has been detected in the callback function.
	 */

	public void onProtocol(ConnectionsParserEvent e)
			throws SmartInspectException;
}
