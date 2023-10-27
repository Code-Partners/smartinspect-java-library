/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.protocols;

import com.gurock.smartinspect.SmartInspect;
import com.gurock.smartinspect.SmartInspectException;
import com.gurock.smartinspect.connections.ConnectionsBuilder;
import com.gurock.smartinspect.formatters.BinaryFormatter;
import com.gurock.smartinspect.formatters.Formatter;
import com.gurock.smartinspect.packets.LogHeader;
import com.gurock.smartinspect.packets.Packet;
import com.gurock.smartinspect.packets.logentry.LogEntry;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

/**
 * Used for sending packets to the SmartInspect Console over a TCP socket connection.
 * <p>
 * This class is used for sending packets over a TCP connection to the Console.
 * It is used when the 'tcp' protocol is specified in the {@link SmartInspect#setConnections} connections string.
 * Please see the isValidOption method for a list of available protocol options.
 * <p>
 * Thread Safety:
 * The public members of this class are threadsafe.
 */
public class TcpProtocol extends Protocol {
	private static final Logger logger = Logger.getLogger(TcpProtocol.class.getName());

	private static final int BUFFER_SIZE = 0x2000;
	private static final byte[] CLIENT_BANNER =
			("SmartInspect Java Library v" + SmartInspect.getVersion() +
					"\n").getBytes();

	private static final int ANSWER_BUFFER_SIZE = 0x2000;

	private Socket fSocket;
	protected InputStream fIstream;
	protected OutputStream fOstream;
	protected Formatter fFormatter;
	private byte[] fAnswer;

	protected String fHostName = "127.0.0.1";
	protected int fTimeout = 30000;
	protected int fPort = 4228;

	/**
	 * Creates and initializes a TcpProtocol instance. For a list of
	 * available TCP protocol options, please refer to the isValidOption
	 * method.
	 */
	public TcpProtocol() {
		this.fAnswer = new byte[ANSWER_BUFFER_SIZE];
		this.fFormatter = new BinaryFormatter();
		loadOptions(); // Set default options
	}

	/**
	 * Overridden. Returns "tcp".
	 *
	 * @return Just "tcp"
	 */
	protected String getName() {
		return "tcp";
	}

	/**
	 * Validates if a protocol option is supported. Overridden method.
	 * <p>
	 * The following table lists all valid options, their default values and descriptions for the TCP protocol:
	 *
	 * <table border="1">
	 *     <caption>Tcp protocol options</caption>
	 *     <thead>
	 *         <tr>
	 *             <th>Valid Options</th>
	 *             <th>Default Value</th>
	 *             <th>Description</th>
	 *         </tr>
	 *     </thead>
	 *     <tbody>
	 *         <tr>
	 *             <td>host</td>
	 *             <td>"127.0.0.1"</td>
	 *             <td>Specifies the hostname where the Console is running</td>
	 *         </tr>
	 *         <tr>
	 *             <td>port</td>
	 *             <td>4228</td>
	 *             <td>Specifies the Console port</td>
	 *         </tr>
	 *         <tr>
	 *             <td>timeout</td>
	 *             <td>30000</td>
	 *             <td>Specifies the connect, receive and send timeout in milliseconds</td>
	 *         </tr>
	 *     </tbody>
	 * </table>
	 * <p>
	 * For further options which affect the behavior of this protocol, please have a look at the documentation of the Protocol.isValidOption method of the parent class.
	 * <h3>Example:</h3>
	 * <pre>
	 * SiAuto.si.setConnections("tcp()");
	 * SiAuto.si.setConnections("tcp(host=\\"localhost\\", port=4229)");
	 * SiAuto.si.setConnections("tcp(timeout=2500)");
	 * </pre>
	 *
	 * @param name The option name to validate
	 * @return True if the option is supported and false otherwise
	 */
	protected boolean isValidOption(String name) {
		return
				name.equals("host") ||
						name.equals("port") ||
						name.equals("timeout") ||
						super.isValidOption(name);
	}

