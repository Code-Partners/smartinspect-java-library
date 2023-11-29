/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.packets.logentry;

import com.gurock.smartinspect.ViewerId;
import com.gurock.smartinspect.packets.Packet;
import com.gurock.smartinspect.packets.PacketType;

import java.awt.*;

/**
 * Represents the Log Entry packet type which is used for nearly all
 * logging methods in the Session class.
 *
 * <p>
 * A Log Entry is the most important packet available in the
 * SmartInspect concept. It is used for almost all logging methods
 * in the Session class, like, for example, Session.logMessage,
 * Session.logObject or Session.logSql.
 * <p>
 * A Log Entry has several properties which describe its creation
 * context (like a thread ID, timestamp or hostname) and other
 * properties which specify the way the Console interprets this packet
 * (like the viewer ID or the background color). Furthermore a Log
 * Entry contains the actual data which will be displayed in the
 * Console.
 * </p>
 *
 * <p>
 * This class is not guaranteed to be threadsafe. However, instances
 * of this class will normally only be used in the context of a single
 * thread.
 * </p>
 */
public final class LogEntry extends Packet {
	private String fSessionName;
	private String fTitle;
	private String fAppName;
	private String fHostName;
	private LogEntryType fLogEntryType;
	private ViewerId fViewerId;
	private Color fColor;
	private byte[] fData;
	private long fTimestamp;
	private int fThreadId;
	private int fProcessId;

	private static int PROCESS_ID = Runtime.getRuntime().hashCode();
	private static final int HEADER_SIZE = 48;

	/**
	 * Overloaded. Creates and initializes a LogEntry instance.
	 */
	public LogEntry() {

	}

	/**
	 * Overloaded. Creates and initializes a LogEntry instance with
	 * a custom log entry type and custom viewer ID.
	 *
	 * <p>If the logEntryType or viewerId argument is a null reference a
	 * NullPointerException will be thrown.</p>
	 *
	 * @param logEntryType The type of the new Log Entry describes the way the Console
	 *                     interprets this packet. Please see the LogEntryType type for more
	 *                     information. Not allowed to be null
	 * @param viewerId     The viewer ID of the new Log Entry describes which viewer
	 *                     should be used in the Console when displaying the data of
	 *                     this Log Entry. Please see ViewerId for more information.
	 *                     Not allowed to be null
	 * @throws NullPointerException if the logEntryType or viewerId argument
	 *                              is null
	 */
	public LogEntry(LogEntryType logEntryType, ViewerId viewerId) {
		setLogEntryType(logEntryType);
		setViewerId(viewerId);
		this.fThreadId = super.getThreadId();
		this.fProcessId = PROCESS_ID;
	}

	/**
	 * Overridden. Returns the total occupied memory size of this Log Entry packet.
	 *
	 * <p> The total occupied memory size of this Log Entry is the size of memory occupied
	 * by all strings, the optional data block and any internal data structures of this Log Entry.
	 *
	 * @return The total occupied memory size of this Log Entry
	 */
	public int getSize() {
		int result = HEADER_SIZE +
				getStringSize(this.fAppName) +
				getStringSize(this.fSessionName) +
				getStringSize(this.fTitle) +
				getStringSize(this.fHostName);

		if (this.fData != null) {
			result += this.fData.length;
		}

		return result;
	}

	/**
	 * Overridden. Returns PacketType.LogEntry.
	 *
	 * @return Just PacketType.LogEntry. For a complete list of available
	 * packet types, please have a look at the documentation of the
	 * PacketType type
	 */
	public PacketType getPacketType() {
		return PacketType.LogEntry;
	}

	/**
	 * Returns the title of this Log Entry. This method can return null if this
	 * Log Entry does not contain a title. If this is the case,
	 * the title of this Log Entry will be empty in the SmartInspect Console.
	 *
	 * @return The title of the Log Entry or null if the title has not been set
	 */
	public String getTitle() {
		return this.fTitle;
	}

