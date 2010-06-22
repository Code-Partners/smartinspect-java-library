//
// <!-- Copyright (C) 2003-2010 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Represents the value list viewer in the Console which can display
//   data as a key/value list.
// </summary>
// <remarks>
//   The value list viewer in the Console interprets the <link
//   LogEntry.getData, data of a Log Entry> as a simple key/value list.
//   Every line in the text data is interpreted as one key/value item of
//   the list. This class takes care of the necessary formatting and
//   escaping required by the corresponding value list viewer of the
//   Console.
//   
//   You can use the ValueListViewerContext class for creating custom
//   log methods around <link Session.logCustomContext, logCustomContext>
//   for sending custom data organized as key/value lists.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>

public class ValueListViewerContext extends ListViewerContext
{
	// <summary>
	//   Overloaded. Creates and initializes a ValueListViewerContext
	//   instance.
	// </summary>
	
	public ValueListViewerContext()
	{
		super(ViewerId.ValueList);
	}

	// <summary>
	//   Overloaded. Creates and initializes a ValueListViewerContext
	//   instance using a different viewer ID.
	// </summary>
	// <param name="vi">The viewer ID to use.</param>
	// <remarks>
	//   This constructor is intended for derived classes, such as the
	//   InspectorViewerContext class, which extend the capabilities of
	//   this class and use a different viewer ID.
	// </remarks>

	protected ValueListViewerContext(ViewerId vi)
	{
		super(vi);
	}

	// <summary>
	//   Overloaded. Appends a string value and its key.
	// </summary>
	// <param name="key">The key to use.</param>
	// <param name="value">The string value to use.</param>

	public void appendKeyValue(String key, String value)
	{
		if (key != null)
		{
			appendText(escapeItem(key));
			appendText("=");
			if (value != null)
			{
				appendText(escapeItem(value));
			}
			appendText("\r\n");
		}
	}

	// <summary>
	//   Overloaded. Appends a char value and its key.
	// </summary>
	// <param name="key">The key to use.</param>
	// <param name="value">The char value to use.</param>

	public void appendKeyValue(String key, char value)
	{
		appendKeyValue(key, String.valueOf(value));
	}

	// <summary>
	//   Overloaded. Appends a boolean value and its key.
	// </summary>
	// <param name="key">The key to use.</param>
	// <param name="value">The boolean value to use.</param>

	public void appendKeyValue(String key, boolean value)
	{
		appendKeyValue(key, String.valueOf(value));
	}

	// <summary>
	//   Overloaded. Appends a byte value and its key.
	// </summary>
	// <param name="key">The key to use.</param>
	// <param name="value">The byte value to use.</param>

	public void appendKeyValue(String key, byte value)
	{
		appendKeyValue(key, String.valueOf(value));
	}

	// <summary>
	//   Overloaded. Appends a short value and its key.
	// </summary>
	// <param name="key">The key to use.</param>
	// <param name="value">The short value to use.</param>

	public void appendKeyValue(String key, short value)
	{
		appendKeyValue(key, String.valueOf(value));
	}
	
	// <summary>
	//   Overloaded. Appends an int value and its key.
	// </summary>
	// <param name="key">The key to use.</param>
	// <param name="value">The int value to use.</param>

	public void appendKeyValue(String key, int value)
	{
		appendKeyValue(key, String.valueOf(value));
	}
	
	// <summary>
	//   Overloaded. Appends a long value and its key.
	// </summary>
	// <param name="key">The key to use.</param>
	// <param name="value">The long value to use.</param>

	public void appendKeyValue(String key, long value)
	{
		appendKeyValue(key, String.valueOf(value));
	}
	
	// <summary>
	//   Overloaded. Appends a float value and its key.
	// </summary>
	// <param name="key">The key to use.</param>
	// <param name="value">The float value to use.</param>

	public void appendKeyValue(String key, float value)
	{
		appendKeyValue(key, String.valueOf(value));
	}
	
	// <summary>
	//   Overloaded. Appends a double value and its key.
	// </summary>
	// <param name="key">The key to use.</param>
	// <param name="value">The double value to use.</param>

	public void appendKeyValue(String key, double value)
	{
		appendKeyValue(key, String.valueOf(value));
	}
	
	// <summary>
	//   Overloaded. Appends an object value and its key.
	// </summary>
	// <param name="key">The key to use.</param>
	// <param name="value">The object value to use.</param>

	public void appendKeyValue(String key, Object value)
	{
		appendKeyValue(key, String.valueOf(value));
	}
	
	// <summary>Escapes a key or a value.</summary>
	// <param name="item">The key or value to escape.</param>
	// <returns>The escaped key or value.</returns>
	// <remarks>
	//   This method ensures that the escaped key or value does not
	//   contain any newline characters, such as the carriage return or
	//   linefeed characters. Furthermore, it escapes the '\' and '='
	//   characters.
	// </remarks>

	public String escapeItem(String item)
	{
		return escapeLine(item, "\\=");
	}
}
