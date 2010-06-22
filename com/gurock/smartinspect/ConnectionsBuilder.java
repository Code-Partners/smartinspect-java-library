//
// <!-- Copyright (C) 2003-2010 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Assists in building a SmartInspect connections string.
// </summary>
// <remarks>
//   The ConnectionsBuilder class assists in creating connections strings
//   as used by the SmartInspect.setConnections method. To get started,
//   please have a look at the following example. For information about
//   connections strings, please refer to the SmartInspect.setConnections
//   method.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>
// <example>
// <code>
// ConnectionsBuilder builder = new ConnectionsBuilder();
// builder.beginProtocol("file");
// builder.addOption("filename", "log.sil");
// builder.addOption("append", true);
// builder.endProtocol();
// SiAuto.si.setConnections(builder.getConnections());
// </code>
// </example>

public class ConnectionsBuilder
{
	private boolean fHasOptions;
	private StringBuffer fBuffer;
	
	// <summary>
	//   Creates and initializes a ConnectionsBuilder instance.
	// </summary>

	public ConnectionsBuilder()
	{
		this.fBuffer = new StringBuffer();
	}
	
	// <summary>
	//   Clears this ConnectionsBuilder instance by removing all protocols
	//   and their options.
	// </summary>
	// <remarks>
	//   After this method has been called, the getConnections method
	//   returns an empty string.
	// </remarks>

	public void clear()
	{
		this.fBuffer.setLength(0);
	}
	
	// <summary>
	//   Begins a new protocol section.
	// </summary>
	// <param name="protocol">The name of the new protocol.</param>
	// <remarks>
	//   This method begins a new protocol with the supplied name. All
	//   subsequent protocol options are added to this protocol until
	//   the new protocol section is closed by calling the endProtocol
	//   method.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The protocol argument is null.
	// </table>
	// </exception>
	
	public void beginProtocol(String protocol)
	{
		if (protocol == null)
		{
			throw new NullPointerException("protocol");
		}
		else 
		{
			if (this.fBuffer.length() != 0)
			{
				this.fBuffer.append(", ");
			}

			this.fBuffer.append(protocol);
			this.fBuffer.append("(");
			this.fHasOptions = false;
		}
	}
	
	// <summary>
	//   Ends the current protocol section.
	// </summary>
	// <remarks>
	//   This method ends the current protocol. To begin a new protocol
	//   section, use the beginProtocol method.
	// </remarks>
	
	public void endProtocol()
	{
		this.fBuffer.append(")");
	}

	private String escape(String value)
	{
		if (value.indexOf('"') >= 0)
		{
			StringBuffer sb = new StringBuffer();
			
			for (int i = 0; i < value.length(); i++)
			{
				char c = value.charAt(i);
				
				if (c == '"')
				{
					sb.append("\"\"");
				}
				else 
				{
					sb.append(c);
				}
			}
			
			return sb.toString();
		}
		else 
		{
			return value;
		}
	}
	
	// <summary>
	//   Overloaded. Adds a new string option to the current protocol
	//   section.
	// </summary>
	// <param name="key">The key of the new option.</param>
	// <param name="value">The value of the new option.</param>
	// <remarks>
	//   This method adds a new string option to the current protocol
	//   section. The supplied value argument is properly escaped if
	//   necessary.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key or value argument is null.
	// </table>
	// </exception>

	public void addOption(String key, String value)
	{
		if (key == null)
		{
			throw new NullPointerException("key");
		}
		else if (value == null)
		{
			throw new NullPointerException("value");
		}
		else
		{
			if (this.fHasOptions)
			{
				this.fBuffer.append(", ");
			}

			this.fBuffer.append(key);
			this.fBuffer.append("=\"");
			this.fBuffer.append(escape(value));
			this.fBuffer.append("\"");

			this.fHasOptions = true;
		}
	}

	// <summary>
	//   Overloaded. Adds a new boolean option to the current protocol
	//   section.
	// </summary>
	// <param name="key">The key of the new option.</param>
	// <param name="value">The value of the new option.</param>
	// <remarks>
	//   This method adds a new boolean option to the current protocol
	//   section.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key argument is null.
	// </table>
	// </exception>

	public void addOption(String key, boolean value)
	{
		addOption(key, value ? "true" : "false");
	}

	// <summary>
	//   Overloaded. Adds a new integer option to the current protocol
	//   section.
	// </summary>
	// <param name="key">The key of the new option.</param>
	// <param name="value">The value of the new option.</param>
	// <remarks>
	//   This method adds a new integer option to the current protocol
	//   section.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key argument is null.
	// </table>
	// </exception>

	public void addOption(String key, int value)
	{
		addOption(key, Integer.toString(value));
	}

	// <summary>
	//   Overloaded. Adds a new Level option to the current protocol
	//   section.
	// </summary>
	// <param name="key">The key of the new option.</param>
	// <param name="value">The value of the new option.</param>
	// <remarks>
	//   This method adds a new Level option to the current protocol
	//   section.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key or value argument is null.
	// </table>
	// </exception>

	public void addOption(String key, Level value)
	{
		if (value == null)
		{
			throw new NullPointerException("value");
		}
		else 
		{
			addOption(key, value.toString().toLowerCase());
		}
	}

	// <summary>
	//   Overloaded. Adds a new FileRotate option to the current protocol
	//   section.
	// </summary>
	// <param name="key">The key of the new option.</param>
	// <param name="value">The value of the new option.</param>
	// <remarks>
	//   This method adds a new FileRotate option to the current protocol
	//   section.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key or value argument is null.
	// </table>
	// </exception>

	public void addOption(String key, FileRotate value)
	{
		if (value == null)
		{
			throw new NullPointerException("value");
		}
		else 
		{
			addOption(key, value.toString().toLowerCase());
		}
	}
	
	// <summary>
	//   Returns the built connections string.
	// </summary>
	// <returns>The built connections string.</returns>
	// <remarks>
	//   This method returns the connections string which has previously
	//   been built with the beginProtocol, addOption and endProtocol
	//   methods.
	// </remarks>
	
	public String getConnections()
	{
		return this.fBuffer.toString();
	}
}
