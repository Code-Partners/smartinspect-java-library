/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import com.gurock.smartinspect.packets.Packet;

/**
 * This class is used by the SmartInspectListener.onFilter event of the SmartInspect class.
 * <p>
 * This class consists of only three class members. At first we have the getPacket method, which returns the
 * packet which caused the event. Then there are the getCancel and setCancel methods which can be used to cancel
 * the processing of certain packets. For more information, please refer to the SmartInspectListener.onFilter
 * documentation.
 * <p>
 * This class is fully threadsafe.
 */
public class FilterEvent extends java.util.EventObject {
	private Packet fPacket;
	private boolean fCancel;

	/**
	 * Creates and initializes a FilterEvent instance.
	 *
	 * @param source The object which fired the event.
	 * @param packet The packet which caused the event.
	 */
	public FilterEvent(Object source, Packet packet) {
		super(source);
		this.fCancel = false;
		this.fPacket = packet;
	}

	/**
	 * Returns the packet which caused the event.
	 *
	 * @return The packet which caused the event.
	 */
	public Packet getPacket() {
		return this.fPacket;
	}

	/**
	 * Indicates if processing of the current packet should be cancelled or not.
	 * <p>
	 * For more information, please refer to the documentation of the
	 * setCancel method or the SmartInspectListener.onFilter event of
	 * the SmartInspect class.
	 *
	 * @return True if processing of the current packet should be cancelled and false otherwise.
	 */
	public boolean getCancel() {
		return this.fCancel;
	}

	/**
	 * This method can be used to cancel the processing of certain
	 * packets during the SmartInspectListener.onFilter event of the
	 * SmartInspect class. For more information on how to use this method, please refer to
	 * the SmartInspectListener.onFilter event documentation.
	 *
	 * @param cancel Specifies if processing of the current packet should be cancelled
	 *               or not.
	 */
	public void setCancel(boolean cancel) {
		this.fCancel = cancel;
	}
}
