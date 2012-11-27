//
// <!-- Copyright (C) Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Used to report any errors concerning the protocol classes.
// </summary>
// <remarks>
//   This exception can be thrown by several Protocol methods
//   like the Protocol.connect, Protocol.disconnect or
//   Protocol.writePacket methods when an error has occurred.
//   
//   See below for an example on how to obtain detailed information
//   in the <link SmartInspectListener.onError, Error event> in the
//   SmartInspect class about the protocol which caused the error.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>
// <example>
// <code>
// import com.gurock.smartinspect.*;
//
// class Listener extends SmartInspectAdapter
// {
// 	public void onError(ErrorEvent e)
// 	{
// 		System.out.println(e.getException());
//
// 		if (e.getException() instanceof ProtocolException)
// 		{
// 			ProtocolException pe = (ProtocolException) e.getException();
//
// 			// A ProtocolException provides additional information
// 			// about the occurred error besides the normal exception
// 			// message, like, for example, the name of the protocol
// 			// which caused this error.
//
// 			System.out.println(pe.getProtocolName());
// 			System.out.println(pe.getProtocolOptions());
// 		}
// 	}
// }
//
//
// public class ErrorHandling
// {
// 	public static void main(String[] args)
// 	{
// 		// Register our event handler for the error event.
// 		SiAuto.si.addListener(new Listener());
// 
// 		try
// 		{
// 			// And force a connection error.
// 			SiAuto.si.setConnections("file(filename=c:\\\\)");
// 		}
// 		catch (InvalidConnectionsException e)
// 		{
// 			// This catch block is useless. It won't be reached
// 			// anyway, because a connection error doesn't result
// 			// in a Java exception. The SmartInspect Java libary
// 			// uses the Error event for this purpose.
// 		}
//
// 		SiAuto.si.setEnabled(true);
// 	}
// }
// </code>
// </example>

public final class ProtocolException extends SmartInspectException
{
	private String fProtocolName;
	private String fProtocolOptions;
	
	// <summary>
	//   Creates and initializes a ProtocolException instance.
	// </summary>
	// <param name="e">
	//   The error message which describes the exception.
	// </param>

	public ProtocolException(String e)
	{
		super(e);
	}

	// <summary>
	//   Returns the name of the protocol which caused this exception.
	// </summary>
	// <returns>
	//   The name of the protocol which caused this exception.
	// </returns>

	public String getProtocolName()
	{
		return this.fProtocolName;
	}

	// <summary>
	//   Sets the name of the protocol which caused this exception.
	// </summary>
	// <param name="name">The name of the protocol.</param>

	public void setProtocolName(String name)
	{
		this.fProtocolName = name;
	}

	// <summary>
	//   Returns the options of the protocol which caused this
	//   exception.
	// </summary>
	// <returns>
	//   The options of the protocol which caused this exception.
	// </returns>

	public String getProtocolOptions()
	{
		return this.fProtocolOptions;
	}

	// <summary>
	//   Sets the options of the protocol which caused this exception.
	// </summary>
	// <param name="options">The options of the protocol.</param>

	public void setProtocolOptions(String options)
	{
		this.fProtocolOptions = options;
	}
}
