/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Responsible for creating a text representation of a packet and
 * writing it to a stream.
 * <p>
 * This class creates a text representation of a packet and writes
 * it to a stream. The representation can be influenced with the
 * setPattern method. The compile method preprocesses a packet and
 * computes the required size of the packet. The write method writes
 * the preprocessed packet to the supplied stream.
 * <p>
 * Note: This class is not guaranteed to be threadsafe.
 */
public class TextFormatter extends Formatter {
	private byte[] fLine;
	private PatternParser fParser;

	/**
	 * Creates and initializes a TextFormatter instance.
	 */
	public TextFormatter() {
		this.fParser = new PatternParser();
	}

	/**
	 * Overridden. Preprocesses (or compiles) a packet and returns the
	 * required size for the compiled result.
	 *
	 * <p>This method creates a text representation of the supplied packet
	 * and computes the required size. The resulting representation
	 * can be influenced with the setPattern method. To write a compiled
	 * packet, call the write method. Please note that this method
	 * only supports LogEntry objects and ignores any other packet.
	 * This means, for packets other than LogEntry, this method always
	 * returns 0.</p>
	 *
	 * @param packet The packet to compile
	 * @return The size for the compiled result
	 */
	public int compile(Packet packet) throws IOException {
		if (packet.getPacketType() == PacketType.LogEntry) {
			String line =
					this.fParser.expand((LogEntry) packet) + "\r\n";
			this.fLine = line.getBytes("UTF-8");
			return this.fLine.length;
		} else {
			this.fLine = null;
			return 0;
		}
	}

	/**
	 * Overridden. Writes a previously compiled packet to the supplied stream.
	 * <p>
	 * This method writes the previously computed text representation
	 * of a packet (see compile) to the supplied stream object.
	 * If the return value of the compile method was 0, nothing is
	 * written.
	 *
	 * @param stream The stream to write the packet to
	 * @throws IOException if an I/O error occurred while trying to write the compiled packet
	 */
	public void write(OutputStream stream) throws IOException {
		if (this.fLine != null) {
			stream.write(this.fLine, 0, this.fLine.length);
		}
	}

	/**
	 * Returns the pattern used to create a text representation of a packet.
	 *
	 * <p>
	 * For detailed information of how a pattern string can look like,
	 * please have a look at the documentation of the PatternParser
	 * class, especially the PatternParser.setPattern method.
	 * </p>
	 *
	 * @return The pattern string
	 */
	public String getPattern() {
		return this.fParser.getPattern();
	}

	/**
	 * Sets the pattern used to create a text representation of a
	 * packet. For detailed information of how a pattern string can look like,
	 * please have a look at the documentation of the PatternParser
	 * class, especially the PatternParser.setPattern method.
	 *
	 * @param pattern The new pattern string
	 */
	public void setPattern(String pattern) {
		this.fParser.setPattern(pattern);
	}

	/**
	 * Returns if this formatter automatically intends log packets like
	 * in the Views of the SmartInspect Console.
	 * <p>
	 * Log Entry packets of type EnterMethod increase the indentation
	 * and packets of type LeaveMethod decrease it.
	 *
	 * @return True if this formatter automatically intends log packets and
	 * false otherwise
	 */
	public boolean getIndent() {
		return this.fParser.getIndent();
	}

	/**
	 * Sets if this formatter automatically indents log packets like in
	 * the views of the SmartInspect Console.
	 * <p>
	 * Log Entry packets of type EnterMethod increase the indentation
	 * and packets of type LeaveMethod decrease it.
	 *
	 * @param indent Should be true if this formatter should automatically indent log
	 *               packets and false otherwise
	 */
	public void setIndent(boolean indent) {
		this.fParser.setIndent(indent);
	}
}
