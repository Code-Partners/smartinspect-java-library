//
// <!-- Copyright (C) 2003-2010 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;
 
// <summary>
//   Represents the adapter class for the SmartInspectListener event
//   interface of the SmartInspect class.
// </summary>
// <remarks>
//   This class is added for convenience when dealing with the SmartInspect
//   event system. This class implements the SmartInspectListener interface
//   by defining empty methods stubs. By deriving from this class instead
//   of implementing the SmartInspectListener interface directly, you can
//   subscribe to certain events by overriding the appropriate methods and
//   ignore other events which are of no use to you by keeping the empty
//   default implementation of this class.
// </remarks>

public class SmartInspectAdapter implements SmartInspectListener 
{
	// <summary>
	//   Provides an empty default implementation for the error event of
	//   the implemented SmartInspectListener event interface.
	// </summary>
	// <param name="e">The event argument for the event handler.</param>
	// <remarks>
	//   This method provides an empty default implementation for the error
	//   event. When deriving from this class, override this method if you
	//   are interested in getting notified about errors. For an example,
	//   please refer to the SmartInspectListener.onError method.
	// </remarks>
	
	public void onError(ErrorEvent e)
	{
		
	}
	
	// <summary>
	//   Provides an empty default implementation for the ControlCommand
	//   event of the implemented SmartInspectListener event interface.
	// </summary>
	// <param name="e">The event argument for the event handler.</param>
	// <remarks>
	//   This method provides an empty default implementation for the
	//   ControlCommand event. When deriving from this class, override
	//   this method if you are interested in getting notified about sent
	//   ControlCommand packets. For an example, please refer to the
	//   documentation of the SmartInspectListener.onControlCommand method.
	// </remarks>
	
	public void onControlCommand(ControlCommandEvent e) 
	{
		
	}

	// <summary>
	//   Provides an empty default implementation for the LogEntry event
	//   of the implemented SmartInspectListener event interface.
	// </summary>
	// <param name="e">The event argument for the event handler.</param>
	// <remarks>
	//   This method provides an empty default implementation for the
	//   LogEntry event. When deriving from this class, override this method
	//   if you are interested in getting notified about sent LogEntry
	//   packets. For an example, please refer to the documentation of the
	//   SmartInspectListener.onLogEntry method.
	// </remarks>
	
	public void onLogEntry(LogEntryEvent e) 
	{
		
	}

	// <summary>
	//   Provides an empty default implementation for the ProcessFlow event
	//   of the implemented SmartInspectListener event interface.
	// </summary>
	// <param name="e">The event argument for the event handler.</param>
	// <remarks>
	//   This method provides an empty default implementation for the
	//   ProcessFlow event. When deriving from this class, override this
	//   method if you are interested in getting notified about sent
	//   ProcessFlow packets. For a detailed example, please refer to the
	//   documentation of the SmartInspectListener.onLogEntry method.
	// </remarks>
	
	public void onProcessFlow(ProcessFlowEvent e) 
	{
		
	}

	// <summary>
	//   Provides an empty default implementation for the Watch event of
	//   the implemented SmartInspectListener event interface.
	// </summary>
	// <param name="e">The event argument for the event handler.</param>
	// <remarks>
	//   This method provides an empty default implementation for the
	//   Watch event. When deriving from this class, override this method
	//   if you are interested in getting notified about sent Watch packets.
	//   For a detailed example, please refer to the documentation of the
	//   SmartInspectListener.onWatch method.
	// </remarks>
	
	public void onWatch(WatchEvent e) 
	{
		
	}

	// <summary>
	//   Provides an empty default implementation for the Filter event of
	//   the implemented SmartInspectListener event interface.
	// </summary>
	// <param name="e">The event argument for the event handler.</param>
	// <remarks>
	//   This method provides an empty default implementation for the
	//   Filter event. When deriving from this class, override this method
	//   if you are interested in filtering out any packets. For a detailed
	//   example, please refer to the SmartInspectListener.onFilter method.
	// </remarks>
	
	public void onFilter(FilterEvent e) 
	{
		
	}
}
