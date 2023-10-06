/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Represents the abstract representation of the Enum pattern. It is used as a base
 * type for all classes in the SmartInspect Java library which represent
 * an enumeration type.
 * This class is fully thread-safe.
 */
public abstract class Enum {
	private int fValue;
	private String fName;
	private String fToString;

	/**
	 * Overloaded. Creates and initializes an Enum instance without a name.
	 *
	 * @param value The integer value of the resulting Enum.
	 */

	protected Enum(int value) {
		this.fValue = value;
	}

	/**
	 * Overloaded. Creates and initializes an Enum instance with a name.
	 *
	 * @param value The integer value of the resulting Enum
	 * @param name  Specifies the name of this instance. Describes this Enum object
	 */
	protected Enum(int value, String name) {
		this.fValue = value;
		this.fName = name;
	}

	/**
	 * Overridden. Creates and returns a string representation of this object.
	 *
	 * @return A string representation of this object. The return value is either
	 * the name of this Enum object if set or the integer value converted to a string.
	 */
	public synchronized String toString() {
		if (this.fToString == null) {
			if (this.fName == null) {
				this.fToString = Integer.toString(this.fValue);
			} else {
				this.fToString = this.fName;
			}
		}

		return this.fToString;
	}

	/**
	 * Returns the integer value of this enum.
	 *
	 * @return The integer value of this enum. For a string representation of this Enum object, refer to the documentation of the toString method.
	 */
	public int getIntValue() {
		return this.fValue;
	}

	/**
	 * Indicates whether some other object is equal to this one.
	 * <p>
	 * This method returns true if the supplied Object is an Enum
	 * and has the same integer value as returned by the getIntValue()
	 * method of this object. If the supplied Object is not an Enum,
	 * this method calls the equals method of the parent class.
	 *
	 * @param o The object to compare this enum with.
	 * @return True if the supplied object equals this enum and false otherwise.
	 */
	public boolean equals(Object o) {
		if (o instanceof Enum) {
			return ((Enum) o).getIntValue() == this.fValue;
		} else {
			return super.equals(o);
		}
	}

	/**
	 * Indicates whether the integer value of this Enum is less than
	 * the integer value of some other Enum.
	 *
	 * @param en The enum to compare this enum with.
	 * @return True if the integer value of this Enum is less than the integer
	 * value of the supplied Enum and false otherwise.
	 */
	public boolean less(Enum en) {
		return this.fValue < en.getIntValue();
	}

	/**
	 * Indicates whether the integer value of this Enum is less than
	 * or equal to the integer value of some other Enum.
	 *
	 * @param en The enum to compare this enum with.
	 * @return True if the integer value of this Enum is less than or equal to
	 * the integer value of the supplied Enum and false otherwise.
	 */
	public boolean lessEqual(Enum en) {
		return this.fValue <= en.getIntValue();
	}

	/**
	 * Indicates whether the integer value of this Enum is greater
	 * than the integer value of some other Enum.
	 *
	 * @param en The enum to compare this enum with.
	 * @return True if the integer value of this Enum is greater than the
	 * integer value of the supplied Enum and false otherwise.
	 */
	public boolean greater(Enum en) {
		return this.fValue > en.getIntValue();
	}

	/**
	 * Indicates whether the integer value of this Enum is greater
	 * than or equal to the integer value of some other Enum.
	 *
	 * @param en The enum to compare this enum with.
	 * @return True if the integer value of this Enum is greater than or
	 * equal to the integer value of the supplied Enum and false otherwise.
	 */
	public boolean greaterEqual(Enum en) {
		return this.fValue >= en.getIntValue();
	}
}
