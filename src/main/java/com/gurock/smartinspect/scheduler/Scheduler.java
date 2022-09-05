//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect.scheduler;

// <summary>
//   Responsible for scheduling protocol operations and executing
//   them asynchronously in a different thread of control.
// </summary>
// <remarks>
//   This class is used by the <link Protocol.isValidOption,
//   asynchronous protocol mode> to asynchronously execute protocol
//   operations. New commands can be scheduled for execution with
//   the schedule method. The scheduler can be started and stopped
//   with the start and stop methods. The scheduler uses a size
//   limited queue to buffer scheduler commands. The maximum size of
//   this queue can be set with the setThreshold method. To influence
//   the behavior of the scheduler if new commands are enqueued and
//   the queue is currently considered full, you can specify the
//   <link setThrottle, throttle mode>.
// </remarks>
// <threadsafety>
//   This class is guaranteed to be threadsafe.
// </threadsafety>

import com.gurock.smartinspect.packets.LogHeader;
import com.gurock.smartinspect.packets.Packet;
import com.gurock.smartinspect.packets.logentry.LogEntry;
import com.gurock.smartinspect.protocols.Protocol;
import com.gurock.smartinspect.protocols.ProtocolCommand;
import com.gurock.smartinspect.protocols.ProtocolException;
import com.gurock.smartinspect.protocols.cloud.CloudProtocol;

import java.util.logging.Logger;

public class Scheduler
{
	public static final Logger logger = Logger.getLogger(Scheduler.class.getName());

	class SchedulerThread extends Thread
	{
		// when fail count > 0, cloud protocol scheduler waits between
		// queue processing iterations
		int consecutivePacketWriteFailCount = 0;

