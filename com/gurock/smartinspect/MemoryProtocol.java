//
// <!-- Copyright (C) Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

import java.io.OutputStream;

// <summary>
//   Used for writing log data to memory and saving it to a stream or
//   protocol object on request.
// </summary>
// <remarks>
//   This class is used for writing log data to memory. On request this
//   data can be saved to a stream or protocol object. To initiate such
//   a request, use the internalDispatch method.
// 
//   This class is used when the 'mem' protocol is specified in the
//   <link SmartInspect.setConnections, connections string>. Please see
//   the isValidOption method for a list of available options for this
//   protocol.
// </remarks>
// <threadsafety>
//   The public members of this class are threadsafe.
// </threadsafety>

public class MemoryProtocol extends Protocol 
{
	private static byte[] HEADER = "SILF".getBytes();
	
	private static byte[] BOM = 
		new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

	private static final boolean DEFAULT_INDENT = false;
	private static final String DEFAULT_PATTERN = 
		"[%timestamp%] %level%: %title%";

	private PacketQueue fQueue;
	private Formatter fFormatter;
	
	private boolean fIndent;
	private long fMaxSize;
	private boolean fAsText;
	private String fPattern;
	
	// <summary>
	//   Creates and initializes a new MemoryProtocol instance. For a
	//   list of available memory protocol options, please refer to the
	//   isValidOption method.
	// </summary>
	
	public MemoryProtocol()
	{
		loadOptions(); // Set default options
	}
	
	// <summary>
	//   Overridden. Creates and initializes the packet queue.
	// </summary>
	// <remarks>
	//   This method creates and initializes a new packet queue with
	//   a maximum size as specified with the initialize method. For
	//   other valid options which might affect the behavior of this
	//   method and protocol, please see the isValidOption method.
	// </remarks>

	protected void internalConnect() throws Exception 
	{
		this.fQueue = new PacketQueue();
		this.fQueue.setBacklog(this.fMaxSize);
	}

	// <summary>
	//   Overridden. Clears the internal queue of packets.
	// </summary>
	// <remarks>
	//   This method does nothing more than to clear the internal queue of
	//   packets. After this method has been called, the internalDispatch
	//   method writes an empty log unless new packets are queued in the
	//   meantime.
	// </remarks>

	protected void internalDisconnect() throws Exception 
	{
		if (this.fQueue != null)
		{
			this.fQueue.clear();
			this.fQueue = null;
		}
	}

	// <summary>
	//   Overridden. Writes a packet to the packet queue.
	// </summary>
	// <param name="packet">The packet to write.</param>
	// <remarks>
	//   This method writes the supplied packet to the internal queue of
	//   packets. If the size of the queue exceeds the maximum size as
	//   specified with the setOptions method, the queue is automatically
	//   resized and older packets are discarded.
	// </remarks>

	protected void internalWritePacket(Packet packet) throws Exception 
	{
		this.fQueue.push(packet);
	}

	// <summary>
	//   Overridden. Implements a custom action for saving the current
	//   queue of packets of this memory protocol to a stream or
	//   protocol object.
	// </summary>
	// <param name="command">
	//   The protocol command which is expected to provide the stream
	//   or protocol object.
	// </param>
	// <seealso cref="com.gurock.smartinspect.Protocol.dispatch"/>
	// <seealso cref="com.gurock.smartinspect.SmartInspect.dispatch"/>
	// <remarks>
	//   Depending on the supplied command argument, this method does
	//   the following.
	// 
	//   If the supplied state object of the protocol command is of type
	//   OutputStream, then this method uses this stream to write the
	//   entire content of the internal queue of packets. The necessary
	//   header is written first and then the actual packets are
	//   appended.
	// 
	//   The header and packet output format can be influenced with the
	//   "astext" protocol option (see isValidOption). If the "astext"
	//   option is true, the header is a UTF8 Byte Order Mark and the
	//   packets are written in plain text format. If the "astext" option
	//   is false, the header is the standard header for SmartInspect log
	//   files and the packets are written in the default binary mode. In
	//   the latter case, the resulting log files can be loaded by the
	//   SmartInspect Console.
	// 
	//   The getAction method of the command argument should currently
	//   always return 0. If the state object is not a stream or 
	//   protocol object or if the command argument is null, then this
	//   method does nothing.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type      Condition
	//   -                   -
	//   Exception           Writing the internal queue of packets to
	//                         the supplied stream or protocol object
	//                         failed.
	// </table>
	// </exception>

	protected void internalDispatch(ProtocolCommand command) 
		throws Exception
	{
		if (command == null)
		{
			return;
		}
		
		Object state = command.getState();
	
		if (state == null)
		{
			return;
		}
		
		// Check if the supplied object is a stream
		if (state instanceof OutputStream)
		{
			OutputStream stream = (OutputStream) state;
			flushToStream(stream);
		}
		else 
		{
			// Check if the supplied object is a protocol
			if (state instanceof Protocol)
			{
				Protocol protocol = (Protocol) state;
				flushToProtocol(protocol);
			}
		}
	}
	
