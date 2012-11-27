//
// <!-- Copyright (C) Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// <summary>
//   Is the abstract base class for a protocol. A protocol is responsible
//   for transporting packets.
// </summary>
// <remarks>
//   A protocol is responsible for the transport of packets. This base
//   class offers all necessary methods to handle the protocol options
//   and it declares several abstract protocol specific methods for
//   handling protocol destinations like connecting or writing packets.
//
//   The following table lists the available protocols together with
//   their identifier in the <link SmartInspect.setConnections,
//   connections string> and a short description.
//
//   <table>
//   Protocol        Identifier    Description
//   -               -             -
//   FileProtocol    "file"        Used for writing log files in the
//                                  standard SmartInspect binary log
//                                  file format which can be loaded
//                                  into the Console.
//
//   MemoryProtocol  "mem"         Used for writing log data to memory
//                                  and saving it to a stream on
//                                  request.
//
//   PipeProtocol    "pipe"        Used for sending log data over a
//                                  named pipe directly to a local
//                                  Console.
//
//   TcpProtocol     "tcp"         Used for sending packets over a TCP
//                                  connection directly to the Console.
//
//   TextProtocol    "text"        Used for writing log files in a
//                                  customizable text format. Best
//                                  suited for end-user notification
//                                  purposes.
//   </table>
//
//   There are several options which are <link isValidOption,
//   common to all protocols> and beyond that each protocol has its own
//   set of additional options. For those protocol specific options,
//   please refer to the documentation of the corresponding protocol
//   class. Protocol options can be set with Initialize and derived
//   classes can query option values using the Get methods.
// </remarks>
// <threadsafety>
//   The public members of this class are threadsafe.
// </threadsafety>

public abstract class Protocol
{
	/* Options */
	private Level fLevel;	
	private boolean fReconnect;
	private long fReconnectInterval;
	private String fCaption;	
	private boolean fBacklogEnabled;
	private long fBacklogQueue;
	private Level fBacklogFlushOn;
	private boolean fBacklogKeepOpen;
	private boolean fAsyncEnabled;
	private long fAsyncQueue;
	private boolean fAsyncThrottle;
	private boolean fAsyncClearOnDisconnect;
	
	/* Internal data */
	private String fAppName;
	private String fHostName;
	private long fReconnectTickCount;
	private Set fListeners;
	private boolean fKeepOpen;
	private boolean fFailed;
	private LookupTable fOptions;
	private PacketQueue fQueue;
	private boolean fConnected;
	private boolean fInitialized;
	private Object fLock;
	private Scheduler fScheduler;

	// <summary>
	//   Creates and initializes a Protocol subclass instance. For a
	//   list of protocol options common to all protocols, please refer
	//   to the isValidOption method.
	// </summary>

	public Protocol()
	{
		this.fLock = new Object();
		this.fOptions = new LookupTable();
		this.fQueue = new PacketQueue();
		this.fListeners = new HashSet();
	}

	private void createOptions(String options) throws SmartInspectException
	{
		try
		{
			OptionsParser parser = new OptionsParser();
			OptionsParserListener listener =
				new OptionsParserListener()
				{
					public void onOption(OptionsParserEvent e) 
						throws SmartInspectException
					{
						addOption(e.getProtocol(), e.getKey(), e.getValue());
					}
				};
			parser.parse(getName(), options, listener);
		}
		catch (SmartInspectException e)
		{
			removeOptions();
			throw e;
		}
	}

	private void addOption(String protocol, String key, String value) 
		throws SmartInspectException
	{
		if (mapOption(key, value))
		{
			return;
		}
			
		if (!isValidOption(key))
		{
			// The option isn't supported by the protocol.
			throw new SmartInspectException(
				"Option \"{0}\" is not available for protocol \"{1}\"",
				new Object[] {key, protocol}
			);
		}

		this.fOptions.put(key, value);
	}
	
	private boolean mapOption(String key, String value)
	{
		/* This method is for backwards compatibility. In older
		 * SmartInspect versions the backlog options didn't have
		 * 'backlog.' prefix. This has been changed in version
		 * 3.0. This method does the mapping between the old and
		 * the new backlog options. */

		if (key.equals("backlog"))
		{
			this.fOptions.put(key, value);
			long backlog = this.fOptions.getSizeValue("backlog", 0);

			if (backlog > 0)
			{
				this.fOptions.add("backlog.enabled", "true");
				this.fOptions.add("backlog.queue", value);
			}
			else 
			{
				this.fOptions.add("backlog.enabled", "false");
				this.fOptions.add("backlog.queue", "0");
			}

			return true;
		}

		if (key.equals("flushon"))
		{
			this.fOptions.put(key, value);
			this.fOptions.add("backlog.flushon", value);
			return true;
		}

		if (key.equals("keepopen"))
		{
			this.fOptions.put(key, value);
			this.fOptions.add("backlog.keepopen", value);
			return true;
		}

		return false;
	}
	
	private void removeOptions()
	{
		this.fOptions.clear();
	}

	// <summary>
	//   Handles a protocol exception.
	// </summary>
	// <param name="message">The exception message.</param>		
	// <remarks>
	//   This method handles an occurred protocol exception. It
	//   first sets the Failed flag and creates a ProtocolException
	//   object with the name and options of this protocol. In
	//   normal blocking mode (see IsValidOption), it then throws
	//   this exception. When operating in asynchronous mode,
	//   it invokes the Error event handlers instead and does not
	//   throw an exception.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   ProtocolException      Always in normal blocking mode.
	//                           Never in asynchronous mode.
	// </table>
	// </exception>

	protected void handleException(String message) 
		throws ProtocolException
	{
		/* Indicate that the last operation has failed*/
		this.fFailed = true;
		
		ProtocolException e = new ProtocolException(message);
		
		/* Fill the exception with the protocol details. */		
		e.setProtocolName(getName());
		e.setProtocolOptions(getOptions());
		
		if (this.fAsyncEnabled)
		{
			doError(e); /* Notify event handlers */
		}
		else 
		{
			throw e;
		}		
	}

