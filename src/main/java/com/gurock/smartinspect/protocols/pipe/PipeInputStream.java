/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.protocols.pipe;

import java.io.FileInputStream;

class PipeInputStream extends FileInputStream {
	public PipeInputStream(PipeHandle handle) {
		super(handle.getHandle());
	}
}
