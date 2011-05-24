//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Represents a scheduler action to execute when a protocol is
//   operating in asynchronous mode. For general information about
//   the asynchronous mode, please refer to Protocol.IsValidOption.
// </summary>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

public class SchedulerAction extends Enum
{
	// <summary>
	//   Represents a connect protocol operation. This action is
	//   enqueued when the Protocol.connect method is called and
	//   the protocol is operating in asynchronous mode.
	// </summary>
	
	public static SchedulerAction Connect = 
		new SchedulerAction(0, "Connect");
	
	// <summary>
	//   Represents a write protocol operation. This action is
	//   enqueued when the Protocol.writePacket method is called
	//   and the protocol is operating in asynchronous mode.
	// </summary>
	
	public static SchedulerAction WritePacket = 
		new SchedulerAction(1, "WritePacket");
	
	// <summary>
	//   Represents a disconnect protocol operation. This action
	//   is enqueued when the Protocol.disconnect method is called
	//   and the protocol is operating in asynchronous mode.
	// </summary>
	
	public static SchedulerAction Disconnect = 
		new SchedulerAction(2, "Disconnect");
	
	// <summary>
	//   Represents a dispatch protocol operation. This action is
	//   enqueued when the Protocol.dispatch method is called and
	//   the protocol is operating in asynchronous mode.
	// </summary>
	
	public static SchedulerAction Dispatch = 
		new SchedulerAction(3, "Dispatch"); 
	
	private SchedulerAction(int value, String name)
	{		
		super(value, name);
	}
}
