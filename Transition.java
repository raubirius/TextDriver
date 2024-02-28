
import java.io.IOException;

import bookcase.TextFile;

/*#
 * This is part of the Text Driver project. See TextDriver.java for version
 * information and changes.
 */

/**
 * Transition means a “tunel” (or “door”) between locations.
 */

public class Transition implements Tonnage, Capacity
{
	private double tonnage = 0, oldTonnage = 0, defaultTonnage = 0;
	private double capacity = 0, oldCapacity = 0, defaultCapacity = 0;

	// Transitions do not keep items:
	// private double spentTonnage = 0;  // dummy
	// private double spentCapacity = 0; // dummy

	private boolean visible = true, oldVisibility = true,
		defaultVisibility = true;

	public final Location target;

	public Transition(Location target)
	{
		this.target = target;
	}


	public boolean canTake(Entry entry)
	{
		if (0 == getTonnage() && 0 == getCapacity()) return true;
		return /* spentTonnage + */entry.getWeight() <= tonnage &&
			/* spentCapacity + */entry.getVolume() <= capacity;
	}

	public boolean canBear(Entry entry)
	{
		if (0 == getTonnage()) return true;
		return /*spentTonnage + */entry.getWeight() <= tonnage;
	}

	public boolean canFit(Entry entry)
	{
		if (0 == getCapacity()) return true;
		return /* spentCapacity + */entry.getVolume() <= capacity;
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
		// spentTonnage += value; // dummy
	}

	public void removeSpentTonnage(double value)
	{
		// spentTonnage -= value; // dummy
	}

	public void setSpentTonnage(double value)
	{
		// spentTonnage = value; // dummy
	}

	public double getSpentTonnage()
	{
		return 0.0; // spentTonnage; // dummy
	}


	public void addSpentCapacity(double value)
	{
		// spentCapacity += value; // dummy
	}

	public void removeSpentCapacity(double value)
	{
		// spentCapacity -= value; // dummy
	}

	public void setSpentCapacity(double value)
	{
		// spentCapacity = value; // dummy
	}

	public double getSpentCapacity()
	{
		return 0.0; // spentCapacity; // dummy
	}


	public void backupProperties()
	{
		oldTonnage = tonnage;
		oldCapacity = capacity;
		oldVisibility = visible;
	}

	public void restoreProperties()
	{
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

		if (visible != oldVisibility)
		{
			DriverOutput.debugMessage(this, "restore visibility from",
				visible, "to", oldVisibility);
			visible = oldVisibility;
		}
	}


	public void saveDefaultProperties()
	{
		defaultTonnage = tonnage;
		defaultCapacity = capacity;
		defaultVisibility = visible;
	}

	public void resetProperties()
	{
		tonnage = defaultTonnage;
		capacity = defaultCapacity;
		visible = defaultVisibility;
	}


	public void writeProperties(TextFile file) throws IOException
	{
		file.write("Target: ");
		file.writeLine(target.originalName);

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

		if (visible != defaultVisibility)
		{
			file.write("    Visible: ");
			file.writeLine(String.valueOf(visible));
		}
	}

	public boolean processProperty(String line)
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

		if (Holder.startsWith(line, "Visible: "))
		{
			visible = Boolean.parseBoolean(Holder.getParamString());
			return true;
		}

		return false;
	}
}
