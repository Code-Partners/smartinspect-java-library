//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

// <summary>
//   This is the base class for all viewer contexts, which deal with
//   binary data. A viewer context is the library-side representation
//   of a viewer in the Console.
// </summary>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>

public class BinaryContext extends ViewerContext
{
	private ByteArrayOutputStream fData;

	// <summary>
	//   Creates and initializes a BinaryContext instance.
	// </summary>
	// <param name="vi">The viewer ID to use.</param>

	public BinaryContext(ViewerId vi)
	{
		super(vi);
		this.fData = new ByteArrayOutputStream();		
	}

	// <summary>
	//   Overridden. Returns the actual binary data which will be
	//   displayed in the viewer specified by the getViewerId method.
	// </summary>
	// <returns>
	//   The actual binary data which will be displayed in the viewer
	//   specified by the getViewerId method.
	// </returns>

	public byte[] getViewerData()
	{
		return this.fData.toByteArray();
	}

	// <summary>Resets the internal data stream.</summary>
	// <remarks>
	//   This method is intended to reset the internal data stream if
	//   custom handling of data is needed by derived classes.
	// </remarks>

	protected void resetData()
	{
		this.fData.reset();
	}

	// <summary>Loads the binary data from a file.</summary>
	// <param name="fileName">
	//   The name of the file to load the binary data from.
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
			FileInputStream fs = new FileInputStream(fileName);
			try
			{
				loadFromStream(fs);
			}
			finally 
			{
				fs.close();
			}
		}
	}

	// <summary>
	//   Loads the binary data from a stream.
	// </summary>
	// <param name="is">
	//   The stream to load the binary data from.
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
			int n;
			byte[] b = new byte[0x2000];

			resetData();
			while ( (n = is.read(b, 0, b.length)) != -1)
			{
				this.fData.write(b, 0, n);
			}
		}
	}

	// <summary>
	//   Overloaded. Appends a buffer.
	// </summary>
	// <param name="b">The buffer to append.</param>
	// <exception>
	// <table>
	//   Exception Type               Condition
	//   -                            -
	//   NullPointerException         The b argument is null.
	// </table>
	// </exception>

	public void appendBytes(byte[] b)
	{
		if (b == null)
		{
			throw new NullPointerException("b argument is null");
		}
		else 
		{
			this.fData.write(b, 0, b.length);
		}
	}

	// <summary>
	//   Overloaded. Appends a buffer. Lets you specify the offset in
	//   the buffer and the amount of bytes to append.
	// </summary>
	// <param name="b">The buffer to append.</param>
	// <param name="off">
	//   The offset at which to begin appending.
	// </param>
	// <param name="len">The number of bytes to append.</param>
	// <table>
	//   Exception Type               Condition
	//   -                            -
	//   IndexOutOfBoundsException    The sum of the off and len
	//                                  parameters is greater than the
	//                                  actual buffer length or the off
	//                                  or len arguments are negative.
	//                                  
	//   NullPointerException         The b argument is null.
	//   
	// </table>
	// </exception>

	public void appendBytes(byte[] b, int off, int len)
	{
		if (b == null)
		{
			throw new NullPointerException("b argument is null");
		}
		else 
		{
			this.fData.write(b, off, len);
		}
	}

	// <summary>
	//   Overridden. Releases any resources used by this binary context.
	// </summary>

	public void close()
	{
		if (this.fData != null)
		{
			try 
			{
				this.fData.close();
				this.fData = null;
			}
			catch (IOException e)
			{
				// This exception cannot occur because
				// we are using a ByteArrayOutputStream.
			}
		}
	}
}
