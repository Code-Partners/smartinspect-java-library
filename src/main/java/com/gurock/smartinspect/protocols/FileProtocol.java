//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

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

// <summary>
//   The standard SmartInspect protocol for writing log packets to a log
//   file.
// </summary>
// <remarks>
//   FileProtocol is the base class for all protocol classes which
//   deal with log files. By default, it uses the binary log file format
//   which is compatible to the Console. Derived classes can change this
//   behavior. For example, for a simple protocol which is capable of
//   creating plain text files, see the TextProtocol class.
//
//   The file protocol supports a variety of options, such as log
//   rotation (by size and date), encryption and I/O buffers. For a
//   complete list of available protocol options, please have a look at
//   the isValidOption method.
// </remarks>
// <threadsafety>
//   The public members of this class are threadsafe.
// </threadsafety>

public class FileProtocol extends Protocol
{
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

	// <summary>
	//   Creates and initializes a new FileProtocol instance. For a
	//   list of available file protocol options, please refer to the
	//   isValidOption method.
	// </summary>

	public FileProtocol()
	{
		this.fRotater = new FileRotater();
		loadOptions(); // Set default options
	}

	// <summary>
	//   Overridden. Returns "file".
	// </summary>
	// <returns>
	//   Just "file". Derived classes can change this behavior by
	//   overriding this method.
	// </returns>

	protected String getName()
	{
		return "file";
	}

	// <summary>
	//  Returns the formatter for this log file protocol.
	// </summary>
	// <returns>The formatter for this log file protocol.</returns>
	// <remarks>
	//   The standard implementation of this method returns an instance
	//   of the BinaryFormatter class. Derived classes can change this
	//   behavior by overriding this method.
	// </remarks>

	protected Formatter getFormatter()
	{
		if (this.fFormatter == null)
		{
			this.fFormatter = new BinaryFormatter();
		}

		return this.fFormatter;
	}

	// <summary>
	//   Returns the default filename for this log file protocol.
	// </summary>
	// <returns>The default filename for this log file protocol.</returns>
	// <remarks>
	//   The standard implementation of this method returns the string
	//   "log.sil" here. Derived classes can change this behavior by
	//   overriding this method.
	// </remarks>

	protected String getDefaultFileName()
	{
		return "log.sil";
	}

	// <summary>
	//   Intended to provide a wrapper stream for the underlying
	//   file stream.
	// </summary>
	// <param name="stream">The underlying file stream.</param>
	// <returns>The wrapper stream.</returns>
	// <remarks>
	//   This method can be used by custom protocol implementers
	//   to wrap the underlying file stream into a filter stream.
	//   Such filter streams could include streams for encrypting
	//   or compressing log files, for example.
	//
	//   By default, this method simply returns the passed stream
	//   argument.
	// </remarks>
	
	protected OutputStream getStream(OutputStream stream)
	{
		return stream;
	}
	
