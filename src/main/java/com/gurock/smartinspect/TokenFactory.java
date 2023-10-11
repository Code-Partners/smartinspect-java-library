/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * Creates instances of Token subclasses.
 * <p>
 * This class has only one public method called getToken, which is capable of creating Token objects depending on the given argument. It is important to note that this class is not guaranteed to be thread safe.
 */
public class TokenFactory {
	private static HashMap fTokens;

	static class AppNameToken extends Token {
		public String expand(LogEntry logEntry) {
			return logEntry.getAppName();
		}
	}

	static class SessionToken extends Token {
		public String expand(LogEntry logEntry) {
			return logEntry.getSessionName();
		}
	}

	static class HostNameToken extends Token {
		public String expand(LogEntry logEntry) {
			return logEntry.getHostName();
		}
	}

	static class TitleToken extends Token {
		public String expand(LogEntry logEntry) {
			return logEntry.getTitle();
		}

		public boolean getIndent() {
			return true;
		}
	}

	static class TimestampToken extends Token {
		private static final String FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

		private Date getTimestamp(LogEntry logEntry) {
			long timestamp = logEntry.getTimestamp() / 1000;
			timestamp -= TimeZone.getDefault().getOffset(timestamp);
			return new Date(timestamp);
		}

		public String expand(LogEntry logEntry) {
			Date timestamp = getTimestamp(logEntry);

			String options = getOptions();
			if (options != null && options.length() > 0) {
				try {
					DateFormat fmt = new SimpleDateFormat(options);
					return fmt.format(timestamp);
				} catch (IllegalArgumentException e) {
				}
			}

			DateFormat fmt = new SimpleDateFormat(FORMAT);
			return fmt.format(timestamp);
		}
	}

	static class LevelToken extends Token {
		public String expand(LogEntry logEntry) {
			return logEntry.getLevel().toString();
		}
	}

	static class ColorToken extends Token {
		private static final String CHARMAP = "0123456789ABCDEF";

		private void appendHex(StringBuffer sb, int value) {
			value &= 0xff;
			sb.append((char) CHARMAP.charAt(value & 0xf));
			sb.append((char) CHARMAP.charAt(value >> 4));
		}

		public String expand(LogEntry logEntry) {
			Color color = logEntry.getColor();

			if (color != null && color != Session.DEFAULT_COLOR) {
				StringBuffer sb = new StringBuffer();
				sb.append("0x");
				appendHex(sb, color.getRed());
				appendHex(sb, color.getGreen());
				appendHex(sb, color.getBlue());
				return sb.toString();
			} else {
				return "<default>";
			}
		}
	}

	static class LogEntryTypeToken extends Token {
		public String expand(LogEntry logEntry) {
			return logEntry.getLogEntryType().toString();
		}
	}

	static class ViewerIdToken extends Token {
		public String expand(LogEntry logEntry) {
			return logEntry.getViewerId().toString();
		}
	}

	static class ThreadIdToken extends Token {
		public String expand(LogEntry logEntry) {
			return Integer.toString(logEntry.getThreadId());
		}
	}

	static class ProcessIdToken extends Token {
		public String expand(LogEntry logEntry) {
			return Integer.toString(logEntry.getProcessId());
		}
	}

	static class LiteralToken extends Token {
		public String expand(LogEntry logEntry) {
			return getValue();
		}
	}

	// <ignore>
	// The documentation system seems to have problems with static
	// code blocks in Java, so we need to ignore the entire block
	// here.

	static {
		fTokens = new HashMap();
		fTokens.put("%appname%", AppNameToken.class);
		fTokens.put("%session%", SessionToken.class);
		fTokens.put("%hostname%", HostNameToken.class);
		fTokens.put("%title%", TitleToken.class);
		fTokens.put("%timestamp%", TimestampToken.class);
		fTokens.put("%level%", LevelToken.class);
		fTokens.put("%color%", ColorToken.class);
		fTokens.put("%logentrytype%", LogEntryTypeToken.class);
		fTokens.put("%viewerid%", ViewerIdToken.class);
		fTokens.put("%thread%", ThreadIdToken.class);
		fTokens.put("%process%", ProcessIdToken.class);
	}

	// </ignore>

	/**
	 * Creates instance of Token subclasses.
	 * This method analyzes and parses the supplied representation of a token and creates an appropriate Token object.
	 * For example, if the value argument is set to "%session%", a Token object is created and returned which is responsible for expanding the %session% variable.
	 * For a list of available tokens and a detailed description, please have a look at the PatternParser class,
	 * especially the PatternParser.setPattern method.
	 *
	 * @param value The original string representation of the token
	 * @return An appropriate Token object for the given string representation of a token
	 */
	public static Token getToken(String value) {
		if (value == null) {
			return createLiteral("");
		}

		if (value.length() <= 2) {
			return createLiteral(value);
		}

		int length = value.length();

		if (value.charAt(0) != '%' || value.charAt(length - 1) != '%') {
			return createLiteral(value);
		}

		String original = value;
		String options = "";

		// Extract the token options: %token{options}%
		int index;
		if (value.charAt(length - 2) == '}') {
			index = value.indexOf('{');

			if (index > -1) {
				index++;
				options = value.substring(index, length - 2);
				value = value.substring(0, index - 1) +
						value.substring(length - 1);
				length = value.length();
			}
		}

		String width = "";
		index = value.indexOf(',');

		// Extract the token width: %token,width%
		if (index > -1) {
			index++;
			width = value.substring(index, length - 1);
			value = value.substring(0, index - 1) +
					value.substring(length - 1);
		}

		value = value.toLowerCase();
		Class impl = (Class) fTokens.get(value);

		if (impl == null) {
			return createLiteral(original);
		}

		Token token;
		try {
			// Create the token and assign the properties
			token = (Token) impl.newInstance();
			token.setOptions(options);
			token.setValue(original);
			token.setWidth(parseWidth(width));
		} catch (Exception e) {
			return createLiteral(original);
		}

		return token;
	}

	private static Token createLiteral(String value) {
		Token token = new LiteralToken();
		token.setOptions("");
		token.setValue(value);
		return token;
	}

	private static int parseWidth(String value) {
		if (value == null) {
			return 0;
		}

		value = value.trim();
		if (value.length() == 0) {
			return 0;
		}

		int width;

		try {
			width = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			width = 0;
		}

		return width;
	}
}
