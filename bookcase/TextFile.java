
/*
 * Created:  13. ‎January ‎2012, ‏‎12:11:56
 * Author:   Roman Horváth
 *
 * For comments and examples see JavaDoc comments below…
 *
 * TODO – finish version changes
 *
 * Version:  1.97
 * Modified: 8. October 2016, 5:47
 * Fixed:    One bug in the copy() method.
 * 
 * Version:  1.96
 * Modified: 16. November 2015, 5:15
 * Fixed:    … propertyExists had to be fixed, because it became buggy
 *           (I have no idea when).
 *
 * Version:  1.95
 * Modified: 6. November 2015, 2:02
 * Changed:  The property mechanism has been rebuild. Original implementation
 *           used three private vectors (two of String and one of Boolean),
 *           the new implementation is using two private nested classes –
 *           Property and Properties. With this change the mechanism became
 *           more consistent. (Some features could move to these classes
 *           in the future.)
 *
 * Version:  1.94
 * Modified: 5. November 2015, 11:44
 * Changed:  Improved data reading mechanism. First motivation was to make
 *           the endOfFile() method more reliable. It turned out that the
 *           method will never be reliable enough (such way as was
 *           implemented) so the majority of its original content has been
 *           removed and replaced by just the simple return command with new
 *           homonymous private field as argument. More changes has been
 *           made. The method readingTheLine() was renamed to readBuffer()
 *           and its return type has changed from String to void. Its body
 *           has drastically changed. The private field lineRead was renamed
 *           to lineBuffer. The bodies of all following methods has less or
 *           more changed: skipSpaces(), endOfLine(), readLine(),
 *           finishReadLine(), openForReading(…), and close().
 * Added:    New method readCharacter().
 * Comments: Some more javadoc comments were completed.
 *
 * Version:  1.93
 * Modified: 2. November 2015, 11:57
 * Changed:  Few internal improvements connected to searching the files for
 *           reading.
 * Added:    New method whereIsFile(…) that finds original location of files for
 *           reading.
 * Comments: Some javadoc comments were completed.
 *
 * Version:  1.92
 * Modified: 1. November 2015, 18:16
 * Changed:  Class became part of bookcase package.
 * Fixed:    With the new change some files in existing project became
 *           “inacessible” (the class was unable to find them anymore).
 *           Methods openForReading, compare, copy and append now search
 *           for the files through the current classpath.
 *
 * Version:  1.91
 * Modified: 31. October 2015, 4:47
 * Changed:  The private method noMoreData() was renamed to endOfFile() and
 *           turned to public. New private method skipSpaces() was created
 *           and was used instead of original noMoreData() method within
 *           read«XY»() methods (where «XY» may be Long, Double or Boolean).
 * Added:    New method endOfLine().
 * Comments: Added @throws clauses. Some comments were added.
 *
 * Version:  1.90
 * Modified: 30. October 2015, 1:50
 * Added:    New method finishReadLine().
 * Comments: Finished some descriptive comments to public fields or methods.
 *
 * Version:  1.89
 * Modified: 22. October 2015, 10:31
 * Added:    New method clearProperties().
 * Comments: Added descriptive comments to all private fields and methods
 *           and to few public ones. Added templates of descriptive comments
 *           to all public methods.
 *
 * Version:  1.88
 * Modified: 22. October 2015, 0:22
 * Improved: Private method getListFromJarFile(…) now processes the entered
 *           path better way.
 *
 * Version:  1.87
 * Modified: 18. October 2015, 6:12
 * Fixed:    Just corrected the misspelled word Writ(t)ing…
 *
 * Version:  1.86
 * Modified: 17. October 2015, 6:22
 * Fixed:    Namespaces may not start with semicolon.
 *
 * Version:  1.85
 * Modified: 12. May 2015, 6:24
 * Added:    New methods: propertyExists(name) and deleteProperty(name).
 *           The class now supports null values of properties.
 * Modified: The texts of error messages (exceptions texts) were modified.
 *
 * Version:  1.81
 * Modified: 31. March 2015, 18:30
 * Fixed:    Method compare – buffers may be filled by different chunk sizes
 *           when comparing files from jar. New version of method compare(…)
 *           is considering this fact and handles it correctly.
 *
 * Version:  1.80
 * Modified: 27. March 2015, 17:31
 * Added:    New version of makeDirectory(…) method – now also parent
 *           directories can be made.
 *
 * Version:  1.79
 * Modified: 21. March 2015, 4:51
 * Fixed:    Two non-existing files now match. Several bugs connected to
 *           handling of non-existing files were repaired.
 *
 * Version:  1.78
 * Modified: 19. March 2015, 1:50
 * Added:    New static method compare(String, String) dedicated to match
 *           content of two files.
 *
 * Version:  1.77
 * Repaired: 14. March 2015, 6:00
 * Fixed:    Bug hidden in private readProperties() method (about character \
 *           at the end of line).
 *
 * Version:  1.76
 * Repaired: 25. February 2015, 12:50
 * Fixed:    The definition of lastModified(String) method was invalid. Badly
 *           it was a copy of the delete(String) method. I apologize!
 *
 * Version:  1.75
 * Repaired: 27. November 2013, 21:45
 * Fixed:    Method write(...) does not put new lines after string arguments
 *           anymore.
 *
 * Version:  1.7
 * Modified: 27. November 2013, 11:00
 * Added:    Another version of method writeLine() – now it is possible
 *           to use this method without parameters.
 *
 * Version:  1.65
 * Modified: 22. October 2013, 13:00
 * Changed:  The error message: Target file „…“ does not exist anymore!
 *           Has been corrected to: Target file “…” already exists!
 *           All Slovak „“ were replaced by English “”.
 *
 * Version:  1.6
 * Modified: 22. August ‎2013, 20:58:00
 * Added:    Added static method lastModified(String).
 *
 * Version:  1.5
 * Modified: 19. August ‎2013, 12:22:08
 * Added:    Added another version of method openForWriting(…) – now is
 *           possible to append content to existing files.
 *
 * Version:  1.4
 * Modified: 13. February ‎2013, 04:00:00
 * Added:    The ability to dismiss unused properties. See the methods:
 *           keepsUnusedProperties(), keepUnusedProperties(), and
 *           dismissUnusedProperties().
 *
 * Version:  1.3
 * Repaired: 1. February ‎2013, 16:04:02
 * Fixed:    Some versions of the method readProperty(…) did not handle
 *           the null default value properly.
 * Changed:  The readProperty(…) methods for Long and Double now return null
 *           if the string value in the text file is "null".
 *
 * Version:  1.2
 * Repaired: 7. January ‎2013, 08:24:18
 * Changed:  add: missing import java.net.URL;
 * Modified: 1. January ‎2013, 21:57:00
 * Changed:  Methods copy(…) sets the target file time and date to .jar’s
 *           file time and date when the source file is read from the .jar
 *           archive.
 *
 * Version:  1.1
 * Modified: 25. December ‎2012, ‏‎06:05:34
 * Added:    Method append(String, String).
 *
 * Version:  1.0
 * Modified: 17. ‎January ‎2012, ‏‎11:58:48
 */


package bookcase;

import java.awt.FileDialog;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;

import java.util.Enumeration;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import java.util.Vector;


/**
 * This class implements basic work with text files. (Default encoding is
 * UTF-8.) Basic input and output can be performed by read…/write… methods,
 * advanced work is possible using readProperty and writeProperty methods.
 * Class provides also the basic work with files like renaming, moving,
 * copying, etc.
 * TODO – Add examples of ussage!
 */
public class TextFile
{
	// File writer
	private BufferedWriter writer = null;

	// File reader
	private BufferedReader reader = null;

	// Flag signaling that the buffer is receiving first line and that there
	// might be the byte-order mark at the start
	private boolean firstLine = false;

	// This flag appeared to be necessary – it indicates the end of file
	private boolean endOfFile = false;

	// Also this flag appeared to be necessary – it coordinates endOfFile,
	// readBuffer, readLine, etc. methods
	private boolean checkNextLine = false;

	// Buffer for read lines
	private final StringBuffer lineBuffer = new StringBuffer();


	/**
	 * This class represents single record within the property record – see
	 * also the {@link Properties} class. Single record may store information
	 * about configuration item that means concrete property, or comment line,
	 * or empty lines read from or written to the configuration file… (Note
	 * that comments for methods of this class have short form – there is no
	 * description of parameters, return values, or other details – because
	 * this class is private.)
	 */
	private class Property
	{
		// Name, hash code, value and a write state:
		public final String name;
		public final int hash;
		public String value;
		public boolean toWrite;


		/** Basic constructor. */
		public Property(String name)
		{
			this.name = name;
			this.value = null;
			this.toWrite = false;
			this.hash = name.hashCode();
		}

		/** Constructor with value. */
		public Property(String name, String value)
		{
			this.name = name;
			this.value = value;
			this.toWrite = false;
			this.hash = name.hashCode();
		}

		/** Constructor with value and write flag. */
		public Property(String name, String value, boolean writeMe)
		{
			this.name = name;
			this.value = value;
			this.toWrite = writeMe;
			this.hash = name.hashCode();
		}

		/** Gets hash code. */
		@Override public int hashCode()
		{
			return hash;
		}

		/**
		 * Checks equality of two objects, if they are compatibile. Equality
		 * may be evaluated against another property or property name which
		 * can be specified by any CharSequence (e.g. String).
		 */
		@Override public boolean equals(Object obj)
		{
			if (null == obj) return null == name;

			if (obj instanceof Property)
			{
				Property oth = (Property)obj;
				if (null == name && null == oth.name) return true;
				if (null == name || null == oth.name) return false;
				return name.equals(oth.name);
			}

			if (obj instanceof CharSequence)
			{
				CharSequence chs = (CharSequence)obj;
				if (null == name && null == chs) return true;
				if (null == name || null == chs) return false;
				return name.equals(chs.toString());
			}

			return false;
		}
	}

