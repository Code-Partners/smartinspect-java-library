/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.contexts;

import com.gurock.smartinspect.ViewerId;

/**
 * Represents the table viewer in the Console which can display text
 * data as a table. The table viewer in the Console interprets the data of a Log Entry as a table. This class
 * takes care of the necessary formatting and escaping required by
 * the corresponding table viewer in the Console.
 *
 * <p>
 * You can use the TableViewerContext class for creating custom
 * log methods around logCustomContext for sending custom data organized as tables.
 * This class is not guaranteed to be thread safe.
 */
public class TableViewerContext extends ListViewerContext {
	private boolean fLineStart;

	/**
	 * Creates and initializes a TableViewerContext instance.
	 */
	public TableViewerContext() {
		super(ViewerId.Table);
		this.fLineStart = true;
	}

	/**
	 * Appends a header to the text data.
	 *
	 * @param header The header to append
	 */
	public void appendHeader(String header) {
		appendLine(header);
		appendLine("");
	}

	/**
	 * Overloaded. Adds a string entry to the current row.
	 *
	 * @param entry The string entry to add
	 */
	public void addRowEntry(String entry) {
		if (entry != null) {
			if (this.fLineStart) {
				this.fLineStart = false;
			} else {
				appendText(", ");
			}
			appendText(escapeCSVEntry(entry));
		}
	}

	/**
	 * Overloaded. Adds a char entry to the current row.
	 *
	 * @param entry The char entry to add
	 */
	public void addRowEntry(char entry) {
		addRowEntry(String.valueOf(entry));
	}

	/**
	 * Overloaded. Adds a boolean entry to the current row.
	 *
	 * @param entry The boolean entry to add
	 */
	public void addRowEntry(boolean entry) {
		addRowEntry(String.valueOf(entry));
	}

	/**
	 * Overloaded. Adds a byte entry to the current row.
	 *
	 * @param entry The byte entry to add
	 */
	public void addRowEntry(byte entry) {
		addRowEntry(String.valueOf(entry));
	}

	/**
	 * Overloaded. Adds a short entry to the current row.
	 *
	 * @param entry The short entry to add
	 */
	public void addRowEntry(short entry) {
		addRowEntry(String.valueOf(entry));
	}

	/**
	 * Overloaded. Adds an int entry to the current row.
	 *
	 * @param entry The int entry to add
	 */
	public void addRowEntry(int entry) {
		addRowEntry(String.valueOf(entry));
	}

	/**
	 * Overloaded. Adds a long entry to the current row.
	 *
	 * @param entry The long entry to add
	 */
	public void addRowEntry(long entry) {
		addRowEntry(String.valueOf(entry));
	}

	/**
	 * Overloaded. Adds a float entry to the current row.
	 *
	 * @param entry The float entry to add
	 */
	public void addRowEntry(float entry) {
		addRowEntry(String.valueOf(entry));
	}

	/**
	 * Overloaded. Adds a double entry to the current row.
	 *
	 * @param entry The double entry to add
	 */
	public void addRowEntry(double entry) {
		addRowEntry(String.valueOf(entry));
	}

	/**
	 * Overloaded. Adds an object entry to the current row.
	 *
	 * @param entry The object entry to add
	 */
	public void addRowEntry(Object entry) {
		addRowEntry(String.valueOf(entry));
	}

	private static String escapeCSVEntry(String entry) {
		if (entry == null || entry.length() == 0) {
			return entry;
		} else {
			StringBuffer sb = new StringBuffer(2 * entry.length());
			sb.append("\"");

			for (int i = 0, len = entry.length(); i < len; i++) {
				char c = entry.charAt(i);
				if (Character.isWhitespace(c)) {
					// Newline characters need to be escaped,
					// they would break the csv format.
					sb.append(" ");
				} else if (c == '"') {
					// '"' characters are used to surround entries
					// in the csv format, so they need to be escaped.
					sb.append("\"\"");
				} else {
					// This character is valid, so just append it.
					sb.append(c);
				}
			}

			sb.append("\"");
			return sb.toString();
		}
	}

	/**
	 * Begins a new row.
	 */
	public void beginRow() {
		this.fLineStart = true;
	}

	/**
	 * Ends the current row.
	 */
	public void endRow() {
		appendLine("");
	}
}
