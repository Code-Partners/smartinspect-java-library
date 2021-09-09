//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect;

import java.util.HashMap;

// <summary>
//   Creates Protocol instances and registers custom protocols.
// </summary>
// <remarks>
//   This class is responsible for creating instances of Protocol
//   subclasses and registering custom protocol implementations. To
//   add a custom protocol, please have a look at the documentation
//   and example of the registerProtocol method.
// </remarks>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

public final class ProtocolFactory
{
	private static Class fProtocolClass = Protocol.class;
	private static HashMap fLookup;
	private static final String PROTOCOL_NOT_FOUND = 
		"The requested protocol is unknown";
	
	// <ignore>
	// The documentation system seems to have problems with static
	// code blocks in Java, so we need to ignore the entire block
	// here.
	
	static 
	{
		fLookup = new HashMap();
		registerProtocol("file", FileProtocol.class);
		registerProtocol("mem", MemoryProtocol.class);
		registerProtocol("tcp", TcpProtocol.class);
		registerProtocol("text", TextProtocol.class);
		registerProtocol("pipe", PipeProtocol.class);
	}
	
	// </ignore>

	private static Protocol createInstance(Class impl) 
		throws SmartInspectException
	{
		try 
		{
			return (Protocol) impl.newInstance();
		}
		catch (Exception e)
		{
			throw new SmartInspectException(e.getMessage());
		}
	}
	
	// <summary>
	//   Creates an instance of a Protocol subclass. 
	// </summary>
	// <param name="name">The protocol name to search for.</param>
	// <param name="options">
	//   The options to apply to the new Protocol instance. Can be
	//   null.
	// </param>
	// <returns>A new instance of a Protocol subclass.</returns>
	// <remarks>
	//   This method tries to create an instance of a Protocol subclass
	//   using the name parameter. If you, for example, specify "file"
	//   as name parameter, this method returns an instance of the
	//   FileProtocol class. If the creation of such an instance has
	//   been successful, the supplied options will be applied to
	//   the protocol.
	//   
	//   For a list of available protocols, please refer to the Protocol
	//   class. Additionally, to add your own custom protocol, please
	//   have a look at the registerProtocol method.
	//   
	//   Please note that if the name argument is null, then the return
	//   value of this method is null as well.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   SmartInspectException  Unknown protocol or invalid options
	//                            syntax.
	// </table>
	// </exception>

	public synchronized static Protocol getProtocol(String name,
		String options) throws SmartInspectException
	{
		if (name == null)
		{
			return null;
		}
		
		Class impl = (Class) fLookup.get(name.trim().toLowerCase());

		if (impl != null)
		{
			Protocol protocol = createInstance(impl);
			protocol.initialize(options);
			return protocol;
		}
		else 
		{
			throw new SmartInspectException(PROTOCOL_NOT_FOUND);
		}
	}
	
	// <summary>
	//   Registers a custom protocol implementation to the SmartInspect
	//   Java library.
	// </summary>
	// <param name="name">
	//   The name of the custom protocol to register.
	// </param>
	// <param name="impl">
	//   The class of your custom protocol. It needs to be derived from
	//   the Protocol class.
	// </param>
	// <remarks>
	//   This method enables you to register your own custom protocols.
	//   This can be used to extend the built-in capabilities of the
	//   SmartInspect Java library. To add your own protocol, derive
	//   your custom protocol class from Protocol, choose a name and
	//   pass this name and the type to this method. After registering
	//   your protocol, you are able to use it in the
	//   <link SmartInspect.setConnections, connections string> just
	//   like any other (standard) protocol.
	//
	//   If one of the supplied arguments is null or the supplied type
	//   is not derived from the Protocol class then no custom protocol
	//   is added.
	// </remarks>
	// <example>
	// <code>
	// import com.gurock.smartinspect.*;
	// 
	// class StdoutProtocol extends Protocol
	// {
	// 	// Implement the abstract methods and handle your protocol
	// 	// specific options ..
	// }
	//
	// public class Program
	// {
	// 	public static void main(String[] args) 
	// 		throws InvalidConnectionsException
	// 	{
	// 		ProtocolFactory.registerProtocol("stdout", StdoutProtocol.class);
	// 		SiAuto.si.setConnections("stdout()");
	// 		SiAuto.si.setEnabled(true);
	// 	}
	// }
	// </code>
	// </example>
	
	public synchronized static void registerProtocol(String name, Class impl)
	{
		if (name != null && impl != null)
		{
			if (fProtocolClass.isAssignableFrom(impl))
			{
				fLookup.put(name.trim().toLowerCase(), impl);
			}
		}
	}
}
