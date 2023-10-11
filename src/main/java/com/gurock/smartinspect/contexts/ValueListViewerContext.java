/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.contexts;

import com.gurock.smartinspect.ViewerId;
import com.gurock.smartinspect.contexts.ListViewerContext;
import com.gurock.smartinspect.packets.logentry.LogEntry;
import com.gurock.smartinspect.session.Session;

/**
 * Represents the value list viewer in the Console which can display data as a key/value list.
 * <p>
 * The value list viewer in the Console interprets the {@link LogEntry#getData data of a Log Entry} as a simple key/value list.
 * Every line in the text data is interpreted as one key/value item of the list. This class takes care of the necessary formatting and
 * escaping required by the corresponding value list viewer of the Console.
 * </p>
 * <p>
 * You can use the ValueListViewerContext class for creating custom log methods around {@link Session#logCustomContext logCustomContext}
 * for sending custom data organized as key/value lists.
 * </p>
 * <p>
 * This class is not guaranteed to be thread safe.
 * </p>
 */
public class ValueListViewerContext extends ListViewerContext {

	/**
	 * Overloaded. Creates and initializes a ValueListViewerContext instance.
	 */
	public ValueListViewerContext() {
		super(ViewerId.ValueList);
	}

	/**
	 * Overloaded. Creates and initializes a ValueListViewerContext
	 * instance using a different viewer ID.
	 * <p>
	 * This constructor is intended for derived classes, such as the
	 * InspectorViewerContext class, which extend the capabilities of
	 * this class and use a different viewer ID.
	 *
	 * @param vi The viewer ID to use
	 */
	protected ValueListViewerContext(ViewerId vi) {
		super(vi);
	}

	/**
	 * Overloaded. Appends a string value and its key.
	 *
	 * @param key   The key to use
	 * @param value The string value to use
	 */
	public void appendKeyValue(String key, String value) {
		if (key != null) {
			appendText(escapeItem(key));
			appendText("=");
			if (value != null) {
				appendText(escapeItem(value));
			}
			appendText("\r\n");
		}
	}

	/**
	 * Overloaded. Appends a char value and its key.
	 *
	 * @param key   The key to use
	 * @param value The char value to use
	 */
	public void appendKeyValue(String key, char value) {
		appendKeyValue(key, String.valueOf(value));
	}

	/**
	 * Overloaded method that appends a boolean value and its associated key.
	 *
	 * @param key   The key to use
	 * @param value The boolean value to use
	 */
	public void appendKeyValue(String key, boolean value) {
		appendKeyValue(key, String.valueOf(value));
	}

	/**
	 * Overloaded function. Appends a byte value and its key.
	 *
	 * @param key   The key to use
	 * @param value The byte value to use
	 */
	public void appendKeyValue(String key, byte value) {
		appendKeyValue(key, String.valueOf(value));
	}

	/**
	 * Overloaded. Appends a short value and its key.
	 *
	 * @param key   The key to use
	 * @param value The short value to use
	 */
	public void appendKeyValue(String key, short value) {
		appendKeyValue(key, String.valueOf(value));
	}

	/**
	 * Overloaded. Appends an int value and its key.
	 *
	 * @param key   The key to use
	 * @param value The int value to use
	 */
	public void appendKeyValue(String key, int value) {
		appendKeyValue(key, String.valueOf(value));
	}

	/**
	 * Overloaded. Appends a long value and its key.
	 *
	 * @param key   The key to use
	 * @param value The long value to use
	 */
	public void appendKeyValue(String key, long value) {
		appendKeyValue(key, String.valueOf(value));
	}

	/**
	 * Overloaded. Appends a float value and its key.
	 *
	 * @param key   The key to use
	 * @param value The float value to use
	 */
	public void appendKeyValue(String key, float value) {
		appendKeyValue(key, String.valueOf(value));
	}

	/**
	 * Overloaded. Appends a double value and its key.
	 *
	 * @param key   The key to use
	 * @param value The double value to use
	 */
	public void appendKeyValue(String key, double value) {
		appendKeyValue(key, String.valueOf(value));
	}

	/**
	 * Overloaded. Appends an object value and its key.
	 *
	 * @param key   The key to use
	 * @param value The object value to use
	 */
	public void appendKeyValue(String key, Object value) {
		appendKeyValue(key, String.valueOf(value));
	}

	/**
	 * Escapes a key or a value.
	 * <p>
	 * This method ensures that the escaped key or value does not
	 * contain any newline characters, such as the carriage return or
	 * linefeed characters. Furthermore, it escapes the '\' and '='
	 * characters.
	 *
	 * @param item The key or value to escape
	 * @return The escaped key or value
	 */
	public String escapeItem(String item) {
		return escapeLine(item, "\\=");
	}
}
