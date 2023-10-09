/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.packets;

import com.gurock.smartinspect.Level;
import com.gurock.smartinspect.SmartInspect;
import com.gurock.smartinspect.protocols.Protocol;

/**
 * This is the abstract base class for all packets in the SmartInspect Java library.
 *
 * <p>This class is the base class for all packets in the SmartInspect
 * Java library. The following table lists the available packets
 * together with a short description:<br>
 * - ControlCommand: Responsible for administrative tasks like clearing the Console.<br>
 * - LogEntry: Represents the most important packet in the entire SmartInspect concept. It is used for the majority of logging methods in the Session class.<br>
 * - LogHeader: Responsible for storing and transferring log metadata. Used by the PipeProtocol and TcpProtocol classes to support the filter and trigger functionality of the SmartInspect Router service application.<br>
 * - ProcessFlow: Responsible for managing thread and process information about your application.<br>
 * - Watch: Responsible for handling variable watches.
 * </p>
 * <p>
 * This class and sub-classes are not guaranteed to be thread-safe.
 * To ensure thread-safety, use setThreadSafe as well as the lock and unlock methods.
 */
public abstract class Packet {
	private Level fLevel;
	private int fBytes;
	private boolean fLocked;
	private Object fLock;
	private boolean fThreadSafe;
	public static final int PACKET_HEADER = 6;

	/**
	 * Creates and initializes a Packet instance with a default log
	 * level of Level.Message.
	 */
	public Packet() {
		this.fLevel = Level.Message;
	}

	/**
	 * Returns the amount of bytes needed for storing this packet
	 * in the standard SmartInspect binary log file format as
	 * represented by BinaryFormatter.
	 * <p>
	 * Please note that this method is only intended to be used by
	 * a possible Java implementation of the SmartInspect SDK. The
	 * SmartInspect SDK is a small library for reading SmartInspect
	 * binary log files and is available for download on the Gurock
	 * Software website.
	 *
	 * @return The amount of bytes needed for storing this packet in the
	 * standard SmartInspect binary log file format
	 */
	public int getBytes() {
		return this.fBytes;
	}

	/**
	 * Sets the amount of bytes needed for storing this packet
	 * in the standard SmartInspect binary log file format as
	 * represented by BinaryFormatter.
	 * <p>
	 * Please note that this method is only intended to be used by
	 * a possible Java implementation of the SmartInspect SDK. The
	 * SmartInspect SDK is a small library for reading SmartInspect
	 * binary log files and is available for download on the Gurock
	 * Software website.
	 *
	 * @param bytes The amount of bytes needed for storing this packet in the
	 *              standard SmartInspect binary log file format
	 */
	public void setBytes(int bytes) {
		this.fBytes = bytes;
	}

	/**
	 * Returns the ID of the current thread.
	 *
	 * <p> This method is intended to be used by derived packet classes
	 * which make use of a thread ID. </p>
	 *
	 * @return The ID the current thread
	 */
	protected int getThreadId() {
		return Thread.currentThread().hashCode();
	}

	/**
	 * Returns the memory size occupied by a string.
	 *
	 * <p>This method calculates and returns the total memory size
	 * occupied by the supplied string. If the supplied argument
	 * is null, 0 is returned.</p>
	 *
	 * @param s The string whose memory size to return. Can be null
	 * @return The memory size occupied by the supplied string or 0 if the
	 * supplied argument is null
	 */
	protected int getStringSize(String s) {
		if (s != null) {
			return s.length() * 2;
		} else {
			return 0;
		}
	}

	/**
	 * Returns the log level of this packet.
	 *
	 * <p> Every packet can have a certain log level value. Log levels
	 * describe the severity of a packet. Please see the Level
	 * enum for more information about log levels and their usage.</p>
	 *
	 * @return The log level of this packet
	 */
	public Level getLevel() {
		return this.fLevel;
	}

	/**
	 * Sets the log level of this packet.
	 * <p>
	 * Every packet can have a certain log level value. Log levels
	 * describe the severity of a packet. Please see the Level
	 * enum for more information about log levels and their usage.
	 * <p>
	 * If the level argument is a null reference a NullPointerException
	 * will be thrown.
	 *
	 * @param level The new log level of this packet. Not allowed to be null
	 * @throws NullPointerException If the level argument is null
	 */
	public void setLevel(Level level) {
		if (level == null) {
			throw new NullPointerException("level argument is null");
		} else {
			this.fLevel = level;
		}
	}

