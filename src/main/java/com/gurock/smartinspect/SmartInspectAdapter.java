/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Represents the adapter class for the SmartInspectListener event
 * interface of the SmartInspect class.
 * <p>
 * This class is added for convenience when dealing with the SmartInspect
 * event system. This class implements the SmartInspectListener interface
 * by defining empty methods stubs. By deriving from this class instead
 * of implementing the SmartInspectListener interface directly, you can
 * subscribe to certain events by overriding the appropriate methods and
 * ignore other events which are of no use to you by keeping the empty
 * default implementation of this class.
 */
public class SmartInspectAdapter implements SmartInspectListener {
	/**
	 * Provides an empty default implementation for the error event of
	 * the implemented SmartInspectListener event interface.
	 *
	 * <p>This method provides an empty default implementation for the error
	 * event. When deriving from this class, override this method if you
	 * are interested in getting notified about errors. For an example,
	 * please refer to the SmartInspectListener.onError method.</p>
	 *
	 * @param e The event argument for the event handler
	 */
	public void onError(ErrorEvent e) {

	}

	/**
	 * Provides an empty default implementation for the ControlCommand
	 * event of the implemented SmartInspectListener event interface.
	 * This method provides an empty default implementation for the
	 * ControlCommand event. When deriving from this class, override
	 * this method if you are interested in getting notified about sent
	 * ControlCommand packets. For an example, please refer to the
	 * documentation of the SmartInspectListener.onControlCommand method.
	 *
	 * @param e The event argument for the event handler
	 */
	public void onControlCommand(ControlCommandEvent e) {

	}

	/**
	 * Provides an empty default implementation for the LogEntry event
	 * of the implemented SmartInspectListener event interface.
	 * <p>
	 * This method provides an empty default implementation for the
	 * LogEntry event. When deriving from this class, override this method
	 * if you are interested in getting notified about sent LogEntry
	 * packets. For an example, please refer to the documentation of the
	 * SmartInspectListener.onLogEntry method.
	 *
	 * @param e The event argument for the event handler
	 */
	public void onLogEntry(LogEntryEvent e) {

	}

	/**
	 * Provides an empty default implementation for the ProcessFlow event
	 * of the implemented SmartInspectListener event interface.
	 * <p>
	 * This method provides an empty default implementation for the
	 * ProcessFlow event. When deriving from this class, override this
	 * method if you are interested in getting notified about sent
	 * ProcessFlow packets. For a detailed example, please refer to the
	 * documentation of the SmartInspectListener.onLogEntry method.
	 *
	 * @param e The event argument for the event handler
	 */
	public void onProcessFlow(ProcessFlowEvent e) {

	}

	/**
	 * Provides an empty default implementation for the Watch event of
	 * the implemented SmartInspectListener event interface.
	 * <p>
	 * This method provides an empty default implementation for the
	 * Watch event. When deriving from this class, override this method
	 * if you are interested in getting notified about sent Watch packets.
	 * For a detailed example, please refer to the documentation of the
	 * SmartInspectListener.onWatch method.
	 *
	 * @param e The event argument for the event handler
	 */
	public void onWatch(WatchEvent e) {

	}

	/**
	 * Provides an empty default implementation for the Filter event of
	 * the implemented SmartInspectListener event interface.
	 * This method provides an empty default implementation for the
	 * Filter event. When deriving from this class, override this method
	 * if you are interested in filtering out any packets. For a detailed
	 * example, please refer to the SmartInspectListener.onFilter method.
	 *
	 * @param e The event argument for the event handler
	 */
	public void onFilter(FilterEvent e) {

	}
}
