//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Provides automatically created objects for using the SmartInspect
//   and Session classes.
// </summary>
// <remarks>
//   This class provides a static field called si of type SmartInspect.
//   Furthermore a Session instance named main with si as parent is ready
//   to use. The SiAuto class is especially useful if do not want to
//   create SmartInspect and Session instances by yourself.
//
//   The <link SmartInspect.setConnections, connections string> of si
//   is set to "pipe(reconnect=true, reconnect.interval=1s)", the
//   <link SmartInspect.setAppName, application name> to "Auto" and
//   the <link Session.setName, session name> of main to "Main".
//
//   <b>Please note that the default connections string has been
//   changed in SmartInspect 3.0</b>. In previous versions, the default
//   connections string was set to "tcp()".
// </remarks>
// <threadsafety>
//   The public static members of this class are threadsafe.
// </threadsafety>
// <example>
// <code>
// import com.gurock.smartinspect.SiAuto;
//
// public class SiAutoExample
// {
// 	public static void main(String[] args)
// 	{
// 		SiAuto.si.setEnabled(true);
// 		SiAuto.main.enterProcess("SiAutoExample");
// 		try 
// 		{
// 			.
// 			.
// 			.
// 		}
// 		finally 
// 		{
// 			SiAuto.main.leaveProcess("SiAutoExample");
// 		}
// 	}
// }
// </code>
// </example>

public final class SiAuto
{
	private static final String APPNAME = "Auto"; 
	private static final String CONNECTIONS = 
		"pipe(reconnect=true, reconnect.interval=1s)";
	private static final String SESSION = "Main"; 
	
	// <summary>
	//   Automatically created SmartInspect instance.
	// </summary>
	// <remarks>
	//   The <link SmartInspect.setConnections, connections string>
	//   is set to "pipe(reconnect=true, reconnect.interval=1s)".
	//   Please see Protocol.isValidOption for information on the
	//   used options. The <link SmartInspect.setAppName,
	//   application name> is set to "Auto".
	//
	//   <b>Please note that the default connections string has been
	//   changed in SmartInspect 3.0</b>. In previous versions, the
	//   default connections string was set to "tcp()".
	// </remarks>

	public static final SmartInspect si = new SmartInspect(APPNAME);

	// <summary>
	//   Automatically created Session instance.
	// </summary>
	// <remarks>
	//   The <link Session.setName, session name> is set to "Main"
	//   and the <link Session.getParent, parent> to SiAuto.si.
	// </remarks>

	public static final Session main = si.addSession(SESSION, true);

	private SiAuto()
	{
	}

	// <ignore>
	// The documentation system seems to have problems with static
	// code blocks in Java, so we need to ignore the entire block
	// here.

	static
	{
		try
		{
			si.setConnections(CONNECTIONS);
		}
		catch (InvalidConnectionsException e)
		{
		}
	}

	// </ignore>
}
