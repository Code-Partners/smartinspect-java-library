/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import com.gurock.smartinspect.packets.controlcommand.ControlCommandEvent;
import com.gurock.smartinspect.packets.logentry.LogEntryEvent;
import com.gurock.smartinspect.packets.processflow.ProcessFlowEvent;
import com.gurock.smartinspect.packets.watch.WatchEvent;

/**
 * This listener interface is used in the SmartInspect class for all
 * kinds of event reporting.
 * The SmartInspect class provides events for error reporting, packet
 * filtering and packet sending. Please see the available event methods
 * in this interface for details and examples.
 */
public interface SmartInspectListener {
	/**
	 * This event is fired after an error occurred.
	 *
	 * <p>This event is fired when an error occurs. An error could be a
	 * connection problem or wrong permissions when writing log files,
	 * for example. Instead of throwing exceptions, this event is used
	 * for error reporting in the SmartInspect Java library. The event
	 * handlers are always called in the context of the thread which
	 * causes the event. In asynchronous protocol mode, this is not necessarily the thread
	 * that initiated the related log call.</p>
	 *
	 * <p><b>Please note</b>: Keep in mind that adding SmartInspect log
	 * statements or other code to the event handlers which can lead
	 * to the error event can cause a presumably undesired recursive
	 * behavior.</p>
	 *
	 * <h4>Example:</h4>
	 * <pre>
	 * import com.gurock.smartinspect.*;
	 *
	 * class Listener extends SmartInspectAdapter
	 * {
	 *     public void onError(ErrorEvent e)
	 *     {
	 *         System.out.println(e.getException());
	 *     }
	 * }
	 *
	 * public class ErrorHandling
	 * {
	 *     public static void main(String[] args)
	 *     {
	 *         // Register our event handler for the error event.
	 *         SiAuto.si.addListener(new Listener());
	 *
	 *         try
	 *         {
	 *             // Force a connection error.
	 *             SiAuto.si.setConnections("file(filename=c:\\\\)");
	 *         }
	 *         catch (InvalidConnectionsException e)
	 *         {
	 *             // This catch block is useless. It won't be reached
	 *             // anyway, because a connection error doesn't result
	 *             // in a Java exception. The SmartInspect Java library
	 *             // uses the Error event for this purpose.
	 *         }
	 *
	 *         SiAuto.si.setEnabled(true);
	 *     }
	 * }
	 * </pre>
	 *
	 * @param e The event argument for the event handlers
	 * @see com.gurock.smartinspect.ErrorEvent
	 */
	public void onError(ErrorEvent e);

	/**
	 * Occurs when a ControlCommand packet is processed.
	 * <p>
	 * You can use this event if custom processing of ControlCommand
	 * packets is needed. The event handlers are always called in the
	 * context of the thread which causes the event.
	 * <p>
	 * <b>Please note</b>: Keep in mind that adding SmartInspect log
	 * statements to the event handlers can cause a presumably undesired
	 * recursive behavior. Also, if you specified that one or more
	 * connections of this SmartInspect object should operate in
	 * asynchronous protocol mode, you need to protect the passed ControlCommand packet and its data by
	 * calling its <link Packet.lock, lock> and <link Packet.unlock,
	 * unlock> methods before and after processing.
	 *
	 * <pre>
	 * import com.gurock.smartinspect.*;
	 *
	 * class Listener extends SmartInspectAdapter
	 * {
	 * 	public void onControlCommand(ControlCommandEvent e)
	 *    {
	 * 		System.out.println(e.getControlCommand().toString());
	 *    }
	 * }
	 *
	 * public class ControlCommandHandling
	 * {
	 * 	public static void main(String[] args)
	 *    {
	 * 		SiAuto.si.setEnabled(true);
	 * 		SiAuto.si.addListener(new Listener());
	 * 		SiAuto.main.clearAll();
	 *    }
	 * }
	 * </pre>
	 *
	 * @param e The event argument for the event handlers
	 * @see com.gurock.smartinspect.packets.controlcommand.ControlCommand
	 * @see com.gurock.smartinspect.packets.controlcommand.ControlCommandEvent
	 */
	public void onControlCommand(ControlCommandEvent e);

	/**
	 * Occurs when a LogEntry packet is processed.
	 * <p>
	 * You can use this event if custom processing of LogEntry packets
	 * is needed. The event handlers are always called in the context
	 * of the thread which causes the event.
	 * <p>
	 * <b>Please note</b>: Keep in mind that adding SmartInspect log
	 * statements to the event handlers can cause a presumably undesired
	 * recursive behavior. Also, if you specified that one or more
	 * connections of this SmartInspect object should operate in
	 * asynchronous protocol mode, you need to protect the passed LogEntry packet and its data by
	 * calling its lock and unlock methods before and after processing.
	 * <p>
	 * Example:
	 * <pre>
	 * import com.gurock.smartinspect.*;
	 *
	 * class Listener extends SmartInspectAdapter
	 * {
	 * 	public void onLogEntry(LogEntryEvent e)
	 *    {
	 * 		System.out.println(e.getLogEntry().toString());
	 *    }
	 * }
	 *
	 * public class LogEntryHandling
	 * {
	 * 	public static void main(String[] args)
	 *    {
	 * 		SiAuto.si.setEnabled(true);
	 * 		SiAuto.si.addListener(new Listener());
	 * 		SiAuto.main.logMessage("This is an event test!");
	 *    }
	 * }
	 * </pre>
	 *
	 * @param e The event argument for the event handlers
	 * @see com.gurock.smartinspect.packets.logentry.LogEntry
	 * @see com.gurock.smartinspect.packets.logentry.LogEntryEvent
	 */
	public void onLogEntry(LogEntryEvent e);

