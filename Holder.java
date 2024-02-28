
import java.io.IOException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import bookcase.TextFile;

/*#
 * This is part of the Text Driver project. See TextDriver.java for version
 * information and changes.
 */

/**
 * This is basic extension of the fundamental class in the class hierarchy of
 * this project. This extension modifies the name, stores original name and
 * extends the fundamental class by properties.
 * See Container, Trigger.
 */

public class Holder extends Entity
{
	// The originalName is public final field.
	// (That means it is in fact constant.)
	public final String originalName;

	public Holder(String name)
	{
		super(name.replaceAll("[\\s ]+", " ").trim());
		originalName = name.replaceAll("\\s+", " ").trim();
	}

	/**
	 * This subclass stores all data required for one property.
	 */
	public class Property
	{
		public double value = 0.0;
		public double oldValue = 0.0;
		public double defaultValue = 0.0;

		// Reserved for future updates…
	}

	private TreeMap<String, Property> properties =
		new TreeMap<String, Property>();

	private String repairPropertyName(String name)
	{
		return name.replaceAll("[\\s ]+", " ").trim().toLowerCase();
	}

	public boolean defineProperty(String name)
	{
		name = repairPropertyName(name);
		Property property = properties.get(name);

		if (null == property)
		{
			property = new Property();
			properties.put(name, property);
			return true;
		}

		return false;
	}

	public boolean setProperty(String name, double value)
	{
		name = repairPropertyName(name);
		Property property = properties.get(name);
		if (null == property) return false;
		property.value = value;
		return true;
	}

	public Double getProperty(String name)
	{
		name = repairPropertyName(name);
		Property property = properties.get(name);
		if (null == property) return null;
		return property.value;
	}


	protected void dumpProperties()
	{
		if (0 == properties.size()) return;

		DriverOutput.writeLine("  Properties:");

		Set<Map.Entry<String, Property>> entries = properties.entrySet();
		for (Map.Entry<String, Property> entry : entries)
		{
			Property property = entry.getValue();
			DriverOutput.write("    ", entry.getKey(), ": ");
			if (null == property)
				DriverOutput.write("error");
			else
			{
				DriverOutput.write(property.value);
				if (property.defaultValue != property.value)
					DriverOutput.write(" (default: ",
						property.defaultValue, ")");
			}
			DriverOutput.writeLine();
		}
	}


	public void backupProperties()
	{
		Collection<Property> values = properties.values();
		for (Property property : values)
		{
			property.oldValue = property.value;
		}
	}

	public void restoreProperties()
	{
		Set<Map.Entry<String, Property>> entries = properties.entrySet();
		for (Map.Entry<String, Property> entry : entries)
		{
			Property property = entry.getValue();
			if (property.value != property.oldValue)
			{
				DriverOutput.debugMessage(this, "restore " + entry.getKey() +
					" from", property.value, "to", property.oldValue);
				property.value = property.oldValue;
			}
		}
	}


	public void saveDefaultProperties()
	{
		Collection<Property> values = properties.values();
		for (Property property : values)
		{
			property.defaultValue = property.value;
		}
	}

	public void resetProperties()
	{
		Collection<Property> values = properties.values();
		for (Property property : values)
		{
			property.value = property.defaultValue;
		}
	}


	public void writeProperties(TextFile file) throws IOException
	{
		file.writeLine(originalName);

		Set<Map.Entry<String, Property>> entries = properties.entrySet();
		for (Map.Entry<String, Property> entry : entries)
		{
			Property property = entry.getValue();
			if (property.value != property.defaultValue)
			{
				file.write("    " + entry.getKey() + ": ");
				file.writeLine(String.valueOf(property.value));
			}
		}
	}

	public boolean processProperty(String line)
	{
		Set<Map.Entry<String, Property>> entries = properties.entrySet();
		for (Map.Entry<String, Property> entry : entries)
		{
			Property property = entry.getValue();
			if (startsWith(line, entry.getKey() + ": "))
			{
				property.value = Double.parseDouble(getParamString());
				return true;
			}
		}

		return false;
	}


	private static String paramString = null;

	public static boolean startsWith(String line, String keyString)
	{
		if (line.startsWith(keyString))
		{
			paramString = line.substring(keyString.length());
			return true;
		}

		return false;
	}

	public static String getParamString()
	{
		return paramString;
	}
}
