//
// <!-- Copyright (C) Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   This interface is used as callback for the ConnectionsParser.parse
//   method.
// </summary>
// <remarks>
//   This interface provides only a single method, called onProtocol,
//   which is called for each found protocol in the parse method of
//   the ConnectionsParser class. Please see the documentation of the
//   onProtocol method for more information.
// </remarks>

public interface ConnectionsParserListener
{
	// <summary>
	//   Represents the callback function for the ConnectionsParser
	//   class.
	// </summary>
	// <param name="e">
	//   A ConnectionsParserEvent argument which offers the possibility
	//   of retrieving information about the found protocol and its options.
	// </param>
	// <remarks>
	//   The ConnectionsParser.parse method calls this callback function
	//   for each found protocol in the supplied connections string. It
	//   is safe to throw exceptions of type SmartInspectException in this
	//   callback.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type           Condition
	//   -                        -
	//   SmartInspectException    An error occurred or has been detected
	//                              in the callback function.
	// </table>
	// </exception>
	
	public void onProtocol(ConnectionsParserEvent e) 
		throws SmartInspectException;
}
