/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * This class is used by the SmartInspectListener.onError event of the
 * SmartInspect class and the ProtocolListener.onError event of the
 * Protocol class.
 *
 * <p>It has only one public class member named getException. This member
 * is a method, which just returns the occurred exception.</p>
 *
 * <p>This class is fully thread-safe.</p>
 */
public final class ErrorEvent extends java.util.EventObject {
	private Exception fException;

	/**
	 * Creates and initializes an ErrorEvent instance.
	 *
	 * @param source The object which fired the event.
	 * @param e      The occurred exception.
	 */
	public ErrorEvent(Object source, Exception e) {
		super(source);
		this.fException = e;
	}

	/**
	 * Returns the occurred exception.
	 *
	 * @return The occurred exception.
	 */
	public Exception getException() {
		return this.fException;
	}
}
