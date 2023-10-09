/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * This class is used by the SmartInspectListener.onLogEntry event of
 * the SmartInspect class.
 * <p>
 * It has only one public class member named getLogEntry. This member
 * is a method, which just returns the sent packet.
 * <p>
 * This class is fully threadsafe.
 */
public final class LogEntryEvent extends java.util.EventObject {
	private LogEntry fLogEntry;

	/**
	 * Creates and initializes a LogEntryEvent instance.
	 *
	 * @param source   The object which fired the event
	 * @param logEntry The LogEntry packet which has just been sent
	 */
	public LogEntryEvent(Object source, LogEntry logEntry) {
		super(source);
		this.fLogEntry = logEntry;
	}

	/**
	 * Returns the LogEntry packet, which has just been sent.
	 *
	 * @return The LogEntry packet which has just been sent
	 */
	public LogEntry getLogEntry() {
		return this.fLogEntry;
	}
}
