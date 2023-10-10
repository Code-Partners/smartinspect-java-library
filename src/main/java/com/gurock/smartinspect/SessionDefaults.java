/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import java.awt.Color;

/**
 * Specifies the default property values for newly created sessions.
 * <p>
 * This class is used by the SmartInspect class to customize the
 * default property values for newly created sessions. Sessions
 * that will be created by or passed to the addSession method of
 * the SmartInspect class will be automatically configured with
 * the values of the session defaults object as returned by the
 * SmartInspect.getSessionDefaults method.
 * <p>
 * This class is guaranteed to be threadsafe.
 */
public class SessionDefaults {
	private boolean fActive;
	private Color fColor;
	private Level fLevel;

	/**
	 * Creates and initializes a new SessionDefaults instance.
	 */
	public SessionDefaults() {
		this.fActive = true;
		this.fColor = Session.DEFAULT_COLOR;
		this.fLevel = Level.Debug;
	}

	/**
	 * Returns the default active status for newly created sessions.
	 * <p>
	 * Please see Session.setActive method for general information
	 * about the active status of sessions.
	 *
	 * @return The default active status for newly created sessions
	 */
	public boolean isActive() {
		return this.fActive;
	}

	/**
	 * Sets the default active status for newly created sessions.
	 * <p>
	 * Please see Session.setActive method for general information
	 * about the active status of sessions.
	 *
	 * @param active The new default active status for newly created sessions
	 */
	public void setActive(boolean active) {
		this.fActive = active;
	}

	/**
	 * Returns the default color for newly created sessions.
	 * Please see Session.setColor method for general information about the background color of sessions.
	 *
	 * @return The default color for newly created sessions
	 */
	public Color getColor() {
		return this.fColor;
	}

	/**
	 * Sets the default color for newly created sessions.
	 * Please see Session.setColor method for general information about the background color of sessions.
	 *
	 * @param color The new default color for newly created sessions
	 */
	public void setColor(Color color) {
		if (color != null) {
			this.fColor = color;
		}
	}

	/**
	 * Returns the default log level for newly created sessions.
	 * <p>
	 * Please see Session.setLevel method for general information about the log level of sessions.
	 *
	 * @return The default log level for newly created sessions
	 */
	public Level getLevel() {
		return this.fLevel;
	}

	/**
	 * Sets the default log level for newly created sessions.
	 * Please see Session.setLevel method for general information about the log level of sessions.
	 *
	 * @param level The new default log level for newly created sessions
	 */
	public void setLevel(Level level) {
		if (level != null) {
			this.fLevel = level;
		}
	}

	protected void assign(Session session) {
		session.setActive(this.fActive);
		session.setColor(this.fColor);
		session.setLevel(this.fLevel);
	}
}