	/**
	 * Sets the title of this Log Entry.
	 * <p>
	 * The title of this Log Entry will be empty in the SmartInspect Console when this method gets a null reference as argument.
	 *
	 * @param title The new title of this Log Entry. Can be null
	 */
	public void setTitle(String title) {
		this.fTitle = title;
	}

	/**
	 * The session name of a Log Entry is normally set to the name of
	 * the session which sent this Log Entry.
	 * <p>
	 * This method can return null if this Log Entry does not contain
	 * a session name. If this is the case, the session name will be
	 * empty in the SmartInspect Console.
	 * <p>
	 * Returns the session name of the Log Entry.
	 *
	 * @return The session name of the Log Entry or null if the session name
	 * has not been set
	 */
	public String getSessionName() {
		return this.fSessionName;
	}

	/**
	 * Sets the session name of the Log Entry.
	 *
	 * <p>The session name of this Log Entry will be empty in the
	 * SmartInspect Console when this method gets a null reference as
	 * argument.</p>
	 *
	 * @param sessionName The new session name of this Log Entry. Can be null
	 */
	public void setSessionName(String sessionName) {
		this.fSessionName = sessionName;
	}

	/**
	 * Returns the hostname of this Log Entry.
	 * <p>
	 * The hostname of a Log Entry is usually set to the name of the machine this Log Entry is sent from.
	 * This method can return null if this Log Entry does not contain a hostname. If this is the case, the hostname will be empty in the SmartInspect Console.
	 *
	 * @return The hostname of the Log Entry or null if the hostname has not been set
	 */
	public String getHostName() {
		return this.fHostName;
	}

	/**
	 * Sets the hostname of this Log Entry.
	 *
	 * <p>The hostname of this Log Entry will be empty in the SmartInspect
	 * Console when this method gets a null reference as argument.</p>
	 *
	 * @param hostName The new hostname of this Log Entry. Can be null
	 */
	public void setHostName(String hostName) {
		this.fHostName = hostName;
	}

	/**
	 * Returns the application name of this Log Entry.
	 * <p>
	 * The application name of a Log Entry is usually set to the name of
	 * the application this Log Entry is created in.
	 * <p>
	 * This method can return null if this Log Entry does not contain an
	 * application name. If this is the case, the application name will
	 * be empty in the SmartInspect Console.
	 *
	 * @return The application name of the Log Entry or null if the application
	 * name has not been set
	 */
	public String getAppName() {
		return this.fAppName;
	}

	/**
	 * Sets the application name of this Log Entry.
	 * <p>
	 * The application name of this Log Entry will be empty in the
	 * SmartInspect Console when this method gets a null reference as
	 * argument.
	 *
	 * @param appName The new application name of this Log Entry. Can be null
	 */
	public void setAppName(String appName) {
		this.fAppName = appName;
	}

	/**
	 * Returns the type of this Log Entry.
	 *
	 * @return The type of this Log Entry. It describes the way the Console
	 * interprets this packet. Please see the LogEntryType enum for more
	 * information
	 */
	public LogEntryType getLogEntryType() {
		return this.fLogEntryType;
	}

	/**
	 * Sets the type of this Log Entry.
	 * <p>
	 * The type of this Log Entry describes the way the Console interprets this packet.
	 * Please see the LogEntryType enum for more information.
	 * If the logEntryType argument is a null reference a NullPointerException will be thrown.
	 *
	 * @param logEntryType The new type of this Log Entry. Not allowed to be null
	 * @throws NullPointerException If the logEntryType argument is null
	 */
	public void setLogEntryType(LogEntryType logEntryType) {
		if (logEntryType == null) {
			throw new NullPointerException("logEntryType");
		} else {
			this.fLogEntryType = logEntryType;
		}
	}

	/**
	 * Returns the viewer ID of this Log Entry.
	 *
	 * @return The viewer ID of the Log Entry. It describes which viewer should
	 * be used in the Console when displaying the data of this Log Entry.
	 * Please see the ViewerId enum for more information
	 */
	public ViewerId getViewerId() {
		return this.fViewerId;
	}

