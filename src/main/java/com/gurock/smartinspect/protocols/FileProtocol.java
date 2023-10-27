/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect.protocols;

import com.gurock.smartinspect.FileHelper;
import com.gurock.smartinspect.FileRotate;
import com.gurock.smartinspect.FileRotater;
import com.gurock.smartinspect.connections.ConnectionsBuilder;
import com.gurock.smartinspect.formatters.BinaryFormatter;
import com.gurock.smartinspect.formatters.Formatter;
import com.gurock.smartinspect.packets.Packet;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Date;

/**
 * The FileProtocol serves as a standard SmartInspect protocol for writing log packets to a log file.
 * This class serves as the base for all protocol classes that deal with log files. The default behavior
 * of using the binary log file format is compatible with the Console. This behavior can be modified in classes
 * that derive from this base class. For instance, creating plain text files can be done by TextProtocol class.
 * A number of options are provided by file protocol which includes log rotation (by size and date), encryption
 * and I/O buffers. For the complete list of available protocol options, one can refer to the isValidOption method.
 * <p>
 * Thread safety: The public members of this class are threadsafe.
 */
public class FileProtocol extends Protocol {
	private static final String HASH = "MD5";
	private static final String CIPHER_ALGO = "AES";
	private static final String CIPHER_TRANS = "AES/CBC/PKCS5Padding";
	private static final int KEY_SIZE = 16;
	private static final int BUFFER_SIZE = 0x2000;
	private static byte[] SILE = "SILE".getBytes();
	private static byte[] SILF = "SILF".getBytes();

	private int fIOBufferCounter;
	private long fFileSize;
	private OutputStream fStream;
	private Formatter fFormatter;
	private FileRotater fRotater;

	private int fIOBuffer;
	private boolean fEncrypt;
	private byte[] fKey;
	private FileRotate fRotate;
	private boolean fAppend;
	private String fFileName;
	private long fMaxSize;
	private int fMaxParts;

	/**
	 * Creates and initializes a new FileProtocol instance. For a
	 * list of available file protocol options, please refer to the
	 * isValidOption method.
	 */
	public FileProtocol() {
		this.fRotater = new FileRotater();
		loadOptions(); // Set default options
	}

	/**
	 * Overridden. Returns "file".
	 *
	 * @return Just "file". Derived classes can change this behavior by overriding this method.
	 */
	protected String getName() {
		return "file";
	}

	/**
	 * Returns the formatter for this log file protocol.
	 * The standard implementation of this method returns an instance
	 * of the BinaryFormatter class. Derived classes can change this
	 * behavior by overriding this method.
	 *
	 * @return The formatter for this log file protocol.
	 */
	protected Formatter getFormatter() {
		if (this.fFormatter == null) {
			this.fFormatter = new BinaryFormatter();
		}

		return this.fFormatter;
	}

	/**
	 * Returns the default filename for this log file protocol.
	 * <p>
	 * The standard implementation of this method returns the string
	 * "log.sil" here. Derived classes can change this behavior by
	 * overriding this method.
	 *
	 * @return The default filename for this log file protocol.
	 */
	protected String getDefaultFileName() {
		return "log.sil";
	}

	/**
	 * Intended to provide a wrapper stream for the underlying file stream.
	 * This method can be used by custom protocol implementers to wrap the underlying file stream into a filter stream.
	 * Such filter streams could include streams for encrypting or compressing log files, for example.
	 * By default, this method simply returns the passed stream argument.
	 *
	 * @param stream The underlying file stream.
	 * @return The wrapper stream.
	 */
	protected OutputStream getStream(OutputStream stream) {
		return stream;
	}

