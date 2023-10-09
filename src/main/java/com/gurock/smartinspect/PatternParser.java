/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import com.gurock.smartinspect.packets.logentry.LogEntry;
import com.gurock.smartinspect.packets.logentry.LogEntryType;

import java.util.ArrayList;

/**
 * Capable of parsing and expanding a pattern string as used in the
 * TextProtocol and TextFormatter classes.
 * The PatternParser class is capable of creating a text representation
 * of a LogEntry object (see expand). The string representation can be
 * influenced by setting a pattern string. Please see the setPattern
 * method for a description.
 * <p>
 * This class is not guaranteed to be threadsafe.
 */
public class PatternParser {
	private static final String SPACES = "   ";

	private int fPosition;
	private String fPattern;
	private ArrayList fTokens;
	private boolean fIndent;
	private StringBuffer fBuffer;
	private int fIndentLevel;

	/**
	 * Creates and initializes a PatternParser instance.
	 */
	public PatternParser() {
		this.fTokens = new ArrayList();
		this.fBuffer = new StringBuffer();
		this.fPattern = "";
	}

	/**
	 * Creates a text representation of a LogEntry by applying a
	 * user-specified Pattern string.
	 *
	 * @param logEntry The LogEntry whose text representation should be computed by
	 *                 applying the current pattern string. All
	 *                 recognized variables in the pattern string are replaced with
	 *                 the actual values of this LogEntry
	 * @return The text representation for the supplied LogEntry object
	 */
	public String expand(LogEntry logEntry) {
		int size = this.fTokens.size();

		if (size == 0) {
			return "";
		}

		this.fBuffer.setLength(0);
		if (logEntry.getLogEntryType() == LogEntryType.LeaveMethod) {
			if (this.fIndentLevel > 0) {
				this.fIndentLevel--;
			}
		}

		for (int i = 0; i < size; i++) {
			Token token = (Token) this.fTokens.get(i);
			if (this.fIndent && token.getIndent()) {
				for (int j = 0; j < this.fIndentLevel; j++) {
					this.fBuffer.append(SPACES);
				}
			}

			String expanded = token.expand(logEntry);
			int width = token.getWidth();

			if (width < 0) {
				// Left-aligned
				this.fBuffer.append(expanded);

				int pad = -width - expanded.length();
				for (int j = 0; j < pad; j++) {
					this.fBuffer.append(' ');
				}
			} else if (width > 0) {
				int pad = width - expanded.length();
				for (int j = 0; j < pad; j++) {
					this.fBuffer.append(' ');
				}

				// Right-aligned
				this.fBuffer.append(expanded);
			} else {
				this.fBuffer.append(expanded);
			}
		}

		if (logEntry.getLogEntryType() == LogEntryType.EnterMethod) {
			this.fIndentLevel++;
		}

		return this.fBuffer.toString();
	}

	private Token next() {
		int length = this.fPattern.length();

		if (this.fPosition < length) {
			boolean isVariable = false;
			int pos = this.fPosition;

			if (this.fPattern.charAt(pos) == '%') {
				isVariable = true;
				pos++;
			}

			while (pos < length) {
				if (this.fPattern.charAt(pos) == '%') {
					if (isVariable) {
						pos++;
					}
					break;
				}
				pos++;
			}

			String value = this.fPattern.substring(this.fPosition, pos);
			this.fPosition = pos;

			return TokenFactory.getToken(value);
		} else {
			return null;
		}
	}

	private void parse() {
		this.fTokens.clear();
		Token token = next();
		while (token != null) {
			this.fTokens.add(token);
			token = next();
		}
	}

	/**
	 * Returns the pattern string for this PatternParser object.
	 * <p>
	 * The pattern string influences the way a text representation of
	 * a LogEntry object is created. A pattern string consists of a
	 * list of so called variable and literal tokens. When a string
	 * representation of a LogEntry object is created, the variables
	 * are replaced with the actual values of the LogEntry object.
	 * For a list of valid tokens, please refer to the documentation
	 * of the setPattern method.
	 *
	 * @return The pattern string for this PatternParser object
	 */
	public String getPattern() {
		return this.fPattern;
	}

