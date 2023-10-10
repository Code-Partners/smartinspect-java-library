/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import java.awt.Color;

import java.io.Reader;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;
import java.util.Collection;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.AccessibleObject;
import java.text.MessageFormat;

/**
 * Logs all kind of data and variables to the SmartInspect Console or to a log file.
 *
 * <p>The Session class offers dozens of useful methods for sending any kind of
 * data with the assistance of its parent. Sessions can send simple messages,
 * warnings, errors and more complex things like pictures, objects, exceptions,
 * system information and much more. They are even able to send variable watches,
 * generate illustrated process and thread information or control the behavior
 * of the SmartInspect Console. It is possible, for example, to clear the entire
 * log in the Console by calling the clearLog method.</p>
 *
 * <p>Please note that log methods of this class do nothing and return immediately
 * if the session is currently inactive its parent is disabled or the log level
 * is not sufficient.</p>
 *
 * <p>This class is fully threadsafe.</p>
 */
public class Session {
	protected static final Color
			DEFAULT_COLOR = new Color(0x05, 0x00, 0x00, 0xff);

	private String fName;
	private Object fCheckpointLock;
	private int fCheckpointCounter;
	private SmartInspect fParent;
	private Color fColor;
	private boolean fActive;
	private Level fLevel;
	private HashMap fCounter;
	private HashMap fCheckpoints;
	private boolean fStored;

	public Session(SmartInspect parent, String name) {
		this.fCheckpointLock = new Object();

		// Initialize the remaining fields.

		this.fParent = parent;
		this.fCheckpointCounter = 0;

		if (name == null) {
			this.fName = "";
		} else {
			this.fName = name;
		}

		this.fLevel = Level.Debug;
		this.fActive = true; // Active by default.
		this.fCounter = new HashMap();
		this.fCheckpoints = new HashMap();
		resetColor();
	}

	/**
	 * Returns if this session is stored in the session tracking list of its Parent.
	 * <p>
	 * See the SmartInspect.getSession and SmartInspect.addSession methods for more information about session tracking.
	 *
	 * @return True if this session is stored in the session tracking list and false otherwise
	 */
	protected boolean isStored() {
		return this.fStored;
	}

	/**
	 * Indicates if this session is stored in the session tracking
	 * list of its Parent. See the SmartInspect.getSession and
	 * SmartInspect.addSession methods for more information about
	 * session tracking.
	 *
	 * @param stored True if this session is stored in the session tracking list
	 *               and false otherwise
	 */
	protected void setStored(boolean stored) {
		this.fStored = stored;
	}

	/**
	 * Sets the session name used for Log Entries.
	 * The session name helps you to identify Log Entries from different
	 * sessions in the SmartInspect Console. If you set the session name
	 * to null, the session name will be empty when sending Log Entries.
	 *
	 * @param name The new session name
	 */
	public void setName(String name) {
		if (name == null) {
			name = "";
		}

		if (this.fStored) {
			this.fParent.updateSession(this, name, this.fName);
		}

		this.fName = name;
	}

	/**
	 * Returns the session name used for Log Entries.
	 * The session name helps you to identify Log Entries from different
	 * sessions in the SmartInspect Console.
	 *
	 * @return The session name used for Log Entries.
	 */
	public String getName() {
		return this.fName;
	}

	/**
	 * Specifies if the session is currently active.
	 * If false is passed to this method, all logging methods of this class will return immediately and do nothing. Please note that
	 * the parent of this session also needs to be enabled in order to log information.
	 * This method is especially useful if you are using multiple sessions at once and want to deactivate a subset of these sessions.
	 * To deactivate all your sessions, you can use the setEnabled method of the parent.
	 *
	 * @param active A boolean value to activate or deactivate this instance
	 */
	public void setActive(boolean active) {
		this.fActive = active;
	}

	/**
	 * Indicates if this session is currently active or not.
	 *
	 * @return True if this session is currently active and false otherwise.
	 */
	public boolean isActive() {
		return this.fActive;
	}

	/**
	 * Returns the log level of this Session object.
	 *
	 * <p>Each Session object can have its own log level. A log message
	 * is only logged if its log level is greater than or equal to the
	 * log level of a session and the session's parent. Log levels can thus
	 * be used to limit the logging output to important messages only.</p>
	 *
	 * @return The log level of this session
	 */
	public Level getLevel() {
		return this.fLevel;
	}

	/**
	 * Sets the log level of this Session object.
	 * Each Session object can have its own log level. A log message
	 * is only logged if its log level is greater than or equal to the
	 * log level of a session and the session {@link Session#getParent}.
	 * Log levels can thus be used to limit the logging output to
	 * important messages only.
	 * <p>
	 * This method does nothing if the level parameter is null.
	 *
	 * @param level The new log level for this session.
	 */
	public void setLevel(Level level) {
		if (level != null) {
			this.fLevel = level;
		}
	}

	/**
	 * Overloaded. Indicates if information can be logged for a certain log level or not.
	 * <p>
	 * This method is used by the logging methods in this class to determine if information should be logged or not. When
	 * extending the Session class by adding new log methods to a derived class, it is recommended to call this method first.
	 * This method returns false if the supplied level argument is null.
	 *
	 * @param level The log level to check for
	 * @return True if information can be logged and false otherwise
	 */
	public boolean isOn(Level level) {
		if (level == null) {
			return false;
		} else {
			return this.fActive &&
					this.fParent.isEnabled() &&
					level.greaterEqual(this.fLevel) &&
					level.greaterEqual(this.fParent.getLevel());
		}
	}

	/**
	 * Overloaded. Indicates if information can be logged or not.
	 * <p>
	 * This method is used by the logging methods in this class
	 * to determine if information should be logged or not. When
	 * extending the Session class by adding new log methods to a
	 * derived class it is recommended to call this method first.
	 *
	 * @return True if information can be logged and false otherwise
	 */
	public boolean isOn() {
		return this.fActive && this.fParent.isEnabled();
	}

	/**
	 * Returns the parent of the session.
	 *
	 * <p>The parent of a session is a SmartInspect instance. It is
	 * responsible for sending the packets to the SmartInspect
	 * Console or for writing them to a file. If the parent is not
	 * {@link SmartInspect#setEnabled}, enabled, all logging methods
	 * of this class will return immediately and do nothing.</p>
	 *
	 * @return The parent of this session
	 */
	public SmartInspect getParent() {
		return this.fParent;
	}

	/**
	 * Sets the background color in the SmartInspect Console of this session.
	 * The session color helps you to identify Log Entries from different sessions
	 * in the SmartInspect Console by changing the background color. Please note
	 * that if you pass a null reference, then the color will not be changed.
	 *
	 * @param color The new background color
	 */
	public void setColor(Color color) {
		if (color != null) {
			this.fColor = color;
		}
	}

	/**
	 * Returns the background color in the SmartInspect Console of this session.
	 * <p>
	 * The session color helps you to identify Log Entries from
	 * different sessions in the SmartInspect Console by changing the
	 * background color.
	 *
	 * @return The background color in the SmartInspect Console of this session
	 */
	public Color getColor() {
		return this.fColor;
	}

	/**
	 * Resets the session color to its default value.
	 * <p>
	 * The default color of a session is transparent.
	 */
	public void resetColor() {
		setColor(DEFAULT_COLOR);
	}

	/**
	 * Overloaded. Logs a simple separator with the default log level.
	 * This method instructs the Console to draw a separator. A
	 * separator is intended to group related Log Entries
	 * and to separate them visually from others. This method can help
	 * organizing Log Entries in the Console. See addCheckpoint for a
	 * method with a similar intention.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @see com.gurock.smartinspect.Session#addCheckpoint
	 */
	public void logSeparator() {
		logSeparator(this.fParent.getDefaultLevel());
	}

	/**
	 * Overloaded. Logs a simple separator with a custom log level.
	 * This method instructs the Console to draw a separator. A
	 * separator is intended to group related Log Entries
	 * and to separate them visually from others. This method can help
	 * organizing Log Entries in the Console. See {@link com.gurock.smartinspect.Session#addCheckpoint}
	 * for a method with a similar intention.
	 *
	 * @param level The log level of this method call
	 * @see com.gurock.smartinspect.Session#addCheckpoint
	 */
	public void logSeparator(Level level) {
		if (isOn(level)) {
			sendLogEntry(level, null, LogEntryType.Separator, ViewerId.None);
		}
	}

	/**
	 * Resets the call stack by using the default log level.
	 *
	 * <p>This method instructs the Console to reset the call stack generated by the EnterMethod
	 * and LeaveMethod methods. It is especially useful if you want to reset the indentation in the
	 * method hierarchy without clearing all Log Entries.
	 *
	 * <p>This method uses the default level of the session's parent as log level. For more
	 * information, please refer to the documentation of the setDefaultLevel method of the
	 * SmartInspect class.
	 */
	public void resetCallstack() {
		resetCallstack(this.fParent.getDefaultLevel());
	}

	/**
	 * Overloaded. Resets the call stack by using a custom log level.
	 * This method instructs the Console to reset the call stack generated by the EnterMethod and LeaveMethod methods.
	 * It is especially useful if you want to reset the indentation in the method hierarchy without clearing all Log Entries.
	 *
	 * @param level The log level of this method call
	 */
	public void resetCallstack(Level level) {
		if (isOn(level)) {
			sendLogEntry(level, null, LogEntryType.ResetCallstack,
					ViewerId.None);
		}
	}

	/**
	 * Overloaded. Enters a method by using the default log level.
	 * The enterMethod method notifies the Console that a new method
	 * has been entered. The Console includes the method in the method
	 * hierarchy. If this method is used consequently, a full call stack
	 * is visible in the Console which helps to locate bugs in the source
	 * code. Please see the leaveMethod method as the counter piece to
	 * enterMethod.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param methodName The name of the method
	 */
	public void enterMethod(String methodName) {
		enterMethod(this.fParent.getDefaultLevel(), methodName);
	}

	/**
	 * Overloaded. Enters a method by using a custom log level.
	 * The enterMethod method notifies the Console that a new method
	 * has been entered. The Console includes the method in the method
	 * hierarchy. If this method is used consequently, a full call stack
	 * is visible in the Console which helps to locate bugs in the source
	 * code. Please see the leaveMethod method as the counter piece to
	 * enterMethod.
	 *
	 * @param level      The log level of this method call
	 * @param methodName The name of the method
	 */
	public void enterMethod(Level level, String methodName) {
		if (isOn(level)) {
			// Send two packets.
			sendLogEntry(level, methodName, LogEntryType.EnterMethod,
					ViewerId.Title);
			sendProcessFlow(level, methodName, ProcessFlowType.EnterMethod);
		}
	}

	/**
	 * Overloaded. Enters a method by using the default log level.
	 * The method name consists of a format string and the related
	 * array of arguments.
	 * <p>
	 * The enterMethod method notifies the Console that a new method
	 * has been entered. The Console includes the method in the method
	 * hierarchy. If this method is used consequently, a full call stack
	 * is visible in the Console which helps to locate bugs in the source
	 * code. Please see the leaveMethod method as the counter piece to
	 * enterMethod.
	 * <p>
	 * The resulting method name consists of a format string and the
	 * related array of arguments.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param methodNameFmt The format string to create the name of the method
	 * @param args          The array of arguments for the format string
	 */
	public void enterMethod(String methodNameFmt, Object[] args) {
		enterMethod(this.fParent.getDefaultLevel(), methodNameFmt, args);
	}

	/**
	 * Overloaded. Enters a method by using a custom log level.
	 * The method name consists of a format string and the related array of arguments.
	 * <p>
	 * The enterMethod method notifies the Console that a new method has been entered.
	 * The Console includes the method in the method hierarchy. If this method is used consequently,
	 * a full call stack is visible in the Console which helps to locate bugs in the source
	 * code. Please see the leaveMethod method as the counter piece to enterMethod.
	 * <p>
	 * The resulting method name consists of a format string and the related array of arguments.
	 *
	 * @param level         The log level of this method call
	 * @param methodNameFmt The format string to create the name of the method
	 * @param args          The array of arguments for the format string
	 */
	public void enterMethod(Level level, String methodNameFmt,
							Object[] args) {
		if (isOn(level)) {
			try {
				enterMethod(level, MessageFormat.format(methodNameFmt, args));
			} catch (Exception e) {
				// The MessageFormat.format method raised an exception.
				logInternalError("enterMethod: " + e.getMessage());
			}
		}
	}

	/**
	 * An overloaded method that enters a method using the default log level.
	 * The resulting method name consists of the class name of the supplied
	 * instance parameter, followed by a dot and the supplied methodName
	 * argument.
	 *
	 * <p>The enterMethod method notifies the Console that a new method
	 * has been entered. The Console includes the method in the method
	 * hierarchy. If this method is used consequently, a full call stack
	 * is visible in the Console which helps to locate bugs in the source
	 * code. Please see the leaveMethod method as the counter piece to
	 * enterMethod.
	 *
	 * <p>The resulting method name consists of the class name of the
	 * supplied instance parameter, followed by a dot and the supplied
	 * methodName argument.
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param methodName The name of the method
	 * @param instance   The class name of this instance and a dot will be prepended
	 *                   to the method name
	 */
	public void enterMethod(Object instance, String methodName) {
		enterMethod(this.fParent.getDefaultLevel(), instance, methodName);
	}

	/**
	 * Overloaded. Enters a method by using a custom log level. The
	 * resulting method name consists of the class name of the supplied
	 * instance parameter, followed by a dot and the supplied methodName
	 * argument.
	 *
	 * <p>
	 * The enterMethod method notifies the Console that a new method
	 * has been entered. The Console includes the method in the method
	 * hierarchy. If this method is used consequently, a full call stack
	 * is visible in the Console which helps to locate bugs in the source
	 * code. Please see the leaveMethod method as the counter piece to
	 * enterMethod.
	 *
	 * <p>
	 * The resulting method name consists of the class name of the
	 * supplied instance parameter, followed by a dot and the supplied
	 * methodName argument.
	 *
	 * @param level      The log level of this method call
	 * @param methodName The name of the method
	 * @param instance   The class name of this instance and a dot will be prepended to the method name
	 */
	public void enterMethod(Level level, Object instance, String methodName) {
		if (isOn(level)) {
			if (instance == null) {
				// The supplied instance is null.
				logInternalError("enterMethod: instance argument is null");
			} else {
				String cls = instance.getClass().getName();
				enterMethod(level, cls + "." + methodName);
			}
		}
	}

	/**
	 * Overloaded. Enters a method by using the default log level. The
	 * resulting method name consists of the class name of the supplied
	 * instance parameter, followed by a dot and the supplied format
	 * string and its related array of arguments.
	 *
	 * <p>The enterMethod method notifies the Console that a new method
	 * has been entered. The Console includes the method in the method
	 * hierarchy. If this method is used consequently, a full call stack
	 * is visible in the Console which helps to locate bugs in the source
	 * code. Please see the leaveMethod method as the counter piece to
	 * enterMethod.
	 *
	 * <p>The resulting method name consists of the class name of the
	 * supplied instance parameter, followed by a dot and the supplied
	 * format string and its related array of arguments.
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param instance      The class name of this instance and a dot will be prepended to the method name
	 * @param methodNameFmt The format string to create the name of the method
	 * @param args          The array of arguments for the format string
	 */
	public void enterMethod(Object instance, String methodNameFmt,
							Object[] args) {
		enterMethod(this.fParent.getDefaultLevel(), instance, methodNameFmt,
				args);
	}

