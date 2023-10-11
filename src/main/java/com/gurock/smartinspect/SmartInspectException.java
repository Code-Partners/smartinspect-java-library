/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import java.text.MessageFormat;

/**
 * Used internally to report any kind of error.
 * This is the base class for several exceptions which are mainly
 * used for internal error reporting. However, it can be useful
 * to have a look at its derived classes, LoadConnectionsException,
 * LoadConfigurationException and ProtocolException, which provide
 * additional information about occurred errors besides the normal
 * exception message.
 * <p>
 * This can be useful if you need to obtain more information about a
 * particular error in the SmartInspectListener.onError event of the
 * SmartInspect class.
 *
 * <p>This class is not guaranteed to be threadsafe.
 */
public class SmartInspectException extends java.lang.Exception {
	/**
	 * Creates and initializes a SmartInspectException instance.
	 *
	 * @param e The error message which describes the exception
	 */
	public SmartInspectException(String e) {
		super(e);
	}

	/**
	 * Creates and initializes a SmartInspectException instance.
	 *
	 * @param fmt  The format string to create a description of the exception
	 * @param args The array of arguments for the format string
	 */
	public SmartInspectException(String fmt, Object[] args) {
		super(MessageFormat.format(fmt, args));
	}
}
