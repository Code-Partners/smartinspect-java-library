/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import com.gurock.smartinspect.connections.ConnectionsParser;
import com.gurock.smartinspect.connections.ConnectionsParserEvent;
import com.gurock.smartinspect.connections.ConnectionsParserListener;
import com.gurock.smartinspect.packets.Packet;
import com.gurock.smartinspect.packets.controlcommand.ControlCommand;
import com.gurock.smartinspect.packets.controlcommand.ControlCommandEvent;
import com.gurock.smartinspect.packets.logentry.LogEntry;
import com.gurock.smartinspect.packets.logentry.LogEntryEvent;
import com.gurock.smartinspect.packets.processflow.ProcessFlow;
import com.gurock.smartinspect.packets.processflow.ProcessFlowEvent;
import com.gurock.smartinspect.packets.watch.Watch;
import com.gurock.smartinspect.packets.watch.WatchEvent;
import com.gurock.smartinspect.protocols.*;
import com.gurock.smartinspect.session.Session;
import com.gurock.smartinspect.session.SessionDefaults;
import com.gurock.smartinspect.session.SessionManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SmartInspect is the most important class in the SmartInspect
 * Java library. It is an interface for the protocols, packets
 * and sessions and is responsible for the error handling.
 * <p>
 * The SmartInspect class is the most important class in the
 * SmartInspect Java library. An instance of this class is able to
 * write log messages to a file or to send them directly to the
 * SmartInspect Console using TCP. You can control these connections
 * by passing a connections string to the setConnections method.
 * <p>
 * The SmartInspect class offers several properties for controlling
 * the logging behavior. Besides the setConnections method there
 * is the setEnabled method which controls if log messages should
 * be sent or not. Furthermore, the setAppName method specifies the
 * application name displayed in the SmartInspect Console. And last
 * but not least, we have the setLevel and setDefaultLevel methods
 * which let you specify the log level of an SmartInspect object and
 * its related sessions.
 * <p>
 * Additionally, the SmartInspect class acts as parent for
 * sessions, which contain the actual logging methods, like, for
 * example, Session.logMessage or Session.logObject. It is possible
 * and common that several different sessions have the same parent
 * and thus share the same connections. The Session class contains
 * dozens of useful methods for logging any kind of data. Sessions
 * can even log variable watches, generate illustrated process and
 * thread information or control the behavior of the SmartInspect
 * Console. It is possible, for example, to clear the entire log in
 * the Console by calling the Session.clearLog method.
 * <p>
 * To accomplish these different tasks the SmartInspect concept uses
 * several different packets. The SmartInspect class manages these
 * packets and logs them to its connections. It is possibility to
 * register event handlers for every packet type which are called
 * after a corresponding packet has been sent.
 * <p>
 * The error handling in the SmartInspect Java library is a little bit
 * different from other libraries. This library uses an event, the
 * {@link SmartInspectListener#onError}, for reporting errors.
 * We've chosen this way because a logging framework should not alter the
 * behavior of an application by firing exceptions. The only exception
 * you need to handle can be thrown by the setConnections method if
 * the {@link SmartInspect#setConnections} contains errors.
 * <p>
 * This class is fully threadsafe.
 */
public class SmartInspect {
	private static final String VERSION = "$SIVERSION";

	private static final String CAPTION_NOT_FOUND =
			"No protocol could be found with the specified caption";

	private static final String
			CONNECTIONS_NOT_FOUND_ERROR = "No connections string found";

	private ClockResolution fResolution;
	private Level fLevel;
	private Level fDefaultLevel;
	private boolean fEnabled;
	private String fAppName;
	private String fConnections;
	private String fHostName;
	private boolean fIsMultiThreaded;

	private Object fLock;
	private List fProtocols;
	private Set fListeners;
	private SessionManager fSessions;
	private ProtocolVariables fVariables;

	/**
	 * Initializes a new instance of the SmartInspect class.
	 *
	 * @param appName The application name used for Log Entries
	 */
	public SmartInspect(String appName) {
		this.fLock = new Object();

		this.fLevel = Level.Debug;
		this.fDefaultLevel = Level.Message;
		this.fConnections = "";
		this.fProtocols = new ArrayList();
		this.fEnabled = false;
		setAppName(appName);

		try {
			// Try to get the hostname of this machine.
			InetAddress localHost = InetAddress.getLocalHost();
			this.fHostName = localHost.getHostName();
		} catch (UnknownHostException e) {
			// We couldn't get the hostname of this machine,
			// so we set the HostName to an empty string.
			this.fHostName = "";
		}

		this.fListeners = new HashSet();
		this.fSessions = new SessionManager();
		this.fResolution = ClockResolution.Standard;
		this.fVariables = new ProtocolVariables();
	}

	/**
	 * Returns the current date and time, optionally with a high resolution.
	 *
	 * <p>If the getResolution method specifies using a high resolution
	 * for timestamps, this method tries to return a timestamp with a
	 * microsecond resolution.
	 * <p>
	 * The SmartInspect Java library needs an external DLL to be
	 * able to use high-resolution timestamps. This DLL is called
	 * SmartInspect.Java.dll. If this DLL cannot be found during
	 * application startup, high-resolution support is not available.
	 * Additionally, even if this DLL is found and loaded correctly,
	 * high-resolution timestamps are only available if the
	 * QueryPerformanceCounter and QueryPerformanceFrequency Windows
	 * functions indicate a successfully working high-resolution
	 * performance counter.
	 * <p>
	 * If high-resolution support is not available, this method
	 * simply returns the local date and time with the help of the
	 * System.currentTimeMillis() function and the default time zone.</p>
	 *
	 * @return The current local date and time in microseconds since January 1, 1970
	 */
	public long now() {
		return Clock.now(this.fResolution);
	}

	/**
	 * Returns the timestamp resolution mode for this SmartInspect object.
	 * This method returns ClockResolution.Standard by default. Please
	 * see the setResolution method for detailed information about
	 * timestamp resolutions.
	 *
	 * @return The timestamp resolution mode for this SmartInspect object
	 * @see com.gurock.smartinspect.ClockResolution
	 */
	public ClockResolution getResolution() {
		return this.fResolution;
	}

	/**
	 * Specifies the timestamp resolution mode for this SmartInspect object.
	 * By changing this setting, you can specify if this object should try to use
	 * high-resolution timestamps for LogEntry, Watch and ProcessFlow packets. High-resolution
	 * timestamps provide a microsecond resolution. Conversely, standard timestamps have a
	 * maximum resolution of 10-55 milliseconds.
	 * <p>
	 * The SmartInspect Java library needs an external DLL to be able to use high-resolution
	 * timestamps. This DLL is called SmartInspect.Java.dll. If this DLL cannot be found during
	 * application startup, high-resolution support is not available. Additionally, even if this
	 * DLL is found and loaded correctly, high-resolution timestamps are only available if the
	 * QueryPerformanceCounter and QueryPerformanceFrequency Windows functions indicate a
	 * successfully working high-resolution performance counter.
	 * <p>
	 * Please note that <b>high-resolution timestamps are not intended to be used on production
	 * systems</b>. It is recommended to use them only during development and debugging.
	 * High-resolution timestamps can introduce several problems that are acceptable on
	 * development machines but normally not tolerable on production systems:
	 *
	 * <table border="1">
	 * 	<thead>
	 * 		<tr>
	 * 			<th>Problem</th>
	 * 			<th>Description</th>
	 * 		</tr>
	 * 	</thead>
	 * 	<tbody>
	 * 		<tr>
	 * 			<td>Performance</td>
	 * 			<td>High-resolution timestamps can be a lot slower than standard timestamps. This actually depends on the concrete implementation of QueryPerformanceCounter (i.e. which timer is used for the high-resolution performance counter [PIT, PMT, TSC, HPET]), but in general one can say that standard timestamps are a lot faster to read.</td>
	 * 		</tr>
	 * 		<tr>
	 * 			<td>Accuracy</td>
	 * 			<td>High-resolution timestamps tend to deviate from the system timer when seen over a longer period of time. Depending on the particular QueryPerformanceCounter implementation, it can happen that high-resolution timestamps induce an error of milliseconds within a few minutes only.</td>
	 * 		</tr>
	 * 		<tr>
	 * 			<td>Reliability</td>
	 * 			<td>Depending on the used timer, QueryPerformanceCounter provides unreliable results under certain, not so uncommon, circumstances. When the TSC timer is used, multi-processor/multi-core systems or processors with varying frequencies (like found in most modern notebooks or desktop machines) are known to cause several problems which make high-resolution timestamps unsuitable for production usage.</td>
	 * 		</tr>
	 * 	</tbody>
	 * </table>
	 * <p>
	 * Due to the mentioned problems, this setting defaults to using the standard timestamp
	 * resolution. If null is passed as argument, the timestamp resolution is not changed.
	 *
	 * @param resolution The new timestamp resolution mode for this SmartInspect object
	 * @see com.gurock.smartinspect.ClockResolution
	 */
	public void setResolution(ClockResolution resolution) {
		if (resolution != null) {
			this.fResolution = resolution;
		}
	}

	/**
	 * Returns the version number of the SmartInspect Java library.
	 * This static function returns the current version number of the
	 * SmartInspect Java library. The returned string always has the
	 * form "MAJOR.MINOR.RELEASE.BUILD"
	 *
	 * @return The current version number
	 */
	public static String getVersion() {
		return VERSION;
	}

	/**
	 * Returns the hostname of the sending machine.
	 * <p>
	 * This method returns the hostname of the current machine. The hostname helps you to
	 * identify Log Entries from different machines in the SmartInspect Console.
	 *
	 * @return The hostname used for the Log Entries
	 */
	public String getHostName() {
		return this.fHostName;
	}

	/**
	 * The application name helps you to identify Log Entries from
	 * different applications in the SmartInspect Console.
	 *
	 * @return The application name used for the Log Entries
	 */
	public String getAppName() {
		return this.fAppName;
	}

	/**
	 * Sets the application name used for the Log Entries.
	 * <p>
	 * The application name helps you to identify Log Entries from
	 * different applications in the SmartInspect Console. If you set
	 * this property to null, the application name will be empty when
	 * sending Log Entries
	 */
	public void setAppName(String appName) {
		if (appName == null) {
			this.fAppName = "";
		} else {
			this.fAppName = appName;
		}

		updateProtocols();
	}

	private void updateProtocols() {
		synchronized (this.fLock) {
			for (int i = 0; i < this.fProtocols.size(); i++) {
				Protocol p = (Protocol) this.fProtocols.get(i);
				p.setHostName(this.fHostName);
				p.setAppName(this.fAppName);
			}
		}
	}

	/**
	 * Returns the log level of this SmartInspect instance and its related sessions.
	 * <p>
	 * The getLevel and setLevel methods of this SmartInspect instance represent the log level used by its
	 * corresponding sessions to determine if information should be logged or not. The default return value
	 * of this method is Level.Debug.
	 * <p>
	 * Please see the corresponding setLevel method for more information on how to use the log level.
	 *
	 * @return The log level of this SmartInspect instance and its related sessions
	 */
	public Level getLevel() {
		return this.fLevel;
	}

	/**
	 * Sets the log level of this SmartInspect instance and its related sessions.
	 *
	 * <p>The getLevel and setLevel methods of this SmartInspect instance
	 * represent the log level used by its corresponding sessions to
	 * determine if information should be logged or not. The default
	 * log level is Level.Debug.</p>
	 *
	 * <p>Every method (except the clear method family) in the Session
	 * class tests if its log level equals or is greater than the
	 * log level of its parent. If this is not the case, the methods
	 * return immediately and won't log anything.</p>
	 *
	 * <p>The log level for a method in the Session class can either be
	 * specified explicitly by passing a Level argument or implicitly
	 * by using the setDefaultLevel method. Every method in the Session
	 * class which makes use of the parent's log level and does not take a Level
	 * argument, uses the setDefaultLevel method of its parent to get the log level.</p>
	 *
	 * <p>Please note that this method does nothing if the supplied level
	 * argument is null. For more information about the default level,
	 * please refer to the documentation of the setDefaultLevel method.</p>
	 *
	 * <pre>
	 *   import com.gurock.smartinspect.*;
	 *   public class Program {
	 *       static void method() {
	 *           SiAuto.main.enterMethod(Level.Debug, "Method");
	 *           try {
	 *               // ...
	 *           } finally {
	 *               SiAuto.main.leaveMethod(Level.Debug, "Method");
	 *           }
	 *       }
	 *       public static void main(String[] args) {
	 *           SiAuto.si.setEnabled(true);
	 *           SiAuto.si.setLevel(Level.Debug);
	 *           method(); // Logs enterMethod and leaveMethod calls.
	 *           SiAuto.si.setLevel(Level.Message);
	 *           method(); // Ignores enterMethod and leaveMethod calls.
	 *       }
	 *   }
	 * </pre>
	 *
	 * @param level The new log level
	 */
	public void setLevel(Level level) {
		if (level != null) {
			this.fLevel = level;
		}
	}

	/**
	 * Returns the default log level of this SmartInspect instance
	 * and its related sessions.
	 *
	 * <p>
	 * The getDefaultLevel and setDefaultLevel methods of this
	 * SmartInspect instance represent the default log level used by
	 * its corresponding sessions. The default return value of this
	 * method is Level.Message.
	 * <p>
	 * Please see the corresponding setDefaultLevel method for more
	 * information on how to use the default log level.
	 * </p>
	 *
	 * @return The default log level of this SmartInspect instance and its
	 * related sessions
	 */
	public Level getDefaultLevel() {
		return this.fDefaultLevel;
	}

	/**
	 * Returns the default log level of this SmartInspect instance and its related sessions.
	 * <p>
	 * The getDefaultLevel and setDefaultLevel methods of this SmartInspect instance represent the default log level used by its corresponding sessions. The default value for the default log level is Level.Message.
	 * </p>
	 * <p>
	 * Every method in the Session class which makes use of the parent's log level and does not take a Level argument, uses the default level of its parent as log level.
	 * </p>
	 * <p>
	 * Please note that this method does nothing if the supplied level argument is null. For more information on how to use this method, please have a look at the following examples.
	 * </p>
	 * <p>
	 * Example:
	 * <pre>
	 * import com.gurock.smartinspect.*;
	 *
	 * public class Program {
	 *     static void method() {
	 *         SiAuto.main.enterMethod("Method");
	 *         try {
	 *             // ...
	 *         } finally {
	 *             SiAuto.main.leaveMethod("Method");
	 *         }
	 *     }
	 *
	 *     public static void main(String[] args) {
	 *         SiAuto.si.setEnabled(true);
	 *
	 *         SiAuto.si.setLevel(Level.Debug);
	 *         SiAuto.si.setDefaultLevel(Level.Verbose);
	 *
	 *         // Since the enterMethod and leaveMethod calls do not
	 *         // specify their log level explicitly (by passing a Level
	 *         // argument), they use the default log level which has
	 *         // just been set to Level.Verbose (see above). And since
	 *         // the log level of the SiAuto.si object is set to
	 *         // Level.Debug, the enterMethod and leaveMethod calls will
	 *         // be logged.
	 *         method(); // Logs enterMethod and leaveMethod calls.
	 *
	 *         SiAuto.si.setLevel(Level.Message);
	 *
	 *         // Since enterMethod and leaveMethod still use Level.Verbose
	 *         // as their log level and the log level of the SiAuto.si
	 *         // object is now set to Level.Message, the enterMethod and
	 *         // leaveMethod calls will be ignored and not be logged.
	 *         method(); // Ignores enterMethod and leaveMethod calls.
	 *     }
	 * }
	 * </pre>
	 *
	 * @param level The new default log level
	 */
	public void setDefaultLevel(Level level) {
		if (level != null) {
			this.fDefaultLevel = level;
		}
	}

	private void connect() {
		// Here, we simply call the connect method of
		// all protocol objects in our collection. If an
		// error occurs we call the Error event.

		for (int i = 0; i < this.fProtocols.size(); i++) {
			try {
				// Try to connect.
				Protocol p = (Protocol) this.fProtocols.get(i);
				p.connect();
			} catch (Exception e) {
				doError(e);
			}
		}
	}

	private void disconnect() {
		// Here, we simply call the disconnect method of
		// all protocol objects in our collection. If an
		// error occurs we call the Error event.

		for (int i = 0; i < this.fProtocols.size(); i++) {
			try {
				// Try to disconnect.
				Protocol p = (Protocol) this.fProtocols.get(i);
				p.disconnect();
			} catch (Exception e) {
				doError(e);
			}
		}
	}

	/**
	 * Returns a boolean value which indicates if this instance is
	 * enabled or not.
	 *
	 * @return True if this instance is enabled and false otherwise. See
	 * setEnabled for more information
	 */
	public boolean isEnabled() {
		return this.fEnabled;
	}

	/**
	 * This method allows you to control if anything should be
	 * logged at all.
	 * <p>
	 * If you pass true to this method, all internal connections
	 * try to connect to their destination. For example, if the
	 * connections string is set to "file(filename=c:\\log.sil)",
	 * the file "c:\\log.sil" will be opened to write all following
	 * packets to it. By passing false, all internal connections
	 * will disconnect.
	 * <p>
	 * Additionally, every Session method evaluates if its parent
	 * is enabled and returns immediately if this is not the case.
	 * This guarantees that the performance loss is minimal when
	 * logging is disabled. A SmartInspect instance is disabled by
	 * default, so you need to enable it, before you can make use
	 * of the SmartInspect instance and its related sessions.
	 * <p>
	 * Please note: If one or more connections of this
	 * SmartInspect object operate in asynchronous protocol mode,
	 * you must disable this object by setting this property to
	 * false before exiting your application to properly exit and
	 * cleanup the protocol related threads. Disabling this instance
	 * may block until the related protocol threads are finished.
	 *
	 * @param enabled A boolean value to enable or disable this instance
	 */
	public void setEnabled(boolean enabled) {
		synchronized (this.fLock) {
			if (enabled) {
				enable();
			} else {
				disable();
			}
		}
	}

	private void enable() {
		if (!this.fEnabled) {
			this.fEnabled = true;
			connect();
		}
	}

	private void disable() {
		if (this.fEnabled) {
			this.fEnabled = false;
			disconnect();
		}
	}

	private void createConnections(String connections)
			throws InvalidConnectionsException {
		this.fIsMultiThreaded = false; /* See below */

		try {
			ConnectionsParser parser = new ConnectionsParser();
			ConnectionsParserListener listener =
					new ConnectionsParserListener() {
						public void onProtocol(ConnectionsParserEvent e)
								throws SmartInspectException {
							addConnection(e.getProtocol(), e.getOptions());
						}
					};
			parser.parse(this.fVariables.expand(connections), listener);
		} catch (Exception e) {
			removeConnections();
			throw new InvalidConnectionsException(e.getMessage());
		}
	}

	private void addConnection(String name, String options)
			throws SmartInspectException {
		Protocol protocol = ProtocolFactory.getProtocol(name, options);

		protocol.addListener(
				new ProtocolListener() {
					public void onError(ErrorEvent e) {
						doError(e.getException());
					}
				}
		);

		this.fProtocols.add(protocol);
		if (protocol.isAsynchronous()) {
			this.fIsMultiThreaded = true;
		}

		protocol.setHostName(this.fHostName);
		protocol.setAppName(this.fAppName);
	}

	/**
	 * Overloaded. Loads the connections string from a file and enables this SmartInspect instance.
	 *
	 * <p>This method loads the connections string from a file. This file should be a plain
	 * text file containing a line like in the following example:
	 * <p>
	 * <code>connections=file(filename=c:\\log.sil)</code>
	 * <p>
	 * Empty, unrecognized lines and lines beginning with a ';' character are ignored.
	 * This version of the method enables logging automatically.
	 * <p>
	 * The Error event is used to notify the application if the specified file cannot be opened
	 * or does not contain a connections string. The connections string and the enabled status
	 * of this instance are not changed if such an error occurs.
	 * <p>
	 * The Error event is also used if a connections string could be read but is found to be invalid.
	 * In this case, an instance of the InvalidConnectionsException exception type is passed to the
	 * Error event. Calling this method with the fileName parameter set to null has no effect.
	 * <p>
	 * This method is useful for customizing the connections string after the deployment of an application.
	 * A typical use case for this method is the following scenario: imagine a customer who needs to send a
	 * log file to customer service to analyse a software problem. If the software in question uses this
	 * loadConnections method, the customer service just needs to send a prepared connections file to the customer.
	 * To enable the logging, the customer now just needs to drop this file to the application's installation
	 * directory or any other predefined location.
	 * <p>
	 * See loadConfiguration for a method which is not limited to loading the connections string,
	 * but is also capable of loading any other property of this object from a file.
	 * <p>
	 * The loadConnections and loadConfiguration methods are both capable of detecting the string encoding
	 * of the connections and configuration files. Please see the loadConfiguration method for details.
	 * <p>
	 * To automatically replace placeholders in a loaded connections string, you can use so-called connection
	 * variables. Please have a look at the setVariable method for more information.
	 *
	 * @param fileName The file to load the connections string from
	 */
	public void loadConnections(String fileName) {
		loadConnections(fileName, false);
	}

	/**
	 * Overloaded. Loads the connections string from a file.
	 *
	 * <p>This method loads the {@link SmartInspect#setConnections} connections string from a file.
	 * This file should be a plain text file containing a line like in the following example:</p>
	 *
	 * <code>connections=file(filename=c:\\log.sil)</code>
	 *
	 * <p>Empty, unrecognized lines and lines beginning with a ';' character are ignored.
	 * This version of the method enables logging automatically unless the doNotEnable parameter is true.
	 * Please note that the doNotEnable parameter has no effect if this SmartInspect instance is already enabled.</p>
	 *
	 * <p>The {@link SmartInspectListener#onError} Error event is used to notify the application if the
	 * specified file cannot be opened or does not contain a {@link SmartInspect#setConnections} connections string.
	 * The {@link SmartInspect#setConnections} connections string and the {@link SmartInspect#setEnabled}
	 * enabled status of this instance are not changed if such an error occurs.</p>
	 *
	 * <p>The {@link SmartInspectListener#onError} Error event is also used if a connections string could be read but
	 * is found to be invalid. In this case, an instance of the InvalidConnectionsException exception
	 * type is passed to the {@link SmartInspectListener#onError} Error event.
	 * Calling this method with the fileName parameter set to null has no effect.</p>
	 *
	 * <p>This version of the method accepts the doNotEnable parameter. If this parameter is set to true,
	 * the {@link SmartInspect#setEnabled} enabled status is not changed. Otherwise this SmartInspect
	 * instance will be enabled. Calling this method with the fileName parameter set to null has no effect.</p>
	 *
	 * <p>This method is useful for customizing the connections string after the deployment of an application.
	 * A typical use case for this method is the following scenario: imagine a customer who needs to send a
	 * log file to customer service to analyse a software problem. If the software in question uses this
	 * loadConnections method, the customer service just needs to send a prepared connections file to the
	 * customer. To enable the logging, the customer now just needs to drop this file to the application's
	 * installation directory or any other predefined location.</p>
	 *
	 * <p>See loadConfiguration for a method which is not limited to loading the connections string,
	 * but is also capable of loading any other property of this object from a file.</p>
	 *
	 * <p>The loadConnections and loadConfiguration methods are both capable of detecting the string
	 * encoding of the connections and configuration files. Please see the loadConfiguration method for details.</p>
	 *
	 * <p>To automatically replace placeholders in a loaded connections string, you can use so called
	 * connection variables. Please have a look at the setVariable method for more information.</p>
	 *
	 * @param fileName    The file to load the connections string from
	 * @param doNotEnable Specifies if this instance shouldn't be enabled automatically
	 */
	public void loadConnections(String fileName, boolean doNotEnable) {
		if (fileName == null) {
			return;
		}

		String connections = null;

		try {
			// Try to read the connections string.
			connections = readConnections(fileName);
		} catch (Exception e) {
			// Catch exceptions while trying to read the 
			// connections string and fire the error event.
			doError(e);
		}

		if (connections == null) {
			return; // No connections string has been found.
		}

		synchronized (this.fLock) {
			if (tryConnections(connections)) {
				if (!doNotEnable) {
					enable();
				}
			}
		}
	}

	private static String readConnections(String fileName)
			throws LoadConnectionsException {
		try {
			Configuration config = new Configuration();
			try {
				config.loadFromFile(fileName);
				if (config.contains("connections")) {
					return config.readString("connections", null);
				}
			} finally {
				config.clear();
			}

			throw new SmartInspectException(CONNECTIONS_NOT_FOUND_ERROR);
		} catch (Exception e) {
			throw new LoadConnectionsException(fileName, e.getMessage());
		}
	}

	/**
	 * Please see the setConnections method for a detailed description
	 * of the connections string and its function.
	 *
	 * @return The connections string of this instance
	 */
	public String getConnections() {
		return this.fConnections;
	}

	/**
	 * Sets all connections used by a SmartInspect instance.
	 * <p>
	 * You can set multiple connections by separating the connections with commas. A connection consists
	 * of a protocol identifier like "file" plus optional protocol parameters in parentheses. If you, for
	 * example, want to log to a file, the connections string must be set to "file()". You can specify
	 * the filename in the parentheses after the protocol identifier like this:
	 * "file(filename=\"c:\\mylogfile.sil\")". Note that only if this instance is enabled, the connections
	 * try to connect to their destinations immediately. By default, no connections are used.
	 * <p>
	 * See the Protocol class for a list of available protocols and ProtocolFactory for a way to add
	 * your own custom protocols. Furthermore have a look at the loadConnections and loadConfiguration
	 * methods, which can load a connections string from a file. Also, for a class which assists in
	 * building connections strings, please refer to the documentation of the ConnectionsBuilder class.
	 * To automatically replace placeholders in the given connections string, you can use so called
	 * connection variables. Please have a look at the setVariable method for more information.
	 * <p>
	 * Please note that an InvalidConnectionsException exception is thrown if an invalid connections
	 * string is supplied.
	 *
	 * <pre>
	 * SiAuto.si.setConnections("");
	 * SiAuto.si.setConnections("file()");
	 * SiAuto.si.setConnections("file(filename=\"log.sil\", append=true)");
	 * SiAuto.si.setConnections("file(append=true), tcp(host=\"localhost\")");
	 * SiAuto.si.setConnections("file(), file(filename=\"anotherlog.sil\")");
	 * </pre>
	 *
	 * @throws InvalidConnectionsException if invalid syntax, unknown protocols or inexistent options are found
	 */
	public void setConnections(String connections)
			throws InvalidConnectionsException {
		synchronized (this.fLock) {
			applyConnections(connections);
		}
	}

	private void applyConnections(String connections)
			throws InvalidConnectionsException {
		// First remove the old connections.
		removeConnections();
		if (connections != null) {
			// Then create the new connections and assign the
			// connections string.
			createConnections(connections);
			this.fConnections = connections;

			if (isEnabled()) {
				// This instance is currently enabled, so we can try
				// to connect now.
				connect();
			}
		}
	}

	private boolean tryConnections(String connections) {
		boolean result = false;

		if (connections != null) {
			try {
				applyConnections(connections);
				result = true;
			} catch (InvalidConnectionsException e) {
				doError(e);
			}
		}

		return result;
	}

	private void removeConnections() {
		disconnect();
		this.fIsMultiThreaded = false; /* See createConnections */
		this.fProtocols.clear();
		this.fConnections = "";
	}

	/**
	 * Loads the properties and sessions of this SmartInspect instance from a file.
	 * The given file should be a plain text file containing key/value pairs. Each key/value pair is expected to be
	 * on its own line. Empty, unrecognized lines and lines beginning with a ';' character are ignored.
	 * The <code>SmartInspectListener.onError</code> event is used to notify the application if an error occurs while trying to load
	 * the configuration from the specified file. Such errors include I/O errors like trying to open a file which does not exist, for
	 * example.
	 * The <code>SmartInspectListener.onError</code> event is also used if the specified configuration file contains an invalid
	 * connections string. In this case, an instance of the <code>InvalidConnectionsException</code> exception type is passed to the
	 * Error event.
	 *
	 * <p>
	 * Note:
	 * <ul>
	 *     <li>Calling this method with the fileName parameter set to null has no effect.</li>
	 *     <li>To monitor a SmartInspect configuration file for changes, please have a look at the ConfigurationTimer class.</li>
	 *     <li>The case of the configuration properties doesn't matter. This means, it makes no difference if you specify 'defaultlevel' or 'DefaultLevel' as key, for example.</li>
	 *     <li>This method is particularly useful for loading the properties of this SmartInspect instance after the deployment of an application. A typical use case for this method is the following scenario: imagine a customer who needs to send a log file to customer service to analyse a software problem. If the software in question uses this loadConfiguration method, the customer service just needs to send a prepared configuration file to the customer.</li>
	 * </ul>
	 * <p>
	 * Refer to <code>{@literal @}example</code> for a typical configuration file format.
	 *
	 * @param fileName The name of the file to load the configuration from.
	 * @see com.gurock.smartinspect.ConfigurationTimer
	 * @see com.gurock.smartinspect.LoadConfigurationException
	 * @see com.gurock.smartinspect.InvalidConnectionsException
	 */
	public void loadConfiguration(String fileName) {
		if (fileName == null) {
			return;
		}

		Configuration config = new Configuration();

		try {
			try {
				config.loadFromFile(fileName);
			} catch (Exception e) {
				doError(new LoadConfigurationException(fileName,
						e.getMessage()));
				return;
			}

			synchronized (this.fLock) {
				applyConfiguration(config);
			}

			this.fSessions.loadConfiguration(config);
		} finally {
			config.clear();
		}
	}

	private void applyConfiguration(Configuration config) {
		if (config.contains("appname")) {
			this.fAppName = config.readString("appname", this.fAppName);
		}

		// The `enabled' configuration value needs to be handled special,
		// because its appearance and value have a direct impact on how
		// to treat the `connections' value and the order in which to
		// apply the values:
		//
		// If the `enabled' value is found, it is very important to
		// differentiate between the values true and false. If the
		// `enabled' value is false, the user obviously either wants
		// to disable this object or keep it disabled. To correctly
		// disable this SmartInspect instance, we need to do that before
		// the connections string is changed. Otherwise it can happen
		// that this SmartInspect instance temporarily uses the new
		// connections string (exactly in the case when it is already
		// enabled).
		//
		// Handling an `enabled' value of true is the other way round.
		// We cannot enable this SmartInspect instance before setting
		// the `connections' value, because this would cause this
		// SmartInspect instance to temporarily use its old connections
		// string.

		String connections = config.readString("connections", null);

		if (config.contains("enabled")) {
			boolean enabled = config.readBoolean("enabled", false);

			if (enabled) {
				tryConnections(connections);
				enable();
			} else {
				disable();
				tryConnections(connections);
			}
		} else {
			tryConnections(connections);
		}

		if (config.contains("level")) {
			this.fLevel = config.readLevel("level", this.fLevel);
		}

		if (config.contains("defaultlevel")) {
			this.fDefaultLevel = config.readLevel("defaultlevel",
					this.fDefaultLevel);
		}
	}

	private Protocol findProtocol(String caption) {
		for (int i = 0; i < this.fProtocols.size(); i++) {
			Protocol p = (Protocol) this.fProtocols.get(i);

			if (p.getCaption().equalsIgnoreCase(caption)) {
				return p;
			}
		}

		return null;
	}

	/**
	 * Executes a custom protocol action of a connection.
	 * <p>
	 * This method dispatches the action and state parameters to the connection identified by the caption argument. If no suitable
	 * connection can be found, the Error event is used. The Error event is also used if an exception is thrown in the custom protocol action.
	 * <p>
	 * The SmartInspect Java library currently implements one custom protocol action in MemoryProtocol. The MemoryProtocol class
	 * is used for writing log packets to memory. On request, it can write its internal queue of packets to a user-supplied
	 * stream or Protocol object with a custom protocol action.
	 * <p>
	 * The request for executing the custom action and writing the queue can be initiated with this dispatch method. Please see
	 * the example section below for details.
	 * <p>
	 * For more information about custom protocol actions, please
	 * refer to the {@link com.gurock.smartinspect.protocols.Protocol#dispatch} method. Also have a look at
	 * Protocol.isValidOption() method which explains how to set
	 * the caption of a connection.
	 * <p>
	 * Please note that the custom protocol action is executed asynchronously if the requested connection operates in
	 * asynchronous protocol mode.
	 * <p>
	 * If the supplied caption argument is null, this method does nothing and returns immediately.
	 * <p>
	 * <pre>
	 * // Set the connections string and enable logging. We do not
	 * // specify a caption for the memory connection and stick with
	 * // the default. By default, the caption of a connection is set
	 * // to the name of the protocol, in our case 'mem'.
	 * SiAuto.si.setConnections("mem()");
	 * SiAuto.si.setEnabled(true);
	 *
	 * // Instrument your application with log statements as usual.
	 * SiAuto.main.logMessage("This is a message");
	 * SiAuto.main.logMessage("This is a message");
	 *
	 * // Then, in case of an unexpected event, for example, in a
	 * // global exception handler, you can write the entire queue
	 * // of packets of your memory protocol connection to a file
	 * // by using the dispatch method.
	 * OutputStream os = new FileOutputStream("log.sil");
	 * try
	 * {
	 * 	SiAuto.si.dispatch("mem", 0, os);
	 * }
	 * finally
	 * {
	 * 	os.close();
	 * }
	 * </pre>
	 * <p>
	 * <pre>
	 * ...
	 *
	 * // Alternative dispatch call with a Protocol object which
	 * // sends the queue content to a local Console via a named
	 * // pipe.
	 * Protocol p = new PipeProtocol();
	 * try
	 * {
	 * 	// Optionally set some protocol options
	 * 	// p.initialize("");
	 * 	p.connect();
	 * 	try
	 * 	{
	 * 		SiAuto.si.dispatch("mem", 0, p);
	 * 	}
	 * 	finally
	 * 	{
	 * 		p.disconnect();
	 * 	}
	 * }
	 * finally
	 * {
	 * 	p.dispose();
	 * }
	 * </pre>
	 *
	 * @param caption The identifier of the connection. Not allowed to be null
	 * @param action The action to execute by the requested connection
	 * @param state An optional object which encapsulates additional protocol specific information about the custom action. Can be null
	 * @see com.gurock.smartinspect.protocols.Protocol#dispatch
	 */
	public void dispatch(String caption, int action, Object state) {
		if (caption == null) {
			return;
		}

		synchronized (this.fLock) {
			try {
				Protocol protocol = findProtocol(caption);

				if (protocol == null) {
					throw new SmartInspectException(CAPTION_NOT_FOUND);
				}

				protocol.dispatch(new ProtocolCommand(action, state));
			} catch (Exception e) {
				doError(e);
			}
		}
	}

	/**
	 * Returns the default property values for new sessions.
	 * This method lets you specify the default property values for new sessions which will be created by or passed to the addSession
	 * method. Please see the addSession method for details. For information about the available session properties, please refer
	 * to the documentation of the Session class.
	 *
	 * @return The default property values for new sessions
	 */
	public SessionDefaults getSessionDefaults() {
		return this.fSessions.getDefaults();
	}

	/**
	 * Adds a new or updates an existing connection variable.
	 * This method sets the value of a given connection variable. A connection variable is a placeholder for strings in the connections string.
	 * When setting a connections string (or loading it from a file with loadConfiguration), any variables which have previously been defined
	 * with setVariable are automatically replaced with their respective values. The variables in the connections string are expected to have
	 * the following form: $variable$. If a connection variable with the given key already exists, its value is overridden. To delete a connection
	 * variable, use unsetVariable. This method does nothing if the key or value argument is null. Connection variables are especially useful
	 * if you load a connections string from a file and would like to handle some protocol options in your application instead of the configuration file.
	 * For example, if you encrypt log files, you probably do not want to specify the encryption key directly in your configuration file for security
	 * reasons. With connection variables, you can define a variable for the encryption key with setVariable and then reference this variable in your
	 * configuration file. The variable is then automatically replaced with the defined value when loading the configuration file. Another example deals
	 * with the directory or path of a log file. If you include a variable in the path of your log file, you can later replace it in your application with
	 * the real value. This might come in handy if you want to write a log file to an environment specific value, such as an application data directory,
	 * for example.
	 * <p>
	 * Example:
	 * <pre>
	 *   // Define the variable "key" with the value "secret"
	 *   SiAuto.si.setVariable("key", "secret");
	 *   ...
	 *   // And include the variable $key$ in the related connections
	 *   // string (the connections string can either be set directly
	 *   // or loaded from a file).
	 *   file(encrypt="true", key="$key$")
	 * </pre>
	 *
	 * @param key   The key of the connection variable
	 * @param value The value of the connection variable
	 */
	public void setVariable(String key, String value) {
		if (key != null && value != null) {
			this.fVariables.put(key, value);
		}
	}

	/**
	 * Returns the value of a connection variable.
	 * Please see the setVariable method for more information
	 * about connection variables.
	 *
	 * @param key The key of the connection variable
	 * @return The value for the given connection variable or null if the
	 * connection variable is unknown
	 */
	public String getVariable(String key) {
		if (key == null) {
			return null;
		} else {
			return this.fVariables.get(key);
		}
	}

	/**
	 * Unsets an existing connection variable
	 *
	 * <p>
	 * This method deletes the connection variable specified by the
	 * given key. Nothing happens if the connection variable doesn't
	 * exist or if the passed key is null.
	 * </p>
	 *
	 * @param key The key of the connection variable to delete
	 */
	public void unsetVariable(String key) {
		if (key != null) {
			this.fVariables.remove(key);
		}
	}

	/**
	 * Adds and returns a new Session instance with this SmartInspect object set as parent.
	 * This method allocates a new session with this SmartInspect instance set as parent
	 * and the supplied sessionName parameter set as session name. The returned session
	 * will be configured with the default session properties as specified by the
	 * getSessionDefaults method. This default configuration can be overridden on
	 * a per-session basis by loading the session configuration with the
	 * loadConfiguration method. Please see the loadConfiguration documentation for details.
	 * This version of the method does not save the returned session for later access.
	 *
	 * @param sessionName The name for the new session. Not allowed to be null
	 * @return The new Session instance or null if the supplied sessionName parameter is null
	 */
	public Session addSession(String sessionName) {
		return addSession(sessionName, false);
	}

	/**
	 * Overloaded. Adds and returns a new Session instance with this
	 * SmartInspect object set as parent and optionally saves it for
	 * later access.
	 * <p>
	 * This method allocates a new session with this SmartInspect
	 * instance set as parent and the supplied sessionName parameter
	 * set as session name. The returned session will be configured
	 * with the default session properties as specified by the
	 * getSessionDefaults method. This default configuration can be
	 * overridden on a per-session basis by loading the session
	 * configuration with the loadConfiguration method. Please see
	 * the loadConfiguration documentation for details.
	 * <p>
	 * If the 'store' parameter is true, the created and returned
	 * session is stored for later access and can be retrieved with
	 * the getSession method. To remove a created session from the
	 * internal list, call the deleteSession method.
	 * <p>
	 * If this method is called multiple times with the same session
	 * name, then the getSession method operates on the session which
	 * got added last. If the sessionName parameter is null, this
	 * method does nothing and returns null as well
	 *
	 * @param sessionName The name for the new session. Not allowed to be null
	 * @param store       Indicates if the newly created session should be stored for later access
	 * @return The new Session instance or null if the supplied sessionName parameter is null
	 */
	public Session addSession(String sessionName, boolean store) {
		if (sessionName == null) {
			return null;
		}

		Session session = new Session(this, sessionName);
		this.fSessions.add(session, store);
		return session;
	}

	/**
	 * Overloaded. Adds an existing Session instance to the internal list of sessions
	 * and saves it for later access.
	 * <p>
	 * This method adds the passed session to the internal list of sessions and
	 * saves it for later access. The passed session will be configured with the
	 * default session properties as specified by the getSessionDefaults method.
	 * This default configuration can be overridden on a per-session basis by
	 * loading the session configuration with the loadConfiguration method.
	 * Please see the loadConfiguration documentation for details.
	 * <p>
	 * The passed session can later be retrieved with the getSession method.
	 * To remove an added session from the internal list, call the deleteSession method.
	 *
	 * @param session The session to store
	 */
	public void addSession(Session session) {
		this.fSessions.add(session, true);
	}

	/**
	 * Removes a session from the internal list of sessions. This method removes a session
	 * which has previously been added with and returned by the addSession method.
	 * After this method returns, the getSession method returns null when called
	 * with the same session name unless a different session with the same name has been added.
	 * This method does nothing if the supplied session argument is null.
	 *
	 * @param session The session to remove from the lookup table of sessions. Not allowed to be null.
	 */
	public void deleteSession(Session session) {
		this.fSessions.delete(session);
	}

	/**
	 * Returns a previously added session.
	 * <p>
	 * This method returns a session which has previously been added
	 * with the addSession method and can be identified by the supplied
	 * sessionName argument. If the requested session is unknown or if
	 * the sessionName argument is null, this method returns null.
	 * <p>
	 * Note that the behavior of this method can be unexpected in terms
	 * of the result value if multiple sessions with the same name have
	 * been added. In this case, this method returns the session which
	 * got added last and not necessarily the session which you expect.
	 * <p>
	 * Adding multiple sessions with the same name should therefore be
	 * avoided.
	 *
	 * @param sessionName The name of the session to lookup and return. Not allowed to be null
	 * @return The requested session or null if the supplied sessionName is null or if the session is unknown
	 */
	public Session getSession(String sessionName) {
		return this.fSessions.get(sessionName);
	}

	/**
	 * Updates an entry in the internal lookup table of sessions.
	 * <p>
	 * Once the name of a session has changed, this method is called
	 * to update the internal session lookup table. The {@code to} parameter
	 * specifies the new name and the {@code from} name the old name of the
	 * session. After this method returns, the new name can be passed
	 * to the getSession method to lookup the supplied session.
	 *
	 * @param session The session whose name has changed and whose entry should be updated
	 * @param to      The new name of the session
	 * @param from    The old name of the session
	 */
	public void updateSession(Session session, String to, String from) {
		this.fSessions.update(session, to, from);
	}

	/**
	 * Adds a new listener for the events of this object.
	 * <p>
	 * This methods adds a new listener for the events of this SmartInspect
	 * object. This can be useful to customize the logging behavior or get
	 * informed about errors. Please see the SmartInspectListener interface
	 * for event examples and details. Also see the documentation of the
	 * SmartInspectAdapter class which simplifies the event handling.
	 *
	 * @param listener The listener to add
	 */
	public void addListener(SmartInspectListener listener) {
		if (listener != null) {
			synchronized (this.fListeners) {
				this.fListeners.add(listener);
			}
		}
	}

	/**
	 * Removes an existing listener for the events of this object.
	 * <p>
	 * This method removes the supplied listener from the event system
	 * of this object. After the listener has been removed, it will no
	 * longer be notified about any events of this object.
	 *
	 * @param listener The listener to remove
	 */
	public void removeListener(SmartInspectListener listener) {
		if (listener != null) {
			synchronized (this.fListeners) {
				this.fListeners.remove(listener);
			}
		}
	}

	/**
	 * Removes all registered listeners for the events of this object.
	 * <p>
	 * This method removes all registered event listeners. After calling
	 * this method, the previously registered listeners will no longer be
	 * notified about any events of this object.
	 */
	public void clearListeners() {
		synchronized (this.fListeners) {
			this.fListeners.clear();
		}
	}

	/**
	 * Invokes the Filter event handlers and determines if the supplied packet should be sent or not.
	 * Derived classes can override this method to intercept the Filter event.
	 *
	 * @param packet The packet which is about to be processed
	 * @return True if the supplied packet shall be filtered and thus not be sent and false otherwise
	 */
	protected boolean doFilter(Packet packet) {
		synchronized (this.fListeners) {
			if (!this.fListeners.isEmpty()) {
				FilterEvent e = new FilterEvent(this, packet);
				Iterator it = this.fListeners.iterator();

				while (it.hasNext()) {
					((SmartInspectListener) it.next()).onFilter(e);
				}

				// Check if a listener cancelled the packet.
				if (e.getCancel()) {
					return true;
				}
			}
		}

		// Do not filter the packet
		return false;
	}

	/**
	 * Invokes the Log Entry event handlers.
	 * <p>
	 * Derived classes can override this method to intercept the
	 * {@link SmartInspectListener#onLogEntry}.
	 *
	 * @param logEntry The Log Entry which has just been processed
	 */
	protected void doLogEntry(LogEntry logEntry) {
		synchronized (this.fListeners) {
			if (!this.fListeners.isEmpty()) {
				Iterator it = this.fListeners.iterator();
				LogEntryEvent e = new LogEntryEvent(this, logEntry);

				while (it.hasNext()) {
					((SmartInspectListener) it.next()).onLogEntry(e);
				}
			}
		}
	}

	/**
	 * Invokes the Control Command event handlers.
	 *
	 * <p>Derived classes can override this method to intercept the
	 * Control Command event. {@link SmartInspectListener#onControlCommand}
	 *
	 * @param controlCommand The Control Command which has just been processed
	 */
	protected void doControlCommand(ControlCommand controlCommand) {
		synchronized (this.fListeners) {
			if (!this.fListeners.isEmpty()) {
				Iterator it = this.fListeners.iterator();
				ControlCommandEvent e = new ControlCommandEvent(this,
						controlCommand);

				while (it.hasNext()) {
					((SmartInspectListener) it.next()).onControlCommand(e);
				}
			}
		}
	}

	/**
	 * Invokes the Watch event handlers.
	 *
	 * <p> Derived classes can override this method to intercept the
	 * {@link SmartInspectListener#onWatch Watch event}.</p>
	 *
	 * @param watch The Watch which has just been processed
	 */
	protected void doWatch(Watch watch) {
		synchronized (this.fListeners) {
			if (!this.fListeners.isEmpty()) {
				Iterator it = this.fListeners.iterator();
				WatchEvent e = new WatchEvent(this, watch);

				while (it.hasNext()) {
					((SmartInspectListener) it.next()).onWatch(e);
				}
			}
		}
	}

	/**
	 * Invokes the Error event handlers.
	 * Derived classes can override this method to intercept the
	 * {@link SmartInspectListener#onError, Error event}
	 *
	 * @param ex The occurred exception
	 */
	protected void doError(Exception ex) {
		synchronized (this.fListeners) {
			if (!this.fListeners.isEmpty()) {
				Iterator it = this.fListeners.iterator();
				ErrorEvent e = new ErrorEvent(this, ex);

				while (it.hasNext()) {
					((SmartInspectListener) it.next()).onError(e);
				}
			}
		}
	}

	/**
	 * Invokes the Process Flow event handlers.
	 * <p>
	 * Derived classes can override this method to intercept the
	 * {@link SmartInspectListener#onProcessFlow Process Flow event}.
	 *
	 * @param processFlow The Process Flow entry which has just been processed
	 */
	protected void doProcessFlow(ProcessFlow processFlow) {
		synchronized (this.fListeners) {
			if (!this.fListeners.isEmpty()) {
				Iterator it = this.fListeners.iterator();
				ProcessFlowEvent e = new ProcessFlowEvent(this, processFlow);

				while (it.hasNext()) {
					((SmartInspectListener) it.next()).onProcessFlow(e);
				}
			}
		}
	}

	/**
	 * Passes a packet to the internal connections.
	 *
	 * <p>The processPacket method passes the supplied packet to the
	 * Protocol.writePacket method of every connection in the internal
	 * connection list. The {@link SmartInspectListener#onError
	 * Error event} is used to report any errors.</p>
	 *
	 * @param packet The packet to log
	 */
	private final void processPacket(Packet packet) {
		synchronized (this.fLock) {
			for (int i = 0; i < this.fProtocols.size(); i++) {
				try {
					// Write the packet using the protocol.
					Protocol p = (Protocol) this.fProtocols.get(i);
					p.writePacket(packet);
				} catch (Exception e) {
					doError(e);
				}
			}
		}
	}

	/**
	 * Logs a Log Entry.
	 * After setting the application name and hostname of the supplied
	 * Log Entry, this method determines if the Log Entry should really
	 * be sent by invoking the doFilter method. If the Log Entry passes
	 * the filter test, it will be logged and the
	 * SmartInspectListener.onLogEntry event is fired.
	 *
	 * @param logEntry The Log Entry to log
	 */
	public final void sendLogEntry(LogEntry logEntry) {
		/* Initialize the log entry packet for safe multi-threaded
		 * access only if this SmartInspect object has one or more
		 * connections which operate in asynchronous protocol mode.
		 * Also see createConnections. */

		if (this.fIsMultiThreaded) {
			logEntry.setThreadSafe(true);
		}

		/* Then fill the properties we are responsible for. */
		logEntry.setAppName(getAppName());
		logEntry.setHostName(getHostName());

		try {
			if (!doFilter(logEntry)) {
				processPacket(logEntry);
				doLogEntry(logEntry);
			}
		} catch (Exception e) {
			doError(e);
		}
	}

	/**
	 * Logs a Control Command.
	 *
	 * <p>At first, this method determines if the Control Command should
	 * really be sent by invoking the doFilter method. If the Control
	 * Command passes the filter test, it will be logged and the
	 * SmartInspectListener.onControlCommand event is fired</p>
	 *
	 * @param controlCommand The Control Command to log
	 */
	public final void sendControlCommand(ControlCommand controlCommand) {
		/* Initialize the control command for safe multi-threaded
		 * access only if this SmartInspect object has one or more
		 * connections which operate in asynchronous protocol mode.
		 * Also see createConnections. */

		if (this.fIsMultiThreaded) {
			controlCommand.setThreadSafe(true);
		}

		try {
			if (!doFilter(controlCommand)) {
				processPacket(controlCommand);
				doControlCommand(controlCommand);
			}
		} catch (Exception e) {
			doError(e);
		}
	}

	/**
	 * Logs a Watch.
	 * <p>
	 * At first, this method determines if the Watch should really be
	 * sent by invoking the doFilter method. If the Watch passes the
	 * filter test, it will be logged and the SmartInspectListener.onWatch
	 * event is fired.
	 *
	 * @param watch The Watch to log
	 */
	public final void sendWatch(Watch watch) {
		/* Initialize the watch packet for safe multi-threaded
		 * access only if this SmartInspect object has one or more
		 * connections which operate in asynchronous protocol mode.
		 * Also see createConnections. */

		if (this.fIsMultiThreaded) {
			watch.setThreadSafe(true);
		}

		try {
			if (!doFilter(watch)) {
				processPacket(watch);
				doWatch(watch);
			}
		} catch (Exception e) {
			doError(e);
		}
	}

	/**
	 * Logs a Process Flow entry.
	 * After setting the hostname of the supplied Process Flow entry,
	 * this method determines if the Process Flow entry should really
	 * be sent by invoking the doFilter method. If the Process
	 * Flow entry passes the filter test, it will be logged and the
	 * SmartInspectListener.onProcessFlow event is fired.
	 *
	 * @param processFlow The Process Flow entry to log
	 */
	public final void sendProcessFlow(ProcessFlow processFlow) {
		/* Initialize the process flow for safe multi-threaded
		 * access only if this SmartInspect object has one or more
		 * connections which operate in asynchronous protocol mode.
		 * Also see createConnections. */

		if (this.fIsMultiThreaded) {
			processFlow.setThreadSafe(true);
		}

		/* Fill the properties we are responsible for. */
		processFlow.setHostName(getHostName());

		try {
			if (!doFilter(processFlow)) {
				processPacket(processFlow);
				doProcessFlow(processFlow);
			}
		} catch (Exception e) {
			doError(e);
		}
	}

	/**
	 * Releases all resources of this SmartInspect object.
	 * <p>
	 * This method disconnects and removes all internal connections
	 * and disables this instance. Moreover, all previously stored
	 * sessions will be removed.
	 */
	public void dispose() {
		synchronized (this.fLock) {
			this.fEnabled = false;
			removeConnections();
		}

		this.fSessions.clear();
	}
}