	/**
	 * Overloaded. Enters a method by using a custom log level. The
	 * resulting method name consists of the class name of the supplied
	 * instance parameter, followed by a dot and the supplied format
	 * string and its related array of arguments.
	 *
	 * <p>The enterMethod method notifies the Console that a new method
	 * has been entered. The Console includes the method in the method
	 * hierarchy. If this method is used consequently, a full call stack
	 * is visible in the Console which helps to locate bugs in the source
	 * code. Please see the leaveMethod method as the counter piece to
	 * enterMethod.
	 *
	 * <p>The resulting method name consists of the class name of the
	 * supplied instance parameter, followed by a dot and the supplied
	 * format string and its related array of arguments.
	 *
	 * @param level         The log level of this method call
	 * @param instance      The class name of this instance and a dot will be prepended
	 *                      to the method name
	 * @param methodNameFmt The format string to create the name of the method
	 * @param args          The array of arguments for the format string
	 */
	public void enterMethod(Level level, Object instance,
							String methodNameFmt, Object[] args) {
		if (isOn(level)) {
			if (instance == null) {
				// The supplied instance is null.
				logInternalError("enterMethod: instance argument is null");
			} else {
				try {
					enterMethod(
							level,
							instance.getClass().getName() + "." +
									MessageFormat.format(methodNameFmt, args)
					);
				} catch (Exception e) {
					// The MessageFormat.format method raised an exception.
					logInternalError("enterMethod: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Overloaded. Leaves a method by using the default log level.
	 * <p>
	 * The leaveMethod method notifies the Console that a method has
	 * been left. The Console closes the current method in the method
	 * hierarchy. If this method is used consequently, a full call stack
	 * is visible in the Console which helps to locate bugs in the source
	 * code. Please see the enterMethod method as the counter piece to
	 * leaveMethod.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param methodName The name of the method
	 */
	public void leaveMethod(String methodName) {
		leaveMethod(this.fParent.getDefaultLevel(), methodName);
	}

	/**
	 * Overloaded. Leaves a method by using a custom log level.
	 * <p>
	 * The leaveMethod method notifies the Console that a method has
	 * been left. The Console closes the current method in the method
	 * hierarchy. If this method is used consequently, a full call stack
	 * is visible in the Console which helps to locate bugs in the source
	 * code. Please see the enterMethod method as the counter piece to
	 * leaveMethod.
	 *
	 * @param level      The log level of this method call
	 * @param methodName The name of the method
	 */
	public void leaveMethod(Level level, String methodName) {
		if (isOn(level)) {
			// Send two packets.
			sendLogEntry(level, methodName, LogEntryType.LeaveMethod,
					ViewerId.Title);
			sendProcessFlow(level, methodName, ProcessFlowType.LeaveMethod);
		}
	}

	/**
	 * Overloaded. Leaves a method by using the default log level. The
	 * method name consists of a format string and its related array of
	 * arguments.
	 * <p>
	 * The leaveMethod method notifies the Console that a method has
	 * been left. The Console closes the current method in the method
	 * hierarchy. If this method is used consequently, a full call stack
	 * is visible in the Console which helps to locate bugs in the source
	 * code. Please see the enterMethod method as the counter piece to
	 * leaveMethod.
	 * <p>
	 * The resulting method name consists of a format string and the
	 * related array of arguments.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param methodNameFmt The format string to create the name of the method
	 * @param args          The array of arguments for the format string
	 */
	public void leaveMethod(String methodNameFmt, Object[] args) {
		leaveMethod(this.fParent.getDefaultLevel(), methodNameFmt, args);
	}

	/**
	 * Overloaded. Leaves a method by using a custom log level. The
	 * method name consists of a format string and its related array of
	 * arguments.
	 * <p>
	 * The leaveMethod method notifies the Console that a method has
	 * been left. The Console closes the current method in the method
	 * hierarchy. If this method is used consequently, a full call stack
	 * is visible in the Console which helps to locate bugs in the source
	 * code. Please see the enterMethod method as the counterpart to
	 * leaveMethod.
	 * <p>
	 * The resulting method name consists of a format string and the
	 * related array of arguments.
	 *
	 * @param level         The log level of this method call
	 * @param methodNameFmt The format string to create the name of the method
	 * @param args          The array of arguments for the format string
	 */

	public void leaveMethod(Level level, String methodNameFmt,
							Object[] args) {
		if (isOn(level)) {
			try {
				leaveMethod(level, MessageFormat.format(methodNameFmt, args));
			} catch (Exception e) {
				// The MessageFormat.format method raised an exception.
				logInternalError("leaveMethod: " + e.getMessage());
			}
		}
	}

	/**
	 * Overloaded. Leaves a method by using the default log level. The
	 * resulting method name consists of the class name of the supplied
	 * instance parameter, followed by a dot and the supplied methodName
	 * argument.
	 *
	 * <p>The leaveMethod method notifies the Console that a method has
	 * been left. The Console closes the current method in the method
	 * hierarchy. If this method is used consequently, a full call stack
	 * is visible in the Console which helps to locate bugs in the source
	 * code. Please see the enterMethod method as the counter piece to
	 * leaveMethod.
	 *
	 * <p>The resulting method name consists of the class name of the
	 * supplied instance parameter, followed by a dot and the supplied
	 * methodName argument.
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class. </p>
	 *
	 * @param instance   The class name of this instance and a dot will be prepended
	 *                   to the method name
	 * @param methodName The name of the method
	 */

	public void leaveMethod(Object instance, String methodName) {
		leaveMethod(this.fParent.getDefaultLevel(), instance, methodName);
	}

	/**
	 * Overloaded. Leaves a method by using a custom log level. The
	 * resulting method name consists of the class name of the supplied
	 * instance parameter, followed by a dot and the supplied methodName
	 * argument.
	 *
	 * <p>The leaveMethod method notifies the Console that a method has
	 * been left. The Console closes the current method in the method
	 * hierarchy. If this method is used consequently, a full call stack
	 * is visible in the Console which helps to locate bugs in the source
	 * code. Please see the enterMethod method as the counter piece to
	 * leaveMethod.
	 * <p>
	 * The resulting method name consists of the class name of the
	 * supplied instance parameter, followed by a dot and the supplied
	 * methodName argument.</p>
	 *
	 * @param level      The log level of this method call
	 * @param instance   The class name of this instance and a dot will be prepended
	 *                   to the method name
	 * @param methodName The name of the method
	 */
	public void leaveMethod(Level level, Object instance, String methodName) {
		if (isOn(level)) {
			if (instance == null) {
				logInternalError("leaveMethod: instance argument is null");
			} else {
				String cls = instance.getClass().getName();
				leaveMethod(level, cls + "." + methodName);
			}
		}
	}

	/**
	 * Overloaded. Leaves a method by using the default log level. The resulting method name consists
	 * of the class name of the supplied instance parameter, followed by a dot and the supplied format
	 * string and its related array of arguments.
	 *
	 * <p>
	 * The leaveMethod method notifies the Console that a method has been left. The Console closes
	 * the current method in the method hierarchy. If this method is used consequently, a full call stack
	 * is visible in the Console which helps to locate bugs in the source code. Please see the enterMethod
	 * method as the counter piece to leaveMethod.
	 * <p>
	 * The resulting method name consists of the class name of the supplied instance parameter,
	 * followed by a dot and the supplied format string and its related array of arguments.
	 * <p>
	 * This method uses the default level of the session's parent as log level. For more information,
	 * please refer to the documentation of the setDefaultLevel method of the SmartInspect class.
	 * </p>
	 *
	 * @param instance      The class name of this instance and a dot will be prepended to the method name
	 * @param methodNameFmt The format string to create the name of the method
	 * @param args          The array of arguments for the format string
	 */
	public void leaveMethod(Object instance, String methodNameFmt,
							Object[] args) {
		leaveMethod(this.fParent.getDefaultLevel(), instance, methodNameFmt,
				args);
	}

	/**
	 * Overloaded. Leaves a method by using a custom log level. The
	 * resulting method name consists of the class name of the supplied
	 * instance parameter, followed by a dot and the supplied format
	 * string and its related array of arguments.
	 *
	 * <p>The leaveMethod method notifies the Console that a method has
	 * been left. The Console closes the current method in the method
	 * hierarchy. If this method is used consequently, a full call stack
	 * is visible in the Console which helps to locate bugs in the source
	 * code. Please see the enterMethod method as the counter piece to
	 * leaveMethod.
	 * <p>
	 * The resulting method name consists of the class name of the
	 * supplied instance parameter, followed by a dot and the supplied
	 * format string and its related array of arguments.
	 *
	 * @param level         The log level of this method call
	 * @param instance      The class name of this instance and a dot will be prepended
	 *                      to the method name
	 * @param methodNameFmt The format string to create the name of the method
	 * @param args          The array of arguments for the format string
	 */
	public void leaveMethod(Level level, Object instance,
							String methodNameFmt, Object[] args) {
		if (isOn(level)) {
			if (instance == null) {
				logInternalError("leaveMethod: instance argument is null");
			} else {
				try {
					leaveMethod(
							level,
							instance.getClass().getName() + "." +
									MessageFormat.format(methodNameFmt, args)
					);
				} catch (Exception e) {
					// The MessageFormat.format method raised an exception.
					logInternalError("leaveMethod: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Overloaded method. Enters a new thread by using the default log level.
	 * <p>
	 * The enterThread method notifies the Console that a new thread
	 * has been entered. The Console display this thread in the Process
	 * Flow toolbox. If this method is used consequently, all threads
	 * of a process are displayed. Please see the leaveThread method as
	 * the counter piece to enterThread.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param threadName The name of the thread
	 */
	public void enterThread(String threadName) {
		enterThread(this.fParent.getDefaultLevel(), threadName);
	}

	/**
	 * Overloaded. Enters a new thread by using a custom log level.
	 * <p>
	 * The enterThread method notifies the Console that a new thread
	 * has been entered. The Console display this thread in the Process
	 * Flow toolbox. If this method is used consequently, all threads
	 * of a process are displayed. Please see the leaveThread method as
	 * the counter piece to enterThread.
	 *
	 * @param level      The log level of this method call
	 * @param threadName The name of the thread
	 */
	public void enterThread(Level level, String threadName) {
		if (isOn(level)) {
			sendProcessFlow(level, threadName, ProcessFlowType.EnterThread);
		}
	}

	/**
	 * Overloaded. Enters a new thread by using the default log level.
	 * The thread name consists of a format string the related array of
	 * arguments.
	 *
	 * <p>The enterThread method notifies the Console that a new thread
	 * has been entered. The Console display this thread in the Process
	 * Flow toolbox. If this method is used consequently, all threads
	 * of a process are displayed. Please see the leaveThread method as
	 * the counter piece to enterThread.</p>
	 *
	 * <p>The resulting thread name consists of a format string and the
	 * related array of arguments.</p>
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.</p>
	 *
	 * @param threadNameFmt The format string to create the name of the thread
	 * @param args          The array of arguments for the format string
	 */
	public void enterThread(String threadNameFmt, Object[] args) {
		enterThread(this.fParent.getDefaultLevel(), threadNameFmt, args);
	}

	/**
	 * Overloaded. Enters a new thread by using a custom log level.
	 * The thread name consists of a format string the related array of
	 * arguments.
	 * <p>
	 * The enterThread method notifies the Console that a new thread
	 * has been entered. The Console display this thread in the Process
	 * Flow toolbox. If this method is used consequently, all threads
	 * of a process are displayed. Please see the leaveThread method as
	 * the counter piece to enterThread.
	 * <p>
	 * The resulting thread name consists of a format string and the
	 * related array of arguments.
	 *
	 * @param level         The log level of this method call
	 * @param threadNameFmt The format string to create the name of the thread
	 * @param args          The array of arguments for the format string
	 */
	public void enterThread(Level level, String threadNameFmt,
							Object[] args) {
		if (isOn(level)) {
			try {
				enterThread(level, MessageFormat.format(threadNameFmt, args));
			} catch (Exception e) {
				// The MessageFormat.format method raised an exception.
				logInternalError("enterThread: " + e.getMessage());
			}
		}
	}

	/**
	 * Overloaded. Leaves a thread by using the default log level.
	 *
	 * <p>The leaveThread method notifies the Console that a thread has
	 * been finished. The Console display this change in the Process
	 * Flow toolbox. Please see the enterThread method as the counter
	 * piece to leaveThread.</p>
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.</p>
	 *
	 * @param threadName The name of the thread
	 */
	public void leaveThread(String threadName) {
		leaveThread(this.fParent.getDefaultLevel(), threadName);
	}

	/**
	 * Overloaded. Leaves a thread by using a custom log level.
	 * The leaveThread method notifies the Console that a thread has been finished. The Console display
	 * this change in the Process Flow toolbox. Please see the enterThread method as the counter
	 * piece to leaveThread.
	 *
	 * @param level      The log level of this method call
	 * @param threadName The name of the thread
	 */
	public void leaveThread(Level level, String threadName) {
		if (isOn(level)) {
			sendProcessFlow(level, threadName, ProcessFlowType.LeaveThread);
		}
	}

	/**
	 * Overloaded method. Leaves a thread using the default log level.
	 * The thread name consists of a format string and the related array of arguments.
	 * <p>
	 * The leaveThread method notifies the Console that a thread has been finished.
	 * The Console displays this change in the Process Flow toolbox.
	 * Please see the enterThread method as the counterpart to leaveThread.
	 * <p>
	 * The resulting thread name consists of a format string and the related array of arguments.
	 * <p>
	 * This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param threadNameFmt The format string to create the name of the thread
	 * @param args          The array of arguments for the format string
	 */
	public void leaveThread(String threadNameFmt, Object[] args) {
		leaveThread(this.fParent.getDefaultLevel(), threadNameFmt, args);
	}

	/**
	 * Leaves a thread by using a custom log level.
	 * The thread name consists of a format string and the related
	 * array of arguments.
	 * <p>
	 * The leaveThread method notifies the Console that a thread has
	 * been finished. The Console display this change in the Process
	 * Flow toolbox. Please see the enterThread method as the counter
	 * piece to leaveThread.
	 * <p>
	 * The resulting thread name consists of a format string and the
	 * related array of arguments.
	 *
	 * @param level         The log level of this method call
	 * @param threadNameFmt The format string to create the name of the thread
	 * @param args          The array of arguments for the format string
	 */
	public void leaveThread(Level level, String threadNameFmt, Object[] args) {
		if (isOn(level)) {
			try {
				leaveThread(level, MessageFormat.format(threadNameFmt, args));
			} catch (Exception e) {
				// The MessageFormat.format method raised an exception.
				logInternalError("leaveThread: " + e.getMessage());
			}
		}
	}

	/**
	 * Overloaded. Enters a new process by using the default log level
	 * and the parent's application name as process name.
	 * <p>
	 * The enterProcess method notifies the Console that a new
	 * process has been entered. The Console displays this process
	 * in the Process Flow toolbox. Please see the leaveProcess
	 * method as the counter piece to enterProcess.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method
	 * of the SmartInspect class.
	 */
	public void enterProcess() {
		enterProcess(this.fParent.getDefaultLevel());
	}

	/**
	 * Overloaded. Enters a new process by using a custom log level
	 * and the parent's application name as process name.
	 * The enterProcess method notifies the Console that a new
	 * process has been entered. The Console displays this process
	 * in the Process Flow toolbox. Please see the leaveProcess
	 * method as the counter piece to enterProcess.
	 *
	 * @param level The log level of this method call
	 */
	public void enterProcess(Level level) {
		if (isOn(level)) {
			sendProcessFlow(level, this.fParent.getAppName(),
					ProcessFlowType.EnterProcess);
			sendProcessFlow(level, "Main Thread", ProcessFlowType.EnterThread);
		}
	}

	/**
	 * Overloaded. Enters a new process by using the default log level.
	 *
	 * <p>The enterProcess method notifies the Console that a new
	 * process has been entered. The Console displays this process
	 * in the Process Flow toolbox. Please see the leaveProcess
	 * method as the counter piece to enterProcess.
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param processName The name of the process
	 */
	public void enterProcess(String processName) {
		enterProcess(this.fParent.getDefaultLevel(), processName);
	}

	/**
	 * Enters a new process by using a custom log level. The enterProcess method notifies
	 * the Console that a new process has been entered. The Console displays this process
	 * in the Process Flow toolbox. Please see the leaveProcess
	 * method as the counter piece to enterProcess.
	 *
	 * @param processName The name of the process
	 */
	public void enterProcess(Level level, String processName) {
		if (isOn(level)) {
			sendProcessFlow(level, processName, ProcessFlowType.EnterProcess);
			sendProcessFlow(level, "Main Thread", ProcessFlowType.EnterThread);
		}
	}

	/**
	 * Overloaded. Enters a new process by using the default log level.
	 * The process name consists of a format string and the related
	 * array of arguments.
	 * <p>
	 * The enterProcess method notifies the Console that a new
	 * process has been entered. The Console displays this process
	 * in the Process Flow toolbox. Please see the leaveProcess
	 * method as the counter piece to enterProcess.
	 * <p>
	 * The resulting process name consists of a format string and
	 * the related array of arguments.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param processNameFmt The format string to create the name of the process
	 * @param args           The array of arguments for the format string
	 */
	public void enterProcess(String processNameFmt, Object[] args) {
		enterProcess(this.fParent.getDefaultLevel(), processNameFmt, args);
	}

	/**
	 * Overloaded. Enters a new process by using a custom log level. The process name consists of a format string and the related array of arguments.
	 * The enterProcess method notifies the Console that a new process has been entered. The Console displays this process
	 * in the Process Flow toolbox. Please see the leaveProcess method as the counter piece to enterProcess.
	 * The resulting process name consists of a format string and the related array of arguments.
	 *
	 * @param level          The log level of this method call
	 * @param processNameFmt The format string to create the name of the process
	 * @param args           The array of arguments for the format string
	 */
	public void enterProcess(Level level, String processNameFmt,
							 Object[] args) {
		if (isOn(level)) {
			try {
				enterProcess(level, MessageFormat.format(processNameFmt, args));
			} catch (Exception e) {
				// The MessageFormat.format method raised an exception.
				logInternalError("enterProcess: " + e.getMessage());
			}
		}
	}

	/**
	 * Overloaded. Leaves a process by using the default log level
	 * and the parent's application name as process name.
	 *
	 * <p>The leaveProcess method notifies the Console that a process
	 * has finished. The Console displays this change in the Process
	 * Flow toolbox. Please see the enterProcess method as the
	 * counter piece to leaveProcess.
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 */
	public void leaveProcess() {
		leaveProcess(this.fParent.getDefaultLevel());
	}

	/**
	 * Overloaded. Leaves a process by using a custom log level
	 * and the parent's application name as process name.
	 * The leaveProcess method notifies the Console that a process
	 * has finished. The Console displays this change in the Process
	 * Flow toolbox. Please see the enterProcess method as the
	 * counter piece to leaveProcess.
	 *
	 * @param level The log level of this method call
	 */
	public void leaveProcess(Level level) {
		if (isOn(level)) {
			sendProcessFlow(level, "Main Thread", ProcessFlowType.LeaveThread);
			sendProcessFlow(level, this.fParent.getAppName(),
					ProcessFlowType.LeaveProcess);
		}
	}

	/**
	 * Overloaded. Leaves a process by using the default log level.
	 * <p>
	 * The leaveProcess method notifies the Console that a process
	 * has finished. The Console displays this change in the Process
	 * Flow toolbox. Please see the enterProcess method as the
	 * counter piece to leaveProcess.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param processName The name of the process
	 */
	public void leaveProcess(String processName) {
		leaveProcess(this.fParent.getDefaultLevel(), processName);
	}

	/**
	 * Overloaded. Leaves a process by using a custom log level.
	 * The leaveProcess method notifies the Console that a process
	 * has finished. The Console displays this change in the Process
	 * Flow toolbox. Please see the enterProcess method as the
	 * counter piece to leaveProcess.
	 *
	 * @param level       The log level of this method call
	 * @param processName The name of the process
	 */
	public void leaveProcess(Level level, String processName) {
		if (isOn(level)) {
			sendProcessFlow(level, "Main Thread", ProcessFlowType.LeaveThread);
			sendProcessFlow(level, processName, ProcessFlowType.LeaveProcess);
		}
	}

	/**
	 * Overloaded. Leaves a process by using the default log level.
	 * The process name consists of a format string and the related
	 * array of arguments.
	 * <p>
	 * The leaveProcess method notifies the Console that a process
	 * has finished. The Console displays this change in the Process
	 * Flow toolbox. Please see the enterProcess method as the
	 * counter piece to leaveProcess.
	 * <p>
	 * The resulting process name consists of a format string and
	 * the related array of arguments.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param processNameFmt The format string to create the name of the process
	 * @param args           The array of arguments for the format string
	 */
	public void leaveProcess(String processNameFmt, Object[] args) {
		leaveProcess(this.fParent.getDefaultLevel(), processNameFmt, args);
	}

	/**
	 * Overloaded. Leaves a process by using a custom log level.
	 * The process name consists of a format string and the related
	 * array of arguments.
	 * <p>
	 * The leaveProcess method notifies the Console that a process
	 * has finished. The Console displays this change in the Process
	 * Flow toolbox. Please see the enterProcess method as the
	 * counter piece to leaveProcess.
	 * <p>
	 * The resulting process name consists of a format string and
	 * the related array of arguments.
	 *
	 * @param level          The log level of this method call
	 * @param processNameFmt The format string to create the name of the process
	 * @param args           The array of arguments for the format string
	 */
	public void leaveProcess(Level level, String processNameFmt,
							 Object[] args) {
		if (isOn(level)) {
			try {
				leaveProcess(level, MessageFormat.format(processNameFmt, args));
			} catch (Exception e) {
				// The MessageFormat.format method raised an exception.
				logInternalError("leaveProcess: " + e.getMessage());
			}
		}
	}

	/**
	 * Overloaded. Logs a colored message with the default log level.
	 * <p>
	 * This method uses the {@link SmartInspect#setDefaultLevel}
	 * of the session's {@link Session#getParent()} as log
	 * level. For more information, please refer to the documentation of
	 * the {@link SmartInspect#setDefaultLevel} method
	 * of the SmartInspect class.
	 *
	 * @param color The background color in the Console
	 * @param title The message to log
	 */
	public void logColored(Color color, String title) {
		logColored(this.fParent.getDefaultLevel(), color, title);
	}

	/**
	 * Overloaded. Logs a colored message with a custom log level.
	 *
	 * @param level The log level of this method call
	 * @param color The background color in the Console
	 * @param title The message to log
	 */
	public void logColored(Level level, Color color, String title) {
		if (isOn(level)) {
			sendLogEntry(level, title, LogEntryType.Message, ViewerId.Title,
					color, null);
		}
	}

	/**
	 * Logs a colored message with the default log level.
	 * The message is created with a format string and a related array
	 * of arguments.
	 * <p>
	 * This version of the method accepts a format string and a
	 * related array of arguments. These parameters will be passed
	 * to the MessageFormat.format() method and the resulting string
	 * will be the message.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param color    The background color in the Console
	 * @param titleFmt A format string to create the message
	 * @param args     The array of arguments for the format string
	 */
	public void logColored(Color color, String titleFmt, Object[] args) {
		logColored(this.fParent.getDefaultLevel(), color, titleFmt, args);
	}

	/**
	 * Overloaded method that logs a colored message with a custom log level.
	 * The message is created with a format string and a related array
	 * of arguments.
	 *
	 * <p>This version of the method accepts a format string and a
	 * related array of arguments. These parameters will be passed
	 * to the MessageFormat.format method and the resulting string
	 * will be the message.</p>
	 *
	 * @param level    The log level of this method call
	 * @param color    The background color in the Console
	 * @param titleFmt A format string to create the message
	 * @param args     The array of arguments for the format string
	 */
	public void logColored(Level level, Color color, String titleFmt,
						   Object[] args) {
		if (isOn(level)) {
			try {
				logColored(level, color,
						MessageFormat.format(titleFmt, args));
			} catch (Exception e) {
				// The MessageFormat.format method raised an exception.
				logInternalError("logColored: " + e.getMessage());
			}
		}
	}

	/**
	 * Overloaded. Logs a debug message with a log level of Level.Debug.
	 *
	 * @param title The message to log.
	 */
	public void logDebug(String title) {
		if (isOn(Level.Debug)) {
			sendLogEntry(Level.Debug, title, LogEntryType.Debug,
					ViewerId.Title);
		}
	}

	/**
	 * Overloaded. Logs a debug message with a log level of
	 * Level.Debug. The message is created with a format string
	 * and a related array of arguments.
	 * This version of the method accepts a format string and a
	 * related array of arguments. These parameters will be passed
	 * to the MessageFormat.format() method and the resulting string
	 * will be the message.
	 *
	 * @param titleFmt A format string to create the message
	 * @param args     The array of arguments for the format string
	 */
	public void logDebug(String titleFmt, Object[] args) {
		if (isOn(Level.Debug)) {
			try {
				logDebug(MessageFormat.format(titleFmt, args));
			} catch (Exception e) {
				// The MessageFormat.format method raised an exception.
				logInternalError("logDebug: " + e.getMessage());
			}
		}
	}

	/**
	 * Overloaded method. This method is responsible for logging a verbose message
	 * with a log level of Level.Verbose.
	 *
	 * @param title The message to log
	 */
	public void logVerbose(String title) {
		if (isOn(Level.Verbose)) {
			sendLogEntry(Level.Verbose, title, LogEntryType.Verbose,
					ViewerId.Title);
		}
	}

	/**
	 * Overloaded. Logs a verbose message with a log level of Level.Verbose.
	 * The message is created with a format string and a related array of arguments.
	 * This version of the method accepts a format string and a related array of arguments.
	 * These parameters will be passed to the MessageFormat.format() method and
	 * the resulting string will be the message.
	 *
	 * @param titleFmt A format string to create the message
	 * @param args     The array of arguments for the format string
	 */
	public void logVerbose(String titleFmt, Object[] args) {
		if (isOn(Level.Verbose)) {
			try {
				logVerbose(MessageFormat.format(titleFmt, args));
			} catch (Exception e) {
				// The MessageFormat.format method raised an exception.
				logInternalError("logVerbose: " + e.getMessage());
			}
		}
	}

	/**
	 * Overloaded. Logs a simple message with a log level of Level.Message.
	 *
	 * @param title The message to log
	 */
	public void logMessage(String title) {
		if (isOn(Level.Message)) {
			sendLogEntry(Level.Message, title, LogEntryType.Message,
					ViewerId.Title);
		}
	}

	/**
	 * Overloaded. Logs a simple message with a log level of
	 * Level.Message. The message is created with a format string
	 * and a related array of arguments.
	 * This version of the method accepts a format string and a
	 * related array of arguments. These parameters will be passed
	 * to the MessageFormat.format() method and the resulting string
	 * will be the message.
	 *
	 * @param titleFmt A format string to create the message
	 * @param args     The array of arguments for the format string
	 */
	public void logMessage(String titleFmt, Object[] args) {
		if (isOn(Level.Message)) {
			try {
				logMessage(MessageFormat.format(titleFmt, args));
			} catch (Exception e) {
				// The MessageFormat.format method raised an exception.
				logInternalError("logMessage: " + e.getMessage());
			}
		}
	}

	/**
	 * Overloaded. Logs a warning message with a log level of Level.Warning.
	 *
	 * @param title The warning to log
	 */
	public void logWarning(String title) {
		if (isOn(Level.Warning)) {
			sendLogEntry(Level.Warning, title, LogEntryType.Warning,
					ViewerId.Title);
		}
	}

	/**
	 * Overloaded. Logs a warning message with a log level of
	 * Level.Warning. The warning message is created with a format
	 * string and a related array of arguments.
	 * <p>
	 * This version of the method accepts a format string and a
	 * related array of arguments. These parameters will be passed
	 * to the MessageFormat.format() method and the resulting string
	 * will be the warning message.
	 *
	 * @param titleFmt A format string to create the warning
	 * @param args     The array of arguments for the format string
	 */
	public void logWarning(String titleFmt, Object[] args) {
		if (isOn(Level.Warning)) {
			try {
				logWarning(MessageFormat.format(titleFmt, args));
			} catch (Exception e) {
				// The MessageFormat.format method raised an exception.
				logInternalError("logWarning: " + e.getMessage());
			}
		}
	}

	/**
	 * Overloaded. Logs an error message with a log level of Level.Error.
	 * This method is ideally used in error handling code such as exception handlers.
	 * If this method is used consequently, it is easy to troubleshoot and solve bugs
	 * in applications or configurations. See logException for a similar method.
	 *
	 * @param title A string which describes the error
	 */
	public void logError(String title) {
		if (isOn(Level.Error)) {
			sendLogEntry(Level.Error, title, LogEntryType.Error,
					ViewerId.Title);
		}
	}

	/**
	 * Logs an error message with a log level of Level.Error. The error message is
	 * created with a format string and a related array of arguments. This version of
	 * the method accepts a format string and a related array of arguments. These
	 * parameters will be passed to the MessageFormat.format() method and the resulting
	 * string will be the error message. This method is ideally used in error handling
	 * code such as exception handlers. If this method is used consequently, it is
	 * easy to troubleshoot and solve bugs in applications or configurations. See
	 * logException for a similar method.
	 *
	 * @param titleFmt A format string to create a description of the error
	 * @param args     The array of arguments for the format string
	 */
	public void logError(String titleFmt, Object[] args) {
		if (isOn(Level.Error)) {
			try {
				logError(MessageFormat.format(titleFmt, args));
			} catch (Exception e) {
				// The MessageFormat.format method raised an exception.
				logInternalError("logError: " + e.getMessage());
			}
		}
	}

	/**
	 * Overloaded method to log a fatal error message with a log level of Level.Fatal.
	 * <p>
	 * This method is ideally used in error handling code such as exception handlers.
	 * If this method is used consequently, it is easy to troubleshoot and solve bugs
	 * in applications or configurations. See logError for a method which
	 * does not describe fatal but recoverable errors.
	 *
	 * @param title A string which describes the fatal error
	 */
	public void logFatal(String title) {
		if (isOn(Level.Fatal)) {
			sendLogEntry(Level.Fatal, title, LogEntryType.Fatal,
					ViewerId.Title);
		}
	}

	/**
	 * Overloaded. Logs a fatal error message with a log level of Level.Fatal. The error message
	 * is created with a format string and a related array of arguments.
	 * <p>
	 * This version of the method accepts a format string and a related array of arguments.
	 * These parameters will be passed to the MessageFormat.format method and the resulting string
	 * will be the fatal error message.
	 * <p>
	 * This method is ideally used in error handling code such as exception handlers. If this method
	 * is used consequently, it is easy to troubleshoot and solve bugs in applications or
	 * configurations. See logError for a method which does not describe fatal but recoverable errors.
	 *
	 * @param titleFmt A format string to create a description of the fatal error
	 * @param args     The array of arguments for the format string
	 */
	public void logFatal(String titleFmt, Object[] args) {
		if (isOn(Level.Fatal)) {
			try {
				logFatal(MessageFormat.format(titleFmt, args));
			} catch (Exception e) {
				// The MessageFormat.format method raised an exception.
				logInternalError("logFatal: " + e.getMessage());
			}
		}
	}

	/**
	 * Logs an internal error with a log level of Level.Error.
	 * This method logs an internal error. Such errors can occur
	 * if session methods are invoked with invalid arguments. For
	 * example, if you pass an invalid format string to logMessage,
	 * the exception will be caught and an internal error with the
	 * exception message will be sent.
	 * <p>
	 * This method is also intended to be used in derived classes
	 * to report any errors in your own methods.
	 *
	 * @param title A string which describes the internal error
	 */
	protected void logInternalError(String title) {
		if (isOn(Level.Error)) {
			sendLogEntry(Level.Error, title, LogEntryType.InternalError,
					ViewerId.Title);
		}
	}

	/**
	 * Overloaded. Logs an internal error with a log level of Level.Error. The error message is created
	 * with a format string and a related array of arguments.
	 * <p>
	 * This method logs an internal error. Such errors can occur if session methods are invoked with
	 * invalid arguments. For example, if you pass an invalid format string to logMessage, the exception
	 * will be caught and an internal error with the exception message will be sent.
	 * <p>
	 * This version of the method accepts a format string and a related array of arguments. These
	 * parameters will be passed to the MessageFormat.format method and the resulting string will
	 * be the error message.
	 * <p>
	 * This method is also intended to be used in derived classes to report any errors in your own methods.
	 *
	 * @param titleFmt A format string to create a description of the internal error
	 * @param args     The array of arguments for the format string
	 */
	protected void logInternalError(String titleFmt,
									Object[] args) {
		if (isOn(Level.Error)) {
			try {
				logInternalError(MessageFormat.format(titleFmt, args));
			} catch (Exception e) {
				// The MessageFormat.format method raised an exception.
				logInternalError("logInternalError: " + e.getMessage());
			}
		}
	}

	/**
	 * Overloaded. Increments the default checkpoint counter and logs a message with the default log level.
	 * This method increments a checkpoint counter and then logs a message using "Checkpoint #N" as the title.
	 * The initial value of the checkpoint counter is 0. You can use the {@link com.gurock.smartinspect.Session#resetCheckpoint} method
	 * to reset the counter to 0 again.
	 * This method is useful, for example, for tracking loops. If addCheckpoint is called for each iteration of a loop, it is easy
	 * to follow the execution of the loop in question. This method can also be used in recursive methods to understand the execution flow.
	 * Furthermore, you can use it to highlight important parts of your code. See {@link com.gurock.smartinspect.Session#logSeparator} for a method with a similar intention.
	 * This method uses the SmartInspect.setDefaultLevel, default level of the session's parent as the log level. For more information,
	 * please refer to the documentation of the SmartInspect.setDefaultLevel, setDefaultLevel method of the SmartInspect class.
	 * Please refer to {@link com.gurock.smartinspect.Session#logSeparator}, {@link com.gurock.smartinspect.Session#resetCheckpoint}
	 */
	public void addCheckpoint() {
		addCheckpoint(this.fParent.getDefaultLevel());
	}

	/**
	 * Overloaded. Increments the default checkpoint counter and logs a message with a custom log level.
	 * <p>
	 * This method increments a checkpoint counter and then logs a message using "Checkpoint #N" as title.
	 * The initial value of the checkpoint counter is 0. You can use the {@link com.gurock.smartinspect.Session#resetCheckpoint}
	 * method to reset the counter to 0 again.
	 * <p>
	 * This method is useful, for example, for tracking loops. If addCheckpoint is called for each iteration of a
	 * loop, it is easy to follow the execution of the loop in question. This method can also be used in recursive
	 * methods to understand the execution flow. Furthermore, you can use it to highlight important parts of your
	 * code. See {@link com.gurock.smartinspect.Session#logSeparator} for a method with a similar intention.
	 *
	 * @param level The log level of this method call
	 * @see com.gurock.smartinspect.Session#logSeparator
	 */
	public void addCheckpoint(Level level) {
		if (isOn(level)) {
			int counter;

			synchronized (this.fCheckpointLock) {
				counter = ++this.fCheckpointCounter;
			}

			String title = "Checkpoint #" + counter;
			sendLogEntry(level, title, LogEntryType.Checkpoint,
					ViewerId.Title);
		}
	}

	/**
	 * Overloaded. Increments the counter of a named checkpoint and
	 * logs a message with the default log level.
	 * <p>
	 * This method increments the counter for the given checkpoint
	 * and then logs a message using "%checkpoint% #N" as title where
	 * %checkpoint% stands for the name of the checkpoint and N for
	 * the incremented counter value. The initial value of the counter
	 * for a given checkpoint is 0. You can use the resetCheckpoint
	 * method to reset the counter to 0 again.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name The name of the checkpoint to increment
	 */
	public void addCheckpoint(String name) {
		addCheckpoint(this.fParent.getDefaultLevel(), name, null);
	}

	/**
	 * Overloaded. Increments the counter of a named checkpoint and
	 * logs a message with a custom log level.
	 * <p>
	 * This method increments the counter for the given checkpoint
	 * and then logs a message using "%checkpoint% #N" as title where
	 * %checkpoint% stands for the name of the checkpoint and N for
	 * the incremented counter value. The initial value of the counter
	 * for a given checkpoint is 0. You can use the resetCheckpoint
	 * method to reset the counter to 0 again.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the checkpoint to increment
	 */
	public void addCheckpoint(Level level, String name) {
		addCheckpoint(level, name, null);
	}

	/**
	 * Increments the counter of a named checkpoint and
	 * logs a message with the default log level and an optional
	 * message. This method increments the counter for the given checkpoint
	 * and then logs a message using "%checkpoint% #N" as title where
	 * %checkpoint% stands for the name of the checkpoint and N for
	 * the incremented counter value. The initial value of the counter
	 * for a given checkpoint is 0. Specify the details parameter to
	 * include an optional message in the resulting log entry. You
	 * can use the resetCheckpoint method to reset the counter to 0
	 * again. This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name    The name of the checkpoint to increment
	 * @param details An optional message to include in the resulting log entry. Can be null
	 */
	public void addCheckpoint(String name, String details) {
		addCheckpoint(this.fParent.getDefaultLevel(), name, details);
	}

	/**
	 * Overloaded function. Increments the counter of a named checkpoint and logs a message with a custom log level and an optional
	 * message. This method increments the counter for the given checkpoint and then logs a message using "%checkpoint% #N" as title where
	 * %checkpoint% stands for the name of the checkpoint and N for the incremented counter value. The initial value of the counter
	 * for a given checkpoint is 0. Specify the details parameter to include an optional message in the resulting log entry. You
	 * can use the resetCheckpoint method to reset the counter to 0 again.
	 *
	 * @param level   The log level of this method call
	 * @param name    The name of the checkpoint to increment
	 * @param details An optional message to include in the resulting log entry. Can be null
	 */
	public void addCheckpoint(Level level, String name, String details) {
		if (isOn(level)) {
			if (name == null) {
				logInternalError("addCheckpoint: name argument is null");
				return;
			}

			int value;
			String key = name.toLowerCase();

			synchronized (this.fCheckpoints) {
				if (this.fCheckpoints.containsKey(key)) {
					Integer i = (Integer) this.fCheckpoints.get(key);
					value = i.intValue();
				} else {
					value = 0;
				}

				value++;
				this.fCheckpoints.put(key, new Integer(value));
			}

			StringBuffer sb = new StringBuffer();
			sb.append(name);
			sb.append(" #");
			sb.append(value);

			if (details != null) {
				sb.append(" (");
				sb.append(details);
				sb.append(")");
			}

			String title = sb.toString();
			sendLogEntry(level, title, LogEntryType.Checkpoint,
					ViewerId.Title);
		}
	}

	/**
	 * Overloaded. Resets the default checkpoint counter.
	 * This method resets the default checkpoint counter to 0. The
	 * checkpoint counter is used by the addCheckpoint method.
	 *
	 * @see com.gurock.smartinspect.Session#addCheckpoint
	 */
	public void resetCheckpoint() {
		synchronized (this.fCheckpointLock) {
			this.fCheckpointCounter = 0;
		}
	}

	/**
	 * Overloaded. Resets the counter of a named checkpoint.
	 * This method resets the counter of the given named checkpoint.
	 * Named checkpoints can be incremented and logged with the
	 * addCheckpoint method.
	 *
	 * @param name The name of the checkpoint to reset
	 */
	public void resetCheckpoint(String name) {
		if (name == null) {
			logInternalError("resetCheckpoint: name argument is null");
			return;
		}

		String key = name.toLowerCase();

		synchronized (this.fCheckpoints) {
			this.fCheckpoints.remove(key);
		}
	}

	/**
	 * Overloaded. Logs an assert message if a condition is false with a log level of Level.Error.
	 * An assert message is logged if this method is called with a condition parameter of the value false.
	 * No Log Entry is generated if this method is called with a condition parameter of the value true.
	 * A typical usage of this method would be to test if a variable is not set to null before you use it.
	 * To do this, you just need to insert a logAssert call to the code section in question with "instance != null" as first parameter.
	 * If the reference is null and thus the expression evaluates to false, a message is logged.
	 *
	 * @param condition The condition to check
	 * @param title     The title of the Log Entry
	 */
	public void logAssert(boolean condition, String title) {
		if (isOn(Level.Error)) {
			if (!condition) {
				sendLogEntry(Level.Error, title, LogEntryType.Assert,
						ViewerId.Title);
			}
		}
	}

	/**
	 * Overloaded. Logs an assert message if a condition is false with
	 * a log level of Lever.Error. The assert message is created with a
	 * format string and a related array of arguments.
	 * <p>
	 * An assert message is logged if this method is called with a
	 * condition parameter of the value false. No Log Entry is generated if this method is called with a
	 * condition parameter of the value true.
	 * <p>
	 * This version of the method accepts a format string and a
	 * related array of arguments. These parameters will be passed
	 * to the MessageFormat.format() method and the resulting string
	 * will be the assert message.
	 * <p>
	 * A typical usage of this method would be to test if a variable
	 * is not set to null before you use it. To do this, you just need
	 * to insert a logAssert call to the code section in question with
	 * "instance != null" as first parameter. If the reference is null
	 * and thus the expression evaluates to false, a message is logged.
	 *
	 * @param condition The condition to check
	 * @param titleFmt  The format string to create the title of the Log Entry
	 * @param args      The array of arguments for the format string
	 */
	public void logAssert(boolean condition, String titleFmt, Object[] args) {
		if (isOn(Level.Error)) {
			if (!condition) {
				try {
					String title = MessageFormat.format(titleFmt, args);
					sendLogEntry(Level.Error, title, LogEntryType.Assert,
							ViewerId.Title);
				} catch (Exception e) {
					// The MessageFormat.format method raised an exception.
					logInternalError("logAssert: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Overloaded. Logs whether a variable is assigned or not with the
	 * default log level.
	 * <p>
	 * If the instance argument is null, then ": Not assigned",
	 * otherwise ": Assigned" will be appended to the title before
	 * the Log Entry is sent.
	 * <p>
	 * This method is useful to check source code for null references
	 * in places where you experienced or expect problems and want to
	 * log possible null references.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title    The title of the variable
	 * @param instance The variable which should be checked for null
	 */
	public void logAssigned(String title, Object instance) {
		logAssigned(this.fParent.getDefaultLevel(), title, instance);
	}

	/**
	 * Overloaded. Logs whether a variable is assigned or not with a
	 * custom log level.
	 *
	 * <p>If the instance argument is null, then ": Not assigned",
	 * otherwise ": Assigned" will be appended to the title before
	 * the log entry is sent.
	 *
	 * <p>This method is useful to check source code for null references
	 * in places where you experienced or expect problems and want to
	 * log possible null references.
	 *
	 * @param level    The log level of this method call
	 * @param title    The title of the variable
	 * @param instance The variable which should be checked for null
	 */
	public void logAssigned(Level level, String title, Object instance) {
		if (isOn(level)) {
			if (instance != null) {
				logMessage(title + ": Assigned");
			} else {
				logMessage(title + ": Not assigned");
			}
		}
	}

	/**
	 * Logs a conditional message with the default log level. This method only sends a message
	 * if the passed 'condition' argument evaluates to true. If 'condition' is false, this
	 * method has no effect and nothing is logged. This method is thus the counter piece to logAssert.
	 * This method uses the default level of the session's parent as log level. For more information,
	 * please refer to the documentation of the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param condition The condition to evaluate
	 * @param title     The title of the conditional message
	 */
	public void logConditional(boolean condition, String title) {
		logConditional(this.fParent.getDefaultLevel(), condition, title);
	}

	/**
	 * Overloaded. Logs a conditional message with the default log
	 * level. The message is created with a format string and a
	 * related array of arguments.
	 * This method only sends a message if the passed 'condition'
	 * argument evaluates to true. If 'condition' is false, this
	 * method has no effect and nothing is logged. This method is
	 * thus the counter piece to logAssert.
	 * <p>
	 * This version of the method accepts a format string and a
	 * related array of arguments. These parameters will be passed
	 * to the MessageFormat.format method and the resulting string
	 * will be the conditional message.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param condition The condition to evaluate
	 * @param titleFmt  The format string to create the conditional message
	 * @param args      The array of arguments for the format string
	 */
	public void logConditional(boolean condition, String titleFmt,
							   Object[] args) {
		logConditional(this.fParent.getDefaultLevel(), condition,
				titleFmt, args);
	}

	/**
	 * Logs a conditional message with a custom log level. Overloaded version of the method.
	 * This method only sends a message if the passed 'condition'
	 * argument evaluates to true. If 'condition' is false, this
	 * method has no effect and nothing is logged. This method is
	 * thus the counter piece to logAssert.
	 *
	 * @param level     The log level of this method call
	 * @param condition The condition to evaluate
	 * @param title     The title of the conditional message
	 */
	public void logConditional(Level level, boolean condition, String title) {
		if (isOn(level)) {
			if (condition) {
				sendLogEntry(level, title, LogEntryType.Conditional,
						ViewerId.Title);
			}
		}
	}

	/**
	 * Logs a conditional message with a custom log level.
	 * The message is created with a format string and a related array of arguments.
	 *
	 * <p>
	 * This method only sends a message if the passed 'condition' argument evaluates to true.
	 * If 'condition' is false, this method has no effect and nothing is logged.
	 * This method is thus the counter piece to logAssert.
	 * <p>
	 * This version of the method accepts a format string and a related array of arguments.
	 * These parameters will be passed to the MessageFormat.format method and the resulting string
	 * will be the conditional message.
	 * <p>
	 * This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of the setDefaultLevel method
	 * of the SmartInspect class.
	 * </p>
	 *
	 * @param level     The log level of this method call
	 * @param condition The condition to evaluate
	 * @param titleFmt  The format string to create the conditional message
	 * @param args      The array of arguments for the format string
	 */
	public void logConditional(Level level, boolean condition,
							   String titleFmt, Object[] args) {
		if (isOn(level)) {
			if (condition) {
				try {
					String title = MessageFormat.format(titleFmt, args);
					sendLogEntry(level, title, LogEntryType.Conditional,
							ViewerId.Title);
				} catch (Exception e) {
					logInternalError("logConditional: " + e.getMessage());
				}
			}
		}
	}

	private static String longToHex(long value, int maxChars) {
		String s = Long.toHexString(value);
		int len = s.length();

		if (len >= maxChars) {
			return s.substring(len - maxChars);
		} else {
			StringBuffer sb = new StringBuffer(s);
			while (len++ < maxChars) {
				sb.insert(0, "0");
			}

			return sb.toString();
		}
	}

	/**
	 * Logs a boolean value with the default log level. Overloaded.
	 * This method logs the name and value of a boolean variable. A title like "name = True" will be displayed in the Console.
	 * This method uses the default level of the session's parent as log level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logBoolean(String name, boolean value) {
		logBoolean(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a boolean value with a custom log level.
	 * This method logs the name and value of a boolean variable. A
	 * title like "name = True" will be displayed in the Console.
	 *
	 * @param level The log level of this method call
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logBoolean(Level level, String name, boolean value) {
		if (isOn(level)) {
			String title = name + " = " + (value ? "True" : "False");
			sendLogEntry(level, title, LogEntryType.VariableValue,
					ViewerId.Title);
		}
	}

	/**
	 * Overloaded method. Logs a char value with the default log level.
	 * This method logs the name and value of a char variable. A title
	 * like "name = 'c'" will be displayed in the Console.
	 * This method uses the {@link SmartInspect#setDefaultLevel} of the session's
	 * {@link #getParent() parent} as log level. For more information, please refer to the documentation
	 * of the {@link SmartInspect#setDefaultLevel} method of the SmartInspect class.
	 *
	 * @param name  The variable name
	 * @param value The variable value
	 */

	public void logChar(String name, char value) {
		logChar(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a char value with a custom log level.
	 * This method logs the name and value of a char variable. A title like "name = 'c'" will be displayed in the Console.
	 *
	 * @param level The log level of this method call
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logChar(Level level, String name, char value) {
		if (isOn(level)) {
			String title = name + " = '" + value + "'";
			sendLogEntry(level, title, LogEntryType.VariableValue,
					ViewerId.Title);
		}
	}

	/**
	 * Overloaded. Logs a byte value with the default log level. This method logs the name and value of a byte variable. A title
	 * like "name = 23" will be displayed in the Console.
	 * <p>
	 * This method uses the default level of the session's parent as log level. For more information,
	 * please refer to the documentation of the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logByte(String name, byte value) {
		logByte(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a byte value with a custom log level.
	 * This method logs the name and value of a byte variable. A title
	 * like "name = 23" will be displayed in the Console.
	 *
	 * @param level The log level of this method call
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logByte(Level level, String name, byte value) {
		logByte(level, name, value, false);
	}

	/**
	 * Overloaded. Logs a byte value with an optional hexadecimal
	 * representation and default log level.
	 * <p>
	 * This method logs the name and value of a byte variable. If you
	 * set the includeHex argument to true then the hexadecimal
	 * representation of the supplied variable value is included as
	 * well.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name       The variable name
	 * @param value      The variable value
	 * @param includeHex Indicates if a hexadecimal representation should be included
	 */
	public void logByte(String name, byte value, boolean includeHex) {
		logByte(this.fParent.getDefaultLevel(), name, value, includeHex);
	}

	/**
	 * This method logs the name and value of a byte variable. If you
	 * set the includeHex argument to true then the hexadecimal
	 * representation of the supplied variable value is included as
	 * well.
	 * Overloaded. Logs a byte value with an optional hexadecimal
	 * representation and custom log level.
	 *
	 * @param level      The log level of this method call
	 * @param name       The variable name
	 * @param value      The variable value
	 * @param includeHex Indicates if a hexadecimal representation should be included
	 */
	public void logByte(Level level, String name, byte value,
						boolean includeHex) {
		if (isOn(level)) {
			StringBuffer title = new StringBuffer();

			title.append(name);
			title.append(" = ");
			title.append(value);

			if (includeHex) {
				title.append(" (0x");
				title.append(longToHex(value, 2));
				title.append(")");
			}

			sendLogEntry(level, title.toString(), LogEntryType.VariableValue,
					ViewerId.Title);
		}
	}

	/**
	 * Overloaded. Logs a short integer value with the default log
	 * level.
	 * <p>
	 * This method logs the name and value of a short integer variable.
	 * A title like "name = 23" will be displayed in the Console.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method
	 * of the SmartInspect class.
	 *
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logShort(String name, short value) {
		logShort(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a short integer value with a custom log level.
	 * <p>
	 * This method logs the name and value of a short integer variable.
	 * A title like "name = 23" will be displayed in the Console.
	 *
	 * @param level The log level of this method call
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logShort(Level level, String name, short value) {
		logShort(level, name, value, false);
	}

	/**
	 * Overloaded. Logs a short integer value with an optional
	 * hexadecimal representation and default log level.
	 * <p>
	 * This method logs the name and value of a short integer variable.
	 * If you set the includeHex argument to true then the hexadecimal
	 * representation of the supplied variable value is included as
	 * well.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name       The variable name
	 * @param value      The variable value
	 * @param includeHex Indicates if a hexadecimal representation should be included
	 */
	public void logShort(String name, short value, boolean includeHex) {
		logShort(this.fParent.getDefaultLevel(), name, value, includeHex);
	}

	/**
	 * Overloaded. Logs a short integer value with an optional
	 * hexadecimal representation and custom log level.
	 * This method logs the name and value of a short integer variable.
	 * If you set the includeHex argument to true then the hexadecimal
	 * representation of the supplied variable value is included as
	 * well.
	 *
	 * @param level      The log level of this method call
	 * @param name       The variable name
	 * @param value      The variable value
	 * @param includeHex Indicates if a hexadecimal representation should be included
	 */
	public void logShort(Level level, String name, short value,
						 boolean includeHex) {
		if (isOn(level)) {
			StringBuffer title = new StringBuffer();

			title.append(name);
			title.append(" = ");
			title.append(value);

			if (includeHex) {
				title.append(" (0x");
				title.append(longToHex(value, 4));
				title.append(")");
			}

			sendLogEntry(level, title.toString(), LogEntryType.VariableValue,
					ViewerId.Title);
		}
	}

	/**
	 * Overloaded. Logs an integer value with the default log level.
	 *
	 * <p>This method logs the name and value of an integer variable. A
	 * title like "name = 23" will be displayed in the Console.
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logInt(String name, int value) {
		logInt(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs an integer value with a custom log level.
	 * <p>
	 * This method logs the name and value of an integer variable. A
	 * title like "name = 23" will be displayed in the Console.
	 *
	 * @param level The log level of this method call
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logInt(Level level, String name, int value) {
		logInt(level, name, value, false);
	}

	/**
	 * Logs an integer value with an optional hexadecimal
	 * representation and default log level.
	 * <p>
	 * This method logs the name and value of an integer variable. If
	 * you set the includeHex argument to true then the hexadecimal
	 * representation of the supplied variable value is included as
	 * well.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name       The variable name
	 * @param value      The variable value
	 * @param includeHex Indicates if a hexadecimal representation should be included
	 */
	public void logInt(String name, int value, boolean includeHex) {
		logInt(this.fParent.getDefaultLevel(), name, value, includeHex);
	}

	/**
	 * Overloaded. Logs an integer value with an optional hexadecimal
	 * representation and custom log level.
	 * <p>
	 * This method logs the name and value of an integer variable. If
	 * you set the includeHex argument to true then the hexadecimal
	 * representation of the supplied variable value is included as
	 * well.
	 *
	 * @param level      The log level of this method call
	 * @param name       The variable name
	 * @param value      The variable value
	 * @param includeHex Indicates if a hexadecimal representation should be included
	 */
	public void logInt(Level level, String name, int value,
					   boolean includeHex) {
		if (isOn(level)) {
			StringBuffer title = new StringBuffer();

			title.append(name);
			title.append(" = ");
			title.append(value);

			if (includeHex) {
				title.append(" (0x");
				title.append(longToHex(value, 8));
				title.append(")");
			}

			sendLogEntry(level, title.toString(), LogEntryType.VariableValue,
					ViewerId.Title);
		}
	}

	/**
	 * Logs a long integer value with the default log level.
	 * <p>
	 * This method logs the name and value of a long integer variable.
	 * A title like "name = 23" will be displayed in the Console.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logLong(String name, long value) {
		logLong(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a long integer value with a custom log
	 * level. This method logs the name and value of a long integer variable.
	 * A title like "name = 23" will be displayed in the Console.
	 *
	 * @param level The log level of this method call
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logLong(Level level, String name, long value) {
		logLong(level, name, value, false);
	}

	/**
	 * Overloaded. Logs a long integer value with an optional
	 * hexadecimal representation and default log level.
	 *
	 * <p>
	 * This method logs the name and value of a long integer variable.
	 * If you set the includeHex argument to true then the hexadecimal
	 * representation of the supplied variable value is included as
	 * well.
	 *
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name       The variable name
	 * @param value      The variable value
	 * @param includeHex Indicates if a hexadecimal representation should be included
	 */
	public void logLong(String name, long value, boolean includeHex) {
		logLong(this.fParent.getDefaultLevel(), name, value, includeHex);
	}

	/**
	 * Overloaded. Logs a long integer value with an optional
	 * hexadecimal representation and custom log level.
	 * <p>
	 * This method logs the name and value of a long integer variable.
	 * If you set the includeHex argument to true then the hexadecimal
	 * representation of the supplied variable value is included as
	 * well.
	 *
	 * @param level      The log level of this method call
	 * @param name       The variable name
	 * @param value      The variable value
	 * @param includeHex Indicates if a hexadecimal representation should be included
	 */
	public void logLong(Level level, String name, long value,
						boolean includeHex) {
		if (isOn(level)) {
			StringBuffer title = new StringBuffer();

			title.append(name);
			title.append(" = ");
			title.append(value);

			if (includeHex) {
				title.append(" (0x");
				title.append(longToHex(value, 16));
				title.append(")");
			}

			sendLogEntry(level, title.toString(), LogEntryType.VariableValue,
					ViewerId.Title);
		}
	}

	/**
	 * Overloaded. Logs a float value with the default log level.
	 *
	 * <p>This method logs the name and value of a float variable. A title
	 * like "name = 3.1415" will be displayed in the Console.</p>
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.</p>
	 *
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logFloat(String name, float value) {
		logFloat(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a float value with a custom log level.
	 * This method logs the name and value of a float variable. A title
	 * like "name = 3.1415" will be displayed in the Console.
	 *
	 * @param level The log level of this method call
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logFloat(Level level, String name, float value) {
		if (isOn(level)) {
			String title = name + " = " + value;
			sendLogEntry(level, title, LogEntryType.VariableValue,
					ViewerId.Title);
		}
	}

	/**
	 * Overloaded. Logs a double value with the default log level.
	 * This method logs the name and value of a double variable. A
	 * title like "name = 3.1415" will be displayed in the Console.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logDouble(String name, double value) {
		logDouble(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a double value with a custom log level.
	 * <p>
	 * This method logs the name and value of a double variable. A
	 * title like "name = 3.1415" will be displayed in the Console.
	 *
	 * @param level The log level of this method call
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logDouble(Level level, String name, double value) {
		if (isOn(level)) {
			String title = name + " = " + value;
			sendLogEntry(level, title, LogEntryType.VariableValue,
					ViewerId.Title);
		}
	}

	/**
	 * Overloaded. Logs a string value with the default log level.
	 * <p>
	 * This method logs the name and value of a string variable. A title like "name = "string""
	 * will be displayed in the Console.
	 * <p>
	 * This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of the setDefaultLevel method
	 * of the SmartInspect class.
	 *
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logString(String name, String value) {
		logString(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a string value with a custom log level.
	 *
	 * <p>This method logs the name and value of a string variable. A
	 * title like "name = "string"" will be displayed in the Console.</p>
	 *
	 * @param level The log level of this method call
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logString(Level level, String name, String value) {
		if (isOn(level)) {
			String title = name + " = \"" + value + "\"";
			sendLogEntry(level, title, LogEntryType.VariableValue,
					ViewerId.Title);
		}
	}

	/**
	 * Overloaded. Logs an object value with the default log level.
	 * <p>
	 * This method logs the name and value of an object. The title to
	 * display in the Console will consist of the name and the return
	 * value of the toString method of the object.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logObjectValue(String name, Object value) {
		logObjectValue(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs an object value with a custom log level.
	 * <p>
	 * This method logs the name and value of an object. The title to
	 * display in the Console will consist of the name and the return
	 * value of the toString method of the object.
	 *
	 * @param level The log level of this method call
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logObjectValue(Level level, String name, Object value) {
		if (isOn(level)) {
			if (value == null) {
				logInternalError("logObjectValue: value argument is null");
			} else {
				String title = name + " = " + value.toString();
				sendLogEntry(level, title, LogEntryType.VariableValue,
						ViewerId.Title);
			}
		}
	}

	/**
	 * Overloaded. Logs a Date value with the default log level.
	 * <p>
	 * This method logs the name and value of a Date instance. A title
	 * like "name = Wed Dec 29 10:52:31 CET 2004" will be displayed in
	 * the Console.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logDate(String name, Date value) {
		logDate(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a Date value with a custom log level.
	 * <p>
	 * This method logs the name and value of a Date instance. A title
	 * like "name = Wed Dec 29 10:52:31 CET 2004" will be displayed in
	 * the Console.
	 *
	 * @param level The log level of this method call
	 * @param name  The variable name
	 * @param value The variable value
	 */
	public void logDate(Level level, String name, Date value) {
		if (isOn(level)) {
			if (value == null) {
				logInternalError("logDate: value argument is null");
			} else {
				String title = name + " = " + value.toString();
				sendLogEntry(level, title, LogEntryType.VariableValue,
						ViewerId.Title);
			}
		}
	}

	/**
	 * Overloaded. Logs the name and value of a boolean variable with the default log level.
	 * This method just calls the logBoolean method.
	 * This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of the setDefaultLevel method
	 * of the SmartInspect class.
	 *
	 * @param name  The name of the variable
	 * @param value The boolean value of the variable
	 */
	public void logValue(String name, boolean value) {
		logValue(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs the name and value of a boolean variable
	 * with a custom log level.
	 * This method just calls the logBoolean method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the variable
	 * @param value The boolean value of the variable
	 */
	public void logValue(Level level, String name, boolean value) {
		logBoolean(level, name, value);
	}

	/**
	 * Overloaded. Logs the name and value of a char variable with the default log level.
	 * This method just calls the logChar method.
	 * This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the variable
	 * @param value The char value of the variable
	 */
	public void logValue(String name, char value) {
		logValue(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Logs the name and value of a char variable with a custom log level.
	 * This method just calls the logChar method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the variable
	 * @param value The char value of the variable
	 */
	public void logValue(Level level, String name, char value) {
		logChar(level, name, value);
	}

	/**
	 * Overloaded. Logs the name and value of a byte variable with
	 * the default log level.
	 * <p>
	 * This method just calls the logByte method.
	 * This method uses the {@link SmartInspect#setDefaultLevel}
	 * of the session's {@link Session#getParent} as log
	 * level. For more information, please refer to the documentation of
	 * the {@link SmartInspect#setDefaultLevel} method
	 * of the SmartInspect class.
	 *
	 * @param name  The name of the variable
	 * @param value The byte value of the variable
	 */
	public void logValue(String name, byte value) {
		logValue(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs the name and value of a byte variable
	 * with a custom log level.
	 * <p>
	 * Note: This method just calls the logByte method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the variable
	 * @param value The byte value of the variable
	 */
	public void logValue(Level level, String name, byte value) {
		logByte(level, name, value);
	}

	/**
	 * Overloaded. Logs the name and value of a short integer variable
	 * with the default log level.
	 * This method just calls the logShort method.
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the variable
	 * @param value The short integer value of the variable
	 */
	public void logValue(String name, short value) {
		logValue(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs the name and value of a short integer
	 * variable with a custom log level.
	 * <p>
	 * This method just calls the logShort method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the variable
	 * @param value The short integer value of the variable
	 */
	public void logValue(Level level, String name, short value) {
		logShort(level, name, value);
	}

	/**
	 * Logs the name and value of an integer variable with the default log level.
	 * <p>
	 * This method just calls the logInt method.
	 * <p>
	 * This method uses the default level of the session's parent as log level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the variable
	 * @param value The integer value of the variable
	 */
	public void logValue(String name, int value) {
		logValue(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs the name and value of an integer variable
	 * with a custom log level.
	 * <p>
	 * This method just calls the logInt method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the variable
	 * @param value The integer value of the variable
	 */
	public void logValue(Level level, String name, int value) {
		logInt(level, name, value);
	}

	/**
	 * Overloaded. Logs the name and value of a long integer variable with the default log level.
	 * This method just calls the logLong method.
	 * This method uses the default level of the session's parent as log level. For more information, please refer to
	 * the documentation of the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the variable
	 * @param value The long integer value of the variable
	 */
	public void logValue(String name, long value) {
		logValue(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs the name and value of a long integer
	 * variable with a custom log level. This method just calls the logLong method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the variable
	 * @param value The long integer value of the variable
	 */
	public void logValue(Level level, String name, long value) {
		logLong(level, name, value);
	}

	/**
	 * Logs the name and value of a float variable with the default log level.
	 * This method just calls the logFloat method.
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the variable
	 * @param value The float value of the variable
	 */
	public void logValue(String name, float value) {
		logValue(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs the name and value of a float variable
	 * with a custom log level.
	 * This method just calls the logFloat method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the variable
	 * @param value The float value of the variable
	 */
	public void logValue(Level level, String name, float value) {
		logFloat(level, name, value);
	}

	/**
	 * Overloaded. Logs the name and value of a double variable with
	 * the default log level.
	 * <p>
	 * This method just calls the logDouble method.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the variable
	 * @param value The double value of the variable
	 */
	public void logValue(String name, double value) {
		logValue(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs the name and value of a double variable with a custom log level.
	 * This method just calls the logDouble method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the variable
	 * @param value The double value of the variable
	 */
	public void logValue(Level level, String name, double value) {
		logDouble(level, name, value);
	}

	/**
	 * Logs the name and value of a string variable with
	 * the default log level. Overloaded.
	 * <p>
	 * This method just calls the logString method.
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the variable
	 * @param value The string value of the variable
	 */
	public void logValue(String name, String value) {
		logValue(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded method that logs the name and value of a string variable
	 * with a custom log level.
	 * This method just calls the logString method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the variable
	 * @param value The string value of the variable
	 */
	public void logValue(Level level, String name, String value) {
		logString(level, name, value);
	}

	/**
	 * Overloaded. Logs the name and value of an object with the
	 * default log level.
	 * <p>
	 * This method just calls the logObjectValue method.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the variable
	 * @param value The object to log
	 */
	public void logValue(String name, Object value) {
		logValue(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Logs the name and value of an object with a custom log level. This method just calls the logObjectValue method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the variable
	 * @param value The object to log
	 */
	public void logValue(Level level, String name, Object value) {
		logObjectValue(level, name, value);
	}

	/**
	 * Overloaded. Logs the name and value of a Date instance with
	 * the default log level.
	 *
	 * <p>This method just calls the logDate method.
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the variable
	 * @param value The Date value of the variable
	 */
	public void logValue(String name, Date value) {
		logValue(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs the name and value of a Date instance
	 * with a custom log level.
	 * This method just calls the logDate method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the variable
	 * @param value The Date value of the variable
	 */
	public void logValue(Level level, String name, Date value) {
		logDate(level, name, value);
	}

	/**
	 * Overloaded. Logs a custom viewer context with the default log
	 * level.
	 * <p>
	 * This method can be used to extend the capabilities of the
	 * SmartInspect Java library. You can assemble a so called viewer
	 * context and thus can send custom data to the SmartInspect
	 * Console. Furthermore, you can choose the viewer in which your
	 * data should be displayed. Every viewer in the Console has
	 * a corresponding viewer context class in this library.
	 * <p>
	 * Have a look at the ViewerContext class and its derived classes
	 * to see a list of available viewer context classes.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title The title to display in the Console
	 * @param lt    The custom Log Entry type
	 * @param ctx   See {@link com.gurock.smartinspect.ViewerContext}
	 */
	public void logCustomContext(String title, LogEntryType lt,
								 ViewerContext ctx) {
		logCustomContext(this.fParent.getDefaultLevel(), title, lt, ctx);
	}

	/**
	 * Overloaded. Logs a custom viewer context with a custom log level.
	 * <p>
	 * This method can be used to extend the capabilities of the SmartInspect Java library.
	 * You can assemble a so-called viewer context and thus can send custom data to the SmartInspect
	 * Console. Furthermore, you can choose the viewer in which your data should be displayed.
	 * Every viewer in the Console has a corresponding viewer context class in this library.
	 * <p>
	 * Have a look at the ViewerContext class and its derived classes to see a list of available
	 * viewer context classes.
	 *
	 * @param level The log level of this method call
	 * @param title The title to display in the Console
	 * @param lt    The custom Log Entry type
	 * @param ctx   @see com.gurock.smartinspect.ViewerContext
	 */
	public void logCustomContext(Level level, String title, LogEntryType lt,
								 ViewerContext ctx) {
		if (isOn(level)) {
			if (lt == null || ctx == null) {
				logInternalError("logCustomContext: Invalid arguments");
			} else {
				sendContext(level, title, lt, ctx);
			}
		}
	}

	/* Internal send methods go here. */

	private void sendContext(Level level, String title, LogEntryType lt,
							 ViewerContext ctx) {
		sendLogEntry(level, title, lt, ctx.getViewerId(), getColor(),
				ctx.getViewerData());
	}

	private void sendLogEntry(Level level, String title, LogEntryType lt,
							  ViewerId vi) {
		sendLogEntry(level, title, lt, vi, getColor(), null);
	}

	private void sendLogEntry(Level level, String title, LogEntryType lt,
							  ViewerId vi, Color color, byte[] data) {
		LogEntry logEntry = new LogEntry(lt, vi);
		logEntry.setTimestamp(this.fParent.now());
		logEntry.setLevel(level);
		logEntry.setTitle(title);

		if (color == DEFAULT_COLOR) {
			logEntry.setColor(color); /* Transparent */
		} else {
			int rgb = color.getRGB() & 0xffffff;
			logEntry.setColor(new Color(rgb, true));
		}

		logEntry.setSessionName(getName()); // Our session name.
		logEntry.setData(data);
		this.fParent.sendLogEntry(logEntry);
	}

	private void sendControlCommand(ControlCommandType ct, byte[] data) {
		ControlCommand controlCommand = new ControlCommand(ct);
		controlCommand.setLevel(Level.Control);
		controlCommand.setData(data);
		this.fParent.sendControlCommand(controlCommand);
	}

	private void sendProcessFlow(Level level, String title,
								 ProcessFlowType pt) {
		ProcessFlow processFlow = new ProcessFlow(pt);
		processFlow.setTimestamp(this.fParent.now());
		processFlow.setLevel(level);
		processFlow.setTitle(title);
		this.fParent.sendProcessFlow(processFlow);
	}

	private void sendWatch(Level level, String name, String value,
						   WatchType wt) {
		Watch watch = new Watch(wt);
		watch.setTimestamp(this.fParent.now());
		watch.setLevel(level);
		watch.setName(name);
		watch.setValue(value);
		this.fParent.sendWatch(watch);
	}

	/**
	 * Overloaded. Logs a text using a custom Log Entry type, viewer ID
	 * and default log level.
	 * <p>
	 * This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of the setDefaultLevel method
	 * of the SmartInspect class.
	 *
	 * @param title The title to display in the Console
	 * @param text  The text to log
	 * @param lt    The custom Log Entry type
	 * @param vi    The custom viewer ID which specifies the way the Console handles
	 *              the text content
	 **/
	public void logCustomText(String title, String text, LogEntryType lt,
							  ViewerId vi) {
		logCustomText(this.fParent.getDefaultLevel(), title, text, lt, vi);
	}

	/**
	 * Logs a text using a custom Log Entry type, viewer ID and custom log level.
	 *
	 * @param level The log level of this method call
	 * @param title The title to display in the Console
	 * @param text  The text to log
	 * @param lt    The custom Log Entry type
	 * @param vi    The custom viewer ID which specifies the way the Console handles
	 *              the text content
	 */
	public void logCustomText(Level level, String title, String text,
							  LogEntryType lt, ViewerId vi) {
		if (isOn(level)) {
			TextContext ctx = new TextContext(vi);
			try {
				try {
					ctx.loadFromText(text);
					sendContext(level, title, lt, ctx);
				} catch (Exception e) {
					logInternalError("logCustomText: " + e.getMessage());
				}
			} finally {
				ctx.close();
			}
		}
	}

	/**
	 * Overloaded. Logs the content of a file using a custom Log Entry
	 * type, viewer ID and default log level.
	 * <p>
	 * This method logs the content of the supplied file using a custom
	 * Log Entry type and viewer ID. The parameters control the way the
	 * content of the file is displayed in the Console. Thus you can
	 * extend the functionality of the SmartInspect Java library with
	 * this method.
	 * <p>
	 * This version of the method uses the fileName argument as
	 * title to display in the Console.
	 * <p>
	 * This method uses the {@link SmartInspect#setDefaultLevel}
	 * default level of the session's {@link Session#getParent} parent as log
	 * level. For more information, please refer to the documentation of
	 * the {@link SmartInspect#setDefaultLevel} method of the SmartInspect class.
	 *
	 * @param fileName The file to log
	 * @param lt       The custom Log Entry type
	 * @param vi       The custom viewer ID which specifies the way the Console
	 *                 handles the file content
	 */
	public void logCustomFile(String fileName, LogEntryType lt, ViewerId vi) {
		logCustomFile(this.fParent.getDefaultLevel(), fileName, lt, vi);
	}

	/**
	 * Overloaded. Logs the content of a file using a custom Log Entry
	 * type, viewer ID and custom log level.
	 * <p>
	 * This method logs the content of the supplied file using a custom
	 * Log Entry type and viewer ID. The parameters control the way the
	 * content of the file is displayed in the Console. Thus you can
	 * extend the functionality of the SmartInspect Java library with
	 * this method.
	 * <p>
	 * This version of the method uses the fileName argument as
	 * title to display in the Console.
	 *
	 * @param level    The log level of this method call
	 * @param fileName The file to log
	 * @param lt       The custom Log Entry type
	 * @param vi       The custom viewer ID which specifies the way the Console handles
	 *                 the file content
	 */
	public void logCustomFile(Level level, String fileName, LogEntryType lt,
							  ViewerId vi) {
		logCustomFile(level, fileName, fileName, lt, vi);
	}

	/**
	 * Overloaded. Logs the content of a file using a custom Log Entry
	 * type, viewer ID, title and default log level.
	 * <p>
	 * This method logs the content of the supplied file using a custom
	 * Log Entry type and viewer ID. The parameters control the way the
	 * content of the file is displayed in the Console. Thus, you can
	 * extend the functionality of the SmartInspect Java library with
	 * this method.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param fileName The file to log
	 * @param lt       The custom Log Entry type
	 * @param vi       The custom viewer ID which specifies the way the Console
	 *                 handles the file content
	 * @param title    The title to display in the Console
	 */
	public void logCustomFile(String title, String fileName, LogEntryType lt,
							  ViewerId vi) {
		logCustomFile(this.fParent.getDefaultLevel(), title, fileName, lt, vi);
	}

	/**
	 * Overloaded. Logs the content of a file using a custom Log Entry type, viewer ID, title and custom log level.
	 *
	 * <p>This method logs the content of the supplied file using a custom
	 * Log Entry type and viewer ID. The parameters control the way the
	 * content of the file is displayed in the Console. Thus you can
	 * extend the functionality of the SmartInspect Java library with
	 * this method.</p>
	 *
	 * @param level    The log level of this method call
	 * @param fileName The file to log
	 * @param lt       The custom Log Entry type
	 * @param vi       The custom viewer ID which specifies the way the Console handles the file content
	 * @param title    The title to display in the Console
	 */
	public void logCustomFile(Level level, String title,
							  String fileName, LogEntryType lt, ViewerId vi) {
		if (isOn(level)) {
			if (lt == null) {
				logInternalError("logCustomFile: lt argument is null");
			} else if (vi == null) {
				logInternalError("logCustomFile: vi argument is null");
			} else {
				BinaryContext ctx = new BinaryContext(vi);
				try {
					try {
						ctx.loadFromFile(fileName);
						sendContext(level, title, lt, ctx);
					} catch (Exception e) {
						logInternalError("logCustomFile: " + e.getMessage());
					}
				} finally {
					ctx.close();
				}
			}
		}
	}

	/**
	 * Logs the content of a stream using a custom Log Entry type, viewer ID and default log level.
	 * This method logs the content of the supplied stream using a custom Log Entry type and viewer ID.
	 * The parameters control the way the content of the file is displayed in the Console. Thus you can extend
	 * the functionality of the SmartInspect Java library with this method.
	 * This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title  The title to display in the Console
	 * @param stream The stream to log
	 * @param lt     The custom Log Entry type
	 * @param vi     The custom viewer ID which specifies the way the Console handles the stream content
	 */
	public void logCustomStream(String title, InputStream stream,
								LogEntryType lt, ViewerId vi) {
		logCustomStream(this.fParent.getDefaultLevel(), title, stream, lt, vi);
	}

	/**
	 * Overloaded. Logs the content of a stream using a custom Log Entry
	 * type, viewer ID and custom log level.
	 *
	 * <p>This method logs the content of the supplied stream using a custom
	 * Log Entry type and viewer ID. The parameters control the way the
	 * content of the file is displayed in the Console. Thus you can
	 * extend the functionality of the SmartInspect Java library with
	 * this method.</p>
	 *
	 * @param level  The log level of this method call
	 * @param title  The title to display in the Console
	 * @param stream The stream to log
	 * @param lt     The custom Log Entry type
	 * @param vi     The custom viewer ID which specifies the way the Console
	 *               handles the stream content
	 */
	public void logCustomStream(Level level, String title,
								InputStream stream, LogEntryType lt, ViewerId vi) {
		if (isOn(level)) {
			if (lt == null) {
				logInternalError("logCustomStream: lt argument is null");
			} else if (vi == null) {
				logInternalError("logCustomStream: vi argument is null");
			} else {
				BinaryContext ctx = new BinaryContext(vi);
				try {
					try {
						ctx.loadFromStream(stream);
						sendContext(level, title, lt, ctx);
					} catch (Exception e) {
						logInternalError("logCustomStream: " + e.getMessage());
					}
				} finally {
					ctx.close();
				}
			}
		}
	}

	/**
	 * Logs the content of a reader using a custom Log Entry type, viewer ID and
	 * default log level.
	 * <p>
	 * This method logs the content of the supplied reader using a custom
	 * Log Entry type and viewer ID. The parameters control the way the
	 * content of the file is displayed in the Console. Thus you can
	 * extend the functionality of the SmartInspect Java library with
	 * this method.
	 * </p>
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 * </p>
	 *
	 * @param title  The title to display in the Console
	 * @param reader The reader to log
	 * @param lt     The custom Log Entry type
	 * @param vi     The custom viewer ID which specifies the way the Console
	 *               handles the reader content
	 */
	public void logCustomReader(String title, Reader reader, LogEntryType lt,
								ViewerId vi) {
		logCustomReader(this.fParent.getDefaultLevel(), title, reader, lt, vi);
	}

	/**
	 * Overloaded. Logs the content of a reader using a custom Log Entry type, viewer ID and custom log level.
	 * This method logs the content of the supplied reader using a custom Log Entry type and viewer ID.
	 * The parameters control the way the content of the file is displayed in the Console. Thus you can
	 * extend the functionality of the SmartInspect Java library with this method.
	 *
	 * @param level  The log level of this method call
	 * @param title  The title to display in the Console
	 * @param reader The reader to log
	 * @param lt     The custom Log Entry type
	 * @param vi     The custom viewer ID which specifies the way the Console handles the reader content
	 */
	public void logCustomReader(Level level, String title, Reader reader,
								LogEntryType lt, ViewerId vi) {
		if (isOn(level)) {
			if (lt == null) {
				logInternalError("logCustomReader: lt argument is null");
			} else if (vi == null) {
				logInternalError("logCustomReader: vi argument is null");
			} else {
				TextContext ctx = new TextContext(vi);
				try {
					try {
						ctx.loadFromReader(reader);
						sendContext(level, title, lt, ctx);
					} catch (Exception e) {
						logInternalError("logCustomReader: " + e.getMessage());
					}
				} finally {
					ctx.close();
				}
			}
		}
	}

	/**
	 * Logs a string with the default log level and displays it in a read-only text field.
	 * This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of the setDefaultLevel method
	 * of the SmartInspect class.
	 *
	 * @param title The title to display in the Console
	 * @param text  The text to log
	 */
	public void logText(String title, String text) {
		logText(this.fParent.getDefaultLevel(), title, text);
	}

	/**
	 * Overloaded. Logs a string with a custom log level and displays it
	 * in a read-only text field.
	 *
	 * @param level The log level of this method call
	 * @param title The title to display in the Console
	 * @param text  The text to log
	 */
	public void logText(Level level, String title, String text) {
		logCustomText(level, title, text, LogEntryType.Text, ViewerId.Data);
	}

	/**
	 * Overloaded. Logs a text file with the default log level and displays
	 * the content in a read-only text field.
	 * <p>
	 * This version of the method uses the fileName argument as title to
	 * display in the Console.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param fileName The file to log
	 */
	public void logTextFile(String fileName) {
		logTextFile(this.fParent.getDefaultLevel(), fileName);
	}

	/**
	 * Overloaded. Logs a text file with a custom log level and displays
	 * the content in a read-only text field.
	 * <p>
	 * This version of the method uses the fileName argument as title to
	 * display in the Console.
	 *
	 * @param level    The log level of this method call
	 * @param fileName The file to log
	 */
	public void logTextFile(Level level, String fileName) {
		logCustomFile(level, fileName, LogEntryType.Text, ViewerId.Data);
	}

	/**
	 * Overloaded. Logs a text file and displays the content in a read-only
	 * text field using a custom title and default log level.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title    The title to display in the Console
	 * @param fileName The file to log
	 */
	public void logTextFile(String title, String fileName) {
		logTextFile(this.fParent.getDefaultLevel(), title, fileName);
	}

	/**
	 * Overloaded. Logs a text file and displays the content in a read-only
	 * text field using a custom title and custom log level.
	 *
	 * @param level    The log level of this method call
	 * @param title    The title to display in the Console
	 * @param fileName The file to log
	 */
	public void logTextFile(Level level, String title, String fileName) {
		logCustomFile(level, title, fileName, LogEntryType.Text,
				ViewerId.Data);
	}

	/**
	 * Overloaded. Logs a stream with the default log level and displays
	 * the content in a read-only text field.
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title  The title to display in the Console
	 * @param stream The stream to log
	 */
	public void logTextStream(String title, InputStream stream) {
		logTextStream(this.fParent.getDefaultLevel(), title, stream);
	}

	/**
	 * Logs a stream with a custom log level and displays
	 * the content in a read-only text field.
	 *
	 * @param level  The log level of this method call
	 * @param title  The title to display in the Console
	 * @param stream The stream to log
	 */
	public void logTextStream(Level level, String title, InputStream stream) {
		logCustomStream(level, title, stream, LogEntryType.Text,
				ViewerId.Data);
	}

	/**
	 * Overloaded method. Logs a reader with the default log level and displays
	 * the content in a read-only text field.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title  The title to display in the Console
	 * @param reader The reader to log
	 */
	public void logTextReader(String title, Reader reader) {
		logTextReader(this.fParent.getDefaultLevel(), title, reader);
	}

	/**
	 * Overloaded. Logs a reader with a custom log level and displays
	 * the content in a read-only text field.
	 *
	 * @param level  The log level of this method call
	 * @param title  The title to display in the Console
	 * @param reader The reader to log
	 */
	public void logTextReader(Level level, String title, Reader reader) {
		logCustomReader(level, title, reader, LogEntryType.Text,
				ViewerId.Data);
	}

	/**
	 * Overloaded. Logs HTML code with the default log level and displays
	 * it in a web browser. This method logs the supplied HTML source code. The source
	 * code is displayed as a website in the web viewer of the Console.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title The title to display in the Console
	 * @param html  The HTML source code to display
	 */
	public void logHtml(String title, String html) {
		logHtml(this.fParent.getDefaultLevel(), title, html);
	}

	/**
	 * Overloaded. Logs HTML code with a custom log level and displays
	 * it in a web browser. This method logs the supplied HTML source code. The source
	 * code is displayed as a website in the web viewer of the Console.
	 *
	 * @param level The log level of this method call
	 * @param title The title to display in the Console
	 * @param html  The HTML source code to display
	 */
	public void logHtml(Level level, String title, String html) {
		logCustomText(level, title, html, LogEntryType.WebContent,
				ViewerId.Web);
	}

	/**
	 * Overloaded. Logs an HTML file with the default log level and
	 * displays the content in a web browser.
	 * <p>
	 * This method logs the HTML source code of the supplied file. The
	 * source code is displayed as a website in the web viewer of the
	 * Console.
	 * <p>
	 * This version of the method uses the fileName argument as title to
	 * display in the Console.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param fileName The HTML file to display
	 */
	public void logHtmlFile(String fileName) {
		logHtmlFile(this.fParent.getDefaultLevel(), fileName);
	}

	/**
	 * Overloaded. Logs an HTML file with a custom log level and displays
	 * the content in a web browser.
	 *
	 * <p>This method logs the HTML source code of the supplied file. The
	 * source code is displayed as a website in the web viewer of the
	 * Console.
	 *
	 * <p>This version of the method uses the fileName argument as title to
	 * display in the Console.
	 *
	 * @param level    The log level of this method call
	 * @param fileName The HTML file to display
	 */
	public void logHtmlFile(Level level, String fileName) {
		logCustomFile(level, fileName, LogEntryType.WebContent, ViewerId.Web);
	}

	/**
	 * Overloaded. Logs an HTML file and displays the content in a web
	 * browser using a custom title and default log level.
	 * <p>
	 * This method logs the HTML source code of the supplied file. The
	 * source code is displayed as a website in the web viewer of the
	 * Console.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title    The title to display in the Console
	 * @param fileName The HTML file to display
	 */
	public void logHtmlFile(String title, String fileName) {
		logHtmlFile(this.fParent.getDefaultLevel(), title, fileName);
	}

	/**
	 * Overloaded. Logs an HTML file and displays the content in a web browser
	 * using a custom title and custom log level.
	 * <p>
	 * This method logs the HTML source code of the supplied file. The
	 * source code is displayed as a website in the web viewer of the
	 * Console.
	 *
	 * @param level    The log level of this method call
	 * @param title    The title to display in the Console
	 * @param fileName The HTML file to display
	 */
	public void logHtmlFile(Level level, String title, String fileName) {
		logCustomFile(level, title, fileName, LogEntryType.WebContent,
				ViewerId.Web);
	}

	/**
	 * Overloaded. Logs a stream with the default log level and displays
	 * the content in a web browser.
	 *
	 * <p>This method logs the HTML source code of the supplied stream.
	 * The source code is displayed as a website in the web viewer of
	 * the Console.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method
	 * of the SmartInspect class.</p>
	 *
	 * @param title  The title to display in the Console
	 * @param stream The stream to display
	 */
	public void logHtmlStream(String title, InputStream stream) {
		logHtmlStream(this.fParent.getDefaultLevel(), title, stream);
	}

	/**
	 * Overloaded. Logs a stream with a custom log level and displays the content in a web browser.
	 *
	 * <p>This method logs the HTML source code of the supplied stream.
	 * The source code is displayed as a website in the web viewer of the Console.</p>
	 *
	 * @param level  The log level of this method call
	 * @param title  The title to display in the Console
	 * @param stream The stream to display
	 */
	public void logHtmlStream(Level level, String title, InputStream stream) {
		logCustomStream(level, title, stream, LogEntryType.WebContent,
				ViewerId.Web);
	}

	/**
	 * Overloaded. Logs a reader with the default log level and displays
	 * the content in a web browser.
	 * <p>
	 * This method logs the HTML source code of the supplied reader.
	 * The source code is displayed as a website in the web viewer of
	 * the Console.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title  The title to display in the Console
	 * @param reader The reader to display
	 */
	public void logHtmlReader(String title, Reader reader) {
		logHtmlReader(this.fParent.getDefaultLevel(), title, reader);
	}

	/**
	 * Overloaded. Logs a reader with a custom log level and displays
	 * the content in a web browser.
	 * <p>
	 * This method logs the HTML source code of the supplied reader.
	 * The source code is displayed as a website in the web viewer of
	 * the Console.
	 *
	 * @param level  The log level of this method call
	 * @param title  The title to display in the Console
	 * @param reader The reader to display
	 */
	public void logHtmlReader(Level level, String title, Reader reader) {
		logCustomReader(level, title, reader, LogEntryType.WebContent,
				ViewerId.Web);
	}

	/**
	 * Overloaded. Logs a byte array with the default log level and displays it in a hex viewer.
	 *
	 * <p>
	 * This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of the setDefaultLevel method of the SmartInspect class.
	 * </p>
	 *
	 * @param title The title to display in the Console
	 * @param b     The byte array to display in the hex viewer
	 */
	public void logBinary(String title, byte[] b) {
		logBinary(this.fParent.getDefaultLevel(), title, b);
	}

	/**
	 * Overloaded. Logs a byte array with a custom log level and displays
	 * it in a hex viewer.
	 *
	 * @param level The log level of this method call
	 * @param title The title to display in the Console
	 * @param b     The byte array to display in the hex viewer
	 */
	public void logBinary(Level level, String title, byte[] b) {
		if (isOn(level)) {
			BinaryViewerContext ctx = new BinaryViewerContext();
			try {
				try {
					ctx.appendBytes(b);
					sendContext(level, title, LogEntryType.Binary, ctx);
				} catch (Exception e) {
					logInternalError("logBinary: " + e.getMessage());
				}
			} finally {
				ctx.close();
			}
		}
	}

	/**
	 * Logs a byte array with the default log level and displays it in a hex viewer.
	 * The input byte array is displayed in a hex viewer along with the specified title.
	 * The display starts from the given offset and displays the specified amount of bytes.
	 * This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of the setDefaultLevel method
	 * of the SmartInspect class.
	 *
	 * @param title The title to display in the Console
	 * @param b     The byte array to display in the hex viewer
	 * @param off   The offset at which to display data from
	 * @param len   The amount of bytes to display
	 */
	public void logBinary(String title, byte[] b, int off, int len) {
		logBinary(this.fParent.getDefaultLevel(), title, b, off, len);
	}

	/**
	 * Overloaded. Logs a byte array with a custom log level and displays
	 * it in a hex viewer.
	 *
	 * @param level The log level of this method call
	 * @param title The title to display in the Console
	 * @param b     The byte array to display in the hex viewer
	 * @param off   The offset at which to display data from
	 * @param len   The amount of bytes to display
	 */
	public void logBinary(Level level, String title, byte[] b, int off,
						  int len) {
		if (isOn(level)) {
			BinaryViewerContext ctx = new BinaryViewerContext();
			try {
				try {
					ctx.appendBytes(b, off, len);
					sendContext(level, title, LogEntryType.Binary, ctx);
				} catch (Exception e) {
					logInternalError("logBinary: " + e.getMessage());
				}
			} finally {
				ctx.close();
			}
		}
	}

	/**
	 * Overloaded. Logs a binary file with the default log level and displays its content in a hex viewer.
	 * <p>
	 * This version of the method uses the supplied fileName argument as title to display in the Console.
	 * This method uses the default level of the session's parent as log level. For more information,
	 * please refer to the documentation of the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param fileName The binary file to display in a hex viewer
	 */
	public void logBinaryFile(String fileName) {
		logBinaryFile(this.fParent.getDefaultLevel(), fileName);
	}

	/**
	 * Overloaded. Logs a binary file with a custom log level and displays
	 * its content in a hex viewer.
	 * This version of the method uses the supplied fileName argument as
	 * title to display in the Console.
	 *
	 * @param level    The log level of this method call
	 * @param fileName The binary file to display in a hex viewer
	 */
	public void logBinaryFile(Level level, String fileName) {
		logCustomFile(level, fileName, LogEntryType.Binary, ViewerId.Binary);
	}

	/**
	 * Logs a binary file and displays its content in a hex
	 * viewer using a custom title and default log level.
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title    The title to display in the Console
	 * @param fileName The binary file to display in a hex viewer
	 */
	public void logBinaryFile(String title, String fileName) {
		logBinaryFile(this.fParent.getDefaultLevel(), title, fileName);
	}

	/**
	 * Overloaded. Logs a binary file and displays its content in a hex
	 * viewer using a custom title and custom log level.
	 *
	 * @param level    The log level of this method call
	 * @param title    The title to display in the Console
	 * @param fileName The binary file to display in a hex viewer
	 */
	public void logBinaryFile(Level level, String title, String fileName) {
		logCustomFile(level, title, fileName, LogEntryType.Binary,
				ViewerId.Binary);
	}

	/**
	 * Overloaded. Logs a binary stream with the default log level and
	 * displays its content in a hex viewer.
	 *
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the {@link SmartInspect#setDefaultLevel} method
	 * of the SmartInspect class.
	 * </p>
	 *
	 * @param title  The title to display in the Console
	 * @param stream The binary stream to display in a hex viewer
	 */
	public void logBinaryStream(String title, InputStream stream) {
		logBinaryStream(this.fParent.getDefaultLevel(), title, stream);
	}

	/**
	 * Overloaded. Logs a binary stream with a custom log level and
	 * displays its content in a hex viewer.
	 *
	 * @param level  The log level of this method call
	 * @param title  The title to display in the Console
	 * @param stream The binary stream to display in a hex viewer
	 */
	public void logBinaryStream(Level level, String title, InputStream stream) {
		logCustomStream(level, title, stream, LogEntryType.Binary,
				ViewerId.Binary);
	}

	/**
	 * Logs a bitmap file with the default log level and
	 * displays it in the Console.
	 * <p>
	 * This version of the method uses the supplied fileName argument as
	 * title to display in the Console.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param fileName The bitmap file to display in the Console
	 */
	public void logBitmapFile(String fileName) {
		logBitmapFile(this.fParent.getDefaultLevel(), fileName);
	}

	/**
	 * Logs a bitmap file with a custom log level and displays it in the Console.
	 * This version of the method uses the supplied fileName argument as title to display in the Console.
	 *
	 * @param level    The log level of this method call
	 * @param fileName The bitmap file to display in the Console
	 */
	public void logBitmapFile(Level level, String fileName) {
		logCustomFile(level, fileName, LogEntryType.Graphic, ViewerId.Bitmap);
	}

	/**
	 * Overloaded. Logs a bitmap file and displays it in the Console using
	 * a custom title and default log level.
	 * <br>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title    The title to display in the Console
	 * @param fileName The bitmap file to display in the Console
	 */
	public void logBitmapFile(String title, String fileName) {
		logBitmapFile(this.fParent.getDefaultLevel(), title, fileName);
	}

	/**
	 * Overloaded. Logs a bitmap file and displays it in the Console using
	 * a custom title and custom log level.
	 *
	 * @param level    The log level of this method call
	 * @param title    The title to display in the Console
	 * @param fileName The bitmap file to display in the Console
	 */
	public void logBitmapFile(Level level, String title, String fileName) {
		logCustomFile(level, title, fileName, LogEntryType.Graphic,
				ViewerId.Bitmap);
	}

	/**
	 * Logs a stream with the default log level and interprets its content as a bitmap.
	 * This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of the setDefaultLevel method
	 * of the SmartInspect class.
	 *
	 * @param title  The title to display in the Console
	 * @param stream The stream to display as bitmap
	 */
	public void logBitmapStream(String title, InputStream stream) {
		logBitmapStream(this.fParent.getDefaultLevel(), title, stream);
	}

	/**
	 * Overloaded. Logs a stream with a custom log level and interprets
	 * its content as a bitmap.
	 *
	 * @param level  The log level of this method call
	 * @param title  The title to display in the Console
	 * @param stream The stream to display as bitmap
	 */
	public void logBitmapStream(Level level, String title, InputStream stream) {
		logCustomStream(level, title, stream, LogEntryType.Graphic,
				ViewerId.Bitmap);
	}

	/**
	 * Overloaded. Logs a JPEG file with the default log level and displays
	 * it in the Console.
	 * <p>
	 * This version of the method uses the supplied fileName argument as
	 * title to display in the Console.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param fileName The JPEG file to display in the Console
	 */
	public void logJpegFile(String fileName) {
		logJpegFile(this.fParent.getDefaultLevel(), fileName);
	}

	/**
	 * Overloaded. Logs a JPEG file with a custom log level and displays
	 * it in the Console.
	 * This version of the method uses the supplied fileName argument as
	 * title to display in the Console.
	 *
	 * @param level    The log level of this method call
	 * @param fileName The JPEG file to display in the Console
	 */
	public void logJpegFile(Level level, String fileName) {
		logCustomFile(level, fileName, LogEntryType.Graphic, ViewerId.Jpeg);
	}

	/**
	 * Overloaded. Logs a JPEG file and displays it in the Console using
	 * a custom title and default log level.
	 * This method uses the SmartInspect.setDefaultLevel, default level of the session's getParent, parent as log
	 * level. For more information, please refer to the documentation of
	 * the SmartInspect.setDefaultLevel, setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title    The title to display in the Console
	 * @param fileName The JPEG file to display in the Console
	 */
	public void logJpegFile(String title, String fileName) {
		logJpegFile(this.fParent.getDefaultLevel(), title, fileName);
	}

	/**
	 * Overloaded. Logs a JPEG file and displays it in the Console using
	 * a custom title and custom log level.
	 *
	 * @param level    The log level of this method call
	 * @param title    The title to display in the Console
	 * @param fileName The JPEG file to display in the Console
	 */
	public void logJpegFile(Level level, String title, String fileName) {
		logCustomFile(level, title, fileName, LogEntryType.Graphic,
				ViewerId.Jpeg);
	}

	/**
	 * Overloaded. Logs a stream with the default log level and interprets
	 * its content as a JPEG image.
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.</p>
	 *
	 * @param title  The title to display in the Console
	 * @param stream The stream to display as JPEG image
	 */
	public void logJpegStream(String title, InputStream stream) {
		logJpegStream(this.fParent.getDefaultLevel(), title, stream);
	}

	/**
	 * Overloaded. Logs a stream with a custom log level and interprets
	 * its content as a JPEG image.
	 *
	 * @param level  The log level of this method call
	 * @param title  The title to display in the Console
	 * @param stream The stream to display as JPEG image
	 */
	public void logJpegStream(Level level, String title, InputStream stream) {
		logCustomStream(level, title, stream, LogEntryType.Graphic,
				ViewerId.Jpeg);
	}

	/**
	 * Overloaded. Logs a Windows icon file with the default log level
	 * and displays it in the Console.
	 *
	 * <p>This version of the method uses the supplied fileName argument as
	 * title to display in the Console.
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param fileName The Windows icon file to display in the Console
	 */
	public void logIconFile(String fileName) {
		logIconFile(this.fParent.getDefaultLevel(), fileName);
	}

	/**
	 * Overloaded. Logs a Windows icon file with a custom log level and
	 * displays it in the Console.
	 * <p>
	 * This version of the method uses the supplied fileName argument as
	 * title to display in the Console.
	 *
	 * @param level    The log level of this method call
	 * @param fileName The Windows icon file to display in the Console
	 */
	public void logIconFile(Level level, String fileName) {
		logCustomFile(level, fileName, LogEntryType.Graphic, ViewerId.Icon);
	}

	/**
	 * Overloaded. Logs a Windows icon file and displays it in the Console using a custom title and default log level.
	 * This method uses the default level of the session's parent as log level. For more information,
	 * please refer to the documentation of the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title    The title to display in the Console
	 * @param fileName The Windows icon file to display in the Console
	 */
	public void logIconFile(String title, String fileName) {
		logIconFile(this.fParent.getDefaultLevel(), title, fileName);
	}

	/**
	 * Overloaded. Logs a Windows icon file and displays it in the Console
	 * using a custom title and custom log level.
	 *
	 * @param level    The log level of this method call
	 * @param title    The title to display in the Console
	 * @param fileName The Windows icon file to display in the Console
	 */
	public void logIconFile(Level level, String title, String fileName) {
		logCustomFile(level, title, fileName, LogEntryType.Graphic,
				ViewerId.Icon);
	}

	/**
	 * Overloaded. Logs a stream with the default log level and interprets
	 * its content as Windows icon.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method
	 * of the SmartInspect class.
	 *
	 * @param title  The title to display in the Console
	 * @param stream The stream to display as Windows icon
	 */
	public void logIconStream(String title, InputStream stream) {
		logIconStream(this.fParent.getDefaultLevel(), title, stream);
	}

	/**
	 * Logs a stream with a custom log level and interprets its content as Windows icon.
	 *
	 * @param level  The log level of this method call
	 * @param title  The title to display in the Console
	 * @param stream The stream to display as Windows icon
	 */
	public void logIconStream(Level level, String title, InputStream stream) {
		logCustomStream(level, title, stream, LogEntryType.Graphic,
				ViewerId.Icon);
	}

	/**
	 * Overloaded. Logs a Windows Metafile file with the default log
	 * level and displays it in the Console.
	 *
	 * <p>This version of the method uses the supplied fileName argument as
	 * title to display in the Console.
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param fileName The Windows Metafile file to display in the Console
	 */
	public void logMetafileFile(String fileName) {
		logMetafileFile(this.fParent.getDefaultLevel(), fileName);
	}

	/**
	 * Overloaded. Logs a Windows Metafile file with a custom log level
	 * and displays it in the Console.
	 * This version of the method uses the supplied fileName argument as
	 * title to display in the Console.
	 *
	 * @param level    The log level of this method call
	 * @param fileName The Windows Metafile file to display in the Console
	 */
	public void logMetafileFile(Level level, String fileName) {
		logCustomFile(level, fileName, LogEntryType.Graphic,
				ViewerId.Metafile);
	}

	/**
	 * Logs a Windows Metafile file and displays it in the console using a custom title and default log level.
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title    The title to display in the console
	 * @param fileName The Windows Metafile file to display in the console
	 */
	public void logMetafileFile(String title, String fileName) {
		logMetafileFile(this.fParent.getDefaultLevel(), title, fileName);
	}

	/**
	 * Overloaded. Logs a Windows Metafile file and displays it in the
	 * Console using a custom title and custom log level.
	 *
	 * @param level    The log level of this method call
	 * @param title    The title to display in the Console
	 * @param fileName The Windows Metafile file to display in the Console
	 */
	public void logMetafileFile(Level level, String title, String fileName) {
		logCustomFile(level, title, fileName, LogEntryType.Graphic,
				ViewerId.Metafile);
	}

	/**
	 * Overloaded. Logs a stream with the default log level and interprets
	 * its content as Windows Metafile image.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title  The title to display in the Console
	 * @param stream The stream to display as Windows Metafile image
	 */
	public void logMetafileStream(String title, InputStream stream) {
		logMetafileStream(this.fParent.getDefaultLevel(), title, stream);
	}

	/**
	 * Overloaded. Logs a stream with a custom log level and interprets
	 * its content as Windows Metafile image.
	 *
	 * @param level  The log level of this method call
	 * @param title  The title to display in the Console
	 * @param stream The stream to display as Windows Metafile image
	 */
	public void logMetafileStream(Level level, String title,
								  InputStream stream) {
		logCustomStream(level, title, stream, LogEntryType.Graphic,
				ViewerId.Metafile);
	}

	/**
	 * Overloaded. Logs a string containing SQL source code with the
	 * default log level. The SQL source code is displayed with syntax
	 * highlighting in the Console.
	 *
	 * <p>This method displays the supplied SQL source code with syntax
	 * highlighting in the Console.
	 *
	 * <p>It is especially useful to debug or track dynamically generated
	 * SQL source code.
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title  The title to display in the Console
	 * @param source The SQL source code to log
	 */
	public void logSql(String title, String source) {
		logSql(this.fParent.getDefaultLevel(), title, source);
	}

	/**
	 * Overloaded. Logs a string containing SQL source code with a custom
	 * log level. The SQL source code is displayed with syntax highlighting
	 * in the Console.
	 * <p>
	 * This method displays the supplied SQL source code with syntax
	 * highlighting in the Console. It is especially useful to debug or track
	 * dynamically generated SQL source code.
	 *
	 * @param level  The log level of this method call
	 * @param title  The title to display in the Console
	 * @param source The SQL source code to log
	 */
	public void logSql(Level level, String title, String source) {
		logSource(level, title, source, SourceId.Sql);
	}

	/**
	 * Overloaded. Logs source code that is displayed with syntax
	 * highlighting in the Console using the default log level.
	 *
	 * <p>This method displays the supplied source code with syntax
	 * highlighting in the Console. The type of the source code can
	 * be specified by the 'id' argument. Please see the SourceId
	 * documentation for information on the supported source code
	 * types.</p>
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method
	 * of the SmartInspect class.</p>
	 *
	 * @param title  The title to display in the Console
	 * @param source The source code to log
	 * @param id     Specifies the type of source code
	 */
	public void logSource(String title, String source, SourceId id) {
		logSource(this.fParent.getDefaultLevel(), title, source, id);
	}

	/**
	 * Overloaded method, logs source code that is displayed with syntax highlighting in the Console using a custom log level.
	 * This method displays the supplied source code with syntax highlighting in the Console. The type of the source code can be specified by the 'id' argument. Please see the SourceId documentation for information on the supported source code types.
	 *
	 * @param level  The log level of this method call
	 * @param title  The title to display in the Console
	 * @param source The source code to log
	 * @param id     Specifies the type of source code
	 */
	public void logSource(Level level, String title, String source,
						  SourceId id) {
		if (isOn(level)) {
			if (id == null) {
				logInternalError("logSource: id argument is null");
			} else {
				logCustomText(level, title, source, LogEntryType.Source,
						id.toViewerId());
			}
		}
	}

	/**
	 * Overloaded. Logs the content of a file as source code with
	 * syntax highlighting using the default log level.
	 *
	 * <p>This method displays the source file with syntax highlighting
	 * in the Console. The type of the source code can be specified by
	 * the 'id' argument. Please see the SourceId documentation for
	 * information on the supported source code types.</p>
	 *
	 * <p>This version of the method uses the supplied fileName argument
	 * as title to display in the Console.</p>
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.</p>
	 *
	 * @param fileName The file which contains the source code
	 * @param id       Specifies the type of source code
	 */
	public void logSourceFile(String fileName, SourceId id) {
		logSourceFile(this.fParent.getDefaultLevel(), fileName, id);
	}

	/**
	 * Overloaded. Logs the content of a file as source code with
	 * syntax highlighting using a custom log level.
	 *
	 * <p>This method displays the source file with syntax highlighting
	 * in the Console. The type of the source code can be specified by
	 * the 'id' argument. Please see the SourceId documentation for
	 * information on the supported source code types.
	 *
	 * <p>This version of the method uses the supplied fileName argument
	 * as title to display in the Console.
	 *
	 * @param level    The log level of this method call
	 * @param fileName The file which contains the source code
	 * @param id       Specifies the type of source code
	 */
	public void logSourceFile(Level level, String fileName, SourceId id) {
		logSourceFile(level, fileName, fileName, id);
	}

	/**
	 * Overloaded. Logs the content of a file as source code with syntax
	 * highlighting using a custom title and default log level.
	 * This method displays the source file with syntax highlighting
	 * in the Console. The type of the source code can be specified by
	 * the 'id' argument. Please see the SourceId documentation for
	 * information on the supported source code types.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title    The title to display in the Console
	 * @param fileName The file which contains the source code
	 * @param id       Specifies the type of source code
	 */
	public void logSourceFile(String title, String fileName, SourceId id) {
		logSourceFile(this.fParent.getDefaultLevel(), title, fileName, id);
	}

	/**
	 * Overloaded. Logs the content of a file as source code with syntax
	 * highlighting using a custom title and custom log level.
	 * <p>
	 * This method displays the source file with syntax highlighting
	 * in the Console. The type of the source code can be specified by
	 * the 'id' argument. Please see the SourceId documentation for
	 * information on the supported source code types.
	 *
	 * @param level    The log level of this method call
	 * @param title    The title to display in the Console
	 * @param fileName The file which contains the source code
	 * @param id       Specifies the type of source code
	 */
	public void logSourceFile(Level level, String title, String fileName,
							  SourceId id) {
		if (isOn(level)) {
			if (id == null) {
				logInternalError("logSourceFile: id argument is null");
			} else {
				logCustomFile(level, title, fileName, LogEntryType.Source,
						id.toViewerId());
			}
		}
	}

	/**
	 * Overloaded. Logs the content of a stream as source code with syntax
	 * highlighting using the default log level.
	 * <p>
	 * This method displays the content of a stream with syntax
	 * highlighting in the Console. The type of the source code can
	 * be specified by the 'id' argument. Please see the SourceId
	 * documentation for information on the supported source code types.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title  The title to display in the Console
	 * @param stream The stream which contains the source code
	 * @param id     Specifies the type of source code
	 */
	public void logSourceStream(String title, InputStream stream, SourceId id) {
		logSourceStream(this.fParent.getDefaultLevel(), title, stream, id);
	}

	/**
	 * Overloaded. Logs the content of a stream as source code with syntax
	 * highlighting using a custom log level.
	 * <p>
	 * This method displays the content of a stream with syntax
	 * highlighting in the Console. The type of the source code can
	 * be specified by the 'id' argument. Please see the SourceId
	 * documentation for information on the supported source code types.
	 *
	 * @param level  The log level of this method call
	 * @param title  The title to display in the Console
	 * @param stream The stream which contains the source code
	 * @param id     Specifies the type of source code
	 */
	public void logSourceStream(Level level, String title, InputStream stream,
								SourceId id) {
		if (isOn(level)) {
			if (id == null) {
				logInternalError("logSourceStream: id argument is null");
			} else {
				logCustomStream(level, title, stream, LogEntryType.Source,
						id.toViewerId());
			}
		}
	}

	/**
	 * Overloaded. Logs the content of a reader as source code with syntax
	 * highlighting using the default log level.
	 *
	 * <p>This method displays the content of a reader with syntax
	 * highlighting in the Console. The type of the source code can
	 * be specified by the 'id' argument. Please see the SourceId
	 * documentation for information on the supported source code types.</p>
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.</p>
	 *
	 * @param title  The title to display in the Console
	 * @param reader The reader which contains the source code
	 * @param id     Specifies the type of source code
	 */
	public void logSourceReader(String title, Reader reader, SourceId id) {
		logSourceReader(this.fParent.getDefaultLevel(), title, reader, id);
	}

	/**
	 * Overloaded. Logs the content of a reader as source code with syntax
	 * highlighting using a custom log level.
	 * <p>
	 * This method displays the content of a reader with syntax
	 * highlighting in the Console. The type of the source code can
	 * be specified by the 'id' argument. Please see the SourceId
	 * documentation for information on the supported source code types.
	 *
	 * @param level  The log level of this method call
	 * @param title  The title to display in the Console
	 * @param reader The reader which contains the source code
	 * @param id     Specifies the type of source code
	 */
	public void logSourceReader(Level level, String title, Reader reader,
								SourceId id) {
		if (isOn(level)) {
			if (id == null) {
				logInternalError("logSourceReader: id argument is null");
			} else {
				logCustomReader(level, title, reader, LogEntryType.Source,
						id.toViewerId());
			}
		}
	}

	/**
	 * Overloaded. Logs the public fields of an object with the default log level.
	 * <p>This method logs all public field names and their current values of an object.
	 * These key/value pairs will be displayed in the Console in an object inspector-like viewer.
	 * <p>This method uses the default level of the session's parent as log level. For more information,
	 * please refer to the documentation of the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title    The title to display in the Console
	 * @param instance The object whose public fields should be logged
	 */
	public void logObject(String title, Object instance) {
		logObject(this.fParent.getDefaultLevel(), title, instance);
	}

	/**
	 * Overloaded. Logs the public fields of an object with a custom log level.
	 * <p>
	 * This method logs all public field names and their current values
	 * of an object. These key/value pairs will be displayed in the Console
	 * in an object inspector like viewer.
	 *
	 * @param level    The log level of this method call
	 * @param title    The title to display in the Console
	 * @param instance The object whose public fields should be logged
	 */
	public void logObject(Level level, String title, Object instance) {
		logObject(level, title, instance, false);
	}

	/**
	 * Overloaded method that logs fields of an object with the default log level.
	 * This method allows specifying whether non-public members should also be logged.
	 * This method logs all field names and their current values of an object.
	 * These key/value pairs will be displayed in the Console in an object inspector-like viewer.
	 * <p>
	 * You can specify if non-public or only public fields should be logged by setting the nonPublic argument to true or false,
	 * respectively. This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title     The title to display in the Console
	 * @param instance  The object whose fields should be logged
	 * @param nonPublic Specifies if non-public members should be logged
	 */
	public void logObject(String title, Object instance, boolean nonPublic) {
		logObject(this.fParent.getDefaultLevel(), title, instance, nonPublic);
	}

	/**
	 * Overloaded. Logs fields of an object with a custom log level.
	 * Lets you specify if non-public members should also be logged.
	 * <p>
	 * This method logs all field names and their current values of an
	 * object. These key/value pairs will be displayed in the Console
	 * in an object inspector like viewer.
	 * You can specify if non public or only public fields should
	 * be logged by setting the nonPublic argument to true or false,
	 * respectively.
	 * </p>
	 *
	 * @param level     The log level of this method call
	 * @param title     The title to display in the Console
	 * @param instance  The object whose fields should be logged
	 * @param nonPublic Specifies if non-public members should be logged
	 */
	public void logObject(Level level, String title, Object instance,
						  boolean nonPublic) {
		if (!isOn(level)) {
			return;
		}

		if (instance == null) {
			logInternalError("logObject: instance argument is null");
			return;
		}

		Field[] fields;
		Class cls = instance.getClass();
		ArrayList list = new ArrayList();

		InspectorViewerContext ctx = new InspectorViewerContext();

		while (cls != null) {
			try {
				// Try to get the fields of the object.
				fields = nonPublic ? cls.getDeclaredFields() : cls.getFields();
				AccessibleObject.setAccessible(fields, true);
			} catch (SecurityException e) {
				logInternalError("logObject: Can not change field access");
				return;
			}

			StringBuffer sb = new StringBuffer(32);
			for (int i = 0; i < fields.length; i++) {
				int mod = fields[i].getModifiers();

				if (!Modifier.isStatic(mod)) {
					try {
						sb.append(ctx.escapeItem(fields[i].getName()));
						sb.append("=");
						sb.append(ctx.escapeItem(
								ObjectRenderer.renderObject(fields[i].get(instance)))
						);
					} catch (IllegalAccessException e) {
						sb.append("<not accessible>");
					}

					list.add(sb.toString());
					sb.setLength(0);
				}
			}

			cls = cls.getSuperclass();
		}

		Collections.sort(list);

		// Fill the inspector viewer context.
		try {
			ctx.startGroup("Fields");

			if (!list.isEmpty()) {
				Iterator it = list.iterator();
				while (it.hasNext()) {
					ctx.appendLine((String) it.next());
				}
			}

			sendContext(level, title, LogEntryType.Object, ctx);
		} catch (Exception e) {
			logInternalError("logObject: " + e.getMessage());
		}
	}

	/**
	 * Logs the content of an exception with a log level of Level.Error.
	 * <p>
	 * This method extracts the exception message and stack trace from the supplied exception
	 * and logs an error with this data. It is especially useful if you place calls to this
	 * method in exception handlers, of course. See logError for a more general method with a
	 * similar intention.
	 * <p>
	 * This version of the method uses the exception message as title to display in the Console.
	 *
	 * @param t The exception to log
	 */
	public void logException(Throwable t) {
		if (isOn(Level.Error)) {
			if (t == null) {
				logInternalError("logException: t argument is null");
			} else {
				logException(t.getMessage(), t);
			}
		}
	}

	/**
	 * Overloaded. Logs the content of an exception with a log level of
	 * Level.Error and a custom title.
	 * <p>
	 * This method extracts the exception message and stack trace
	 * from the supplied exception and logs an error with this data.
	 * It is especially useful if you place calls to this method in
	 * exception handlers, of course. See logError for a more general
	 * method with a similar intention.
	 *
	 * @param title The title to display in the Console
	 * @param t     The exception to log
	 */
	public void logException(String title, Throwable t) {
		if (isOn(Level.Error)) {
			if (t == null) {
				logInternalError("logException: t argument is null");
				return;
			}

			DataViewerContext ctx = new DataViewerContext();
			try {
				try {
					StringWriter writer = new StringWriter();
					try {
						// Write the stack trace to the writer..
						t.printStackTrace(new PrintWriter(writer, true));

						// And then fill and send the context accordingly.
						ctx.loadFromText(writer.toString());
						sendContext(Level.Error, title, LogEntryType.Error,
								ctx);
					} finally {
						writer.close();
					}
				} catch (Exception e) {
					logInternalError("logException: " + e.getMessage());
				}
			} finally {
				ctx.close();
			}
		}
	}

	private static String bytesToString(long n) {
		int i = 0;
		double d = (double) n;

		while (d >= 1024 && i <= 3) {
			d /= 1024;
			i++;
		}

		String s = MessageFormat.format(
				"{0,number,#.##}", new Object[]{new Double(d)}
		);

		switch (i) {
			case 0:
				s += " Bytes";
				break;

			case 1:
				s += " KB";
				break;

			case 2:
				s += " MB";
				break;

			case 3:
				s += " GB";
				break;
		}

		return s;
	}

	/**
	 * Overloaded. Logs memory statistics about the virtual machine with the default log level.
	 * This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of the setDefaultLevel method
	 * of the SmartInspect class.
	 */
	public void logMemoryStatistic() {
		logMemoryStatistic(this.fParent.getDefaultLevel());
	}

	/**
	 * Overloaded. Logs memory statistics about the virtual machine with
	 * a custom log level.
	 *
	 * @param level The log level of this method call
	 */
	public void logMemoryStatistic(Level level) {
		logMemoryStatistic(level, "Memory statistic");
	}

	/**
	 * Logs memory statistics about the virtual machine
	 * using a custom title and default log level.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title The title to display in the Console
	 */
	public void logMemoryStatistic(String title) {
		logMemoryStatistic(this.fParent.getDefaultLevel(), title);
	}

	/**
	 * Overloaded. Logs memory statistics about the virtual machine
	 * using a custom title and default log level.
	 *
	 * @param level The log level of this method call
	 * @param title The title to display in the Console
	 */
	public void logMemoryStatistic(Level level, String title) {
		if (isOn(level)) {
			ValueListViewerContext ctx = new ValueListViewerContext();
			try {
				try {
					Runtime rt = Runtime.getRuntime();

					ctx.appendKeyValue("Free memory",
							bytesToString(rt.freeMemory()));
					ctx.appendKeyValue("Maximal memory",
							bytesToString(rt.maxMemory()));
					ctx.appendKeyValue("Total memory",
							bytesToString(rt.totalMemory()));

					sendContext(level, title, LogEntryType.MemoryStatistic,
							ctx);
				} catch (Exception e) {
					logInternalError("logMemoryStatistic: " + e.getMessage());
				}
			} finally {
				ctx.close();
			}
		}
	}

	/**
	 * Overloaded. Logs information about the current thread with the default log level.
	 *
	 * <p>This method logs information about the current thread. This includes its name,
	 * its current state and more.</p>
	 *
	 * <p>logCurrentThread is especially useful in a multi-threaded program like in a network server
	 * application. By using this method you can easily track all threads of a process and
	 * obtain detailed information about them.</p>
	 *
	 * <p>See logThread for a more general method, which can handle any thread.</p>
	 *
	 * <p>This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of the setDefaultLevel method
	 * of the SmartInspect class.</p>
	 */
	public void logCurrentThread() {
		logCurrentThread(this.fParent.getDefaultLevel());
	}

	/**
	 * <p>Logs information about the current thread with a custom log level.</p>
	 *
	 * <p>This method logs information about the current thread. This includes its name, its current state and more.</p>
	 *
	 * <p>logCurrentThread is especially useful in a multi-threaded program like in a network server application.
	 * By using this method you can easily track all threads of a process and obtain detailed information about them.</p>
	 *
	 * <p>See logThread for a more general method, which can handle any thread.</p>
	 *
	 * @param level The log level of this method call
	 */
	public void logCurrentThread(Level level) {
		if (isOn(level)) {
			String title = "Current thread";
			Thread thread = Thread.currentThread();
			String name = thread.getName();

			if (name != null && name.length() > 0) {
				// Append the thread name to the title.
				title += ": " + name;
			}

			// Just call logThread with the
			// current thread and our own title.
			logThread(level, title, thread);
		}
	}

	/**
	 * Logs information about the current thread using a
	 * custom title and the default log level.
	 * <br>
	 * This method logs information about the current thread. This
	 * includes its name, its current state and more.
	 * <br>
	 * logCurrentThread is especially useful in a multithreaded
	 * program like in a network server application. By using this
	 * method you can easily track all threads of a process and
	 * obtain detailed information about them.
	 * <br>
	 * See logThread for a more general method, which can handle any
	 * thread.
	 * <br>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 * <br>
	 *
	 * @param title The title to display in the Console
	 */
	public void logCurrentThread(String title) {
		logCurrentThread(this.fParent.getDefaultLevel(), title);
	}

	/**
	 * Overloaded method that logs information about the current thread using a
	 * custom title and a custom log level.
	 * <p>
	 * This method logs information about the current thread. This
	 * includes its name, its current state and more.
	 * <p>
	 * logCurrentThread is especially useful in a multithreaded
	 * program like in a network server application. By using this
	 * method you can easily track all threads of a process and
	 * obtain detailed information about them.
	 * <p>
	 * See logThread for a more general method, which can handle any
	 * thread.
	 *
	 * @param level The log level of this method call
	 * @param title The title to display in the Console
	 */
	public void logCurrentThread(Level level, String title) {
		if (isOn(level)) {
			// Just call logThread with the current thread.
			logThread(level, title, Thread.currentThread());
		}
	}

	/**
	 * This method logs information about the supplied thread. This includes its
	 * name, its current state and more. logThread is especially useful in a multi-threaded
	 * program like in a network server application. By using this method you can easily
	 * track all threads of a process and obtain detailed information about them.
	 * <p>
	 * This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title  The title to display in the Console
	 * @param thread The thread to log
	 */
	public void logThread(String title, Thread thread) {
		logThread(this.fParent.getDefaultLevel(), title, thread);
	}

	/**
	 * Overloaded. Logs information about a thread with a custom log level.
	 * <p>
	 * This method logs information about the supplied thread. This
	 * includes its name, its current state and more.
	 * <p>
	 * logThread is especially useful in a multithreaded program
	 * like in a network server application. By using this method you
	 * can easily track all threads of a process and obtain detailed
	 * information about them.
	 *
	 * @param level  The log level of this method call
	 * @param title  The title to display in the Console
	 * @param thread The thread to log
	 */
	public void logThread(Level level, String title, Thread thread) {
		if (isOn(level)) {
			if (thread == null) {
				logInternalError("logThread: thread argument is null");
				return;
			}

			ValueListViewerContext ctx = new ValueListViewerContext();
			try {
				try {
					ctx.appendKeyValue("Name", thread.getName());
					ctx.appendKeyValue("Alive", thread.isAlive());
					ctx.appendKeyValue("Priority", thread.getPriority());
					ctx.appendKeyValue("Daemon", thread.isDaemon());
					ctx.appendKeyValue("Interrupted", thread.isInterrupted());
					sendContext(level, title, LogEntryType.Text, ctx);
				} catch (Exception e) {
					logInternalError("logThread: " + e.getMessage());
				}
			} finally {
				ctx.close();
			}
		}
	}

	/**
	 * Overloaded. Logs the content of a collection with the default log level.
	 *
	 * <p>
	 * This method iterates through the supplied collection and
	 * {@link ObjectRenderer#renderObject} every element into
	 * a string. These elements will be displayed in a listview in
	 * the Console.
	 * </p>
	 * <p>
	 * This method uses the {@link SmartInspect#setDefaultLevel}
	 * of the session's {@link Session#getParent} as log level.
	 * For more information, please refer to the documentation of the
	 * {@link SmartInspect#setDefaultLevel} method
	 * of the SmartInspect class.
	 * </p>
	 *
	 * @param title The title to display in the Console
	 * @param c     The collection to log
	 */
	public void logCollection(String title, Collection c) {
		logCollection(this.fParent.getDefaultLevel(), title, c);
	}

	/**
	 * Overloaded. Logs the content of a collection with a custom log
	 * level.
	 * This method iterates through the supplied collection and
	 * renders every element into a string. These elements will be displayed in a listview in
	 * the Console.
	 *
	 * @param level The log level of this method call
	 * @param title The title to display in the Console
	 * @param c     The collection to log
	 */
	public void logCollection(Level level, String title, Collection c) {
		if (isOn(level)) {
			if (c == null) {
				logInternalError("logCollection: c argument is null");
				return;
			}

			ListViewerContext ctx = new ListViewerContext();
			try {
				try {
					Iterator it = c.iterator();

					while (it.hasNext()) {
						Object o = it.next();

						if (o == c) {
							ctx.appendLine("<cycle>");
						} else {
							ctx.appendLine(ObjectRenderer.renderObject(o));
						}
					}

					sendContext(level, title, LogEntryType.Text, ctx);
				} catch (Exception e) {
					logInternalError("logCollection: " + e.getMessage());
				}
			} finally {
				ctx.close();
			}
		}
	}

	/**
	 * Overloaded. Logs the content of a map with the default log level.
	 *
	 * <p>This methods iterates through the supplied dictionary and
	 * renders every key/value pair into a string. These pairs will be displayed in a special
	 * key/value table in the Console.</p>
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.</p>
	 *
	 * @param title The title to display in the Console
	 * @param map   The map to log
	 */
	public void logMap(String title, Map map) {
		logMap(this.fParent.getDefaultLevel(), title, map);
	}

	/**
	 * Logs the content of a map with a custom log level.
	 * <p>
	 * This method iterates through the supplied dictionary and
	 * {@link ObjectRenderer#renderObject} every key/value
	 * pair into a string. These pairs will be displayed in a special
	 * key/value table in the Console.
	 *
	 * @param level The log level of this method call
	 * @param title The title to display in the Console
	 * @param map   The map to log
	 */
	public void logMap(Level level, String title, Map map) {
		if (isOn(level)) {
			if (map == null) {
				logInternalError("logMap: map argument is null");
				return;
			}

			ValueListViewerContext ctx = new ValueListViewerContext();
			try {
				try {
					Iterator it = map.keySet().iterator();

					while (it.hasNext()) {
						Object key = it.next();
						Object val = map.get(key);

						ctx.appendKeyValue(
								(key == map ? "<cycle>" : ObjectRenderer.renderObject(key)),
								(val == map ? "<cycle>" : ObjectRenderer.renderObject(val))
						);
					}

					sendContext(level, title, LogEntryType.Text, ctx);
				} catch (Exception e) {
					logInternalError("logMap: " + e.getMessage());
				}
			} finally {
				ctx.close();
			}
		}
	}

	/**
	 * Overloaded. Logs the content of an array with the default log level.
	 * <p>
	 * This method iterates through the supplied array and
	 * {@link ObjectRenderer#renderObject renders} every element into a string.
	 * These elements will be displayed in a listview in the Console.
	 * <p>
	 * Please see logCollection to log a collection and logMap to log a map.
	 * <p>
	 * This method uses the {@link SmartInspect#setDefaultLevel default level} of the
	 * session's {@link #getParent parent} as log level. For more information,
	 * please refer to the documentation of the {@link SmartInspect#setDefaultLevel setDefaultLevel}
	 * method of the SmartInspect class.
	 *
	 * @param title The title to display in the Console
	 * @param array The array to log
	 */
	public void logArray(String title, Object[] array) {
		logArray(this.fParent.getDefaultLevel(), title, array);
	}

	/**
	 * Overloaded. Logs the content of an array with a custom log level.
	 * This method iterates through the supplied array and
	 * {@link ObjectRenderer#renderObject} every element into a string. These elements will be displayed in a listview in the Console.
	 * Please see logCollection to log a collection and logMap to log a map.
	 *
	 * @param level The log level of this method call
	 * @param title The title to display in the Console
	 * @param array The array to log
	 */
	public void logArray(Level level, String title, Object[] array) {
		if (isOn(level)) {
			if (array == null) {
				logInternalError("logArray: array argument is null");
				return;
			}

			ListViewerContext ctx = new ListViewerContext();
			try {
				try {
					for (int i = 0, len = array.length; i < len; i++) {
						Object o = array[i];

						if (o == array) {
							ctx.appendLine("<cycle>");
						} else {
							ctx.appendLine(ObjectRenderer.renderObject(o));
						}
					}

					sendContext(level, title, LogEntryType.Text, ctx);
				} catch (Exception e) {
					logInternalError("logArray: " + e.getMessage());
				}
			} finally {
				ctx.close();
			}
		}
	}

	private ViewerContext buildStackTrace() {
		ListViewerContext ctx = new ListViewerContext();

		StackTraceElement[] straces =
				new Exception("Current stack trace").getStackTrace();

		for (int i = 2; i < straces.length; i++) {
			StackTraceElement elem = straces[i];

			if (elem != null) {
				// Convert the stack trace element into
				// a string and append it to our context.
				ctx.appendLine(straces[i].toString().trim());
			}
		}

		return ctx;
	}

	/**
	 * Overloaded. Logs the current stack trace with the default log level.
	 * This method logs the current stack trace. The resulting Log Entry contains all methods including the related classes that are currently on the stack.
	 * Furthermore, the filename and line numbers will be included.
	 * This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of the setDefaultLevel method of the SmartInspect class.
	 */
	public void logCurrentStackTrace() {
		if (isOn(this.fParent.getDefaultLevel())) {
			ViewerContext ctx = buildStackTrace();
			try {
				sendContext(this.fParent.getDefaultLevel(),
						"Current stack trace", LogEntryType.Text, ctx);
			} finally {
				ctx.close();
			}
		}
	}

	/**
	 * Overloaded. Logs the current stack trace with a custom log level.
	 * This method logs the current stack trace. The resulting Log Entry contains all methods
	 * including the related classes that are currently on the stack. Furthermore, the filename
	 * and line numbers will be included.
	 *
	 * @param level The log level of this method call
	 */
	public void logCurrentStackTrace(Level level) {
		if (isOn(level)) {
			ViewerContext ctx = buildStackTrace();
			try {
				sendContext(level, "Current stack trace", LogEntryType.Text,
						ctx);
			} finally {
				ctx.close();
			}
		}
	}

	/**
	 * Overloaded. Logs the current stack trace using a custom title and default log level.
	 * This method logs the current stack trace. The resulting Log Entry contains all methods including the
	 * related classes that are currently on the stack. Furthermore, the filename and line numbers will be included.
	 * This method uses the default level of the session's parent as log level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title The title to display in the Console
	 */
	public void logCurrentStackTrace(String title) {
		if (isOn(this.fParent.getDefaultLevel())) {
			ViewerContext ctx = buildStackTrace();
			try {
				sendContext(this.fParent.getDefaultLevel(), title,
						LogEntryType.Text, ctx);
			} finally {
				ctx.close();
			}
		}
	}

	/**
	 * Overloaded. Logs the current stack trace using a custom title
	 * and custom log level.
	 *
	 * <p>This method logs the current stack trace. The resulting Log Entry contains all methods including the
	 * related classes that are currently on the stack. Furthermore, the filename and line numbers will be included.</p>
	 *
	 * @param level The log level of this method call
	 * @param title The title to display in the Console
	 */
	public void logCurrentStackTrace(Level level, String title) {
		if (isOn(level)) {
			ViewerContext ctx = buildStackTrace();
			try {
				sendContext(level, title, LogEntryType.Text, ctx);
			} finally {
				ctx.close();
			}
		}
	}

	/**
	 * Logs information about the system with the default log level.
	 * <p>
	 * The logged information include the version of the operating system, the Java version and more.
	 * This method is useful for logging general information at the program startup. This guarantees
	 * that the support staff or developers have general information about the execution environment.
	 * <p>
	 * This method uses the default level of the session's parent as log level. For more information,
	 * please refer to the documentation of the setDefaultLevel method of the SmartInspect class.
	 */
	public void logSystem() {
		logSystem(this.fParent.getDefaultLevel());
	}

	/**
	 * Logs information about the system with a custom log level.
	 * The logged information includes the version of the operating
	 * system, the Java version and more. This method is useful for
	 * logging general information at the program startup. This
	 * guarantees that support staff or developers have general
	 * information about the execution environment.
	 *
	 * @param level The log level of this method call
	 */
	public void logSystem(Level level) {
		logSystem(level, "System information");
	}

	/**
	 * Overloaded. Logs information about the system using a custom
	 * title and the default log level.
	 * <p>
	 * The logged information include the version of the operating
	 * system, the Java version and more. This method is useful for
	 * logging general information at the program startup. This
	 * guarantees that the support staff or developers have general
	 * information about the execution environment.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title The title to display in the Console
	 */
	public void logSystem(String title) {
		logSystem(this.fParent.getDefaultLevel(), title);
	}

	/**
	 * Overloaded. Logs information about the system using a custom
	 * title and a custom log level.
	 * The logged information include the version of the operating
	 * system, the Java version and more. This method is useful for
	 * logging general information at the program startup. This
	 * guarantees that the support staff or developers have general
	 * information about the execution environment.
	 *
	 * @param level The log level of this method call
	 * @param title The title to display in the Console
	 */
	public void logSystem(Level level, String title) {
		if (!isOn(level)) {
			return;
		}

		InspectorViewerContext ctx = new InspectorViewerContext();
		try {
			try {
				ctx.startGroup("Operating System");
				ctx.appendKeyValue("Name", System.getProperty("os.name"));
				ctx.appendKeyValue("Version", System.getProperty("os.version"));


				ctx.startGroup("User");
				ctx.appendKeyValue("Name", System.getProperty("user.name"));
				ctx.appendKeyValue("Home", System.getProperty("user.home"));
				ctx.appendKeyValue("Current directory",
						System.getProperty("user.dir"));

				ctx.startGroup("Java");
				ctx.appendKeyValue("VM name",
						System.getProperty("java.vm.name"));
				ctx.appendKeyValue("VM vendor",
						System.getProperty("java.vm.vendor"));
				ctx.appendKeyValue("VM version",
						System.getProperty("java.vm.version"));
				ctx.appendKeyValue("Runtime name",
						System.getProperty("java.runtime.name"));
				ctx.appendKeyValue("Runtime version",
						System.getProperty("java.runtime.version"));

				sendContext(level, title, LogEntryType.System, ctx);
			} catch (Exception e) {
				logInternalError("logSystem: " + e.getMessage());
			}
		} finally {
			ctx.close();
		}
	}

	/**
	 * Overloaded. Logs the metadata of a ResultSet with the default log level.
	 *
	 * <p>This method sends the schema of a ResultSet. A schema includes
	 * the most important properties of every column in the set.
	 *
	 * <p>This logResultSetMetaData method is especially useful in database
	 * applications with lots of queries. It gives you the possibility to
	 * see the raw schema of query results.
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title The title to display in the Console
	 * @param rset  The ResultSet instance whose metadata should be logged
	 */
	public void logResultSetMetaData(String title, ResultSet rset) {
		logResultSetMetaData(this.fParent.getDefaultLevel(), title, rset);
	}

	/**
	 * Overloaded. Logs the metadata of a ResultSet with a custom log
	 * level.
	 * This method sends the schema of a ResultSet. A schema includes
	 * the most important properties of every column in the set.
	 * This logResultSetMetaData method is especially useful in database
	 * applications with lots of queries. It gives you the possibility to
	 * see the raw schema of query results.
	 *
	 * @param level The log level of this method call
	 * @param title The title to display in the Console
	 * @param rset  The ResultSet instance whose metadata should be logged
	 */
	public void logResultSetMetaData(Level level, String title,
									 ResultSet rset) {
		if (isOn(level)) {
			if (rset == null) {
				logInternalError("logResultSetMetaData: rset argument is null");
			} else {
				try {
					logResultSetMetaData(level, title, rset.getMetaData());
				} catch (SQLException e) {
					logInternalError("logResultSetMetaData: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Overloaded method that logs the metadata of a ResultSet with the default log level.
	 * <p>
	 * This method sends the schema of a ResultSet. A schema includes the most important properties of every column in the set.
	 * </p>
	 * <p>
	 * The logResultSetMetaData method is especially useful in database applications with lots of queries. It gives you the ability to
	 * see the raw schema of query results.
	 * </p>
	 * <p>
	 * This method uses the default level of the session's parent as log level. For more details, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 * </p>
	 *
	 * @param rmeta The metadata of a ResultSet which should be logged
	 */
	public void logResultSetMetaData(String title, ResultSetMetaData rmeta) {
		logResultSetMetaData(this.fParent.getDefaultLevel(), title, rmeta);
	}

	/**
	 * Overloaded. Logs the metadata of a ResultSet with a custom log
	 * level.
	 * <p>
	 * This method sends the schema of a ResultSet. A schema includes
	 * the most important properties of every column in the set.
	 * This logResultSetMetaData method is especially useful in database
	 * applications with lots of queries. It gives you the possibility to
	 * see the raw schema of query results.
	 *
	 * @param level The log level of this method call
	 * @param rmeta The metadata of a ResultSet which should be logged
	 */
	public void logResultSetMetaData(Level level, String title,
									 ResultSetMetaData rmeta) {
		if (!isOn(level)) {
			return;
		}

		if (rmeta == null) {
			logInternalError("logResultSetMetaData: rmeta argument is null");
			return;
		}

		TableViewerContext ctx = new TableViewerContext();
		try {
			try {
				// Write the header first.
				ctx.appendHeader(
						"Name, Type, \"Read Only\", Searchable, Nullable," +
								"\"Auto Increment\", \"Case Sensitive\"\r\n\r\n"
				);

				// Then the actual meta data.
				for (int i = 1, count = rmeta.getColumnCount(); i <= count; i++) {
					ctx.beginRow();
					try {
						ctx.addRowEntry(rmeta.getColumnName(i));
						ctx.addRowEntry(rmeta.getColumnTypeName(i));
						ctx.addRowEntry(rmeta.isReadOnly(i));
						ctx.addRowEntry(rmeta.isSearchable(i));

						switch (rmeta.isNullable(i)) {
							case ResultSetMetaData.columnNoNulls: {
								ctx.addRowEntry(false);
								break;
							}

							case ResultSetMetaData.columnNullable: {
								ctx.addRowEntry(true);
								break;
							}

							default: {
								ctx.addRowEntry("unknown");
								break;
							}
						}

						ctx.addRowEntry(rmeta.isAutoIncrement(i));
						ctx.addRowEntry(rmeta.isCaseSensitive(i));
					} finally {
						ctx.endRow();
					}
				}

				sendContext(level, title, LogEntryType.DatabaseStructure,
						ctx);
			} catch (Exception e) {
				logInternalError("logResultSetMetaData: " + e.getMessage());
			}
		} finally {
			ctx.close();
		}
	}

	/**
	 * Logs the content of a ResultSet with the default log level.
	 *
	 * <p>This method logs the content of a ResultSet instance. This logResultSet method is
	 * especially useful in database applications with lots of queries. It gives you
	 * the possibility to see the raw query results.</p>
	 *
	 * <p>Please note that this method tries to restore the original row position of the
	 * supplied ResultSet instance after reading the content, but only if its type
	 * is not ResultSet.TYPE_FORWARD_ONLY.</p>
	 *
	 * <p>This method uses the default level of the session's parent as log level. For
	 * more information, please refer to the documentation of the setDefaultLevel method
	 * of the SmartInspect class.</p>
	 *
	 * @param title The title to display in the Console
	 * @param rset  The ResultSet instance whose content should be logged
	 */
	public void logResultSet(String title, ResultSet rset) {
		logResultSet(this.fParent.getDefaultLevel(), title, rset);
	}

	/**
	 * Overloaded method that logs the content of a ResultSet with a custom log level. This method logs the content of a ResultSet instance.
	 * This logResultSet method is quite useful in database applications with myriad queries. It provides the opportunity
	 * to see the raw query results. Please note that this method attempts to restore the original row
	 * position of the supplied ResultSet instance after reading the content,
	 * but only if its type isn't ResultSet.TYPE_FORWARD_ONLY.
	 *
	 * @param level The log level of this method call
	 * @param title The title to display in the Console
	 * @param rset  The ResultSet instance whose content should be logged
	 */
	public void logResultSet(Level level, String title, ResultSet rset) {
		if (!isOn(level)) {
			return;
		}

		if (rset == null) {
			logInternalError("logResultSet: rset argument is null");
			return;
		}

		TableViewerContext ctx = new TableViewerContext();
		try {
			try {
				ResultSetMetaData meta = rset.getMetaData();
				int columnCount = meta.getColumnCount();

				// We need to write the headers of the table,
				// that means, the names of the colums.

				ctx.beginRow();
				try {
					for (int i = 1; i <= columnCount; i++) {
						ctx.addRowEntry(meta.getColumnName(i));
					}
				} finally {
					ctx.endRow();
				}

				int oldRow = rset.getRow();
				try {
					if (rset.getType() != ResultSet.TYPE_FORWARD_ONLY) {
						if (!rset.isFirst()) {
							try {
								// Try to move the result set to the first row.
								rset.first();
							} catch (SQLException e) {
								// Ignore possible exceptions and display
								// only the rows from the current position.
							}
						}
					}

					// After we've written the table header, we
					// can now write the whole result set content.

					while (rset.next()) {
						ctx.beginRow();
						try {
							for (int i = 1; i <= columnCount; i++) {
								ctx.addRowEntry(rset.getString(i));
							}
						} finally {
							ctx.endRow();
						}
					}

					sendContext(level, title, LogEntryType.DatabaseResult,
							ctx);
				} finally {
					if (rset.getType() != ResultSet.TYPE_FORWARD_ONLY) {
						// Reset to the original row position.
						rset.absolute(oldRow);
					}
				}
			} catch (Exception e) {
				logInternalError("logResultSet: " + e.getMessage());
			}
		} finally {
			ctx.close();
		}
	}

	/**
	 * Overloaded method, logs the content of a StringBuffer instance with the
	 * default log level. The content of the supplied StringBuffer instance is displayed in
	 * a read-only text field. This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title The title to display in the Console
	 * @param sb    The StringBuffer instance whose content should be logged
	 */
	public void logStringBuffer(String title, StringBuffer sb) {
		logStringBuffer(this.fParent.getDefaultLevel(), title, sb);
	}

	/**
	 * Overloaded. Logs the content of a StringBuffer instance with a
	 * custom log level.
	 * The content of the supplied StringBuffer instance is displayed in
	 * a read-only text field.
	 *
	 * @param level The log level of this method call
	 * @param title The title to display in the Console
	 * @param sb    The StringBuffer instance whose content should be logged
	 */
	public void logStringBuffer(Level level, String title, StringBuffer sb) {
		if (isOn(level)) {
			if (sb == null) {
				logInternalError("logStringBuffer: sb argument is null");
			} else {
				logText(level, title, sb.toString());
			}
		}
	}

	/**
	 * Logs the content of a binary stream with the default log level.
	 *
	 * <p>The content of the supplied binary stream will be displayed in a read-only hex editor. </p>
	 *
	 * <p>This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of the setDefaultLevel method
	 * of the SmartInspect class. </p>
	 *
	 * @param title  The title to display in the Console
	 * @param stream The stream whose content should be logged
	 */
	public void logStream(String title, InputStream stream) {
		logStream(this.fParent.getDefaultLevel(), title, stream);
	}

	/**
	 * Overloaded. Logs the content of a binary stream with a custom log level.
	 * The content of the supplied binary stream will be displayed in a read-only hex editor.
	 *
	 * @param level  The log level of this method call
	 * @param title  The title to display in the Console
	 * @param stream The stream whose content should be logged
	 */
	public void logStream(Level level, String title, InputStream stream) {
		logBinaryStream(level, title, stream);
	}

	/**
	 * Overloaded. Logs the content of a text reader with the default log
	 * level. The content of the supplied reader is displayed in a read-only
	 * text field. This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title  The title to display in the Console
	 * @param reader The reader whose content should be logged
	 */
	public void logReader(String title, Reader reader) {
		logReader(this.fParent.getDefaultLevel(), title, reader);
	}

	/**
	 * Overloaded. Logs the content of a text reader with a custom log
	 * level. The content of the supplied reader is displayed in a read-only
	 * text field.
	 *
	 * @param level  The log level of this method call
	 * @param title  The title to display in the Console
	 * @param reader The reader whose content should be logged
	 */
	public void logReader(Level level, String title, Reader reader) {
		logTextReader(level, title, reader);
	}

	/**
	 * Clears all Log Entries in the Console.
	 */
	public void clearLog() {
		if (isOn()) {
			sendControlCommand(ControlCommandType.ClearLog, null);
		}
	}

	/**
	 * Clears all Watches in the Console.
	 */
	public void clearWatches() {
		if (isOn()) {
			sendControlCommand(ControlCommandType.ClearWatches, null);
		}
	}

	/**
	 * Clears all AutoViews in the Console.
	 */
	public void clearAutoViews() {
		if (isOn()) {
			sendControlCommand(ControlCommandType.ClearAutoViews, null);
		}
	}

	/**
	 * Resets the whole Console.
	 * <p>
	 * This method resets the whole Console. This means that all
	 * Watches, Log Entries, Process Flow entries and AutoViews
	 * will be deleted.
	 */
	public void clearAll() {
		if (isOn()) {
			sendControlCommand(ControlCommandType.ClearAll, null);
		}
	}

	/**
	 * Clears all Process Flow entries in the Console.
	 */
	public void clearProcessFlow() {
		if (isOn()) {
			sendControlCommand(ControlCommandType.ClearProcessFlow, null);
		}
	}

	private int updateCounter(String name, boolean increment) {
		int value;
		String key = name.toLowerCase();

		synchronized (this.fCounter) {
			if (this.fCounter.containsKey(key)) {
				Integer i = (Integer) this.fCounter.get(key);
				value = i.intValue();
			} else {
				value = 0;
			}

			if (increment) {
				value++;
			} else {
				value--;
			}

			this.fCounter.put(key, new Integer(value));
		}

		return value;
	}

	/**
	 * Overloaded. Increments a named counter by one and automatically
	 * sends its name and value as integer watch with the default log
	 * level.
	 * <p>
	 * The Session class tracks a list of so called named counters.
	 * A counter has a name and a value of type integer. This method
	 * increments the value for the specified counter by one and then
	 * sends a normal integer watch with the name and value of the
	 * counter. The initial value of a counter is 0. To reset the
	 * value of a counter to 0 again, you can call resetCounter.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 * <p>
	 * See decCounter for a method which decrements the value of a
	 * named counter instead of incrementing it.
	 *
	 * @param name The name of the counter to log
	 */
	public void incCounter(String name) {
		incCounter(this.fParent.getDefaultLevel(), name);
	}

	/**
	 * Overloaded. Increments a named counter by one and automatically
	 * sends its name and value as integer watch with a custom log
	 * level.
	 * <p>
	 * The Session class tracks a list of so called named counters.
	 * A counter has a name and a value of type integer. This method
	 * increments the value for the specified counter by one and then
	 * sends a normal integer watch with the name and value of the
	 * counter. The initial value of a counter is 0. To reset the
	 * value of a counter to 0 again, you can call resetCounter.
	 * <p>
	 * See decCounter for a method which decrements the value of a
	 * named counter instead of incrementing it.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the counter to log
	 */
	public void incCounter(Level level, String name) {
		if (isOn(level)) {
			if (name == null) {
				logInternalError("incCounter: name argument is null");
			} else {
				int value = updateCounter(name, true);
				sendWatch(level, name, Integer.toString(value),
						WatchType.Integer);
			}
		}
	}

	/**
	 * Overloaded. Decrements a named counter by one and automatically
	 * sends its name and value as integer watch with the default log
	 * level.
	 * <p>
	 * The Session class tracks a list of so called named counters.
	 * A counter has a name and a value of type integer. This method
	 * decrements the value for the specified counter by one and then
	 * sends a normal integer watch with the name and value of the
	 * counter. The initial value of a counter is 0. To reset the
	 * value of a counter to 0 again, you can call resetCounter.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 * <p>
	 * See incCounter for a method which increments the value of a
	 * named counter instead of decrementing it.
	 *
	 * @param name The name of the counter to log
	 */
	public void decCounter(String name) {
		decCounter(this.fParent.getDefaultLevel(), name);
	}

	/**
	 * Overloaded. Decrements a named counter by one and automatically
	 * sends its name and value as integer watch with a custom log
	 * level.
	 * <p>
	 * The Session class tracks a list of so called named counters.
	 * A counter has a name and a value of type integer. This method
	 * decrements the value for the specified counter by one and then
	 * sends a normal integer watch with the name and value of the
	 * counter. The initial value of a counter is 0. To reset the
	 * value of a counter to 0 again, you can call resetCounter.
	 * <p>
	 * See incCounter for a method which increments the value of a
	 * named counter instead of decrementing it.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the counter to log
	 */
	public void decCounter(Level level, String name) {
		if (isOn(level)) {
			if (name == null) {
				logInternalError("decCounter: name argument is null");
			} else {
				int value = updateCounter(name, false);
				sendWatch(level, name, Integer.toString(value),
						WatchType.Integer);
			}
		}
	}

	/**
	 * Resets a named counter to its initial value of 0.
	 * This method resets the integer value of a named counter to 0
	 * again. If the supplied counter is unknown, this method has no
	 * effect. Please refer to the incCounter and decCounter methods
	 * for more information about named counters.
	 *
	 * @param name The name of the counter to reset
	 */
	public void resetCounter(String name) {
		if (name == null) {
			logInternalError("resetCounter: name argument is null");
			return;
		}

		String key = name.toLowerCase();

		synchronized (this.fCounter) {
			this.fCounter.remove(key);
		}
	}

	/**
	 * Logs a char Watch with the default log level.
	 * <p>
	 * This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of the setDefaultLevel method
	 * of the SmartInspect class.
	 *
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchChar(String name, char value) {
		watchChar(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a char Watch with a custom log level.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchChar(Level level, String name, char value) {
		if (isOn(level)) {
			sendWatch(level, name, Character.toString(value), WatchType.Char);
		}
	}

	/**
	 * Overloaded. Logs a String Watch with the default log level.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchString(String name, String value) {
		watchString(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a String Watch with a custom log level.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchString(Level level, String name, String value) {
		if (isOn(level)) {
			sendWatch(level, name, value, WatchType.String);
		}
	}

	/**
	 * Overloaded. Logs a byte Watch with the default log level.
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.</p>
	 *
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchByte(String name, byte value) {
		watchByte(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a byte Watch with a custom log level.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchByte(Level level, String name, byte value) {
		watchByte(level, name, value, false);
	}

	/**
	 * Overloaded. Logs a byte Watch with an optional hexadecimal
	 * representation and default log level.
	 * <p>
	 * This method logs a byte Watch. You can specify if a hexadecimal
	 * representation should be included as well by setting the
	 * includeHex parameter to true.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name       The name of the Watch
	 * @param value      The value to display as Watch value
	 * @param includeHex Indicates if a hexadecimal representation should be included
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchByte(String name, byte value, boolean includeHex) {
		watchByte(this.fParent.getDefaultLevel(), name, value, includeHex);
	}

	/**
	 * Overloaded. Logs a byte Watch with an optional hexadecimal
	 * representation and custom log level.
	 * <p>
	 * This method logs a byte Watch. You can specify if a
	 * hexadecimal representation should be included as well
	 * by setting the includeHex parameter to true.
	 *
	 * @param level      The log level of this method call
	 * @param name       The name of the Watch
	 * @param value      The value to display as Watch value
	 * @param includeHex Indicates if a hexadecimal representation should be included
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchByte(Level level, String name, byte value,
						  boolean includeHex) {
		if (isOn(level)) {
			String v = Byte.toString(value);

			if (includeHex) {
				StringBuffer sb = new StringBuffer();
				sb.append(" (0x");
				sb.append(longToHex(value, 2));
				sb.append(")");
				v += sb.toString();
			}

			sendWatch(level, name, v, WatchType.Integer);
		}
	}

	/**
	 * Overloaded. Logs a short integer Watch with the default log level.
	 * This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchShort(String name, short value) {
		watchShort(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a short integer Watch with a custom log level.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchShort(Level level, String name, short value) {
		watchShort(level, name, value, false);
	}

	/**
	 * Overloaded. Logs a short integer Watch with an optional
	 * hexadecimal representation and default log level.
	 *
	 * <p>This method logs a short integer Watch. You can specify if a
	 * hexadecimal representation should be included as well by setting
	 * the includeHex parameter to true.</p>
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.</p
	 *
	 * @param name       The name of the Watch
	 * @param value      The value to display as Watch value
	 * @param includeHex Indicates if a hexadecimal representation should be included
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchShort(String name, short value, boolean includeHex) {
		watchShort(this.fParent.getDefaultLevel(), name, value, includeHex);
	}

	/**
	 * Overloaded. Logs a short integer Watch with an optional
	 * hexadecimal representation and custom log level.
	 * <p>
	 * This method logs a short integer Watch. You can specify
	 * if a hexadecimal representation should be included as well
	 * by setting the includeHex parameter to true.
	 *
	 * @param level      The log level of this method call
	 * @param name       The name of the Watch
	 * @param value      The value to display as Watch value
	 * @param includeHex Indicates if a hexadecimal representation should be included
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchShort(Level level, String name, short value,
						   boolean includeHex) {
		if (isOn(level)) {
			String v = Short.toString(value);

			if (includeHex) {
				StringBuffer sb = new StringBuffer();
				sb.append(" (0x");
				sb.append(longToHex(value, 4));
				sb.append(")");
				v += sb.toString();
			}

			sendWatch(level, name, v, WatchType.Integer);
		}
	}

	/**
	 * Overloaded method. This method logs an integer Watch with the default log level.
	 *
	 * <p> This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchInt(String name, int value) {
		watchInt(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs an integer Watch with a custom log level.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchInt(Level level, String name, int value) {
		watchInt(level, name, value, false);
	}

	/**
	 * Overloaded. Logs an integer Watch with an optional hexadecimal
	 * representation and default log level.
	 *
	 * <p>This method logs an integer Watch. You can specify if a
	 * hexadecimal representation should be included as well by
	 * setting the includeHex parameter to true.
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name       The name of the Watch
	 * @param value      The value to display as Watch value
	 * @param includeHex Indicates if a hexadecimal representation should be included
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchInt(String name, int value, boolean includeHex) {
		watchInt(this.fParent.getDefaultLevel(), name, value, includeHex);
	}

	/**
	 * Logs an integer Watch with an optional hexadecimal representation and custom log level.
	 * This method logs an integer Watch. You can specify if
	 * a hexadecimal representation should be included as well
	 * by setting the includeHex parameter to true.
	 *
	 * @param level      The log level of this method call
	 * @param name       The name of the Watch
	 * @param value      The value to display as Watch value
	 * @param includeHex Indicates if a hexadecimal representation should be included
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchInt(Level level, String name, int value,
						 boolean includeHex) {
		if (isOn(level)) {
			String v = Integer.toString(value);

			if (includeHex) {
				StringBuffer sb = new StringBuffer();
				sb.append(" (0x");
				sb.append(longToHex(value, 8));
				sb.append(")");
				v += sb.toString();
			}

			sendWatch(level, name, v, WatchType.Integer);
		}
	}

	/**
	 * Overloaded. Logs a long integer Watch with the default log
	 * level. This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchLong(String name, long value) {
		watchLong(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a long integer Watch with a custom log
	 * level.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchLong(Level level, String name, long value) {
		watchLong(level, name, value, false);
	}

	/**
	 * Overloaded method. Logs a long integer Watch with an optional
	 * hexadecimal representation and default log level.
	 * <p>
	 * This method logs a long integer Watch. You can specify if a
	 * hexadecimal representation should be included as well by setting
	 * the includeHex parameter to true.
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name       The name of the Watch
	 * @param value      The value to display as Watch value
	 * @param includeHex Indicates if a hexadecimal representation should be included
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchLong(String name, long value, boolean includeHex) {
		watchLong(this.fParent.getDefaultLevel(), name, value, includeHex);
	}

	/**
	 * Overloaded. Logs a long integer Watch with an optional
	 * hexadecimal representation and custom log level. This method logs a long integer Watch. You can specify
	 * if a hexadecimal representation should be included as well
	 * by setting the includeHex parameter to true.
	 *
	 * @param level      The log level of this method call
	 * @param name       The name of the Watch
	 * @param value      The value to display as Watch value
	 * @param includeHex Indicates if a hexadecimal representation should be included.
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchLong(Level level, String name, long value,
						  boolean includeHex) {
		if (isOn(level)) {
			String v = Long.toString(value);

			if (includeHex) {
				StringBuffer sb = new StringBuffer();
				sb.append(" (0x");
				sb.append(longToHex(value, 16));
				sb.append(")");
				v += sb.toString();
			}

			sendWatch(level, name, v, WatchType.Integer);
		}
	}

	/**
	 * Overloaded. Logs a float Watch with the default log level.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchFloat(String name, float value) {
		watchFloat(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a float Watch with a custom log level.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchFloat(Level level, String name, float value) {
		if (isOn(level)) {
			sendWatch(level, name, Float.toString(value), WatchType.Float);
		}
	}

	/**
	 * Logs a double Watch with the default log level.
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchDouble(String name, double value) {
		watchDouble(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Logs a double Watch with a custom log level.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchDouble(Level level, String name, double value) {
		if (isOn(level)) {
			sendWatch(level, name, Double.toString(value), WatchType.Float);
		}
	}

	/**
	 * Logs a boolean Watch with the default log level.
	 *
	 * <p>This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.</p>
	 *
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchBoolean(String name, boolean value) {
		watchBoolean(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a boolean Watch with a custom log level.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchBoolean(Level level, String name, boolean value) {
		if (isOn(level)) {
			String v = value ? "True" : "False";
			sendWatch(level, name, v, WatchType.Boolean);
		}
	}

	/**
	 * Overloaded. Logs a Date Watch with the default log level.
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchDate(String name, Date value) {
		watchDate(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a Date Watch with a custom log level.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchDate(Level level, String name, Date value) {
		if (isOn(level)) {
			if (value == null) {
				logInternalError("watchDate: value argument is null");
			} else {
				sendWatch(level, name, value.toString(), WatchType.Timestamp);
			}
		}
	}

	/**
	 * Overloaded. Logs an Object Watch with the default log level.
	 * The value of the resulting Watch is the return value of the
	 * toString method of the supplied Object.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchObject(String name, Object value) {
		watchObject(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs an Object Watch with a custom log level.
	 * The value of the resulting Watch is the return value
	 * of the toString method of the supplied Object.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The value to display as Watch value
	 * @see com.gurock.smartinspect.Watch
	 */
	public void watchObject(Level level, String name, Object value) {
		if (isOn(level)) {
			if (value == null) {
				logInternalError("watchObject: value argument is null");
			} else {
				sendWatch(level, name, value.toString(), WatchType.Object);
			}
		}
	}

	/**
	 * Overloaded. Logs a char Watch with the default log level.
	 * <p>
	 * This method just calls the watchChar method.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the Watch
	 * @param value The char value to display as Watch value
	 */
	public void watch(String name, char value) {
		watch(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a char Watch with a custom log level.
	 * <p>
	 * This method just calls the watchChar method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The char value to display as Watch value
	 */
	public void watch(Level level, String name, char value) {
		watchChar(level, name, value);
	}

	/**
	 * Overloaded. Logs a String Watch with the default log level.
	 * This method just calls the watchString method.
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the Watch
	 * @param value The string value to display as Watch value
	 */
	public void watch(String name, String value) {
		watch(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a String Watch with a custom log level.
	 * This method just calls the watchString method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The string value to display as Watch value
	 */
	public void watch(Level level, String name, String value) {
		watchString(level, name, value);
	}

	/**
	 * This method just calls the watchByte method. Uses the default level of the session's parent as log level.
	 * For more information, please refer to the documentation of the setDefaultLevel method of the SmartInspect class.
	 * <p>
	 * Overloaded. Logs a byte watch with the default log level.
	 *
	 * @param name  The name of the watch
	 * @param value The byte value to display as watch value
	 */
	public void watch(String name, byte value) {
		watch(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a byte Watch with a custom level.
	 * This method just calls the watchByte method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The byte value to display as Watch value
	 */
	public void watch(Level level, String name, byte value) {
		watchByte(level, name, value);
	}

	/**
	 * Overloaded. Logs a short integer Watch with the default log level.
	 * This method just calls the watchShort method.
	 * This method uses the {@link SmartInspect#setDefaultLevel} of the session's {@link Session#getParent} as log
	 * level. For more information, please refer to the documentation of the {@link SmartInspect#setDefaultLevel} method
	 * of the SmartInspect class.
	 *
	 * @param name  The name of the Watch.
	 * @param value The short integer value to display as Watch value.
	 */
	public void watch(String name, short value) {
		watch(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a short integer Watch with a custom log level.
	 * This method just calls the watchShort method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The short integer value to display as Watch value
	 */
	public void watch(Level level, String name, short value) {
		watchShort(level, name, value);
	}

	/**
	 * Overloaded method that logs an integer Watch with the default log
	 * level. This method just calls the watchInt method. The method uses the default level of
	 * the session's parent as the log level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the Watch
	 * @param value The integer value to display as Watch value
	 */
	public void watch(String name, int value) {
		watch(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs an integer Watch with a custom log level.
	 * This method just calls the watchInt method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The integer value to display as Watch value
	 */
	public void watch(Level level, String name, int value) {
		watchInt(level, name, value);
	}

	/**
	 * Overloaded. Logs a long integer Watch with the default log level.
	 * This method just calls the watchLong method.
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the Watch
	 * @param value The long integer value to display as Watch value
	 */
	public void watch(String name, long value) {
		watch(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a long integer Watch with a custom log level.
	 * <p>
	 * This method just calls the watchLong method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The long integer value to display as Watch value
	 */
	public void watch(Level level, String name, long value) {
		watchLong(level, name, value);
	}

	/**
	 * Overloaded. Logs a float Watch with the default log level.
	 * This method just calls the watchFloat method.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the Watch
	 * @param value The float value to display as Watch value
	 */
	public void watch(String name, float value) {
		watch(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a float Watch with a custom log level.
	 * <p>
	 * This method just calls the watchFloat method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The float value to display as Watch value
	 */
	public void watch(Level level, String name, float value) {
		watchFloat(level, name, value);
	}

	/**
	 * Overloaded. Logs a double Watch with the default log level.
	 * This method just calls the watchDouble method.
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the Watch
	 * @param value The double value to display as Watch value
	 */
	public void watch(String name, double value) {
		watch(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Logs a double Watch with a custom log level.
	 * This method just calls the watchDouble method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The double value to display as Watch value
	 */
	public void watch(Level level, String name, double value) {
		watchDouble(level, name, value);
	}

	/**
	 * Overloaded. Logs a boolean Watch with the default log level.
	 *
	 * <p>This method just calls the watchBoolean method. This method uses the default level of the session's
	 * parent as log level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.</p>
	 *
	 * @param name  The name of the Watch
	 * @param value The boolean value to display as Watch value
	 */
	public void watch(String name, boolean value) {
		watch(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a boolean Watch with a custom log level.
	 * This method just calls the watchBoolean method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The boolean value to display as Watch value
	 */
	public void watch(Level level, String name, boolean value) {
		watchBoolean(level, name, value);
	}

	/**
	 * Overloaded. Logs a Date Watch with the default log level.
	 * <p>
	 * This method just calls the watchDate method.
	 * <p>
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param name  The name of the Watch
	 * @param value The Date value to display as Watch value
	 */
	public void watch(String name, Date value) {
		watch(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs a Date Watch with a custom log level.
	 * This method just calls the watchDate method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The Date value to display as Watch value
	 */
	public void watch(Level level, String name, Date value) {
		watchDate(level, name, value);
	}

	/**
	 * Overloaded. Logs an Object Watch with the default log level.
	 * This method just calls the watchObject method.
	 * This method uses the {@link SmartInspect#setDefaultLevel} of the session's {@link Session#getParent} as log level.
	 * For more information, please refer to the documentation of the {@link SmartInspect#setDefaultLevel} method
	 * of the SmartInspect class.
	 *
	 * @param name  The name of the Watch
	 * @param value The Object value to display as Watch value
	 */
	public void watch(String name, Object value) {
		watch(this.fParent.getDefaultLevel(), name, value);
	}

	/**
	 * Overloaded. Logs an Object Watch with a custom log level.
	 * This method just calls the watchObject method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the Watch
	 * @param value The Object value to display as Watch value
	 */
	public void watch(Level level, String name, Object value) {
		watchObject(level, name, value);
	}

	/**
	 * Overloaded. Logs a custom Log Entry with the default log level. This method is useful for implementing custom Log Entry
	 * methods. For example, if you want to display some information in a particular way in the Console, you can just create a
	 * simple method which formats the data in question correctly and sends them using this sendCustomLogEntry method.
	 * <p>
	 * This method uses the default level of the session's parent as log level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param title The title of the new Log Entry
	 * @param lt    The Log Entry type to use
	 * @param vi    The Viewer ID to use
	 * @param data  Optional data block which can be null
	 * @see com.gurock.smartinspect.LogEntry
	 */
	public void sendCustomLogEntry(String title, LogEntryType lt,
								   ViewerId vi, byte[] data) {
		sendCustomLogEntry(this.fParent.getDefaultLevel(), title, lt, vi,
				data);
	}

	/**
	 * Overloaded. Logs a custom Log Entry with a custom log level.
	 * This method is useful for implementing custom Log Entry
	 * methods. For example, if you want to display some information
	 * in a particular way in the Console, you can just create a
	 * simple method which formats the data in question correctly and
	 * sends them using this sendCustomLogEntry method.
	 *
	 * @param level The log level of this method call
	 * @param title The title of the new Log Entry
	 * @param lt    The Log Entry type to use
	 * @param vi    The Viewer ID to use
	 * @param data  Optional data block which can be null
	 * @see com.gurock.smartinspect.LogEntry
	 */
	public void sendCustomLogEntry(Level level, String title,
								   LogEntryType lt, ViewerId vi, byte[] data) {
		if (isOn(level)) {
			if (lt == null) {
				logInternalError("sendCustomLogEntry: lt argument is null");
			} else if (vi == null) {
				logInternalError("sendCustomLogEntry: vi argument is null");
			} else {
				sendLogEntry(level, title, lt, vi, getColor(), data);
			}
		}
	}

	/**
	 * Overloaded. Logs a custom Control Command with the default log level.
	 * This method uses the default level of the session's parent as log
	 * level. For more information, please refer to the documentation of
	 * the setDefaultLevel method of the SmartInspect class.
	 *
	 * @param ct   The Control Command type to use
	 * @param data Optional data block which can be null
	 * @see com.gurock.smartinspect.ControlCommand
	 */
	public void sendCustomControlCommand(ControlCommandType ct, byte[] data) {
		sendCustomControlCommand(this.fParent.getDefaultLevel(), ct, data);
	}

	/**
	 * Overloaded. Logs a custom Control Command with a custom log level.
	 *
	 * @param level The log level of this method call
	 * @param ct    The Control Command type to use
	 * @param data  Optional data block which can be null
	 * @see com.gurock.smartinspect.ControlCommand
	 */
	public void sendCustomControlCommand(Level level, ControlCommandType ct,
										 byte[] data) {
		if (isOn(level)) {
			if (ct == null) {
				logInternalError("sendCustomControlCommand: ct argument is null");
			} else {
				sendControlCommand(ct, data);
			}
		}
	}

	/**
	 * Overloaded. Logs a custom Watch with the default log level. This method is useful for implementing custom Watch methods. For example,
	 * if you want to track the status of an instance of a specific class, you can just create a simple method which extracts all necessary
	 * information about this instance and logs them using this sendCustomWatch method. This method uses the default level of the
	 * session's parent as log level. For more information, please refer to the documentation of the setDefaultLevel method of the
	 * SmartInspect class.
	 *
	 * @param name  The name of the new Watch
	 * @param value The value of the new Watch
	 * @param wt    The Watch type to use
	 * @see com.gurock.smartinspect.Watch
	 */
	public void sendCustomWatch(String name, String value, WatchType wt) {
		sendCustomWatch(this.fParent.getDefaultLevel(), name, value, wt);
	}

	/**
	 * Overloaded method. Logs a custom Watch with a custom log level. This method is useful for implementing custom Watch methods.
	 * For example, if you want to track the status of an instance of a specific class, you can just create a simple method which
	 * extracts all necessary information about this instance and logs them using this sendCustomWatch method.
	 *
	 * @param level The log level of this method call
	 * @param name  The name of the new Watch
	 * @param value The value of the new Watch
	 * @param wt    The Watch type to use
	 * @see com.gurock.smartinspect.Watch
	 */
	public void sendCustomWatch(Level level, String name, String value,
								WatchType wt) {
		if (isOn(level)) {
			if (wt == null) {
				logInternalError("sendCustomWatch: wt argument is null");
			} else {
				sendWatch(level, name, value, wt);
			}
		}
	}

	/**
	 * Overloaded. Logs a custom Process Flow entry with the default log level.
	 * <p>
	 * This method uses the default level of the session's parent as log level.
	 * For more information, please refer to the
	 * documentation of the {@link SmartInspect#setDefaultLevel} method
	 * of the SmartInspect class.
	 * </p>
	 *
	 * @param title The title of the new Process Flow entry.
	 * @param pt    The Process Flow type to use.
	 * @see com.gurock.smartinspect.ProcessFlow
	 */
	public void sendCustomProcessFlow(String title, ProcessFlowType pt) {
		sendCustomProcessFlow(this.fParent.getDefaultLevel(), title, pt);
	}

	/**
	 * Overloaded. Logs a custom Process Flow entry with a custom log level.
	 *
	 * @param title The title of the new Process Flow entry
	 * @param pt    The Process Flow type to use
	 * @see com.gurock.smartinspect.ProcessFlow
	 */
	public void sendCustomProcessFlow(Level level, String title,
									  ProcessFlowType pt) {
		if (isOn(level)) {
			if (pt == null) {
				logInternalError("sendCustomProcessFlow: pt argument is null");
			} else {
				sendProcessFlow(level, title, pt);
			}
		}
	}
}
