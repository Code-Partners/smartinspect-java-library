//
//<!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect;

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

public class LogHeader extends Packet 
{
	private static final int HEADER_SIZE = 4;
	
	private String fAppName;
	private String fHostName;
	
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
		return "hostname=" +
			(this.fHostName != null ? this.fHostName : "") +
			"\r\n" +
			"appname=" +
			(this.fAppName != null ? this.fAppName : "") +
			"\r\n";
	}
	
	// <summary>
	//   Returns the hostname of this Log Header.
	// </summary>
	// <returns>
	//   The hostname of the Log Header or null if the hostname has not
	//   been set.
	// </returns>
	// <remarks>
	//   The hostname of a Log Header is usually set to the name of the
	//   machine this Log Entry is sent from. This method can return
	//   null if this Log Header does not contain a hostname.
	// </remarks>
	
	public String getHostName()
	{
		return this.fHostName;
	}

	// <summary>
	//   Sets the hostname of this Log Header.
	// </summary>
	// <param name="hostName">
	//   The new hostname of this Log Header. Can be null.</param>

	public void setHostName(String hostName)
	{
		this.fHostName = hostName;
	}

	// <summary>
	//   Returns the application name of this Log Header.
	// </summary>
	// <returns>
	//   The application name of the Log Header or null if the application
	//   name has not been set.
	// </returns>
	// <remarks>
	//   The application name of a Log Header is usually set to the name of
	//   the application this Log Header is created in. This method can
	//   return null if this Log Header does not contain an application
	//   name.
	// </remarks>

	public String getAppName()
	{
		return this.fAppName;
	}

	// <summary>
	//   Sets the application name of this Log Header.
	// </summary>
	// <param name="appName">
	//   The new application name of this Log Header. Can be null.
	// </param>

	public void setAppName(String appName)
	{
		this.fAppName = appName;
	}
}
