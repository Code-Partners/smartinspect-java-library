/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

/**
 * Is the base class for all viewer contexts, which deal with text
 * data. A viewer context is the library-side representation of a
 * viewer in the Console.
 * <p>
 * This class is not guaranteed to be threadsafe.
 */
public class TextContext extends ViewerContext {
	private static byte[] BOM =
			new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
	private StringBuffer fData;

	/**
	 * Creates and initializes a TextContent instance.
	 *
	 * @param vi The viewer ID to use
	 */
	public TextContext(ViewerId vi) {
		super(vi);
		this.fData = new StringBuffer();
	}

	/**
	 * Overridden. Returns the actual text data which will be displayed
	 * in the viewer specified by the getViewerId method
	 *
	 * @return The actual text data which will be displayed in the viewer
	 * specified by the getViewerId method
	 */
	public byte[] getViewerData() {
		try {
			byte[] data = this.fData.toString().getBytes("UTF-8");
			byte[] result = new byte[data.length + BOM.length];
			System.arraycopy(BOM, 0, result, 0, BOM.length);
			System.arraycopy(data, 0, result, BOM.length, data.length);
			return result;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * Resets the internal data.
	 * <p>
	 * This method is intended to reset the internal text data if
	 * custom handling of data is needed by derived classes.
	 */
	protected void resetData() {
		this.fData.setLength(0);
	}

	/**
	 * Loads the text from a file.
	 *
	 * @param fileName The name of the file to load the text from
	 * @throws NullPointerException If the fileName argument is null
	 * @throws IOException          If an I/O error occurred
	 */
	public void loadFromFile(String fileName) throws IOException {
		if (fileName == null) {
			throw new NullPointerException("fileName argument is null");
		} else {
			loadFromReader(new FileReader(fileName));
		}
	}

	/**
	 * Loads the text from a stream
	 *
	 * @param is The stream to load the text from
	 * @throws NullPointerException if the is argument is null
	 * @throws IOException          if an I/O error occurs
	 */
	public void loadFromStream(InputStream is) throws IOException {
		if (is == null) {
			throw new NullPointerException("is argument is null");
		} else {
			loadFromReader(new InputStreamReader(is));
		}
	}

	/**
	 * Loads the text from a reader.
	 *
	 * @param r The reader to read the text from
	 * @throws NullPointerException If the r argument is null
	 * @throws IOException          If an I/O error occurred
	 */
	public void loadFromReader(Reader r) throws IOException {
		if (r == null) {
			throw new NullPointerException("r argument is null");
		} else {
			int n;
			char[] c = new char[0x2000];
			StringBuffer sb = new StringBuffer();

			while ((n = r.read(c, 0, c.length)) != -1) {
				sb.append(c, 0, n);
			}

			resetData();
			appendText(sb.toString());
		}
	}

	/**
	 * Loads the text.
	 *
	 * @param text The text to load
	 * @throws NullPointerException If the text argument is null
	 */
	public void loadFromText(String text) {
		if (text == null) {
			throw new NullPointerException("text argument is null");
		} else {
			resetData();
			appendText(text);
		}
	}

	/**
	 * Appends text.
	 *
	 * @param text The text to append
	 * @throws NullPointerException if the text argument is null
	 */
	public void appendText(String text) {
		if (text == null) {
			throw new NullPointerException("text argument is null");
		} else {
			this.fData.append(text);
		}
	}

	/**
	 * Appends a line to the text data.
	 * This method appends the supplied line and a carriage return +
	 * linefeed character to the internal text data after it has been
	 * escaped by the escapeLine method.
	 *
	 * @param line The line to append. It should not be null
	 * @throws NullPointerException if the line argument is null
	 */
	public void appendLine(String line) {
		if (line == null) {
			throw new NullPointerException("line argument is null");
		} else {
			this.fData.append(escapeLine(line));
			this.fData.append("\r\n");
		}
	}

	/**
	 * Escapes a line.
	 * <p>
	 * If overriden in derived classes, this method escapes a line
	 * depending on the viewer format used. The default implementation
	 * does no escaping.
	 *
	 * @param line The line to escape
	 * @return The escaped line
	 */
	protected String escapeLine(String line) {
		return line;
	}

	/**
	 * Overrides to release any resources of this text context.
	 */
	public void close() {
		this.fData = null;
	}
}
