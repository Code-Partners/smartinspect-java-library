//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

// <summary>
//   Is the base class for all viewer contexts, which deal with text
//   data. A viewer context is the library-side representation of a
//   viewer in the Console.
// </summary>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>

public class TextContext extends ViewerContext
{
	private static byte[] BOM = 
		new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
	private StringBuffer fData;
	
	// <summary>
	//   Creates and initializes a TextContent instance.
	// </summary>
	// <param name="vi">The viewer ID to use.</param>
	
	public TextContext(ViewerId vi)
	{
		super(vi);		
		this.fData = new StringBuffer();
	}
	
	// <summary>
	//   Overriden. Returns the actual text data which will be displayed
	//   in the viewer specified by the getViewerId method.
	// </summary>
	// <returns>
	//   The actual text data which will be displayed in the viewer
	//   specified by the getViewerId method.
	// </returns>
	
	public byte[] getViewerData()
	{
		try
		{
			byte[] data = this.fData.toString().getBytes("UTF-8");
			byte[] result = new byte[data.length + BOM.length];
			System.arraycopy(BOM, 0, result, 0, BOM.length);
			System.arraycopy(data, 0, result, BOM.length, data.length);
			return result;
		}
		catch (UnsupportedEncodingException e)
		{
			return null;
		}
	}

	// <summary>Resets the internal data.</summary>
	// <remarks>
	//   This method is intended to reset the internal text data if
	//   custom handling of data is needed by derived classes.
	// </remarks>

	protected void resetData()
	{
		this.fData.setLength(0);
	}

	// <summary>Loads the text from a file.</summary>
	// <param name="fileName">
	//   The name of the file to load the text from.
	// </param>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The fileName argument is null.
	//   IOException            An I/O error occurred.
	// </table>
	// </exception>
	
	public void loadFromFile(String fileName) throws IOException
	{
		if (fileName == null)
		{
			throw new NullPointerException("fileName argument is null");
		}
		else 
		{
			loadFromReader(new FileReader(fileName));
		}
	}

	// <summary>Loads the text from a stream.</summary>
	// <param name="is">
	//   The stream to load the text from.
	// </param>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The is argument is null.
	//   IOException            An I/O error occurred.
	// </table>
	// </exception>
	
	public void loadFromStream(InputStream is) throws IOException
	{
		if (is == null)
		{
			throw new NullPointerException("is argument is null");
		}
		else
		{
			loadFromReader(new InputStreamReader(is));
		}
	}

	// <summary>Loads the text from a reader.</summary>
	// <param name="r">
	//   The reader to read the text from.
	// </param>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The r argument is null.
	//   IOException            An I/O error occurred.
	// </table>
	// </exception>

	public void loadFromReader(Reader r) throws IOException
	{
		if (r == null)
		{
			throw new NullPointerException("r argument is null");
		}
		else 
		{
			int n;
			char[] c = new char[0x2000];
			StringBuffer sb = new StringBuffer();
			
			while ( (n = r.read(c, 0, c.length)) != -1)
			{
				sb.append(c, 0, n);
			}

			resetData();
			appendText(sb.toString());
		}
	}

	// <summary>Loads the text.</summary>
	// <param name="text">The text to load.</param>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The text argument is null.
	// </table>
	// </exception>

	public void loadFromText(String text)
	{
		if (text == null)
		{
			throw new NullPointerException("text argument is null");
		}
		else 
		{
			resetData();
			appendText(text);
		}		
	}

	// <summary>Appends text.</summary>
	// <param name="text">The text to append.</param>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The text argument is null.
	// </table>
	// </exception>

	public void appendText(String text)
	{
		if (text == null)
		{
			throw new NullPointerException("text argument is null");
		}
		else 
		{
			this.fData.append(text);
		}
	}

	// <summary>Appends a line to the text data.</summary>
	// <param name="line">The line to append.</param>
	// <remarks>
	//   This method appends the supplied line and a carriage return +
	//   linefeed character to the internal text data after it has been
	//   escaped by the escapeLine method.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The line argument is null.
	// </table>
	// </exception>

	public void appendLine(String line)
	{
		if (line == null)
		{
			throw new NullPointerException("line argument is null");
		}
		else 
		{
			this.fData.append(escapeLine(line));
			this.fData.append("\r\n");
		}
	}

	// <summary>Escapes a line.</summary>
	// <param name="line">The line to escape.</param>
	// <returns>The escaped line.</returns>
	// <remarks>
	//   If overriden in derived classes, this method escapes a line
	//   depending on the viewer format used. The default implementation
	//   does no escaping.
	// </remarks>

	protected String escapeLine(String line)
	{
		return line;
	}

	// <summary>
	//   Overriden. Releases any resources of this text context.
	// </summary>

	public void close()
	{
		this.fData = null;
	}
}
