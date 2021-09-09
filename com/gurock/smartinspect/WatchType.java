//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Represents the type of a Watch packet. The type of a Watch
//   specifies its variable type. 
// </summary>
// <remarks>
//   For example, if a Watch packet has a type of WatchType.String,
//   the represented variable is treated as string in the Console.
// </remarks>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

public final class WatchType extends Enum
{
	// <summary>
	//   Instructs the Console to treat a Watch value as char.
	// </summary>

	public static final WatchType Char = new WatchType(0, "Char");
	
	// <summary>
	//   Instructs the Console to treat a Watch value as string.
	// </summary>

	public static final WatchType String = new WatchType(1, "String");

	// <summary>
	//   Instructs the Console to treat a Watch value as integer.
	// </summary>

	public static final WatchType Integer = new WatchType(2, "Integer");

	// <summary>
	//   Instructs the Console to treat a Watch value as float.
	// </summary>

	public static final WatchType Float = new WatchType(3, "Float");

	// <summary>
	//   Instructs the Console to treat a Watch value as boolean.
	// </summary>

	public static final WatchType Boolean = new WatchType(4, "Boolean");

	// <summary>
	//   Instructs the Console to treat a Watch value as address.
	// </summary>

	public static final WatchType Address = new WatchType(5, "Address");

	// <summary>
	//   Instructs the Console to treat a Watch value as timestamp.
	// </summary>	

	public static final WatchType Timestamp = new WatchType(6, "Timestamp");

	// <summary>
	//   Instructs the Console to treat a Watch value as object.
	// </summary>	

	public static final WatchType Object = new WatchType(7, "Object");

	private WatchType(int value, String name)
	{
		super(value, name);
	}
}