	/**
	 * Occurs when a ProcessFlow packet is processed.
	 * You can use this event if custom processing of ProcessFlow packets is needed.
	 * The event handlers are always called in the context of the thread which causes the event.
	 *
	 * <p><b>Please note</b>: Keep in mind that adding SmartInspect log
	 * statements to the event handlers can cause a presumably undesired
	 * recursive behavior. Also, if you specified that one or more
	 * connections of this SmartInspect object should operate in
	 * asynchronous protocol mode, you need to protect the passed ProcessFlow packet and its data by
	 * calling its lock and unlock methods before and after processing.</p>
	 *
	 * <p>Example:</p>
	 * <pre>
	 * import com.gurock.smartinspect.*;
	 *
	 * class Listener extends SmartInspectAdapter
	 * {
	 *  	public void onProcessFlow(ProcessFlowEvent e)
	 *     {
	 * 	    System.out.println(e.getProcessFlow().toString());
	 *    }
	 * }
	 *
	 * public class ProcessFlowHandling
	 * {
	 * 	 public static void main(String[] args)
	 *     {
	 * 	    SiAuto.si.setEnabled(true);
	 * 	    SiAuto.si.addListener(new Listener());
	 * 	    SiAuto.main.enterThread("Main Thread");
	 *     }
	 * }
	 * </pre>
	 *
	 * @param e The event argument for the event handlers
	 * @see com.gurock.smartinspect.packets.processflow.ProcessFlow
	 * @see com.gurock.smartinspect.packets.processflow.ProcessFlowEvent
	 */
	public void onProcessFlow(ProcessFlowEvent e);

	/**
	 * This event occurs when a Watch packet is processed. You can use this
	 * event if custom processing of Watch packets is needed. The event handlers
	 * are always called in the context of the thread that causes the event.
	 *
	 * <p><b>Please note</b>: Keep in mind that adding SmartInspect log
	 * statements to the event handlers can cause a presumably undesired
	 * recursive behavior. Also, if you specified that one or more connections
	 * of this SmartInspect object should operate in asynchronous protocol mode,
	 * you need to protect the passed Watch packet and its data by
	 * calling its lock and unlock methods before and after processing.
	 *
	 * <pre>
	 * Usage example:
	 *
	 * import com.gurock.smartinspect.*;
	 *
	 * class Listener extends SmartInspectAdapter
	 * {
	 *   public void onWatch(WatchEvent e)
	 *   {
	 *     System.out.println(e.getWatch().toString());
	 *   }
	 * }
	 *
	 * public class WatchHandling
	 * {
	 *   public static void main(String[] args)
	 *   {
	 *     SiAuto.si.setEnabled(true);
	 *     SiAuto.si.addListener(new Listener());
	 *     SiAuto.main.watchInt("Integer", 23);
	 *   }
	 * }
	 * </pre>
	 *
	 *
	 * @param e the event argument for the event handlers
	 * @see com.gurock.smartinspect.packets.watch.Watch
	 * @see com.gurock.smartinspect.packets.watch.WatchEvent
	 */
	public void onWatch(WatchEvent e);

	/**
	 * Occurs before a packet is processed. Offers the opportunity to filter out packets.
	 * This event can be used if filtering of certain packets is needed. The event handlers are always called in the context
	 * of the thread which causes the event. Please see the example for more information on how to use this event.
	 * <b>Please note</b>: Keep in mind that adding SmartInspect log statements to the event handlers can cause a presumably
	 * undesired recursive behavior.
	 * <pre>
	 * import com.gurock.smartinspect.*;
	 *
	 * class Listener extends SmartInspectAdapter
	 * {
	 *   public void onFilter(FilterEvent e)
	 *   {
	 *     if (e.getPacket() instanceof LogEntry)
	 *     {
	 *       LogEntry logEntry = (LogEntry) e.getPacket();
	 *       if (logEntry.getTitle().equals("Cancel Me"))
	 *       {
	 *         e.setCancel(true);
	 *       }
	 *     }
	 *   }
	 * }
	 *
	 * public class FilterHandling
	 * {
	 *   public static void main(String[] args)
	 *   {
	 *     SiAuto.si.setEnabled(true);
	 *
	 *     // Register a listener for the filter event
	 *     SiAuto.si.addListener(new Listener());
	 *
	 *     // The second message will not be logged
	 *     SiAuto.main.logMessage("Message");
	 *     SiAuto.main.logMessage("Cancel Me");
	 *   }
	 * }
	 * </pre>
	 *
	 * @param e The event argument for the event handlers
	 * @see com.gurock.smartinspect.packets.Packet
	 * @see com.gurock.smartinspect.FilterEvent
	 */
	public void onFilter(FilterEvent e);
}