	private void flushToStream(OutputStream stream)
		throws Exception
	{
		// Write the necessary file header
		if (this.fAsText)
		{
			stream.write(BOM, 0, BOM.length);
		}
		else 
		{
			stream.write(HEADER, 0, HEADER.length);
		}

		// Write the current content of our queue
		Packet packet = this.fQueue.pop();
		while (packet != null)
		{
			this.fFormatter.format(packet, stream);
			packet = this.fQueue.pop();
		}
	}
	
	private void flushToProtocol(Protocol protocol)
		throws Exception
	{
		// Write the current content of our queue
		Packet packet = this.fQueue.pop();
		while (packet != null)
		{
			protocol.writePacket(packet);
			packet = this.fQueue.pop();
		}
	}
	
	// <summary>
	//   Overridden. Returns "mem".
	// </summary>
	// <returns>
	//   Just "mem". Derived classes can change this behavior by
	//   overriding this method.
	// </returns>
		
	protected String getName()
	{
		return "mem";
	}
	
	private void initializeFormatter()
	{
		if (this.fAsText)
		{
			this.fFormatter = new TextFormatter();
			((TextFormatter) this.fFormatter).setPattern(this.fPattern);
			((TextFormatter) this.fFormatter).setIndent(this.fIndent);
		}
		else 
		{
			this.fFormatter = new BinaryFormatter();
		}
	}
	
	// <summary>
	//   Overridden. Fills a ConnectionsBuilder instance with the
	//   options currently used by this memory protocol.
	// </summary>
	// <param name="builder">
	//   The ConnectionsBuilder object to fill with the current options
	//   of this protocol.
	// </param>

	protected void buildOptions(ConnectionsBuilder builder)
	{
		super.buildOptions(builder);
		builder.addOption("maxsize", (int) this.fMaxSize / 1024);
		builder.addOption("astext", this.fAsText);
		builder.addOption("indent", this.fIndent);
		builder.addOption("pattern", this.fPattern);
	}
	
	// <summary>
	//   Overridden. Validates if a protocol option is supported.
	// </summary>
	// <param name="name">The option name to validate.</param>
	// <returns>
	//   True if the option is supported and false otherwise.
	// </returns>
	// <remarks>
	//   The following table lists all valid options, their default values
	//   and descriptions for this memory protocol. For a list of options
	//   common to all protocols, please have a look at the
	//   <link Protocol.isValidOption, isValidOption> method of the parent
	//   class.
	//
	//   <table>
	//   Valid Options  Default Value                     Description
	//   -              -                                 -
	//   astext         false                             Specifies if logging
	//                                                     data should be
	//                                                     written as text
	//                                                     instead of binary.
	//
	//   indent         false                             Indicates if the
	//                                                     logging output
	//                                                     should
	//                                                     automatically be
	//                                                     indented like in
	//                                                     the Console if
	//                                                     'astext' is set
	//                                                     to true.
	//
	//   maxsize        2048                              Specifies the
	//                                                     maximum size of the
	//                                                     packet queue of this
	//                                                     protocol in
	//                                                     kilobytes. Specify
	//                                                     size units like
	//                                                     this: "1 MB".
	//                                                     Supported units are
	//                                                     "KB", "MB" and "GB".
	// 
	//   pattern        "[%timestamp%] %level%: %title%"  Specifies the pattern
	//                                                     used to create a
	//                                                     text representation
	//                                                     of a packet.
	//   </table>
	// 
	//   If the "astext" option is used for creating a textual output instead
	//   of the default binary, the "pattern" string specifies the textual
	//   representation of a log packet. For detailed information of how a
	//   pattern string can look like, please have a look at the documentation
	//   of the PatternParser class, especially the PatternParser.setPattern
	//   method.
	// </remarks>
	// <example>
	// <code>
	// SiAuto.si.setConnections("mem()");
	// SiAuto.si.setConnections("mem(maxsize=\\"8MB\\")");
	// SiAuto.si.setConnections("mem(astext=true)");
	// </code>
	// </example>
	
	protected boolean isValidOption(String name)
	{
		return 
			name.equals("astext") ||
			name.equals("pattern") ||
			name.equals("maxsize") ||
			name.equals("indent") ||
			super.isValidOption(name);
	}
	
	// <summary>
	//   Overridden. Loads and inspects memory specific options.
	// </summary>
	// <remarks>
	//   This method loads all relevant options and ensures their
	//   correctness. See isValidOption for a list of options which
	//   are recognized by the memory protocol.
	// </remarks>

	protected void loadOptions()
	{
		super.loadOptions();
		this.fMaxSize = getSizeOption("maxsize", 2048);
		this.fAsText = getBooleanOption("astext", false);
		this.fPattern = getStringOption("pattern", DEFAULT_PATTERN);
		this.fIndent = getBooleanOption("indent", DEFAULT_INDENT);
		initializeFormatter();
	}
}
