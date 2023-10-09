/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * This class is used by the OptionsParser class to inform interested
 * parties about found options. It offers the necessary properties to
 * retrieve the found options in the event handlers.
 * <p>
 * This class is used by the {@link OptionsParser#parse} method.
 * <p>
 * This class is fully threadsafe.
 */
public class OptionsParserEvent extends java.util.EventObject {
	private String fProtocol;
	private String fKey;
	private String fValue;

	/**
	 * Creates and initializes a new OptionsParserEventArgs instance.
	 *
	 * @param source   The object which caused the event
	 * @param protocol The protocol of the new option
	 * @param key      The key of the new option
	 * @param value    The value of the new option
	 */
	public OptionsParserEvent(Object source, String protocol, String key,
							  String value) {
		super(source);
		this.fProtocol = protocol;
		this.fKey = key;
		this.fValue = value;
	}

	/**
	 * This method returns the protocol of the option which has just
	 * been found by a OptionsParser object.
	 *
	 * @return The protocol of the found option
	 */
	public String getProtocol() {
		return this.fProtocol;
	}

	/**
	 * This method returns the key of the option which has just been
	 * found by an OptionsParser object.
	 *
	 * @return The key of the found option.
	 */
	public String getKey() {
		return this.fKey;
	}

	/**
	 * This method returns the value of the option which has just been
	 * found by a OptionsParser object.
	 *
	 * @return The value of the found option.
	 */
	public String getValue() {
		return this.fValue;
	}
}
