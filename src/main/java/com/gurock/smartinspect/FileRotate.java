/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Specifies the log rotate mode for the FileProtocol class and
 * derived classes.
 */
public class FileRotate extends Enum {
	/**
	 * Completely disables the log rotate functionality.
	 */
	public static final FileRotate None = new FileRotate(0, "None");

	/**
	 * Instructs the file protocol to rotate log files hourly.
	 */
	public static final FileRotate Hourly = new FileRotate(1, "Hourly");

	/**
	 * Instructs the file protocol to rotate log files daily.
	 */
	public static final FileRotate Daily = new FileRotate(2, "Daily");

	/**
	 * Instructs the file protocol to rotate log files weekly.
	 */
	public static final FileRotate Weekly = new FileRotate(3, "Weekly");

	/**
	 * Instructs the file protocol to rotate log files monthly.
	 */
	public static final FileRotate Monthly = new FileRotate(4, "Monthly");

	private FileRotate(int value, String name) {
		super(value, name);
	}
}