	// <summary>
	//   Fills a ConnectionsBuilder instance with the options currently
	//   used by this protocol.
	// </summary>
	// <param name="builder">
	//   The ConnectionsBuilder object to fill with the current options
	//   of this protocol.
	// </param>
	// <remarks>
	//   The filled options string consists of key, value option pairs
	//   separated by commas.
	// 
	//   This function takes care of the options <link isValidOption,
	//   common to all protocols>. To include protocol specific options,
	//   override this function.
	// </remarks>

	protected void buildOptions(ConnectionsBuilder builder)
	{
		/* Asynchronous options */

		builder.addOption("async.enabled", this.fAsyncEnabled);
		builder.addOption("async.clearondisconnect",
			this.fAsyncClearOnDisconnect);
		builder.addOption("async.queue", (int) this.fAsyncQueue / 1024);
		builder.addOption("async.throttle", this.fAsyncThrottle);

		/* Backlog options */

		builder.addOption("backlog.enabled", this.fBacklogEnabled);
		builder.addOption("backlog.flushon", this.fBacklogFlushOn);
		builder.addOption("backlog.keepopen", this.fBacklogKeepOpen);
		builder.addOption("backlog.queue", 
			(int) this.fBacklogQueue / 1024);

		/* General options */

		builder.addOption("level", this.fLevel);
		builder.addOption("caption", this.fCaption);
		builder.addOption("reconnect", this.fReconnect);
		builder.addOption("reconnect.interval", 
			(int) this.fReconnectInterval);
	}

	private String getOptions()
	{
		ConnectionsBuilder builder = new ConnectionsBuilder();
		buildOptions(builder);
		return builder.getConnections();
	}

	// <summary>
	//   Sets and initializes the options of this protocol.
	// </summary>
	// <param name="options">The new protocol options.</param>
	// <remarks>
	//   This method expects an options string which consists of key,
	//   value pairs separated by commas like this:
	//   "filename=log.sil, append=true". To use a comma in a value,
	//   you can use quotation marks like in the following example:
	//   "filename=\\"log.sil\\", append=true".
	//
	//   Please note that a SmartInspectException exception is thrown
	//   if a wrong options string is assigned. A wrong options string
	//   could use an invalid syntax or contain one or more unknown
	//   option keys. This method can be called only once. Further
	//   calls have no effect. Pass null or an empty string to use the
	//   default options of a particular protocol.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type            Condition
	//   -                         -
	//   SmartInspectException     Invalid options syntax, an unknown
	//                               option key.
	// </table>
	// </exception>

	public void initialize(String options) throws SmartInspectException
	{
		synchronized (this.fLock)
		{
			if (!this.fInitialized)
			{
				if (options != null)
				{
					createOptions(options);
				}
				
				loadOptions();
				this.fInitialized = true;
			}
		}
	}

	// <summary>
	//   Gets the string of a key.
	// </summary>
	// <param name="key">The key to search for.</param>
	// <param name="defaultValue">
	//   The value to return if the key does not exist. Note that this
	//   method can throw an exception of type NullPointerException if
	//   you pass a null reference as key.
	// </param>
	// <returns>
	//   Either the value if the key exists or defaultValue otherwise.
	// </returns>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key argument is null.
	// </table>
	// </exception>

	protected String getStringOption(String key, String defaultValue)
	{
		return this.fOptions.getStringValue(key, defaultValue);
	}

	// <summary>
	//   Gets the integer value of a key.
	// </summary>
	// <param name="key">The key to search for.</param>
	// <param name="defaultValue">
	//   The value to return if the key does not exist.
	// </param>
	// <returns>
	//   Either the value if the key exists and is a valid integer or
	//   defaultValue otherwise.
	// </returns>
	// <remarks>
	//   Please note that if a related value could be found but is not
	//   a valid integer, the supplied default value will be returned.
	//   Only non-negative integers will be recognized as valid values.
	//   Note that this method can throw an exception of type
	//   NullPointerException if you pass a null reference as key.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key argument is null.
	// </table>
	// </exception>

	protected int getIntegerOption(String key, int defaultValue)
	{
		return this.fOptions.getIntegerValue(key, defaultValue);
	}

	// <summary>
	//   Gets the boolean value of a key.
	// </summary>
	// <param name="key">The key to search for.</param>
	// <param name="defaultValue">
	//   The value to return if the key does not exist.
	// </param>
	// <returns>
	//   Either the value if the key exists or defaultValue otherwise.
	// </returns>
	// <remarks>
	//   A boolean value will be treated as true if the value of the
	//   key matches either "true", "yes" or "1" and as false
	//   otherwise. Note that this method can throw an exception of
	//   type NullPointerException if you pass a null reference as
	//   key.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key argument is null.
	// </table>
	// </exception>

	protected boolean getBooleanOption(String key, boolean defaultValue)
	{
		return this.fOptions.getBooleanValue(key, defaultValue);
	}

	// <summary>
	//   Gets a Level value of a key.
	// </summary>
	// <param name="key">The key whose value to return.</param>
	// <param name="defaultValue">
	//   The value to return if the given key is unknown.
	// </param>
	// <returns>
	//   Either the value converted to the corresponding Level value for
	//   the given key if an element with the given key exists and the
	//   found value is a valid Level value or defaultValue otherwise.
	// </returns>
	// <remarks>
	//   This method returns the defaultValue argument if either the
	//   supplied key is unknown or the found value is not a valid Level
	//   value. Please see the Level enum for more information on the
	//   available values. 	Note that this method can throw an exception
	//   of type  NullPointerException if you pass a null reference as
	//   key.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key argument is null.
	// </table>
	// </exception>

	protected Level getLevelOption(String key, Level defaultValue)
	{
		return this.fOptions.getLevelValue(key, defaultValue);
	}

