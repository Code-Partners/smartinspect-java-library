/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Specifies the viewer for displaying the title or data of a Log Entry in the Console.
 * There are many viewers available for displaying the data of a Log Entry in different ways.
 * For example, there are viewers that can display lists, tables, binary dumps of data or even websites.
 * Every viewer in the Console has a corresponding so-called viewer context in this library which can be
 * used to send custom logging information. To get started, please see the documentation of the
 * Session.logCustomContext method and ViewerContext class.
 * This class is fully threadsafe.
 */
public final class ViewerId extends Enum {
	/**
	 * Instructs the Console to use no viewer at all.
	 */
	public static final ViewerId None = new ViewerId(-1, "None");

	/**
	 * Instructs the Console to display the title of a Log Entry
	 * in a read-only text field.
	 */
	public static final ViewerId Title = new ViewerId(0, "Title");

	/**
	 * Instructs the Console to display the data of a Log Entry
	 * in a read-only text field.
	 */
	public static final ViewerId Data = new ViewerId(1, "Data");

	/**
	 * Instructs the Console to display the data of a Log Entry
	 * as a list.
	 */
	public static final ViewerId List = new ViewerId(2, "List");

	/**
	 * Instructs the Console to display the data of a Log Entry
	 * as a key/value list.
	 */
	public static final ViewerId ValueList = new ViewerId(3, "ValueList");

	/**
	 * Instructs the Console to display the data of a Log Entry
	 * using an object inspector.
	 */
	public static final ViewerId Inspector = new ViewerId(4, "Inspector");

	/**
	 * Instructs the Console to display the data of a Log Entry
	 * as a table.
	 */
	public static final ViewerId Table = new ViewerId(5, "Table");

	/**
	 * Instructs the Console to display the data of a Log Entry
	 * as a website.
	 */
	public static final ViewerId Web = new ViewerId(100, "Web");

	/**
	 * Instructs the Console to display the data of a Log Entry
	 * as a binary dump using a read-only hex editor.
	 */
	public static final ViewerId Binary = new ViewerId(200, "Binary");

	/**
	 * Instructs the Console to display the data of a Log Entry
	 * as HTML source with syntax highlighting.
	 */
	public static final ViewerId HtmlSource = new ViewerId(300, "HtmlSource");

	/**
	 * Instructs the Console to display the data of a Log Entry
	 * as JavaScript source with syntax highlighting.
	 */
	public static final ViewerId JavaScriptSource =
			new ViewerId(301, "JavaScriptSource");

	/**
	 * Instructs the Console to display the data of a Log Entry as VBScript source with syntax highlighting.
	 */
	public static final ViewerId VbScriptSource =
			new ViewerId(302, "VbScriptSource");

	/**
	 * Instructs the Console to display the data of a Log Entry
	 * as Perl source with syntax highlighting.
	 */
	public static final ViewerId PerlSource = new ViewerId(303, "PerlSource");

	/**
	 * Instructs the Console to display the data of a Log Entry
	 * as SQL source with syntax highlighting.
	 */
	public static final ViewerId SqlSource = new ViewerId(304, "SqlSource");

	/**
	 * Instructs the Console to display the data of a Log Entry
	 * as INI source with syntax highlighting.
	 */
	public static final ViewerId IniSource = new ViewerId(305, "IniSource");

	/**
	 * Instructs the Console to display the data of a Log Entry
	 * as Python source with syntax highlighting.
	 */
	public static final ViewerId PythonSource =
			new ViewerId(306, "PythonSource");

	/**
	 * Instructs the Console to display the data of a Log Entry
	 * as XML source with syntax highlighting.
	 */
	public static final ViewerId XmlSource = new ViewerId(307, "XmlSource");

	/**
	 * Instructs the Console to display the data of a Log Entry
	 * as bitmap image.
	 */
	public static final ViewerId Bitmap = new ViewerId(400, "Bitmap");

	/**
	 * Instructs the Console to display the data of a Log Entry
	 * as a JPEG image.
	 */
	public static final ViewerId Jpeg = new ViewerId(401, "Jpeg");

	/**
	 * Instructs the Console to display the data of a Log Entry
	 * as Windows icon.
	 */
	public static final ViewerId Icon = new ViewerId(402, "Icon");

	/**
	 * Instructs the Console to display the data of a Log Entry
	 * as Windows Metafile image.
	 */
	public static final ViewerId Metafile = new ViewerId(403, "Metafile");

	private ViewerId(int value, String name) {
		super(value, name);
	}
}
