
import java.io.IOException;

import bookcase.TextFile;

/*#
 * This is part of the Text Driver project. See TextDriver.java for version
 * information and changes.
 */

/**
 * This is Container extended by two properties: weight and volume.
 * See Avatar, Item.
 */

public class Entry extends Container implements Weight, Volume
{
	private double weight = 1, oldWeight = 1, defaultWeight = 1;
	private double volume = 1, oldVolume = 1, defaultVolume = 1;

	private Container placement = null, oldPlacement = null,
		defaultPlacement = null; // isIn

	public Entry(String name)
	{
		super(name);
	}


	@Override public void copy(Object obj)
	{
		super.copy(obj);
		if (obj instanceof Entry)
		{
			Entry entry = (Entry)obj;
			this.weight = entry.weight;
			this.volume = entry.volume;
		}
	}


	private boolean recursivePlacement(Entry entry)
	{
		// Check for recursive placement
		Entry check = this;
		while (null != check)
		{
			if (entry == check) return true;
			if (check.placement instanceof Entry)
				check = (Entry)(check.placement);
			else
				break;
		}

		return false;
	}

	@Override public boolean canTake(Entry entry)
	{
		if (recursivePlacement(entry)) return false;
		if (null != placement && !placement.canBear(entry)) return false;
		return super.canTake(entry);
	}

	@Override public boolean canBear(Entry entry)
	{
		if (null != placement && !placement.canBear(entry)) return false;
		return super.canBear(entry);
	}

	@Override public boolean canFit(Entry entry)
	{
		if (null != placement) return false;
		return super.canFit(entry);
	}

	@Override public void addSpentTonnage(double value)
	{
		if (null != placement) placement.addSpentTonnage(value);
		super.addSpentTonnage(value);
	}

	@Override public void removeSpentTonnage(double value)
	{
		if (null != placement) placement.removeSpentTonnage(value);
		super.removeSpentTonnage(value);
	}

	@Override public void setSpentTonnage(double value)
	{
		if (null != placement)
		{
			placement.removeSpentTonnage(super.getSpentTonnage());
			super.setSpentTonnage(value);
			placement.addSpentTonnage(super.getSpentTonnage());
		}
		else
			super.setSpentTonnage(value);
	}


	public void setWeight(double value)
	{
		DriverOutput.debugMessage(this, "set weight to", value);
		weight = value;
	}

	public double getWeight()
	{
		return weight + getWeightOfEntries();
	}


	public void setVolume(double value)
	{
		DriverOutput.debugMessage(this, "set volume to", value);
		volume = value;
	}

	public double getVolume()
	{
		return volume;
	}


	protected void setPlacement(Container newPlacement)
	{
		DriverOutput.debugMessage(this, "change placement from",
			placement, "to", newPlacement);
		placement = newPlacement;
	}

	public Container getPlacement()
	{
		return placement;
	}


	@Override public String insertEntry(Entry entry)
	{
		if (recursivePlacement(entry))
			return "alertEntryRecursivePlacement";
		return super.insertEntry(entry);
	}


	@Override public void backupProperties()
	{
		super.backupProperties();
		oldWeight = weight;
		oldVolume = volume;
		oldPlacement = placement;
	}

	@Override public void restoreProperties()
	{
		super.restoreProperties();

		if (weight != oldWeight)
		{
			DriverOutput.debugMessage(this, "restore weight from",
				weight, "to", oldWeight);
			weight = oldWeight;
		}

		if (volume != oldVolume)
		{
			DriverOutput.debugMessage(this, "restore volume from",
				volume, "to", oldVolume);
			volume = oldVolume;
		}

		if (placement != oldPlacement)
		{
			DriverOutput.debugMessage(this, "restore placement from",
				placement, "to", oldPlacement);
			placement = oldPlacement;
		}
	}


	@Override public void saveDefaultProperties()
	{
		super.saveDefaultProperties();
		defaultWeight = weight;
		defaultVolume = volume;
		defaultPlacement = placement;
	}

	@Override public void resetProperties()
	{
		super.resetProperties();
		weight = defaultWeight;
		volume = defaultVolume;
		placement = defaultPlacement;
	}


	@Override public void writeProperties(TextFile file) throws IOException
	{
		super.writeProperties(file);

		if (weight != defaultWeight)
		{
			file.write("    Weight: ");
			file.writeLine(String.valueOf(weight));
		}

		if (volume != defaultVolume)
		{
			file.write("    Volume: ");
			file.writeLine(String.valueOf(volume));
		}

		if (placement != defaultPlacement)
		{
			if (null == placement)
				file.writeLine("    ClearPlacement.");
			else
			{
				file.write("    Placement: ");
				file.writeLine(placement.originalName);
			}
		}
	}

	@Override public boolean processProperty(String line)
	{
		if (super.processProperty(line)) return true;

		if (Holder.startsWith(line, "Weight: "))
		{
			weight = Double.parseDouble(Holder.getParamString());
			return true;
		}

		if (Holder.startsWith(line, "Volume: "))
		{
			volume = Double.parseDouble(Holder.getParamString());
			return true;
		}

		if (Holder.startsWith(line, "ClearPlacement."))
		{
			placement = null;
			return true;
		}

		if (Holder.startsWith(line, "Placement: "))
		{
			return null != (placement = engine.
				getPlacement(Holder.getParamString()));
		}

		return false;
	}
}
