/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.packets;

import java.util.HashMap;
import java.util.Map;

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

	private final Map<String, String> values = new HashMap<>();

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
		StringBuilder content = new StringBuilder();

		for (Map.Entry<String, String> entry : values.entrySet()) {
			String v = entry.getValue() != null ? entry.getValue() : "";

			content.append(entry.getKey())
					.append('=')
					.append(v)
					.append("\r\n");
		}

		return content.toString();
	}

	/**
	 * Adds a key-value pair to this Log Header packet. If the key already exists,
	 * its value is overwritten with the provided value.
	 *
	 * @param key   The key to be associated with the specified value
	 * @param value The value to be associated with the specified key
	 */
	public void addValue(String key, String value) {
		values.put(key, value);
	}
}
