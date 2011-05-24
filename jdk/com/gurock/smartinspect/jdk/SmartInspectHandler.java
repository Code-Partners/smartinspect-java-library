//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect.jdk;

import com.gurock.smartinspect.ErrorEvent;
import com.gurock.smartinspect.Session;
import com.gurock.smartinspect.SiAuto;
import com.gurock.smartinspect.SmartInspectAdapter;

import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.LogManager;
import java.util.logging.Level;

// <summary>
//   Directs logging output to a SmartInspect session.
// </summary>
// <remarks>
//   This class can be used as handler in the java.util.logging.Logger
//   class. Therefore it is especially useful if you want to add the 
//   SmartInspect capabilities to existing code which uses the j2se
//   logging framework.
//
//   This class uses a session which receives the logging output.
//   Depending on the called java.util.logging.Logger method, the
//   publish method formats a record and chooses a suitable Session
//   method to send this message.
// </remarks>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>
// <example>
// <code>
// import java.util.logging.*;
// import com.gurock.smartinspect.*;
// import com.gurock.smartinspect.jdk.*;
//
// public class HandlerExample
// {
// 	public static void main(String[] args)
// 	{
// 		// Enable SmartInspect logging.
// 		SiAuto.si.setEnabled(true);
//
// 		// Remove the default handlers.
// 		LogManager.getLogManager().reset();
//
// 		// Add a new SmartInspect handler which uses SiAuto.main
// 		// as session and then set the log level to Level.ALL.
// 		Logger.global.addHandler(new SmartInspectHandler());
// 		Logger.global.setLevel(Level.ALL);
//
// 		// Write messages to the session using indentation.
// 		Logger.global.fine("Test Message");
// 		Logger.global.entering("HandlerExample", "main");
// 		Logger.global.fine("Test Message");
// 		Logger.global.fine("Test Message");
// 		Logger.global.exiting("HandlerExample", "main");
// 		Logger.global.fine("Test Message");
//
// 		// Write test warnings and failures.
// 		Logger.global.warning("Test Warning");
// 		Logger.global.severe("Test Error");
// 		Logger.global.throwing("HandlerExample", "main", new Exception("Test"));
// 	}
// }
// </code>
// </example>

public final class SmartInspectHandler extends java.util.logging.Handler
{
	private Session fSession;
	private Formatter fFormatter;
	
	// <summary>
	//   Overloaded. Creates and initializes a new SmartInspectHandler
	//   instance with SiAuto.main as session.
	// </summary>
	
	public SmartInspectHandler()
	{
		this(SiAuto.main);
	}
	
	// <summary>
	//   Overloaded. Creates and initializes a new SmartInspectHandler
	//   instance using a new session with SiAuto.si as parent.
	// </summary>
	// <param name="sessionName">The name for the new session.</param>
	// <remarks>
	//   This constructor creates a new session with the supplied
	//   sessionName parameter as name and the default background color.
	//   The parent of this session is SiAuto.si.
	// </remarks>
	
	public SmartInspectHandler(String sessionName)
	{
		this(SiAuto.si.addSession(sessionName));
	}
	
	// <summary>
	//   Overloaded. Creates and initializes a new SmartInspectHandler
	//   instance with an existing session.
	// </summary>
	// <param name="session">The session to use.</param>
	
	public SmartInspectHandler(Session session)
	{
		setLevel(Level.ALL); // Log all by default.
		setFormatter(new SmartInspectFormatter());
		setSession(session);
	}

	// <summary>
	//   Overriden. Returns the formatter used to format LogRecords.
	// </summary>
	// <returns>
	//   The formatter used to format LogRecords.
	// </returns>

	public synchronized Formatter getFormatter()
	{
		return this.fFormatter;
	}

	// <summary>
	//   Overriden. Sets the formatter used to format LogRecords.
	// </summary>
	// <param name="formatter">
	//   The formatter used to format LogRecords.
	// </param>
	// <remarks>
	//   Please note that this SmartInspectHandler only accepts 
	//   instances of the SmartInspectFormatter class. Other formatters
	//   will silently be ignored.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   SecurityException      If a security manager exists and if the caller
	//                            does not have LoggingPermission("control").
	// </table>
	// </exception>
	
	public synchronized void setFormatter(Formatter formatter)
		throws SecurityException
	{
		if (formatter != null)
		{
			if (formatter instanceof SmartInspectFormatter)
			{
				LogManager.getLogManager().checkAccess();
				this.fFormatter = formatter;
			}
		}
	}

	// <summary>
	//   Returns the session used to publish LogRecords.
	// </summary>
	// <returns>
	//   The session used to publish LogRecords.
	// </returns>

	public Session getSession()
	{
		return this.fSession;
	}

