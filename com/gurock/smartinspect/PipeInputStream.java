//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect;

import java.io.FileInputStream;

class PipeInputStream extends FileInputStream
{
	public PipeInputStream(PipeHandle handle)
	{
		super(handle.getHandle());
	}
}
