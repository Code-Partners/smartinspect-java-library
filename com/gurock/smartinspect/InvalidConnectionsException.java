//
// <!-- Copyright (C) Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Used to report errors concerning the connections string in the
//   SmartInspect class.
// </summary>
// <remarks>
//   An invalid syntax, unknown protocols or inexistent options in the
//   <link SmartInspect.setConnections, connections string> will result
//   in an InvalidConnectionsException exception. This exception type
//   is used by the setConnections method of the SmartInspect class.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>

public final class InvalidConnectionsException extends java.lang.Exception
{
	// <summary>
	//   Creates and initializes an InvalidConnectionsException instance.
	// </summary>
	// <param name="e">
	//   The error message which describes the exception.
	// </param>
		
	public InvalidConnectionsException(String e)
	{
		super(e);
	}
}

