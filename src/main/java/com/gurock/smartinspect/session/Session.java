//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect.session;

import com.gurock.smartinspect.*;
import com.gurock.smartinspect.contexts.*;
import com.gurock.smartinspect.packets.controlcommand.ControlCommand;
import com.gurock.smartinspect.packets.controlcommand.ControlCommandType;
import com.gurock.smartinspect.packets.logentry.LogEntry;
import com.gurock.smartinspect.packets.logentry.LogEntryType;
import com.gurock.smartinspect.packets.processflow.ProcessFlow;
import com.gurock.smartinspect.packets.processflow.ProcessFlowType;
import com.gurock.smartinspect.packets.watch.Watch;
import com.gurock.smartinspect.packets.watch.WatchType;

import java.awt.*;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;

// <summary>
//   Logs all kind of data and variables to the SmartInspect Console
//   or to a log file.
// </summary>
// <remarks>
//   The Session class offers dozens of useful methods for sending
//   any kind of data with the assistance of its <link Session.getParent,
//   parent>. Sessions can send simple messages, warnings, errors and
//   more complex things like pictures, objects, exceptions, system
//   information and much more. They are even able to send variable
//   watches, generate illustrated process and thread information or
//   control the behavior of the SmartInspect Console. It is possible,
//   for example, to clear the entire log in the Console by calling the
//   clearLog method.
//   
//   Please note that log methods of this class do nothing and return
//   immediately if the session is currently <link setActive, inactive>
//   its <link getParent, parent> is <link SmartInspect.setEnabled,
//   disabled> or the <link SmartInspect.setLevel, log level> is not
//   sufficient.
// </remarks>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

public class Session
{
	public static final Color
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

	public Session(SmartInspect parent, String name)
	{
		this.fCheckpointLock = new Object();

		// Initialize the remaining fields.

		this.fParent = parent;
		this.fCheckpointCounter = 0;
		
		if (name == null)
		{
			this.fName = "";
		}
		else 
		{
			this.fName = name;
		}

		this.fLevel = Level.Debug;
		this.fActive = true; // Active by default.
		this.fCounter = new HashMap();
		this.fCheckpoints = new HashMap();		
		resetColor();
	}

	// <summary>
	//   Returns if this session is stored in the session tracking
	//   list of its Parent.
	// </summary>
	// <returns>
	//   True if this session is stored in the session tracking list
	//   and false otherwise.
	// </returns>
	// <remarks>
	//   See the SmartInspect.getSession and SmartInspect.addSession
	//   methods for more information about session tracking.
	// </remarks>
	
	protected boolean isStored()
	{
		return this.fStored;
	}

	// <summary>
	//   Indicates if this session is stored in the session tracking
	//   list of its Parent.
	// </summary>
	// <param name="stored">
	//   True if this session is stored in the session tracking list
	//   and false otherwise.
	// </param>
	// <remarks>
	//   See the SmartInspect.getSession and SmartInspect.addSession
	//   methods for more information about session tracking.
	// </remarks>

	protected void setStored(boolean stored)
	{
		this.fStored = stored;
	}

	// <summary>
	//   Sets the session name used for Log Entries.
	// </summary>
	// <param name="name">The new session name.</param>
	// <remarks>
	//   The session name helps you to identify Log Entries from different
	//   sessions in the SmartInspect Console. If you set the session name
	//   to null, the session name will be empty when sending Log Entries.
	// </remarks>

	public void setName(String name)
	{
		if (name == null)
		{
			name = "";
		}

		if (this.fStored)
		{
			this.fParent.updateSession(this, name, this.fName);
		}

		this.fName = name;
	}

	// <summary>
	//   Returns the session name used for Log Entries.
	// </summary>
	// <returns>
	//   The session name used for Log Entries.
	// </returns>
	// <remarks>
	//   The session name helps you to identify Log Entries from different
	//   sessions in the SmartInspect Console.
	// </remarks>

	public String getName()
	{
		return this.fName;
	}
	
	// <summary>
	//   Specifies if the session is currently active.  
	// </summary>
	// <param name="active">
	//   A boolean value to activate or deactivate this instance.
	// </param>
	// <remarks>
	//   If false is passed to this method, all logging methods of this
	//   class will return immediately and do nothing. Please note that
	//   the <link getParent, parent> of this session also needs to be 
	//   <link SmartInspect.setEnabled, enabled> in order to log
	//   information.
	//   
	//   This method is especially useful if you are using multiple
	//   sessions at once and want to deactivate a subset of these
	//   sessions. To deactivate all your sessions, you can use the
	//   <link SmartInspect.setEnabled, setEnabled> method of the <link
	//   getParent, parent>.
	// </remarks>
	
	public void setActive(boolean active)
	{
		this.fActive = active;
	}
	
	// <summary>
	//   Indicates if this session is currently active or not.
	// </summary>
	// <returns>
	//   True if this session is currently active and false
	//   otherwise.
	// </returns>
	
	public boolean isActive()
	{
		return this.fActive;
	}

	// <summary>
	//   Returns the log level of this Session object.
	// </summary>
	// <returns>The log level of this session.</returns>
	// <remarks>
	//   Each Session object can have its own log level. A log message
	//   is only logged if its log level is greater than or equal to the
	//   log level of a session and the session <link Parent, getParent>.
	//   Log levels can thus be used to limit the logging output to
	//   important messages only.
	// </remarks>
	
	public Level getLevel()
	{
		return this.fLevel;
	}
	
	// <summary>
	//   Sets the log level of this Session object.
	// </summary>
	// <param name="level">The new log level for this session.</param>
	// <remarks>
	//   Each Session object can have its own log level. A log message
	//   is only logged if its log level is greater than or equal to the
	//   log level of a session and the session <link Parent, getParent>.
	//   Log levels can thus be used to limit the logging output to
	//   important messages only.
	//
	//   This method does nothing if the level parameter is null.
	// </remarks>
	
	public void setLevel(Level level)
	{
		if (level != null)
		{
			this.fLevel = level;
		}
	}
	
	// <summary>
	//   Overloaded. Indicates if information can be logged for a
	//   certain log level or not.  
	// </summary>
	// <param name="level">The log level to check for.</param>
	// <returns>
	//   True if information can be logged and false otherwise.
	// </returns>
	// <remarks>
	//   This method is used by the logging methods in this class
	//   to determine if information should be logged or not. When
	//   extending the Session class by adding new log methods to a
	//   derived class it is recommended to call this method first.
	//
	//   This method returns false if the supplied level argument is
	//   null.
	// </remarks>
	
	public boolean isOn(Level level)
	{
		if (level == null)
		{
			return false;
		}
		else 
		{
			return this.fActive && 
				this.fParent.isEnabled() &&
				level.greaterEqual(this.fLevel) &&
				level.greaterEqual(this.fParent.getLevel());
		}
	}

	// <summary>
	//   Overloaded. Indicates if information can be logged or not.  
	// </summary>
	// <returns>
	//   True if information can be logged and false otherwise.
	// </returns>
	// <remarks>
	//   This method is used by the logging methods in this class
	//   to determine if information should be logged or not. When
	//   extending the Session class by adding new log methods to a
	//   derived class it is recommended to call this method first.
	// </remarks>
	
	public boolean isOn()
	{
		return this.fActive && this.fParent.isEnabled();
	}
	
	// <summary>
	//   Returns the parent of the session.
	// </summary>
	// <returns>
	//   The parent of this session.
	// </returns>
	// <remarks>
	//   The parent of a session is a SmartInspect instance. It is
	//   responsible for sending the packets to the SmartInspect
	//   Console or for writing them to a file. If the parent is not
	//   <link SmartInspect.setEnabled, enabled>, all logging methods
	//   of this class will return immediately and do nothing.
	// </remarks>

	public SmartInspect getParent()
	{
		return this.fParent;
	}

	// <summary>
	//   Sets the background color in the SmartInspect Console of
	//   this session.
	// </summary>
	// <param name="color">The new background color.</param>
	// <remarks>
	//   The session color helps you to identify Log Entries from
	//   different sessions in the SmartInspect Console by changing
	//   the background color. Please note that if you pass a null
	//   reference, then the color will not be changed.
	// </remarks>

	public void setColor(Color color)
	{
		if (color != null)
		{
			this.fColor = color;
		}
	}

	// <summary>
	//   Returns the background color in the SmartInspect Console of
	//   this session.
	// </summary>
	// <returns>
	//   The background color in the SmartInspect Console of this session.
	// </returns>
	// <remarks>
	//   The session color helps you to identify Log Entries from
	//   different sessions in the SmartInspect Console by changing the
	//   background color.
	// </remarks>

	public Color getColor()
	{
		return this.fColor;
	}

	// <summary>
	//   Resets the session color to its default value.
	// </summary>
	// <remarks>
	//   The default color of a session is transparent. 
	// </remarks>

	public void resetColor()
	{
		setColor(DEFAULT_COLOR);
	}

	// <summary>
	//   Overloaded. Logs a simple separator with the default log level.
	// </summary>
	// <seealso cref="com.gurock.smartinspect.session.Session.addCheckpoint"/>
	// <remarks>
	//   This method instructs the Console to draw a separator. A
	//   separator is intended to group related <link LogEntry, Log Entries>
	//   and to separate them visually from others. This method can help
	//   organizing Log Entries in the Console. See addCheckpoint for a
	//   method with a similar intention.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logSeparator()
	{
		logSeparator(this.fParent.getDefaultLevel());
	}
	
	// <summary>
	//   Overloaded. Logs a simple separator with a custom log level.
	// </summary>
	// <seealso cref="com.gurock.smartinspect.session.Session.addCheckpoint"/>
	// <param name="level">The log level of this method call.</param>
	// <remarks>
	//   This method instructs the Console to draw a separator. A
	//   separator is intended to group related <link LogEntry, Log Entries>
	//   and to separate them visually from others. This method can help
	//   organizing Log Entries in the Console. See addCheckpoint for a
	//   method with a similar intention.
	// </remarks>	
	
	public void logSeparator(Level level)
	{
		if (isOn(level))
		{
			sendLogEntry(level, null, LogEntryType.Separator, ViewerId.None);
		}
	}

	// <summary>
	//   Overloaded. Resets the call stack by using the default log
	//   level.
	// </summary>
	// <remarks>
	//   This method instructs the Console to reset the call stack
	//   generated by the EnterMethod and LeaveMethod methods. It
	//   is especially useful if you want to reset the indentation
	//   in the method hierarchy without clearing all
	//   <link LogEntry, Log Entries>.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void resetCallstack()
	{
		resetCallstack(this.fParent.getDefaultLevel());
	}

	// <summary>
	//   Overloaded. Resets the call stack by using a custom log
	//   level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <remarks>
	//   This method instructs the Console to reset the call stack
	//   generated by the EnterMethod and LeaveMethod methods. It
	//   is especially useful if you want to reset the indentation
	//   in the method hierarchy without clearing all
	//   <link LogEntry, Log Entries>.
	// </remarks>

	public void resetCallstack(Level level)
	{
		if (isOn(level))
		{
			sendLogEntry(level, null, LogEntryType.ResetCallstack, 
				ViewerId.None);
		}
	}
	
	// <summary>
	//   Overloaded. Enters a method by using the default log level.
	// </summary>
	// <param name="methodName">The name of the method.</param>
	// <remarks>
	//   The enterMethod method notifies the Console that a new method
	//   has been entered. The Console includes the method in the method
	//   hierarchy. If this method is used consequently, a full call stack
	//   is visible in the Console which helps locating bugs in the source
	//   code. Please see the leaveMethod method as the counter piece to
	//   enterMethod.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void enterMethod(String methodName)
	{
		enterMethod(this.fParent.getDefaultLevel(), methodName);
	}
	
	// <summary>
	//   Overloaded. Enters a method by using a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="methodName">The name of the method.</param>
	// <remarks>
	//   The enterMethod method notifies the Console that a new method
	//   has been entered. The Console includes the method in the method
	//   hierarchy. If this method is used consequently, a full call stack
	//   is visible in the Console which helps locating bugs in the source
	//   code. Please see the leaveMethod method as the counter piece to
	//   enterMethod.
	// </remarks>
	
	public void enterMethod(Level level, String methodName)
	{
		if (isOn(level))
		{
			// Send two packets.
			sendLogEntry(level, methodName, LogEntryType.EnterMethod, 
				ViewerId.Title);
			sendProcessFlow(level, methodName, ProcessFlowType.EnterMethod);
		}
	}

	// <summary>
	//   Overloaded. Enters a method by using the default log level.
	//   The method name consists of a format string and the related
	//   array of arguments.
	// </summary>
	// <param name="methodNameFmt">
	//   The format string to create the name of the method.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   The enterMethod method notifies the Console that a new method
	//   has been entered. The Console includes the method in the method
	//   hierarchy. If this method is used consequently, a full call stack
	//   is visible in the Console which helps locating bugs in the source
	//   code. Please see the leaveMethod method as the counter piece to
	//   enterMethod.
	//
	//   The resulting method name consists of a format string and the
	//   related array of arguments.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void enterMethod(String methodNameFmt, Object[] args)
	{
		enterMethod(this.fParent.getDefaultLevel(), methodNameFmt, args);
	}
	
	// <summary>
	//   Overloaded. Enters a method by using a custom log level.
	//   The method name consists of a format string and the related
	//   array of arguments.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="methodNameFmt">
	//   The format string to create the name of the method.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   The enterMethod method notifies the Console that a new method
	//   has been entered. The Console includes the method in the method
	//   hierarchy. If this method is used consequently, a full call stack
	//   is visible in the Console which helps locating bugs in the source
	//   code. Please see the leaveMethod method as the counter piece to
	//   enterMethod.
	//
	//   The resulting method name consists of a format string and the
	//   related array of arguments.
	// </remarks>

	public void enterMethod(Level level, String methodNameFmt, 
		Object[] args)
	{
		if (isOn(level))
		{
			try
			{
				enterMethod(level, MessageFormat.format(methodNameFmt, args));
			}
			catch (Exception e)
			{
				// The MessageFormat.format method raised an exception.
				logInternalError("enterMethod: " + e.getMessage());
			}
		}
	}

	// <summary>
	//   Overloaded. Enters a method by using the default log level. The
	//   resulting method name consists of the class name of the supplied
	//   instance parameter, followed by a dot and the supplied methodName
	//   argument.
	// </summary>
	// <param name="methodName">The name of the method.</param>
	// <param name="instance">
	//   The class name of this instance and a dot will be prepended
	//   to the method name.
	// </param>
	// <remarks>
	//   The enterMethod method notifies the Console that a new method
	//   has been entered. The Console includes the method in the method
	//   hierarchy. If this method is used consequently, a full call stack
	//   is visible in the Console which helps locating bugs in the source
	//   code. Please see the leaveMethod method as the counter piece to
	//   enterMethod.
	//
	//   The resulting method name consists of the class name of the
	//   supplied instance parameter, followed by a dot and the supplied
	//   methodName argument.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void enterMethod(Object instance, String methodName)
	{
		enterMethod(this.fParent.getDefaultLevel(), instance, methodName);
	}
	
	// <summary>
	//   Overloaded. Enters a method by using a custom log level. The
	//   resulting method name consists of the class name of the supplied
	//   instance parameter, followed by a dot and the supplied methodName
	//   argument.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="methodName">The name of the method.</param>
	// <param name="instance">
	//   The class name of this instance and a dot will be prepended
	//   to the method name.
	// </param>
	// <remarks>
	//   The enterMethod method notifies the Console that a new method
	//   has been entered. The Console includes the method in the method
	//   hierarchy. If this method is used consequently, a full call stack
	//   is visible in the Console which helps locating bugs in the source
	//   code. Please see the leaveMethod method as the counter piece to
	//   enterMethod.
	//
	//   The resulting method name consists of the class name of the
	//   supplied instance parameter, followed by a dot and the supplied
	//   methodName argument.
	// </remarks>

	public void enterMethod(Level level, Object instance, String methodName)
	{
		if (isOn(level))
		{
			if (instance == null)
			{
				// The supplied instance is null.
				logInternalError("enterMethod: instance argument is null");
			}
			else
			{
				String cls = instance.getClass().getName();
				enterMethod(level, cls + "." + methodName);
			}
		}
	}

