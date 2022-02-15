//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Used in the logSource methods of the Session class to specify the
//   type of source code.
// </summary>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

public final class SourceId extends Enum
{
	private ViewerId fVi;
	private String fToString;

	// <summary>
	//   Instructs the logSource methods of the Session class to choose
	//   syntax highlighting for HTML.
	// </summary>

	public final static SourceId Html = 
		new SourceId(ViewerId.HtmlSource);

	// <summary>
	//   Instructs the logSource methods of the Session class to choose
	//   syntax highlighting for Java Script.
	// </summary>

	public final static SourceId JavaScript = 
		new SourceId(ViewerId.JavaScriptSource);

	// <summary>
	//   Instructs the logSource methods of the Session class to choose
	//   syntax highlighting for VBScript.
	// </summary>

	public final static SourceId VbScript = 
		new SourceId(ViewerId.VbScriptSource);

	// <summary>
	//   Instructs the logSource methods of the Session class to choose
	//   syntax highlighting for Perl.
	// </summary>

	public final static SourceId Perl = 
		new SourceId(ViewerId.PerlSource);

	// <summary>
	//   Instructs the logSource methods of the Session class to choose
	//   syntax highlighting for SQL.
	// </summary>

	public final static SourceId Sql = 
		new SourceId(ViewerId.SqlSource);

	// <summary>
	//   Instructs the logSource methods of the Session class to choose
	//   syntax highlighting for INI files.
	// </summary>

	public final static SourceId Ini = 
		new SourceId(ViewerId.IniSource);

	// <summary>
	//   Instructs the logSource methods of the Session class to choose
	//   syntax highlighting for Python.
	// </summary>

	public final static SourceId Python = 
		new SourceId(ViewerId.PythonSource);

	// <summary>
	//   Instructs the logSource methods of the Session class to choose
	//   syntax highlighting for XML.
	// </summary>

	public final static SourceId Xml = 
		new SourceId(ViewerId.XmlSource);

	private SourceId(ViewerId vi)
	{
		super(vi.getIntValue());
		this.fVi = vi;
	}

	// <summary>
	//   Overridden. Creates and returns a string representation of this
	//   object.
	// </summary>
	// <returns>
	//   A string representation of this object.
	// </returns>

	public synchronized String toString()
	{
		if (this.fToString == null)
		{
			this.fToString = this.fVi.toString();
			int index = this.fToString.lastIndexOf("Source");

			if (index != -1)
			{
				this.fToString = this.fToString.substring(0, index);
			}
		}

		return this.fToString;
	}

	// <summary>
	//   Returns the related viewer ID for this object.
	// </summary>
	// <returns>
	//   The related viewer ID.
	// </returns>

	public ViewerId toViewerId()
	{
		return this.fVi;
	}
}
