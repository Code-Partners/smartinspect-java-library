//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
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