	// <summary>
	//   Gets an integer value of a key. The integer value is
	//   interpreted as a byte size and it is supported to specify
	//   byte units.
	// </summary>
	// <param name="key">The key whose value to return.</param>
	// <param name="defaultValue">
	//   The value to return if the given key is unknown.
	// </param>
	// <returns>
	//   Either the value converted to an integer for the given key if
	//   an element with the given key exists and the found value is a
	//   valid integer or defaultValue otherwise.
	// </returns>
	// <remarks>
	//   This method returns the defaultValue argument if either the
	//   supplied key is unknown or the found value is not a valid
	//   integer or ends with an unknown byte unit. Only non-negative
	//   integer values are recognized as valid.
	//
	//   It is possible to specify a size unit at the end of the value.
	//   If a known unit is found, this function multiplies the
	//   resulting value with the corresponding factor. For example, if
	//   the value of the element is "1KB", the return value of this
	//   function would be 1024.
	//
	//   The following table lists the available units together with a
	//   short description and the corresponding factor.
	//
	//   <table>
	//   Unit Name  Description  Factor
	//   -          -            -
	//   KB         Kilo Byte    1024
	//   MB         Mega Byte    1024^2
	//   GB         Giga Byte    1024^3
	//   </table>
	//
	//   If no unit is specified, this function defaults to the KB
	//   unit. Note that this method can throw an exception of type
	//   NullPointerException if you pass a null reference as key.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key argument is null.
	// </table>
	// </exception>

	protected long getSizeOption(String key, long defaultValue)
	{
		return this.fOptions.getSizeValue(key, defaultValue);
	}

	// <summary>
	//   Gets an integer value of a key. The integer value is
	//   interpreted as a time span and it is supported to specify
	//   time span units.
	// </summary>
	// <param name="key">The key whose value to return.</param>
	// <param name="defaultValue">
	//   The value to return if the given key is unknown.
	// </param>
	// <returns>
	//   Either the value converted to an integer for the given key if
	//   an element with the given key exists and the found value is a
	//   valid integer or defaultValue otherwise. The value is returned
	//   in milliseconds.
	// </returns>
	// <remarks>
	//   This method returns the defaultValue argument if either the
	//   supplied key is unknown or the found value is not a valid
	//   integer or ends with an unknown time span unit.
	//
	//   It is possible to specify a time span unit at the end of the
	//   value. If a known unit is found, this function multiplies the
	//   resulting value with the corresponding factor. For example, if
	//   the value of the element is "1s", the return value of this
	//   function would be 1000.
	//
	//   The following table lists the available units together with a
	//   short description and the corresponding factor.
	//
	//   <table>
	//   Unit Name  Description  Factor
	//   -          -            -
	//   s          Seconds      1000
	//   m          Minutes      60*s
	//   h          Hours        60*m
	//   d          Days         24*h
	//   </table>
	//
	//   If no unit is specified, this function defaults to the Seconds
	//   unit. Please note that the value is always returned in
	//   milliseconds.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type          Condition
	//   -                       -
	//   NullPointerException    The key argument is null.
	// </table>
	// </exception>
	
	protected long getTimespanOption(String key, long defaultValue)
	{
		return this.fOptions.getTimespanValue(key, defaultValue);
	}
	
	// <summary>
	//   Gets a FileRotate value of a key.
	// </summary>
	// <param name="key">The key whose value to return.</param>
	// <param name="defaultValue">
	//   The value to return if the given key is unknown.
	// </param>
	// <returns>
	//   Either the value converted to a FileRotate value for the given
	//   key if an element with the given key exists and the found value
	//   is a valid FileRotate or defaultValue otherwise. Note that
	//   this method can throw an exception of type NullPointerException
	//   if you pass a null reference as key.
	// </returns>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key argument is null.
	// </table>
	// </exception>

	protected FileRotate getRotateOption(String key, 
		FileRotate defaultValue)
	{
		return this.fOptions.getRotateValue(key, defaultValue);
	}
	
	// <summary>
	//   Gets the byte array value of a key.
	// </summary>
	// <param name="key">The key whose value to return.</param>
	// <param name="size">
	//   The desired size in bytes of the returned byte array. If
	//   the element value does not have the expected size, it is
	//   shortened or padded automatically.
	// </param>
	// <param name="defaultValue">
	//   The value to return if the given key is unknown or if the
	//   found value has an invalid format.
	// </param>
	// <returns>
	//   Either the value converted to a byte array for the given key
	//   if an element with the given key exists and the found value
	//   has a valid format or defaultValue otherwise.
	// </returns>
	// <remarks>
	//   The returned byte array always has the desired length as
	//   specified by the size argument. If the element value does
	//   not have the required size after conversion, it is shortened
	//   or padded (with zeros) automatically. This method returns
	//   the defaultValue argument if either the supplied key is
	//   unknown or the found value does not have a valid format
	//   (e.g. invalid characters when using hexadecimal strings).
	//
	//   Note that this method can throw an exception of type
	//   NullPointerException if you pass a null reference as key.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key argument is null.
	// </table>
	// </exception>
	
	public byte[] getBytesOption(String key, int size,
		byte[] defaultValue)
	{
		return this.fOptions.getBytesValue(key, size, defaultValue);
	}
	
