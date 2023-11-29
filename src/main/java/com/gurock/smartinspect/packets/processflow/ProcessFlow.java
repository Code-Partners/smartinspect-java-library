/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.packets.processflow;

import com.gurock.smartinspect.packets.Packet;
import com.gurock.smartinspect.packets.PacketType;

/**
 * Represents the Process Flow packet type which is used in the
 * enter/leave methods in the Session class.
 * <p>
 * A Process Flow entry is responsible for illustrated process and
 * thread information.
 * <p>
 * It has several properties which describe its creation context (like
 * a thread ID, timestamp or hostname) and other properties which
 * specify the way the Console interprets this packet (like the process
 * flow ID). Furthermore, a Process Flow entry contains the actual data,
 * namely the title, which will be displayed in the Console.
 * <p>
 * This class is not guaranteed to be thread-safe. However, instances
 * of this class will normally only be used in the context of a single
 * thread.
 */
public final class ProcessFlow extends Packet {
	private String fHostName;
	private ProcessFlowType fProcessFlowType;
	private String fTitle;
	private long fTimestamp;
	private int fThreadId;
	private int fProcessId;

	private static int PROCESS_ID = Runtime.getRuntime().hashCode();
	private static final int HEADER_SIZE = 28;

	/**
	 * Overloaded. Creates and initializes a ProcessFlow instance.
	 */
	public ProcessFlow() {

	}

	/**
	 * Creates and initializes a ProcessFlow instance with a custom process flow type.
	 *
	 * <p>If the processFlowType argument is a null reference a NullPointerException will be thrown.</p>
	 *
	 * @param processFlowType The type of the new Process Flow entry describes the way the
	 *                        Console interprets this packet. Please see the ProcessFlowType
	 *                        enum for more information. Now allowed to be null
	 * @throws NullPointerException if the processFlowType argument is null
	 */
	public ProcessFlow(ProcessFlowType processFlowType) {
		setProcessFlowType(processFlowType);
		this.fThreadId = super.getThreadId();
		this.fProcessId = PROCESS_ID;
	}

	/**
	 * Overridden. Returns the total occupied memory size of this Process Flow packet.
	 * The total occupied memory size of this Process Flow entry is the size of memory
	 * occupied by all strings and any internal data structures of this Process Flow entry.
	 *
	 * @return The total occupied memory size of this Process Flow entry
	 */
	public int getSize() {
		return HEADER_SIZE +
				getStringSize(this.fTitle) +
				getStringSize(this.fHostName);
	}

	/**
	 * Overridden. Returns PacketType.ProcessFlow.
	 *
	 * @return PacketType.ProcessFlow. For a complete list of available
	 * packet types, please have a look at the documentation of the
	 * PacketType enum
	 */
	public PacketType getPacketType() {
		return PacketType.ProcessFlow;
	}

	/**
	 * Returns the title of this Process Flow entry.
	 *
	 * <p>
	 * This method can return null if this Process Flow entry does
	 * not contain a title. If this is the case, it will be empty in
	 * the SmartInspect Console.
	 *
	 * @return The title of the Process Flow or null if the title has not
	 * been set
	 */
	public String getTitle() {
		return this.fTitle;
	}

	/**
	 * Sets the title of the Process Flow Entry.
	 * <p>
	 * The title of this Process Flow entry will be empty in the
	 * SmartInspect Console when this method gets a null reference as
	 * argument.
	 *
	 * @param title The new title of this Process Flow entry. Can be null
	 */
	public void setTitle(String title) {
		this.fTitle = title;
	}

	/**
	 * Returns the hostname of this Process Flow entry.
	 * <p>
	 * The hostname of this Process Flow entry is usually set to the
	 * name of the machine this Process Flow entry is sent from.
	 * <p>
	 * This method can return null if this Process Flow entry does not
	 * contain a hostname. If this is the case, it will be empty in the
	 * SmartInspect Console.
	 *
	 * @return The hostname of the machine, which sent this Process Flow entry
	 * or null if the hostname has not been set
	 */
	public String getHostName() {
		return this.fHostName;
	}

	/**
	 * Sets the hostname of this Process Flow Entry.
	 *
	 * <p>The hostname of this Process Flow entry will be empty in the
	 * SmartInspect Console when this method gets a null reference as
	 * argument.</p>
	 *
	 * @param hostName The new hostname of this Process Flow entry. Can be null
	 */
	public void setHostName(String hostName) {
		this.fHostName = hostName;
	}

	/**
	 * Returns the type of this Process Flow entry.
	 *
	 * @return The type of the Process Flow entry. It describes the way the
	 * Console interprets this packet. Please see the ProcessFlowType enum
	 * for more information
	 */
	public ProcessFlowType getProcessFlowType() {
		return this.fProcessFlowType;
	}

	/**
	 * Sets the type of this Process Flow entry.
	 * <p>
	 * The type of the Process Flow entry describes the way the Console interprets this packet.
	 * Please see the ProcessFlowType enum for more information.
	 * If the processFlowType argument is a null reference a NullPointerException will be thrown.
	 *
	 * @param processFlowType The new type of this Process Flow entry. Not allowed to be null
	 * @throws NullPointerException if the processFlowType argument is null
	 */
	public void setProcessFlowType(ProcessFlowType processFlowType) {
		if (processFlowType == null) {
			throw new NullPointerException("processFlowType");
		} else {
			this.fProcessFlowType = processFlowType;
		}
	}

	/**
	 * Returns the timestamp of this ProcessFlow object.
	 *
	 * @return The creation time of this Process Flow object as returned by the SmartInspect.now method
	 */
	public long getTimestamp() {
		return this.fTimestamp;
	}

	/**
	 * Sets the timestamp of this ProcessFlow object.
	 *
	 * @param timestamp The new timestamp of this ProcessFlow object. The passed value
	 *                  should represent the local date and time in microseconds since
	 *                  January 1, 1970. See SmartInspect.now() for more information
	 */
	public void setTimestamp(long timestamp) {
		this.fTimestamp = timestamp;
	}

	/**
	 * Returns the process ID of this ProcessFlow object.
	 *
	 * @return The ID of the process this Process Flow object was created in
	 */
	public int getProcessId() {
		return this.fProcessId;
	}

	/**
	 * Sets the process ID of this ProcessFlow object.
	 *
	 * @param processId The new process ID of this Process Flow object
	 */
	public void setProcessId(int processId) {
		this.fProcessId = processId;
	}

	/**
	 * Returns the thread ID of this ProcessFlow object.
	 *
	 * @return The ID of the thread this Process Flow object was created in
	 */
	public int getThreadId() {
		return this.fThreadId;
	}

	/**
	 * Sets the thread ID of this ProcessFlow object.
	 *
	 * @param threadId The new thread ID of this Process Flow object.
	 */
	public void setThreadId(int threadId) {
		this.fThreadId = threadId;
	}
}
