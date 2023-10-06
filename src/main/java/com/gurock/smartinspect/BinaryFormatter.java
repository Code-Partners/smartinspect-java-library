/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Responsible for formatting and writing a packet in the standard
 * SmartInspect binary format.
 * <p>
 * This class formats and writes a packet in the standard binary format
 * which can be read by the SmartInspect Console. The compile method
 * preprocesses a packet and computes the required size of the packet.
 * The write method writes the preprocessed packet to the supplied
 * stream.
 * <p>
 * This class is not guaranteed to be threadsafe.
 */
public class BinaryFormatter extends Formatter
{
	private static final long MICROSECONDS_PER_DAY = 86400000000L;
	private static final int DAY_OFFSET = 25569;
	private static final int MAX_STREAM_CAPACITY = 1 * 1024 * 1024;

	private int fSize;
	private byte[] fBuffer;
	private ByteArrayOutputStream fStream;
	private Packet fPacket;

	/**
	 * Creates and initializes a BinaryFormatter instance.
	 */
	public BinaryFormatter()
	{
		this.fBuffer = new byte[8];
		this.fStream = new ByteArrayOutputStream();
	}
	
	private void resetStream()
	{
		if (this.fSize > MAX_STREAM_CAPACITY)
		{
			// Reset the stream capacity if the previous packet
			// was very big. This ensures that the amount of memory
			// can shrink again after a big packet has been sent.
			this.fStream = new ByteArrayOutputStream();
		}
		else 
		{
			// Only reset the position. This ensures a very good
			// performance since no reallocations are necessary.
			this.fStream.reset();
		}
	}

	/**
	 * Overridden. Preprocesses (or compiles) a packet and returns the
	 * required size for the compiled result.
	 * <p>
	 * This method preprocesses the supplied packet and computes the
	 * required binary format size. To write this compiled packet,
	 * call the write method.
	 * @param packet The packet to compile
	 * @return The size for the compiled result
     */
	public int compile(Packet packet) throws IOException
	{
		resetStream();
		this.fPacket = packet;
		
		PacketType type = packet.getPacketType();
		
		if (type == PacketType.LogEntry)
		{
			compileLogEntry();
		}
		else if (type == PacketType.LogHeader)
		{
			compileLogHeader();
		}
		else if (type == PacketType.Watch)
		{
			compileWatch();
		}
		else if (type == PacketType.ControlCommand)
		{
			compileControlCommand();
		}
		else if (type == PacketType.ProcessFlow)
		{
			compileProcessFlow();
		}
	
		this.fSize = this.fStream.size();
		return this.fSize + Packet.PACKET_HEADER;
	}
	
	private final void writeTimestamp(long value) throws IOException
	{
		long us;
		double timestamp;
	
		// Calculate current Timestamp:
		// A Timestamp is represented by a double. The integral
		// part of the value is the number of days that have
		// passed since 12/30/1899. The fractional part of the
		// value is the fraction of a 24 hour day that has elapsed.
	
		us = value;
		// us += TimeZone.getDefault().getOffset(value / 1000) * 1000;
		timestamp = us / MICROSECONDS_PER_DAY + DAY_OFFSET;
		timestamp += (double) (us % MICROSECONDS_PER_DAY) /
			MICROSECONDS_PER_DAY;
	
		writeDouble(timestamp);
	}
	
	private final void writeLength(byte[] value) throws IOException
	{
		if (value != null)
		{
			writeInt(value.length);
		}
		else 
		{
			writeInt(0);
		}
	}
	
	private final void writeShort(short value) throws IOException
	{
		writeShort(this.fStream, value);
	}
	
	private final void writeShort(OutputStream stream, short value)
		throws IOException
	{
		this.fBuffer[0] = (byte) value;
		this.fBuffer[1] = (byte) (value >> 8);
		stream.write(this.fBuffer, 0, 2);
	}
	
	private final void writeInt(int value) throws IOException
	{
		writeInt(this.fStream, value);
	}
	
	private final void writeInt(OutputStream stream, int value)
		throws IOException
	{
		this.fBuffer[0] = (byte) value;
		this.fBuffer[1] = (byte) (value >> 8);
		this.fBuffer[2] = (byte) (value >> 16);
		this.fBuffer[3] = (byte) (value >> 24);
		stream.write(this.fBuffer, 0, 4);
	}
	
