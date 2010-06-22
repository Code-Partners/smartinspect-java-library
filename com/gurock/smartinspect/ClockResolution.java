//
//<!-- Copyright (C) 2003-2010 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Represents the timestamp resolution mode for the Clock class.
// </summary>
// <remarks>
//   SmartInspect currently supports two different kinds of timestamp
//   resolutions. The standard resolution is the default timestamp
//   behavior of the SmartInspect Java library and usually provides
//   a maximum resolution of 10-55 milliseconds (depending on the
//   Windows version). This is the recommended option for production
//   systems. High-resolution timestamps, on the other hand, can provide
//   a microseconds resolution but are only intended to be used on
//   development machines.
//
//   Please see SmartInspect.setResolution for details.
// </remarks>

public class ClockResolution extends Enum
{
	// <summary>
	//   Represents the standard timestamp resolution. This is the
	//   default timestamp behavior of the SmartInspect Java library
	//   and the recommended option for production systems.
	// </summary>
	
	public static final ClockResolution Standard = 
		new ClockResolution(0, "Standard");
	
	// <summary>
	//   Represents timestamps with a very high resolution (microseconds).
	//   This option is not intended to be used on production systems. See
	//   SmartInspect.setResolution for details.
	// </summary>
	
	public static final ClockResolution High = 
		new ClockResolution(1, "High");

	private ClockResolution(int value, String name)
	{
		super(value, name);
	}
}
