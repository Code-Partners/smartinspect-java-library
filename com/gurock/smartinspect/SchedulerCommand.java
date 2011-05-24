//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Represents a scheduler command as used by the Scheduler class
//   and the asynchronous protocol mode.
// </summary>
// <remarks>
//   This class is used by the Scheduler class to enqueue protocol
//   operations for later execution when operating in asynchronous
//   mode. For detailed information about the asynchronous protocol 
//   mode, please refer to Protocol.isValidOption.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>

public class SchedulerCommand
{
	private SchedulerAction fAction = SchedulerAction.Connect;
	private Object fState;
	
	// <summary>
	//   Returns the scheduler action to execute. Please refer
	//   to the documentation of the SchedulerAction class for more
	//   information about possible values.
	// </summary>
	// <returns>The scheduler action to execute.</returns>
	
	public SchedulerAction getAction()
	{
		return this.fAction;
	}
	
	// <summary>
	//   Sets the scheduler action to execute. Please refer to
	//   the documentation of the SchedulerAction class for more
	//   information about possible values.
	// </summary>
	// <param name="action">
	//   The new scheduler action. Null references are ignored.
	// </param>

	public void setAction(SchedulerAction action)
	{
		if (action != null)
		{
			this.fAction = action;
		}
	}

	// <summary>
	//   Returns the optional scheduler command state object which
	//   provides additional information about the scheduler command.
	//   This method can return null.
	// </summary>
	// <returns>
	//   The optional scheduler command state object. Can be null.
	// </returns>
	
	public Object getState()
	{
		return this.fState;
	}
	
	// <summary>
	//   Sets the optional scheduler command state object which
	//   provides additional information about the scheduler command.
	// </summary>
	// <param name="action">
	//   The new scheduler command state object. Can ben null.
	// </param>

	public void setState(Object state)
	{
		this.fState = state;
	}
	
	// <summary>
	//   Calculates and returns the total memory size occupied by
	//   this scheduler command.
	// </summary>
	// <remarks>
	//   This method returns the total occupied memory size of this
	//   scheduler command. This functionality is used by the
	//   <link Protocol.isValidOption, asynchronous protocol mode> to
	//   track the total size of scheduler commands.
	// </remarks>
	
	public int getSize()
	{
		if (this.fAction != SchedulerAction.WritePacket)
		{
			return 0;
		}
		
		if (this.fState != null)
		{
			return ((Packet) this.fState).getSize();
		}
		else 
		{
			return 0;
		}
	}
}