	/**
	 * Overridden. Validates if a protocol option is supported.
	 *
	 * <p>
	 * The following table lists all valid options, their default values
	 * and descriptions for the file protocol.
	 * <table border="1">
	 * <caption>File protocol options</caption>
	 * <tr>
	 *   <th>Valid Options</th>
	 *   <th>Default Value</th>
	 *   <th>Description</th>
	 * </tr>
	 *   <tr>
	 *     <td>append</td>
	 *     <td>false</td>
	 *     <td>
	 *         Specifies if new packets should be appended to the destination file
	 *         instead of overwriting the file first.
	 *     </td>
	 *   </tr>
	 *   <tr>
	 *     <td>buffer</td>
	 *     <td>0</td>
	 *     <td>
	 *         Specifies the I/O buffer size in
	 * 	       kilobytes. It is possible to
	 * 	       specify size units like this:
	 * 	       "1 MB". Supported units are "KB",
	 * 	       "MB" and "GB". A value of 0
	 * 	       disables this feature. Enabling
	 * 	       the I/O buffering greatly improves
	 * 	       the logging performance but has
	 * 	       the disadvantage that log packets
	 * 	       are temporarily stored in memory
	 * 	       and are not immediately written
	 * 	       to disk.
	 *     </td>
	 *   </tr>
	 *   <tr>
	 *     <td>encrypt</td>
	 *     <td>false</td>
	 *     <td>
	 *         Specifies if the resulting log
	 * 	       file should be encrypted. Note
	 * 	       that the 'append' option cannot
	 * 	       be used with encryption enabled.
	 * 	       If encryption is enabled the
	 * 	       'append' option has no effect.
	 *     </td>
	 *   </tr>
	 *   <tr>
	 *     <td>filename</td>
	 *     <td>[varies]</td>
	 *     <td>
	 *         Specifies the filename of the log.
	 *     </td>
	 *   </tr>
	 *   <tr>
	 *     <td>key</td>
	 *     <td>[empty]</td>
	 *     <td>
	 *         Specifies the secret encryption
	 * 	       key as string if the 'encrypt'
	 * 	       option is enabled.
	 *     </td>
	 *   </tr>
	 *   <tr>
	 *     <td>maxparts</td>
	 *     <td>[varies]</td>
	 *     <td>
	 *         Specifies the maximum amount of
	 * 	       log files at any given time when
	 * 	       log rotating is enabled or the
	 * 	       maxsize option is set. Specify
	 * 	       0 for no limit. See below for
	 * 	       information on the default
	 * 	       value for this option.
	 *     </td>
	 *   </tr>
	 *   <tr>
	 *     <td>maxsize</td>
	 *     <td>0</td>
	 *     <td>
	 *         Specifies the maximum size of a
	 *         log file in kilobytes. When this
	 *         size is reached, the current log
	 *         file is closed and a new file
	 *         is opened. The maximum amount
	 *         of log files can be set with
	 *         the maxparts option. It is
	 *         possible to specify size units
	 *         like this: "1 MB". Supported
	 *         units are "KB", "MB" and "GB".
	 *         A value of 0 disables this
	 *         feature.
	 *     </td>
	 *   </tr>
	 *   <tr>
	 *     <td>rotate</td>
	 *     <td>none</td>
	 *     <td>
	 *         Specifies the rotate mode for log
	 *         files. Please see below for a
	 *         list of available values. A
	 *         value of "none" disables this
	 *         feature. The maximum amount
	 *         of log files can be set with
	 *         the maxparts option.
	 *     </td>
	 *   </tr>
	 * </table>
	 *
	 * <p>
	 * When using the standard binary log file protocol ("file" in
	 * the {@code SmartInspect.setConnections}),
	 * the default filename is set to "log.sil". When using text log
	 * files ("text" in the {@code SmartInspect.setConnections}),
	 * the default filename is "log.txt".
	 * <p>
	 * Example usage:
	 *
	 * <pre>{@code
	 * SiAuto.si.setConnections("file()");
	 * SiAuto.si.setConnections("file(filename=\"log.sil\", append=true)");
	 * SiAuto.si.setConnections("file(filename=\"log.sil\")");
	 * SiAuto.si.setConnections("file(maxsize=\"16MB\", maxparts=5)");
	 * SiAuto.si.setConnections("file(rotate=weekly)");
	 * SiAuto.si.setConnections("file(encrypt=true, key=\"secret\")");
	 * }</pre>
	 *
	 * @param name The option name to validate.
	 * @return True if the option is supported and false otherwise.
	 */
	protected boolean isValidOption(String name) {
		return
				name.equals("append") ||
						name.equals("buffer") ||
						name.equals("encrypt") ||
						name.equals("filename") ||
						name.equals("key") ||
						name.equals("maxsize") ||
						name.equals("maxparts") ||
						name.equals("rotate") ||
						super.isValidOption(name);
	}

