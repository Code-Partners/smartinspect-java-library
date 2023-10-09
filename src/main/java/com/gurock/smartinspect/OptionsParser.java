/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Responsible for parsing the options part of a SmartInspect connections string.
 * This class offers a single method only, called parse, which is responsible for
 * parsing the options part of a connections string. This method informs the
 * caller about found options with a supplied callback listener.
 *
 * <p>This class is not guaranteed to be threadsafe.</p>
 *
 * @see com.gurock.smartinspect.OptionsParserListener
 */
public class OptionsParser {
	private void doOption(OptionsParserListener callback, String protocol,
						  String key, String value) throws SmartInspectException {
		value = value.trim();
		key = key.toLowerCase().trim();

		OptionsParserEvent e =
				new OptionsParserEvent(this, protocol, key, value);

		callback.onOption(e);
	}

	private void internalParse(String protocol, String options,
							   OptionsParserListener callback) throws SmartInspectException {
		char c;
		StringBuffer key = new StringBuffer();
		StringBuffer value = new StringBuffer();

		for (int i = 0, length = options.length(); i < length; ) {
			// Store key
			c = options.charAt(i);
			while (i++ < length - 1) {
				key.append(c);
				c = options.charAt(i);
				if (c == '=') {
					break;
				}
			}

			if (c != '=') {
				// The options string is invalid because the '='
				// character is missing.
				throw new SmartInspectException(
						"Missing \"=\" at \"" + protocol + "\" protocol"
				);
			} else if (i < length) {
				i++;
			}

			// Store value
			boolean quoted = false;
			while (i < length) {
				c = options.charAt(i++);
				if (c == '"') {
					if (i < length) {
						if (options.charAt(i) != '"') {
							quoted = !quoted;
							continue;
						} else {
							i++; // Skip one '"'
						}
					} else {
						quoted = !quoted;
						continue;
					}
				} else if (c == ',' && !quoted) {
					break;
				}

				value.append(c);
			}

			doOption(callback, protocol, key.toString(), value.toString());
			key.setLength(0);
			value.setLength(0);
		}
	}

	/**
	 * Parses the options part of a connections string and informs the caller about found options with the
	 * supplied callback listener.
	 * For information about the correct syntax of the options,
	 * please refer to the documentation of the Protocol.setOptions
	 * method.
	 * <p>
	 *
	 * @param protocol The related protocol. Not allowed to be null
	 * @param options  The options to parse. Not allowed to be null
	 * @param callback The callback listener which should be informed about found options. Not allowed to be null
	 * @throws NullPointerException  If the protocol, options or callback argument is null
	 * @throws SmartInspectException If the options string syntax is invalid
	 * @see com.gurock.smartinspect.OptionsParserListener
	 */
	public void parse(String protocol, String options,
					  OptionsParserListener callback) throws SmartInspectException {
		if (protocol == null) {
			throw new NullPointerException("protocol");
		} else if (options == null) {
			throw new NullPointerException("options");
		} else if (callback == null) {
			throw new NullPointerException("callback");
		} else {
			options = options.trim();
			if (options.length() > 0) {
				internalParse(protocol, options, callback);
			}
		}
	}
}
