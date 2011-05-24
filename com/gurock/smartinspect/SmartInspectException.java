//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

import java.text.MessageFormat;

// <summary>
//   Used internally to report any kind of error.
// </summary>
// <remarks>
//   This is the base class for several exceptions which are mainly
//   used for internal error reporting. However, it can be useful
//   to have a look at its derived classes, LoadConnectionsException,
//   LoadConfigurationException and ProtocolException, which provide
//   additional information about occurred errors besides the normal
//   exception message.
//   
//   This can be useful if you need to obtain more information about a
//   particular error in the SmartInspectListener.onError event of the
//   SmartInspect class.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>

public class SmartInspectException extends java.lang.Exception
{
	// <summary>
	//   Creates and initializes a SmartInspectException instance.
	// </summary>
	// <param name="e">
	//   The error message which describes the exception.
	// </param>

	public SmartInspectException(String e)
	{
		super(e);
	}

	// <summary>
	//   Creates and initializes a SmartInspectException instance.
	// </summary>
	// <param name="fmt">
	//   The format string to create a description of the exception.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>

	public SmartInspectException(String fmt, Object[] args)
	{
		super(MessageFormat.format(fmt, args));
	}
}
