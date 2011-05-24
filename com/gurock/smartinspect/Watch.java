//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Represents the Watch packet type which is used in the watch
//   methods in the Session classes.
// </summary>
// <remarks>
//   A Watch is responsible for sending variables and their values
//   to the Console. These key/value pairs will be displayed in the
//   Watches toolbox. If a Watch with the same name is sent twice,
//   the old value is overwritten and the Watches toolbox displays
//   the most current value.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe. However, instances
//   of this class will normally only be used in the context of a single
//   thread.
// </threadsafety>

public final class Watch extends Packet
{
	private WatchType fWatchType;
	private String fName;
	private String fValue;
	private long fTimestamp;

	private static final int HEADER_SIZE = 20;

	// <summary>
	//   Overloaded. Creates and initializes a Watch instance.
	// </summary>

	public Watch()
	{
		
	}

	// <summary>
	//   Overloaded. Creates and initializes a Watch instance with a
	//   custom watch type.
	// </summary>
	// <param name="watchType">
	//   The type of the new Watch describes the variable type (String,
	//   Integer and so on). Please see the WatchType enum for more
	//   information. Not allowed to be null.
	// </param>
	// <remarks>
	//   If the watchType argument is a null reference a
	//   NullPointerException will be thrown.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The watchType argument is null.
	// </table>
	// </exception>
	
	public Watch(WatchType watchType)
	{
		setWatchType(watchType);
	}

	// <summary>
	//   Overridden. Returns the total occupied memory size of this Watch
	//   packet.
	// </summary>
	// <returns>
	//   The total occupied memory size of this Watch.
	// </returns>
	// <remarks>
	//   The total occupied memory size of this Watch is the size of
	//   memory occupied by all strings and any internal data structures
	//   of this Watch.
	// </remarks>

	public int getSize()
	{
		return HEADER_SIZE +
			getStringSize(this.fName) +
			getStringSize(this.fValue);
	}

	// <summary>
	//   Overridden. Returns PacketType.Watch.
	// </summary>
	// <returns>
	//   Just PacketType.Watch. For a complete list of available packet
	//   types, please have a look at the documentation of the PacketType
	//   enum.
	// </returns>	

	public PacketType getPacketType()
	{
		return PacketType.Watch;
	}

	// <summary>Returns the name of this Watch.</summary>
	// <returns>
	//   The name of this Watch or null if the name of this Watch has
	//   not been set.
	// </returns>
	// <remarks>
	//   If a Watch with the same name is sent twice, the old value is
	//   overwritten and the Watches toolbox displays the most current
	//   value.
	//
	//   This method can return null if this Watch does not contain a
	//   name. If this is the case, it will be empty in the SmartInspect
	//   Console.
	// </remarks>

	public String getName()
	{
		return this.fName;
	}

	// <summary>
	//   Sets the name of this Watch.
	// </summary>
	// <param name="name">
	//   The new name of this Watch. Can be null.
	// </param>
	// <remarks>
	//   If a Watch with the same name is sent twice, the old value is
	//   overwritten and the Watches toolbox displays the most current
	//   value.
	//
	//   The name of this Watch will be empty in the SmartInspect
	//   Console when this method gets a null reference as argument.
	// </remarks>

	public void setName(String name)
	{
		this.fName = name;
	}

	// <summary>Returns the value of this Watch.</summary>
	// <returns>
	//   The value of this Watch or null if the value of this Watch has
	//   not been set.
	// </returns>
	// <remarks>
	//   The value of a Watch is always sent as String. To view the
	//   type of this variable Watch, please have a look at the
	//   getWatchType method.
	//
	//   This method can return null if this Watch does not contain a
	//   value. If this is the case, it will be empty in the SmartInspect
	//   Console.
	// </remarks>
	
	public String getValue()
	{
		return this.fValue;
	}

	// <summary>
	//   Sets the value of this Watch.
	// </summary>
	// <param name="value">
	//   The new value of this Watch. Can be null.
	// </param>
	// <remarks>
	//   The value of a Watch is always sent as String. To change the
	//   type of this variable Watch, please have a look at the
	//   setWatchType method.
	//
	//   The value of this Watch will be empty in the SmartInspect
	//   Console when this method gets a null reference as argument.
	// </remarks>

	public void setValue(String value)
	{
		this.fValue = value;	
	}

	// <summary>
	//   Returns the type of this Watch.
	// </summary>
	// <returns>
	//   The type of this Watch. It describes the variable type (String,
	//   Integer and so on). Please see the WatchType enum for more
	//   information.
	// </returns>

	public WatchType getWatchType()
	{
		return this.fWatchType;
	}

	// <summary>
	//   Sets the type of this Watch.
	// </summary>
	// <param name="watchType">
	//   The new type of this Watch. Not allowed to be null.
	// </param>
	// <remarks>
	//   The type of this Watch describes the variable type (String,
	//   Integer and so on). Please see the WatchType enum for more
	//   information.
	//
	//   If the watchType argument is a null reference a
	//   NullPointerException will be thrown.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The watchType argument is null.
	// </table>
	// </exception>

	public void setWatchType(WatchType watchType)
	{
		if (watchType == null)
		{
			throw new NullPointerException("watchType argument is null");
		}
		else 
		{
			this.fWatchType = watchType;
		}
	}
	
	// <summary>
	//   Returns the timestamp of this Watch object.
	// </summary>
	// <returns>
	//   The creation time of this Watch object as returned by the
	//   SmartInspect.now method.
	// </returns>
	
	public long getTimestamp()
	{
		return this.fTimestamp;
	}
	
	// <summary>
	//   Sets the timestamp of this Watch object.
	// </summary>
	// <param name="timestamp">
	//   The new timestamp of this Watch object. The passed value should
	//   represent the local date and time in microseconds since January
	//   1, 1970. See SmartInspect.now for more information.
	// </param>
	
	public void setTimestamp(long timestamp)
	{
		this.fTimestamp = timestamp;
	}
}
