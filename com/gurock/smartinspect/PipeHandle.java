//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

class PipeHandle
{
	private FileDescriptor fHandle;
	private RandomAccessFile fFile;
	
	public PipeHandle(String pipeName)
		throws FileNotFoundException, IOException
	{
		String fileName = "\\\\.\\pipe\\" + pipeName; 
		this.fFile = new RandomAccessFile(fileName, "rw");
		this.fHandle = this.fFile.getFD();		
	}
	
	protected FileDescriptor getHandle()
	{
		return this.fHandle;
	}
}
