/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A configurable timer for monitoring and reloading SmartInspect
 * configuration files on changes.
 *
 * <p>Use this class to monitor and automatically reload SmartInspect
 * configuration files. This timer periodically checks if the
 * related configuration file has changed (by comparing the last
 * write time) and automatically tries to reload the configuration
 * properties. You can pass the SmartInspect object to configure,
 * the name of the configuration file to monitor and the interval
 * in which this timer should check for changes.</p>
 *
 * <p>For information about SmartInspect configuration files, please
 * refer to the documentation of the SmartInspect.loadConfiguration
 * method.</p>
 *
 * <p>This class is fully threadsafe.</p>
 */
public class ConfigurationTimer {
	private Timer fTimer;
	private SmartInspect fSmartInspect;
	private String fFileName;
	private Date fLastUpdate;

	class ConfigurationTask extends TimerTask {
		public void run() {
			Date lastUpdate = getFileAge(fFileName);

			if (lastUpdate == null) {
				return;
			}

			if (!lastUpdate.after(fLastUpdate)) {
				return;
			}

			fLastUpdate = lastUpdate;
			fSmartInspect.loadConfiguration(fFileName);
		}
	}

	/**
	 * Creates and initializes a new ConfigurationTimer object.
	 *
	 * @param smartInspect The SmartInspect object to configure.
	 * @param fileName     The name of the configuration file to monitor.
	 * @param period       The milliseconds interval in which this timer should check for changes.
	 * @throws NullPointerException     if the smartInspect or fileName parameter is null.
	 * @throws IllegalArgumentException if the period parameter is negative.
	 */
	public ConfigurationTimer(SmartInspect smartInspect,
							  String fileName, int period) {
		if (smartInspect == null) {
			throw new NullPointerException("smartInspect");
		}

		if (fileName == null) {
			throw new NullPointerException("fileName");
		}

		this.fFileName = fileName;
		this.fSmartInspect = smartInspect;
		this.fLastUpdate = getFileAge(this.fFileName);

		if (this.fLastUpdate != null) {
			this.fSmartInspect.loadConfiguration(this.fFileName);
		}

		this.fTimer = new Timer();
		this.fTimer.schedule(new ConfigurationTask(), period, period);
	}

	private static Date getFileAge(String fileName) {
		Date result = null;

		try {
			File file = new File(fileName);
			long lastUpdate = file.lastModified();

			if (lastUpdate != 0L) {
				result = new Date(lastUpdate);
			}
		} catch (SecurityException e) {
		}

		return result;
	}

	/**
	 * The method releases all resources of this ConfigurationTimer object
	 * and stops monitoring the SmartInspect configuration file for
	 * changes.
	 */
	public synchronized void dispose() {
		if (this.fTimer != null) {
			this.fTimer.cancel();
			this.fTimer = null;
		}
	}
}