	// <summary>
	//   Validates if a option is supported by this protocol.
	// </summary>
	// <param name="name">The option name to validate.</param>
	// <returns>
	//   True if the option is supported and false otherwise.
	// </returns>
	// <remarks>
	//   The following table lists all valid options, their default
	//   values and descriptions common to all protocols. See below
	//   for explanations.
	//
	//   <table>
	//   Option Name              Default   Description
	//   -                        -         -
	//   level                    debug     Specifies the log level of
	//                                       this protocol.
	//
	//   reconnect                false     Specifies if a reconnect
	//                                       should be initiated when a
	//                                       connection gets dropped.
	// 
	//   reconnect.interval       0         If reconnecting is enabled,
	//                                       specifies the minimum time
	//                                       in seconds between two
	//                                       successive reconnect
	//                                       attempts. If 0 is specified,
	//                                       a reconnect attempt is
	//                                       initiated for each packet
	//                                       if needed. It is possible to
	//                                       specify time span units like
	//                                       this: "1s". Supported units
	//                                       are "s" (seconds), "m"
	//                                       (minutes), "h" (hours) and
	//                                       "d" (days).
	//
	//   caption                  [name]    Specifies the caption of
	//                                       this protocol as used by
	//                                       SmartInspect.Dispatch.
	//                                       By default, it is set to
	//                                       the protocol identifier
	//                                       (e.g., "file" or "mem").
	// 
	//   async.enabled            false     Specifies if this protocol
	//                                       should operate in
	//                                       asynchronous instead of the
	//                                       default blocking mode.
	//  
	//   async.queue              2048      Specifies the maximum size
	//                                       of the asynchronous queue in
	//                                       kilobytes. It is possible
	//                                       to specify size units like
	//                                       this: "1 MB". Supported
	//                                       units are "KB", "MB" and
	//                                       "GB".
	// 
	//   async.throttle           true      Specifies if the application
	//                                       should be automatically
	//                                       throttled in asynchronous
	//                                       mode when more data is
	//                                       logged than the queue can
	//                                       handle.
	// 
	//   async.clearondisconnect  false     Specifies if the current
	//                                       content of the asynchronous
	//                                       queue should be discarded
	//                                       before disconnecting. Useful
	//                                       if an application must not
	//                                       wait for the logging to
	//                                       complete before exiting.
	// 
	//   backlog.enabled          false     Enables the backlog feature
	//                                       (see below).
	// 
	//   backlog.queue            2048      Specifies the maximum size
	//                                       of the backlog queue in
	//                                       kilobytes. It is possible
	//                                       to specify size units like
	//                                       this: "1 MB". Supported
	//                                       units are "KB", "MB" and
	//                                       "GB".
	// 
	//   backlog.flushon          error     Specifies the flush level for
	//                                       the backlog functionality.
	//
	//   backlog.keepopen         false     Specifies if the connection
	//                                       should be kept open between
	//                                       two successive writes when
	//                                       the backlog feature is used.
	//   </table>
	//
	//   With the log level of a protocol you can limit the amount of
	//   data being logged by excluding packets which don't have a
	//   certain minimum log level. For example, if you set the level
	//   to "message", all packets with a log level of "debug" or
	//   "verbose" are ignored. For a complete list of available log
	//   level values, please see the documentation of the Level enum.
	//
	//   The caption option specifies the caption for this protocol
	//   as used by the SmartInspect.Dispatch method. This method
	//   can send and initiate custom protocol actions and the caption
	//   is used to lookup the requested connection. By default, the
	//   caption is set to the identifier of a protocol (e.g., "file"
	//   or "mem"). For more information about the dispatching of
	//   custom protocol actions, please refer to the documentation of
	//   the Dispatch and SmartInspect.Dispatch methods.
	// 
	//   If the backlog option is enabled, all packets whose log level
	//   is less than the flushon level and equal to or higher than the
	//   general log level of a protocol, will be written to a queue
	//   rather than directly to the protocol specific destination. When
	//   a packet arrives with a log level of at least the same value
	//   as the flushon option, the current content of the queue is
	//   written. The total amount of memory occupied by this queue
	//   can be set with the queue option. If the packet queue has
	//   been filled up with packets and a new packet is about to be
	//   stored, old packets are discarded.
	//
	//   As an example, if the backlog queue is set to "2 MB" and the
	//   flushon level to "error", all packets with a log level less
	//   than error are written to a queue first. By specifying a queue
	//   option of "2 MB", the baclog queue is set to a maximum memory
	//   size of 2 megabyte. Now, when a packet with a log level of
	//   error arrives, the current content of the queue and then the
	//   error itself are written.
	//
	//   With the keepopen option of the backlog feature you can specify
	//   if a connection should be kept open between two successive
	//   writes. When keepopen is set to false, a connection is only
	//   available during the actual write / flush. A connection is
	//   thus only created when absolutely necessary.
	//
	//   A protocol can either operate in normal blocking (the default)
	//   or in asynchronous mode. In blocking mode, the operations of
	//   this protocol (Connect, Disconnect, Dispatch and WritePacket)
	//   are executed synchronously and block the caller until they are
	//   done. In asynchronous mode, these operations are not executed
	//   directly but scheduled for execution in a different thread 
	//   and return immediately. Asynchronous logging can increase the
	//   logging performance and reduce the blocking of applications.
	//   
	//   When operating in asynchronous mode, this protocol uses a
	//   queue to buffer the logging data. The total amount of memory
	//   occupied by this queue can be set with the queue option. The
	//   throttle option specifies if an application should be
	//   automatically throttled in asynchronous mode when more data
	//   is logged / generated than the queue can handle. If this
	//   option is disabled and the queue is currently full, old
	//   packets are discarded when new data is logged. The throttle
	//   option ensures that no logging data is lost but can be disabled
	//   if logging performance is critical.
	// 
	//   With the clearondisconnect option, you can specify if the
	//   current content of the asynchronous queue should be discarded
	//   before disconnecting. This can be useful if an application must
	//   not wait for the logging to complete before exiting.
	// 
	//   The reconnect option allows a protocol to reconnect
	//   automatically before a packet is being written. A reconnect
	//   might be necessary if a working connection has been unexpectedly
	//   disconnected or could not be established in the first place.
	//   Possible errors during a reconnect attempt will silently be
	//   ignored and not reported.
	//
	//   Please note that the reconnect functionality causes a protocol
	//   by default to initiate a connection attempt for every packet
	//   until a connection has been successfully (re-) established.
	//   This can be a very time consuming process, especially when
	//   using a protocol which requires a complex connection process
	//   like <link TcpProtocol, TCP>, for example. This can slow down
	//   the logging performance. When using the reconnect option, it
	//   is thus recommended to also enable asynchronous logging to not
	//   block the application or to specify a reconnect interval to
	//   minimize the reconnect attempts.
	// </remarks>

