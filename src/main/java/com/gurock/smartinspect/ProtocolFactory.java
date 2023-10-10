/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import java.util.HashMap;

/**
 * Creates Protocol instances and registers custom protocols.
 * <p>
 * This class is responsible for creating instances of Protocol
 * subclasses and registering custom protocol implementations. To
 * add a custom protocol, please have a look at the documentation
 * and example of the registerProtocol method.
 * <p>
 * This class is fully threadsafe.
 */
public final class ProtocolFactory {
	private static Class fProtocolClass = Protocol.class;
	private static HashMap fLookup;
	private static final String PROTOCOL_NOT_FOUND =
			"The requested protocol is unknown";

	// <ignore>
	// The documentation system seems to have problems with static
	// code blocks in Java, so we need to ignore the entire block
	// here.

	static {
		fLookup = new HashMap();
		registerProtocol("file", FileProtocol.class);
		registerProtocol("mem", MemoryProtocol.class);
		registerProtocol("tcp", TcpProtocol.class);
		registerProtocol("text", TextProtocol.class);
		registerProtocol("pipe", PipeProtocol.class);
	}

	// </ignore>

	private static Protocol createInstance(Class impl)
			throws SmartInspectException {
		try {
			return (Protocol) impl.newInstance();
		} catch (Exception e) {
			throw new SmartInspectException(e.getMessage());
		}
	}

	/**
	 * Creates an instance of a Protocol subclass.
	 * <p>
	 * This method tries to create an instance of a Protocol subclass
	 * using the name parameter. If you, for example, specify "file"
	 * as name parameter, this method returns an instance of the
	 * FileProtocol class. If the creation of such an instance has
	 * been successful, the supplied options will be applied to
	 * the protocol.
	 * <p>
	 * For a list of available protocols, please refer to the Protocol
	 * class. Additionally, to add your own custom protocol, please
	 * have a look at the registerProtocol method.
	 * <p>
	 * Please note that if the name argument is null, then the return
	 * value of this method is null as well.
	 *
	 * @param name    The protocol name to search for
	 * @param options The options to apply to the new Protocol instance. Can be null
	 * @return A new instance of a Protocol subclass
	 * @throws SmartInspectException if the protocol is unknown or options have invalid syntax
	 */
	public synchronized static Protocol getProtocol(String name,
													String options) throws SmartInspectException {
		if (name == null) {
			return null;
		}

		Class impl = (Class) fLookup.get(name.trim().toLowerCase());

		if (impl != null) {
			Protocol protocol = createInstance(impl);
			protocol.initialize(options);
			return protocol;
		} else {
			throw new SmartInspectException(PROTOCOL_NOT_FOUND);
		}
	}

	/**
	 * Registers a custom protocol implementation to the SmartInspect Java library.
	 * <br>
	 * This method enables you to register your own custom protocols. This can be used to extend the built-in capabilities of the
	 * SmartInspect Java library. To add your own protocol, derive your custom protocol class from Protocol, choose a name and
	 * pass this name and the type to this method. After registering your protocol, you are able to use it in the
	 * connections string just like any other (standard) protocol.
	 * <br>
	 * If one of the supplied arguments is null or the supplied type is not derived from the Protocol class then no custom protocol
	 * is added.
	 * <p>
	 * <b>Example:</b>
	 * <pre>
	 * import com.gurock.smartinspect.*;
	 *
	 * class StdoutProtocol extends Protocol
	 * {
	 *    // Implement the abstract methods and handle your protocol
	 * 	// specific options ..
	 * }
	 *
	 * public class Program
	 * {
	 *    public static void main(String[] args)
	 * 		throws InvalidConnectionsException
	 *    {
	 * 	   ProtocolFactory.registerProtocol("stdout", StdoutProtocol.class);
	 * 	   SiAuto.si.setConnections("stdout()");
	 * 	   SiAuto.si.setEnabled(true);
	 *    }
	 * }
	 * </pre>
	 *
	 * @param name The name of the custom protocol to register.
	 * @param impl The class of your custom protocol. It needs to be derived from the Protocol class
	 */
	public synchronized static void registerProtocol(String name, Class impl) {
		if (name != null && impl != null) {
			if (fProtocolClass.isAssignableFrom(impl)) {
				fLookup.put(name.trim().toLowerCase(), impl);
			}
		}
	}
}
