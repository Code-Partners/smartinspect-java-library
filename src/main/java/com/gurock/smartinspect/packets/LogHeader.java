//
//<!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect.packets;

// <summary>
//   Represents the Log Header packet type which is used for storing
//   and transferring log metadata.
// </summary>
// <remarks>
//   The LogHeader class is used to store and transfer log metadata.
//   After the PipeProtocol or TcpProtocol has established a connection,
//   a Log Header packet with the metadata of the current logging
//   context is created and written. Log Header packets are used by
//   the SmartInspect Router application for its filter and trigger
//   functionality.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe. However, instances
//   of this class will normally only be used in the context of a single
//   thread.
// </threadsafety>

import java.util.HashMap;
import java.util.Map;

public class LogHeader extends Packet
{
	private static final int HEADER_SIZE = 4;
	
 	private final Map<String, String> values = new HashMap<>();
	
	// <summary>
	//   Overridden. Returns the total occupied memory size of this Log
	//   Header packet.
	// </summary>
	// <returns>
	//   The total occupied memory size of this Log Header.
	// </returns>
	// <remarks>
	//   The total occupied memory size of this Log Header is the size
	//   of memory occupied by all strings and any internal data
	//   structures of this Log Header.
	// </remarks>
	
	public int getSize() 
	{
		return HEADER_SIZE + getStringSize(getContent());
	}

	// <summary>
	//   Overridden. Returns PacketType.LogHeader.
	// </summary>
	// <returns>
	//   Just PacketType.LogHeader. For a complete list of available
	//   packet types, please have a look at the documentation of the
	//   PacketType type.
	// </returns>
	
	public PacketType getPacketType()
	{
		return PacketType.LogHeader;
	}
	
	// <summary>
	//   Returns the entire content of this Log Header packet.
	// </summary>
	// <returns>
	//   The entire content of this Log Header packet.
	// </returns>
	// <remarks>
	//   The content of a Log Header packet is a key-value (syntax:
	//   key=value) list of the properties of this Log Header packet
	//   (currently only the getAppName and the getHostName strings).
	//   Key-value pairs are separated by carriage return and newline
	//   characters.
	// </remarks>
	
	public String getContent()
	{
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

	public void addValue(String key, String value) {
		values.put(key, value);
	}

}
