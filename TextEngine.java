
import static java.lang.System.err;
import static java.lang.System.out;

import java.awt.Color;

import java.io.IOException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.Comparator;
import java.util.Enumeration;
// Following import was here for the unsorted way of the implementation of
// the aliases. Look for words “unsorted way” in this source code for details…
// import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import javax.swing.DefaultListModel;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import bookcase.TextFile;

/*#
 * This is part of the Text Driver project. This is the engine. Thanks to
 * TextDriverInterface the engine is reusable in other projects. The
 * TextDriver provides the default implementation of TextDriverInterface.
 * This default implementation also contains a GUI.
 * (See TextDriver.java for version information and changes.)
 */

/**
 * This engine includes parser for definition files, runtime script
 * interpreter and runtime command line interpreter.
 */

public class TextEngine
{
	private class AliasesComparator implements Comparator<String>
	{
		public int compare(String key1, String key2)
		{
			if (key1.length() == key2.length())
				return key2.compareTo(key1);
			return key2.length() - key1.length();
		}

		// Old Java…
		// public boolean equals(Object obj)
		// {
		// 	return this == obj;
		// }
	}

	// Define script engine for this text engine
	private final ScriptEngineManager mgr = new ScriptEngineManager();
	private final ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");

	// List of all tokens (commands, locations, items…)
	// Note: The TreeSet is not effective to use here, because we need to
	// find the matching element in the collection and that feature is not
	// implemented in TreeSet.
	private final Vector<Entity> tokens = new Vector<Entity>();
	// private final TreeSet<Entity> tokens = new TreeSet<Entity>();

	// List of tokens that must be unique (commands, triggers)
	// private final Vector<Entity> uniqueTokens = new Vector<Entity>();
	private final TreeSet<Entity> uniqueTokens = new TreeSet<Entity>();

	// Map of aliases for tokens (for items and locations)
	private final TreeMap<String, Entity> aliases =
		new TreeMap<String, Entity>(new AliasesComparator());

	// Tokenized command
	private final Vector<Entity> tokenizedCommand = new Vector<Entity>();

	// Instances for default commands and operators – they must be defined in
	// the definition file(‼) and they also cannot be redefined‼
	private Entity walkCommand = null;     // example: go
	private Entity walkOperator = null;    // example: using
	private Entity returnCommand = null;   // example: return
	private Entity returnOperator = null;  // example: using
	private Entity exploreCommand = null;  // example: explore
	private Entity exploreOperator = null; // example: using
	private Entity pickCommand = null;     // example: pick
	private Entity pickOperator = null;    // example: using
	private Entity dropCommand = null;     // example: drop
	private Entity dropOperator = null;    // example: using
	private Entity throwCommand = null;    // example: throw
	private Entity throwOperator = null;   // example: at
	private Entity insertCommand = null;   // example: insert
	private Entity insertOperator = null;  // example: in
	private Entity removeCommand = null;   // example: remove
	private Entity removeOperator = null;  // example: from
	private Entity examineCommand = null;  // example: examine
	private Entity examineOperator = null; // example: using
	private Entity useCommand = null;      // example: use
	private Entity useOperator = null;     // example: with
	private Entity actionCommand = null;   // example: talk
	private Entity actionOperator = null;  // example: to

	// Lists of locations, items and triggers
	private final Vector<Location> locations = new Vector<Location>();
	private final Vector<Item> items = new Vector<Item>();
	private final Vector<Trigger> triggers = new Vector<Trigger>();

	// Special scripts
	private final Trigger startup = new Trigger("Startup Script");
	private final Trigger readState = new Trigger("Read State Script");
	private final Trigger query = new Trigger("Query Script");
	private final Trigger populate = new Trigger("Populate Script");

	// Avatar definition
	public final Avatar avatar = new Avatar();

	// Working holders
	private Holder sourceHolder = null;
	private Holder targetHolder = null;
	private Holder resourceHolder = null;

	// Current messages
	public String successMessage = null;
	public String failureMessage = null;
	public String commandType = null;
	public String reasonType = null;

	// File
	private final TextFile definition = new TextFile();
	private String definitionFilename = "";
	private int definitionLineNumber = 0;

	// Other fields
	private boolean debug = false;
	private boolean autoexplore = true;
	private boolean tokenStartsChecking = true;
	private String paramString = null;
	private String commandLineSeparator = ",";
	private final StringBuffer debugBuffer = new StringBuffer();

	private final String avatarKeyword = "avatar";
	private final String sourceKeyword = "src";
	private final String targetKeyword = "tgt";
	private final String resourceKeyword = "rsc";
	private final String commandKeyword = "cmd";
	private final String reasonKeyword = "rsn";
	private final Entity avatarToken;


	public void enableDebugMode() { debug = true; }

	public void debugMessage(Object object,
		String action, Object... values)
	{
		if (debug)
		{
			debugBuffer.append("  ");
			debugBuffer.append(object);
			debugBuffer.append(" (");
			debugBuffer.append(object.getClass().getName());
			debugBuffer.append(") ");
			debugBuffer.append(action);
			if (0 != values.length)
			{
				for (Object obj : values)
				{
					debugBuffer.append(" ");
					debugBuffer.append(obj);
				}
			}
			debugBuffer.append('\n');
		}
	}

	public void debugFlush()
	{
		if (0 != debugBuffer.length())
		{
			out.println(debugBuffer.toString());
			debugBuffer.setLength(0);
		}
	}

	public void debugClear()
	{
		if (0 != debugBuffer.length())
			debugBuffer.setLength(0);
	}

	public String commandLineSeparator() { return commandLineSeparator; }


	private final TextDriverInterface textDriver;

	// Constructor
	public TextEngine(TextDriverInterface textDriver)
	{
		this.textDriver = textDriver;
		Entity.engine = this;

		// Add default tokens:
		avatarToken = addToken(avatarKeyword);
		addToken(sourceKeyword);
		addToken(targetKeyword);
		addToken(resourceKeyword);
		addToken(commandKeyword);
		addToken(reasonKeyword);
	}


	// Evaulating of expressions uses the script engine…

	public static String replaceForVars(String line, Vector<String> forVars)
	{
		// Note: %(forVar[##]) is very “tight” name that will be replaced
		// fist. That means that also following expression will work:
		// %(%(forVar)->weight)
		int indexOf = line.indexOf("%(forVar");
		if (-1 == indexOf && -1 == line.indexOf("%(^forVar")) return line;

		if (null == forVars || 0 == forVars.size())
			return line.replaceAll("%\\(\\^?forVar[0-9]*\\)", "");

		StringBuffer buffer = new StringBuffer(line);
		boolean replaced = true;

		// Max. level of replacement recursion is 16
		for (int j = 0; j < 16 && replaced; ++j)
		{
			replaced = false;

			for (int i = forVars.size() - 1; i >= 0; --i)
			{
				String parameter = forVars.elementAt(i);
				String paramString = "%(forVar" + (i + 1) + ")";

				if (-1 != (indexOf = buffer.indexOf(paramString)))
				{
					buffer.replace(indexOf, indexOf +
						paramString.length(), parameter);
					replaced = true;
				}

				if (parameter.length() >= 1)
				{
					paramString = "%(^forVar" + (i + 1) + ")";
					parameter = Character.toUpperCase(
						parameter.charAt(0)) + parameter.substring(1);

					if (-1 != (indexOf = buffer.indexOf(paramString)))
					{
						buffer.replace(indexOf, indexOf +
							paramString.length(), parameter);
						replaced = true;
					}
				}
			}

			// Note: %(forVar) is the same as %(forVar1)
			String parameter = forVars.elementAt(0);
			String paramString = "%(forVar)";

			if (-1 != (indexOf = buffer.indexOf(paramString)))
			{
				buffer.replace(indexOf, indexOf +
					paramString.length(), parameter);
				replaced = true;
			}

			if (parameter.length() >= 1)
			{
				paramString = "%(^forVar)";
				parameter = Character.toUpperCase(
					parameter.charAt(0)) + parameter.substring(1);

				if (-1 != (indexOf = buffer.indexOf(paramString)))
				{
					buffer.replace(indexOf, indexOf +
						paramString.length(), parameter);
					replaced = true;
				}
			}
		}

		return buffer.toString().replaceAll("%\\(\\^?forVar[0-9]*\\)", "");
	}

	public static String replaceParameters(String line,
		Vector<String> parameters)
	{
		// Note: %(##) is very “tight” identification that will be replaced
		// fist. That means that also following expression will work:
		// %(%(1)->weight)
		int indexOf = line.indexOf("%(");
		if (-1 == indexOf && -1 == line.indexOf("%(^")) return line;

		if (null == parameters || 0 == parameters.size())
			return line.replaceAll("%\\(\\^?[0-9]+\\)", "");

		StringBuffer buffer = new StringBuffer(line);
		boolean replaced = true;

		// Max. level of replacement recursion is 16
		for (int j = 0; j < 16 && replaced; ++j)
		{
			replaced = false;

			for (int i = parameters.size() - 1; i >= 0; --i)
			{
				String parameter = parameters.elementAt(i);
				String paramString = "%(" + (i + 1) + ")";

				if (-1 != (indexOf = buffer.indexOf(paramString)))
				{
					buffer.replace(indexOf, indexOf +
						paramString.length(), parameter);
					replaced = true;
				}

				if (parameter.length() >= 1)
				{
					paramString = "%(^" + (i + 1) + ")";
					parameter = Character.toUpperCase(
						parameter.charAt(0)) + parameter.substring(1);

					if (-1 != (indexOf = buffer.indexOf(paramString)))
					{
						buffer.replace(indexOf, indexOf +
							paramString.length(), parameter);
						replaced = true;
					}
				}
			}
		}

		return buffer.toString().replaceAll("%\\(\\^?[0-9]+\\)", "");
	}

	// Replaces properties and parameters with their values
	public String replaceProperties(String expression, Holder currentHolder,
		Vector<String> parameters)
	{
		int indexOf = expression.indexOf("%(");
		if (-1 == indexOf) return expression;

		int i = 0;
		StringBuffer buffer = new StringBuffer(
			replaceParameters(expression, parameters));

		// out.println("  Replace properties: " + buffer);

		if (-1 != (indexOf = buffer.indexOf("%("))) do
		{
			int indexOf2 = buffer.indexOf(")", indexOf);

			if (-1 == indexOf2)
				throwErrorMessage("errorExpressionSyntaxError",
					definitionFilename, definitionLineNumber, expression);

			String property = buffer.substring(indexOf + 2, indexOf2);
			String value = "0";

			// Check if the fist “letter” of the result should be capitalized
			boolean capitalize;
			if (capitalize = ('^' == property.charAt(0)))
				property = property.substring(1);

			if (commandKeyword.equals(property))
			{
				value = null == commandType ? "" : commandType;
			}
			else if (reasonKeyword.equals(property))
			{
				value = null == reasonType ? "" : reasonType;
			}
			else
			{
				int indexOf3 = property.indexOf("->");
				Holder owner;

				// out.println("Found property request: " + property);

				if (-1 == indexOf3)
					owner = currentHolder;
				else
				{
					String ownerName = property.substring(0, indexOf3);
					property = property.substring(indexOf3 + 2);

					if (avatarKeyword.equals(ownerName.trim()))
						owner = avatar;
					else
					{
						if (sourceKeyword.equals(ownerName.trim()))
						{
							owner = sourceHolder;
							// Explanation for throwErrorMessage below:
							if (null == owner) ownerName += " is null";
						}
						else if (targetKeyword.equals(ownerName.trim()))
						{
							owner = targetHolder;
							// Explanation for throwErrorMessage below:
							if (null == owner) ownerName += " is null";
						}
						else if (resourceKeyword.equals(ownerName.trim()))
						{
							owner = resourceHolder;
							// Explanation for throwErrorMessage below:
							if (null == owner) ownerName += " is null";
						}
						else
						{
							owner = findItem(ownerName);
							if (null == owner) owner = findLocation(ownerName);
							if (null == owner) owner = findTrigger(ownerName);
						}

						if (null == owner) throwErrorMessage(
							"errorUnknownOwnerOfProperty", definitionFilename,
							definitionLineNumber, ownerName);
					}
				}

				if (null == owner) owner = avatar;

				String propertyMethod = "get" +
					Character.toUpperCase(property.charAt(0)) +
					property.substring(1).toLowerCase();

				// out.println("  Property owner: " + owner);
				// out.println("  Property name: " + property);
				// out.println("  Property method name: " + propertyMethod);

				Class<? extends Holder> ownerClass = owner.getClass();
				boolean propertyNotFound = true;

				// do // deprecated
				// { // deprecated
					try
					{
						@SuppressWarnings("unchecked")
						Object objectValue = ownerClass.getMethod(
							propertyMethod).invoke(owner);
						if (null == objectValue) value = "";
						else value = objectValue.toString();

						propertyNotFound = false;

						if (capitalize && 0 != value.length())
							value = Character.toUpperCase(value.charAt(0)) +
								value.substring(1);

						// break; // deprecated
					}
					catch (Exception e)
					{
						// err.println("  Exception in “getProperty”: " + e); // e.printStackTrace();
					}

					String alternateMethod = "is" +
						Character.toUpperCase(property.charAt(0)) +
						property.substring(1).toLowerCase();
					// out.println("  Property alternate method name: " + alternateMethod);

					try
					{
						@SuppressWarnings("unchecked")
						Object objectValue = ownerClass.getMethod(
							alternateMethod).invoke(owner);
						if (null == objectValue) value = "";
						else value = objectValue.toString();

						propertyNotFound = false;

						if (capitalize && 0 != value.length())
							value = Character.toUpperCase(value.charAt(0)) +
								value.substring(1);

						// break; // deprecated
					}
					catch (Exception e)
					{
						// err.println("  Exception in “getProperty”: " + e); // e.printStackTrace();
						// err.println("  Dumping class: " + ownerClass);
						// Method[] methods = ownerClass.getMethods();
						// for (Method method : methods)
						// 	err.println("    " + method.toString());
					}

				// 	ownerClass = ownerClass.getSuperclass(); // deprecated
				// } // deprecated
				// while (null != ownerClass); // deprecated

				if (propertyNotFound)
				{
					// Custom properties
					Double getValue = owner.getProperty(property);
					if (null == getValue)
						throwErrorMessage("errorUnknownProperty",
							definitionFilename, definitionLineNumber,
							property);
					value = getValue.toString();
				}
			}


			buffer.replace(indexOf, indexOf2 + 1, value);
			// out.println("  Property value: " + value);
			// out.println("  Replaced buffer: " + buffer);

			if (++i > 500) return buffer.toString();

			indexOf = buffer.indexOf("%(");
		}
		while (-1 != indexOf);

		return buffer.toString();
	}

	public double evaluateDouble(String expression, Holder currentHolder,
		Vector<String> parameters)
	{
		try
		{
			return (Double)jsEngine.eval(replaceProperties(
				expression, currentHolder, parameters));
		}
		catch (ScriptException e)
		{
			DriverOutput.printError(e);
			return 0.0; // The result will be zero in case of failure.
			// throw new NumberFormatException(e.getMessage());
		}
	}

	public boolean evaluateBoolean(String expression, Holder currentHolder,
		Vector<String> parameters)
	{
		try
		{
			return (Boolean)jsEngine.eval(replaceProperties(
				expression, currentHolder, parameters));
		}
		catch (ScriptException e)
		{
			DriverOutput.printError(e);
			return false; // The result will be false in case of failure.
			// throw new NumberFormatException(e.getMessage());
		}
	}

	public void setProperty(String property, String expression,
		Holder currentHolder, Vector<String> parameters)
	{
		int indexOf = property.indexOf("->");
		Holder owner;

		if (-1 == indexOf)
			owner = currentHolder;
		else
		{
			String ownerName = property.substring(0, indexOf);
			property = property.substring(indexOf + 2).trim();

			if (avatarKeyword.equals(ownerName.trim()))
				owner = avatar;
			else
			{
				if (sourceKeyword.equals(ownerName.trim()))
				{
					owner = sourceHolder;
					if (null == owner) ownerName += " is null";
				}
				else if (targetKeyword.equals(ownerName.trim()))
				{
					owner = targetHolder;
					if (null == owner) ownerName += " is null";
				}
				else if (resourceKeyword.equals(ownerName.trim()))
				{
					owner = resourceHolder;
					if (null == owner) ownerName += " is null";
				}
				else
				{
					owner = findItem(ownerName);
					if (null == owner) owner = findLocation(ownerName);
					if (null == owner) owner = findTrigger(ownerName);
				}

				if (null == owner) throwErrorMessage(
					"errorUnknownOwnerOfProperty", definitionFilename,
					definitionLineNumber, ownerName);
			}
		}

		if (null == owner) owner = avatar;

		String propertyMethod = "set" +
			Character.toUpperCase(property.charAt(0)) +
			property.substring(1).toLowerCase();
		double value = 0;

		// out.println("  Property owner: " + owner);
		// out.println("  Property name: " + property);
		// out.println("  Property method name: " + propertyMethod);

		Class<? extends Holder> ownerClass = owner.getClass();
		boolean propertyNotFound = true;

		value = evaluateDouble(expression, currentHolder, parameters);
		// out.println("  Property new value: " + value);

		// do // deprecated
		// { // deprecated
			try
			{
				// out.println("  Dumping class: " + ownerClass);
				// Method[] methods = ownerClass.getMethods();
				// for (Method method : methods)
				// 	out.println("    " + method.toString());

				@SuppressWarnings("unchecked")
				Object object = ownerClass.getMethod(propertyMethod,
					double.class).invoke(owner, value);

				propertyNotFound = false;
				// break; // deprecated
			}
			catch (Exception e)
			{
				// err.println("  Exception in “setProperty”: " + e);
			}

		// 	ownerClass = ownerClass.getSuperclass(); // deprecated
		// } // deprecated
		// while (null != ownerClass); // deprecated

		if (propertyNotFound)
		{
			// Custom properties
			if (!owner.setProperty(property, value))
				throwErrorMessage("errorUnknownProperty",
					definitionFilename, definitionLineNumber, property);
		}
	}

	// This method would provide the possibility of defining variables inside
	// the script engine, if it would be necessary… But there should be
	// defined a new method for dismissing the variables defined.
	/*
	public void setVariable(String name, String value) throws ScriptException
	{ jsEngine.eval("var " + name + " = " + value + ";"); }
	*/


	public Entry getEntry(String name)
	{
		if (avatar.equals(name)) return avatar;
		return findItem(name);
	}

	public Container getPlacement(String name)
	{
		if (avatar.equals(name)) return avatar;

		Container placement = findLocation(name);
		if (null != placement) return placement;

		return findItem(name);
	}


	public Location findLocation(String name)
	{
		name = name.replaceAll("[\\s ]+", " ").trim();
		// int indexOf = locations.indexOf(name);
		for (Location location : locations)
			if (location.equals(name)) return location;
		// if (-1 != indexOf) return locations.elementAt(indexOf); // does not work
		return null;
	}

	public Item findItem(String name)
	{
		name = name.replaceAll("[\\s ]+", " ").trim();
		// int indexOf = items.indexOf(name);
		for (Item item : items)
			if (item.equals(name)) return item;
		// if (-1 != indexOf) return items.elementAt(indexOf); // does not work
		return null;
	}

	public Trigger findTrigger(String name)
	{
		name = name.replaceAll("[\\s ]+", " ").trim();
		// int indexOf = triggers.indexOf(name);
		for (Trigger trigger : triggers)
			if (trigger.equals(name)) return trigger;
		// if (-1 != indexOf) return triggers.elementAt(indexOf); // does not work
		return null;
	}

	public Location findLocation(Entity entity)
	{
		return findLocation(entity.name);
	}

