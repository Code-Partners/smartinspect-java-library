/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Represents the Control Command packet type which is used for
 * administrative tasks like resetting or clearing the Console.
 * <p>
 * A Control Command is used for several administrative Console tasks.
 * Among other things, this packet type allows you to reset the Console.
 * <p>
 * This class is not guaranteed to be threadsafe. However, instances
 * of this class will normally only be used in the context of a single
 * thread.
 */
public final class ControlCommand extends Packet {
	private byte[] fData;
	private ControlCommandType fControlCommandType;
	private static final int HEADER_SIZE = 8;

	/**
	 * Overloaded. Creates and initializes a ControlCommand instance.
	 */
	public ControlCommand() {

	}

	public ControlCommand(ControlCommandType controlCommandType) {
		setControlCommandType(controlCommandType);
	}

	/**
	 * Overridden. Returns the total occupied memory size of this Control Command packet.
	 * The total occupied memory size of this Control Command is the size of memory occupied the optional data block
	 * and any internal data structures of this Control Command.
	 *
	 * @return The total occupied memory size of this Control Command.
	 */
	public int getSize() {
		int result = HEADER_SIZE;

		if (this.fData != null) {
			result += this.fData.length;
		}

		return result;
	}

	/**
	 * Overridden. Returns PacketType.ControlCommand.
	 *
	 * @return PacketType.ControlCommand. For a complete list of available
	 * packet types, please have a look at the documentation of the
	 * PacketType type.
	 */
	public PacketType getPacketType() {
		return PacketType.ControlCommand;
	}

	/**
	 * Returns the type of this Control Command.
	 * <p>
	 * The type of the Control Command describes the way the Console
	 * interprets this packet. Please see the ControlCommandType type
	 * for more information.
	 *
	 * @return The type of this Control Command packet.
	 */
	public ControlCommandType getControlCommandType() {
		return this.fControlCommandType;
	}

	/**
	 * Sets the type of this Control Command.
	 * <p>
	 * The type of the Control Command describes the way the Console
	 * interprets this packet. Please see the ControlCommandType type
	 * for more information.
	 * <p>
	 * If the controlCommandType argument is a null reference a
	 * NullPointerException will be thrown.
	 *
	 * @param controlCommandType The new type of this Control Command packet. Not allowed to be null.
	 * @throws NullPointerException if the controlCommandType argument is null.
	 */
	public void setControlCommandType(ControlCommandType controlCommandType) {
		if (controlCommandType == null) {
			throw new NullPointerException("controlCommandType");
		} else {
			this.fControlCommandType = controlCommandType;
		}
	}

	/**
	 * Returns the optional data block of the Control Command.
	 * <p>
	 * This method can return null if this Control Command does not
	 * contain additional data.
	 * <p>
	 * <b>Important:</b> Treat the returned array as read-only. This
	 * means, modifying this array in any way is not supported.
	 *
	 * @return The optional data block of the Control Command or null if this
	 * Control Command does not contain additional data.
	 */
	public byte[] getData() {
		return this.fData;
	}

	/**
	 * Sets the optional data block of the Control Command.
	 * <p>
	 * Because of the fact that the data block of a Control Command
	 * is optional, it is allowed to pass a null reference.
	 * <b>Important:</b> Treat the passed array as read-only. This
	 * means, modifying this array in any way after passing it to this
	 * method is not supported.
	 *
	 * @param data The new data block of this Control Command. Can be null.
	 */
	public void setData(byte[] data) {
		this.fData = data;
	}
}
