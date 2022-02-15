//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect.protocols;

import java.util.HashMap;
import java.util.Iterator;

// <summary>
//   Manages connection variables.
// </summary>
// <remarks>
//   This class manages a list of connection variables. Connection
//   variables are placeholders for strings in the
//   <link SmartInspect.setConnections, connections string> of the
//   SmartInspect class. Please see SmartInspect.setVariable for more
//   information.
// </remarks>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

public class ProtocolVariables 
{
	private class KeyValue
	{
		private String fKey;
		private String fValue;

		public KeyValue(String key, String value)
		{
			this.fKey = key;
			this.fValue = value;
		}
		
		public String getKey()
		{
			return this.fKey;
		}
		
		public String getValue()
		{
			return this.fValue;
		}
	}
	
	private HashMap fItems;
	private Object fLock;
	
	// <summary>
	//   Creates and initializes a ProtocolVariables instance.
	// </summary>
	
	public ProtocolVariables()
	{
		this.fItems = new HashMap();
		this.fLock = new Object();
	}
	
	// <summary>
	//   Adds or updates an element with a specified key and value to
	//   the set of connection variables.
	// </summary>
	// <param name="key">The key of the element.</param>
	// <param name="value">The value of the element.</param>
	// <remarks>
	//   This method adds a new element with a given key and value to
	//   the set of connection variables. If an element for the given
	//   key already exists, the original element's value is updated.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key or value argument is null.
	// </table>
	// </exception>
	
	public void put(String key, String value)
	{
		if (key == null)
		{
			throw new NullPointerException("key parameter is null");			
		}
		
		if (value == null)
		{
			throw new NullPointerException("value parameter is null");			
		}

		KeyValue pair = new KeyValue(key, value);

		synchronized (this.fLock) 
		{
			this.fItems.put(makeKey(key), pair);
		}
	}

	// <summary>
	//   Adds a new element with a specified key and value to the
	//   set of connection variables.
	// </summary>
	// <param name="key">The key of the element.</param>
	// <param name="value">The value of the element.</param>
	// <remarks>
	//   This method adds a new element with a given key and value
	//   to the set of connection variables. If an element for the
	//   given key already exists, the original element's value is
	//   not updated.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key or value argument is null.
	// </table>
	// </exception>
	
	public void add(String key, String value)
	{
		if (key == null)
		{
			throw new NullPointerException("key parameter is null");			
		}
		
		synchronized(this.fLock)
		{
			if (!this.fItems.containsKey(makeKey(key)))
			{
				put(key, value);
			}
		}
	}
	
	// <summary>
	//   Removes an existing element with a given key from this set
	//   of connection variables.
	// </summary>
	// <param name="key">The key of the element to remove.</param>
	// <remarks>
	//  This method removes the element with the given key from the
	//  internal set of connection variables. Nothing happens if
	//  no element with the given key can be found.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key argument is null.
	// </table>
	// </exception>
	
	public void remove(String key)
	{
		if (key == null)
		{
			throw new NullPointerException("key parameter is null");			
		}

		synchronized (this.fLock)
		{
			this.fItems.remove(makeKey(key));
		}
	}
	
	// <summary>
	//   Tests if the collection contains a value for a given key. 
	// </summary>
	// <param name="key">The key to test for.</param>
	// <returns>
	//   True if a value exists for the given key and false otherwise.
	// </returns>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key argument is null.
	// </table>
	// </exception>
	
	public boolean contains(String key)
	{
		if (key == null)
		{
			throw new NullPointerException("key parameter is null");			
		}

		synchronized (this.fLock) 
		{
			return this.fItems.containsKey(makeKey(key));
		}
	}
	
	// <summary>
	//   Removes all key/value pairs of the collection.
	// </summary>
	
	public void clear()
	{
		synchronized (this.fLock)
		{
			this.fItems.clear();
		}
	}
	
	// <summary>
	//   Returns the number of key/value pairs of this collection.	
	// </summary>
	// <returns>
	//   The number of key/value pairs of this collection.	
	// </returns>
	
	public int getCount()
	{
		synchronized (this.fLock)
		{
			return this.fItems.size();
		}
	}
	
	// <summary>
	//   Expands and returns a connections string.
	// </summary>
	// <param name="connections">
	//	  The connections string to expand and return.
	// </param>
	// <returns>The expanded connections string.</returns>
	// <remarks>
	//  This method replaces all variables which have previously been
	//  added to this collection (with add or put) in the given
	//  connections string with their respective values and then
	//  returns it. Variables in the connections string must have the
	//  following form: $variable$.
	// </remarks>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The connections argument is null.
	// </table>
	// </exception>
	
	public String expand(String connections)
	{
		if (connections == null)
		{
			throw new NullPointerException("connections parameter is null");
		}
		
		synchronized (this.fLock)
		{
			if (this.fItems.size() == 0)
			{
				return connections; 
			}
			
			Iterator it = this.fItems.keySet().iterator();
			
			while (it.hasNext())
			{
				String key = makeKey((String) it.next()); 
				KeyValue pair = (KeyValue) this.fItems.get(key);
				
				if (pair != null)
				{
					key = "$" + pair.getKey() + "$";
					String value = pair.getValue();
					connections = replace(connections, key, value);
				}
			}
		}
	
		return connections;
	}
	
	private String replace(String connections, String key, String value)
	{
		String search = connections;
		StringBuffer buffer = new StringBuffer();
		
		while (search.length() > 0)
		{
			int offset = search.indexOf(key);
			
			if (offset <= 0)
			{
				buffer.append(search);
				break;
			}
			
			buffer.append(search.substring(0, offset));
			buffer.append(value);
			search = search.substring(offset + key.length());
		}
		
		return buffer.toString();
	}
	
	// <summary>
	//   Returns a value of an element for a given key.
	// </summary>
	// <param name="key">The key whose value to return.</param>
	// <returns>
	//   Either the value for a given key if an element with the given
	//   key exists or null otherwise.
	// </returns>
	// <exception>
	// <table>
	//   Exception Type         Condition
	//   -                      -
	//   NullPointerException   The key argument is null.
	// </table>
	// </exception>
	
	public String get(String key)
	{
		if (key == null)
		{
			throw new NullPointerException("key parameter is null");
		}

		String value;
		
		synchronized (this.fLock)
		{
			KeyValue pair = (KeyValue) this.fItems.get(makeKey(key));
			
			if (pair == null)
			{
				value = null;
			}
			else 
			{
				value = pair.getValue();
			}
		}
		
		return value;
	}	

	private String makeKey(String key)
	{
		return key.toLowerCase();
	}
}
