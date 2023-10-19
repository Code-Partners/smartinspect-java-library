/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Used for writing customizable plain text log files.
 *
 * <p>TextProtocol is used for writing plain text log files. This
 * class is used when the 'text' protocol is specified in the
 * connections string. See the
 * {@link SmartInspect#setConnections} method for a list of available protocol options.</p>
 *
 * <p>The public members of this class are threadsafe.</p>
 */
public class TextProtocol extends FileProtocol {
	private static byte[] HEADER =
			new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

	private static final boolean DEFAULT_INDENT = false;
	private static final String DEFAULT_PATTERN =
			"[%timestamp%] %level%: %title%";

	private boolean fIndent;
	private String fPattern;
	private Formatter fFormatter;

	/**
	 * Overridden. Returns "text".
	 *
	 * @return Just "text". Derived classes can change this behavior by
	 * overriding this method
	 */
	protected String getName() {
		return "text";
	}

	/**
	 * Returns the formatter for this log file protocol.
	 * <p>
	 * The standard implementation of this method returns an instance
	 * of the TextFormatter class. Derived classes can change this
	 * behavior by overriding this method.
	 *
	 * @return The formatter for this log file protocol
	 */
	protected Formatter getFormatter() {
		if (this.fFormatter == null) {
			this.fFormatter = new TextFormatter();
		}

		return this.fFormatter;
	}

	/**
	 * Returns the default filename for this log file protocol.
	 * The standard implementation of this method returns the string
	 * "log.txt" here. Derived classes can change this behavior by
	 * overriding this method.
	 *
	 * @return The default filename for this log file protocol
	 */
	protected String getDefaultFileName() {
		return "log.txt";
	}

	/**
	 * Overridden. Intended to write the header of a log file.
	 * The implementation of this method writes the standard UTF8
	 * BOM (byte order mark) to the supplied stream in order to
	 * identify the log file as text file in UTF8 encoding. Derived
	 * classes may change this behavior by overriding this method.
	 *
	 * @param stream The stream to which the header should be written to
	 * @param size   Specifies the current size of the supplied stream
	 * @return The new size of the stream after writing the header. If no
	 * header is written, the supplied size argument is returned
	 */
	protected long writeHeader(OutputStream stream, long size)
			throws IOException {
		if (size == 0) {
			stream.write(HEADER, 0, HEADER.length);
			stream.flush();
			return HEADER.length;
		} else {
			return size;
		}
	}

	/**
	 * Overridden. Intended to write the footer of a log file.
	 * <p>
	 * The implementation of this method does nothing. Derived
	 * class may change this behavior by overriding this method.
	 *
	 * @param stream The stream to which the footer should be written to
	 */
	protected void writeFooter(OutputStream stream) {

	}

	/**
	 * Overridden. Fills a ConnectionsBuilder instance with the
	 * options currently used by this text protocol.
	 *
	 * @param builder The ConnectionsBuilder object to fill with the current options
	 *                of this protocol
	 */
	protected void buildOptions(ConnectionsBuilder builder) {
		super.buildOptions(builder);
		builder.addOption("indent", this.fIndent);
		builder.addOption("pattern", this.fPattern);
	}

	/**
	 * Overridden. Validates if a protocol option is supported.
	 * <table>
	 * <caption>Text protocol options</caption>
	 * <tr><th>Valid Options</th><th>Default Value</th><th>Description</th></tr>
	 * <tr><td>indent</td><td>false</td><td>Indicates if the logging output
	 *  should automatically be indented like in the Console</td></tr>
	 * <tr><td>pattern</td><td>"[%timestamp%] %level%: %title%"</td><td>Specifies the pattern used
	 * to create a text representation of a packet</td></tr>
	 * </table>
	 * <p>
	 * For detailed information of how a pattern string can look like,
	 * please have a look at the documentation of the PatternParser
	 * class, especially the PatternParser.setPattern method.
	 * <p>
	 * The following table lists all valid options, their default
	 * values and descriptions for this text file protocol. For a
	 * list of options common to all file protocols, please have a
	 * look at the isValidOption method of the parent class. Please note that this text
	 * protocol <b>does not support log file encryption</b>.
	 * <pre>
	 * SiAuto.Si.setConnections("text()");
	 * SiAuto.Si.setConnections("text(filename=\"log.txt\", append=true)");
	 * SiAuto.Si.setConnections("text(filename=\"log.txt\")");
	 * SiAuto.Si.setConnections("text(maxsize=\"16MB\")");
	 * }
	 * </pre>
	 *
	 * @param name The option name to validate.
	 * @return True if the option is supported and false otherwise.
	 */
	protected boolean isValidOption(String name) {
		if (name.equals("encrypt") || name.equals("key")) {
			return false;
		} else {
			return
					name.equals("pattern") ||
							name.equals("indent") ||
							super.isValidOption(name);
		}
	}

	/**
	 * Overridden. Loads and inspects file specific options.
	 * This method loads all relevant options and ensures their
	 * correctness. See isValidOption for a list of options which
	 * are recognized by the file protocol.
	 */
	protected void loadOptions() {
		super.loadOptions();
		this.fPattern = getStringOption("pattern", DEFAULT_PATTERN);
		this.fIndent = getBooleanOption("indent", DEFAULT_INDENT);
		((TextFormatter) getFormatter()).setPattern(this.fPattern);
		((TextFormatter) getFormatter()).setIndent(this.fIndent);
	}
}
