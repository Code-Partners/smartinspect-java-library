//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Represents a custom protocol action command as used by the
//   Protocol.dispatch method.
// </summary>
// <remarks>
//   This class is used by custom protocol actions. For detailed information
//   about custom protocol actions, please refer to the Protocol.dispatch
//   and SmartInspect.dispatch methods.
// </remarks>
// <threadsafety>
//   The public members of this class are threadsafe.
// </threadsafety>

public class ProtocolCommand 
{
	private int fAction;
	private Object fState;

	// <summary>
	//   Creates and initializes a new ProtocolCommand instance.
	// </summary>
	// <param name="action">The custom protocol action to execute.</param>
	// <param name="state">
	//   Optional object which provides additional information about the
	//   custom protocol action.
	// </param>
	
	public ProtocolCommand(int action, Object state)
	{
		this.fAction = action;
		this.fState = state;
	}
	
	// <summary>
	//   Returns the custom protocol action to execute. The return value
	//   of this method is protocol specific.
	// </summary>
	// <returns>The custom protocol action to execute.</returns>
	
	public int getAction()
	{
		return this.fAction;
	}
	
	// <summary>
	//   Returns the optional protocol command object which provides
	//   additional information about the custom protocol action. This
	//   method can return null.
	// </summary>
	// <returns>The optional protocol command object.</returns>
	
	public Object getState()
	{
		return this.fState;
	}
}
