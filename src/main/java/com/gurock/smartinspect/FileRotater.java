/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Responsible for the log file rotate management as used by the
 * FileProtocol class.
 *
 * <p> This class implements a flexible log file rotate management system.
 * For a detailed description of how to use this class, please refer
 * to the documentation of the initialize, update and setMode methods.
 *
 * <p> Note: This class is not guaranteed to be threadsafe.
 */
public class FileRotater {
	private static final int EPOCH = 1970;
	private static final double DAYS_PER_YEAR = 365.2425;

	private Calendar fCalendar;
	private FileRotate fMode;
	private int fTimeValue;

	/**
	 * Creates a new FileRotater instance with a default mode of
	 * FileRotate.None. Please refer to the update and initialize
	 * methods for additional information about this class.
	 */
	public FileRotater() {
		this.fCalendar = GregorianCalendar.getInstance();
		this.fCalendar.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		this.fMode = FileRotate.None;
	}

	private static int getDays(Calendar calendar) {
		int years = calendar.get(Calendar.YEAR) - EPOCH;
		return (int) (years * DAYS_PER_YEAR) + calendar.get(Calendar.DAY_OF_YEAR);
	}

	private int getTimeValue(Date now) {
		int timeValue = 0;

		if (this.fMode != FileRotate.None) {
			this.fCalendar.setTime(now);

			if (this.fMode == FileRotate.Hourly) {
				timeValue = getDays(this.fCalendar) * 24 + this.fCalendar.get(Calendar.HOUR_OF_DAY);
			} else if (this.fMode == FileRotate.Daily) {
				timeValue = getDays(this.fCalendar);
			} else if (this.fMode == FileRotate.Weekly) {
				setToMonday(this.fCalendar);
				timeValue = getDays(this.fCalendar);
			} else if (this.fMode == FileRotate.Monthly) {
				timeValue = this.fCalendar.get(Calendar.YEAR) * 12 + this.fCalendar.get(Calendar.MONTH);
			}
		}

		return timeValue;
	}

	/**
	 * Initializes this FileRotater object with a user-supplied
	 * timestamp.
	 * <p>
	 * Always call this method after creating a new FileRotater object
	 * and before calling the update method the first time. For additional
	 * information please refer to the update method.
	 *
	 * @param now The user-specified timestamp to use to initialize this object.
	 */
	public void initialize(Date now) {
		this.fTimeValue = getTimeValue(now);
	}

	/**
	 * This method updates the internal date of this FileRotater object and returns
	 * whether the rotate state has changed since the last call to this method or to
	 * initialize.
	 * Before calling this method, always call the initialize method.
	 *
	 * @param now The timestamp to update this object
	 * @return True if the rotate state has changed since the last call to this method
	 * or to initialize and false otherwise
	 * @throws Exception exception
	 */
	public boolean update(Date now) throws Exception {
		int timeValue = getTimeValue(now);

		if (timeValue != this.fTimeValue) {
			this.fTimeValue = timeValue;
			return true;
		} else {
			return false;
		}
	}

	private static void setToMonday(Calendar calendar) {
		int day = calendar.get(Calendar.DAY_OF_WEEK);

		if (day != Calendar.MONDAY) {
			int days = 0;

			switch (day) {
				case Calendar.TUESDAY:
					days = -1;
					break;
				case Calendar.WEDNESDAY:
					days = -2;
					break;
				case Calendar.THURSDAY:
					days = -3;
					break;
				case Calendar.FRIDAY:
					days = -4;
					break;
				case Calendar.SATURDAY:
					days = -5;
					break;
				case Calendar.SUNDAY:
					days = -6;
					break;
			}

			calendar.add(Calendar.DATE, days);
		}
	}

	/**
	 * Returns the FileRotate mode of this FileRotater object.
	 * <p>
	 * This method returns the current FileRotate mode. For a complete
	 * list of available return values, please refer to the FileRotate
	 * enum.
	 *
	 * @return The FileRotate mode of this FileRotater object.
	 */
	public FileRotate getMode() {
		return this.fMode;
	}

	/**
	 * Sets the FileRotate mode of this FileRotater object.
	 * <p>
	 * Always call the initialize method after changing the log rotate
	 * mode to reinitialize this FileRotater object. For a complete
	 * list of available file log rotate values, please refer to the
	 * FileRotate enum.
	 *
	 * @param mode The new FileRotate mode.
	 * @throws NullPointerException if the mode argument is null.
	 */
	public void setMode(FileRotate mode) {
		if (mode == null) {
			throw new NullPointerException("mode");
		} else {
			this.fMode = mode;
		}
	}
}
