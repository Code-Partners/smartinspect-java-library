//
// <!-- Copyright (C) 2003-2010 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Manages a memory size limited queue of packets.
// </summary>
// <remarks>
//   This class is responsible for managing a size limited queue of
//   packets. This functionality is needed by the protocol
//   <link Protocol.isValidOption, backlog> feature. The maximum total
//   memory size of the queue can be set with the setBacklog method.
//   New packets can be added with the push method. Packets which are
//   no longer needed can be retrieved and removed from the queue with
//   the pop method.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>

public class PacketQueue
{
	private static final int OVERHEAD = 24;
	private long fBacklog;
	private long fSize;
	private PacketQueueItem fHead;
	private PacketQueueItem fTail;
	private int fCount;
	
	class PacketQueueItem
	{
		public Packet packet;
		public PacketQueueItem next;
		public PacketQueueItem previous;
	}
	
	// <summary>
	//   Adds a new packet to the queue.
	// </summary>
	// <param name="packet">The packet to add.</param>
	// <remarks>
	//   This method adds the supplied packet to the queue. The size of
	//   the queue is incremented by the size of the supplied packet
	//   (plus some internal management overhead). If the total occupied
	//   memory size of this queue exceeds the <link setBacklog, backlog>
	//   limit after adding the new packet, then already added packets
	//   will be removed from this queue until the <link setBacklog,
	//   backlog> size limit is reached again.
	// </remarks>

	public void push(Packet packet)
	{
		PacketQueueItem item = new PacketQueueItem();
		item.packet = packet;

		if (this.fTail == null)
		{
			this.fTail = item;
			this.fHead = item;
		}
		else 
		{
			this.fTail.next = item;
			item.previous = this.fTail;
			this.fTail = item;
		}

		this.fCount++;
		this.fSize += packet.getSize() + OVERHEAD;
		resize();
	}
	
	// <summary>
	//   Returns a packet and removes it from the queue.
	// </summary>
	// <returns>
	//   The removed packet or null if the queue does not contain any
	//   packets.
	// </returns>
	// <remarks>
	//   If the queue is not empty, this method removes the oldest packet
	//   from the queue (also known as FIFO) and returns it. The total
	//   size of the queue is decremented by the size of the returned
	//   packet (plus some internal management overhead).
	// </remarks>

	public Packet pop()
	{
		Packet result = null;
		PacketQueueItem item = this.fHead;

		if (item != null)
		{
			result = item.packet;
			this.fHead = item.next;

			if (this.fHead != null)
			{
				this.fHead.previous = null;
			}
			else 
			{
				this.fTail = null;
			}

			this.fCount--;
			this.fSize -= result.getSize() + OVERHEAD;
		}

		return result;
	}
	
	// <summary>
	//   Removes all packets from the queue.
	// </summary>
	// <remarks>
	//   Removing all packets of the queue is done by calling the pop
	//   method for each packet in the current queue.
	// </remarks>
	
	public void clear()
	{
		while (pop() != null);
	}
	
	private void resize()
	{
		while (this.fBacklog < this.fSize)
		{
			if (pop() == null)
			{
				this.fSize = 0;
				break;
			}
		}
	}
	
	// <summary>
	//   Returns the total maximum memory size of this queue in bytes.
	// </summary>
	// <returns>
	//   The total maximum memory size of this queue in bytes.
	// </returns>
	// <remarks>
	//   Please see the setBacklog method for more information about the
	//   backlog property.
	// </remarks>

	public long getBacklog()
	{
		return this.fBacklog;
	}
	
	// <summary>
	//   Sets the total maximum memory size of this queue in bytes.
	// </summary>
	// <param name="backlog">The new backlog size.</param>
	// <remarks>
	//   Each time a new packet is added with the push method, it will
	//   be verified that the total occupied memory size of the queue
	//   still falls below the supplied backlog limit. To satisfy this
	//   constraint, old packets are removed from the queue when
	//   necessary.
	// </remarks>

	public void setBacklog(long backlog)
	{
		this.fBacklog = backlog;
		resize();
	}
	
	// <summary>
	//   Returns the current amount of packets in this queue.
	// </summary>
	// <returns>The current amount of packets in this queue.</returns>
	// <remarks>
	//   For each added packet this counter is incremented by one
	//   and for each removed packet (either with the pop method
	//   or automatically while resizing the queue) this counter
	//   is decremented by one. If the queue is empty, this method
	//   returns 0.
	// </remarks>
	
	public int getCount()
	{
		return this.fCount;
	}
}
