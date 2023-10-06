/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Represents the data viewer in the Console which can display simple
 * and unformatted text.
 * <p>
 * The data viewer in the Console interprets the data of a Log Entry as text and displays
 * it in a read-only text field.
 * <p>
 * You can use the DataViewerContext class for creating custom log
 * methods around logCustomContext for sending custom text data.
 * <p>
 * Note: This class is not guaranteed to be threadsafe.
 */
public class DataViewerContext extends TextContext {
	/**
	 * Creates and initializes a DataViewerContext instance.
	 */
	public DataViewerContext() {
		super(ViewerId.Data);
	}
}

