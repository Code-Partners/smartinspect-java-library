/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This is the base class for all viewer contexts, which deal with
 * binary data. A viewer context is the library-side representation
 * of a viewer in the Console.
 * <p>
 * This class is not guaranteed to be threadsafe.
 */
public class BinaryContext extends ViewerContext
{
	private ByteArrayOutputStream fData;

	/**
	 * Creates and initializes a BinaryContext instance.
	 * @param vi The viewer ID to use
	 */
	public BinaryContext(ViewerId vi)
	{
		super(vi);
		this.fData = new ByteArrayOutputStream();		
	}

	/**
	 * Overridden. Returns the actual binary data which will be
	 * displayed in the viewer specified by the getViewerId
	 *
	 * @return The actual binary data which will be displayed in the viewer
	 * specified by the getViewerId method.
	 */
	public byte[] getViewerData()
	{
		return this.fData.toByteArray();
	}

	/**
	 * Resets the internal data stream.
	 * <p>
	 * This method is intended to reset the internal data stream if
	 * custom handling of data is needed by derived classes.
	 */
	protected void resetData()
	{
		this.fData.reset();
	}

	/**
	 * Loads the binary data from a file.
	 * @param fileName The name of the file to load the binary data from.
	 * @throws NullPointerException The fileName argument is null
	 * @throws IOException An I/O error occurred
	 */
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

	/**
	 * Loads the binary data from a stream.
	 * @param is The stream to load the binary data from.
	 * @throws NullPointerException The is argument is null
	 * @throws IOException An I/O error occurred
	 */
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

	/**
	 * Overloaded. Appends a buffer.
	 * @param b The buffer to append
	 * @throws NullPointerException The b argument is null
	 */
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

	/**
	 * Overloaded. Appends a buffer. Lets you specify the offset in
	 * the buffer and the amount of bytes to append.
	 * @param b The buffer to append
	 * @param off The offset at which to begin appending
	 * @param len The number of bytes to append
	 * @throws IndexOutOfBoundsException The sum of the off and len parameters is greater than the
	 * actual buffer length or the off or len arguments are negative
	 * @throws NullPointerException The b argument is null
	 */
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

	/**
	 * Overridden. Releases any resources used by this binary context.
	 */
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
