//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect.formatters;

import com.gurock.smartinspect.packets.Packet;

import java.io.IOException;
import java.io.OutputStream;

// <summary>
//   Responsible for formatting and writing a packet.
// </summary>
// <remarks>
//   This abstract class defines several methods which are intended to
//   preprocess a packet and subsequently write it to a stream. The
//   process of preprocessing (or compiling) and writing a packet can
//   either be executed with a single step by calling the format method
//   or with two steps by calls to compile and write.
// </remarks>
// <threadsafety>
//   This class and subclasses thereof are not guaranteed to be
//   threadsafe.
// </threadsafety>

public abstract class Formatter 
{
	// <summary>
	//   Preprocesses (or compiles) a packet and returns the required
	//   size for the compiled result.
	// </summary>
	// <param name="packet">The packet to compile.</param>
	// <returns>The size for the compiled result.</returns>
	// <remarks>
	//   To write a previously compiled packet, call the write method.
	//   Derived classes are intended to compile the supplied packet
	//   and return the required size for the compiled result.
	// </remarks>

	public abstract int compile(Packet packet) throws IOException;
	
	// <summary>
	//   Writes a previously compiled packet to the supplied stream.
	// </summary>
	// <param name="stream">The stream to write the packet to.</param>
	// <remarks>
	//   This method is intended to write a previously compiled packet
	//   (see compile) to the supplied stream object. If the return
	//   value of the compile method was 0, nothing is written.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type          Condition
	//   -                       -
	//   IOException             An I/O error occurred while trying
	//                             to write the compiled packet.
	// </table>
	// </exception>
	
	public abstract void write(OutputStream stream) throws IOException;
	
	// <summary>
	//   Compiles a packet and writes it to a stream.  
	// </summary>
	// <param name="packet">The packet to compile.</param>
	// <param name="stream">The stream to write the packet to.</param>
	// <remarks>
	//   This non-abstract method simply calls the compile method with
	//   the supplied packet object and then the write method with
	//   the supplied stream object.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type          Condition
	//   -                       -
	//   IOException             An I/O error occurred while trying
	//                             to write the compiled packet.
	// </table>
	// </exception>
	
	public void format(Packet packet, OutputStream stream) 
		throws IOException
	{
		compile(packet);
		write(stream);
	}
}
