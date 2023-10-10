/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Responsible for scheduling protocol operations and executing
 * them asynchronously in a different thread of control.
 * <p>
 * This class is used by the asynchronous protocol mode to asynchronously execute protocol
 * operations. New commands can be scheduled for execution with
 * the schedule method. The scheduler can be started and stopped
 * with the start and stop methods. The scheduler uses a size
 * limited queue to buffer scheduler commands. The maximum size of
 * this queue can be set with the setThreshold method. To influence
 * the behavior of the scheduler if new commands are enqueued and
 * the queue is currently considered full, you can specify the
 * throttle mode.
 * <p>
 * This class is guaranteed to be threadsafe.
 */
public class Scheduler {
	class SchedulerThread extends Thread {
		public void run() {
			while (true) {
				int count = dequeue();

				if (count == 0) {
					break; /* Stopped and no more commands */
				}

				if (!runCommands(count)) {
					break; /* Stopped */
				}
			}

			fBuffer = null; /* No longer needed */
		}

		private boolean runCommands(int count) {
			for (int i = 0; i < count; i++) {
				boolean stopped = fStopped; /* See below. */

				SchedulerCommand command = fBuffer[i];
				runCommand(command);
				fBuffer[i] = null;

				if (!stopped) {
					continue;
				}

				/* The scheduler has been stopped before the last
				 * command has been processed. To shutdown this
				 * thread as fast as possible we check if the last
				 * command of the protocol has failed (or if the
				 * last command has failed to change the previous
				 * failure status, respectively). If this is the
				 * case, we clear the queue and exit this thread
				 * immediately. */

				if (fProtocol.failed()) {
					clear();
					return false;
				}
			}

			return true;
		}

		private void runCommand(SchedulerCommand command) {
			/* Process the dequeued command. The Impl method cannot
			 * throw an exception. Exceptions are reported with the
			 * error event of the protocol in asynchronous mode. */

			SchedulerAction action = command.getAction();

			try {
				if (action == SchedulerAction.Connect) {
					fProtocol.implConnect();
				} else if (action == SchedulerAction.WritePacket) {
					Packet packet = (Packet) command.getState();
					fProtocol.implWritePacket(packet);
				} else if (action == SchedulerAction.Disconnect) {
					fProtocol.implDisconnect();
				} else if (action == SchedulerAction.Dispatch) {
					ProtocolCommand cmd = (ProtocolCommand)
							command.getState();
					fProtocol.implDispatch(cmd);
				}
			} catch (Exception e) {
				/* Cannot happen, see above, but required */
			}
		}
	}

	private static final int BUFFER_SIZE = 0x10;

	private Thread fThread;
	private SchedulerQueue fQueue;
	private Object fMonitor;
	private boolean fThrottle;
	private long fThreshold;
	private Protocol fProtocol;
	private boolean fStopped;
	private boolean fStarted;
	private SchedulerCommand[] fBuffer;

	/**
	 * Creates and initializes a new Scheduler instance.
	 *
	 * @param protocol The protocol on which to execute the actual operations like
	 *                 connect, disconnect, write or dispatch
	 */
	public Scheduler(Protocol protocol) {
		this.fProtocol = protocol;
		this.fMonitor = new Object();
		this.fQueue = new SchedulerQueue();
		this.fBuffer = new SchedulerCommand[BUFFER_SIZE];
	}

	/**
	 * Starts this scheduler and the internal scheduler thread.
	 * <p>
	 * This method must be called before scheduling new commands with the schedule
	 * method. Call stop to stop the internal thread when the scheduler is no longer
	 * needed. Note that this method starts the internal scheduler thread only once.
	 * This means that subsequent calls to this method have no effect.
	 */
	public void start() {
		synchronized (this.fMonitor) {
			if (this.fStarted) {
				return; /* Can be started only once */
			}

			this.fThread = new SchedulerThread();
			this.fThread.start();
			this.fStarted = true;
		}
	}