	/**
	 * This method returns the total occupied memory size of this packet.
	 * This functionality is used by the backlog protocol feature to calculate
	 * the total backlog queue size.
	 *
	 * @return The total memory size occupied by this packet
	 */
	public abstract int getSize();

	/**
	 * Intended to return the type of the packet.
	 *
	 * @return The type of this packet. Please see the PacketType type for a
	 * list of available packet types
	 */
	public abstract PacketType getPacketType();

	/**
	 * This method locks this packet for safe multi-threaded packet processing
	 * if this packet is operating in thread-safe mode.
	 *
	 * <p>You need to call this method before reading or changing
	 * properties of a packet when using this packet from multiple
	 * threads at the same time. This is needed, for example, when
	 * one or more {@link SmartInspect#setConnections} connections
	 * of a SmartInspect object are told to operate in
	 * Protocol.isValidOption() asynchronous protocol mode.
	 * Each lock call must be matched by a call to unlock.</p>
	 *
	 * <p>Before using lock and unlock in a multi-threaded environment
	 * you must indicate that this packet should operate in
	 * thread-safe mode by setting the {@link #setThreadSafe} method to true.
	 * Otherwise, the lock and unlock methods do nothing. Note
	 * that setting the {@link #setThreadSafe} method is done automatically
	 * if this packet has been created by the Session class and is
	 * processed by a related SmartInspect object which has one or
	 * more connections which operate in asynchronous protocol
	 * mode.</p>
	 */
	public void lock() {
		if (this.fThreadSafe) {
			synchronized (this.fLock) {
				while (this.fLocked) {
					try {
						this.fLock.wait();
					} catch (InterruptedException e) {
						/* Ignore */
					}
				}

				this.fLocked = true;
			}
		}
	}

	/**
	 * Unlocks a previously locked packet.
	 * <p>
	 * You need to call this method after reading or changing properties of a packet
	 * when using this packet from multiple threads at the same time. This is needed,
	 * for example, when one or more connections of a SmartInspect object are told
	 * to operate in asynchronous protocol mode. Each unlock call must be matched
	 * by a previous call to lock.
	 * <p>
	 * Before using lock and unlock in a multi-threaded environment you must indicate that
	 * this packet should operate in thread-safe mode by setting the setThreadSafe method
	 * to true. Otherwise, the lock and unlock methods do nothing. Note that setting
	 * the setThreadSafe method is done automatically if this packet has been created
	 * by the Session class and is processed by a related SmartInspect object which
	 * has one or more connections which operate in asynchronous protocol mode.
	 */
	public void unlock() {
		if (this.fThreadSafe) {
			synchronized (this.fLock) {
				this.fLocked = false;
				this.fLock.notify(); /* One is enough */
			}
		}
	}

	/**
	 * Indicates if this packet is used in a multi-threaded SmartInspect environment.
	 * If this method returns true, the lock and unlock methods lock this packet
	 * for safe multi-threaded access.
	 * Otherwise, the lock and unlock methods do nothing.
	 * Please see the corresponding setThreadSafe method for more information.
	 *
	 * @return True if this packet is used in a multi-threaded SmartInspect
	 * environment and false otherwise.
	 */
	public boolean isThreadSafe() {
		return this.fThreadSafe;
	}

	/**
	 * Specifies if this packet is used in a multi-threaded SmartInspect environment.
	 * <p>
	 * Set this method to true before calling lock and unlock in a multi-threaded environment.
	 * Otherwise, the lock and unlock methods do nothing. Note that setting this method is done
	 * automatically if this packet has been created by the Session class and is processed
	 * by a related SmartInspect object which has one or more connections which operate
	 * in asynchronous protocol mode.
	 * </p>
	 * <p>
	 * Setting this method must be done before using this packet from multiple
	 * threads simultaneously.
	 * </p>
	 * @param threadSafe True if this packet is used in a multi-threaded SmartInspect
	 * environment and false otherwise
	 */

	public void setThreadSafe(boolean threadSafe) {
		if (threadSafe == this.fThreadSafe) {
			return;
		}

		this.fThreadSafe = threadSafe;

		if (threadSafe) {
			this.fLock = new Object();
		} else {
			this.fLock = null;
		}
	}
}