	/**
	 * Overridden. Fills a ConnectionsBuilder instance with the
	 * options currently used by this file protocol.
	 *
	 * @param builder The ConnectionsBuilder object to fill with the current options
	 *                of this protocol.
	 */
	protected void buildOptions(ConnectionsBuilder builder) {
		super.buildOptions(builder);
		builder.addOption("append", this.fAppend);
		builder.addOption("buffer", (int) this.fIOBuffer / 1024);
		builder.addOption("filename", this.fFileName);
		builder.addOption("maxsize", (int) this.fMaxSize / 1024);
		builder.addOption("maxparts", this.fMaxParts);
		builder.addOption("rotate", this.fRotate);

		/* Do not add encrypt options for security */
	}

	/**
	 * Overridden. Loads and inspects file specific options.
	 * <p>
	 * This method loads all relevant options and ensures their
	 * correctness. See isValidOption for a list of options which
	 * are recognized by the file protocol.
	 */
	protected void loadOptions() {
		super.loadOptions();

		this.fFileName = getStringOption("filename", getDefaultFileName());
		this.fAppend = getBooleanOption("append", false);

		this.fIOBuffer = (int) getSizeOption("buffer", 0);
		this.fMaxSize = getSizeOption("maxsize", 0);
		this.fRotate = getRotateOption("rotate", FileRotate.None);

		if (this.fMaxSize > 0 && this.fRotate == FileRotate.None) {
			/* Backwards compatibility */
			this.fMaxParts = getIntegerOption("maxparts", 2);
		} else {
			this.fMaxParts = getIntegerOption("maxparts", 0);
		}

		this.fKey = getBytesOption("key", KEY_SIZE, null);
		this.fEncrypt = getBooleanOption("encrypt", false);

		if (this.fEncrypt) {
			this.fAppend = false; /* Not applicable */
		}

		this.fRotater.setMode(this.fRotate);
	}

	/**
	 * Overridden. Opens the destination file.
	 *
	 * <p>This method tries to open the destination file, which can be
	 * specified by passing the "filename" option to the initialize
	 * method. For other valid options which might affect the behavior
	 * of this method, please see the isValidOption method.</p>
	 *
	 * @throws Exception if opening the destination file failed.
	 */
	protected void internalConnect() throws Exception {
		internalDoConnect(this.fAppend);
	}

	private void throwException(String message) throws ProtocolException {
		throw new ProtocolException(message);
	}

	private void internalBeforeConnect() throws Exception {
		/* Validate encryption key first */
		if (this.fEncrypt) {
			if (this.fKey == null) {
				throwException("No encryption key");
			} else {
				if (this.fKey.length != KEY_SIZE) {
					throwException("Invalid key size");
				}
			}
		}
	}

	private void internalDoConnect(boolean append) throws Exception {
		internalBeforeConnect();

		String fileName;

		if (isRotating()) {
			fileName = FileHelper.getFileName(this.fFileName, append);
		} else {
			fileName = this.fFileName;
		}

		/* Open the destination file */
		this.fStream = new FileOutputStream(fileName, append);
		this.fFileSize = new File(fileName).length();

		if (this.fEncrypt) {
			this.fStream = getCipher(this.fStream);
		}

		this.fStream = getStream(this.fStream);
		this.fFileSize = writeHeader(this.fStream, this.fFileSize);

		if (this.fIOBuffer > 0) {
			this.fStream = new BufferedOutputStream(this.fStream,
					this.fIOBuffer);
			this.fIOBufferCounter = 0;
		} else {
			this.fStream = new BufferedOutputStream(this.fStream, BUFFER_SIZE);
		}


		internalAfterConnect(fileName);
	}

	private void internalAfterConnect(String fileName) throws Exception {
		if (!isRotating()) {
			return; /* Nothing to do */
		}

		if (this.fRotate != FileRotate.None) {
			/* We need to initialize our FileRotater object with
			 * the creation time of the opened log file in order
			 * to be able to correctly rotate the log by date in
			 * internalWritePacket. */

			Date fileDate = FileHelper.getFileDate(
					this.fFileName, fileName);

			this.fRotater.initialize(fileDate);
		}

		if (this.fMaxParts == 0) /* Unlimited log files */ {
			return;
		}

		/* Ensure that we have at most 'maxParts' files */
		FileHelper.deleteFiles(this.fFileName, this.fMaxParts);
	}

	private boolean isRotating() {
		return this.fRotate != FileRotate.None || this.fMaxSize > 0;
	}

