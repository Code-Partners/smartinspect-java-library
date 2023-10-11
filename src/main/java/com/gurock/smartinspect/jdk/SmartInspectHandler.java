/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.jdk;

import com.gurock.smartinspect.ErrorEvent;
import com.gurock.smartinspect.SiAuto;
import com.gurock.smartinspect.SmartInspectAdapter;
import com.gurock.smartinspect.session.Session;

import java.util.logging.*;

/**
 * Directs logging output to a SmartInspect session.
 * <p>
 * This class can be used as handler in the java.util.logging.Logger
 * class. Therefore, it is especially useful if you want to add the
 * SmartInspect capabilities to existing code which uses the j2se
 * logging framework.
 * <p>
 * This class uses a session which receives the logging output.
 * Depending on the called java.util.logging.Logger method, the
 * publish method formats a record and chooses a suitable Session
 * method to send this message.
 *
 * <p>
 * Note: This class is fully threadsafe.
 *
 * <pre>
 * {@code
 * import java.util.logging.*;
 * import com.gurock.smartinspect.*;
 * import com.gurock.smartinspect.jdk.*;
 *
 * public class HandlerExample
 * {
 * 	public static void main(String[] args)
 *    {
 * 		// Enable SmartInspect logging.
 * 		SiAuto.si.setEnabled(true);
 *
 * 		// Remove the default handlers.
 * 		LogManager.getLogManager().reset();
 *
 * 		// Add a new SmartInspect handler which uses SiAuto.main
 * 		// as session and then set the log level to Level.ALL.
 * 		Logger.global.addHandler(new SmartInspectHandler());
 * 		Logger.global.setLevel(Level.ALL);
 *
 * 		// Write messages to the session using indentation.
 * 		Logger.global.fine("Test Message");
 * 		Logger.global.entering("HandlerExample", "main");
 * 		Logger.global.fine("Test Message");
 * 		Logger.global.fine("Test Message");
 * 		Logger.global.exiting("HandlerExample", "main");
 * 		Logger.global.fine("Test Message");
 *
 * 		// Write test warnings and failures.
 * 		Logger.global.warning("Test Warning");
 * 		Logger.global.severe("Test Error");
 * 		Logger.global.throwing("HandlerExample", "main", new Exception("Test"));
 *    }
 * }
 * </pre>
 */
public final class SmartInspectHandler extends java.util.logging.Handler {
	private Session fSession;
	private Formatter fFormatter;

	/**
	 * Overloaded. Creates and initializes a new SmartInspectHandler instance with SiAuto.main as session.
	 */
	public SmartInspectHandler() {
		this(SiAuto.main);
	}

	/**
	 * Overloaded. Creates and initializes a new SmartInspectHandler instance
	 * using a new session with SiAuto.si as parent.
	 * <p>
	 * This constructor creates a new session with the supplied sessionName
	 * parameter as name and the default background color. The parent
	 * of this session is SiAuto.si.
	 *
	 * @param sessionName The name for the new session
	 */
	public SmartInspectHandler(String sessionName) {
		this(SiAuto.si.addSession(sessionName));
	}

	/**
	 * Overloaded. Creates and initializes a new SmartInspectHandler instance with an existing session.
	 *
	 * @param session The session to use
	 */
	public SmartInspectHandler(Session session) {
		setLevel(Level.ALL); // Log all by default.
		setFormatter(new SmartInspectFormatter());
		setSession(session);
	}

	/**
	 * Overridden. Returns the formatter used to format LogRecords.
	 *
	 * @return The formatter used to format LogRecords
	 */
	public synchronized Formatter getFormatter() {
		return this.fFormatter;
	}

	/**
	 * Overridden. Sets the formatter used to format LogRecords.
	 *
	 * <p>
	 * Please note that this SmartInspectHandler only accepts
	 * instances of the SmartInspectFormatter class. Other formatters
	 * will silently be ignored.
	 * </p>
	 *
	 * @param formatter The formatter used to format LogRecords
	 * @throws SecurityException If a security manager exists and if the caller does not have LoggingPermission("control")
	 */
	public synchronized void setFormatter(Formatter formatter)
			throws SecurityException {
		if (formatter != null) {
			if (formatter instanceof SmartInspectFormatter) {
				LogManager.getLogManager().checkAccess();
				this.fFormatter = formatter;
			}
		}
	}

	/**
	 * Returns the session used to publish LogRecords.
	 *
	 * @return The session used to publish LogRecords
	 */
	public Session getSession() {
		return this.fSession;
	}

	private void setSession(Session session) {
		this.fSession = session;

		// Use the SmartInspect error event to report
		// any errors to the logging ErrorManager.

		this.fSession.getParent().addListener
				(
						new SmartInspectAdapter() {
							public void onError(ErrorEvent e) {
								// Just forward the error to the ErrorManager.
								Exception ex = e.getException();
								reportError(ex.getMessage(), ex,
										ErrorManager.GENERIC_FAILURE);
							}
						}
				);
	}

