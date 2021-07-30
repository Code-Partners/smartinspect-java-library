//
// <!-- Copyright (C) Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

import java.util.HashMap;

// <summary>
//   Manages and configures Session instances.
// </summary>
// <remarks>
//   This class manages and configures a list of sessions. Sessions
//   can be configured and added to the list with the add method. To
//   lookup a stored session, you can use get. To remove an existing
//   session from the list, call delete.
//   
//   Stored sessions will be reconfigured if loadConfiguration has
//   been called and contains corresponding session entries.
// </remarks>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

public class SessionManager
{
	private static final String PREFIX = "session.";
	
	private Object fLock;
	private HashMap fSessions;
	private HashMap fSessionInfos;
	private SessionDefaults fDefaults;
	
	// <summary>
	//   Creates and initializes a new SessionManager instance.
	// </summary>
	
	public SessionManager()
	{
		this.fLock = new Object();
		this.fSessions = new HashMap();
		this.fSessionInfos = new HashMap();
		this.fDefaults = new SessionDefaults();
	}

	// <summary>
	//   Loads the configuration properties of this session manager.
	// </summary>
	// <param name="config">
	//   The Configuration object to load the configuration from.
	// </param>
	// <remarks>
	//   This method loads the configuration of this session manager
	//   from the passed Configuration object. Sessions which have
	//   already been stored or will be added with add will
	//   automatically configured with the new properties if the
	//   passed Configuration object contains corresponding session
	//   entries. Moreover, this method also loads the default session
	//   properties which will be applied to all sessions which are
	//   passed to add.
	// 
	//   Please see the SmartInspect.loadConfiguration method for
	//   details on how session entries look like.
	// </remarks>
	
	public void loadConfiguration(Configuration config)
	{
		synchronized (this.fLock)
		{
			this.fSessionInfos.clear();
			loadInfos(config);
			loadDefaults(config);
		}
	}
	
	private void loadInfos(Configuration config)
	{
		for (int i = 0; i < config.getCount(); i++)
		{
			String key = config.readKey(i);
			
			/* Do we have a session here? */

			if (key.length() < PREFIX.length())
			{
				continue; /* No, too short */
			}

			String prefix = key.substring(0, PREFIX.length());

			if (!prefix.toLowerCase().equals(PREFIX)) 
			{
				continue; /* No prefix match */
			}

			String suffix = key.substring(PREFIX.length());
			int index = suffix.lastIndexOf('.');

			if (index == -1)
			{
				continue;
			}

			String name = suffix.substring(0, index).toLowerCase();

			/* Duplicate session configuration entry? */

			if (this.fSessionInfos.containsKey(name))
			{
				continue;
			}

			SessionInfo info = loadInfo(name, config);
			this.fSessionInfos.put(name, info);

			/* Do we need to update a related session? */

			Session session = (Session) this.fSessions.get(name);

			if (session != null)
			{
				assign(session, info);
			}
		}
	}
	
	private void loadDefaults(Configuration config)
	{
		this.fDefaults.setActive(
			config.readBoolean("sessiondefaults.active",
			this.fDefaults.isActive()));

		this.fDefaults.setLevel( 
			config.readLevel("sessiondefaults.level",
			this.fDefaults.getLevel()));

		this.fDefaults.setColor( 
			config.readColor("sessiondefaults.color",
			this.fDefaults.getColor()));
	}
	
	private SessionInfo loadInfo(String name, Configuration config)
	{
		SessionInfo info = new SessionInfo();
		
		info.name = name;
		info.hasActive = config.contains(PREFIX + name + ".active");

		if (info.hasActive)
		{
			info.active = config.readBoolean(PREFIX + name + ".active",
				true);
		}

		info.hasLevel = config.contains(PREFIX + name + ".level");

		if (info.hasLevel)
		{
			info.level = config.readLevel(PREFIX + name + ".level",
				Level.Debug);
		}

		info.hasColor = config.contains(PREFIX + name + ".color");

		if (info.hasColor)
		{
			info.color = config.readColor(PREFIX + name + ".color",
				Session.DEFAULT_COLOR);
		}

		return info;
	}
	
	// <summary>
	//   Configures a passed Session instance and optionally saves it
	//   for later access.
	// </summary>
	// <param name="session">
	//   The session to configure and to save for later access, if
	//   desired.
	// </param>
	// <param name="store">
	//   Indicates if the passed session should be stored for later
	//   access.
	// </param>
	// <remarks>
	//   This method configures the passed session with the default
	//   session properties as specified by the <link getDefaults,
	//   defaults property>. This default configuration can be overridden
	//   on a per-session basis by loading the session configuration with
	//   the loadConfiguration method.
	// 
	//   If the 'store' parameter is true, the passed session is stored
	//   for later access and can be retrieved with the get method. To
	//   remove a stored session from the internal list, call delete. 
	//   
	//   If this method is called multiple times with the same session
	//   name, then the get method operates on the session which got
	//   added last. If the session parameter is null, this method does
	//   nothing.
	// </remarks>
	
