/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.contexts;

import com.gurock.smartinspect.ViewerId;

/**
 * Represents the list viewer in the Console which can display simple
 * lists of text data.
 *
 * <p>The list viewer in the Console interprets the data of a Log Entry as a list.
 * Every line in the text data is interpreted as one item of the list.
 * This class takes care of the necessary formatting and escaping required by the
 * corresponding list viewer in the Console.
 *
 * <p>You can use the ListViewerContext class for creating custom
 * log methods around logCustomContext for sending custom data organized as simple lists.
 *
 * <p>This class is not guaranteed to be threadsafe.
 */
public class ListViewerContext extends TextContext {
	/**
	 * Overloaded. Creates and initializes a ListViewerContext instance.
	 */
	public ListViewerContext() {
		super(ViewerId.List);
	}

	/**
	 * Overloaded. Creates and initializes a ListViewerContext instance
	 * using a different viewer ID. This constructor is intended for derived classes,
	 * such as the ValueListViewerContext class, which extend the capabilities of
	 * this class and use a different viewer ID.
	 *
	 * @param vi The viewer ID to use.
	 */
	protected ListViewerContext(ViewerId vi) {
		super(vi);
	}

	/**
	 * Overridden. Escapes a line.
	 * This method ensures that the escaped line does not contain any newline characters,
	 * such as the carriage return or linefeed characters.
	 *
	 * @param line The line to escape.
	 * @return The escaped line.
	 */
	protected String escapeLine(String line) {
		return escapeLine(line, null);
	}

	/**
	 * Escapes a line. This method ensures that the escaped line does not contain
	 * characters listed in the toEscape parameter plus any newline
	 * characters, such as the carriage return or linefeed characters.
	 *
	 * @param line     The line to escape.
	 * @param toEscape A set of characters which should be escaped in addition to the
	 *                 newline characters. Can be null or empty.
	 * @return The escaped line.
	 */
	protected static String escapeLine(String line, String toEscape) {
		if (line == null || line.length() == 0) {
			return line;
		} else {
			char b = '\u0000';
			StringBuffer sb = new StringBuffer(line.length());

			for (int i = 0, len = line.length(); i < len; i++) {
				char c = line.charAt(i);
				if (c == '\r' || c == '\n') {
					if (b != '\r' && b != '\n') {
						// Newline characters need to be removed,
						// they would break the list format.
						sb.append(' ');
					}
				} else if (toEscape != null && toEscape.indexOf(c) != -1) {
					// The current character need to be escaped as
					// well (with the \ character).
					sb.append("\\");
					sb.append(c);
				} else {
					// This character is valid, so just append it.
					sb.append(c);
				}
				b = c;
			}

			return sb.toString();
		}
	}
}
