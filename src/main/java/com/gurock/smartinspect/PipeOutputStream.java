//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect;

import java.io.FileOutputStream;

class PipeOutputStream extends FileOutputStream
{
	public PipeOutputStream(PipeHandle handle)
	{
		super(handle.getHandle());
	}
}
