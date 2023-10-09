/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Used to report errors concerning the connections string in the
 * SmartInspect class.
 * <p>
 * Note: An invalid syntax, unknown protocols or inexistent options in the
 * connections string will result in an InvalidConnectionsException exception.
 * This exception type is used by the setConnections method of the SmartInspect class.
 * <p>
 * This class is not guaranteed to be threadsafe.
 */
public final class InvalidConnectionsException extends java.lang.Exception {
	/**
	 * Creates and initializes an InvalidConnectionsException instance.
	 *
	 * @param e The error message which describes the exception.
	 */
	public InvalidConnectionsException(String e) {
		super(e);
	}
}

