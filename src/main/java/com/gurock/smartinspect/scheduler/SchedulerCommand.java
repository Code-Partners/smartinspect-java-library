/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.scheduler;

import com.gurock.smartinspect.packets.Packet;

/**
 * Represents a scheduler command as used by the Scheduler class
 * and the asynchronous protocol mode.
 * <p>
 * This class is used by the Scheduler class to enqueue protocol
 * operations for later execution when operating in asynchronous
 * mode. For detailed information about the asynchronous protocol
 * mode, please refer to Protocol.isValidOption.
 * </p>
 * <p>
 * This class is not guaranteed to be threadsafe.
 * </p>
 */
public class SchedulerCommand {
	private SchedulerAction fAction = SchedulerAction.Connect;
	private Object fState;

	/**
	 * Returns the scheduler action to execute. Please refer
	 * to the documentation of the SchedulerAction class for more
	 * information about possible values.
	 *
	 * @return The scheduler action to execute
	 */
	public SchedulerAction getAction() {
		return this.fAction;
	}

	/**
	 * Sets the scheduler action to execute. Please refer to
	 * the documentation of the SchedulerAction class for more
	 * information about possible values.
	 *
	 * @param action The new scheduler action. Null references are ignored
	 */
	public void setAction(SchedulerAction action) {
		if (action != null) {
			this.fAction = action;
		}
	}

	/**
	 * Returns the optional scheduler command state object which
	 * provides additional information about the scheduler command.
	 * This method can return null.
	 *
	 * @return The optional scheduler command state object, can be null
	 */
	public Object getState() {
		return this.fState;
	}

	/**
	 * Sets the optional scheduler command state object which
	 * provides additional information about the scheduler command.
	 *
	 * @param state The new scheduler command state object. Can be null
	 */
	public void setState(Object state) {
		this.fState = state;
	}

	/**
	 * Calculates and returns the total memory size occupied by
	 * this scheduler command.
	 * <p>
	 * This method returns the total occupied memory size of this
	 * scheduler command. This functionality is used by the
	 * asynchronous protocol mode to track the total size of
	 * scheduler commands.
	 *
	 * @return total memory size
	 */
	public int getSize() {
		if (this.fAction != SchedulerAction.WritePacket) {
			return 0;
		}

		if (this.fState != null) {
			return ((Packet) this.fState).getSize();
		} else {
			return 0;
		}
	}
}