	/**
	 * Sets the pattern string for this PatternParser object.
	 * <p>
	 * The pattern string influences the way a text representation of a LogEntry object is created. A pattern string consists of a list of so called variable and literal tokens. When a string representation of a LogEntry object is created, the variables are replaced with the actual values of the LogEntry object.
	 * <p>
	 * Variables have a unique name, are surrounded with '%' characters and can have an optional options string enclosed in curly braces like this: %name{options}%.
	 * <p>
	 * You can also specify the minimum width of a value like this: %name,width%. Width must be a valid positive or negative integer. If the width is greater than 0, formatted values will be right-aligned. If the width is less than 0, they will be left-aligned.
	 * <p>
	 * The following table lists the available variables together with the corresponding LogEntry method/property.
	 * <p>
	 * <table border="1">
	 *     <thead>
	 *         <tr>
	 *             <th>Variable</th>
	 *             <th>Corresponding Method</th>
	 *         </tr>
	 *     </thead>
	 *     <tbody>
	 *         <tr>
	 *             <td>%appname%</td>
	 *             <td>LogEntry.getAppName</td>
	 *         </tr>
	 *         <tr>
	 *             <td>%color%</td>
	 *             <td>LogEntry.getColor</td>
	 *         </tr>
	 *         <tr>
	 *             <td>%hostname%</td>
	 *             <td>LogEntry.getHostName</td>
	 *         </tr>
	 *         <tr>
	 *             <td>%level%</td>
	 *             <td>LogEntry.getLevel</td>
	 *         </tr>
	 *         <tr>
	 *             <td>%logentrytype%</td>
	 *             <td>LogEntry.getLogEntryType</td>
	 *         </tr>
	 *         <tr>
	 *             <td>%process%</td>
	 *             <td>LogEntry.getProcessId</td>
	 *         </tr>
	 *         <tr>
	 *             <td>%session%</td>
	 *             <td>LogEntry.getSessionName</td>
	 *         </tr>
	 *         <tr>
	 *             <td>%thread%</td>
	 *             <td>LogEntry.getThreadId</td>
	 *         </tr>
	 *         <tr>
	 *             <td>%timestamp%</td>
	 *             <td>LogEntry.getTimestamp</td>
	 *         </tr>
	 *         <tr>
	 *             <td>%title%</td>
	 *             <td>LogEntry.getTitle</td>
	 *         </tr>
	 *         <tr>
	 *             <td>%viewerid%</td>
	 *             <td>LogEntry.getViewerId</td>
	 *         </tr>
	 *     </tbody>
	 * </table>
	 * <p>
	 * For the timestamp token, you can use the options string to pass a custom date/time format string. This can look as follows: %timestamp{HH:mm:ss.SSS}%.
	 * <p>
	 * The format string must be a valid Java DateFormat format string. The default format string used by the timestamp token is "yyyy-MM-dd HH:mm:ss.SSS".
	 * <p>
	 * Literals are preserved as specified in the pattern string. When a specified variable is unknown, it is handled as literal.
	 * <p>
	 * Example:
	 * <pre>
	 * [%timestamp%] %level,8%: %title%
	 * [%timestamp%] %session%: %title% (Level: %level%)
	 * </pre>
	 *
	 * @param pattern The new pattern string
	 */
	public void setPattern(String pattern) {
		this.fPosition = 0;
		if (pattern != null) {
			this.fPattern = pattern.trim();
		} else {
			this.fPattern = "";
		}
		parse();
	}

	/**
	 * Returns if the expand method automatically intends log packets like in the Views of the SmartInspect Console.
	 * Log Entry packets of type EnterMethod increase the indentation and packets of type LeaveMethod decrease it.
	 *
	 * @return True if the expand method automatically intends log packets and false otherwise
	 */
	public boolean getIndent() {
		return this.fIndent;
	}

	/**
	 * Sets if the expand method automatically indents log packets like
	 * in the Views of the SmartInspect Console.
	 * <p>
	 * Log Entry packets of type EnterMethod increase the indentation
	 * and packets of type LeaveMethod decrease it.
	 *
	 * @param indent Should be true if the expand method should automatically indent
	 *               log packets and false otherwise
	 */
	public void setIndent(boolean indent) {
		this.fIndent = indent;
	}
}
