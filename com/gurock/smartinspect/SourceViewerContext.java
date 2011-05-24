//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Represents the source viewer in the Console which can display text
//   data as source code with syntax highlighting.
// </summary>
// <remarks>
//   The source viewer in the Console interprets the <link LogEntry.getData,
//   data of a Log Entry> as source code and displays it in a read-only
//   text editor with syntax highlighting.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>

public class SourceViewerContext extends TextContext
{
	// <summary>
	//   Creates and initializes a SourceViewerContext instance.
	// </summary>
	// <param name="id">The source ID to use.</param>
	
	public SourceViewerContext(SourceId id)
	{
		super(id.toViewerId());
	}
}

