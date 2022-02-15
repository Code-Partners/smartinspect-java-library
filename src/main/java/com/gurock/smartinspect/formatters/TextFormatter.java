//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect.formatters;

import com.gurock.smartinspect.PatternParser;
import com.gurock.smartinspect.packets.Packet;
import com.gurock.smartinspect.packets.PacketType;
import com.gurock.smartinspect.packets.logentry.LogEntry;

import java.io.IOException;
import java.io.OutputStream;

// <summary>
//   Responsible for creating a text representation of a packet and
//   writing it to a stream.
// </summary>
// <remarks>
//   This class creates a text representation of a packet and writes
//   it to a stream. The representation can be influenced with the
//   setPattern method. The compile method preprocesses a packet and
//   computes the required size of the packet. The write method writes
//   the preprocessed packet to the supplied stream.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>

public class TextFormatter extends Formatter
{
	private byte[] fLine;
	private PatternParser fParser;

	// <summary>
	//   Creates and initializes a TextFormatter instance.
	// </summary>

	public TextFormatter()
	{
		this.fParser = new PatternParser();	
	}
	
	// <summary>
	//   Overridden. Preprocesses (or compiles) a packet and returns the
	//   required size for the compiled result.
	// </summary>
	// <param name="packet">The packet to compile.</param>
	// <returns>The size for the compiled result.</returns>
	// <remarks>
	//   This method creates a text representation of the supplied packet
	//   and computes the required size. The resulting representation
	//   can be influenced with the setPattern method. To write a compiled
	//   packet, call the write method. Please note that this method
	//   only supports LogEntry objects and ignores any other packet.
	//   This means, for packets other than LogEntry, this method always
	//   returns 0.
	// </remarks>

	public int compile(Packet packet) throws IOException
	{
		if (packet.getPacketType() == PacketType.LogEntry)
		{
			String line =
				this.fParser.expand((LogEntry) packet) + "\r\n";
			this.fLine = line.getBytes("UTF-8");
			return this.fLine.length;
		}
		else 
		{
			this.fLine = null;
			return 0;
		}
	}

	// <summary>
	//   Overridden. Writes a previously compiled packet to the supplied
	//   stream.
	// </summary>
	// <param name="stream">The stream to write the packet to.</param>
	// <remarks>
	//   This method writes the previously computed text representation
	//   of a packet (see compile) to the supplied stream object.
	//   If the return value of the compile method was 0, nothing is
	//   written.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type          Condition
	//   -                       -
	//   IOException             An I/O error occurred while trying
	//                             to write the compiled packet.
	// </table>
	// </exception>

	public void write(OutputStream stream) throws IOException 
	{
		if (this.fLine != null)
		{
			stream.write(this.fLine, 0, this.fLine.length);
		}
	}

	// <summary>
	//   Returns the pattern used to create a text representation of
	//   a packet.
	// </summary>
	// <returns>The pattern string.</returns>
	// <remarks>
	//   For detailed information of how a pattern string can look like,
	//   please have a look at the documentation of the PatternParser
	//   class, especially the PatternParser.setPattern method.
	// </remarks>
	
	public String getPattern()
	{
		return this.fParser.getPattern();
	}
	
	// <summary>
	//   Sets the pattern used to create a text representation of a
	//   packet.
	// </summary>
	// <param name="pattern">The new pattern string.</param>
	// <remarks>
	//   For detailed information of how a pattern string can look like,
	//   please have a look at the documentation of the PatternParser
	//   class, especially the PatternParser.setPattern method.
	// </remarks>
	
	public void setPattern(String pattern)
	{
		this.fParser.setPattern(pattern);
	}
	
	// <summary>
	//   Returns if this formatter automatically intends log packets like
	//   in the Views of the SmartInspect Console.
	// </summary>
	// <returns>
	//   True if this formatter automatically intends log packets and
	//   false otherwise.
	// </returns>
	// <remarks>
	//   Log Entry packets of type EnterMethod increase the indentation
	//   and packets of type LeaveMethod decrease it.
	// </remarks>

	public boolean getIndent()
	{
		return this.fParser.getIndent();
	}
	
	// <summary>
	//   Sets if this formatter automatically intends log packets like in
	//   the Views of the SmartInspect Console.
	// </summary>
	// <param name="indent">
	//   Should be true if this formatter should automatically intend log
	//   packets and false otherwise.
	// </param>
	// <remarks>
	//   Log Entry packets of type EnterMethod increase the indentation
	//   and packets of type LeaveMethod decrease it.
	// </remarks>

	public void setIndent(boolean indent)
	{
		this.fParser.setIndent(indent);
	}
}
