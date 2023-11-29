/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for handling the SmartInspect configuration and loading
 * it from a file. It is primarily used for loading and reading values from a
 * SmartInspect configuration file. For more information, please refer
 * to the documentation of the SmartInspect.loadConfiguration method.
 * <p>
 * Note: This class is not guaranteed to be thread-safe.
 */

public class Configuration {
	private int MAX_BOM = 3;
	private LookupTable fItems;
	private List fKeys;

	/**
	 * Creates and initializes a Configuration instance.
	 */
	public Configuration() {
		this.fKeys = new ArrayList();
		this.fItems = new LookupTable();
	}

	private String readStream(InputStream is, String encoding)
			throws IOException {
		StringBuffer sb = new StringBuffer();

		InputStreamReader reader = new InputStreamReader(is, encoding);
		try {
			int n;
			char[] c = new char[0x2000];

			while ((n = reader.read(c, 0, c.length)) > 0) {
				sb.append(c, 0, n);
			}
		} finally {
			reader.close();
		}

		return sb.toString();
	}

	private String readFile(String fileName) throws IOException {
		PushbackInputStream ps = new PushbackInputStream(
				new FileInputStream(fileName), MAX_BOM /* Buffer size */
		);
		try {
			String encoding = "US-ASCII";
			byte[] bom = new byte[MAX_BOM];

			int n = ps.read(bom, 0, bom.length);
			int pushback = n; /* Unread all bytes */

			/* Detect the Unicode encoding automatically. We currently
			 * support UTF8, UTF16 little endian and UTF16 big endian.
			 * If no known BOM can be found, we fall back to the default
			 * ASCII encoding.
			 */

			if (n == bom.length) {
				if ((bom[0] == (byte) 0xEF) &&
						(bom[1] == (byte) 0xBB) &&
						(bom[2] == (byte) 0xBF)) {
					encoding = "UTF-8";
					pushback = 0; /* No unread required */
				} else if (
						(bom[0] == (byte) 0xFE) &&
								(bom[1] == (byte) 0xFF)) {
					encoding = "UTF-16BE";
					pushback = 1; /* Unread last byte */
				} else if (
						(bom[0] == (byte) 0xFF) &&
								(bom[1] == (byte) 0xFE)) {
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

			if (pushback > 0) {
				ps.unread(bom, n - pushback, pushback);
			}

			return readStream(ps, encoding);
		} finally {
			ps.close();
		}
	}

	/**
	 * Loads the configuration from a file.
	 *
	 * <p>This method loads key/value pairs separated with a '=' character from a file.
	 * Empty, unrecognized lines or lines beginning with a ';' character are ignored.</p>
	 *
	 * @param fileName The name of the file to load the configuration from.
	 * @throws IOException          If an I/O error occurred while trying to load the configuration or if the specified file does not exist.
	 * @throws NullPointerException If the fileName argument is null.
	 */
	public void loadFromFile(String fileName) throws IOException {
		if (fileName == null) {
			throw new NullPointerException("fileName argument is null");
		}

		BufferedReader reader =
				new BufferedReader(new StringReader(readFile(fileName)));
		try {
			clear();
			String line;

			while ((line = reader.readLine()) != null) {
				if (!line.startsWith(";")) {
					parse(line);
				}
			}
		} finally {
			reader.close();
		}
	}

	private void parse(String line) {
		int index = line.indexOf('=');

		if (index != -1) {
			String key = line.substring(0, index).trim();
			String value = line.substring(index + 1).trim();

			if (!this.fItems.contains(key)) {
				this.fKeys.add(key);
			}

			this.fItems.put(key, value);
		}
	}

	/**
	 * Tests if the configuration contains a value for a given key.
	 *
	 * @param key The key to test for.
	 * @return True if a value exists for the given key and false otherwise.
	 */
	public boolean contains(String key) {
		return this.fItems.contains(key);
	}

	/**
	 * Removes all key/value pairs of the configuration.
	 */
	public void clear() {
		this.fKeys.clear();
		this.fItems.clear();
	}

	/**
	 * Returns the number of key/value pairs of this SmartInspect
	 * configuration.
	 *
	 * @return The number of key/value pairs of this SmartInspect configuration.
	 */
	public int getCount() {
		return this.fItems.getCount();
	}

	/**
	 * Returns a String value of an element for a given key.
	 *
	 * @param key          The key whose value to return.
	 * @param defaultValue The value to return if the given key is unknown.
	 * @return Either the value for a given key if an element with the given
	 * key exists or defaultValue otherwise.
	 * @throws NullPointerException If the key argument is null.
	 */
	public String readString(String key, String defaultValue) {
		return this.fItems.getStringValue(key, defaultValue);
	}

	/**
	 * Returns either the value converted to a boolean for the given key if an
	 * element with the given key exists or defaultValue otherwise. This method returns
	 * a boolean value of true if the found value of the given key matches
	 * either "true", "1" or "yes" and false otherwise. If the supplied key is unknown,
	 * the defaultValue argument is returned.
	 *
	 * @param key          The key whose value to return.
	 * @param defaultValue The value to return if the given key is unknown.
	 * @return Either value converted to a boolean for the given key
	 * if an element with the given key exists or defaultValue otherwise.
	 * @throws NullPointerException If the key argument is null.
	 */
	public boolean readBoolean(String key, boolean defaultValue) {
		return this.fItems.getBooleanValue(key, defaultValue);
	}

	/**
	 * Returns an int value of an element for a given key.
	 *
	 * @param key          The key whose value to return.
	 * @param defaultValue The value to return if the given key is unknown.
	 * @return Either the value converted to an int for the given key if an
	 * element with the given key exists and the found value is a
	 * valid int or defaultValue otherwise.
	 *
	 * <p>
	 * This method returns the defaultValue argument if either the
	 * supplied key is unknown or the found value is not a valid int.
	 * Only non-negative int values are recognized as valid.
	 * </p>
	 * @throws NullPointerException If the key argument is null.
	 */
	public int readInteger(String key, int defaultValue) {
		return this.fItems.getIntegerValue(key, defaultValue);
	}

	/**
	 * Returns a Level value of an element for a given key.
	 *
	 * @param key          The key whose value to return.
	 * @param defaultValue The value to return if the given key is unknown.
	 * @return Either the value converted to the corresponding Level value for the given key if an element with the given key exists and
	 * the found value is a valid Level value or defaultValue otherwise.
	 * Note: This method returns the defaultValue argument if either the supplied key is unknown or the found value is not a valid Level
	 * value. Please see the Level enum for more information on the available values.
	 * @throws NullPointerException If the key argument is null.
	 */
	public Level readLevel(String key, Level defaultValue) {
		return this.fItems.getLevelValue(key, defaultValue);
	}

	/**
	 * Returns a Color value of an element for a given key.
	 * <p>This method requires the element value to be specified as a hexadecimal string.
	 * To indicate that the element value represents a hexadecimal string,
	 * the element value must begin with "0x", "{@literal &}H" or "$".
	 * A '0' nibble is appended if the hexadecimal string has an odd length.</p>
	 * <p>The hexadecimal value must represent a three or four byte integer value. The hexadecimal values are handled as follows:
	 * <ul>
	 * <li>3 bytes: RRGGBB</li>
	 * <li>4 bytes: AARRGGBB</li>
	 * <li>Other: Ignored</li>
	 * </ul>
	 * <p>A stands for the alpha channel and R, G, and B represent the red, green and blue channels, respectively.
	 * If the value is not given as hexadecimal value with a length of 6 or 8 characters excluding the hexadecimal prefix identifier
	 * or if the value does not have a valid hexadecimal format, this method returns defaultValue.</p>
	 *
	 * @param key          The key whose value to return.
	 * @param defaultValue The value to return if the given key is unknown or if the found value has an invalid format.
	 * @return Either the value converted to a Color value for the given key if an element with the given key exists and the found value has a valid format or defaultValue otherwise.
	 * @throws NullPointerException if the key argument is null.
	 */
	public Color readColor(String key, Color defaultValue) {
		return this.fItems.getColorValue(key, defaultValue);
	}

	/**
	 * Returns a key of this SmartInspect configuration for a given index.
	 *
	 * <p>To find out the total number of key/value pairs in this SmartInspect configuration,
	 * use {@link #getCount()}. To get the value for a given key, use {@link #readString(String key, String defaultValue)}.</p>
	 *
	 * @param index The index in this SmartInspect configuration.
	 * @return A key of this SmartInspect configuration for the given index.
	 * @throws IndexOutOfBoundsException if the index argument is not a valid
	 *                                   index of this SmartInspect configuration.
	 */
	public String readKey(int index) {
		return (String) this.fKeys.get(index);
	}
}
