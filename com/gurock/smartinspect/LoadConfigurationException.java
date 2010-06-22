//
// <!-- Copyright (C) 2003-2010 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Used to report errors concerning the SmartInspect.loadConfiguration
//   method.
// </summary>
// <remarks>
//   This exception is used to report errors concerning the 
//   SmartInspect.loadConfiguration method. This method is able to load
//   the SmartInspect properties from a file. Therefore errors can occur
//   when trying to load properties from an inexistent file or when the
//   file can not be opened for reading, for example.
//
//   If such an error occurs, an instance of this class will be passed
//   to the <link SmartInspectListener.onError, Error event> in the
//   SmartInspect class. Please note, that, if a connections string can
//   be read while loading the configuration file, but is found to be
//   invalid then this exception type will not be used. In this case,
//   the SmartInspect.loadConfiguration method will use the
//   InvalidConnectionsException exception instead.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>
// <example>
// <code>
// import com.gurock.smartinspect.*;
//
// class Listener extends SmartInspectAdapter
// {
// 	public void onError(ErrorEvent e)
// 	{
// 		System.out.println(e.getException());
//
// 		if (e.getException() instanceof LoadConfigurationException)
// 		{
// 			LoadConfigurationException le =
// 				(LoadConfigurationException) e.getException();
//
// 			// A LoadConfigurationException provides additional information
// 			// about the occurred error besides the normal exception
// 			// message. It contains the name of the file which caused the
// 			// exception while trying to read the connections string from it.
//
// 			System.out.println(le.getFileName());
// 		}
// 	}
// }
//
// public class ErrorHandling
// {
// 	public static void main(String[] args)
// 	{
// 		// Register our event handler for the error event.
// 		SiAuto.si.addListener(new Listener());
//
// 		// Force an error event by passing a name of file
// 		// which does not exist to the loadConfiguration method.
// 		SiAuto.si.loadConfiguration("Inexistent.sic");
// 	}
// }
// </code>
// </example>

public final class LoadConfigurationException extends SmartInspectException
{
	private String fFileName;

	// <summary>
	//   Creates and initializes a LoadConfigurationException instance.
	// </summary>
	// <param name="fileName">
	//   The name of the file which caused this exception.
	// </param>
	// <param name="e">
	//   The error message which describes the exception.
	// </param>

	public LoadConfigurationException(String fileName, String e)
	{
		super(e);
		this.fFileName = fileName;
	}

	// <summary>
	//   Returns the name of the file which caused this exception while
	//   trying to load the SmartInspect properties from it.
	// </summary>
	// <returns>
	//   The name of the file which caused the exception.
	// </returns>

	public String getFileName()
	{
		return this.fFileName;
	}

	// <summary>
	//   Sets the name of the file which caused this exception while
	//   trying to load the SmartInspect properties from it.
	// </summary>
	// <param name="fileName">
	//   The name of the file which caused this exception.
	// </param>

	public void setFileName(String fileName)
	{
		this.fFileName = fileName;
	}
}
