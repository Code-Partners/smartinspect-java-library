/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Represents a custom protocol action command as used by the
 * Protocol.dispatch method.
 * <p>
 * This class is used by custom protocol actions. For detailed information
 * about custom protocol actions, please refer to the Protocol.dispatch
 * and SmartInspect.dispatch methods.
 * <p>
 * The public members of this class are threadsafe.
 */

public class ProtocolCommand {
	private int fAction;
	private Object fState;

	/**
	 * Creates and initializes a new ProtocolCommand instance.
	 *
	 * @param action The custom protocol action to execute
	 * @param state  Optional object which provides additional information about the custom protocol action
	 */
	public ProtocolCommand(int action, Object state) {
		this.fAction = action;
		this.fState = state;
	}

	/**
	 * Returns the custom protocol action to execute. The return value
	 * of this method is protocol specific.
	 *
	 * @return The custom protocol action to execute
	 */
	public int getAction() {
		return this.fAction;
	}

	/**
	 * Returns the optional protocol command object which provides
	 * additional information about the custom protocol action. This
	 * method can return null.
	 *
	 * @return The optional protocol command object
	 */
	public Object getState() {
		return this.fState;
	}
}