	/**
	 * Overridden. Does nothing.
	 */

	public void close() throws SecurityException {
	}

	/**
	 * Overridden. Does nothing.
	 */

	public void flush() {
	}

	private com.gurock.smartinspect.Level convertLevel(Level level) {
		if (level == Level.FINEST || level == Level.FINER) {
			return com.gurock.smartinspect.Level.Debug;
		} else if (level == Level.FINE || level == Level.CONFIG) {
			return com.gurock.smartinspect.Level.Verbose;
		} else if (level == Level.WARNING) {
			return com.gurock.smartinspect.Level.Warning;
		} else if (level == Level.SEVERE) {
			return com.gurock.smartinspect.Level.Error;
		} else {
			// Default to com.gurock.smartinspect.Debug.Message,
			// this includes Level.INFO as well. 
			return com.gurock.smartinspect.Level.Message;
		}
	}

	/**
	 * Overriden. Publishes a LogRecord.
	 *
	 * <p>This method formats the supplied record and chooses a suitable
	 * Session method to log the record. If the record contains a
	 * throwable object, then this method calls the Session.logException
	 * method. Otherwise the action depends on the level and message of
	 * the record.</p>
	 *
	 * <p>If the message of the record starts with "ENTRY" or "RETURN", then
	 * the Session.enterMethod or Session.leaveMethod methods are called.
	 * The log level for these enterMethod and leaveMethod method calls
	 * depends on the level of the supplied record. The following table
	 * lists the conversion method:</p>
	 *
	 * <table>
	 * <tr><th>java.util.logging.Level</th><th>com.gurock.smartinspect.Level</th></tr>
	 * <tr><td>Level.FINEST, Level.FINER</td><td>Level.Debug</td></tr>
	 * <tr><td>Level.FINE, LEVEL.CONFIG</td><td>Level.Verbose</td></tr>
	 * <tr><td>Level.INFO</td><td>Level.Message</td></tr>
	 * <tr><td>Level.WARNING</td><td>Level.Warning</td></tr>
	 * <tr><td>Level.SEVERE</td><td>Level.Error</td></tr>
	 * </table>
	 *
	 * <p>If the supplied record does not use any of the above Level values
	 * of the java.util.logging package, then this method defaults to
	 * the com.gurock.smartinspect.Level.Message level.</p>
	 *
	 * <p>In contrast, if the message of the record does not start with the
	 * "ENTRY" or "RETURN" tokens, this method sends a simple message.
	 * The log entry type of the message here depends on the level of the supplied record.
	 * The following table lists the conversion method:</p>
	 *
	 * <table>
	 * <tr><th>java.util.logging.Level</th><th>Corresponding Session method</th></tr>
	 * <tr><td>Level.FINEST, Level.FINER</td><td>Session.logDebug</td></tr>
	 * <tr><td>Level.FINE, LEVEL.CONFIG</td><td>Session.logVerbose</td></tr>
	 * <tr><td>Level.INFO</td><td>Session.logMessage</td></tr>
	 * <tr><td>Level.WARNING</td><td>Session.logWarning</td></tr>
	 * <tr><td>Level.SEVERE</td><td>Session.logError</td></tr>
	 * </table>
	 *
	 * <p>Similar to above, if the supplied LogRecord does not use any of the
	 * listed Level values of the java.util.logging package, this method
	 * defaults to Session.logMessage.</p>
	 *
	 * @param record The record to publish
	 */
	public void publish(LogRecord record) {
		if (!isLoggable(record)) {
			// The record should not be logged.
			return;
		}

		// First format the title.
		String title = getFormatter().format(record);

		// Then we need to choose a suitable session method.
		// The choice depends on the content of the record, e.g.
		// if it contains an exception object or not.

		if (record.getThrown() != null) {
			// The record contains an exception, so just
			// forward it to the logException method.
			this.fSession.logException(title, record.getThrown());
		} else {
			Level level = record.getLevel();
			if (record.getMessage().startsWith("ENTRY")) {
				this.fSession.enterMethod(convertLevel(level), title);
			} else if (record.getMessage().startsWith("RETURN")) {
				this.fSession.leaveMethod(convertLevel(level), title);
			} else {
				if (level == Level.FINEST || level == Level.FINER) {
					this.fSession.logDebug(title);
				} else if (level == Level.FINE || level == Level.CONFIG) {
					this.fSession.logVerbose(title);
				} else if (level == Level.WARNING) {
					this.fSession.logWarning(title);
				} else if (level == Level.SEVERE) {
					this.fSession.logError(title);
				} else {
					this.fSession.logMessage(title);
				}
			}
		}
	}
}