	// <summary>
	//   Overridden. Validates if a protocol option is supported.
	// </summary>
	// <param name="name">The option name to validate.</param>
	// <returns>
	//   True if the option is supported and false otherwise.
	// </returns>
	// <remarks>
	//   The following table lists all valid options, their default values
	//   and descriptions for the file protocol.
	//
	//   <table>
	//   Valid Options  Default Value  Description
	//   -              -              -
	//   append         false          Specifies if new packets should be
	//                                   appended to the destination file
	//                                   instead of overwriting the file
	//                                   first.
	//
	//   buffer         0              Specifies the I/O buffer size in
	//                                   kilobytes. It is possible to
	//                                   specify size units like this:
	//                                   "1 MB". Supported units are "KB",
	//                                   "MB" and "GB". A value of 0
	//                                   disables this feature. Enabling
	//                                   the I/O buffering greatly improves
	//                                   the logging performance but has
	//                                   the disadvantage that log packets
	//                                   are temporarily stored in memory
	//                                   and are not immediately written
	//                                   to disk.
	//
	//   encrypt        false          Specifies if the resulting log
	//                                   file should be encrypted. Note
	//                                   that the 'append' option cannot
	//                                   be used with encryption enabled.
	//                                   If encryption is enabled the
	//                                   'append' option has no effect.
	// 
	//   filename       [varies]       Specifies the filename of the log.
	// 
	//   key            [empty]        Specifies the secret encryption
	//                                   key as string if the 'encrypt'
	//                                   option is enabled.
	//
	//   maxparts       [varies]       Specifies the maximum amount of
	//                                   log files at any given time when
	//                                   log rotating is enabled or the
	//                                   maxsize option is set. Specify
	//                                   0 for no limit. See below for
	//                                   information on the default
	//                                   value for this option.
	// 
	//   maxsize        0              Specifies the maximum size of a
	//                                   log file in kilobytes. When this
	//                                   size is reached, the current log
	//                                   file is closed and a new file
	//                                   is opened. The maximum amount
	//                                   of log files can be set with
	//                                   the maxparts option. It is
	//                                   possible to specify size units
	//                                   like this: "1 MB". Supported
	//                                   units are "KB", "MB" and "GB".
	//                                   A value of 0 disables this
	//                                   feature.
	//
	//   rotate         none           Specifies the rotate mode for log
	//                                   files. Please see below for a
	//                                   list of available values. A
	//                                   value of "none" disables this
	//                                   feature. The maximum amount
	//                                   of log files can be set with
	//                                   the maxparts option.
	//   </table>
	//
	//   When using the standard binary log file protocol ("file" in
	//   the <link SmartInspect.setConnections, connections string>),
	//   the default filename is set to "log.sil". When using text log
	//   files ("text" in the <link SmartInspect.setConnections,
	//   connections string>), the default filename is "log.txt".
	//
	//   The append option specifies if new packets should be appended
	//   to the destination file instead of overwriting the file. The
	//   default value of this option is "false".
	//
	//   The rotate option specifies the date log rotate mode for this
	//   file protocol. When this option is used, the filename of the
	//   resulting log consists of the value of the filename option and
	//   an appended time stamp (the used time stamp format thereby is
	//   "yyyy-MM-dd-HH-mm-ss"). To avoid problems with daylight saving
	//   time or time zone changes, the time stamp is always in UTC
	//   (Coordinated Universal Time). The following table lists the
	//   available rotate modes together with a short description.
	//
	//   <table>
	//   Rotate Mode    Description
	//   -              -
	//   None           Rotating is disabled
	//   Hourly         Rotate hourly
	//   Daily          Rotate daily
	//   Weekly         Rotate weekly
	//   Monthly        Rotate monthly
	//   </table>
	//
	//   As example, if you specify "log.sil" as value for the filename
	//   option and use the Daily rotate mode, the log file is rotated
	//   daily and always has a name of log-yyyy-MM-dd-HH-mm-ss.sil. In
	//   addition to, or instead of, rotating log files by date, you
	//   can also let the file protocol rotate log files by size. To
	//   enable this feature, set the maxsize option to the desired
	//   maximum size. Similar to rotating by date, the resulting log
	//   files include a time stamp. Note that starting with
	//   SmartInspect 3.0, it is supported to combine the maxsize and
	//   rotate options (i.e. use both options at the same time).
	//   
	//   To control the maximum amount of created log files for the
	//   rotate and/or maxsize options, you can use the maxparts option.
	//   The default value for maxparts is 2 when used with the maxsize
	//   option, 0 when used with rotate and 0 when both options,
	//   maxsize and rotate, are used.
	//
	//   SmartInspect log files can be automatically encrypted by
	//   enabling the 'encrypt' option. The used cipher is Rijndael
	//   (AES) with a key size of 128 bit. The specified
	//   key is automatically shortened or padded (with zeros) to a
	//   key size of 128 bit. Note that the 'append' option cannot be
	//   used in combination with encryption enabled. If encryption
	//   is enabled the 'append' option has no effect.
	//
	//   For further options which affect the behavior of this protocol,
	//   please have a look at the documentation of the
	//   <link Protocol.isValidOption, isValidOption> method of the
	//   parent class.
	// </remarks>
	// <example>
	// <code>
	// SiAuto.si.setConnections("file()");
	// SiAuto.si.setConnections("file(filename=\\"log.sil\\", append=true)");
	// SiAuto.si.setConnections("file(filename=\\"log.sil\\")");
	// SiAuto.si.setConnections("file(maxsize=\\"16MB\\", maxparts=5)");
	// SiAuto.si.setConnections("file(rotate=weekly)");
	// SiAuto.si.setConnections("file(encrypt=true, key=\\"secret\\")");	
	// </code>
	// </example>

