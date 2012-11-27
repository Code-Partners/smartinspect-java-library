//
// <!-- Copyright (C) Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   This class is used by the ConnectionsParser.parse method.
// </summary>
// <remarks>
//   This class is used by the ConnectionsParser class to inform
//   interested parties about found protocols and options. It offers
//   the necessary method to retrieve the found protocols and
//   options in the event handlers.
// </remarks>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

public class ConnectionsParserEvent extends java.util.EventObject
{
	private String fProtocol;
	private String fOptions;
	
	// <summary>
	//   Creates and initializes a new ConnectionsParserEvent instance.
	// </summary>
	// <param name="source">The object which caused the event.</param>
	// <param name="protocol">The protocol which has been found.</param>
	// <param name="options">The options of the new protocol.</param>
	
	public ConnectionsParserEvent(Object source, String protocol,
		String options)
	{
		super(source);
		this.fProtocol = protocol;
		this.fOptions = options;
	}
	
	// <summary>
	//   This method returns the protocol which has just been found by a
	//   ConnectionsParser object.
	// </summary>
	// <returns>The found protocol.</returns>
	
	public String getProtocol()
	{
		return this.fProtocol;
	}
	
	// <summary>
	//   This method property returns the related options for the protocol
	//   which has just been found by a ConnectionsParser object.
	// </summary>
	// <returns>The related options for the found protocol.</returns>
	
	public String getOptions()
	{
		return this.fOptions;
	}
}
