
import java.io.IOException;

import java.util.Vector;

import bookcase.TextFile;

/*#
 * This is part of the Text Driver project. See TextDriver.java for version
 * information and changes.
 */

/**
 * Container is anything that can (possibly) contain items. This can be
 * location, item or avatar. See Entry, Location, Avatar.
 */

public class Container extends Holder implements Tonnage, Capacity
{
	private double tonnage = 0, oldTonnage = 0,
		defaultTonnage = 0;
	private double capacity = 0, oldCapacity = 0,
		defaultCapacity = 0;

	private double spentTonnage = 0, oldSpentTonnage = 0,
		defaultSpentTonnage = 0;
	private double spentCapacity = 0, oldSpentCapacity = 0,
		defaultSpentCapacity = 0;

	private boolean visible = true, oldVisibility = true,
		defaultVisibility = true;

	public boolean touched = false;

	private final Vector<Entry> entries = new Vector<Entry>();
	private final Vector<Entry> entriesOld = new Vector<Entry>();
	private final Vector<Entry> entriesDefault = new Vector<Entry>();


	public Container(String name)
	{
		super(name);
	}


	public String getName()
	{
		return originalName;
	}


	public Vector<Entry> getEntries()
	{
		return entries;
	}


	public void copy(Object obj)
	{
		if (obj instanceof Container)
		{
			Container container = (Container)obj;
			this.tonnage = container.tonnage;
			this.capacity = container.capacity;
			this.visible = container.visible;
		}
	}


	public boolean canTake(Entry entry)
	{
		return spentTonnage + entry.getWeight() <= tonnage &&
			spentCapacity + entry.getVolume() <= capacity;
	}

	public boolean canBear(Entry entry)
	{
		return spentTonnage + entry.getWeight() <= tonnage;
	}

	public boolean canFit(Entry entry)
	{
		return spentCapacity + entry.getVolume() <= capacity;
	}


	public boolean isVisible()
	{
		return visible;
	}

	public void show()
	{
		DriverOutput.debugMessage(this, "show");
		visible = true;
	}

	public void hide()
	{
		DriverOutput.debugMessage(this, "hide");
		visible = false;
	}


	public void setTonnage(double value)
	{
		DriverOutput.debugMessage(this, "set tonnage to", value);
		tonnage = value;
	}

	public double getTonnage()
	{
		return tonnage;
	}


	public void setCapacity(double value)
	{
		DriverOutput.debugMessage(this, "set capacity to", value);
		capacity = value;
	}

	public double getCapacity()
	{
		return capacity;
	}


	public void addSpentTonnage(double value)
	{
		DriverOutput.debugMessage(this, "increase spentTonnage",
			spentTonnage, "by", value);
		spentTonnage += value;
	}

	public void removeSpentTonnage(double value)
	{
		DriverOutput.debugMessage(this, "decrease spentTonnage",
			spentTonnage, "by", value);
		spentTonnage -= value;
	}

	public void setSpentTonnage(double value)
	{
		DriverOutput.debugMessage(this, "set spentTonnage to", value);
		spentTonnage = value;
	}

	public double getSpentTonnage()
	{
		return spentTonnage;
	}


	public void addSpentCapacity(double value)
	{
		DriverOutput.debugMessage(this, "increase spentCapacity",
			spentCapacity, "by", value);
		spentCapacity += value;
	}

	public void removeSpentCapacity(double value)
	{
		DriverOutput.debugMessage(this, "decrease spentCapacity",
			spentCapacity, "by", value);
		spentCapacity -= value;
	}

	public void setSpentCapacity(double value)
	{
		DriverOutput.debugMessage(this, "set spentCapacity to", value);
		spentCapacity = value;
	}

	public double getSpentCapacity()
	{
		return spentCapacity;
	}


	public double getWeightOfEntries()
	{
		double sum = 0;
		for (Entry entry : entries)
			sum += entry.getWeight();
		return sum;
	}


	/**
	 * Returned String is ID of error message; returned null means success.
	 */
	public String insertEntry(Entry entry)
	{
		if (null != entry.getPlacement())
			return "alertEntryAlreadyPlaced";
		if (!canBear(entry))
			return "alertEntryTooHeavy";
		if (!canFit(entry))
			return "alertEntryTooLarge";
		addSpentTonnage(entry.getWeight());
		addSpentCapacity(entry.getVolume());
		entries.add(entry);
		entry.setPlacement(this);
		return null;
	}

	/**
	 * Returned String is ID of error message; returned null means success.
	 */
	public String removeEntry(Entry entry)
	{
		if (!entries.contains(entry))
			return "alertEntryNotInPlace";
		removeSpentTonnage(entry.getWeight());
		removeSpentCapacity(entry.getVolume());
		entries.remove(entry);
		entry.setPlacement(null);
		return null;
	}


	@Override public void backupProperties()
	{
		super.backupProperties();
		oldTonnage = tonnage;
		oldCapacity = capacity;
		oldSpentTonnage = spentTonnage;
		oldSpentCapacity = spentCapacity;
		oldVisibility = visible;
		entriesOld.clear();
		entriesOld.addAll(entries);
	}