	public Item findItem(Entity entity)
	{
		return findItem(entity.name);
	}

	public Trigger findTrigger(Entity entity)
	{
		return findTrigger(entity.name);
	}


	public boolean isCommandDefined(String commandID)
	{
		try
		{
			Field field = TextEngine.class.
				getDeclaredField(commandID + "Command");
			Entity entity = (Entity)field.get(this);
			return null != entity;
		}
		catch (Exception e)
		{
			out.println("  TextEngine command field: " + commandID);
			DriverOutput.printError(e);
		}

		return false;
	}

	public Entity getCommandEntity(String commandID)
	{
		try
		{
			Field field = TextEngine.class.
				getDeclaredField(commandID + "Command");
			Entity entity = (Entity)field.get(this);

			if (null == entity)
			{
				entity = addToken(commandID);
				checkUniqueToken(entity);
				field.set(this, entity);
			}

			return entity;
		}
		catch (Exception e)
		{
			out.println("  TextEngine command field: " + commandID);
			DriverOutput.printError(e);
		}
		return null;
	}

	public Entity getOperatorEntity(String operatorID)
	{
		try
		{
			Field field = TextEngine.class.
				getDeclaredField(operatorID + "Operator");
			Entity entity = (Entity)field.get(this);

			if (null == entity)
			{
				if (operatorID.equals("throw"))
					entity = addToken("at");
				else if (operatorID.equals("insert"))
					entity = addToken("in");
				else if (operatorID.equals("remove"))
					entity = addToken("from");
				else if (operatorID.equals("use"))
					entity = addToken("with");
				else
					entity = addToken("using");
				field.set(this, entity);
			}

			return entity;
		}
		catch (Exception e)
		{
			out.println("  TextEngine operator field: " + operatorID);
			DriverOutput.printError(e);
		}
		return null;
	}


	private boolean startsWith(String line, String keyString)
	{
		if (line.startsWith(keyString))
		{
			paramString = line.substring(keyString.length());
			return true;
		}

		return false;
	}