		public void run()
		{
			while (true)
			{
				int count = dequeue();

				if (count == 0)
				{
					break; /* Stopped and no more commands */
				}
				
				if (!runCommands(count))
				{
					break; /* Stopped */
				}

				if (fProtocol instanceof CloudProtocol) {
					// if the previous packet sending failed, do not retry immediately
					if (consecutivePacketWriteFailCount > 0) {
						try {
							logger.fine("Previous packet failed to send, waiting one second before trying again");

							Thread.sleep(1000);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
			
			fBuffer = null; /* No longer needed */
		}
		
		private boolean runCommands(int count)
		{
			for (int i = 0; i < count; i++)
			{
				boolean stopped = fStopped; /* See below. */

				SchedulerCommand command = fBuffer[i];				
				runCommand(command);
				fBuffer[i] = null;

				if (!stopped)
				{
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

				if (fProtocol.failed())
				{
					clear();
					return false;
				}
			}
			
			return true;
		}
		
		private void runCommand(SchedulerCommand command)
		{
			/* Process the dequeued command. The Impl method cannot
			 * throw an exception. Exceptions are reported with the
			 * error event of the protocol in asynchronous mode. */

			SchedulerAction action = command.getAction();
			
			try 
			{
				if (action == SchedulerAction.Connect)
				{
					fProtocol.implConnect();
				}
				else if (action == SchedulerAction.WritePacket)
				{
					writePacketAction(command);
				}
				else if (action == SchedulerAction.Disconnect)
				{
					fProtocol.implDisconnect();
				}
				else if (action == SchedulerAction.Dispatch)
				{
					ProtocolCommand cmd = (ProtocolCommand)
						command.getState();
					fProtocol.implDispatch(cmd);				
				}
			}
			catch (Exception e)
			{
				/* Cannot happen, see above, but required */
			}
		}

		private void writePacketAction(SchedulerCommand command) throws ProtocolException {
			Packet packet = (Packet) command.getState();
			fProtocol.implWritePacket(packet);

			// if sending packet failed, put it back to the head of the queue,
			// instead of discarding
			if ((fProtocol instanceof CloudProtocol) && fProtocol.failed()) {
				if (!((CloudProtocol) fProtocol).isReconnectAllowed()) {
					logger.fine("Reconnect is disabled, no need to requeue packet we failed to send");

					return;
				}

				consecutivePacketWriteFailCount++;

				logger.fine(
						"Sending packet failed, scheduling again to the head of the queue, " +
						"consecutive fail count = " + consecutivePacketWriteFailCount
				);

				if (packet instanceof LogEntry) {
					String title = ((LogEntry) packet).getTitle();
					logger.fine("title = " + title);
				} else if (packet instanceof LogHeader) {
					String title = ((LogHeader) packet).getContent();
					logger.fine("title = " + title);
				}

				fProtocol.scheduleWritePacket(packet, SchedulerQueue.QueueEnd.HEAD);
			} else {
				consecutivePacketWriteFailCount = 0;
			}
		}
	}

	private static final int BUFFER_SIZE = 0x10;

	// for preserving order while re-queueing packets (see `runCommand`), buffer size is set to 1
	private static final int CLOUD_PROTOCOL_BUFFER_SIZE = 0x1;
	
	private Thread fThread;
	private SchedulerQueue fQueue;
	private Object fMonitor;
	private boolean fThrottle;
	private long fThreshold;
	private Protocol fProtocol;
	private boolean fStopped;
	private boolean fStarted;
	private SchedulerCommand[] fBuffer;

	// <summary>
	//   Creates and initializes a new Scheduler instance.
	// </summary>
	// <param name="protocol">
	//   The protocol on which to execute the actual operations like
	//   connect, disconnect, write or dispatch.
	// </param>

	public Scheduler(Protocol protocol)
	{
		this.fProtocol = protocol;
		this.fMonitor = new Object();
		this.fQueue = new SchedulerQueue();

		int size = fProtocol instanceof CloudProtocol ? CLOUD_PROTOCOL_BUFFER_SIZE : BUFFER_SIZE;
		this.fBuffer = new SchedulerCommand[size];
	}
	
	// <summary>
	//   Starts this scheduler and the internal scheduler thread.
	// </summary>
	// <remarks>
	//   This method must be called before scheduling new commands
	//   with the schedule method. Call stop to stop the internal
	//   thread when the scheduler is no longer needed. Note that
	//   this method starts the internal scheduler thread only once.
	//   This means that subsequent calls to this method have no
	//   effect.
	// </remarks>

	public void start()
	{
		synchronized (this.fMonitor)
		{
			if (this.fStarted)
			{
				return; /* Can be started only once */
			}

			this.fThread = new SchedulerThread();
			this.fThread.start();
			this.fStarted = true;
		}
	}
	
	// <summary>
	//   Stops this scheduler and the internal scheduler thread.
	// </summary>
	// <remarks>
	//   This is the matching method for start. After calling this
	//   method, new commands will no longer be accepted by schedule
	//   and are ignored. This method blocks until the internal
	//   thread has processed the current content of the queue.
	//   Call clear before calling stop to exit the internal thread
	//   as soon as possible.
	// </remarks>

	public void stop()
	{
		synchronized (this.fMonitor)
		{
			if (!this.fStarted)
			{
				return; /* Not started yet */
			}
			
			this.fStopped = true;
			this.fMonitor.notify();
		}
		
		try 
		{
			this.fThread.join();
		}
		catch (InterruptedException e)
		{
		}
	}
	
	// <summary>
	//   Returns the maximum size of the scheduler command queue.
	// </summary>
	// <returns>
	//   The maximum size of the scheduler command queue.
	// </returns>
	// <remarks>
	//   To influence the behavior of the scheduler if new commands
	//   are enqueued and the queue is currently considered full,
	//   you can specify the <link setThrottle, throttle mode>.
	// </remarks>
	
	public long getThreshold()
	{
		return this.fThreshold;
	}
	
	// <summary>
	//   Sets the maximum size of the scheduler command queue.
	// </summary>
	// <param name="treshold">
	//   The new maximum size of the scheduler command queue.
	// </param>
	// <remarks>
	//   To influence the behavior of the scheduler if new commands
	//   are enqueued and the queue is currently considered full,
	//   you can specify the <link setThrottle, throttle mode>.
	// </remarks>
	
	public void setThreshold(long threshold)
	{
		this.fThreshold = threshold;
	}
	
	// <summary>
	//   Indicates if the scheduler should automatically throttle
	//   threads that enqueue new scheduler commands.
	// </summary>
	// <returns>
	//   True if the scheduler should automatically throttle threads
	//   that enqueue new scheduler commands and false otherwise.
	// </returns>
	// <remarks>
	//   If this method returns true and the queue is considered
	//   full when enqueuing new commands, the enqueuing thread is
	//   automatically throttled until there is room in the queue
	//   for the new command. In non-throttle mode, the thread is
	//   not blocked but older commands are removed from the queue.
	// </remarks>
	
	public boolean getThrottle()
	{
		return this.fThrottle;
	}
	
	// <summary>
	//   Specifies if the scheduler should automatically throttle
	//   threads that enqueue new scheduler commands.
	// </summary>
	// <param name="throttle">
	//   Pass true if the scheduler should automatically throttle
	//   threads that enqueue new scheduler commands and false
	//   otherwise.
	// </returns>
	// <remarks>
	//   If this method is passed true and the queue is considered
	//   full when enqueuing new commands, the enqueuing thread is
	//   automatically throttled until there is room in the queue
	//   for the new command. In non-throttle mode, the thread is
	//   not blocked but older commands are removed from the queue.
	// </remarks>
	
	public void setThrottle(boolean throttle)
	{
		this.fThrottle = throttle;
	}
	
	// <summary>
	//   Schedules a new command for asynchronous execution.
	// </summary>
	// <param name="command">The command to schedule.</param>
	// <returns>
	//   True if the command could be scheduled for asynchronous
	//   execution and false otherwise.
	// </returns>
	// <remarks>
	//   This method adds the passed command to the internal queue of
	//   scheduler commands. The command is eventually executed by the
	//   internal scheduler thread. This method can block the caller
	//   if the scheduler operates in <link setThrottle, throttle mode>
	//   and the internal queue is currently considered full (see
	//   setThreshold).
	// </remarks>

	public boolean schedule(SchedulerCommand command, SchedulerQueue.QueueEnd insertTo)
	{
		return enqueue(command, insertTo);
	}
	
	private boolean enqueue(SchedulerCommand command, SchedulerQueue.QueueEnd insertTo)
	{
		if (!this.fStarted)
		{
			return false; /* Not yet started */				
		}
		
		if (this.fStopped)
		{
			return false; /* No new commands anymore */
		}

		int commandSize = command.getSize();
		
		if (commandSize > this.fThreshold)
		{
			return false;
		}
		
		synchronized (this.fMonitor)
		{
			if (!this.fThrottle || this.fProtocol.failed())
			{
				if (this.fQueue.getSize() + commandSize > this.fThreshold)
				{
					this.fQueue.trim(commandSize);
				}
			}
			else 
			{
				while (this.fQueue.getSize() + commandSize > this.fThreshold)
				{
					try 
					{
						this.fMonitor.wait();
					}
					catch (InterruptedException e)
					{
					}
				}
			}

			this.fQueue.enqueue(command, insertTo);
			this.fMonitor.notify();
		}
		
		return true;
	}
	
	private int dequeue()
	{
		int count = 0;
		int length = this.fBuffer.length;

		synchronized (this.fMonitor)
		{
			while (this.fQueue.getCount() == 0)
			{
				if (this.fStopped)
				{
					break;
				}
				
				try 
				{
					this.fMonitor.wait();
				}
				catch (InterruptedException e)
				{						
				}
			}

			while (this.fQueue.getCount() > 0)
			{
				this.fBuffer[count] = this.fQueue.dequeue();
				
				if (++count >= length)
				{
					break;
				}
			}

			this.fMonitor.notify();
		}

		return count;
	}
	
	// <summary>
	//   Removes all scheduler commands from this scheduler.
	// </summary>
	// <remarks>
	//   This method clears the current queue of scheduler commands.
	//   If the stop method is called after calling clear and no new
	//   commands are stored between these two calls, the internal
	//   scheduler thread will exit as soon as possible (after the
	//   current command, if any, has been processed).
	// </remarks>

	public void clear()
	{
		synchronized (this.fMonitor)
		{
			this.fQueue.clear();
			this.fMonitor.notify();
		}
	}
}
