/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Manages a memory size limited queue of packets.
 * <p>
 * This class is responsible for managing a size limited queue of
 * packets. This functionality is needed by the protocol
 * {@link Protocol#isValidOption backlog} feature. The maximum total
 * memory size of the queue can be set with the setBacklog method.
 * New packets can be added with the push method. Packets which are
 * no longer needed can be retrieved and removed from the queue with
 * the pop method.
 * <p>
 * Note: This class is not guaranteed to be thread safe.
 */
public class PacketQueue {
	private static final int OVERHEAD = 24;
	private long fBacklog;
	private long fSize;
	private PacketQueueItem fHead;
	private PacketQueueItem fTail;
	private int fCount;

	class PacketQueueItem {
		public Packet packet;
		public PacketQueueItem next;
		public PacketQueueItem previous;
	}

	/**
	 * Adds a new packet to the queue.
	 *
	 * <p>
	 * This method adds the supplied packet to the queue. The size of the queue is
	 * incremented by the size of the supplied packet (plus some internal
	 * management overhead). If the total occupied memory size of this queue
	 * exceeds the backlog limit after adding the new packet,
	 * then already added packets will be removed from this queue until the
	 * backlog size limit is reached again.
	 * </p>
	 *
	 * @param packet The packet to add
	 */
	public void push(Packet packet) {
		PacketQueueItem item = new PacketQueueItem();
		item.packet = packet;

		if (this.fTail == null) {
			this.fTail = item;
			this.fHead = item;
		} else {
			this.fTail.next = item;
			item.previous = this.fTail;
			this.fTail = item;
		}

		this.fCount++;
		this.fSize += packet.getSize() + OVERHEAD;
		resize();
	}

	/**
	 * Returns a packet and removes it from the queue. If the queue is not empty, this method removes the oldest packet
	 * from the queue (also known as FIFO) and returns it. The total size of the queue is decremented by the size of the returned
	 * packet (plus some internal management overhead).
	 *
	 * @return The removed packet or null if the queue does not contain any packets
	 */
	public Packet pop() {
		Packet result = null;
		PacketQueueItem item = this.fHead;

		if (item != null) {
			result = item.packet;
			this.fHead = item.next;

			if (this.fHead != null) {
				this.fHead.previous = null;
			} else {
				this.fTail = null;
			}

			this.fCount--;
			this.fSize -= result.getSize() + OVERHEAD;
		}

		return result;
	}

	/**
	 * Removes all packets from the queue.
	 *
	 * <p>Removing all packets of the queue is done by calling the pop
	 * method for each packet in the current queue.</p>
	 */
	public void clear() {
		while (pop() != null) ;
	}

	private void resize() {
		while (this.fBacklog < this.fSize) {
			if (pop() == null) {
				this.fSize = 0;
				break;
			}
		}
	}

	/**
	 * Returns the total maximum memory size of this queue in bytes.
	 * Please see the setBacklog method for more information about the
	 * backlog property.
	 *
	 * @return The total maximum memory size of this queue in bytes
	 */
	public long getBacklog() {
		return this.fBacklog;
	}

	/**
	 * Sets the total maximum memory size of this queue in bytes.
	 *
	 * <p>Each time a new packet is added with the push method, it will
	 * be verified that the total occupied memory size of the queue
	 * still falls below the supplied backlog limit. To satisfy this
	 * constraint, old packets are removed from the queue when
	 * necessary.</p>
	 *
	 * @param backlog The new backlog size
	 */
	public void setBacklog(long backlog) {
		this.fBacklog = backlog;
		resize();
	}

	/**
	 * Returns the current amount of packets in this queue.
	 * <p>
	 * For each added packet this counter is incremented by one
	 * and for each removed packet (either with the pop method
	 * or automatically while resizing the queue) this counter
	 * is decremented by one. If the queue is empty, this method
	 * returns 0.
	 *
	 * @return The current amount of packets in this queue
	 */
	public int getCount() {
		return this.fCount;
	}
}