	protected boolean isValidOption(String name)
	{
		return 
			name.equals("caption") ||
			name.equals("level") ||
			name.equals("reconnect") ||
			name.equals("reconnect.interval") ||
			name.equals("backlog.enabled") ||
			name.equals("backlog.flushon") ||
			name.equals("backlog.keepopen") ||
			name.equals("backlog.queue") ||
			name.equals("async.enabled") ||
			name.equals("async.queue") ||
			name.equals("async.throttle") ||
			name.equals("async.clearondisconnect");
	}

	// <summary>
	//   Loads and inspects protocol specific options.
	// </summary>
	// <remarks>
	//   This method is intended to give real protocol implementations
	//   the opportunity to load and inspect options. This method will
	//   be called automatically when the options have been changed.
	//   The default implementation of this method takes care of the
	//   options <link isValidOption, common to all protocols> and
	//   should thus always be called by derived classes which override
	//   this method.
	// </remarks>

	protected void loadOptions()
	{
		/* General protocol options */

		this.fLevel = getLevelOption("level", Level.Debug);
		this.fCaption = getStringOption("caption", getName());
		this.fReconnect = getBooleanOption("reconnect", false);
		this.fReconnectInterval = 
			getTimespanOption("reconnect.interval", 0);

		/* Backlog protocol options */

		this.fBacklogEnabled = getBooleanOption("backlog.enabled",
			false);
		this.fBacklogQueue = getSizeOption("backlog.queue", 2048);
		this.fBacklogFlushOn = getLevelOption("backlog.flushon",
			Level.Error);
		this.fBacklogKeepOpen = getBooleanOption("backlog.keepopen",
			false);

		this.fQueue.setBacklog(this.fBacklogQueue);
		this.fKeepOpen = !this.fBacklogEnabled || this.fBacklogKeepOpen;

		/* Asynchronous protocol options */

		this.fAsyncEnabled = getBooleanOption("async.enabled", false);
		this.fAsyncThrottle = getBooleanOption("async.throttle", true);
		this.fAsyncQueue = getSizeOption("async.queue", 2048);
		this.fAsyncClearOnDisconnect = 
			getBooleanOption("async.clearondisconnect", false);	
	}

	// <summary>
	//   Resets the protocol and brings it into a consistent state.
	// </summary>
	// <remarks>
	//   This method resets the current protocol state by clearing the
	//   internal queue of packets, setting the connected status to
	//   false and calling the abstract internalDisconnect method of a
	//   real protocol implementation to cleanup any protocol specific
	//   resources.
	// </remarks>
	
	protected void reset() throws Exception
	{
		this.fConnected = false;
		this.fQueue.clear();
		try 
		{
			internalDisconnect();
		}
		finally 
		{
			this.fReconnectTickCount = System.currentTimeMillis();
		}
	}
	
	private void startScheduler()
	{
		this.fScheduler = new Scheduler(this);
		this.fScheduler.setThreshold(this.fAsyncQueue);
		this.fScheduler.setThrottle(this.fAsyncThrottle);
		this.fScheduler.start();
	}
	
	private void stopScheduler()
	{
		this.fScheduler.stop();
		this.fScheduler = null;
	}
	
	// <summary>
	//   Connects to the protocol destination.
	// </summary>
	// <remarks>
	//   This method initiates a protocol specific connection attempt.
	//   The behavior of real implementations of this method can often
	//   be changed by setting protocol options with the initialize
	//   method. This method is always called in a threadsafe and
	//   exception-safe context.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   Exception              Connecting to the destination failed.
	// </table>
	// </exception>
	
	protected abstract void internalConnect() throws Exception;

	protected void implConnect() throws ProtocolException
	{
		if (!this.fConnected && this.fKeepOpen)
		{
			try 
			{
				try 
				{
					internalConnect();
					this.fConnected = true;
					this.fFailed = false; /* Success */
				}
				catch (Exception e)
				{
					reset();
					throw e;
				}
			}
			catch (Exception e)
			{
				handleException(e.getMessage());
			}
		}
	}
	
	private void scheduleConnect()
	{
		SchedulerCommand command = new SchedulerCommand();
		command.setAction(SchedulerAction.Connect);
		this.fScheduler.schedule(command);
	}
	
	// <summary>
	//   Connects to the protocol destination.
	// </summary>
	// <remarks>
	//   In normal blocking mode (see isValidOption), this method
	//   does nothing more than to verify that the protocol is not
	//   already connected and does not use the <link isValidOption,
	//   keepopen backlog feature> and then calls the abstract
	//   protocol specific internalConnect method in a threadsafe
	//   and exception-safe context.
	// 
	//   When operating in asynchronous mode instead, this method
	//   schedules a connect operation for asynchronous execution
	//   and returns immediately. Please note that possible
	//   exceptions which occur during the eventually executed
	//   connect are not thrown directly but reported with the
	//   <link ProtocolListener.onError, error event>.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   ProtocolException      Connecting to the destination failed.
	//                           Can only occur when operating in
	//                           normal blocking mode. In asynchronous
	//                           mode, the error event is used for
	//                           reporting exceptions instead.
	// </table>
	// </exception>

	public void connect() throws ProtocolException
	{
		synchronized (this.fLock)
		{
			if (this.fAsyncEnabled)
			{
				if (this.fScheduler != null)
				{
					return; /* Already running */
				}
				
				startScheduler();
				scheduleConnect();
			}
			else 
			{
				implConnect();
			}
		}
	}
	
