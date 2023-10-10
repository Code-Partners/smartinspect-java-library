/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Provides automatically created objects for using the SmartInspect
 * and Session classes. This class provides a static field called si
 * of type SmartInspect. Furthermore, a Session instance named main
 * with si as parent is ready to use. The SiAuto class is especially
 * useful if you do not want to create SmartInspect and Session
 * instances by yourself.
 * <p>
 * The connections string of si is set to "pipe(reconnect=true,
 * reconnect.interval=1s)", the application name to "Auto" and
 * the session name of main to "Main".
 * <p>
 * <b>Please note that the default connections string has been
 * changed in SmartInspect 3.0</b>. In previous versions, the default
 * connections string was set to "tcp()".
 * <p>
 * The public static members of this class are threadsafe.
 *
 * <pre>
 * import com.gurock.smartinspect.SiAuto;
 *
 * public class SiAutoExample
 * {
 *      public static void main(String[] args)
 *      {
 *          SiAuto.si.setEnabled(true);
 *          SiAuto.main.enterProcess("SiAutoExample");
 *          try
 *          {
 *              .
 *              .
 *              .
 *          }
 *          finally
 *          {
 *              SiAuto.main.leaveProcess("SiAutoExample");
 *          }
 *      }
 * }
 * </pre>
 */
public final class SiAuto {
	private static final String APPNAME = "Auto";
	private static final String CONNECTIONS =
			"pipe(reconnect=true, reconnect.interval=1s)";
	private static final String SESSION = "Main";

	/**
	 * Automatically created SmartInspect instance.
	 *
	 * <p>The connections string is set to "pipe(reconnect=true, reconnect.interval=1s)".
	 * Please see {@link Protocol#isValidOption} for information on the
	 * used options. The application name is set to "Auto".</p>
	 *
	 * <p><b>Please note that the default connections string has been
	 * changed in SmartInspect 3.0</b>. In previous versions, the
	 * default connections string was set to "tcp()".</p>
	 */
	public static final SmartInspect si = new SmartInspect(APPNAME);

	/**
	 * Automatically created Session instance.
	 *
	 * <p>The session name is set to "Main"
	 * and the parent to SiAuto.si.</p>
	 */
	public static final Session main = si.addSession(SESSION, true);

	private SiAuto() {
	}

	// <ignore>
	// The documentation system seems to have problems with static
	// code blocks in Java, so we need to ignore the entire block
	// here.

	static {
		try {
			si.setConnections(CONNECTIONS);
		} catch (InvalidConnectionsException e) {
		}
	}

	// </ignore>
}