	private void setSession(Session session)
	{
		this.fSession = session;
		
		// Use the SmarInspect error event to report
		// any errors to the logging ErrorManager.
		
		this.fSession.getParent().addListener
		(
			new SmartInspectAdapter()
			{
				public void onError(ErrorEvent e)
				{
					// Just forward the error to the ErrorManager.
					Exception ex = e.getException();
					reportError(ex.getMessage(), ex, 
						ErrorManager.GENERIC_FAILURE);
				}
			}
		);
	}

	// <summary>
	//   Overriden. Does nothing.
	// </summary>
	
	public void close() throws SecurityException
	{
	}
	
	// <summary>
	//   Overriden. Does nothing.
	// </summary>

	public void flush()
	{
	}
	
	private com.gurock.smartinspect.Level convertLevel(Level level)
	{
		if (level == Level.FINEST || level == Level.FINER)
		{
			return com.gurock.smartinspect.Level.Debug;
		}
		else if (level == Level.FINE || level == Level.CONFIG)
		{
			return com.gurock.smartinspect.Level.Verbose;
		}
		else if (level == Level.WARNING)
		{
			return com.gurock.smartinspect.Level.Warning;
		}
		else if (level == Level.SEVERE)
		{
			return com.gurock.smartinspect.Level.Error;
		}
		else 
		{
			// Default to com.gurock.smartinspect.Debug.Message,
			// this includes Level.INFO as well. 
			return com.gurock.smartinspect.Level.Message; 
		}
	}
		
	// <summary>
	//   Overriden. Publishes a LogRecord.
	// </summary>
	// <param name="record">The record to publish.</param>
	// <remarks>
	//   This method formats the supplied record and chooses a suitable
	//   Session method to log the record. If the record contains a
	//   throwable object, then this method calls the Session.logException
	//   method. Otherwise the action depends on the level and message of
	//   the record. 
	//   
	//   If the message of the record starts with "ENTRY" or "RETURN", then
	//   the Session.enterMethod or Session.leaveMethod methods are called.
	//   The log level for these enterMethod and leaveMethod method calls
	//   depends on the level of the supplied record. The following table
	//   lists the conversion method:
	//
	//   <table>
	//   java.util.logging.Level            com.gurock.smartinspect.Level
	//   -                                  -
	//   Level.FINEST, Level.FINER          Level.Debug
	//   Level.FINE, LEVEL.CONFIG           Level.Verbose
	//   Level.INFO                         Level.Message
	//   Level.WARNING                      Level.Warning
	//   Level.SEVERE                       Level.Error
	//   </table>
	//   
	//   If the supplied record does not use any of the above Level values
	//   of the java.util.logging package, then this method defaults to
	//   the com.gurock.smartinspect.Level.Message level.
	//
	//   In contrast, if the message of the record does not start with the
	//   "ENTRY" or "RETURN" tokens, this method sends a simple message.
	//   The <link com.gurock.smartinspect.LogEntryType, log entry type>
	//   of the message here depends on the level of the supplied record.
	//   The following table lists the conversion method:
	//   
	//   <table>
	//   java.util.logging.Level            Corresponding Session method
	//   -                                  -
	//   Level.FINEST, Level.FINER          Session.logDebug
	//   Level.FINE, LEVEL.CONFIG           Session.logVerbose
	//   Level.INFO                         Session.logMessage
	//   Level.WARNING                      Session.logWarning
	//   Level.SEVERE                       Session.logError
	//   </table>
	//
	//   Similar to above, if the supplied LogRecord does not use any of the
	//   listed Level values of the java.util.logging package, this method
	//   defaults to Session.logMessage.
	// </remarks>
	
	public void publish(LogRecord record)
	{
		if (!isLoggable(record))
		{
			// The record should not be logged.
			return;
		}
		
		// First format the title.
		String title = getFormatter().format(record);

		// Then we need to choose a suitable session method.
		// The choice depends on the content of the record, e.g.
		// if it contains an exception object or not.

		if (record.getThrown() != null)
		{
			// The record contains an exception, so just
			// forward it to the logException method.
			this.fSession.logException(title, record.getThrown());
		}
		else 
		{			
			Level level = record.getLevel();
			if (record.getMessage().startsWith("ENTRY"))
			{
				this.fSession.enterMethod(convertLevel(level), title);
			}
			else if (record.getMessage().startsWith("RETURN"))
			{
				this.fSession.leaveMethod(convertLevel(level), title);				
			}
			else 
			{
				if (level == Level.FINEST || level == Level.FINER)
				{
					this.fSession.logDebug(title);
				}
				else if (level == Level.FINE || level == Level.CONFIG)
				{
					this.fSession.logVerbose(title);					
				}
				else if (level == Level.WARNING)
				{
					this.fSession.logWarning(title);					
				}
				else if (level == Level.SEVERE)
				{					
					this.fSession.logError(title);
				}
				else 
				{
					this.fSession.logMessage(title);
				}
			}
		}
	}
}


