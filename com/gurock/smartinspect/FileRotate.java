//
// <!-- Copyright (C) 2003-2010 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Specifies the log rotate mode for the FileProtocol class and
//   derived classes.
// </summary>

public class FileRotate extends Enum
{
	// <summary>
	//   Completely disables the log rotate functionality.
	// </summary>
	
	public static final FileRotate None = new FileRotate(0, "None");

	// <summary>
	//   Instructs the file protocol to rotate log files hourly.
	// </summary>
	
	public static final FileRotate Hourly = new FileRotate(1, "Hourly");

	// <summary>
	//   Instructs the file protocol to rotate log files daily.
	// </summary>
	
	public static final FileRotate Daily = new FileRotate(2, "Daily");
	
	// <summary>
	//   Instructs the file protocol to rotate log files weekly.
	// </summary>
	
	public static final FileRotate Weekly = new FileRotate(3, "Weekly");
	
	// <summary>
	//   Instructs the file protocol to rotate log files monthly.
	// </summary>
	
	public static final FileRotate Monthly = new FileRotate(4, "Monthly");
	
	private FileRotate(int value, String name)
	{
		super(value, name);
	}
}