	private byte[] longToBytes(long n) {
		byte[] b = new byte[8];
		b[0] = (byte) (n & 0xff);
		b[1] = (byte) ((n >> 8) & 0xff);
		b[2] = (byte) ((n >> 16) & 0xff);
		b[3] = (byte) ((n >> 24) & 0xff);
		b[4] = (byte) ((n >> 32) & 0xff);
		b[5] = (byte) ((n >> 40) & 0xff);
		b[6] = (byte) ((n >> 48) & 0xff);
		b[7] = (byte) ((n >> 56) & 0xff);
		return b;
	}

	private byte[] getIVector() throws GeneralSecurityException {
		MessageDigest md5 = MessageDigest.getInstance(HASH);
		long timestamp = System.currentTimeMillis();
		return md5.digest(longToBytes(timestamp));
	}

	private OutputStream getCipher(OutputStream stream)
			throws IOException, GeneralSecurityException {
		if (this.fEncrypt) {
			byte[] iv = getIVector();

			/* Prepend the encryption header */
			stream.write(SILE, 0, SILE.length);
			stream.write(iv, 0, iv.length);
			stream.flush();

			/* Build and initialize the cipher */
			SecretKeySpec key = new SecretKeySpec(this.fKey, CIPHER_ALGO);
			Cipher cipher = Cipher.getInstance(CIPHER_TRANS);
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

			/* And then wrap the file stream */
			return new CipherOutputStream(stream, cipher);
		} else {
			return stream;
		}
	}

	/**
	 * Overridden. Intended to write the header of a log file.
	 * This default implementation of this method writes the standard
	 * binary protocol header to the supplied OutputStream instance.
	 * Derived classes may change this behavior by overriding this
	 * method.
	 *
	 * @param stream The stream to which the header should be written to.
	 * @param size   Specifies the current size of the supplied stream.
	 * @return The new size of the stream after writing the header. If no
	 * header is written, the supplied size argument is returned.
	 * @throws IOException io exception
	 */
	protected long writeHeader(OutputStream stream, long size)
			throws IOException {
		if (size == 0) {
			stream.write(SILF, 0, SILF.length);
			stream.flush();
			return SILF.length;
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
	 * @param stream The stream to which the footer should be written to.
	 */
	protected void writeFooter(OutputStream stream) {

	}

	private void rotate() throws Exception {
		internalDisconnect();
		internalDoConnect(false); /* Always create a new file */
	}

	/**
	 * Overridden. Writes a packet to the destination file.
	 * <p>
	 * If the "maxsize" option is set and the supplied packet would
	 * exceed the maximum size of the destination file, then the
	 * current log file is closed and a new file is opened.
	 * Additionally, if the "rotate" option is active, the log file
	 * is rotated if necessary. Please see the documentation of the
	 * isValidOption method for more information.
	 * <p>
	 *
	 * @param packet The packet to write.
	 * @throws Exception If writing the packet to the destination file fails.
	 */
	protected void internalWritePacket(Packet packet) throws Exception {
		Formatter formatter = getFormatter();
		int packetSize = formatter.compile(packet);

		if (this.fRotate != FileRotate.None) {
			if (this.fRotater.update(new Date())) {
				rotate();
			}
		}

		if (this.fMaxSize > 0) {
			this.fFileSize += packetSize;
			if (this.fFileSize > this.fMaxSize) {
				rotate();

				if (packetSize > this.fMaxSize) {
					return;
				}

				this.fFileSize += packetSize;
			}
		}

		formatter.write(this.fStream);

		if (this.fIOBuffer > 0) {
			this.fIOBufferCounter += packetSize;
			if (this.fIOBufferCounter > this.fIOBuffer) {
				this.fIOBufferCounter = 0;
				this.fStream.flush();
			}
		} else {
			this.fStream.flush();
		}
	}

	/**
	 * Overridden. Closes the destination file.
	 *
	 * <p>This method closes the underlying file handle if previously
	 * created and disposes any supplemental objects.</p>
	 *
	 * @throws Exception if closing the destination file fails.
	 */
	protected void internalDisconnect() throws Exception {
		if (this.fStream != null) {
			try {
				writeFooter(this.fStream);
			} finally {
				try {
					this.fStream.close();
				} finally {
					this.fStream = null;
				}
			}
		}
	}
}
