/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.protocols;

import com.gurock.smartinspect.SmartInspect;
import java.io.OutputStream;

import com.gurock.smartinspect.connections.ConnectionsBuilder;
import com.gurock.smartinspect.formatters.BinaryFormatter;
import com.gurock.smartinspect.formatters.Formatter;
import com.gurock.smartinspect.formatters.TextFormatter;
import com.gurock.smartinspect.packets.Packet;
import com.gurock.smartinspect.packets.PacketQueue;

/**
 * Used for writing log data to memory and saving it to a stream or
 * protocol object on request.
 * <p>
 * This class is used for writing log data to memory. On request, this
 * data can be saved to a stream or protocol object. To initiate such
 * a request, use the internalDispatch method.
 * This class is used when the 'mem' protocol is specified in the
 * connections string of the {@link SmartInspect#setConnections}. Please see
 * the isValidOption method for a list of available options for this
 * protocol.
 * </p>
 * <p>
 * The public members of this class are threadsafe.
 * </p>
 */
public class MemoryProtocol extends Protocol {
	private static byte[] HEADER = "SILF".getBytes();

	private static byte[] BOM =
			new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

	private static final boolean DEFAULT_INDENT = false;
	private static final String DEFAULT_PATTERN =
			"[%timestamp%] %level%: %title%";

	private PacketQueue fQueue;
	private Formatter fFormatter;

	private boolean fIndent;
	private long fMaxSize;
	private boolean fAsText;
	private String fPattern;

	/**
	 * Creates and initializes a new MemoryProtocol instance. For a list of available memory protocol options,
	 * please refer to the isValidOption method.
	 */
	public MemoryProtocol() {
		loadOptions(); // Set default options
	}

	/**
	 * Overridden. Creates and initializes the packet queue.
	 *
	 * <p>This method creates and initializes a new packet queue with a maximum size as specified with the initialize method.
	 * For other valid options which might affect the behavior of this method and protocol, please see the isValidOption method.</p>
	 */
	protected void internalConnect() throws Exception {
		this.fQueue = new PacketQueue();
		this.fQueue.setBacklog(this.fMaxSize);
	}

	/**
	 * Overridden. Clears the internal queue of packets.
	 * This method does nothing more than to clear the internal queue of
	 * packets. After this method has been called, the internalDispatch
	 * method writes an empty log unless new packets are queued in the
	 * meantime.
	 */
	protected void internalDisconnect() throws Exception {
		if (this.fQueue != null) {
			this.fQueue.clear();
			this.fQueue = null;
		}
	}

	/**
	 * Overridden. Writes a packet to the packet queue.
	 * This method writes the supplied packet to the internal queue of
	 * packets. If the size of the queue exceeds the maximum size as
	 * specified with the setOptions method, the queue is automatically
	 * resized and older packets are discarded.
	 *
	 * @param packet The packet to write.
	 */
	protected void internalWritePacket(Packet packet) throws Exception {
		this.fQueue.push(packet);
	}

	/**
	 * Overrides and implements a custom action for saving the current
	 * queue of packets of this memory protocol to a stream or
	 * protocol object.
	 *
	 * <p>Depending on the supplied command argument, this method does
	 * the following. If the supplied state object of the protocol command is of type
	 * OutputStream, then this method uses this stream to write the entire content of the internal
	 * queue of packets. The necessary header is written first and then the actual packets are
	 * appended. The header and packet output format can be influenced with the "astext" protocol
	 * option (see isValidOption). If the "astext" option is true, the header is a UTF8 Byte Order
	 * Mark and the packets are written in plain text format. If the "astext" option is false, the
	 * header is the standard header for SmartInspect log files and the packets are written in the
	 * default binary mode. In the latter case, the resulting log files can be loaded by the
	 * SmartInspect Console.
	 *
	 * <p>The getAction method of the command argument should currently always return 0. If the
	 * state object is not a stream or protocol object or if the command argument is null, then
	 * this method does nothing.</p>
	 *
	 * @param command The protocol command which is expected to provide the stream or protocol object
	 * @throws Exception if writing the internal queue of packets to the supplied stream or protocol object fails
	 * @see com.gurock.smartinspect.protocols.Protocol#dispatch
	 * @see com.gurock.smartinspect.SmartInspect#dispatch
	 */
	protected void internalDispatch(ProtocolCommand command)
			throws Exception {
		if (command == null) {
			return;
		}

		Object state = command.getState();

		if (state == null) {
			return;
		}

		// Check if the supplied object is a stream
		if (state instanceof OutputStream) {
			OutputStream stream = (OutputStream) state;
			flushToStream(stream);
		} else {
			// Check if the supplied object is a protocol
			if (state instanceof Protocol) {
				Protocol protocol = (Protocol) state;
				flushToProtocol(protocol);
			}
		}
	}

