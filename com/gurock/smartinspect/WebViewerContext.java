//
// <!-- Copyright (C) Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

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

