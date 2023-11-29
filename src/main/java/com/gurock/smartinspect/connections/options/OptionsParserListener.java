/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.connections.options;

import com.gurock.smartinspect.SmartInspectException;

/**
 * This interface is used as callback for the {@link OptionsParser#parse}
 * method.
 * <p>
 * This interface provides only a single method, called onOption,
 * which is called for each found option in the parse method of
 * the OptionsParser class. Please see the documentation of the
 * onOptions method of more information.
 */
public interface OptionsParserListener {
	/**
	 * Represents the callback function for the OptionsParser class.
	 * The {@link OptionsParser#parse} method calls this callback function for each found option
	 * in the supplied options part of a connections string. It is safe to throw
	 * exceptions of type SmartInspectException in this callback.
	 *
	 * @param e A OptionsParserEvent argument which offers the possibility of
	 *          retrieving information about the found protocol and its options
	 * @throws SmartInspectException If an error occurred or has been detected in the callback function
	 */
	public void onOption(OptionsParserEvent e) throws SmartInspectException;
}
