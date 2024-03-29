/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Represents a simple collection of key/value pairs.
 *
 * <p>The LookupTable class is responsible for storing and returning
 * values which are organized by keys. Values can be added with the
 * put method. To query a String value for a given key, the
 * getStringValue method can be used. To query and automatically
 * convert values to types other than String, please have a look at
 * the get method family.</p>
 *
 * <p>This class is not guaranteed to be threadsafe.</p>
 */
public class LookupTable {
	private HashMap fItems;
	private static final int SECONDS_FACTOR = 1000;
	private static final int MINUTES_FACTOR = SECONDS_FACTOR * 60;
	private static final int HOURS_FACTOR = MINUTES_FACTOR * 60;
	private static final int DAYS_FACTOR = HOURS_FACTOR * 24;
	public static final int KB_FACTOR = 1024;
	private static final int MB_FACTOR = KB_FACTOR * 1024;
	private static final int GB_FACTOR = MB_FACTOR * 1024;

	/**
	 * Creates and initializes a LookupTable instance.
	 */
	public LookupTable() {
		this.fItems = new HashMap();
	}

	/**
	 * Adds or updates an element with a specified key and value to
	 * the LookupTable.
	 * <p>
	 * This method adds a new element with a given key and value to
	 * the collection of key/value pairs. If an element for the given
	 * key already exists, the original element's value is updated.
	 *
	 * @param key   The key of the element
	 * @param value The value of the element
	 * @throws NullPointerException if the key or value argument is null
	 */
	public void put(String key, String value) {
		if (key == null) {
			throw new NullPointerException("key parameter is null");
		}

		if (value == null) {
			throw new NullPointerException("value parameter is null");
		}

		this.fItems.put(key.toLowerCase(), value);
	}

	/**
	 * Adds a new element with a specified key and value to the LookupTable.
	 *
	 * <p>This method adds a new element with a given key and value to the collection of key/value pairs.
	 * If an element for the given key already exists, the original element's value is not updated.</p>
	 *
	 * @param key   The key of the element
	 * @param value The value of the element
	 * @throws NullPointerException if the key or value argument is null
	 */
	public void add(String key, String value) {
		if (!contains(key)) {
			put(key, value);
		}
	}

	/**
	 * Removes an existing element with a given key from this lookup table.
	 * This method removes the element with the given key from the internal list.
	 * Nothing happens if no element with the given key can be found.
	 *
	 * @param key The key of the element to remove
	 * @throws NullPointerException if the key argument is null
	 */
	public void remove(String key) {
		if (key == null) {
			throw new NullPointerException("key parameter is null");
		}

		this.fItems.remove(key.toLowerCase());
	}

	/**
	 * Tests if the collection contains a value for a given key.
	 *
	 * @param key The key to test for
	 * @return True if a value exists for the given key and false otherwise
	 * @throws NullPointerException if the key argument is null
	 */
	public boolean contains(String key) {
		if (key == null) {
			throw new NullPointerException("key parameter is null");
		}

		return this.fItems.containsKey(key.toLowerCase());
	}

	/**
	 * Removes all key/value pairs of the collection
	 */
	public void clear() {
		this.fItems.clear();
	}

	/**
	 * Returns the number of key/value pairs of this collection.
	 *
	 * @return The number of key/value pairs of this collection
	 */
	public int getCount() {
		return this.fItems.size();
	}

	/**
	 * Returns a value of an element for a given key.
	 *
	 * @param key          The key whose value to return
	 * @param defaultValue The value to return if the given key is unknown
	 * @return Either the value for a given key if an element with the given key exists or defaultValue otherwise
	 * @throws NullPointerException If the key argument is null
	 */
	public String getStringValue(String key, String defaultValue) {
		if (key == null) {
			throw new NullPointerException("key parameter is null");
		}

		String value = (String) this.fItems.get(key.toLowerCase());
		return value != null ? value : defaultValue;
	}

	/**
	 * Returns a value of an element converted to a boolean for a given key.
	 * <p>
	 * This method returns a boolean value of true if the found value
	 * of the given key matches either "true", "1" or "yes" and false
	 * otherwise. If the supplied key is unknown, the defaultValue
	 * argument is returned.
	 *
	 * @param key          The key whose value to return. Cannot be null
	 * @param defaultValue The value to return if the given key is unknown
	 * @return Either the value converted to a boolean for the given key if an
	 * element with the given key exists or defaultValue otherwise
	 * @throws NullPointerException if the key argument is null
	 */
	public boolean getBooleanValue(String key, boolean defaultValue) {
		String value = getStringValue(key, null);

		if (value != null) {
			String v = value.toLowerCase().trim();
			return v.equals("true") || v.equals("1") || v.equals("yes");
		} else {
			return defaultValue;
		}
	}

