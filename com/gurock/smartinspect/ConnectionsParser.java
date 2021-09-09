//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Responsible for parsing a SmartInspect connections string.
// </summary>
// <seealso cref="com.gurock.smartinspect.ConnectionsParserListener"/>
// <remarks>
//   This class offers a single method only, called parse, which is
//   responsible for parsing a connections string. This method informs
//   the caller about found protocols and options with a supplied
//   callback listener.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>

public class ConnectionsParser
{
	private void doProtocol(ConnectionsParserListener callback,
		String protocol, String options) throws SmartInspectException
	{
		options = options.trim();
		protocol = protocol.toLowerCase().trim();
		
		ConnectionsParserEvent e =
			new ConnectionsParserEvent(this, protocol, options);

		callback.onProtocol(e);
	}
	
	private void internalParse(String connections, 
		ConnectionsParserListener callback) throws SmartInspectException
	{
		char c;
		StringBuffer name = new StringBuffer();
		StringBuffer options = new StringBuffer();

		for (int i = 0, length = connections.length(); i < length; )
		{
			// Store protocol name.
			c = connections.charAt(i);
			while (i++ < length - 1)
			{
				name.append(c);
				c = connections.charAt(i);
				if (c == '(')
				{
					break;
				}
			}

			if (c != '(')
			{
				// The connections string is invalid because the '('
				// character is missing.
				throw new SmartInspectException(
					"Missing \"(\" at position " + (i + 1)
				);
			}
			else if (i < length)
			{
					i++;
			}

			// Store protocol options.
			boolean quoted = false;
			while (i < length)
			{
				c = connections.charAt(i++);
				if (c == '"')
				{
					if (i < length)
					{
						if (connections.charAt(i) != '"')
						{
							quoted = !quoted;
						}
						else 
						{
							i++;
							options.append('"');
						}
					}
				}
				else if (c == ')' && !quoted)
				{
					break;
				}
				options.append(c);
			}

			if (quoted)
			{
				throw new SmartInspectException(
					"Quoted values not closed at protocol \"" +
					name + "\""
				);
			}

			if (c != ')')
			{
				// The connections string is invalid because the ')'
				// character is missing.
				throw new SmartInspectException(
					"Missing \")\" at position " + (i + 1)
				);
			}
			else if (i < length && connections.charAt(i) == ',')
			{
				// Skip the ',' character.
				i++;
			}

			doProtocol(callback, name.toString(), options.toString());
			name.setLength(0);
			options.setLength(0);
		}
	}

	// <summary>
	//   Parses a connections string.
	// </summary>
	// <seealso cref="com.gurock.smartinspect.ConnectionsParserListener"/>
	// <param name="connections">
	//   The connections string to parse. Not allowed to be null.
	// </param>
	// <param name="callback">
	//   The callback listener which should be informed about found
	//   protocols and their options. Not allowed to be null.
	// </param>
	// <remarks>
	//   This method parses the supplied connections string and informs
	//   the caller about found protocols and options with the supplied
	//   callback listener.
	// 
	//   For information about the correct syntax, please refer to the
	//   documentation of the SmartInspect.setConnections method.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type           Condition
	//   -                        -
	//   NullPointerException     The connections or callback argument
	//                              is null.
	//   SmartInspectException    Invalid connections string syntax.
	// </table>
	// </exception>
	
	public void parse(String connections, ConnectionsParserListener callback)
		throws SmartInspectException
	{
		if (connections == null)
		{
			throw new NullPointerException("connections");
		}
		else if (callback == null)
		{
			throw new NullPointerException("callback");
		}
		else 
		{
			connections = connections.trim();
			if (connections.length() > 0)
			{
				internalParse(connections, callback);
			}
		}
	}
}
