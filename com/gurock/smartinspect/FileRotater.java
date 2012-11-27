//
// <!-- Copyright (C) Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

// <summary>
//   Responsible for the log file rotate management as used by the
//   FileProtocol class.
// </summary>
// <remarks>
//   This class implements a flexible log file rotate management system.
//   For a detailed description of how to use this class, please refer
//   to the documentation of the initialize, update and setMode methods.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>

public class FileRotater 
{
	private static final int EPOCH = 1970;
	private static final double DAYS_PER_YEAR = 365.2425;

	private Calendar fCalendar;
	private FileRotate fMode;
	private int fTimeValue;

	// <summary>
	//   Creates a new FileRotater instance with a default mode of
	//   FileRotate.None. Please refer to the update and initialize
	//   methods for additional information about this class.
	// </summary>
	
	public FileRotater()
	{
		this.fCalendar = GregorianCalendar.getInstance();
		this.fCalendar.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		this.fMode = FileRotate.None;
	}
	
	private static int getDays(Calendar calendar)
	{
		int years = calendar.get(Calendar.YEAR) - EPOCH;
		return (int) (years * DAYS_PER_YEAR) + 
			calendar.get(Calendar.DAY_OF_YEAR);
	}
	
	private int getTimeValue(Date now)
	{
		int timeValue = 0;

		if (this.fMode != FileRotate.None)
		{
			this.fCalendar.setTime(now);
			
			if (this.fMode == FileRotate.Hourly)
			{
				timeValue = getDays(this.fCalendar) * 24 + 
					this.fCalendar.get(Calendar.HOUR_OF_DAY);
			}
			else if (this.fMode == FileRotate.Daily)
			{
				timeValue = getDays(this.fCalendar); 
			}
			else if (this.fMode == FileRotate.Weekly)
			{
				setToMonday(this.fCalendar);
				timeValue = getDays(this.fCalendar); 
			}
			else if (this.fMode == FileRotate.Monthly)
			{
				timeValue = this.fCalendar.get(Calendar.YEAR) * 12 +
					this.fCalendar.get(Calendar.MONTH);
			}
		}
		
		return timeValue;
	}
	
	// <summary>
	//   Initializes this FileRotater object with a user-supplied
	//   timestamp.
	// </summary>
	// <param name="now">
	//   The user-specified timestamp to use to initialize this object.
	// </param>
	// <remarks>
	//   Always call this method after creating a new FileRotater object
	//   and before calling the update method the first time. For additional
	//   information please refer to the update method.
	// </remarks>
	
	public void initialize(Date now)
	{
		this.fTimeValue = getTimeValue(now);
	}
	
	// <summary>
	//   Updates the date of this FileRotater object and returns
	//   whether the rotate state has changed since the last call to
	//   this method or to initialize.
	// </summary>
	// <returns>
	//   True if the rotate state has changed since the last call to
	//   this method or to initialize and false otherwise.
	// </returns>
	// <param name="now">The timestamp to update this object.</param>
	// <remarks>
	//   This method updates the internal date of this FileRotater
	//   object and returns whether the rotate state has changed since
	//   the last call to this method or to initialize. Before calling
	//   this method, always call the initialize method.
	// </remarks>
	
	public boolean update(Date now) throws Exception
	{
		int timeValue = getTimeValue(now);
		
		if (timeValue != this.fTimeValue)
		{
			this.fTimeValue = timeValue;
			return true;
		}
		else 
		{
			return false;
		}
	}
	
	private static void setToMonday(Calendar calendar)
	{
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		
		if (day != Calendar.MONDAY)
		{
			int days = 0;

			switch (day)
			{
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

	// <summary>
	//   Returns the FileRotate mode of this FileRotater object.
	// </summary>
	// <returns>The FileRotate mode of this FileRotater object.</returns>
	// <remarks>
	//   This method returns the current FileRotate mode. For a complete
	//   list of available return values, please refer to the FileRotate
	//   enum.
	// </remarks>
	
	public FileRotate getMode()
	{
		return this.fMode;
	}
	
	// <summary>
	//   Sets the FileRotate mode of this FileRotater object.
	// </summary>
	// <param name="mode">The new FileRotate mode.</returns>
	// <remarks>
	//   Always call the initialize method after changing the log rotate
	//   mode to reinitialize this FileRotater object. For a complete
	//   list of available file log rotate values, please refer to the
	//   FileRotate enum.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type           Condition
	//   -                        -
	//   NullPointerException     The mode argument is null.
	// </table>
	// </exception>
	
	public void setMode(FileRotate mode)
	{
		if (mode == null)
		{
			throw new NullPointerException("mode");
		}
		else 
		{
			this.fMode = mode;
		}
	}
}
