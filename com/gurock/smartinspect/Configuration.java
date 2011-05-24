//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.PushbackInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// <summary>
//   Responsible for handling the SmartInspect configuration and loading
//   it from a file.
// </summary>
// <remarks>
//   This class is responsible for loading and reading values from a
//   SmartInspect configuration file. For more information, please refer
//   to the documentation of the SmartInspect.loadConfiguration method.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>

public class Configuration 
{
	private int MAX_BOM = 3;
	private LookupTable fItems;
	private List fKeys;

	// <summary>
	//   Creates and initializes a Configuration instance.
	// </summary>
	
	public Configuration()
	{
		this.fKeys = new ArrayList();
		this.fItems = new LookupTable();
	}
	
	private String readStream(InputStream is, String encoding) 
		throws IOException
	{
		StringBuffer sb = new StringBuffer();
		
		InputStreamReader reader = new InputStreamReader(is, encoding);
		try
		{
			int n;
			char[] c = new char[0x2000];
			
			while ( (n = reader.read(c, 0, c.length)) > 0)
			{
				sb.append(c, 0, n);
			}
		}
		finally 
		{
			reader.close();
		}
				
		return sb.toString();
	}
	
	private String readFile(String fileName) throws IOException
	{
		PushbackInputStream ps = new PushbackInputStream(
				new FileInputStream(fileName), MAX_BOM /* Buffer size */
			);
		try 
		{
			String encoding = "US-ASCII";
			byte[] bom = new byte[MAX_BOM];
			
			int n = ps.read(bom, 0, bom.length);
			int pushback = n; /* Unread all bytes */
			
			/* Detect the Unicode encoding automatically. We currently
			 * support UTF8, UTF16 little endian and UTF16 big endian.
			 * If no known BOM can be found, we fall back to the default
			 * ASCII encoding. 
			 */
			
			if (n == bom.length)
			{
				if ((bom[0] == (byte) 0xEF) &&
					(bom[1] == (byte) 0xBB) &&
					(bom[2] == (byte) 0xBF))
				{					
					encoding = "UTF-8";
					pushback = 0; /* No unread required */ 
				}
				else if (
					(bom[0] == (byte) 0xFE) &&
					(bom[1] == (byte) 0xFF))
				{
					encoding = "UTF-16BE";
					pushback = 1; /* Unread last byte */
				}
				else if (
					(bom[0] == (byte) 0xFF) &&
					(bom[1] ==  (byte) 0xFE))
				{
					encoding = "UTF-16LE";
					pushback = 1; /* Unread last byte */
				}
			}
			
			/* MAX_BOM bytes could be read but no known BOM found OR
			 * less than MAX_BOM bytes could be read:
			 *   n = <bytes_read>;             // MAX_BOM or less
			 *   pushback = n = <bytes read>;  // bytes to push back 
			 *   n - pushback = n - n = 0;     // offset
			 * 
			 * Found correct UTF8 BOM:
			 *   n = MAX_BOM;                  // 3
			 *   pushback = 0;
			 *   => no bytes pushed back
			 * 
			 * Found correct UTF16 BOM (LE or BE):
			 *   n = MAX_BOM;                  // 3
			 *   pushback = 1;                 // 1 byte to push back
			 *   n - pushback = MAX_BOM - 1    // offset = 2
			 */
			
			if (pushback > 0)
			{
				ps.unread(bom, n - pushback, pushback);
			}
			
			return readStream(ps, encoding);
		}
		finally 
		{
			ps.close();
		}
	}
	
	// <summary>
	//   Loads the configuration from a file.
	// </summary>
	// <param name="fileName">
	//   The name of the file to load the configuration from.
	// </param>
	// <remarks>
	//   This method loads key/value pairs separated with a '='
	//   character from a file. Empty, unrecognized lines or lines
	//   beginning with a ';' character are ignored.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   IOException            An I/O error occurred while trying
	//                           to load the configuration or if the
	//                           specified file does not exist.
	//   NullPointerException   The fileName argument is null.
	// </table>
	// </exception>	
	
	public void loadFromFile(String fileName) throws IOException
	{
		if (fileName == null)
		{
			throw new NullPointerException("fileName argument is null");
		}
		
		BufferedReader reader = 
			new BufferedReader(new StringReader(readFile(fileName)));
		try
		{
			clear();			
			String line;
			
			while ( (line = reader.readLine()) != null)
			{
				if (!line.startsWith(";"))
				{
					parse(line);
				}
			}
		}
		finally 
		{
			reader.close();
		}
	}

	private void parse(String line)
	{
		int index = line.indexOf('=');
		
		if (index != -1)
		{
			String key = line.substring(0, index).trim();
			String value = line.substring(index + 1).trim();

			if (!this.fItems.contains(key))
			{
				this.fKeys.add(key);
			}
			
			this.fItems.put(key, value);
		}
	}
	
	// <summary>
	//   Tests if the configuration contains a value for a given key. 
	// </summary>
	// <param name="key">The key to test for.</param>
	// <returns>
	//   True if a value exists for the given key and false otherwise.
	// </returns>
	
	public boolean contains(String key)
	{
		return this.fItems.contains(key);
	}
	
	// <summary>
	//   Removes all key/value pairs of the configuration.
	// </summary>
	
	public void clear() 
	{
		this.fKeys.clear();
		this.fItems.clear();
	}
	