	/**
	 * Properties record class – stores information about group of properties
	 * read from or written to configuration files – see alse the {@link
	 * Property} class. (Note that comments for methods of this class have
	 * short form – there is no description of parameters, return values,
	 * or other details – because this class is private.)
	 */
	@SuppressWarnings("serial")
	private class Properties extends Vector<Property>
	{
		/** Adds new basic property record. */
		public void add(String name)
		{
			add(new Property(name));
		}

		/** Adds new property record with value. */
		public void add(String name, String value)
		{
			add(new Property(name, value));
		}

		/** Adds new property record with value and default write state. */
		public void add(String name, String value, boolean writeMe)
		{
			add(new Property(name, value, writeMe));
		}

		/**
		 * Sets the value of specified property to null, where name is
		 * the name of the property.
		 */
		public void set(String name)
		{
			int indexOf = indexOf(name);
			if (-1 == indexOf) add(new Property(name));
			else get(indexOf).value = null;
		}

		/**
		 * Sets new value of specified property, where name is the name of
		 * the property and value is its new value.
		 */
		public void set(String name, String value)
		{
			int indexOf = indexOf(name);
			if (-1 == indexOf) add(new Property(name, value));
			else get(indexOf).value = value;
		}

		/**
		 * Sets new value and write state of specified property, where name
		 * is the name of the property, value is its new value, and writeMe
		 * is new write state.
		 */
		public void set(String name, String value, boolean writeMe)
		{
			int indexOf = indexOf(name);
			if (-1 == indexOf) add(new Property(name, value, writeMe));
			else
			{
				Property property = get(indexOf);
				property.value = value;
				property.toWrite = writeMe;
			}
		}

		/**
		 * Sets new write state of specified property, where name is the
		 * name of the property and writeMe is the new write state.
		 */
		public void set(String name, boolean writeMe)
		{
			int indexOf = indexOf(name);
			if (-1 == indexOf) add(new Property(name, null, writeMe));
			else get(indexOf).toWrite = writeMe;
		}


		/**
		 * Inserts new basic property record at specified position within
		 * this record of properties.
		 */
		public void insert(int index, String name)
		{
			insertElementAt(new Property(name), index);
		}

		/**
		 * Inserts new property record with value at specified position
		 * within this record of properties.
		 */
		public void insert(int index, String name, String value)
		{
			insertElementAt(new Property(name, value), index);
		}

		/**
		 * Inserts new property record with value and default write state
		 * at specified position within this record of properties.
		 */
		public void insert(int index, String name, String value,
			boolean writeMe)
		{
			insertElementAt(new Property(name, value, writeMe), index);
		}


		/**
		 * Finds the index of specified object within this properties record,
		 * if specified object is compatibile with the record (it is property
		 * or property name – string).
		 */
		@Override public int indexOf(Object o)
		{
			for (int i = 0; i < size(); ++i)
				if (o == null ? get(i) == null : get(i).equals(o)) return i;
			return -1;
		}

		/**
		 * Finds the index of specified object within this properties record
		 * starting the search at specified position, if specified object is
		 * compatibile with the record (it is property or property name –
		 * string).
		 */
		@Override public int indexOf(Object o, int index)
		{
			for (int i = index; i < size(); ++i)
				if (o == null ? get(i) == null : get(i).equals(o)) return i;
			return -1;
		}
	}


	// Properties fields:

	private final Properties properties = new Properties();
	private boolean keepUnusedProperties = true;
	private boolean propertiesLoaded = false;
	private String propertiesNamespace = null;
	private int lastWrittenProperty = -1;


	// Validates path stored in entered File object
	private static void checkPathValidity(File path)
		throws FileNotFoundException, IllegalArgumentException
	{
		if (null == path)
			throw new IllegalArgumentException(
				"The directory name must not be empty.");

		if (!path.exists())
			throw new FileNotFoundException("The directory “" + path +
				"” does not exist.");

		if (!path.isDirectory())
			throw new IllegalArgumentException("The path entered (" + path +
				") is not a directory.");

		if (!path.canRead())
			throw new IllegalArgumentException("Directory “" + path +
				"” cannot be read.");
	}


	// Writes out all properties
	private void writeAllProperties() throws IOException
	{
		for (Property property : properties)
		{
			if (keepUnusedProperties || property.toWrite)
			{
				if (property.name.equals("")) writeLine(); else
				{
					if (null == property.value) writeLine(property.name);
					else if (
						-1 != property.value.indexOf('\n') ||
						-1 != property.value.indexOf('\r') ||
						-1 != property.value.indexOf('\\'))
					{
						int indexOf = 0;
						StringBuffer filteredValue =
							new StringBuffer(property.value);

						while (-1 != (indexOf = filteredValue.
							indexOf("\\", indexOf)))
						{
							filteredValue.replace(indexOf,
								indexOf + 1, "\\\\");
							// 4debug: value = new String(filteredValue);
							indexOf += 2;
						}

						indexOf = 0;
						while (-1 != (indexOf = filteredValue.
							indexOf("\n", indexOf)))
							filteredValue.replace(indexOf, indexOf + 1, "\\n");

						indexOf = 0;
						while (-1 != (indexOf = filteredValue.
							indexOf("\r", indexOf)))
							filteredValue.replace(indexOf, indexOf + 1, "\\r");

						if (property.name.startsWith(";"))
							writeLine(";" + filteredValue);
						else
							writeLine(property.name + "=" + filteredValue);
					}
					else
					{
						if (property.name.startsWith(";"))
							writeLine(";" + property.value);
						else
							writeLine(property.name + "=" + property.value);
					}
				}
			}
		}
	}

	// Validates entered property name
	private void checkNameValidity(String name)
		throws IllegalArgumentException
	{
		if (name.equals(""))
			throw new IllegalArgumentException(
				"The property name must not be empty.");

		if (name.startsWith(";"))
			throw new IllegalArgumentException(
				"The property name must not start with the comment character.");

		if (-1 != name.indexOf('.'))
			throw new IllegalArgumentException(
				"The property name must not contain the dot character.");

		if (-1 != name.indexOf('='))
			throw new IllegalArgumentException(
				"The property name must not include the equal sign.");
	}

	// Default string for the null value
	private final static String nullString = "null";

	// Gets Long object from the buffer
	private Long getLong(StringBuffer string)
	{
		if (0 == string.length()) return null;
		Long number = null;
		int indexOf = string.indexOf(" ");

		while (0 == indexOf)
		{
			string.deleteCharAt(0);
			if (0 == string.length()) return null;
			indexOf = string.indexOf(" ");
		}

		if (indexOf == -1)
		{
			try
			{
				if (!string.toString().equalsIgnoreCase(nullString))
					number = Long.valueOf(string.toString());
				string.setLength(0);
			}
			catch (Exception e) { detailedErrorReport(e, false); }
		}
		else
		{
			try
			{
				if (!string.substring(0, indexOf).
					equalsIgnoreCase(nullString))
					number = Long.valueOf(string.substring(0, indexOf));
				string.delete(0, indexOf + 1);
			}
			catch (Exception e) { detailedErrorReport(e); }
		}

		return number;
	}

	// Gets Double object from the buffer
	private Double getDouble(StringBuffer string)
	{
		if (0 == string.length()) return null;
		Double number = null;
		int indexOf = string.indexOf(" ");

		while (0 == indexOf)
		{
			string.deleteCharAt(0);
			if (0 == string.length()) return null;
			indexOf = string.indexOf(" ");
		}

		if (indexOf == -1)
		{
			try
			{
				if (!string.toString().equalsIgnoreCase(nullString))
					number = Double.valueOf(string.toString());
				string.setLength(0);
			}
			catch (Exception e) { detailedErrorReport(e, false); }
		}
		else
		{
			try
			{
				if (!string.substring(0, indexOf).
					equalsIgnoreCase(nullString))
					number = Double.valueOf(string.substring(0, indexOf));
				string.delete(0, indexOf + 1);
			}
			catch (Exception e) { detailedErrorReport(e); }
		}

		return number;
	}

	// Gets Boolean object from the buffer
	private Boolean getBoolean(StringBuffer string)
	{
		if (0 == string.length()) return null;
		Boolean value = null;
		int indexOf = string.indexOf(" ");

		while (0 == indexOf)
		{
			string.deleteCharAt(0);
			if (0 == string.length()) return null;
			indexOf = string.indexOf(" ");
		}

		if (indexOf == -1)
		{
			if (!string.toString().equalsIgnoreCase(nullString))
				value = Boolean.valueOf(string.toString());
			string.setLength(0);
		}
		else
		{
			if (!string.substring(0, indexOf).equalsIgnoreCase(nullString))
				value = Boolean.valueOf(string.substring(0, indexOf));
			string.delete(0, indexOf + 1);
		}

		return value;
	}

