//
// <!-- Copyright (C) 2003-2010 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Represents the data viewer in the Console which can display simple
//   and unformatted text.
// </summary>
// <remarks>
//   The data viewer in the Console interprets the <link LogEntry.getData,
//   data of a Log Entry> as text and displays it in a read-only text
//   field.
//
//   You can use the DataViewerContext class for creating custom log
//   methods around <link Session.logCustomContext, logCustomContext> for
//   sending custom text data.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>

public class DataViewerContext extends TextContext
{
	// <summary>
	//   Creates and initializes a DataViewerContext instance.
	// </summary>
	
	public DataViewerContext()
	{
		super(ViewerId.Data);
	}
}

