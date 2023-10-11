/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Is the abstract base class for a viewer context.
 * <p>
 * A viewer context contains a viewer ID and data which can be
 * displayed in a viewer in the Console. Every viewer in the Console
 * has a corresponding viewer context class in this library. A viewer
 * context is capable of processing data and to format them in a way
 * so that the corresponding viewer in the Console can display it.
 * <p>
 * Viewer contexts provide a simple way to extend the functionality
 * of the SmartInspect Java library. See the Session.logCustomContext
 * method for a detailed example.
 * <p>
 * This class is not guaranteed to be threadsafe.
 */
public abstract class ViewerContext {
	private ViewerId fVi;

	/**
	 * Creates and initializes a ViewerContext instance.
	 *
	 * @param vi The viewer ID to use
	 */
	protected ViewerContext(ViewerId vi) {
		this.fVi = vi;

	}

	/**
	 * Returns the viewer ID which specifies the viewer to use in
	 * the Console.
	 *
	 * @return The viewer ID which specifies the viewer to use in the Console
	 */
	public ViewerId getViewerId() {
		return this.fVi;
	}

	/**
	 * Returns the actual data which will be displayed in the viewer
	 * specified by the getViewerId method.
	 *
	 * @return The actual data which will be displayed in the viewer specified
	 * by the getViewerId method
	 */
	public abstract byte[] getViewerData();

	/**
	 * Releases any resources of this viewer context.
	 * <p>
	 * The default implementation does nothing. Derived classes can
	 * change this behavior by overriding this method.
	 */
	public void close() {
		// Do nothing by default.
	}
}
