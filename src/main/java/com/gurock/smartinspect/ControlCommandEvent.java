/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * This class is used by the SmartInspectListener.onControlCommand
 * event of the SmartInspect class.
 * <p>
 * It has only one public class member named getControlCommand.
 * This member is a method, which just returns the sent packet.
 * </p>
 * <p>
 * This class is fully threadsafe.
 * </p>
 */
public final class ControlCommandEvent extends java.util.EventObject {
	private ControlCommand fControlCommand;

	/**
	 * Creates and initializes a ControlCommandEvent instance.
	 *
	 * @param source         The object which fired the event
	 * @param controlCommand The ControlCommand which has just been sent
	 */
	public ControlCommandEvent(Object source, ControlCommand controlCommand) {
		super(source);
		this.fControlCommand = controlCommand;
	}

	/**
	 * Returns the ControlCommand packet, which has just been sent.
	 *
	 * @return The ControlCommand packet which has just been sent.
	 */
	public ControlCommand getControlCommand() {
		return this.fControlCommand;
	}
}
