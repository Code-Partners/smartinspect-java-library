//
// <!-- Copyright (C) 2003-2010 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Is the abstract base class for all packets in the SmartInspect
//   Java library.
// </summary>
// <remarks>
//   This class is the base class for all packets in the SmartInspect
//   Java library. The following table lists the available packets
//   together with a short description.
//
//   <table>
//   Packet              Description
//   -                   -
//   ControlCommand      Responsible for administrative tasks like
//                         clearing the Console.
//
//   LogEntry            Represents the most important packet in the
//                         entire SmartInspect concept. Is used for
//                         the majority of logging methods in the
//                         Session class.
//
//   LogHeader           Responsible for storing and transferring
//                         log metadata. Used by the PipeProtocol and
//                         TcpProtocol classes to support the filter
//                         and trigger functionality of the
//                         SmartInspect Router service application.
//
//   ProcessFlow         Responsible for managing thread and process
//                         information about your application.
//
//   Watch               Responsible for handling variable watches. 
//   </table>
// </remarks>
// <threadsafety>
//   This class and sub-classes are not guaranteed to be threadsafe.
//   To ensure thread-safety, use setThreadSafe as well as the lock and
//   unlock methods.
// </threadsafety>

public abstract class Packet
{
	private Level fLevel;
	private int fBytes;
	private boolean fLocked;
	private Object fLock;
	private boolean fThreadSafe;
	protected static final int PACKET_HEADER = 6;

	// <summary>
	//   Creates and initializes a Packet instance with a default log
	//   level of Level.Message.
	// </summary>

	public Packet()
	{
		this.fLevel = Level.Message;
	}
	
	// <summary>
	//   Returns the amount of bytes needed for storing this packet
	//   in the standard SmartInspect binary log file format as
	//   represented by BinaryFormatter.
	// </summary>
	// <returns>
	//   The amount of bytes needed for storing this packet in the
	//   standard SmartInspect binary log file format.
	// </returns>
	// <remarks>
	//   Please note that this method is only intended to be used by
	//   a possible Java implementation of the SmartInspect SDK. The
	//   SmartInspect SDK is a small library for reading SmartInspect
	//   binary log files and is available for download on the Gurock
	//   Software website.
	// </remarks>

	public int getBytes()
	{
		return this.fBytes;
	}
	
	// <summary>
	//   Sets the amount of bytes needed for storing this packet
	//   in the standard SmartInspect binary log file format as
	//   represented by BinaryFormatter.
	// </summary>
	// <param name="bytes">
	//   The amount of bytes needed for storing this packet in the
	//   standard SmartInspect binary log file format.
	// </param>
	// <remarks>
	//   Please note that this method is only intended to be used by
	//   a possible Java implementation of the SmartInspect SDK. The
	//   SmartInspect SDK is a small library for reading SmartInspect
	//   binary log files and is available for download on the Gurock
	//   Software website.
	// </remarks>	
	
	public void setBytes(int bytes)
	{
		this.fBytes = bytes;
	}
	
	// <summary>
	//   Returns the ID of the current thread.
	// </summary>
	// <returns>The ID the current thread.</returns>
	// <remarks>
	//   This method is intended to be used by derived packet classes
	//   which make use of a thread ID.
	// </remarks>
	
	protected int getThreadId()
	{
		return Thread.currentThread().hashCode();
	}

	// <summary>
	//   Returns the memory size occupied by a string.
	// </summary>
	// <param name="s">
	//   The string whose memory size to return. Can be null.
	// </param>
	// <returns>
	//   The memory size occupied by the supplied string or 0 if the
	//   supplied argument is null.
	// </returns>
	// <remarks>
	//   This method calculates and returns the total memory size
	//   occupied by the supplied string. If the supplied argument
	//   is null, 0 is returned.
	// </remarks>
	
	protected int getStringSize(String s)
	{
		if (s != null)	
		{
			return s.length() * 2;
		}
		else 
		{
			return 0;
		}
	}

	// <summary>
	//   Returns the log level of this packet.
	// </summary>
	// <returns>The log level of this packet.</returns>
	// <remarks>
	//   Every packet can have a certain log level value. Log levels
	//   describe the severity of a packet. Please see the Level
	//   enum for more information about log levels and their usage.
	// </remarks>
	
	public Level getLevel()
	{
		return this.fLevel;
	}
	
	// <summary>
	//   Sets the log level of this packet.
	// </summary>
	// <param name="level">
	//   The new log level of this packet. Not allowed to be null.
	// </param>
	// <remarks>
	//   Every packet can have a certain log level value. Log levels
	//   describe the severity of a packet. Please see the Level
	//   enum for more information about log levels and their usage.
	//
	//   If the level argument is a null reference a NullPointerException
	//   will be thrown.
	// </remarks>	
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The level argument is null.
	// </table>
	// </exception>
	
	public void setLevel(Level level)
	{
		if (level == null)
		{
			throw new NullPointerException("level argument is null");			
		}
		else 
		{
			this.fLevel = level;
		}
	}
	
