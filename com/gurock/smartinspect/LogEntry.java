//
// <!-- Copyright (C) 2003-2010 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

import java.awt.Color;

// <summary>
//   Represents the Log Entry packet type which is used for nearly all
//   logging methods in the Session class.
// </summary>
// <remarks>
//   A Log Entry is the most important packet available in the
//   SmartInspect concept. It is used for almost all logging methods
//   in the Session class, like, for example, Session.logMessage,
//   Session.logObject or Session.logSql.
//
//   A Log Entry has several properties which describe its creation
//   context (like a thread ID, timestamp or hostname) and other
//   properties which specify the way the Console interprets this packet
//   (like the viewer ID or the background color). Furthermore a Log
//   Entry contains the actual data which will be displayed in the
//   Console.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe. However, instances
//   of this class will normally only be used in the context of a single
//   thread.
// </threadsafety>

public final class LogEntry extends Packet
{
	private String fSessionName;
	private String fTitle;
	private String fAppName;
	private String fHostName;
	private LogEntryType fLogEntryType;
	private ViewerId fViewerId;
	private Color fColor;
	private byte[] fData;
	private long fTimestamp;
	private int fThreadId;
	private int fProcessId;

	private static int PROCESS_ID = Runtime.getRuntime().hashCode();
	private static final int HEADER_SIZE = 48;

	// <summary>
	//   Overloaded. Creates and initializes a LogEntry instance.
	// </summary>

	public LogEntry()
	{
		
	}
	
	// <summary>
	//   Overloaded. Creates and initializes a LogEntry instance with
	//   a custom log entry type and custom viewer ID.
	// </summary>
	// <param name="logEntryType">
	//   The type of the new Log Entry describes the way the Console
	//   interprets this packet. Please see the LogEntryType type for more
	//   information. Not allowed to be null.
	// </param>
	// <param name="viewerId">
	//   The viewer ID of the new Log Entry describes which viewer
	//   should be used in the Console when displaying the data of
	//   this Log Entry. Please see ViewerId for more information.
	//   Not allowed to be null.
	// </param>
	// <remarks>
	//   If the logEntryType or viewerId argument is a null reference a
	//   NullPointerException will be thrown.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The logEntryType or viewerId argument
	//                          is null.
	// </table>
	// </exception>

	public LogEntry(LogEntryType logEntryType, ViewerId viewerId)
	{
		setLogEntryType(logEntryType);
		setViewerId(viewerId);
		this.fThreadId = super.getThreadId();
		this.fProcessId = PROCESS_ID;
	}

	// <summary>
	//   Overridden. Returns the total occupied memory size of this Log
	//   Entry packet.
	// </summary>
	// <returns>
	//   The total occupied memory size of this Log Entry.
	// </returns>
	// <remarks>
	//   The total occupied memory size of this Log Entry is the size
	//   of memory occupied by all strings, the optional <link getData,
	//   data block> and any internal data structures of this Log Entry.
	// </remarks>

	public int getSize()
	{
		int result = HEADER_SIZE +
			getStringSize(this.fAppName) +
			getStringSize(this.fSessionName) +
			getStringSize(this.fTitle) +
			getStringSize(this.fHostName);
		
		if (this.fData != null)
		{
			result += this.fData.length;
		}

		return result;
	}

	// <summary>
	//   Overridden. Returns PacketType.LogEntry.
	// </summary>
	// <returns>
	//   Just PacketType.LogEntry. For a complete list of available
	//   packet types, please have a look at the documentation of the
	//   PacketType type.
	// </returns>

	public PacketType getPacketType()
	{
		return PacketType.LogEntry;
	}

	// <summary>
	//   Returns the title of this Log Entry.
	// </summary>
	// <returns>
	//   The title of the Log Entry or null if the title has not been
	//   set.
	// </returns>
	// <remarks>
	//   This method can return null if this Log Entry does not contain
	//   a title. If this is the case, the title of this Log Entry will be
	//   empty in the SmartInspect Console.
	// </remarks>

	public String getTitle()
	{
		return this.fTitle;
	}

	// <summary>
	//   Sets the title of this Log Entry.
	// </summary>
	// <param name="title">
	//   The new title of this Log Entry. Can be null.
	// </param>
	// <remarks>
	//   The title of this Log Entry will be empty in the SmartInspect
	//   Console when this method gets a null reference as argument.
	// </remarks>

	public void setTitle(String title)
	{
		this.fTitle = title;
	}

