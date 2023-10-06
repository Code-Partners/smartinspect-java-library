/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

import java.util.Arrays;
import java.util.TimeZone;

/**
 * Provides access to the current date and time, optionally with a
 * high resolution.
 * <p>
 * See now for a method which returns the current date and time,
 * optionally with a very high resolution. See calibrate for a
 * method which can synchronize the high-resolution timer with the
 * system clock.
 * <p>
 * This class is fully threadsafe.
 * @see ClockResolution
 */
public class Clock
{
	private static final String LIBRARY = "SmartInspect.Java";
	private static final int CALIBRATE_ROUNDS = 5;

	private static long fOffset;
	private static boolean fSupported;
	private static double fFrequency;
	
	// <ignore>
	// The documentation system seems to have problems with static
	// code blocks in Java, so we need to ignore the entire block
	// here.
	
	static
	{
		fSupported = false;
		
		try 
		{
			System.loadLibrary(LIBRARY);				
			long frequency = getFrequency();
			
			if (frequency != 0)
			{
				fSupported = true;
				fFrequency = frequency / 1000000.0;
				fOffset = getOffset();
			}
		}
		catch (UnsatisfiedLinkError e)
		{
		}
		catch (SecurityException e)
		{
		}
	}
	
	// </ignore>
	
	private static native long getCounter();
	private static native long getFrequency();
		
	private static long getTicks()
	{
		long timestamp = getCounter();
		return (long) (timestamp / fFrequency);
	}
	
	private static long getMicros()
	{
		long ms = System.currentTimeMillis();
		return (ms + TimeZone.getDefault().getOffset(ms)) * 1000;
	}
	
	private static long getOffset()
	{
		return getMicros() - getTicks();
	}

	/**
	 * Returns the current date and time, optionally with a high
	 * resolution.
	 * <p>
	 * If ClockResolution.High is passed as value for the resolution
	 * argument, this method tries to return a timestamp with a
	 * microsecond resolution.
	 * <p>
	 * The SmartInspect Java library needs an external DLL to be
	 * able to use high-resolution timestamps. This DLL is called
	 * SmartInspect.Java.dll. If this DLL cannot be found during
	 * application startup, high-resolution support is not available.
	 * Additionally, even if this DLL is found and loaded correctly,
	 * high-resolution timestamps are only available if the
	 * QueryPerformanceCounter and QueryPerformanceFrequency Windows
	 * functions indicate a successfully working high-resolution
	 * performance counter.
	 * <p>
	 * Please note that high-resolution timestamps are not intended to
	 * be used on production systems. It is recommended to use them only
	 * during development and debugging. See SmartInspect.setResolution
	 * for details.
	 * <p>
	 * If high-resolution support is not available, this method simply
	 * returns the local date and time with the help of the
	 * System.currentTimeMillis() function and the default time zone.
	 * @param resolution Specifies the desired resolution mode for the returned timestamp
	 * @return The current local date and time in microseconds since January 1, 1970
	 */
	public static long now(ClockResolution resolution)
	{
		if (resolution == ClockResolution.High && fSupported)
		{
			try 
			{
				return getTicks() + fOffset;
			}
			catch (UnsatisfiedLinkError e)
			{
			}
		}

		return getMicros(); /* Fallback */
	}
	
	private static long doCalibrate()
	{
		long millis = System.currentTimeMillis();
		while (millis == System.currentTimeMillis()) ;
		return getOffset();
	}

	/**
	 * Calibrates the high-resolution timer and synchronizes it
	 * with the system clock.
	 * <p>
	 * Use this method to calibrate the high-resolution timer and
	 * to improve the timer synchronization with the system clock.
	 * <p>
	 * Background: Without calling this method before calling now
	 * in high-resolution mode, now returns a value which is only
	 * loosely synchronized with the system clock. The returned
	 * value might differ by a few milliseconds. This can usually
	 * safely be ignored for a single process application, but may
	 * be an issue for distributed interacting applications with
	 * multiple processes. In this case, calling calibrate once on
	 * application startup might be necessary to improve the system
	 * clock synchronization of each process in order to get
	 * comparable timestamps across all processes.
	 * <p>
	 * Note that calling this method is quite costly, it can easily
	 * take 50 milliseconds, depending on the system clock timer
	 * resolution of the underlying operating system. Also note that
	 * the general limitations (see SmartInspect.setResolution) of
	 * high-resolution timestamps still apply after calling this
	 * method.
	 */
	public static void calibrate()
	{
		if (!fSupported)
		{
			return;
		}
		
		long[] rounds = new long[CALIBRATE_ROUNDS];
		
		for (int i = 0; i < CALIBRATE_ROUNDS; i++)
		{
			rounds[i] = doCalibrate();
		}
		
		fOffset = getMedian(rounds);
	}
	
	private static long getMedian(long[] array)
	{
		Arrays.sort(array);		
		if ((array.length & 1) == 1)
		{
			return array[(array.length - 1) / 2];
		}
		else 
		{
			long n = array[(array.length / 2) - 1];
			long m = array[array.length / 2];
			return (n + m) / 2;	
		}
	}
}