	/**
	 * Stops this scheduler and the internal scheduler thread.
	 * <p>
	 * This is the matching method for start. After calling this
	 * method, new commands will no longer be accepted by schedule
	 * and are ignored. This method blocks until the internal
	 * thread has processed the current content of the queue.
	 * Call clear before calling stop to exit the internal thread
	 * as soon as possible.
	 */
	public void stop() {
		synchronized (this.fMonitor) {
			if (!this.fStarted) {
				return; /* Not started yet */
			}

			this.fStopped = true;
			this.fMonitor.notify();
		}

		try {
			this.fThread.join();
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Returns the maximum size of the scheduler command queue.
	 *
	 * <p>To influence the behavior of the scheduler if new commands
	 * are enqueued and the queue is currently considered full,
	 * you can specify the {@link #setThrottle throttle mode}.
	 *
	 * @return The maximum size of the scheduler command queue
	 */
	public long getThreshold() {
		return this.fThreshold;
	}

	/**
	 * Sets the maximum size of the scheduler command queue.
	 * <p>
	 * To influence the behavior of the scheduler if new commands
	 * are enqueued and the queue is currently considered full,
	 * you can specify the throttle mode.
	 *
	 * @param threshold The new maximum size of the scheduler command queue
	 */
	public void setThreshold(long threshold) {
		this.fThreshold = threshold;
	}

	/**
	 * Indicates if the scheduler should automatically throttle threads that enqueue new scheduler commands.
	 * <p>
	 * If this method returns true and the queue is considered full when enqueuing new commands,
	 * the enqueuing thread is automatically throttled until there is room in the queue for the new command.
	 * In non-throttle mode, the thread is not blocked but older commands are removed from the queue.
	 *
	 * @return True if the scheduler should automatically throttle threads that enqueue new scheduler commands and false otherwise
	 */
	public boolean getThrottle() {
		return this.fThrottle;
	}

	/**
	 * Specifies if the scheduler should automatically throttle
	 * threads that enqueue new scheduler commands.
	 * <p>
	 * If this method is passed true and the queue is considered
	 * full when enqueuing new commands, the enqueuing thread is
	 * automatically throttled until there is room in the queue
	 * for the new command. In non-throttle mode, the thread is
	 * not blocked but older commands are removed from the queue.
	 *
	 * @param throttle Pass true if the scheduler should automatically throttle
	 *                 threads that enqueue new scheduler commands and false
	 *                 otherwise
	 */
	public void setThrottle(boolean throttle) {
		this.fThrottle = throttle;
	}

	/**
	 * Schedules a new command for asynchronous execution.
	 * This method adds the passed command to the internal queue of
	 * scheduler commands. The command is eventually executed by the
	 * internal scheduler thread. This method can block the caller
	 * if the scheduler operates in throttle mode
	 * and the internal queue is currently considered full (see
	 * {@link Scheduler#setThreshold}).
	 *
	 * @param command The command to schedule
	 * @return True if the command could be scheduled for asynchronous
	 * execution and false otherwise
	 */
	public boolean schedule(SchedulerCommand command) {
		return enqueue(command);
	}

	private boolean enqueue(SchedulerCommand command) {
		if (!this.fStarted) {
			return false; /* Not yet started */
		}

		if (this.fStopped) {
			return false; /* No new commands anymore */
		}

		int commandSize = command.getSize();

		if (commandSize > this.fThreshold) {
			return false;
		}

		synchronized (this.fMonitor) {
			if (!this.fThrottle || this.fProtocol.failed()) {
				if (this.fQueue.getSize() + commandSize > this.fThreshold) {
					this.fQueue.trim(commandSize);
				}
			} else {
				while (this.fQueue.getSize() + commandSize > this.fThreshold) {
					try {
						this.fMonitor.wait();
					} catch (InterruptedException e) {
					}
				}
			}

			this.fQueue.enqueue(command);
			this.fMonitor.notify();
		}

		return true;
	}

	private int dequeue() {
		int count = 0;
		int length = this.fBuffer.length;

		synchronized (this.fMonitor) {
			while (this.fQueue.getCount() == 0) {
				if (this.fStopped) {
					break;
				}

				try {
					this.fMonitor.wait();
				} catch (InterruptedException e) {
				}
			}

			while (this.fQueue.getCount() > 0) {
				this.fBuffer[count] = this.fQueue.dequeue();

				if (++count >= length) {
					break;
				}
			}

			this.fMonitor.notify();
		}

		return count;
	}

	/**
	 * Removes all scheduler commands from this scheduler.
	 * This method clears the current queue of scheduler commands.
	 * If the stop method is called after calling clear and no new
	 * commands are stored between these two calls, the internal
	 * scheduler thread will exit as soon as possible (after the
	 * current command, if any, has been processed).
	 */
	public void clear() {
		synchronized (this.fMonitor) {
			this.fQueue.clear();
			this.fMonitor.notify();
		}
	}
}
