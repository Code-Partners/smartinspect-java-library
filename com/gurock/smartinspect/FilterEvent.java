//
// <!-- Copyright (C) 2003-2010 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   This class is used by the SmartInspectListener.onFilter event of
//   the SmartInspect class.
// </summary>
// <remarks>
//   This class consists of only three class members. At first we have
//   the getPacket method, which returns the packet which caused the
//   event.
//   
//   Then there are the getCancel and setCancel methods which can be
//   used to cancel the processing of certain packets. For more
//   information, please refer to the SmartInspectListener.onFilter
//   documentation.
// </remarks>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

public class FilterEvent extends java.util.EventObject
{
	private Packet fPacket;
	private boolean fCancel;

	// <summary>
	//   Creates and initializes a FilterEvent instance.
	// </summary>
	// <param name="source">The object which fired the event.</param>
	// <param name="packet">
	//   The packet which caused the event.
	// </param>
	
	public FilterEvent(Object source, Packet packet)
	{
		super(source);
		this.fCancel = false;
		this.fPacket = packet;
	}
	
	// <summary>
	//   Returns the packet which caused the event.
	// </summary>
	// <returns>
	//   The packet which caused the event.
	// </returns>	
	
	public Packet getPacket()
	{
		return this.fPacket;
	}
	
	// <summary>
	//   Indicates if processing of the current packet should be cancelled
	//   or not.
	// </summary>
	// <returns>
	//   True if processing of the current packet should be cancelled
	//   and false otherwise.
	// </returns>
	// <remarks>
	//   For more information, please refer to the documentation of the
	//   setCancel method or the SmartInspectListener.onFilter event of
	//   the SmartInspect class.
	// </remarks>

	public boolean getCancel()
	{
		return this.fCancel;
	}
	
	// <summary>
	//   This method can be used to cancel the processing of certain
	//   packets during the SmartInspectListener.onFilter event of the
	//   SmartInspect class.
	// </summary>
	// <param name="cancel">
	//   Specifies if processing of the current packet should be cancelled
	//   or not.
	// </param>
	// <remarks>
	//   For more information on how to use this method, please refer to
	//   the SmartInspectListener.onFilter event documentation.
	// </remarks>
	
	public void setCancel(boolean cancel)
	{
		this.fCancel = cancel;
	}
}