	// <summary>
	//   Reconnects to the protocol specific destination.
	// </summary>
	// <returns>
	//   True if the reconnect attempt has been successful and false
	//   otherwise.
	// </returns>
	// <remarks>
	//   This method initiates a protocol specific reconnect attempt.
	//   The behavior of real method implementations can often be
	//   changed by setting protocol options with initialize. This
	//   method is always called in a threadsafe and exception-safe
	//   context.
	//
	//   The default implementation simply calls the protocol specific
	//   internalConnect method. Derived classes can change this behavior
	//   by overriding this method. 
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   Exception              Reconnecting to the destination failed.
	// </table>
	// </exception>
	
	protected boolean internalReconnect() throws Exception
	{
		internalConnect();
		return true;
	}
	
	private void reconnect()
	{
		if (this.fReconnectInterval > 0)		
		{
			long tickCount = System.currentTimeMillis();
			if (tickCount - this.fReconnectTickCount
				< this.fReconnectInterval)
			{
				return; /* Reconnect interval has not been reached */
			}
		}
		
		try 
		{
			if (internalReconnect())
			{
				this.fConnected = true;
			}
		}
		catch (Exception e)
		{
			/* Reconnect exceptions are not reported, but we
			/* need to record that the last connection attempt
			 * has failed (see below). */
		}

		this.fFailed = !this.fConnected;

		if (this.fFailed)
		{
			try 
			{
				reset();
			}
			catch (Exception e)
			{
				/* Ignored */
			}
		}
	}
	
	// <summary>
	//   Disconnects from the protocol destination.
	// </summary>
	// <remarks>
	//   This method is intended for real protocol implementations
	//   to disconnect from the protocol specific source. This
	//   could be closing a file or disconnecting a TCP socket, for
	//   example. This method is always called in a threadsafe and
	//   exception-safe context.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type    Condition
	//   -                 -
	//   Exception         Disconnecting from the destination failed.
	// </table>
	// </exception>
	
	protected abstract void internalDisconnect() throws Exception;

	protected void implDisconnect() throws ProtocolException
	{
		if (this.fConnected)
		{
			try 
			{
				reset();
			}
			catch (Exception e)
			{
				handleException(e.getMessage());
			}
		}
		else 
		{
			this.fQueue.clear();
		}
	}

	private void scheduleDisconnect()
	{
		SchedulerCommand command = new SchedulerCommand();
		command.setAction(SchedulerAction.Disconnect);
		this.fScheduler.schedule(command);
	}

	// <summary>
	//   Disconnects from the protocol destination.
	// </summary>
	// <remarks>
	//   In normal blocking mode (see isValidOption), this method
	//   checks if this protocol has a working connection and then
	//   calls the protocol specific internalDisconnect method in a
	//   threadsafe and exception-safe context.
	// 
	//   When operating in asynchronous mode instead, this method
	//   schedules a disconnect operation for asynchronous execution
	//   and then blocks until the internal protocol thread is done.
	//   Please note that possible exceptions which occur during
	//   the eventually executed disconnect are not thrown directly
	//   but reported with the <link ProtocolListener.onError,
	//   error event>.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type       Condition
	//   -                    -
	//   ProtocolException    Disconnecting from the destination
	//                         failed. Can only occur when operating
	//                         in normal blocking mode. In asynchronous
	//                         mode, the error event is used for
	//                         reporting exceptions instead.
	// </table>
	// </exception>

	public void disconnect() throws ProtocolException
	{
		synchronized (this.fLock)
		{
			if (this.fAsyncEnabled)
			{
				if (this.fScheduler == null)
				{
					return; /* Not running */
				}
				
				if (this.fAsyncClearOnDisconnect)
				{
					this.fScheduler.clear();
				}
				
				scheduleDisconnect();
				stopScheduler();
			}
			else 
			{
				implDisconnect();
			}
		}
	}

	protected void internalWriteLogHeader() throws Exception
	{
		LogHeader logHeader = new LogHeader();
		logHeader.setHostName(this.fHostName);
		logHeader.setAppName(this.fAppName);
		internalWritePacket(logHeader);
	}
	
	// <summary>
	//   Writes a packet to the protocol destination.
	// </summary>
	// <param name="packet">The packet to write.</param>
	// <remarks>
	//   This method is intended for real protocol implementations
	//   to write the supplied packet to the protocol specific
	//   destination. This method is always called in a threadsafe
	//   and exception-safe context.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type      Condition
	//   -                   -
	//   Exception           Writing the packet to the destination
	//                        failed.
	// </table>
	// </exception>
	
	protected abstract void internalWritePacket(Packet packet) 
		throws Exception;

	protected void implWritePacket(Packet packet) 
		throws ProtocolException
	{
		if (!this.fConnected && !this.fReconnect && this.fKeepOpen)
		{
			return;
		}
		
		Level level = packet.getLevel();
			
		try 
		{
			try 
			{
				boolean skip = false;
				
				if (this.fBacklogEnabled)
				{
					if (level.greaterEqual(this.fBacklogFlushOn) &&
						level != Level.Control)
					{
						flushQueue();
					}
					else 
					{
						this.fQueue.push(packet);
						skip = true;
					}
				}
				
				if (!skip)
				{
					forwardPacket(packet, !this.fKeepOpen);
				}
			}
			catch (Exception e)
			{
				reset();
				throw e;
			}
		}			
		catch (Exception e)
		{
			handleException(e.getMessage());
		}
	}		

	private void flushQueue() throws Exception
	{
		Packet packet = this.fQueue.pop();
		while (packet != null)
		{
			forwardPacket(packet, false);
			packet = this.fQueue.pop();
		}
	}
	
	private void forwardPacket(Packet packet, boolean disconnect) 
		throws Exception
	{
		if (!this.fConnected)
		{
			if (!this.fKeepOpen)
			{
				internalConnect();
				this.fConnected = true;
				this.fFailed = false; /* Success */
			}
			else 
			{
				reconnect();
			}
		}
		
		if (this.fConnected)
		{
			packet.lock();
			try
			{
				internalWritePacket(packet);
			}
			finally 
			{
				packet.unlock();
			}
			
			if (disconnect)
			{
				this.fConnected = false;
				internalDisconnect();
			}
		}
	}
		
