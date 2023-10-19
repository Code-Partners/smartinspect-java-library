/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Responsible for formatting and writing a packet.
 *
 * <p>This abstract class defines several methods which are intended to
 * preprocess a packet and subsequently write it to a stream. The
 * process of preprocessing (or compiling) and writing a packet can
 * either be executed with a single step by calling the format method
 * or with two steps by calls to compile and write.</p>
 *
 * <p>Note: This class and subclasses thereof are not guaranteed to be
 * threadsafe.</p>
 */
public abstract class Formatter {
	/**
	 * Preprocesses (or compiles) a packet and returns the required size for the compiled
	 * result.
	 * To write a previously compiled packet, call the write method.
	 * Derived classes are intended to compile the supplied packet
	 * and return the required size for the compiled result.
	 *
	 * @param packet The packet to compile
	 * @return The size for the compiled result
	 * @throws IOException io exception
	 */
	public abstract int compile(Packet packet) throws IOException;

	/**
	 * Writes a previously compiled packet to the supplied stream.
	 * <p>
	 * This method is intended to write a previously compiled packet
	 * (see compile) to the supplied stream object. If the return
	 * value of the compile method was 0, nothing is written.
	 *
	 * @param stream The stream to write the packet to.
	 * @throws IOException If an I/O error occurred while trying to write the
	 *                     compiled packet.
	 */
	public abstract void write(OutputStream stream) throws IOException;

	/**
	 * Compiles a packet and writes it to a stream.
	 *
	 * <p>This non-abstract method simply calls the compile method with
	 * the supplied packet object and then the write method with
	 * the supplied stream object.</p>
	 *
	 * @param packet The packet to compile.
	 * @param stream The stream to write the packet to.
	 * @throws IOException If an I/O error occurred while trying
	 *                     to write the compiled packet.
	 */
	public void format(Packet packet, OutputStream stream)
			throws IOException {
		compile(packet);
		write(stream);
	}
}
