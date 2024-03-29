/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Used to report errors concerning the SmartInspect.loadConnections method.
 * This exception is used to report errors concerning the SmartInspect.loadConnections method.
 * This method is capable of loading a connections string from a file. Therefore, errors can occur
 * when trying to load a connections string from an inexistent file or when the file cannot be opened
 * for reading, for example.
 * <p>
 * If such an error occurs, an instance of this class will be passed to the Error event in the
 * SmartInspect class. Please note, that if a connections string can be read correctly, but is found
 * to be invalid, then this exception type will not be used. In this case, the
 * SmartInspect.loadConnections method will use the InvalidConnectionsException exception instead.
 * <p>
 * This class is not thread-safe.
 * <p>
 * Example:
 * <pre>
 * import com.gurock.smartinspect.*;
 *
 * class Listener extends SmartInspectAdapter {
 *     public void onError(ErrorEvent e) {
 *         System.out.println(e.getException());
 *
 *         if (e.getException() instanceof LoadConnectionsException) {
 *             LoadConnectionsException le = (LoadConnectionsException) e.getException();
 *
 *             // A LoadConnectionsException provides additional information
 *             // about the occurred error besides the normal exception
 *             // message. It contains the name of the file which caused the
 *             // exception while trying to read the connections string from
 *             // it.
 *
 *             System.out.println(le.getFileName());
 *         }
 *     }
 * }
 *
 * public class ErrorHandling {
 *     public static void main(String[] args) {
 *         // Register our event handler for the error event.
 *         SiAuto.si.addListener(new Listener());
 *
 *         // Force an error event by passing a name of file
 *         // which does not exist to the loadConnections method.
 *         SiAuto.si.loadConnections("Inexistent.sic");
 *     }
 * }
 * </pre>
 */
public final class LoadConnectionsException extends SmartInspectException {
	private String fFileName;

	/**
	 * Creates and initializes a LoadConnectionsException instance.
	 *
	 * @param fileName The name of the file which caused this exception
	 * @param e        The error message which describes the exception
	 */
	public LoadConnectionsException(String fileName, String e) {
		super(e);
		this.fFileName = fileName;
	}

	/**
	 * Returns the name of the file which caused this exception while
	 * trying to load the connections string from it.
	 *
	 * @return The name of the file which caused the exception
	 */
	public String getFileName() {
		return this.fFileName;
	}

	/**
	 * Sets the name of the file which caused this exception
	 * while trying to load the connections string from it.
	 *
	 * @param fileName The name of the file which caused this exception
	 */
	public void setFileName(String fileName) {
		this.fFileName = fileName;
	}
}
