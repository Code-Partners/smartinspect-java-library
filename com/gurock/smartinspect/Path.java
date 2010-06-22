//
// <!-- Copyright (C) 2003-2010 Gurock Software GmbH. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Provides static methods to perform certain operations on strings
//   which represent a path.
// </summary>
// <remarks>
//   The Path class provides several methods to perform transformations
//   on path strings. This class only transforms strings and does no
//   operations on the corresponding filesystem entries. For example,
//   the changeExtension method changes the extension of a given path
//   string but does not change the actual filesystem entry. Operations
//   are done in a cross-platform manner.
// </remarks>
// <threadsafety>
//   This class is not guaranteed to be threadsafe.
// </threadsafety>

public class Path 
{
	// <summary>
	//   Changes the file extension of a filename for a given path.
	// </summary>
	// <param name="path">
	//   The path information to modify. Allowed to be null.
	// </param>
	// <param name="extension">
	//   The file extension (with leading period). Specify null or an empty
	//   string to remove an existing file extension.
	// </param>
	// <returns>
	//   The supplied path but with the changed or removed extension.
	// </returns>
	// <remarks>
	//   If the supplied path argument is null, the return value of this
	//   method is null as well. If the supplied extension argument is
	//   null or an empty string, an existing file extension is removed
	//   from the given path.
	// </remarks>
	
	public static String changeExtension(String path, String extension)
	{
		if (path != null)
		{
			int index = path.lastIndexOf('.');
			if (index != -1)
			{
				path = path.substring(0, index);
				if (extension != null)
				{
					path += extension;
				}
			}
		}
		
		return path;
	}
	
	// <summary>
	//   Returns the file extension of a filename for a given path.
	// </summary>
	// <param name="path">The path from which to get the extension.</param>
	// <returns>
	//   The file extension of the supplied path if available or an empty
	//   string otherwise.
	// </returns>
	// <remarks>
	//   This method returns the file extension of the given path string 
	//   including the leading period character. If the supplied path
	//   parameter is a null reference or does not contain an extension,
	//   the return value of this method is an empty string. 
	// </remarks>
	
	public static String getExtension(String path)
	{
		String extension = "";
		
		if (path != null)
		{
			int index = path.lastIndexOf('.');
			if (index != -1 && index < path.length() - 1)
			{
				extension = path.substring(index);
			}
		}
		
		return extension;
	}
	
	// <summary>
	//   Returns the file name for a given path.
	// </summary>
	// <param name="path">
	//   The path from which to get the file name. Allowed to be null.
	// </param>
	// <returns>
	//   The file name of the supplied path if available or null if the
	//   supplied path argument is null.
	// </returns>
	// <remarks>
	//   This method returns the file name of the given path string 
	//   excluding any directory or volume separator characters. If the
	//   supplied path parameter is a null reference, the return value
	//   of this method is null as well. If the last character of the
	//   supplied path argument is a directory or volume character, then
	//   the return value of this method is an empty string.
	// </remarks>
		
	public static String getFileName(String path)
	{
		String fileName = path;
		
		if (path != null)
		{
			int index = path.lastIndexOf(getFileSeparator());
						
			if (index != -1)
			{
				if (index < path.length() - 1)
				{
					// Do not include file separator char.
					fileName = path.substring(index + 1);
				}
				else 
				{
					fileName = "";
				}
			}
		}
		
		return fileName;
	}
	
	// <summary>
	//   Returns the directory name for a given path.
	// </summary>
	// <param name="path">
	//   The path from which to get the directory name. Allowed to be
	//   null.
	// </param>
	// <returns>
	//   The directory of the supplied path if available or null if the
	//   supplied path argument is null.
	// </returns>
	// <remarks>
	//   This method returns the directory name of the given path string. 
	//   If the supplied path parameter is a null reference, the return
	//   value of this method is null as well.
	// </remarks>
	
	public static String getDirectoryName(String path)
	{
		String directoryName = path;
		
		if (path != null)
		{
			int index = path.lastIndexOf(getFileSeparator());
			
			if (index != -1)
			{
				if (index > 0 && path.charAt(index - 1) != ':')
				{
					index--;					
				}
				
				directoryName = path.substring(0, index + 1);
			}
			else 
			{
				directoryName = "";
			}
		}
		
		return directoryName;
	}
	
	// <summary>
	//   Returns the file separator character of the current runtime
	//   environment.
	// </summary>
	// <returns>
	//   The file separator of the current runtime environment.
	// </returns>

	public static String getFileSeparator()
	{
		String fileSeparator = System.getProperty("file.separator");

		if (fileSeparator == null)
		{
			// Fall back to the Windows default
			fileSeparator = "\\"; 
		}
		
		return fileSeparator;
	}
}
