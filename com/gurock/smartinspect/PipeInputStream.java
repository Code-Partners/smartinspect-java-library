//
// <!-- Copyright (C) 2003-2010 Gurock Software GmbH. All rights reserved. -->
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