	// <summary>
	//   Calculates and returns the total memory size occupied by a
	//   packet.
	// </summary>
	// <returns>
	//   The total memory size occupied by this packet.
	// </returns>
	// <remarks>
	//   This method returns the total occupied memory size of this packet.
	//   This functionality is used by the <link Protocol.isValidOption, 
	//   backlog> protocol feature to calculate the total backlog queue
	//   size.
	// </remarks>

	public abstract int getSize();
	
	// <summary>
	//   Intended to return the type of the packet.
	// </summary>
	// <returns>
	//   The type of this packet. Please see the PacketType type for a
	//   list of available packet types.
	// </returns>
		
	public abstract PacketType getPacketType();
	
	// <summary>
	//   Locks this packet for safe multi-threaded packet processing
	//   if this packet is operating in thread-safe mode.
	// </summary>
	// <remarks>
	//   You need to call this method before reading or changing
	//   properties of a packet when using this packet from multiple
	//   threads at the	same time. This is needed, for example, when
	//   one or more <link SmartInspect.setConnections, connections>
	//   of a SmartInspect object are told to operate in
	//   <link Protocol.isValidOption, asynchronous protocol mode>.
	//   Each lock call must be matched by a call to unlock.
	//
	//   Before using lock and unlock in a multi-threaded environment
	//   you must indicate that this packet should operate in
	//   thread-safe mode by setting the setThreadSafe method to true.
	//   Otherwise, the lock and unlock methods do nothing. Note
	//   that setting the setThreadSafe method is done automatically
	//   if this packet has been created by the Session class and is
	//   processed by a related SmartInspect object which has one or
	//   more connections which operate in asynchronous protocol
	//   mode.
	// </remarks>
	
	public void lock()
	{
		if (this.fThreadSafe)
		{
			synchronized (this.fLock)
			{
				while (this.fLocked)
				{
					try 
					{
						this.fLock.wait();
					}
					catch (InterruptedException e)
					{
						/* Ignore */
					}
				}	
				
				this.fLocked = true;
			}
		}
	}
	
	// <summary>
	//   Unlocks a previously locked packet.
	// </summary>
	// <remarks>
	//   You need to call this method after reading or changing
	//   properties of a packet when using this packet from multiple
	//   threads at the same time. This is needed, for example, when
	//   one or more <link SmartInspect.setConnections, connections>
	//   of a SmartInspect object are told to operate in
	//   <link Protocol.isValidOption, asynchronous protocol mode>.
	//   Each unlock call must be matched by a previous call to lock.
	//
	//   Before using lock and unlock in a multi-threaded environment
	//   you must indicate that this packet should operate in
	//   thread-safe mode by setting the setThreadSafe method to true.
	//   Otherwise, the lock and unlock methods do nothing. Note
	//   that setting the setThreadSafe method is done automatically
	//   if this packet has been created by the Session class and is
	//   processed by a related SmartInspect object which has one or
	//   more connections which operate in asynchronous protocol
	//   mode.
	// </remarks>
	
	public void unlock()
	{
		if (this.fThreadSafe)
		{
			synchronized (this.fLock)
			{
				this.fLocked = false;
				this.fLock.notify(); /* One is enough */
			}
		}
	}
	
	// <summary>
	//   Indicates if this packet is used in a multi-threaded
	//   SmartInspect environment.
	// </summary>
	// <returns>
	//   True if this packet is used in a multi-threaded SmartInspect
	//   environment and false otherwise. 
	// </returns>
	// <remarks>
	//   If this method returns true, the lock and unlock methods 
	//   lock this packet for safe multi-threaded access. Otherwise,
	//   the lock and unlock methods do nothing. Please see the
	//   corresponding setThreadSafe method for more information.
	// </remarks>

	public boolean isThreadSafe()	
	{
		return this.fThreadSafe;
	}
	
	// <summary>
	//   Specifies if this packet is used in a multi-threaded
	//   SmartInspect environment.
	// </summary>
	// <param name="threadSafe">
	//   True if this packet is used in a multi-threaded SmartInspect
	//   environment and false otherwise. 
	// </param>
	// <remarks>
	//   Set this method to true before calling lock and unlock
	//   in a multi-threaded environment. Otherwise, the lock and
	//   unlock methods do nothing. Note that setting this
	//   method is done automatically if this packet has been
	//   created by the Session class and is processed by a related
	//   SmartInspect object which has one or more connections which
	//   operate in asynchronous protocol mode.
	// 
	//   Setting this method must be done before using this packet
	//   from multiple threads simultaneously.
	// </remarks>
	
	public void setThreadSafe(boolean threadSafe)
	{
		if (threadSafe == this.fThreadSafe)
		{
			return;
		}
		
		this.fThreadSafe = threadSafe;
		
		if (threadSafe)
		{
			this.fLock = new Object();
		}		
		else 
		{
			this.fLock = null;
		}
	}
}
