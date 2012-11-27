//
// <!-- Copyright (C) Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Specifies the viewer for displaying the title or data of a Log
//   Entry in the Console.
// </summary>
// <remarks>
//   There are many viewers available for displaying the data of a
//   Log Entry in different ways. For example, there are viewers that
//   can display lists, tables, binary dumps of data or even websites.
//   
//   Every viewer in the Console has a corresponding so called viewer
//   context in this library which can be used to send custom logging
//   information. To get started, please see the documentation of the
//   Session.logCustomContext method and ViewerContext class.
// </remarks>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

public final class ViewerId extends Enum
{
	// <summary>
	//   Instructs the Console to use no viewer at all.
	// </summary>

	public static final ViewerId None = new ViewerId(-1, "None");

	// <summary>
	//   Instructs the Console to display the title of a Log Entry
	//   in a read-only text field.
	// </summary>

	public static final ViewerId Title = new ViewerId(0, "Title");

	// <summary>
	//   Instructs the Console to display the data of a Log Entry
	//   in a read-only text field.
	// </summary>

	public static final ViewerId Data = new ViewerId(1, "Data");

	// <summary>
	//   Instructs the Console to display the data of a Log Entry
	//   as a list.
	// </summary>

	public static final ViewerId List = new ViewerId(2, "List");

	// <summary>
	//   Instructs the Console to display the data of a Log Entry
	//   as a key/value list.
	// </summary>

	public static final ViewerId ValueList = new ViewerId(3, "ValueList");

	// <summary>
	//   Instructs the Console to display the data of a Log Entry
	//   using an object inspector.
	// </summary>

	public static final ViewerId Inspector = new ViewerId(4, "Inspector");

	// <summary>
	//   Instructs the Console to display the data of a Log Entry
	//   as a table.
	// </summary>

	public static final ViewerId Table = new ViewerId(5, "Table");

	// <summary>
	//   Instructs the Console to display the data of a Log Entry
	//   as a website.
	// </summary>

	public static final ViewerId Web = new ViewerId(100, "Web");

	// <summary>
	//   Instructs the Console to display the data of a Log Entry
	//   as a binary dump using a read-only hex editor.
	// </summary>

	public static final ViewerId Binary = new ViewerId(200, "Binary");

	// <summary>
	//   Instructs the Console to display the data of a Log Entry
	//   as HTML source with syntax highlighting.
	// </summary>

	public static final ViewerId HtmlSource = new ViewerId(300, "HtmlSource");

	// <summary>
	//   Instructs the Console to display the data of a Log Entry
	//   as JavaScript source with syntax highlighting.
	// </summary>

	public static final ViewerId JavaScriptSource = 
		new ViewerId(301, "JavaScriptSource");

	// <summary>
	//   Instructs the Console to display the data of a Log Entry
	//   as VBScript source with syntax highlighting.
	// </summary>

	public static final ViewerId VbScriptSource = 
		new ViewerId(302, "VbScriptSource");

	// <summary>
	//   Instructs the Console to display the data of a Log Entry
	//   as Perl source with syntax highlighting.
	// </summary>

	public static final ViewerId PerlSource = new ViewerId(303, "PerlSource");

	// <summary>
	//   Instructs the Console to display the data of a Log Entry
	//   as SQL source with syntax highlighting.
	// </summary>

	public static final ViewerId SqlSource = new ViewerId(304, "SqlSource");

	// <summary>
	//   Instructs the Console to display the data of a Log Entry
	//   as INI source with syntax highlighting.
	// </summary>

	public static final ViewerId IniSource = new ViewerId(305, "IniSource");

	// <summary>
	//   Instructs the Console to display the data of a Log Entry
	//   as Python source with syntax highlighting.
	// </summary>

	public static final ViewerId PythonSource = 
		new ViewerId(306, "PythonSource");

	// <summary>
	//   Instructs the Console to display the data of a Log Entry
	//   as XML source with syntax highlighting.
	// </summary>

	public static final ViewerId XmlSource = new ViewerId(307, "XmlSource");

	// <summary>
	//   Instructs the Console to display the data of a Log Entry
	//   as bitmap image.
	// </summary>

	public static final ViewerId Bitmap = new ViewerId(400, "Bitmap");

	// <summary>
	//   Instructs the Console to display the data of a Log Entry
	//   as JPEG image.
	// </summary>

	public static final ViewerId Jpeg = new ViewerId(401, "Jpeg");

	// <summary>
	//   Instructs the Console to display the data of a Log Entry
	//   as Windows icon.
	// </summary>

	public static final ViewerId Icon = new ViewerId(402, "Icon");

	// <summary>
	//   Instructs the Console to display the data of a Log Entry
	//   as Windows Metafile image.
	// </summary>

	public static final ViewerId Metafile = new ViewerId(403, "Metafile");

	private ViewerId(int value, String name)
	{
		super(value, name);
	}
}
