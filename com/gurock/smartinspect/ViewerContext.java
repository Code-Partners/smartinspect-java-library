//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Is the abstract base class for a viewer context.
// </summary>
// <remarks>
//   A viewer context contains a viewer ID and data which can be
//   displayed in a viewer in the Console. Every viewer in the Console
//   has a corresponding viewer context class in this library. A viewer
//   context is capable of processing data and to format them in a way
//   so that the corresponding viewer in the Console can display it.
//   
//   Viewer contexts provide a simple way to extend the functionality
//   of the SmartInspect Java library. See the Session.logCustomContext
//   method for a detailed example.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>

public abstract class ViewerContext
{
	private ViewerId fVi;

	// <summary>
	//   Creates and initializes a ViewerContext instance.
	// </summary>
	// <param name="vi">The viewer ID to use.</param>

	protected ViewerContext(ViewerId vi)
	{
		this.fVi = vi;
	
	}

	// <summary>
	//   Returns the viewer ID which specifies the viewer to use in
	//   the Console.
	// </summary>
	// <returns>
	//   The viewer ID which specifies the viewer to use in the Console.
	// </returns>

	public ViewerId getViewerId()
	{
		return this.fVi;
	}

	// <summary>
	//   Returns the actual data which will be displayed in the viewer
	//   specified by the getViewerId method.
	// </summary>
	// <returns>
	//   The actual data which will be displayed in the viewer specified
	//   by the getViewerId method.
	// </returns>

	public abstract byte[] getViewerData();
	
	// <summary>
	//   Releases any resources of this viewer context.
	// </summary>
	// <remarks>
	//   The default implementation does nothing. Derived classes can
	//   change this behavior by overriding this method.
	// </remarks>
	
	public void close()
	{
		// Do nothing by default.
	}
}
