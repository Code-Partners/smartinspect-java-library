/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Manages a queue of scheduler commands.
 * <p>
 * This class is responsible for managing a queue of scheduler
 * commands. This functionality is needed by the asynchronous protocol mode
 * and the Scheduler class. New commands can be added with the
 * enqueue method. Commands can be dequeued with dequeue. This
 * queue does not have a maximum size or count.
 * <p>
 * This class is not guaranteed to be thread-safe.
 */
public class SchedulerQueue {
	private static final int OVERHEAD = 24;
	private long fSize;
	private int fCount;
	private SchedulerQueueItem fHead;
	private SchedulerQueueItem fTail;

	class SchedulerQueueItem {
		public SchedulerCommand command;
		public SchedulerQueueItem next;
		public SchedulerQueueItem previous;
	}

	/**
	 * Adds a new scheduler command to the queue.
	 * <p>
	 * This method adds the supplied scheduler command to the queue.
	 * The size of the queue is incremented by the
	 * size of the supplied command (plus some internal management
	 * overhead). This queue does not have a maximum size or count.
	 *
	 * @param command The command to add
	 */
	public void enqueue(SchedulerCommand command) {
		SchedulerQueueItem item = new SchedulerQueueItem();
		item.command = command;
		add(item);
	}

	private void add(SchedulerQueueItem item) {
		if (this.fTail == null) {
			this.fTail = item;
			this.fHead = item;
		} else {
			this.fTail.next = item;
			item.previous = this.fTail;
			this.fTail = item;
		}

		this.fCount++;
		this.fSize += item.command.getSize() + OVERHEAD;
	}

	/**
	 * Returns a scheduler command and removes it from the queue.
	 * If the queue is not empty, this method removes the oldest
	 * scheduler command from the queue (also known as FIFO) and
	 * returns it. The total size of the queue is
	 * decremented by the size of the returned command (plus some
	 * internal management overhead).
	 *
	 * @return The removed scheduler command or null if the queue does not contain any packets
	 */
	public SchedulerCommand dequeue() {
		SchedulerQueueItem item = this.fHead;

		if (item != null) {
			remove(item);
			return item.command;
		} else {
			return null;
		}
	}

	private void remove(SchedulerQueueItem item) {
		if (item == this.fHead) /* Head */ {
			this.fHead = item.next;
			if (this.fHead != null) {
				this.fHead.previous = null;
			} else /* Was also tail */ {
				this.fTail = null;
			}
		} else {
			item.previous.next = item.next;
			if (item.next == null) /* Tail */ {
				this.fTail = item.previous;
			} else {
				item.next.previous = item.previous;
			}
		}

		this.fCount--;
		this.fSize -= item.command.getSize() + OVERHEAD;
	}

	/**
	 * Tries to skip and remove scheduler commands from this queue.
	 * This method removes the next WritePacket scheduler commands from this queue
	 * until the specified minimum amount of bytes has been removed.
	 * Administrative scheduler commands (connect, disconnect or dispatch) are not removed.
	 * If the queue is currently empty or does not contain enough WritePacket
	 * commands to achieve the specified minimum amount of bytes, this method returns false.
	 *
	 * @param size The minimum amount of bytes to remove from this queue
	 * @return True if enough scheduler commands could be removed and false otherwise
	 */
	public boolean trim(int size) {
		if (size <= 0) {
			return true;
		}

		int removedBytes = 0;
		SchedulerQueueItem item = this.fHead;

		while (item != null) {
			if (item.command.getAction() == SchedulerAction.WritePacket) {
				removedBytes += item.command.getSize() + OVERHEAD;
				remove(item);

				if (removedBytes >= size) {
					return true;
				}
			}

			item = item.next;
		}

		return false;
	}

	/**
	 * Removes all scheduler commands from this queue.
	 * <p>
	 * Removing all scheduler commands of the queue is done by
	 * calling the dequeue method for each command in the current
	 * queue
	 */
	public void clear() {
		while (dequeue() != null) ;
	}

	/**
	 * Returns the current amount of scheduler commands in this queue.
	 * <p>
	 * For each added scheduler command this counter is incremented
	 * by one and for each removed command (with dequeue) this
	 * counter is decremented by one. If the queue is empty, this
	 * method returns 0.
	 *
	 * @return The current amount of scheduler commands in this queue
	 */
	public int getCount() {
		return this.fCount;
	}

	/**
	 * Returns the current size of this queue in bytes.
	 * <p>
	 * For each added scheduler command this counter is incremented
	 * by the size of the command (plus some internal management
	 * overhead) and for each removed command (with dequeue) this
	 * counter is then decremented again. If the queue is empty,
	 * this method returns 0.
	 *
	 * @return The current size of this queue in bytes
	 */
	public long getSize() {
		return this.fSize;
	}
}