	/**
	 * Sets the viewer ID of this Entry.
	 * <p>
	 * The viewer ID of the Log Entry describes which viewer should
	 * be used in the Console when displaying the data of this Log
	 * Entry. Please see the ViewerId enum for more information.
	 * <p>
	 * If the viewerId argument is a null reference a
	 * NullPointerException will be thrown.
	 *
	 * @param viewerId The new viewer ID of this Log Entry. Not allowed to be null
	 * @throws NullPointerException if the viewerId argument is null
	 */
	public void setViewerId(ViewerId viewerId) {
		if (viewerId == null) {
			throw new NullPointerException("viewerId");
		} else {
			this.fViewerId = viewerId;
		}
	}

	/**
	 * Returns the background color of this Log Entry. The background color of a Log Entry is normally set to the
	 * color of the session which sent this Log Entry. This method can return null if this Log Entry uses the default
	 * background color in the SmartInspect Console.
	 *
	 * @return The background color of the Log Entry or null if this Log
	 */
	public Color getColor() {
		return this.fColor;
	}

	/**
	 * Sets the background color of this Log Entry.
	 * <p>
	 * This Log Entry uses the default background color in the SmartInspect Console when this method gets a null reference as argument.
	 *
	 * @param color The new background color of this Log Entry. Can be null
	 */
	public void setColor(Color color) {
		this.fColor = color;
	}

	/**
	 * Returns the optional data block of the Log Entry.
	 * <p>
	 * This method can return null if this Log Entry does not contain
	 * additional data.
	 * <p>
	 * <b>Important:</b> Treat the returned array as read-only. This
	 * means, modifying this array in any way is not supported.
	 *
	 * @return The optional data block of the Log Entry or null if this Log
	 * Entry does not contain additional data
	 */
	public byte[] getData() {
		return this.fData;
	}

	/**
	 * Sets the optional data block of this Log Entry.
	 *
	 * <p>
	 * Because of the fact that the data block of a Log Entry is
	 * optional, it is allowed to pass a null reference.
	 * </p>
	 * <p>
	 * <b>Important:</b> Treat the passed array as read-only. This
	 * means, modifying this array in any way after passing it to
	 * this method is not supported.
	 * </p>
	 *
	 * @param data The new data block of this Log Entry. Can be null
	 */

	public void setData(byte[] data) {
		this.fData = data;
	}

	/**
	 * Returns the timestamp of this LogEntry object.
	 *
	 * @return The creation time of this Log Entry object as returned by the
	 * SmartInspect.now() method.
	 */
	public long getTimestamp() {
		return this.fTimestamp;
	}

	/**
	 * Sets the timestamp of this LogEntry object.
	 *
	 * @param timestamp The new timestamp of this LogEntry object. The passed value should represent
	 *                  the local date and time in microseconds since January 1, 1970. See SmartInspect.now() for more information.
	 */
	public void setTimestamp(long timestamp) {
		this.fTimestamp = timestamp;
	}

	/**
	 * Returns the process ID of this LogEntry object.
	 *
	 * @return The ID of the process this Log Entry object was created in
	 */
	public int getProcessId() {
		return this.fProcessId;
	}

	/**
	 * Sets the process ID of this LogEntry object.
	 *
	 * @param processId The new process ID of this LogEntry object
	 */
	public void setProcessId(int processId) {
		this.fProcessId = processId;
	}

	/**
	 * Returns the thread ID of this LogEntry object.
	 *
	 * @return The ID of the thread this Log Entry object was created in
	 */
	public int getThreadId() {
		return this.fThreadId;
	}

	/**
	 * Sets the thread ID of this LogEntry object.
	 *
	 * @param threadId The new thread ID of this Log Entry object
	 */
	public void setThreadId(int threadId) {
		this.fThreadId = threadId;
	}
}
