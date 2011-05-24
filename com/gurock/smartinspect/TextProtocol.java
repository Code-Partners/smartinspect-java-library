//
//<!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

import java.io.IOException;
import java.io.OutputStream;

// <summary>
//   Used for writing customizable plain text log files.
// </summary>
// <remarks>
//   TextProtocol is used for writing plain text log files. This
//   class is used when the 'text' protocol is specified in the
//   <link SmartInspect.setConnections, connections string>. See the
//   isValidOption method for a list of available protocol options.
// </remarks>
// <threadsafety>
//   The public members of this class are threadsafe.
// </threadsafety>

public class TextProtocol extends FileProtocol
{
	private static byte[] HEADER = 
		new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
	
	private static final boolean DEFAULT_INDENT = false;
	private static final String DEFAULT_PATTERN = 
		"[%timestamp%] %level%: %title%";
	
	private boolean fIndent;
	private String fPattern;
	private Formatter fFormatter;
	
	// <summary>
	//   Overridden. Returns "text".
	// </summary>
	// <returns>
	//   Just "text". Derived classes can change this behavior by
	//   overriding this method.
	// </returns>

	protected String getName()
	{
		return "text";
	}

	// <summary>
	//  Returns the formatter for this log file protocol.
	// </summary>
	// <returns>The formatter for this log file protocol.</returns>
	// <remarks>
	//   The standard implementation of this method returns an instance
	//   of the TextFormatter class. Derived classes can change this
	//   behavior by overriding this method.
	// </remarks>

	protected Formatter getFormatter()	
	{
		if (this.fFormatter == null)
		{
			this.fFormatter = new TextFormatter();
		}
		
		return this.fFormatter;
	}
	
	// <summary>
	//   Returns the default filename for this log file protocol.
	// </summary>
	// <returns>The default filename for this log file protocol.</returns>
	// <remarks>
	//   The standard implementation of this method returns the string
	//   "log.txt" here. Derived classes can change this behavior by
	//   overriding this method.
	// </remarks>
	
	protected String getDefaultFileName()
	{
		return "log.txt";
	}
	
	// <summary>
	//   Overridden. Intended to write the header of a log file.
	// </summary>
	// <param name="stream">
	//   The stream to which the header should be written to.
	// </param>
	// <param name="size">
	//   Specifies the current size of the supplied stream.
	// </param>
	// <returns>
	//   The new size of the stream after writing the header. If no
	//   header is written, the supplied size argument is returned.
	// </returns>
	// <remarks>
	//   The implementation of this method writes the standard UTF8
	//   BOM (byte order mark) to the supplied stream in order to
	//   identify the log file as text file in UTF8 encoding. Derived
	//   classes may change this behavior by overriding this method.
	// </remarks>

	protected long writeHeader(OutputStream stream, long size) 
		throws IOException
	{
		if (size == 0)
		{
			stream.write(HEADER, 0, HEADER.length);
			stream.flush();
			return HEADER.length;
		}
		else 
		{
			return size;
		}		
	}
	
	// <summary>
	//   Overridden. Intended to write the footer of a log file.
	// </summary>
	// <param name="stream">
	//   The stream to which the footer should be written to.
	// </param>
	// <remarks>
	//   The implementation of this method does nothing. Derived
	//   class may change this behavior by overriding this method.
	// </remarks>

	protected void writeFooter(OutputStream stream)
	{
		
	}
	
	// <summary>
	//   Overridden. Fills a ConnectionsBuilder instance with the
	//   options currently used by this text protocol.
	// </summary>
	// <param name="builder">
	//   The ConnectionsBuilder object to fill with the current options
	//   of this protocol.
	// </param>

	protected void buildOptions(ConnectionsBuilder builder)
	{
		super.buildOptions(builder);
		builder.addOption("indent", this.fIndent);
		builder.addOption("pattern", this.fPattern);
	}
	
	// <summary>
	//   Overridden. Validates if a protocol option is supported.
	// </summary>
	// <param name="name">The option name to validate.</param>
	// <returns>
	//   True if the option is supported and false otherwise.
	// </returns>
	// <remarks>
	//   The following table lists all valid options, their default
	//   values and descriptions for this text file protocol. For a
	//   list of options common to all file protocols, please have a
	//   look at the <link FileProtocol.isValidOption, isValidOption>
	//   method of the parent class. Please note that this text
	//   protocol <b>does not support log file encryption</b>.
	//
	//   <table>
	//   Valid Options  Default Value                     Description
	//   -              -                                 -
	//   indent         false                             Indicates if the
	//                                                     logging output
	//                                                     should
	//                                                     automatically be
	//                                                     indented like in
	//                                                     the Console.
	//
	//   pattern        "[%timestamp%] %level%: %title%"  Specifies the
	//                                                     pattern used
	//                                                     to create a
	//                                                     text
	//                                                     representation
	//                                                     of a packet.
	//   </table>
	//
	//   For detailed information of how a pattern string can look like,
	//   please have a look at the documentation of the PatternParser
	//   class, especially the PatternParser.setPattern method.
	// </remarks>
	// <example>
	// <code>
	// SiAuto.Si.setConnections("text()");
	// SiAuto.Si.setConnections("text(filename=\\"log.txt\\", append=true)");
	// SiAuto.Si.setConnections("text(filename=\\"log.txt\\")");
	// SiAuto.Si.setConnections("text(maxsize=\\"16MB\\")");
	// </code>
	// </example>
	
	protected boolean isValidOption(String name)
	{
		if (name.equals("encrypt") || name.equals("key"))
		{
			return false;
		}
		else
		{ 
			return 
				name.equals("pattern") || 
				name.equals("indent") ||
				super.isValidOption(name);
		}
	}
	
	// <summary>
	//   Overridden. Loads and inspects file specific options.
	// </summary>
	// <remarks>
	//   This method loads all relevant options and ensures their
	//   correctness. See isValidOption for a list of options which
	//   are recognized by the file protocol.
	// </remarks>
	
	protected void loadOptions()
	{
		super.loadOptions();
		this.fPattern = getStringOption("pattern", DEFAULT_PATTERN);
		this.fIndent = getBooleanOption("indent", DEFAULT_INDENT);
		((TextFormatter) getFormatter()).setPattern(this.fPattern);
		((TextFormatter) getFormatter()).setIndent(this.fIndent);
	}
}
