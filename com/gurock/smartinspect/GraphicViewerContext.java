//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Represents the graphic viewer in the Console which can display
//   images.
// </summary>
// <remarks>
//   The graphic viewer in the Console interprets the <link LogEntry.getData,
//   data of a Log Entry> as picture.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>

public class GraphicViewerContext extends TextContext
{
	// <summary>
	//   Creates and initializes a GraphicViewerContext instance. 
	// </summary>
	// <param name="id">The graphic ID to use.</param>

	public GraphicViewerContext(GraphicId id)
	{
		super(id.toViewerId());
	}
}

