//
// <!-- Copyright (C) 2003-2011 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Represents the Control Command packet type which is used for
//   administrative tasks like resetting or clearing the Console.
// </summary>
// <remarks>
//   A Control Command is used for several administrative Console tasks.
//   Among other things, this packet type allows you to
//   <link Session.clearAll, reset the Console>.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe. However, instances
//   of this class will normally only be used in the context of a single
//   thread.
// </threadsafety>

public final class ControlCommand extends Packet
{
	private byte[] fData;
	private ControlCommandType fControlCommandType;
	private static final int HEADER_SIZE = 8;

	// <summary>
	//   Overloaded. Creates and initializes a ControlCommand instance.
	// </summary>

	public ControlCommand()
	{

	}

	// <summary>
	//   Overloaded. Creates and initializes a ControlCommand instance
	//   with a custom control command type.
	// </summary>
	// <param name="controlCommandType">
	//   The type of the new Control Command describes the way the
	//   Console interprets this packet. Please see the ControlCommandType
	//   type for more information. Not allowed to be null.
	// </param>
	// <remarks>
	//   If the controlCommandType argument is a null reference a
	//   NullPointerException will be thrown.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The controlCommandType argument is null.
	// </table>
	// </exception>

	public ControlCommand(ControlCommandType controlCommandType)
	{
		setControlCommandType(controlCommandType);
	}

	// <summary>
	//   Overridden. Returns the total occupied memory size of this
	//   Control Command packet.
	// </summary>
	// <returns>
	//   The total occupied memory size of this Control Command.
	// </returns>
	// <remarks>
	//   The total occupied memory size of this Control Command is the
	//   size of memory occupied the optional <link getData, data block>
	//   and any internal data structures of this Control Command.
	// </remarks>

	public int getSize()
	{
		int result = HEADER_SIZE;

		if (this.fData != null)
		{
			result += this.fData.length;
		}

		return result;
	}

	// <summary>
	//   Overridden. Returns PacketType.ControlCommand.
	// </summary>
	// <returns>
	//   Just PacketType.ControlCommand. For a complete list of available
	//   packet types, please have a look at the documentation of the
	//   PacketType type.
	// </returns>	

	public PacketType getPacketType()
	{
		return PacketType.ControlCommand;
	}

	// <summary>
	//   Returns the type of this Control Command.
	// </summary>
	// <returns>
	//   The type of this Control Command packet.
	// </returns>
	// <remarks>
	//   The type of the Control Command describes the way the Console
	//   interprets this packet. Please see the ControlCommandType type
	//   for more information.
	// </remarks>

	public ControlCommandType getControlCommandType()
	{
		return this.fControlCommandType;
	}

	// <summary>
	//   Sets the type of this Control Command.
	// </summary>
	// <param name="controlCommandType">
	//   The new type of this Control Command packet. Not allowed to be
	//   null.
	// </param>
	// <remarks>
	//   The type of the Control Command describes the way the Console
	//   interprets this packet. Please see the ControlCommandType type
	//   for more information.
	//
	//   If the controlCommandType argument is a null reference a
	//   NullPointerException will be thrown.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The controlCommandType argument is null.
	// </table>
	// </exception>

	public void setControlCommandType(ControlCommandType controlCommandType)
	{
		if (controlCommandType == null)
		{
			throw new NullPointerException("controlCommandType");
		}
		else 
		{
			this.fControlCommandType = controlCommandType;
		}
	}

	// <summary>
	//   Returns the optional data block of the Control Command.
	// </summary>
	// <returns>
	//   The optional data block of the Control Command or null if this
	//   Control Command does not contain additional data.
	// </returns>
	// <remarks>
	//   This method can return null if this Control Command does not
	//   contain additional data.
	//
	//   <b>Important:</b> Treat the returned array as read-only. This
	//   means, modifying this array in any way is not supported.
	// </remarks>

	public byte[] getData()
	{
		return this.fData;
	}

	// <summary>
	//   Sets the optional data block of the Control Command.
	// </summary>
	// <param name="data">
	//   The new data block of this Control Command. Can be null.
	// </param>
	// <remarks>
	//   Because of the fact that the data block of a Control Command
	//   is optional, it is allowed to pass a null reference.
	//
	//   <b>Important:</b> Treat the passed array as read-only. This
	//   means, modifying this array in any way after passing it to this
	//   method is not supported.
	// </remarks>

	public void setData(byte[] data)
	{
		this.fData = data;
	}
}