	private void scheduleWritePacket(Packet packet)
	{
		SchedulerCommand command = new SchedulerCommand();
		command.setAction(SchedulerAction.WritePacket);
		command.setState(packet);
		this.fScheduler.schedule(command);
	}
	
	// <summary>
	//   Writes a packet to the protocol specific destination.
	// </summary>
	// <param name="packet">The packet to write.</param>
	// <remarks>
	//   This method first checks if the log level of the supplied
	//   packet is sufficient to be logged. If this is not the
	//   case, this method returns immediately.
	// 
	//   Otherwise, in normal blocking mode (see isValidOption),
	//   this method verifies that this protocol is successfully
	//   connected and then writes the supplied packet to the
	//   <link isValidOption, backlog queue> or passes it directly
	//   to the protocol specific destination by calling the
	//   InternalWritePacket method. Calling internalWritePacket
	//   is always done in a threadsafe and exception-safe way.
	// 
	//   When operating in asynchronous mode instead, this method
	//   schedules a write operation for asynchronous execution and
	//   returns immediately. Please note that possible exceptions
	//   which occur during the eventually executed write are not
	//   thrown directly but reported with the 
	//   <link ProtocolListener.onError, error event>.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type        Condition
	//   -                     -
	//   ProtocolException     Writing the packet to the destination
	//                          failed. Can only occur when operating
	//                          in normal blocking mode. In
	//                          asynchronous mode, the error event is
	//                          used for reporting exceptions instead.
	// </table>
	// </exception>

	public void writePacket(Packet packet) throws ProtocolException
	{		
		synchronized (this.fLock)
		{
			if (packet.getLevel().less(this.fLevel))
			{
				return;
			}
			
			if (this.fAsyncEnabled)
			{
				if (this.fScheduler == null)
				{
					return; /* Not running */
				}
				
				scheduleWritePacket(packet);
			}			
			else 
			{
				implWritePacket(packet);
			}
		}
	}
	
	// <summary>
	//   Specifies the name of a real protocol implementation.
	// </summary>
	// <returns>
	//   The name of a real protocol implementation.
	// </returns>
	// <remarks>
	//   Real implementations should return a meaningful name which
	//   represents the protocol. For example, the FileProtocol returns
	//   "file", the TcpProtocol "tcp" and the TextProtocol "text".
	// </remarks>

	protected abstract String getName();
	
	// <summary>
	//   Executes a protocol specific custom action.
	// </summary>
	// <param name="command">
	//   The protocol command which provides protocol specific
	//   information about the custom action.
	// </param>
	// <seealso cref="com.gurock.smartinspect.SmartInspect.dispatch"/>
	// <remarks>
	//   The default implementation does nothing. Derived protocol
	//   implementations can override this method to add custom actions.
	//   Please see the MemoryProtocol.internalDispatch method for an
	//   example. This method is always called in a threadsafe and
	//   exception-safe way.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   Exception              Executing the custom action failed.
	// </table>
	// </exception>
	
	protected void internalDispatch(ProtocolCommand command) 
		throws Exception
	{
		// Empty by default
	}

	protected void implDispatch(ProtocolCommand command) 
		throws ProtocolException
	{
		if (this.fConnected)
		{
			try 
			{
				internalDispatch(command);
			}
			catch (Exception e)
			{
				handleException(e.getMessage());
			}
		}
	}
	
	private void scheduleDispatch(ProtocolCommand cmd)
	{
		SchedulerCommand command = new SchedulerCommand();
		command.setAction(SchedulerAction.Dispatch);
		command.setState(cmd);
		this.fScheduler.schedule(command);
	}
	
	// <summary>
	//   Dispatches a custom action to a concrete implementation of
	//   a protocol.
	// </summary>
	// <param name="command">
	//   The protocol command object which provides protocol specific
	//   information about the custom action. Can be null.
	// </param>
	// <seealso cref="com.gurock.smartinspect.SmartInspect.dispatch"/>
	// <remarks>
	//   In normal blocking mode (see isValidOption), this method
	//   does nothing more than to call the protocol specific
	//   internalDispatch method with the supplied command argument
	//   in a threadsafe and exception-safe way. Please note that
	//   this method dispatches the custom action only if the protocol
	//   is currently connected.
	// 
	//   When operating in asynchronous mode instead, this method
	//   schedules a dispatch operation for asynchronous execution
	//   and returns immediately. Please note that possible
	//   exceptions which occur during the eventually executed
	//   dispatch are not thrown directly but reported with the
	//   <link ProtocolListener.onError, error event>.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type       Condition
	//   -                    -
	//   ProtocolException    An exception occurred in the custom
	//                         action. Can only occur when operating
	//                         in normal blocking mode. In
	//                         asynchronous mode, the error event is
	//                         used for reporting exceptions instead.
	// </table>
	// </exception>
	
	public void dispatch(ProtocolCommand command) 
		throws ProtocolException
	{
		synchronized (this.fLock)
		{
			if (this.fAsyncEnabled)
			{
				if (this.fScheduler == null)
				{
					return; /* Not running */
				}
				
				scheduleDispatch(command);
			}
			else 
			{
				implDispatch(command);
			}
		}
	}
	
	// <summary>
	//   Returns the caption of this protocol.
	// </summary>
	// <returns>
	//   The caption of this protocol as used by the SmartInspect.dispatch
	//   method.
	// </returns>
	// <remarks>
	//   The caption is used in the SmartInspect.dispatch method to lookup
	//   the requested connection. The caption can be set with initialize.
	//   If you use only one connection at once or does not use the
	//   SmartInspect.dispatch method, the caption option can safely be
	//   ignored.
	// 
	//   For more information, please refer to the documentation of the
	//   dispatch and SmartInspect.dispatch methods.
	// </remarks>

	public String getCaption()
	{
		return this.fCaption;
	}