	public void parseDefinition(String basename) throws java.io.IOException
	{
		Holder currentHolder = null;

		Location tempLocation = null;
		Item tempItem = null;
		Trigger tempTrigger = null;

		Transition lastTransition = null;
		Transition lastReverseTransition = null;
		Vector<String> currentScript = null;
		SimpleAttributeSet currentStyle = null;

		String line, endKeyword = "end";
		double parameter = 0.0;

		definitionLineNumber = 0;
		definition.openForReading(definitionFilename =
			(basename + textDriver.getWorldExtension()));
		int definitionFlag = 0;

		while (null != (line = definition.readLine()))
		{
			line = line.trim(); ++definitionLineNumber;
			if (0 == line.length() || ';' == line.charAt(0)) continue;

			if (null != currentScript)
			{
				if (line.equals(endKeyword))
					currentScript = null;
				else
					currentScript.add(line);
			}
			else if (0 != definitionFlag)
			{
				switch (definitionFlag)
				{
				case 1: // menu item
				case 2: // main menu item
				case 3: // context menu item

					if (line.equals("end"))
					{
						switch (definitionFlag)
						{
						case 1: // menu item
							textDriver.defineMenuItemAdd();
							break;

						case 2: // main menu item
							textDriver.defineMenuItemAddMain();
							break;

						case 3: // context menu item
							if (currentHolder instanceof Trigger)
								textDriver.defineMenuItemAddContext(
									(Trigger)currentHolder);
							else
								out.println("Unknown target of context " +
									"menu item in " + definitionFilename +
									" at line " + definitionLineNumber +
									". Item was ignored.");
							break;
						}

						definitionFlag = 0;
					}
					else if (startsWith(line, "text "))
						textDriver.defineMenuItemText(paramString);
					else if (startsWith(line, "mnemonic "))
						textDriver.defineMenuItemMnemonic(paramString);
					else if (startsWith(line, "accelerator "))
						textDriver.defineMenuItemAccelerator(paramString);
					else if (startsWith(line, "icon "))
						textDriver.defineMenuItemIcon(paramString);
					else if (startsWith(line, "trigger "))
						textDriver.defineMenuItemTrigger(paramString);
					// else if (line.equals("clear")) // ‼NOT ALLOWED‼
					// 	textDriver.defineMenuItemClear();
					else
						out.println("Unknown menu item property has been " +
							"ignored: " + line + "; in " + definitionFilename
							+ " at line " + definitionLineNumber);
					break;

				default: // unknown
					definitionFlag = 0;
				}
			}
			else if (null != currentStyle)
			{
				if (line.equals("end"))
				{
					currentStyle = null;
				}
				else if (line.equals("clear"))
				{
					currentStyle.removeAttributes(
						currentStyle.getAttributeNames());
				}
				else if (startsWith(line, "color "))
				{
					try {
						StyleConstants.setForeground(currentStyle,
							Color.decode(paramString));
					} catch (Exception e) { err.println(e); }
				}
				else if (startsWith(line, "size "))
				{
					try {
						StyleConstants.setFontSize(currentStyle,
							Integer.parseInt(paramString));
					} catch (Exception e) { err.println(e); }
				}
				else if (startsWith(line, "background "))
				{
					try {
						StyleConstants.setBackground(currentStyle,
							Color.decode(paramString));
					} catch (Exception e) { err.println(e); }
				}
				else if (startsWith(line, "font "))
				{
					StyleConstants.setFontFamily(
						currentStyle, paramString);
				}
				else if (startsWith(line, "bold "))
				{
					StyleConstants.setBold(currentStyle,
						Boolean.parseBoolean(paramString));
				}
				else if (startsWith(line, "italic "))
				{
					StyleConstants.setItalic(currentStyle,
						Boolean.parseBoolean(paramString));
				}
				else if (startsWith(line, "strike "))
				{
					StyleConstants.setStrikeThrough(currentStyle,
						Boolean.parseBoolean(paramString));
				}
				else if (startsWith(line, "subscript "))
				{
					StyleConstants.setSubscript(currentStyle,
						Boolean.parseBoolean(paramString));
				}
				else if (startsWith(line, "superscript "))
				{
					StyleConstants.setSuperscript(currentStyle,
						Boolean.parseBoolean(paramString));
				}
				else if (startsWith(line, "underline "))
				{
					StyleConstants.setUnderline(currentStyle,
						Boolean.parseBoolean(paramString));
				}
				else
					err.println("  Error: unknown style property “" +
						line + "” at line " + definitionLineNumber
						+ " in " + definitionFilename + ".");
			}
			else if (startsWith(line, "build "))
			{
				tempLocation = new Location(paramString);
				checkUniqueToken(addToken(tempLocation.name));
				locations.add(tempLocation);
				currentHolder = tempLocation;
				lastTransition = null;
				lastReverseTransition = null;
			}
			else if (startsWith(line, "create "))
			{
				tempItem = new Item(paramString);
				checkUniqueToken(addToken(tempItem.name));
				items.add(tempItem);
				currentHolder = tempItem;
				lastTransition = null;
				lastReverseTransition = null;
			}
			else if (line.equals("avatar"))
			{
				currentHolder = avatar;
				lastTransition = null;
				lastReverseTransition = null;
			}
			else if (startsWith(line, "update "))
			{
				if (null != (tempLocation = findLocation(paramString)))
					currentHolder = tempLocation;
				else if (null != (tempItem = findItem(paramString)))
					currentHolder = tempItem;
				else
					currentHolder = null;

				lastTransition = null;
				lastReverseTransition = null;

				if (null == currentHolder)
					throwErrorMessage("errorUnknownUpdateTarget",
						definitionFilename, definitionLineNumber, paramString);
			}
			else if (startsWith(line, "oneway ") ||
					 startsWith(line, "connect "))
			{
				if (!(currentHolder instanceof Location))
					throwErrorMessage("errorUnexpectedTransition",
						definitionFilename, definitionLineNumber);

				boolean oneway = line.startsWith("oneway ");

				if (startsWith(paramString, "to "))
				{
					tempLocation = findLocation(paramString);
					if (null == tempLocation) throwErrorMessage(
						"errorLocationNotFound", definitionFilename,
							definitionLineNumber, paramString);

					lastTransition = ((Location)currentHolder).
						connectWith(tempLocation);
					if (oneway)
						lastReverseTransition = null;
					else
						lastReverseTransition = tempLocation.
							connectWith((Location)currentHolder);
				}
				else if (startsWith(paramString, "tonnage "))
				{
					parameter = evaluateDouble(paramString,
						currentHolder, null);
					if (null == lastTransition) throwErrorMessage(
						"errorTransitionTargetMissing", definitionFilename,
							definitionLineNumber);

					lastTransition.setTonnage(parameter);
					if (!oneway && null != lastReverseTransition)
						lastReverseTransition.setTonnage(parameter);
				}
				else if (startsWith(paramString, "capacity "))
				{
					parameter = evaluateDouble(paramString,
						currentHolder, null);
					if (null == lastTransition) throwErrorMessage(
						"errorTransitionTargetMissing", definitionFilename,
							definitionLineNumber);

					lastTransition.setCapacity(parameter);
					if (!oneway && null != lastReverseTransition)
						lastReverseTransition.setCapacity(parameter);
				}
				else if (paramString.equals("close"))
				{
					if (null == lastTransition) throwErrorMessage(
						"errorTransitionTargetMissing", definitionFilename,
							definitionLineNumber);

					lastTransition.hide();
					if (!oneway && null != lastReverseTransition)
						lastReverseTransition.hide();
				}
				else if (paramString.equals("open"))
				{
					if (null == lastTransition) throwErrorMessage(
						"errorTransitionTargetMissing", definitionFilename,
							definitionLineNumber);

					lastTransition.show();
					if (!oneway && null != lastReverseTransition)
						lastReverseTransition.show();
				}
				else
				{
					throwErrorMessage("errorUnsupportedProperty",
						definitionFilename, definitionLineNumber, line);
				}
			}
			else if (startsWith(line, "copy "))
			{
				if (currentHolder instanceof Location)
				{
					tempLocation = findLocation(paramString);
					if (null == tempLocation) throwErrorMessage(
						"errorLocationNotFound", definitionFilename,
							definitionLineNumber, paramString);
					((Location)currentHolder).copy(tempLocation);
				}
				else if (currentHolder instanceof Item)
				{
					tempItem = findItem(paramString);
					if (null == tempItem) throwErrorMessage(
						"errorItemNotFound", definitionFilename,
							definitionLineNumber, paramString);
					((Item)currentHolder).copy(tempItem);
				}
				else if (currentHolder instanceof Trigger)
				{
					tempTrigger = findTrigger(paramString);
					if (null == tempTrigger) throwErrorMessage(
						"errorTriggerNotFound", definitionFilename,
							definitionLineNumber, paramString);
					((Trigger)currentHolder).copy(tempTrigger);
				}
				else
				{
					throwErrorMessage("errorUnsupportedOperation",
						definitionFilename, definitionLineNumber, line);
				}
			}
			else if (startsWith(line, "alias "))
			{
				if (null == currentHolder)
				{
					textDriver.parseAndWriteWarning(
						"warningNullEntity", definitionFilename,
						definitionLineNumber, line);
				}
				else
				{
					String warning = addAlias(paramString, currentHolder);

					if (null != warning)
					{
						textDriver.parseAndWriteWarning(
							warning, definitionFilename,
							definitionLineNumber, paramString);
					}
				}
			}
			else if (startsWith(line, "location "))
			{
				tempLocation = findLocation(paramString);
				if (null == tempLocation) throwErrorMessage(
					"errorLocationNotFound", definitionFilename,
						definitionLineNumber, paramString);
				if (currentHolder instanceof Entry)
				{
					String error = tempLocation.
						insertEntry(((Entry)currentHolder));
					if (null != error)
					{
						throwErrorMessage("errorPlacementError",
							definitionFilename, definitionLineNumber,
							textDriver.parseMessage(error), currentHolder);
					}

					// ‼Not here‼ – make this after startup script
					// if (avatar.equals(currentHolder))
					// 	tempLocation.setVisited();
				}
				else
				{
					throwErrorMessage("errorUnsupportedProperty",
						definitionFilename, definitionLineNumber, line);
				}
			}
			else if (startsWith(line, "inside "))
			{
				tempItem = findItem(paramString);
				if (null == tempItem) throwErrorMessage(
					"errorItemNotFound", definitionFilename,
						definitionLineNumber, paramString);
				if (currentHolder instanceof Entry)
				{
					String error = tempItem.
						insertEntry(((Entry)currentHolder));
					if (null != error)
					{
						throwErrorMessage("errorPlacementError",
							definitionFilename, definitionLineNumber,
							textDriver.parseMessage(error), currentHolder);
					}
				}
				else
				{
					throwErrorMessage("errorUnsupportedProperty",
						definitionFilename, definitionLineNumber, line);
				}
			}
			else if (line.equals("give"))
			{
				if (currentHolder instanceof Entry)
				{
					String error = avatar.
						insertEntry(((Entry)currentHolder));
					if (null != error)
					{
						// out.println(definitionFilename);
						// out.println(definitionLineNumber);
						// out.println(error);
						// out.println(textDriver.parseMessage(error));
						// out.println(currentHolder);
						// out.println(textDriver.parseMessage(
						// 	"errorPlacementError", definitionFilename,
						// 	definitionLineNumber,
						// 	textDriver.parseMessage(error), currentHolder));

						throwErrorMessage("errorPlacementError",
							definitionFilename, definitionLineNumber,
							textDriver.parseMessage(error), currentHolder);
					}
				}
				else
				{
					throwErrorMessage("errorUnsupportedProperty",
						definitionFilename, definitionLineNumber, line);
				}
			}
			else if (startsWith(line, "tonnage "))
			{
				if (currentHolder instanceof Tonnage)
				{
					parameter = evaluateDouble(paramString,
						currentHolder, null);
					((Tonnage)currentHolder).setTonnage(parameter);
				}
				else
				{
					throwErrorMessage("errorUnsupportedProperty",
						definitionFilename, definitionLineNumber, line);
				}
			}
			else if (startsWith(line, "capacity "))
			{
				if (currentHolder instanceof Capacity)
				{
					parameter = evaluateDouble(paramString,
						currentHolder, null);
					((Capacity)currentHolder).setCapacity(parameter);
				}
				else
				{
					throwErrorMessage("errorUnsupportedProperty",
						definitionFilename, definitionLineNumber, line);
				}
			}
			else if (line.equals("show") || line.equals("reveal"))
			{
				if (currentHolder instanceof Container)
				{
					((Container)currentHolder).show();
				}
				else
				{
					throwErrorMessage("errorUnsupportedProperty",
						definitionFilename, definitionLineNumber, line);
				}
			}
			else if (line.equals("hide") || line.equals("conceal"))
			{
				if (currentHolder instanceof Container)
				{
					((Container)currentHolder).hide();
				}
				else
				{
					throwErrorMessage("errorUnsupportedProperty",
						definitionFilename, definitionLineNumber, line);
				}
			}
			else if (line.equals("public") || line.equals("private"))
			{
				if (currentHolder instanceof Trigger)
				{
					if (line.equals("public"))
						((Trigger)currentHolder).setPublic();
					else
						((Trigger)currentHolder).setPrivate();
				}
				else
				{
					throwErrorMessage("errorUnsupportedProperty",
						definitionFilename, definitionLineNumber, line);
				}
			}
			/* Not this way…
			else if (startsWith(line, "name in list "))
			{
				if (currentHolder instanceof Trigger)
				{
					((Trigger)currentHolder).setNameInList(paramString);
				}
				else
				{
					throwErrorMessage("errorUnsupportedProperty",
						definitionFilename, definitionLineNumber, line);
				}
			}
			*/
			else if (startsWith(line, "weight "))
			{
				if (currentHolder instanceof Weight)
				{
					parameter = evaluateDouble(paramString,
						currentHolder, null);
					((Weight)currentHolder).setWeight(parameter);
				}
				else
				{
					throwErrorMessage("errorUnsupportedProperty",
						definitionFilename, definitionLineNumber, line);
				}
			}
			else if (startsWith(line, "volume "))
			{
				if (currentHolder instanceof Volume)
				{
					parameter = evaluateDouble(paramString,
						currentHolder, null);
					((Volume)currentHolder).setVolume(parameter);
				}
				else
				{
					throwErrorMessage("errorUnsupportedProperty",
						definitionFilename, definitionLineNumber, line);
				}
			}
			else if (startsWith(line, "redefine "))
			{
				if (startsWith(paramString, "command line separator "))
				{
					commandLineSeparator = paramString.trim();
				}
				else if (startsWith(paramString, "end keyword "))
				{
					endKeyword = paramString;
				}
				else
				{
					throwErrorMessage("errorUnknownRedefinition",
						definitionFilename, definitionLineNumber, paramString);
				}
			}
			else if (startsWith(line, "define "))
			{
				if (startsWith(paramString, "property "))
				{
					if (null == currentHolder)
						throwErrorMessage("errorNullPropertyHolder",
							definitionFilename, definitionLineNumber,
							paramString);
					else
						currentHolder.defineProperty(paramString);
				}
				else if (startsWith(paramString, "command line separator "))
				{
					commandLineSeparator = paramString.trim();
				}
				else if (startsWith(paramString, "walk command "))
				{
					if (null == walkCommand)
					{
						walkCommand = addToken(paramString);
						checkUniqueToken(walkCommand);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "walk alias command "))
				{
					if (null != walkCommand)
					{
						String warning = addAlias(paramString, walkCommand);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullCommand", definitionFilename,
							definitionLineNumber, "walk");
				}
				else if (startsWith(paramString, "walk operator "))
				{
					if (null == walkOperator)
					{
						walkOperator = addToken(paramString);
						// checkUniqueToken(walkOperator);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "walk alias operator "))
				{
					if (null != walkOperator)
					{
						String warning = addAlias(paramString, walkOperator);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullOperator", definitionFilename,
							definitionLineNumber, "walk");
				}
				else if (startsWith(paramString, "return command "))
				{
					if (null == returnCommand)
					{
						returnCommand = addToken(paramString);
						checkUniqueToken(returnCommand);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "return alias command "))
				{
					if (null != returnCommand)
					{
						String warning = addAlias(paramString, returnCommand);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullCommand", definitionFilename,
							definitionLineNumber, "return");
				}
				else if (startsWith(paramString, "return operator "))
				{
					if (null == returnOperator)
					{
						returnOperator = addToken(paramString);
						// checkUniqueToken(returnOperator);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "return alias operator "))
				{
					if (null != returnOperator)
					{
						String warning = addAlias(paramString,
							returnOperator);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullOperator", definitionFilename,
							definitionLineNumber, "return");
				}
				else if (startsWith(paramString, "explore command "))
				{
					if (null == exploreCommand)
					{
						exploreCommand = addToken(paramString);
						checkUniqueToken(exploreCommand);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "explore alias command "))
				{
					if (null != exploreCommand)
					{
						String warning = addAlias(paramString, exploreCommand);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullCommand", definitionFilename,
							definitionLineNumber, "explore");
				}
				else if (startsWith(paramString, "explore operator "))
				{
					if (null == exploreOperator)
					{
						exploreOperator = addToken(paramString);
						// checkUniqueToken(exploreOperator);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "explore alias operator "))
				{
					if (null != exploreOperator)
					{
						String warning = addAlias(paramString,
							exploreOperator);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullOperator", definitionFilename,
							definitionLineNumber, "explore");
				}
				else if (startsWith(paramString, "pick command "))
				{
					if (null == pickCommand)
					{
						pickCommand = addToken(paramString);
						checkUniqueToken(pickCommand);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "pick alias command "))
				{
					if (null != pickCommand)
					{
						String warning = addAlias(paramString, pickCommand);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullCommand", definitionFilename,
							definitionLineNumber, "pick");
				}
				else if (startsWith(paramString, "pick operator "))
				{
					if (null == pickOperator)
					{
						pickOperator = addToken(paramString);
						// checkUniqueToken(pickOperator);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "pick alias operator "))
				{
					if (null != pickOperator)
					{
						String warning = addAlias(paramString, pickOperator);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullOperator", definitionFilename,
							definitionLineNumber, "pick");
				}
				else if (startsWith(paramString, "drop command "))
				{
					if (null == dropCommand)
					{
						dropCommand = addToken(paramString);
						checkUniqueToken(dropCommand);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "drop alias command "))
				{
					if (null != dropCommand)
					{
						String warning = addAlias(paramString, dropCommand);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullCommand", definitionFilename,
							definitionLineNumber, "drop");
				}
				else if (startsWith(paramString, "drop operator "))
				{
					if (null == dropOperator)
					{
						dropOperator = addToken(paramString);
						// checkUniqueToken(dropOperator);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "drop alias operator "))
				{
					if (null != dropOperator)
					{
						String warning = addAlias(paramString, dropOperator);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullOperator", definitionFilename,
							definitionLineNumber, "drop");
				}
				else if (startsWith(paramString, "throw command "))
				{
					if (null == throwCommand)
					{
						throwCommand = addToken(paramString);
						checkUniqueToken(throwCommand);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "throw alias command "))
				{
					if (null != throwCommand)
					{
						String warning = addAlias(paramString, throwCommand);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullCommand", definitionFilename,
							definitionLineNumber, "throw");
				}
				else if (startsWith(paramString, "throw operator "))
				{
					if (null == throwOperator)
					{
						throwOperator = addToken(paramString);
						// checkUniqueToken(throwOperator);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "throw alias operator "))
				{
					if (null != throwOperator)
					{
						String warning = addAlias(paramString, throwOperator);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullOperator", definitionFilename,
							definitionLineNumber, "throw");
				}
				else if (startsWith(paramString, "insert command "))
				{
					if (null == insertCommand)
					{
						insertCommand = addToken(paramString);
						checkUniqueToken(insertCommand);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "insert alias command "))
				{
					if (null != insertCommand)
					{
						String warning = addAlias(paramString, insertCommand);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullCommand", definitionFilename,
							definitionLineNumber, "insert");
				}
				else if (startsWith(paramString, "insert operator "))
				{
					if (null == insertOperator)
					{
						insertOperator = addToken(paramString);
						// checkUniqueToken(insertOperator);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "insert alias operator "))
				{
					if (null != insertOperator)
					{
						String warning = addAlias(paramString,
							insertOperator);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullOperator", definitionFilename,
							definitionLineNumber, "insert");
				}
				else if (startsWith(paramString, "remove command "))
				{
					if (null == removeCommand)
					{
						removeCommand = addToken(paramString);
						checkUniqueToken(removeCommand);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "remove alias command "))
				{
					if (null != removeCommand)
					{
						String warning = addAlias(paramString, removeCommand);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullCommand", definitionFilename,
							definitionLineNumber, "remove");
				}
				else if (startsWith(paramString, "remove operator "))
				{
					if (null == removeOperator)
					{
						removeOperator = addToken(paramString);
						// checkUniqueToken(removeOperator);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "remove alias operator "))
				{
					if (null != removeOperator)
					{
						String warning = addAlias(paramString,
							removeOperator);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullOperator", definitionFilename,
							definitionLineNumber, "remove");
				}
				else if (startsWith(paramString, "examine command "))
				{
					if (null == examineCommand)
					{
						examineCommand = addToken(paramString);
						checkUniqueToken(examineCommand);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "examine alias command "))
				{
					if (null != examineCommand)
					{
						String warning = addAlias(paramString, examineCommand);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullCommand", definitionFilename,
							definitionLineNumber, "examine");
				}
				else if (startsWith(paramString, "examine operator "))
				{
					if (null == examineOperator)
					{
						examineOperator = addToken(paramString);
						// checkUniqueToken(examineOperator);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "examine alias operator "))
				{
					if (null != examineOperator)
					{
						String warning = addAlias(paramString,
							examineOperator);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullOperator", definitionFilename,
							definitionLineNumber, "examine");
				}
				else if (startsWith(paramString, "use command "))
				{
					if (null == useCommand)
					{
						useCommand = addToken(paramString);
						checkUniqueToken(useCommand);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "use alias command "))
				{
					if (null != useCommand)
					{
						String warning = addAlias(paramString, useCommand);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullCommand", definitionFilename,
							definitionLineNumber, "use");
				}
				else if (startsWith(paramString, "use operator "))
				{
					if (null == useOperator)
					{
						useOperator = addToken(paramString);
						// checkUniqueToken(useOperator);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "use alias operator "))
				{
					if (null != useOperator)
					{
						String warning = addAlias(paramString, useOperator);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullOperator", definitionFilename,
							definitionLineNumber, "use");
				}
				else if (startsWith(paramString, "action command "))
				{
					if (null == actionCommand)
					{
						actionCommand = addToken(paramString);
						checkUniqueToken(actionCommand);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "action alias command "))
				{
					if (null != actionCommand)
					{
						String warning = addAlias(paramString, actionCommand);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullCommand", definitionFilename,
							definitionLineNumber, "action");
				}
				else if (startsWith(paramString, "action operator "))
				{
					if (null == actionOperator)
					{
						actionOperator = addToken(paramString);
						// checkUniqueToken(actionOperator);
					}
					else
						throwErrorMessage("errorInvalidRedefinition",
							definitionFilename, definitionLineNumber, line);
				}
				else if (startsWith(paramString, "action alias operator "))
				{
					if (null != actionOperator)
					{
						String warning = addAlias(paramString, actionOperator);

						if (null != warning)
						{
							textDriver.parseAndWriteWarning(
								warning, definitionFilename,
								definitionLineNumber, paramString);
						}
					}
					else
						textDriver.parseAndWriteWarning(
							"warningNullOperator", definitionFilename,
							definitionLineNumber, "action");
				}
				else if (startsWith(paramString, "trigger "))
				{
					tempTrigger = new Trigger(paramString);
					checkUniqueToken(addToken(tempTrigger.name));
					triggers.add(tempTrigger);
					currentHolder = tempTrigger;
					lastTransition = null;
					lastReverseTransition = null;
				}
				else
					throwErrorMessage("errorUnknownDefinition",
						definitionFilename, definitionLineNumber,
							paramString);
			}
			else if (line.equals("startup"))
			{
				currentHolder = startup;
				lastTransition = null;
				lastReverseTransition = null;
			}
			else if (line.equals("read state"))
			{
				currentHolder = readState;
				lastTransition = null;
				lastReverseTransition = null;
			}
			else if (line.equals("query"))
			{
				currentHolder = query;
				lastTransition = null;
				lastReverseTransition = null;
			}
			else if (line.equals("populate"))
			{
				currentHolder = populate;
				lastTransition = null;
				lastReverseTransition = null;
			}
			else if (line.equals("menu item"))
			{
				definitionFlag = 1; // menu item
			}
			else if (startsWith(line, "menu item "))
			{
				definitionFlag = 1; // menu item
				textDriver.defineMenuItemText(paramString);
			}
			else if (line.equals("menu separator"))
			{
				textDriver.insertSeparator();
			}
			else if (line.equals("main menu item"))
			{
				definitionFlag = 2; // main menu item
			}
			else if (startsWith(line, "main menu item "))
			{
				definitionFlag = 2; // main menu item
				textDriver.defineMenuItemText(paramString);
			}
			else if (line.equals("main menu clear"))
			{
				textDriver.clearMainMenu();
			}
			else if (line.equals("add default menu item open"))
			{
				textDriver.insertOpenMenuItem();
			}
			else if (line.equals("add default menu item save"))
			{
				textDriver.insertSaveMenuItem();
			}
			else if (line.equals("add default menu item exit"))
			{
				textDriver.insertExitMenuItem();
			}
			else if (line.equals("add default menu item about"))
			{
				textDriver.insertAboutMenuItem();
			}
			else if (line.equals("context menu item"))
			{
				definitionFlag = 3; // context menu item
			}
			else if (startsWith(line, "context menu item "))
			{
				definitionFlag = 3; // context menu item
				textDriver.defineMenuItemText(paramString);
			}
			else if (line.equals("context menu separator"))
			{
				if (currentHolder instanceof Trigger &&
					null != ((Trigger)currentHolder).contextMenu)
					((Trigger)currentHolder).contextMenu.addSeparator();
				else
					out.println("Unknown target of context " +
						"menu separator in " + definitionFilename +
						" at line " + definitionLineNumber +
						". Separator was ignored.");
			}
			else if (startsWith(line, "double click "))
			{
				if (currentHolder instanceof Trigger)
					((Trigger)currentHolder).setDoubleClick(paramString);
				else
					throwErrorMessage("errorUnsupportedProperty",
						definitionFilename, definitionLineNumber, line);
			}
			else if (startsWith(line, "style "))
			{
				currentStyle = textDriver.newStyle(paramString);
				/*
					err.println("  Error: unknown style “" + paramString +
						"” at line " + definitionLineNumber + " in " +
						definitionFilename + ".");
				*/
			}
			else if (line.equals("on show") || line.equals("on reveal"))
			{
				if (currentHolder instanceof Location)
					currentScript = ((Location)currentHolder).onReveal;
				else if (currentHolder instanceof Item)
					currentScript = ((Item)currentHolder).onShow;
				else
					throwErrorMessage("errorUnknownHandler",
						definitionFilename, definitionLineNumber, line);
			}
			else if (line.equals("on hide") || line.equals("on conceal"))
			{
				if (currentHolder instanceof Location)
					currentScript = ((Location)currentHolder).onConceal;
				else if (currentHolder instanceof Item)
					currentScript = ((Item)currentHolder).onHide;
				else
					throwErrorMessage("errorUnknownHandler",
						definitionFilename, definitionLineNumber, line);
			}
			else if (line.equals("on enter"))
			{
				if (currentHolder instanceof Location)
					currentScript = ((Location)currentHolder).onEnter;
				else
					throwErrorMessage("errorUnknownHandler",
						definitionFilename, definitionLineNumber, line);
			}
			else if (line.equals("on leave"))
			{
				if (currentHolder instanceof Location)
					currentScript = ((Location)currentHolder).onLeave;
				else
					throwErrorMessage("errorUnknownHandler",
						definitionFilename, definitionLineNumber, line);
			}
			else if (line.equals("on explore"))
			{
				if (currentHolder instanceof Location)
					currentScript = ((Location)currentHolder).onExplore;
				else
					throwErrorMessage("errorUnknownHandler",
						definitionFilename, definitionLineNumber, line);
			}
			else if (line.equals("on pick"))
			{
				if (currentHolder instanceof Item)
					currentScript = ((Item)currentHolder).onPick;
				else
					throwErrorMessage("errorUnknownHandler",
						definitionFilename, definitionLineNumber, line);
			}
			else if (line.equals("on drop"))
			{
				if (currentHolder instanceof Item)
					currentScript = ((Item)currentHolder).onDrop;
				else
					throwErrorMessage("errorUnknownHandler",
						definitionFilename, definitionLineNumber, line);
			}
			else if (line.equals("on throw"))
			{
				if (currentHolder instanceof Item)
					currentScript = ((Item)currentHolder).onThrow;
				else
					throwErrorMessage("errorUnknownHandler",
						definitionFilename, definitionLineNumber, line);
			}
			else if (line.equals("on insert"))
			{
				if (currentHolder instanceof Item)
					currentScript = ((Item)currentHolder).onInsert;
				else
					throwErrorMessage("errorUnknownHandler",
						definitionFilename, definitionLineNumber, line);
			}
			else if (line.equals("on admit"))
			{
				if (currentHolder instanceof Item)
					currentScript = ((Item)currentHolder).onAdmit;
				else
					throwErrorMessage("errorUnknownHandler",
						definitionFilename, definitionLineNumber, line);
			}
			else if (line.equals("on remove"))
			{
				if (currentHolder instanceof Item)
					currentScript = ((Item)currentHolder).onRemove;
				else
					throwErrorMessage("errorUnknownHandler",
						definitionFilename, definitionLineNumber, line);
			}
			else if (line.equals("on release"))
			{
				if (currentHolder instanceof Item)
					currentScript = ((Item)currentHolder).onRelease;
				else
					throwErrorMessage("errorUnknownHandler",
						definitionFilename, definitionLineNumber, line);
			}
			else if (line.equals("on examine"))
			{
				if (currentHolder instanceof Item)
					currentScript = ((Item)currentHolder).onExamine;
				else
					throwErrorMessage("errorUnknownHandler",
						definitionFilename, definitionLineNumber, line);
			}
			else if (line.equals("on use"))
			{
				if (currentHolder instanceof Item)
					currentScript = ((Item)currentHolder).onUse;
				else
					throwErrorMessage("errorUnknownHandler",
						definitionFilename, definitionLineNumber, line);
			}
			else if (line.equals("on action"))
			{
				if (currentHolder instanceof Item)
					currentScript = ((Item)currentHolder).onAction;
				else
					throwErrorMessage("errorUnknownHandler",
						definitionFilename, definitionLineNumber, line);
			}
			else if (line.equals("on drive"))
			{
				if (currentHolder instanceof Item)
					currentScript = ((Item)currentHolder).onDrive;
				else
					throwErrorMessage("errorUnknownHandler",
						definitionFilename, definitionLineNumber, line);
			}
			else if (line.equals("on invoke"))
			{
				if (currentHolder instanceof Trigger)
					currentScript = ((Trigger)currentHolder).onInvoke;
				else
					throwErrorMessage("errorUnknownHandler",
						definitionFilename, definitionLineNumber, line);
			}
			else if (startsWith(line, "on "))
			{
				if (currentHolder instanceof Avatar)
				{
					if (paramString.equals("common examine"))
					{
						currentScript = ((Avatar)currentHolder).
							onCommonExamine;
					}
					else if (startsWith(paramString, "common "))
					{
						if (paramString.equals("success"))
							currentScript = ((Avatar)currentHolder).
								onCommonSuccess;
						else if (paramString.equals("failure"))
							currentScript = ((Avatar)currentHolder).
								onCommonFailure;
						else
							throwErrorMessage(
								"errorUnknownHandler", definitionFilename,
								definitionLineNumber, line);
					}
					else if (startsWith(paramString, "walk "))
					{
						if (paramString.equals("success"))
							currentScript = ((Avatar)currentHolder).
								onWalkSuccess;
						else if (paramString.equals("failure"))
							currentScript = ((Avatar)currentHolder).
								onWalkFailure;
						else
							throwErrorMessage(
								"errorUnknownHandler", definitionFilename,
								definitionLineNumber, line);
					}
					else if (startsWith(paramString, "return "))
					{
						if (paramString.equals("success"))
							currentScript = ((Avatar)currentHolder).
								onReturnSuccess;
						else if (paramString.equals("failure"))
							currentScript = ((Avatar)currentHolder).
								onReturnFailure;
						else
							throwErrorMessage(
								"errorUnknownHandler", definitionFilename,
								definitionLineNumber, line);
					}
					else if (startsWith(paramString, "explore "))
					{
						if (paramString.equals("success"))
							currentScript = ((Avatar)currentHolder).
								onExploreSuccess;
						else if (paramString.equals("failure"))
							currentScript = ((Avatar)currentHolder).
								onExploreFailure;
						else
							throwErrorMessage(
								"errorUnknownHandler", definitionFilename,
								definitionLineNumber, line);
					}
					else if (startsWith(paramString, "pick "))
					{
						if (paramString.equals("success"))
							currentScript = ((Avatar)currentHolder).
								onPickSuccess;
						else if (paramString.equals("failure"))
							currentScript = ((Avatar)currentHolder).
								onPickFailure;
						else
							throwErrorMessage(
								"errorUnknownHandler", definitionFilename,
								definitionLineNumber, line);
					}
					else if (startsWith(paramString, "drop "))
					{
						if (paramString.equals("success"))
							currentScript = ((Avatar)currentHolder).
								onDropSuccess;
						else if (paramString.equals("failure"))
							currentScript = ((Avatar)currentHolder).
								onDropFailure;
						else
							throwErrorMessage(
								"errorUnknownHandler", definitionFilename,
								definitionLineNumber, line);
					}
					else if (startsWith(paramString, "throw "))
					{
						if (paramString.equals("success"))
							currentScript = ((Avatar)currentHolder).
								onThrowSuccess;
						else if (paramString.equals("failure"))
							currentScript = ((Avatar)currentHolder).
								onThrowFailure;
						else
							throwErrorMessage(
								"errorUnknownHandler", definitionFilename,
								definitionLineNumber, line);
					}
					else if (startsWith(paramString, "insert "))
					{
						if (paramString.equals("success"))
							currentScript = ((Avatar)currentHolder).
								onInsertSuccess;
						else if (paramString.equals("failure"))
							currentScript = ((Avatar)currentHolder).
								onInsertFailure;
						else
							throwErrorMessage(
								"errorUnknownHandler", definitionFilename,
								definitionLineNumber, line);
					}
					else if (startsWith(paramString, "remove "))
					{
						if (paramString.equals("success"))
							currentScript = ((Avatar)currentHolder).
								onRemoveSuccess;
						else if (paramString.equals("failure"))
							currentScript = ((Avatar)currentHolder).
								onRemoveFailure;
						else
							throwErrorMessage(
								"errorUnknownHandler", definitionFilename,
								definitionLineNumber, line);
					}
					else if (startsWith(paramString, "examine "))
					{
						if (paramString.equals("success"))
							currentScript = ((Avatar)currentHolder).
								onExamineSuccess;
						else if (paramString.equals("failure"))
							currentScript = ((Avatar)currentHolder).
								onExamineFailure;
						else
							throwErrorMessage(
								"errorUnknownHandler", definitionFilename,
								definitionLineNumber, line);
					}
					else if (startsWith(paramString, "use "))
					{
						if (paramString.equals("success"))
							currentScript = ((Avatar)currentHolder).
								onUseSuccess;
						else if (paramString.equals("failure"))
							currentScript = ((Avatar)currentHolder).
								onUseFailure;
						else
							throwErrorMessage(
								"errorUnknownHandler", definitionFilename,
								definitionLineNumber, line);
					}
					else if (startsWith(paramString, "action "))
					{
						if (paramString.equals("success"))
							currentScript = ((Avatar)currentHolder).
								onActionSuccess;
						else if (paramString.equals("failure"))
							currentScript = ((Avatar)currentHolder).
								onActionFailure;
						else
							throwErrorMessage(
								"errorUnknownHandler", definitionFilename,
								definitionLineNumber, line);
					}
				}
				else
					throwErrorMessage("errorUnknownHandler",
						definitionFilename, definitionLineNumber, line);
			}
			else if (startsWith(line, "set "))
			{
				if (startsWith(paramString, "property "))
				{
					if (null == currentHolder)
						throwErrorMessage("errorNullPropertyHolder",
							definitionFilename, definitionLineNumber,
							paramString);
					else
					{
						int indexOf = paramString.indexOf('=');
						if (-1 != indexOf)
						{
							String property = paramString.
								substring(0, indexOf);
							String expression = paramString.
								substring(indexOf + 1);

							parameter = evaluateDouble(expression,
								currentHolder, null);

							if (!currentHolder.setProperty(property,
								parameter)) throwErrorMessage(
									"errorUnknownProperty", definitionFilename,
									definitionLineNumber, property);
						}
						else
							throwErrorMessage(
								"syntaxError", definitionFilename,
								definitionLineNumber, paramString);
					}
				}
				else if (startsWith(paramString, "title "))
					textDriver.setTitle(paramString);
				else if (startsWith(paramString, "input line label "))
					textDriver.setInputLineLabel(paramString);
				else if (startsWith(paramString, "central text label "))
					textDriver.setCentralTextLabel(paramString);
				else if (startsWith(paramString, "locations list label "))
					textDriver.locationsList().setLabelText(paramString);
				else if (startsWith(paramString, "items list label "))
					textDriver.itemsList().setLabelText(paramString);
				else if (startsWith(paramString, "inventory list label "))
					textDriver.inventoryList().setLabelText(paramString);
				else if (startsWith(paramString, "inserted list label "))
					textDriver.insertedList().setLabelText(paramString);
				else throwErrorMessage("errorUnknownComponent",
					definitionFilename, definitionLineNumber, paramString);
			}
			else if (startsWith(line, "clear "))
			{
				if (paramString.equals("title"))
					textDriver.setTitle(null);
				else if (paramString.equals("input line label"))
					textDriver.setInputLineLabel(null);
				else if (paramString.equals("central text label"))
					textDriver.setCentralTextLabel(null);
				else if (paramString.equals("locations list label"))
					textDriver.locationsList().setLabelText(null);
				else if (paramString.equals("items list label"))
					textDriver.itemsList().setLabelText(null);
				else if (paramString.equals("inventory list label"))
					textDriver.inventoryList().setLabelText(null);
				else if (paramString.equals("inserted list label"))
					textDriver.insertedList().setLabelText(null);
				else if (paramString.equals("locations list"))
					textDriver.locationsList().clear();
				else if (paramString.equals("items list"))
					textDriver.itemsList().clear();
				else if (paramString.equals("inventory list"))
					textDriver.inventoryList().clear();
				else if (paramString.equals("inserted list"))
					textDriver.insertedList().clear();
				else if (paramString.equals("screen") ||
					paramString.equals("central text"))
					textDriver.clearScreen();
				else if (paramString.equals("main menu"))
					textDriver.clearMainMenu();
				else throwErrorMessage("errorUnknownComponent",
					definitionFilename, definitionLineNumber, paramString);
			}
			else if (startsWith(line, "enable "))
			{
				if (paramString.equals("autopopulate"))
					textDriver.enableAutopopulate();
				else if (paramString.equals("autoexplore"))
					autoexplore = true;
				else if (paramString.equals("tokenStartsChecking"))
					tokenStartsChecking = true;
				else throwErrorMessage("errorUnsupportedProperty",
					definitionFilename, definitionLineNumber, paramString);
			}
			else if (startsWith(line, "disable "))
			{
				if (paramString.equals("autopopulate"))
					textDriver.disableAutopopulate();
				else if (paramString.equals("autoexplore"))
					autoexplore = false;
				else if (paramString.equals("tokenStartsChecking"))
					tokenStartsChecking = false;
				else throwErrorMessage("errorUnsupportedProperty",
					definitionFilename, definitionLineNumber, paramString);
			}
			else if (startsWith(line, "show "))
			{
				if (paramString.equals("locations list"))
				{
					textDriver.locationsList().setVisible(true);
					textDriver.reorderListsPane();
				}
				else if (paramString.equals("items list"))
				{
					textDriver.itemsList().setVisible(true);
					textDriver.reorderListsPane();
				}
				else if (paramString.equals("inventory list"))
				{
					textDriver.inventoryList().setVisible(true);
					textDriver.reorderListsPane();
				}
				else if (paramString.equals("inserted list"))
				{
					textDriver.insertedList().setVisible(true);
					textDriver.reorderListsPane();
				}
				else throwErrorMessage("errorUnknownComponent",
					definitionFilename, definitionLineNumber, line);
			}
			else if (startsWith(line, "hide "))
			{
				if (paramString.equals("locations list"))
				{
					textDriver.locationsList().setVisible(false);
					textDriver.reorderListsPane();
				}
				else if (paramString.equals("items list"))
				{
					textDriver.itemsList().setVisible(false);
					textDriver.reorderListsPane();
				}
				else if (paramString.equals("inventory list"))
				{
					textDriver.inventoryList().setVisible(false);
					textDriver.reorderListsPane();
				}
				else if (paramString.equals("inserted list"))
				{
					textDriver.insertedList().setVisible(false);
					textDriver.reorderListsPane();
				}
				else throwErrorMessage("errorUnknownComponent",
					definitionFilename, definitionLineNumber, line);
			}
			else
			{
				throwErrorMessage("errorUnexpectedCommand",
					definitionFilename, definitionLineNumber, line);
			}
		}

		if (null != currentScript)
			throwErrorMessage("errorUnexpectedEndOfFile",
				definitionFilename, definitionLineNumber);

		definition.close();
		debugClear();
	}


	public class ScriptInstance
	{
		private String endIfKeyword = "end if";
		private String endForKeyword = "end for";

		private boolean exit = false;
		private String filenameBak = null;
		private int lineNumberBak = 0;
		private int startLine = 0;
		private int forLevel = 0;

		private final Vector<String> searchEnd = new Vector<String>();
		private final Vector<String> forVars = new Vector<String>();

		public final String scriptName;
		private final Vector<String> script;
		private final Holder currentHolder;
		private final Vector<String> parameters;

		private ScriptInstance(String scriptName, Vector<String> script,
			Holder currentHolder, Vector<String> parameters)
		{
			this.scriptName = scriptName;
			this.script = script;
			this.currentHolder = currentHolder;
			this.parameters = parameters;
			init();
		}

		private String getLine(int i)
		{
			definitionFilename = scriptName;
			definitionLineNumber = i + 1;
			return replaceProperties(replaceForVars(
				script.elementAt(i).trim(), forVars),
				currentHolder, parameters);
		}

		private void init()
		{
			// save definition data
			filenameBak = definitionFilename;
			lineNumberBak = definitionLineNumber;

			if (startLine < script.size()) do
			{
				String line = getLine(startLine);

				if (startsWith(line, "redefine "))
				{
					if (startsWith(paramString, "end if keyword "))
					{
						endIfKeyword = paramString;
					}
					else if (startsWith(paramString, "end for keyword "))
					{
						endForKeyword = paramString;
					}
					else
					{
						throwErrorMessage(
							"errorUnknownRedefinition",
							scriptName, startLine, paramString);
					}

					++startLine; continue;
				}

				break;
			}
			while (startLine < script.size());
		}

		private int findEnd(int from)
		{
			searchEnd.clear();
			String printEnd = null;

			for (int i = from; i < script.size(); ++i)
			{
				String line = getLine(i);

				if (null != printEnd)
				{
					if (line.equals(printEnd)) printEnd = null;
				}
				else if (0 < searchEnd.size() &&
					line.equals(searchEnd.lastElement()))
				{
					searchEnd.removeElementAt(searchEnd.size() - 1);
				}
				else if (line.startsWith("if "))
				{
					searchEnd.add(endIfKeyword);
				}
				else if (line.startsWith("for "))
				{
					searchEnd.add(endForKeyword);
				}
				else if (line.equals("print block"))
				{
					printEnd = "print end";
				}
				else if (line.startsWith("print block "))
				{
					// paramString
					printEnd = line.substring(12);
				}

				if (0 == searchEnd.size()) return i;
			}

			return -1;
		}

		private boolean matchVisibilityCondition(
			Container container, int condition)
		{
			if (-1 == condition) return true;
			if (1 == condition) return container.isVisible();
			return !container.isVisible();
		}

		private boolean run(int start, int end) // run range
		{
			String printEnd = null;
			Location tempLocation = null, tempDestination = null;
			Item tempItem = null, tempTool = null;
			Transition tempTransition = null;
			Trigger tempTrigger = null;
			Container tempContainer = null;
			Entry tempEntry = null;
			String tempError = null;

			for (int i = start; i < end; ++i)
			{
				String line = getLine(i);

				if (null != printEnd)
				{
					if (line.equals(printEnd)) printEnd = null; else
						textDriver.writeLine(line);
				}
				else if (line.equals("exit"))
				{
					exit = true;
					return true;
				}
				else if (startsWith(line, "exit "))
				{
					exit = true;
					successMessage = paramString;
					return true;
				}
				else if (line.equals("deny"))
				{
					failureMessage = textDriver.parseMessage(
						"alertOperationDenied");
					return false;
				}
				else if (startsWith(line, "deny "))
				{
					failureMessage = paramString;
					return false;
				}
				else if (line.equals("fail"))
				{
					throwErrorMessage("alertOperationFailed");
				}
				else if (startsWith(line, "fail "))
				{
					throw new RuntimeException(paramString);
				}
				else if (startsWith(line, "success message "))
				{
					successMessage = paramString;
				}
				else if (startsWith(line, "failure message "))
				{
					failureMessage = paramString;
				}
				else if (line.equals("query"))
				{
					if (queryFlag)
					{
						// Should not happen (but checking it to be sure)
						err.println("  Error: double query at line " +
							(i + 1) + " in " + scriptName + ".");
					}
					else
					{
						exit = true;
						queryInputLineLabelBackup =
							textDriver.getInputLineLabel();
						textDriver.setInputLineLabel(null);
						queryFlag = true;
					}
					return true;
				}
				else if (startsWith(line, "query "))
				{
					if (queryFlag)
					{
						// Should not happen (but checking it to be sure)
						err.println("  Error: double query at line " +
							(i + 1) + " in " + scriptName + ".");
					}
					else
					{
						exit = true;
						queryInputLineLabelBackup =
							textDriver.getInputLineLabel();
						textDriver.setInputLineLabel(paramString);
						queryFlag = true;
					}
					return true;
				}
				else if (startsWith(line, "call "))
				{
					if (!invokeTrigger(paramString, false))
						return false;
				}
				else if (startsWith(line, "run "))
				{
					// Ignores deny command (might not be safe)
					String failureBackup = failureMessage;
					invokeTrigger(paramString, false);
					failureMessage = failureBackup;
				}
				else if (startsWith(line, "set "))
				{
					if (startsWith(paramString, "title "))
						textDriver.setTitle(paramString);
					else if (startsWith(paramString, "input line label "))
						textDriver.setInputLineLabel(paramString);
					else if (startsWith(paramString, "central text label "))
						textDriver.setCentralTextLabel(paramString);
					else if (startsWith(paramString, "locations list label "))
						textDriver.locationsList().setLabelText(paramString);
					else if (startsWith(paramString, "items list label "))
						textDriver.itemsList().setLabelText(paramString);
					else if (startsWith(paramString, "inventory list label "))
						textDriver.inventoryList().setLabelText(paramString);
					else if (startsWith(paramString, "inserted list label "))
						textDriver.insertedList().setLabelText(paramString);
					else
					{
						int indexOf = paramString.indexOf('=');
						if (-1 != indexOf)
						{
							String property = paramString.
								substring(0, indexOf);
							String expression = paramString.
								substring(indexOf + 1);
							setProperty(property, expression,
								currentHolder, parameters);
						}
						else throwErrorMessage(
							"errorUnknownComponent", scriptName,
							i + 1, paramString);
					}
				}
				else if (startsWith(line, "add "))
				{
					TextDriverInterface.ScrollList list = null;

					if (startsWith(paramString, "locations list item "))
						list = textDriver.locationsList();
					else if (startsWith(paramString, "items list item "))
						list = textDriver.itemsList();
					else if (startsWith(paramString, "inventory list item "))
						list = textDriver.inventoryList();
					else if (startsWith(paramString, "inserted list item "))
						list = textDriver.insertedList();

					if (null == list)
						throwErrorMessage("errorUnknownComponent",
							scriptName, i + 1, paramString);

					tempLocation = findLocation(paramString);
					if (null != tempLocation)
						list.add(tempLocation);
					else
					{
						tempItem = findItem(paramString);
						if (null != tempItem)
							list.add(tempItem);
						else
						{
							tempTrigger = findTrigger(paramString);
							if (null != tempTrigger)
								list.add(tempTrigger);
							else
								throwErrorMessage(
									"errorUnknownEntity", scriptName, i + 1,
									paramString);
						}
					}
				}
				else if (startsWith(line, "clear "))
				{
					if (paramString.equals("title"))
						textDriver.setTitle(null);
					else if (paramString.equals("input line label"))
						textDriver.setInputLineLabel(null);
					else if (paramString.equals("central text label"))
						textDriver.setCentralTextLabel(null);
					else if (paramString.equals("locations list label"))
						textDriver.locationsList().setLabelText(null);
					else if (paramString.equals("items list label"))
						textDriver.itemsList().setLabelText(null);
					else if (paramString.equals("inventory list label"))
						textDriver.inventoryList().setLabelText(null);
					else if (paramString.equals("inserted list label"))
						textDriver.insertedList().setLabelText(null);
					else if (paramString.equals("locations list"))
						textDriver.locationsList().clear();
					else if (paramString.equals("items list"))
						textDriver.itemsList().clear();
					else if (paramString.equals("inventory list"))
						textDriver.inventoryList().clear();
					else if (paramString.equals("inserted list"))
						textDriver.insertedList().clear();
					else if (paramString.equals("screen") ||
						paramString.equals("central text"))
						textDriver.clearScreen();
					else if (paramString.equals("style"))
						textDriver.clearStyle();
					else throwErrorMessage("errorUnknownComponent",
						scriptName, i + 1, paramString);
				}
				else if (startsWith(line, "style "))
				{
					int indexOf;
					if (paramString.equals("none"))
					{
						textDriver.clearStyle();
					}
					else if (-1 == (indexOf = paramString.indexOf(" ")))
					{
						textDriver.setStyle(paramString);
					}
					else
					{
						SimpleAttributeSet currentStyle = textDriver.getStyle(
							paramString.substring(0, indexOf));
						paramString = paramString.substring(indexOf + 1);

						if (null == currentStyle)
						{
							err.println("  Error: unknown style “" +
								paramString + "” at line " + (i + 1) +
								" in " + scriptName + ".");
						}
						else
						{
							if (paramString.equals("clear"))
							{
								currentStyle.removeAttributes(
									currentStyle.getAttributeNames());
							}
							else if (startsWith(paramString, "color "))
							{
								try {
									StyleConstants.setForeground(currentStyle,
										Color.decode(paramString));
								} catch (Exception e) { err.println(e); }
							}
							else if (startsWith(paramString, "size "))
							{
								try {
									StyleConstants.setFontSize(currentStyle,
										Integer.parseInt(paramString));
								} catch (Exception e) { err.println(e); }
							}
							else if (startsWith(paramString, "background "))
							{
								try {
									StyleConstants.setBackground(currentStyle,
										Color.decode(paramString));
								} catch (Exception e) { err.println(e); }
							}
							else if (startsWith(paramString, "font "))
							{
								StyleConstants.setFontFamily(
									currentStyle, paramString);
							}
							else if (startsWith(paramString, "bold "))
							{
								StyleConstants.setBold(currentStyle,
									Boolean.parseBoolean(paramString));
							}
							else if (startsWith(paramString, "italic "))
							{
								StyleConstants.setItalic(currentStyle,
									Boolean.parseBoolean(paramString));
							}
							else if (startsWith(paramString, "strike "))
							{
								StyleConstants.setStrikeThrough(currentStyle,
									Boolean.parseBoolean(paramString));
							}
							else if (startsWith(paramString, "subscript "))
							{
								StyleConstants.setSubscript(currentStyle,
									Boolean.parseBoolean(paramString));
							}
							else if (startsWith(paramString, "superscript "))
							{
								StyleConstants.setSuperscript(currentStyle,
									Boolean.parseBoolean(paramString));
							}
							else if (startsWith(paramString, "underline "))
							{
								StyleConstants.setUnderline(currentStyle,
									Boolean.parseBoolean(paramString));
							}
							else
								err.println("  Error: unknown style " +
									"property “" + paramString +
									"” at line " + (i + 1) + " in " +
									scriptName + ".");
						}
					}
				}
				else if (startsWith(line, "print "))
				{
					if (startsWith(paramString, "image "))
						textDriver.insertIcon(paramString);
					else if (startsWith(paramString, "block "))
						printEnd = paramString;
					else if (startsWith(paramString, "line "))
						textDriver.writeLine(paramString);
					else if (startsWith(paramString, "words "))
						textDriver.write(paramString);
					else if (paramString.equals("line"))
						textDriver.writeLine();
					else if (paramString.equals("space"))
						textDriver.write(" ");
					else if (paramString.equals("block"))
						printEnd = "print end";
					else if (paramString.equals("words") ||
						paramString.equals("image")); // ignore
					else if (paramString.equals("parameters"))
					{
						boolean first = true;
						for (String parameter : parameters)
						{
							if (first) first = false; else
							textDriver.write(" ");
							textDriver.write(parameter);
						}
					}
					else
						textDriver.writeLine(paramString);
				}
				else if (startsWith(line, "insert "))
				{
					if (tokenize(tokenizedCommand, paramString))
					{
						if (tokenizedCommand.size() < 2)
							throwErrorMessage(
								"errorInvalidNumberOfTokens",
								scriptName, i + 1);

						if (avatarToken.equals(
							tokenizedCommand.elementAt(0)))
							tempEntry = avatar;
						else
							tempEntry = findItem(
								tokenizedCommand.elementAt(0));

						if (null == tempEntry)
							throwErrorMessage(
								"errorUnknownEntity", scriptName, i + 1,
								tokenizedCommand.elementAt(0));

						if (avatarToken.equals(
							tokenizedCommand.elementAt(1)))
							tempContainer = avatar;
						else
						{
							tempContainer = findLocation(
								tokenizedCommand.elementAt(1));

							if (null == tempContainer)
							{
								tempContainer = findItem(
									tokenizedCommand.elementAt(1));

								if (null == tempContainer)
									throwErrorMessage(
										"errorUnknownEntity", scriptName,
										i + 1, tokenizedCommand.elementAt(1));
							}
						}

						if (null != (tempError = tempContainer.
							insertEntry(tempEntry)))
							throwErrorMessage(tempError, tempEntry);
					}
				}
				else if (startsWith(line, "remove "))
				{
					if (tokenize(tokenizedCommand, paramString))
					{
						if (tokenizedCommand.size() < 1)
							throwErrorMessage(
								"errorInvalidNumberOfTokens",
								scriptName, i + 1);

						if (avatarToken.equals(
							tokenizedCommand.elementAt(0)))
							tempEntry = avatar;
						else
							tempEntry = findItem(
								tokenizedCommand.elementAt(0));

						if (null == tempEntry)
							throwErrorMessage(
								"errorUnknownEntity", scriptName, i + 1,
								tokenizedCommand.elementAt(0));

						if (tokenizedCommand.size() < 2)
						{
							tempContainer = tempEntry.getPlacement();

							if (null == tempContainer)
								throwErrorMessage(
									"errorInvalidPlacement",
									scriptName, i + 1, tempEntry);
						}
						else
						{
							if (avatarToken.equals(
								tokenizedCommand.elementAt(1)))
								tempContainer = avatar;
							else
							{
								tempContainer = findLocation(
									tokenizedCommand.elementAt(1));

								if (null == tempContainer)
								{
									tempContainer = findItem(
										tokenizedCommand.elementAt(1));

									if (null == tempContainer)
										throwErrorMessage(
											"errorUnknownEntity",
											scriptName, i + 1,
											tokenizedCommand.elementAt(1));
								}
							}
						}

						if (null != (tempError = tempContainer.
							removeEntry(tempEntry)))
							throwErrorMessage(tempError, tempEntry);
					}
				}
				else if (startsWith(line, "teleport "))
				{
					if (tokenize(tokenizedCommand, paramString))
					{
						if (tokenizedCommand.size() < 2)
							throwErrorMessage(
								"errorInvalidNumberOfTokens",
								scriptName, i + 1);

						if (avatarToken.equals(
							tokenizedCommand.elementAt(0)))
							tempEntry = avatar;
						else
							tempEntry = findItem(
								tokenizedCommand.elementAt(0));

						if (null == tempEntry)
							throwErrorMessage(
								"errorUnknownEntity", scriptName, i + 1,
								tokenizedCommand.elementAt(0));

						// Remove from anywhere
						if (null != (tempContainer =
								tempEntry.getPlacement()) &&
							null != (tempError =
								tempContainer.removeEntry(tempEntry)))
						{
							// Error
							throwErrorMessage(
								tempError, tempEntry);
						}

						// Insert
						if (avatarToken.equals(
							tokenizedCommand.elementAt(1)))
							tempContainer = avatar;
						else
						{
							tempContainer = findLocation(
								tokenizedCommand.elementAt(1));

							if (null == tempContainer)
							{
								tempContainer = findItem(
									tokenizedCommand.elementAt(1));

								if (null == tempContainer)
									throwErrorMessage(
										"errorUnknownEntity", scriptName,
										i + 1, tokenizedCommand.elementAt(1));
							}
						}

						if (null != (tempError = tempContainer.
							insertEntry(tempEntry)))
							throwErrorMessage(tempError, tempEntry);
					}
				}
				else if (startsWith(line, "open "))
				{
					if (tokenize(tokenizedCommand, paramString))
					{
						if (tokenizedCommand.size() < 2)
							throwErrorMessage(
								"errorInvalidNumberOfTokens",
								scriptName, i + 1);

						tempLocation = findLocation(
							tokenizedCommand.elementAt(0));

						if (null == tempLocation)
							throwErrorMessage(
								"errorLocationNotFound", scriptName, i + 1,
								tokenizedCommand.elementAt(0));

						tempDestination = findLocation(
							tokenizedCommand.elementAt(1));

						if (null == tempLocation)
							throwErrorMessage(
								"errorLocationNotFound", scriptName, i + 1,
								tokenizedCommand.elementAt(1));

						tempTransition = tempLocation.
							getTransition(tempDestination);

						if (null == tempTransition)
							throwErrorMessage(
								"errorTransitionNotFound",
								scriptName, i + 1, tempLocation,
								tempDestination);

						tempTransition.show();
					}
				}
				else if (startsWith(line, "show ") ||
					startsWith(line, "reveal "))
				{
					if (paramString.equals("locations list"))
					{
						textDriver.locationsList().setVisible(true);
						textDriver.reorderListsPane();
					}
					else if (paramString.equals("items list"))
					{
						textDriver.itemsList().setVisible(true);
						textDriver.reorderListsPane();
					}
					else if (paramString.equals("inventory list"))
					{
						textDriver.inventoryList().setVisible(true);
						textDriver.reorderListsPane();
					}
					else if (paramString.equals("inserted list"))
					{
						textDriver.insertedList().setVisible(true);
						textDriver.reorderListsPane();
					}
					else if (paramString.equals("avatar"))
					{
						avatar.show();
					}
					else if (startsWith(paramString, "location ") ||
						startsWith(paramString, "item ") ||
						startsWith(paramString, "entity "))
					{
						tempLocation = findLocation(paramString);
						if (null != tempLocation)
						{
							tempLocation.show();
						}
						else
						{
							tempItem = findItem(paramString);
							if (null != tempItem)
							{
								tempItem.show();
							}
							else throwErrorMessage(
								"errorUnknownEntity", scriptName, i + 1,
								paramString);
						}
					}
					else
						throwErrorMessage("errorUnknownEntity",
							scriptName, i + 1, paramString);
				}
				else if (startsWith(line, "close "))
				{
					if (tokenize(tokenizedCommand, paramString))
					{
						if (tokenizedCommand.size() < 2)
							throwErrorMessage(
								"errorInvalidNumberOfTokens",
								scriptName, i + 1);

						tempLocation = findLocation(
							tokenizedCommand.elementAt(0));

						if (null == tempLocation)
							throwErrorMessage(
								"errorLocationNotFound", scriptName, i + 1,
								tokenizedCommand.elementAt(0));

						tempDestination = findLocation(
							tokenizedCommand.elementAt(1));

						if (null == tempLocation)
							throwErrorMessage(
								"errorLocationNotFound", scriptName, i + 1,
								tokenizedCommand.elementAt(1));

						tempTransition = tempLocation.
							getTransition(tempDestination);

						if (null == tempTransition)
							throwErrorMessage(
								"errorTransitionNotFound",
								scriptName, i + 1, tempLocation,
								tempDestination);

						tempTransition.hide();
					}
				}
				else if (startsWith(line, "hide ") ||
					startsWith(line, "conceal "))
				{
					if (paramString.equals("locations list"))
					{
						textDriver.locationsList().setVisible(false);
						textDriver.reorderListsPane();
					}
					else if (paramString.equals("items list"))
					{
						textDriver.itemsList().setVisible(false);
						textDriver.reorderListsPane();
					}
					else if (paramString.equals("inventory list"))
					{
						textDriver.inventoryList().setVisible(false);
						textDriver.reorderListsPane();
					}
					else if (paramString.equals("inserted list"))
					{
						textDriver.insertedList().setVisible(false);
						textDriver.reorderListsPane();
					}
					else if (paramString.equals("avatar"))
					{
						avatar.hide();
					}
					else if (startsWith(paramString, "location ") ||
						startsWith(paramString, "item ") ||
						startsWith(paramString, "entity "))
					{
						tempLocation = findLocation(paramString);
						if (null != tempLocation)
						{
							tempLocation.hide();
						}
						else
						{
							tempItem = findItem(paramString);
							if (null != tempItem)
							{
								tempItem.hide();
							}
							else throwErrorMessage(
								"errorUnknownEntity", scriptName, i + 1,
								paramString);
						}
					}
					else
						throwErrorMessage("errorUnknownEntity",
							scriptName, i + 1, paramString);
				}
				else if (startsWith(line, "if "))
				{
					boolean condition = false;

					if (paramString.equals("no parameters"))
						condition = 0 == parameters.size();
					else if (startsWith(paramString, "here "))
					{
						if (null == (tempItem = findItem(paramString)))
							throwErrorMessage("errorItemNotFound",
								scriptName, i + 1, paramString);
						condition = tempItem.getPlacement() ==
							avatar.getPlacement();
					}
					else if (startsWith(paramString, "not here "))
					{
						if (null == (tempItem = findItem(paramString)))
							throwErrorMessage("errorItemNotFound",
								scriptName, i + 1, paramString);
						condition = tempItem.getPlacement() !=
							avatar.getPlacement();
					}
					else if (startsWith(paramString, "have "))
					{
						if (null == (tempItem = findItem(paramString)))
							throwErrorMessage("errorItemNotFound",
								scriptName, i + 1, paramString);
						condition = tempItem.getPlacement() == avatar;
					}
					else if (startsWith(paramString, "not have "))
					{
						if (null == (tempItem = findItem(paramString)))
							throwErrorMessage("errorItemNotFound",
								scriptName, i + 1, paramString);
						condition = tempItem.getPlacement() != avatar;
					}
					else if (paramString.equals("have"))
					{
						if (currentHolder instanceof Item)
							condition = ((Item)currentHolder).
								getPlacement() == avatar;
						else
							throwErrorMessage(
								"failureMissingItemForCommand",
								scriptName, i + 1, line);
					}
					else if (paramString.equals("not have"))
					{
						if (currentHolder instanceof Item)
							condition = ((Item)currentHolder).
								getPlacement() != avatar;
						else
							throwErrorMessage(
								"failureMissingItemForCommand",
								scriptName, i + 1, line);
					}
					else if (startsWith(paramString, "avatar in "))
					{
						if (null == (tempLocation = findLocation(paramString)))
						{
							if (null == (tempItem = findItem(paramString)))
								throwErrorMessage("errorItemNotFound",
									scriptName, i + 1, paramString);
							condition = tempItem == avatar.getPlacement();
						}
						else condition = tempLocation == avatar.getPlacement();
					}
					else if (startsWith(paramString, "avatar not in "))
					{
						if (null == (tempLocation = findLocation(paramString)))
						{
							if (null == (tempItem = findItem(paramString)))
								throwErrorMessage("errorItemNotFound",
									scriptName, i + 1, paramString);
							condition = tempItem != avatar.getPlacement();
						}
						else condition = tempLocation == avatar.getPlacement();
					}
					else if (paramString.equals("here") ||
						paramString.equals("avatar in"))
					{
						condition = currentHolder == avatar.getPlacement();
					}
					else if (paramString.equals("not here") ||
						paramString.equals("avatar not in"))
					{
						condition = currentHolder != avatar.getPlacement();
					}
					else if (startsWith(paramString, "visited "))
					{
						if (null == (tempLocation = findLocation(paramString)))
							throwErrorMessage("errorLocationNotFound",
								scriptName, i + 1, paramString);
						condition = tempLocation.isVisited();
					}
					else if (paramString.equals("visited"))
					{
						if (currentHolder instanceof Location)
							condition = ((Location)currentHolder).isVisited();
						else
							throwErrorMessage(
								"failureMissingLocationForCommand",
								scriptName, i + 1, line);
					}
					else if (startsWith(paramString, "not visited "))
					{
						if (null == (tempLocation = findLocation(paramString)))
							throwErrorMessage("errorLocationNotFound",
								scriptName, i + 1, paramString);
						condition = !tempLocation.isVisited();
					}
					else if (paramString.equals("not visited"))
					{
						if (currentHolder instanceof Location)
							condition = !((Location)currentHolder).isVisited();
						else
							throwErrorMessage(
								"failureMissingLocationForCommand",
								scriptName, i + 1, line);
					}
					else if (startsWith(paramString, "visible "))
					{
						if (null == (tempLocation = findLocation(paramString)))
						{
							if (null == (tempItem = findItem(paramString)))
								throwErrorMessage("errorItemNotFound",
									scriptName, i + 1, paramString);
							condition = tempItem.isVisible();
						}
						else condition = tempLocation.isVisible();
					}
					else if (paramString.equals("visible"))
					{
						if (currentHolder instanceof Container)
							condition = ((Container)currentHolder).isVisible();
						else
							throwErrorMessage(
								"failureMissingContainerForCommand",
								scriptName, i + 1, line);
					}
					else if (startsWith(paramString, "hidden "))
					{
						if (null == (tempLocation = findLocation(paramString)))
						{
							if (null == (tempItem = findItem(paramString)))
								throwErrorMessage("errorItemNotFound",
									scriptName, i + 1, paramString);
							condition = !tempItem.isVisible();
						}
						else condition = !tempLocation.isVisible();
					}
					else if (paramString.equals("hidden"))
					{
						if (currentHolder instanceof Container) condition =
							!((Container)currentHolder).isVisible();
						else
							throwErrorMessage(
								"failureMissingContainerForCommand",
								scriptName, i + 1, line);
					}
					else
						condition = evaluateBoolean(paramString,
							currentHolder, parameters);

					// out.println("If: " + paramString);
					// out.println("  result: " + condition);

					int startIf = i + 1, endIf = findEnd(i);
					if (condition)
					{
						if (!run(startIf, endIf)) return false;
						if (exit) return true;
					}

					i = endIf;
					// out.println("  continue at: " + i);
				}
				else if (startsWith(line, "for "))
				{
					// throw new RuntimeException("“For” command is not implemented yet!");

					int startFor = i + 1, endFor = findEnd(i);
					int thisForLevel = forLevel++;
					forVars.insertElementAt("", thisForLevel);

					if (paramString.equals("parameters"))
					{
						for (String parameter : parameters)
						{
							forVars.setElementAt(parameter, thisForLevel);
							if (!run(startFor, endFor)) return false;
							if (exit) return true;
						}
					}
					else
					{
						int visibility, forVariant = 0;

						if (startsWith(paramString, "visible ") ||
							startsWith(paramString, "revealed "))
							visibility = 1;
						else if (startsWith(paramString, "hidden ") ||
							startsWith(paramString, "concealed "))
							visibility = 0;
						else
							visibility = -1;

						if (startsWith(paramString, "present "))
						{
							/* IRRELEVANT
							if (startsWith(paramString, "items "))
							{ forVariant = xxx; } else */

							if (paramString.equals("items"))
							{
								forVariant = 1;
								paramString = "";
							}
							else
							{
								throwErrorMessage(
									"errorUnknownForVariant",
									scriptName, i + 1, line);
							}
						}
						else if (startsWith(paramString, "inventory "))
						{
							/* IRRELEVANT
							if (startsWith(paramString, "items "))
							{ forVariant = xxx; } else */

							if (paramString.equals("items"))
							{
								forVariant = 2;
								paramString = "";
							}
							else
							{
								throwErrorMessage(
									"errorUnknownForVariant",
									scriptName, i + 1, line);
							}
						}

						if (startsWith(paramString, "items "))
						{
							if (0 == forVariant)
							{
								if (startsWith(paramString, "placed "))
								{
									forVariant = 3;
									tempLocation = findLocation(paramString);
									if (null == tempLocation)
										throwErrorMessage(
											"errorLocationNotFound",
											scriptName, i + 1, paramString);
								}
								else if (startsWith(paramString, "inside "))
								{
									forVariant = 4;
									tempItem = findItem(paramString);
									if (null == tempItem)
										throwErrorMessage(
											"errorItemNotFound",
											scriptName, i + 1, paramString);
								}
								else
								{
									throwErrorMessage(
										"errorUnknownForVariant",
										scriptName, i + 1, line);
								}
							}
							else
							{
								throwErrorMessage(
									"errorUnknownForVariant",
									scriptName, i + 1, line);
							}
						}
						else if (paramString.equals("items"))
						{
							if (0 == forVariant)
							{
								forVariant = 5;
								paramString = "";
							}
							else
							{
								throwErrorMessage(
									"errorUnknownForVariant",
									scriptName, i + 1, line);
							}
						}

						if (startsWith(paramString, "locations "))
						{
							if (0 == forVariant)
							{
								if (startsWith(paramString, "connected "))
								{
									forVariant = -1;
									tempLocation = findLocation(paramString);
									if (null == tempLocation)
										throwErrorMessage(
											"errorLocationNotFound",
											scriptName, i + 1, paramString);
								}
								else if (startsWith(paramString, "isolated "))
								{
									forVariant = -2;
									tempLocation = findLocation(paramString);
									if (null == tempLocation)
										throwErrorMessage(
											"errorLocationNotFound",
											scriptName, i + 1, paramString);
								}
								else
								{
									throwErrorMessage(
										"errorUnknownForVariant",
										scriptName, i + 1, line);
								}
							}
							else
							{
								throwErrorMessage(
									"errorUnknownForVariant",
									scriptName, i + 1, line);
							}
						}
						else if (paramString.equals("locations"))
						{
							if (0 == forVariant)
							{
								forVariant = -3;
								paramString = "";
							}
							else
							{
								throwErrorMessage(
									"errorUnknownForVariant",
									scriptName, i + 1, line);
							}
						}

						if (startsWith(paramString, "destinations "))
						{
							if (0 == forVariant)
							{
								if (startsWith(paramString, "connected "))
								{
									forVariant = -4;
									tempLocation = findLocation(paramString);
									if (null == tempLocation)
										throwErrorMessage(
											"errorLocationNotFound",
											scriptName, i + 1, paramString);
								}
								else if (startsWith(paramString, "isolated "))
								{
									forVariant = -5;
									tempLocation = findLocation(paramString);
									if (null == tempLocation)
										throwErrorMessage(
											"errorLocationNotFound",
											scriptName, i + 1, paramString);
								}
								else
								{
									throwErrorMessage(
										"errorUnknownForVariant",
										scriptName, i + 1, line);
								}
							}
							else
							{
								throwErrorMessage(
									"errorUnknownForVariant",
									scriptName, i + 1, line);
							}
						}
						else if (paramString.equals("destinations"))
						{
							if (0 == forVariant)
							{
								forVariant = -6;
								paramString = "";
							}
							else
							{
								throwErrorMessage(
									"errorUnknownForVariant",
									scriptName, i + 1, line);
							}
						}

						final Vector<String> forList = new Vector<String>();
						Vector<Transition> transitions;

						switch (forVariant)
						{
							case -6: // destinations (all)
								if (null != (tempContainer =
									avatar.getPlacement()) &&
									tempContainer instanceof Location)
								{
									transitions = ((Location)tempContainer).
										getTransitions();
									for (Transition transition : transitions)
									{
										if (matchVisibilityCondition(
											transition.target, visibility))
											forList.add(transition.target.name);
									}
								}
								break;

							case -5: // destinations isolated
								if (null != (tempContainer =
									avatar.getPlacement()) &&
									tempContainer instanceof Location)
								{
									transitions = ((Location)tempContainer).
										getTransitions();
									for (Transition transition : transitions)
									{
										if (matchVisibilityCondition(
											transition.target, visibility) &&
											!transition.target.
												isConnectedWith(tempLocation))
											forList.add(transition.target.name);
									}
								}
								break;

							case -4: // destinations connected
								if (null != (tempContainer =
									avatar.getPlacement()) &&
									tempContainer instanceof Location)
								{
									transitions = ((Location)tempContainer).
										getTransitions();
									for (Transition transition : transitions)
									{
										if (matchVisibilityCondition(
											transition.target, visibility) &&
											transition.target.
												isConnectedWith(tempLocation))
											forList.add(transition.target.name);
									}
								}
								break;

							case -3: // locations (all)
								for (Location location : locations)
									if (matchVisibilityCondition(
										location, visibility))
										forList.add(location.name);
								break;

							case -2: // locations isolated
								for (Location location : locations)
									if (matchVisibilityCondition(
										location, visibility) &&
										!location.isConnectedWith(tempLocation))
										forList.add(location.name);
								break;

							case -1: // locations connected
								for (Location location : locations)
									if (matchVisibilityCondition(
										location, visibility) &&
										location.isConnectedWith(tempLocation))
										forList.add(location.name);
								break;

							case 1: // present items
								for (Item item : items)
									if (matchVisibilityCondition(
										item, visibility) &&
										item.getPlacement() ==
										avatar.getPlacement())
										forList.add(item.name);
								break;

							case 2: // inventory items
								for (Item item : items)
									if (matchVisibilityCondition(
										item, visibility) &&
										item.getPlacement() == avatar)
										forList.add(item.name);
								break;

							case 3: // items placed «target location»
								for (Item item : items)
									if (matchVisibilityCondition(
										item, visibility) &&
										item.getPlacement() == tempLocation)
										forList.add(item.name);
								break;

							case 4: // items inside «target item»
								for (Item item : items)
									if (matchVisibilityCondition(
										item, visibility) &&
										item.getPlacement() == tempItem)
										forList.add(item.name);
								break;

							case 5: // items (all)
								for (Item item : items)
									if (matchVisibilityCondition(
										item, visibility))
										forList.add(item.name);
								break;

							default:
								throwErrorMessage(
									"errorUnknownForVariant",
									scriptName, i + 1, line);
						}

						for (String parameter : forList)
						{
							forVars.setElementAt(parameter, thisForLevel);
							if (!run(startFor, endFor)) return false;
							if (exit) return true;
						}
					}

					forVars.removeElementAt(thisForLevel);
					--forLevel;
					i = endFor;
				}
				else if (line.equals(endIfKeyword) ||
					line.equals(endForKeyword))
				{
					// Do nothing (but send info to err stream)
					err.println("  Error: odd “" + line + "” at line " +
						(i + 1) + " in " + scriptName + ".");
				}
				else if (startsWith(line, "redefine "))
				{
					throwErrorMessage(
						"errorRedefinitionDenied", scriptName, i + 1);
				}
				else
				{
					throwErrorMessage(
						"errorUnknownCommand", scriptName, i + 1, line);
					// ✗ Ignore all unknown commands? – Will decide later.
					// err.println("  Error: unknown “" + line + "” at line " +
					// 	(i + 1) + " in " + scriptName + ".");
				}
			}

			return true;
		}

		public boolean run()
		{
			try
			{
				return run(startLine, script.size());
			}
			finally
			{
				// restore definition data
				definitionFilename = filenameBak;
				definitionLineNumber = lineNumberBak;
			}
		}
	}


	public boolean runScript(String scriptName, Vector<String> script,
		Holder currentHolder, Vector<String> parameters)
	{
		ScriptInstance instance = new ScriptInstance(scriptName,
			script, currentHolder, parameters);
		return instance.run();
	}

	private String[] args = null;

	public boolean runStartup(String[] args)
	{
		this.args = args;
		return runStartup();
	}

	public boolean runStartup()
	{
		final Vector<String> parameters = new Vector<String>();

		if (null != args)
			for (String arg : args)
			{
				// out.println(arg);
				if (startsWith(arg, "-param:"))
					parameters.add(paramString);
			}

		if (!runScript(startup.name, startup.onInvoke, startup, parameters))
			return false;

		if (autoexplore &&
			avatar.getPlacement() != null &&
			avatar.getPlacement().isVisible() &&
			avatar.getPlacement() instanceof Location)
		{
			Location placement = (Location)avatar.getPlacement();

			if (0 != placement.onExplore.size())
			{
				if (!runScript("On explore " + placement.name,
					placement.onExplore, placement, null)) return false;
			}

			placement.setVisited();
		}

		return true;
	}

	public boolean runReadState()
	{
		final Vector<String> parameters = new Vector<String>();

		if (null != args)
			for (String arg : args)
			{
				// out.println(arg);
				if (startsWith(arg, "-param:"))
					parameters.add(paramString);
			}

		if (!runScript(readState.name, readState.onInvoke,
			readState, parameters)) return false;

		if (autoexplore &&
			avatar.getPlacement() != null &&
			avatar.getPlacement().isVisible() &&
			avatar.getPlacement() instanceof Location)
		{
			Location placement = (Location)avatar.getPlacement();

			if (0 != placement.onExplore.size())
			{
				if (!runScript("On explore " + placement.name,
					placement.onExplore, placement, null)) return false;
			}

			placement.setVisited();
		}

		return true;
	}

	public void runPopulate()
	{
		runScript(populate.name, populate.onInvoke, populate, null);
	}

	private boolean queryFlag = false;
	private String queryInputLineLabelBackup = null;

	public boolean queryMode()
	{
		return queryFlag;
	}

	public boolean executeQuery(String queryLine)
	{
		if (queryFlag)
		{
			queryFlag = false;
			textDriver.setInputLineLabel(queryInputLineLabelBackup);

			final Vector<String> parameters = parseArguments(queryLine);
			return runScript(query.name, query.onInvoke, query, parameters);
		}
		return false;
	}

	public boolean runAvatarCommonFailure()
	{
		try
		{
			if (0 != avatar.onCommonFailure.size())
				runScript("On common failure " + avatar.name,
					avatar.onCommonFailure, avatar, null);
		}
		catch (Exception e)
		{
			if (0 != e.getMessage().length())
			{
				textDriver.setStyle(textDriver.getErrorStyle());
				textDriver.writeLine(e.getMessage());
				return true;
			}
		}

		return false;
	}

	public Vector<String> parameters(Object... params)
	{
		Vector<String> vector = new Vector<String>();
		for (Object obj : params)
		{
			if (null == obj) vector.add("");
			else vector.add(obj.toString());
		}
		return vector;
	}

	private Vector<String> parseArguments(String arguments)
	{
		final Vector<String> parameters = new Vector<String>();

		// try {
		for (int i = 0; 0 != arguments.length() && i < 1000; ++i)
		{
			if (' ' == arguments.charAt(0))
			{
				// out.println("Space leak…");
				if (1 == arguments.length()) break;
				arguments = arguments.substring(1);
			}

			if ('(' == arguments.charAt(0))
			{
				int count = 0, index;

				for (index = 0; index < arguments.length(); ++index)
				{
					if ('(' == arguments.charAt(index)) ++count;
					else if (')' == arguments.charAt(index)) --count;
					if (0 == count) break;
				}

				parameters.add(arguments.substring(1, index));
				// out.println("Add 1: " + parameters.lastElement());
				arguments = arguments.substring(index + 1);
			}
			else
			{
				boolean notFound = true;

				for (Entity token : tokens)
				{
					if (arguments.length() >= token.name.length() &&
						arguments.startsWith(token.name))
					{
						// out.println("Found token: " + token);
						parameters.add(arguments.substring(0,
							token.name.length() - 1));
						// out.println("Add 2: " + parameters.lastElement());
						arguments = arguments.substring(token.name.length());
						notFound = false;
						break;
					}
				}

				if (notFound)
				{
					/* “unsorted way”
					for (Enumeration<String> keys = aliases.keys();
						keys.hasMoreElements();)
					{
						String key = keys.nextElement();

						if (arguments.length() >= key.length() &&
							arguments.startsWith(key))
						{
							parameters.add(arguments.substring(0,
								key.length() - 1));
							arguments = arguments.substring(key.length());
							notFound = false;
							break;
						}
					}
					*/
					Set<String> keys = aliases.keySet();
					for (String key : keys)
					{
						if (arguments.length() >= key.length() &&
							arguments.startsWith(key))
						{
							parameters.add(arguments.substring(0,
								key.length() - 1));
							arguments = arguments.substring(key.length());
							notFound = false;
							break;
						}
					}
				}

				if (notFound)
				{
					int indexOf = arguments.indexOf(" ");
					if (-1 == indexOf)
					{
						if (0 != arguments.length())
						{
							parameters.add(arguments);
							// out.println("Add 3: " + parameters.lastElement());
							arguments = "";
						}
					}
					else
					{
						parameters.add(arguments.substring(0, indexOf));
						// out.println("Add 4: " + parameters.lastElement());
						arguments = arguments.substring(indexOf + 1);
					}
				}
			}

			// out.println("Parse arguments (progress): " + arguments);
		}
		// } catch (Exception e) { e.printStackTrace(); }

		return parameters;
	}

	/**
	 *  @param  command     trigger name with arguments or command line
	 *  @param  publicOnly  if is true only the public triggers will be
	 *                      searched in the trigger list (and invoked)
	 */
	public boolean invokeTrigger(String command, boolean publicOnly)
	{
		Trigger trigger = null;
		String trimmed = command.replaceAll(
			"[\\s ]+", " ").trim().toLowerCase() + " ";

		for (Trigger findTrigger : triggers)
		{
			if (trimmed.startsWith(findTrigger.name.toLowerCase()))
			{
				if (trimmed.length() > findTrigger.name.length() &&
					' ' != trimmed.charAt(findTrigger.name.length()))
					continue;

				trimmed = trimmed.substring(findTrigger.name.length());
				trigger = findTrigger;
				break;
			}
		}

		if (null == trigger) return false;
		if (publicOnly && !trigger.isPublic()) return false;

		final Vector<String> parameters = parseArguments(trimmed);

		return runScript(trigger.name, trigger.onInvoke, trigger, parameters);
	}

	/** @param command command line */
	public boolean executeCommand(String command)
	{
		if (-1 != command.indexOf(commandLineSeparator))
		{
			final Vector<String> commands = new Vector<String>();
			splitCommandLine(commands, command);
			textDriver.saveMark();

			for (String cmd : commands)
			{
				try
				{
					if (!executeCommand(cmd))
					{
						textDriver.clearFromMark();
						return false;
					}
				}
				catch (RuntimeException e)
				{
					textDriver.clearFromMark();
					throw e;
				}

				if (null != successMessage)
				{
					textDriver.setStyle(textDriver.getSuccessStyle());
					textDriver.writeLine(successMessage);
				}

				textDriver.resetMessages();
			}

			successMessage = null;
			textDriver.clearMark();
			return true;
		}

		if (debug)
		{
			if (command.equalsIgnoreCase("locations"))
			{
				for (Location location : locations)
					textDriver.writeLine("> ", location);
				successMessage = "Number of locations: " + locations.size();
				return true;
			}
			else if (command.equalsIgnoreCase("items"))
			{
				for (Item item : items)
					textDriver.writeLine("> ", item);
				successMessage = "Number of items: " + items.size();
				return true;
			}
			else if (command.equalsIgnoreCase("triggers"))
			{
				for (Trigger trigger : triggers)
					textDriver.writeLine("> ", trigger);
				successMessage = "Number of triggers: " + triggers.size();
				return true;
			}
			else if (command.equalsIgnoreCase("tokens"))
			{
				for (Entity token : tokens)
					textDriver.writeLine("> ", token, "(",
						token.name.length() - 1, ")");
				successMessage = "Number of tokens: " + tokens.size();
				return true;
			}
			else if (command.equalsIgnoreCase("aliases"))
			{
				successMessage = "Number of aliases: " + dumpAliases();
				return true;
			}
			else if (command.equalsIgnoreCase("styles"))
			{
				textDriver.write("Predefined styles: ");
				int count = textDriver.dumpStyles();
				successMessage = "Number of styles: " + count;
				return true;
			}
			else if (command.equalsIgnoreCase("dump"))
			{
				textDriver.write("You can dump: locations, ");
				textDriver.write("items, triggers, tokens, ");
				textDriver.writeLine("aliases, and styles.");
				// successMessage = "OK";
				return true;
			}
			else if (startsWith(command, "dump "))
			{
				Location location = findLocation(paramString);
				if (null != location)
				{
					textDriver.writeLine(
						"Dump of location: ", location.name);

					if (0 != location.getTonnage())
						textDriver.writeLine(
							"  Tonnage: ", location.getTonnage());

					if (0 != location.getCapacity())
						textDriver.writeLine(
							"  Capacity: ", location.getCapacity());

					if (!location.isVisible())
						textDriver.writeLine("  Concealed");

					if (0 != location.getTransitions().size())
					{
						textDriver.write("  Transitions: ");
						boolean first = true;
						for (Transition transition : location.getTransitions())
						{
							if (first) first = false;
							else textDriver.write(", ");

							textDriver.write(transition.target);

							if (0 != transition.getTonnage() ||
								0 != transition.getCapacity())
							{
								textDriver.write(
									" (tonnage: ", 0 == transition.
									getTonnage() ? "–" : transition.
									getTonnage(), ", capacity: ",
									0 == transition.getCapacity() ? "–" :
									transition.getCapacity(), ")");
							}
						}
						textDriver.writeLine(".");
					}

					location.dumpEntries();
					location.dumpProperties();
					dumpAliases(location);

					if (0 != location.onReveal.size())
						textDriver.writeLine("  On reveal: ",
							location.onReveal.size(), " command(s)");
					if (0 != location.onConceal.size())
						textDriver.writeLine("  On conceal: ",
							location.onConceal.size(), " command(s)");
					if (0 != location.onEnter.size())
						textDriver.writeLine("  On enter: ",
							location.onEnter.size(), " command(s)");
					if (0 != location.onLeave.size())
						textDriver.writeLine("  On leave: ",
							location.onLeave.size()," command(s)");
					if (0 != location.onExplore.size())
						textDriver.writeLine("  On explore: ",
							location.onExplore.size(), " command(s)");
				}

				Item item = findItem(paramString);
				if (null != item)
				{
					textDriver.writeLine(
						"Dump of item: ", item.name);

					if (0 != item.getTonnage())
						textDriver.writeLine(
							"  Tonnage: ", item.getTonnage());

					if (0 != item.getCapacity())
						textDriver.writeLine(
							"  Capacity: ", item.getCapacity());

					textDriver.writeLine(
						"  Weight: ", item.getWeight());

					textDriver.writeLine(
						"  Volume: ", item.getVolume());

					if (null != item.getPlacement())
						textDriver.writeLine(
							"  Placement: ", item.getPlacement());

					if (!item.isVisible())
						textDriver.writeLine("  Hidden");

					item.dumpEntries();
					item.dumpProperties();
					dumpAliases(item);

					if (0 != item.onShow.size())
						textDriver.writeLine("  On show: ",
							item.onShow.size(), " command(s)");
					if (0 != item.onHide.size())
						textDriver.writeLine("  On hide: ",
							item.onHide.size(), " command(s)");
					if (0 != item.onPick.size())
						textDriver.writeLine("  On pick: ",
							item.onPick.size(), " command(s)");
					if (0 != item.onDrop.size())
						textDriver.writeLine("  On drop: ",
							item.onDrop.size()," command(s)");
					if (0 != item.onThrow.size())
						textDriver.writeLine("  On throw: ",
							item.onThrow.size()," command(s)");

					if (0 != item.onInsert.size())
						textDriver.writeLine("  On insert: ",
							item.onInsert.size()," command(s)");
					if (0 != item.onAdmit.size())
						textDriver.writeLine("  On admit: ",
							item.onAdmit.size()," command(s)");
					if (0 != item.onRemove.size())
						textDriver.writeLine("  On remove: ",
							item.onRemove.size()," command(s)");
					if (0 != item.onRelease.size())
						textDriver.writeLine("  On release: ",
							item.onRelease.size()," command(s)");
					if (0 != item.onExamine.size())
						textDriver.writeLine("  On examine: ",
							item.onExamine.size()," command(s)");
					if (0 != item.onUse.size())
						textDriver.writeLine("  On use: ",
							item.onUse.size()," command(s)");
					if (0 != item.onAction.size())
						textDriver.writeLine("  On action: ",
							item.onAction.size()," command(s)");

					if (0 != item.onDrive.size())
						textDriver.writeLine("  On drive: ",
							item.onDrive.size(), " command(s)");
				}

				if (avatar.equals(paramString))
				{
					textDriver.writeLine(
						"Dump of ", avatar.name);

					if (0 != avatar.getTonnage())
						textDriver.writeLine(
							"  Tonnage: ", avatar.getTonnage());

					if (0 != avatar.getCapacity())
						textDriver.writeLine(
							"  Capacity: ", avatar.getCapacity());

					textDriver.writeLine(
						"  Weight: ", avatar.getWeight());

					textDriver.writeLine(
						"  Volume: ", avatar.getVolume());

					if (null != avatar.getPlacement())
						textDriver.writeLine(
							"  Placement: ", avatar.getPlacement());

					if (!avatar.isVisible())
						textDriver.writeLine("  Hidden");

					avatar.dumpEntries();
					avatar.dumpProperties();
					dumpAliases(avatar);
				}

				Trigger trigger = findTrigger(paramString);
				if (null != trigger)
				{
					textDriver.writeLine(
						"Dump of trigger: ", trigger.name);

					if (trigger.isPublic())
						textDriver.writeLine("  Is public");

					trigger.dumpProperties();
					dumpAliases(trigger);

					if (0 != trigger.onInvoke.size())
						textDriver.writeLine("  On invoke: ",
							trigger.onInvoke.size(), " command(s)");
				}

				SimpleAttributeSet style = textDriver.getStyle(paramString);

				if (null != style)
				{
					textDriver.writeLine("Dump of style: ", paramString);

					for (Enumeration<?> keys =
						style.getAttributeNames();
						keys.hasMoreElements();)
					{
						Object key = keys.nextElement();
						Object attr = style.getAttribute(key);
						textDriver.writeLine("  " + key + ": " + attr);
					}
				}

				return location != null || item != null ||
					avatar.equals(paramString) || trigger != null ||
					style != null;
			}
			else if (startsWith(command, "tokenize "))
			{
				if (tokenize(tokenizedCommand, paramString))
				{
					for (Entity token : tokenizedCommand)
						textDriver.writeLine("> ", token);

					successMessage = "Tokens OK.";
					return true;
				}
			}
		}

		// out.println(command);

		if (invokeTrigger(command, true)) return true;

		if (tokenize(tokenizedCommand, command))
		{
			if (tokenizedCommand.size() > 0)
				commandType = tokenizedCommand.elementAt(0).toString().trim();

			if (tokenizedCommand.size() == 4)
			{
				if (walkCommand == tokenizedCommand.elementAt(0))
				{
					if (walkOperator != tokenizedCommand.elementAt(2))
					{
						failureMessage = textDriver.parseMessage(
							"failureInvalidOperator", tokenizedCommand.
							elementAt(2));
						return false;
					}

					if (!walk(tokenizedCommand.elementAt(1),
						tokenizedCommand.elementAt(3)))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onWalkFailure.size())
							runScript("On walk failure " + avatar.name,
								avatar.onWalkFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onWalkSuccess.size())
						runScript("On walk success " + avatar.name,
							avatar.onWalkSuccess, avatar, null);

					return true;
				}
				else if (returnCommand == tokenizedCommand.elementAt(0))
				{
					if (returnOperator != tokenizedCommand.elementAt(2))
					{
						failureMessage = textDriver.parseMessage(
							"failureInvalidOperator", tokenizedCommand.
							elementAt(2));
						return false;
					}

					if (!return_(tokenizedCommand.elementAt(1),
						tokenizedCommand.elementAt(3)))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onReturnFailure.size())
							runScript("On return failure " + avatar.name,
								avatar.onReturnFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onReturnSuccess.size())
						runScript("On return success " + avatar.name,
							avatar.onReturnSuccess, avatar, null);

					return true;
				}
				else if (exploreCommand == tokenizedCommand.elementAt(0))
				{
					if (exploreOperator != tokenizedCommand.elementAt(2))
					{
						failureMessage = textDriver.parseMessage(
							"failureInvalidOperator", tokenizedCommand.
							elementAt(2));
						return false;
					}

					if (!explore(tokenizedCommand.elementAt(1),
						tokenizedCommand.elementAt(3)))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onExploreFailure.size())
							runScript("On explore failure " + avatar.name,
								avatar.onExploreFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onExploreSuccess.size())
						runScript("On explore success " + avatar.name,
							avatar.onExploreSuccess, avatar, null);

					return true;
				}
				else if (pickCommand == tokenizedCommand.elementAt(0))
				{
					if (pickOperator != tokenizedCommand.elementAt(2))
					{
						failureMessage = textDriver.parseMessage(
							"failureInvalidOperator", tokenizedCommand.
							elementAt(2));
						return false;
					}

					if (!pick(tokenizedCommand.elementAt(1),
						tokenizedCommand.elementAt(3)))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onPickFailure.size())
							runScript("On pick failure " + avatar.name,
								avatar.onPickFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onPickSuccess.size())
						runScript("On pick success " + avatar.name,
							avatar.onPickSuccess, avatar, null);

					return true;
				}
				else if (dropCommand == tokenizedCommand.elementAt(0))
				{
					if (dropOperator != tokenizedCommand.elementAt(2))
					{
						failureMessage = textDriver.parseMessage(
							"failureInvalidOperator", tokenizedCommand.
							elementAt(2));
						return false;
					}

					if (!drop(tokenizedCommand.elementAt(1),
						tokenizedCommand.elementAt(3)))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onDropFailure.size())
							runScript("On drop failure " + avatar.name,
								avatar.onDropFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onDropSuccess.size())
						runScript("On drop success " + avatar.name,
							avatar.onDropSuccess, avatar, null);

					return true;
				}
				else if (throwCommand == tokenizedCommand.elementAt(0))
				{
					if (throwOperator != tokenizedCommand.elementAt(2))
					{
						failureMessage = textDriver.parseMessage(
							"failureInvalidOperator", tokenizedCommand.
							elementAt(2));
						return false;
					}

					if (!throw_(tokenizedCommand.elementAt(1),
						tokenizedCommand.elementAt(3)))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onThrowFailure.size())
							runScript("On throw failure " + avatar.name,
								avatar.onThrowFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onThrowSuccess.size())
						runScript("On throw success " + avatar.name,
							avatar.onThrowSuccess, avatar, null);

					return true;
				}
				else if (insertCommand == tokenizedCommand.elementAt(0))
				{
					if (insertOperator != tokenizedCommand.elementAt(2))
					{
						failureMessage = textDriver.parseMessage(
							"failureInvalidOperator", tokenizedCommand.
							elementAt(2));
						return false;
					}

					if (!insert(tokenizedCommand.elementAt(1),
						tokenizedCommand.elementAt(3)))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onInsertFailure.size())
							runScript("On insert failure " + avatar.name,
								avatar.onInsertFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onInsertSuccess.size())
						runScript("On insert success " + avatar.name,
							avatar.onInsertSuccess, avatar, null);

					return true;
				}
				else if (removeCommand == tokenizedCommand.elementAt(0))
				{
					if (removeOperator != tokenizedCommand.elementAt(2))
					{
						failureMessage = textDriver.parseMessage(
							"failureInvalidOperator", tokenizedCommand.
							elementAt(2));
						return false;
					}

					if (!remove(tokenizedCommand.elementAt(1),
						tokenizedCommand.elementAt(3)))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onRemoveFailure.size())
							runScript("On remove failure " + avatar.name,
								avatar.onRemoveFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onRemoveSuccess.size())
						runScript("On remove success " + avatar.name,
							avatar.onRemoveSuccess, avatar, null);

					return true;
				}
				else if (examineCommand == tokenizedCommand.elementAt(0))
				{
					if (examineOperator != tokenizedCommand.elementAt(2))
					{
						failureMessage = textDriver.parseMessage(
							"failureInvalidOperator", tokenizedCommand.
							elementAt(2));
						return false;
					}

					if (!examine(tokenizedCommand.elementAt(1),
						tokenizedCommand.elementAt(3)))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onExamineFailure.size())
							runScript("On examine failure " + avatar.name,
								avatar.onExamineFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onExamineSuccess.size())
						runScript("On examine success " + avatar.name,
							avatar.onExamineSuccess, avatar, null);

					return true;
				}
				else if (useCommand == tokenizedCommand.elementAt(0))
				{
					if (useOperator != tokenizedCommand.elementAt(2))
					{
						failureMessage = textDriver.parseMessage(
							"failureInvalidOperator", tokenizedCommand.
							elementAt(2));
						return false;
					}

					if (!use(tokenizedCommand.elementAt(1),
						tokenizedCommand.elementAt(3)))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onUseFailure.size())
							runScript("On use failure " + avatar.name,
								avatar.onUseFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onUseSuccess.size())
						runScript("On use success " + avatar.name,
							avatar.onUseSuccess, avatar, null);

					return true;
				}
				else if (actionCommand == tokenizedCommand.elementAt(0))
				{
					if (actionOperator != tokenizedCommand.elementAt(2))
					{
						failureMessage = textDriver.parseMessage(
							"failureInvalidOperator", tokenizedCommand.
							elementAt(2));
						return false;
					}

					if (!action(tokenizedCommand.elementAt(1),
						tokenizedCommand.elementAt(3)))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onActionFailure.size())
							runScript("On action failure " + avatar.name,
								avatar.onActionFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onActionSuccess.size())
						runScript("On action success " + avatar.name,
							avatar.onActionSuccess, avatar, null);

					return true;
				}
				else
				{
					failureMessage = textDriver.parseMessage(
						"failureUnknownCommand",
						tokenizedCommand.elementAt(0));
				}
			}
			else if (tokenizedCommand.size() == 2)
			{
				if (walkCommand == tokenizedCommand.elementAt(0))
				{
					if (!walk(tokenizedCommand.elementAt(1), null))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onWalkFailure.size())
							runScript("On walk failure " + avatar.name,
								avatar.onWalkFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onWalkSuccess.size())
						runScript("On walk success " + avatar.name,
							avatar.onWalkSuccess, avatar, null);

					return true;
				}
				else if (returnCommand == tokenizedCommand.elementAt(0))
				{
					if (!return_(tokenizedCommand.elementAt(1), null))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onReturnFailure.size())
							runScript("On return failure " + avatar.name,
								avatar.onReturnFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onReturnSuccess.size())
						runScript("On return success " + avatar.name,
							avatar.onReturnSuccess, avatar, null);

					return true;
				}
				else if (exploreCommand == tokenizedCommand.elementAt(0))
				{
					if (!explore(tokenizedCommand.elementAt(1), null))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onExploreFailure.size())
							runScript("On explore failure " + avatar.name,
								avatar.onExploreFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onExploreSuccess.size())
						runScript("On explore success " + avatar.name,
							avatar.onExploreSuccess, avatar, null);

					return true;
				}
				else if (pickCommand == tokenizedCommand.elementAt(0))
				{
					if (!pick(tokenizedCommand.elementAt(1), null))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onPickFailure.size())
							runScript("On pick failure " + avatar.name,
								avatar.onPickFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onPickSuccess.size())
						runScript("On pick success " + avatar.name,
							avatar.onPickSuccess, avatar, null);

					return true;
				}
				else if (dropCommand == tokenizedCommand.elementAt(0))
				{
					if (!drop(tokenizedCommand.elementAt(1), null))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onDropFailure.size())
							runScript("On drop failure " + avatar.name,
								avatar.onDropFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onDropSuccess.size())
						runScript("On drop success " + avatar.name,
							avatar.onDropSuccess, avatar, null);

					return true;
				}
				else if (throwCommand == tokenizedCommand.elementAt(0))
				{
					if (!throw_(tokenizedCommand.elementAt(1), null))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onThrowFailure.size())
							runScript("On throw failure " + avatar.name,
								avatar.onThrowFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onThrowSuccess.size())
						runScript("On throw success " + avatar.name,
							avatar.onThrowSuccess, avatar, null);

					return true;
				}
				else if (insertCommand == tokenizedCommand.elementAt(0))
				{
					if (!insert(tokenizedCommand.elementAt(1), null))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onInsertFailure.size())
							runScript("On insert failure " + avatar.name,
								avatar.onInsertFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onInsertSuccess.size())
						runScript("On insert success " + avatar.name,
							avatar.onInsertSuccess, avatar, null);

					return true;
				}
				else if (removeCommand == tokenizedCommand.elementAt(0))
				{
					if (!remove(tokenizedCommand.elementAt(1), null))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onRemoveFailure.size())
							runScript("On remove failure " + avatar.name,
								avatar.onRemoveFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onRemoveSuccess.size())
						runScript("On remove success " + avatar.name,
							avatar.onRemoveSuccess, avatar, null);

					return true;
				}
				else if (examineCommand == tokenizedCommand.elementAt(0))
				{
					if (!examine(tokenizedCommand.elementAt(1), null))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onExamineFailure.size())
							runScript("On examine failure " + avatar.name,
								avatar.onExamineFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onExamineSuccess.size())
						runScript("On examine success " + avatar.name,
							avatar.onExamineSuccess, avatar, null);

					return true;
				}
				else if (useCommand == tokenizedCommand.elementAt(0))
				{
					if (!use(tokenizedCommand.elementAt(1), null))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onUseFailure.size())
							runScript("On use failure " + avatar.name,
								avatar.onUseFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onUseSuccess.size())
						runScript("On use success " + avatar.name,
							avatar.onUseSuccess, avatar, null);

					return true;
				}
				else if (actionCommand == tokenizedCommand.elementAt(0))
				{
					if (!action(tokenizedCommand.elementAt(1), null))
					{
						if (0 != avatar.onCommonFailure.size())
							runScript("On common failure " + avatar.name,
								avatar.onCommonFailure, avatar, null);

						if (0 != avatar.onActionFailure.size())
							runScript("On action failure " + avatar.name,
								avatar.onActionFailure, avatar, null);

						return false;
					}

					if (0 != avatar.onCommonSuccess.size())
						runScript("On common success " + avatar.name,
							avatar.onCommonSuccess, avatar, null);

					if (0 != avatar.onActionSuccess.size())
						runScript("On action success " + avatar.name,
							avatar.onActionSuccess, avatar, null);

					return true;
				}
				else
				{
					failureMessage = textDriver.parseMessage(
						"failureUnknownCommand",
						tokenizedCommand.elementAt(0));
				}
			}
			else
			{
				failureMessage = textDriver.parseMessage(
					"failureInvalidNumberOfTokens", tokenizedCommand.size());
			}
		}
		else
		{
			failureMessage = textDriver.parseMessage(
				"failureCommandLineTooLong");
		}

