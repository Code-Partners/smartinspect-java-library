//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Represents the Process Flow packet type which is used in the
//   enter-/leave methods in the Session class.
// </summary>
// <remarks>
//   A Process Flow entry is responsible for illustrated process and
//   thread information.
//
//   It has several properties which describe its creation context (like
//   a thread ID, timestamp or hostname) and other properties which
//   specify the way the Console interprets this packet (like the process
//   flow ID). Furthermore a Process Flow entry contains the actual data,
//   namely the title, which will be displayed in the Console.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe. However, instances
//   of this class will normally only be used in the context of a single
//   thread.
// </threadsafety>

public final class ProcessFlow extends Packet
{
	private String fHostName;
	private ProcessFlowType fProcessFlowType;
	private String fTitle;
	private long fTimestamp;
	private int fThreadId;
	private int fProcessId;

	private static int PROCESS_ID = Runtime.getRuntime().hashCode();
	private static final int HEADER_SIZE = 28;

	// <summary>
	//   Overloaded. Creates and initializes a ProcessFlow instance.
	// </summary>
	
	public ProcessFlow()
	{

	}
	
	// <summary>
	//   Overloaded. Creates and initializes a ProcessFlow instance
	//   with a custom process flow type.
	// </summary>
	// <param name="processFlowType">
	//   The type of the new Process Flow entry describes the way the
	//   Console interprets this packet. Please see the ProcessFlowType
	//   enum for more information. Now allowed to be null.
	// </param>
	// <remarks>
	//   If the processFlowType argument is a null reference a
	//   NullPointerException will be thrown.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The processFlowType argument is null.
	// </table>
	// </exception>

	public ProcessFlow(ProcessFlowType processFlowType)
	{
		setProcessFlowType(processFlowType);
		this.fThreadId = super.getThreadId();
		this.fProcessId = PROCESS_ID;
	}

	// <summary>
	//   Overridden. Returns the total occupied memory size of this
	//   Process Flow packet.
	// </summary>
	// <returns>
	//   The total occupied memory size of this Process Flow entry.
	// </returns>	
	// <remarks>
	//   The total occupied memory size of this Process Flow entry is
	//   the size of memory occupied by all strings and any internal
	//   data structures of this Process Flow entry.
	// </remarks>

	public int getSize()
	{
		return HEADER_SIZE +
			getStringSize(this.fTitle) +
			getStringSize(this.fHostName);
	}

	// <summary>
	//   Overridden. Returns PacketType.ProcessFlow.
	// </summary>
	// <returns>
	//   Just PacketType.ProcessFlow. For a complete list of available          
	//   packet types, please have a look at the documentation of the
	//   PacketType enum.
	// </returns>		

	public PacketType getPacketType()
	{
		return PacketType.ProcessFlow;
	}

	// <summary>
	//   Returns the title of this Process Flow entry.
	// </summary>
	// <returns>
	//   The title of the Process Flow or null if the title has not
	//   been set.
	// </returns>
	// <remarks>
	//   This method can return null if this Process Flow entry does
	//   not contain a title. If this is the case, it will be empty in
	//   the SmartInspect Console.
	// </remarks>

	public String getTitle()
	{
		return this.fTitle;
	}

	// <summary>
	//   Sets the title of the Process Flow Entry.
	// </summary>
	// <param name="title">
	//   The new title of this Process Flow entry. Can be null.
	// </param>
	// <remarks>
	//   The title of this Process Flow entry will be empty in the
	//   SmartInspect Console when this method gets a null reference as
	//   argument.
	// </remarks>

	public void setTitle(String title)
	{
		this.fTitle = title;
	}

	// <summary>
	//   Returns the hostname of this Process Flow entry.
	// </summary>
	// <returns>
	//   The hostname of the machine, which sent this Process Flow entry
	//   or null if the hostname has not been set.
	// </returns>
	// <remarks>
	//   The hostname of this Process Flow entry is usually set to the
	//   name of the machine this Process Flow entry is sent from.
	//
	//   This method can return null if this Process Flow entry does not
	//   contain a hostname. If this is the case, it will be empty in the
	//   SmartInspect Console.
	// </remarks>

	public String getHostName()
	{
		return this.fHostName;
	}

	// <summary>
	//   Sets the hostname of this Process Flow Entry.
	// </summary>
	// <param name="hostName">
	//   The new hostname of this Process Flow entry. Can be null.
	// </param>
	// <remarks>
	//   The hostname of this Process Flow entry will be empty in the
	//   SmartInspect Console when this method gets a null reference as
	//   argument.
	// </remarks>

	public void setHostName(String hostName)
	{
		this.fHostName = hostName;
	}

	// <summary>
	//   Returns the type of this Process Flow entry.
	// </summary>
	// <returns>
	//   The type of the Process Flow entry. It describes the way the
	//   Console interprets this packet. Please see the ProcessFlowType enum
	//   for more information.
	// </returns>

	public ProcessFlowType getProcessFlowType()
	{
		return this.fProcessFlowType;
	}

	// <summary>
	//   Sets the type of this Process Flow entry.
	// </summary>
	// <param name="processFlowType">
	//   The new type of this Process Flow entry. Not allowed to be
	//   null.
	// </param>
	// <remarks>
	//   The type of the Process Flow entry describes the way the
	//   Console interprets this packet. Please see the ProcessFlowType
	//   enum for more information.
	//
	//   If the processFlowType argument is a null reference a
	//   NullPointerException will be thrown.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The processFlowType argument is null.
	// </table>
	// </exception>

	public void setProcessFlowType(ProcessFlowType processFlowType)
	{
		if (processFlowType == null)
		{
			throw new NullPointerException("processFlowType");
		}
		else 
		{
			this.fProcessFlowType = processFlowType;
		}
	}
	
	// <summary>
	//   Returns the timestamp of this ProcessFlow object.
	// </summary>
	// <returns>
	//   The creation time of this Process Flow object as returned by
	//   the SmartInspect.now method.
	// </returns>
	
	public long getTimestamp()
	{
		return this.fTimestamp;
	}

	// <summary>
	//   Sets the timestamp of this ProcessFlow object.
	// </summary>
	// <param name="timestamp">
	//   The new timestamp of this ProcessFlow object. The passed value
	//   should represent the local date and time in microseconds since
	//   January 1, 1970. See SmartInspect.now for more information.
	// </param>
	
	public void setTimestamp(long timestamp)
	{
		this.fTimestamp = timestamp;
	}
	
	// <summary>
	//   Returns the process ID of this ProcessFlow object.
	// </summary>
	// <returns>
	//   The ID of the process this Process Flow object was created
	//   in.
	// </returns>

	public int getProcessId()
	{
		return this.fProcessId;
	}
	
	// <summary>
	//   Sets the process ID of this ProcessFlow object.
	// </summary>
	// <param name="processId">
	//   The new process ID of this Process Flow object.
	// </returns>

	public void setProcessId(int processId)
	{
		this.fProcessId = processId;
	}

	// <summary>
	//   Returns the thread ID of this ProcessFlow object.
	// </summary>
	// <returns>
	//   The ID of the thread this Process Flow object was created
	//   in.
	// </returns>

	public int getThreadId()
	{
		return this.fThreadId;
	}
	
	// <summary>
	//   Sets the thread ID of this ProcessFlow object.
	// </summary>
	// <param name="threadId">
	//   The new thread ID of this Process Flow object.
	// </returns>

	public void setThreadId(int threadId)
	{
		this.fThreadId = threadId;
	}
}
