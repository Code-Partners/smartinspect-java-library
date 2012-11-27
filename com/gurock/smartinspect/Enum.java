//
// <!-- Copyright (C) Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Is the abstract representation of the Enum pattern. Is used as base
//   type for all classes in the SmartInspect Java library which represent
//   an enumeration type.
// </summary>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

abstract class Enum
{
	private int fValue;
	private String fName;
	private String fToString;

	// <summary>
	//   Overloaded. Creates and initializes an Enum instance without a 
	//   name.
	// </summary>
	// <param name="value">
	//   The integer value of the resulting Enum.
	// </param>

	protected Enum(int value)
	{
		this.fValue = value;
	}

	// <summary>
	//   Overloaded. Creates and initializes an Enum instance with a name.
	// </summary>
	// <param name="value">
	//   The integer value of the resulting Enum.
	// </param>
	// <param name="name">
	//   Specifies the name of this instance. Describes this Enum object.
	// </param>

	protected Enum(int value, String name)
	{
		this.fValue = value;
		this.fName = name;
	}

	// <summary>
	//   Overridden. Creates and returns a string representation of this
	//   object.
	// </summary>
	// <returns>
	//   A string representation of this object. The return value is either
	//   the name of this Enum object if set or the integer value converted
	//   to a string.
	// </returns>

	public synchronized String toString()
	{
		if (this.fToString == null)
		{
			if (this.fName == null)
			{
				this.fToString = Integer.toString(this.fValue);
			}
			else 
			{
				this.fToString = this.fName;
			}
		}

		return this.fToString;
	}

	// <summary>
	//   Returns the integer value of this enum.
	// </summary>
	// <returns>
	//   The integer value of this enum. For a string representation of
	//   this Enum object, refer to the documentation of the toString
	//   method.
	// </returns>

	public int getIntValue()
	{
		return this.fValue;
	}
	
	// <summary>
	//   Indicates whether some other object is equal to this one.
	// </summary>
	// <param name="o">The object to compare this enum with.</param>
	// <returns>
	//   True if the supplied object equals this enum and false
	//   otherwise.
	// </returns>
	// <remarks>
	//   This method returns true if the supplied Object is an Enum
	//   and has the same integer value as returned by the getIntValue()
	//   method of this object. If the supplied Object is not an Enum,
	//   this method calls the equals method of the parent class.
	// </remarks>
	
	public boolean equals(Object o)
	{
		if (o instanceof Enum)
		{
			return ((Enum) o).getIntValue() == this.fValue;
		}
		else 
		{
			return super.equals(o);
		}
	}
	
	// <summary>
	//   Indicates whether the integer value of this Enum is less than
	//   the integer value of some other Enum.
	// </summary>
	// <param name="en">The enum to compare this enum with.</param>
	// <returns>
	//   True if the integer value of this Enum is less than the integer
	//   value of the supplied Enum and false otherwise.
	// </returns>
		
	public boolean less(Enum en)
	{
		return this.fValue < en.getIntValue();
	}

	// <summary>
	//   Indicates whether the integer value of this Enum is less than
	//   or equal to the integer value of some other Enum.
	// </summary>
	// <param name="en">The enum to compare this enum with.</param>
	// <returns>
	//   True if the integer value of this Enum is less than or equal to
	//   the integer value of the supplied Enum and false otherwise.
	// </returns>

	public boolean lessEqual(Enum en)
	{
		return this.fValue <= en.getIntValue();
	}
	
	// <summary>
	//   Indicates whether the integer value of this Enum is greater
	//   than the integer value of some other Enum.
	// </summary>
	// <param name="en">The enum to compare this enum with.</param>
	// <returns>
	//   True if the integer value of this Enum is greater than the
	//   integer value of the supplied Enum and false otherwise.
	// </returns>

	public boolean greater(Enum en)
	{
		return this.fValue > en.getIntValue();
	}
	
	// <summary>
	//   Indicates whether the integer value of this Enum is greater
	//   than or equal to the integer value of some other Enum.
	// </summary>
	// <param name="en">The enum to compare this enum with.</param>
	// <returns>
	//   True if the integer value of this Enum is greater than or equal
	//   to the integer value of the supplied Enum and false otherwise.
	// </returns>

	public boolean greaterEqual(Enum en)
	{
		return this.fValue >= en.getIntValue();
	}
}
