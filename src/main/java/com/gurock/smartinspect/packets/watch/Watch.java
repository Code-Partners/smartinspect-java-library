/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.packets.watch;

import com.gurock.smartinspect.packets.Packet;
import com.gurock.smartinspect.packets.PacketType;

/**
 * Represents the Watch packet type which is used in the watch methods in the Session classes.
 * <p>
 * A Watch is responsible for sending variables and their values to the Console. These key/value pairs will be
 * displayed in the Watches toolbox. If a Watch with the same name is sent twice, the old value is overwritten
 * and the Watches toolbox displays the most current value.
 * <p>
 * Note: This class is not guaranteed to be thread safe. However, instances of this class will normally only
 * be used in the context of a single thread.
 */
public final class Watch extends Packet {
	private WatchType fWatchType;
	private String fName;
	private String fValue;
	private long fTimestamp;

	private static final int HEADER_SIZE = 20;

	/**
	 * Overloaded. Creates and initializes a Watch instance.
	 */
	public Watch() {

	}

	/**
	 * Overloaded. Creates and initializes a Watch instance with a
	 * custom watch type.
	 *
	 * <p> If the watchType argument is a null reference a
	 * NullPointerException will be thrown. </p>
	 *
	 * @param watchType The type of the new Watch describes the variable type (String,
	 *                  Integer and so on). Please see the WatchType enum for more
	 *                  information. Not allowed to be null
	 * @throws NullPointerException if the watchType argument is null
	 */
	public Watch(WatchType watchType) {
		setWatchType(watchType);
	}

	/**
	 * Overridden. Returns the total occupied memory size of this Watch packet.
	 * The total occupied memory size of this Watch is the size of memory occupied by all strings and any internal data structures of this Watch.
	 *
	 * @return The total occupied memory size of this Watch
	 */
	public int getSize() {
		return HEADER_SIZE +
				getStringSize(this.fName) +
				getStringSize(this.fValue);
	}

	/**
	 * Overridden. Returns PacketType.Watch.
	 *
	 * @return PacketType.Watch. For a complete list of available packet
	 * types, please refer to the documentation of the PacketType enum
	 */
	public PacketType getPacketType() {
		return PacketType.Watch;
	}

	/**
	 * Returns the name of this Watch.
	 * <p>
	 * If a Watch with the same name is sent twice, the old value is
	 * overwritten and the Watches toolbox displays the most current
	 * value.
	 * <p>
	 * This method can return null if this Watch does not contain a
	 * name. If this is the case, it will be empty in the SmartInspect
	 * Console.
	 *
	 * @return The name of this Watch or null if the name of this Watch has not been set
	 */
	public String getName() {
		return this.fName;
	}

	/**
	 * Sets the name of this Watch.
	 *
	 * <p>If a Watch with the same name is sent twice, the old value is
	 * overwritten and the Watches toolbox displays the most current
	 * value. The name of this Watch will be empty in the SmartInspect
	 * Console when this method gets a null reference as argument.</p>
	 *
	 * @param name The new name of this Watch, can be null
	 */
	public void setName(String name) {
		this.fName = name;
	}

	/**
	 * Returns the value of this Watch.
	 * The value of a Watch is always sent as String. To view the
	 * type of this variable Watch, please have a look at the
	 * getWatchType method.
	 * This method can return null if this Watch does not contain a
	 * value. If this is the case, it will be empty in the SmartInspect
	 * Console.
	 *
	 * @return The value of this Watch or null if the value of this Watch has
	 * not been set
	 */
	public String getValue() {
		return this.fValue;
	}

	/**
	 * Sets the value of this Watch.
	 * <p>
	 * The value of a Watch is always sent as String. To change the
	 * type of this variable Watch, please have a look at the
	 * setWatchType method.
	 * <p>
	 * The value of this Watch will be empty in the SmartInspect
	 * Console when this method gets a null reference as argument.
	 *
	 * @param value The new value of this Watch, can be null
	 */
	public void setValue(String value) {
		this.fValue = value;
	}

	/**
	 * Returns the type of this Watch.
	 *
	 * @return The type of this Watch. It describes the variable type (String,
	 * Integer and so on). Please see the WatchType enum for more
	 * information.
	 */
	public WatchType getWatchType() {
		return this.fWatchType;
	}

	/**
	 * Sets the type of this Watch.
	 *
	 * <p>The type of this Watch describes the variable type (String,
	 * Integer and so on). Please see the WatchType enum for more
	 * information.
	 *
	 * <p>If the watchType argument is a null reference a
	 * NullPointerException will be thrown.
	 *
	 * @param watchType The new type of this Watch. Not allowed to be null.
	 * @throws NullPointerException The watchType argument is null.
	 */
	public void setWatchType(WatchType watchType) {
		if (watchType == null) {
			throw new NullPointerException("watchType argument is null");
		} else {
			this.fWatchType = watchType;
		}
	}

	/**
	 * Returns the timestamp of this Watch object.
	 *
	 * @return the creation time of this Watch object as returned by the
	 * SmartInspect.now() method
	 */
	public long getTimestamp() {
		return this.fTimestamp;
	}

	/**
	 * Sets the timestamp of this Watch object.
	 *
	 * @param timestamp The new timestamp of this Watch object. The passed value should
	 *                  represent the local date and time in microseconds since January
	 *                  1, 1970. See SmartInspect.now() for more information
	 */
	public void setTimestamp(long timestamp) {
		this.fTimestamp = timestamp;
	}
}
