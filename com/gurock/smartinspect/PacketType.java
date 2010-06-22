//
// <!-- Copyright (C) 2003-2010 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Represents the type of a packet. In the SmartInspect concept,
//   there are multiple packet types each serving a special purpose.
//   For a good starting point on packets, please have a look at the
//   documentation of the Packet class.
// </summary>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

public final class PacketType extends Enum
{
	// <summary>
	//   Identifies a packet as Log Entry. Please have a look at the
	//   documentation of the LogEntry class for information about
	//   this packet type.
	// </summary>

	public static final
		PacketType LogEntry = new PacketType(4, "LogEntry");

	// <summary>
	//   Identifies a packet as Control Command. Please have a look
	//   at the documentation of the ControlCommand class for more
	//   information about this packet type.
	// </summary>

	public static final
		PacketType ControlCommand = new PacketType(1, "ControlCommand");

	// <summary>
	//   Identifies a packet as Watch. Please have a look at the
	//   documentation of the Watch class for information about
	//   this packet type.
	// </summary>

	public static final
		PacketType Watch = new PacketType(5, "Watch");

	// <summary>
	//   Identifies a packet as Process Flow entry. Please have a
	//   look at the documentation of the ProcessFlow class for
	//   information about this packet type.
	// </summary>

	public static final
		PacketType ProcessFlow = new PacketType(6, "ProcessFlow");

	// <summary>
	//   Identifies a packet as Log Header. Please have a look at the
	//   documentation of the LogHeader class for information about
	//   this packet type.
	// </summary>

	public static final
		PacketType LogHeader = new PacketType(7, "LogHeader");

	private PacketType(int value, String name)
	{
		super(value, name);
	}
}
