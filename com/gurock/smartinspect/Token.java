//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Represents a token in the pattern string of the TextProtocol
//   protocol.
// </summary>
// <remarks>
//   This is the abstract base class for all available tokens. Derived
//   classes are not documented for clarity reasons. To create a
//   suitable token object for a given token string, you can use the
//   TokenFactory class.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>

public abstract class Token
{
	private String fValue;
	private String fOptions;
	private int fWidth;
	
	// <summary>
	//   Creates a string representation of a variable or literal token.
	// </summary>
	// <param name="logEntry">
	//   The LogEntry to use to create the string representation.
	// </param>
	// <returns>
	//   The text representation of this token for the supplied LogEntry
	//   object.
	// </returns>
	// <remarks>
	//   With the help of the supplied LogEntry, this token is expanded
	//   into a string. For example, if this token represents the
	//   %session% variable of a pattern string, this expand method
	//   simply returns the session name of the supplied LogEntry.
	//
	//   For a literal token, the supplied LogEntry argument is ignored
	//   and the <link setValue, value property> is returned.
	// </remarks>
	
	public abstract String expand(LogEntry logEntry);
	
	// <summary>
	//   Returns the raw string value of the parsed pattern string for
	//   this token.
	// </summary>
	// <returns>
	//   The raw string value of the parsed pattern string for this token.
	// </returns>
	// <remarks>
	//   This method returns the raw string of this token as found in
	//   the parsed pattern string. For a variable, this property is
	//   set to the variable name surrounded with '%' characters and an
	//   optional options string like this: %name{options}%. For a
	//   literal, this property can have any value.
	// </remarks>
	
	public String getValue()
	{
		return this.fValue;
	}
	
	// <summary>
	//   Sets the raw string value of the parsed pattern string for this
	//   token.
	// </summary>
	// <param name="value">The new value of this token.</param>
	// <remarks>
	//   This method sets the raw string of this token as found in the
	//   parsed pattern string. For a variable, this property is set
	//   to the variable name surrounded with '%' characters and an
	//   optional options string like this: %name{options}%. For a
	//   literal, this property can have any value.
	// </remarks>
	
	public void setValue(String value)
	{
		this.fValue = value;
	}
	
	// <summary>
	//   Returns the optional options string for this token.
	// </summary>
	// <returns>The optional options string for this token.</returns>
	// <remarks>
	//   A variable token can have an optional options string. In the
	//   raw string representation of a token, an options string can be
	//   specified in curly brackets after the variable name like this:
	//   %name{options}%. For a literal, this property is always set to
	//   an empty string. 
	// </remarks>
	
	public String getOptions()
	{
		return this.fOptions;
	}
	
	// <summary>
	//   Sets the optional options string for this token.
	// </summary>
	// <param name="value">The new options for this token.</param>
	// <remarks>
	//   A variable token can have an optional options string. In the
	//   raw string representation of a token, an options string can be
	//   specified in curly braces after the variable name like this:
	//   %name{options}%. For a literal, this property is always set to
	//   an empty string. 
	// </remarks>
	
	public void setOptions(String options)
	{
		this.fOptions = options;
	}

	// <summary>
	//   Indicates if this token supports indenting.
	// </summary>
	// <returns>
	//   True if this token supports indenting and false otherwise.
	// </returns>
	// <remarks>
	//   This method always returns false unless this token represents
	//   the title token of a pattern string. This method is used in the
	//   PatternParser.expand method to determine if a token allows
	//   indenting.
	// </remarks>
	
	public boolean getIndent()
	{
		return false;
	}
	
	// <summary>
	//   Sets the minimum width of this token.
	// </summary>
	// <param name="width">The new minimum width of this token.</param>
	// <remarks>
	//   A variable token can have an optional width modifier. In the
	//   raw string representation of a token, a width modifier can be
	//   specified after the variable name like this: %name,width%.
	//   Width must be a valid positive or negative integer.
	//   
	//   If the width is greater than 0, formatted values will be
	//   right-aligned. If the width is less than 0, they will be
	//   left-aligned.
	//   
	//   For a literal, this property is always set to 0. 
	// </remarks>
	
	public void setWidth(int width)
	{
		this.fWidth = width;
	}
	
	// <summary>
	//   Returns the minimum width of this token.
	// </summary>
	// <returns>The minimum width of this token.</returns>
	// <remarks>
	//   A variable token can have an optional width modifier. In the
	//   raw string representation of a token, a width modifier can be
	//   specified after the variable name like this: %name,width%.
	//   Width must be a valid positive or negative integer.
	//   
	//   If the width is greater than 0, formatted values will be
	//   right-aligned. If the width is less than 0, they will be
	//   left-aligned.
	//   
	//   For a literal, this property is always set to 0. 
	// </remarks>
	
	public int getWidth()
	{
		return this.fWidth;
	}
}
