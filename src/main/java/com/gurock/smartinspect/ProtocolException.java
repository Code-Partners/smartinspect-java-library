/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * This class is used to report any errors concerning the protocol classes.
 * <p>
 * This exception can be thrown by several Protocol methods
 * like the Protocol.connect, Protocol.disconnect or
 * Protocol.writePacket methods when an error has occurred.
 * <p>
 * See below for an example on how to obtain detailed information
 * in the Error event in the SmartInspect class about the protocol which caused the error.
 * <p>
 * Note: This class is not guaranteed to be threadsafe.
 * Example:
 * <pre>
 * import com.gurock.smartinspect.*;
 *
 * class Listener extends SmartInspectAdapter
 * {
 * 	public void onError(ErrorEvent e)
 *    {
 * 		System.out.println(e.getException());
 *
 * 		if (e.getException() instanceof ProtocolException)
 *        {
 * 			ProtocolException pe = (ProtocolException) e.getException();
 *
 * 			// A ProtocolException provides additional information
 * 			// about the occurred error besides the normal exception
 * 			// message, like, for example, the name of the protocol
 * 			// which caused this error.
 *
 * 			System.out.println(pe.getProtocolName());
 * 			System.out.println(pe.getProtocolOptions());
 *        }
 *    }
 * }
 *
 * public class ErrorHandling
 * {
 * 	public static void main(String[] args)
 *    {
 * 		// Register our event handler for the error event.
 * 		SiAuto.si.addListener(new Listener());
 *
 * 		try
 *        {
 * 			// And force a connection error.
 * 			SiAuto.si.setConnections("file(filename=c:\\\\)");
 *        }
 * 		catch (InvalidConnectionsException e)
 *        {
 * 			// This catch block is useless. It won't be reached
 * 			// anyway, because a connection error doesn't result
 * 			// in a Java exception. The SmartInspect Java library
 * 			// uses the Error event for this purpose.
 *        }
 *
 * 		SiAuto.si.setEnabled(true);
 *    }
 * }
 * </pre>
 */
public final class ProtocolException extends SmartInspectException {
	private String fProtocolName;
	private String fProtocolOptions;

	/**
	 * Creates and initializes a ProtocolException instance.
	 *
	 * @param e The error message which describes the exception
	 */
	public ProtocolException(String e) {
		super(e);
	}

	/**
	 * Returns the name of the protocol which caused this exception
	 *
	 * @return The name of the protocol which caused this exception
	 */
	public String getProtocolName() {
		return this.fProtocolName;
	}

	/**
	 * Sets the name of the protocol which caused this exception.
	 *
	 * @param name The name of the protocol
	 */
	public void setProtocolName(String name) {
		this.fProtocolName = name;
	}

	/**
	 * Returns the options of the protocol which caused this exception.
	 *
	 * @return The options of the protocol which caused this exception
	 */
	public String getProtocolOptions() {
		return this.fProtocolOptions;
	}

	/**
	 * Sets the options of the protocol which caused this exception.
	 *
	 * @param options The options of the protocol
	 */
	public void setProtocolOptions(String options) {
		this.fProtocolOptions = options;
	}
}