	@Override public void restoreProperties()
	{
		super.restoreProperties();

		if (tonnage != oldTonnage)
		{
			DriverOutput.debugMessage(this, "restore tonnage from",
				tonnage, "to", oldTonnage);
			tonnage = oldTonnage;
		}

		if (capacity != oldCapacity)
		{
			DriverOutput.debugMessage(this, "restore capacity from",
				capacity, "to", oldCapacity);
			capacity = oldCapacity;
		}

		if (spentTonnage != oldSpentTonnage)
		{
			DriverOutput.debugMessage(this, "restore spentTonnage from",
				spentTonnage, "to", oldSpentTonnage);
			spentTonnage = oldSpentTonnage;
		}

		if (spentCapacity != oldSpentCapacity)
		{
			DriverOutput.debugMessage(this, "restore spentCapacity from",
				spentCapacity, "to", oldSpentCapacity);
			spentCapacity = oldSpentCapacity;
		}

		if (visible != oldVisibility)
		{
			DriverOutput.debugMessage(this, "restore visibility from",
				visible, "to", oldVisibility);
			visible = oldVisibility;
		}

		entries.clear();
		entries.addAll(entriesOld);
	}


	@Override public void saveDefaultProperties()
	{
		super.saveDefaultProperties();

		defaultTonnage = tonnage;
		defaultCapacity = capacity;
		defaultSpentTonnage = spentTonnage;
		defaultSpentCapacity = spentCapacity;
		defaultVisibility = visible;

		entriesDefault.clear();
		entriesDefault.addAll(entries);
	}

	@Override public void resetProperties()
	{
		super.resetProperties();

		tonnage = defaultTonnage;
		capacity = defaultCapacity;
		spentTonnage = defaultSpentTonnage;
		spentCapacity = defaultSpentCapacity;
		visible = defaultVisibility;

		entries.clear();
		entries.addAll(entriesDefault);
	}


	@Override public void writeProperties(TextFile file) throws IOException
	{
		super.writeProperties(file);

		if (tonnage != defaultTonnage)
		{
			file.write("    Tonnage: ");
			file.writeLine(String.valueOf(tonnage));
		}

		if (capacity != defaultCapacity)
		{
			file.write("    Capacity: ");
			file.writeLine(String.valueOf(capacity));
		}

		if (spentTonnage != defaultSpentTonnage)
		{
			file.write("    SpentTonnage: ");
			file.writeLine(String.valueOf(spentTonnage));
		}

		if (spentCapacity != defaultSpentCapacity)
		{
			file.write("    SpentCapacity: ");
			file.writeLine(String.valueOf(spentCapacity));
		}

		if (visible != defaultVisibility)
		{
			file.write("    Visible: ");
			file.writeLine(String.valueOf(visible));
		}

		boolean saveEntries = false;

		if (entries.size() != entriesDefault.size())
			saveEntries = true;
		else
		{
			for (int i = 0; i < entries.size(); ++i)
			{
				if (entries.get(i) != entriesDefault.get(i))
				{
					saveEntries = true;
					break;
				}
			}
		}

		if (saveEntries)
		{
			file.writeLine("    ClearEntries.");
			// file.write("    Entries: ");
			// file.writeLine(String.valueOf(entries.size()));

			for (Entry entry : entries)
			{
				file.write("        AddEntry: ");
				file.writeLine(entry.originalName);
			}
		}
	}

	@Override public boolean processProperty(String line)
	{
		if (Holder.startsWith(line, "Tonnage: "))
		{
			tonnage = Double.parseDouble(Holder.getParamString());
			return true;
		}

		if (Holder.startsWith(line, "Capacity: "))
		{
			capacity = Double.parseDouble(Holder.getParamString());
			return true;
		}

		if (Holder.startsWith(line, "SpentTonnage: "))
		{
			spentTonnage = Double.parseDouble(Holder.getParamString());
			return true;
		}

		if (Holder.startsWith(line, "SpentCapacity: "))
		{
			spentCapacity = Double.parseDouble(Holder.getParamString());
			return true;
		}

		if (Holder.startsWith(line, "Visible: "))
		{
			visible = Boolean.parseBoolean(Holder.getParamString());
			return true;
		}

		if (Holder.startsWith(line, "ClearEntries."))
		{
			entries.clear();
			return true;
		}

		if (Holder.startsWith(line, "AddEntry: "))
		{
			Entry entry = engine.getEntry(Holder.getParamString());

			if (null != entry)
			{
				entries.add(entry);
				return true;
			}

			return false;
		}

		if (super.processProperty(line)) return true;
		return false;
	}


	protected void dumpEntries()
	{
		if (0 == entries.size()) return;

		boolean first = true;
		DriverOutput.write("  Entries: ");

		for (Entry entry : entries)
		{
			if (first) first = false; else
				DriverOutput.write(", ");
			DriverOutput.write(entry);
		}

		if (first)
			DriverOutput.writeLine("–");
		else
			DriverOutput.writeLine(".");
	}
}