	/**
	 * Overridden. Fills a ConnectionsBuilder instance with the
	 * options currently used by this TCP protocol.
	 *
	 * @param builder The ConnectionsBuilder object to fill with the current options
	 *                of this protocol
	 */
	protected void buildOptions(ConnectionsBuilder builder) {
		super.buildOptions(builder);
		builder.addOption("host", this.fHostName);
		builder.addOption("port", this.fPort);
		builder.addOption("timeout", this.fTimeout);
	}

	/**
	 * Overridden. Loads and inspects TCP specific options.
	 * <p>
	 * This method loads all relevant options and ensures their correctness.
	 * See isValidOption for a list of options which are recognized by the TCP protocol
	 */
	protected void loadOptions() {
		super.loadOptions();
		this.fHostName = getStringOption("host", "127.0.0.1");
		this.fTimeout = getIntegerOption("timeout", 30000);
		this.fPort = getIntegerOption("port", 4228);
	}

	protected void doHandShake() throws IOException, SmartInspectException {
		readServerBanner();
		sendClientBanner();
	}

	protected void readServerBanner() throws SmartInspectException, IOException {
		int n;

		// Read the server banner from the Console.
		while ((n = fIstream.read()) != '\n') {
			if (n == -1) {
				// This indicates a failure on the server
				// side. Doesn't make sense to proceed here.

				throw new SmartInspectException(
						"Could not read server banner correctly: " +
								"Connection has been closed unexpectedly"
				);
			}
		}
	}

	protected void sendClientBanner() throws IOException {
		// And write ours in return!
		fOstream.write(CLIENT_BANNER, 0, CLIENT_BANNER.length);
		fOstream.flush();
	}

	/**
	 * Overridden. Creates and connects a TCP socket.
	 * This method tries to connect a TCP socket to a SmartInspect
	 * Console. The hostname and port can be specified by passing
	 * the "hostname" and "port" options to the initialize method.
	 * Furthermore, it is possible to specify the connect timeout
	 * by using the "timeout" option.
	 *
	 * @throws Exception if creating or connecting the socket failed
	 */
	protected void internalConnect() throws Exception {
		this.fSocket = internalInitializeSocket();

		if (this.fSocket.isConnected()) {
			this.fIstream = this.fSocket.getInputStream();
			this.fOstream = new BufferedOutputStream(
					this.fSocket.getOutputStream(), BUFFER_SIZE
			);

			doHandShake();
			internalWriteLogHeader(); /* Write the log header */

			logger.fine("Connected");
		}
	}

	protected Socket internalInitializeSocket() throws Exception {
		Socket socket = new Socket();
		socket.setTcpNoDelay(true);
		socket.setSoTimeout(this.fTimeout);

		socket.connect(
				new InetSocketAddress(this.fHostName, this.fPort),
				this.fTimeout
		);

		return socket;
	}

	/**
	 * Overridden. Sends a packet to the Console.
	 * <p>
	 * This method sends the supplied packet to the SmartInspect
	 * Console and waits for a valid response.
	 *
	 * @param packet The packet to write
	 * @throws Exception If sending the packet to the Console failed
	 */
	protected void internalWritePacket(Packet packet) throws Exception {
		this.fFormatter.format(packet, this.fOstream);

		this.fOstream.flush();

		// in bytes
		int answerSize = this.fIstream.read(this.fAnswer, 0, ANSWER_BUFFER_SIZE);

		internalValidateWritePacketAnswer(answerSize, fAnswer);
	}

	protected void internalValidateWritePacketAnswer(int bytesRead, byte[] answerBytes) throws Exception {
		// 2 bytes for OK
		if (bytesRead != 2) {
			throw new SmartInspectException(
					"Could not read server answer correctly: " +
							"Connection has been closed unexpectedly"
			);
		}
	}

	/**
	 * Overridden. Closes the TCP socket connection.
	 * <p>
	 * This method closes the underlying socket handle if previously
	 * created and disposes any supplemental objects.
	 *
	 * @throws Exception If Closing the TCP socket failed
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

		if (this.fSocket != null) {
			try {
				this.fSocket.close();
			} finally {
				this.fSocket = null;
			}
		}
	}
}
