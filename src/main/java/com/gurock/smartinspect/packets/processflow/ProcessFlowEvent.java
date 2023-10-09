/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.packets.processflow;

/**
 * This class is used by the SmartInspectListener.onProcessFlow event
 * of the SmartInspect class. It has only one public class member named getProcessFlow. This
 * member is a method, which just returns the sent packet. This class is fully threadsafe.
 */
public final class ProcessFlowEvent extends java.util.EventObject {
	private ProcessFlow fProcessFlow;

	/**
	 * Creates and initializes a ProcessFlowEvent instance.
	 *
	 * @param source      The object which fired the event
	 * @param processFlow The ProcessFlow packet which has just been sent
	 */
	public ProcessFlowEvent(Object source, ProcessFlow processFlow) {
		super(source);
		this.fProcessFlow = processFlow;
	}

	/**
	 * Returns the ProcessFlow packet, which has just been sent.
	 *
	 * @return The ProcessFlow packet which has just been sent
	 */
	public ProcessFlow getProcessFlow() {
		return this.fProcessFlow;
	}
}
