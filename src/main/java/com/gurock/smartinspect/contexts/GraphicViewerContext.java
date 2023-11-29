/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.contexts;

import com.gurock.smartinspect.GraphicId;

/**
 * Represents the graphic viewer in the Console which can display images. 
 * <p>
 * The graphic viewer in the Console interprets the data of a Log Entry as picture.
 * This class is not guaranteed to be threadsafe.
 */
public class GraphicViewerContext extends TextContext {
	/**
	 * Creates and initializes a GraphicViewerContext instance.
	 *
	 * @param id The graphic ID to use.
	 */
	public GraphicViewerContext(GraphicId id) {
		super(id.toViewerId());
	}
}