	// <summary>
	//   Returns the session name of the Log Entry.
	// </summary>
	// <returns>
	//   The session name of the Log Entry or null if the session name
	//   has not been set.
	// </returns>
	// <remarks>
	//   The session name of a Log Entry is normally set to the name of
	//   the session which sent this Log Entry.
	//
	//   This method can return null if this Log Entry does not contain
	//   a session name. If this is the case, the session name will be
	//   empty in the SmartInspect Console.
	// </remarks>
	
	public String getSessionName()
	{
		return this.fSessionName;
	}

	// <summary>
	//   Sets the session name of the Log Entry.
	// </summary>
	// <param name="sessionName">
	//   The new session name of this Log Entry. Can be null.</param>
	// <remarks>
	//   The session name of this Log Entry will be empty in the
	//   SmartInspect Console when this method gets a null reference as
	//   argument.
	// </remarks>

	public void setSessionName(String sessionName)
	{
		this.fSessionName = sessionName;
	}

	// <summary>
	//   Returns the hostname of this Log Entry.
	// </summary>
	// <returns>
	//   The hostname of the Log Entry or null if the hostname has not
	//   been set.
	// </returns>
	// <remarks>
	//   The hostname of a Log Entry is usually set to the name of the
	//   machine this Log Entry is sent from.
	//
	//   This method can return null if this Log Entry does not contain a
	//   hostname. If this is the case, the hostname will be empty in the
	//   SmartInspect Console.
	// </remarks>
	
	public String getHostName()
	{
		return this.fHostName;
	}

	// <summary>
	//   Sets the hostname of this Log Entry.
	// </summary>
	// <param name="hostName">
	//   The new hostname of this Log Entry. Can be null.</param>
	// <remarks>
	//   The hostname of this Log Entry will be empty in the SmartInspect
	//   Console when this method gets a null reference as argument.
	// </remarks>

	public void setHostName(String hostName)
	{
		this.fHostName = hostName;
	}

	// <summary>
	//   Returns the application name of this Log Entry.
	// </summary>
	// <returns>
	//   The application name of the Log Entry or null if the application
	//   name has not been set.
	// </returns>
	// <remarks>
	//   The application name of a Log Entry is usually set to the name of
	//   the application this Log Entry is created in.
	//
	//   This method can return null if this Log Entry does not contain an
	//   application name. If this is the case, the application name will
	//   be empty in the SmartInspect Console.
	// </remarks>

	public String getAppName()
	{
		return this.fAppName;
	}

	// <summary>
	//   Sets the application name of this Log Entry.
	// </summary>
	// <param name="appName">
	//   The new application name of this Log Entry. Can be null.
	// </param>
	// <remarks>
	//   The application name of this Log Entry will be empty in the
	//   SmartInspect Console when this method gets a null reference as
	//   argument.
	// </remarks>

	public void setAppName(String appName)
	{
		this.fAppName = appName;
	}

	// <summary>
	//   Returns the type of this Log Entry.
	// </summary>
	// <returns>
	//   The type of this Log Entry. It describes the way the Console
	//   interprets this packet. Please see the LogEntryType enum for more
	//   information.
	// </returns>

	public LogEntryType getLogEntryType()
	{
		return this.fLogEntryType;
	}

	// <summary>
	//   Sets the type of this Log Entry.
	// </summary>
	// <param name="logEntryType">
	//   The new type of this Log Entry. Not allowed to be null.
	// </param>
	// <remarks>
	//   The type of this Log Entry describes the way the Console
	//   interprets this packet. Please see the LogEntryType enum for more
	//   information.
	//
	//   If the logEntryType argument is a null reference a
	//   NullPointerException will be thrown.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The logEntryType argument is null.
	// </table>
	// </exception>

	public void setLogEntryType(LogEntryType logEntryType)
	{
		if (logEntryType == null)
		{
			throw new NullPointerException("logEntryType");
		}
		else 
		{
			this.fLogEntryType = logEntryType;
		}
	}

	// <summary>
	//   Returns the viewer ID of this Log Entry.
	// </summary>
	// <returns>
	//   The viewer ID of the Log Entry. It describes which viewer should
	//   be used in the Console when displaying the data of this Log Entry.
	//   Please see the ViewerId enum for more information.
	// </returns>

	public ViewerId getViewerId()
	{
		return this.fViewerId;
	}