	private void flushToStream(OutputStream stream)
			throws Exception {
		// Write the necessary file header
		if (this.fAsText) {
			stream.write(BOM, 0, BOM.length);
		} else {
			stream.write(HEADER, 0, HEADER.length);
		}

		// Write the current content of our queue
		Packet packet = this.fQueue.pop();
		while (packet != null) {
			this.fFormatter.format(packet, stream);
			packet = this.fQueue.pop();
		}
	}

	private void flushToProtocol(Protocol protocol)
			throws Exception {
		// Write the current content of our queue
		Packet packet = this.fQueue.pop();
		while (packet != null) {
			protocol.writePacket(packet);
			packet = this.fQueue.pop();
		}
	}

	/**
	 * Overridden. Returns "mem".
	 *
	 * @return Just "mem". Derived classes can change this behavior by
	 * overriding this method
	 */
	protected String getName() {
		return "mem";
	}

	private void initializeFormatter() {
		if (this.fAsText) {
			this.fFormatter = new TextFormatter();
			((TextFormatter) this.fFormatter).setPattern(this.fPattern);
			((TextFormatter) this.fFormatter).setIndent(this.fIndent);
		} else {
			this.fFormatter = new BinaryFormatter();
		}
	}

	/**
	 * Overridden. Fills a ConnectionsBuilder instance with the
	 * options currently used by this memory protocol.
	 *
	 * @param builder The ConnectionsBuilder object to fill with the current options
	 *                of this protocol
	 */
	protected void buildOptions(ConnectionsBuilder builder) {
		super.buildOptions(builder);
		builder.addOption("maxsize", (int) this.fMaxSize / 1024);
		builder.addOption("astext", this.fAsText);
		builder.addOption("indent", this.fIndent);
		builder.addOption("pattern", this.fPattern);
	}

	/**
	 * Overridden. Validates if a protocol option is supported.
	 * The following table lists all valid options, their default values and descriptions for this memory protocol.
	 * For a list of options common to all protocols, please have a look at the method of the parent class.
	 *
	 * <table border="1">
	 *     <caption>Memory protocol options</caption>
	 *     <thead>
	 *         <tr>
	 *             <th>Valid Options</th>
	 *             <th>Default Value</th>
	 *             <th>Description</th>
	 *         </tr>
	 *     </thead>
	 *     <tbody>
	 *         <tr>
	 *             <td>astext</td>
	 *             <td>false</td>
	 *             <td>Specifies if logging data should be written as text instead of binary.</td>
	 *         </tr>
	 *         <tr>
	 *             <td>indent</td>
	 *             <td>false</td>
	 *             <td>Indicates if the logging output should automatically be indented like in the Console if 'astext' is set to true.</td>
	 *         </tr>
	 *         <tr>
	 *             <td>maxsize</td>
	 *             <td>2048</td>
	 *             <td>Specifies the maxi size of the packet queue of this protocol in kilobytes. Specify size units like this: "1 MB". Supported units are "KB", "MB" and "GB".</td>
	 *         </tr>
	 *         <tr>
	 *             <td>pattern</td>
	 *             <td>"[%timestamp%] %level%: %title%"</td>
	 *             <td>Specifies the pattern used to create a text representation of a packet.</td>
	 *         </tr>
	 *     </tbody>
	 * </table>
	 * <p>
	 * If the "astext" option is used for creating a textual output instead of the default binary, the "pattern" string specifies the textual representation of a log packet. For detailed information of how a pattern string can look like, please have a look at the documentation of the PatternParser class, especially the PatternParser.setPattern method.
	 * <pre>
	 * Example Usage:
	 * SiAuto.si.setConnections("mem()")
	 * SiAuto.si.setConnections("mem(maxsize=\"8MB\")")
	 * SiAuto.si.setConnections("mem(astext=true)")
	 * </pre>
	 *
	 * @param name The option name to validate
	 * @return True if the option is supported and false otherwise
	 */
	protected boolean isValidOption(String name) {
		return
				name.equals("astext") ||
						name.equals("pattern") ||
						name.equals("maxsize") ||
						name.equals("indent") ||
						super.isValidOption(name);
	}

	/**
	 * Overridden. Loads and inspects memory specific options.
	 * <p>
	 * This method loads all relevant options and ensures their
	 * correctness. See isValidOption for a list of options which
	 * are recognized by the memory protocol.
	 */
	protected void loadOptions() {
		super.loadOptions();
		this.fMaxSize = getSizeOption("maxsize", 2048);
		this.fAsText = getBooleanOption("astext", false);
		this.fPattern = getStringOption("pattern", DEFAULT_PATTERN);
		this.fIndent = getBooleanOption("indent", DEFAULT_INDENT);
		initializeFormatter();
	}
}
