//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// <summary>
//   Used for sending packets to a local SmartInspect Console over a
//   named pipe connection.
// </summary>
// <remarks>
//   This class is used for sending packets through a local named pipe
//   to the SmartInspect Console. It is used when the 'pipe' protocol
//   is specified in the <link SmartInspect.setConnections,
//   connections string>. Please see the isValidOption method for a
//   list of available protocol options. Please note that this protocol
//   can only be used for local connections. For remote connections to
//   other machines, please use TcpProtocol.
// </remarks>
// <threadsafety>
//   The public members of this class are threadsafe.
// </threadsafety>

public class PipeProtocol extends Protocol
{
	private static final int BUFFER_SIZE = 0x2000;
	private static final byte[] CLIENT_BANNER =
		("SmartInspect Java Library v" + SmartInspect.getVersion() + 
		"\n").getBytes();

	private String fPipeName;
	private Formatter fFormatter;
	private OutputStream fOstream;
	private InputStream fIstream;

	// <summary>
	//   Creates and initializes a PipeProtocol instance. For a list
	//   of available pipe protocol options, please refer to the
	//   isValidOption method.
	// </summary>	
	
	public PipeProtocol()
	{
		this.fFormatter = new BinaryFormatter();
		loadOptions();
	}

	// <summary>
	//   Overridden. Returns "pipe".
	// </summary>

	protected String getName() 
	{
		return "pipe";
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
	//   Overridden. Validates if a protocol option is supported.
	// </summary>
	// <param name="name">The option name to validate.</param>
	// <returns>
	//   True if the option is supported and false otherwise.
	// </returns>
	// <remarks>
	//   The following table lists all valid options, their default
	//   values and descriptions for the pipe protocol.
	//   
	//   <table>
	//   Valid Options  Default Value   Description
	//   -              -               -
	//   pipename       "smartinspect"  Specifies the named pipe 
	//                                   for sending log packets to
	//                                   the SmartInspect Console.
	//   </table>
	//   
	//   For further options which affect the behavior of this
	//   protocol, please have a look at the documentation of the
	//   <link Protocol.isValidOption, isValidOption> method of the
	//   parent class.
	// </remarks>
	// <example>
	// <code>
	// SiAuto.Si.setConnections("pipe()");
	// SiAuto.Si.setConnections("pipe(pipename=\\"logging\\")");
	// </code>
	// </example>
	
	protected boolean isValidOption(String name)
	{
		return 
			name.equals("pipename") ||
			super.isValidOption(name);
	}

	// <summary>
	//   Overridden. Fills a ConnectionsBuilder instance with the
	//   options currently used by this pipe protocol.
	// </summary>
	// <param name="builder">
	//   The ConnectionsBuilder object to fill with the current options
	//   of this protocol.
	// </param>
	
	protected void buildOptions(ConnectionsBuilder builder)
	{
		super.buildOptions(builder);
		builder.addOption("pipename", this.fPipeName);
	}
	
	// <summary>
	//   Overridden. Loads and inspects pipe specific options.
	// </summary>
	// <remarks>
	//   This method loads all relevant options and ensures their
	//   correctness. See isValidOption for a list of options which
	//   are recognized by the pipe protocol.
	// </remarks>
	
	protected void loadOptions()
	{
		super.loadOptions();
		this.fPipeName = getStringOption("pipename", "smartinspect");
	}
	
	// <summary>
	//   Overridden. Connects to the specified local named pipe.
	// </summary>
	// <remarks>
	//   This method tries to establish a connection to a local named
	//   pipe of a SmartInspect Console. The name of the pipe can be
	//   specified by passing the "pipename" option to the initialize
	//   method.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type      Condition
	//   -                   -
	//   Exception           Establishing the named pipe connection
	//                        failed.
	// </table>
	// </exception>
	
	protected void internalConnect() throws Exception 
	{
		PipeHandle handle = new PipeHandle(this.fPipeName);
		this.fIstream = new PipeInputStream(handle); 
		this.fOstream = new BufferedOutputStream(
				new PipeOutputStream(handle), BUFFER_SIZE);
		doHandShake(this.fIstream, this.fOstream);
		internalWriteLogHeader(); /* Write the log header */
	}

	// <summary>
	//   Overridden. Sends a packet to the Console.
	// </summary>
	// <param name="packet">The packet to write.</param>
	// <remarks>
	//   This method sends the supplied packet to the SmartInspect
	//   Console over the previously established named pipe connection.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type      Condition
	//   -                   -
	//   Exception           Sending the packet to the Console failed.
	// </table>
	// </exception>
	
	protected void internalWritePacket(Packet packet) throws Exception 
	{
		this.fFormatter.format(packet, this.fOstream);
		this.fOstream.flush();
	}
	
	// <summary>
	//   Overridden. Closes the connection to the specified local
	//   named pipe.
	// </summary>
	// <remarks>
	//   This method closes the named pipe handle if previously created
	//   and disposes any supplemental objects.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   Exception              Closing the named pipe handle failed.
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
	}
}
