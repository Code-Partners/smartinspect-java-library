/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */
package com.gurock.smartinspect.connections;

import com.gurock.smartinspect.FileRotate;
import com.gurock.smartinspect.Level;

/**
 * Assists in building a SmartInspect connections string.
 *
 * <p>
 * The ConnectionsBuilder class assists in creating connections strings
 * as used by the SmartInspect.setConnections method. To get started,
 * please have a look at the following example. For information about
 * connections strings, please refer to the SmartInspect.setConnections
 * method.
 * </p>
 *
 * <p>
 * This class is not guaranteed to be threadsafe.
 * </p>
 *
 * <pre>
 * Usage:
 *   ConnectionsBuilder builder = new ConnectionsBuilder();
 *   builder.beginProtocol("file");
 *   builder.addOption("filename", "log.sil");
 *   builder.addOption("append", true);
 *   builder.endProtocol();
 *   SiAuto.si.setConnections(builder.getConnections());
 * </pre>
 */
public class ConnectionsBuilder {
	private boolean fHasOptions;
	private StringBuffer fBuffer;

	/**
	 * Creates and initializes a ConnectionsBuilder instance.
	 */
	public ConnectionsBuilder() {
		this.fBuffer = new StringBuffer();
	}

	/**
	 * Clears this ConnectionsBuilder instance by removing all protocols
	 * and their options.
	 *
	 * <p>After this method has been called, the getConnections method
	 * returns an empty string.</p>
	 */
	public void clear() {
		this.fBuffer.setLength(0);
	}

	/**
	 * Begins a new protocol section.
	 * <p>This method begins a new protocol with the supplied name. All
	 * subsequent protocol options are added to this protocol until
	 * the new protocol section is closed by calling the endProtocol
	 * method.</p>
	 *
	 * @param protocol The name of the new protocol.
	 * @throws NullPointerException If the protocol argument is null.
	 */
	public void beginProtocol(String protocol) {
		if (protocol == null) {
			throw new NullPointerException("protocol");
		} else {
			if (this.fBuffer.length() != 0) {
				this.fBuffer.append(", ");
			}

			this.fBuffer.append(protocol);
			this.fBuffer.append("(");
			this.fHasOptions = false;
		}
	}

	/**
	 * Ends the current protocol section.
	 * <p>
	 * This method ends the current protocol. To begin a new protocol
	 * section, use the beginProtocol method.
	 */
	public void endProtocol() {
		this.fBuffer.append(")");
	}

	private String escape(String value) {
		if (value.indexOf('"') >= 0) {
			StringBuffer sb = new StringBuffer();

			for (int i = 0; i < value.length(); i++) {
				char c = value.charAt(i);

				if (c == '"') {
					sb.append("\"\"");
				} else {
					sb.append(c);
				}
			}

			return sb.toString();
		} else {
			return value;
		}
	}

	/**
	 * Overloaded method. Adds a new string option to the current protocol section.
	 * <p>This method adds a new string option to the current protocol
	 * section. The supplied value argument is properly escaped if necessary.</p>
	 *
	 * @param key   The key of the new option.
	 * @param value The value of the new option.
	 * @throws NullPointerException if the key or value argument is null.
	 */
	public void addOption(String key, String value) {
		if (key == null) {
			throw new NullPointerException("key");
		} else if (value == null) {
			throw new NullPointerException("value");
		} else {
			if (this.fHasOptions) {
				this.fBuffer.append(", ");
			}

			this.fBuffer.append(key);
			this.fBuffer.append("=\"");
			this.fBuffer.append(escape(value));
			this.fBuffer.append("\"");

			this.fHasOptions = true;
		}
	}

	/**
	 * This method adds a new boolean option to the current protocol section.
	 *
	 * @param key   The key of the new option.
	 * @param value The value of the new option.
	 * @throws NullPointerException if the key argument is null
	 */
	public void addOption(String key, boolean value) {
		addOption(key, value ? "true" : "false");
	}

	/**
	 * Adds a new integer option to the current protocol
	 * section.
	 * <p>This method adds a new integer option to the current protocol
	 * section.
	 *
	 * @param key   The key of the new option.
	 * @param value The value of the new option.
	 * @throws NullPointerException If the key argument is null.
	 */
	public void addOption(String key, int value) {
		addOption(key, Integer.toString(value));
	}

	/**
	 * Overloaded method. This method adds a new Level option to the current protocol
	 * section.
	 *
	 * @param key   The key of the new option.
	 * @param value The value of the new option.
	 * @throws NullPointerException if the key or value argument is null.
	 */
	public void addOption(String key, Level value) {
		if (value == null) {
			throw new NullPointerException("value");
		} else {
			addOption(key, value.toString().toLowerCase());
		}
	}

	/**
	 * Overloaded method. Adds a new FileRotate option to the current protocol section.
	 * <p>This method adds a new FileRotate option to the current protocol section.</p>
	 *
	 * @param key   The key of the new option.
	 * @param value The value of the new option.
	 * @throws NullPointerException if The key or value argument is null.
	 */
	public void addOption(String key, FileRotate value) {
		if (value == null) {
			throw new NullPointerException("value");
		} else {
			addOption(key, value.toString().toLowerCase());
		}
	}

	/**
	 * Returns the built connections string.
	 * <p> This method returns the connections string which has previously
	 * been built with the beginProtocol, addOption and endProtocol
	 * methods. </p>
	 *
	 * @return The built connections string.
	 */
	public String getConnections() {
		return this.fBuffer.toString();
	}
}
