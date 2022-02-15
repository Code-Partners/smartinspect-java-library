//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect.contexts;

// <summary>
//   Represents the web viewer in the Console which can display HTML
//   text content as web pages.
// </summary>
// <remarks>
//   The web viewer in the Console interprets the <link LogEntry.getData,
//   data of a Log Entry> as an HTML website.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>

import com.gurock.smartinspect.ViewerId;

public class WebViewerContext extends TextContext
{

	// <summary>
	//   Creates and initializes a WebViewerContext instance.
	// </summary>

	public WebViewerContext()
	{
		super(ViewerId.Web);
	}
}