	// <summary>
	//   Returns the number of key/value pairs of this SmartInspect
	//   configuration.
	// </summary>
	// <returns>
	//   The number of key/value pairs of this SmartInspect configuration.	
	// </returns>
	
	public int getCount()
	{
		return this.fItems.getCount();
	}
	
	// <summary>
	//   Returns a String value of an element for a given key.
	// </summary>
	// <param name="key">The key whose value to return.</param>
	// <param name="defaultValue">
	//   The value to return if the given key is unknown.
	// </param>
	// <returns>
	//   Either the value for a given key if an element with the given
	//   key exists or defaultValue otherwise.
	// </returns>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key argument is null.
	// </table>
	// </exception>
	
	public String readString(String key, String defaultValue)
	{
		return this.fItems.getStringValue(key, defaultValue);
	}
		
	// <summary>
	//   Returns a boolean value of an element for a given key.
	// </summary>
	// <param name="key">The key whose value to return.</param>
	// <param name="defaultValue">
	//   The value to return if the given key is unknown.
	// </param>
	// <returns>
	//   Either the value converted to a boolean for the given key if an
	//   element with the given key exists or defaultValue otherwise.
	// </returns>
	// <remarks>
	//   This method returns a boolean value of true if the found value
	//   of the given key matches either "true", "1" or "yes" and false
	//   otherwise. If the supplied key is unknown, the defaultValue
	//   argument is returned.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key argument is null.
	// </table>
	// </exception>
	
	public boolean readBoolean(String key, boolean defaultValue)
	{
		return this.fItems.getBooleanValue(key, defaultValue);
	}
	
	// <summary>
	//   Returns an int value of an element for a given key.
	// </summary>
	// <param name="key">The key whose value to return.</param>
	// <param name="defaultValue">
	//   The value to return if the given key is unknown.
	// </param>
	// <returns>
	//   Either the value converted to an int for the given key if an
	//   element with the given key exists and the found value is a
	//   valid int or defaultValue otherwise.
	// </returns>
	// <remarks>
	//   This method returns the defaultValue argument if either the
	//   supplied key is unknown or the found value is not a valid int.
	//   Only non-negative int values are recognized as valid. 
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key argument is null.
	// </table>
	// </exception>
	
	public int readInteger(String key, int defaultValue)
	{
		return this.fItems.getIntegerValue(key, defaultValue);
	}
	
	// <summary>
	//   Returns a Level value of an element for a given key.
	// </summary>
	// <param name="key">The key whose value to return.</param>
	// <param name="defaultValue">
	//   The value to return if the given key is unknown.
	// </param>
	// <returns>
	//   Either the value converted to the corresponding Level value for
	//   the given key if an element with the given key exists and the
	//   found value is a valid Level value or defaultValue otherwise.
	// </returns>
	// <remarks>
	//   This method returns the defaultValue argument if either the
	//   supplied key is unknown or the found value is not a valid Level
	//   value. Please see the Level enum for more information on the
	//   available values.  
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key argument is null.
	// </table>
	// </exception>
		
	public Level readLevel(String key, Level defaultValue)
	{
		return this.fItems.getLevelValue(key, defaultValue); 
	}
	
	// <summary>
	//   Returns a Color value of an element for a given key.
	// </summary>
	// <param name="key">The key whose value to return.</param>
	// <param name="defaultValue">
	//   The value to return if the given key is unknown or if the
	//   found value has an invalid format.
	// </param>
	// <returns>
	//   Either the value converted to a Color value for the given key
	//   if an element with the given key exists and the found value
	//   has a valid format or defaultValue otherwise.
	// </returns>
	// <remarks>
	//   The element value must be specified as hexadecimal string.
	//   To indicate that the element value represents a hexadecimal
	//   string, the element value must begin with "0x", "&H" or "$".
	//   A '0' nibble is appended if the hexadecimal string has an odd
	//   length.
	// 
	//   The hexadecimal value must represent a three or four byte
	//   integer value. The hexadecimal value is handled as follows.
	//   
	//   <table>
	//   Bytes          Format
	//   -              -
	//   3              RRGGBB
	//   4              AARRGGBB
	//   Other          Ignored
	//   </table>
	// 
	//   A stands for the alpha channel and R, G and B represent the
	//   red, green and blue channels, respectively. If the value is not
	//   given as hexadecimal value with a length of 6 or 8 characters
	//   excluding the hexadecimal prefix identifier or if the value
	//   does not have a valid hexadecimal format, this method returns
	//   defaultValue.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type          Condition
	//   -                       -
	//   NullPointerException    The key argument is null.
	// </table>
	// </exception>
	
	public Color readColor(String key, Color defaultValue)
	{
		return this.fItems.getColorValue(key, defaultValue);
	}
	
	// <summary>
	//   Returns a key of this SmartInspect configuration for a
	//   given index.
	// </summary>
	// <param name="index">
	//   The index in this SmartInspect configuration.
	// </param>
	// <returns>
	//   A key of this SmartInspect configuration for the given index.
	// </returns>
	// <remarks>
	//   To find out the total number of key/value pairs in this
	//   SmartInspect configuration, use getCount. To get the value for
	//   a given key, use readString.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type               Condition
	//   -                            -
	//   IndexOutOfBoundsException    The index argument is not a valid
	//                                 index of this SmartInspect
	//                                 configuration.
	// </table>
	// </exception>
	
	public String readKey(int index)
	{
		return (String) this.fKeys.get(index);
	}
}
