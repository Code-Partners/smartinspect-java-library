/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.contexts;

import com.gurock.smartinspect.ViewerId;

/**
 * Represents the inspector viewer in the Console which displays
 * key/value pairs in an object inspector control.
 *
 * <p>The inspector viewer in the Console interprets the
 * data of a Log Entry as a key/value list with
 * group support like object inspectors from popular IDEs. This class
 * takes care of the necessary formatting and escaping required by the
 * corresponding inspector viewer in the Console.
 *
 * <p>You can use the InspectorViewerContext class for creating custom
 * log methods around logCustomContext
 * for sending custom data organized as grouped key/value pairs.
 *
 * <p><b>Thread Safety:</b>
 * This class is not guaranteed to be thread safe.
 */
public class InspectorViewerContext extends ValueListViewerContext {
	/**
	 * Creates and initializes an InspectorViewerContext instance.
	 */
	public InspectorViewerContext() {
		super(ViewerId.Inspector);
	}

	/**
	 * Starts a new group.
	 *
	 * @param group The name of the group to use.
	 */
	public void startGroup(String group) {
		if (group != null) {
			appendText("[");
			appendText(escapeItem(group));
			appendText("]\r\n");
		}
	}

	/**
	 * Overridden. Escapes a key or a value.
	 * This method ensures that the escaped key or value does not contain
	 * any newline characters, such as the carriage return or linefeed characters.
	 * Furthermore, it escapes the '\', '=', '[' and ']' characters.
	 *
	 * @param item The key or value to escape.
	 * @return The escaped key or value.
	 */
	public String escapeItem(String item) {
		return escapeLine(item, "\\=[]");
	}
}
