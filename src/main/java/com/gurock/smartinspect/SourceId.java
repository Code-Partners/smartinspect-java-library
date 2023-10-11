/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Used in the logSource methods of the Session class to specify the type of source code.
 * <p>
 * This class is fully thread-safe.
 */
public final class SourceId extends Enum {
	private ViewerId fVi;
	private String fToString;

	/**
	 * Instructs the logSource methods of the Session class to choose syntax highlighting for HTML.
	 */
	public final static SourceId Html =
			new SourceId(ViewerId.HtmlSource);

	/**
	 * Instructs the logSource methods of the Session class to choose
	 * syntax highlighting for JavaScript.
	 */
	public final static SourceId JavaScript =
			new SourceId(ViewerId.JavaScriptSource);

	/**
	 * Instructs the logSource methods of the Session class to choose
	 * syntax highlighting for VBScript.
	 */
	public final static SourceId VbScript =
			new SourceId(ViewerId.VbScriptSource);

	/**
	 * Instructs the logSource methods of the Session class to choose
	 * syntax highlighting for Perl.
	 */
	public final static SourceId Perl =
			new SourceId(ViewerId.PerlSource);

	/**
	 * Instructs the logSource methods of the Session class to choose
	 * syntax highlighting for SQL.
	 */
	public final static SourceId Sql =
			new SourceId(ViewerId.SqlSource);

	/**
	 * Instructs the logSource methods of the Session class to choose
	 * syntax highlighting for INI files.
	 */
	public final static SourceId Ini =
			new SourceId(ViewerId.IniSource);

	/**
	 * Instructs the logSource methods of the Session class to choose
	 * syntax highlighting for Python.
	 */
	public final static SourceId Python =
			new SourceId(ViewerId.PythonSource);

	/**
	 * Instructs the logSource methods of the Session class to choose
	 * syntax highlighting for XML.
	 */
	public final static SourceId Xml =
			new SourceId(ViewerId.XmlSource);

	private SourceId(ViewerId vi) {
		super(vi.getIntValue());
		this.fVi = vi;
	}

	/**
	 * Overridden. Creates and returns a string representation of this object.
	 *
	 * @return A string representation of this object
	 */
	public synchronized String toString() {
		if (this.fToString == null) {
			this.fToString = this.fVi.toString();
			int index = this.fToString.lastIndexOf("Source");

			if (index != -1) {
				this.fToString = this.fToString.substring(0, index);
			}
		}

		return this.fToString;
	}

	/**
	 * Returns the related viewer ID for this object.
	 *
	 * @return The related viewer ID
	 */
	protected ViewerId toViewerId() {
		return this.fVi;
	}
}
