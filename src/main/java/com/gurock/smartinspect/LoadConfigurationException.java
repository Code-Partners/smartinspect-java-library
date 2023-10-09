/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Used to report errors concerning the SmartInspect.loadConfiguration method. This exception is used to report errors concerning
 * the SmartInspect.loadConfiguration method. This method is able to load the SmartInspect properties from a file. Therefore,
 * errors can occur when trying to load properties from an inexistent file or when the file can not be opened for reading, for example.
 *
 * <p>If such an error occurs, an instance of this class will be passed to the Error event in the SmartInspect class. Please note,
 * that, if a connections string can be read while loading the configuration file, but is found to be invalid then this exception
 * type will not be used. In this case, the SmartInspect.loadConfiguration method will use the InvalidConnectionsException
 * exception instead.</p>
 *
 * <p>This class is not guaranteed to be threadsafe.</p>
 *
 * <pre>
 *   import com.gurock.smartinspect.*;
 *
 *   class Listener extends SmartInspectAdapter
 *   {
 *       public void onError(ErrorEvent e)
 *       {
 *           System.out.println(e.getException());
 *           if (e.getException() instanceof LoadConfigurationException)
 *           {
 *               LoadConfigurationException le = (LoadConfigurationException) e.getException();
 *               System.out.println(le.getFileName());
 *           }
 *       }
 *   }
 *
 *   public class ErrorHandling
 *   {
 *       public static void main(String[] args)
 *       {
 *           // Register our event handler for the error event.
 *           SiAuto.si.addListener(new Listener());
 *           // Force an error event by passing a name of file which does not exist to the loadConfiguration method.
 *           SiAuto.si.loadConfiguration("Inexistent.sic");
 *       }
 *   }
 * </pre>
 */
public final class LoadConfigurationException extends SmartInspectException {
	private String fFileName;

	/**
	 * Creates and initializes a LoadConfigurationException instance.
	 *
	 * @param fileName The name of the file which caused this exception
	 * @param e        The error message which describes the exception
	 */
	public LoadConfigurationException(String fileName, String e) {
		super(e);
		this.fFileName = fileName;
	}

	/**
	 * Returns the name of the file which caused this exception while
	 * trying to load the SmartInspect properties from it.
	 *
	 * @return The name of the file which caused the exception.
	 */
	public String getFileName() {
		return this.fFileName;
	}

	/**
	 * Sets the name of the file which caused this exception while
	 * trying to load the SmartInspect properties from it.
	 *
	 * @param fileName The name of the file which caused this exception
	 */
	public void setFileName(String fileName)
	{
		this.fFileName = fileName;
	}
}
