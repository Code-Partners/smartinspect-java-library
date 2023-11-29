/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.scheduler;

import com.gurock.smartinspect.Enum;

/**
 * Represents a scheduler action to execute when a protocol is
 * operating in asynchronous mode. For general information about
 * the asynchronous mode, please refer to Protocol.IsValidOption
 * This class is fully threadsafe.
 */
public class SchedulerAction extends Enum {
	/**
	 * Represents a connect protocol operation. This action is
	 * enqueued when the Protocol.connect() method is called and
	 * the protocol is operating in asynchronous mode.
	 */
	public static SchedulerAction Connect =
			new SchedulerAction(0, "Connect");

	/**
	 * Represents a write protocol operation. This action is
	 * enqueued when the Protocol.writePacket method is called
	 * and the protocol is operating in asynchronous mode.
	 */
	public static SchedulerAction WritePacket =
			new SchedulerAction(1, "WritePacket");

	/**
	 * Represents a disconnect protocol operation. This action
	 * is enqueued when the Protocol.disconnect() method is called
	 * and the protocol is operating in asynchronous mode.
	 */
	public static SchedulerAction Disconnect =
			new SchedulerAction(2, "Disconnect");

	/**
	 * Represents a dispatch protocol operation. This action is
	 * enqueued when the Protocol.dispatch() method is called and
	 * the protocol is operating in asynchronous mode.
	 */
	public static SchedulerAction Dispatch =
			new SchedulerAction(3, "Dispatch");

	private SchedulerAction(int value, String name) {
		super(value, name);
	}
}