	// <summary>
	//   Overloaded. Enters a method by using the default log level. The
	//   resulting method name consists of the class name of the supplied
	//   instance parameter, followed by a dot and the supplied format
	//   string and its related array of arguments.
	// </summary>
	// <param name="instance">
	//   The class name of this instance and a dot will be prepended
	//   to the method name.
	// </param>
	// <param name="methodNameFmt">
	//   The format string to create the name of the method.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   The enterMethod method notifies the Console that a new method
	//   has been entered. The Console includes the method in the method
	//   hierarchy. If this method is used consequently, a full call stack
	//   is visible in the Console which helps locating bugs in the source
	//   code. Please see the leaveMethod method as the counter piece to
	//   enterMethod.
	//
	//   The resulting method name consists of the class name of the
	//   supplied instance parameter, followed by a dot and the supplied
	//   format string and its related array of arguments.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void enterMethod(Object instance, String methodNameFmt, 
		Object[] args)
	{
		enterMethod(this.fParent.getDefaultLevel(), instance, methodNameFmt, 
			args);
	}

	// <summary>
	//   Overloaded. Enters a method by using a custom log level. The
	//   resulting method name consists of the class name of the supplied
	//   instance parameter, followed by a dot and the supplied format
	//   string and its related array of arguments.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="instance">
	//   The class name of this instance and a dot will be prepended
	//   to the method name.
	// </param>
	// <param name="methodNameFmt">
	//   The format string to create the name of the method.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   The enterMethod method notifies the Console that a new method
	//   has been entered. The Console includes the method in the method
	//   hierarchy. If this method is used consequently, a full call stack
	//   is visible in the Console which helps locating bugs in the source
	//   code. Please see the leaveMethod method as the counter piece to
	//   enterMethod.
	//
	//   The resulting method name consists of the class name of the
	//   supplied instance parameter, followed by a dot and the supplied
	//   format string and its related array of arguments.
	// </remarks>

	public void enterMethod(Level level, Object instance, 
		String methodNameFmt, Object[] args)
	{
		if (isOn(level))
		{
			if (instance == null)
			{
				// The supplied instance is null.
				logInternalError("enterMethod: instance argument is null");
			}
			else
			{
				try
				{
					enterMethod(
							level,
							instance.getClass().getName() + "." +
							MessageFormat.format(methodNameFmt, args)
						);
				}
				catch (Exception e)
				{
					// The MessageFormat.format method raised an exception.
					logInternalError("enterMethod: " + e.getMessage());
				}
			}
		}
	}

	// <summary>
	//   Overloaded. Leaves a method by using the default log level.
	// </summary>
	// <param name="methodName">The name of the method.</param>
	// <remarks>
	//   The leaveMethod method notifies the Console that a method has
	//   been left. The Console closes the current method in the method
	//   hierarchy. If this method is used consequently, a full call stack
	//   is visible in the Console which helps locating bugs in the source
	//   code. Please see the enterMethod method as the counter piece to
	//   leaveMethod.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void leaveMethod(String methodName)
	{
		leaveMethod(this.fParent.getDefaultLevel(), methodName);
	}
	
	// <summary>
	//   Overloaded. Leaves a method by using a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="methodName">The name of the method.</param>
	// <remarks>
	//   The leaveMethod method notifies the Console that a method has
	//   been left. The Console closes the current method in the method
	//   hierarchy. If this method is used consequently, a full call stack
	//   is visible in the Console which helps locating bugs in the source
	//   code. Please see the enterMethod method as the counter piece to
	//   leaveMethod.
	// </remarks>

	public void leaveMethod(Level level, String methodName)
	{
		if (isOn(level))
		{
			// Send two packets.
			sendLogEntry(level, methodName, LogEntryType.LeaveMethod, 
				ViewerId.Title);
			sendProcessFlow(level, methodName, ProcessFlowType.LeaveMethod);
		}
	}

	// <summary>
	//   Overloaded. Leaves a method by using the default log level. The
	//   method name consists of a format string and its related array of
	//   arguments.
	// </summary>
	// <param name="methodNameFmt">
	//   The format string to create the name of the method.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   The leaveMethod method notifies the Console that a method has
	//   been left. The Console closes the current method in the method
	//   hierarchy. If this method is used consequently, a full call stack
	//   is visible in the Console which helps locating bugs in the source
	//   code. Please see the enterMethod method as the counter piece to
	//   leaveMethod.
	//
	//   The resulting method name consists of a format string and the
	//   related array of arguments.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void leaveMethod(String methodNameFmt, Object[] args)
	{
		leaveMethod(this.fParent.getDefaultLevel(), methodNameFmt, args);
	}
	
	// <summary>
	//   Overloaded. Leaves a method by using a custom log level. The
	//   method name consists of a format string and its related array of
	//   arguments.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="methodNameFmt">
	//   The format string to create the name of the method.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   The leaveMethod method notifies the Console that a method has
	//   been left. The Console closes the current method in the method
	//   hierarchy. If this method is used consequently, a full call stack
	//   is visible in the Console which helps locating bugs in the source
	//   code. Please see the enterMethod method as the counter piece to
	//   leaveMethod.
	//
	//   The resulting method name consists of a format string and the
	//   related array of arguments.
	// </remarks>

	public void leaveMethod(Level level, String methodNameFmt, 
		Object[] args)
	{
		if (isOn(level))
		{
			try
			{
				leaveMethod(level, MessageFormat.format(methodNameFmt, args));
			}
			catch (Exception e)
			{
				// The MessageFormat.format method raised an exception.
				logInternalError("leaveMethod: " + e.getMessage());
			}
		}
	}

	// <summary>
	//   Overloaded. Leaves a method by using the default log level. The
	//   resulting method name consists of the class name of the supplied
	//   instance parameter, followed by a dot and the supplied methodName
	//   argument.
	// </summary>
	// <param name="instance">
	//   The class name of this instance and a dot will be prepended
	//   to the method name.
	// </param>
	// <param name="methodName">The name of the method.</param>
	// <remarks>
	//   The leaveMethod method notifies the Console that a method has
	//   been left. The Console closes the current method in the method
	//   hierarchy. If this method is used consequently, a full call stack
	//   is visible in the Console which helps locating bugs in the source
	//   code. Please see the enterMethod method as the counter piece to
	//   leaveMethod.
	//
	//   The resulting method name consists of the class name of the
	//   supplied instance parameter, followed by a dot and the supplied
	//   methodName argument.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void leaveMethod(Object instance, String methodName)
	{
		leaveMethod(this.fParent.getDefaultLevel(), instance, methodName);
	}
	
	// <summary>
	//   Overloaded. Leaves a method by using a custom log level. The
	//   resulting method name consists of the class name of the supplied
	//   instance parameter, followed by a dot and the supplied methodName
	//   argument.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="instance">
	//   The class name of this instance and a dot will be prepended
	//   to the method name.
	// </param>
	// <param name="methodName">The name of the method.</param>
	// <remarks>
	//   The leaveMethod method notifies the Console that a method has
	//   been left. The Console closes the current method in the method
	//   hierarchy. If this method is used consequently, a full call stack
	//   is visible in the Console which helps locating bugs in the source
	//   code. Please see the enterMethod method as the counter piece to
	//   leaveMethod.
	//
	//   The resulting method name consists of the class name of the
	//   supplied instance parameter, followed by a dot and the supplied
	//   methodName argument.
	// </remarks>

	public void leaveMethod(Level level, Object instance, String methodName)
	{
		if (isOn(level))
		{
			if (instance == null)
			{
				logInternalError("leaveMethod: instance argument is null");
			}
			else
			{
				String cls = instance.getClass().getName();
				leaveMethod(level,cls + "." + methodName);
			}
		}
	}

	// <summary>
	//   Overloaded. Leaves a method by using the default log level. The
	//   resulting method name consists of the class name of the supplied
	//   instance parameter, followed by a dot and the supplied format
	//   string and its related array of arguments.
	// </summary>
	// <param name="instance">
	//   The class name of this instance and a dot will be prepended
	//   to the method name.
	// </param>
	// <param name="methodNameFmt">
	//   The format string to create the name of the method.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   The leaveMethod method notifies the Console that a method has
	//   been left. The Console closes the current method in the method
	//   hierarchy. If this method is used consequently, a full call stack
	//   is visible in the Console which helps locating bugs in the source
	//   code. Please see the enterMethod method as the counter piece to
	//   leaveMethod.
	//
	//   The resulting method name consists of the class name of the
	//   supplied instance parameter, followed by a dot and the supplied
	//   format string and its related array of arguments.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void leaveMethod(Object instance, String methodNameFmt, 
		Object[] args)
	{
		leaveMethod(this.fParent.getDefaultLevel(), instance, methodNameFmt, 
			args);
	}
	
	// <summary>
	//   Overloaded. Leaves a method by using a custom log level. The
	//   resulting method name consists of the class name of the supplied
	//   instance parameter, followed by a dot and the supplied format
	//   string and its related array of arguments.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="instance">
	//   The class name of this instance and a dot will be prepended
	//   to the method name.
	// </param>
	// <param name="methodNameFmt">
	//   The format string to create the name of the method.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   The leaveMethod method notifies the Console that a method has
	//   been left. The Console closes the current method in the method
	//   hierarchy. If this method is used consequently, a full call stack
	//   is visible in the Console which helps locating bugs in the source
	//   code. Please see the enterMethod method as the counter piece to
	//   leaveMethod.
	//
	//   The resulting method name consists of the class name of the
	//   supplied instance parameter, followed by a dot and the supplied
	//   format string and its related array of arguments.
	// </remarks>

	public void leaveMethod(Level level, Object instance, 
		String methodNameFmt, Object[] args)
	{
		if (isOn(level))
		{
			if (instance == null)
			{
				logInternalError("leaveMethod: instance argument is null");
			}
			else
			{
				try
				{
					leaveMethod(
							level,
							instance.getClass().getName() + "." +
							MessageFormat.format(methodNameFmt, args)
					);
				}
				catch (Exception e)
				{
					// The MessageFormat.format method raised an exception.
					logInternalError("leaveMethod: " + e.getMessage());
				}
			}
		}
	}

	// <summary>
	//   Overloaded. Enters a new thread by using the default log level.
	// </summary>
	// <param name="threadName">The name of the thread.</param>
	// <remarks>
	//   The enterThread method notifies the Console that a new thread
	//   has been entered. The Console display this thread in the Process
	//   Flow toolbox. If this method is used consequently, all threads
	//   of a process are displayed. Please see the leaveThread method as
	//   the counter piece to enterThread.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void enterThread(String threadName)
	{
		enterThread(this.fParent.getDefaultLevel(), threadName);
	}
	
	// <summary>
	//   Overloaded. Enters a new thread by using a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="threadName">The name of the thread.</param>
	// <remarks>
	//   The enterThread method notifies the Console that a new thread
	//   has been entered. The Console display this thread in the Process
	//   Flow toolbox. If this method is used consequently, all threads
	//   of a process are displayed. Please see the leaveThread method as
	//   the counter piece to enterThread.
	// </remarks>

	public void enterThread(Level level, String threadName)
	{
		if (isOn(level))
		{
			sendProcessFlow(level, threadName, ProcessFlowType.EnterThread);
		}
	}

	// <summary>
	//   Overloaded. Enters a new thread by using the default log level.
	//   The thread name consists of a format string the related array of
	//   arguments.
	// </summary>
	// <param name="threadNameFmt">
	//   The format string to create the name of the thread.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   The enterThread method notifies the Console that a new thread
	//   has been entered. The Console display this thread in the Process
	//   Flow toolbox. If this method is used consequently, all threads
	//   of a process are displayed. Please see the leaveThread method as
	//   the counter piece to enterThread.
	//
	//   The resulting thread name consists of a format string and the
	//   related array of arguments.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void enterThread(String threadNameFmt, Object[] args)
	{
		enterThread(this.fParent.getDefaultLevel(), threadNameFmt, args);
	}
	
	// <summary>
	//   Overloaded. Enters a new thread by using a custom log level.
	//   The thread name consists of a format string the related array of
	//   arguments.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="threadNameFmt">
	//   The format string to create the name of the thread.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   The enterThread method notifies the Console that a new thread
	//   has been entered. The Console display this thread in the Process
	//   Flow toolbox. If this method is used consequently, all threads
	//   of a process are displayed. Please see the leaveThread method as
	//   the counter piece to enterThread.
	//
	//   The resulting thread name consists of a format string and the
	//   related array of arguments.
	// </remarks>

	public void enterThread(Level level, String threadNameFmt, 
		Object[] args)
	{
		if (isOn(level))
		{
			try
			{
				enterThread(level, MessageFormat.format(threadNameFmt, args));
			}
			catch (Exception e)
			{
				// The MessageFormat.format method raised an exception.
				logInternalError("enterThread: " + e.getMessage());
			}
		}
	}

	// <summary>
	//   Overloaded. Leaves a thread by using the default log level.
	// </summary>
	// <param name="threadName">The name of the thread.</param>
	// <remarks>
	//   The leaveThread method notifies the Console that a thread has
	//   been finished. The Console display this change in the Process
	//   Flow toolbox. Please see the enterThread method as the counter
	//   piece to leaveThread.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void leaveThread(String threadName)
	{
		leaveThread(this.fParent.getDefaultLevel(), threadName);
	}
	
	// <summary>
	//   Overloaded. Leaves a thread by using a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="threadName">The name of the thread.</param>
	// <remarks>
	//   The leaveThread method notifies the Console that a thread has
	//   been finished. The Console display this change in the Process
	//   Flow toolbox. Please see the enterThread method as the counter
	//   piece to leaveThread.
	// </remarks>

	public void leaveThread(Level level, String threadName)
	{
		if (isOn(level))
		{
			sendProcessFlow(level, threadName, ProcessFlowType.LeaveThread);
		}
	}

	// <summary>
	//   Overloaded. Leaves a thread by using the default log level.
	//   The thread name consists of a format string and the related
	//   array of arguments.
	// </summary>
	// <param name="threadNameFmt">
	//   The format string to create the name of the thread.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   The leaveThread method notifies the Console that a thread has
	//   been finished. The Console display this change in the Process
	//   Flow toolbox. Please see the enterThread method as the counter
	//   piece to leaveThread.
	// 
	//   The resulting thread name consists of a format string and the
	//   related array of arguments.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void leaveThread(String threadNameFmt, Object[] args)
	{
		leaveThread(this.fParent.getDefaultLevel(), threadNameFmt, args);
	}
	
	// <summary>
	//   Overloaded. Leaves a thread by using a custom log level.
	//   The thread name consists of a format string and the related
	//   array of arguments.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="threadNameFmt">
	//   The format string to create the name of the thread.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   The leaveThread method notifies the Console that a thread has
	//   been finished. The Console display this change in the Process
	//   Flow toolbox. Please see the enterThread method as the counter
	//   piece to leaveThread.
	// 
	//   The resulting thread name consists of a format string and the
	//   related array of arguments.
	// </remarks>

	public void leaveThread(Level level, String threadNameFmt, Object[] args)
	{
		if (isOn(level))
		{
			try
			{
				leaveThread(level, MessageFormat.format(threadNameFmt, args));
			}
			catch (Exception e)
			{
				// The MessageFormat.format method raised an exception.
				logInternalError("leaveThread: " + e.getMessage());
			}
		}
	}

	// <summary>
	//   Overloaded. Enters a new process by using the default log level
	//   and the parent's application name as process name.
	// </summary>
	// <remarks>
	//   The enterProcess method notifies the Console that a new
	//   process has been entered. The Console displays this process
	//   in the Process Flow toolbox. Please see the leaveProcess
	//   method as the counter piece to enterProcess.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void enterProcess()
	{
		enterProcess(this.fParent.getDefaultLevel());	
	}
	
	// <summary>
	//   Overloaded. Enters a new process by using a custom log level
	//   and the parent's application name as process name.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <remarks>
	//   The enterProcess method notifies the Console that a new
	//   process has been entered. The Console displays this process
	//   in the Process Flow toolbox. Please see the leaveProcess
	//   method as the counter piece to enterProcess.
	// </remarks>

	public void enterProcess(Level level)
	{
		if (isOn(level))
		{
			sendProcessFlow(level, this.fParent.getAppName(), 
				ProcessFlowType.EnterProcess);
			sendProcessFlow(level, "Main Thread", ProcessFlowType.EnterThread);
		}
	}
	
	// <summary>
	//   Overloaded. Enters a new process by using the default log level.
	// </summary>
	// <param name="processName">The name of the process.</param>
	// <remarks>
	//   The enterProcess method notifies the Console that a new
	//   process has been entered. The Console displays this process
	//   in the Process Flow toolbox. Please see the leaveProcess
	//   method as the counter piece to enterProcess.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void enterProcess(String processName)
	{
		enterProcess(this.fParent.getDefaultLevel(), processName);
	}
	
	// <summary>
	//   Overloaded. Enters a new process by using a custom log level.
	// </summary>
	// <param name="processName">The name of the process.</param>
	// <remarks>
	//   The enterProcess method notifies the Console that a new
	//   process has been entered. The Console displays this process
	//   in the Process Flow toolbox. Please see the leaveProcess
	//   method as the counter piece to enterProcess.
	// </remarks>

	public void enterProcess(Level level, String processName)
	{
		if (isOn(level))
		{
			sendProcessFlow(level, processName, ProcessFlowType.EnterProcess);
			sendProcessFlow(level, "Main Thread", ProcessFlowType.EnterThread);
		}
	}

	// <summary>
	//   Overloaded. Enters a new process by using the default log level.
	//   The process name consists of a format string and the related
	//   array of arguments.
	// </summary>
	// <param name="processNameFmt">
	//   The format string to create the name of the process.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   The enterProcess method notifies the Console that a new
	//   process has been entered. The Console displays this process
	//   in the Process Flow toolbox. Please see the leaveProcess
	//   method as the counter piece to enterProcess.
	//
	//   The resulting process name consists of a format string and
	//   the related array of arguments.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void enterProcess(String processNameFmt, Object[] args)
	{
		enterProcess(this.fParent.getDefaultLevel(), processNameFmt, args);
	}
	
	// <summary>
	//   Overloaded. Enters a new process by using a custom log level.
	//   The process name consists of a format string and the related
	//   array of arguments.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="processNameFmt">
	//   The format string to create the name of the process.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   The enterProcess method notifies the Console that a new
	//   process has been entered. The Console displays this process
	//   in the Process Flow toolbox. Please see the leaveProcess
	//   method as the counter piece to enterProcess.
	//
	//   The resulting process name consists of a format string and
	//   the related array of arguments.
	// </remarks>

	public void enterProcess(Level level, String processNameFmt, 
		Object[] args)
	{
		if (isOn(level))
		{
			try
			{
				enterProcess(level, MessageFormat.format(processNameFmt, args));
			}
			catch (Exception e)
			{
				// The MessageFormat.format method raised an exception.
				logInternalError("enterProcess: " + e.getMessage());
			}
		}
	}

	// <summary>
	//   Overloaded. Leaves a process by using the default log level
	//   and the parent's application name as process name.
	// </summary>
	// <remarks>
	//   The leaveProcess method notifies the Console that a process
	//   has finished. The Console displays this change in the Process
	//   Flow toolbox. Please see the enterProcess method as the
	//   counter piece to leaveProcess.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void leaveProcess()
	{
		leaveProcess(this.fParent.getDefaultLevel());
	}
	
	// <summary>
	//   Overloaded. Leaves a process by using a custom log level
	//   and the parent's application name as process name.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <remarks>
	//   The leaveProcess method notifies the Console that a process
	//   has finished. The Console displays this change in the Process
	//   Flow toolbox. Please see the enterProcess method as the
	//   counter piece to leaveProcess.
	// </remarks>

	public void leaveProcess(Level level)
	{
		if (isOn(level))
		{
			sendProcessFlow(level, "Main Thread", ProcessFlowType.LeaveThread);
			sendProcessFlow(level, this.fParent.getAppName(), 
				ProcessFlowType.LeaveProcess);
		}
	}	
	
	// <summary>
	//   Overloaded. Leaves a process by using the default log level.
	// </summary>
	// <param name="processName">The name of the process.</param>
	// <remarks>
	//   The leaveProcess method notifies the Console that a process
	//   has finished. The Console displays this change in the Process
	//   Flow toolbox. Please see the enterProcess method as the
	//   counter piece to leaveProcess.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void leaveProcess(String processName)
	{
		leaveProcess(this.fParent.getDefaultLevel(), processName);
	}
	
	// <summary>
	//   Overloaded. Leaves a process by using a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="processName">The name of the process.</param>
	// <remarks>
	//   The leaveProcess method notifies the Console that a process
	//   has finished. The Console displays this change in the Process
	//   Flow toolbox. Please see the enterProcess method as the
	//   counter piece to leaveProcess.
	// </remarks>

	public void leaveProcess(Level level, String processName)
	{
		if (isOn(level))
		{
			sendProcessFlow(level, "Main Thread", ProcessFlowType.LeaveThread);
			sendProcessFlow(level, processName, ProcessFlowType.LeaveProcess);
		}
	}

	// <summary>
	//   Overloaded. Leaves a process by using the default log level.
	//   The process name consists of a format string and the related
	//   array of arguments.
	// </summary>
	// <param name="processNameFmt">
	//   The format string to create the name of the process.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   The leaveProcess method notifies the Console that a process
	//   has finished. The Console displays this change in the Process
	//   Flow toolbox. Please see the enterProcess method as the
	//   counter piece to leaveProcess.
	//
	//   The resulting process name consists of a format string and
	//   the related array of arguments.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void leaveProcess(String processNameFmt, Object[] args)
	{
		leaveProcess(this.fParent.getDefaultLevel(), processNameFmt, args);
	}
	
	// <summary>
	//   Overloaded. Leaves a process by using a custom log level.
	//   The process name consists of a format string and the related
	//   array of arguments.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="processNameFmt">
	//   The format string to create the name of the process.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   The leaveProcess method notifies the Console that a process
	//   has finished. The Console displays this change in the Process
	//   Flow toolbox. Please see the enterProcess method as the
	//   counter piece to leaveProcess.
	//
	//   The resulting process name consists of a format string and
	//   the related array of arguments.
	// </remarks>

	public void leaveProcess(Level level, String processNameFmt, 
		Object[] args)
	{
		if (isOn(level))
		{
			try
			{
				leaveProcess(level, MessageFormat.format(processNameFmt, args));
			}
			catch (Exception e)
			{
				// The MessageFormat.format method raised an exception.
				logInternalError("leaveProcess: " + e.getMessage());
			}
		}
	}

	// <summary>
	//   Overloaded. Logs a colored message with the default log
	//   level.
	// </summary>
	// <param name="color">The background color in the Console.</param>
	// <param name="title">The message to log.</param>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void logColored(Color color, String title)
	{
		logColored(this.fParent.getDefaultLevel(), color, title);
	}
	
	// <summary>
	//   Overloaded. Logs a colored message with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="color">The background color in the Console.</param>
	// <param name="title">The message to log.</param>
	
	public void logColored(Level level, Color color, String title)
	{
		if (isOn(level))
		{
			sendLogEntry(level, title, LogEntryType.Message, ViewerId.Title,
				color, null);
		}
	}

	// <summary>
	//   Overloaded. Logs a colored message with the default log level.
	//   The message is created with a format string and a related array
	//   of arguments.
	// </summary>
	// <param name="color">The background color in the Console.</param>
	// <param name="titleFmt">
	//   A format string to create the message.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   This version of the method accepts a format string and a
	//   related array of arguments. These parameters will be passed
	//   to the MessageFormat.format method and the resulting string
	//   will be the message.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logColored(Color color, String titleFmt, Object[] args)
	{
		logColored(this.fParent.getDefaultLevel(), color, titleFmt, args);
	}
	
	// <summary>
	//   Overloaded. Logs a colored message with a custom log level.
	//   The message is created with a format string and a related array
	//   of arguments.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="color">The background color in the Console.</param>
	// <param name="titleFmt">
	//   A format string to create the message.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   This version of the method accepts a format string and a
	//   related array of arguments. These parameters will be passed
	//   to the MessageFormat.format method and the resulting string
	//   will be the message.
	// </remarks>

	public void logColored(Level level, Color color, String titleFmt, 
		Object[] args)
	{
		if (isOn(level))
		{
			try
			{
				logColored(level, color, 
					MessageFormat.format(titleFmt, args));
			}
			catch (Exception e)
			{
				// The MessageFormat.format method raised an exception.
				logInternalError("logColored: " + e.getMessage());
			}
		}
	}

	// <summary>
	//   Overloaded. Logs a debug message with a log level of 
	//   Level.Debug.
	// </summary>
	// <param name="title">The message to log.</param>

	public void logDebug(String title)
	{
		if (isOn(Level.Debug))
		{
			sendLogEntry(Level.Debug, title, LogEntryType.Debug, 
				ViewerId.Title);
		}
	}

	// <summary>
	//   Overloaded. Logs a debug message with a log level of
	//   Level.Debug. The message is created  with a format string
	//   and a related array of arguments.
	// </summary>
	// <param name="titleFmt">
	//   A format string to create the message.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   This version of the method accepts a format string and a
	//   related array of arguments. These parameters will be passed
	//   to the MessageFormat.format method and the resulting string
	//   will be the message.
	// </remarks>

	public void logDebug(String titleFmt, Object[] args)
	{
		if (isOn(Level.Debug))
		{
			try
			{
				logDebug(MessageFormat.format(titleFmt, args));
			}
			catch (Exception e)
			{
				// The MessageFormat.format method raised an exception.
				logInternalError("logDebug: " + e.getMessage());
			}
		}
	}	

	// <summary>
	//   Overloaded. Logs a verbose message with a log level of
	//   Level.Verbose.
	// </summary>
	// <param name="title">The message to log.</param>

	public void logVerbose(String title)
	{
		if (isOn(Level.Verbose))
		{
			sendLogEntry(Level.Verbose, title, LogEntryType.Verbose, 
				ViewerId.Title);
		}
	}

	// <summary>
	//   Overloaded. Logs a verbose message with a log level of
	//   Level.Verbose. The message is created with a format string
	//   and a related array of arguments.
	// </summary>
	// <param name="titleFmt">
	//   A format string to create the message.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   This version of the method accepts a format string and a
	//   related array of arguments. These parameters will be passed
	//   to the MessageFormat.format method and the resulting string
	//   will be the message.
	// </remarks>

	public void logVerbose(String titleFmt, Object[] args)
	{
		if (isOn(Level.Verbose))
		{
			try
			{
				logVerbose(MessageFormat.format(titleFmt, args));
			}
			catch (Exception e)
			{
				// The MessageFormat.format method raised an exception.
				logInternalError("logVerbose: " + e.getMessage());
			}
		}
	}	
	
	// <summary>
	//   Overloaded. Logs a simple message with a log level of
	//   Level.Message.
	// </summary>
	// <param name="title">The message to log.</param>

	public void logMessage(String title)
	{
		if (isOn(Level.Message))
		{
			sendLogEntry(Level.Message, title, LogEntryType.Message, 
				ViewerId.Title);
		}
	}

	// <summary>
	//   Overloaded. Logs a simple message with a log level of
	//   Level.Message. The message is created with a format string
	//   and a related array of arguments.
	// </summary>
	// <param name="titleFmt">
	//   A format string to create the message.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   This version of the method accepts a format string and a
	//   related array of arguments. These parameters will be passed
	//   to the MessageFormat.format method and the resulting string
	//   will be the message.
	// </remarks>

	public void logMessage(String titleFmt, Object[] args)
	{
		if (isOn(Level.Message))
		{
			try
			{
				logMessage(MessageFormat.format(titleFmt, args));
			}
			catch (Exception e)
			{
				// The MessageFormat.format method raised an exception.
				logInternalError("logMessage: " + e.getMessage());
			}
		}
	}

	// <summary>
	//   Overloaded. Logs a warning message with a log level of
	//   Level.Warning.
	// </summary>
	// <param name="title">The warning to log.</param>

	public void logWarning(String title)
	{
		if (isOn(Level.Warning))
		{
			sendLogEntry(Level.Warning, title, LogEntryType.Warning, 
				ViewerId.Title);
		}
	}

	// <summary>
	//   Overloaded. Logs a warning message with a log level of
	//   Level.Warning. The warning message is created with a format
	//   string and a related array of arguments.
	// </summary>
	// <param name="titleFmt">
	//   A format string to create the warning.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   This version of the method accepts a format string and a
	//   related array of arguments. These parameters will be passed
	//   to the MessageFormat.format method and the resulting string
	//   will be the warning message.
	// </remarks>

	public void logWarning(String titleFmt, Object[] args)
	{
		if (isOn(Level.Warning))
		{
			try
			{
				logWarning(MessageFormat.format(titleFmt, args));
			}
			catch (Exception e)
			{
				// The MessageFormat.format method raised an exception.
				logInternalError("logWarning: " + e.getMessage());
			}
		}
	}

	// <summary>
	//   Overloaded. Logs an error message with a log level of
	//   Level.Error.
	// </summary>
	// <param name="title">
	//   A string which describes the error.
	// </param>
	// <remarks>
	//   This method is ideally used in error handling code such as
	//   exception handlers. If this method is used consequently, it
	//   is easy to troubleshoot and solve bugs in applications or
	//   configurations. See logException for a similar method.
	// </remarks>

	public void logError(String title)
	{
		if (isOn(Level.Error))
		{
			sendLogEntry(Level.Error, title, LogEntryType.Error, 
				ViewerId.Title);
		}
	}

	// <summary>
	//   Overloaded. Logs an error message with a log level of
	//   Level.Error. The error message is created with a format
	//   string and a related array of arguments.
	// </summary>
	// <param name="titleFmt">
	//   A format string to create a description of the error.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   This version of the method accepts a format string and a
	//   related array of arguments. These parameters will be passed
	//   to the MessageFormat.format method and the resulting string
	//   will be the error message.
	//
	//   This method is ideally used in error handling code such as
	//   exception handlers. If this method is used consequently, it
	//   is easy to troubleshoot and solve bugs in applications or
	//   configurations. See logException for a similar method.
	// </remarks>

	public void logError(String titleFmt, Object[] args)
	{
		if (isOn(Level.Error))
		{
			try
			{
				logError(MessageFormat.format(titleFmt, args));
			}
			catch (Exception e)
			{
				// The MessageFormat.format method raised an exception.
				logInternalError("logError: " + e.getMessage());
			}
		}
	}

	// <summary>
	//   Overloaded. Logs a fatal error message with a log level of
	//   Level.Fatal.
	// </summary>
	// <param name="title">
	//   A string which describes the fatal error.
	// </param>
	// <remarks>
	//   This method is ideally used in error handling code such as
	//   exception handlers. If this method is used consequently, it
	//   is easy to troubleshoot and solve bugs in applications or
	//   configurations. See logError for a method which does not
	//   describe fatal but recoverable errors.
	// </remarks>

	public void logFatal(String title)
	{
		if (isOn(Level.Fatal))
		{
			sendLogEntry(Level.Fatal, title, LogEntryType.Fatal, 
				ViewerId.Title);
		}
	}

	// <summary>
	//   Overloaded. Logs a fatal error message with a log level of
	//   Level.Fatal. The error message is created with a format
	//   string and a related array of arguments.
	// </summary>
	// <param name="titleFmt">
	//   A format string to create a description of the fatal error.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   This version of the method accepts a format string and a
	//   related array of arguments. These parameters will be passed
	//   to the MessageFormat.format method and the resulting string
	//   will be the fatal error message.
	//
	//   This method is ideally used in error handling code such as
	//   exception handlers. If this method is used consequently, it
	//   is easy to troubleshoot and solve bugs in applications or
	//   configurations. See logError for a method which does not
	//   describe fatal but recoverable errors.
	// </remarks>

	public void logFatal(String titleFmt, Object[] args)
	{
		if (isOn(Level.Fatal))
		{
			try
			{
				logFatal(MessageFormat.format(titleFmt, args));
			}
			catch (Exception e)
			{
				// The MessageFormat.format method raised an exception.
				logInternalError("logFatal: " + e.getMessage());
			}
		}
	}

	// <summary>
	//   Overloaded. Logs an internal error with a log level of
	//   Level.Error.
	// </summary>
	// <param name="title">
	//   A string which describes the internal error.
	// </param>
	// <remarks>
	//   This method logs an internal error. Such errors can occur 
	//   if session methods are invoked with invalid arguments. For
	//   example, if you pass an invalid format string to logMessage,
	//   the exception will be caught and an internal error with the
	//   exception message will be sent.
	//
	//   This method is also intended to be used in derived classes
	//   to report any errors in your own methods.
	// </remarks>

	protected void logInternalError(String title)
	{
		if (isOn(Level.Error))
		{
			sendLogEntry(Level.Error, title, LogEntryType.InternalError, 
				ViewerId.Title);
		}
	}

	// <summary>
	//   Overloaded. Logs an internal error with a log level of
	//   Level.Error. The error message is created with a format
	//   string and a related array of
	//   arguments.
	// </summary>
	// <param name="titleFmt">
	//   A format string to create a description of the internal error.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   This method logs an internal error. Such errors can occur 
	//   if session methods are invoked with invalid arguments. For
	//   example, if you pass an invalid format string to logMessage,
	//   the exception will be caught and an internal error with the
	//   exception message will be sent.
	//
	//   This version of the method accepts a format string and a
	//   related array of arguments. These parameters will be passed
	//   to the MessageFormat.format method and the resulting string
	//   will be the error message.
	//
	//   This method is also intended to be used in derived classes
	//   to report any errors in your own methods.
	// </remarks>

	protected void logInternalError(String titleFmt, 
		Object[] args)
	{
		if (isOn(Level.Error))
		{
			try
			{
				logInternalError(MessageFormat.format(titleFmt, args));
			}
			catch (Exception e)
			{
				// The MessageFormat.format method raised an exception.
				logInternalError("logInternalError: " + e.getMessage());
			}
		}
	}

	// <summary>
	//   Overloaded. Increments the default checkpoint counter and logs
	//   a message with the default log level.
	// </summary>
	// <seealso cref="com.gurock.smartinspect.session.Session.logSeparator"/>
	// <seealso cref="com.gurock.smartinspect.session.Session.resetCheckpoint"/>
	// <remarks>
	//   This method increments a checkpoint counter and then logs a
	//   message using "Checkpoint #N" as title. The initial value of
	//   the checkpoint counter is 0. You can use the resetCheckpoint
	//   method to reset the counter to 0 again.
	//
	//   This method is useful, for example, for tracking loops. If
	//   addCheckpoint is called for each iteration of a loop, it is
	//   easy to follow the execution of the loop in question. This
	//   method can also be used in recursive methods to understand
	//   the execution flow. Furthermore you can use it to highlight
	//   important parts of your code. See logSeparator for a method
	//   with a similar intention.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void addCheckpoint()
	{
		addCheckpoint(this.fParent.getDefaultLevel());
	}
	
	// <summary>
	//   Overloaded. Increments the default checkpoint counter and logs
	//   a message with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <seealso cref="com.gurock.smartinspect.session.Session.logSeparator"/>
	// <seealso cref="com.gurock.smartinspect.session.Session.resetCheckpoint"/>
	// <remarks>
	//   This method increments a checkpoint counter and then logs a
	//   message using "Checkpoint #N" as title. The initial value of
	//   the checkpoint counter is 0. You can use the resetCheckpoint
	//   method to reset the counter to 0 again.
	//
	//   This method is useful, for example, for tracking loops. If
	//   addCheckpoint is called for each iteration of a loop, it is
	//   easy to follow the execution of the loop in question. This
	//   method can also be used in recursive methods to understand
	//   the execution flow. Furthermore you can use it to highlight
	//   important parts of your code. See logSeparator for a method
	//   with a similar intention.
	// </remarks>

	public void addCheckpoint(Level level)
	{
		if (isOn(level))
		{
			int counter;
			
			synchronized (this.fCheckpointLock)
			{
				counter = ++this.fCheckpointCounter;
			}

			String title = "Checkpoint #" + counter;
			sendLogEntry(level, title, LogEntryType.Checkpoint, 
				ViewerId.Title);
		}
	}
	
	// <summary>
	//   Overloaded. Increments the counter of a named checkpoint and
	//   logs a message with the default log level.
	// </summary>
	// <param name="name">
	//   The name of the checkpoint to increment.
	// </param>
	// <remarks>
	//   This method increments the counter for the given checkpoint
	//   and then logs a message using "%checkpoint% #N" as title where
	//   %checkpoint% stands for the name of the checkpoint and N for
	//   the incremented counter value. The initial value of the counter
	//   for a given checkpoint is 0. You can use the resetCheckpoint
	//   method to reset the counter to 0 again.
	// 
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void addCheckpoint(String name)
	{
		addCheckpoint(this.fParent.getDefaultLevel(), name, null);
	}
	
	// <summary>
	//   Overloaded. Increments the counter of a named checkpoint and
	//   logs a message with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">
	//   The name of the checkpoint to increment.
	// </param>
	// <remarks>
	//   This method increments the counter for the given checkpoint
	//   and then logs a message using "%checkpoint% #N" as title where
	//   %checkpoint% stands for the name of the checkpoint and N for
	//   the incremented counter value. The initial value of the counter
	//   for a given checkpoint is 0. You can use the resetCheckpoint
	//   method to reset the counter to 0 again.
	// </remarks>
	
	public void addCheckpoint(Level level, String name)
	{
		addCheckpoint(level, name, null);
	}

	// <summary>
	//   Overloaded. Increments the counter of a named checkpoint and
	//   logs a message with the default log level and an optional
	//   message.
	// </summary>
	// <param name="name">
	//   The name of the checkpoint to increment.
	// </param>
	// <param name="details">
	//   An optional message to include in the resulting log entry.
	//   Can be null.
	// </param>
	// <remarks>
	//   This method increments the counter for the given checkpoint
	//   and then logs a message using "%checkpoint% #N" as title where
	//   %checkpoint% stands for the name of the checkpoint and N for
	//   the incremented counter value. The initial value of the counter
	//   for a given checkpoint is 0. Specify the details parameter to
	//   include an optional message in the resulting log entry. You
	//   can use the resetCheckpoint method to reset the counter to 0
	//   again. 
	// 
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void addCheckpoint(String name, String details)
	{
		addCheckpoint(this.fParent.getDefaultLevel(), name, details);
	}
	
	// <summary>
	//   Overloaded. Increments the counter of a named checkpoint and
	//   logs a message with a custom log level and an optional
	//   message.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">
	//   The name of the checkpoint to increment.
	// </param>
	// <param name="details">
	//   An optional message to include in the resulting log entry.
	//   Can be null.
	// </param>
	// <remarks>
	//   This method increments the counter for the given checkpoint
	//   and then logs a message using "%checkpoint% #N" as title where
	//   %checkpoint% stands for the name of the checkpoint and N for
	//   the incremented counter value. The initial value of the counter
	//   for a given checkpoint is 0. Specify the details parameter to
	//   include an optional message in the resulting log entry. You
	//   can use the resetCheckpoint method to reset the counter to 0
	//   again. 
	// </remarks>
	
	public void addCheckpoint(Level level, String name, String details)
	{
		if (isOn(level))
		{
			if (name == null)
			{
				logInternalError("addCheckpoint: name argument is null");
				return;
			}
			
			int value;
			String key = name.toLowerCase();
			
			synchronized (this.fCheckpoints)
			{
				if (this.fCheckpoints.containsKey(key))
				{
					Integer i = (Integer) this.fCheckpoints.get(key);
					value = i.intValue();
				}
				else 
				{
					value = 0;
				}
				
				value++;
				this.fCheckpoints.put(key, new Integer(value));
			}
			
			StringBuffer sb = new StringBuffer();
			sb.append(name);
			sb.append(" #");
			sb.append(value);
			
			if (details != null)
			{
				sb.append(" (");
				sb.append(details);
				sb.append(")");
			}
			
			String title = sb.toString();
			sendLogEntry(level, title, LogEntryType.Checkpoint,
				ViewerId.Title);
		}
	}

	// <summary>
	//   Overloaded. Resets the default checkpoint counter.
	// </summary>
	// <seealso cref="com.gurock.smartinspect.session.Session.addCheckpoint"/>
	// <remarks>
	//   This method resets the default checkpoint counter to 0. The
	//   checkpoint counter is used by the addCheckpoint method.
	// </remarks>

	public void resetCheckpoint()
	{
		synchronized (this.fCheckpointLock)
		{
			this.fCheckpointCounter = 0;
		}
	}

	// <summary>
	//   Overloaded. Resets the counter of a named checkpoint.
	// </summary>
	// <param name="name">The name of the checkpoint to reset.</param>
	// <remarks>
	//   This method resets the counter of the given named checkpoint.
	//   Named checkpoints can be incremented and logged with the
	//   addCheckpoint method.
	// </remarks>
	
	public void resetCheckpoint(String name)
	{
		if (name == null)
		{
			logInternalError("resetCheckpoint: name argument is null");
			return;
		}
		
		String key = name.toLowerCase();
		
		synchronized (this.fCheckpoints)
		{
			this.fCheckpoints.remove(key);
		}
	}
	
	// <summary>
	//   Overloaded. Logs an assert message if a condition is false with
	//   a log level of Level.Error.
	// </summary>
	// <param name="condition">The condition to check.</param>
	// <param name="title">The title of the Log Entry.</param>
	// <remarks>
	//   An assert message is logged if this method is called with a
	//   condition parameter of the value false. No <link LogEntry,
	//   Log Entry> is generated if this method is called with a
	//   condition parameter of the value true.
	//
	//   A typical usage of this method would be to test if a variable
	//   is not set to null before you use it. To do this, you just need
	//   to insert a logAssert call to the code section in question with
	//   "instance != null" as first parameter. If the reference is null
	//   and thus the expression evaluates to false, a message is logged.
	// </remarks>

	public void logAssert(boolean condition, String title)
	{
		if (isOn(Level.Error))
		{
			if (!condition)
			{
				sendLogEntry(Level.Error, title, LogEntryType.Assert,
					ViewerId.Title);
			}
		}
	}

	// <summary>
	//   Overloaded. Logs an assert message if a condition is false with
	//   a log level of Lever.Error. The assert message is created with a
	//   format string and a related array of arguments.
	// </summary>
	// <param name="condition">The condition to check.</param>
	// <param name="titleFmt">
	//   The format string to create the title of the Log Entry.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	// <remarks>
	//   An assert message is logged if this method is called with a
	//   condition parameter of the value false. No <link LogEntry,
	//   Log Entry> is generated if this method is called with a
	//   condition parameter of the value true.
	//
	//   This version of the method accepts a format string and a
	//   related array of arguments. These parameters will be passed
	//   to the MessageFormat.format method and the resulting string
	//   will be the assert message.
	//
	//   A typical usage of this method would be to test if a variable
	//   is not set to null before you use it. To do this, you just need
	//   to insert a logAssert call to the code section in question with
	//   "instance != null" as first parameter. If the reference is null
	//   and thus the expression evaluates to false, a message is logged.
	// </remarks>

	public void logAssert(boolean condition, String titleFmt, Object[] args)
	{
		if (isOn(Level.Error))
		{
			if (!condition)
			{
				try
				{
					String title = MessageFormat.format(titleFmt, args);
					sendLogEntry(Level.Error, title, LogEntryType.Assert,
						ViewerId.Title);
				}
				catch (Exception e)
				{
					// The MessageFormat.format method raised an exception.
					logInternalError("logAssert: " + e.getMessage());
				}
			}
		}
	}

	// <summary>
	//   Overloaded. Logs whether a variable is assigned or not with the
	//   default log level.
	// </summary>
	// <param name="title">The title of the variable.</param>
	// <param name="instance">
	//   The variable which should be checked for null.
	// </param>
	// <remarks>
	//   If the instance argument is null, then ": Not assigned",
	//   otherwise ": Assigned" will be appended to the title before
	//   the <link LogEntry, Log Entry> is sent.
	//
	//   This method is useful to check source code for null references
	//   in places where you experienced or expect problems and want to
	//   log possible null references.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logAssigned(String title, Object instance)
	{
		logAssigned(this.fParent.getDefaultLevel(), title, instance);
	}
	
	// <summary>
	//   Overloaded. Logs whether a variable is assigned or not with a
	//   custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title of the variable.</param>
	// <param name="instance">
	//   The variable which should be checked for null.
	// </param>
	// <remarks>
	//   If the instance argument is null, then ": Not assigned",
	//   otherwise ": Assigned" will be appended to the title before
	//   the <link LogEntry, Log Entry> is sent.
	//
	//   This method is useful to check source code for null references
	//   in places where you experienced or expect problems and want to
	//   log possible null references.
	// </remarks>

	public void logAssigned(Level level, String title, Object instance)
	{
		if (isOn(level))
		{
			if (instance != null)
			{
				logMessage(title + ": Assigned");
			}
			else
			{
				logMessage(title + ": Not assigned");
			}
		}
	}
	
	// <summary>
	//   Overloaded. Logs a conditional message with the default log
	//   level.
	// </summary>
	// <param name="condition">The condition to evaluate.</param>
	// <param name="title">The title of the conditional message.</param>
	// <remarks>
	//   This method only sends a message if the passed 'condition'
	//   argument evaluates to true. If 'condition' is false, this
	//   method has no effect and nothing is logged. This method is
	//   thus the counter piece to logAssert.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logConditional(boolean condition, String title)
	{
		logConditional(this.fParent.getDefaultLevel(), condition, title);
	}	

	// <summary>
	//   Overloaded. Logs a conditional message with the default log
	//   level. The message is created with a format string and a
	//   related array of arguments.
	// </summary>
	// <param name="condition">The condition to evaluate.</param>
	// <param name="titleFmt">
	//   The format string to create the conditional message.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	//   This method only sends a message if the passed 'condition'
	//   argument evaluates to true. If 'condition' is false, this
	//   method has no effect and nothing is logged. This method is
	//   thus the counter piece to logAssert.
	//
	//   This version of the method accepts a format string and a
	//   related array of arguments. These parameters will be passed
	//   to the MessageFormat.format method and the resulting string
	//   will be the conditional message.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logConditional(boolean condition, String titleFmt,
		Object[] args)
	{
		logConditional(this.fParent.getDefaultLevel(), condition,
			titleFmt, args);		
	}
	
	// <summary>
	//   Overloaded. Logs a conditional message with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="condition">The condition to evaluate.</param>
	// <param name="title">The title of the conditional message.</param>
	// <remarks>
	//   This method only sends a message if the passed 'condition'
	//   argument evaluates to true. If 'condition' is false, this
	//   method has no effect and nothing is logged. This method is
	//   thus the counter piece to logAssert.
	// </remarks>

	public void logConditional(Level level, boolean condition, String title)
	{
		if (isOn(level))
		{
			if (condition)
			{
				sendLogEntry(level, title, LogEntryType.Conditional,
					ViewerId.Title);
			}
		}
	}
	
	// <summary>
	//   Overloaded. Logs a conditional message with a custom log
	//   level. The message is created with a format string and a
	//   related array of arguments.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="condition">The condition to evaluate.</param>
	// <param name="titleFmt">
	//   The format string to create the conditional message.
	// </param>
	// <param name="args">
	//   The array of arguments for the format string.
	// </param>
	//   This method only sends a message if the passed 'condition'
	//   argument evaluates to true. If 'condition' is false, this
	//   method has no effect and nothing is logged. This method is
	//   thus the counter piece to logAssert.
	//
	//   This version of the method accepts a format string and a
	//   related array of arguments. These parameters will be passed
	//   to the MessageFormat.format method and the resulting string
	//   will be the conditional message.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logConditional(Level level, boolean condition, 
		String titleFmt, Object[] args)
	{
		if (isOn(level))
		{
			if (condition)
			{
				try
				{
					String title = MessageFormat.format(titleFmt, args);
					sendLogEntry(level, title, LogEntryType.Conditional,
							ViewerId.Title);
				}
				catch (Exception e)
				{
					logInternalError("logConditional: " + e.getMessage());
				}
			}
		}
	}
	
	private static String longToHex(long value, int maxChars)
	{
		String s = Long.toHexString(value);
		int len = s.length();

		if (len >= maxChars)
		{
			return s.substring(len - maxChars);
		}
		else
		{
			StringBuffer sb = new StringBuffer(s);
			while (len++ < maxChars)
			{
				sb.insert(0, "0");
			}

			return sb.toString();
		}
	}
	
	// <summary>
	//   Overloaded. Logs a boolean value with the default log level.
	// </summary>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of a boolean variable. A
	//   title like "name = True" will be displayed in the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logBoolean(String name, boolean value)
	{
		logBoolean(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a boolean value with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of a boolean variable. A
	//   title like "name = True" will be displayed in the Console.
	// </remarks>

	public void logBoolean(Level level, String name, boolean value)
	{
		if (isOn(level))
		{
			String title = name + " = " + (value ? "True" : "False");
			sendLogEntry(level, title, LogEntryType.VariableValue, 
				ViewerId.Title);
		}
	}

	// <summary>
	//   Overloaded. Logs a char value with the default log level.
	// </summary>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of a char variable. A title
	//   like "name = 'c'" will be displayed in the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logChar(String name, char value)
	{
		logChar(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a char value with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of a char variable. A title
	//   like "name = 'c'" will be displayed in the Console.
	// </remarks>

	public void logChar(Level level, String name, char value)
	{
		if (isOn(level))
		{
			String title = name + " = '" + value + "'";
			sendLogEntry(level, title, LogEntryType.VariableValue, 
				ViewerId.Title);
		}
	}

	// <summary>
	//   Overloaded. Logs a byte value with the default log level.
	// </summary>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of a byte variable. A title
	//   like "name = 23" will be displayed in the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logByte(String name, byte value)
	{
		logByte(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a byte value with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of a byte variable. A title
	//   like "name = 23" will be displayed in the Console.
	// </remarks>

	public void logByte(Level level, String name, byte value)
	{
		logByte(level, name, value, false);
	}

	// <summary>
	//   Overloaded. Logs a byte value with an optional hexadecimal
	//   representation and default log level.
	// </summary>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <param name="includeHex">
	//   Indicates if a hexadecimal representation should be included.
	// </param>
	// <remarks>
	//   This method logs the name and value of a byte variable. If you
	//   set the includeHex argument to true then the hexadecimal
	//   representation of the supplied variable value is included as
	//   well.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>	
	
	public void logByte(String name, byte value, boolean includeHex)
	{
		logByte(this.fParent.getDefaultLevel(), name, value, includeHex);
	}
	
	// <summary>
	//   Overloaded. Logs a byte value with an optional hexadecimal
	//   representation and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <param name="includeHex">
	//   Indicates if a hexadecimal representation should be included.
	// </param>
	// <remarks>
	//   This method logs the name and value of a byte variable. If you
	//   set the includeHex argument to true then the hexadecimal
	//   representation of the supplied variable value is included as
	//   well.
	// </remarks>	
	
	public void logByte(Level level, String name, byte value, 
		boolean includeHex)
	{
		if (isOn(level))
		{
			StringBuffer title = new StringBuffer();
			
			title.append(name);
			title.append(" = ");
			title.append(value);
			
			if (includeHex)
			{
				title.append(" (0x");
				title.append(longToHex(value, 2));
				title.append(")");
			}
			
			sendLogEntry(level, title.toString(), LogEntryType.VariableValue,
				ViewerId.Title);
		}
	}

	// <summary>
	//   Overloaded. Logs a short integer value with the default log
	//   level.
	// </summary>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of a short integer variable.
	//   A title like "name = 23" will be displayed in the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logShort(String name, short value)
	{
		logShort(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a short integer value with a custom log
	//   level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of a short integer variable.
	//   A title like "name = 23" will be displayed in the Console.
	// </remarks>

	public void logShort(Level level, String name, short value)
	{
		logShort(level, name, value, false);
	}

	// <summary>
	//   Overloaded. Logs a short integer value with an optional
	//   hexadecimal representation and default log level.
	// </summary>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <param name="includeHex">
	//   Indicates if a hexadecimal representation should be included.
	// </param>
	// <remarks>
	//   This method logs the name and value of a short integer variable.
	//   If you set the includeHex argument to true then the hexadecimal
	//   representation of the supplied variable value is included as
	//   well.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>	
	
	public void logShort(String name, short value, boolean includeHex)
	{
		logShort(this.fParent.getDefaultLevel(), name, value, includeHex);
	}
	
	// <summary>
	//   Overloaded. Logs a short integer value with an optional
	//   hexadecimal representation and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <param name="includeHex">
	//   Indicates if a hexadecimal representation should be included.
	// </param>
	// <remarks>
	//   This method logs the name and value of a short integer variable.
	//   If you set the includeHex argument to true then the hexadecimal
	//   representation of the supplied variable value is included as
	//   well.
	// </remarks>	
	
	public void logShort(Level level, String name, short value, 
		boolean includeHex)
	{
		if (isOn(level))
		{
			StringBuffer title = new StringBuffer();
			
			title.append(name);
			title.append(" = ");
			title.append(value);
			
			if (includeHex)
			{
				title.append(" (0x");
				title.append(longToHex(value, 4));
				title.append(")");
			}
			
			sendLogEntry(level, title.toString(), LogEntryType.VariableValue,
				ViewerId.Title);
		}
	}
	
	// <summary>
	//   Overloaded. Logs an integer value with the default log level.
	// </summary>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of an integer variable. A
	//   title like "name = 23" will be displayed in the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logInt(String name, int value)
	{
		logInt(this.fParent.getDefaultLevel(), name, value);	
	}
	
	// <summary>
	//   Overloaded. Logs an integer value with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of an integer variable. A
	//   title like "name = 23" will be displayed in the Console.
	// </remarks>

	public void logInt(Level level, String name, int value)
	{
		logInt(level, name, value, false);
	}

	// <summary>
	//   Overloaded. Logs an integer value with an optional hexadecimal
	//   representation and default log level.
	// </summary>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <param name="includeHex">
	//   Indicates if a hexadecimal representation should be included.
	// </param>
	// <remarks>
	//   This method logs the name and value of an integer variable. If
	//   you set the includeHex argument to true then the hexadecimal
	//   representation of the supplied variable value is included as
	//   well.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>	
	
	public void logInt(String name, int value, boolean includeHex)
	{
		logInt(this.fParent.getDefaultLevel(), name, value, includeHex);
	}
	
	// <summary>
	//   Overloaded. Logs an integer value with an optional hexadecimal
	//   representation and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <param name="includeHex">
	//   Indicates if a hexadecimal representation should be included.
	// </param>
	// <remarks>
	//   This method logs the name and value of an integer variable. If
	//   you set the includeHex argument to true then the hexadecimal
	//   representation of the supplied variable value is included as
	//   well.
	// </remarks>	
	
	public void logInt(Level level, String name, int value, 
		boolean includeHex)
	{
		if (isOn(level))
		{
			StringBuffer title = new StringBuffer();
			
			title.append(name);
			title.append(" = ");
			title.append(value);
			
			if (includeHex)
			{
				title.append(" (0x");
				title.append(longToHex(value, 8));
				title.append(")");
			}
			
			sendLogEntry(level, title.toString(), LogEntryType.VariableValue,
				ViewerId.Title);
		}
	}
	
	// <summary>
	//   Overloaded. Logs a long integer value with the default log
	//   level.
	// </summary>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of a long integer variable.
	//   A title like "name = 23" will be displayed in the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logLong(String name, long value)
	{
		logLong(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a long integer value with a custom log
	//   level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of a long integer variable.
	//   A title like "name = 23" will be displayed in the Console.
	// </remarks>

	public void logLong(Level level, String name, long value)
	{
		logLong(level, name, value, false);
	}

	// <summary>
	//   Overloaded. Logs a long integer value with an optional
	//   hexadecimal representation and default log level.
	// </summary>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <param name="includeHex">
	//   Indicates if a hexadecimal representation should be included.
	// </param>
	// <remarks>
	//   This method logs the name and value of a long integer variable.
	//   If you set the includeHex argument to true then the hexadecimal
	//   representation of the supplied variable value is included as
	//   well.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logLong(String name, long value, boolean includeHex)
	{
		logLong(this.fParent.getDefaultLevel(), name, value, includeHex);
	}
		
	// <summary>
	//   Overloaded. Logs a long integer value with an optional
	//   hexadecimal representation and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <param name="includeHex">
	//   Indicates if a hexadecimal representation should be included.
	// </param>
	// <remarks>
	//   This method logs the name and value of a long integer variable.
	//   If you set the includeHex argument to true then the hexadecimal
	//   representation of the supplied variable value is included as
	//   well.
	// </remarks>

	public void logLong(Level level, String name, long value, 
		boolean includeHex)
	{
		if (isOn(level))
		{
			StringBuffer title = new StringBuffer();
			
			title.append(name);
			title.append(" = ");
			title.append(value);
			
			if (includeHex)
			{
				title.append(" (0x");
				title.append(longToHex(value, 16));
				title.append(")");
			}
			
			sendLogEntry(level, title.toString(), LogEntryType.VariableValue,
				ViewerId.Title);
		}
	}

	// <summary>
	//   Overloaded. Logs a float value with the default log level.
	// </summary>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of a float variable. A title
	//   like "name = 3.1415" will be displayed in the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logFloat(String name, float value)
	{
		logFloat(this.fParent.getDefaultLevel(), name, value);	
	}
	
	// <summary>
	//   Overloaded. Logs a float value with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of a float variable. A title
	//   like "name = 3.1415" will be displayed in the Console.
	// </remarks>

	public void logFloat(Level level, String name, float value)
	{
		if (isOn(level))
		{
			String title = name + " = " + value;
			sendLogEntry(level, title, LogEntryType.VariableValue, 
				ViewerId.Title);
		}
	}

	// <summary>
	//   Overloaded. Logs a double value with the default log level.
	// </summary>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of a double variable. A
	//   title like "name = 3.1415" will be displayed in the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logDouble(String name, double value)
	{
		logDouble(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a double value with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of a double variable. A
	//   title like "name = 3.1415" will be displayed in the Console.
	// </remarks>

	public void logDouble(Level level, String name, double value)
	{
		if (isOn(level))
		{
			String title = name + " = " + value;
			sendLogEntry(level, title, LogEntryType.VariableValue, 
				ViewerId.Title);
		}
	}

	// <summary>
	//   Overloaded. Logs a string value with the default log level.
	// </summary>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of a string variable. A
	//   title like "name = "string"" will be displayed in the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logString(String name, String value)
	{
		logString(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a string value with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of a string variable. A
	//   title like "name = "string"" will be displayed in the Console.
	// </remarks>

	public void logString(Level level, String name, String value)
	{
		if (isOn(level))
		{
			String title = name + " = \"" + value + "\"";
			sendLogEntry(level, title, LogEntryType.VariableValue, 
				ViewerId.Title);
		}
	}

	// <summary>
	//   Overloaded. Logs an object value with the default log level.
	// </summary>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of an object. The title to
	//   display in the Console will consist of the name and the return
	//   value of the toString method of the object.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logObjectValue(String name, Object value)
	{
		logObjectValue(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs an object value with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of an object. The title to
	//   display in the Console will consist of the name and the return
	//   value of the toString method of the object.
	// </remarks>

	public void logObjectValue(Level level, String name, Object value)
	{
		if (isOn(level))
		{
			if (value == null)
			{
				logInternalError("logObjectValue: value argument is null");
			}
			else 
			{
				String title = name + " = " + value.toString();
				sendLogEntry(level, title, LogEntryType.VariableValue, 
					ViewerId.Title);
			}
		}
	}

	// <summary>
	//   Overloaded. Logs a Date value with the default log level.
	// </summary>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of a Date instance. A title
	//   like "name = Wed Dec 29 10:52:31 CET 2004" will be displayed in
	//   the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logDate(String name, Date value)
	{
		logDate(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a Date value with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The variable name.</param>
	// <param name="value">The variable value.</param>
	// <remarks>
	//   This method logs the name and value of a Date instance. A title
	//   like "name = Wed Dec 29 10:52:31 CET 2004" will be displayed in
	//   the Console.
	// </remarks>

	public void logDate(Level level, String name, Date value)
	{
		if (isOn(level))
		{
			if (value == null)
			{
				logInternalError("logDate: value argument is null");
			}
			else
			{
				String title = name + " = " + value.toString();
				sendLogEntry(level, title, LogEntryType.VariableValue, 
					ViewerId.Title);
			}
		}
	}

	// <summary>
	//   Overloaded. Logs the name and value of a boolean variable with
	//   the default log level.
	// </summary>
	// <param name="name">The name of the variable.</param>
	// <param name="value">The boolean value of the variable.</param>
	// <remarks>
	//   This method just calls the logBoolean method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logValue(String name, boolean value)
	{
		logValue(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs the name and value of a boolean variable
	//   with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the variable.</param>
	// <param name="value">The boolean value of the variable.</param>
	// <remarks>This method just calls the logBoolean method.</remarks>

	public void logValue(Level level, String name, boolean value)
	{
		logBoolean(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs the name and value of a char variable with
	//   the default log level.
	// </summary>
	// <param name="name">The name of the variable.</param>
	// <param name="value">The char value of the variable.</param>
	// <remarks>
	//   This method just calls the logChar method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logValue(String name, char value)
	{
		logValue(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs the name and value of a char variable
	//   with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the variable.</param>
	// <param name="value">The char value of the variable.</param>
	// <remarks>This method just calls the logChar method.</remarks>

	public void logValue(Level level, String name, char value)
	{
		logChar(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs the name and value of a byte variable with
	//   the default log level.
	// </summary>
	// <param name="name">The name of the variable.</param>
	// <param name="value">The byte value of the variable.</param>
	// <remarks>
	//   This method just calls the logByte method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void logValue(String name, byte value)
	{
		logValue(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs the name and value of a byte variable
	//   with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the variable.</param>
	// <param name="value">The byte value of the variable.</param>
	// <remarks>This method just calls the logByte method.</remarks>
	
	public void logValue(Level level, String name, byte value)
	{
		logByte(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs the name and value of a short integer variable
	//   with the default log level.
	// </summary>
	// <param name="name">The name of the variable.</param>
	// <param name="value">
	//   The short integer value of the variable.
	// </param>
	// <remarks>
	//   This method just calls the logShort method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void logValue(String name, short value)
	{
		logValue(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs the name and value of a short integer
	//   variable with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the variable.</param>
	// <param name="value">
	//   The short integer value of the variable.
	// </param>
	// <remarks>This method just calls the logShort method.</remarks>
	
	public void logValue(Level level, String name, short value)
	{
		logShort(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs the name and value of an integer variable
	//   with the default log level.
	// </summary>
	// <param name="name">The name of the variable.</param>
	// <param name="value">The integer value of the variable.</param>
	// <remarks>
	//   This method just calls the logInt method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void logValue(String name, int value)
	{
		logValue(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs the name and value of an integer variable
	//   with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the variable.</param>
	// <param name="value">The integer value of the variable.</param>
	// <remarks>This method just calls the logInt method.</remarks>
	
	public void logValue(Level level, String name, int value)
	{
		logInt(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs the name and value of a long integer variable
	//   with the default log level.
	// </summary>
	// <param name="name">The name of the variable.</param>
	// <param name="value">
	//   The long integer value of the variable.
	// </param>
	// <remarks>
	//   This method just calls the logLong method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void logValue(String name, long value)
	{
		logValue(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs the name and value of a long integer
	//   variable with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the variable.</param>
	// <param name="value">
	//   The long integer value of the variable.
	// </param>
	// <remarks>This method just calls the logLong method.</remarks>
	
	public void logValue(Level level, String name, long value)
	{
		logLong(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs the name and value of a float variable with
	//   the default log level.
	// </summary>
	// <param name="name">The name of the variable.</param>
	// <param name="value">The float value of the variable.</param>
	// <remarks>
	//   This method just calls the logFloat method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logValue(String name, float value)
	{
		logValue(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs the name and value of a float variable
	//   with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the variable.</param>
	// <param name="value">The float value of the variable.</param>
	// <remarks>This method just calls the logFloat method.</remarks>

	public void logValue(Level level, String name, float value)
	{
		logFloat(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs the name and value of a double variable with
	//   the default log level.
	// </summary>
	// <param name="name">The name of the variable.</param>
	// <param name="value">The double value of the variable.</param>
	// <remarks>
	//   This method just calls the logDouble method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logValue(String name, double value)
	{
		logValue(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs the name and value of a double variable
	//   with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the variable.</param>
	// <param name="value">The double value of the variable.</param>
	// <remarks>This method just calls the logDouble method.</remarks>

	public void logValue(Level level, String name, double value)
	{
		logDouble(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs the name and value of a string variable with
	//   the default log level.
	// </summary>
	// <param name="name">The name of the variable.</param>
	// <param name="value">The string value of the variable.</param>
	// <remarks>
	//   This method just calls the logString method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logValue(String name, String value)
	{
		logValue(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs the name and value of a string variable
	//   with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the variable.</param>
	// <param name="value">The string value of the variable.</param>
	// <remarks>This method just calls the logString method.</remarks>

	public void logValue(Level level, String name, String value)
	{
		logString(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs the name and value of an object with the
	//   default log level.
	// </summary>
	// <param name="name">The name of the variable.</param>
	// <param name="value">The object to log.</param>
	// <remarks>
	//   This method just calls the logObjectValue method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logValue(String name, Object value)
	{
		logValue(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs the name and value of an object
	//   with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the variable.</param>
	// <param name="value">The object to log.</param>
	// <remarks>This method just calls the logObjectValue method.</remarks>

	public void logValue(Level level, String name, Object value)
	{
		logObjectValue(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs the name and value of a Date instance with
	//   the default log level.
	// </summary>
	// <param name="name">The name of the variable.</param>
	// <param name="value">The Date value of the variable.</param>
	// <remarks>
	//   This method just calls the logDate method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logValue(String name, Date value)
	{
		logValue(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs the name and value of a Date instance
	//   with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the variable.</param>
	// <param name="value">The Date value of the variable.</param>
	// <remarks>This method just calls the logDate method.</remarks>

	public void logValue(Level level, String name, Date value)
	{
		logDate(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs a custom viewer context with the default log
	//   level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="lt">The custom Log Entry type.</param>
	// <param name="ctx">
	// <seealso cref="com.gurock.smartinspect.contexts.ViewerContext"/>
	// <remarks>
	//   This method can be used to extend the capabilities of the
	//   SmartInspect Java library. You can assemble a so called viewer
	//   context and thus can send custom data to the SmartInspect
	//   Console. Furthermore, you can choose the viewer in which your
	//   data should be displayed. Every viewer in the Console has
	//   a corresponding viewer context class in this library. 
	//   
	//   Have a look at the ViewerContext class and its derived classes
	//   to see a list of available viewer context classes.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logCustomContext(String title, LogEntryType lt, 
		ViewerContext ctx)
	{
		logCustomContext(this.fParent.getDefaultLevel(), title, lt, ctx);
	}
	
	// <summary>
	//   Overloaded. Logs a custom viewer context with a custom log
	//   level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="lt">The custom Log Entry type.</param>
	// <param name="ctx">
	// <seealso cref="com.gurock.smartinspect.contexts.ViewerContext"/>
	// <remarks>
	//   This method can be used to extend the capabilities of the
	//   SmartInspect Java library. You can assemble a so called viewer
	//   context and thus can send custom data to the SmartInspect
	//   Console. Furthermore, you can choose the viewer in which your
	//   data should be displayed. Every viewer in the Console has
	//   a corresponding viewer context class in this library. 
	//   
	//   Have a look at the ViewerContext class and its derived classes
	//   to see a list of available viewer context classes.
	// </remarks>

	public void logCustomContext(Level level, String title, LogEntryType lt,
		ViewerContext ctx)
	{
		if (isOn(level))
		{
			if (lt == null || ctx == null)
			{
				logInternalError("logCustomContext: Invalid arguments");
			}
			else 
			{
				sendContext(level, title, lt, ctx);
			}
		}
	}

	/* Internal send methods go here. */

	private void sendContext(Level level, String title, LogEntryType lt, 
		ViewerContext ctx)
	{
		sendLogEntry(level, title, lt, ctx.getViewerId(), getColor(),
			ctx.getViewerData());
	}
	
	private void sendLogEntry(Level level, String title, LogEntryType lt, 
		ViewerId vi)
	{
		sendLogEntry(level, title, lt, vi, getColor(), null);
	}

	private void sendLogEntry(Level level, String title, LogEntryType lt, 
		ViewerId vi, Color color, byte[] data)
	{
		LogEntry logEntry = new LogEntry(lt, vi);
		logEntry.setTimestamp(this.fParent.now());
		logEntry.setLevel(level);
		logEntry.setTitle(title);
		
		if (color == DEFAULT_COLOR)
		{
			logEntry.setColor(color); /* Transparent */
		}
		else 
		{
			int rgb = color.getRGB() & 0xffffff;
			logEntry.setColor(new Color(rgb, true));
		}
		
		logEntry.setSessionName(getName()); // Our session name.
		logEntry.setData(data);
		this.fParent.sendLogEntry(logEntry);
	}

	private void sendControlCommand(ControlCommandType ct, byte[] data)
	{
		ControlCommand controlCommand = new ControlCommand(ct);
		controlCommand.setLevel(Level.Control);
		controlCommand.setData(data);
		this.fParent.sendControlCommand(controlCommand);
	}

	private void sendProcessFlow(Level level, String title, 
		ProcessFlowType pt)
	{
		ProcessFlow processFlow = new ProcessFlow(pt);
		processFlow.setTimestamp(this.fParent.now());
		processFlow.setLevel(level);
		processFlow.setTitle(title);
		this.fParent.sendProcessFlow(processFlow);
	}

	private void sendWatch(Level level, String name, String value, 
		WatchType wt)
	{
		Watch watch = new Watch(wt);
		watch.setTimestamp(this.fParent.now());
		watch.setLevel(level);
		watch.setName(name);
		watch.setValue(value);
		this.fParent.sendWatch(watch);
	}

	// <summary>
	//   Overloaded. Logs a text using a custom Log Entry type, viewer ID
	//   and default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="text">The text to log.</param>
	// <param name="lt">The custom Log Entry type.</param>
	// <param name="vi">
	//   The custom viewer ID which specifies the way the Console handles
	//   the text content.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </param>

	public void logCustomText(String title, String text, LogEntryType lt, 
		ViewerId vi)
	{
		logCustomText(this.fParent.getDefaultLevel(), title, text, lt, vi);
	}
	
	// <summary>
	//   Overloaded. Logs a text using a custom Log Entry type, viewer ID
	//   and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="text">The text to log.</param>
	// <param name="lt">The custom Log Entry type.</param>
	// <param name="vi">
	//   The custom viewer ID which specifies the way the Console handles
	//   the text content.
	// </param>

	public void logCustomText(Level level, String title, String text, 
		LogEntryType lt, ViewerId vi)
	{
		if (isOn(level))
		{
			TextContext ctx = new TextContext(vi);
			try
			{
				try 
				{				
					ctx.loadFromText(text);
					sendContext(level, title, lt, ctx);				
				}	
				catch (Exception e)
				{
					logInternalError("logCustomText: " + e.getMessage());
				}
			}
			finally 
			{
				ctx.close();
			}
		}
	}

	// <summary>
	//   Overloaded. Logs the content of a file using a custom Log Entry
	//   type, viewer ID and default log level.
	// </summary>
	// <param name="fileName">The file to log.</param>
	// <param name="lt">The custom Log Entry type.</param>
	// <param name="vi">
	//   The custom viewer ID which specifies the way the Console
	//   handles the file content.
	// </param>
	// <remarks>
	//   This method logs the content of the supplied file using a custom
	//   Log Entry type and viewer ID. The parameters control the way the
	//   content of the file is displayed in the Console. Thus you can
	//   extend the functionality of the SmartInspect Java library with
	//   this method.
	//
	//   This version of the method uses the fileName argument as
	//   title to display in the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logCustomFile(String fileName, LogEntryType lt, ViewerId vi)
	{
		logCustomFile(this.fParent.getDefaultLevel(), fileName, lt, vi);
	}
	
	// <summary>
	//   Overloaded. Logs the content of a file using a custom Log Entry
	//   type, viewer ID and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="fileName">The file to log.</param>
	// <param name="lt">The custom Log Entry type.</param>
	// <param name="vi">
	//   The custom viewer ID which specifies the way the Console
	//   handles the file content.
	// </param>
	// <remarks>
	//   This method logs the content of the supplied file using a custom
	//   Log Entry type and viewer ID. The parameters control the way the
	//   content of the file is displayed in the Console. Thus you can
	//   extend the functionality of the SmartInspect Java library with
	//   this method.
	//
	//   This version of the method uses the fileName argument as
	//   title to display in the Console.
	// </remarks>

	public void logCustomFile(Level level, String fileName, LogEntryType lt,
		ViewerId vi)
	{
		logCustomFile(level, fileName, fileName, lt, vi);
	}

	// <summary>
	//   Overloaded. Logs the content of a file using a custom Log Entry
	//   type, viewer ID, title and default log level.
	// </summary>
	// <param name="fileName">The file to log.</param>
	// <param name="lt">The custom Log Entry type.</param>
	// <param name="vi">
	//   The custom viewer ID which specifies the way the Console
	//   handles the file content.
	// </param>
	// <param name="title">The title to display in the Console.</param>
	// <remarks>
	//   This method logs the content of the supplied file using a custom
	//   Log Entry type and viewer ID. The parameters control the way the
	//   content of the file is displayed in the Console. Thus you can
	//   extend the functionality of the SmartInspect Java library with
	//   this method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logCustomFile(String title, String fileName, LogEntryType lt, 
		ViewerId vi)
	{
		logCustomFile(this.fParent.getDefaultLevel(), title, fileName, lt, vi);
	}
	
	// <summary>
	//   Overloaded. Logs the content of a file using a custom Log Entry
	//   type, viewer ID, title and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="fileName">The file to log.</param>
	// <param name="lt">The custom Log Entry type.</param>
	// <param name="vi">
	//   The custom viewer ID which specifies the way the Console
	//   handles the file content.
	// </param>
	// <param name="title">The title to display in the Console.</param>
	// <remarks>
	//   This method logs the content of the supplied file using a custom
	//   Log Entry type and viewer ID. The parameters control the way the
	//   content of the file is displayed in the Console. Thus you can
	//   extend the functionality of the SmartInspect Java library with
	//   this method.
	// </remarks>

	public void logCustomFile(Level level, String title, 
		String fileName, LogEntryType lt, ViewerId vi)
	{
		if (isOn(level))
		{
			if (lt == null)
			{
				logInternalError("logCustomFile: lt argument is null");				
			}
			else if (vi == null)
			{
				logInternalError("logCustomFile: vi argument is null");				
			}
			else 
			{
				BinaryContext ctx = new BinaryContext(vi);
				try 
				{
					try 
					{
						ctx.loadFromFile(fileName);
						sendContext(level, title, lt, ctx);
					}
					catch (Exception e)
					{
						logInternalError("logCustomFile: " + e.getMessage());
					}
				}
				finally 
				{
					ctx.close();
				}
			}
		}
	}

	// <summary>
	//   Overloaded. Logs the content of a stream using a custom Log Entry
	//   type, viewer ID and default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">The stream to log.</param>
	// <param name="lt">The custom Log Entry type.</param>
	// <param name="vi">
	//   The custom viewer ID which specifies the way the Console
	//   handles the stream content.
	// </param>
	// <remarks>
	//   This method logs the content of the supplied stream using a custom
	//   Log Entry type and viewer ID. The parameters control the way the
	//   content of the file is displayed in the Console. Thus you can
	//   extend the functionality of the SmartInspect Java library with
	//   this method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logCustomStream(String title, InputStream stream, 
		LogEntryType lt, ViewerId vi)
	{
		logCustomStream(this.fParent.getDefaultLevel(), title, stream, lt, vi);
	}
	
	// <summary>
	//   Overloaded. Logs the content of a stream using a custom Log Entry
	//   type, viewer ID and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">The stream to log.</param>
	// <param name="lt">The custom Log Entry type.</param>
	// <param name="vi">
	//   The custom viewer ID which specifies the way the Console
	//   handles the stream content.
	// </param>
	// <remarks>
	//   This method logs the content of the supplied stream using a custom
	//   Log Entry type and viewer ID. The parameters control the way the
	//   content of the file is displayed in the Console. Thus you can
	//   extend the functionality of the SmartInspect Java library with
	//   this method.
	// </remarks>

	public void logCustomStream(Level level, String title, 
		InputStream stream, LogEntryType lt, ViewerId vi)
	{
		if (isOn(level))
		{
			if (lt == null)
			{
				logInternalError("logCustomStream: lt argument is null");				
			}
			else if (vi == null)
			{
				logInternalError("logCustomStream: vi argument is null");				
			}
			else 
			{
				BinaryContext ctx = new BinaryContext(vi);
				try 
				{
					try 
					{
						ctx.loadFromStream(stream);
						sendContext(level, title, lt, ctx);
					}
					catch (Exception e)
					{
						logInternalError("logCustomStream: " + e.getMessage());
					}
				}
				finally 
				{
					ctx.close();
				}
			}
		}
	}

	// <summary>
	//   Overloaded. Logs the content of a reader using a custom Log Entry
	//   type, viewer ID and default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="reader">The reader to log.</param>
	// <param name="lt">The custom Log Entry type.</param>
	// <param name="vi">
	//   The custom viewer ID which specifies the way the Console
	//   handles the reader content.
	// </param>
	// <remarks>
	//   This method logs the content of the supplied reader using a custom
	//   Log Entry type and viewer ID. The parameters control the way the
	//   content of the file is displayed in the Console. Thus you can
	//   extend the functionality of the SmartInspect Java library with
	//   this method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logCustomReader(String title, Reader reader, LogEntryType lt, 
		ViewerId vi)
	{
		logCustomReader(this.fParent.getDefaultLevel(), title, reader, lt, vi);
	}
	
	// <summary>
	//   Overloaded. Logs the content of a reader using a custom Log Entry
	//   type, viewer ID and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="reader">The reader to log.</param>
	// <param name="lt">The custom Log Entry type.</param>
	// <param name="vi">
	//   The custom viewer ID which specifies the way the Console
	//   handles the reader content.
	// </param>
	// <remarks>
	//   This method logs the content of the supplied reader using a custom
	//   Log Entry type and viewer ID. The parameters control the way the
	//   content of the file is displayed in the Console. Thus you can
	//   extend the functionality of the SmartInspect Java library with
	//   this method.
	// </remarks>

	public void logCustomReader(Level level, String title, Reader reader,
		LogEntryType lt, ViewerId vi)
	{
		if (isOn(level))
		{
			if (lt == null)
			{
				logInternalError("logCustomReader: lt argument is null");				
			}
			else if (vi == null)
			{
				logInternalError("logCustomReader: vi argument is null");				
			}
			else 
			{
				TextContext ctx = new TextContext(vi);
				try 
				{
					try 
					{
						ctx.loadFromReader(reader);
						sendContext(level, title, lt, ctx);
					}
					catch (Exception e)
					{
						logInternalError("logCustomReader: " + e.getMessage());
					}
				}
				finally 
				{
					ctx.close();
				}
			}
		}
	}

	// <summary>
	//   Overloaded. Logs a string with the default log level and displays
	//   it in a read-only text field.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="text">The text to log.</param>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void logText(String title, String text)
	{
		logText(this.fParent.getDefaultLevel(), title, text);
	}
	
	// <summary>
	//   Overloaded. Logs a string with a custom log level and displays it
	//   in a read-only text field.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="text">The text to log.</param>

	public void logText(Level level, String title, String text)
	{
		logCustomText(level, title, text, LogEntryType.Text, ViewerId.Data);
	}

	// <summary>
	//   Overloaded. Logs a text file with the default log level and displays
	//   the content in a read-only text field.
	// </summary>
	// <param name="fileName">The file to log.</param>
	// <remarks>
	//   This version of the method uses the fileName argument as title to
	//   display in the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logTextFile(String fileName)
	{
		logTextFile(this.fParent.getDefaultLevel(), fileName);
	}
	
	// <summary>
	//   Overloaded. Logs a text file with a custom log level and displays
	//   the content in a read-only text field.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="fileName">The file to log.</param>
	// <remarks>
	//   This version of the method uses the fileName argument as title to
	//   display in the Console.
	// </remarks>

	public void logTextFile(Level level, String fileName)
	{
		logCustomFile(level, fileName, LogEntryType.Text, ViewerId.Data);
	}

	// <summary>
	//   Overloaded. Logs a text file and displays the content in a read-only
	//   text field using a custom title and default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="fileName">The file to log.</param>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logTextFile(String title, String fileName)
	{
		logTextFile(this.fParent.getDefaultLevel(), title, fileName);
	}
	
	// <summary>
	//   Overloaded. Logs a text file and displays the content in a read-only
	//   text field using a custom title and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="fileName">The file to log.</param>

	public void logTextFile(Level level, String title, String fileName)
	{
		logCustomFile(level, title, fileName, LogEntryType.Text, 
			ViewerId.Data);
	}

	// <summary>
	//   Overloaded. Logs a stream with the default log level and displays
	//   the content in a read-only text field.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">The stream to log.</param>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logTextStream(String title, InputStream stream)
	{
		logTextStream(this.fParent.getDefaultLevel(), title, stream);
	}
	
	// <summary>
	//   Overloaded. Logs a stream with a custom log level and displays
	//   the content in a read-only text field.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">The stream to log.</param>

	public void logTextStream(Level level, String title, InputStream stream)
	{
		logCustomStream(level, title, stream, LogEntryType.Text, 
			ViewerId.Data);
	}

	// <summary>
	//   Overloaded. Logs a reader with the default log level and displays
	//   the content in a read-only text field.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="reader">The reader to log.</param>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logTextReader(String title, Reader reader)
	{
		logTextReader(this.fParent.getDefaultLevel(), title, reader);
	}
	
	// <summary>
	//   Overloaded. Logs a reader with a custom log level and displays
	//   the content in a read-only text field.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="reader">The reader to log.</param>

	public void logTextReader(Level level, String title, Reader reader)
	{
		logCustomReader(level, title, reader, LogEntryType.Text, 
			ViewerId.Data);
	}

	// <summary>
	//   Overloaded. Logs HTML code with the default log level and displays
	//   it in a web browser.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="html">The HTML source code to display.</param>
	// <remarks>
	//   This method logs the supplied HTML source code. The source
	//   code is displayed as a website in the web viewer of the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logHtml(String title, String html)
	{
		logHtml(this.fParent.getDefaultLevel(), title, html);
	}
	
	// <summary>
	//   Overloaded. Logs HTML code with a custom log level and displays
	//   it in a web browser.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="html">The HTML source code to display.</param>
	// <remarks>
	//   This method logs the supplied HTML source code. The source
	//   code is displayed as a website in the web viewer of the Console.
	// </remarks>

	public void logHtml(Level level, String title, String html)
	{
		logCustomText(level, title, html, LogEntryType.WebContent, 
			ViewerId.Web);
	}

	// <summary>
	//   Overloaded. Logs an HTML file with the default log level and
	//   displays the content in a web browser.
	// </summary>
	// <param name="fileName">The HTML file to display.</param>
	// <remarks>
	//   This method logs the HTML source code of the supplied file. The
	//   source code is displayed as a website in the web viewer of the
	//   Console.
	//
	//   This version of the method uses the fileName argument as title to
	//   display in the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logHtmlFile(String fileName)
	{
		logHtmlFile(this.fParent.getDefaultLevel(), fileName);
	}
	
	// <summary>
	//   Overloaded. Logs an HTML file with a custom log level and displays
	//   the content in a web browser.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="fileName">The HTML file to display.</param>
	// <remarks>
	//   This method logs the HTML source code of the supplied file. The
	//   source code is displayed as a website in the web viewer of the
	//   Console.
	//
	//   This version of the method uses the fileName argument as title to
	//   display in the Console.
	// </remarks>

	public void logHtmlFile(Level level, String fileName)
	{
		logCustomFile(level, fileName, LogEntryType.WebContent, ViewerId.Web);
	}

	// <summary>
	//   Overloaded. Logs an HTML file and displays the content in a web
	//   browser using a custom title and default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="fileName">The HTML file to display.</param>
	// <remarks>
	//   This method logs the HTML source code of the supplied file. The
	//   source code is displayed as a website in the web viewer of the
	//   Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logHtmlFile(String title, String fileName)
	{
		logHtmlFile(this.fParent.getDefaultLevel(), title, fileName);
	}
	
	// <summary>
	//   Overloaded. Logs an HTML file and displays the content in a web
	//   browser using a custom title and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="fileName">The HTML file to display.</param>
	// <remarks>
	//   This method logs the HTML source code of the supplied file. The
	//   source code is displayed as a website in the web viewer of the
	//   Console.
	// </remarks>

	public void logHtmlFile(Level level, String title, String fileName)
	{
		logCustomFile(level, title, fileName, LogEntryType.WebContent, 
			ViewerId.Web);
	}

	// <summary>
	//   Overloaded. Logs a stream with the default log level and displays
	//   the content in a web browser.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">The stream to display.</param>
	// <remarks>
	//   This method logs the HTML source code of the supplied stream.
	//   The source code is displayed as a website in the web viewer of
	//   the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logHtmlStream(String title, InputStream stream)
	{
		logHtmlStream(this.fParent.getDefaultLevel(), title, stream);
	}
	
	// <summary>
	//   Overloaded. Logs a stream with a custom log level and displays
	//   the content in a web browser.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">The stream to display.</param>
	// <remarks>
	//   This method logs the HTML source code of the supplied stream.
	//   The source code is displayed as a website in the web viewer of
	//   the Console.
	// </remarks>

	public void logHtmlStream(Level level, String title, InputStream stream)
	{
		logCustomStream(level, title, stream, LogEntryType.WebContent, 
			ViewerId.Web);
	}

	// <summary>
	//   Overloaded. Logs a reader with the default log level and displays
	//   the content in a web browser.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="reader">The reader to display.</param>
	// <remarks>
	//   This method logs the HTML source code of the supplied reader.
	//   The source code is displayed as a website in the web viewer of
	//   the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logHtmlReader(String title, Reader reader)
	{
		logHtmlReader(this.fParent.getDefaultLevel(), title, reader);
	}
	
	// <summary>
	//   Overloaded. Logs a reader with a custom log level and displays
	//   the content in a web browser.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="reader">The reader to display.</param>
	// <remarks>
	//   This method logs the HTML source code of the supplied reader.
	//   The source code is displayed as a website in the web viewer of
	//   the Console.
	// </remarks>

	public void logHtmlReader(Level level, String title, Reader reader)
	{
		logCustomReader(level, title, reader, LogEntryType.WebContent, 
			ViewerId.Web);
	}

	// <summary>
	//   Overloaded. Logs a byte array with the default log level and
	//   displays it in a hex viewer.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="b">The byte array to display in the hex viewer.</param>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logBinary(String title, byte[] b)
	{
		logBinary(this.fParent.getDefaultLevel(), title, b);
	}
	
	// <summary>
	//   Overloaded. Logs a byte array with a custom log level and displays
	//   it in a hex viewer.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="b">The byte array to display in the hex viewer.</param>

	public void logBinary(Level level, String title, byte[] b)
	{
		if (isOn(level))
		{
			BinaryViewerContext ctx = new BinaryViewerContext();
			try 
			{
				try 
				{
					ctx.appendBytes(b);
					sendContext(level, title, LogEntryType.Binary, ctx);
				}
				catch (Exception e)
				{
					logInternalError("logBinary: " + e.getMessage());
				}
			}
			finally 
			{
				ctx.close();
			}
		}
	}

	// <summary>
	//   Overloaded. Logs a byte array with the default log level and
	//   displays it in a hex viewer.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="b">The byte array to display in the hex viewer.</param>
	// <param name="off">The offset at which to display data from.</param>
	// <param name="len">The amount of bytes to display.</param>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logBinary(String title, byte[] b, int off, int len)
	{
		logBinary(this.fParent.getDefaultLevel(), title, b, off, len);
	}
	
	// <summary>
	//   Overloaded. Logs a byte array with a custom log level and displays
	//   it in a hex viewer.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="b">The byte array to display in the hex viewer.</param>
	// <param name="off">The offset at which to display data from.</param>
	// <param name="len">The amount of bytes to display.</param>

	public void logBinary(Level level, String title, byte[] b, int off, 
		int len)
	{
		if (isOn(level))
		{
			BinaryViewerContext ctx = new BinaryViewerContext();
			try 
			{
				try 
				{
					ctx.appendBytes(b, off, len);
					sendContext(level, title, LogEntryType.Binary, ctx);
				}
				catch (Exception e)
				{
					logInternalError("logBinary: " + e.getMessage());
				}
			}
			finally 
			{
				ctx.close();
			}
		}
	}
	
	// <summary>
	//   Overloaded. Logs a binary file with the default log level and
	//   displays its content in a hex viewer.
	// </summary>
	// <param name="fileName">
	//   The binary file to display in a hex viewer.
	// </param>
	// <remarks>
	//   This version of the method uses the supplied fileName argument as
	//   title to display in the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logBinaryFile(String fileName)
	{
		logBinaryFile(this.fParent.getDefaultLevel(), fileName);
	}
	
	// <summary>
	//   Overloaded. Logs a binary file with a custom log level and displays
	//   its content in a hex viewer.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="fileName">
	//   The binary file to display in a hex viewer.
	// </param>
	// <remarks>
	//   This version of the method uses the supplied fileName argument as
	//   title to display in the Console.
	// </remarks>

	public void logBinaryFile(Level level, String fileName)
	{
		logCustomFile(level, fileName, LogEntryType.Binary, ViewerId.Binary);
	}

	// <summary>
	//   Overloaded. Logs a binary file and displays its content in a hex
	//   viewer using a custom title and default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="fileName">
	//   The binary file to display in a hex viewer.
	// </param>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logBinaryFile(String title, String fileName)
	{
		logBinaryFile(this.fParent.getDefaultLevel(), title, fileName);
	}
	
	// <summary>
	//   Overloaded. Logs a binary file and displays its content in a hex
	//   viewer using a custom title and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="fileName">
	//   The binary file to display in a hex viewer.
	// </param>

	public void logBinaryFile(Level level, String title, String fileName)
	{
		logCustomFile(level, title, fileName, LogEntryType.Binary, 
			ViewerId.Binary);
	}

	// <summary>
	//   Overloaded. Logs a binary stream with the default log level and
	//   displays its content in a hex viewer.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">
	//   The binary stream to display in a hex viewer.
	// </param>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logBinaryStream(String title, InputStream stream)
	{
		logBinaryStream(this.fParent.getDefaultLevel(), title, stream);
	}
	
	// <summary>
	//   Overloaded. Logs a binary stream with a custom log level and
	//   displays its content in a hex viewer.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">
	//   The binary stream to display in a hex viewer.
	// </param>

	public void logBinaryStream(Level level, String title, InputStream stream)
	{
		logCustomStream(level, title, stream, LogEntryType.Binary, 
			ViewerId.Binary);
	}

	// <summary>
	//   Overloaded. Logs a bitmap file with the default log level and
	//   displays it in the Console.
	// </summary>
	// <param name="fileName">
	//   The bitmap file to display in the Console.
	// </param>
	// <remarks>
	//   This version of the method uses the supplied fileName argument as
	//   title to display in the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logBitmapFile(String fileName)
	{
		logBitmapFile(this.fParent.getDefaultLevel(), fileName);
	}
	
	// <summary>
	//   Overloaded. Logs a bitmap file with a custom log level and displays
	//   it in the Console.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="fileName">
	//   The bitmap file to display in the Console.
	// </param>
	// <remarks>
	//   This version of the method uses the supplied fileName argument as
	//   title to display in the Console.
	// </remarks>

	public void logBitmapFile(Level level, String fileName)
	{
		logCustomFile(level, fileName, LogEntryType.Graphic, ViewerId.Bitmap);
	}

	// <summary>
	//   Overloaded. Logs a bitmap file and displays it in the Console using
	//   a custom title and default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="fileName">
	//   The bitmap file to display in the Console.
	// </param>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logBitmapFile(String title, String fileName)
	{
		logBitmapFile(this.fParent.getDefaultLevel(), title, fileName);
	}
	
	// <summary>
	//   Overloaded. Logs a bitmap file and displays it in the Console using
	//   a custom title and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="fileName">
	//   The bitmap file to display in the Console.
	// </param>

	public void logBitmapFile(Level level, String title, String fileName)
	{
		logCustomFile(level, title, fileName, LogEntryType.Graphic, 
			ViewerId.Bitmap);
	}

	// <summary>
	//   Overloaded. Logs a stream with the default log level and interprets
	//   its content as a bitmap.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">The stream to display as bitmap.</param>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logBitmapStream(String title, InputStream stream)
	{
		logBitmapStream(this.fParent.getDefaultLevel(), title, stream);
	}
	
	// <summary>
	//   Overloaded. Logs a stream with a custom log level and interprets
	//   its content as a bitmap.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">The stream to display as bitmap.</param>

	public void logBitmapStream(Level level, String title, InputStream stream)
	{
		logCustomStream(level, title, stream, LogEntryType.Graphic, 
			ViewerId.Bitmap);
	}

	// <summary>
	//   Overloaded. Logs a JPEG file with the default log level and displays
	//   it in the Console.
	// </summary>
	// <param name="fileName">
	//   The JPEG file to display in the Console.
	// </param>
	// <remarks>
	//   This version of the method uses the supplied fileName argument as
	//   title to display in the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logJpegFile(String fileName)
	{
		logJpegFile(this.fParent.getDefaultLevel(), fileName);
	}
	
	// <summary>
	//   Overloaded. Logs a JPEG file with a custom log level and displays
	//   it in the Console.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="fileName">
	//   The JPEG file to display in the Console.
	// </param>
	// <remarks>
	//   This version of the method uses the supplied fileName argument as
	//   title to display in the Console.
	// </remarks>

	public void logJpegFile(Level level, String fileName)
	{
		logCustomFile(level, fileName, LogEntryType.Graphic, ViewerId.Jpeg);
	}

	// <summary>
	//   Overloaded. Logs a JPEG file and displays it in the Console using
	//   a custom title and default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="fileName">
	//   The JPEG file to display in the Console.
	// </param>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logJpegFile(String title, String fileName)
	{
		logJpegFile(this.fParent.getDefaultLevel(), title, fileName);
	}
	
	// <summary>
	//   Overloaded. Logs a JPEG file and displays it in the Console using
	//   a custom title and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="fileName">
	//   The JPEG file to display in the Console.
	// </param>

	public void logJpegFile(Level level, String title, String fileName)
	{
		logCustomFile(level, title, fileName, LogEntryType.Graphic, 
			ViewerId.Jpeg);
	}

	// <summary>
	//   Overloaded. Logs a stream with the default log level and interprets
	//   its content as a JPEG image.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">The stream to display as JPEG image.</param>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logJpegStream(String title, InputStream stream)
	{
		logJpegStream(this.fParent.getDefaultLevel(), title, stream);
	}
	
	// <summary>
	//   Overloaded. Logs a stream with a custom log level and interprets
	//   its content as a JPEG image.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">The stream to display as JPEG image.</param>

	public void logJpegStream(Level level, String title, InputStream stream)
	{
		logCustomStream(level, title, stream, LogEntryType.Graphic, 
			ViewerId.Jpeg);
	}

	// <summary>
	//   Overloaded. Logs a Windows icon file with the default log level
	//   and displays it in the Console.
	// </summary>
	// <param name="fileName">
	//   The Windows icon file to display in the Console.
	// </param>
	// <remarks>
	//   This version of the method uses the supplied fileName argument as
	//   title to display in the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logIconFile(String fileName)
	{
		logIconFile(this.fParent.getDefaultLevel(), fileName);
	}
	
	// <summary>
	//   Overloaded. Logs a Windows icon file with a custom log level and
	//   displays it in the Console.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="fileName">
	//   The Windows icon file to display in the Console.
	// </param>
	// <remarks>
	//   This version of the method uses the supplied fileName argument as
	//   title to display in the Console.
	// </remarks>

	public void logIconFile(Level level, String fileName)
	{
		logCustomFile(level, fileName, LogEntryType.Graphic, ViewerId.Icon);
	}

	// <summary>
	//   Overloaded. Logs a Windows icon file and displays it in the Console
	//   using a custom title and default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="fileName">
	//   The Windows icon file to display in the Console.
	// </param>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void logIconFile(String title, String fileName)
	{
		logIconFile(this.fParent.getDefaultLevel(), title, fileName);
	}
	
	// <summary>
	//   Overloaded. Logs a Windows icon file and displays it in the Console
	//   using a custom title and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="fileName">
	//   The Windows icon file to display in the Console.
	// </param>
	
	public void logIconFile(Level level, String title, String fileName)
	{
		logCustomFile(level, title, fileName, LogEntryType.Graphic, 
			ViewerId.Icon);
	}

	// <summary>
	//   Overloaded. Logs a stream with the default log level and interprets
	//   its content as Windows icon.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">The stream to display as Windows icon.</param>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logIconStream(String title, InputStream stream)
	{
		logIconStream(this.fParent.getDefaultLevel(), title, stream);
	}
	
	// <summary>
	//   Overloaded. Logs a stream with a custom log level and interprets
	//   its content as Windows icon.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">The stream to display as Windows icon.</param>

	public void logIconStream(Level level, String title, InputStream stream)
	{
		logCustomStream(level, title, stream, LogEntryType.Graphic, 
			ViewerId.Icon);
	}

	// <summary>
	//   Overloaded. Logs a Windows Metafile file with the default log
	//   level and displays it in the Console.
	// </summary>
	// <param name="fileName">
	//   The Windows Metafile file to display in the Console.
	// </param>
	// <remarks>
	//   This version of the method uses the supplied fileName argument as
	//   title to display in the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logMetafileFile(String fileName)
	{
		logMetafileFile(this.fParent.getDefaultLevel(), fileName);
	}
	
	// <summary>
	//   Overloaded. Logs a Windows Metafile file with a custom log level
	//   and displays it in the Console.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="fileName">
	//   The Windows Metafile file to display in the Console.
	// </param>
	// <remarks>
	//   This version of the method uses the supplied fileName argument as
	//   title to display in the Console.
	// </remarks>

	public void logMetafileFile(Level level, String fileName)
	{
		logCustomFile(level, fileName, LogEntryType.Graphic, 
			ViewerId.Metafile);
	}

	// <summary>
	//   Overloaded. Logs a Windows Metafile file and displays it in the
	//   Console using a custom title and default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="fileName">
	//   The Windows Metafile file to display in the Console.
	// </param>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void logMetafileFile(String title, String fileName)
	{
		logMetafileFile(this.fParent.getDefaultLevel(), title, fileName);
	}
	
	// <summary>
	//   Overloaded. Logs a Windows Metafile file and displays it in the
	//   Console using a custom title and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="fileName">
	//   The Windows Metafile file to display in the Console.
	// </param>
	
	public void logMetafileFile(Level level, String title, String fileName)
	{
		logCustomFile(level, title, fileName, LogEntryType.Graphic, 
			ViewerId.Metafile);
	}

	// <summary>
	//   Overloaded. Logs a stream with the default log level and interprets
	//   its content as Windows Metafile image.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">
	//   The stream to display as Windows Metafile image.
	// </param>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logMetafileStream(String title, InputStream stream)
	{
		logMetafileStream(this.fParent.getDefaultLevel(), title, stream);
	}
	
	// <summary>
	//   Overloaded. Logs a stream with a custom log level and interprets
	//   its content as Windows Metafile image.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">
	//   The stream to display as Windows Metafile image.
	// </param>

	public void logMetafileStream(Level level, String title, 
		InputStream stream)
	{
		logCustomStream(level, title, stream, LogEntryType.Graphic, 
			ViewerId.Metafile);
	}

	// <summary>
	//   Overloaded. Logs a string containing SQL source code with the
	//   default log level. The SQL source code is displayed with syntax
	//   highlighting in the Console.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="source">The SQL source code to log.</param>
	// <remarks>
	//   This method displays the supplied SQL source code with syntax
	//   highlighting in the Console.
	//
	//   It is especially useful to debug or track dynamically generated
	//   SQL source code.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logSql(String title, String source)	
	{
		logSql(this.fParent.getDefaultLevel(), title, source);
	}
	
	// <summary>
	//   Overloaded. Logs a string containing SQL source code with a custom
	//   log level. The SQL source code is displayed with syntax highlighting
	//   in the Console.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="source">The SQL source code to log.</param>
	// <remarks>
	//   This method displays the supplied SQL source code with syntax
	//   highlighting in the Console.
	//
	//   It is especially useful to debug or track dynamically generated
	//   SQL source code.
	// </remarks>

	public void logSql(Level level, String title, String source)	
	{
		logSource(level, title, source, SourceId.Sql);
	}

	// <summary>
	//   Overloaded. Logs source code that is displayed with syntax
	//   highlighting in the Console using the default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="source">The source code to log.</param>
	// <param name="id">Specifies the type of source code.</param>
	// <remarks>
	//   This method displays the supplied source code with syntax
	//   highlighting in the Console. The type of the source code can
	//   be specified by the 'id' argument. Please see the SourceId
	//   documentation for information on the supported source code
	//   types.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logSource(String title, String source, SourceId id)
	{
		logSource(this.fParent.getDefaultLevel(), title, source, id);
	}
	
	// <summary>
	//   Overloaded. Logs source code that is displayed with syntax
	//   highlighting in the Console using a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="source">The source code to log.</param>
	// <param name="id">Specifies the type of source code.</param>
	// <remarks>
	//   This method displays the supplied source code with syntax
	//   highlighting in the Console. The type of the source code can
	//   be specified by the 'id' argument. Please see the SourceId
	//   documentation for information on the supported source code
	//   types.
	// </remarks>

	public void logSource(Level level, String title, String source, 
		SourceId id)
	{
		if (isOn(level))
		{
			if (id == null)
			{
				logInternalError("logSource: id argument is null");
			}
			else
			{
				logCustomText(level, title, source, LogEntryType.Source,
					id.toViewerId());
			}
		}
	}

	// <summary>
	//   Overloaded. Logs the content of a file as source code with
	//   syntax highlighting using the default log level.
	// </summary>
	// <param name="fileName">The file which contains the source code.</param>
	// <param name="id">Specifies the type of source code.</param>
	// <remarks>
	//   This method displays the source file with syntax highlighting
	//   in the Console. The type of the source code can be specified by
	//   the 'id' argument. Please see the SourceId documentation for
	//   information on the supported source code types.
	//
	//   This version of the method uses the supplied fileName argument
	//   as title to display in the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logSourceFile(String fileName, SourceId id)
	{
		logSourceFile(this.fParent.getDefaultLevel(), fileName, id);
	}
	
	// <summary>
	//   Overloaded. Logs the content of a file as source code with syntax
	//   highlighting using a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="fileName">The file which contains the source code.</param>
	// <param name="id">Specifies the type of source code.</param>
	// <remarks>
	//   This method displays the source file with syntax highlighting
	//   in the Console. The type of the source code can be specified by
	//   the 'id' argument. Please see the SourceId documentation for
	//   information on the supported source code types.
	//
	//   This version of the method uses the supplied fileName argument
	//   as title to display in the Console.
	// </remarks>

	public void logSourceFile(Level level, String fileName, SourceId id)
	{
		logSourceFile(level, fileName, fileName, id);
	}

	// <summary>
	//   Overloaded. Logs the content of a file as source code with syntax
	//   highlighting using a custom title and default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="fileName">The file which contains the source code.</param>
	// <param name="id">Specifies the type of source code.</param>
	// <remarks>
	//   This method displays the source file with syntax highlighting
	//   in the Console. The type of the source code can be specified by
	//   the 'id' argument. Please see the SourceId documentation for
	//   information on the supported source code types.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logSourceFile(String title, String fileName, SourceId id)
	{
		logSourceFile(this.fParent.getDefaultLevel(), title, fileName, id);
	}
	
	// <summary>
	//   Overloaded. Logs the content of a file as source code with syntax
	//   highlighting using a custom title and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="fileName">The file which contains the source code.</param>
	// <param name="id">Specifies the type of source code.</param>
	// <remarks>
	//   This method displays the source file with syntax highlighting
	//   in the Console. The type of the source code can be specified by
	//   the 'id' argument. Please see the SourceId documentation for
	//   information on the supported source code types.
	// </remarks>

	public void logSourceFile(Level level, String title, String fileName, 
		SourceId id)
	{
		if (isOn(level))
		{
			if (id == null)
			{
				logInternalError("logSourceFile: id argument is null");
			}
			else
			{
				logCustomFile(level, title, fileName, LogEntryType.Source, 
					id.toViewerId());
			}
		}
	}

	// <summary>
	//   Overloaded. Logs the content of a stream as source code with syntax
	//   highlighting using the default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">The stream which contains the source code.</param>
	// <param name="id">Specifies the type of source code.</param>
	// <remarks>
	//   This method displays the content of a stream with syntax
	//   highlighting in the Console. The type of the source code can
	//   be specified by the 'id' argument. Please see the SourceId
	//   documentation for information on the supported source code types.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logSourceStream(String title, InputStream stream, SourceId id)
	{
		logSourceStream(this.fParent.getDefaultLevel(), title, stream, id);
	}
	
	// <summary>
	//   Overloaded. Logs the content of a stream as source code with syntax
	//   highlighting using a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">The stream which contains the source code.</param>
	// <param name="id">Specifies the type of source code.</param>
	// <remarks>
	//   This method displays the content of a stream with syntax
	//   highlighting in the Console. The type of the source code can
	//   be specified by the 'id' argument. Please see the SourceId
	//   documentation for information on the supported source code types.
	// </remarks>

	public void logSourceStream(Level level, String title, InputStream stream,
		SourceId id)
	{
		if (isOn(level))
		{
			if (id == null)
			{
				logInternalError("logSourceStream: id argument is null");
			}
			else
			{
				logCustomStream(level, title, stream, LogEntryType.Source, 
					id.toViewerId());
			}
		}
	}

	// <summary>
	//   Overloaded. Logs the content of a reader as source code with syntax
	//   highlighting using the default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="reader">The reader which contains the source code.</param>
	// <param name="id">Specifies the type of source code.</param>
	// <remarks>
	//   This method displays the content of a reader with syntax
	//   highlighting in the Console. The type of the source code can
	//   be specified by the 'id' argument. Please see the SourceId
	//   documentation for information on the supported source code types.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void logSourceReader(String title, Reader reader, SourceId id)
	{
		logSourceReader(this.fParent.getDefaultLevel(), title, reader, id);
	}
	
	// <summary>
	//   Overloaded. Logs the content of a reader as source code with syntax
	//   highlighting using a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="reader">The reader which contains the source code.</param>
	// <param name="id">Specifies the type of source code.</param>
	// <remarks>
	//   This method displays the content of a reader with syntax
	//   highlighting in the Console. The type of the source code can
	//   be specified by the 'id' argument. Please see the SourceId
	//   documentation for information on the supported source code types.
	// </remarks>
	
	public void logSourceReader(Level level, String title, Reader reader,
		SourceId id)
	{
		if (isOn(level))
		{
			if (id == null)
			{
				logInternalError("logSourceReader: id argument is null");
			}
			else
			{
				logCustomReader(level, title, reader, LogEntryType.Source, 
					id.toViewerId());
			}
		}
	}

	// <summary>
	//   Overloaded. Logs the public fields of an object with the default
	//   log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="instance">
	//   The object whose public fields should be logged.
	// </param>
	// <remarks>
	//   This method logs all public field names and their current values
	//   of an object. These key/value pairs will be displayed in the Console
	//   in an object inspector like viewer.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logObject(String title, Object instance)
	{
		logObject(this.fParent.getDefaultLevel(), title, instance);
	}
	
	// <summary>
	//   Overloaded. Logs the public fields of an object with a custom
	//   log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="instance">
	//   The object whose public fields should be logged.
	// </param>
	// <remarks>
	//   This method logs all public field names and their current values
	//   of an object. These key/value pairs will be displayed in the Console
	//   in an object inspector like viewer.
	// </remarks>

	public void logObject(Level level, String title, Object instance)
	{
		logObject(level, title, instance, false);
	}

	// <summary>
	//   Overloaded. Logs fields of an object with the default log level.
	//   Lets you specify if non public members should also be logged.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="instance">
	//   The object whose fields should be logged.
	// </param>
	// <param name="nonPublic">
	//   Specifies if non public members should be logged.
	// </param>
	// <remarks>
	//   This method logs all field names and their current values of an
	//   object. These key/value pairs will be displayed in the Console
	//   in an object inspector like viewer.
	//
	//   You can specify if non public or only public fields should
	//   be logged by setting the nonPublic argument to true or false,
	//   respectively.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logObject(String title, Object instance, boolean nonPublic)
	{
		logObject(this.fParent.getDefaultLevel(), title, instance, nonPublic);
	}
	
	// <summary>
	//   Overloaded. Logs fields of an object with a custom log level.
	//   Lets you specify if non public members should also be logged.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="instance">
	//   The object whose fields should be logged.
	// </param>
	// <param name="nonPublic">
	//   Specifies if non public members should be logged.
	// </param>
	// <remarks>
	//   This method logs all field names and their current values of an
	//   object. These key/value pairs will be displayed in the Console
	//   in an object inspector like viewer.
	//
	//   You can specify if non public or only public fields should
	//   be logged by setting the nonPublic argument to true or false,
	//   respectively.
	// </remarks>

	public void logObject(Level level, String title, Object instance, 
		boolean nonPublic)
	{
		if (!isOn(level))
		{
			return;
		}

		if (instance == null)
		{
			logInternalError("logObject: instance argument is null");
			return;
		}

		Field[] fields;
		Class cls = instance.getClass();
		ArrayList list = new ArrayList();

		InspectorViewerContext ctx = new InspectorViewerContext();

		while (cls != null)
		{
			try
			{
				// Try to get the fields of the object.
				fields = nonPublic ? cls.getDeclaredFields() : cls.getFields();
				AccessibleObject.setAccessible(fields, true);
			}
			catch (SecurityException e)
			{
				logInternalError("logObject: Can not change field access");
				return;
			}

			StringBuffer sb = new StringBuffer(32);
			for (int i = 0; i < fields.length; i++)
			{
				int mod = fields[i].getModifiers();

				if (!Modifier.isStatic(mod))
				{
					try
					{
						sb.append(ctx.escapeItem(fields[i].getName()));
						sb.append("=");
						sb.append(ctx.escapeItem(
							ObjectRenderer.renderObject(fields[i].get(instance)))
						);
					}
					catch (IllegalAccessException e)
					{
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
		try 
		{
			ctx.startGroup("Fields");
			
			if (!list.isEmpty())
			{
				Iterator it = list.iterator();
				while (it.hasNext())
				{
					ctx.appendLine((String) it.next());
				}
			}

			sendContext(level, title, LogEntryType.Object, ctx);
		}
		catch (Exception e)
		{
			logInternalError("logObject: " + e.getMessage());
		}
	}

	// <summary>
	//   Overloaded. Logs the content of an exception with a log level
	//   of Level.Error.
	// </summary>
	// <param name="t">The exception to log.</param>
	// <remarks>
	//   This method extracts the exception message and stack trace
	//   from the supplied exception and logs an error with this data.
	//   It is especially useful if you place calls to this method in
	//   exception handlers, of course. See logError for a more general
	//   method with a similar intention.
	//
	//   This version of the method uses the exception message as
	//   title to display in the Console.
	// </remarks>

	public void logException(Throwable t)
	{
		if (isOn(Level.Error))
		{
			if (t == null)
			{
				logInternalError("logException: t argument is null");
			}
			else
			{
				logException(t.getMessage(), t);
			}
		}
	}

	// <summary>
	//   Overloaded. Logs the content of an exception with a log level of
	//   Level.Error and a custom title.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="t">The exception to log.</param>
	// <remarks>
	//   This method extracts the exception message and stack trace
	//   from the supplied exception and logs an error with this data.
	//   It is especially useful if you place calls to this method in
	//   exception handlers, of course. See logError for a more general
	//   method with a similar intention.
	// </remarks>

	public void logException(String title, Throwable t)
	{
		if (isOn(Level.Error))
		{
			if (t == null)
			{
				logInternalError("logException: t argument is null");
				return;
			}
			
			DataViewerContext ctx = new DataViewerContext();
			try
			{
				try 
				{
					StringWriter writer = new StringWriter();
					try 
					{
						// Write the stack trace to the writer..
						t.printStackTrace(new PrintWriter(writer, true));

						// And then fill and send the context accordingly.
						ctx.loadFromText(writer.toString());
						sendContext(Level.Error, title, LogEntryType.Error, 
							ctx);
					}
					finally 
					{
						writer.close();	
					}					
				}
				catch (Exception e)
				{
					logInternalError("logException: " + e.getMessage());
				}
			}
			finally 
			{
				ctx.close();
			}
		}
	}

	private static String bytesToString(long n)
	{
		int i = 0;
		double d = (double) n;

		while (d >= 1024 && i <= 3)
		{
			d /= 1024;
			i++;
		}

		String s = MessageFormat.format(
			"{0,number,#.##}", new Object[] {new Double(d)}
		);

		switch (i)
		{
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

	// <summary>
	//   Overloaded. Logs memory statistics about the virtual machine with
	//   the default log level.
	// </summary>
	// <remarks>	
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logMemoryStatistic()
	{
		logMemoryStatistic(this.fParent.getDefaultLevel());
	}
	
	// <summary>
	//   Overloaded. Logs memory statistics about the virtual machine with
	//   a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>

	public void logMemoryStatistic(Level level)
	{
		logMemoryStatistic(level, "Memory statistic");
	}

	// <summary>
	//   Overloaded. Logs memory statistics about the virtual machine
	//   using a custom title and default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <remarks>	
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logMemoryStatistic(String title)
	{
		logMemoryStatistic(this.fParent.getDefaultLevel(), title);
	}

	// <summary>
	//   Overloaded. Logs memory statistics about the virtual machine
	//   using a custom title and default log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>

	public void logMemoryStatistic(Level level, String title)
	{
		if (isOn(level))
		{
			ValueListViewerContext ctx = new ValueListViewerContext();
			try
			{	
				try 
				{
					Runtime rt = Runtime.getRuntime();
					
					ctx.appendKeyValue("Free memory", 
						bytesToString(rt.freeMemory()));
					ctx.appendKeyValue("Maximal memory",
						bytesToString(rt.maxMemory()));
					ctx.appendKeyValue("Total memory",
						bytesToString(rt.totalMemory()));
					
					sendContext(level, title, LogEntryType.MemoryStatistic, 
						ctx);
				}
				catch (Exception e)
				{
					logInternalError("logMemoryStatistic: " + e.getMessage());
				}
			}
			finally 
			{
				ctx.close();
			}
		}
	}

	// <summary>
	//   Overloaded. Logs information about the current thread with the
	//   default log level.
	// </summary>
	// <remarks>
	//   This method logs information about the current thread. This
	//   includes its name, its current state and more.
	//
	//   logCurrentThread is especially useful in a multi-threaded
	//   program like in a network server application. By using this
	//   method you can easily track all threads of a process and
	//   obtain detailed information about them.
	//
	//   See logThread for a more general method, which can handle any
	//   thread.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logCurrentThread()
	{
		logCurrentThread(this.fParent.getDefaultLevel());
	}

	// <summary>
	//   Overloaded. Logs information about the current thread with a
	//   custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <remarks>
	//   This method logs information about the current thread. This
	//   includes its name, its current state and more.
	//
	//   logCurrentThread is especially useful in a multi-threaded
	//   program like in a network server application. By using this
	//   method you can easily track all threads of a process and
	//   obtain detailed information about them.
	//
	//   See logThread for a more general method, which can handle any
	//   thread.
	// </remarks>

	public void logCurrentThread(Level level)
	{
		if (isOn(level))
		{
			String title = "Current thread";
			Thread thread = Thread.currentThread();
			String name = thread.getName();

			if (name != null && name.length() > 0)
			{
				// Append the thread name to the title.
				title += ": " + name;
			}
			
			// Just call logThread with the
			// current thread and our own title.
			logThread(level, title, thread);
		}
	}

	// <summary>
	//   Overloaded. Logs information about the current thread using a
	//   custom title and the default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <remarks>
	//   This method logs information about the current thread. This
	//   includes its name, its current state and more.
	//
	//   logCurrentThread is especially useful in a multi-threaded
	//   program like in a network server application. By using this
	//   method you can easily track all threads of a process and
	//   obtain detailed information about them.
	//
	//   See logThread for a more general method, which can handle any
	//   thread.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logCurrentThread(String title)
	{
		logCurrentThread(this.fParent.getDefaultLevel(), title);
	}
	
	// <summary>
	//   Overloaded. Logs information about the current thread using a
	//   custom title and a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <remarks>
	//   This method logs information about the current thread. This
	//   includes its name, its current state and more.
	//
	//   logCurrentThread is especially useful in a multi-threaded
	//   program like in a network server application. By using this
	//   method you can easily track all threads of a process and
	//   obtain detailed information about them.
	//
	//   See logThread for a more general method, which can handle any
	//   thread.
	// </remarks>

	public void logCurrentThread(Level level, String title)
	{
		if (isOn(level))
		{
			// Just call logThread with the current thread.
			logThread(level, title, Thread.currentThread());
		}
	}

	// <summary>
	//   Overloaded. Logs information about a thread with the default log
	//   level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="thread">The thread to log.</param>
	// <remarks>
	//   This method logs information about the supplied thread. This
	//   includes its name, its current state and more.
	//
	//   logThread is especially useful in a multi-threaded program
	//   like in a network server application. By using this method you
	//   can easily track all threads of a process and obtain detailed
	//   information about them.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void logThread(String title, Thread thread)
	{
		logThread(this.fParent.getDefaultLevel(), title, thread);
	}

	// <summary>
	//   Overloaded. Logs information about a thread with a custom log
	//   level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="thread">The thread to log.</param>
	// <remarks>
	//   This method logs information about the supplied thread. This
	//   includes its name, its current state and more.
	//
	//   logThread is especially useful in a multi-threaded program
	//   like in a network server application. By using this method you
	//   can easily track all threads of a process and obtain detailed
	//   information about them.
	// </remarks>
		
	public void logThread(Level level, String title, Thread thread)
	{
		if (isOn(level))
		{
			if (thread == null)
			{
				logInternalError("logThread: thread argument is null");
				return;
			}
			
			ValueListViewerContext ctx = new ValueListViewerContext();
			try 
			{
				try 
				{
					ctx.appendKeyValue("Name", thread.getName());
					ctx.appendKeyValue("Alive", thread.isAlive());
					ctx.appendKeyValue("Priority", thread.getPriority());
					ctx.appendKeyValue("Daemon", thread.isDaemon());
					ctx.appendKeyValue("Interrupted", thread.isInterrupted());
					sendContext(level, title, LogEntryType.Text, ctx);
				}
				catch (Exception e)
				{
					logInternalError("logThread: " + e.getMessage());
				}
			}
			finally
			{
				ctx.close();
			}
		}
	}

	// <summary>
	//   Overloaded. Logs the content of a collection with the default log
	//   level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="c">The collection to log.</param>
	// <remarks>
	//   This method iterates through the supplied collection and
	//   <link ObjectRenderer.renderObject, renders> every element into
	//   a string. These elements will be displayed in a listview in
	//   the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logCollection(String title, Collection c)
	{
		logCollection(this.fParent.getDefaultLevel(), title, c);
	}
	
	// <summary>
	//   Overloaded. Logs the content of a collection with a custom log
	//   level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="c">The collection to log.</param>
	// <remarks>
	//   This method iterates through the supplied collection and
	//   <link ObjectRenderer.renderObject, renders> every element into
	//   a string. These elements will be displayed in a listview in
	//   the Console.
	// </remarks>

	public void logCollection(Level level, String title, Collection c)
	{
		if (isOn(level))
		{
			if (c == null)
			{
				logInternalError("logCollection: c argument is null");
				return;
			}
			
			ListViewerContext ctx = new ListViewerContext();
			try
			{				
				try 
				{
					Iterator it = c.iterator();

					while (it.hasNext())
					{
						Object o = it.next();
						
						if (o == c)
						{
							ctx.appendLine("<cycle>");
						}
						else 
						{
							ctx.appendLine(ObjectRenderer.renderObject(o));
						}	
					}
								
					sendContext(level, title, LogEntryType.Text, ctx);
				}
				catch (Exception e)
				{
					logInternalError("logCollection: " + e.getMessage());
				}
			}
			finally 
			{
				ctx.close();
			}
		}
	}

	// <summary>
	//   Overloaded. Logs the content of a map with the default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="map">The map to log.</param>
	// <remarks>
	//   This methods iterates through the supplied dictionary and
	//   <link ObjectRenderer.renderObject, renders> every key/value
	//   pair into a string. These pairs will be displayed in a special
	//   key/value table in the Console.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logMap(String title, Map map)
	{
		logMap(this.fParent.getDefaultLevel(), title, map);
	}
	
	// <summary>
	//   Overloaded. Logs the content of a map with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="map">The map to log.</param>
	// <remarks>
	//   This methods iterates through the supplied dictionary and
	//   <link ObjectRenderer.renderObject, renders> every key/value
	//   pair into a string. These pairs will be displayed in a special
	//   key/value table in the Console.
	// </remarks>

	public void logMap(Level level, String title, Map map)
	{
		if (isOn(level))
		{
			if (map == null)
			{
				logInternalError("logMap: map argument is null");
				return;
			}
			
			ValueListViewerContext ctx = new ValueListViewerContext();
			try 
			{				
				try
				{
					Iterator it = map.keySet().iterator();

					while (it.hasNext())
					{
						Object key = it.next();
						Object val = map.get(key);

						ctx.appendKeyValue(
							(key == map ? "<cycle>" : ObjectRenderer.renderObject(key)),
							(val == map ? "<cycle>" : ObjectRenderer.renderObject(val)) 
						);
					}
				
					sendContext(level, title, LogEntryType.Text, ctx);
				}
				catch (Exception e)
				{
					logInternalError("logMap: " + e.getMessage());
				}
			}
			finally 
			{
				ctx.close();
			}
		}
	}

	// <summary>
	//   Overloaded. Logs the content of an array with the default log
	//   level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="array">The array to log.</param>
	// <remarks>
	//   This method iterates through the supplied array and
	//   <link ObjectRenderer.renderObject, renders> every element into
	//   a string. These elements will be displayed in a listview in
	//   the Console.
	//
	//   Please see logCollection to log a collection and logMap to log
	//   a map.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logArray(String title, Object[] array)
	{
		logArray(this.fParent.getDefaultLevel(), title, array);
	}
	
	// <summary>
	//   Overloaded. Logs the content of an array with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="array">The array to log.</param>
	// <remarks>
	//   This method iterates through the supplied array and
	//   <link ObjectRenderer.renderObject, renders> every element into
	//   a string. These elements will be displayed in a listview in
	//   the Console.
	//
	//   Please see logCollection to log a collection and logMap to log
	//   a map.
	// </remarks>

	public void logArray(Level level, String title, Object[] array)
	{
		if (isOn(level))
		{
			if (array == null)
			{
				logInternalError("logArray: array argument is null");
				return;
			}

			ListViewerContext ctx = new ListViewerContext();
			try 
			{				
				try 
				{
					for (int i = 0, len = array.length; i < len; i++)
					{
						Object o = array[i];
	
						if (o == array)
						{
							ctx.appendLine("<cycle>");					
						}
						else 
						{
							ctx.appendLine(ObjectRenderer.renderObject(o));
						}
					}				
					
					sendContext(level, title, LogEntryType.Text, ctx);
				}
				catch (Exception e)
				{
					logInternalError("logArray: " + e.getMessage());
				}
			}
			finally 
			{
				ctx.close();
			}
		}
	}

	private ViewerContext buildStackTrace()
	{
		ListViewerContext ctx = new ListViewerContext();

		StackTraceElement[] straces = 
			new Exception("Current stack trace").getStackTrace();
			
		for (int i = 2; i < straces.length; i++)
		{
			StackTraceElement elem = straces[i];

			if (elem != null)
			{
				// Convert the stack trace element into
				// a string and append it to our context.
				ctx.appendLine(straces[i].toString().trim());
			}
		}
			
		return ctx;
	}

	// <summary>
	//   Overloaded. Logs the current stack trace with the default log
	//   level.
	// </summary>
	// <remarks>
	//   This method logs the current stack trace. The resulting
	//   <link LogEntry, Log Entry> contains all methods including the
	//   related classes that are currently on the stack. Furthermore
	//   the filename and line numbers will be included. 
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logCurrentStackTrace()
	{
		if (isOn(this.fParent.getDefaultLevel()))
		{
			ViewerContext ctx = buildStackTrace();
			try 
			{
				sendContext(this.fParent.getDefaultLevel(),
					"Current stack trace", LogEntryType.Text, ctx);
			}
			finally 
			{
				ctx.close();
			}
		}
	}
	
	// <summary>
	//   Overloaded. Logs the current stack trace with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <remarks>
	//   This method logs the current stack trace. The resulting
	//   <link LogEntry, Log Entry> contains all methods including the
	//   related classes that are currently on the stack. Furthermore
	//   the filename and line numbers will be included. 
	// </remarks>

	public void logCurrentStackTrace(Level level)
	{
		if (isOn(level))
		{
			ViewerContext ctx = buildStackTrace();
			try 
			{
				sendContext(level, "Current stack trace", LogEntryType.Text, 
					ctx);
			}
			finally 
			{
				ctx.close();
			}
		}
	}

	// <summary>
	//   Overloaded. Logs the current stack trace using a custom title
	//   and default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <remarks>
	//   This method logs the current stack trace. The resulting
	//   <link LogEntry, Log Entry> contains all methods including the
	//   related classes that are currently on the stack. Furthermore
	//   the filename and line numbers will be included. 
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logCurrentStackTrace(String title)
	{
		if (isOn(this.fParent.getDefaultLevel()))
		{
			ViewerContext ctx = buildStackTrace();
			try 
			{
				sendContext(this.fParent.getDefaultLevel(), title, 
					LogEntryType.Text, ctx);
			}
			finally 
			{
				ctx.close();
			}
		}
	}
	
	// <summary>
	//   Overloaded. Logs the current stack trace using a custom title
	//   and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <remarks>
	//   This method logs the current stack trace. The resulting
	//   <link LogEntry, Log Entry> contains all methods including the
	//   related classes that are currently on the stack. Furthermore
	//   the filename and line numbers will be included. 
	// </remarks>

	public void logCurrentStackTrace(Level level, String title)
	{
		if (isOn(level))
		{
			ViewerContext ctx = buildStackTrace();
			try 
			{
				sendContext(level, title, LogEntryType.Text, ctx);
			}
			finally 
			{
				ctx.close();
			}
		}
	}

	// <summary>
	//   Overloaded. Logs information about the system with the default log
	//   level.
	// </summary>
	// <remarks>
	//   The logged information include the version of the operating
	//   system, the Java version and more. This method is useful for
	//   logging general information at the program startup. This
	//   guarantees that the support staff or developers have general
	//   information about the execution environment.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logSystem()
	{
		logSystem(this.fParent.getDefaultLevel());
	}
	
	// <summary>
	//   Overloaded. Logs information about the system with a custom log
	//   level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <remarks>
	//   The logged information include the version of the operating
	//   system, the Java version and more. This method is useful for
	//   logging general information at the program startup. This
	//   guarantees that the support staff or developers have general
	//   information about the execution environment.
	// </remarks>

	public void logSystem(Level level)
	{
		logSystem(level, "System information");
	}

	// <summary>
	//   Overloaded. Logs information about the system using a custom
	//   title and the default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <remarks>
	//   The logged information include the version of the operating
	//   system, the Java version and more. This method is useful for
	//   logging general information at the program startup. This
	//   guarantees that the support staff or developers have general
	//   information about the execution environment.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logSystem(String title)
	{
		logSystem(this.fParent.getDefaultLevel(), title);
	}
	
	// <summary>
	//   Overloaded. Logs information about the system using a custom
	//   title and a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <remarks>
	//   The logged information include the version of the operating
	//   system, the Java version and more. This method is useful for
	//   logging general information at the program startup. This
	//   guarantees that the support staff or developers have general
	//   information about the execution environment.
	// </remarks>

	public void logSystem(Level level, String title)
	{
		if (!isOn(level))
		{
			return;
		}

		InspectorViewerContext ctx = new InspectorViewerContext();
		try 
		{
			try 
			{
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
			}
			catch (Exception e)
			{
				logInternalError("logSystem: " + e.getMessage());
			}
		}
		finally 
		{
			ctx.close();
		}
	}

	// <summary>
	//   Overloaded. Logs the metadata of a ResultSet with the default log
	//   level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="rset">
	//   The ResultSet instance whose metadata should be logged.
	// </param>
	// <remarks>
	//   This method sends the schema of a ResultSet. A schema includes
	//   the most important properties of every column in the set.
	//
	//   This logResultSetMetaData method is especially useful in database
	//   applications with lots of queries. It gives you the possibility to
	//   see the raw schema of query results.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logResultSetMetaData(String title, ResultSet rset)
	{
		logResultSetMetaData(this.fParent.getDefaultLevel(), title, rset);
	}
	
	// <summary>
	//   Overloaded. Logs the metadata of a ResultSet with a custom log
	//   level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="rset">
	//   The ResultSet instance whose metadata should be logged.
	// </param>
	// <remarks>
	//   This method sends the schema of a ResultSet. A schema includes
	//   the most important properties of every column in the set.
	//
	//   This logResultSetMetaData method is especially useful in database
	//   applications with lots of queries. It gives you the possibility to
	//   see the raw schema of query results.
	// </remarks>

	public void logResultSetMetaData(Level level, String title, 
		ResultSet rset)
	{
		if (isOn(level))
		{
			if (rset == null)
			{
				logInternalError("logResultSetMetaData: rset argument is null");
			}
			else 
			{
				try 
				{
					logResultSetMetaData(level, title, rset.getMetaData());
				}
				catch (SQLException e)
				{
					logInternalError("logResultSetMetaData: " + e.getMessage());
				}
			}
		}
	}

	// <summary>
	//   Overloaded. Logs the metadata of a ResultSet with the default log
	//   level.
	// </summary>
	// <param name="rmeta">
	//   The metadata of a ResultSet which should be logged.
	// </param>
	// <remarks>
	//   This method sends the schema of a ResultSet. A schema includes
	//   the most important properties of every column in the set.
	//
	//   This logResultSetMetaData method is especially useful in database
	//   applications with lots of queries. It gives you the possibility to
	//   see the raw schema of query results.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void logResultSetMetaData(String title, ResultSetMetaData rmeta)
	{
		logResultSetMetaData(this.fParent.getDefaultLevel(), title, rmeta);
	}

	// <summary>
	//   Overloaded. Logs the metadata of a ResultSet with a custom log
	//   level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="rmeta">
	//   The metadata of a ResultSet which should be logged.
	// </param>
	// <remarks>
	//   This method sends the schema of a ResultSet. A schema includes
	//   the most important properties of every column in the set.
	//
	//   This logResultSetMetaData method is especially useful in database
	//   applications with lots of queries. It gives you the possibility to
	//   see the raw schema of query results.
	// </remarks>

	public void logResultSetMetaData(Level level, String title,
		ResultSetMetaData rmeta)
	{
		if (!isOn(level))
		{
			return;
		}

		if (rmeta == null)
		{
			logInternalError("logResultSetMetaData: rmeta argument is null");
			return;
		}

		TableViewerContext ctx = new TableViewerContext();
		try 
		{
			try 
			{
				// Write the header first.
				ctx.appendHeader(
					"Name, Type, \"Read Only\", Searchable, Nullable," +
					"\"Auto Increment\", \"Case Sensitive\"\r\n\r\n"
				);

				// Then the actual meta data.				
				for (int i = 1, count = rmeta.getColumnCount(); i <= count; i++)
				{
					ctx.beginRow();
					try 
					{						
						ctx.addRowEntry(rmeta.getColumnName(i));
						ctx.addRowEntry(rmeta.getColumnTypeName(i));
						ctx.addRowEntry(rmeta.isReadOnly(i));				
						ctx.addRowEntry(rmeta.isSearchable(i));
				
						switch (rmeta.isNullable(i))
						{
							case ResultSetMetaData.columnNoNulls:
							{
								ctx.addRowEntry(false);
								break;
							}
					
							case ResultSetMetaData.columnNullable:
							{
								ctx.addRowEntry(true);
								break;
							}
					
							default:
							{
								ctx.addRowEntry("unknown");
								break;
							}
						}
				
						ctx.addRowEntry(rmeta.isAutoIncrement(i));
						ctx.addRowEntry(rmeta.isCaseSensitive(i));
					}
					finally 
					{
						ctx.endRow();
					}
				}
				
				sendContext(level, title, LogEntryType.DatabaseStructure, 
					ctx);
			}
			catch (Exception e)
			{
				logInternalError("logResultSetMetaData: " + e.getMessage());
			}
		}
		finally 
		{
			ctx.close();
		}
	}

	// <summary>
	//   Overloaded. Logs the content of a ResultSet with the default log
	//   level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="rset">
	//   The ResultSet instance whose content should be logged.
	// </param>
	// <remarks>
	//   This method logs the content of a ResultSet instance.
	//
	//   This logResultSet method is especially useful in database
	//   applications with lots of queries. It gives you the possibility
	//   to see the raw query results.
	//
	//   Please note that this method tries to restore the original row
	//   position of the supplied ResultSet instance after reading the
	//   content, but only if its type is not ResultSet.TYPE_FORWARD_ONLY.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void logResultSet(String title, ResultSet rset)
	{
		logResultSet(this.fParent.getDefaultLevel(), title, rset);
	}
	
	// <summary>
	//   Overloaded. Logs the content of a ResultSet with a custom log
	//   level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="rset">
	//   The ResultSet instance whose content should be logged.
	// </param>
	// <remarks>
	//   This method logs the content of a ResultSet instance.
	//
	//   This logResultSet method is especially useful in database
	//   applications with lots of queries. It gives you the possibility
	//   to see the raw query results.
	//
	//   Please note that this method tries to restore the original row
	//   position of the supplied ResultSet instance after reading the
	//   content, but only if its type is not ResultSet.TYPE_FORWARD_ONLY.
	// </remarks>
	
	public void logResultSet(Level level, String title, ResultSet rset)
	{
		if (!isOn(level))
		{
			return;
		}

		if (rset == null)
		{
			logInternalError("logResultSet: rset argument is null");
			return;
		}
		
		TableViewerContext ctx = new TableViewerContext();
		try 
		{
			try 
			{
				ResultSetMetaData meta = rset.getMetaData();
				int columnCount = meta.getColumnCount();
	
				// We need to write the headers of the table,
				// that means, the names of the colums.
				
				ctx.beginRow();
				try 
				{
					for (int i = 1; i <= columnCount; i++)				
					{
						ctx.addRowEntry(meta.getColumnName(i));
					}
				}
				finally 
				{
					ctx.endRow();
				}

				int oldRow = rset.getRow();
				try 
				{
					if (rset.getType() != ResultSet.TYPE_FORWARD_ONLY)
					{
						if (!rset.isFirst())
						{
							try
							{
								// Try to move the result set to the first row.
								rset.first();
							}
							catch (SQLException e)
							{
								// Ignore possible exceptions and display
								// only the rows from the current position.
							}
						}
					}
				
					// After we've written the table header, we
					// can now write the whole result set content.
				
					while (rset.next())
					{
						ctx.beginRow();
						try 
						{
							for (int i = 1; i <= columnCount; i++)
							{
								ctx.addRowEntry(rset.getString(i));
							}
						}
						finally 
						{
							ctx.endRow();
						}
					}

					sendContext(level, title, LogEntryType.DatabaseResult, 
						ctx);
				}
				finally 
				{
					if (rset.getType() != ResultSet.TYPE_FORWARD_ONLY)
					{
						// Reset to the original row position.
						rset.absolute(oldRow);
					}
				}
			}
			catch (Exception e)
			{				
				logInternalError("logResultSet: " + e.getMessage());
			}
		}
		finally 
		{
			ctx.close();
		}
	}

	// <summary>
	//   Overloaded. Logs the content of a StringBuffer instance with the
	//   default log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="sb">
	//   The StringBuffer instance whose content should be logged.
	// </param>
	// <remarks>
	//   The content of the supplied StringBuffer instance is displayed in
	//   a read-only text field.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logStringBuffer(String title, StringBuffer sb)
	{
		logStringBuffer(this.fParent.getDefaultLevel(), title, sb);
	}
	
	// <summary>
	//   Overloaded. Logs the content of a StringBuffer instance with a
	//   custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="sb">
	//   The StringBuffer instance whose content should be logged.
	// </param>
	// <remarks>
	//   The content of the supplied StringBuffer instance is displayed in
	//   a read-only text field.
	// </remarks>

	public void logStringBuffer(Level level, String title, StringBuffer sb)
	{
		if (isOn(level))
		{
			if (sb == null)
			{
				logInternalError("logStringBuffer: sb argument is null");
			}
			else
			{
				logText(level, title, sb.toString());
			}
		}
	}

	// <summary>
	//   Overloaded. Logs the content of a binary stream with the default
	//   log level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">The stream whose content should be logged.</param>
	// <remarks>
	//   The content of the supplied binary stream will be displayed in a
	//   read-only hex editor.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logStream(String title, InputStream stream)
	{
		logStream(this.fParent.getDefaultLevel(), title, stream);
	}
	
	// <summary>
	//   Overloaded. Logs the content of a binary stream with a custom
	//   log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="stream">The stream whose content should be logged.</param>
	// <remarks>
	//   The content of the supplied binary stream will be displayed in a
	//   read-only hex editor.
	// </remarks>

	public void logStream(Level level, String title, InputStream stream)
	{
		logBinaryStream(level, title, stream);
	}

	// <summary>
	//   Overloaded. Logs the content of a text reader with the default log
	//   level.
	// </summary>
	// <param name="title">The title to display in the Console.</param>
	// <param name="reader">The reader whose content should be logged.</param>
	// <remarks>
	//   The content of the supplied reader is displayed in a read-only
	//   text field.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void logReader(String title, Reader reader)
	{
		logReader(this.fParent.getDefaultLevel(), title, reader);
	}
	
	// <summary>
	//   Overloaded. Logs the content of a text reader with a custom log
	//   level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title to display in the Console.</param>
	// <param name="reader">The reader whose content should be logged.</param>
	// <remarks>
	//   The content of the supplied reader is displayed in a read-only
	//   text field.
	// </remarks>

	public void logReader(Level level, String title, Reader reader)
	{
		logTextReader(level, title, reader);
	}

	// <summary>
	//   Clears all Log Entries in the Console.
	// </summary>

	public void clearLog()
	{
		if (isOn())
		{
			sendControlCommand(ControlCommandType.ClearLog, null);
		}
	}

	// <summary>
	//   Clears all Watches in the Console.
	// </summary>

	public void clearWatches()
	{
		if (isOn())
		{
			sendControlCommand(ControlCommandType.ClearWatches, null);
		}
	}

	// <summary>
	//   Clears all AutoViews in the Console.
	// </summary>

	public void clearAutoViews()
	{
		if (isOn())
		{
			sendControlCommand(ControlCommandType.ClearAutoViews, null);
		}
	}

	// <summary>
	//   Resets the whole Console.
	// </summary>
	// <remarks>
	//   This methods resets the whole Console. This means that all
	//   Watches, Log Entries, Process Flow entries and AutoViews
	//   will be deleted.
	// </remarks>

	public void clearAll()
	{
		if (isOn())
		{
			sendControlCommand(ControlCommandType.ClearAll, null);
		}
	}

	// <summary>
	//   Clears all Process Flow entries in the Console.
	// </summary>

	public void clearProcessFlow()
	{
		if (isOn())
		{
			sendControlCommand(ControlCommandType.ClearProcessFlow, null);
		}
	}

	private int updateCounter(String name, boolean increment)
	{
		int value;
		String key = name.toLowerCase();
		
		synchronized(this.fCounter)
		{		
			if (this.fCounter.containsKey(key))
			{
				Integer i = (Integer) this.fCounter.get(key);
				value = i.intValue();
			}
			else
			{
				value = 0;
			}
			
			if (increment)
			{
				value++;
			}
			else 
			{
				value--;
			}
			
			this.fCounter.put(key, new Integer(value));
		}
		
		return value;
	}
	
	// <summary>
	//   Overloaded. Increments a named counter by one and automatically
	//   sends its name and value as integer watch with the default log
	//   level.
	// </summary>
	// <param name="name">The name of the counter to log.</param>
	// <remarks>
	//   The Session class tracks a list of so called named counters.
	//   A counter has a name and a value of type integer. This method
	//   increments the value for the specified counter by one and then
	//   sends a normal integer watch with the name and value of the
	//   counter. The initial value of a counter is 0. To reset the
	//   value of a counter to 0 again, you can call resetCounter.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	//   
	//   See decCounter for a method which decrements the value of a
	//   named counter instead of incrementing it.
	// </remarks>
	
	public void incCounter(String name)
	{
		incCounter(this.fParent.getDefaultLevel(), name);
	}
	
	// <summary>
	//   Overloaded. Increments a named counter by one and automatically
	//   sends its name and value as integer watch with a custom log
	//   level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the counter to log.</param>
	// <remarks>
	//   The Session class tracks a list of so called named counters.
	//   A counter has a name and a value of type integer. This method
	//   increments the value for the specified counter by one and then
	//   sends a normal integer watch with the name and value of the
	//   counter. The initial value of a counter is 0. To reset the
	//   value of a counter to 0 again, you can call resetCounter.
	//
	//   See decCounter for a method which decrements the value of a
	//   named counter instead of incrementing it.
	// </remarks>
	
	public void incCounter(Level level, String name)
	{
		if (isOn(level))
		{
			if (name == null)
			{
				logInternalError("incCounter: name argument is null");
			}
			else 
			{
				int value = updateCounter(name, true);
				sendWatch(level, name, Integer.toString(value),
					WatchType.Integer);
			}
		}
	}
	
	// <summary>
	//   Overloaded. Decrements a named counter by one and automatically
	//   sends its name and value as integer watch with the default log
	//   level.
	// </summary>
	// <param name="name">The name of the counter to log.</param>
	// <remarks>
	//   The Session class tracks a list of so called named counters.
	//   A counter has a name and a value of type integer. This method
	//   decrements the value for the specified counter by one and then
	//   sends a normal integer watch with the name and value of the
	//   counter. The initial value of a counter is 0. To reset the
	//   value of a counter to 0 again, you can call resetCounter.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	//   
	//   See incCounter for a method which increments the value of a
	//   named counter instead of decrementing it.
	// </remarks>

	public void decCounter(String name)
	{
		decCounter(this.fParent.getDefaultLevel(), name);
	}
	
	// <summary>
	//   Overloaded. Decrements a named counter by one and automatically
	//   sends its name and value as integer watch with a custom log
	//   level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the counter to log.</param>
	// <remarks>
	//   The Session class tracks a list of so called named counters.
	//   A counter has a name and a value of type integer. This method
	//   decrements the value for the specified counter by one and then
	//   sends a normal integer watch with the name and value of the
	//   counter. The initial value of a counter is 0. To reset the
	//   value of a counter to 0 again, you can call resetCounter.
	//
	//   See incCounter for a method which increments the value of a
	//   named counter instead of decrementing it.
	// </remarks>

	public void decCounter(Level level, String name)
	{
		if (isOn(level))
		{
			if (name == null)
			{
				logInternalError("decCounter: name argument is null");
			}
			else 
			{
				int value = updateCounter(name, false);
				sendWatch(level, name, Integer.toString(value),
					WatchType.Integer);
			}
		}
	}
	
	// <summary>
	//   Resets a named counter to its initial value of 0.
	// </summary>
	// <param name="name">The name of the counter to reset.</param>
	// <remarks>
	//   This method resets the integer value of a named counter to 0
	//   again. If the supplied counter is unknown, this method has no
	//   effect. Please refer to the incCounter and decCounter methods
	//   for more information about named counters.
	// </remarks>
	
	public void resetCounter(String name)
	{
		if (name == null)
		{
			logInternalError("resetCounter: name argument is null");
			return;
		}
		
		String key = name.toLowerCase();
		
		synchronized(this.fCounter)
		{
			this.fCounter.remove(key);
		}
	}
	
	// <summary>
	//   Overloaded. Logs a char Watch with the default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void watchChar(String name, char value)
	{
		watchChar(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a char Watch with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>

	public void watchChar(Level level, String name, char value)
	{
		if (isOn(level))
		{
			sendWatch(level, name, Character.toString(value), WatchType.Char);
		}
	}

	// <summary>
	//   Overloaded. Logs a String Watch with the default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watchString(String name, String value)
	{
		watchString(this.fParent.getDefaultLevel(), name, value);	
	}
	
	// <summary>
	//   Overloaded. Logs a String Watch with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	
	public void watchString(Level level, String name, String value)
	{
		if (isOn(level))
		{
			sendWatch(level, name, value, WatchType.String);
		}
	}

	// <summary>
	//   Overloaded. Logs a byte Watch with the default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watchByte(String name, byte value)
	{
		watchByte(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a byte Watch with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	
	public void watchByte(Level level, String name, byte value)
	{
		watchByte(level, name, value, false);
	}

	// <summary>
	//   Overloaded. Logs a byte Watch with an optional hexadecimal
	//   representation and default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <param name="includeHex">
	//   Indicates if a hexadecimal representation should be included.
	// </param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method logs a byte Watch. You can specify if a hexadecimal
	//   representation should be included as well by setting the
	//   includeHex parameter to true.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watchByte(String name, byte value, boolean includeHex)
	{
		watchByte(this.fParent.getDefaultLevel(), name, value, includeHex);
	}
	
	// <summary>
	//   Overloaded. Logs a byte Watch with an optional hexadecimal
	//   representation and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <param name="includeHex">
	//   Indicates if a hexadecimal representation should be included.
	// </param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method logs a byte Watch. You can specify if a
	//   hexadecimal representation should be included as well
	//   by setting the includeHex parameter to true.
	// </remarks>
	
	public void watchByte(Level level, String name, byte value, 
		boolean includeHex)
	{
		if (isOn(level))
		{
			String v = Byte.toString(value);
			
			if (includeHex)
			{
				StringBuffer sb = new StringBuffer();
				sb.append(" (0x");
				sb.append(longToHex(value, 2));
				sb.append(")");
				v += sb.toString();
			}
			
			sendWatch(level, name, v, WatchType.Integer);
		}		
	}

	// <summary>
	//   Overloaded. Logs a short integer Watch with the default log
	//   level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void watchShort(String name, short value)
	{
		watchShort(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a short integer Watch with a custom log
	//   level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>

	public void watchShort(Level level, String name, short value)
	{
		watchShort(level, name, value, false);
	}

	// <summary>
	//   Overloaded. Logs a short integer Watch with an optional
	//   hexadecimal representation and default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <param name="includeHex">
	//   Indicates if a hexadecimal representation should be included.
	// </param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method logs a short integer Watch. You can specify if a
	//   hexadecimal representation should be included as well by setting
	//   the includeHex parameter to true.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watchShort(String name, short value, boolean includeHex)
	{
		watchShort(this.fParent.getDefaultLevel(), name, value, includeHex);
	}
	
	// <summary>
	//   Overloaded. Logs a short integer Watch with an optional
	//   hexadecimal representation and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <param name="includeHex">
	//   Indicates if a hexadecimal representation should be included.
	// </param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method logs a short integer Watch. You can specify
	//   if a hexadecimal representation should be included as well
	//   by setting the includeHex parameter to true.
	// </remarks>
	
	public void watchShort(Level level, String name, short value, 
		boolean includeHex)
	{
		if (isOn(level))
		{
			String v = Short.toString(value);
			
			if (includeHex)
			{
				StringBuffer sb = new StringBuffer();
				sb.append(" (0x");
				sb.append(longToHex(value, 4));
				sb.append(")");
				v += sb.toString();
			}
			
			sendWatch(level, name, v, WatchType.Integer);
		}
	}
	
	// <summary>
	//   Overloaded. Logs an integer Watch with the default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watchInt(String name, int value)
	{
		watchInt(this.fParent.getDefaultLevel(), name, value);	
	}
	
	// <summary>
	//   Overloaded. Logs an integer Watch with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	
	public void watchInt(Level level, String name, int value)
	{
		watchInt(level, name, value, false);
	}

	// <summary>
	//   Overloaded. Logs an integer Watch with an optional hexadecimal
	//   representation and default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <param name="includeHex">
	//   Indicates if a hexadecimal representation should be included.
	// </param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method logs an integer Watch. You can specify if a
	//   hexadecimal representation should be included as well by
	//   setting the includeHex parameter to true.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watchInt(String name, int value, boolean includeHex)
	{
		watchInt(this.fParent.getDefaultLevel(), name, value, includeHex);
	}
	
	// <summary>
	//   Overloaded. Logs an integer Watch with an optional hexadecimal
	//   representation and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <param name="includeHex">
	//   Indicates if a hexadecimal representation should be included.
	// </param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method logs an integer Watch. You can specify if
	//   a hexadecimal representation should be included as well
	//   by setting the includeHex parameter to true.
	// </remarks>
	
	public void watchInt(Level level, String name, int value, 
		boolean includeHex)
	{
		if (isOn(level))
		{
			String v = Integer.toString(value);
			
			if (includeHex)
			{
				StringBuffer sb = new StringBuffer();
				sb.append(" (0x");
				sb.append(longToHex(value, 8));
				sb.append(")");
				v += sb.toString();
			}
			
			sendWatch(level, name, v, WatchType.Integer);
		}
	}

	// <summary>
	//   Overloaded. Logs a long integer Watch with the default log
	//   level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watchLong(String name, long value)
	{
		watchLong(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a long integer Watch with a custom log
	//   level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	
	public void watchLong(Level level, String name, long value)
	{
		watchLong(level, name, value, false);
	}

	// <summary>
	//   Overloaded. Logs a long integer Watch with an optional
	//   hexadecimal representation and default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <param name="includeHex">
	//   Indicates if a hexadecimal representation should be included.
	// </param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method logs a long integer Watch. You can specify if a
	//   hexadecimal representation should be included as well by setting
	//   the includeHex parameter to true.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watchLong(String name, long value, boolean includeHex)
	{
		watchLong(this.fParent.getDefaultLevel(), name, value, includeHex);	
	}
	
	// <summary>
	//   Overloaded. Logs a long integer Watch with an optional
	//   hexadecimal representation and custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <param name="includeHex">
	//   Indicates if a hexadecimal representation should be included.
	// </param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method logs a long integer Watch. You can specify
	//   if a hexadecimal representation should be included as well
	//   by setting the includeHex parameter to true.
	// </remarks>
	
	public void watchLong(Level level, String name, long value, 
		boolean includeHex)
	{
		if (isOn(level))
		{
			String v = Long.toString(value);
			
			if (includeHex)
			{
				StringBuffer sb = new StringBuffer();
				sb.append(" (0x");
				sb.append(longToHex(value, 16));
				sb.append(")");
				v += sb.toString();
			}
			
			sendWatch(level, name, v, WatchType.Integer);
		}
	}

	// <summary>
	//   Overloaded. Logs a float Watch with the default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watchFloat(String name, float value)
	{
		watchFloat(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a float Watch with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	
	public void watchFloat(Level level, String name, float value)
	{
		if (isOn(level))
		{
			sendWatch(level, name, Float.toString(value), WatchType.Float);
		}
	}

	// <summary>
	//   Overloaded. Logs a double Watch with the default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watchDouble(String name, double value)
	{
		watchDouble(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a double Watch with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	
	public void watchDouble(Level level, String name, double value)
	{
		if (isOn(level))
		{
			sendWatch(level, name, Double.toString(value), WatchType.Float);
		}
	}

	// <summary>
	//   Overloaded. Logs a boolean Watch with the default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watchBoolean(String name, boolean value)
	{
		watchBoolean(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a boolean Watch with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	
	public void watchBoolean(Level level, String name, boolean value)
	{
		if (isOn(level))
		{
			String v = value ? "True" : "False";
			sendWatch(level, name, v, WatchType.Boolean);
		}
	}

	// <summary>
	//   Overloaded. Logs a Date Watch with the default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watchDate(String name, Date value)
	{
		watchDate(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a Date Watch with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	
	public void watchDate(Level level, String name, Date value)
	{
		if (isOn(level))
		{
			if (value == null)
			{
				logInternalError("watchDate: value argument is null");
			}
			else 
			{
				sendWatch(level, name, value.toString(), WatchType.Timestamp);
			}
		}
	}

	// <summary>
	//   Overloaded. Logs an Object Watch with the default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   The value of the resulting Watch is the return value of the
	//   toString method of the supplied Object.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watchObject(String name, Object value)
	{
		watchObject(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs an Object Watch with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">The value to display as Watch value.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   The value of the resulting Watch is the return value
	//   of the toString method of the supplied Object.
	// </remarks>
	
	public void watchObject(Level level, String name, Object value)
	{
		if (isOn(level))
		{
			if (value == null)
			{
				logInternalError("watchObject: value argument is null");
			}
			else 
			{
				sendWatch(level, name, value.toString(), WatchType.Object);
			}
		}
	}

	// <summary>
	//   Overloaded. Logs a char Watch with the default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The char value to display as Watch value.
	// </param>
	// <remarks>
	//   This method just calls the watchChar method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watch(String name, char value)
	{
		watch(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a char Watch with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The char value to display as Watch value.
	// </param>
	// <remarks>This method just calls the watchChar method.</remarks>
	
	public void watch(Level level, String name, char value)
	{
		watchChar(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs a String Watch with the default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The string value to display as Watch value.
	// </param>
	// <remarks>
	//   This method just calls the watchString method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watch(String name, String value)
	{
		watch(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a String Watch with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The string value to display as Watch value.
	// </param>
	// <remarks>This method just calls the watchString method.</remarks>
	
	public void watch(Level level, String name, String value)
	{
		watchString(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs a byte Watch with the default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The byte value to display as Watch value.
	// </param>
	// <remarks>
	//   This method just calls the watchByte method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watch(String name, byte value)
	{
		watch(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a byte Watch with the a custom level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The byte value to display as Watch value.
	// </param>
	// <remarks>This method just calls the watchByte method.</remarks>
	
	public void watch(Level level, String name, byte value)
	{
		watchByte(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs a short integer Watch with the default log
	//   level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The short integer value to display as Watch value.
	// </param>
	// <remarks>
	//   This method just calls the watchShort method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watch(String name, short value)
	{
		watch(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a short integer Watch with a custom log
	//   level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The short integer value to display as Watch value.
	// </param>
	// <remarks>This method just calls the watchShort method.</remarks>
	
	public void watch(Level level, String name, short value)
	{
		watchShort(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs an integer Watch with the default log
	//   level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The integer value to display as Watch value.
	// </param>
	// <remarks>
	//   This method just calls the watchInt method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watch(String name, int value)
	{
		watch(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs an integer Watch with a custom log
	//   level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The integer value to display as Watch value.
	// </param>
	// <remarks>This method just calls the watchInt method.</remarks>
	
	public void watch(Level level, String name, int value)
	{
		watchInt(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs a long integer Watch with the default log
	//   level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The long integer value to display as Watch value.
	// </param>
	// <remarks>
	//   This method just calls the watchLong method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watch(String name, long value)
	{
		watch(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a long integer Watch with a custom log
	//   level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The long integer value to display as Watch value.
	// </param>
	// <remarks>This method just calls the watchLong method.</remarks>
	
	public void watch(Level level, String name, long value)
	{
		watchLong(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs a float Watch with the default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The float value to display as Watch value.
	// </param>
	// <remarks>
	//   This method just calls the watchFloat method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watch(String name, float value)
	{
		watch(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a float Watch with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The float value to display as Watch value.
	// </param>
	// <remarks>This method just calls the watchFloat method.</remarks>
	
	public void watch(Level level, String name, float value)
	{
		watchFloat(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs a double Watch with the default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The double value to display as Watch value.
	// </param>
	// <remarks>
	//   This method just calls the watchDouble method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watch(String name, double value)
	{
		watch(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a double Watch with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The double value to display as Watch value.
	// </param>
	// <remarks>This method just calls the watchDouble method.</remarks>
	
	public void watch(Level level, String name, double value)
	{
		watchDouble(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs a boolean Watch with the default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The boolean value to display as Watch value.
	// </param>
	// <remarks>
	//   This method just calls the watchBoolean method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void watch(String name, boolean value)
	{
		watch(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a boolean Watch with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The boolean value to display as Watch value.
	// </param>
	// <remarks>This method just calls the watchBoolean method.</remarks>
	
	public void watch(Level level, String name, boolean value)
	{
		watchBoolean(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs a Date Watch with the default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The Date value to display as Watch value.
	// </param>
	// <remarks>
	//   This method just calls the watchDate method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void watch(String name, Date value)
	{
		watch(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs a Date Watch with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The Date value to display as Watch value.
	// </param>
	// <remarks>This method just calls the watchDate method.</remarks>

	public void watch(Level level, String name, Date value)
	{
		watchDate(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs an Object Watch with the default log level.
	// </summary>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The Object value to display as Watch value.
	// </param>
	// <remarks>
	//   This method just calls the watchObject method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void watch(String name, Object value)
	{
		watch(this.fParent.getDefaultLevel(), name, value);
	}
	
	// <summary>
	//   Overloaded. Logs an Object Watch with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the Watch.</param>
	// <param name="value">
	//   The Object value to display as Watch value.
	// </param>
	// <remarks>This method just calls the watchObject method.</remarks>

	public void watch(Level level, String name, Object value)
	{
		watchObject(level, name, value);
	}

	// <summary>
	//   Overloaded. Logs a custom Log Entry with the default log
	//   level.
	// </summary>
	// <param name="title">The title of the new Log Entry.</param>
	// <param name="lt">The Log Entry type to use.</param>
	// <param name="vi">The Viewer ID to use.</param>
	// <param name="data">Optional data block which can be null.</param>
	// <seealso cref="com.gurock.smartinspect.logentry.LogEntry"/>
	// <remarks>
	//   This method is useful for implementing custom Log Entry
	//   methods. For example, if you want to display some information
	//   in a particular way in the Console, you can just create a
	//   simple method which formats the data in question correctly and
	//   sends them using this sendCustomLogEntry method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void sendCustomLogEntry(String title, LogEntryType lt,
		ViewerId vi, byte[] data)
	{
		sendCustomLogEntry(this.fParent.getDefaultLevel(), title, lt, vi, 
			data);
	}
	
	// <summary>
	//   Overloaded. Logs a custom Log Entry with a custom log
	//   level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">The title of the new Log Entry.</param>
	// <param name="lt">The Log Entry type to use.</param>
	// <param name="vi">The Viewer ID to use.</param>
	// <param name="data">Optional data block which can be null.</param>
	// <seealso cref="com.gurock.smartinspect.logentry.LogEntry"/>
	// <remarks>
	//   This method is useful for implementing custom Log Entry
	//   methods. For example, if you want to display some information
	//   in a particular way in the Console, you can just create a
	//   simple method which formats the data in question correctly and
	//   sends them using this sendCustomLogEntry method.
	// </remarks>

	public void sendCustomLogEntry(Level level, String title, 
		LogEntryType lt, ViewerId vi, byte[] data)
	{
		if (isOn(level))
		{
			if (lt == null)
			{
				logInternalError("sendCustomLogEntry: lt argument is null");
			}
			else if (vi == null)
			{
				logInternalError("sendCustomLogEntry: vi argument is null");
			}
			else
			{
				sendLogEntry(level, title, lt, vi, getColor(), data);
			}
		}
	}

	// <summary>
	//   Overloaded. Logs a custom Control Command with the default
	//   log level.
	// </summary>
	// <param name="ct">The Control Command type to use.</param>
	// <param name="data">Optional data block which can be null.</param>
	// <seealso cref="com.gurock.smartinspect.packets.controlcommand.ControlCommand"/>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>
	
	public void sendCustomControlCommand(ControlCommandType ct, byte[] data)
	{
		sendCustomControlCommand(this.fParent.getDefaultLevel(), ct, data);
	}
	
	// <summary>
	//   Overloaded. Logs a custom Control Command with a custom
	//   log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="ct">The Control Command type to use.</param>
	// <param name="data">Optional data block which can be null.</param>
	// <seealso cref="com.gurock.smartinspect.packets.controlcommand.ControlCommand"/>

	public void sendCustomControlCommand(Level level, ControlCommandType ct,
		byte[] data)
	{
		if (isOn(level))
		{
			if (ct == null)
			{
				logInternalError("sendCustomControlCommand: ct argument is null");
			}
			else
			{
				sendControlCommand(ct, data);
			}
		}
	}

	// <summary>
	//   Overloaded. Logs a custom Watch with the default log level.
	// </summary>
	// <param name="name">The name of the new Watch.</param>
	// <param name="value">The value of the new Watch.</param>
	// <param name="wt">The Watch type to use.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method is useful for implementing custom Watch methods.
	//   For example, if you want to track the status of an instance of
	//   a specific class, you can just create a simple method which
	//   extracts all necessary information about this instance and logs
	//   them using this sendCustomWatch method.
	//
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void sendCustomWatch(String name, String value, WatchType wt)
	{
		sendCustomWatch(this.fParent.getDefaultLevel(), name, value, wt);
	}
	
	// <summary>
	//   Overloaded. Logs a custom Watch with a custom log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="name">The name of the new Watch.</param>
	// <param name="value">The value of the new Watch.</param>
	// <param name="wt">The Watch type to use.</param>
	// <seealso cref="com.gurock.smartinspect.packets.watch.Watch"/>
	// <remarks>
	//   This method is useful for implementing custom Watch methods.
	//   For example, if you want to track the status of an instance of
	//   a specific class, you can just create a simple method which
	//   extracts all necessary information about this instance and logs
	//   them using this sendCustomWatch method.
	// </remarks>

	public void sendCustomWatch(Level level, String name, String value,
		WatchType wt)
	{
		if (isOn(level))
		{
			if (wt == null)
			{
				logInternalError("sendCustomWatch: wt argument is null");
			}
			else
			{
				sendWatch(level, name, value, wt);
			}
		}
	}

	// <summary>
	//   Overloaded. Logs a custom Process Flow entry with the default
	//   log level.
	// </summary>
	// <param name="level">The log level of this method call.</param>
	// <param name="title">
	//   The title of the new Process Flow entry.
	// </param>
	// <param name="pt">The Process Flow type to use.</param>
	// <seealso cref="com.gurock.smartinspect.packets.processflow.ProcessFlow"/>
	// <remarks>
	//   This method uses the <link SmartInspect.setDefaultLevel,
	//   default level> of the session's <link getParent, parent> as log
	//   level. For more information, please refer to the documentation of
	//   the <link SmartInspect.setDefaultLevel, setDefaultLevel> method
	//   of the SmartInspect class.
	// </remarks>

	public void sendCustomProcessFlow(String title, ProcessFlowType pt)
	{
		sendCustomProcessFlow(this.fParent.getDefaultLevel(), title, pt);
	}

	// <summary>
	//   Overloaded. Logs a custom Process Flow entry with a custom
	//   log level.
	// </summary>
	// <param name="title">
	//   The title of the new Process Flow entry.
	// </param>
	// <param name="pt">The Process Flow type to use.</param>
	// <seealso cref="com.gurock.smartinspect.packets.processflow.ProcessFlow"/>
	
	public void sendCustomProcessFlow(Level level, String title,
		ProcessFlowType pt)
	{
		if (isOn(level))
		{
			if (pt == null)
			{
				logInternalError("sendCustomProcessFlow: pt argument is null");
			}
			else
			{
				sendProcessFlow(level, title, pt);
			}
		}
	}
}
