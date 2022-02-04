//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Logger;

// <summary>
//   Used for sending packets to the SmartInspect Console over a TCP
//   socket connection.
// </summary>
// <remarks>
//   This class is used for sending packets over a TCP connection to
//   the Console. It is used when the 'tcp' protocol is specified in
//   the <link SmartInspect.setConnections, connections string>.
//   Please see the isValidOption method for a list of available
//   protocol options.
// </remarks>
// <threadsafety>
//   The public members of this class are threadsafe.
// </threadsafety>

public class TcpProtocol extends Protocol
{
	private static final Logger logger = Logger.getLogger(TcpProtocol.class.getName());

	private static final int BUFFER_SIZE = 0x2000;
	private static final byte[] CLIENT_BANNER =
		("SmartInspect Java Library v" + SmartInspect.getVersion() + 
		"\r\n").getBytes();

	private static final int ANSWER_BUFFER_SIZE = 0x2000;

	private Socket fSocket;
	private InputStream fIstream;
	protected OutputStream fOstream;
	protected Formatter fFormatter;
	private byte[] fAnswer;

	private String fHostName = "127.0.0.1";
	private int fTimeout = 30000;
	private int fPort = 4228;

	// <summary>
	//   Creates and initializes a TcpProtocol instance. For a list of
	//   available TCP protocol options, please refer to the isValidOption
	//   method.
	// </summary>
	
	public TcpProtocol()
	{
		this.fAnswer = new byte[ANSWER_BUFFER_SIZE];
		this.fFormatter = new BinaryFormatter();
		loadOptions(); // Set default options
	}
	
	// <summary>
	//   Overridden. Returns "tcp".
	// </summary>
	// <returns>Just "tcp".</returns>

