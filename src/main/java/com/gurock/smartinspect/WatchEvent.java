/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * This class is used by the SmartInspectListener.onWatch event of
 * the SmartInspect class.
 * It has only one public class member named getWatch. This member is
 * a method, which just returns the sent packet.
 * <p>
 * This class is fully threadsafe.
 */
public final class WatchEvent extends java.util.EventObject {
	private Watch fWatch;

	/**
	 * Creates and initializes a WatchEvent instance.
	 *
	 * @param source The object which fired the event
	 * @param watch  The Watch which has just been sent
	 */
	public WatchEvent(Object source, Watch watch) {
		super(source);
		this.fWatch = watch;
	}

	/**
	 * Returns the Watch packet, which has just been sent.
	 *
	 * @return The Watch packet which has just been sent
	 */
	public Watch getWatch() {
		return this.fWatch;
	}
}

