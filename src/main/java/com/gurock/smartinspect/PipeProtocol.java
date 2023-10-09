/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Used for sending packets to a local SmartInspect Console over a
 * named pipe connection.
 * <p>
 * This class is used for sending packets through a local named pipe
 * to the SmartInspect Console. It is used when the 'pipe' protocol
 * is specified in the connections string. Please see the isValidOption method for a
 * list of available protocol options. Please note that this protocol
 * can only be used for local connections. For remote connections to
 * other machines, please use TcpProtocol.
 * <p>
 * The public members of this class are threadsafe.
 */
public class PipeProtocol extends Protocol {
	private static final int BUFFER_SIZE = 0x2000;
	private static final byte[] CLIENT_BANNER = ("SmartInspect Java Library v" + SmartInspect.getVersion() + "\n").getBytes();

	private String fPipeName;
	private Formatter fFormatter;
	private OutputStream fOstream;
	private InputStream fIstream;

	/**
	 * Creates and initializes a PipeProtocol instance. For a list
	 * of available pipe protocol options, please refer to the
	 * isValidOption method.
	 */
	public PipeProtocol() {
		this.fFormatter = new BinaryFormatter();
		loadOptions();
	}

	/**
	 * Overridden. Returns "pipe".
	 */
	protected String getName() {
		return "pipe";
	}

	private static void doHandShake(InputStream is, OutputStream os) throws IOException, SmartInspectException {
		int n;

		// Read the server banner from the Console. 
		while ((n = is.read()) != '\n') {
			if (n == -1) {
				// This indicates a failure on the server
				// side. Doesn't make sense to proceed here.

				throw new SmartInspectException("Could not read server banner correctly: " + "Connection has been closed unexpectedly");
			}
		}

		// And write ours in return!
		os.write(CLIENT_BANNER, 0, CLIENT_BANNER.length);
		os.flush();
	}

	/**
	 * Overridden. Validates if a protocol option is supported.
	 * <p>
	 * The following table lists all valid options, their default
	 * values and descriptions for the pipe protocol.
	 *
	 * <table border="1">
	 *     <thead>
	 *         <tr>
	 *             <th>Valid Options</th>
	 *             <th>Default Value</th>
	 *             <th>Description</th>
	 *         </tr>
	 *     </thead>
	 *     <tbody>
	 *         <tr>
	 *             <td>pipename</td>
	 *             <td>"smartinspect"</td>
	 *             <td>Specifies the named pipe for sending log packets to the SmartInspect Console.</td>
	 *         </tr>
	 *     </tbody>
	 * </table>
	 * <p>
	 *  For further options which affect the behavior of this
	 *  protocol, please have a look at the documentation of the
	 *  {@link Protocol#isValidOption} method of the
	 *  parent class.
	 *
	 * <p>
	 * Example:
	 * <pre>
	 * SiAuto.Si.setConnections("pipe()");
	 * SiAuto.Si.setConnections("pipe(pipename=\\"logging\\")");
	 * </pre>
	 *
	 * @param name The option name to validate.
	 * @return True if the option is supported and false otherwise.
	 */
	protected boolean isValidOption(String name) {
		return name.equals("pipename") || super.isValidOption(name);
	}

	/**
	 * Overridden. Fills a ConnectionsBuilder instance with the
	 * options currently used by this pipe protocol.
	 *
	 * @param builder The ConnectionsBuilder object to fill with the current options
	 *                of this protocol
	 */
	protected void buildOptions(ConnectionsBuilder builder) {
		super.buildOptions(builder);
		builder.addOption("pipename", this.fPipeName);
	}

	/**
	 * Overridden. Loads and inspects pipe specific options.
	 * <p>
	 * This method loads all relevant options and ensures their correctness. See isValidOption for a list of options which
	 * are recognized by the pipe protocol.
	 */
	protected void loadOptions() {
		super.loadOptions();
		this.fPipeName = getStringOption("pipename", "smartinspect");
	}

	/**
	 * This method tries to establish a connection to a local named
	 * pipe of a SmartInspect Console. The name of the pipe can be
	 * specified by passing the "pipename" option to the initialize
	 * method.
	 *
	 * <p>Overridden. Connects to the specified local named pipe.
	 *
	 * @throws Exception if establishing the named pipe connection failed
	 */
	protected void internalConnect() throws Exception {
		PipeHandle handle = new PipeHandle(this.fPipeName);
		this.fIstream = new PipeInputStream(handle);
		this.fOstream = new BufferedOutputStream(new PipeOutputStream(handle), BUFFER_SIZE);
		doHandShake(this.fIstream, this.fOstream);
		internalWriteLogHeader(); /* Write the log header */
	}

	/**
	 * Overridden. Sends a packet to the Console.
	 * This method sends the supplied packet to the SmartInspect
	 * Console over the previously established named pipe connection.
	 *
	 * @param packet The packet to write
	 * @throws Exception if sending the packet to the Console failed
	 */
	protected void internalWritePacket(Packet packet) throws Exception {
		this.fFormatter.format(packet, this.fOstream);
		this.fOstream.flush();
	}

	/**
	 * Overridden. Closes the connection to the specified local named pipe.
	 * <p>
	 * This method closes the named pipe handle if previously created
	 * and disposes any supplemental objects.
	 *
	 * @throws Exception If closing the named pipe handle failed
	 */
	protected void internalDisconnect() throws Exception {
		if (this.fIstream != null) {
			try {
				this.fIstream.close();
			} finally {
				this.fIstream = null;
			}
		}

		if (this.fOstream != null) {
			try {
				this.fOstream.close();
			} finally {
				this.fOstream = null;
			}
		}
	}
}
