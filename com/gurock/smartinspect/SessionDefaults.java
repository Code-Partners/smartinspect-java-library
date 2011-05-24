//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

import java.awt.Color;

// <summary>
//   Specifies the default property values for newly created sessions.
// </summary>
// <remarks>
//   This class is used by the SmartInspect class to customize the
//   default property values for newly created sessions. Sessions
//   that will be created by or passed to the addSession method of
//   the SmartInspect class will be automatically configured with
//   the values of the session defaults object as returned by the
//   SmartInspect.getSessionDefaults method.
// </remarks>
// <threadsafety>
//   This class is guaranteed to be threadsafe.
// </threadsafety>

public class SessionDefaults
{
	private boolean fActive;
	private Color fColor;
	private Level fLevel;
	
	// <summary>
	//   Creates and initializes a new SessionDefaults instance.
	// </summary>
	
	public SessionDefaults()
	{
		this.fActive = true;
		this.fColor = Session.DEFAULT_COLOR;
		this.fLevel = Level.Debug;
	}
	
	// <summary>
	//   Returns the default active status for newly created sessions.
	// </summary>
	// <returns>
	//  The default active status for newly created sessions.
	// </returns>
	// <remarks>
	//   Please see Session.setActive method for general information
	//   about the active status of sessions.
	// </remarks>
	
	public boolean isActive()
	{
		return this.fActive;
	}
	
	// <summary>
	//   Sets the default active status for newly created sessions.
	// </summary>
	// <param name="active">
	//   The new default active status for newly created sessions.
	// </active>
	// <remarks>
	//   Please see Session.setActive method for general information
	//   about the active status of sessions.
	// </remarks>

	public void setActive(boolean active)
	{
		this.fActive = active;
	}
	
	// <summary>
	//   Returns the default color for newly created sessions.
	// </summary>
	// <returns>
	//  The default color for newly created sessions.
	// </returns>
	// <remarks>
	//   Please see Session.setColor method for general information
	//   about the background color of sessions.
	// </remarks>
	
	public Color getColor()
	{
		return this.fColor;
	}
	
	// <summary>
	//   Sets the default color for newly created sessions.
	// </summary>
	// <param name="color">
	//   The new default color for newly created sessions.
	// </active>
	// <remarks>
	//   Please see Session.setColor method for general information
	//   about the background color of sessions.
	// </remarks>
	
	public void setColor(Color color)
	{
		if (color != null)
		{
			this.fColor = color;
		}		
	}
	
	// <summary>
	//   Returns the default log level for newly created sessions.
	// </summary>
	// <returns>
	//  The default log level for newly created sessions.
	// </returns>
	// <remarks>
	//   Please see Session.setLevel method for general information
	//   about the log level of sessions.
	// </remarks>
	
	public Level getLevel()
	{
		return this.fLevel;
	}
	
	// <summary>
	//   Sets the default log level for newly created sessions.
	// </summary>
	// <param name="level">
	//   The new default log level for newly created sessions.
	// </active>
	// <remarks>
	//   Please see Session.setLevel method for general information
	//   about the log level of sessions.
	// </remarks>
	
	public void setLevel(Level level)
	{
		if (level != null)		
		{
			this.fLevel = level;
		}
	}
	
	protected void assign(Session session)
	{
		session.setActive(this.fActive);
		session.setColor(this.fColor);
		session.setLevel(this.fLevel);
	}
}