	private final void writeLong(long value) throws IOException
	{
		this.fBuffer[0] = (byte) value;
		this.fBuffer[1] = (byte) (value >> 8);
		this.fBuffer[2] = (byte) (value >> 16);
		this.fBuffer[3] = (byte) (value >> 24);
		this.fBuffer[4] = (byte) (value >> 32);
		this.fBuffer[5] = (byte) (value >> 40);
		this.fBuffer[6] = (byte) (value >> 48);
		this.fBuffer[7] = (byte) (value >> 56);
		this.fStream.write(this.fBuffer, 0, 8);
	}
	
	private final void writeDouble(double value) throws IOException
	{
		writeLong(Double.doubleToLongBits(value));
	}

	private final void writeEnum(Enum value) throws IOException
	{
		if (value != null)
		{
			writeInt(value.getIntValue());
		}
		else 
		{
			writeInt(-1);
		}
	}
	
	private void writeColor(Color value) throws IOException
	{
		int color;

		if (value == null)
		{
			color = (int) 0xff000000 | 5;
		}
		else
		{
			color = value.getRed() | value.getGreen() << 8 |
				value.getBlue() << 16 | value.getAlpha() << 24;
		}

		writeInt(color);
	}
	
	private void writeData(byte[] value) throws IOException
	{
		if (value != null)
		{
			this.fStream.write(value, 0, value.length);
		}
	}
	
	private byte[] encodeString(String value)
	{
		byte[] result = null;
		
		if (value != null)
		{
			try
			{
				result = value.getBytes("UTF-8");
			}
			catch (UnsupportedEncodingException e)
			{
			}
		}

		return result;
	}
	
	private void compileControlCommand() throws IOException
	{
		ControlCommand controlCommand = 
			(ControlCommand) this.fPacket;
		
		writeEnum(controlCommand.getControlCommandType());
		writeLength(controlCommand.getData());
		writeData(controlCommand.getData());
	}
	
	private void compileLogHeader() throws IOException
	{
		LogHeader logHeader = (LogHeader) this.fPacket;
	
		byte[] content = encodeString(logHeader.getContent());
		writeLength(content);
		writeData(content);
	}

	private void compileLogEntry() throws IOException
	{
		LogEntry logEntry = (LogEntry) this.fPacket;
	
		byte[] appName = encodeString(logEntry.getAppName());
		byte[] sessionName = encodeString(logEntry.getSessionName());
		byte[] title = encodeString(logEntry.getTitle());
		byte[] hostName = encodeString(logEntry.getHostName());
		
		writeEnum(logEntry.getLogEntryType());
		writeEnum(logEntry.getViewerId());
		writeLength(appName);
		writeLength(sessionName);
		writeLength(title);
		writeLength(hostName);
		writeLength(logEntry.getData());
		writeInt(logEntry.getProcessId());
		writeInt(logEntry.getThreadId());
		writeTimestamp(logEntry.getTimestamp());
		writeColor(logEntry.getColor());

		writeData(appName);
		writeData(sessionName);
		writeData(title);
		writeData(hostName);
		writeData(logEntry.getData());
	}
	
	private void compileProcessFlow() throws IOException
	{
		ProcessFlow processFlow = (ProcessFlow) this.fPacket;
		
		byte[] title = encodeString(processFlow.getTitle());
		byte[] hostName = encodeString(processFlow.getHostName());

		writeEnum(processFlow.getProcessFlowType());
		writeLength(title);
		writeLength(hostName);
		writeInt(processFlow.getProcessId());
		writeInt(processFlow.getThreadId());
		writeTimestamp(processFlow.getTimestamp());
		
		writeData(title);
		writeData(hostName);
	}
	
	private void compileWatch() throws IOException
	{
		Watch watch = (Watch) this.fPacket;
		
		byte[] name = encodeString(watch.getName());
		byte[] value = encodeString(watch.getValue());

		writeLength(name);
		writeLength(value);
		writeEnum(watch.getWatchType());
		writeTimestamp(watch.getTimestamp());

		writeData(name);
		writeData(value);
	}

	/**
	 * Overridden. Writes a previously compiled packet to the supplied
	 * stream.
	 * <p>
	 * This method writes the previously compiled packet (see Compile)
	 * to the supplied stream object. If the return value of the
	 * compile method was 0, nothing is written.
	 * @param stream The stream to write the packet to
	 * @throws IOException An I/O error occurred while trying
	 * to write the compiled packet.
	 */
	public void write(OutputStream stream) throws IOException
	{
		if (this.fSize > 0)
		{
			writeShort(stream, 
				(short) this.fPacket.getPacketType().getIntValue());
			writeInt(stream, this.fSize);
			this.fStream.writeTo(stream);
		}
	}
}
