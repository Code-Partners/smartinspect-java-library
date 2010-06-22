//
// <!-- Copyright (C) 2003-2010 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   This class is used by the OptionsParser.parse method.
// </summary>
// <remarks>
//   This class is used by the OptionsParser class to inform interested
//   parties about found options. It offers the necessary properties to
//   retrieve the found options in the event handlers.
// </remarks>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

public class OptionsParserEvent extends java.util.EventObject
{
	private String fProtocol;
	private String fKey;
	private String fValue;
	
	// <summary>
	//   Creates and initializes a new OptionsParserEventArgs nstance.
	// </summary>
	// <param name="source">The object which caused the event.</param>
	// <param name="protocol">The protocol of the new option.</param>
	// <param name="key">The key of the new option.</param>
	// <param name="value">The value of the new option.</param>
	
	public OptionsParserEvent(Object source, String protocol, String key,
		String value)
	{
		super(source);
		this.fProtocol = protocol;
		this.fKey = key;
		this.fValue = value;
	}
	
	// <summary>
	//   This method returns the protocol of the option which has just
	//   been found by a OptionsParser object.
	// </summary>
	// <returns>The protocol of the found option.</returns>
	
	public String getProtocol()
	{
		return this.fProtocol;
	}
	
	// <summary>
	//   This method returns the key of the option which has just been
	//   found by a OptionsParser object.
	// </summary>
	// <returns>The key of the found option.</returns>

	public String getKey()
	{
		return this.fKey;
	}
	
	// <summary>
	//   This method returns the value of the option which has just been
	//   found by a OptionsParser object.
	// </summary>
	// <returns>The value of the found option.</returns>

	public String getValue()
	{
		return this.fValue;
	}
}