	// Reads all properties from the file open for reading
	private void readProperties()
		throws IOException, IllegalArgumentException
	{
		if (propertiesLoaded) return;

		if (null == reader)
			throw new IOException("There is no file open for reading.");

		String string, name, value;
		while (null != (string = readLine()))
		{
			if (string.equals(""))
			{
				properties.add("");
				continue;
			}

			int indexOf;
			if (string.startsWith(";"))
			{
				value = string.substring(1);

				if (-1 != value.indexOf('\\'))
				{
					StringBuffer filteredValue = new StringBuffer(value);

					for (indexOf = 0; -1 != (indexOf = filteredValue.
						indexOf("\\", indexOf)); ++indexOf)
					{
						filteredValue.deleteCharAt(indexOf);
						if (indexOf < filteredValue.length())
						switch (filteredValue.charAt(indexOf))
						{
							case 'n': filteredValue.setCharAt(indexOf, '\n');
								break;
							case 'r': filteredValue.setCharAt(indexOf, '\r');
								break;
							case 't': filteredValue.setCharAt(indexOf, '\t');
								break;
							case 'b': filteredValue.setCharAt(indexOf, '\b');
								break;
							case 'f': filteredValue.setCharAt(indexOf, '\f');
								break;
						}
					}

					value = new String(filteredValue);
				}

				properties.add(";", value);
				continue;
			}

			indexOf = string.indexOf("=");
			if (-1 == indexOf)
			{
				name = new String(string);
				value = null;
			}
			else
			{
				name = string.substring(0, indexOf);
				value = string.substring(1 + indexOf);
			}

			name = name.trim();
			if (name.equals(""))
				throw new IllegalArgumentException(
					"The file contains a unnamed property.");

			if (name.startsWith(";"))	// if the comment is space indented
				throw new IllegalArgumentException(
					"The file contains a property starting " +
					"with the comment sign.");

			if (-1 != properties.indexOf(name))
				throw new IllegalArgumentException(
					"The file contains doubled property: " + name);

			if (null != value && -1 != value.indexOf('\\'))
			{
				StringBuffer filteredValue = new StringBuffer(value);

				for (indexOf = 0; -1 != (indexOf = filteredValue.
					indexOf("\\", indexOf)); ++indexOf)
				{
					filteredValue.deleteCharAt(indexOf);
					if (indexOf < filteredValue.length())
					switch (filteredValue.charAt(indexOf))
					{
						case 'n': filteredValue.setCharAt(indexOf, '\n');
							break;
						case 'r': filteredValue.setCharAt(indexOf, '\r');
							break;
						case 't': filteredValue.setCharAt(indexOf, '\t');
							break;
						case 'b': filteredValue.setCharAt(indexOf, '\b');
							break;
						case 'f': filteredValue.setCharAt(indexOf, '\f');
							break;
					}
				}

				value = new String(filteredValue);
			}

			properties.add(name, value);
		}

		propertiesLoaded = true;
	}

	// Flag of automatic detailed error reporting
	private static boolean detailedErrorReport = false;

