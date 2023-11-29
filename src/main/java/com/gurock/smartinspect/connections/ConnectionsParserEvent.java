/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.connections;

/**
 * This class is used by the ConnectionsParser.parse method.
 *
 * <p>This class is used by the ConnectionsParser class to inform
 * interested parties about found protocols and options. It offers
 * the necessary method to retrieve the found protocols and
 * options in the event handlers.</p>
 *
 * <p>This class is fully threadsafe.</p>
 */
public class ConnectionsParserEvent extends java.util.EventObject {
	private String fProtocol;
	private String fOptions;

	/**
	 * Creates and initializes a new ConnectionsParserEvent instance.
	 *
	 * @param source   The object which caused the event.
	 * @param protocol The protocol which has been found.
	 * @param options  The options of the new protocol.
	 */
	public ConnectionsParserEvent(Object source, String protocol,
								  String options) {
		super(source);
		this.fProtocol = protocol;
		this.fOptions = options;
	}

	/**
	 * This method returns the protocol which has just been found by a
	 * ConnectionsParser object.
	 *
	 * @return The found protocol.
	 */
	public String getProtocol() {
		return this.fProtocol;
	}

	/**
	 * This method property returns the related options for the protocol
	 * which has just been found by a ConnectionsParser object.
	 *
	 * @return The related options for the found protocol.
	 */
	public String getOptions() {
		return this.fOptions;
	}
}
