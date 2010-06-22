//
//<!-- Copyright (C) 2003-2010 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class FileHelper
{
	private static final String ALREADY_EXISTS_SUFFIX = "a";
	
	/* Do not change */
	private static final String DATETIME_FORMAT = "yyyy-MM-dd-HH-mm-ss";
	private static final String DATETIME_SEPARATOR = "-";
	private static final int DATETIME_TOKENS = 6;
	
	public static Date getFileDate(String baseName, String path)
		throws SmartInspectException
	{
		Date date = tryGetFileDate(baseName, path);		
		
		if (date == null)
		{
			throw new SmartInspectException("Invalid filename");
		}
		
		return date;
	}
	
	private static Date tryGetFileDate(String baseName, String path)
	{
		String fileName = Path.getFileName(path);
		
		baseName = Path.changeExtension(
			Path.getFileName(baseName),
			null);

		/* In order to avoid possible bugs with the creation
		 * time of file names (log files on Windows or Samba
		 * shares, for instance), we parse the name of the log
		 * file and do not use its creation time. */

		int index = fileName.indexOf(baseName);

		if (index != 0)
		{
			return null;
		}

		String value = Path.changeExtension(
			fileName.substring(baseName.length() + 1),
			null);
		
		/* Strip any added ALREADY_EXISTS_SUFFIX characters.
		 * This can happen if we are non-append mode and need
		 * to add this special character/suffix in order to
		 * not override an existing file. */

		if (value.length() > DATETIME_FORMAT.length())
		{
			value = value.substring(0, DATETIME_FORMAT.length());
		}

		return tryParseFileDate(value);
	}

	private static Date tryParseFileDate(String fileDate)
	{
		if (fileDate == null)
		{
			return null;
		}

		if (fileDate.length() != DATETIME_FORMAT.length())
		{
			return null;
		}

		for (int i = 0; i < fileDate.length(); i++)
		{
			char c = fileDate.charAt(i);
			if (!Character.isDigit(c) && 
				c != DATETIME_SEPARATOR.charAt(0))
			{
				return null;
			}
		}

		String[] values = fileDate.split(DATETIME_SEPARATOR);

		if (values == null || values.length != DATETIME_TOKENS)
		{
			return null;
		}

		Calendar calendar = GregorianCalendar.getInstance(
			getUtcTimeZone());
		
		calendar.set(
			Integer.parseInt(values[0]), /* Year */
			Integer.parseInt(values[1]) - 1, /* Month, 0-based */
			Integer.parseInt(values[2]), /* Day */
			Integer.parseInt(values[3]), /* Hour */
			Integer.parseInt(values[4]), /* Minute */
			Integer.parseInt(values[5])  /* Second */
		);
		
		return calendar.getTime();
	}
	
	private static boolean isValidFile(String baseName, String path)
	{
		return tryGetFileDate(baseName, path) != null;
	}
	
	public static String getFileName(String baseName, boolean append)
	{
		/* In rotating mode, we need to differentiate between
		 * append and non-append mode. In append mode, we try
		 * to use an already existing file. In non-append mode,
		 * we just use a new file with the current timestamp
		 * appended. */

		if (append)
		{
			String fileName = findFileName(baseName);

			if (fileName != null)
			{
				return fileName;
			}
		}

		return expandFileName(baseName); /* Fallback */
	}
	
	private static String findFileName(String baseName)
	{
		String[] files = getFiles(baseName);
		
		if (files == null || files.length == 0)
		{
			return null;
		}
		
		return files[files.length - 1];
	}
	
	private static String expandFileName(String baseName)
	{
		DateFormat formatter = new SimpleDateFormat(DATETIME_FORMAT);
		formatter.setTimeZone(getUtcTimeZone());
		
		String result = new StringBuffer().
			append(Path.changeExtension(baseName, null)).
			append("-").
			append(formatter.format(new Date())).
			append(Path.getExtension(baseName)).
			toString();
		
		/* Append a special character/suffix to the expanded
		 * file name if the file already exists in order to
		 * not override an existing log file. */
		
		while (new File(result).exists())
		{
			result = new StringBuffer().
				append(Path.changeExtension(result, null)).
				append(ALREADY_EXISTS_SUFFIX).
				append(Path.getExtension(result)).
				toString();
		}
		
		return result;
	}
	
	private static TimeZone getUtcTimeZone()
	{
		return TimeZone.getTimeZone("Etc/UTC");	
	}
	
	private static String[] getFiles(final String baseName)
	{
		String directoryName = Path.getDirectoryName(baseName);
		
		File directory;		
		if (directoryName == null || directoryName.length() == 0)
		{
			directory = new File(".");
		}
		else 
		{
			directory = new File(directoryName);
		}
				
		String fileName = Path.getFileName(baseName);
		final String fileExtension = Path.getExtension(fileName);
		final String filePrefix = Path.changeExtension(fileName, null);			
		
		String[] files = directory.list(
			new FilenameFilter()
			{
				public boolean accept(File dir, String name)
				{
					if (name == null || name.length() == 0)
					{
						return false;
					}
					
					if (!name.startsWith(filePrefix) ||
						!name.endsWith(fileExtension))
					{
						return false;
					}
					
					/* Important! Ensure that only valid files
					 * are included in the result. */
					return isValidFile(baseName, name);
				}
			}
		);

		if (files == null || files.length == 0)
		{
			return files;
		}
		
		if (directoryName != null && directoryName.length() != 0)
		{
			String fileSeparator = Path.getFileSeparator();
			
			if (!directoryName.endsWith(fileSeparator))
			{
				directoryName = directoryName + fileSeparator;
			}
			
			for (int i = 0; i < files.length; i++)
			{
				files[i] = directoryName + files[i];  
			}
		}
		
		Arrays.sort(files);
		return files;
	}

	public static void deleteFiles(String baseName, int maxParts)
	{
		String[] files = getFiles(baseName);
		
		if (files == null)
		{
			return;
		}
		
		for (int i = 0; i < files.length; i++)
		{
			if (i + maxParts >= files.length)
			{
				break;
			}

			new File(files[i]).delete();
		}
	}
}
