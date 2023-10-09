/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Represents the Log Header packet type which is used for storing
 * and transferring log metadata.
 *
 * <p>The LogHeader class is used to store and transfer log metadata.
 * After the PipeProtocol or TcpProtocol has established a connection,
 * a Log Header packet with the metadata of the current logging
 * context is created and written. Log Header packets are used by
 * the SmartInspect Router application for its filter and trigger
 * functionality.</p>
 *
 * <p>This class is not guaranteed to be threadsafe. However, instances
 * of this class will normally only be used in the context of a single
 * thread.</p>
 */
public class LogHeader extends Packet {
	private static final int HEADER_SIZE = 4;

	private String fAppName;
	private String fHostName;

	/**
	 * Overridden. Returns the total occupied memory size of this Log Header packet.
	 * The total occupied memory size of this Log Header is the size
	 * of memory occupied by all strings and any internal data
	 * structures of this Log Header.
	 *
	 * @return The total occupied memory size of this Log Header
	 */
	public int getSize() {
		return HEADER_SIZE + getStringSize(getContent());
	}

	/**
	 * Overridden. Returns PacketType.LogHeader.
	 *
	 * @return Just PacketType.LogHeader. For a complete list of available
	 * packet types, please have a look at the documentation of the
	 * PacketType type
	 */
	public PacketType getPacketType() {
		return PacketType.LogHeader;
	}

	/**
	 * Returns the entire content of this Log Header packet.
	 * The content of a Log Header packet is a key-value (syntax:
	 * key=value) list of the properties of this Log Header packet
	 * (currently only the getAppName and the getHostName strings).
	 * Key-value pairs are separated by carriage return and newline
	 * characters.
	 *
	 * @return The entire content of this Log Header packet
	 */
	public String getContent() {
		return "hostname=" +
				(this.fHostName != null ? this.fHostName : "") +
				"\r\n" +
				"appname=" +
				(this.fAppName != null ? this.fAppName : "") +
				"\r\n";
	}

	/**
	 * Returns the hostname of this Log Header.
	 * The hostname of a Log Header is usually set to the name of the machine this Log Entry is sent from.
	 * This method can return null if this Log Header does not contain a hostname.
	 *
	 * @return The hostname of the Log Header or null if the hostname has not been set
	 */
	public String getHostName() {
		return this.fHostName;
	}

	/**
	 * Sets the hostname of this Log Header.
	 *
	 * @param hostName The new hostname of this Log Header. Can be null
	 */
	public void setHostName(String hostName) {
		this.fHostName = hostName;
	}

	/**
	 * Returns the application name of this Log Header.
	 * <p>
	 * The application name of a Log Header is usually set to the name of the application this Log Header is created in.
	 * This method can return null if this Log Header does not contain an application name.
	 *
	 * @return The application name of the Log Header or null if the application name has not been set
	 */
	public String getAppName() {
		return this.fAppName;
	}

	/**
	 * Sets the application name of this Log Header.
	 *
	 * @param appName The new application name of this Log Header. Can be null
	 */
	public void setAppName(String appName) {
		this.fAppName = appName;
	}
}