	// <summary>
	//   Sets the viewer ID of the this Entry.
	// </summary>
	// <param name="viewerId">
	//   The new viewer ID of this Log Entry. Not allowed to be null.
	// </param>
	// <remarks>
	//   The viewer ID of the Log Entry describes which viewer should
	//   be used in the Console when displaying the data of this Log
	//   Entry. Please see the ViewerId enum for more information.
	//
	//   If the viewerId argument is a null reference a
	//   NullPointerException will be thrown.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The viewerId argument is null.
	// </table>
	// </exception>

	public void setViewerId(ViewerId viewerId)
	{
		if (viewerId == null)
		{
			throw new NullPointerException("viewerId");			
		}
		else 
		{
			this.fViewerId = viewerId;
		}
	}

	// <summary>
	//   Returns the background color of this Log Entry.
	// </summary>
	// <returns>
	//   The background color of the Log Entry or null if this Log
	//   Entry uses the default background color in the SmartInspect
	//   Console.
	// </returns>
	// <remarks>
	//   The background color of a Log Entry is normally set to the
	//   color of the session which sent this Log Entry.
	//
	//   This method can return null if this Log Entry uses the default
	//   background color in the SmartInspect Console.
	// </remarks>

	public Color getColor()
	{
		return this.fColor;
	}

	// <summary>
	//   Sets the background color of this Log Entry.
	// </summary>
	// <param name="color">
	//   The new background color of this Log Entry. Can be null.
	// </param>
	// <remarks>
	//   This Log Entry uses the default background color in the
	//   SmartInspect Console when this method gets a null reference as
	//   argument.
	// </remarks>

	public void setColor(Color color)
	{
		this.fColor = color;
	}

	// <summary>
	//   Returns the optional data block of the Log Entry.
	// </summary>
	// <returns>
	//   The optional data block of the Log Entry or null if this Log
	//   Entry does not contain additional data.
	// </returns>
	// <remarks>
	//   This method can return null if this Log Entry does not contain
	//   additional data.
	//
	//   <b>Important:</b> Treat the returned array as read-only. This
	//   means, modifying this array in any way is not supported.
	// </remarks>

	public byte[] getData()
	{
		return this.fData;
	}

	// <summary>
	//   Sets the optional data block of this Log Entry.
	// </summary>
	// <param name="data">
	//   The new data block of this Log Entry. Can be null.
	// </param>
	// <remarks>
	//   Because of the fact that the data block of a Log Entry is
	//   optional, it is allowed to pass a null reference.
	//
	//   <b>Important:</b> Treat the passed array as read-only. This
	//   means, modifying this array in any way after passing it to
	//   this method is not supported.
	// </remarks>

	public void setData(byte[] data)
	{
		this.fData = data;
	}

	// <summary>
	//   Returns the timestamp of this LogEntry object.
	// </summary>
	// <returns>
	//   The creation time of this Log Entry object as returned by the
	//   SmartInspect.now method.
	// </returns>
	
	public long getTimestamp()
	{
		return this.fTimestamp;
	}
	
	// <summary>
	//   Sets the timestamp of this LogEntry object.
	// </summary>
	// <param name="timestamp">
	//   The new timestamp of this LogEntry object. The passed value
	//   should represent the local date and time in microseconds since
	//   January 1, 1970. See SmartInspect.now for more information.
	// </param>

	public void setTimestamp(long timestamp)
	{
		this.fTimestamp = timestamp;
	}
	
	// <summary>
	//   Returns the process ID of this LogEntry object.
	// </summary>
	// <returns>
	//   The ID of the process this Log Entry object was created in.
	// </returns>

	public int getProcessId()
	{
		return this.fProcessId;
	}

	// <summary>
	//   Sets the process ID of this LogEntry object.
	// </summary>
	// <param name="processId">
	//   The new process ID of this Log Entry object.
	// </returns>

	public void setProcessId(int processId)
	{
		this.fProcessId = processId;
	}

	// <summary>
	//   Returns the thread ID of this LogEntry object.
	// </summary>
	// <returns>
	//   The ID of the thread this Log Entry object was created in.
	// </returns>

	public int getThreadId()
	{
		return this.fThreadId;
	}
	
	// <summary>
	//   Sets the thread ID of this LogEntry object.
	// </summary>
	// <param name="threadId">
	//   The new thread ID of this Log Entry object.
	// </returns>

	public void setThreadId(int threadId)
	{
		this.fThreadId = threadId;
	}
}