	// Provides possibility to write out all details about the exception on
	// standard error
	private static void detailedErrorReport(Exception e)
	{
		if (detailedErrorReport)
		{
			System.err.println(e.getClass().getName());
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	// Provides possibility to write out all details about the exception on
	// standard error
	private static void detailedErrorReport(Exception e,
		boolean alwaysNever)
	{
		if (alwaysNever)
		{
			System.err.println(e.getClass().getName());
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	// Gets the .jar pathname (if no .jar pathname has been specified)
	private static String getJarPathName()
	{
		StringBuffer jarName = new StringBuffer(TextFile.class.
			getProtectionDomain().getCodeSource().getLocation().getPath());
		int indexOf;
		if (-1 != (indexOf = jarName.lastIndexOf("/")))
			jarName.delete(0, 1 + indexOf);
		if (0 == jarName.length()) return null; // not a .jar file
		jarName.insert(0, System.getProperty("user.dir") + File.separator);
		return jarName.toString();
	}

	// Gets list of items (files and directiries) from .jar file
	private static String[] getListFromJarFile(String jarName,
		String pathName, boolean includeFiles, boolean includeDirectories)
	{
		Vector<String> aList = null;
		JarFile jarFile = null;
		int indexOf1 = -1;

		if (null != pathName)
		{
			if (pathName.isEmpty()) pathName = null; else
			{
				pathName = pathName.toUpperCase();
				String[] dirs = pathName.split("[\\\\/]+");

				Vector<String> parts = new Vector<String>();

				for (String dir : dirs)
				{
					if (dir.equals(".."))
					{
						if (!parts.isEmpty())
							parts.remove(parts.size() - 1);
					}
					else if (!dir.equals(".")) parts.add(dir);
				}

				if (parts.isEmpty()) pathName = null; else
				{
					StringBuffer newPath = new StringBuffer();

					for (String part : parts)
					{
						newPath.append(part);
						newPath.append('/');
					}

					pathName = newPath.toString();
				}
			}
		}

		try
		{
			if (null == jarName)
				jarFile = new JarFile(getJarPathName());
			else
				jarFile = new JarFile(jarName);

			Enumeration<JarEntry> jarEntries = jarFile.entries();
			aList = new Vector<String>();

			while (jarEntries.hasMoreElements())
			{
				String entryName =
					jarEntries.nextElement().getName();

				String directoryName = null;
				String fileName = null;
				int indexOf2 = -1;

				if (null != pathName)
				{
					if (!entryName.toUpperCase().
						startsWith(pathName)) continue;

					entryName = entryName.
						substring(pathName.length(),
							entryName.length());
				}

				if (-1 != (indexOf1 = entryName.indexOf("/")))
				{
					directoryName = entryName.
						substring(0, indexOf1);
				}
				else
				{
					if (-1 == (indexOf2 = entryName.
						indexOf("/", ++indexOf1)))
					{
						fileName = entryName.
							substring(indexOf1,
								entryName.length());
					}
				}

				if (includeDirectories && null != directoryName)
				{
					if (!aList.contains(directoryName))
						aList.add(directoryName);
				}

				if (includeFiles && null != fileName)
					aList.add(fileName);
			}
		}
		catch (IOException e) { }
		catch (NullPointerException e) { }
		finally
		{
			if (jarFile != null)
			{
				try { jarFile.close(); }
				catch (IOException ioe) { }
			}
		}

		if (null == aList) return null;

		String theList[] = new String[aList.size()];
		for (int i = 0; i < aList.size(); i++)
			theList[i] = aList.get(i);

		return theList;
	}


	// Finds file within current path and classpath and returns the
	// input stream
	private static InputStream getFileInputStream(String fileName)
		throws FileNotFoundException
	{
		InputStream inputStream; FileNotFoundException notFound = null;

		// First try to open the stream at current location:
		try
		{
			inputStream = new FileInputStream(fileName);
			return inputStream;
		}
		catch (FileNotFoundException e)
		{
			notFound = e;
		}

		// Then try to open the stream from current classpath:
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();

		if (classLoader instanceof URLClassLoader)
		{
			URL[] urls = ((URLClassLoader)classLoader).getURLs();

			for (URL url: urls)
			{
				try
				{
					inputStream = new FileInputStream(new File(url.toURI()).
						getCanonicalPath() + File.separator + fileName);
					return inputStream;
				}
				catch (FileNotFoundException | URISyntaxException e)
				{
					// Ignored this time… (sorry)
					// notFound = e;
				}
				catch (IOException e)
				{
					// Also ignored… (second sorry)
				}
			}
		}

		throw notFound;
	}


	// Gets input stream of resource (originally desired as file)
	private static InputStream getResourceInputStream(String fileName)
	{
		String sourceUri = fileName.replace('\\', '/');
		InputStream
			inputStream = TextFile.class.getResourceAsStream(sourceUri);

		if (null == inputStream && !sourceUri.startsWith("/"))
		{
			sourceUri = "/" + sourceUri;
			inputStream = TextFile.class.getResourceAsStream(sourceUri);
		}

		return inputStream;
	}


	// Finds file within current path and classpath and returns it; or
	// returns null if the file has not been found
	private static File findFile(String fileName) throws FileNotFoundException
	{
		File file;

		// First try to find the file at current location:
		file = new File(fileName);
		if (file.exists()) return file;

		// Then try to find the file from current classpath:
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();

		if (classLoader instanceof URLClassLoader)
		{
			URL[] urls = ((URLClassLoader)classLoader).getURLs();

			for (URL url: urls)
			{
				try
				{
					file = new File(new File(url.toURI()).
						getCanonicalPath() + File.separator + fileName);
					if (file.exists()) return file;
				}
				catch (URISyntaxException e)
				{
					// Ignored this time… (sorry)
					// notFound = e;
				}
				catch (IOException e)
				{
					// Also ignored… (second sorry)
				}
			}
		}

		return null;
	}


	// Finds the resource in current .jar file and returns its URL
	private static URL findResource(String fileName)
	{
		String sourceUri = fileName.replace('\\', '/');
		URL url = TextFile.class.getResource(sourceUri);

		if (null == url && !sourceUri.startsWith("/"))
		{
			sourceUri = "/" + sourceUri;
			url = TextFile.class.getResource(sourceUri);
		}

		return url;
	}


	// General reading method
	private void readBuffer() throws IOException
	{
		if (endOfFile) return;

		lineBuffer.setLength(0);
		String line = reader.readLine();
		checkNextLine = false;

		if (null == line) endOfFile = true;
		else if (firstLine)
		{
			if (!line.isEmpty())
			{
				// Correction for UTF-8 byte-order marker
				// (all data is read through this method and no data is read
				// not using this method so the patch is on reliable place)

				if (line.charAt(0) == '\uFEFF')
					lineBuffer.append(line.substring(1));
				else
					lineBuffer.append(line);
			}

			firstLine = false;
		}
		else lineBuffer.append(line);
	}


	// Skips the spaces at the beginning of current line; The ‘true’ return
	// value means that there is no more data available
	private boolean skipSpaces() throws IOException
	{
		int length, position;
		if (firstLine) readBuffer();

		do
		{
			do
			{
				if (endOfFile) return true;
				length = lineBuffer.length();
				if (0 == length) readBuffer();
			}
			while (0 == length);

			position = 0;

			while (position < length && ' ' == lineBuffer.charAt(position))
				++position;

			lineBuffer.delete(0, position);
			checkNextLine = true;
		}
		while (position >= length);

		return false;
	}


	/**
	 * TODO – Complete the description!
	 *
	 * @param pathName TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 *
	 * @throws FileNotFoundException if the path (or directory) does not exist
	 * @throws IllegalArgumentException TODO – Add the explanation – when!
	 */
	public static String[] getFilesList(String pathName)
		throws FileNotFoundException, IllegalArgumentException
	{
		File path = new File(pathName);
		try
		{
			checkPathValidity(path);
		}
		catch (FileNotFoundException notFound)
		{
			String theList[] = getListFromJarFile(
				null, pathName, true, false);
			if (null == theList)
			{
				if (null == pathName || !pathName.isEmpty())
					throw notFound;
				path = new File(".");
			}
			else return theList;
		}

		File itemsList[] = path.listFiles();
		Vector<String> aList = new Vector<String>();

		for (int i = 0; i < itemsList.length; i++)
			if (itemsList[i].isFile())
				aList.add(itemsList[i].getName());

		if (0 == aList.size()) return null;

		String theList[] = new String[aList.size()];
		for (int i = 0; i < aList.size(); i++)
			theList[i] = aList.get(i);

		return theList;
	}

	/**
	 * TODO – Complete the description!
	 *
	 * @param pathName TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 *
	 * @throws FileNotFoundException if the path (or directory) does not exist
	 * @throws IllegalArgumentException TODO – Add the explanation – when!
	 */
	public static String[] getDirectoriesList(String pathName)
		throws FileNotFoundException, IllegalArgumentException
	{
		File path = new File(pathName);
		try
		{
			checkPathValidity(path);
		}
		catch (FileNotFoundException notFound)
		{
			String theList[] = getListFromJarFile(
				null, pathName, false, true);
			if (null == theList)
			{
				if (null == pathName || !pathName.isEmpty())
					throw notFound;
				path = new File(".");
			}
			else return theList;
		}

		File itemsList[] = path.listFiles();
		Vector<String> aList = new Vector<String>();

		for (int i = 0; i < itemsList.length; i++)
			if (itemsList[i].isDirectory())
				aList.add(itemsList[i].getName());

		if (0 == aList.size()) return null;

		String theList[] = new String[aList.size()];
		for (int i = 0; i < aList.size(); i++)
			theList[i] = aList.get(i);

		return theList;
	}

	/**
	 * TODO – Complete the description!
	 *
	 * @param pathName TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 *
	 * @throws FileNotFoundException if the path (or directory) does not exist
	 * @throws IllegalArgumentException TODO – Add the explanation – when!
	 */
	public static String[] getFilesAndDirectoriesList(String pathName)
		throws FileNotFoundException, IllegalArgumentException
	{
		File path = new File(pathName);
		try
		{
			checkPathValidity(path);
		}
		catch (FileNotFoundException notFound)
		{
			String theList[] = getListFromJarFile(
				null, pathName, true, true);
			if (null == theList)
			{
				if (null == pathName || !pathName.isEmpty())
					throw notFound;
				path = new File(".");
			}
			else return theList;
		}

		File itemsList[] = path.listFiles();
		Vector<String> aList = new Vector<String>();

		for (int i = 0; i < itemsList.length; i++)
			if (itemsList[i].isFile() ||
				itemsList[i].isDirectory())
				aList.add(itemsList[i].getName());

		if (0 == aList.size()) return null;

		String theList[] = new String[aList.size()];
		for (int i = 0; i < aList.size(); i++)
			theList[i] = aList.get(i);

		return theList;
	}

	/**
	 * TODO – Complete the description!
	 *
	 * @param pathName TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 *
	 * @throws FileNotFoundException if the path (or directory) does not exist
	 * @throws IllegalArgumentException TODO – Add the explanation – when!
	 */
	public static String[] getList(String pathName)
		throws FileNotFoundException, IllegalArgumentException
	{
		File path = new File(pathName);
		try
		{
			checkPathValidity(path);
		}
		catch (FileNotFoundException notFound)
		{
			String theList[] = getListFromJarFile(
				null, pathName, true, true);
			if (null == theList)
			{
				if (null == pathName || !pathName.isEmpty())
					throw notFound;
				path = new File(".");
			}
			else return theList;
		}

		File itemsList[] = path.listFiles();
		Vector<String> aList = new Vector<String>();

		for (int i = 0; i < itemsList.length; i++)
			aList.add(itemsList[i].getName());

		if (0 == aList.size()) return null;

		String theList[] = new String[aList.size()];
		for (int i = 0; i < aList.size(); i++)
			theList[i] = aList.get(i);

		return theList;
	}


	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 */
	public static boolean exists(String name)
	{ return new File(name).exists(); }


	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 * @param parentsToo TODO – Add the parameter description!
	 *
	 * @return {@code true} on success, {@code false} otherwise
	 */
	public static boolean makeDirectory(String name, boolean parentsToo)
	{ return parentsToo ? new File(name).mkdirs() : new File(name).mkdir(); }


	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 *
	 * @return {@code true} on success, {@code false} otherwise
	 */
	public static boolean makeDirectory(String name)
	{ return new File(name).mkdir(); }


	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 */
	public static boolean isFile(String name)
	{ return new File(name).isFile(); }


	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 */
	public static boolean isDirectory(String name)
	{ return new File(name).isDirectory(); }


	/**
	 * Compares the contents of two files. If both files does not exist the
	 * method returs true, if only one of the files exist exception is thrown.
	 * If both files exist, they are compared and the result of compariso is
	 * returned as boolean value: true means that the contents are identical.
	 *
	 * @param name1 name (and path) of first file to compare
	 * @param name2 name (and path) of second file to compare
	 *
	 * @return {@code true} – both files exist and their contents are
	 *     identical; {@code false} – both files does not exist or both exist,
	 *     but their contents are not identical
	 *
	 * @throws FileNotFoundException if only one of the files does not exist
	 * @throws IOException if one of entered names does not point to a file,
	 *     or one of files is not readable…
	 */
	public static boolean compare(String name1, String name2)
		throws FileNotFoundException, IOException
	{
		File file1 = findFile(name1);
		File file2 = findFile(name2);

		InputStream readFrom1 = null;
		InputStream readFrom2 = null;

		boolean bothAreFiles = true;

		if (null != file1)
		{
			if (!file1.isFile())
				throw new IOException("The first file “" + name1 +
					"” is not a file.");

			readFrom1 = new FileInputStream(file1);
		}
		else
		{
			try
			{
				readFrom1 = getResourceInputStream(name1);
				bothAreFiles = false;
			}
			catch (Exception e)
			{
				readFrom1 = null;
			}
		}

		if (null != file2)
		{
			if (!file2.isFile())
				throw new IOException("The second file “" + name2 +
					"” is not a file.");

			readFrom2 = new FileInputStream(file2);
		}
		else
		{
			try
			{
				readFrom2 = getResourceInputStream(name2);
				bothAreFiles = false;
			}
			catch (Exception e)
			{
				readFrom2 = null;
			}
		}

		if (null == readFrom1)
		{
			if (null == readFrom2)
			{
				// Both non-existing files matches
				return true;
			}

			throw new FileNotFoundException("The first file “" + name1 +
				"” does not exist.");
		}

		if (null == readFrom2)
		{
			throw new FileNotFoundException("The second file “" + name2 +
				"” does not exist.");
		}

		if (bothAreFiles)
		{
			if (file1.length() != file2.length())
			{
				readFrom1.close();
				readFrom2.close();
				return false;
			}

			if (file1.getCanonicalFile().equals(file2.getCanonicalFile()))
			{
				readFrom1.close();
				readFrom2.close();
				return true;
			}
		}

		byte[] buffer1 = new byte[32768];
		byte[] buffer2 = new byte[32768];
		boolean sameContent = true;

		int bytesRead1 = readFrom1.read(buffer1);
		int bytesRead2 = readFrom2.read(buffer2);
		int ptr1 = 0, ptr2 = 0;

		// The point is that each of buffers can be loaded by different
		// chunk sizes. Despite of this fact, the files can match!
		if (bytesRead1 > 0 || bytesRead2 > 0)
		{
			while (ptr1 < bytesRead1 && ptr2 < bytesRead2)
			{
				if (buffer1[ptr1] != buffer2[ptr2])
				{
					sameContent = false;
					break;
				}

				++ptr1; ++ptr2;

				if (ptr1 >= bytesRead1)
				{
					bytesRead1 = readFrom1.read(buffer1);
					ptr1 = 0;
				}

				if (ptr2 >= bytesRead2)
				{
					bytesRead2 = readFrom2.read(buffer2);
					ptr2 = 0;
				}
			}

			if (sameContent && (bytesRead1 > 0 || bytesRead2 > 0))
				sameContent = false;
		}

		readFrom1.close();
		readFrom2.close();
		return sameContent;
	}

	/**
	 * TODO – Complete the description!
	 *
	 * @param source TODO – Add the parameter description!
	 * @param target TODO – Add the parameter description!
	 * @param overwrite TODO – Add the parameter description!
	 *
	 * @throws FileNotFoundException if the source file does not exist
	 * @throws IOException may raise after some I/O operation failed
	 */
	public static void copy(String source, String target,
		boolean overwrite) throws FileNotFoundException, IOException
	{
		File sourceFile = findFile(source);
		File targetFile = new File(target);

		if (!overwrite && targetFile.exists())
			throw new IOException("The target file “" + target +
				"” already exists.");
		if (targetFile.exists() && !targetFile.isFile())
			throw new IOException("The target file “" + target +
				"” is not a file.");

		InputStream readFrom;

		if (null != sourceFile)
		{
			if (!sourceFile.isFile())
				throw new IOException("The source file “" + source +
					"” is not a file.");

			readFrom = new FileInputStream(sourceFile);
		}
		else
		{
			try
			{
				readFrom = getResourceInputStream(source);
			}
			catch (Exception e)
			{
				readFrom = null;
			}
		}

		if (null == readFrom)
		{
			throw new FileNotFoundException("The source file “" +
				source + "” does not exist.");
		}

		FileOutputStream writeTo = new FileOutputStream(targetFile);

		byte[] buffer = new byte[32768];
		int bytesRead;

		while ((bytesRead = readFrom.read(buffer)) > 0)
		{
			writeTo.write(buffer, 0, bytesRead);
		}

		readFrom.close();
		writeTo.close();

		if (null != sourceFile && sourceFile.exists())
			targetFile.setLastModified(sourceFile.lastModified());
		else
		{
			URL url = findResource(source);
			long time = url.openConnection().getLastModified();
			if (0 != time) targetFile.setLastModified(time);
		}
	}

	/**
	 * TODO – Complete the description!
	 *
	 * @param source TODO – Add the parameter description!
	 * @param target TODO – Add the parameter description!
	 *
	 * @throws FileNotFoundException if the source file does not exist
	 * @throws IOException may raise after some I/O operation failed
	 */
	public static void copy(String source, String target)
		throws FileNotFoundException, IOException
	{ copy(source, target, false); }

	/**
	 * TODO – Complete the description!
	 *
	 * @param source TODO – Add the parameter description!
	 * @param target TODO – Add the parameter description!
	 *
	 * @throws FileNotFoundException if the source file does not exist
	 * @throws IOException may raise after some I/O operation failed
	 */
	public static void append(String source, String target)
		throws FileNotFoundException, IOException
	{
		File sourceFile = findFile(source);
		File targetFile = new File(target);

		if (targetFile.exists() && !targetFile.isFile())
			throw new IOException("The target file “" + target +
				"” is not a file!");

		InputStream readFrom;

		if (null != sourceFile)
		{
			if (!sourceFile.isFile())
				throw new IOException("The source file “" + source +
					"” is not a file!");

			readFrom = new FileInputStream(sourceFile);
		}
		else
		{
			try
			{
				readFrom = getResourceInputStream(source);
			}
			catch (Exception e)
			{
				readFrom = null;
			}
		}

		if (null == readFrom)
		{
			throw new FileNotFoundException("The source file “" + source +
				"” does not exist.");
		}

		FileOutputStream writeTo = new FileOutputStream(targetFile, true);

		byte[] buffer = new byte[32768];
		int bytesRead;

		while ((bytesRead = readFrom.read(buffer)) > 0)
		{
			writeTo.write(buffer, 0, bytesRead);
		}

		readFrom.close();
		writeTo.close();
	}


	/**
	 * TODO – Complete the description!
	 *
	 * @param source TODO – Add the parameter description!
	 * @param target TODO – Add the parameter description!
	 *
	 * @return {@code true} on success, {@code false} otherwise
	 */
	public static boolean rename(String source, String target)
	{ return new File(source).renameTo(new File(target)); }


	/**
	 * TODO – Complete the description!
	 *
	 * @param source TODO – Add the parameter description!
	 * @param targetPathName TODO – Add the parameter description!
	 *
	 * @return {@code true} on success, {@code false} otherwise
	 */
	public static boolean move(String source, String targetPathName)
	{
		File sourceEntry = new File(source);
		File targetEntry = new File(targetPathName);

		return sourceEntry.renameTo(new
			File(targetEntry, sourceEntry.getName()));
	}


	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 *
	 * @return {@code true} on success, {@code false} otherwise
	 */
	public static boolean delete(String name)
	{ return new File(name).delete(); }


	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 */
	public static long lastModified(String name)
	{ return new File(name).lastModified(); }


	/**
	 * TODO – Complete the description!
	 *
	 * @return TODO – Add return value description!
	 */
	public String propertiesNamespace()
	{ return propertiesNamespace; }

	/**
	 * TODO – Complete the description!
	 *
	 * @param newNamespace TODO – Add the parameter description!
	 */
	public void propertiesNamespace(String newNamespace)
	{
		if (null == newNamespace)
		{
			propertiesNamespace = null;
			return;
		}

		if (newNamespace.equals(""))
			throw new IllegalArgumentException(
				"The namespace must not be empty.");

		if (newNamespace.endsWith("."))
			throw new IllegalArgumentException(
				"The namespace must not end with the dot character.");

		if (newNamespace.startsWith(";"))
			throw new IllegalArgumentException(
				"The namespace must not start with the comment character.");

		if (newNamespace.startsWith("."))
			throw new IllegalArgumentException(
				"The namespace must not start with the dot character.");

		if (-1 != newNamespace.indexOf('='))
			throw new IllegalArgumentException(
				"The namespace must not include the equal sign.");

		propertiesNamespace = newNamespace;
	}


	/**
	 * TODO – Complete the description!
	 *
	 * @param title TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 */
	public static String dialogLoad(String title)
	{
		FileDialog dialog = new FileDialog(
			(javax.swing.JFrame)null, title, FileDialog.LOAD);
		dialog.setVisible(true);
		if (null != dialog.getFile())
			return dialog.getDirectory() + File.separator + dialog.getFile();
		return null;
	}


	/**
	 * TODO – Complete the description!
	 *
	 * @param title TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 */
	public static String dialogSave(String title)
	{
		FileDialog dialog = new FileDialog(
			(javax.swing.JFrame)null, title, FileDialog.SAVE);
		dialog.setVisible(true);
		if (null != dialog.getFile())
			return dialog.getDirectory() + File.separator + dialog.getFile();
		return null;
	}


	/**
	 * This method will search for the file at available locations.
	 * It will search the files indended to be open for reading.
	 * First the current location is searched, then the classpath,
	 * and the last location is current .jar file where the classes
	 * are packed in.
	 *
	 * @param fileName the file name (indended to be open for reading)
	 * @return real location of the file or null, if the file has not
	 *     been found on any available location
	 */
	public static String whereIsFile(String fileName)
	{
		try
		{
			File file = findFile(fileName);
			if (null != file) return file.getCanonicalPath();
		}
		catch (FileNotFoundException e)
		{
			// Ignored… (sorry)
		}
		catch (IOException e)
		{
			// Ignored… (sorry)
		}

		try
		{
			URL url = findResource(fileName);
			if (null != url)
				return URLDecoder.decode(url.toString(), "UTF-8");
				// Warning! Here should stay UTF-8 encoding because here
				// the conversion for Java Virtual Machine is performed!
		}
		catch (IOException e)
		{
			// Ignored… (sorry)
		}

		return null;
	}


	/**
	 * Opens the file for writing. If the file exists, it content will be
	 * cleared (truncated to zero). This is necessary for configuration files.
	 *
	 * @param fileName name of the file that should be open for writing
	 *
	 * @throws IOException may raise after some I/O operation failed
	 * @throws NullPointerException if the fileName is null
	 */
	public void openForWriting(String fileName)
		throws IOException, NullPointerException
	{
		if (null == fileName)
			throw new NullPointerException(
				"The file name must not be concealed!");

		close();

		writer = new BufferedWriter(new OutputStreamWriter(
			new FileOutputStream(fileName), "UTF-8"));
	}

	/**
	 * Opens file for writing. The second parameter allows to specify whether
	 * the file should be appended. True value of second parameter will cause
	 * that the content of the file will be kept and next content will be
	 * appended to the end of file.
	 *
	 * @param fileName name of the file that should be open for writing
	 * @param append flag that allows to open the file for appending; true
	 *     means that the original content of the file will be kept untouched
	 *     and consequent write operations will append the new content to the
	 *     end of the file; Warning! Appending must not be applied to
	 *     configuration files! Because such “appended” configuration files
	 *     would became unreadable.
	 *
	 * @throws IOException may raise after some I/O operation failed
	 * @throws NullPointerException if the fileName is null
	 */
	public void openForWriting(String fileName, boolean append)
		throws IOException, NullPointerException
	{
		if (null == fileName)
			throw new NullPointerException(
				"The file name must not be concealed.");

		close();

		writer = new BufferedWriter(new OutputStreamWriter(
			new FileOutputStream(fileName, append), "UTF-8"));
	}

	/**
	 * Opens file for reading. First the file is searched at current path,
	 * then the classpath is searched and at last the method attempts to find
	 * the file in current .jar file. If the file does not exist anywhere an
	 * exception is thrown.
	 *
	 * @param fileName name of the file that should be open for reading
	 *
	 * @throws IOException may raise after some I/O operation failed
	 * @throws NullPointerException if the fileName is null
	 */
	public void openForReading(String fileName)
		throws IOException, NullPointerException
	{
		if (null == fileName)
			throw new NullPointerException(
				"The file name must not be concealed.");

		close();
		FileNotFoundException notFound = null;

		// Try to open the stream from at current
		// location or from current classpath:
		try
		{
			reader = new BufferedReader(new InputStreamReader(
				getFileInputStream(fileName), "UTF-8"));

			firstLine = true;
			endOfFile = false;
			checkNextLine = true;

			propertiesLoaded = false;
			properties.clear();
			return;
		}
		catch (FileNotFoundException e)
		{
			notFound = e;
		}

		// Last attempt – load the file as resource:
		try
		{
			reader = new BufferedReader(new InputStreamReader(
				getResourceInputStream(fileName), "UTF-8"));

			firstLine = true;
			endOfFile = false;
			checkNextLine = true;

			propertiesLoaded = false;
			properties.clear();
		}
		catch (NullPointerException isNull)
		{
			if (null != notFound) throw notFound;
			throw isNull;
		}
	}

	/**
	 * Closes the file and flushes the internal buffers of the file.
	 * After calling this method, data can be no longer read from or
	 * written to this file.
	 *
	 * @throws IOException may raise after some I/O operation failed
	 */
	public void close() throws IOException
	{
		if (null != writer)
		{
			writeAllProperties();
			writer.close();
			writer = null;

			if (!keepUnusedProperties)
				for (Property property : properties)
					property.toWrite = false;
		}

		if (null != reader)
		{
			reader.close();
			reader = null;
		}
	}


	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 *
	 * @throws IOException may raise after some I/O operation failed
	 * @throws IllegalArgumentException if the property name does not fulfil
	 *     some of necessary conditions
	 */
	public boolean propertyExists(String name)
		throws IOException, IllegalArgumentException
	{
		name = name.trim();
		checkNameValidity(name);

		if (null != propertiesNamespace)
			name = propertiesNamespace + '.' + name;

		readProperties();
		return -1 != properties.indexOf(name);
	}


	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 * @param defaultValue TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 *
	 * @throws IOException may raise after some I/O operation failed
	 * @throws IllegalArgumentException if the property name does not fulfil
	 *     some of necessary conditions
	 */
	public String readProperty(String name, String defaultValue)
		throws IOException, IllegalArgumentException
	{
		name = name.trim();
		checkNameValidity(name);

		if (null != propertiesNamespace)
			name = propertiesNamespace + '.' + name;

		readProperties(); int indexOf;
		if (-1 == (indexOf = properties.indexOf(name)))
		{
			if (null == defaultValue) return null;
			return new String(defaultValue);
		}

		String value = properties.get(indexOf).value;
		if (null == value) return null;
		return new String(value);
	}

	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 * @param defaultValue TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 *
	 * @throws IOException may raise after some I/O operation failed
	 * @throws IllegalArgumentException if the property name does not fulfil
	 *     some of necessary conditions
	 */
	public StringBuffer readProperty(String name, StringBuffer defaultValue)
		throws IOException, IllegalArgumentException
	{
		name = name.trim();
		checkNameValidity(name);

		if (null != propertiesNamespace)
			name = propertiesNamespace + '.' + name;

		readProperties(); int indexOf;
		if (-1 == (indexOf = properties.indexOf(name)))
		{
			if (null == defaultValue) return null;
			return new StringBuffer(defaultValue);
		}

		String value = properties.get(indexOf).value;
		if (null == value) return null;
		return new StringBuffer(value);
	}

	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 * @param defaultValue TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 *
	 * @throws IOException may raise after some I/O operation failed
	 * @throws IllegalArgumentException if the property name does not fulfil
	 *     some of necessary conditions
	 */
	public Long readProperty(String name, Long defaultValue)
		throws IOException, IllegalArgumentException
	{
		name = name.trim();
		checkNameValidity(name);

		if (null != propertiesNamespace)
			name = propertiesNamespace + '.' + name;

		readProperties(); int indexOf;
		if (-1 == (indexOf = properties.indexOf(name)))
		{
			if (null == defaultValue) return null;
			return new Long(defaultValue);
		}

		String value = properties.get(indexOf).value;
		if (null == value) return null;
		value = value.trim();
		if (value.equalsIgnoreCase(nullString)) return null;
		return Long.valueOf(value);
	}

	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 * @param defaultValue TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 *
	 * @throws IOException may raise after some I/O operation failed
	 * @throws IllegalArgumentException if the property name does not fulfil
	 *     some of necessary conditions
	 */
	public Double readProperty(String name, Double defaultValue)
		throws IOException, IllegalArgumentException
	{
		name = name.trim();
		checkNameValidity(name);

		if (null != propertiesNamespace)
			name = propertiesNamespace + '.' + name;

		readProperties(); int indexOf;
		if (-1 == (indexOf = properties.indexOf(name)))
		{
			if (null == defaultValue) return null;
			return new Double(defaultValue);
		}

		String value = properties.get(indexOf).value;
		if (null == value) return null;
		value = value.trim();
		if (value.equalsIgnoreCase(nullString)) return null;
		return Double.valueOf(value);
	}

	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 * @param defaultValue TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 *
	 * @throws IOException may raise after some I/O operation failed
	 * @throws IllegalArgumentException if the property name does not fulfil
	 *     some of necessary conditions
	 */
	public Boolean readProperty(String name, Boolean defaultValue)
		throws IOException, IllegalArgumentException
	{
		name = name.trim();
		checkNameValidity(name);

		if (null != propertiesNamespace)
			name = propertiesNamespace + '.' + name;

		readProperties(); int indexOf;
		if (-1 == (indexOf = properties.indexOf(name)))
		{
			if (null == defaultValue) return null;
			return new Boolean(defaultValue);
		}

		String value = properties.get(indexOf).value;
		if (null == value) return null;
		value = value.trim();
		if (value.equalsIgnoreCase(nullString)) return null;
		return Boolean.valueOf(value);
	}

	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 * @param defaultValue TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 *
	 * @throws IOException may raise after some I/O operation failed
	 * @throws IllegalArgumentException if the property name does not fulfil
	 *     some of necessary conditions
	 */
	public int[] readProperty(String name, int[] defaultValue)
		throws IOException, IllegalArgumentException
	{
		name = name.trim();
		checkNameValidity(name);

		if (null != propertiesNamespace)
			name = propertiesNamespace + '.' + name;

		readProperties(); int indexOf;
		int[] array;

		if (-1 == (indexOf = properties.indexOf(name)))
		{
			if (null == defaultValue) return null;
			array = new int[defaultValue.length];
			for (int i = 0; i < defaultValue.length; ++i)
				array[i] = defaultValue[i];
			return array;
		}

		String stringValue = properties.get(indexOf).value;
		if (null == stringValue) return null;
		StringBuffer string = new StringBuffer(stringValue);
		Vector<Long> vector = new Vector<Long>();
		Long value;

		while (null != (value = getLong(string)))
			vector.add(value);

		if (0 == vector.size() && null != defaultValue)
		{
			array = new int[defaultValue.length];
			for (int i = 0; i < defaultValue.length; ++i)
				array[i] = defaultValue[i];
			return array;
		}

		array = new int[vector.size()];
		for (int i = 0; i < vector.size(); ++i)
			array[i] = vector.elementAt(i).intValue();

		return array;
	}

	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 * @param defaultValue TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 *
	 * @throws IOException may raise after some I/O operation failed
	 * @throws IllegalArgumentException if the property name does not fulfil
	 *     some of necessary conditions
	 */
	public float[] readProperty(String name, float[] defaultValue)
		throws IOException, IllegalArgumentException
	{
		name = name.trim();
		checkNameValidity(name);

		if (null != propertiesNamespace)
			name = propertiesNamespace + '.' + name;

		readProperties(); int indexOf;
		float[] array;

		if (-1 == (indexOf = properties.indexOf(name)))
		{
			if (null == defaultValue) return null;
			array = new float[defaultValue.length];
			for (int i = 0; i < defaultValue.length; ++i)
				array[i] = defaultValue[i];
			return array;
		}

		String stringValue = properties.get(indexOf).value;
		if (null == stringValue) return null;
		StringBuffer string = new StringBuffer(stringValue);
		Vector<Double> vector = new Vector<Double>();
		Double value;

		while (null != (value = getDouble(string)))
			vector.add(value);

		if (0 == vector.size() && null != defaultValue)
		{
			array = new float[defaultValue.length];
			for (int i = 0; i < defaultValue.length; ++i)
				array[i] = defaultValue[i];
			return array;
		}

		array = new float[vector.size()];
		for (int i = 0; i < vector.size(); ++i)
			array[i] = vector.elementAt(i).floatValue();

		return array;
	}

	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 * @param defaultValue TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 *
	 * @throws IOException may raise after some I/O operation failed
	 * @throws IllegalArgumentException if the property name does not fulfil
	 *     some of necessary conditions
	 */
	public long[] readProperty(String name, long[] defaultValue)
		throws IOException, IllegalArgumentException
	{
		name = name.trim();
		checkNameValidity(name);

		if (null != propertiesNamespace)
			name = propertiesNamespace + '.' + name;

		readProperties(); int indexOf;
		long[] array;

		if (-1 == (indexOf = properties.indexOf(name)))
		{
			if (null == defaultValue) return null;
			array = new long[defaultValue.length];
			for (int i = 0; i < defaultValue.length; ++i)
				array[i] = defaultValue[i];
			return array;
		}

		String stringValue = properties.get(indexOf).value;
		if (null == stringValue) return null;
		StringBuffer string = new StringBuffer(stringValue);
		Vector<Long> vector = new Vector<Long>();
		Long value;

		while (null != (value = getLong(string)))
			vector.add(value);

		if (0 == vector.size() && null != defaultValue)
		{
			array = new long[defaultValue.length];
			for (int i = 0; i < defaultValue.length; ++i)
				array[i] = defaultValue[i];
			return array;
		}

		array = new long[vector.size()];
		for (int i = 0; i < vector.size(); ++i)
			array[i] = vector.elementAt(i).longValue();

		return array;
	}

	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 * @param defaultValue TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 *
	 * @throws IOException may raise after some I/O operation failed
	 * @throws IllegalArgumentException if the property name does not fulfil
	 *     some of necessary conditions
	 */
	public double[] readProperty(String name, double[] defaultValue)
		throws IOException, IllegalArgumentException
	{
		name = name.trim();
		checkNameValidity(name);

		if (null != propertiesNamespace)
			name = propertiesNamespace + '.' + name;

		readProperties(); int indexOf;
		double[] array;

		if (-1 == (indexOf = properties.indexOf(name)))
		{
			if (null == defaultValue) return null;
			array = new double[defaultValue.length];
			for (int i = 0; i < defaultValue.length; ++i)
				array[i] = defaultValue[i];
			return array;
		}

		String stringValue = properties.get(indexOf).value;
		if (null == stringValue) return null;
		StringBuffer string = new StringBuffer(stringValue);
		Vector<Double> vector = new Vector<Double>();
		Double value;

		while (null != (value = getDouble(string)))
			vector.add(value);

		if (0 == vector.size() && null != defaultValue)
		{
			array = new double[defaultValue.length];
			for (int i = 0; i < defaultValue.length; ++i)
				array[i] = defaultValue[i];
			return array;
		}

		array = new double[vector.size()];
		for (int i = 0; i < vector.size(); ++i)
			array[i] = vector.elementAt(i).doubleValue();

		return array;
	}

	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 * @param defaultValue TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 *
	 * @throws IOException may raise after some I/O operation failed
	 * @throws IllegalArgumentException if the property name does not fulfil
	 *     some of necessary conditions
	 */
	public boolean[] readProperty(String name, boolean[] defaultValue)
		throws IOException, IllegalArgumentException
	{
		name = name.trim();
		checkNameValidity(name);

		if (null != propertiesNamespace)
			name = propertiesNamespace + '.' + name;

		readProperties(); int indexOf;
		boolean[] array;

		if (-1 == (indexOf = properties.indexOf(name)))
		{
			if (null == defaultValue) return null;
			array = new boolean[defaultValue.length];
			for (int i = 0; i < defaultValue.length; ++i)
				array[i] = defaultValue[i];
			return array;
		}

		String stringValue = properties.get(indexOf).value;
		if (null == stringValue) return null;
		StringBuffer string = new StringBuffer(stringValue);
		Vector<Boolean> vector = new Vector<Boolean>();
		Boolean value;

		while (null != (value = getBoolean(string)))
			vector.add(value);

		if (0 == vector.size() && null != defaultValue)
		{
			array = new boolean[defaultValue.length];
			for (int i = 0; i < defaultValue.length; ++i)
				array[i] = defaultValue[i];
			return array;
		}

		array = new boolean[vector.size()];
		for (int i = 0; i < vector.size(); ++i)
			array[i] = vector.elementAt(i).booleanValue();

		return array;
	}

	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 * @param defaultValue TODO – Add the parameter description!
	 *
	 * @return TODO – Add return value description!
	 *
	 * @throws IOException may raise after some I/O operation failed
	 * @throws IllegalArgumentException if the property name does not fulfil
	 *     some of necessary conditions
	 */
	public char[] readProperty(String name, char[] defaultValue)
		throws IOException, IllegalArgumentException
	{
		name = name.trim();
		checkNameValidity(name);

		if (null != propertiesNamespace)
			name = propertiesNamespace + '.' + name;

		readProperties(); int indexOf;
		char[] array;

		if (-1 == (indexOf = properties.indexOf(name)))
		{
			if (null == defaultValue) return null;
			array = new char[defaultValue.length];
			for (int i = 0; i < defaultValue.length; ++i)
				array[i] = defaultValue[i];
			return array;
		}

		String value = properties.get(indexOf).value;
		if (null == value) return null;
		StringBuffer string = new StringBuffer(value);

		array = new char[string.length()];
		for (int i = 0; i < string.length(); ++i)
			array[i] = string.charAt(i);

		return array;
	}


	/**
	 * TODO – Complete the description!
	 *
	 * @return TODO – Add return value description!
	 */
	public boolean keepsUnusedProperties()
	{ return keepUnusedProperties; }


	/**
	 * TODO – Complete the description!
	 */
	public void keepUnusedProperties() 
	{ keepUnusedProperties = true; }


	/**
	 * TODO – Complete the description!
	 */
	public void dismissUnusedProperties()
	{ keepUnusedProperties = false; }


	/**
	 * Clears all the properties possibly stored in memory after last file read.
	 */
	public void clearProperties()
	{
		properties.clear();
		propertiesLoaded = false;
		propertiesNamespace = null;
		lastWrittenProperty = -1;
	}


	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 *
	 * @throws IOException may raise after some I/O operation failed
	 * @throws IllegalArgumentException if the property name does not fulfil
	 *     some of necessary conditions
	 */
	public void deleteProperty(String name)
		throws IOException, IllegalArgumentException
	{
		name = name.trim();
		checkNameValidity(name);

		if (null != propertiesNamespace)
			name = propertiesNamespace + '.' + name;

		if (null == writer)
			throw new IOException("There is no file open for writing.");

		int indexOf;

		if (-1 != (indexOf = properties.indexOf(name)))
			properties.remove(indexOf);

		if (lastWrittenProperty >= properties.size())
			lastWrittenProperty = properties.size() - 1;
	}


	/**
	 * TODO – Complete the description!
	 *
	 * @param name TODO – Add the parameter description!
	 * @param value TODO – Add the parameter description!
	 *
	 * @throws IOException may raise after some I/O operation failed
	 * @throws IllegalArgumentException if the property name does not fulfil
	 *     some of necessary conditions
	 */
	public void writeProperty(String name, Object value)
		throws IOException, IllegalArgumentException
	{
		name = name.trim();
		checkNameValidity(name);

		if (null != propertiesNamespace)
			name = propertiesNamespace + '.' + name;

		if (null == writer)
			throw new IOException("There is no file open for writing.");

		int indexOf;

		if (null == value)
		{
			if (-1 != (indexOf = properties.indexOf(name)))
			{
				Property property = properties.get(indexOf);
				property.value = null;
				property.toWrite = true;
				lastWrittenProperty = indexOf;
			}
			else
			{
				properties.add(name, null, true);
				lastWrittenProperty = properties.size() - 1;
			}

			return;
		}

		StringBuffer string;

		if (value instanceof int[])
		{
			int[] array = (int[])value;
			string = new StringBuffer();
			for (int i = 0; i < array.length; ++i)
				string.append(array[i] + " ");
			value = string;
		}
		else if (value instanceof float[])
		{
			float[] array = (float[])value;
			string = new StringBuffer();
			for (int i = 0; i < array.length; ++i)
				string.append(array[i] + " ");
			value = string;
		}
		else if (value instanceof long[])
		{
			long[] array = (long[])value;
			string = new StringBuffer();
			for (int i = 0; i < array.length; ++i)
				string.append(array[i] + " ");
			value = string;
		}
		else if (value instanceof double[])
		{
			double[] array = (double[])value;
			string = new StringBuffer();
			for (int i = 0; i < array.length; ++i)
				string.append(array[i] + " ");
			value = string;
		}
		else if (value instanceof boolean[])
		{
			boolean[] array = (boolean[])value;
			string = new StringBuffer();
			for (int i = 0; i < array.length; ++i)
				string.append(array[i] + " ");
			value = string;
		}
		else if (value instanceof char[])
		{
			string = new StringBuffer();
			string.append((char[])value);
			value = string;
		}

		if (-1 != (indexOf = properties.indexOf(name)))
		{
			Property property = properties.get(indexOf);
			property.value = value.toString();
			property.toWrite = true;
			lastWrittenProperty = indexOf;
		}
		else
		{
			properties.add(name, value.toString(), true);
			lastWrittenProperty = properties.size() - 1;
		}
	}


	/**
	 * TODO – Complete the description!
	 *
	 * @param comment the content of the comment
	 */
	public void writePropertiesComment(String comment)
	{
		++lastWrittenProperty;
		if (lastWrittenProperty >= properties.size())
		{
			properties.add(";", comment, true);
			lastWrittenProperty = properties.size();
		}
		else
		{
			Property property = properties.get(lastWrittenProperty);
			if (property.name.equals(";"))
			{
				property.value = comment;
				property.toWrite = true;
			}
			else
				properties.insert(lastWrittenProperty, ";", comment, true);
		}
	}


	/**
	 * TODO – Complete the description!
	 */
	public void writePropertiesEmptyLine()
	{
		++lastWrittenProperty;
		if (lastWrittenProperty >= properties.size())
		{
			properties.add("", null, true);
			lastWrittenProperty = properties.size();
		}
		else
		{
			Property property = properties.get(lastWrittenProperty);
			if (property.name.equals(""))
				property.toWrite = true;
			else
				properties.insert(lastWrittenProperty, "", null, true);
		}
	}


	/**
	 * Checks if the current reading position is at the end of the line.
	 * The file must be open for reading.
	 *
	 * @return {@code true} if current reading position is at the end of the
	 *     line; {@code false} otherwise
	 */
	public boolean endOfLine() throws IOException
	{
		if (null == reader)
			throw new IOException("There is no file open for reading.");

		if (firstLine) readBuffer();
		return 0 == lineBuffer.length();
	}

	/**
	 * Checks if the input buffer and stream are empty. The file must be open
	 * for reading.
	 *
	 * @return {@code true} if there are no more data in the buffer or input
	 *     stream; {@code false} otherwise
	 */
	public boolean endOfFile() throws IOException
	{
		if (null == reader)
			throw new IOException("There is no file open for reading.");

		if (firstLine || (0 == lineBuffer.length() && checkNextLine))
			readBuffer();
		return endOfFile;
	}


	/**
	 * Reads and returns the line of text from the file open for reading.
	 * If there is no more data in the file, this method returns null.
	 *
	 * @return read line of text or null
	 *
	 * @throws IOException may raise after some I/O operation failed
	 */
	public String readLine() throws IOException
	{
		if (null == reader)
			throw new IOException("There is no file open for reading.");

		if (firstLine || (0 == lineBuffer.length() && checkNextLine))
			readBuffer();
		if (endOfFile) return null;

		String line = new String(lineBuffer);
		lineBuffer.setLength(0);
		checkNextLine = true;
		return line;
	}

	/**
	 * Allows to finish reading of currently processed line of text read from
	 * text file. It reads the rest of the line stored in internal buffer and
	 * clears the buffer. This method never returns null.
	 *
	 * @return rest of the text line read or empty string
	 *
	 * @throws IOException may raise after some I/O operation failed
	 */
	public String finishReadLine() throws IOException
	{
		if (endOfLine())
		{
			checkNextLine = true;
			return "";
		}

		String line = new String(lineBuffer);
		lineBuffer.setLength(0);
		checkNextLine = true;
		return line;
	}

	/**
	 * Prečíta zo súboru otvoreného na nasledujúci znak a vráti jeho
	 * hodnotu. Znaky nových riadkov sú touto metódou ignorované. Ak
	 * metóda dosiahne koniec súboru, vráti hodnotu {@code valnull}
	 *
	 * @return Character value read from file or null
	 *
	 * @throws IOException may raise after some I/O operation failed
	 */
	public Character readCharacter() throws IOException
	{
		if (null == reader)
			throw new IOException("There is no file open for reading.");

		if (firstLine) readBuffer();

		while (0 == lineBuffer.length())
		{
			readBuffer();
			if (endOfFile) return null;
		}

		Character ch = new Character(lineBuffer.charAt(0));
		lineBuffer.deleteCharAt(0);
		checkNextLine = true;
		return ch;
	}


	/**
	 * Reads and returns next Long value from the file open for reading.
	 * This method returns null if there is no more data in the file or the
	 * value at the current reading position is not a valid Long value.
	 *
	 * @return Long value read from file or null
	 *
	 * @throws IOException may raise after some I/O operation failed
	 */
	public Long readLong() throws IOException
	{
		if (skipSpaces()) return null;

		Long number = null;
		int indexOf = lineBuffer.indexOf(" ");

		if (-1 == indexOf)
		{
			try
			{
				number = Long.valueOf(lineBuffer.toString());
				lineBuffer.setLength(0);
				checkNextLine = true;
			}
			catch (Exception e) { detailedErrorReport(e, false); }
		}
		else
		{
			try
			{
				number = Long.valueOf(lineBuffer.substring(0, indexOf));
				lineBuffer.delete(0, indexOf + 1);
				checkNextLine = true;
			}
			catch (Exception e) { detailedErrorReport(e); }
		}

		return number;
	}

	/**
	 * Reads and returns next Double value from the file open for reading.
	 * This method returns null if there is no more data in the file or the
	 * value at the current reading position is not a valid Double value.
	 *
	 * @return Double value read from file or null
	 *
	 * @throws IOException may raise after some I/O operation failed
	 */
	public Double readDouble() throws IOException
	{
		if (skipSpaces()) return null;

		Double number = null;
		int indexOf = lineBuffer.indexOf(" ");

		if (-1 == indexOf)
		{
			try
			{
				number = Double.valueOf(lineBuffer.toString());
				lineBuffer.setLength(0);
				checkNextLine = true;
			}
			catch (Exception e) { detailedErrorReport(e, false); }
		}
		else
		{
			try
			{
				number = Double.valueOf(lineBuffer.substring(0, indexOf));
				lineBuffer.delete(0, indexOf + 1);
				checkNextLine = true;
			}
			catch (Exception e) { detailedErrorReport(e); }
		}

		return number;
	}

	/**
	 * Reads and returns next Boolean value from the file open for reading.
	 * This method returns null if there is no more data in the file or the
	 * value at the current reading position is not a valid Boolean value or
	 * there is string "null" at the current reading position in the file.
	 *
	 * @return Boolean value read from file or null
	 *
	 * @throws IOException may raise after some I/O operation failed
	 */
	public Boolean readBoolean() throws IOException
	{
		if (skipSpaces()) return null;

		Boolean value = null;
		int indexOf = lineBuffer.indexOf(" ");

		if (-1 == indexOf)
		{
			if (!lineBuffer.toString().equalsIgnoreCase(nullString))
				value = Boolean.valueOf(lineBuffer.toString());
			lineBuffer.setLength(0);
			checkNextLine = true;
		}
		else
		{
			if (!lineBuffer.substring(0, indexOf).equalsIgnoreCase(nullString))
				value = Boolean.valueOf(lineBuffer.substring(0, indexOf));
			lineBuffer.delete(0, indexOf + 1);
			checkNextLine = true;
		}

		return value;
	}

	/**
	 * TODO – Complete the description!
	 *
	 * @param objects TODO – Add the parameter description!
	 *
	 * @return number of data units (blocks) that has been written to arguments;
	 *     this value strongly depends on data types of arguments; if
	 *     argument is of type int[], the return value is number of string
	 *     blocks that have been converted to integers; if argument is of
	 *     type char[], then the return value is number of characters written
	 *     to this array (new lines are ignored); if the argument is of type
	 *     StringBuffer, the return value is length of line read; if the
	 *     argument types are mixed, the return value is sum of such
	 *     miscellaneous numbers
	 *
	 * @throws IOException may raise after some I/O operation failed
	 * @throws IllegalArgumentException TODO – Add the explanation – when!
	 */
	public int read(Object... objects)
		throws IOException, IllegalArgumentException
	{
		if (null == reader)
			throw new IOException("There is no file open for reading.");

		int numRead = 0;

		for (Object obj : objects)
		{
			if (obj instanceof StringBuffer)
			{
				((StringBuffer)obj).setLength(0);
				String line = readLine();
				((StringBuffer)obj).append(line);
				numRead += line.length();
			}
			else if (obj instanceof int[])
			{
				int[] array = (int[])obj;
				Long number;
				for (int i = 0; i < array.length; ++i)
				{
					if (null != (number = this.readLong()))
					{
						array[i] = number.intValue();
						++numRead;
					}
					else
						array[i] = 0;
				}
			}
			else if (obj instanceof float[])
			{
				float[] array = (float[])obj;
				Double number;
				for (int i = 0; i < array.length; ++i)
				{
					if (null != (number = this.readDouble()))
					{
						array[i] = number.floatValue();
						++numRead;
					}
					else
						array[i] = 0;
				}
			}
			else if (obj instanceof long[])
			{
				long[] array = (long[])obj;
				Long number;
				for (int i = 0; i < array.length; ++i)
				{
					if (null != (number = this.readLong()))
					{
						array[i] = number.longValue();
						++numRead;
					}
					else
						array[i] = 0;
				}
			}
			else if (obj instanceof double[])
			{
				double[] array = (double[])obj;
				Double number;
				for (int i = 0; i < array.length; ++i)
				{
					if (null != (number = this.readDouble()))
					{
						array[i] = number.doubleValue();
						++numRead;
					}
					else
						array[i] = 0;
				}
			}
			else if (obj instanceof boolean[])
			{
				boolean[] array = (boolean[])obj;
				Boolean value;
				for (int i = 0; i < array.length; ++i)
				{
					if (null != (value = this.readBoolean()))
					{
						array[i] = value;//.booleanValue();
						++numRead;
					}
					else
						array[i] = false;
				}
			}
			else if (obj instanceof char[])
			{
				char[] array = (char[])obj;
				// reader.read(array);
				Character value;
				for (int i = 0; i < array.length; ++i)
				{
					if (null != (value = this.readCharacter()))
					{
						array[i] = value;
						++numRead;
					}
					else
						array[i] = 0;
				}
			}
			else
			{
				throw new IllegalArgumentException(
					"Illegal data type passed to method TextFile.read: " +
					obj.getClass().getCanonicalName());
			}
		}

		return numRead;
	}


	/**
	 * TODO – Complete the description!
	 *
	 * @param text TODO – Add the parameter description!
	 *
	 * @throws IOException may raise after some I/O operation failed
	 */
	public void writeString(String text) throws IOException
	{
		if (null == writer)
			throw new IOException("There is no file open for writing.");
		writer.write(text);
	}

	/**
	 * TODO – Complete the description!
	 *
	 * @param text TODO – Add the parameter description!
	 *
	 * @throws IOException may raise after some I/O operation failed
	 */
	public void writeLine(String text) throws IOException
	{
		if (null == writer)
			throw new IOException("There is no file open for writing.");
		writer.write(text + "\r\n");
	}

	/**
	 * TODO – Complete the description!
	 *
	 * @throws IOException may raise after some I/O operation failed
	 */
	public void writeLine() throws IOException
	{
		if (null == writer)
			throw new IOException("There is no file open for writing.");
		writer.write("\r\n");
	}

	/**
	 * TODO – Complete the description!
	 *
	 * @param objects TODO – Add the parameter description!
	 *
	 * @throws IOException may raise after some I/O operation failed
	 */
	public void write(Object... objects) throws IOException
	{
		for (Object obj : objects)
		{
			if (obj instanceof String || obj instanceof StringBuffer)
				writeString(obj.toString());
			else if (obj instanceof int[])
			{
				int[] array = (int[])obj;

				for (int i = 0; i < array.length; ++i)
					writeString(array[i] + " ");
			}
			else if (obj instanceof float[])
			{
				float[] array = (float[])obj;

				for (int i = 0; i < array.length; ++i)
					writeString(array[i] + " ");
			}
			else if (obj instanceof long[])
			{
				long[] array = (long[])obj;

				for (int i = 0; i < array.length; ++i)
					writeString(array[i] + " ");
			}
			else if (obj instanceof double[])
			{
				double[] array = (double[])obj;

				for (int i = 0; i < array.length; ++i)
					writeString(array[i] + " ");
			}
			else if (obj instanceof boolean[])
			{
				boolean[] array = (boolean[])obj;

				for (int i = 0; i < array.length; ++i)
					writeString(array[i] + " ");
			}
			else if (obj instanceof char[])
			{
				writeString(new String((char[])obj));
				/*
				char[] array = (char[])obj;

				for (int i = 0; i < array.length; ++i)
					writeString(array[i]);
				*/
			}
			else
				writeString(obj + " ");
		}
	}
}
