//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect.connections.options;

// <summary>
//   This interface is used as callback for the OptionsParser.parse
//   method.
// </summary>
// <remarks>
//   This interface provides only a single method, called onOption,
//   which is called for each found option in the parse method of
//   the OptionsParser class. Please see the documentation of the
//   onOptions method of more information.
//</remarks>

import com.gurock.smartinspect.SmartInspectException;

public interface OptionsParserListener
{
	// <summary>
	//   Represents the callback function for the OptionsParser class.
	// </summary>
	// <param name="e">
	//   A OptionsParserEvent argument which offers the possibility of
	//   retrieving information about the found protocol and its options.
	// </param>
	// <remarks>
	//   The OptionsParser.parse method calls this callback function
	//   for each found option in the supplied options part of a
	//   connections string. It is safe to throw exceptions of type
	//   SmartInspectException in this callback.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type           Condition
	//   -                        -
	//   SmartInspectException    An error occurred or has been detected
	//                              in the callback function.
	// </table>
	// </exception>	
	
	public void onOption(OptionsParserEvent e) throws SmartInspectException;
}