	public void add(Session session, boolean store)
	{
		if (session == null)
		{
			return;
		}
		
		String name = session.getName().toLowerCase();
		
		synchronized (this.fLock)
		{
			this.fDefaults.assign(session);
			
			if (store)
			{
				this.fSessions.put(name, session);
				session.setStored(true);
			}

			configure(session, name);
		}
	}
	
	// <summary>
	//   Removes a session from the internal list of sessions.
	// </summary>
	// <param name="session">
	//   The session to remove from the lookup table of sessions. Not
	//   allowed to be null.
	// </param>
	// <remarks>
	//   This method removes a session which has previously been added
	//   with the add method. After this method returns, the get method
	//   returns null when called with the same session name unless a
	//   different session with the same name has been added.
	//   
	//   This method does nothing if the supplied session argument is
	//   null.
	// </remarks>
	
	public void delete(Session session)
	{
		if (session == null)
		{
			return;
		}
		
		String name = session.getName().toLowerCase();
		
		synchronized (this.fLock)
		{
			if (this.fSessions.get(name) == session)
			{
				this.fSessions.remove(name);
			}
		}
	}

	// <summary>
	//   Returns a previously added session.
	// </summary>
	// <param name="name">
	//   The name of the session to lookup and return. Not allowed to
	//   be null.
	// </param>
	// <returns>
	//   The requested session or null if the supplied name is null
	//   or if the session is unknown.
	// </returns>
	// <remarks>
	//   This method returns a session which has previously been
	//   added with the add method and can be identified by the
	//   supplied name parameter. If the requested session is unknown
	//   or if the name argument is null, this method returns null.
	//   
	//   Note that the behavior of this method can be unexpected in
	//   terms of the result value if multiple sessions with the same
	//   name have been added. In this case, this method returns the
	//   session which got added last and not necessarily the session
	//   which you expect. 
	//   
	//   Adding multiple sessions with the same name should therefore
	//   be avoided.
	// </remarks>
	
	public Session get(String name)
	{
		if (name == null)
		{
			return null;
		}

		name = name.toLowerCase();
		
		synchronized (this.fLock)
		{
			return (Session) this.fSessions.get(name);
		}
	}
	
	// <summary>
	//   Updates an entry in the internal lookup table of sessions.
	// </summary>
	// <param name="session">
	//   The session whose name has changed and whose entry should
	//   be updated.
	// </param>
	// <param name="to">The new name of the session.</param>
	// <param name="from">The old name of the session.</param>
	// <remarks>
	//   Once the name of a session has changed, this method is called
	//   to update the internal session lookup table. The 'to' argument
	//   specifies the new name and 'from' the old name of the session.
	//   After this method returns, the new name can be passed to the
	//   get method to lookup the supplied session.
	// </remarks>
	
	public void update(Session session, String to, String from)
	{
		if (session == null)
		{
			return;
		}
		
		if (from == null || to == null)
		{
			return;
		}
		
		to = to.toLowerCase();
		from = from.toLowerCase();
		
		synchronized (this.fLock)
		{
			if (this.fSessions.get(from) == session)
			{
				this.fSessions.remove(from);
			}
				
			configure(session, to);
			this.fSessions.put(to, session);
		}
	}
	
	private void configure(Session session, String name)
	{
		SessionInfo info = 
			(SessionInfo) this.fSessionInfos.get(name);
		
		if (info != null)
		{
			assign(session, info);
		}
	}
	
	private void assign(Session session, SessionInfo info)
	{
		if (info.active)
		{
			if (info.hasColor)
			{
				session.setColor(info.color);
			}
			
			if (info.hasLevel)
			{
				session.setLevel(info.level);
			}
			
			if (info.hasActive)
			{
				session.setActive(info.active);
			}
		}
		else
		{
			if (info.hasActive)
			{
				session.setActive(info.active);
			}
			
			if (info.hasLevel)
			{
				session.setLevel(info.level);
			}
			
			if (info.hasColor)
			{
				session.setColor(info.color);
			}
		}
	}
	
	// <summary>
	//   Clears the configuration of this session manager and removes
	//   all sessions from the internal lookup table.
	// </summary>
	
	public void clear()
	{
		synchronized (this.fLock)
		{
			this.fSessions.clear();
			this.fSessionInfos.clear();
		}
	}
	
	// <summary>
	//   Returns the default property values for new sessions.
	// </summary>
	// <returns>The default property values for new sessions</returns>
	// <remarks>
	//   This method lets you specify the default property values
	//   for new sessions which will be passed to the add method.
	//   Please see the add method for details. For information about
	//   the available session properties, please refer to the
	//   documentation of the Session class.
	// </remarks>
	
	public SessionDefaults getDefaults()
	{
		return this.fDefaults;
	}
}