	/**
	 * Returns a value of an element converted to an integer for a given key.
	 * This method returns the defaultValue argument if either the
	 * supplied key is unknown or the found value is not a valid int.
	 * Only non-negative integer values are recognized as valid.
	 *
	 * @param key          The key whose value to return
	 * @param defaultValue The value to return if the given key is unknown
	 * @return Either the value converted to an int for the given key if an
	 * element with the given key exists and the found value is a valid int or defaultValue otherwise
	 * @throws NullPointerException if the key argument is null
	 */
	public int getIntegerValue(String key, int defaultValue) {
		int result = defaultValue;
		String value = getStringValue(key, null);

		if (value != null) {
			value = value.trim();
			if (isValidInteger(value)) {
				result = Integer.parseInt(value);
			}
		}

		return result;
	}

	private static boolean isValidInteger(String value) {
		if (value != null && value.length() > 0) {
			for (int i = 0; i < value.length(); i++) {
				if (!Character.isDigit(value.charAt(i))) {
					return false;
				}
			}

			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns a value of an element converted to a Level value for a given key.
	 * This method returns the defaultValue argument if either the supplied key is unknown
	 * or the found value is not a valid Level value. Please see the Level enum for more
	 * information on the available values.
	 *
	 * @param key          The key whose value to return
	 * @param defaultValue The value to return if the given key is unknown
	 * @return Either the value converted to the corresponding Level value for the given key
	 * if an element with the given key exists and the found value is a valid Level value
	 * or defaultValue otherwise
	 * @throws NullPointerException If the key argument is null
	 */
	public Level getLevelValue(String key, Level defaultValue) {
		Level result = defaultValue;
		String value = getStringValue(key, null);

		if (value != null) {
			value = value.toLowerCase().trim();
			if (value.equals("debug")) {
				result = Level.Debug;
			} else if (value.equals("verbose")) {
				result = Level.Verbose;
			} else if (value.equals("message")) {
				result = Level.Message;
			} else if (value.equals("warning")) {
				result = Level.Warning;
			} else if (value.equals("error")) {
				result = Level.Error;
			} else if (value.equals("fatal")) {
				result = Level.Fatal;
			}
		}

		return result;
	}

	private static boolean isValidSizeUnit(String u) {
		return u.equals("kb") || u.equals("mb") || u.equals("gb");
	}

	/**
	 * Returns a value of an element converted to an integer for a
	 * given key. The integer value is interpreted as a byte size and
	 * it is supported to specify byte units.
	 *
	 * <p>This method returns the defaultValue argument if either the
	 * supplied key is unknown or the found value is not a valid
	 * integer or ends with an unknown byte unit. Only non-negative
	 * integer values are recognized as valid.
	 *
	 * <p>It is possible to specify a size unit at the end of the value.
	 * If a known unit is found, this function multiplies the
	 * resulting value with the corresponding factor. For example, if
	 * the value of the element is "1KB", the return value of this
	 * function would be 1024.</p>
	 *
	 * <p>The following table lists the available units together with a
	 * short description and the corresponding factor:</p>
	 * <table>
	 *   <caption>Size units</caption>
	 *   <tr>
	 *     <th>Unit Name</th>
	 *     <th>Description</th>
	 *     <th>Factor</th>
	 *   </tr>
	 *   <tr>
	 *     <td>KB</td>
	 *     <td>Kilo Byte</td>
	 *     <td>1024</td>
	 *   </tr>
	 *   <tr>
	 *     <td>MB</td>
	 *     <td>Mega Byte</td>
	 *     <td>1024^2</td>
	 *   </tr>
	 *   <tr>
	 *     <td>GB</td>
	 *     <td>Giga Byte</td>
	 *     <td>1024^3</td>
	 *   </tr>
	 * </table>
	 *
	 * <p>If no unit is specified, this function defaults to the KB
	 * unit.</p>
	 *
	 * @param key          The key whose value to return
	 * @param defaultValue The value to return if the given key is unknown
	 * @return Either the value converted to an integer for the given key if
	 * an element with the given key exists and the found value is a
	 * valid integer or defaultValue otherwise
	 * @throws NullPointerException if the key argument is null
	 */
	public long getSizeValue(String key, long defaultValue) {
		long result = defaultValue * KB_FACTOR;
		String value = getStringValue(key, null);

		if (value != null) {
			result = sizeToLong(value, defaultValue);
		}

		return result;
	}

	public static long sizeToLong(String value, long defaultResult) {
		long result = defaultResult;
		int factor = KB_FACTOR;
		value = value.trim();

		if (value.length() >= 2) {
			String unit =
					value.substring(value.length() - 2).toLowerCase();

			if (isValidSizeUnit(unit)) {
				value = value.substring(0, value.length() - 2).trim();

				if (unit.equals("kb")) {
					factor = KB_FACTOR;
				} else if (unit.equals("mb")) {
					factor = MB_FACTOR;
				} else if (unit.equals("gb")) {
					factor = GB_FACTOR;
				}
			}
		}

		if (isValidInteger(value)) {
			try {
				result = factor * Long.parseLong(value);
			} catch (NumberFormatException e) {
				/* Return default */
			}
		}
		return result;
	}

	private static boolean isValidTimespanUnit(String u) {
		return u.equals("s") || u.equals("m") || u.equals("h") ||
				u.equals("d");
	}

	/**
	 * Returns a value of an element converted to an integer for a given key. The integer value is interpreted as a time span, and it is supported to specify time span units.
	 * <p>
	 * This method returns the defaultValue argument if either the supplied key is unknown or the found value is not a valid integer or ends with an unknown time span unit.
	 * <p>
	 * It is possible to specify a time span unit at the end of the value. If a known unit is found, this function multiplies the resulting value with the corresponding factor. For example, if the value of the element is "1s", the return value of this function would be 1000.
	 * <p>
	 * The following table lists the available units together with a short description and the corresponding factor.
	 * <pre>
	 *   Unit Name  Description  Factor
	 *   -          -            -
	 *   s          Seconds      1000
	 *   m          Minutes      60*s
	 *   h          Hours        60*m
	 *   d          Days         24*h
	 * </pre>
	 * <p>
	 * If no unit is specified, this function defaults to the Seconds unit. Please note that the value is always returned in milliseconds.
	 *
	 * @param key          The key whose value to return
	 * @param defaultValue The value to return if the given key is unknown
	 * @return Either the value converted to an integer for the given key if an element with the given key exists and the found value is a valid integer or defaultValue otherwise. The value is returned in milliseconds
	 * @throws NullPointerException if the key argument is null
	 */
	public long getTimespanValue(String key, long defaultValue) {
		long result = defaultValue * SECONDS_FACTOR;
		String value = getStringValue(key, null);

		if (value != null) {
			int factor = SECONDS_FACTOR;
			value = value.trim();

			if (value.length() >= 1) {
				String unit =
						value.substring(value.length() - 1).toLowerCase();

				if (isValidTimespanUnit(unit)) {
					value = value.substring(0, value.length() - 1).trim();

					if (unit.equals("s")) {
						factor = SECONDS_FACTOR;
					} else if (unit.equals("m")) {
						factor = MINUTES_FACTOR;
					} else if (unit.equals("h")) {
						factor = HOURS_FACTOR;
					} else if (unit.equals("d")) {
						factor = DAYS_FACTOR;
					}
				}
			}

			if (isValidInteger(value)) {
				try {
					result = factor * Long.parseLong(value);
				} catch (NumberFormatException e) {
					/* Return default */
				}
			}
		}

		return result;
	}

	/**
	 * Returns a value of an element converted to a FileRotate value for a given key.
	 *
	 * @param key          The key whose value to return
	 * @param defaultValue The value to return if the given key is unknown
	 * @return Either the value converted to a FileRotate value for the given key if an element with
	 * the given key exists and the found value is a valid FileRotate or defaultValue otherwise
	 * @throws NullPointerException If the key argument is null
	 */
	public FileRotate getRotateValue(String key, FileRotate defaultValue) {
		FileRotate result = defaultValue;
		String value = getStringValue(key, null);

		if (value != null) {
			value = value.toLowerCase().trim();
			if (value.equals("none")) {
				result = FileRotate.None;
			} else if (value.equals("hourly")) {
				result = FileRotate.Hourly;
			} else if (value.equals("daily")) {
				result = FileRotate.Daily;
			} else if (value.equals("weekly")) {
				result = FileRotate.Weekly;
			} else if (value.equals("monthly")) {
				result = FileRotate.Monthly;
			}
		}

		return result;
	}

	/**
	 * Returns a Color value of an element for a given key.
	 * The element value must be specified as hexadecimal string.
	 * To indicate that the element value represents a hexadecimal string, the element value must begin with "0x", "{@literal &}H" or "$".
	 * A '0' nibble is appended if the hexadecimal string has an odd length.
	 * The hexadecimal value must represent a three or four byte integer value. The hexadecimal value is handled as follows.
	 * <table border="1">
	 *  <caption>Formats</caption>
	 * 	<tr>
	 * 		<th>Bytes</th>
	 * 		<th>Format</th>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>3</td>
	 * 		<td>RRGGBB</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>4</td>
	 * 		<td>AARRGGBB</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>Other</td>
	 * 		<td>Ignored</td>
	 * 	</tr>
	 * </table>
	 * A stands for the alpha channel and R, G and B represent the red, green and blue channels, respectively.
	 * If the value is not given as hexadecimal value with a length of 6 or 8 characters excluding the hexadecimal prefix identifier or
	 * if the value does not have a valid hexadecimal format, this method returns defaultValue.
	 *
	 * @param key          The key whose value to return
	 * @param defaultValue The value to return if the given key is unknown or if the found value has an invalid format
	 * @return Either the value converted to a Color value for the given key if an element with the given key
	 * exists and the found value has a valid format or defaultValue otherwise
	 * @throws NullPointerException If the key argument is null
	 */
	public Color getColorValue(String key, Color defaultValue) {
		String value = getStringValue(key, null);

		if (value != null) {
			byte[] b = convertHexValue(value.trim());

			if (b == null) {
				return defaultValue;
			}

			switch (b.length) {
				/* Take care of signed/unsigned */

				case 3:
					return new Color(
							0xff & (int) b[0],
							0xff & (int) b[1],
							0xff & (int) b[2]);

				case 4:
					return new Color(
							0xff & (int) b[1],
							0xff & (int) b[2],
							0xff & (int) b[3],
							0xff & (int) b[0]);
			}
		}

		return defaultValue;
	}

	/**
	 * Returns a byte array value of an element for a given key.
	 * The returned byte array always has the desired length as
	 * specified by the size argument. If the element value does
	 * not have the required size after conversion, it is shortened
	 * or padded (with zeros) automatically. This method returns
	 * the defaultValue argument if either the supplied key is
	 * unknown or the found value does not have a valid format
	 * (e.g. invalid characters when using hexadecimal strings).
	 *
	 * @param key          The key whose value to return. This parameter should not be null.
	 *                     Throws NullPointerException if the key argument is null.
	 * @param size         The desired size in bytes of the returned byte array. If
	 *                     the element value does not have the expected size, it is
	 *                     shortened or padded automatically.
	 * @param defaultValue The value to return if the given key is unknown or if the
	 *                     found value has an invalid format.
	 * @return Either the value converted to a byte array for the given key
	 * if an element with the given key exists and the found value
	 * has a valid format or defaultValue otherwise.
	 */

	public byte[] getBytesValue(String key, int size,
								byte[] defaultValue) {
		String value = getStringValue(key, null);

		if (value != null) {
			byte[] b = convertUnicodeValue(value.trim());

			if (b == null) {
				return defaultValue; /* Invalid hex format */
			} else if (b.length == size) {
				return b;
			}

			byte[] r = new byte[size];

			if (b.length > size) {
				System.arraycopy(b, 0, r, 0, r.length);
			} else {
				System.arraycopy(b, 0, r, 0, b.length);
			}

			return r;
		}

		return defaultValue;
	}

	private static String[] HEX_ID =
			{
					"0x", /* C# and Java */
					"&H", /* Visual Basic .NET */
					"$"   /* Object Pascal */
			};

	private static byte[] convertHexValue(String value) {
		for (int i = 0; i < HEX_ID.length; i++) {
			String id = HEX_ID[i];
			if (value.startsWith(id)) {
				value = value.substring(id.length());
				return convertHexString(value);
			}
		}

		return null;
	}

	private static byte[] convertUnicodeValue(String value) {
		try {
			return value.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	private static byte[] HEX_TBL =
			{
					0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f,
					0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f,
					0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f,
					0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f,
					0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f,
					0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f,
					0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
					0x08, 0x09, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f,
					0x7f, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x7f
			};

	private static boolean isValidHex(String value) {
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c >= HEX_TBL.length || HEX_TBL[c] > 0x0f) {
				return false; /* Invalid character */
			}
		}

		return true;
	}

	private static byte[] convertHexString(String value) {
		value = value.toUpperCase();

		if ((value.length() & 1) != 0) /* Odd? */ {
			value = value + "0"; /* Append a 0 nibble */
		}

		byte[] b = null;

		if (isValidHex(value)) {
			b = new byte[value.length() / 2];

			for (int i = 0; i < b.length; i++) {
				byte hi = HEX_TBL[value.charAt(i << 1)];
				byte lo = HEX_TBL[value.charAt((i << 1) + 1)];
				b[i] = (byte) (hi << 4 | lo);
			}
		}

		return b;
	}
}
