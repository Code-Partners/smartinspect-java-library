//
// <!-- Copyright (C) Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Represents the table viewer in the Console which can display text
//   data as a table.
// </summary>
// <remarks>
//   The table viewer in the Console interprets the <link LogEntry.getData,
//   data of a Log Entry> as a table. This class
//   takes care of the necessary formatting and escaping required by
//   the corresponding table viewer in the Console.
//
//   You can use the TableViewerContext class for creating custom
//   log methods around <link Session.logCustomContext, logCustomContext>
//   for sending custom data organized as tables.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>

public class TableViewerContext extends ListViewerContext
{
	private boolean fLineStart;
	
	// <summary>
	//   Creates and initializes a TableViewerContext instance.
	// </summary>
	
	public TableViewerContext()
	{
		super(ViewerId.Table);
		this.fLineStart = true;		
	}	

	// <summary>
	//   Appends a header to the text data.
	// </summary>
	// <param name="header">The header to append.</param>

	public void appendHeader(String header)
	{
		appendLine(header);
		appendLine("");
	}

	// <summary>
	//   Overloaded. Adds a string entry to the current row.
	// </summary>
	// <param name="entry">The string entry to add.</param>

	public void addRowEntry(String entry)
	{
		if (entry != null)
		{
			if (this.fLineStart)
			{
				this.fLineStart = false;
			}
			else 
			{
				appendText(", ");
			}
			appendText(escapeCSVEntry(entry));
		}
	}

	// <summary>
	//   Overloaded. Adds a char entry to the current row.
	// </summary>
	// <param name="entry">The char entry to add.</param>

	public void addRowEntry(char entry)
	{
		addRowEntry(String.valueOf(entry));
	}

	// <summary>
	//   Overloaded. Adds a boolean entry to the current row.
	// </summary>
	// <param name="entry">The boolean entry to add.</param>

	public void addRowEntry(boolean entry)
	{
		addRowEntry(String.valueOf(entry));
	}

	// <summary>
	//   Overloaded. Adds a byte entry to the current row.
	// </summary>
	// <param name="entry">The byte entry to add.</param>

	public void addRowEntry(byte entry)
	{
		addRowEntry(String.valueOf(entry));
	}

	// <summary>
	//   Overloaded. Adds a short entry to the current row.
	// </summary>
	// <param name="entry">The short entry to add.</param>

	public void addRowEntry(short entry)
	{
		addRowEntry(String.valueOf(entry));
	}

	// <summary>
	//   Overloaded. Adds an int entry to the current row.
	// </summary>
	// <param name="entry">The int entry to add.</param>

	public void addRowEntry(int entry)
	{
		addRowEntry(String.valueOf(entry));
	}

	// <summary>
	//   Overloaded. Adds a long entry to the current row.
	// </summary>
	// <param name="entry">The long entry to add.</param>

	public void addRowEntry(long entry)
	{
		addRowEntry(String.valueOf(entry));
	}

	// <summary>
	//   Overloaded. Adds a float entry to the current row.
	// </summary>
	// <param name="entry">The float entry to add.</param>

	public void addRowEntry(float entry)
	{
		addRowEntry(String.valueOf(entry));
	}

	// <summary>
	//   Overloaded. Adds a double entry to the current row.
	// </summary>
	// <param name="entry">The double entry to add.</param>

	public void addRowEntry(double entry)
	{
		addRowEntry(String.valueOf(entry));
	}

	// <summary>
	//   Overloaded. Adds an object entry to the current row.
	// </summary>
	// <param name="entry">The object entry to add.</param>

	public void addRowEntry(Object entry)
	{
		addRowEntry(String.valueOf(entry));
	}

	private static String escapeCSVEntry(String entry)
	{
		if (entry == null || entry.length() == 0)
		{
			return entry;
		}
		else
		{
			StringBuffer sb = new StringBuffer(2*entry.length());
			sb.append("\"");
			
			for (int i = 0, len = entry.length(); i < len; i++)
			{
				char c = entry.charAt(i);
				if (Character.isWhitespace(c))
				{
					// Newline characters need to be escaped,
					// they would break the csv format.
					sb.append(" ");
				}
				else if (c == '"')
				{
					// '"' characters are used to surround entries
					// in the csv format, so they need to be escaped.
					sb.append("\"\"");
				}
				else 
				{
					// This character is valid, so just append it.
					sb.append(c);
				}
			}
			
			sb.append("\"");
			return sb.toString();
		}
	}

	// <summary>
	//   Begins a new row.
	// </summary>

	public void beginRow()
	{
		this.fLineStart = true;		
	}

	// <summary>
	//   Ends the current row.
	// </summary>

	public void endRow()
	{
		appendLine("");
	}
}