	protected String getName()
	{
		return "tcp";
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
	//   and descriptions for the TCP protocol.
	//
	//   <table>
	//   Valid Options  Default Value  Description
	//   -              -              -
	//   host           "127.0.0.1"    Specifies the hostname where
	//                                   the Console is running.
	//
	//   port           4228           Specifies the Console port.
	//
	//   timeout        30000          Specifies the connect, receive and
	//                                   send timeout in milliseconds.
	//   </table>
	// </remarks>
	//
	//   For further options which affect the behavior of this protocol,
	//   please have a look at the documentation of the
	//   <link Protocol.isValidOption, isValidOption> method of the
	//   parent class.
	// <example>
	// <code>
	// SiAuto.si.setConnections("tcp()");
	// SiAuto.si.setConnections("tcp(host=\\"localhost\\", port=4229)");
	// SiAuto.si.setConnections("tcp(timeout=2500)");
	// </code>
	// </example>

	protected boolean isValidOption(String name)
	{
		return 
			name.equals("host") ||
			name.equals("port") ||
			name.equals("timeout") ||
			super.isValidOption(name);
	}

	// <summary>
	//   Overridden. Fills a ConnectionsBuilder instance with the
	//   options currently used by this TCP protocol.
	// </summary>
	// <param name="builder">
	//   The ConnectionsBuilder object to fill with the current options
	//   of this protocol.
	// </param>

	protected void buildOptions(ConnectionsBuilder builder)
	{
		super.buildOptions(builder);
		builder.addOption("host", this.fHostName);
		builder.addOption("port", this.fPort);
		builder.addOption("timeout", this.fTimeout);
	}
	
	// <summary>
	//   Overridden. Loads and inspects TCP specific options.
	// </summary>
	// <remarks>
	//   This method loads all relevant options and ensures their
	//   correctness. See isValidOption for a list of options which
	//   are recognized by the TCP protocol.
	// </remarks>

	protected void loadOptions()
	{
		super.loadOptions();
		this.fHostName = getStringOption("host", "127.0.0.1");
		this.fTimeout = getIntegerOption("timeout", 30000);
		this.fPort = getIntegerOption("port", 4228);
	}

	private static void doHandShake(InputStream is, OutputStream os)
		throws IOException, SmartInspectException
	{
		int n;

		// Read the server banner from the Console.
		while ( (n = is.read()) != '\n')
		{
			if (n == -1)
			{
				// This indicates a failure on the server
				// side. Doesn't make sense to proceed here.

				throw new SmartInspectException(
						"Could not read server banner correctly: " +
						"Connection has been closed unexpectedly"
					);
			}
		}

		// And write ours in return!
		os.write(CLIENT_BANNER, 0, CLIENT_BANNER.length);
		os.flush();
	}

	// <summary>
	//   Overridden. Creates and connects a TCP socket.
	// </summary>
	// <remarks>
	//   This method tries to connect a TCP socket to a SmartInspect
	//   Console. The hostname and port can be specified by passing
	//   the "hostname" and "port" options to the initialize method.
	//   Furthermore, it is possible to specify the connect timeout
	//   by using the "timeout" option.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type       Condition
	//   -                    -
	//   Exception            Creating or connecting the socket failed.
	// </table>
	// </exception>

	protected void internalConnect() throws Exception
	{
		this.fSocket = new Socket();
		this.fSocket.setTcpNoDelay(true);
		this.fSocket.setSoTimeout(this.fTimeout);

		this.fSocket.connect(
			new InetSocketAddress(this.fHostName, this.fPort),
			this.fTimeout
		);

		if (this.fSocket.isConnected())
		{
			this.fIstream = this.fSocket.getInputStream();
			this.fOstream = new BufferedOutputStream(
					this.fSocket.getOutputStream(), BUFFER_SIZE
				);

			doHandShake(this.fIstream, this.fOstream);
			internalWriteLogHeader(); /* Write the log header */

			logger.fine("Connected");
		}
	}

	// <summary>
	//   Overridden. Sends a packet to the Console.
	// </summary>
	// <param name="packet">The packet to write.</param>
	// <remarks>
	//   This method sends the supplied packet to the SmartInspect
	//   Console and waits for a valid response.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type       Condition
	//   -                    -
	//   Exception            Sending the packet to the Console failed.
	// </table>
	// </exception>

	protected void internalWritePacket(Packet packet) throws Exception
	{
		logger.fine(
				"packet = " + packet + ", type = " + packet.getPacketType()
				+ ", " + packet.getPacketType().getIntValue()
//				+ ", " + packet.getSize()
		);

		if (packet instanceof LogEntry) {
			String title = ((LogEntry) packet).getTitle();
			logger.fine("title = " + title);
		} else if (packet instanceof LogHeader) {
			String title = ((LogHeader) packet).getContent();
			logger.fine("title = " + title);
		}

		this.fFormatter.format(packet, this.fOstream);

		this.fOstream.flush();

		// in bytes
		int answerSize = this.fIstream.read(this.fAnswer, 0, ANSWER_BUFFER_SIZE);

		internalValidateWritePacketAnswer(answerSize, fAnswer);
	}

	protected void internalValidateWritePacketAnswer(int bytesRead, byte[] answerBytes) throws Exception {
		// 2 bytes for OK
		if (bytesRead != 2)
		{
			throw new SmartInspectException(
					"Could not read server answer correctly: " +
							"Connection has been closed unexpectedly"
			);
		}
	}

	// <summary>
	//   Overridden. Closes the TCP socket connection.
	// </summary>
	// <remarks>
	//   This method closes the underlying socket handle if previously
	//   created and disposes any supplemental objects.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type       Condition
	//   -                    -
	//   Exception            Closing the TCP socket failed.
	// </table>
	// </exception>

	protected void internalDisconnect() throws Exception
	{
		if (this.fIstream != null)
		{
			try 
			{
				this.fIstream.close();
			}
			finally
			{
				this.fIstream = null;
			}
		}

		if (this.fOstream != null)
		{
			try 
			{
				this.fOstream.close();
			}
			finally 
			{
				this.fOstream = null;
			}
		}

		if (this.fSocket != null)
		{
			try 
			{
				this.fSocket.close();
			}
			finally 
			{
				this.fSocket = null;
			}
		}
	}
}
