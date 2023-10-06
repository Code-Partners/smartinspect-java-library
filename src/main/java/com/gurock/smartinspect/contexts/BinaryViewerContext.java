/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.contexts;

import com.gurock.smartinspect.ViewerId;

/**
 * Represents the binary viewer in the Console which can display binary
 * data in a read-only hex editor.
 * <p>
 * The binary viewer in the Console interprets the <link LogEntry.getData,
 * data of a Log Entry> as binary data and displays it in a read-only
 * hex editor.
 * <p>
 * You can use the BinaryViewerContext class for creating custom log
 * methods around `Session.logCustomContext` for
 * sending custom binary data.
 * <p>
 * This class is not guaranteed to be threadsafe.
 */
public class BinaryViewerContext extends BinaryContext
{
	/**
	 * Creates and initializes a BinaryViewerContext instance.
	 */
	public BinaryViewerContext()
	{
		super(ViewerId.Binary);
	}
}

