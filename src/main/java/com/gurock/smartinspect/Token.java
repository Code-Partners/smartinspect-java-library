/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import com.gurock.smartinspect.packets.logentry.LogEntry;

/**
 * Represents a token in the pattern string of the TextProtocol protocol.
 * <p>
 * This is the abstract base class for all available tokens. Derived classes are not documented for clarity reasons.
 * To create a suitable token object for a given token string, you can use the TokenFactory class.
 * <p>
 * Note: This class is not guaranteed to be threadsafe.
 */
public abstract class Token {
	private String fValue;
	private String fOptions;
	private int fWidth;

	/**
	 * Creates a string representation of a variable or literal token.
	 * With the help of the supplied LogEntry, this token is expanded
	 * into a string. For example, if this token represents the
	 * %session% variable of a pattern string, this method
	 * simply returns the session name of the supplied LogEntry.
	 * <p>
	 * For a literal token, the supplied LogEntry argument is ignored
	 * and the value property is returned.
	 *
	 * @param logEntry The LogEntry to use to create the string representation
	 * @return The text representation of this token for the supplied LogEntry
	 * object
	 */
	public abstract String expand(LogEntry logEntry);

	/**
	 * Returns the raw string value of the parsed pattern string for
	 * this token.
	 * <p>
	 * This method returns the raw string of this token as found in
	 * the parsed pattern string. For a variable, this property is
	 * set to the variable name surrounded with '%' characters and an
	 * optional options string like this: %name{options}%. For a
	 * literal, this property can have any value.
	 *
	 * @return The raw string value of the parsed pattern string for this token
	 */
	public String getValue() {
		return this.fValue;
	}

	/**
	 * Sets the raw string value of the parsed pattern string for this token.
	 * <p>
	 * This method sets the raw string of this token as found in the
	 * parsed pattern string. For a variable, this property is set
	 * to the variable name surrounded with '%' characters and an
	 * optional options string like this: %name{options}%. For a
	 * literal, this property can have any value.
	 *
	 * @param value The new value of this token
	 */
	public void setValue(String value) {
		this.fValue = value;
	}

	/**
	 * Returns the optional options string for this token.
	 * <p>
	 * A variable token can have an optional options string. In the
	 * raw string representation of a token, an options string can be
	 * specified in curly brackets after the variable name like this:
	 * %name{options}%. For a literal, this property is always set to
	 * an empty string.
	 *
	 * @return The optional options string for this token
	 */
	public String getOptions() {
		return this.fOptions;
	}

	/**
	 * Sets the optional options string for this token.
	 * <p>
	 * A variable token can have an optional options string. In the
	 * raw string representation of a token, an options string can be
	 * specified in curly braces after the variable name like this:
	 * %name{options}%. For a literal, this property is always set to
	 * an empty string.
	 *
	 * @param options The new options for this token
	 */
	public void setOptions(String options) {
		this.fOptions = options;
	}

	/**
	 * Indicates if this token supports indenting.
	 * <p>
	 * This method always returns false unless this token represents
	 * the title token of a pattern string. This method is used in the
	 * PatternParser.expand() method to determine if a token allows
	 * indenting.
	 *
	 * @return True if this token supports indenting and false otherwise
	 */
	public boolean getIndent() {
		return false;
	}

	/**
	 * Sets the minimum width of this token.
	 * A variable token can have an optional width modifier. In the
	 * raw string representation of a token, a width modifier can be
	 * specified after the variable name like this: %name,width%.
	 * Width must be a valid positive or negative integer.
	 * <p>
	 * If the width is greater than 0, formatted values will be
	 * right-aligned. If the width is less than 0, they will be
	 * left-aligned.
	 * <p>
	 * For a literal, this property is always set to 0.
	 *
	 * @param width The new minimum width of this token
	 */
	public void setWidth(int width) {
		this.fWidth = width;
	}

	/**
	 * Returns the minimum width of this token.
	 *
	 * <p>A variable token can have an optional width modifier. In the
	 * raw string representation of a token, a width modifier can be
	 * specified after the variable name like this: %name,width%.
	 * Width must be a valid positive or negative integer.</p>
	 *
	 * <p>If the width is greater than 0, formatted values will be
	 * right-aligned. If the width is less than 0, they will be
	 * left-aligned.</p>
	 *
	 * <p>For a literal, this property is always set to 0.</p>
	 *
	 * @return The minimum width of this token
	 */
	public int getWidth() {
		return this.fWidth;
	}
}