	// <summary>
	//   Disconnects from the protocol destination.
	// </summary>
	// <remarks>
	//   In normal blocking mode (see isValidOption), this method
	//   checks if this protocol has a working connection and then
	//   calls the protocol specific internalDisconnect method in a
	//   threadsafe and exception-safe context.
	// 
	//   When operating in asynchronous mode instead, this method
	//   schedules a disconnect operation for asynchronous execution
	//   and then blocks until the internal protocol thread is done.
	//   Please note that possible exceptions which occur during
	//   the eventually executed disconnect are not thrown directly
	//   but reported with the <link ProtocolListener.onError,
	//   error event>.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type       Condition
	//   -                    -
	//   ProtocolException    Disconnecting from the destination
	//                         failed. Can only occur when operating
	//                         in normal blocking mode. In asynchronous
	//                         mode, the error event is used for
	//                         reporting exceptions instead.
	// </table>
	// </exception>
	
	public void dispose() throws ProtocolException
	{
		disconnect();
	}
	
	// <summary>
	//   Returns if the last executed connection-related operation of
	//   this protocol has failed. Indicates if the next operation is
	//   likely to block.
	// </summary>
	// <returns>
	//   True if the last executed connection-related operation of
	//   this protocol has failed and false otherwise.
	// </returns>
	
	public boolean failed()
	{
		return this.fFailed;
	}
	
	// <summary>
	//   Adds a new listener for the events of this object.
	// </summary>
	// <param name="listener">The listener to add.</param>
	// <remarks>
	//   This methods adds a new listener for the events of this
	//   Protocol object. This can be useful to get informed about
	//   possible protocol errors. Please see the ProtocolListener
	//   interface for details. Also see the documentation of the
	//   ProtocolAdapter class which simplifies the event handling.
	//   Note that the error event is only used in combination with
	//   asynchronous logging (please see isValidOption for more
	//   information). In normal blocking mode, exceptions are
	//   reported by throwing.
	// </remarks>
	
	public void addListener(ProtocolListener listener)
	{
		if (listener != null)
		{
			synchronized (this.fListeners)
			{
				this.fListeners.add(listener);
			}
		}
	}
	
	// <summary>
	//   Removes a new listener for the events of this object.
	// </summary>
	// <param name="listener">The listener to add.</param>
	// <remarks>
	//   This method removes the supplied listener from the event
	//   system of this object. After the listener has been
	//   removed, it will no longer be notified about any events
	//   of this object. Note that the error event is only used
	//   in combination with asynchronous logging (please see
	//   isValidOption for more information). In normal blocking
	//   mode, exceptions are reported by throwing.
	// </remarks>

	public void removeListener(ProtocolListener listener)
	{
		if (listener != null)
		{
			synchronized (this.fListeners)
			{
				this.fListeners.remove(listener);
			}
		}
	}

	// <summary>
	//   Invokes the ProtocolListener.onError event handlers.
	// </summary>
	// <param name="ex">The occurred exception.</param>
	// <remarks>
	//   Derived classes can override this method to intercept the
	//   ProtocolListener.onError event. Note that the error event
	//   is only used in combination with asynchronous logging
	//   (please see isValidOption for more information). In normal
	//   blocking mode, exceptions are reported by throwing.
	// </remarks>
	
	protected void doError(Exception ex)
	{
		synchronized (this.fListeners)
		{
			if (!this.fListeners.isEmpty())
			{
				Iterator it = this.fListeners.iterator();
				ErrorEvent e = new ErrorEvent(this, ex);

				while (it.hasNext())
				{
					((ProtocolListener) it.next()).onError(e);
				}
			}
		}
	}
	
	// <summary>
	//   Indicates if this protocol is operating in asynchronous
	//   protocol mode.
	// </summary>
	// <returns>
	//   True if this protocol is operating in asynchronous protocol
	//   mode and false otherwise.
	// </returns>
	// <remarks>
	//   If this method returns true, this protocol is operating
	//   in asynchronous protocol mode. Otherwise, it returns false.
	//   Asynchronous protocol mode can be enabled with the
	//   initialize method. Also see isValidOption for information
	//   on asynchronous logging and how to enable it.
	// </remarks>
	
	protected boolean isAsynchronous()
	{
		return this.fAsyncEnabled;
	}
	
	// <summary>
	//   Returns the hostname of this protocol.
	// </summary>
	// <returns>The hostname of this protocol.</returns>
	// <remarks>
	//   The hostname of a protocol is usually set to the name of
	//   the machine this protocol is created in. The hostname can
	//   be used to write LogHeader packets after a successful
	//   protocol connect.
	// </remarks>

	public String getHostName()
	{
		return this.fHostName;
	}

	// <summary>
	//   Sets the hostname of this protocol.
	// </summary>
	// <param name="hostName">The new hostname.</param>
	// <remarks>
	//   The hostname of a protocol is usually set to the name of
	//   the machine this protocol is created in. The hostname can
	//   be used to write LogHeader packets after a successful
	//   protocol connect.
	// </remarks>

	public void setHostName(String hostName)
	{
		this.fHostName = hostName;
	}

	// <summary>
	//   Returns the application name of this protocol.
	// </summary>
	// <returns>The application name of this protocol.</returns>
	// <remarks>
	//   The application name of a protocol is usually set to the
	//   name of the application this protocol is created in. The
	//   application name can be used to write LogHeader packets
	//   after a successful protocol connect.
	// </remarks>

	public String getAppName()
	{
		return this.fAppName;
	}

	// <summary>
	//   Sets the application name of this protocol.
	// </summary>
	// <param name="appName">
	//   The new application name.
	// </param>
	// <remarks>
	//   The application name of a protocol is usually set to the
	//   name of the application this protocol is created in. The
	//   application name can be used to write LogHeader packets
	//   after a successful protocol connect.
	// </remarks>
	
	public void setAppName(String appName)
	{
		this.fAppName = appName;
	}
}
