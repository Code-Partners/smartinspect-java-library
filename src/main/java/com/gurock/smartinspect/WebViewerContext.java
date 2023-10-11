/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Represents the web viewer in the Console which can display HTML
 * text content as web pages.
 *
 * <p>The web viewer in the Console interprets the <a href="LogEntry.getData">
 * data of a Log Entry</a> as an HTML website.
 *
 * <p><b>Note:</b> This class is not guaranteed to be threadsafe.
 */
public class WebViewerContext extends TextContext {
	/**
	 * Creates and initializes a WebViewerContext instance.
	 */
	public WebViewerContext() {
		super(ViewerId.Web);
	}
}

