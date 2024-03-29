/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.jdk;

import java.util.logging.LogRecord;

/**
 * Used by the SmartInspectHandler class to create
 * a string representation of a LogRecord instance.
 */
public final class SmartInspectFormatter extends java.util.logging.Formatter {
	private static final String[] fSpecialTokens = {"ENTRY", "RETURN"};

	/**
	 * Overridden. Formats a LogRecord.
	 *
	 * @param record The LogRecord to format
	 * @return A string representation of the supplied LogRecord
	 */
	public String format(LogRecord record) {
		StringBuffer sb = new StringBuffer();

		if (record.getSourceClassName() != null) {
			sb.append(record.getSourceClassName());
			if (record.getSourceMethodName() != null) {
				sb.append(".");
				sb.append(record.getSourceMethodName());
			}
		} else if (record.getLoggerName() != null) {
			sb.append(record.getLoggerName());
		}

		String message = null;

		if (record.getThrown() != null) {
			message = record.getThrown().getMessage();
		} else {
			message = formatMessage(record);

			if (message != null) {
				message = message.trim();

				// Check for special tokens, like "ENTRY", which
				// will be handled differently by the handler than
				// normal messages. They need to be removed.

				for (int i = 0; i < fSpecialTokens.length; i++) {
					if (message.startsWith(fSpecialTokens[i])) {
						// Remove the special token.
						message = message.substring(fSpecialTokens[i].length());
						break;
					}
				}
			}
		}

		if (message != null && message.length() > 0) {
			if (sb.length() > 0) {
				sb.append(":");
				if (!message.startsWith(" ")) {
					sb.append(" ");
				}
			}
			sb.append(message);
		}

		return sb.toString();
	}
}

