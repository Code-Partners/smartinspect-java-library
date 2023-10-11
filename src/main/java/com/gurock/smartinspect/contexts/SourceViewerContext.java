/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.contexts;

import com.gurock.smartinspect.SourceId;

/**
 * Represents the source viewer in the Console which can display text
 * data as source code with syntax highlighting.
 * <p>
 * The source viewer in the Console interprets the data of a Log Entry as source code and
 * displays it in a read-only text editor with syntax highlighting.
 * <p>
 * This class is not guaranteed to be threadsafe.
 */
public class SourceViewerContext extends TextContext {
	/**
	 * Creates and initializes a SourceViewerContext instance.
	 *
	 * @param id The source ID to use
	 */
	public SourceViewerContext(SourceId id) {
		super(id.toViewerId());
	}
}