		return false;
	}


	public void backupAll()
	{
		for (Location location : locations)
			location.backupProperties();
		for (Item item : items)
			item.backupProperties();

		for (Trigger trigger : triggers)
			trigger.backupProperties();

		startup.backupProperties();
		readState.backupProperties();
		query.backupProperties();
		populate.backupProperties();

		avatar.backupProperties();
	}

	public void restoreAll()
	{
		for (Location location : locations)
			location.restoreProperties();
		for (Item item : items)
			item.restoreProperties();

		for (Trigger trigger : triggers)
			trigger.restoreProperties();

		startup.restoreProperties();
		readState.restoreProperties();
		query.restoreProperties();
		populate.restoreProperties();

		avatar.restoreProperties();
	}


	public void saveDefaultAll()
	{
		for (Location location : locations)
			location.saveDefaultProperties();
		for (Item item : items)
			item.saveDefaultProperties();

		for (Trigger trigger : triggers)
			trigger.saveDefaultProperties();

		startup.saveDefaultProperties();
		readState.saveDefaultProperties();
		query.saveDefaultProperties();
		populate.saveDefaultProperties();

		avatar.saveDefaultProperties();
	}

	public void resetAll()
	{
		for (Location location : locations)
			location.resetProperties();
		for (Item item : items)
			item.resetProperties();

		for (Trigger trigger : triggers)
			trigger.resetProperties();

		startup.resetProperties();
		readState.resetProperties();
		query.resetProperties();
		populate.resetProperties();

		avatar.resetProperties();
	}


	public void writeAll(TextFile file) throws IOException
	{
		for (Location location : locations)
		{
			file.writeLine();
			file.write("Location: ");
			location.writeProperties(file);
		}

		for (Item item : items)
		{
			file.writeLine();
			file.write("Item: ");
			item.writeProperties(file);
		}

		for (Trigger trigger : triggers)
		{
			file.writeLine();
			file.write("Trigger: ");
			trigger.writeProperties(file);
		}

		file.writeLine();
		startup.writeProperties(file);

		file.writeLine();
		readState.writeProperties(file);

		file.writeLine();
		query.writeProperties(file);

		file.writeLine();
		populate.writeProperties(file);

		file.writeLine();
		file.write("Avatar: ");
		avatar.writeProperties(file);
	}

	public void readAll(TextFile file) throws IOException
	{
		Location tempLocation = null, tempTransitionSource = null;
		Transition tempTransition = null; Item tempItem = null;
		Trigger tempTrigger = null;

		String line, where = "save file"; int lineNumber = 0;

		while (null != (line = file.readLine()))
		{
			line = line.trim(); ++lineNumber;
			if (0 == line.length() || ';' == line.charAt(0)) continue;

			if (startsWith(line, "Location: "))
			{
				tempLocation = findLocation(paramString);
				tempTransitionSource = null;
				tempTransition = null;
				tempItem = null;
				tempTrigger = null;

				if (null == tempLocation)
					throwErrorMessage("errorLocationNotFound",
						where, lineNumber, paramString);
			}
			else if (startsWith(line, "Transition: "))
			{
				tempLocation = null;
				tempTransitionSource = findLocation(paramString);
				tempTransition = null;
				tempItem = null;
				tempTrigger = null;

				if (null == tempTransitionSource)
					throwErrorMessage("errorLocationNotFound",
						where, lineNumber, paramString);
			}
			else if (startsWith(line, "Target: "))
			{
				if (null == tempTransitionSource)
					throwErrorMessage(
						"errorTransitionSourceMissing", where, lineNumber);

				tempLocation = findLocation(paramString);

				if (null == tempLocation)
					throwErrorMessage("errorLocationNotFound",
						where, lineNumber, paramString);

				tempTransition = tempTransitionSource.
					getTransition(tempLocation);

				if (null == tempTransition) throwErrorMessage(
					"errorTransitionNotFound", where, lineNumber,
						tempTransitionSource, tempLocation);

				tempTransitionSource = null;
				tempLocation = null;
				tempItem = null;
				tempTrigger = null;
			}
			else if (startsWith(line, "Item: "))
			{
				tempLocation = null;
				tempTransitionSource = null;
				tempTransition = null;
				tempItem = findItem(paramString);
				tempTrigger = null;

				if (null == tempItem)
					throwErrorMessage("errorItemNotFound",
						where, lineNumber, paramString);
			}
			else if (startsWith(line, "Trigger: "))
			{
				tempLocation = null;
				tempTransitionSource = null;
				tempTransition = null;
				tempItem = null;
				tempTrigger = findTrigger(paramString);

				if (null == tempTrigger)
					throwErrorMessage("errorTriggerNotFound",
						where, lineNumber, paramString);
			}
			else if (startsWith(line, startup.name))
			{
				tempLocation = null;
				tempTransitionSource = null;
				tempTransition = null;
				tempItem = null;
				tempTrigger = startup;
			}
			else if (startsWith(line, readState.name))
			{
				tempLocation = null;
				tempTransitionSource = null;
				tempTransition = null;
				tempItem = null;
				tempTrigger = readState;
			}
			else if (startsWith(line, query.name))
			{
				tempLocation = null;
				tempTransitionSource = null;
				tempTransition = null;
				tempItem = null;
				tempTrigger = query;
			}
			else if (startsWith(line, populate.name))
			{
				tempLocation = null;
				tempTransitionSource = null;
				tempTransition = null;
				tempItem = null;
				tempTrigger = populate;
			}
			else if (startsWith(line, "Avatar: "))
			{
				tempLocation = null;
				tempTransitionSource = null;
				tempTransition = null;
				tempItem = null;
				tempTrigger = null;
			}
			else
			{
				if (null != tempLocation)
				{
					if (!tempLocation.processProperty(line))
						throwErrorMessage("errorUnknownProperty",
							where, lineNumber, line);
				}
				else if (null != tempTransition)
				{
					if (!tempTransition.processProperty(line))
						throwErrorMessage("errorUnknownProperty",
							where, lineNumber, line);
				}
				else if (null != tempItem)
				{
					if (!tempItem.processProperty(line))
						throwErrorMessage("errorUnknownProperty",
							where, lineNumber, line);
				}
				else if (null != tempTrigger)
				{
					if (!tempTrigger.processProperty(line))
						throwErrorMessage("errorUnknownProperty",
							where, lineNumber, line);
				}
				else
				{
					if (!avatar.processProperty(line))
						throwErrorMessage("errorUnknownProperty",
							where, lineNumber, line);
				}
			}
		}

		/*
		for (Location location : locations)
			location.resetProperties();
		for (Item item : items)
			item.resetProperties();
		avatar.resetProperties();
		*/
	}


	public boolean walk(Entity place, Entity mean)
	{
		// Note: Avatar might be inside some item…
		if (null == avatar.getPlacement() ||
			!avatar.getPlacement().isVisible())
		{
			failureMessage = textDriver.parseMessage(
				"failureAvatarIsNowhere");
			return false;
		}

		if (!(avatar.getPlacement() instanceof Location))
		{
			failureMessage = textDriver.parseMessage(
				"failureAvatarIsInside", avatar.getPlacement());
			return false;
		}

		Location placement = (Location)avatar.getPlacement();
		Location destination = findLocation(place);

		if (null == destination || !destination.isVisible())
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownLocation", place);
			return false;
		}

		Item tool = null == mean ? null : findItem(mean);

		if (null != mean && (null == tool || !tool.isVisible()))
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownMean", mean);
			return false;
		}

		Transition transition = placement.getTransition(destination);

		if (null == transition || !transition.isVisible())
		{
			failureMessage = textDriver.parseMessage(
				"failureTransitionNotFound", place);
			return false;
		}

		if (!transition.canBear(avatar))
		{
			failureMessage = textDriver.parseMessage(
				"failureAvatarTooHeavy");
			return false;
		}

		if (!transition.canFit(avatar))
		{
			failureMessage = textDriver.parseMessage(
				"failureAvatarTooLarge");
			return false;
		}

		sourceHolder = placement;
		targetHolder = destination;
		resourceHolder = tool;

		if (0 != placement.onLeave.size())
		{
			if (!runScript("On leave " + placement.name,
				placement.onLeave, placement, null)) return false;
		}

		String error;

		if (null != (error = placement.removeEntry(avatar)))
		{
			failureMessage = textDriver.parseMessage(error, avatar);
			return false;
		}

		if (null != tool)
		{
			if (0 == tool.onDrive.size())
			{
				failureMessage = textDriver.parseMessage(
					"failureItemNotVehicle", tool);
				return false;
			}
			else
			{
				if (!runScript("On drive " + tool.name,
					tool.onDrive, tool, null)) return false;
			}
		}

		if (null != (error = destination.insertEntry(avatar)))
		{
			failureMessage = textDriver.parseMessage(error, avatar);
			return false;
		}

		if (0 != destination.onEnter.size())
		{
			if (!runScript("On enter " + destination.name,
				destination.onEnter, destination, null)) return false;
		}

		if (destination != avatar.getPlacement())
		{
			// Teleported‼ – change destination
			out.println("Teleported from " + destination + " to " +
				avatar.getPlacement() + ".");
			if (avatar.getPlacement() instanceof Location)
				destination = (Location)avatar.getPlacement();
			else
				return true;
		}

		if (autoexplore && 0 != destination.onExplore.size())
		{
			if (!runScript("On explore " + destination.name,
				destination.onExplore, destination, null)) return false;
		}

		destination.setVisited();

		return true;
	}

	public boolean return_(Entity place, Entity mean)
	{
		// Note: Avatar might be inside some item…
		if (null == avatar.getPlacement() ||
			!avatar.getPlacement().isVisible())
		{
			failureMessage = textDriver.parseMessage(
				"failureAvatarIsNowhere");
			return false;
		}

		if (!(avatar.getPlacement() instanceof Location))
		{
			failureMessage = textDriver.parseMessage(
				"failureAvatarIsInside", avatar.getPlacement());
			return false;
		}

		Location placement = (Location)avatar.getPlacement();
		Location destination = findLocation(place);

		if (null == destination || !destination.isVisible())
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownLocation", place);
			return false;
		}

		Item tool = null == mean ? null : findItem(mean);

		if (null != mean && (null == tool || !tool.isVisible()))
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownMean", mean);
			return false;
		}

		if (null != tool && 0 == tool.onDrive.size())
		{
			failureMessage = textDriver.parseMessage(
				"failureItemNotVehicle", tool);
			return false;
		}

		// ‼TODO‼
		// sourceHolder = placement;
		// targetHolder = destination;
		// resourceHolder = tool;


		throw new RuntimeException("The command “return” is not implemented yet!");
		// return true;
	}

	public boolean explore(Entity place, Entity mean)
	{
		// Note: Avatar might be inside some item…
		if (null == avatar.getPlacement() ||
			!avatar.getPlacement().isVisible())
		{
			failureMessage = textDriver.parseMessage(
				"failureAvatarIsNowhere");
			return false;
		}

		if (!(avatar.getPlacement() instanceof Location))
		{
			failureMessage = textDriver.parseMessage(
				"failureAvatarIsInside", avatar.getPlacement());
			return false;
		}

		Location placement = (Location)avatar.getPlacement();
		Item tool = null == mean ? null : findItem(mean);

		if (null != mean && (null == tool || !tool.isVisible()))
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownMean", mean);
			return false;
		}

		Location destination = findLocation(place);

		if (placement != destination)
		{
			if (null == destination || !destination.isVisible())
			{
				failureMessage = textDriver.parseMessage(
					"failureUnknownLocation", place);
				return false;
			}

			Transition transition = placement.getTransition(destination);

			if (null == transition || !transition.isVisible())
			{
				failureMessage = textDriver.parseMessage(
					"failureTransitionNotFound", place);
				return false;
			}
		}

		sourceHolder = destination;
		targetHolder = tool;
		resourceHolder = null;

		if (0 != destination.onExplore.size())
		{
			if (!runScript("On explore " + destination.name,
				destination.onExplore, destination, null)) return false;
		}

		return true;
	}

	public boolean pick(Entity entity, Entity instrument)
	{
		Item item = findItem(entity);
		Item tool = null == instrument ? null : findItem(instrument);

		if (null == item || !item.isVisible())
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownItem", entity);
			return false;
		}

		if (null != instrument && (null == tool || !tool.isVisible()))
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownMean", instrument);
			return false;
		}

		if (avatar == item.getPlacement())
		{
			failureMessage = textDriver.parseMessage(
				"failureAvatarHaveAlready", item);
			return false;
		}

		if (avatar.getPlacement() != item.getPlacement())
		{
			failureMessage = textDriver.parseMessage(
				"failureItemNotHere", item);
			return false;
		}

		if (null != tool && avatar != tool.getPlacement())
		{
			failureMessage = textDriver.parseMessage(
				"failureAvatarDoesNotHave", tool);
			return false;
		}

		// out.println("Placement of avatar: " + avatar.getPlacement());
		// out.println("Placement of item: " + item.getPlacement());
		// if (null != tool)
		// 	out.println("Placement of tool: " + tool.getPlacement());

		sourceHolder = item;
		targetHolder = tool;
		resourceHolder = null;

		if (0 != item.onPick.size())
		{
			if (!runScript("On pick " + item.name, item.onPick, item, null))
				return false;
		}

		String error;

		if (null != item.getPlacement())
		{
			if (null != (error = item.getPlacement().removeEntry(item)))
			{
				failureMessage = textDriver.parseMessage(error, item);
				return false;
			}
		}

		if (null != (error = avatar.insertEntry(item)))
		{
			failureMessage = textDriver.parseMessage(error, item);
			return false;
		}

		return true;
	}

	public boolean drop(Entity entity, Entity instrument)
	{
		Item item = findItem(entity);
		Item tool = null == instrument ? null : findItem(instrument);

		if (null == item || !item.isVisible())
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownItem", entity);
			return false;
		}

		if (null != instrument && (null == tool || !tool.isVisible()))
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownMean", instrument);
			return false;
		}

		if (avatar != item.getPlacement())
		{
			failureMessage = textDriver.parseMessage(
				"failureAvatarDoesNotHave", item);
			return false;
		}

		if (null != tool && avatar != tool.getPlacement())
		{
			failureMessage = textDriver.parseMessage(
				"failureAvatarDoesNotHave", tool);
			return false;
		}

		sourceHolder = item;
		targetHolder = tool;
		resourceHolder = null;

		if (0 != item.onDrop.size())
		{
			if (!runScript("On drop " + item.name, item.onDrop, item, null))
				return false;
		}

		String error;

		if (null != (error = avatar.removeEntry(item)))
		{
			failureMessage = textDriver.parseMessage(error, item);
			return false;
		}

		if (null != avatar.getPlacement())
		{
			if (null != (error = avatar.getPlacement().insertEntry(item)))
			{
				failureMessage = textDriver.parseMessage(error, item);
				return false;
			}
		}

		return true;
	}

	public boolean throw_(Entity entity, Entity onWhat)
	{
		Item item = findItem(entity);
		Item target = null == onWhat ? null : findItem(onWhat);

		if (null == item || !item.isVisible())
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownItem", entity);
			return false;
		}

		if (null != onWhat && (null == target || !target.isVisible()))
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownMean", onWhat);
			return false;
		}

		if (avatar != item.getPlacement())
		{
			failureMessage = textDriver.parseMessage(
				"failureAvatarDoesNotHave", item);
			return false;
		}

		// Here is the first difference against the drop command – the target
		// item must be “here” (that means at the same location as the
		// avatar), not in avatar’s inventory:
		if (null != target && avatar.getPlacement() != target.getPlacement())
		{
			failureMessage = textDriver.parseMessage(
				"failureItemNotHere", target);
			return false;
		}

		sourceHolder = item;
		targetHolder = target;
		resourceHolder = null;

		// The basic result is the same as the result of the drop command.

		String error;

		if (null != (error = avatar.removeEntry(item)))
		{
			failureMessage = textDriver.parseMessage(error, item);
			return false;
		}

		if (null != avatar.getPlacement())
		{
			if (null != (error = avatar.getPlacement().insertEntry(item)))
			{
				failureMessage = textDriver.parseMessage(error, item);
				return false;
			}
		}

		// Another difference is that the handler is executed after the
		// default action, not before. This allows to script the handler so
		// that the target entity “catches” the item.
		if (0 != item.onThrow.size())
		{
			if (!runScript("On throw " + item.name, item.onThrow, item, null))
				return false;
		}

		return true;
	}

	public boolean insert(Entity entity, Entity target)
	{
		if (null == target)
		{
			failureMessage = textDriver.parseMessage(
				"failureMissingTarget");
			return false;
		}

		Item item = findItem(entity);
		Item where = findItem(target);

		if (null == item || !item.isVisible())
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownItem", entity);
			return false;
		}

		if (null == where || !where.isVisible())
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownItem", target);
			return false;
		}

		if (avatar.getPlacement() != item.getPlacement() &&
			avatar != item.getPlacement())
		{
			failureMessage = textDriver.parseMessage(
				"failureItemNotHere", item);
			return false;
		}

		if (avatar.getPlacement() != where.getPlacement() &&
			avatar != where.getPlacement())
		{
			failureMessage = textDriver.parseMessage(
				"failureItemNotHere", where);
			return false;
		}

		sourceHolder = item;
		targetHolder = where;
		resourceHolder = null;

		if (0 != item.onInsert.size())
		{
			if (!runScript("On insert " + item.name,
				item.onInsert, item, null)) return false;
		}

		if (0 != where.onAdmit.size())
		{
			if (!runScript("On admit " + where.name,
				where.onAdmit, where, null)) return false;
		}

		String error;

		if (null != item.getPlacement())
		{
			if (null != (error = item.getPlacement().removeEntry(item)))
			{
				failureMessage = textDriver.parseMessage(error, item);
				return false;
			}
		}

		if (null != (error = where.insertEntry(item)))
		{
			failureMessage = textDriver.parseMessage(error, item);
			return false;
		}

		return true;
	}

	public boolean remove(Entity entity, Entity source)
	{
		if (null == source)
		{
			failureMessage = textDriver.parseMessage(
				"failureMissingSource");
			return false;
		}

		Item item = findItem(entity);
		Item from = findItem(source);

		if (null == item || !item.isVisible())
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownItem", entity);
			return false;
		}

		if (null == from || !from.isVisible())
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownItem", source);
			return false;
		}

		if (from != item.getPlacement())
		{
			failureMessage = textDriver.parseMessage(
				"failureItemNotInside", item, from);
			return false;
		}

		if (avatar.getPlacement() != from.getPlacement() &&
			avatar != from.getPlacement())
		{
			failureMessage = textDriver.parseMessage(
				"failureItemNotHere", from);
			return false;
		}

		sourceHolder = item;
		targetHolder = from;
		resourceHolder = null;

		if (0 != item.onRemove.size())
		{
			if (!runScript("On remove " + item.name,
				item.onRemove, item, null)) return false;
		}

		if (0 != from.onRelease.size())
		{
			if (!runScript("On release " + from.name,
				from.onRelease, from, null)) return false;
		}

		String error;

		if (null != (error = from.removeEntry(item)))
		{
			failureMessage = textDriver.parseMessage(error, item);
			return false;
		}

		try
		{
			// Try to give the item to avatar
			if (null != (error = avatar.insertEntry(item)))
			{
				if (null == avatar.getPlacement())
					return false;
				else
					throw new Exception();
			}
		}
		catch (Exception e)
		{
			// If failed to give the item to avatar, try to drop it
			if (null != avatar.getPlacement())
			{
				if (null != (error = avatar.getPlacement().insertEntry(item)))
				{
					failureMessage = textDriver.parseMessage(error, item);
					return false;
				}
			}
			else throw new RuntimeException(e.getMessage(), e.getCause());
		}

		return true;
	}

	public boolean examine(Entity entity, Entity instrument)
	{
		Item item = findItem(entity);
		Item tool = null == instrument ? null : findItem(instrument);

		if (null == item || !item.isVisible())
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownItem", entity);
			return false;
		}

		if (null != instrument && (null == tool || !tool.isVisible()))
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownMean", instrument);
			return false;
		}

		sourceHolder = item;
		targetHolder = tool;
		resourceHolder = null;

		if (0 == item.onExamine.size())
		{
			if (0 == avatar.onCommonExamine.size())
			{
				failureMessage = textDriver.parseMessage(
					"failureItemUnableExamine", item);
				return false;
			}

			return runScript("On common examine",
				avatar.onCommonExamine, avatar, null);
		}

		return runScript("On examine " + item.name,
			item.onExamine, item, null);
	}

	public boolean use(Entity entity, Entity instrument)
	{
		Item item = findItem(entity);
		Item tool = null == instrument ? null : findItem(instrument);

		if (null == item || !item.isVisible())
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownItem", entity);
			return false;
		}

		if (null != instrument && (null == tool || !tool.isVisible()))
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownMean", instrument);
			return false;
		}

		sourceHolder = item;
		targetHolder = tool;
		resourceHolder = null;

		if (0 == item.onUse.size())
		{
			failureMessage = textDriver.parseMessage(
				"failureItemUnusable", item);
			return false;
		}

		return runScript("On use " + item.name, item.onUse, item, null);
	}

	public boolean action(Entity entity, Entity instrument)
	{
		Item item = findItem(entity);
		Item tool = null == instrument ? null : findItem(instrument);

		if (null == item || !item.isVisible())
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownItem", entity);
			return false;
		}

		if (null != instrument && (null == tool || !tool.isVisible()))
		{
			failureMessage = textDriver.parseMessage(
				"failureUnknownMean", instrument);
			return false;
		}

		sourceHolder = item;
		targetHolder = tool;
		resourceHolder = null;

		if (0 == item.onAction.size())
		{
			failureMessage = textDriver.parseMessage(
				"failureItemNoAction", item);
			return false;
		}

		return runScript("On action " + item.name, item.onAction, item, null);
	}


	public void splitCommandLine(Vector<String> commands, String commandLine)
	{
		commands.clear();
		int i = 0;

		do
		{
			int indexOf = commandLine.indexOf(commandLineSeparator);

			if (-1 != indexOf)
			{
				commands.add(commandLine.substring(0, indexOf));
				commandLine = commandLine.substring(indexOf + 1);
			}
			else
			{
				commands.add(commandLine);
				commandLine = "";
			}

			if (++i > 1000) return;
		}
		while (commandLine.length() > 0);
	}


	public boolean tokenize(Vector<Entity> result, String string)
	{
		string = string.replaceAll("[\\s ]+", " ").trim().toLowerCase() + " ";
		result.clear();

		int i = 0;
		do
		{
			boolean notFound = true;

			for (Entity token : tokens)
			{
				if (string.length() >= token.name.length() &&
					string.startsWith(token.name))
				{
					string = string.substring(token.name.length());
					result.add(token);
					notFound = false;
					break;
				}
			}

			if (notFound)
			{
				/* “unsorted way”
				for (Enumeration<String> keys = aliases.keys();
					keys.hasMoreElements();)
				{
					String key = keys.nextElement();

					if (string.length() >= key.length() &&
						string.startsWith(key))
					{
						string = string.substring(key.length());
						result.add(aliases.get(key));
						notFound = false;
						break;
					}
				}
				*/
				Set<Map.Entry<String, Entity>> entries = aliases.entrySet();
				for (Map.Entry<String, Entity> entry : entries)
				{
					String key = entry.getKey();

					if (string.length() >= key.length() &&
						string.startsWith(key))
					{
						string = string.substring(key.length());
						result.add(entry.getValue());
						notFound = false;
						break;
					}
				}
			}

			if (notFound) throwErrorMessage(
				"failureTokenNotFound", string.length() > 20 ?
					string.substring(0, 20) + "…" : string);

			if (++i > 1000) return false;
		}
		while (string.length() > 0);

		return true;
	}


	public Entity addToken(String token)
	{
		Entity newToken = new Entity(
			token.replaceAll("[\\s ]+", " ").trim().toLowerCase() + " ");

		// Check the aliases and print warnings…
		/* “unsorted way”
		for (Enumeration<String> keys = aliases.keys();
			keys.hasMoreElements();)
		{
			String key = keys.nextElement();

			if (key.startsWith(newToken.name) ||
				newToken.name.startsWith(key))
				textDriver.parseAndWriteWarning(
					"warningAliasTokenConflict", newToken.name);
		}
		*/
		Set<String> keys = aliases.keySet();
		for (String key : keys)
		{
			if (tokenStartsChecking &&
				(key.startsWith(newToken.name) ||
				newToken.name.startsWith(key)))
				textDriver.parseAndWriteWarning(
					"warningAliasTokenConflict", newToken.name);
		}


		int indexOf = tokens.indexOf(newToken);

		if (-1 == indexOf)
		{
			for (Entity check : tokens)
			{
				if (tokenStartsChecking &&
					(check.name.startsWith(newToken.name) ||
					newToken.name.startsWith(check.name)))
					throwErrorMessage(
						"errorAmbiguousTokens", definitionFilename,
						definitionLineNumber, newToken, check);
			}
			put(tokens, newToken);
			return newToken;
		}

		// Else it is a duplicate
		newToken = null;
		return tokens.elementAt(indexOf);
	}

	private int dumpAliases()
	{
		int count = 0;

		/* “unsorted way”
		for (Enumeration<String> keys = aliases.keys();
			keys.hasMoreElements();)
		{
			String key = keys.nextElement(); ++count;
			textDriver.writeLine("> ", key, "(",
				key.length() - 1, ") for " + aliases.get(key).name);
		}
		*/
		Set<Map.Entry<String, Entity>> entries = aliases.entrySet();
		for (Map.Entry<String, Entity> entry : entries)
		{
			++count;
			textDriver.writeLine("> ", entry.getKey(), "(",
				entry.getKey().length() - 1, ") for " +
				entry.getValue().name);
		}

		return count;
	}

	private void dumpAliases(Entity entity)
	{
		boolean first = true;

		/* “unsorted way”
		for (Enumeration<String> keys = aliases.keys();
			keys.hasMoreElements();)
		{
			String key = keys.nextElement();

			if (aliases.get(key) == entity)
			{
				if (first)
				{
					textDriver.write("  Aliases: ");
					first = false;
				}
				else
				{
					textDriver.write(", ");
				}

				textDriver.write(
					key, "(", key.length() - 1, ")");
			}
		}
		*/
		Set<Map.Entry<String, Entity>> entries = aliases.entrySet();
		for (Map.Entry<String, Entity> entry : entries)
		{
			if (entry.getValue() == entity)
			{
				if (first)
				{
					textDriver.write("  Aliases: ");
					first = false;
				}
				else
				{
					textDriver.write(", ");
				}

				textDriver.write( entry.getKey(),
					"(", entry.getKey().length() - 1, ")");
			}
		}

		if (!first) textDriver.writeLine();
	}

	private String addAlias(String alias, Entity aliasFor)
	{
		// Errors/Warnings:
		//  Overriding – trying to override one of main tokens
		//  Ambiguous  – is starting with another alias or main token
		//               or vice versa
		//  Duplicate  – attempt to create alias for another entity

		String newAlias = alias.replaceAll(
			"[\\s ]+", " ").trim().toLowerCase() + " ";

		for (Entity check : tokens)
		{
			if (check.name.equals(newAlias))
				return "warningOverridingAlias";

			if (tokenStartsChecking &&
				(check.name.startsWith(newAlias) ||
				newAlias.startsWith(check.name)))
				return "warningAmbiguousAlias";
		}

		/* “unsorted way”
		for (Enumeration<String> keys = aliases.keys();
			keys.hasMoreElements();)
		{
			String key = keys.nextElement();

			if (newAlias.equals(key))
			{
				if (aliases.get(key) != aliasFor)
					return "warningDuplicateAlias";
				return null;
			}
			else if (key.startsWith(newAlias) ||
				newAlias.startsWith(key))
				return "warningAmbiguousAlias";
		}
		*/
		Set<Map.Entry<String, Entity>> entries = aliases.entrySet();
		for (Map.Entry<String, Entity> entry : entries)
		{
			String key = entry.getKey();

			if (newAlias.equals(key))
			{
				if (entry.getValue() != aliasFor)
					return "warningDuplicateAlias";
				return null;
			}
			else if (tokenStartsChecking &&
				(key.startsWith(newAlias) ||
				newAlias.startsWith(key)))
				return "warningAmbiguousAlias";
		}

		aliases.put(newAlias, aliasFor);
		// dumpAliases(); textDriver.writeLine();
		return null;
	}

	public void checkUniqueToken(Entity uniqueToken)
	{
		// if (-1 != uniqueTokens.indexOf(uniqueToken)) ...
		if (uniqueTokens.contains(uniqueToken))
			throwErrorMessage("errorDuplicateToken",
				definitionFilename, definitionLineNumber, uniqueToken);
		else
			uniqueTokens.add(uniqueToken);
	}


	// This method is dedicated to use with list of tokens. Long tokens
	// should be placed first.
	public static void put(Vector<Entity> vector, Entity entity)
	{
		int where = TextEngine.whereToPut(vector, entity);
		vector.insertElementAt(entity, where);
	}

	// Search place for put(v, e) method
	public static int whereToPut(Vector<Entity> vector, Entity entity)
	{
		if (vector.size() == 0) return 0;

		int left = 0;
		int right = vector.size() - 1;
		int length1 = entity.name.length();

		while (left <= right)
		{
			int middle = (right + left) / 2;
			int length2 = vector.elementAt(middle).name.length();

			if (length2 > length1)
				left = middle + 1;
			else if (length2 < length1)
				right = middle - 1;
			else
				return middle;
		}

		return left > 0 ? left : 0;
	}


	// Populate lists:

	public void populateLocations(TextDriverInterface.ScrollList list)
	{
		list.clear();
		if (null != avatar.getPlacement() &&
			avatar.getPlacement() instanceof Location)
		{
			Vector<Transition> transitions =
				((Location)avatar.getPlacement()).getTransitions();
			for (Transition transition : transitions)
			{
				if (transition.isVisible() && transition.target.isVisible())
					list.add(transition.target);
			}
		}
	}

	public void populateItems(TextDriverInterface.ScrollList list)
	{
		list.clear();
		if (null != avatar.getPlacement())
		{
			Vector<Entry> entries = avatar.getPlacement().getEntries();
			for (Entry entry : entries)
			{
				if (entry.isVisible() && avatar != entry)
					list.add(entry);
			}
		}
	}

	public void populateInventory(TextDriverInterface.ScrollList list)
	{
		list.clear();
		Vector<Entry> entries = avatar.getEntries();

		for (Entry entry : entries)
			if (entry.isVisible())
				list.add(entry);
	}

	// public void populateInserted(TextDriverInterface.ScrollList list) {}
	// (this list is for special purposes)


	public void throwErrorMessage(
		String templateName, Object... arguments)
	{
		throw new RuntimeException(textDriver.
			parseMessage(templateName, arguments));
	}


	public static void main(String[] args)
	{
		TextDriver.main(args);
	}
}