	protected boolean isValidOption(String name)
	{
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

	// <summary>
	//   Overridden. Fills a ConnectionsBuilder instance with the
	//   options currently used by this file protocol.
	// </summary>
	// <param name="builder">
	//   The ConnectionsBuilder object to fill with the current options
	//   of this protocol.
	// </param>

	protected void buildOptions(ConnectionsBuilder builder)
	{
		super.buildOptions(builder);
		builder.addOption("append", this.fAppend);
		builder.addOption("buffer", (int) this.fIOBuffer / 1024);
		builder.addOption("filename", this.fFileName);
		builder.addOption("maxsize", (int) this.fMaxSize / 1024);
		builder.addOption("maxparts", this.fMaxParts);
		builder.addOption("rotate", this.fRotate);
		
		/* Do not add encrypt options for security */
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

		this.fFileName = getStringOption("filename", getDefaultFileName());
		this.fAppend = getBooleanOption("append", false);
		
		this.fIOBuffer = (int) getSizeOption("buffer", 0);
		this.fMaxSize = getSizeOption("maxsize", 0);
		this.fRotate = getRotateOption("rotate", FileRotate.None);

		if (this.fMaxSize > 0 && this.fRotate == FileRotate.None)
		{
			/* Backwards compatibility */
			this.fMaxParts = getIntegerOption("maxparts", 2);
		}
		else 
		{
			this.fMaxParts = getIntegerOption("maxparts", 0);
		}		
		
		this.fKey = getBytesOption("key", KEY_SIZE, null);
		this.fEncrypt = getBooleanOption("encrypt", false);

		if (this.fEncrypt)
		{
			this.fAppend = false; /* Not applicable */
		}

		this.fRotater.setMode(this.fRotate);
	}

	// <summary>
	//   Overridden. Opens the destination file.
	// </summary>
	// <remarks>
	//   This method tries to open the destination file, which can be
	//   specified by passing the "filename" option to the initialize
	//   method. For other valid options which might affect the behavior
	//   of this method, please see the isValidOption method.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type       Condition
	//   -                    -
	//   Exception            Opening the destination file failed.
	// </table>
	// </exception>

	protected void internalConnect() throws Exception
	{
		internalDoConnect(this.fAppend);
	}
	
	private void throwException(String message) throws ProtocolException
	{
		throw new ProtocolException(message);
	}

	private void internalBeforeConnect() throws Exception
	{
		/* Validate encryption key first */
		if (this.fEncrypt)
		{
			if (this.fKey == null)
			{
				throwException("No encryption key");
			}
			else
			{
				if (this.fKey.length != KEY_SIZE)
				{
					throwException("Invalid key size");
				}
			}
		}
	}
	
	private void internalDoConnect(boolean append) throws Exception
	{
		internalBeforeConnect();
		
		String fileName;
		
		if (isRotating())
		{
			fileName = FileHelper.getFileName(this.fFileName, append);
		}
		else
		{
			fileName = this.fFileName;
		}

		/* Open the destination file */
		this.fStream = new FileOutputStream(fileName, append);		
		this.fFileSize = new File(fileName).length();
		
		if (this.fEncrypt)
		{
			this.fStream = getCipher(this.fStream);
		}
		
		this.fStream = getStream(this.fStream);
		this.fFileSize = writeHeader(this.fStream, this.fFileSize);

		if (this.fIOBuffer > 0)
		{
			this.fStream = new BufferedOutputStream(this.fStream, 
				this.fIOBuffer);
			this.fIOBufferCounter = 0;
		}
		else 
		{
			this.fStream = new BufferedOutputStream(this.fStream, BUFFER_SIZE);
		}
		
		
		internalAfterConnect(fileName);
	}
	
	private void internalAfterConnect(String fileName) throws Exception
	{
		if (!isRotating())
		{
			return; /* Nothing to do */
		}
		
		if (this.fRotate != FileRotate.None)
		{
			/* We need to initialize our FileRotater object with
			 * the creation time of the opened log file in order
			 * to be able to correctly rotate the log by date in
			 * internalWritePacket. */
			
			Date fileDate = FileHelper.getFileDate(
				this.fFileName, fileName);
			
			this.fRotater.initialize(fileDate);
		}
		
		if (this.fMaxParts == 0) /* Unlimited log files */
		{
			return;
		}
		
		/* Ensure that we have at most 'maxParts' files */
		FileHelper.deleteFiles(this.fFileName, this.fMaxParts);
	}

	private boolean isRotating()
	{
		return this.fRotate != FileRotate.None || this.fMaxSize > 0;
	}
		
	private byte[] longToBytes(long n)
	{
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
	
	private byte[] getIVector() throws GeneralSecurityException
	{
		MessageDigest md5 = MessageDigest.getInstance(HASH);
		long timestamp = System.currentTimeMillis();
		return md5.digest(longToBytes(timestamp));
	}
	
	private OutputStream getCipher(OutputStream stream) 
		throws IOException, GeneralSecurityException
	{
		if (this.fEncrypt)
		{
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
		}
		else 
		{
			return stream;
		}
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
	//   This default implementation of this method writes the standard
	//   binary protocol header to the supplied OutputStream instance.
	//   Derived classes may change this behavior by overriding this
	//   method.
	// </remarks>

	protected long writeHeader(OutputStream stream, long size)
		throws IOException
	{
		if (size == 0)
		{
			stream.write(SILF, 0, SILF.length);
			stream.flush();
			return SILF.length;
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

	private void rotate() throws Exception
	{
		internalDisconnect();
		internalDoConnect(false); /* Always create a new file */
	}
	
	// <summary>
	//   Overridden. Writes a packet to the destination file.
	// </summary>
	// <param name="packet">The packet to write.</param>
	// <remarks>
	//   If the "maxsize" option is set and the supplied packet would
	//   exceed the maximum size of the destination file, then the
	//   current log file is closed and a new file is opened.
	//   Additionally, if the "rotate" option is active, the log file
	//   is rotated if necessary. Please see the documentation of the
	//   isValidOption method for more information.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type       Condition
	//   -                    -
	//   Exception            Writing the packet to the destination
	//                          file failed.
	// </table>
	// </exception>

	protected void internalWritePacket(Packet packet) throws Exception
	{
		Formatter formatter = getFormatter();
		int packetSize = formatter.compile(packet);

		if (this.fRotate != FileRotate.None)
		{
			if (this.fRotater.update(new Date()))
			{
				rotate();
			}
		}
		
		if (this.fMaxSize > 0)
		{
			this.fFileSize += packetSize;
			if (this.fFileSize > this.fMaxSize)
			{
				rotate();

				if (packetSize > this.fMaxSize)
				{
					return;
				}

				this.fFileSize += packetSize;
			}
		}

		formatter.write(this.fStream);
		
		if (this.fIOBuffer > 0)
		{
			this.fIOBufferCounter += packetSize;
			if (this.fIOBufferCounter > this.fIOBuffer)
			{
				this.fIOBufferCounter = 0;
				this.fStream.flush();
			}			
		}
		else 
		{
			this.fStream.flush();
		}
	}

	// <summary>
	//   Overridden. Closes the destination file.
	// </summary>
	// <remarks>
	//   This method closes the underlying file handle if previously
	//   created and disposes any supplemental objects.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type       Condition
	//   -                    -
	//   Exception            Closing the destination file failed.
	// </table>
	// </exception>

	protected void internalDisconnect() throws Exception
	{
		if (this.fStream != null)
		{
			try 
			{
				writeFooter(this.fStream);
			}
			finally
			{
				try 
				{
					this.fStream.close();
				}
				finally 
				{
					this.fStream = null;
				}
			}
		}
	}
}
